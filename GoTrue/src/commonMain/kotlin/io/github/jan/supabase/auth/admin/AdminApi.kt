package io.github.jan.supabase.auth.admin

import io.github.jan.supabase.auth.GoTrue
import io.github.jan.supabase.auth.checkErrors
import io.github.jan.supabase.auth.currentAccessToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.supabaseJson
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/**
 * The admin interface for the supabase auth module. Service role access token is required. Import it via [GoTrue.importAuthToken]. Never share it publicly
 */
sealed interface AdminApi {

    /**
     * Creates a new user using an email and password
     * @return the newly created user
     */
    suspend fun createUserWithEmail(builder: UserBuilder.Email.() -> Unit): UserInfo

    /**
     * Creates a new user using a phone number and password
     * @return the newly created user
     */
    suspend fun createUserWithPhone(builder: UserBuilder.Phone.() -> Unit): UserInfo

    /**
     * Retrieves all users
     */
    suspend fun retrieveUsers(): List<UserInfo>

    /**
     * Retrieves a user by its id
     * @param uid the id of the user
     */
    suspend fun retrieveUserById(uid: String): UserInfo

    /**
     * Deletes a user by its id
     * @param uid the id of the user
     */
    suspend fun deleteUser(uid: String)

    /**
     * Invites a user by their email
     * @param email the email of the user
     * @param redirectTo the url to redirect to after the user has confirmed the invite
     * @param data optional user metadata
     */
    suspend fun inviteUserByEmail(email: String, redirectTo: String? = null, data: JsonObject? = null)

    /**
     * Updates a user by its id
     * @param uid the id of the user
     */
    suspend fun updateUserById(uid: String, builder: UserUpdateBuilder.() -> Unit): UserInfo

}

@PublishedApi
internal class AdminApiImpl(val auth: GoTrue) : AdminApi {

    override suspend fun createUserWithEmail(builder: UserBuilder.Email.() -> Unit): UserInfo {
        val userBuilder = UserBuilder.Email().apply(builder)
        return auth.supabaseClient.httpClient.post(auth.resolveUrl("/admin/users")) {
            contentType(ContentType.Application.Json)
            setBody(userBuilder as UserBuilder)
            addAuthorization()
        }.checkErrors().body()
    }

    override suspend fun createUserWithPhone(builder: UserBuilder.Phone.() -> Unit): UserInfo {
        val userBuilder = UserBuilder.Phone().apply(builder)
        return auth.supabaseClient.httpClient.post(auth.resolveUrl("/admin/users")) {
            contentType(ContentType.Application.Json)
            setBody(userBuilder as UserBuilder)
            addAuthorization()
        }.checkErrors().body()
    }

    override suspend fun retrieveUsers(): List<UserInfo> {
        return auth.supabaseClient.httpClient.get(auth.resolveUrl("/admin/users")) {
            addAuthorization()
        }.checkErrors().body<JsonObject>().let { supabaseJson.decodeFromJsonElement(it["users"] ?: throw IllegalStateException("Didn't get users json field on method retrieveUsers. Full body: $it")) }
    }

    override suspend fun retrieveUserById(uid: String): UserInfo {
        return auth.supabaseClient.httpClient.get(auth.resolveUrl("/admin/users/$uid")) {
            addAuthorization()
        }.checkErrors().body()
    }

    override suspend fun deleteUser(uid: String) {
        auth.supabaseClient.httpClient.delete(auth.resolveUrl("/admin/users/$uid")) {
            addAuthorization()
        }.checkErrors()
    }

    override suspend fun inviteUserByEmail(email: String, redirectTo: String?, data: JsonObject?) {
        val redirect = redirectTo?.let { "?redirect_to=$it" } ?: ""
        auth.supabaseClient.httpClient.post(auth.resolveUrl("/invite$redirect")) {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("email", email)
                data?.let { put("data", it) }
            })
            addAuthorization()
        }.checkErrors()
    }

    override suspend fun updateUserById(uid: String, builder: UserUpdateBuilder.() -> Unit): UserInfo {
        val updateBuilder = UserUpdateBuilder().apply(builder)
        return auth.supabaseClient.httpClient.put(auth.resolveUrl("/admin/users/$uid")) {
            contentType(ContentType.Application.Json)
            setBody(updateBuilder)
            addAuthorization()
        }.checkErrors().body()
    }

    fun HttpRequestBuilder.addAuthorization() {
        header(HttpHeaders.Authorization, "Bearer ${auth.currentAccessToken() ?: tokenException()}")
    }

    private fun tokenException(): Nothing = throw IllegalStateException("You need the service role access token to use admin methods. Use Auth#importAuthToken to import it. Never share it publicly")

}

/**
 * Generates a link for [linkType]
 *
 * Example:
 * ```
 * val (link, user) = generateLinkFor(LinkType.MagicLink) {
 *    email = "example@foo.bar
 * }
 *```
 * @param linkType the type of the link. E.g. [LinkType.MagicLink]
 * @param redirectTo the url to redirect to after the user has clicked the link
 * @param config additional configuration required for [linkType]
 */
suspend inline fun <reified C : LinkType.Config> AdminApi.generateLinkFor(
    linkType: LinkType<C>,
    redirectTo: String? = null,
    noinline config: C.() -> Unit
): Pair<String, UserInfo> {
    this as AdminApiImpl
    val generatedConfig = linkType.createConfig(config)
    val redirect = redirectTo?.let { "?redirect_to=$redirectTo" } ?: ""
    val user = auth.supabaseClient.httpClient.post(auth.resolveUrl("/admin/generate_link$redirect")) {
        contentType(ContentType.Application.Json)
        setBody(buildJsonObject {
            put("type", linkType.type)
            putJsonObject(Json.encodeToJsonElement(generatedConfig).jsonObject)
        })
        addAuthorization()
    }.checkErrors().body<UserInfo>();
    return user.actionLink!! to user
}
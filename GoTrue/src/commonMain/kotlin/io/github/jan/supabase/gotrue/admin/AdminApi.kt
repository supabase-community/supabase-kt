package io.github.jan.supabase.gotrue.admin

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.GoTrueImpl
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserMfaFactor
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.supabaseJson
import io.ktor.client.call.body
import io.ktor.http.HttpHeaders
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
    suspend fun createUserWithEmail(builder: AdminUserBuilder.Email.() -> Unit): UserInfo

    /**
     * Creates a new user using a phone number and password
     * @return the newly created user
     */
    suspend fun createUserWithPhone(builder: AdminUserBuilder.Phone.() -> Unit): UserInfo

    /**
     * Removes a user session
     */
    suspend fun logout(jwt: String)

    /**
     * Retrieves all users
     */
    suspend fun retrieveUsers(page: Int? = null, perPage: Int? = null): List<UserInfo>

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
    suspend fun updateUserById(uid: String, builder: AdminUserUpdateBuilder.() -> Unit): UserInfo

    /**
     * Retrieves all MFA factors of a user
     * @param uid the id of the user
     * @return A list of all MFA factors
     */
    suspend fun retrieveFactors(uid: String): List<UserMfaFactor>

    /**
     * Deletes a verified MFA factor of a user
     * @param uid the id of the user
     * @param factorId the id of the factor
     */
    suspend fun deleteFactor(uid: String, factorId: String)

}

@PublishedApi
internal class AdminApiImpl(val gotrue: GoTrue) : AdminApi {

    val api = (gotrue as GoTrueImpl).api

    override suspend fun logout(jwt: String) {
        api.post("logout") {
            headers[HttpHeaders.Authorization] = "Bearer $jwt"
        }
    }

    override suspend fun createUserWithEmail(builder: AdminUserBuilder.Email.() -> Unit): UserInfo {
        val userBuilder = AdminUserBuilder.Email().apply(builder) as AdminUserBuilder
        return api.postJson("admin/users", userBuilder).safeBody()
    }

    override suspend fun createUserWithPhone(builder: AdminUserBuilder.Phone.() -> Unit): UserInfo {
        val userBuilder = AdminUserBuilder.Phone().apply(builder) as AdminUserBuilder
        return api.postJson("admin/users", userBuilder).safeBody()
    }

    override suspend fun retrieveUsers(page: Int?, perPage: Int?): List<UserInfo> {
        return api.get("admin/users") {
            page?.let { url.parameters.append("page", it.toString()) }
            perPage?.let { url.parameters.append("per_page", it.toString()) }
        }.body<JsonObject>().let { supabaseJson.decodeFromJsonElement(it["users"] ?: error("Didn't get users json field on method retrieveUsers. Full body: $it")) }
    }

    override suspend fun retrieveUserById(uid: String): UserInfo {
        return api.get("admin/users/$uid").safeBody()
    }

    override suspend fun deleteUser(uid: String) {
        api.delete("admin/users/$uid")
    }

    override suspend fun inviteUserByEmail(email: String, redirectTo: String?, data: JsonObject?) {
        val body = buildJsonObject {
            put("email", email)
            data?.let { put("data", it) }
        }
        api.postJson("invite", body) { redirectTo?.let { url.parameters.append("redirect_to", it) }}
    }

    override suspend fun updateUserById(uid: String, builder: AdminUserUpdateBuilder.() -> Unit): UserInfo {
        val updateBuilder = AdminUserUpdateBuilder().apply(builder)
        return api.putJson("admin/users/$uid", updateBuilder).safeBody()
    }

    override suspend fun deleteFactor(uid: String, factorId: String) {
        api.delete("admin/users/$uid/factors/$factorId")
    }

    override suspend fun retrieveFactors(uid: String): List<UserMfaFactor> {
        return api.get("admin/users/$uid/factors").safeBody()
    }

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
@OptIn(SupabaseInternal::class)
suspend inline fun <reified C : LinkType.Config> AdminApi.generateLinkFor(
    linkType: LinkType<C>,
    redirectTo: String? = null,
    noinline config: C.() -> Unit
): Pair<String, UserInfo> {
    this as AdminApiImpl
    val generatedConfig = linkType.createConfig(config)
    val body = buildJsonObject {
        put("type", linkType.type)
        putJsonObject(Json.encodeToJsonElement(generatedConfig).jsonObject)
    }
    val user = api.postJson("admin/generate_link", body) { redirectTo?.let { url.parameters.append("redirect_to", it) }}.body<UserInfo>()
    return user.actionLink!! to user
}
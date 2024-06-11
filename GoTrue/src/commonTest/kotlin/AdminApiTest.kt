import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SignOutScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.minimalSettings
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.testing.assertMethodIs
import io.github.jan.supabase.testing.assertPathIs
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.respondJson
import io.github.jan.supabase.testing.toJsonElement
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class AdminApiTest {

    private val configuration: SupabaseClientBuilder.() -> Unit = {
        install(Auth) {
            minimalSettings()
        }
    }

    @Test
    fun testSignOut() {
        runTest {
            val jwt = "jwt"
            val scope = SignOutScope.LOCAL
            val client = createMockedSupabaseClient(
                configuration = configuration
            ) {
                assertPathIs("/logout", it.url.pathAfterVersion())
                assertMethodIs(HttpMethod.Post, it.method)
                val params = it.url.parameters
                val headers = it.headers
                assertEquals(scope.name.lowercase(), params["scope"])
                assertEquals("Bearer $jwt", headers["Authorization"])
                respond("")
            }
            client.auth.admin.signOut(
                jwt = jwt,
                scope = scope
            )
        }
    }

    @Test
    fun testCreateUserWithEmail() {
        runTest {
            val email = "email"
            val password = "password"
            val userMetadata = buildJsonObject {
                put("type", "user")
            }
            val appMetadata = buildJsonObject {
                put("type", "app")
            }
            val autoConfirm = true
            val client = createMockedSupabaseClient(
                configuration = configuration
            ) {
                assertPathIs("/admin/users", it.url.pathAfterVersion())
                assertMethodIs(HttpMethod.Post, it.method)
                val body = it.body.toJsonElement().jsonObject
                assertEquals(email, body["email"]?.jsonPrimitive?.content)
                assertEquals(password, body["password"]?.jsonPrimitive?.content)
                assertEquals(userMetadata, body["user_metadata"]?.jsonObject)
                assertEquals(appMetadata, body["app_metadata"]?.jsonObject)
                assertEquals(autoConfirm, body["email_confirm"]?.jsonPrimitive?.boolean)
                respondJson(
                    Json.encodeToString(UserInfo(
                        id = "id",
                        email = email,
                        appMetadata = appMetadata,
                        userMetadata = userMetadata,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now(),
                        confirmedAt = Clock.System.now(),
                        aud = "aud",
                    ))
                )
            }
            val newUser = client.auth.admin.createUserWithEmail {
                this.email = email
                this.password = password
                this.userMetadata = userMetadata
                this.appMetadata = appMetadata
                this.autoConfirm = autoConfirm
            }
            assertEquals(email, newUser.email)
            assertEquals(appMetadata, newUser.appMetadata)
            assertEquals(userMetadata, newUser.userMetadata)
        }
    }

    @Test
    fun testCreateUserWithPhone() {
        runTest {
            val phone = "+123456789"
            val password = "password"
            val userMetadata = buildJsonObject {
                put("type", "user")
            }
            val appMetadata = buildJsonObject {
                put("type", "app")
            }
            val autoConfirm = true
            val client = createMockedSupabaseClient(
                configuration = configuration
            ) {
                assertPathIs("/admin/users", it.url.pathAfterVersion())
                assertMethodIs(HttpMethod.Post, it.method)
                val body = it.body.toJsonElement().jsonObject
                assertEquals(phone, body["phone"]?.jsonPrimitive?.content)
                assertEquals(password, body["password"]?.jsonPrimitive?.content)
                assertEquals(userMetadata, body["user_metadata"]?.jsonObject)
                assertEquals(appMetadata, body["app_metadata"]?.jsonObject)
                assertEquals(autoConfirm, body["phone_confirm"]?.jsonPrimitive?.boolean)
                respondJson(
                    Json.encodeToString(UserInfo(
                        id = "id",
                        phone = phone,
                        appMetadata = appMetadata,
                        userMetadata = userMetadata,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now(),
                        confirmedAt = Clock.System.now(),
                        aud = "aud",
                    ))
                )
            }
            val newUser = client.auth.admin.createUserWithPhone {
                this.phone = phone
                this.password = password
                this.userMetadata = userMetadata
                this.appMetadata = appMetadata
                this.autoConfirm = autoConfirm
            }
            assertEquals(phone, newUser.phone)
            assertEquals(appMetadata, newUser.appMetadata)
            assertEquals(userMetadata, newUser.userMetadata)
        }
    }

    @Test
    fun testRetrieveUsers() {
        runTest {
            val page = 3
            val perPage = 50
            val client = createMockedSupabaseClient(
                configuration = configuration
            ) {
                assertPathIs("/admin/users", it.url.pathAfterVersion())
                assertMethodIs(HttpMethod.Get, it.method)
                val params = it.url.parameters
                assertEquals(page.toString(), params["page"])
                assertEquals(perPage.toString(), params["per_page"])
                respondJson(
                    buildJsonObject {
                        put("users",  Json.encodeToJsonElement(
                            listOf(
                                UserInfo(
                                    id = "id",
                                    email = "email",
                                    appMetadata = null,
                                    userMetadata = null,
                                    createdAt = Clock.System.now(),
                                    updatedAt = Clock.System.now(),
                                    confirmedAt = Clock.System.now(),
                                    aud = "aud",
                                )
                            )
                        ))
                    }
                )
            }
            val users = client.auth.admin.retrieveUsers(page = page, perPage = perPage)
            assertEquals(1, users.size)
            assertEquals("email", users.first().email)
        }
    }

}
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.respondJson
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertIsNot

class AuthRestExceptionTest {

    private val configuration: SupabaseClientBuilder.() -> Unit = {
        install(Auth) {
            minimalConfig()
        }
    }

    @Test
    fun testErrorsWithErrorCode() {
        runTest {
            val client = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    respondJson(
                        code = HttpStatusCode.BadRequest,
                        data = buildJsonObject {
                            put("error_code", "error_code")
                            put("message", "error_message")
                        }
                    )
                }
            )
            val exception = assertFailsWith<AuthRestException> {
                client.auth.signUpWith(Email) {
                    email = "example@email.com"
                    password = "password"
                }
            }
            assertEquals("error_code", exception.error)
            assertEquals("error_message", exception.errorDescription)
        }
    }

    @Test
    fun testPasswordWeakAuthRestException() {
        runTest {
            val client = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    respondJson(
                        code = HttpStatusCode.BadRequest,
                        data = buildJsonObject {
                            put("error_code", "weak_password")
                            put("message", "error_message")
                            put("weak_password", buildJsonObject {
                                putJsonArray("reasons") {
                                    add("reason1")
                                    add("reason2")
                                }
                            })
                        }
                    )
                }
            )
            val exception = assertFailsWith<AuthWeakPasswordException> {
                client.auth.signUpWith(Email) {
                    email = "example@email.com"
                    password = "password"
                }
            }
            assertEquals("weak_password", exception.error)
            assertEquals("error_message", exception.errorDescription)
            assertEquals(listOf("reason1", "reason2"), exception.reasons)
        }
    }

    @Test
    fun testErrorsWithoutErrorCode() {
        runTest {
            val client = createMockedSupabaseClient(
                configuration = configuration,
                requestHandler = {
                    respondJson(
                        code = HttpStatusCode.BadRequest,
                        data = buildJsonObject {
                            put("message", "error_message")
                        }
                    )
                }
            )
            val exception = assertFails {
                client.auth.signUpWith(Email) {
                    email = "example@email.com"
                    password = "password"
                }
            }
            assertIsNot<AuthRestException>(exception)
            assertIs<BadRequestRestException>(exception)
        }
    }

}
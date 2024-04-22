import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.AuthConfig
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.gotrue.PKCEConstants
import io.github.jan.supabase.gotrue.SessionSource
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.minimalSettings
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.gotrue.providers.builtin.Phone
import io.github.jan.supabase.testing.assertMethodIs
import io.github.jan.supabase.testing.assertPathIs
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.toJsonElement
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthTest {

    private val configuration: SupabaseClientBuilder.() -> Unit = {
        install(Auth) {
            minimalSettings()
            flowType = FlowType.PKCE
        }
    }

    @Test
    fun testSignUpWithEmailNoAutoconfirm() {
        runTest {
            val expectedEmail = "example@email.com"
            val expectedPassword = "password"
            val captchaToken = "captchaToken"
            val userData = buildJsonObject {
                put("key", "value")
            }
            val expectedUrl = "https://example.com"
            val client = createMockedSupabaseClient(configuration = configuration) {
                val body = it.body.toJsonElement().jsonObject
                val metaSecurity = body["gotrue_meta_security"]!!.jsonObject
                val params = it.url.parameters
                assertEquals(expectedUrl, params["redirect_to"])
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/signup", it.url.pathAfterVersion())
                assertEquals(expectedEmail, body["email"]?.jsonPrimitive?.content)
                assertEquals(expectedPassword, body["password"]?.jsonPrimitive?.content)
                assertEquals(captchaToken, metaSecurity["captcha_token"]?.jsonPrimitive?.content)
                assertEquals(userData, body["data"]!!.jsonObject)
                containsCodeChallenge(body)
                respond(
                    sampleUserObject(email = expectedEmail),
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
            val user = client.auth.signUpWith(Email, redirectUrl = expectedUrl) {
                email = expectedEmail
                password = expectedPassword
                this.captchaToken = captchaToken
                data = userData
            }
            assertEquals(expectedEmail, user?.email, "Email should be equal")
        }
    }

    @Test
    fun testSignUpWithEmailAutoconfirm() {
        runTest {
            val expectedEmail = "example@email.com"
            val expectedPassword = "password"
            val captchaToken = "captchaToken"
            val userData = buildJsonObject {
                put("key", "value")
            }
            val client = createMockedSupabaseClient(configuration = configuration) {
                val body = it.body.toJsonElement().jsonObject
                val metaSecurity = body["gotrue_meta_security"]!!.jsonObject
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/signup", it.url.pathAfterVersion())
                assertEquals(expectedEmail, body["email"]?.jsonPrimitive?.content)
                assertEquals(expectedPassword, body["password"]?.jsonPrimitive?.content)
                assertEquals(captchaToken, metaSecurity["captcha_token"]?.jsonPrimitive?.content)
                assertEquals(userData, body["data"]!!.jsonObject)
                containsCodeChallenge(body)
                respond(
                    sampleUserSession(),
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
            val user = client.auth.signUpWith(Email) {
                email = expectedEmail
                password = expectedPassword
                this.captchaToken = captchaToken
                data = userData
            }
            assertNull(user)
            assertNotNull(client.auth.currentSessionOrNull(), "Session should not be null")
            assertEquals(client.auth.sessionSource(), SessionSource.SignUp(Email))
        }
    }

    @Test
    fun testSignUpWithPhoneAutoconfirm() {
        runTest {
            val expectedPhone = "+1234567890"
            val expectedPassword = "password"
            val captchaToken = "captchaToken"
            val userData = buildJsonObject {
                put("key", "value")
            }
            val client = createMockedSupabaseClient(configuration = configuration) {
                val body = it.body.toJsonElement().jsonObject
                val metaSecurity = body["gotrue_meta_security"]!!.jsonObject
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/signup", it.url.pathAfterVersion())
                assertEquals(expectedPhone, body["phone"]?.jsonPrimitive?.content)
                assertEquals(expectedPassword, body["password"]?.jsonPrimitive?.content)
                assertEquals(captchaToken, metaSecurity["captcha_token"]?.jsonPrimitive?.content)
                assertEquals(userData, body["data"]!!.jsonObject)
                containsCodeChallenge(body)
                respond(
                    sampleUserSession(),
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
            val user = client.auth.signUpWith(Phone) {
                phone = expectedPhone
                password = expectedPassword
                this.captchaToken = captchaToken
                data = userData
            }
            assertNull(user)
            assertNotNull(client.auth.currentSessionOrNull(), "Session should not be null")
            assertEquals(client.auth.sessionSource(), SessionSource.SignUp(Phone))
        }
    }

    @Test
    fun testSignUpWithPhoneNoAutoconfirm() {
        runTest {
            val expectedPhone = "+1234567890"
            val expectedPassword = "password"
            val captchaToken = "captchaToken"
            val userData = buildJsonObject {
                put("key", "value")
            }
            val expectedUrl = "https://example.com"
            val client = createMockedSupabaseClient(configuration = configuration) {
                val body = it.body.toJsonElement().jsonObject
                val metaSecurity = body["gotrue_meta_security"]!!.jsonObject
                val params = it.url.parameters
                assertEquals(expectedUrl, params["redirect_to"])
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/signup", it.url.pathAfterVersion())
                assertEquals(expectedPhone, body["phone"]?.jsonPrimitive?.content)
                assertEquals(expectedPassword, body["password"]?.jsonPrimitive?.content)
                assertEquals(captchaToken, metaSecurity["captcha_token"]?.jsonPrimitive?.content)
                assertEquals(userData, body["data"]!!.jsonObject)
                containsCodeChallenge(body)
                respond(
                    sampleUserObject(phone = expectedPhone),
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
            val user = client.auth.signUpWith(Phone, redirectUrl = expectedUrl) {
                phone = expectedPhone
                password = expectedPassword
                this.captchaToken = captchaToken
                data = userData
            }
            assertEquals(expectedPhone, user?.phone, "Phone should be equal")
        }
    }

    @Test
    fun testSignUpOtpWithPhone() {
        runTest {
            val expectedPhone = "+1234567890"
            val captchaToken = "captchaToken"
            val userData = buildJsonObject {
                put("key", "value")
            }
            val expectedUrl = "https://example.com"
            val client = createMockedSupabaseClient(configuration = configuration) {
                val body = it.body.toJsonElement().jsonObject
                val metaSecurity = body["gotrue_meta_security"]!!.jsonObject
                val params = it.url.parameters
                assertEquals(expectedUrl, params["redirect_to"])
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/otp", it.url.pathAfterVersion())
                assertEquals(expectedPhone, body["phone"]?.jsonPrimitive?.content)
                assertEquals(captchaToken, metaSecurity["captcha_token"]?.jsonPrimitive?.content)
                assertEquals(userData, body["data"]!!.jsonObject)
                containsCodeChallenge(body)
                respond("")
            }
            client.auth.signUpWith(OTP, redirectUrl = expectedUrl) {
                phone = expectedPhone
                this.captchaToken = captchaToken
                data = userData
            }
        }
    }

    @Test
    fun testSignUpOtpWithEmail() {
        runTest {
            val expectedEmail = "example@email.com"
            val captchaToken = "captchaToken"
            val userData = buildJsonObject {
                put("key", "value")
            }
            val expectedUrl = "https://example.com"
            val client = createMockedSupabaseClient(configuration = configuration) {
                val body = it.body.toJsonElement().jsonObject
                val metaSecurity = body["gotrue_meta_security"]!!.jsonObject
                val params = it.url.parameters
                assertEquals(expectedUrl, params["redirect_to"])
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/otp", it.url.pathAfterVersion())
                assertEquals(expectedEmail, body["email"]?.jsonPrimitive?.content)
                assertEquals(captchaToken, metaSecurity["captcha_token"]?.jsonPrimitive?.content)
                assertEquals(userData, body["data"]!!.jsonObject)
                containsCodeChallenge(body)
                respond("")
            }
            client.auth.signUpWith(OTP, redirectUrl = expectedUrl) {
                email = expectedEmail
                this.captchaToken = captchaToken
                data = userData
            }
        }
    }

    @Test
    fun testSignInWithIDToken() {
        runTest {
            val captchaToken = "captchaToken"
            val userData = buildJsonObject {
                put("key", "value")
            }
            val expectedIdToken = "idToken"
            val expectedProvider = Google
            val expectedAccessToken = "accessToken"
            val expectedNonce = "nonce"
            val client = createMockedSupabaseClient(configuration = configuration) {
                val body = it.body.toJsonElement().jsonObject
                val metaSecurity = body["gotrue_meta_security"]!!.jsonObject
                val params = it.url.parameters
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/token", it.url.pathAfterVersion())
                assertEquals("id_token", params["grant_type"])
                assertEquals(captchaToken, metaSecurity["captcha_token"]?.jsonPrimitive?.content)
                assertEquals(userData, body["data"]!!.jsonObject)
                assertEquals(expectedIdToken, body["id_token"]?.jsonPrimitive?.content)
                assertEquals(expectedProvider.name, body["provider"]?.jsonPrimitive?.content)
                assertEquals(expectedAccessToken, body["access_token"]?.jsonPrimitive?.content)
                assertEquals(expectedNonce, body["nonce"]?.jsonPrimitive?.content)
                containsCodeChallenge(body)
                respond(
                    sampleUserSession(),
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
            client.auth.signInWith(IDToken) {
                this.captchaToken = captchaToken
                data = userData
                this.idToken = expectedIdToken
                provider = expectedProvider
                this.nonce = expectedNonce
                accessToken = expectedAccessToken
            }
            assertNotNull(client.auth.currentSessionOrNull(), "Session should not be null")
        }
    }

    private fun sampleUserObject(email: String? = null, phone: String? = null) = """
        {
            "id": "id",
            "aud": "aud",
            "email": "$email",
            "phone": "$phone"
        }
    """.trimIndent()

    private fun sampleUserSession() = """
        {
        "access_token": "token",
        "refresh_token": "refresh",
        "token_type": "bearer",
        "expires_in": 3600
        }
    """.trimIndent()

    private fun containsCodeChallenge(body: JsonObject) {
        assertNotNull(body["code_challenge"])
        assertEquals(PKCEConstants.CHALLENGE_METHOD, body["code_challenge_method"]?.jsonPrimitive?.content)
    }

    private fun Auth.sessionSource() = (sessionStatus.value as SessionStatus.Authenticated).source

    private inline fun <reified T> Auth.sessionSourceAs() = sessionSource() as T

}

expect fun AuthConfig.platformSettings()
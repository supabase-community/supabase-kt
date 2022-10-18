import com.russhwolf.settings.MapSettings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class GoTrueTest {

    val mockEngine = GoTrueMock().engine

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_login_with_email_with_wrong_credentials() {
        val client = noSessionSupabaseClient()
        runTest {
            assertFailsWith<RestException>("Login with email and wrong password should fail") {
                client.gotrue.loginWith(Email, "https://website.com") {
                    email = "email@example.com"
                    password = "wrong_password"
                }
            }
            client.close()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_loading_session_from_storage() {
        val client = sessionSupabaseClient()
        assertNotNull(client.gotrue.currentSession.value)
        runTest { client.close() }
    }

    fun sessionSupabaseClient(expiresIn: Long = 7) = createSupabaseClient(
        supabaseUrl = "https://example.com",
        supabaseKey = "example",
    ) {
        httpEngine = mockEngine

        install(GoTrue) {
            sessionManager = SettingsSessionManager(MapSettings(
                "session" to Json.encodeToString(UserSession("token", "refresh_token", expiresIn, "type", null))
            ))
        }
    }

    private fun noSessionSupabaseClient() = createSupabaseClient(
        supabaseUrl = "https://example.com",
        supabaseKey = "example",
    ) {
        httpEngine = mockEngine

        install(GoTrue) {
            alwaysAutoRefresh = false
            autoLoadFromStorage = false
        }
    }

}
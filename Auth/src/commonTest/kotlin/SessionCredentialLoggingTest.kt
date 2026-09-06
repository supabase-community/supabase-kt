import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.parseSessionFromFragment
import io.github.jan.supabase.StringMasking
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.logging.SupabaseLoggingProcessor
import io.github.jan.supabase.testing.createMockedSupabaseClient
import kotlinx.coroutines.test.runTest
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val SECRET_ACCESS_TOKEN = "SECRET-ACCESS-TOKEN-fd8a1c2e"
private const val SECRET_REFRESH_TOKEN = "SECRET-REFRESH-TOKEN-90bb4d17"
private const val SECRET_PROVIDER_TOKEN = "SECRET-PROVIDER-TOKEN-4471aa02"
private const val SECRET_PROVIDER_REFRESH_TOKEN = "SECRET-PROVIDER-REFRESH-TOKEN-c0de11"

private val ALL_SECRETS = listOf(
    SECRET_ACCESS_TOKEN,
    SECRET_REFRESH_TOKEN,
    SECRET_PROVIDER_TOKEN,
    SECRET_PROVIDER_REFRESH_TOKEN,
)

/**
 * Auth logs from a background coroutine during `init`, so the recorder has to tolerate concurrent
 * appends while the test reads. An immutable list behind a CAS loop keeps reads snapshot-consistent.
 */
@OptIn(ExperimentalAtomicApi::class)
private class RecordingLoggingProcessor : SupabaseLoggingProcessor {

    private val recorded = AtomicReference<List<String>>(emptyList())

    val messages: List<String> get() = recorded.load()

    override fun isEnabled(level: LogLevel) = true

    override fun processLog(level: LogLevel, tag: String, throwable: Throwable?, message: String) {
        while (true) {
            val current = recorded.load()
            if (recorded.compareAndSet(current, current + message)) return
        }
    }

}

private fun secretSession(expiresIn: Long = 3600) = UserSession(
    accessToken = SECRET_ACCESS_TOKEN,
    refreshToken = SECRET_REFRESH_TOKEN,
    providerToken = SECRET_PROVIDER_TOKEN,
    providerRefreshToken = SECRET_PROVIDER_REFRESH_TOKEN,
    expiresIn = expiresIn,
    tokenType = "Bearer",
    user = null,
)

/**
 * Regression tests for the credential-in-logs defect.
 *
 * `UserSession` is a data class, so before the fix its synthesized `toString()` printed all four
 * bearer credentials verbatim, and `AuthImpl.importSession` interpolated it into a DEBUG log line.
 * Platform logs are routinely collected by crash/analytics reporters, which put a long-lived
 * refresh token outside the app's trust boundary.
 */
class SessionCredentialLoggingTest {

    @Test
    fun testToStringMasksEveryCredential() {
        val rendered = secretSession().toString()
        ALL_SECRETS.forEach {
            assertFalse(rendered.contains(it), "UserSession.toString() leaked $it")
        }
        // Still useful for debugging: prefix and length survive.
        assertContains(rendered, "SE... (len=${SECRET_ACCESS_TOKEN.length})")
    }

    @Test
    fun testSessionSourceHoldingASessionDoesNotLeak() {
        // SessionSource.Refresh is a data class wrapping a UserSession, so its toString delegates
        // to UserSession.toString(). This is the transitive path that AuthImpl interpolates as
        // `$source`.
        val rendered = SessionSource.Refresh(secretSession()).toString()
        ALL_SECRETS.forEach {
            assertFalse(rendered.contains(it), "SessionSource.toString() leaked $it")
        }
    }

    @Test
    fun testImportSessionDoesNotLogCredentials() = runTest {
        val recorder = RecordingLoggingProcessor()
        val client = createMockedSupabaseClient(configuration = {
            defaultLogLevel = LogLevel.DEBUG
            defaultLoggingFactory = { recorder }
            install(Auth) { minimalConfig() }
        })
        try {
            client.auth.importSession(secretSession())
            assertTrue(recorder.messages.isNotEmpty(), "expected the DEBUG path to actually log")
            recorder.messages.forEach { message ->
                ALL_SECRETS.forEach { secret ->
                    assertFalse(message.contains(secret), "log line leaked $secret: $message")
                }
            }
        } finally {
            client.close()
        }
    }

    @Test
    fun testParseSessionFromFragmentDoesNotLogCredentials() = runTest {
        val recorder = RecordingLoggingProcessor()
        val client = createMockedSupabaseClient(configuration = {
            defaultLogLevel = LogLevel.DEBUG
            defaultLoggingFactory = { recorder }
            install(Auth) { minimalConfig() }
        })
        try {
            val fragment = "access_token=$SECRET_ACCESS_TOKEN" +
                    "&refresh_token=$SECRET_REFRESH_TOKEN" +
                    "&provider_token=$SECRET_PROVIDER_TOKEN" +
                    "&expires_in=3600" +
                    "&token_type=bearer"
            client.auth.parseSessionFromFragment(fragment)
            recorder.messages.forEach { message ->
                ALL_SECRETS.forEach { secret ->
                    assertFalse(message.contains(secret), "log line leaked $secret: $message")
                }
            }
        } finally {
            client.close()
        }
    }

    /**
     * `Auth.tryToGetUser` logs at ERROR, and ERROR is enabled under the default `LogLevel.INFO`
     * (`isEnabled` is `minSeverity <= level`, and DEBUG < INFO < WARNING < ERROR). An unmasked token
     * there reached the platform log in a stock configuration -- no debug logging required.
     */
    @Test
    fun testMaskStringIsUsedForRawAccessTokens() {
        val masked = StringMasking.maskString(SECRET_ACCESS_TOKEN, showLength = true)
        assertFalse(masked.contains(SECRET_ACCESS_TOKEN))
        assertContains(masked, "(len=${SECRET_ACCESS_TOKEN.length})")
    }

}

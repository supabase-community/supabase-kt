import com.russhwolf.settings.MapSettings
import io.github.jan.supabase.gotrue.SettingsCodeVerifierCache
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsCodeVerifierCacheTest {

    @Test
    fun testMemoryCodeVerifierCache() {
        runTest {
            val codeVerifier = "codeVerifier"
            val codeVerifierCache = SettingsCodeVerifierCache(MapSettings(SettingsCodeVerifierCache.SETTINGS_KEY to codeVerifier))
            assertEquals(codeVerifier, codeVerifierCache.loadCodeVerifier())
            val newCodeVerifier = "newCodeVerifier"
            codeVerifierCache.saveCodeVerifier(newCodeVerifier)
            assertEquals(newCodeVerifier, codeVerifierCache.loadCodeVerifier())
            codeVerifierCache.deleteCodeVerifier()
            assertNull(codeVerifierCache.loadCodeVerifier())
        }
    }

}
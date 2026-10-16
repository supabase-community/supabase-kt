import com.russhwolf.settings.MapSettings
import io.github.jan.supabase.auth.PKCEConstants
import io.github.jan.supabase.auth.SettingsCodeVerifierCache
import io.github.jan.supabase.auth.pkceLegacyKey
import io.github.jan.supabase.auth.pkceVerifierSlotKey
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsCodeVerifierCacheTest {

    @Test
    fun testStorePKCEVerifier() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            val flowId = "flow-1"
            val verifier = "testVerifier123"
            
            cache.storePKCEVerifier(flowId, verifier)
            assertEquals(verifier, cache.retrievePKCEVerifier(flowId))
            assertEquals(verifier, settings.getString(pkceVerifierSlotKey(SettingsCodeVerifierCache.SETTINGS_KEY, flowId), ""))
        }
    }

    @Test
    fun testRetrievePKCEVerifier() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            val flowId = "flow-1"
            val verifier = "testVerifier123"
            
            cache.storePKCEVerifier(flowId, verifier)
            val retrieved = cache.retrievePKCEVerifier(flowId)
            assertEquals(verifier, retrieved)
        }
    }

    @Test
    fun testRetrieveNonExistentFlow() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            val retrieved = cache.retrievePKCEVerifier("non-existent")
            assertNull(retrieved)
        }
    }

    @Test
    fun testLegacyFallback() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            val legacyVerifier = "legacyVerifier"
            
            settings.putString(pkceLegacyKey(SettingsCodeVerifierCache.SETTINGS_KEY), legacyVerifier)
            
            val retrieved = cache.retrievePKCEVerifier(null)
            assertEquals(legacyVerifier, retrieved)
        }
    }

    @Test
    fun testRemovePKCEVerifier() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            val flowId = "flow-1"
            val verifier = "testVerifier123"
            
            cache.storePKCEVerifier(flowId, verifier)
            assertEquals(verifier, cache.retrievePKCEVerifier(flowId))
            cache.removePKCEVerifier(flowId)
            assertNull(cache.retrievePKCEVerifier(flowId))
        }
    }

    @Test
    fun testRemoveAllPKCEVerifiers() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            
            cache.storePKCEVerifier("flow-1", "verifier1")
            cache.storePKCEVerifier("flow-2", "verifier2")
            cache.storePKCEVerifier("flow-3", "verifier3")
            
            assertEquals("verifier1", cache.retrievePKCEVerifier("flow-1"))
            assertEquals("verifier2", cache.retrievePKCEVerifier("flow-2"))
            assertEquals("verifier3", cache.retrievePKCEVerifier("flow-3"))
            
            cache.removeAllPKCEVerifiers()
            
            assertNull(cache.retrievePKCEVerifier("flow-1"))
            assertNull(cache.retrievePKCEVerifier("flow-2"))
            assertNull(cache.retrievePKCEVerifier("flow-3"))
            
            val flowIndex = cache.getPKCEFlowIndex()
            assertTrue(flowIndex.isEmpty())
        }
    }

    @Test
    fun testFlowIndexTracking() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            
            cache.storePKCEVerifier("flow-1", "verifier1")
            var flowIndex = cache.getPKCEFlowIndex()
            
            cache.storePKCEVerifier("flow-2", "verifier2")
            flowIndex = cache.getPKCEFlowIndex()
            assertEquals("verifier1", cache.retrievePKCEVerifier("flow-1"))
            assertEquals("verifier2", cache.retrievePKCEVerifier("flow-2"))
        }
    }

    @Test
    fun testEvictionWhenMaxFlowsExceeded() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            
            var evictionCount = 0
            val onEvict: (String) -> Unit = { _ -> evictionCount++ }
            
            for (i in 1..PKCEConstants.PKCE_MAX_CONCURRENT_FLOWS) {
                cache.storePKCEVerifier("flow-$i", "verifier$i", onEvict)
            }
            
            assertEquals(0, evictionCount)
            
            for (i in 1..PKCEConstants.PKCE_MAX_CONCURRENT_FLOWS) {
                assertEquals("verifier$i", cache.retrievePKCEVerifier("flow-$i"))
            }
            
            cache.storePKCEVerifier("flow-extra", "verifier-extra", onEvict)
            assertEquals(1, evictionCount)
        }
    }

    @Test
    fun testConcurrentFlows() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            
            val job1 = async {
                cache.storePKCEVerifier("flow-1", "verifier1")
                cache.retrievePKCEVerifier("flow-1")
            }
            
            val job2 = async {
                cache.storePKCEVerifier("flow-2", "verifier2")
                cache.retrievePKCEVerifier("flow-2")
            }
            
            val job3 = async {
                cache.storePKCEVerifier("flow-3", "verifier3")
                cache.retrievePKCEVerifier("flow-3")
            }
            
            assertEquals("verifier1", job1.await())
            assertEquals("verifier2", job2.await())
            assertEquals("verifier3", job3.await())
            
            assertEquals("verifier1", cache.retrievePKCEVerifier("flow-1"))
            assertEquals("verifier2", cache.retrievePKCEVerifier("flow-2"))
            assertEquals("verifier3", cache.retrievePKCEVerifier("flow-3"))
        }
    }

    @Test
    fun testLegacyCompatibilityMaintained() {
        runTest {
            val settings = MapSettings()
            val cache = SettingsCodeVerifierCache(settings)
            val flowId = "flow-1"
            val verifier = "testVerifier123"
            
            cache.storePKCEVerifier(flowId, verifier)
            
            val newKey = pkceVerifierSlotKey(SettingsCodeVerifierCache.SETTINGS_KEY, flowId)
            val legacyKey = pkceLegacyKey(SettingsCodeVerifierCache.SETTINGS_KEY)
            
            assertEquals(verifier, settings.getString(newKey, ""))
            assertEquals(verifier, settings.getString(legacyKey, ""))
        }
    }

}
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CodeVerifierCacheTest {

    @Test
    fun testStorePKCEVerifier() {
        runTest {
            val cache = MemoryCodeVerifierCache()
            val flowId = "flow-1"
            val verifier = "testVerifier123"
            
            cache.storePKCEVerifier(flowId, verifier)
            assertEquals(verifier, cache.retrievePKCEVerifier(flowId))
        }
    }

    @Test
    fun testRetrievePKCEVerifier() {
        runTest {
            val cache = MemoryCodeVerifierCache()
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
            val cache = MemoryCodeVerifierCache()
            val retrieved = cache.retrievePKCEVerifier("non-existent")
            assertNull(retrieved)
        }
    }

    @Test
    fun testRemovePKCEVerifier() {
        runTest {
            val cache = MemoryCodeVerifierCache()
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
            val cache = MemoryCodeVerifierCache()
            
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
        }
    }

    @Test
    fun testConcurrentFlows() {
        runTest {
            val cache = MemoryCodeVerifierCache()
            
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
    fun testMultipleFlowsIndependence() {
        runTest {
            val cache = MemoryCodeVerifierCache()
            
            cache.storePKCEVerifier("flow-1", "verifier1")
            cache.storePKCEVerifier("flow-2", "verifier2")
            
            cache.removePKCEVerifier("flow-1")
            
            assertNull(cache.retrievePKCEVerifier("flow-1"))
            assertEquals("verifier2", cache.retrievePKCEVerifier("flow-2"))
        }
    }

    @Test
    fun testThreadSafetyWithMutex() {
        runTest {
            val cache = MemoryCodeVerifierCache()
            
            val jobs = (1..10).map { index ->
                async {
                    val flowId = "flow-$index"
                    val verifier = "verifier$index"
                    cache.storePKCEVerifier(flowId, verifier)
                    cache.retrievePKCEVerifier(flowId)
                }
            }
            
            val results = jobs.map { it.await() }
            
            results.forEachIndexed { index, result ->
                assertEquals("verifier${index + 1}", result)
            }
        }
    }

}
package io.github.jan.supabase.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicReference

/**
 * A cache for the code verifier used in the PKCE flow.
 */
interface CodeVerifierCache {

    val storageKey: String

    suspend fun storePKCEVerifier(flowId: String, verifier: String, onEvictFlow: (String) -> Unit = {})

    suspend fun retrievePKCEVerifier(flowId: String?): String?

    suspend fun removePKCEVerifier(flowId: String?)

    suspend fun removeAllPKCEVerifiers()

}

/**
 * A [CodeVerifierCache] that uses the [AtomicReference] API.
 */
class MemoryCodeVerifierCache(codeVerifier: String? = null, override val storageKey: String): CodeVerifierCache {

    val verifiers = mutableMapOf<String, String>()
    val mutex = Mutex()

    override suspend fun removeAllPKCEVerifiers() {
        mutex.withLock {
            verifiers.clear()
        }
    }

    override suspend fun removePKCEVerifier(flowId: String?) {
        mutex.withLock {
            verifiers.remove(flowId)
        }
    }

    override suspend fun retrievePKCEVerifier(flowId: String?): String? {
        return mutex.withLock {
            verifiers[flowId]
        }
    }

    override suspend fun storePKCEVerifier(flowId: String, verifier: String, onEvictFlow: (String) -> Unit) {
        mutex.withLock {
            verifiers[flowId] = verifier
        }
    }

}

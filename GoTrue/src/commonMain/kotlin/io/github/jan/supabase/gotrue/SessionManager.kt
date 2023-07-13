package io.github.jan.supabase.gotrue

import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.atomicfu.atomic

/**
 * Represents the session manager. Used for saving and restoring the session from storage
 */
interface SessionManager {

    /**
     * Saves the given session.
     */
    suspend fun saveSession(session: UserSession)

    /**
     * Loads the saved session from storage.
     */
    suspend fun loadSession(): UserSession?

    /**
     * Deletes the saved session from storage.
     */
    suspend fun deleteSession()

}

/**
 * A [SessionManager] that uses the [AtomicMutableMap] API.
 */
class MemorySessionManager: SessionManager {

    private var session by atomic<UserSession?>(null)

    override suspend fun saveSession(session: UserSession) {
        this.session = session
    }

    override suspend fun loadSession(): UserSession? {
        return session
    }

    override suspend fun deleteSession() {
        session = null
    }

}
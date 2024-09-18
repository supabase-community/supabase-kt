package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.user.UserSession
import kotlinx.atomicfu.AtomicRef
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
 * A [SessionManager] that uses the [AtomicRef] API.
 */
class MemorySessionManager(session: UserSession? = null): SessionManager {

    private var session by atomic(session)

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
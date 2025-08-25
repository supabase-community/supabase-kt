package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.user.UserSession
import kotlin.concurrent.atomics.AtomicReference

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
 * A [SessionManager] that uses the [AtomicReference] API.
 */
class MemorySessionManager(session: UserSession? = null): SessionManager {

    private val session = AtomicReference(session)

    override suspend fun saveSession(session: UserSession) {
        this.session.store(session)
    }

    override suspend fun loadSession(): UserSession? {
        return session.load()
    }

    override suspend fun deleteSession() {
        session.store(null)
    }

}
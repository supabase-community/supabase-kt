package io.github.jan.supabase.gotrue

import co.touchlab.kermit.Logger
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.supabaseJson
import io.github.reactivecircus.cache4k.Cache
import kotlinx.serialization.encodeToString

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
 * A [SessionManager] that uses the [Cache] API.
 */
class MemorySessionManager(private val cache: Cache<String, String> = Cache.Builder<String, String>().build()): SessionManager {

    override suspend fun saveSession(session: UserSession) {
        cache.put(SETTINGS_KEY, supabaseJson.encodeToString(session))
    }

    override suspend fun loadSession(): UserSession? {
        val session = cache.get(SETTINGS_KEY) ?: return null
        return try {
            supabaseJson.decodeFromString(session)
        } catch(e: Exception) {
            Logger.e(e) { "Failed to load session" }
            null
        }
    }

    override suspend fun deleteSession() {
        cache.invalidate(SETTINGS_KEY)
    }

    companion object {

        /**
         * The key used for saving the session
         */
        const val SETTINGS_KEY = "session"

    }

}
package io.github.jan.supacompose.auth

import android.content.SharedPreferences
import io.github.aakira.napier.Napier
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.supabaseJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private fun SharedPreferences.Editor.saveSession(session: UserSession) {
    putString("session", supabaseJson.encodeToString(session))
}

private fun SharedPreferences.loadSession(): UserSession? {
    val session = getString("session", null) ?: return null
    return try {
        supabaseJson.decodeFromString(session)
    } catch (e: Exception) {
        Napier.e(e) { "Couldn't load session from shared preferences" }
        null
    }
}
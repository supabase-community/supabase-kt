package io.github.jan.supacompose.auth

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession

expect class SessionManager() {

    suspend fun saveSession(supabaseClient: SupabaseClient, session: UserSession)

    suspend fun loadSession(supabaseClient: SupabaseClient): UserSession?

    suspend fun deleteSession(supabaseClient: SupabaseClient)

}
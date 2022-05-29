package io.github.jan.supacompose.auth

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession

expect class SessionManager() {

    suspend fun saveSession(supabaseClient: SupabaseClient, auth: Auth, session: UserSession)

    suspend fun loadSession(supabaseClient: SupabaseClient, auth: Auth): UserSession?

    suspend fun deleteSession(supabaseClient: SupabaseClient, auth: Auth)

}
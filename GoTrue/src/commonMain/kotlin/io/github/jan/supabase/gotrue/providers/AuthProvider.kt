package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.user.UserSession

/**
 * An authentication provider
 */
interface AuthProvider<C, R> {

    /**
     * Used to login a user
     * @param supabaseClient The SupabaseClient to import the session into
     * @param onSuccess The callback to call when the login was successful
     * @param config The configuration for the login
     */
    suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    )

    /**
     * Used to sign up a user.
     * @param supabaseClient The SupabaseClient to import the session into
     * @param onSuccess The callback to call when the login was successful
     * @param config The configuration for the login
     * @return The result of the sign up or null, if autoconfirm is enabled
     */
    suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    ): R?

}
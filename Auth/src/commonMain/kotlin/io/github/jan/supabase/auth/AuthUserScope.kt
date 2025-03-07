package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental

/**
 * A scope that provides access to the current user
 */
interface AuthUserScope {

    /**
     * The corresponding [SupabaseClient]
     */
    val supabase: SupabaseClient

    /**
     * The current user or null if no user is signed in from the [Auth] plugin
     * @see Auth
     * @see Auth.currentUserOrNull
     */
    @SupabaseExperimental
    fun user() = supabase.auth.currentUserOrNull()

    /**
     * The id of the current user or null if no user is signed in
     * @see user
     */
    @SupabaseExperimental
    fun userId() = user()?.id

}
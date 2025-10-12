package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.user.UserSession

/**
 * TODO
 */
interface AuthDependentPluginConfig {

    /**
     * Whether to require a valid [UserSession] in the [Auth] plugin to make any request with this plugin. The [SupabaseClient.supabaseKey] cannot be used as fallback.
     */
    var requireValidSession: Boolean

}
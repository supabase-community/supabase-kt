package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.user.UserSession

/**
 * Configuration for plugins depending on the Auth plugin
 */
interface AuthDependentPluginConfig {

    /**
     * Whether to require a valid [UserSession] in the [Auth] plugin to make any request with this plugin. The [SupabaseClient.supabaseKey] cannot be used as fallback.
     */
    @SupabaseExperimental
    var requireValidSession: Boolean

}

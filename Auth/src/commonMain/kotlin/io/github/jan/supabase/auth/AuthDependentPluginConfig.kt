package io.github.jan.supabase.auth

/**
 * TODO
 */
interface AuthDependentPluginConfig {

    /**
     * Whether to require a valid [io.github.jan.supabase.auth.user.UserSession] in the [Auth] plugin to make any request with this plugin.
     */
    var requireValidSession: Boolean

}
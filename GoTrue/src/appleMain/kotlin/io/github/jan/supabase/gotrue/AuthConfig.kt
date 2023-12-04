package io.github.jan.supabase.gotrue

import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.MainConfig

/**
 * The configuration for [Auth]
 */
actual class AuthConfig : MainConfig, CustomSerializationConfig, AuthConfigDefaults() {

    /**
     * The scheme for the redirect url, when using deep linking
     */
    var scheme: String? = null

    /**
     * The host for the redirect url, when using deep linking
     */
    var host: String? = null

}
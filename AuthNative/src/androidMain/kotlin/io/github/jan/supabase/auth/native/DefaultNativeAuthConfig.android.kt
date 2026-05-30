package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.native.external.ExternalAuthAction

actual class PlatformNativeAuthConfig actual constructor(): DefaultNativeAuthConfig() {

    /**
     * The action to use for the OAuth flow.
     */
    var defaultExternalAuthAction: ExternalAuthAction = ExternalAuthAction.DEFAULT

    @PublishedApi internal val nativeAuthConfig: NativeAuthConfig = NativeAuthConfig()

    inline fun AuthConfig.nativeAuth(config: NativeAuthConfig.() -> Unit) {
        platformConfig().nativeAuthConfig.apply(config)
    }

}

data class NativeAuthConfig(
    var googleClientId: String? = null
)

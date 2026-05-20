package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.native.external.ExternalAuthAction

actual class PlatformNativeAuthConfig actual constructor(): DefaultNativeAuthConfig() {

    /**
     * The action to use for the OAuth flow.
     */
    var defaultExternalAuthAction: ExternalAuthAction = ExternalAuthAction.DEFAULT

    @PublishedApi internal val nativeAuthConfig: NativeAuthConfig = NativeAuthConfig()

    inline fun nativeAuth(config: NativeAuthConfig.() -> Unit) {
        nativeAuthConfig.apply(config)
    }

}

data class NativeAuthConfig(
    var googleClientId: String? = null
)
package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.native.oauth.ExternalAuthAction

actual class PlatformNativeAuthConfig actual constructor(): DefaultNativeAuthConfig() {

    /**
     * The action to use for the OAuth flow.
     */
    var defaultExternalAuthAction: ExternalAuthAction = ExternalAuthAction.DEFAULT

}
package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.oauth.ExternalAuthAction

actual class PlatformNativeAuthConfig actual constructor(): DefaultNativeAuthConfig() {

    /**
     * The action to use for the OAuth flow.
     */
    var defaultExternalAuthAction: ExternalAuthAction = ExternalAuthAction.DEFAULT

}
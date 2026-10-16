package io.github.jan.supabase.auth.native.external.apple

import io.github.jan.supabase.auth.Auth

actual class AppleCredential {

    init {
        throw UnsupportedOperationException()
    }

}

actual suspend fun Auth.signInWithApple(config: AppleSignInConfig.() -> Unit): AppleSignInResult = signInWithAppleFallback(config)
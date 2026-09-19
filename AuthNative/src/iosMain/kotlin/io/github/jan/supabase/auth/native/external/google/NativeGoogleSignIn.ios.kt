package io.github.jan.supabase.auth.native.external.google

import io.github.jan.supabase.auth.Auth

actual class GoogleCredential {

    init {
        throw UnsupportedOperationException()
    }

}

actual suspend fun Auth.signWithGoogle(config: GoogleSignInConfig.() -> Unit): GoogleSignInResult = signInWithGoogleFallback(config)

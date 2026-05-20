package io.github.jan.supabase.auth.native.external.google

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserSession

actual class GoogleSignInResult {
    actual val session: UserSession get() = error("No session will be returned upon starting the OAuth. Use `currentSessionOrNull()` after OAuth completed.")
}

actual suspend fun Auth.signWithGoogle(config: GoogleSignInConfig.() -> Unit): GoogleSignInResult {
    throw UnsupportedOperationException("Not supported")
}

package io.github.jan.supabase.auth.native.external.google

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserSession

expect class GoogleSignInResult {

    val session: UserSession

}

expect suspend fun Auth.signWithGoogle(
    config: GoogleSignInConfig.() -> Unit = {}
): GoogleSignInResult
package io.github.jan.supabase.auth.native.external.apple

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserSession

expect class AppleSignInResult {

    val session: UserSession

}

expect suspend fun Auth.signInWithApple(
    config: AppleSignInConfig.() -> Unit = {}
): AppleSignInResult
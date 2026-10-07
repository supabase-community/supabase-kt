package io.github.jan.supabase.auth.native.external.apple

import platform.AuthenticationServices.ASAuthorizationAppleIDCredential

internal sealed interface AppleCredentialResult {

    data class Success(val credential: ASAuthorizationAppleIDCredential, val idToken: String): AppleCredentialResult

    data class Error(val message: String, val exception: Exception? = null): AppleCredentialResult

    data object ClosedByUser: AppleCredentialResult

}
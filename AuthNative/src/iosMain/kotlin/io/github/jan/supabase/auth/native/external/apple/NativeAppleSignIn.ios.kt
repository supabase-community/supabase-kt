package io.github.jan.supabase.auth.native.external.apple

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.hash
import io.ktor.util.generateNonce
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.CompletableDeferred
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName

actual typealias AppleCredential = ASAuthorizationAppleIDCredential

@OptIn(BetaInteropApi::class)
actual suspend fun Auth.signInWithApple(config: AppleSignInConfig.() -> Unit): AppleSignInResult {
    var signInConfig = AppleSignInConfig("-").apply(config)
    val appleIDProvider = ASAuthorizationAppleIDProvider()
    val nonce = signInConfig.nonce ?: generateNonce()
    val request = appleIDProvider.createRequest().apply {
        requestedScopes =
            listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
        this.nonce = nonce.hash()
    }
    val signInResult = CompletableDeferred<AppleCredentialResult>()
    val authorizationDelegate =
        AuthorizationDelegate {
            signInResult.complete(it)
        }

    val controller = ASAuthorizationController(listOf(request)).apply {
        delegate = authorizationDelegate
        presentationContextProvider = presentationAnchor()
    }
    controller.performRequests()
    return when(val result = signInResult.await()) {
        is AppleCredentialResult.Error -> {
            val exception = result.exception ?: Exception(result.message)
            throw exception
        }
        is AppleCredentialResult.Success -> {
            signInConfig = AppleSignInConfig(result.idToken).apply {
                config()
                this.nonce = nonce
            }
            val session = signInWithIdToken(signInConfig)
            AppleSignInResult.Success(session, result.credential)
        }
        AppleCredentialResult.ClosedByUser -> AppleSignInResult.Cancelled
    }
}
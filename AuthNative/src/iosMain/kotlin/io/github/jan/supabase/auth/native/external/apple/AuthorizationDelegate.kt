package io.github.jan.supabase.auth.native.external.apple

import kotlinx.cinterop.BetaInteropApi
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

@BetaInteropApi
internal class AuthorizationDelegate(
    private val onResult: (AppleCredentialResult) -> Unit
) : NSObject(), ASAuthorizationControllerDelegateProtocol {
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        try {
            val credentials =
                didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
            credentials?.identityToken
                ?.let { NSString.create(it, encoding = NSUTF8StringEncoding)?.toString() }
                ?.let { idToken ->
                    onResult(AppleCredentialResult.Success(credentials, idToken))
                }
        } catch (e: Exception) {
            onResult(AppleCredentialResult.Error(e.message ?: "Error", e))
        }
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        when (didCompleteWithError.code.toUInt()) {
            1001.toUInt() -> onResult(AppleCredentialResult.ClosedByUser)
            else -> onResult.invoke(AppleCredentialResult.Error(didCompleteWithError.localizedDescription))
        }
    }
}

internal fun presentationAnchor(): ASAuthorizationControllerPresentationContextProvidingProtocol {
    return object : NSObject(), ASAuthorizationControllerPresentationContextProvidingProtocol {
        override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor {
            val x = 0.toUInt()
            val first = UIApplication
                .sharedApplication
                .connectedScenes
                .mapNotNull { it as UIWindowScene }
                .firstOrNull { it.activationState.toUInt() == x }

            return first?.windows?.mapNotNull { it as UIWindow }?.firstOrNull { it.isKeyWindow() }
        }
    }
}
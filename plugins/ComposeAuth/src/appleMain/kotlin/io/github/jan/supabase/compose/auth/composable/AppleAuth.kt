package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.loginWithApple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.base64EncodedStringWithOptions
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject



/**
 * Composable for Apple login with native behavior
 */
@Composable
actual fun ComposeAuth.rememberLoginWithApple(
    onResult: (NativeSignInResult) -> Unit,
    fallback: suspend () -> Unit
): NativeSignInState {

    val state = remember { NativeSignInState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = state.started) {

        if (config.appleLoginConfig == null) {
            fallback.invoke()
            state.reset()
            return@LaunchedEffect
        }

        val appleIDProvider = ASAuthorizationAppleIDProvider()
        val request = appleIDProvider.createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            nonce = config.appleLoginConfig?.nonce
        }

        val controller = ASAuthorizationController(listOf(request)).apply {
            delegate = authorizationController {
                scope.launch(Dispatchers.Main) {
                    if (it is NativeSignInResult.Success && it.idToken != null) {
                        loginWithApple(it.idToken)
                    }
                    onResult.invoke(it)
                    state.reset()
                }
            }

            presentationContextProvider = presentationAnchor()

        }
        controller.performRequests()
    }

    return state
}


internal fun authorizationController(
    onResult: (NativeSignInResult) -> Unit
): ASAuthorizationControllerDelegateProtocol {
    return object : NSObject(), ASAuthorizationControllerDelegateProtocol {
        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithAuthorization: ASAuthorization
        ) {
            try {
                val credentials =
                    didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
                credentials?.identityToken?.base64EncodedStringWithOptions(
                    NSUTF8StringEncoding
                )?.let { idToken ->
                    onResult.invoke(NativeSignInResult.Success(idToken))
                }
            } catch (e: Exception) {
                onResult.invoke(NativeSignInResult.Error(e.message ?: "error"))
            }
        }

        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithError: NSError
        ) {
            when (didCompleteWithError.code.toUInt()) {
                1001.toUInt() -> onResult.invoke(NativeSignInResult.ClosedByUser)
                else -> onResult.invoke(NativeSignInResult.Error(didCompleteWithError.localizedDescription))
            }
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
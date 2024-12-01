package io.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.supabase.compose.auth.ComposeAuth
import io.supabase.compose.auth.hash
import io.supabase.compose.auth.signInWithApple
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.CoroutineScope
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
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

/**
 * Composable function that implements Native Apple Auth.
 *
 * On unsupported platforms it will use the [fallback]
 *
 * @param onResult Callback for the result of the login
 * @param fallback Fallback function for unsupported platforms
 * @return [NativeSignInState]
 */
@BetaInteropApi
@OptIn(ExperimentalStdlibApi::class)
@Composable
actual fun ComposeAuth.rememberSignInWithApple(
    onResult: (NativeSignInResult) -> Unit,
    fallback: suspend () -> Unit
): NativeSignInState {
    val state = remember { NativeSignInState(this.serializer) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = state.status) {
        if (state.status is NativeSignInStatus.Started) {
            try {
                if (config.appleLoginConfig != null) {
                    val status = state.status as NativeSignInStatus.Started
                    val appleIDProvider = ASAuthorizationAppleIDProvider()
                    val hashedNonce = status.nonce?.hash()

                    val request = appleIDProvider.createRequest().apply {
                        requestedScopes =
                            listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
                        nonce = hashedNonce
                    }

                    val controller = ASAuthorizationController(listOf(request)).apply {
                        delegate = authorizationController(scope, status) {
                            onResult.invoke(it)
                            state.reset()
                        }

                        presentationContextProvider = presentationAnchor()

                    }
                    controller.performRequests()
                } else {
                    fallback.invoke()
                }
            } catch (e: Exception) {
                onResult.invoke(NativeSignInResult.Error(e.message ?: "error"))
            } finally {
                state.reset()
            }
        }
    }

    return state
}

@BetaInteropApi
internal fun ComposeAuth.authorizationController(
    scope: CoroutineScope,
    status: NativeSignInStatus.Started,
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
                credentials?.identityToken
                    ?.let { NSString.create(it, encoding = NSUTF8StringEncoding)?.toString() }
                    ?.let { idToken ->
                        scope.launch {
                            signInWithApple(idToken, status.nonce, status.extraData)
                            onResult.invoke(NativeSignInResult.Success)
                        }
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
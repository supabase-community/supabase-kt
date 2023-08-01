package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.jan.supabase.compose.auth.AppleLoginConfig
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.loginWithApple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.Foundation.NSError
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.base64EncodedStringWithOptions
import platform.darwin.NSObject

@Composable
fun ComposeAuth.rememberLoginWithApple(
    onResult: (NativeSignInResult) -> Unit,
    fallback: suspend () -> Unit
): NativeSignInState {

    val state = remember { NativeSignInState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = state.started) {

        if (config.loginConfig == null || config.loginConfig !is AppleLoginConfig) {
            fallback.invoke()
            state.reset()

        }

        val appleIDProvider = ASAuthorizationAppleIDProvider()
        val request = appleIDProvider.createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            nonce = (config.loginConfig as? AppleLoginConfig)?.nonce
        }

        val controller = ASAuthorizationController(listOf(request)).apply {
            delegate = object : NSObject(), ASAuthorizationControllerDelegateProtocol {
                override fun authorizationController(
                    controller: ASAuthorizationController,
                    didCompleteWithAuthorization: ASAuthorization
                ) {
                    scope.launch(Dispatchers.Main) {
                        try {
                            val credentials =
                                didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
                            credentials?.identityToken?.base64EncodedStringWithOptions(
                                NSUTF8StringEncoding
                            )?.let { idToken ->
                                loginWithApple(idToken)
                                onResult.invoke(NativeSignInResult.Success)
                            }
                        } catch (e: Exception) {
                            onResult.invoke(NativeSignInResult.Error(e.message ?: "error"))
                        }
                        state.reset()
                        super.authorizationController(controller, didCompleteWithAuthorization)
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


                    state.reset()
                    super.authorizationController(controller, didCompleteWithError)
                }
            }

//        presentationContextProvider = need help

        }
        controller.performRequests()
    }

    return state
}

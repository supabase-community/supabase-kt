package io.github.jan.supabase.auth.native.external

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.DefaultOAuthConfig
import io.github.jan.supabase.auth.OAuthConfig
import io.github.jan.supabase.auth.OAuthProvider
import io.github.jan.supabase.auth.OAuthProviders

/**
 * Signs in the user with the specified [provider] via OAuth
 *
 * Example:
 * ```kotlin
 * auth.signInWithOAuth(OAuthProviders.GOOGLE)
 * ```
 *
 * The action performed will depend on the Kotlin target.
 *
 * On Android, it will automatically open a Custom Tab (if not changed in the config) and receive the result after a successful sign-in
 *
 * On iOS, it will create a ASWebAuthentication session and receive the result automatically.
 *
 * On Desktop targets, it will create an HTTP callback server to receive the OAuth result.
 *
 * On Web targets, it will just redirect to the OAuth url and then receive the result automatically
 *
 * On other targets, it will just open the system browser, which will redirect to the specified redirect url after a successful sign-in
 *
 * @param provider the provider to use for signing in. E.g. [OAuthProviders.GOOGLE], [OAuthProviders.APPLE]
 * @param config The configuration to use for the sign-in.
 * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
suspend fun Auth.signInWithOAuth(
    provider: OAuthProvider,
    config: OAuthConfig
) {
    val redirectUrl = config.redirectUrl ?: defaultRedirectUrl()
    startOAuthSession(
        redirectUrl,
        {
            getOAuthUrl(provider) {
                this.redirectUrl = it
                this.scopes.addAll(config.scopes)
                this.queryParams.putAll(config.queryParams)
            }
        },
        onSessionSuccess = {
            importSession(it)
        }
    )
}

suspend fun Auth.signInWithOAuth(
    provider: OAuthProvider,
    config: OAuthConfig.() -> Unit = {}
) = signInWithOAuth(provider, DefaultOAuthConfig().apply(config))
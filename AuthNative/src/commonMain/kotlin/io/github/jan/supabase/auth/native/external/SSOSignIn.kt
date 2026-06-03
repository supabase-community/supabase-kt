package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SSOConfig
import io.github.jan.supabase.auth.SSODomain
import io.github.jan.supabase.auth.SSOIdentifier
import io.github.jan.supabase.auth.SSOProvider
import io.github.jan.supabase.auth.native.external.signInWithOAuth
import io.github.jan.supabase.auth.native.external.startOAuthSession

/**
 * Attempts a single-sign on using an enterprise Identity Provider. A
 * successful SSO attempt will redirect the current page to the identity
 * provider authorization page. The redirect URL is implementation and SSO
 * protocol specific.
 *
 * You can use it by providing a SSO domain. Typically you can extract this
 * domain by asking users for their email address. If this domain is
 * registered on the Auth instance the redirect will use that organization's
 * currently active SSO Identity Provider for the login.
 *
 * If you have built an organization-specific login page, you can use the
 * organization's SSO Identity Provider UUID directly instead.
 *
 * Example:
 * ```kotlin
 * supabase.auth.signInWithSSO(SSODomain("myDomain"))
 * ```
 * or with a provider id:
 * ```kotlin
 * supabase.auht.signInWithSSO(SSOProvider("provider-id"))
 * ```
 *
 * This method works similar to [signInWithOAuth], see its documentation for more information.
 * @param identifier The [SSOIdentifier]. A [SSODomain] or a [SSOProvider].
 * @param config Extra configuration
 * @throws RestException or one of its subclasses if receiving an error response. If the error response contains an error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
suspend fun Auth.signInWithSSO(
    identifier: SSOIdentifier,
    config: SSOConfig.() -> Unit = {}
) {
    startOAuthSession(
        redirectUrl = SSOConfig(identifier).apply(config).redirectTo,
        getUrl = {
            getSSOUrl(identifier, config)
        },
        onSessionSuccess = {
            importSession(it)
        }
    )
}
package io.github.jan.supabase.auth.oauth

/**
 * Response type for getting OAuth authorization details.
 * Returns either full [authorization details][OAuthAuthorizationDetailResponse.Details] (if consent needed) or [redirect URL][OAuthAuthorizationDetailResponse.Redirect] (if already consented).
 * Only relevant when the OAuth 2.1 server is enabled in Supabase Auth.
 */
sealed interface OAuthAuthorizationDetailResponse
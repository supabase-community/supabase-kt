package io.github.jan.supabase.auth.exception

/**
 * Thrown when there was no PKCE code verifier found in storage e.g. when providing a flow id, but the corresponding code verifier was evicted.
 */
class AuthPKCECodeVerifierMissing: Exception("PKCE code verifier not found in storage.")
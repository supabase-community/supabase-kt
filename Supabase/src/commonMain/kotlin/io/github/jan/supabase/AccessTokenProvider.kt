package io.github.jan.supabase

/**
 * Optional function for using a third-party authentication system with
 * Supabase. The function should return an access token or ID token (JWT) by
 * obtaining it from the third-party auth client library. Note that this
 * function may be called concurrently and many times. Use memoization and
 * locking techniques if this is not supported by the client libraries.
 *
 * When set, the Auth plugin from `auth-kt` cannot be used.
 * Create another client if you wish to use Supabase Auth and third-party
 * authentications concurrently in the same application.
 */
typealias AccessTokenProvider = suspend () -> String
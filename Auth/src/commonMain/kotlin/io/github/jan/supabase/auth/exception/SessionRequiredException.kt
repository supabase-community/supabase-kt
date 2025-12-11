package io.github.jan.supabase.auth.exception

/**
 * An exception thrown when trying to perform a request that requires a valid session while no user is logged in.
 */
class SessionRequiredException: Exception("You need to be logged in to perform this request")
package io.github.jan.supabase.auth.exception

import io.github.jan.supabase.StringMasking
import io.ktor.http.Url

/**
 * An exception thrown when trying to perform a request that requires a valid session while no user is logged in.
 */
class SessionRequiredException(val url: String): Exception("You need to be logged in to perform this request\nURL: ${StringMasking.maskUrl(
    Url(url)
)}")
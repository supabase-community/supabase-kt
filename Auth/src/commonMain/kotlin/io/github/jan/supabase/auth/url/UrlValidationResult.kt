package io.github.jan.supabase.auth.url

import io.github.jan.supabase.auth.user.UserSession

internal sealed interface UrlValidationResult {

    data object ErrorFound: UrlValidationResult

    data class SessionFound(val session: UserSession): UrlValidationResult

    data object Skipped: UrlValidationResult

}
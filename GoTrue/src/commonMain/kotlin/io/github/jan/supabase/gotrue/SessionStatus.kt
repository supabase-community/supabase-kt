package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.user.UserSession
import kotlin.jvm.JvmInline

sealed interface SessionStatus {
    object NotAuthenticated : SessionStatus
    object LoadingFromStorage : SessionStatus
    object NetworkError : SessionStatus

    @JvmInline
    value class Authenticated(val session: UserSession) : SessionStatus
}

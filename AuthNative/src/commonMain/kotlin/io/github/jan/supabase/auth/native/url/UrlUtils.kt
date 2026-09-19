package io.github.jan.supabase.auth.native.url

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionFlag
import io.github.jan.supabase.auth.tryToGetUser
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.launch

@SupabaseInternal
fun Auth.parseFragmentAndImportSession(fragment: String, onFinish: (UserSession?) -> Unit = {}) {
    val res = validateHash(fragment)
    if(res !is UrlValidationResult.SessionFound) {
        onFinish(null)
        return
    }
    val session = res.session
    authScope.launch {
        val user = tryToGetUser(session.accessToken)
        val newSession = session.copy(user = user)
        onFinish(newSession)
        importSession(newSession, flag = if(session.type == "recovery") SessionFlag.RECOVERY else SessionFlag.EXTERNAL )
    }
}
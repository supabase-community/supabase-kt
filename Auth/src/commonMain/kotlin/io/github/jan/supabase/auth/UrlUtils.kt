package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.url.validateHash
import io.github.jan.supabase.auth.user.UserSession
import io.ktor.client.request.HttpRequestBuilder

@SupabaseInternal
fun Auth.parseFragmentAndImportSession(fragment: String, onFinish: (UserSession?) -> Unit = {}) {
    val session = validateHash(fragment)
    if(session == null) {
        onFinish(null)
        return
    }
    this as AuthImpl
    /*authScope.launch {
        val user = retrieveUser(session.accessToken)
        val newSession = session.copy(user = user)
        onFinish(newSession)
        importSession(newSession, source = SessionSource.External)
    }*/
}

@SupabaseInternal
fun HttpRequestBuilder.redirectTo(url: String) {
    this.url.parameters["redirect_to"] = url
}
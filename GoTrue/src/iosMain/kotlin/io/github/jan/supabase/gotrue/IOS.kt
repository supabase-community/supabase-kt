package io.github.jan.supabase.gotrue

import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL

@SupabaseExperimental
fun SupabaseClient.handleDeeplinks(url: NSURL, onSessionSuccess: (UserSession) -> Unit = {}) {
    if(url.scheme != gotrue.config.scheme || url.host != gotrue.config.host){
        Napier.d { "Received deeplink with wrong scheme or host" }
        return
    }
    val fragment = url.fragment
    if(fragment == null) {
        Napier.d { "No fragment for deeplink" }
        return
    }
    parseFragment(fragment, onSessionSuccess)
}
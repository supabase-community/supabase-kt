package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents

@SupabaseExperimental
fun SupabaseClient.handleDeeplinks(url: NSURL, onSessionSuccess: (UserSession) -> Unit = {}) {
    val components = NSURLComponents(url, true)
    if(components.scheme != gotrue.config.scheme || components.host != gotrue.config.host) return
    val fragment = components.fragment ?: return
    parseFragment(fragment, onSessionSuccess)
}
package io.github.jan.supabase.auth.oauth

import android.net.Uri
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.parseFragmentAndImportSession
import io.github.jan.supabase.auth.url.handledUrlParameterError
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.launch

internal fun Auth.listenForDeeplinks() {
    authScope.launch {
        AuthFlowManager.redirectFlow.collect {
            handleDeeplinks(it)
        }
    }
}

internal fun Auth.handleDeeplinks(
    data: Uri,
    onSessionSuccess: (UserSession) -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    when(config.flowType) {
        FlowType.IMPLICIT -> {
            val fragment = data.fragment ?: return
            parseFragmentAndImportSession(fragment) {
                it?.let(onSessionSuccess)
            }
        }
        FlowType.PKCE -> {
            if(handledUrlParameterError { data.getQueryParameter(it) }) {
                return
            }
            val code = data.getQueryParameter("code") ?: return
            authScope.launch {
                try {
                    exchangeCodeForSession(code)
                    onSessionSuccess(currentSessionOrNull() ?: error("No session available"))
                } catch (e: Throwable) {
                    onError(e)
                }
            }
        }
    }
}
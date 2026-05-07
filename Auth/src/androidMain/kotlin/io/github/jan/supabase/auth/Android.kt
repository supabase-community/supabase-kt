package io.github.jan.supabase.auth

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.url.handledUrlParameterError
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.launch

//TODO: Add context receiver 'Activity'
/**
 * Handle deeplinks for authentication.
 * This handles the deeplinks for implicit and PKCE flow.
 * @param intent The intent from the activity
 * @param onSessionSuccess The callback when the session was successfully imported
 * @param onError Callback invoked if an error occurs during the [Auth.exchangeCodeForSession] call.
 */
@OptIn(SupabaseInternal::class)
@Deprecated("No longer necessary on Android if providing the manifest placeholder in your build configuration")
fun SupabaseClient.handleDeeplinks(
    intent: Intent,
    onSessionSuccess: (UserSession) -> Unit = {},
    onError: (Throwable) -> Unit = {}
) = intent.data?.let { auth.handleDeeplinks(it, onSessionSuccess, onError) }

internal fun Auth.handleDeeplinks(
    data: Uri,
    onSessionSuccess: (UserSession) -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    val scheme = data.scheme ?: return
    val host = data.host ?: return
    if(scheme != config.scheme || host != config.host) return
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
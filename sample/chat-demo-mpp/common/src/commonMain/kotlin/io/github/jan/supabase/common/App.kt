package io.github.jan.supabase.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.common.ui.screen.ChatScreen
import io.github.jan.supabase.common.ui.screen.LoginScreen

@Composable
fun App(viewModel: ChatViewModel) {
    val sessionStatus by viewModel.sessionStatus.collectAsState()

    Surface {
        when(sessionStatus) {
            is SessionStatus.Authenticated -> {
                ChatScreen(viewModel, (sessionStatus as SessionStatus.Authenticated).session.user ?: error("User is null"))
            }
            is SessionStatus.NotAuthenticated -> {
                LoginScreen(viewModel)
            }
            else -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                }
            }
        }
    }

}

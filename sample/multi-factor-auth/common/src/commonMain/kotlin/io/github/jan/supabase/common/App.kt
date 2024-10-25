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
import io.github.jan.supabase.common.ui.components.AlertDialog
import io.github.jan.supabase.common.ui.screen.LoginScreen
import io.github.jan.supabase.common.ui.screen.MfaScreen

@Composable
fun App(viewModel: AppViewModel) {
    val loginAlert by viewModel.loginAlert.collectAsState()
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    println(sessionStatus)
    Surface {
        when(sessionStatus) {
            is SessionStatus.Authenticated -> {
                MfaScreen(viewModel)
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

    if(loginAlert != null) {
        AlertDialog(loginAlert!!) {
            viewModel.loginAlert.value = null
        }
    }

}

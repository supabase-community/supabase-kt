package io.github.jan.supabase.common.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.gotrue.mfa.MfaStatus

@Composable
fun MfaScreen(viewModel: AppViewModel) {
    val status by viewModel.statusFlow.collectAsState(MfaStatus(false, false))
    when {
        status.enabled && status.active -> { //only when logged in using mfa & mfa enabled
            AppScreen(viewModel)
        }
        status.enabled && !status.active -> { //show only when mfa enabled & not logged in using mfa
            MfaLoginScreen(viewModel)
        }
        else -> { //show only when logged in using mfa and mfa disabled or not set up
            MfaSetupScreen(viewModel)
        }
    }
}
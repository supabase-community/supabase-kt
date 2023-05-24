package io.github.jan.supabase.common.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.gotrue.gotrue

@Composable
fun MfaScreen(viewModel: AppViewModel) {
    val isLoggedInUsingMfa by viewModel.isLoggedInUsingMfa.collectAsState(false)
    val mfaEnabled by viewModel.mfaEnabled.collectAsState(false)
    println(viewModel.supabaseClient.gotrue.mfa.verifiedFactors)
    println(mfaEnabled)
    //updateCurrentUser saves the old session, so fix that and then see if mfaEnabled still says true after disabling mfa
    when {
        isLoggedInUsingMfa && mfaEnabled -> { //only when logged in using mfa & mfa enabled
            AppScreen(viewModel)
        }
        mfaEnabled && !isLoggedInUsingMfa -> { //show only when mfa enabled & not logged in using mfa
            MfaLoginScreen(viewModel)
        }
        else -> { //show only when logged in using mfa and mfa disabled or not set up
            MfaSetupScreen(viewModel)
        }
    }
}
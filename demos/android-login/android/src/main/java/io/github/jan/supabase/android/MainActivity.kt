package io.github.jan.supabase.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.common.ui.screen.LoginScreen
import io.github.jan.supabase.common.ui.screen.LoginType
import io.github.jan.supabase.common.ui.screen.OAuthScreen
import io.github.jan.supabase.compose.auth.composable.rememberLoginWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.handleDeeplinks
import io.github.jan.supabase.gotrue.providers.Spotify
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by inject()
    private val supabaseClient: SupabaseClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supabaseClient.handleDeeplinks(intent)
        setContent {
            MaterialTheme {
                val status by viewModel.sessionStatus.collectAsState()
                when (status) {
                    is SessionStatus.Authenticated -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button({
                                viewModel.logout()
                            }) {
                                Text("Logout")
                            }
                        }
                    }
                    else -> {
                        val googleState = supabaseClient.composeAuth.rememberLoginWithGoogle()
                        var showInAppGoogleWebView by remember { mutableStateOf(false) }
                        LoginScreen {
                            when (it) {
                                LoginType.GoogleNative -> googleState.startFlow()
                                LoginType.SpotifyInApp -> showInAppGoogleWebView = true
                                is LoginType.Login -> viewModel.login(it.email, it.password)
                                is LoginType.SignUp -> viewModel.signUp(it.email, it.password)
                            }
                        }
                        if (showInAppGoogleWebView) {
                            OAuthScreen(
                                url = supabaseClient.gotrue.oAuthUrl(
                                    Spotify,
                                    "http://localhost"
                                ),
                                parseFragment = {
                                    viewModel.parseFragment(it)
                                },
                                disable = {
                                    showInAppGoogleWebView = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}
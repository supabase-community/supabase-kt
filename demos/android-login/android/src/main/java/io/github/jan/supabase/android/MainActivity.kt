package io.github.jan.supabase.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.common.ui.screen.LoginScreen
import io.github.jan.supabase.common.ui.screen.LoginType
import io.github.jan.supabase.common.ui.screen.OAuthScreen
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.handleDeeplinks
import io.github.jan.supabase.gotrue.providers.Google
import org.koin.android.ext.android.inject

const val SERVER_CLIENT_ID =
    "178705897393-1o04rilnoit4a6ls84d2751a3jvibbij.apps.googleusercontent.com"

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Napier.base(DebugAntilog())
        viewModel.supabaseClient.handleDeeplinks(intent)
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
                        val oneTapState = rememberOneTapSignInState()
                        var showInAppGoogleWebView by remember { mutableStateOf(false) }
                        LoginScreen {
                            when (it) {
                                LoginType.GoogleNative -> oneTapState.open()
                                LoginType.GoogleInApp -> showInAppGoogleWebView = true
                                is LoginType.Login -> viewModel.login(it.email, it.password)
                                is LoginType.SignUp -> viewModel.signUp(it.email, it.password)
                            }
                        }
                        OneTapSignInWithGoogle(
                            state = oneTapState,
                            clientId = SERVER_CLIENT_ID,
                            onTokenIdReceived = {
                                viewModel.loginWithIdToken(it)
                            },
                            onDialogDismissed = { println(it) }
                        )
                        if (showInAppGoogleWebView) {
                            OAuthScreen(
                                url = viewModel.supabaseClient.gotrue.oAuthUrl(
                                    Google,
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
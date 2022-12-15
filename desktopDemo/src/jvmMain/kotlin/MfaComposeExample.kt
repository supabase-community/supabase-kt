import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.russhwolf.settings.PreferencesSettings
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.mfa.FactorType
import io.github.jan.supabase.gotrue.mfa.MfaFactor
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.prefs.Preferences

suspend fun main() {
    Napier.base(DebugAntilog())
    val client = createSupabaseClient(
        supabaseUrl = System.getenv("SUPABASE_URL"),
        supabaseKey = System.getenv("SUPABASE_KEY")
    ) {
        install(GoTrue) {
            sessionManager = SettingsSessionManager(PreferencesSettings(Preferences.userRoot().node("custom_name")))
        }
    }
    val scope = CoroutineScope(Dispatchers.IO)
    application {
        Window(::exitApplication) {
            val status by client.gotrue.sessionStatus.collectAsState()
            val mfaEnabled by client.gotrue.mfa.isMfaEnabledFlow.collectAsState(false)
            val loggedInUsingMfa by client.gotrue.mfa.loggedInUsingMfaFlow.collectAsState(false)
            var factor by remember { mutableStateOf<MfaFactor<FactorType.TOTP.Response>?>(null) }
            //val status by client.realtime.status.collectAsState()
            if (status is SessionStatus.Authenticated && (loggedInUsingMfa || !mfaEnabled)) { //check if the user is authenticated and already logged in using mfa
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column {
                        Text("Logged in as ${(status as SessionStatus.Authenticated).session.user?.email}")
                        Text("MFA enabled: $mfaEnabled")
                        Row {
                            Button({
                                scope.launch {
                                    client.gotrue.invalidateSession()
                                }
                            }) {
                                Text("Logout")
                            }
                        }
                        if (!mfaEnabled && factor == null) {
                            Button({
                                scope.launch {
                                    factor = client.gotrue.mfa.enroll(FactorType.TOTP)
                                }
                            }) {
                                Text("Enable MFA")
                            }
                        } else if(mfaEnabled && loggedInUsingMfa) {
                            Button({
                                scope.launch {
                                    client.gotrue.mfa.unenroll(client.gotrue.mfa.verifiedFactors.first().id)
                                }
                            }) {
                                Text("Disable MFA")
                            }
                        } else if (factor != null) {
                            Dialog({
                                scope.launch {
                                    client.gotrue.mfa.unenroll(factor!!.id) //unenroll the factor we just enrolled
                                    factor = null
                                }
                            }, title = "Scan QR code") {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        var code by remember { mutableStateOf("") }
                                        Image(rememberSvgPainter(factor!!.data.qrCode), "QR Code")
                                        TextField(code, { code = it }, placeholder = { Text("Enter code") })
                                        Button({
                                            scope.launch {
                                                try {
                                                    client.gotrue.mfa.createChallengeAndVerify(factor!!.id, code)
                                                    factor = null
                                                } catch(_: BadRequestRestException) {
                                                    println("Invalid TOTP Code!")
                                                }
                                            }
                                        }) {
                                            Text("Verify")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if(!loggedInUsingMfa && status is SessionStatus.Authenticated && mfaEnabled) { //check if the user is authenticated but not logged in using mfa (and mfa is enabled)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        var code by remember { mutableStateOf("") }
                        TextField(code, { code = it }, placeholder = { Text("Enter MFA Code") })
                        Button({
                            scope.launch {
                                try {
                                    client.gotrue.mfa.createChallengeAndVerify(client.gotrue.mfa.verifiedFactors.first().id, code)
                                } catch(_: BadRequestRestException) {
                                    println("Invalid TOTP Code!")
                                }
                            }
                        }) {
                            Text("Verify")
                        }
                    }
                }
            } else { //login screen
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    Column {
                        TextField(email, { email = it }, placeholder = { Text("Email") })
                        TextField(
                            password,
                            { password = it },
                            placeholder = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Button(onClick = {
                            scope.launch {
                                client.gotrue.loginWith(Email) {
                                    this.email = email
                                    this.password = password
                                }
                            }
                        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Login")
                        }
                        Button(onClick = {
                            scope.launch {
                                client.gotrue.loginWith(Google)
                            }
                        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Login with Discord")
                        }
                        //
                    }
                }

            }
        }
    }

}

fun svgPainter(encodedData: ByteArray, density: Density) : Painter = androidx.compose.ui.res.loadSvgPainter(ByteArrayInputStream(encodedData), density)

@Composable
fun rememberSvgPainter(svg: String): Painter {
    val density = LocalDensity.current
    return remember(svg) { svgPainter(svg.encodeToByteArray(), density) }
}
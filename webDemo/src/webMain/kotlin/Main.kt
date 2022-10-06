import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.providers.Discord
import io.github.jan.supacompose.auth.providers.builtin.Email
import io.github.jan.supacompose.createSupabaseClient
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.EmailInput
import org.jetbrains.compose.web.dom.PasswordInput
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

fun main() {
    val client = createSupabaseClient {
        supabaseUrl = "https://arnyfaeuskyqfxkvotgj.supabase.co"
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFybnlmYWV1c2t5cWZ4a3ZvdGdqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NTMwMzkxMTEsImV4cCI6MTk2ODYxNTExMX0.ItmL8lfnOL9oy7CEX9N6TnYt10VVhk-KTlwley4aq1M"

        install(Auth)
    }

    renderComposable(rootElementId = "root") {
        val session by client.auth.currentSession.collectAsState()
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        if(session != null) {
            Span({ style { padding(15.px) } }) {
                Text("Logged in as ${session!!.user?.email}")
            }
        } else {
            EmailInput(email) {
                onInput {
                    email = it.value
                }
            }
            PasswordInput(password) {
                onInput {
                    password = it.value
                }
            }
            Button({
                onClick {
                    scope.launch {
                        client.auth.loginWith(Email) {
                            this.email = email
                            this.password = password
                        }
                    }
                }
            }) {
                Text("Login")
            }
            Button({
                onClick {
                    scope.launch {
                        client.auth.loginWith(Discord)
                    }
                }
            }) {
                Text("Login with Discord")
            }
        }
    }
}
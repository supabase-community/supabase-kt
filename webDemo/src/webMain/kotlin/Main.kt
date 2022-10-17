import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.jan.supabase.auth.GoTrue
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Discord
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
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
        supabaseUrl = ""
        supabaseKey = ""

        install(GoTrue)
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
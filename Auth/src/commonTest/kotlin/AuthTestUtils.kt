import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus

fun Auth.sessionSource() = (sessionStatus.value as SessionStatus.Authenticated).source

inline fun <reified T> Auth.sessionSourceAs() = sessionSource() as T
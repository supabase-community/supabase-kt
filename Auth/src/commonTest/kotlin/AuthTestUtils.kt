import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus

fun Auth.sessionSource() = (sessionStatus.value as SessionStatus.Authenticated).source

inline fun <reified T> Auth.sessionSourceAs() = sessionSource() as T
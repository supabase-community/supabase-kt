import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus

fun Auth.sessionFlag() = (sessionStatus.value as SessionStatus.Authenticated).flag
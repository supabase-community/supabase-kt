import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.openUrl

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    openUrl(Uri.parse(url), auth.config.defaultExternalAuthAction)
}
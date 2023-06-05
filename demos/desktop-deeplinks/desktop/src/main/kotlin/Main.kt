import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.jan.supabase.common.App
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.common.URL_PROTOCOL
import io.github.jan.supabase.common.di.initKoin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tk.pratanumandal.unique4j.Unique4j

const val APP_ID = "io.github.jan.supabase.desktop-deeplinks-202306-jvm"

class RootComponent : KoinComponent {

    val viewModel: AppViewModel by inject()

}

class Unique4jImpl(private val args: List<String>, private val onReceiveMessage: (message: String) -> Unit): Unique4j(APP_ID) {

    override fun receiveMessage(message: String) = onReceiveMessage(message)

    override fun sendMessage(): String {
        return args.firstOrNull { it.startsWith(URL_PROTOCOL) } ?: ""
    }

}

fun main(args: Array<String>) {
    initKoin()
    val root = RootComponent()
    val unique4j = Unique4jImpl(args.toList()) {
        if(it.startsWith(URL_PROTOCOL)) { // handle received message
            val fragment = it.substringAfter("#")
            root.viewModel.importSession(fragment)
        }
    }
    val lockFlag = unique4j.acquireLock()
    if(lockFlag) { // if there is no other instance running and we acquired the lock, we check for a deeplink in the args
        val protocol = args.firstOrNull { it.startsWith(URL_PROTOCOL) }
        if(protocol != null) {
            val fragment = protocol.substringAfter("#")
            root.viewModel.importSession(fragment)
        }
    }
    application {
        Window(onCloseRequest = ::exitApplication, title = "Desktop Login App") {
            App(root.viewModel)
        }
    }
    val releaseFlag = unique4j.freeLock()
}

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.jan.supabase.common.App
import io.github.jan.supabase.common.ChatViewModel
import io.github.jan.supabase.common.di.initKoin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RootComponent : KoinComponent {

    val viewModel: ChatViewModel by inject()

}

fun main() {
    initKoin()
    val root = RootComponent()
    application {
        Window(onCloseRequest = ::exitApplication, title = "Chat App") {
            App(root.viewModel)
        }
    }
}

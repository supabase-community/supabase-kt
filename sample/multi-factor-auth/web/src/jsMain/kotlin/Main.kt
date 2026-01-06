import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.github.jan.supabase.common.App
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.common.di.initKoin
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RootComponent : KoinComponent {

    val viewModel: AppViewModel by inject()

}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    val root = RootComponent()
    onWasmReady {
        ComposeViewport("ComposeTarget") {
            App(root.viewModel)
        }
    }
}
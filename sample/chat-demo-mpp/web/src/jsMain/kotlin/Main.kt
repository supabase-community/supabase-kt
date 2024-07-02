// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.Window
import io.github.jan.supabase.common.App
import io.github.jan.supabase.common.ChatViewModel
import io.github.jan.supabase.common.di.initKoin
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RootComponent : KoinComponent {

    val viewModel: ChatViewModel by inject()

}

fun main() {
    initKoin()
    val root = RootComponent()
    onWasmReady {
        Window(title = "Demo Chat App") {
            App(root.viewModel)
        }
    }
}
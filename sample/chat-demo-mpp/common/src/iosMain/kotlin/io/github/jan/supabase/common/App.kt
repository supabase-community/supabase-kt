package io.github.jan.supabase.common

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.UIKit.UIViewController

class RootComponent : KoinComponent {
    private val viewModel: ChatViewModel by inject()
    fun getViewModel(): ChatViewModel = viewModel
}

fun AppIos(viewModel: ChatViewModel): UIViewController = ComposeUIViewController {
    App(viewModel = viewModel)
}
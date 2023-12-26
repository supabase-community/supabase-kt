package io.github.jan.supabase.common

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import platform.UIKit.UIViewController


object RootIosHelper : KoinComponent {
    fun getViewModel() = getKoin().get<ChatViewModel>()

}

fun AppIos(viewModel: ChatViewModel): UIViewController = ComposeUIViewController {
    App(viewModel = viewModel)
}
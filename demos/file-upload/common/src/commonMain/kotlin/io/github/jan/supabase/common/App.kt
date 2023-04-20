package io.github.jan.supabase.common

import androidx.compose.runtime.Composable
import io.github.jan.supabase.common.ui.screen.UploadScreen

@Composable
fun App(viewModel: UploadViewModel) {
    UploadScreen(viewModel)
}

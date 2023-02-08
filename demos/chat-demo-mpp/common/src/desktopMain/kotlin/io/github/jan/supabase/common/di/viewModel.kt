package io.github.jan.supabase.common.di

import io.github.jan.supabase.common.ChatViewModel
import org.koin.core.module.Module

actual fun Module.viewModel() {
    single { createViewModule() }
}
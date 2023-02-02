package io.github.jan.supabase.common.di

import io.github.jan.supabase.common.ChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module

actual fun Module.viewModel() {
    viewModel { createViewModule() }
}
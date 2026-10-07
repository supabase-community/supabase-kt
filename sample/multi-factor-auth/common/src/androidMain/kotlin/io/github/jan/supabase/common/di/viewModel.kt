package io.github.jan.supabase.common.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

actual fun Module.viewModel() {
    viewModel { createViewModule() }
}
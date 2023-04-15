package io.github.jan.supabase.common.di

import io.github.jan.supabase.common.UploadViewModel
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

expect fun Module.viewModel()

fun Scope.createViewModule() = UploadViewModel(get(), get())

val viewModelModule = module {
    viewModel()
}
package io.github.jan.supabase.common.di

import io.github.jan.supabase.common.AppViewModel
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

expect fun Module.viewModel()

fun Scope.createViewModule() = AppViewModel(get())

val viewModelModule = module {
    viewModel()
}
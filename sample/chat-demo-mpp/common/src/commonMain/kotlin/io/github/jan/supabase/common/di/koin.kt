package io.github.jan.supabase.common.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(additionalConfiguration: KoinApplication.() -> Unit = {}) {
    startKoin {
        modules(supabaseModule, netModule, viewModelModule)
        additionalConfiguration()
    }
}
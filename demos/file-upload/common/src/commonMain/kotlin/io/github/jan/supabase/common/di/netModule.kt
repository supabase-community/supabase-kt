package io.github.jan.supabase.common.di

import io.github.jan.supabase.common.net.UploadManager
import org.koin.dsl.module

val netModule = module {
    single<UploadManager> {

    }
}
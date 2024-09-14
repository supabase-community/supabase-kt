package io.github.jan.supabase.common.di

import io.github.jan.supabase.common.net.AuthApi
import io.github.jan.supabase.common.net.AuthApiImpl
import io.github.jan.supabase.common.net.MessageApi
import io.github.jan.supabase.common.net.MessageApiImpl
import org.koin.dsl.module

val netModule = module {
    single<MessageApi> { MessageApiImpl(get()) }
    single<AuthApi> { AuthApiImpl(get()) }
}
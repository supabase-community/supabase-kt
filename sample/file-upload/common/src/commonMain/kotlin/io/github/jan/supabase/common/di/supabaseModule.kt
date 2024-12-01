package io.github.jan.supabase.common.di

import io.supabase.SupabaseClient
import io.supabase.annotations.SupabaseExperimental
import io.supabase.createSupabaseClient
import io.supabase.storage.Storage
import io.supabase.storage.resumable.SettingsResumableCache
import io.supabase.storage.storage
import org.koin.dsl.module

const val BUCKET = "YOUR_BUCKET"

@OptIn(SupabaseExperimental::class)
val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://id.supabase.co",
            supabaseKey = "apikey"
        ) {
            install(Storage) {
                resumable {
                    cache = SettingsResumableCache()
                }
            }
        }
    }
    single {
        get<SupabaseClient>().storage[BUCKET].resumable
    }
}
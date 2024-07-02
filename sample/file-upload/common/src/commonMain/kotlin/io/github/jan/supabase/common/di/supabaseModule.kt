package io.github.jan.supabase.common.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.SettingsResumableCache
import io.github.jan.supabase.storage.storage
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
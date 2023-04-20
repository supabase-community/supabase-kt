package io.github.jan.supabase.common.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.ResumableCache
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
                    cache = ResumableCache.Disk()
                }
            }
        }
    }
    single {
        get<SupabaseClient>().storage[BUCKET].resumable
    }
}
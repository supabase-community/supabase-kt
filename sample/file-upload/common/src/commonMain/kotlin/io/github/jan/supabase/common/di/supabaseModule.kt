package io.github.jan.supabase.common.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.SettingsResumableCache
import io.github.jan.supabase.storage.storage
import org.koin.dsl.module

const val BUCKET = "test"

@OptIn(SupabaseExperimental::class)
val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://arnyfaeuskyqfxkvotgj.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFybnlmYWV1c2t5cWZ4a3ZvdGdqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NTMwMzkxMTEsImV4cCI6MTk2ODYxNTExMX0.ItmL8lfnOL9oy7CEX9N6TnYt10VVhk-KTlwley4aq1M"
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
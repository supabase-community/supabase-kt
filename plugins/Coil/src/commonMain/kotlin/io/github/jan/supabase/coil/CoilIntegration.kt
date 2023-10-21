package io.github.jan.supabase.coil

import coil.ImageLoader
import coil.fetch.Fetcher
import coil.request.Options
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.storage.StorageItem
import io.github.jan.supabase.storage.storage

interface CoilIntegration: SupabasePlugin, Fetcher.Factory<StorageItem> {

    class Config()

    companion object : SupabasePluginProvider<Config, CoilIntegration> {

        override val key = "coil"

        override fun create(supabaseClient: SupabaseClient, config: Config): CoilIntegration {
            return CoilIntegrationImpl(supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

    }

}

internal class CoilIntegrationImpl(private val supabaseClient: SupabaseClient) : CoilIntegration {

    override fun create(data: StorageItem, options: Options, imageLoader: ImageLoader): Fetcher {
        return SupabaseStorageFetcher(storage, data)
    }

}
package io.github.jan.supabase.coil

import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.request.RequestContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.storage.StorageItem
import io.github.jan.supabase.storage.storage

/**
 * A plugin that implements [Fetcher.Factory] to support using [StorageItem] as data when creating an [ImageRequest] or using it as a model in Compose Multiplatform.
 */
interface SketchIntegration: SupabasePlugin<SketchIntegration.Config>, Fetcher.Factory {

    /**
     * The configuration for the sketch integration.
     */
    class Config

    companion object : SupabasePluginProvider<Config, SketchIntegration> {

        override val key = "sketch"

        override val logger: SupabaseLogger = SupabaseClient.createLogger("Supabase-Sketch")

        override fun create(supabaseClient: SupabaseClient, config: Config): SketchIntegration {
            return SketchIntegrationImpl(supabaseClient, config)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

    }

}

internal class SketchIntegrationImpl(
    override val supabaseClient: SupabaseClient,
    override val config: SketchIntegration.Config
) : SketchIntegration {

    override fun create(requestContext: RequestContext): Fetcher? {
        val uri = requestContext.request.uri
        if(!isSupabaseUri(uri)) {
            if(uri.scheme == SupabaseStorageFetcher.SCHEME) SketchIntegration.logger.d { "Invalid Supabase URI: $uri" }
            return null
        }
        SketchIntegration.logger.d { "Creating Storage Fetcher" }
        return SupabaseStorageFetcher(supabaseClient.storage, requestContext)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SketchIntegrationImpl
        if (supabaseClient != other.supabaseClient) return false
        if (config != other.config) return false
        return true
    }

    override fun hashCode(): Int {
        var result = supabaseClient.hashCode()
        result = 31 * result + config.hashCode()
        return result
    }

    override fun toString(): String {
        return "SketchIntegrationImpl(supabaseClient=$supabaseClient, config=$config)"
    }

}

/**
 * With the [SketchIntegration] plugin installed, you can use this property to access the coil fetcher factory.
 */
@SupabaseExperimental
val SupabaseClient.sketch: SketchIntegration
    get() = pluginManager.getPlugin(SketchIntegration)
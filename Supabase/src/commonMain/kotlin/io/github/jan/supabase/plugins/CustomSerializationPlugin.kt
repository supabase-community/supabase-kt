package io.github.jan.supabase.plugins

import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.SupabaseSerializer

/**
 * A plugin, which allows to customize the serialization
 */
interface CustomSerializationPlugin {

    /**
     * The serializer used for this module. Defaults to [SupabaseClientBuilder.defaultSerializer], when null.
     */
    val serializer: SupabaseSerializer

}
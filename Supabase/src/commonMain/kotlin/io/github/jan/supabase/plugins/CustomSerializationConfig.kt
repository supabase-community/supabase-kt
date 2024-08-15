package io.github.jan.supabase.plugins

import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.SupabaseSerializer

/**
 * A configuration for a plugin, which allows to customize the serialization
 */
interface CustomSerializationConfig {

    /**
     * The serializer used for this module. Defaults to [SupabaseClientBuilder.defaultSerializer], when null.
     */
    var serializer: SupabaseSerializer?

}
package io.github.jan.supabase.plugins

import io.github.jan.supabase.KotlinXSupabaseSerializer
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseExperimental

/**
 * A configuration for a plugin, which allows to customize the serialization
 */
interface CustomSerializationConfig {

    /**
     * The serializer used for this module. Defaults to [KotlinXSupabaseSerializer]
     */
    @SupabaseExperimental
    var serializer: SupabaseSerializer

}
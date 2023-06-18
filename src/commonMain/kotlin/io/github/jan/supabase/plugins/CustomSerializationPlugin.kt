package io.github.jan.supabase.plugins

import io.github.jan.supabase.KotlinXSupabaseSerializer
import io.github.jan.supabase.SupabaseSerializer

interface CustomSerializationPlugin {

    /**
     * The serializer used for this module. Defaults to [KotlinXSupabaseSerializer]
     */
    var serializer: SupabaseSerializer

}
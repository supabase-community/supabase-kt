package io.github.jan.supabase.plugins

import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal

/**
 * An interface for data that can be serialized with [SupabaseSerializer]
 */
interface SerializableData {

    /**
     * The serializer used to serialize this data
     */
    @SupabaseInternal
    val serializer: SupabaseSerializer

}
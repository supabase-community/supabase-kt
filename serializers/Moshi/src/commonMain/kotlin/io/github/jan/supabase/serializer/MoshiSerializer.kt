package io.github.jan.supabase.serializer

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.supabase.SupabaseSerializer
import kotlin.reflect.KType

/**
 * A [SupabaseSerializer] that uses moshi
 */
class MoshiSerializer(
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
) : SupabaseSerializer {

    @OptIn(ExperimentalStdlibApi::class)
    override fun <T : Any> encode(type: KType, value: T): String = moshi.adapter<T>(type).toJson(value)

    @OptIn(ExperimentalStdlibApi::class)
    override fun <T : Any> decode(type: KType, value: String): T {
        val adapter = moshi.adapter<T>(type)
        return adapter.fromJson(value) ?: error("Failed to decode $value")
    }

}
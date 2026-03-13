package io.github.jan.supabase.storage.vectors.index

/**
 * Supported data types for vectors
 * Currently only [FLOAT32] is supported
 */
enum class VectorDataType {
    FLOAT32;

    val value = this.name.lowercase()
}
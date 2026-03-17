package io.github.jan.supabase.storage.vectors.index

/**
 * Supported data types for vectors.
 *
 * Currently only [FLOAT32] is supported.
 * @property value Lowercase API representation of the data type.
 */
enum class VectorDataType {
    /** 32-bit floating point vector data type. */
    FLOAT32;

    /** Lowercase API representation of the data type. */
    val value = this.name.lowercase()
}
package io.github.jan.supabase.storage.resumable

import kotlin.jvm.JvmInline

/**
 * Represents a fingerprint of an upload url. This is used to identify an upload url in the cache. Use [Fingerprint.invoke] to create a fingerprint
 * @param value The fingerprint value
 */
@JvmInline
value class Fingerprint private constructor(val value: String) {

    private val parts get() = value.split("::")

    /**
     * The source of the file upload
     */
    val source get() = parts[0]

    /**
     * The size of the data
     */
    val size get() = parts[1].toLong()

    companion object {

        /**
         * Creates a fingerprint from the [source] and the [size] of the file
         */
        operator fun invoke(source: String, size: Long) = Fingerprint("$source::$size")

        operator fun invoke(value: String): Fingerprint? {
            val fingerprint = Fingerprint(value)
            val parts = fingerprint.parts
            return if(parts.size != 2) null else fingerprint
        }

    }

}
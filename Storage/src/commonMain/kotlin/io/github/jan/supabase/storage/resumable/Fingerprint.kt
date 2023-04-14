package io.github.jan.supabase.storage.resumable

import kotlin.jvm.JvmInline

/**
 * Represents a fingerprint of an upload url. This is used to identify an upload url in the cache. Use [Fingerprint.invoke] to create a fingerprint
 * @param value The fingerprint value
 */
@JvmInline
value class Fingerprint private constructor(val value: String) {

    private val parts get() = value.split(":")

    /**
     * The bucket id
     */
    val bucket get() = parts[0]

    /**
     * The path of the file upload
     */
    val path get() = parts[1]

    /**
     * The size of the data
     */
    val size get() = parts[2].toLong()


    companion object {

        /**
         * Creates a fingerprint from the [bucket], the [path] and the [size] of the file
         */
        operator fun invoke(bucket: String, path: String, size: Long) = Fingerprint("$bucket:$path:$size")

        operator fun invoke(value: String): Fingerprint? {
            val fingerprint = Fingerprint(value)
            val parts = fingerprint.parts
            return if(parts.size != 3) null else fingerprint
        }

    }

}
package io.github.jan.supabase.storage.resumable

import kotlin.jvm.JvmInline

/**
 * Represents a fingerprint of an upload url. This is used to identify an upload url in the cache. Use [Fingerprint.invoke] to create a fingerprint
 * @param value The fingerprint value
 */
@JvmInline
value class Fingerprint private constructor(val value: String) {

    companion object {

        /**
         * Creates a fingerprint from the [bucket], the [path] and the [size] of the file
         */
        operator fun invoke(bucket: String, path: String, size: Long) = Fingerprint("$bucket:$path:$size")

    }

}
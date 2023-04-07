package io.github.jan.supabase.storage

/**
 * A builder for [Bucket]s
 */
class BucketBuilder {

    /**
     * Whether the bucket should be public
     */
    var public = false

    /**
     * The file size limit for the bucket. E.g. **10.megabytes**
     */
    var fileSizeLimit: FileSizeLimit? = null

    /**
     * The allowed mime types for the bucket
     */
    internal var allowedMimeTypes: List<String>? = null


    /**
     * Sets the file size limit for the bucket
     */
    fun allowedMimeTypes(vararg mimeTypes: String) {
        allowedMimeTypes = mimeTypes.toList()
    }

    /**
     * Sets the file size limit for the bucket
     */
    fun allowedMimeTypes(mimeTypes: List<String>) {
        allowedMimeTypes = mimeTypes
    }

    val Long.bytes get() = FileSizeLimit("${this}b")
    val Long.kilobytes get() = FileSizeLimit("${this}kb")
    val Long.megabytes get() = FileSizeLimit("${this}mb")
    val Long.gigabytes get() = FileSizeLimit("${this}gb")

    val Int.bytes get() = FileSizeLimit("${this}b")
    val Int.kilobytes get() = FileSizeLimit("${this}kb")
    val Int.megabytes get() = FileSizeLimit("${this}mb")
    val Int.gigabytes get() = FileSizeLimit("${this}gb")

}
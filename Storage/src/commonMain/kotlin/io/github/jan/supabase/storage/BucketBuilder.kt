package io.github.jan.supabase.storage

class BucketBuilder {

    var public = false
    var fileSizeLimit: FileSizeLimit? = null
    internal var allowedMimeTypes: List<String>? = null

    fun allowedMimeTypes(vararg mimeTypes: String) {
        allowedMimeTypes = mimeTypes.toList()
    }

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
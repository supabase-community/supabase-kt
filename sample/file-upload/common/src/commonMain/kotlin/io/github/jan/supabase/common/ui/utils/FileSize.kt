package io.github.jan.supabase.common.ui.utils

val Long.fileSize: String
    get() {
        val size = this.toDouble()
        return when {
            size < 1024 -> "${size.toLong()} B"
            size < 1024 * 1024 -> "${(size / 1024).toLong()} KB"
            size < 1024 * 1024 * 1024 -> "${(size / 1024 / 1024).toLong()} MB"
            else -> "${(size / 1024 / 1024 / 1024).toLong()} GB"
        }
    }
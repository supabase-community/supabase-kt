package io.github.jan.supabase.storage

import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel

data class UploadData(val stream: ByteReadChannel, val size: Long)
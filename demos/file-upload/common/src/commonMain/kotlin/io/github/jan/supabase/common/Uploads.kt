package io.github.jan.supabase.common

import io.github.jan.supabase.storage.resumable.Fingerprint
import io.github.jan.supabase.storage.resumable.ResumableUploadState
import io.ktor.utils.io.ByteReadChannel

sealed interface UploadState {

    val fingerprint: Fingerprint

    data class Loading(override val fingerprint: Fingerprint) : UploadState
    data class Loaded(override val fingerprint: Fingerprint, val state: ResumableUploadState) : UploadState

}

expect class MPFile {

    val name: String
    val extension: String
    val source: String
    val size: Long
    val dataProducer: suspend (offset: Long) -> ByteReadChannel

}
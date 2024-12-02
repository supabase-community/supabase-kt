package io.github.jan.supabase.common

import io.supabase.storage.resumable.Fingerprint
import io.supabase.storage.resumable.ResumableUploadState
import io.github.vinceglb.filekit.core.PlatformFile
import io.ktor.utils.io.ByteReadChannel

sealed interface UploadState {

    val fingerprint: Fingerprint

    data class Loading(override val fingerprint: Fingerprint) : UploadState
    data class Loaded(override val fingerprint: Fingerprint, val state: ResumableUploadState) : UploadState

}

expect val PlatformFile.dataProducer: suspend (offset: Long) -> ByteReadChannel
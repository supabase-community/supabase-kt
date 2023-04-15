package io.github.jan.supabase.common

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.common.net.UploadItem
import io.github.jan.supabase.common.net.UploadManager
import io.github.jan.supabase.storage.createOrContinueUpload
import io.github.jan.supabase.storage.resumable.Fingerprint
import io.github.jan.supabase.storage.resumable.ResumableClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

expect open class MPViewModel() {

    val coroutineScope: CoroutineScope

}

class UploadViewModel(
    val supabaseClient: SupabaseClient,
    val uploadManager: UploadManager,
) : MPViewModel() {

    val uploadItems = MutableStateFlow(emptyList<UploadItem>())

    init {
        Napier.base(DebugAntilog())
    }

    fun uploadFile(file: File, path: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                val upload = uploadManager.createOrResumeUpload(file, path)
                upload.progressFlow
                    .onEach {
                        uploadItems.value = uploadItems.value.filter { item -> item.fileName != file.name }
                        uploadItems.value = uploadItems.value + UploadItem(file.name, path, it)
                    }
                    .launchIn(coroutineScope)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun pauseUpload(fingerprint: Fingerprint) {
        uploadManager.pauseUpload(fingerprint)
    }

    fun cancelUpload(fingerprint: Fingerprint) {
        uploadManager.cancelUpload(fingerprint)
    }

}
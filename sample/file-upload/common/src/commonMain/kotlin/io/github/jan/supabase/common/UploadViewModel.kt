package io.github.jan.supabase.common

import androidx.compose.ui.ExperimentalComposeUiApi
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.collections.AtomicMutableMap
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.resumable.Fingerprint
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.github.jan.supabase.storage.resumable.ResumableUpload
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

expect open class MPViewModel() {

    val coroutineScope: CoroutineScope

}

@OptIn(SupabaseInternal::class)
class UploadViewModel(
    private val resumableClient: ResumableClient,
) : MPViewModel() {

    val uploadItems = MutableStateFlow<List<UploadState>>(emptyList())
    private val uploads = AtomicMutableMap<Fingerprint, ResumableUpload>()

    fun queueUpload(file: PlatformFile, path: String) {
        val fingerprint = Fingerprint(file.path ?: file.name, file.getSize() ?: error("Invalid file"))
        if(uploadItems.value.any { it.fingerprint == fingerprint }) {
            return
        }
        uploadItems.value += UploadState.Loading(fingerprint)
        coroutineScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val upload = resumableClient.createOrContinueUpload(
                    file.dataProducer,
                    file.path ?: file.name,
                    file.getSize() ?: error("Invalid file"),
                    path
                ) {
                    upsert = true
                }
                uploads[upload.fingerprint] = upload
                uploadItems.value = uploadItems.value.map {
                    if(it.fingerprint == upload.fingerprint) UploadState.Loaded(upload.fingerprint, upload.stateFlow.value) else it
                }
                upload.stateFlow
                    .onEach {
                        uploadItems.value = uploadItems.value.map { state ->
                            if (state.fingerprint == it.fingerprint) UploadState.Loaded(it.fingerprint, it) else state
                        }
                    }
                    .launchIn(coroutineScope)
            }.onFailure {
                uploadItems.value = uploadItems.value.filter { state -> state.fingerprint != fingerprint }
                it.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    fun queueUploadFromURIs(paths: List<String>) {
        coroutineScope.launch(Dispatchers.Default) {
            parseFileTreeFromURIs(paths).forEach { file ->
                queueUpload(file, file.name)
            }
        }
    }

    fun queueUploadFromPath(path: String) {
        coroutineScope.launch(Dispatchers.Default) {
            parseFileTreeFromPath(path).forEach { file ->
                queueUpload(file, file.name)
            }
        }
    }

    fun startUploading(fingerprint: Fingerprint) {
        coroutineScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val upload = uploads[fingerprint] ?: error("Upload not found")
                upload.startOrResumeUploading()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun pauseUpload(fingerprint: Fingerprint) {
        coroutineScope.launch(Dispatchers.Default) {
            uploads[fingerprint]?.pause()
        }
    }

    fun cancelUpload(fingerprint: Fingerprint) {
        coroutineScope.launch(Dispatchers.Default) {
            uploads[fingerprint]?.cancel()
            uploads.remove(fingerprint)
            uploadItems.value = uploadItems.value.filter { it.fingerprint != fingerprint }
        }
    }

    fun cancelAll(fingerprints: List<Fingerprint>) {
        coroutineScope.launch(Dispatchers.Default) {
            uploadItems.value.forEach {
                if(it.fingerprint in fingerprints) {
                    uploads[it.fingerprint]?.cancel()
                    uploads.remove(it.fingerprint)
                    uploadItems.value = uploadItems.value.filter { state -> state.fingerprint != it.fingerprint }
                }
            }
        }
    }

    fun uploadAll(fingerprints: List<Fingerprint>) {
        uploadItems.value.forEach {
            if(it.fingerprint in fingerprints) {
                if(it !is UploadState.Loaded) return@forEach
                if(it.state.status is UploadStatus.Progress && it.state.paused) {
                    startUploading(it.fingerprint)
                }
            }
        }
    }

    fun loadPreviousUploads() {
        coroutineScope.launch {
            kotlin.runCatching {
                resumableClient.continuePreviousPlatformUploads().map {
                    it.await()
                }.forEach { upload ->
                    uploads[upload.fingerprint] = upload
                    uploadItems.value += UploadState.Loaded(
                        upload.fingerprint,
                        upload.stateFlow.value
                    )
                    upload.stateFlow
                        .onEach {
                            uploadItems.value = uploadItems.value.map { state ->
                                if (state.fingerprint == it.fingerprint) UploadState.Loaded(
                                    it.fingerprint,
                                    it
                                ) else state
                            }
                        }
                        .launchIn(coroutineScope)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

}
package io.github.jan.supabase.storage

import io.github.jan.supabase.storage.resumable.ResumableClient
import io.ktor.util.cio.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.fileSize

suspend fun ResumableClient.startOrResumeUpload(file: File, path: String) = startOrResumeUpload(file.readChannel(), file.length(), path)

suspend fun ResumableClient.startOrResumeUpload(file: Path, path: String) = startOrResumeUpload(file.readChannel(), file.fileSize(), path)
package io.github.jan.supabase.common

import io.github.jan.supabase.storage.continuePreviousFileUploads
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.github.jan.supabase.storage.resumable.ResumableUpload
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Deferred
import java.net.URI
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

actual fun parseFileTreeFromPath(path: String): List<PlatformFile> {
    val file = Paths.get(path)
    return if(file.isDirectory()) {
        file.listDirectoryEntries().map { PlatformFile(it.toFile()) }
    } else {
        listOf(PlatformFile(file.toFile()))
    }
}

actual fun parseFileTreeFromURIs(paths: List<String>): List<PlatformFile> {
    return paths.mapNotNull {
        val file = Paths.get(URI(it))
        if (file.isDirectory()) {
            null //ignore for now, listDirectoryEntries() weirdly breaks compose
        } else {
            PlatformFile(file.toFile())
        }
    }
}

actual suspend fun ResumableClient.continuePreviousPlatformUploads(): List<Deferred<ResumableUpload>> = continuePreviousFileUploads()
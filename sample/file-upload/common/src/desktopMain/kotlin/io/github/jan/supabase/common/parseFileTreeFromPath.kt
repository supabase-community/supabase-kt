package io.github.jan.supabase.common

import io.github.jan.supabase.storage.continuePreviousFileUploads
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.github.jan.supabase.storage.resumable.ResumableUpload
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Deferred

actual fun parseFileTreeFromPath(path: String): List<PlatformFile> =emptyList() /*{
    val file = Paths.get(path)
    return if(file.isDirectory()) {
        //file.listDirectoryEntries()
        emptyList()
    } else {
        listOf(MPFile(file))
    }
}*/

actual fun parseFileTreeFromURIs(paths: List<String>): List<PlatformFile> = emptyList()/*{
    return paths.mapNotNull {
        val file = Paths.get(URI(it))
        if (file.isDirectory()) {
            null //ignore for now, listDirectoryEntries() weirdly breaks compose
        } else {
            MPFile(file)
        }
    }
}*/

actual suspend fun ResumableClient.continuePreviousPlatformUploads(): List<Deferred<ResumableUpload>> = continuePreviousFileUploads()
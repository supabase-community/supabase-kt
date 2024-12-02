package io.github.jan.supabase.common

import androidx.compose.ui.ExperimentalComposeUiApi
import io.supabase.storage.resumable.ResumableClient
import io.supabase.storage.resumable.ResumableUpload
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Deferred

@OptIn(ExperimentalComposeUiApi::class)
expect fun parseFileTreeFromURIs(paths: List<String>): List<PlatformFile>

expect fun parseFileTreeFromPath(path: String): List<PlatformFile>

expect suspend fun ResumableClient.continuePreviousPlatformUploads(): List<Deferred<ResumableUpload>>
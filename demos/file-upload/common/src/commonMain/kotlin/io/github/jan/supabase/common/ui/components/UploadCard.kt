package io.github.jan.supabase.common.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.common.ui.utils.fileSize
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.resumable.ResumableUploadState

fun fileIcon(extension: String): ImageVector {
    return when (extension) {
        in listOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "svg") -> Icons.Filled.Image
        in listOf("mp4", "webm") -> Icons.Filled.VideoFile
        in listOf("mp3", "wav", "ogg", "flac") -> Icons.Filled.AudioFile
        in listOf("txt", "json", "xml", "yml", "docx") -> Icons.Filled.Description
        else -> Icons.Filled.UploadFile
    }
}

val defaultIconSize = 24.0.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadCard(
    state: ResumableUploadState,
    modifier: Modifier = Modifier,
    resume: () -> Unit = {},
    pause: () -> Unit = {},
) {
    var showFullPath by remember { mutableStateOf(false) }
    ElevatedCard({ showFullPath = !showFullPath }, modifier) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(fileIcon(state.fingerprint.source.substringAfterLast(".")), contentDescription = null, modifier = Modifier.size(80.dp, 80.dp).padding(8.dp))
            Text(state.path, maxLines = if(showFullPath) 6 else 1, overflow = TextOverflow.Ellipsis)
            Text(state.fingerprint.size.fileSize, fontSize = 12.sp)
            Box(modifier = Modifier.padding(8.dp)) {
                when {
                    state.status is UploadStatus.Progress && state.paused -> {
                        IconButton(onClick = { resume() }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        }
                    }
                    state.status is UploadStatus.Progress && !state.paused -> {
                        IconButton(onClick = { pause() }) {
                            CircularProgressIndicator(
                                state.progress,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(defaultIconSize)
                            )
                        }
                    }
                    else -> {
                        Icon(Icons.Filled.Check, contentDescription = null)
                    }
                }
            }
        }
    }
}
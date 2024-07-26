package io.github.jan.supabase.common.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.common.UploadState
import io.github.jan.supabase.common.UploadViewModel
import io.github.jan.supabase.common.ui.components.UploadCard
import io.github.jan.supabase.common.ui.utils.applyDragging
import io.github.jan.supabase.storage.resumable.Fingerprint
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun UploadScreen(viewModel: UploadViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadPreviousUploads()
    }
    val states by viewModel.uploadItems.collectAsState(emptyList())
    val isDragging = remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<Fingerprint>() }
    val fileLauncher = rememberFilePickerLauncher(
        type = PickerType.File()
    ) { file ->
        file?.let {
            viewModel.queueUpload(file, file.name)
        }
    }
    Column {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .applyDragging(isDragging) {
                    viewModel.queueUploadFromURIs(it)
                }
        ) {
            items(states, { it.fingerprint.source + it.fingerprint.size }) {
                Box(Modifier.animateItemPlacement()) {
                    if(it is UploadState.Loaded) {
                        UploadCard(
                            it.state,
                            modifier = Modifier.padding(10.dp),
                            { viewModel.startUploading(it.fingerprint) },
                            { viewModel.pauseUpload(it.fingerprint) },
                        )
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = {
                                viewModel.cancelUpload(it.fingerprint)
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = null)
                            }
                        }
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                            Checkbox(
                                checked = it.fingerprint in selected,
                                onCheckedChange = { _ ->
                                    if (it.fingerprint in selected) selected.remove(it.fingerprint) else selected.add(
                                        it.fingerprint
                                    )
                                })
                        }
                    } else {
                        CircularProgressIndicator(Modifier.size(120.dp).padding(10.dp))
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button({
                viewModel.uploadAll(selected)
            }) {
                Text("Upload selected")
            }
            Button({
                viewModel.cancelAll(selected)
            }, modifier = Modifier.padding(start = 10.dp)) {
                Text("Cancel selected")
            }
        }
    }

    if (states.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.clickable {
                    fileLauncher.launch()
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    Icons.Filled.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).padding(10.dp)
                )
                Text("Drop files here", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton({
            fileLauncher.launch()
        }, Modifier.padding(10.dp)) {
            Icon(Icons.Filled.UploadFile, contentDescription = null)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Transparent.copy(alpha = if (isDragging.value) 0.5f else 0f))
    )
}
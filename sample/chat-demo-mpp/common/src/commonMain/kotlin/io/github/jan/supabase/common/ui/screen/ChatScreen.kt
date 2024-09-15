package io.github.jan.supabase.common.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.CurrentPlatformTarget
import io.github.jan.supabase.PlatformTarget
import io.github.jan.supabase.common.ChatViewModel
import io.github.jan.supabase.common.ui.components.MessageCard
import io.github.jan.supabase.common.ui.components.PasswordChangeDialog
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel, user: UserInfo) {
    val messages by viewModel.messages.map { it.reversed() }.collectAsState(emptyList())
    var message by remember { mutableStateOf("") }
    val ownId = user.id
    val reset by viewModel.passwordReset.collectAsState()
    val alert by viewModel.alert.collectAsState()

    LaunchedEffect(Unit) {
        if(CurrentPlatformTarget in listOf(PlatformTarget.JVM, PlatformTarget.JS, PlatformTarget.ANDROID)) {
            viewModel.retrieveMessages()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            reverseLayout = true,
            modifier = Modifier.fillMaxSize().weight(1f)
        ) {
            items(messages, { it.id }) {
                MessageCard(it, it.creatorId == ownId, Modifier.fillMaxSize().padding(8.dp)) {
                    viewModel.deleteMessage(it.id)
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f).padding(8.dp)
            )
            IconButton({
                viewModel.createMessage(message)
                message = ""
            }, enabled = message.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton({
                viewModel.disconnectFromRealtime()
                viewModel.logout()
            }, modifier = Modifier.padding(5.dp)) {
                Icon(Icons.AutoMirrored.Filled.Logout, "Logout")
            }
            TextButton(
                onClick = {
                    viewModel.passwordReset.value = true
                }
            ) {
                Text("Reset password")
            }
        }
    }

    if(reset) {
        PasswordChangeDialog(
            onDismiss = { viewModel.passwordReset.value = false },
            onConfirm = { viewModel.changePassword(it) }
        )
    }

    if(alert != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.alert.value = null
            },
            title = { Text("Info") },
            text = { Text(alert!!) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.alert.value = null
                    }
                ) {
                    Text("Ok")
                }
            }
        )
    }

}
package io.github.jan.supabase.common.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.CurrentPlatformTarget
import io.github.jan.supabase.PlatformTarget
import io.github.jan.supabase.common.ChatViewModel
import io.github.jan.supabase.common.ui.components.MessageCard
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel, user: UserInfo) {
    val messages by viewModel.messages.map { it.reversed() }.collectAsState(emptyList())
    var message by remember { mutableStateOf("") }
    val ownId = user.id

    LaunchedEffect(Unit) {
        if(CurrentPlatformTarget in listOf(PlatformTarget.JVM, PlatformTarget.JS, PlatformTarget.ANDROID)) {
            viewModel.retrieveMessages()
            viewModel.connectToRealtime()
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
                MessageCard(it, it.creatorId == ownId) {
                    viewModel.deleteMessage(it.id)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.scale(0.7f)
            )
            IconButton({
                viewModel.createMessage(message)
                message = ""
            }, enabled = message.isNotBlank()) {
                Icon(Icons.Filled.Send, "Send")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        Button({
            viewModel.disconnectFromRealtime()
            viewModel.logout()
        }, enabled = true, modifier = Modifier.padding(5.dp)) {
            Text("Logout")
        }
    }

}
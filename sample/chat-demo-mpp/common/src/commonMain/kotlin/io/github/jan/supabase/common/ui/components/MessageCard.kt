package io.github.jan.supabase.common.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.common.net.Message

@Composable
fun MessageCard(message: Message, own: Boolean, modifier: Modifier, onDelete: () -> Unit) {
    Box(contentAlignment = if(own) Alignment.CenterEnd else Alignment.CenterStart, modifier = modifier) {
        val backgroundColor = if(own) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        Row(verticalAlignment = Alignment.CenterVertically) {
            ElevatedCard(modifier = Modifier.widthIn(max = 200.dp), colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(message.content)
                    Text("UID: " + message.creatorId, fontSize = 8.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if(own) {
                IconButton(onClick = onDelete, modifier = Modifier.padding(2.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete message")
                }
            }
        }
    }
}
package io.github.jan.supabase.common.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState
import io.github.jan.supabase.common.ui.components.OAuthWebView

@Composable
fun OAuthScreen(url: String, parseFragment: (url: String) -> Unit, disable: () -> Unit) {
    val state = rememberWebViewState(url)
    val navigator = rememberWebViewNavigator()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    onClick = {
                        if (state.isLoading) navigator.reload() else navigator.stopLoading()
                    },
                    modifier = Modifier
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                }
            }
            Text(
                state.lastLoadedUrl ?: "",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = {
                    disable()
                }
            ) {
                Icon(Icons.Filled.Close, contentDescription = null)
            }
        }
        OAuthWebView(
            modifier = Modifier.weight(1f),
            state = state,
            navigator = navigator,
            parseFragment = parseFragment
        )
    }
}
package io.github.jan.supabase.common.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewNavigator
import com.google.accompanist.web.WebViewState
import io.ktor.http.Url

@Composable
fun OAuthWebView(state: WebViewState, navigator: WebViewNavigator, modifier: Modifier = Modifier, parseFragment: (url: String) -> Unit) {
    LaunchedEffect(key1 = state.lastLoadedUrl) {
        state.lastLoadedUrl?.let {
            val url = Url(it)
            if(url.host == "localhost") {
                navigator.loadUrl("about:blank")
                parseFragment(url.fragment)
            }
        }
    }
    WebView(
        modifier = modifier,
        state = state,
        navigator = navigator,
        onCreated = {
            it.settings.javaScriptEnabled = true
            it.settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Ubuntu Chromium/70.0.3538.77 Chrome/70.0.3538.77 Safari/537.36"
        }
    )
}
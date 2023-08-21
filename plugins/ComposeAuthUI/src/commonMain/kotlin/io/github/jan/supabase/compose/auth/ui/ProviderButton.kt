package io.github.jan.supabase.compose.auth.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.gotrue.providers.OAuthProvider

val DEFAULT_ICON_SIZE = 24.dp //from Material3

/**
 * Displays an icon representing the specified OAuth provider.
 *
 * @param provider The OAuth provider for which the icon should be displayed.
 * @param contentDescription The content description for the icon.
 * @param modifier The modifier to be applied to the icon. Note that the size of the icon is not fixed.
 */
@SupabaseExperimental
@Composable
fun ProviderIcon(provider: OAuthProvider, contentDescription: String?, modifier: Modifier = Modifier) {
    providerPainter(provider, LocalDensity.current)?.let {
        Icon(
            painter = it,
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = modifier
        )
    }
}

/**
 * Renders the content for a button that represents an OAuth provider login option.
 *
 * @param provider The OAuth provider to authenticate with.
 * @param text The text to display in the button. Default value is "Login in with" followed by the capitalized provider name.
 */
@SupabaseExperimental
@Composable
fun RowScope.ProviderButtonContent(provider: OAuthProvider, text: String = "Login in with ${provider.name.capitalize()}") {
    ProviderIcon(provider, "Login in with ${provider.name}", Modifier.size(DEFAULT_ICON_SIZE))
    Spacer(Modifier.width(8.dp))
    Text(text)
}

private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
package io.github.jan.supabase.compose.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.providers.Apple
import io.github.jan.supabase.gotrue.providers.Azure
import io.github.jan.supabase.gotrue.providers.Bitbucket
import io.github.jan.supabase.gotrue.providers.Discord
import io.github.jan.supabase.gotrue.providers.Facebook
import io.github.jan.supabase.gotrue.providers.Figma
import io.github.jan.supabase.gotrue.providers.Github
import io.github.jan.supabase.gotrue.providers.Gitlab
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.Kakao
import io.github.jan.supabase.gotrue.providers.Keycloak
import io.github.jan.supabase.gotrue.providers.LinkedIn
import io.github.jan.supabase.gotrue.providers.Notion
import io.github.jan.supabase.gotrue.providers.OAuthProvider
import io.github.jan.supabase.gotrue.providers.Slack
import io.github.jan.supabase.gotrue.providers.Spotify
import io.github.jan.supabase.gotrue.providers.Twitch
import io.github.jan.supabase.gotrue.providers.Twitter
import io.github.jan.supabase.gotrue.providers.WorkOS

@SupabaseInternal
expect fun svgPainter(bytes: ByteArray, density: Density): Painter

@SupabaseInternal
@Composable
@Suppress("CyclomaticComplexMethod")
fun providerPainter(provider: OAuthProvider, density: Density): Painter? {
    val painter by produceState<Painter?>(null) {
        val data = when(provider) {
            Apple -> ProviderIcons.Apple
            Azure -> ProviderIcons.Azure
            Bitbucket -> ProviderIcons.BitBucket
            Discord -> ProviderIcons.Discord
            Facebook -> ProviderIcons.Facebook
            Figma -> ProviderIcons.Figma
            Github -> ProviderIcons.Github
            Gitlab -> ProviderIcons.Gitlab
            Google -> ProviderIcons.Google
            Kakao -> ProviderIcons.Kakao
            Keycloak -> ProviderIcons.Keycloak
            LinkedIn -> ProviderIcons.Linkedin
            Notion -> ProviderIcons.Notion
            Slack -> ProviderIcons.Slack
            Spotify -> ProviderIcons.Spotify
            Twitch -> ProviderIcons.Twitch
            Twitter -> ProviderIcons.Twitter
            WorkOS -> ProviderIcons.WorkOS
            else -> null
        }
        value = data?.let { svgPainter(it.encodeToByteArray(), density) }
    }
    return painter
}
package io.github.jan.supabase.compose.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import io.github.jan.supabase.gotrue.providers.OAuthProvider
import korlibs.io.file.std.resourcesVfs

expect fun svgPainter(bytes: ByteArray, density: Density): Painter

@Composable
fun providerPainter(provider: OAuthProvider, density: Density): Painter? {
    val painter by produceState<Painter?>(null) {
        value = svgPainter(resourcesVfs["icons/${provider.name}.svg"].readBytes(), density)
    }
    return painter
}
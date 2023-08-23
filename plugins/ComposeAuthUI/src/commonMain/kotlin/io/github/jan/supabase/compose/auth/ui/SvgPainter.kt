package io.github.jan.supabase.compose.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.providers.OAuthProvider
import korlibs.io.file.std.resourcesVfs

@SupabaseInternal
expect fun svgPainter(bytes: ByteArray, density: Density): Painter

@SupabaseInternal
@Composable
fun providerPainter(provider: OAuthProvider, density: Density): Painter? {
    val painter by produceState<Painter?>(null) {
        value = svgPainter(resourcesVfs["icons/${provider.name}.svg"].readBytes(), density)
    }
    return painter
}
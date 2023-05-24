package io.github.jan.supabase.common.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import java.nio.ByteBuffer

@Composable
actual fun QRCode(svgData: String, modifier: Modifier) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
    AsyncImage(model = ByteBuffer.wrap(svgData.toByteArray()), imageLoader = imageLoader, modifier = modifier, contentDescription = null)
}
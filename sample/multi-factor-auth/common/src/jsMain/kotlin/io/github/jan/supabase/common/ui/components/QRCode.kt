package io.github.jan.supabase.common.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.Data
import org.jetbrains.skia.svg.SVGDOM

@Composable
actual fun QRCode(svgData: String, modifier: Modifier) {
    val svgDom = remember { SVGDOM(data = Data.makeFromBytes(svgData.encodeToByteArray())) }
    Canvas(modifier = modifier) {
        svgDom.setContainerSize(size.width,size.height)
        drawIntoCanvas { canvas ->
            svgDom.render(canvas.nativeCanvas)
        }
    }
}
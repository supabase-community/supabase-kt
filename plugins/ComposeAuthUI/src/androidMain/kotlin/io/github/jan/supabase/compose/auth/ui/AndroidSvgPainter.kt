package io.github.jan.supabase.compose.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.caverock.androidsvg.PreserveAspectRatio
import com.caverock.androidsvg.SVG
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlin.math.ceil

@Composable
actual fun svgPainter(bytes: ByteArray, density: Density): Painter {
    val svg = SVG.getFromInputStream(ByteReadChannel(bytes).toInputStream())
    return SVGPainter(svg, density)
}

internal class SVGPainter(
    private val dom: SVG,
    private val density: Density
) : Painter() {

    private val defaultSize: Size = run {
        val width = dom.documentWidth
        val height = dom.documentHeight
        if (width == 0f && height == 0f) {
            Size.Unspecified
        } else {
            Size(width, height)
        }
    }

    override val intrinsicSize: Size
        get() {
            return if (defaultSize.isSpecified) {
                defaultSize * density.density
            } else {
                Size.Unspecified
            }
        }

    override fun DrawScope.onDraw() {
        drawSvg(
            size = IntSize(ceil(size.width).toInt(), ceil(size.height).toInt()).toSize()
        )
    }

    private fun DrawScope.drawSvg(size: Size) {
        drawIntoCanvas { canvas ->
            if (dom.documentViewBox == null) {
                dom.setDocumentViewBox(0f, 0f, dom.documentWidth, dom.documentHeight)
            }
            dom.documentWidth = size.width
            dom.documentHeight = size.height
            dom.documentPreserveAspectRatio = PreserveAspectRatio.STRETCH
            dom.renderToCanvas(canvas.nativeCanvas)
        }
    }
}
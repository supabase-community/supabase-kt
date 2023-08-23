package io.github.jan.supabase.compose.auth.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import io.github.jan.supabase.annotations.SupabaseInternal
import org.jetbrains.skia.Data
import org.jetbrains.skia.Rect
import org.jetbrains.skia.svg.SVGDOM
import org.jetbrains.skia.svg.SVGLength
import org.jetbrains.skia.svg.SVGLengthUnit
import org.jetbrains.skia.svg.SVGPreserveAspectRatio
import org.jetbrains.skia.svg.SVGPreserveAspectRatioAlign
import kotlin.math.ceil

@SupabaseInternal
actual fun svgPainter(bytes: ByteArray, density: Density): Painter = loadSvgPainter(bytes, density)

// Note: copied from here:
// https://github.com/JetBrains/compose-multiplatform-core/blob/5c26b7b9f5619ee4f319c6caf43192851b8ee15e/compose/ui/ui/src/desktopMain/kotlin/androidx/compose/ui/res/DesktopSvgResources.desktop.kt#L51
internal fun loadSvgPainter(
    bytes: ByteArray,
    density: Density
): Painter {
    val data = Data.makeFromBytes(bytes)
    return SVGPainter(SVGDOM(data), density)
}

private class SVGPainter(
    private val dom: SVGDOM,
    private val density: Density
) : Painter() {
    private val root = dom.root

    private val defaultSizePx: Size = run {
        val width = root?.width?.withUnit(SVGLengthUnit.PX)?.value ?: 0f
        val height = root?.height?.withUnit(SVGLengthUnit.PX)?.value ?: 0f
        if (width == 0f && height == 0f) {
            Size.Unspecified
        } else {
            Size(width, height)
        }
    }

    init {
        if (root?.viewBox == null && defaultSizePx.isSpecified) {
            root?.viewBox = Rect.makeXYWH(0f, 0f, defaultSizePx.width, defaultSizePx.height)
        }
    }

    override val intrinsicSize: Size
        get() {
            return if (defaultSizePx.isSpecified) {
                defaultSizePx * density.density
            } else {
                Size.Unspecified
            }
        }

    private var previousDrawSize: Size = Size.Unspecified
    private var alpha: Float = 1.0f
    private var colorFilter: ColorFilter? = null

    // with caching into bitmap FPS is 3x-4x higher (tested with idea-logo.svg with 30x30 icons)
    private val drawCache = DrawCache()

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        if (previousDrawSize != size) {
            drawCache.drawCachedImage(
                IntSize(ceil(size.width).toInt(), ceil(size.height).toInt()),
                density = this,
                layoutDirection,
            ) {
                drawSvg(size)
            }
        }

        drawCache.drawInto(this, alpha, colorFilter)
    }

    private fun DrawScope.drawSvg(size: Size) {
        drawIntoCanvas { canvas ->
            root?.width = SVGLength(size.width, SVGLengthUnit.PX)
            root?.height = SVGLength(size.height, SVGLengthUnit.PX)
            root?.preserveAspectRatio = SVGPreserveAspectRatio(SVGPreserveAspectRatioAlign.NONE)
            dom.render(canvas.nativeCanvas)
        }
    }
}
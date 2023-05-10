package io.github.jan.supabase.common.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import java.io.ByteArrayInputStream

@Composable
actual fun QRCode(svgData: String, modifier: Modifier) {
    val painter = remember { loadSvgPainter(svgData.byteInputStream(), Density(1f)) }
    Image(painter, null, modifier)
}
package io.supabase.compose.auth.ui

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import io.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual fun svgPainter(bytes: ByteArray, density: Density): Painter = loadSvgPainter(bytes.inputStream(), density)
package io.github.jan.supabase.common.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun QRCode(
    svgData: String,
    modifier: Modifier = Modifier,
)
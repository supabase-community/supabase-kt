package io.github.jan.supabase.common.ui.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap

actual fun bytesToBitmap(bytes: ByteArray): ImageBitmap = org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
package io.github.jan.supabase.common.ui.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun bytesToBitmap(bytes: ByteArray): ImageBitmap = BitmapFactory.decodeByteArray (bytes, 0, bytes.size).asImageBitmap()
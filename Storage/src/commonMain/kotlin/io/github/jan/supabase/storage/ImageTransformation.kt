package io.github.jan.supabase.storage

import io.github.jan.supabase.storage.ImageTransformation.Resize
import io.ktor.http.ParametersBuilder
import io.ktor.http.formUrlEncode

/**
 * Represents a transformation for an image. Used for [Storage] objects-
 * @property width The width of the image
 * @property height The height of the image
 * @property resize The resize mode
 * @see Resize
 */
data class ImageTransformation(
    var width: Int? = null,
    var height: Int? = null,
    var resize: Resize? = null,
) {

    fun queryString(): String {
        val builder = ParametersBuilder()
        width?.let { builder.append("width", it.toString()) }
        height?.let { builder.append("height", it.toString()) }
        resize?.let { builder.append("resize", it.name) }
        return builder.build().formUrlEncode()
    }

    enum class Resize {
        /**
         * Resizes the image while keeping the aspect ratio to fill a given size and crops projecting parts
         */
        COVER,

        /**
         * Resizes the image while keeping the aspect ratio to fit a given size.
         */
        CONTAIN,

        /**
         * Resizes the image without keeping the aspect ratio.
         */
        FILL
    }

}
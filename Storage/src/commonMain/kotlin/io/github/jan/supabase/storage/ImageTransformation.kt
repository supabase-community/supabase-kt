package io.github.jan.supabase.storage

import io.github.jan.supabase.storage.ImageTransformation.Resize
import io.ktor.http.*

/**
 * Represents a transformation for an image. Used for [Storage] objects-
 * @property width The width of the image
 * @property height The height of the image
 * @property resize The resize mode
 * @property quality The quality of the image. (Percentage 1-100, defaults to 80)
 * @property format Specify in which format you want the image to receive. (Defaults to 'origin', which means the original format
 * @see Resize
 * @see BucketApi.downloadAuthenticated
 * @see BucketApi.downloadPublic
 */
class ImageTransformation {

    var width: Int? = null
    var height: Int? = null
    var resize: Resize? = null
    var quality: Int? = null
        set(value) {
            if(value !in 1..100) throw IllegalArgumentException("Quality must be between 1 and 100")
            field = value
        }
    var format: String? = null

    internal fun queryString(): String {
        val builder = ParametersBuilder()
        width?.let { builder.append("width", it.toString()) }
        height?.let { builder.append("height", it.toString()) }
        resize?.let { builder.append("resize", it.name.lowercase()) }
        quality?.let { builder.append("quality", it.toString()) }
        format?.let { builder.append("format", it) }
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
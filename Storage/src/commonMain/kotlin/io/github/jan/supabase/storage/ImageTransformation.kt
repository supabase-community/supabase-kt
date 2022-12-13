package io.github.jan.supabase.storage

import io.github.jan.supabase.storage.ImageTransformation.Resize

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

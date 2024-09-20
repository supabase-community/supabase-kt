package io.github.jan.supabase.storage

import io.github.jan.supabase.network.HttpRequestOverride

/**
 * Builder for downloading files with additional options
 */
class DownloadOptionBuilder(
    internal var transform: ImageTransformation.() -> Unit = {},
    internal val httpRequestOverrides: MutableList<HttpRequestOverride> = mutableListOf()
) {

    /**
     * Transforms the image before downloading
     * @param transform The transformation to apply
     */
    fun transform(transform: ImageTransformation.() -> Unit) {
        this.transform = transform
    }

    /**
     * Overrides the HTTP request
     * @param override The override to apply
     */
    fun httpOverride(override: HttpRequestOverride) {
        httpRequestOverrides.add(override)
    }

}
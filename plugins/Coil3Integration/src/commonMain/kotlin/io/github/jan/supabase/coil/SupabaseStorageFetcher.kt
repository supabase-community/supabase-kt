package io.github.jan.supabase.coil

import coil3.Extras
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.network.httpHeaders
import coil3.request.Options
import coil3.toUri
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.StorageItem
import io.github.jan.supabase.storage.authenticatedRequest

internal class SupabaseStorageFetcher(
    private val storage: Storage,
    private val item: StorageItem,
    private val options: Options,
    private val imageLoader: ImageLoader
) : Fetcher {

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun fetch(): FetchResult? {
        Coil3Integration.logger.d { "Received fetcher request for item $item" }
        val bucket = storage[item.bucketId]
        val (token, url) = if (item.authenticated) {
            bucket.authenticatedRequest(item.path)
        } else {
            null to bucket.publicUrl(item.path)
        }
        val extras = options.extras.newBuilder()
        if (item.authenticated) {
            extras[Extras.Key.httpHeaders] = options.httpHeaders.newBuilder().apply {
                set("Authorization", "Bearer $token")
            }.build()
        }
        val (fetcher, _) = imageLoader.components.newFetcher(
            url.toUri(),
            options.copy(extras = extras.build()),
            imageLoader
        ) ?: error("No fetcher found for $url")
        return fetcher.fetch()
    }

}
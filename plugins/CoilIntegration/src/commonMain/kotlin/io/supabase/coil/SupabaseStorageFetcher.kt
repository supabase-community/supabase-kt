package io.supabase.coil

import android.net.Uri
import coil.ImageLoader
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import io.supabase.logging.d
import io.supabase.storage.Storage
import io.supabase.storage.StorageItem
import io.supabase.storage.authenticatedRequest

internal class SupabaseStorageFetcher(
    private val storage: Storage,
    private val item: StorageItem,
    private val options: Options,
    private val imageLoader: ImageLoader
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        CoilIntegration.logger.d { "Received fetcher request for item $item" }
        val bucket = storage[item.bucketId]
        val (token, url) = if (item.authenticated) {
            bucket.authenticatedRequest(item.path)
        } else {
            null to bucket.publicUrl(item.path)
        }
        val headers = if (item.authenticated) {
            options.headers.newBuilder().set("Authorization", "Bearer $token").build()
        } else {
            options.headers
        }
        val (fetcher, _) = imageLoader.components.newFetcher(Uri.parse(url), options.copy(headers = headers), imageLoader) ?: error("No fetcher found for $url")
        return fetcher.fetch()
    }

}
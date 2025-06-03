package io.github.jan.supabase.coil

import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.annotation.WorkerThread
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.http.HttpHeaders
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.request.httpHeaders
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.StorageItem
import io.github.jan.supabase.storage.authenticatedRequest

/**
 * Adds support for fetching [StorageItem]s from Supabase Storage in Sketch.
 */
fun ComponentRegistry.Builder.supportSupabaseStorage(supabase: SupabaseClient): ComponentRegistry.Builder = apply {
    addFetcher(supabase.sketch)
}


internal class SupabaseStorageFetcher(
    private val storage: Storage,
    private val request: RequestContext
) : Fetcher {

    @WorkerThread
    override suspend fun fetch(): Result<FetchResult> {
        val item = request.request.uri.toStorageItem()
        SketchIntegration.logger.d { "Received fetcher request for item $item" }
        val bucket = storage[item.bucketId]
        val (token, url) = if (item.authenticated) {
            bucket.authenticatedRequest(item.path)
        } else {
            null to bucket.publicUrl(item.path)
        }
        val newRequest = ImageRequest(request.request.context, url) {
            httpHeaders(HttpHeaders {
                if (item.authenticated) {
                    set("Authorization", "Bearer $token")
                }
            })
        }
        val newContext = RequestContext(request.sketch, newRequest)
        val fetcher = request.sketch.components.newFetcherOrThrow(newContext)
        return fetcher.fetch()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SupabaseStorageFetcher
        if(storage != other.storage) return false
        if (request != other.request) return false
        return true
    }

    override fun toString(): String {
        return "SupabaseStorageFetcher(storage=$storage, request=$request)"
    }

    override fun hashCode(): Int {
        var result = storage.hashCode()
        result = 31 * result + request.hashCode()
        return result
    }

    companion object {
        internal const val SCHEME = "supabase"
    }

}
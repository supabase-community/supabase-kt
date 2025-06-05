package io.github.jan.supabase.coil

import com.github.panpf.sketch.util.Uri
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.storage.StorageItem
import io.ktor.http.parseQueryString

/**
 * Converts a [StorageItem] to a Sketch URI.
 *
 * The URI will have the format `supabase:///<bucketId>/<path>?authenticated=<authenticated>`.
 * This is an experimental feature and may change in the future.
 *
 * @return The Sketch URI representation of the [StorageItem].
 */
@SupabaseExperimental
fun StorageItem.asSketchUri(): String {
    return "supabase:///${bucketId}/${path}?authenticated=${authenticated}"
}

internal fun Uri.toStorageItem(): StorageItem {
    if(pathSegments.isEmpty()) {
        error("Invalid StorageItem URI: $this")
    }
    val bucketId = pathSegments[0]
    val path = pathSegments.drop(1).joinToString("/")
    val query = query?.let { parseQueryString(it) }?.get("authenticated")?.toBoolean() ?: false
    return StorageItem(bucketId = bucketId, path = path, authenticated = query)
}

internal fun isSupabaseUri(uri: Uri): Boolean =
    SupabaseStorageFetcher.SCHEME.equals(uri.scheme, ignoreCase = true)
            && uri.authority?.takeIf { it.isNotEmpty() } == null
            && uri.pathSegments.size > 1
package io.github.jan.supabase.coil

import com.github.panpf.sketch.util.Uri
import io.github.jan.supabase.storage.StorageItem
import io.ktor.http.parseQueryString

fun StorageItem.asSketchUri(): String {
    return "supabase:///${bucketId}/${path}?authenticated=${authenticated}&bla=da"
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
    SupabaseStorageFetcher.SCHEME.equals(uri.scheme, ignoreCase = true).also(::println)
            && uri.authority?.takeIf { it.isNotEmpty() } == null
            && uri.pathSegments.size > 1
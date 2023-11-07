package io.github.jan.supabase.imageloader

import com.seiko.imageloader.component.fetcher.FetchResult
import com.seiko.imageloader.component.fetcher.Fetcher
import com.seiko.imageloader.model.extraData
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.StorageItem
import io.ktor.http.*
import okio.Buffer
import kotlin.collections.set

internal class SupabaseStorageFetcher(
    private val storage: Storage,
    private val item: StorageItem
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val bucket = storage[item.bucketId]
        val data = if (item.authenticated) {
            bucket.downloadAuthenticated(item.path)
        } else {
            bucket.downloadPublic(item.path)
        }
        return FetchResult.OfSource(
            source = Buffer().write(data),
            extra = extraData {
                this["KEY_MIME_TYPE"] =
                    ContentType.defaultForFileExtension(item.path.substringAfterLast(".")).toString()
            }
        )
    }

}

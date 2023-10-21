package io.github.jan.supabase.coil

import coil.fetch.FetchResult
import coil.fetch.Fetcher
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.StorageItem

class SupabaseStorageFetcher(private val storage: Storage, private val item: StorageItem) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val bucket = storage[item.bucketId]
        val data = if(item.authenticated) {
            bucket.downloadAuthenticated(item.path)
        } else {
            bucket.downloadPublic(item.path)
        }
        return FetchResult.Success(data)
    }

}
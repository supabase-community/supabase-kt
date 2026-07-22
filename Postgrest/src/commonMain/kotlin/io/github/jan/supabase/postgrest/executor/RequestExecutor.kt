package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.result.PostgrestResult

/**
 * Request executor interface
 *
 */
sealed interface RequestExecutor {

    /**
     * Execute given PostgrestRequest
     *
     * @param path [String]
     * @param request [PostgrestRequestBuilder]
     * @return [PostgrestResult]
     */
    suspend fun execute(postgrest: Postgrest, path: String, request: PostgrestRequestBuilder): PostgrestResult
}
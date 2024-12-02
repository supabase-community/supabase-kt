package io.supabase.postgrest.executor

import io.supabase.postgrest.Postgrest
import io.supabase.postgrest.request.PostgrestRequest
import io.supabase.postgrest.result.PostgrestResult

/**
 * Request executor interface
 *
 */
sealed interface RequestExecutor {

    /**
     * Execute given PostgrestRequest
     *
     * @param path [String]
     * @param request [PostgrestRequest]
     * @return [PostgrestResult]
     */
    suspend fun execute(postgrest: Postgrest, path: String, request: PostgrestRequest): PostgrestResult
}
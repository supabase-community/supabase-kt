package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PostgrestImpl
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult

@PublishedApi
internal data object RestRequestExecutor : RequestExecutor {
    override suspend fun execute(
        postgrest: Postgrest,
        path: String,
        request: PostgrestRequest
    ): PostgrestResult {
        val authenticatedSupabaseApi = (postgrest as PostgrestImpl).api
        return authenticatedSupabaseApi.request(path) {
            configurePostgrestRequest(request, postgrest)
        }.asPostgrestResult(postgrest)
    }
}
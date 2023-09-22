package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult

@PublishedApi
internal data object RestRequestExecutor : RequestExecutor {
    override suspend fun execute(
        postgrest: Postgrest,
        path: String,
        request: PostgrestRequest
    ): PostgrestResult {
        return postgrest.api.request(path) {
            configurePostgrestRequest(request, postgrest)
        }.asPostgrestResult(postgrest)
    }
}
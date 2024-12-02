package io.supabase.postgrest.executor

import io.supabase.postgrest.Postgrest
import io.supabase.postgrest.PostgrestImpl
import io.supabase.postgrest.request.PostgrestRequest
import io.supabase.postgrest.result.PostgrestResult

@PublishedApi
internal data object RestRequestExecutor : RequestExecutor {

    override suspend fun execute(
        postgrest: Postgrest,
        path: String,
        request: PostgrestRequest
    ): PostgrestResult {
        val authenticatedSupabaseApi = (postgrest as PostgrestImpl).api
        return authenticatedSupabaseApi.request(path) {
            configurePostgrestRequest(request)
        }.asPostgrestResult(postgrest)
    }

}
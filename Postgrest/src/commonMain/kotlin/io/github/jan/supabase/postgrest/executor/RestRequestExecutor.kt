package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult

/**
 * Implementation of [RestRequestExecutor], containing logic of making API to Supabase's Postgrest APIs
 *
 * @property postgrest: instance of Postgrest
 * @constructor Create with instance of Postgrest
 */
class RestRequestExecutor constructor(private val postgrest: Postgrest) : RequestExecutor {
    override suspend fun execute(
        path: String,
        request: PostgrestRequest
    ): PostgrestResult {
        return postgrest.api.request(path) {
            configurePostgrestRequest(request, postgrest)
        }.asPostgrestResult(postgrest)
    }
}
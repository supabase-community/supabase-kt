package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.ktor.client.statement.HttpResponse

interface RequestExecutor {

    suspend fun execute(path: String, request: PostgrestRequest): PostgrestResult
    suspend fun HttpResponse.asPostgrestResult(postgrest: Postgrest): PostgrestResult =
        PostgrestResult(bodyOrNull(), headers, postgrest)

}
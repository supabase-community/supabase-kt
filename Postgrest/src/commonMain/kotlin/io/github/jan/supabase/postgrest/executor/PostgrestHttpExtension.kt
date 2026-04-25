package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText


internal suspend fun HttpResponse.asPostgrestResult(postgrest: Postgrest): PostgrestResult =
    PostgrestResult(bodyAsText(), headers, postgrest)
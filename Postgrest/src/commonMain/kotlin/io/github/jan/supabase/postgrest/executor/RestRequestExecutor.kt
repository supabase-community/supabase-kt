package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PostgrestImpl
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.http.HttpMethod
import kotlin.math.pow
import kotlinx.coroutines.delay

@PublishedApi
internal data object RestRequestExecutor : RequestExecutor {

    private val RETRYABLE_METHODS = setOf(HttpMethod.Get, HttpMethod.Head)
    private val RETRYABLE_STATUS_CODES = setOf(503, 520)
    private const val MAX_RETRY_DELAY_MS = 30_000L

    override suspend fun execute(
        postgrest: Postgrest,
        path: String,
        request: PostgrestRequest
    ): PostgrestResult {
        val maxRetries = postgrest.config.maxRetries
        val shouldRetry = request.retry && request.method in RETRYABLE_METHODS
        val retryCount = if (shouldRetry) maxRetries else 0

        val authenticatedSupabaseApi = (postgrest as PostgrestImpl).api

        var lastException: Exception? = null
        for (attempt in 0..retryCount) {
            try {
                val response = authenticatedSupabaseApi.request(path) {
                    configurePostgrestRequest(request)
                    if (attempt > 0) {
                        headers.append("x-retry-count", attempt.toString())
                    }
                }
                return response.asPostgrestResult(postgrest)
            } catch (e: RestException) {
                lastException = e
                if (attempt < retryCount && e.statusCode in RETRYABLE_STATUS_CODES) {
                    delay(getRetryDelay(attempt))
                } else {
                    throw e
                }
            } catch (e: HttpRequestException) {
                lastException = e
                if (attempt < retryCount) {
                    delay(getRetryDelay(attempt))
                } else {
                    throw e
                }
            }
        }
        throw lastException!!
    }

    private val BASE_DELAY_MS = 1000L

    private fun getRetryDelay(attempt: Int): Long {
        val exponentialDelay = BASE_DELAY_MS * 2.0.pow(attempt).toLong()
        return minOf(exponentialDelay, MAX_RETRY_DELAY_MS)
    }

}

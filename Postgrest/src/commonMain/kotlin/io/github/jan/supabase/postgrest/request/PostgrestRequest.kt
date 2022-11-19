package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PostgrestImpl
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.PostgrestBuilder
import io.github.jan.supabase.postgrest.query.PostgrestResult
import io.github.jan.supabase.postgrest.query.Returning
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.parametersOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

sealed interface PostgrestRequest {

    val body: JsonElement? get() = null
    val method: HttpMethod
    val filter: Map<String, String>
    val prefer: List<String>

    suspend fun execute(path: String, postgrest: Postgrest): PostgrestResult {
        postgrest as PostgrestImpl
        return postgrest.api.request(path) {
            method = this@PostgrestRequest.method
            contentType(ContentType.Application.Json)
            val token  = postgrest.config.jwtToken ?: postgrest.supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull()
            token?.let {
                headers[HttpHeaders.Authorization] = "Bearer $it"
            }
            headers[PostgrestBuilder.HEADER_PREFER] = prefer.joinToString(",")
            setBody(this@PostgrestRequest.body)
            url.parameters.appendAll(parametersOf(filter.mapValues { (_, value) -> listOf(value) }))
        }.asPostgrestResult()
    }

    private suspend fun HttpResponse.asPostgrestResult(): PostgrestResult = PostgrestResult(body())

    data class RPC(
        private val head: Boolean = false,
        private val count: Count? = null,
        override val filter: Map<String, String>,
        override val body: JsonElement? = null,
        ): PostgrestRequest {

        override val method = if(head) HttpMethod.Head else HttpMethod.Post
        override val prefer = if (count != null) listOf("count=${count.identifier}") else listOf()

    }

    data class Select(
        private val head: Boolean = false,
        private val count: Count? = null,
        override val filter: Map<String, String>
    ): PostgrestRequest {

        override val method = if(head) HttpMethod.Head else HttpMethod.Get
        override val prefer = if (count != null) listOf("count=${count.identifier}") else listOf()

    }

    data class Insert(
        override val body: JsonArray,
        private val upsert: Boolean = false,
        private val onConflict: String? = null,
        private val returning: Returning = Returning.REPRESENTATION,
        private val count: Count? = null,
        override val filter: Map<String, String>,
    ): PostgrestRequest {

        override val method = HttpMethod.Post
        override val prefer = buildList {
            add("return=${returning.identifier}")
            if(upsert) add("resolution=merge-duplicates")
            if(count != null) add("count=${count.identifier}")
        }

    }

    data class Update(
        private val returning: Returning = Returning.REPRESENTATION,
        private val count: Count? = null,
        override val filter: Map<String, String>,
        override val body: JsonElement
    ) : PostgrestRequest {

        override val method = HttpMethod.Patch
        override val prefer = buildList {
            add("return=${returning.identifier}")
            if (count != null) add("count=${count.identifier}")
        }

    }

    data class Delete(
        private val returning: Returning = Returning.REPRESENTATION,
        private val count: Count? = null,
        override val filter: Map<String, String>
    ): PostgrestRequest {

        override val method = HttpMethod.Delete
        override val prefer = buildList {
            add("return=${returning.identifier}")
            if (count != null) add("count=${count.identifier}")
        }

    }

}
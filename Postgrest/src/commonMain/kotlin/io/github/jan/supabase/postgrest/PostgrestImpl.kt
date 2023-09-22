package io.github.jan.supabase.postgrest

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.gotrue.authenticatedSupabaseApi
import io.github.jan.supabase.postgrest.query.PostgrestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal class PostgrestImpl(override val supabaseClient: SupabaseClient, override val config: Postgrest.Config) : Postgrest {

    override val apiVersion: Int
        get() = Postgrest.API_VERSION

    override val pluginKey: String
        get() = Postgrest.key

    override var serializer = config.serializer ?: supabaseClient.defaultSerializer

    @OptIn(SupabaseInternal::class)
    override val api = supabaseClient.authenticatedSupabaseApi(this)

    override fun from(table: String): PostgrestBuilder {
        return PostgrestBuilder(
            postgrest = this,
            table = table,
        )
    }

    override fun from(schema: String, table: String): PostgrestBuilder {
        return PostgrestBuilder(
            postgrest = this,
            table = table,
            schema = schema,
        )
    }

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        val body = response.bodyOrNull<PostgrestErrorResponse>() ?: PostgrestErrorResponse("Unknown error")
        return when(response.status) {
            HttpStatusCode.Unauthorized -> UnauthorizedRestException(body.message, response, body.details ?: body.hint)
            HttpStatusCode.NotFound -> NotFoundRestException(body.message, response, body.details ?: body.hint)
            HttpStatusCode.BadRequest -> BadRequestRestException(body.message, response, body.details ?: body.hint)
            else -> UnknownRestException(body.message, response, body.details ?: body.hint)
        }
    }

}
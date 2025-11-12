package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.network.NetworkInterceptor
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders

object SessionNetworkInterceptor: NetworkInterceptor.Before {

    override suspend fun call(builder: HttpRequestBuilder, supabase: SupabaseClient) {
        val authHeader = builder.headers[HttpHeaders.Authorization]?.replace("Bearer ", "")

    }

}
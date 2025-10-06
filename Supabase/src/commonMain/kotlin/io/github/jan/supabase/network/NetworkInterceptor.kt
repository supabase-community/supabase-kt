package io.github.jan.supabase.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse

@SupabaseInternal
sealed interface NetworkInterceptor {

    fun interface Before: NetworkInterceptor {

        fun call(builder: HttpRequestBuilder, supabase: SupabaseClient)

    }

    fun interface After: NetworkInterceptor {

        fun call(response: HttpResponse, supabase: SupabaseClient)

    }

}
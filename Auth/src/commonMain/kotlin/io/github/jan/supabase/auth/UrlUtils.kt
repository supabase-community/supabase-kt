package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.client.request.HttpRequestBuilder

@SupabaseInternal
fun HttpRequestBuilder.redirectTo(url: String) {
    this.url.parameters["redirect_to"] = url
}
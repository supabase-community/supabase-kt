package io.supabase.postgrest

import kotlinx.serialization.Serializable

@Serializable
internal data class PostgrestErrorResponse(
    val message: String,
    val hint: String? = null,
    val details: String? = null,
    val code: String? = null,
)

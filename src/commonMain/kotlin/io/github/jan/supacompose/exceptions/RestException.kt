package io.github.jan.supacompose.exceptions

class RestException(val status: Int, val error: String, description: String, headers: List<String> = emptyList()) : Exception("$error: $description (HTTP status $status) \nHeaders: $headers")

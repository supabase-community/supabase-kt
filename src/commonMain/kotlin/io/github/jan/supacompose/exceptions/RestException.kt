package io.github.jan.supacompose.exceptions

class RestException(val status: Int, val error: String, description: String, headers: List<String> = emptyList()) : Exception("$description (HTTP status $status) Headers: $headers")

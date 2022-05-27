package io.github.jan.supacompose.exceptions

class RestException(val status: Int, val error: String, val description: String) : Exception("$description (HTTP status $status)")

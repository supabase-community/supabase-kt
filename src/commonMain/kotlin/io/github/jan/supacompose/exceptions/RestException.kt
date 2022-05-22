package io.github.jan.supacompose.exceptions

class RestException(status: Int, message: String): Exception("$message (HTTP status $status)")

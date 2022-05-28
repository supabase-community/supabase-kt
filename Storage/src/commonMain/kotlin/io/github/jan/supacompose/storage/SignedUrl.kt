package io.github.jan.supacompose.storage

import kotlinx.serialization.Serializable

@Serializable
data class SignedUrl(val error: String?, val signedURL: String, val path: String)

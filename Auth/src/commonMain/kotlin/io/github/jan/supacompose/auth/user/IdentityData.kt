package io.github.jan.supacompose.auth.user


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdentityData(
    @SerialName("sub")
    val sub: String
)
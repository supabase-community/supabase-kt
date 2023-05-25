@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")
package io.github.jan.supabase.gotrue.user


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppMetadata(
    @SerialName("provider")
    val provider: String,
    @SerialName("providers")
    val providers: List<String>
)
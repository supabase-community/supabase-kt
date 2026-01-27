package io.github.jan.supabase.testing

import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64

fun JsonObject.encodeBase64(): String {
    return Base64.UrlSafe
        .withPadding(Base64.PaddingOption.ABSENT)
        .encode(this.toString().encodeToByteArray())
}

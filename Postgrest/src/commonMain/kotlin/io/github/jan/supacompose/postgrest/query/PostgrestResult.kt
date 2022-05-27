package io.github.jan.supacompose.postgrest.query

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class PostgrestResult(val body: String, val statusCode: Int) {

    inline fun <reified T> decodeAs(json: Json = Json): T = json.decodeFromString(body)

}

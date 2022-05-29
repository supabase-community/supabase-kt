package io.github.jan.supacompose.realtime

import io.github.jan.supacompose.supabaseJson
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@kotlinx.serialization.Serializable
data class RowItem(
    val name: String,
    val type: String
)

@kotlinx.serialization.Serializable
data class RealtimeChannelMessage(
    val columns: List<RowItem>,
    @SerialName("commit_timestamp")
    val commitTimestamp: Instant,
    val errors: JsonElement?,
    @SerialName("old_record")
    val oldRecord: JsonObject?,
    val record: JsonObject,
    val schema: String,
    val table: String,
    @SerialName("type")
    val action: RealtimeChannel.Action
) {

    inline fun <reified T> record() = supabaseJson.decodeFromJsonElement<T>(record)

}

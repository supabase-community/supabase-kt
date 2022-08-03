package io.github.jan.supacompose.realtime.events

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Column(val name: String, val type: String)

sealed interface ChannelAction {

    val columns: List<Column>
    val commitTimestamp: Instant
    val oldRecord: JsonObject? get() = null
    val record: JsonObject? get() = null

    @Serializable
    data class Insert(
        override val record: JsonObject,
        override val columns: List<Column>,
        override val commitTimestamp: Instant
    ): ChannelAction

    @Serializable
    data class Update(
        override val record: JsonObject,
        @SerialName("old_record")
        override val oldRecord: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ): ChannelAction

    @Serializable
    data class Delete(
        override val record: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ): ChannelAction

    @Serializable
    data class Select(
        override val record: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ): ChannelAction

}
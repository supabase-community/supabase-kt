package io.github.jan.supabase.realtime

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Contains information about a column
 * @property name the name of the column
 * @property type the type of the column
 */
@Serializable
data class Column(val name: String, val type: String)

/**
 * Represents a postgres action, containing a record.
 */
interface HasRecord {

    /**
     * The new record, if the action has one
     */
    val record: JsonObject
}

/**
 * Represents a postgres action, containing an old record.
 */
interface HasOldRecord {

    /**
     * The old record, if the action has one
     */
    val oldRecord: JsonObject
}

/**
 * Represents a postgres action
 */
sealed interface PostgresAction {

    /**
     * Contains data of the row's columns
     */
    val columns: List<Column>

    /**
     * The time when the action was committed
     */
    val commitTimestamp: Instant

    /**
     * Represents a postgres insert action
     */
    @Serializable
    data class Insert(
        override val record: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ) : PostgresAction, HasRecord

    /**
     * Represents a postgres update action
     */
    @Serializable
    data class Update(
        override val record: JsonObject,
        @SerialName("old_record")
        override val oldRecord: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ) : PostgresAction, HasRecord, HasOldRecord

    /**
     * Represents a postgres delete action
     */
    @Serializable
    data class Delete(
        @SerialName("old_record")
        override val oldRecord: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ) : PostgresAction, HasOldRecord

    /**
     * Represents a postgres select action
     */
    @Serializable
    data class Select(
        override val record: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ) : PostgresAction, HasRecord

}

/**
 * Decodes [HasRecord.record] as [T] and returns it or returns null when it cannot be decoded as [T]
 * @param json the [Json] instance to use for decoding
 */
inline fun <reified T> HasRecord.decodeRecordOrNull(json: Json = Json): T? {
    return try {
        json.decodeFromJsonElement<T>(record)
    } catch (e: Exception) {
        null
    }
}

/**
 * Decodes [HasOldRecord.oldRecord] as [T] and returns it or returns null when it cannot be decoded as [T]
 * @param json the [Json] instance to use for decoding
 */
inline fun <reified T> HasOldRecord.decodeOldRecordOrNull(json: Json = Json): T? {
    return try {
        json.decodeFromJsonElement<T>(oldRecord)
    } catch (e: Exception) {
        null
    }
}

/**
 * Decodes [HasRecord.record] as [T] and returns it
 * @param json the [Json] instance to use for decoding
 */
inline fun <reified T> HasRecord.decodeRecord(json: Json = Json) = json.decodeFromJsonElement<T>(record)

/**
 * Decodes [HasOldRecord.oldRecord] as [T] and returns it
 * @param json the [Json] instance to use for decoding
 */
inline fun <reified T> HasOldRecord.decodeOldRecord(json: Json = Json) = json.decodeFromJsonElement<T>(oldRecord)
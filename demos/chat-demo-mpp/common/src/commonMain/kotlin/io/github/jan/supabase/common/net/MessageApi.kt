package io.github.jan.supabase.common.net

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class Message(
    val id: Int,
    val content: String,
    @SerialName("creator_id")
    val creatorId: String,
    @SerialName("created_at")
    val createdAt: Instant,
)

sealed interface MessageApi {

    suspend fun retrieveMessages(): List<Message>

    suspend fun createMessage(content: String): Message

    suspend fun deleteMessage(id: Int)

}

internal class MessageApiImpl(
    private val client: SupabaseClient
) : MessageApi {

    private val table = client.postgrest["messages"]

    override suspend fun retrieveMessages(): List<Message> = table.select().decodeList()

    override suspend fun createMessage(content: String): Message {
        val user = (client.gotrue.currentSessionOrNull() ?: error("No session available")).user ?: error("No user available")
        return table.insert(buildJsonObject {
           put("content", content)
           put("creator_id", user.id)
        }).decodeSingle()
    }

    override suspend fun deleteMessage(id: Int) {
        table.delete {
            Message::id eq id
        }
    }


}
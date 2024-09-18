package io.github.jan.supabase.common.net

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow
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

    suspend fun retrieveMessages(): Flow<List<Message>>

    suspend fun createMessage(content: String): Message

    suspend fun deleteMessage(id: Int)

}

internal class MessageApiImpl(
    private val client: SupabaseClient
) : MessageApi {

    private val table = client.postgrest["messages"]

    @OptIn(SupabaseExperimental::class)
    override suspend fun retrieveMessages(): Flow<List<Message>> = table.selectAsFlow(Message::id)

    override suspend fun createMessage(content: String): Message {
        val user = (client.auth.currentSessionOrNull() ?: error("No session available")).user ?: error("No user available")
        return table.insert(buildJsonObject {
           put("content", content)
           put("creator_id", user.id)
        }) {
            select()
        }.decodeSingle()
    }

    override suspend fun deleteMessage(id: Int) {
        table.delete {
            filter {
                Message::id eq id
            }
        }
    }


}
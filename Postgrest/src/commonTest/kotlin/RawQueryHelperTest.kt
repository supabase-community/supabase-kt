import io.github.jan.supabase.postgrest.query.RawQueryHelper
import kotlin.test.Test
import kotlin.test.assertEquals

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class RawQueryHelperTest {

    @Test
    fun queryWithMultipleForeignKeys_whenClassHasOtherClasses() {
        val result = RawQueryHelper.queryWithMultipleForeignKeys<MessageDto>()
        assertEquals(
            """
            content,
            recipient: recipient(id, username, email),
            sender: sender(id, username, email),
            created_at
        """.trimIndent(), result
        )
    }

    @Test
    fun queryWithMultipleForeignKeys_whenClassDoesNotHaveOtherClasses() {
        val result = RawQueryHelper.queryWithMultipleForeignKeys<UserDto>()
        assertEquals(
            """
            userdto(id, username, email)
        """.trimIndent(), result
        )
    }

    @Test
    fun queryWithMultipleForeignKeys_whenClassHasOneProperty() {
        val result = RawQueryHelper.queryWithMultipleForeignKeys<UserSingleProperty>()
        assertEquals(
            """
            usersingleproperty(name)
        """.trimIndent(), result
        )
    }

    @Test
    fun queryWithForeignKey() {
        val result = RawQueryHelper.queryWithForeignKey<System>()
        println(result)
        assertEquals(
            """
                name,
                address,
                owner(name)
        """.trimIndent(), result
        )
    }


    @Test
    fun queryWithForeignKey_whenForeignKeyNameIsCustomized() {
        val result = RawQueryHelper.queryWithForeignKey<System>("custom")
        println(result)
        assertEquals(
            """
                name,
                address,
                custom(name)
        """.trimIndent(), result
        )
    }
}


@Serializable
private data class MessageDto(
    @SerialName("content")
    val content: String,

    @SerialName("recipient")
    val recipient: UserDto,

    @SerialName("sender")
    val createdBy: UserDto,

    @SerialName("created_at")
    val createdAt: String,
)

@Serializable
private data class UserDto(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String,
)

@Serializable
private data class UserSingleProperty(
    @SerialName("name")
    val id: String,
)

@Serializable
private data class System(
    @SerialName("name")
    val name: String,

    @SerialName("address")
    val address: String,

    @SerialName("owner")
    val owner: UserSingleProperty,
)
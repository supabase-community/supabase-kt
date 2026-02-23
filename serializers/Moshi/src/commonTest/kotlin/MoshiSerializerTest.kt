import io.github.jan.supabase.decode
import io.github.jan.supabase.encode
import io.github.jan.supabase.serializer.MoshiSerializer
import io.github.jan.supabase.testing.createMockedSupabaseClient
import org.junit.Test
import kotlin.test.assertEquals

class MoshiSerializerTest {

    @Test
    fun testMoshiSerializer() {
        val serializer = MoshiSerializer()
        val supabaseClient = createMockedSupabaseClient(
            configuration = {
                defaultSerializer = serializer
            }
        )
        assertEquals(serializer, supabaseClient.defaultSerializer)
        val value = mapOf("key" to "value")
        val encoded = serializer.encode(value)
        val decoded = serializer.decode<Map<String, String>>(encoded)
        assertEquals(value, decoded)
    }

    @Test
    fun testMoshiSerializerForNulls() {
        val serializer = MoshiSerializer()
        val supabaseClient = createMockedSupabaseClient(
            configuration = {
                defaultSerializer = serializer
            })
        assertEquals(serializer, supabaseClient.defaultSerializer)
        val value = mapOf<String, String?>("key" to null)
        val encoded = serializer.encode(value)
        val decoded = serializer.decode<Map<String, String?>>(encoded)
        assertEquals(value, decoded)
    }
}
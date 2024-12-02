import io.github.jan.supabase.serializer.MoshiSerializer
import io.supabase.encode
import io.supabase.testing.createMockedSupabaseClient
import org.junit.Test
import kotlin.reflect.typeOf
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
        val decoded = serializer.decode<Map<String, String>>(typeOf<Map<String, String>>(), encoded)
        assertEquals(value, decoded)
    }

}
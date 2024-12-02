import io.github.jan.supabase.serializer.JacksonSerializer
import io.supabase.encode
import io.supabase.testing.createMockedSupabaseClient
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

class JacksonSerializerTest {

    @Test
    fun testJacksonSerializer() {
        val serializer = JacksonSerializer()
        val supabaseClient = createMockedSupabaseClient(
            configuration = {
                defaultSerializer = serializer
            }
        )
        assertEquals(serializer,supabaseClient.defaultSerializer)
        val value = mapOf("key" to "value")
        val encoded = serializer.encode(value)
        val decoded = serializer.decode<Map<String, String>>(typeOf<Map<String, String>>(), encoded)
        assertEquals(value, decoded)
    }

}
import io.github.jan.supabase.SupabaseSerializer
import kotlin.reflect.KType

class DummySerializer: SupabaseSerializer {
    override fun <T : Any> encode(type: KType, value: T): String {
        TODO("Not yet implemented")
    }

    override fun <T : Any> decode(type: KType, value: String): T {
        TODO("Not yet implemented")
    }
}
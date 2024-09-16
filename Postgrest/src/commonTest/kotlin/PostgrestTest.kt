import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.postgrest.Postgrest
import kotlin.test.Test

class PostgrestTest {

    private val configureClient: SupabaseClientBuilder.() -> Unit = {
        install(Postgrest)
    }

    @Test
    fun test() {
        val supabase = createM
    }

}
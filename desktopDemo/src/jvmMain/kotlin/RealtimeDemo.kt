import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime

suspend fun main() {
    Napier.base(DebugAntilog()) //to see supabase-kt logs
    val client = createSupabaseClient(
        supabaseUrl = "https://URL.supabase.co/realtime/v1",
        supabaseKey = "API_KEY"
    ) {
        ignoreModulesInUrl = true
        install(Realtime)
    }
    client.realtime.connect()
    val channel = client.realtime.createChannel("channelId")
    val changes = channel.postgresChangeFlow<PostgresAction.Update>("public") {
        table = "test"
    }
    channel.join()
    changes.collect {
        println(it)
    }
}
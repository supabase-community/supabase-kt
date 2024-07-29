import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.CallbackManager
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.PostgresJoinConfig
import io.github.jan.supabase.realtime.Presence
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresListDataFlow
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import io.github.jan.supabase.realtime.presenceDataFlow
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@Serializable
data class DummyData(val key: Int, val content: String = "")

class RealtimeExtTest {

    @Test
    fun testPresenceDataFlow() {
        runTest {
            val firstPresenceList = listOf(
                DummyData(0),
                DummyData(1),
                DummyData(2),
            )
            val secondPresenceList = listOf(
                DummyData(0),
                DummyData(1),
                DummyData(3),
            )
            createTestClient(
                wsHandler = { i, o ->
                    handleSubscribe(i, o, "channelId")
                },
                supabaseHandler = {
                    val channel = it.channel("channelId")
                    coroutineScope {
                        launch {
                            val broadcastFlow = channel.presenceDataFlow<DummyData>()
                            broadcastFlow.take(3).collectIndexed { index, value ->
                                when(index) {
                                    0 -> assertContentEquals(firstPresenceList, value)
                                    1 -> assertContentEquals(secondPresenceList, value)
                                    2 -> assertContentEquals(emptyList(), value)
                                }
                            }
                        }
                        launch {
                            channel.subscribe(true)
                            channel.callbackManager.triggerPresence(firstPresenceList, emptyList())
                            channel.callbackManager.triggerPresence(listOf(DummyData(3)), listOf(DummyData(2)))
                            channel.callbackManager.triggerPresence(emptyList(), secondPresenceList)
                        }
                    }.join()
                }
            )
        }
    }

    @Test
    fun testPostgresListDataFlow() { //Maybe there is a better way to test this
        runTest {
            createTestClient(
                wsHandler = { i, o ->
                    handleSubscribe(i, o, "channelId")
                },
                supabaseHandler = {
                    val channel = it.channel("channelId")
                    val dataFlow = channel.postgresListDataFlow("public", "table", primaryKey = DummyData::key)
                    val initialData = dataFlow.first() //The initial data has to be waited for first because otherwise the callbacks are not registered yet
                    assertContentEquals(listOf(DummyData(0, "content")), initialData)
                    //Now we can test updates
                    coroutineScope {
                        launch {
                            dataFlow.drop(1).take(4).collectIndexed { index, value ->
                                when(index) {
                                    0 -> assertContentEquals(listOf(DummyData(0, "content4")), value) //2.
                                    1 -> assertContentEquals(listOf(DummyData(0, "content4"), DummyData(1, "content2")), value)//3.
                                    2 -> assertContentEquals(listOf(DummyData(0, "content3"), DummyData(1, "content2")), value)//4.
                                    3 -> assertContentEquals(listOf(DummyData(0, "content3")), value) //5.
                                }
                            }
                        }
                        launch {
                            channel.subscribe(true)
                            channel.callbackManager.setServerChanges(listOf(PostgresJoinConfig("public", "table", null, "*", 0)))
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Update>(DummyData(0, "content4"), DummyData(0, "content")) //2.
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Insert>(DummyData(1, "content2"), null) //3.
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Update>(DummyData(0, "content3"), DummyData(0, "content")) //4.
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Delete>(null, DummyData(1)) //5.
                        }
                    }.join()
                },
                mockEngineHandler = {
                    respond(Json.encodeToJsonElement(listOf(DummyData(0, "content"))).toString()) //1.
                },
                supabaseConfig = {
                    install(Postgrest)
                }
            )
        }
    }

    @Test
    fun testPostgresSingleDataFlow() { //Maybe there is a better way to test this
        runTest {
            createTestClient(
                wsHandler = { i, o ->
                    handleSubscribe(i, o, "channelId")
                },
                supabaseHandler = {
                    val channel = it.channel("channelId")
                    val dataFlow = channel.postgresSingleDataFlow("public", "table", primaryKey = DummyData::key) {
                        //Does not really matter
                    }
                    val initialData = dataFlow.first() //The initial data has to be waited for first because otherwise the callbacks are not registered yet
                    assertEquals(DummyData(0, "content"), initialData)
                    //Now we can test updates
                    coroutineScope {
                        launch {
                            dataFlow.drop(1).collect { value ->
                                assertEquals(DummyData(0, "content4"), value) //2.
                            }
                        }
                        launch {
                            channel.subscribe(true)
                            channel.callbackManager.setServerChanges(listOf(PostgresJoinConfig("public", "table", "key=eq.0", "*", 0))) //The method will add a filter for the primary key. In this case 0.
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Update>(DummyData(0, "content4"), DummyData(0, "content")) //2.
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Delete>(null, DummyData(0)) //3.
                        }
                    }.join()
                },
                mockEngineHandler = {
                    respond(Json.encodeToJsonElement(DummyData(0, "content")).toString()) //1.
                },
                supabaseConfig = {
                    install(Postgrest)
                }
            )
        }
    }

    private inline fun <reified T: PostgresAction> CallbackManager.triggerPostgresChange(
        record: DummyData?,
        oldRecord: DummyData?
    ) {
        when(T::class) {
            PostgresAction.Insert::class -> triggerPostgresChange(listOf(0), PostgresAction.Insert(encode(record), listOf(), Clock.System.now(), KotlinXSerializer()))
            PostgresAction.Update::class -> triggerPostgresChange(listOf(0), PostgresAction.Update(encode(record), encode(oldRecord), listOf(), Clock.System.now(), KotlinXSerializer()))
            PostgresAction.Delete::class -> triggerPostgresChange(listOf(0), PostgresAction.Delete(encode(oldRecord), listOf(), Clock.System.now(), KotlinXSerializer()))
            else -> error("Unknown event type ${T::class}")
        }
    }

    private fun encode(record: DummyData?) = Json.encodeToJsonElement(record).jsonObject

    private fun CallbackManager.triggerPresence(
        joins: List<DummyData>,
        leaves: List<DummyData>
    ) {
        val joinsMap = joins.associateBy { it.key.toString() }.mapValues { Presence(it.key, Json.encodeToJsonElement(it.value).jsonObject) }
        val leavesMap = leaves.associateBy { it.key.toString() }.mapValues { Presence(it.key, Json.encodeToJsonElement(it.value).jsonObject) }
        triggerPresenceDiff(joinsMap, leavesMap)
    }

}
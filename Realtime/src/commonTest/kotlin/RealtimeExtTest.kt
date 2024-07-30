import app.cash.turbine.test
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.CallbackManager
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.PostgresJoinConfig
import io.github.jan.supabase.realtime.Presence
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresListDataFlow
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import io.github.jan.supabase.realtime.presenceDataFlow
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.testing.pathAfterVersion
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
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

    //The way this works is that we trigger presence events via the callback manager and then check if the flow receives the correct data
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
                            broadcastFlow.test(FLOW_TIMEOUT) {
                                assertContentEquals(firstPresenceList, awaitItem())
                                assertContentEquals(secondPresenceList, awaitItem())
                                assertContentEquals(emptyList(), awaitItem())
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
            val filter = FilterOperation("id", FilterOperator.EQ, "0")
            createTestClient(
                wsHandler = { i, o ->
                    handleSubscribe(i, o, "channelId")
                },
                supabaseHandler = {
                    val channel = it.channel("channelId")
                    val dataFlow = channel.postgresListDataFlow(
                        "public",
                        "table",
                        filter = filter,
                        primaryKey = DummyData::key
                    )
                    dataFlow.first() //The initial data has to be waited for first because otherwise the callbacks are not registered yet
                    //Now we can test updates
                    coroutineScope {
                        launch {
                            dataFlow.test(FLOW_TIMEOUT) {
                                assertContentEquals(listOf(DummyData(0, "first")), awaitItem()) //1.
                                assertContentEquals(listOf(DummyData(0, "second")), awaitItem()) //2.
                                assertContentEquals(listOf(DummyData(0, "second"), DummyData(1, "third")), awaitItem())//3.
                                assertContentEquals(listOf(DummyData(0, "fourth"), DummyData(1, "third")), awaitItem())//4.
                                assertContentEquals(listOf(DummyData(0, "fourth")), awaitItem()) //5.
                            }
                        }
                        launch {
                            channel.subscribe(true)
                            channel.callbackManager.setServerChanges(listOf(PostgresJoinConfig("public", "table", "id=eq.0", "*", 0)))
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Update>(DummyData(0, "second"), DummyData(0, "first")) //2.
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Insert>(DummyData(1, "third"), null) //3.
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Update>(DummyData(0, "fourth"), DummyData(0, "second")) //4.
                            channel.callbackManager.triggerPostgresChange<PostgresAction.Delete>(null, DummyData(1)) //5.
                        }
                    }.join()
                },
                mockEngineHandler = {
                    assertEquals("/table", it.url.pathAfterVersion())
                    val urlFilter = it.url.parameters["id"]
                    assertEquals("eq.0", urlFilter)
                    respond(Json.encodeToJsonElement(listOf(DummyData(0, "first"))).toString()) //1.
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
                        eq("id", 0)
                    }
                    dataFlow.first() //The initial data has to be waited for first because otherwise the callbacks are not registered yet

                    //Now we can test updates
                    coroutineScope {
                        launch {
                            dataFlow.test(FLOW_TIMEOUT) {
                                assertEquals(DummyData(0, "content"), awaitItem()) //1.
                                assertEquals(DummyData(0, "content4"), awaitItem()) //2.
                                awaitComplete() //The flow will complete after the deletion
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
                    assertEquals("/table", it.url.pathAfterVersion())
                    val urlFilter = it.url.parameters["id"]
                    assertEquals("eq.0", urlFilter)
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
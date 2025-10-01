package io.github.jan.supabase.realtime

import io.github.jan.supabase.createSupabaseClient
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun createChannel(
    presenceEnabled: Boolean = false,
    topic: String = "realtime:presence_test"
): RealtimeChannelImpl {
    val supabaseClient = createSupabaseClient(
        supabaseUrl = "https://projectref.supabase.co",
        supabaseKey = "project-anon-key"
    ) { }
    val realtimeImpl = RealtimeImpl(supabaseClient, Realtime.Config())
    return RealtimeChannelImpl(
        realtimeImpl,
        topic,
        BroadcastJoinConfig(acknowledgeBroadcasts = false, receiveOwnBroadcasts = false),
        PresenceJoinConfig(key = "", enabled = presenceEnabled),
    )
}

private fun systemAckMessage(topic: String): RealtimeMessage {
    val payload = buildJsonObject {
        put("status", "ok")
    }
    return RealtimeMessage(topic, RealtimeChannel.CHANNEL_EVENT_SYSTEM, payload, null)
}

private fun extractPresenceEnabled(payload: JsonObject): Boolean {
    val presenceConfig = payload["config"]?.jsonObject?.get("presence")?.jsonObject
        ?: error("Presence config missing in payload: $payload")
    return presenceConfig["enabled"]?.jsonPrimitive?.boolean
        ?: error("Presence enabled flag missing in payload: $payload")
}

class RealtimePresenceTest {

    @Test
    fun joinPayloadDefaultsToPresenceDisabled() {
        val channel = createChannel()
        val payload = channel.createJoinPayload()
        assertFalse(extractPresenceEnabled(payload))
    }

    @Test
    fun joinPayloadRespectsExplicitPresenceConfiguration() {
        val channel = createChannel(presenceEnabled = true)
        val payload = channel.createJoinPayload()
        assertTrue(extractPresenceEnabled(payload))
    }

    @Test
    fun joinPayloadEnablesPresenceWhenCallbacksRegistered() {
        val channel = createChannel()
        channel.callbackManager.addPresenceCallback {}
        channel.onPresenceCallbackAdded()

        val payload = channel.createJoinPayload()
        assertTrue(extractPresenceEnabled(payload))
    }

    @Test
    fun presenceNotEnabledBeforeSubscriptionWhenListenerAdded() {
        val channel = createChannel()
        channel.callbackManager.addPresenceCallback {}
        channel.onPresenceCallbackAdded()

        assertFalse(channel.isPresenceEnabledForJoin())
    }

    @Test
    fun presenceEnabledAfterSubscribeWhenListenerAddedBeforeAck() {
        val topic = "realtime:early_presence"
        val channel = createChannel(topic = topic)
        channel.callbackManager.addPresenceCallback {}
        channel.onPresenceCallbackAdded()
        assertFalse(channel.isPresenceEnabledForJoin())

        channel.onMessage(systemAckMessage(topic))

        assertTrue(channel.isPresenceEnabledForJoin())
    }

    @Test
    fun presenceEnabledWhenListenerAddedAfterSubscription() {
        val topic = "realtime:late_presence"
        val channel = createChannel(topic = topic)
        channel.onMessage(systemAckMessage(topic))
        assertFalse(channel.isPresenceEnabledForJoin())

        channel.callbackManager.addPresenceCallback {}
        channel.onPresenceCallbackAdded()

        assertTrue(channel.isPresenceEnabledForJoin())
    }

    @Test
    fun presenceAlreadyEnabledSkipsAdditionalResubscribe() {
        val topic = "realtime:configured_presence"
        val channel = createChannel(presenceEnabled = true, topic = topic)
        channel.onMessage(systemAckMessage(topic))
        assertTrue(channel.isPresenceEnabledForJoin())

        channel.callbackManager.addPresenceCallback {}
        channel.onPresenceCallbackAdded()

        assertTrue(channel.isPresenceEnabledForJoin())
    }
}

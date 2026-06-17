package io.github.jan.supabase.realtime

sealed class RealtimeBroadcast<T>(
    val topic: String,
    val event: String,
    val payload: T
) {

    class Binary(topic: String, event: String, payload: ByteArray): RealtimeBroadcast<ByteArray>(topic, event, payload)


    class Json(topic: String, event: String, payload: String): RealtimeBroadcast<String>(topic, event, payload)

}

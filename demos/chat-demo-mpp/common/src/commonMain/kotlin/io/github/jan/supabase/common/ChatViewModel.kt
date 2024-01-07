package io.github.jan.supabase.common


import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.common.net.Message
import io.github.jan.supabase.common.net.MessageApi
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

expect open class MPViewModel() {

    val coroutineScope: CoroutineScope

}
class ChatViewModel(
    val supabaseClient: SupabaseClient,
    private val realtimeChannel: RealtimeChannel,
    private val messageApi: MessageApi
) : MPViewModel() {

    val sessionStatus = supabaseClient.auth.sessionStatus
    val loginAlert = MutableStateFlow<String?>(null)
    val messages = MutableStateFlow<List<Message>>(emptyList())

    //Auth

    fun signUp(email: String, password: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
            }.onSuccess {
                loginAlert.value = "Successfully registered! Check your E-Mail to verify your account."
            }.onFailure {
                loginAlert.value = "There was an error while registering: ${it.message}"
            }
        }
    }

    fun login(email: String, password: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            }.onFailure {
                it.printStackTrace()
                loginAlert.value = "There was an error while logging in. Check your credentials and try again."
            }
        }
    }

    fun loginWithGoogle() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.signInWith(Google)
            }
        }
    }

    fun logout() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.signOut()
                messages.value = emptyList()
            }
        }
    }

    //Realtime
    fun connectToRealtime() {
        coroutineScope.launch {
            kotlin.runCatching {
                realtimeChannel.postgresChangeFlow<PostgresAction>("public") {
                    table = "messages"
                }.onEach {
                    when(it) {
                        is PostgresAction.Delete -> messages.value = messages.value.filter { message -> message.id != it.oldRecord["id"]!!.jsonPrimitive.int }
                        is PostgresAction.Insert -> messages.value = messages.value + it.decodeRecord<Message>()
                        is PostgresAction.Select -> error("Select should not be possible")
                        is PostgresAction.Update -> error("Update should not be possible")
                    }
                }.launchIn(coroutineScope)

                realtimeChannel.subscribe()

            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun disconnectFromRealtime() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.realtime.disconnect()
            }
        }
    }

    //Interacting with the message api
    fun createMessage(message: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                messageApi.createMessage(message)
            }.onFailure {
                Logger.e(it) { "Error while creating message" }
            }
        }
    }

    fun deleteMessage(id: Int) {
        coroutineScope.launch {
            kotlin.runCatching {
                messageApi.deleteMessage(id)
            }.onFailure {
                Logger.e(it) { "Error while deleting message" }
            }
        }
    }

    fun retrieveMessages() {
        coroutineScope.launch {
            kotlin.runCatching {
                messageApi.retrieveMessages()
            }.onSuccess {
                messages.value = it
            }.onFailure {
                Logger.e(it) { "Error while retrieving messages" }
            }
        }
    }

}
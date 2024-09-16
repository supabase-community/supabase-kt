package io.github.jan.supabase.common


import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.parseFragmentAndImportSession
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

const val URL_PROTOCOL = "io.github.jan.supabase://login"

expect open class MPViewModel() {

    val coroutineScope: CoroutineScope

}

class AppViewModel(
    val supabaseClient: SupabaseClient
) : MPViewModel() {

    val sessionStatus = supabaseClient.gotrue.sessionStatus
    val loginAlert = MutableStateFlow<String?>(null)

    //Auth

    fun signUp(email: String, password: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.signUpWith(Email, redirectUrl = URL_PROTOCOL) {
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
                supabaseClient.gotrue.loginWith(Email) {
                    this.email = email
                    this.password = password
                }
            }.onFailure {
                it.printStackTrace()
                loginAlert.value = "There was an error while logging in. Check your credentials and try again."
            }
        }
    }

    @OptIn(SupabaseInternal::class)
    fun importSession(fragment: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.parseFragmentAndImportSession(fragment)
            }.onFailure {
                it.printStackTrace()
                loginAlert.value = "There was an error while logging in."
            }
        }
    }

    fun loginWithGoogle() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.loginWith(Google, redirectUrl = URL_PROTOCOL)
            }
        }
    }

    fun logout() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.logout()
            }
        }
    }

}
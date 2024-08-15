package io.github.jan.supabase.common


import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.mfa.FactorType
import io.github.jan.supabase.gotrue.mfa.MfaFactor
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

expect open class MPViewModel() {

    val coroutineScope: CoroutineScope

}

class AppViewModel(
    val supabaseClient: SupabaseClient
) : MPViewModel() {

    val sessionStatus = supabaseClient.auth.sessionStatus
    val loginAlert = MutableStateFlow<String?>(null)
    val statusFlow = supabaseClient.auth.mfa.statusFlow
    val enrolledFactor = MutableStateFlow<MfaFactor<FactorType.TOTP.Response>?>(null)

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
        enrolledFactor.value = null
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.signOut()
            }
        }
    }

    //MFa
    fun unenrollFactor() {
        enrolledFactor.value = null
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.mfa.unenroll(supabaseClient.auth.mfa.verifiedFactors.firstOrNull()?.id ?: supabaseClient.auth.mfa.retrieveFactorsForCurrentUser().first { it.isVerified }.id)
                supabaseClient.auth.retrieveUserForCurrentSession(true)
            }
        }
    }

    fun enrollFactor() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.mfa.enroll(FactorType.TOTP)
            }.onSuccess {
                enrolledFactor.value = it
            }.onFailure {
                it.printStackTrace()
                loginAlert.value = "There was an error while enrolling a factor: ${it.message}"
            }
        }
    }

    fun createAndVerifyChallenge(code: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                val factor = enrolledFactor.value?.id ?: supabaseClient.auth.mfa.verifiedFactors.firstOrNull()?.id ?: supabaseClient.auth.mfa.retrieveFactorsForCurrentUser().first { it.isVerified }.id
                supabaseClient.auth.mfa.createChallengeAndVerify(
                    factorId = factor,
                    code = code
                )
            }.onFailure {
                it.printStackTrace()
                if(it is RestException) {
                    loginAlert.value = "Invalid code!"
                }
            }
        }
    }

}
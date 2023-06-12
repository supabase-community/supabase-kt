package io.github.jan.supabase.common

import io.github.aakira.napier.DebugAntilog
import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.gotrue
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

    init {
        Logger.base(DebugAntilog())
    }

    val sessionStatus = supabaseClient.gotrue.sessionStatus
    val loginAlert = MutableStateFlow<String?>(null)
    val isLoggedInUsingMfa = supabaseClient.gotrue.mfa.loggedInUsingMfaFlow
    val mfaEnabled = supabaseClient.gotrue.mfa.isMfaEnabledFlow
    val enrolledFactor = MutableStateFlow<MfaFactor<FactorType.TOTP.Response>?>(null)

    //Auth

    fun signUp(email: String, password: String) {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.signUpWith(Email) {
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

    fun loginWithGoogle() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.loginWith(Google)
            }
        }
    }

    fun logout() {
        enrolledFactor.value = null
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.invalidateSession()
            }
        }
    }

    //MFa
    fun unenrollFactor() {
        enrolledFactor.value = null
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.mfa.unenroll(supabaseClient.gotrue.mfa.verifiedFactors.firstOrNull()?.id ?: supabaseClient.gotrue.mfa.retrieveFactorsForCurrentUser().first { it.isVerified }.id)
                supabaseClient.gotrue.retrieveUserForCurrentSession(true)
            }
        }
    }

    fun enrollFactor() {
        coroutineScope.launch {
            kotlin.runCatching {
                supabaseClient.gotrue.mfa.enroll(FactorType.TOTP)
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
                val factor = enrolledFactor.value?.id ?: supabaseClient.gotrue.mfa.verifiedFactors.firstOrNull()?.id ?: supabaseClient.gotrue.mfa.retrieveFactorsForCurrentUser().first { it.isVerified }.id
                supabaseClient.gotrue.mfa.createChallengeAndVerify(
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
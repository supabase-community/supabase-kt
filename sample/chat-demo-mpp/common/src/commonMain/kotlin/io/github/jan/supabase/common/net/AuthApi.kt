package io.github.jan.supabase.common.net

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

sealed interface AuthApi {

    suspend fun signIn(email: String, password: String)

    suspend fun signUp(email: String, password: String)

    suspend fun signInWithGoogle()

    suspend fun verifyOtp(email: String, otp: String)

    suspend fun signOut()

    suspend fun resetPassword(email: String)

    suspend fun changePassword(newPassword: String)

    fun sessionStatus(): Flow<SessionStatus>

}

internal class AuthApiImpl(
    private val client: SupabaseClient
) : AuthApi {

    private val auth = client.auth

    override fun sessionStatus(): Flow<SessionStatus> {
        return auth.sessionStatus
    }

    override suspend fun verifyOtp(email: String, otp: String) {
        auth.verifyEmailOtp(OtpType.Email.EMAIL, email, otp)
    }

    override suspend fun signInWithGoogle() {
        auth.signInWith(Google)
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun changePassword(newPassword: String) {
        auth.updateUser {
            this.password = newPassword
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String) {
        auth.resetPasswordForEmail(email)
    }


}
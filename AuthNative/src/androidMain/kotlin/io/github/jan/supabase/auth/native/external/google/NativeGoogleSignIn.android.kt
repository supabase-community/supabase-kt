package io.github.jan.supabase.auth.native.external.google

import android.content.Intent
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.AuthFlowManager
import io.github.jan.supabase.auth.native.external.NativeSignInCancelledException
import io.github.jan.supabase.auth.native.external.activities.SupabaseNativeAuthActivity
import io.github.jan.supabase.auth.native.external.applicationContext
import io.github.jan.supabase.auth.native.platformConfig
import io.github.jan.supabase.auth.user.UserSession
import io.ktor.util.generateNonce

actual class GoogleSignInResult(actual val session: UserSession, val credential: GoogleIdTokenCredential)

actual suspend fun Auth.signWithGoogle(config: GoogleSignInConfig.() -> Unit): GoogleSignInResult {
    val context = applicationContext()
    var signInConfig = GoogleSignInConfig("--").apply(config)
    val googleClientId = this.config.platformConfig().nativeAuthConfig.googleClientId ?: error("No google client id set in config")
    val deferred = AuthFlowManager.prepareSignInWait()
    val intent = Intent(context, SupabaseNativeAuthActivity::class.java).apply {
        putExtra(SupabaseNativeAuthActivity.EXTRA_CLIENT_ID, googleClientId)
        putExtra(SupabaseNativeAuthActivity.EXTRA_DIALOG_TYPE, signInConfig.type.name)
        putExtra(SupabaseNativeAuthActivity.EXTRA_NONCE, signInConfig.nonce ?: generateNonce())
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
    when(val result = deferred.await()) {
        is GoogleCredentialResult.Error -> {
            val exception = result.exception ?: error(result.message)
            throw exception
        }
        is GoogleCredentialResult.Success -> {
            signInConfig = GoogleSignInConfig(result.credential.idToken).apply(config)
            val session = signInWithIdToken(signInConfig)
            return GoogleSignInResult(session, result.credential)
        }

        GoogleCredentialResult.ClosedByUser -> throw NativeSignInCancelledException()
    }
}
package io.github.jan.supabase.auth.native.external.google

import android.content.Intent
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.AuthFlowManager
import io.github.jan.supabase.auth.native.applicationContext
import io.github.jan.supabase.auth.native.external.activities.SupabaseNativeAuthActivity
import io.github.jan.supabase.auth.native.platformConfig
import io.github.jan.supabase.logging.i
import io.ktor.util.generateNonce

actual typealias GoogleCredential = GoogleIdTokenCredential

actual suspend fun Auth.signWithGoogle(config: GoogleSignInConfig.() -> Unit): GoogleSignInResult {
    val googleClientId = this.config.platformConfig().nativeAuthConfig.googleClientId
    if(googleClientId == null) {
        logger.i { "No Google client id set in config, proceeding to use OAuth for authentication..." }
        return signInWithGoogleFallback(config)
    }
    val context = applicationContext()
    var signInConfig = GoogleSignInConfig("--").apply(config)
    val deferred = AuthFlowManager.prepareSignInWait()
    if(signInConfig.nonce == null) signInConfig.nonce = generateNonce()
    val intent = Intent(context, SupabaseNativeAuthActivity::class.java).apply {
        putExtra(SupabaseNativeAuthActivity.EXTRA_CLIENT_ID, googleClientId)
        putExtra(SupabaseNativeAuthActivity.EXTRA_DIALOG_TYPE, signInConfig.type.name)
        putExtra(SupabaseNativeAuthActivity.EXTRA_NONCE, signInConfig.nonce)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
    return when(val result = deferred.await()) {
        is GoogleCredentialResult.Error -> {
            val exception = result.exception ?: error(result.message)
            throw exception
        }
        is GoogleCredentialResult.Success -> {
            signInConfig = GoogleSignInConfig(result.credential.idToken).apply {
                config()
                nonce = signInConfig.nonce
            }
            val session = signInWithIdToken(signInConfig)
            GoogleSignInResult.Success(session, result.credential)
        }

        GoogleCredentialResult.ClosedByUser -> GoogleSignInResult.Cancelled
    }
}
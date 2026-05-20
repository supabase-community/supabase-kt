package io.github.jan.supabase.auth.native.external.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import io.github.jan.supabase.auth.native.AuthFlowManager
import io.github.jan.supabase.auth.native.external.ExternalAuthAction

class SupabaseAuthDispatcherActivity: SupabaseAuthActivity() {

    private var stoppedForAuth = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val deeplinkUri = intent?.data
        val oAuthUri = intent?.parcelable<Uri>("auth_url")
        when {
            deeplinkUri != null -> {
                AuthFlowManager.handleRedirect(deeplinkUri)
                returnToApp()
                finish()
            }
            oAuthUri != null -> {
                val type = intent.parcelable<ExternalAuthAction>("action")
                if (type != null) {
                    stoppedForAuth = true
                    openUrl(oAuthUri, type)
                }
            }
            else -> finish()
        }
    }

    internal fun openUrl(uri: Uri, action: ExternalAuthAction) {
        when(action) {
            ExternalAuthAction.ExternalBrowser -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(browserIntent)
            }
            ExternalAuthAction.CustomTab -> {
                val intent = CustomTabsIntent.Builder().build()
                intent.launchUrl(this, uri)
            }
        }
    }

}
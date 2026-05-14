package io.github.jan.supabase.auth.oauth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.browser.customtabs.CustomTabsIntent

class SupabaseAuthDispatcherActivity: Activity() {

    private var stoppedForAuth = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (stoppedForAuth) {
            returnToApp()
        }
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

    fun returnToApp() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    internal fun openUrl(uri: Uri, action: ExternalAuthAction) {
        when(action) {
            ExternalAuthAction.ExternalBrowser -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(browserIntent)
            }
            is ExternalAuthAction.CustomTabs -> {
                val intent = CustomTabsIntent.Builder().build()
                intent.launchUrl(this, uri)
            }
        }
    }

}
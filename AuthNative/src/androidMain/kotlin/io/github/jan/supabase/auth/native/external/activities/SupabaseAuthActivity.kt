package io.github.jan.supabase.auth.native.external.activities

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.activity.ComponentActivity

open class SupabaseAuthActivity: ComponentActivity() {

    private var stoppedForAuth = false

    override fun onResume() {
        super.onResume()
        if (stoppedForAuth) {
            returnToApp()
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

}
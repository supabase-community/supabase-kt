package io.github.jan.supabase.gotrue

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import co.touchlab.kermit.Logger
import io.github.jan.supabase.annotiations.SupabaseInternal
import kotlinx.coroutines.launch

private var appContext: Context? = null

internal class SupabaseInitializer : Initializer<Context> {
    override fun create(context: Context): Context = context.applicationContext.also { appContext = it }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}

internal fun applicationContext(): Context = appContext ?: error("Application context not initialized")

@SupabaseInternal
actual fun GoTrue.setupPlatform() {
    addLifecycleCallbacks(this)
}

private fun addLifecycleCallbacks(gotrue: GoTrue) {
    if(!gotrue.config.enableLifecycleCallbacks) return
    val lifecycle = ProcessLifecycleOwner.get().lifecycle
    gotrue as GoTrueImpl
    val scope = gotrue.authScope
    lifecycle.addObserver(
        object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                if(!gotrue.isAutoRefreshRunning && gotrue.config.alwaysAutoRefresh) {
                    Logger.d {
                        "Starting auto refresh"
                    }
                    scope.launch {
                        try {
                            gotrue.startAutoRefreshForCurrentSession()
                        } catch(e: IllegalStateException) {
                            Logger.d {
                                "No session found for auto refresh"
                            }
                        }
                    }
                }
            }
            override fun onStop(owner: LifecycleOwner) {
                if(gotrue.isAutoRefreshRunning) {
                    Logger.d { "Cancelling auto refresh because app is switching to the background" }
                    scope.launch {
                        gotrue.stopAutoRefreshForCurrentSession()
                    }
                }
            }
        }
    )
}
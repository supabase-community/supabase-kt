package io.github.jan.supabase.gotrue

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.logging.d
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private var appContext: Context? = null

internal class SupabaseInitializer : Initializer<Context> {
    override fun create(context: Context): Context = context.applicationContext.also { appContext = it }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}

internal fun applicationContext(): Context = appContext ?: error("Application context not initialized")

@SupabaseInternal
actual fun Auth.setupPlatform() {
    addLifecycleCallbacks(this)
}

private fun addLifecycleCallbacks(gotrue: Auth) {
    if(!gotrue.config.enableLifecycleCallbacks) return
    val lifecycle = ProcessLifecycleOwner.get().lifecycle
    gotrue as AuthImpl
    val scope = gotrue.authScope
    scope.launch(Dispatchers.Main) {
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {

                override fun onStart(owner: LifecycleOwner) {
                    if(!gotrue.isAutoRefreshRunning && gotrue.config.alwaysAutoRefresh) {
                        gotrue.logger.d {
                            "Starting auto refresh"
                        }
                        scope.launch {
                            try {
                                gotrue.startAutoRefreshForCurrentSession()
                            } catch(e: IllegalStateException) {
                                gotrue.logger.d {
                                    "No session found for auto refresh"
                                }
                            }
                        }
                    }
                }
                override fun onStop(owner: LifecycleOwner) {
                    if(gotrue.isAutoRefreshRunning) {
                        gotrue.logger.d { "Cancelling auto refresh because app is switching to the background" }
                        scope.launch {
                            gotrue.stopAutoRefreshForCurrentSession()
                        }
                    }
                }
            }
        )
    }
}

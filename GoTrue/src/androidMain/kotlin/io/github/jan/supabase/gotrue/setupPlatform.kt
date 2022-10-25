package io.github.jan.supabase.gotrue

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private var appContext: Context? = null

internal class SupabaseInitializer : Initializer<Context> {
    override fun create(context: Context): Context = context.applicationContext.also { appContext = it }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}

internal fun applicationContext(): Context = appContext ?: throw IllegalStateException("Application context not initialized")

actual fun GoTrue.setupPlatform() {
    addLifecycleCallbacks(this)
}

private fun addLifecycleCallbacks(gotrue: GoTrue) {
    val lifecycle = ProcessLifecycleOwner.get().lifecycle
    val scope = CoroutineScope(Dispatchers.IO)
    lifecycle.addObserver(
        object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                scope.launch {
                    gotrue.startAutoRefreshForCurrentSession()
                }
            }
            override fun onStop(owner: LifecycleOwner) {
                Napier.d { "Cancelling session job because app is switching to the background" }
                gotrue.stopAutoRefreshForCurrentSession()
            }
        }
    )
}
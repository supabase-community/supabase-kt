package io.github.jan.supabase.gotrue

import android.content.Context
import androidx.startup.Initializer

private var appContext: Context? = null

internal class SupabaseInitializer : Initializer<Context> {
    override fun create(context: Context): Context = context.applicationContext.also { appContext = it }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}

internal fun applicationContext(): Context = appContext ?: throw IllegalStateException("Application context not initialized")

actual fun GoTrue.setupPlatform() {
}
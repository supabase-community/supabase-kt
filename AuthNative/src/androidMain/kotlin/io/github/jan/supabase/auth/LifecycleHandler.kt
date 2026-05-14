package io.github.jan.supabase.auth

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.logging.d
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun addLifecycleCallbacks(auth: Auth) {
    if(!auth.config.enableLifecycleCallbacks) return auth.initDone()
    val lifecycle = ProcessLifecycleOwner.get().lifecycle
    val scope = auth.authScope
    scope.launch(Dispatchers.Main) {
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {

                override fun onStart(owner: LifecycleOwner) {
                    if(!auth.isAutoRefreshRunning && auth.config.alwaysAutoRefresh) {
                        auth.logger.d {
                            "Trying to re-load session from storage..."
                        }
                        scope.launch {
                            val sessionFound = auth.loadFromStorage()
                            if(!sessionFound) {
                                auth.logger.d {
                                    "No session found, not starting auto refresh"
                                }
                                auth.initDone()
                            } else {
                                auth.logger.d {
                                    "Session found, auto refresh started"
                                }
                            }
                        }
                    }
                }
                override fun onStop(owner: LifecycleOwner) {
                    if(auth.isAutoRefreshRunning) {
                        auth.logger.d { "Cancelling auto refresh because app is switching to the background" }
                        scope.launch {
                            auth.stopAutoRefreshForCurrentSession()
                            auth.setSessionStatus(SessionStatus.Initializing)
                        }
                    }
                }
            }
        )
    }
}

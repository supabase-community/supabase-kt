package io.github.jan.supabase.gotrue

import io.github.jan.supabase.plugins.MainConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

expect class GoTrueConfig() : MainConfig, GoTrueConfigDefaults

open class GoTrueConfigDefaults {

    /**
     * The duration after which [GoTrue] should retry refreshing a session, when it failed due to network issues
     */
    var retryDelay: Duration = 10.seconds

    /**
     * Whether to always automatically refresh the session, when it expires
     */
    var alwaysAutoRefresh: Boolean = true

    /**
     * Whether to automatically load the session from [sessionManager], when [GoTrue] is initialized
     */
    var autoLoadFromStorage: Boolean = true

    /**
     * The session manager used to store/load the session. When null, the default [SettingsSessionManager] will be used
     */
    var sessionManager: SessionManager? = null

    /**
     * The dispatcher used for all gotrue related network requests
     */
    var coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default

    var customUrl: String? = null
    var jwtToken: String? = null
}

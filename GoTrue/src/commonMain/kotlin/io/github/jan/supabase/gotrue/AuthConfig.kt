package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.MainConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The configuration for [Auth]
 */
expect class AuthConfig() : CustomSerializationConfig, AuthConfigDefaults

/**
 * The default values for the [AuthConfig]
 */
open class AuthConfigDefaults : MainConfig() {

    /**
     * The duration after which [Auth] should retry refreshing a session, when it failed due to network issues
     */
    var retryDelay: Duration = 10.seconds

    /**
     * Whether to always automatically refresh the session, when it expires
     */
    var alwaysAutoRefresh: Boolean = true

    /**
     * Whether to automatically load the session from [sessionManager], when [Auth] is initialized
     */
    var autoLoadFromStorage: Boolean = true

    /**
     * Whether to automatically save the session to [sessionManager], when the session changes
     */
    var autoSaveToStorage: Boolean = true

    /**
     * The session manager used to store/load the session. When null, the default [SettingsSessionManager] will be used
     */
    var sessionManager: SessionManager? = null

    /**
     * The cache used to store/load the code verifier for the [FlowType.PKCE] flow. When null, the default [SettingsCodeVerifierCache] will be used
     */
    var codeVerifierCache: CodeVerifierCache? = null

    /**
     * The dispatcher used for all gotrue related network requests
     */
    var coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * The type of login flow to use. Defaults to [FlowType.IMPLICIT]
     */
    var flowType: FlowType = FlowType.IMPLICIT

    /**
     * A serializer used for serializing/deserializing objects e.g. in [Auth.signInWith]. Defaults to [SupabaseClientBuilder.defaultSerializer], when null.
     */
    var serializer: SupabaseSerializer? = null

    /**
     * The deeplink scheme used for the implicit and PKCE flow. When null, deeplinks won't be used as redirect urls
     */
    var scheme: String? = null

    /**
     * The deeplink host used for the implicit and PKCE flow. When null, deeplinks won't be used as redirect urls
     */
    var host: String? = null

}

/**
 * The type of login flow to use
 */
enum class FlowType {
    /**
     * The implicit flow is the default flow, which is easier to use, but less secure.
     *
     * Note: OTP's via a link and sign up verification links are not supported on desktop. Replace your email template to send the token instead.
     */
    IMPLICIT,

    /**
     * The PKCE flow is more secure, as it uses a code verifier to exchange the code for a session making it harder to intercept the session
     *
     * Note: OTP's via a link and sign up verification links are not supported on desktop. Replace your email template to send the token instead.
     */
    PKCE
}
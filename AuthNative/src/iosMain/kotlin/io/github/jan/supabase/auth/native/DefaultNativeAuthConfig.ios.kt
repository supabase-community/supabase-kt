package io.github.jan.supabase.auth.native

actual class PlatformNativeAuthConfig actual constructor() : DefaultNativeAuthConfig() {

    /**
     * App scheme used for OAuth and magic link handling.
     */
    var appScheme: String? = null

    /**
     * Optional app host used for OAuth and magic link handling. Not required.
     */
    var appHost: String? = null

}
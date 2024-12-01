package io.supabase.compose.auth

/**
 * Config for Apple's Authorization API
 *
 * Note: This is a placeholder for future implementation
 */
data object AppleLoginConfig

/**
 * Helper function that return native configs
 */
fun ComposeAuth.Config.appleNativeLogin() {
    appleLoginConfig = AppleLoginConfig
}
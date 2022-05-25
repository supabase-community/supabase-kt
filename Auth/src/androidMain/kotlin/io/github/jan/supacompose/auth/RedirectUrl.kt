package io.github.jan.supacompose.auth

actual fun Auth.generateRedirectUrl(fallbackUrl: String?): String? {
    if(fallbackUrl != null) return fallbackUrl
    this as AuthImpl
    return "${config.scheme}://${config.host}"
}
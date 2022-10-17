package io.github.jan.supabase.auth

actual fun GoTrue.generateRedirectUrl(fallbackUrl: String?): String? = fallbackUrl
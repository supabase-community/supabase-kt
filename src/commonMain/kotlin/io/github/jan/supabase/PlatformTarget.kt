package io.github.jan.supabase

enum class PlatformTarget {
    DESKTOP, ANDROID, WEB;
}

expect val CurrentPlatformTarget: PlatformTarget
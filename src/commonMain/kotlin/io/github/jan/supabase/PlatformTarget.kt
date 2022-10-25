package io.github.jan.supabase

enum class PlatformTarget {
    DESKTOP, ANDROID, WEB, IOS;
}

expect val CurrentPlatformTarget: PlatformTarget
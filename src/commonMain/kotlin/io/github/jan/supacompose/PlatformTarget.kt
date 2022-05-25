package io.github.jan.supacompose

enum class PlatformTarget {
    DESKTOP, ANDROID, WEB;
}

expect val CurrentPlatformTarget: PlatformTarget
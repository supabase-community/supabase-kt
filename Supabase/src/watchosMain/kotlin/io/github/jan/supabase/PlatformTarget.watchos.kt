package io.github.jan.supabase

import io.supabase.supabase.getOSVersion

internal actual fun getOSInformation(): OSInformation? = OSInformation(
    name = "tvOS",
    version = getOSVersion()
)
package io.github.jan.supabase

import io.supabase.supabase.getOSVersion

internal actual val OSInformation: OSInformation = OSInformation(
    name = "iOS",
    version = getOSVersion()
)
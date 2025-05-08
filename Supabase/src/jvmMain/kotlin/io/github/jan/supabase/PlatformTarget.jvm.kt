package io.github.jan.supabase

internal actual val OSInformation: OSInformation = OSInformation(
    name = System.getProperty("os.name"),
    version = System.getProperty("os.version")
)
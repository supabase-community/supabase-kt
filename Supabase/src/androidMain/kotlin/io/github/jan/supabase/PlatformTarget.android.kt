package io.github.jan.supabase

internal actual val OSInformation: OSInformation = OSInformation(
    name = "Android",
    version = android.os.Build.VERSION.SDK_INT.toString()
)
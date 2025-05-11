package io.github.jan.supabase

actual val OSInformation: OSInformation = OSInformation(
    name = "Windows",
    version = getOSVersion()
)

private fun getOSVersion(): String {
    TODO("Not yet implemented")
}
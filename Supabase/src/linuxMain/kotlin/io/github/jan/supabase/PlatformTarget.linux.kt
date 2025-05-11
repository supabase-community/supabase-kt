package io.github.jan.supabase

internal actual fun getOSInformation(): OSInformation = OSInformation(
    name = "Linux",
    version = getOSVersion()
)

private fun getOSVersion(): String {
    //Get OS Version, this is from Native Linux code:
    TODO("Not yet implemented")
}
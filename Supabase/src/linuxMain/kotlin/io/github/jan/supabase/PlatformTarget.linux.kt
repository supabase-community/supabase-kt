package io.github.jan.supabase

actual val OSInformation: OSInformation = OSInformation(
    name = "Linux",
    version = getOSVersion()
)

private fun getOSVersion(): String {
    //Get OS Version, this is from Native Linux code:
    KERN
}
package io.github.jan.supabase.gotrue.providers

expect class ExternalAuthConfig: ExternalAuthConfigDefaults

open class ExternalAuthConfigDefaults {

    val scopes = mutableListOf<String>()
    val queryParams = mutableMapOf<String, String>()

}
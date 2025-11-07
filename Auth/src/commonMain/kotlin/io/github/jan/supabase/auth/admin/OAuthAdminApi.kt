package io.github.jan.supabase.auth.admin

sealed interface OAuthAdminApi {

    suspend fun listClients()

    suspend fun createClient()

    suspend fun getClient()

    suspend fun updateClient()

    suspend fun deleteClient()

    suspend fun regenerateClientSecret()

}
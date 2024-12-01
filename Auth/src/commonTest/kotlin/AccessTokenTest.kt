import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.supabase.auth.Auth
import io.supabase.auth.auth
import io.supabase.auth.minimalSettings
import io.supabase.auth.resolveAccessToken
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccessTokenTest {

    @Test
    fun testAccessTokenWithJwtToken() {
        runTest {
            val client = createMockedSupabaseClient(
                configuration = {
                    install(Auth) {
                        minimalSettings()
                    }
                }
            )
            client.auth.importAuthToken("myAuth") //this should be ignored as per plugin tokens override the used access token
            assertEquals("myJwtToken", client.resolveAccessToken("myJwtToken"))
        }
    }

    @Test
    fun testAccessTokenWithKeyAsFallback() {
        runTest {
            val client = createMockedSupabaseClient(supabaseKey = "myKey")
            assertEquals("myKey", client.resolveAccessToken())
        }
    }

    @Test
    fun testAccessTokenWithoutKey() {
        runTest {
            val client = createMockedSupabaseClient()
            assertNull(client.resolveAccessToken(keyAsFallback = false))
        }
    }

    @Test
    fun testAccessTokenWithCustomAccessToken() {
        runTest {
            val client = createMockedSupabaseClient(
                configuration = {
                    accessToken = {
                        "myCustomToken"
                    }
                }
            )
            assertEquals("myCustomToken", client.resolveAccessToken())
        }
    }

    @Test
    fun testAccessTokenWithAuth() {
        runTest {
            val client = createMockedSupabaseClient(
                configuration = {
                    install(Auth) {
                        minimalSettings()
                    }
                }
            )
            client.auth.importAuthToken("myAuth")
            assertEquals("myAuth", client.resolveAccessToken())
        }
    }

}
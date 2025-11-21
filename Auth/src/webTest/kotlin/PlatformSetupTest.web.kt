import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.BrowserBridge
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.setupPlatform
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.respondJson
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PlatformSetupTest {

    lateinit var dispatcher: TestDispatcher

    @BeforeTest
    fun setupDispatcher() {
        dispatcher = StandardTestDispatcher()
    }

    @kotlin.test.Test
    fun testPlatformSetupTestNoCodeOrHash() = runTest(dispatcher) {
        val auth = createAuthClient(autoSetup = false, dispatcher)
        assertEquals(SessionStatus.Initializing, auth.sessionStatus.value)
        auth.setupPlatform()
        advanceUntilIdle()
        assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
    }

    @kotlin.test.Test
    fun testPlatformSetupWithInvalidHash() = runTest(dispatcher) {
        val bridge = BrowserBridgeMock(
            hash = "abc$#as"
        )
        val auth = createAuthClient(autoSetup = false, dispatcher, bridge)
        assertEquals(SessionStatus.Initializing, auth.sessionStatus.value);
        auth.setupPlatform()
        advanceUntilIdle() //TODO: Also check that the hash is unchanged
        assertIs<SessionStatus.NotAuthenticated>(auth.sessionStatus.value)
    }

    @kotlin.test.Test
    fun testPlatformSetupWithValidSessionHash() = runTest(dispatcher) {
        val session = userSession(user = UserInfo(
            id = "id",
            aud = "aud")
        )
        val bridge = BrowserBridgeMock(
            hash = "#access_token=${session.accessToken}&refresh_token=${session.refreshToken}&expires_in=${session.expiresIn}&token_type=${session.tokenType}"
        )
        val auth = createAuthClient(autoSetup = false, dispatcher, bridge, requestHandler = {
            respondJson(session.user)
        })
        assertEquals(SessionStatus.Initializing, auth.sessionStatus.value)
        auth.setupPlatform()
        advanceUntilIdle() //TODO: also check that the hash params were removed
        assertIs<SessionStatus.Authenticated>(auth.sessionStatus.value)
        assertEquals(session, (auth.sessionStatus.value as SessionStatus.Authenticated).session)
    }

    internal fun createAuthClient(
        autoSetup: Boolean,
        dispatcher: CoroutineDispatcher,
        bridge: BrowserBridge = BrowserBridgeMock(),
        requestHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respond("") }
    ) = createMockedSupabaseClient(
        configuration = {
            coroutineDispatcher = dispatcher
            install(Auth) {
                autoSetupPlatform = autoSetup
                autoLoadFromStorage = false
                browserBridge = bridge
            }
        },
        requestHandler = requestHandler
    ).auth


}

internal class BrowserBridgeMock(
    override val hash: String = "",
    override val href: String = "",
    private val replaceCurrentUrl: (String) -> Unit = {},
): BrowserBridge {
    override fun replaceCurrentUrl(newUrl: String) {
        replaceCurrentUrl(newUrl)
    }

    override fun onHashChange(callback: () -> Unit) {
        TODO("Not yet implemented")
    }

}
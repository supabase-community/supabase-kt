import com.apollographql.mockserver.MockRequestBase
import com.apollographql.mockserver.MockResponse
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.MockServerHandler
import io.github.jan.supabase.graphql.GraphQL
import io.github.jan.supabase.testing.createMockedSupabaseClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ApolloHttpInterceptorTest {

    @Test
    fun testApolloHttpInterceptor() {
        val mockServerHandler = object : MockServerHandler {
            override fun handle(request: MockRequestBase): MockResponse {
                println(request.headers)
                return MockResponse.Builder().build()
            }
        }
        val mockServer = MockServer.Builder().handler(mockServerHandler).build()
        runTest {
            val url = mockServer.url()
            val supabaseClient = createMockedSupabaseClient(
                supabaseUrl = "https://test.supabase.co",
                supabaseKey = "testkey",
                configuration = {
                    install(GraphQL) {
                        apolloConfiguration {
                            serverUrl(url)
                        }
                    }
                }
            )
           // supabaseClient.graphql.apolloClient.query(MockQuery("Test")).execute()
        }
    }

}

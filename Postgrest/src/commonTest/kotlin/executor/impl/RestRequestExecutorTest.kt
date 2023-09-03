package executor.impl

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.executor.impl.RestRequestExecutor
import kotlin.test.BeforeTest
import kotlin.test.Test

class RestRequestExecutorTest {

    lateinit var testee: RestRequestExecutor


    // Need a mock mechanism to test
    private lateinit var mockedPostgrest: Postgrest

    @BeforeTest
    fun setUp() {
        testee = RestRequestExecutor(mockedPostgrest)
    }

    @Test
    fun testExecuteDeleteRequest_returnSuccess() {

    }

    @Test
    fun testExecuteInsertRequest_returnSuccess() {

    }

    @Test
    fun testExecuteRpcRequest_returnSuccess() {

    }

    @Test
    fun testExecuteSelectRequest_returnSuccess() {

    }

    @Test
    fun testExecuteUpdateRequest_thenReturnSuccess() {

    }
}
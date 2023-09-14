package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.postgrest.PostgrestImpl
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.postgrest.request.DeleteRequest
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import io.mockative.once
import io.mockative.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class RestRequestExecutorTest {

    lateinit var testee: RestRequestExecutor

    @Mock
    private val postgrest = mock(classOf<PostgrestImpl>())

    @BeforeTest
    fun setUp() {
        testee = RestRequestExecutor(postgrest = postgrest)
    }

    @Test
    fun testExecuteDeleteRequest_returnSuccess() = runTest {
        val request = DeleteRequest(
            returning = Returning.REPRESENTATION,
            count = null,
            filter = mapOf(),
            schema = ""
        )

        every { postgrest.apiVersion }
            .returns(4)
        testee.execute("/user", request)

        verify {
            postgrest.api
        }.wasInvoked(exactly = once)
    }

    @Test
    fun testExecuteInsertRequest_returnSuccess() {
        assertTrue(true)

    }

    @Test
    fun testExecuteRpcRequest_returnSuccess() {
        assertTrue(true)

    }

    @Test
    fun testExecuteSelectRequest_returnSuccess() {
        assertTrue(true)

    }

    @Test
    fun testExecuteUpdateRequest_thenReturnSuccess() {
        assertTrue(true)

    }
}
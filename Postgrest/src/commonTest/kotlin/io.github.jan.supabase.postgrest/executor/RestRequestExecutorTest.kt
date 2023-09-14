package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.postgrest.Postgrest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class RestRequestExecutorTest {

    lateinit var testee: RestRequestExecutor

    // Need a mechanism to mock this
    lateinit var postgrest: Postgrest

    @BeforeTest
    fun setUp() {
//        testee = RestRequestExecutor(postgrest = postgrest)
    }

    @Test
    fun testExecuteDeleteRequest_returnSuccess() {
        assertTrue(true)
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
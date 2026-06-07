package io.github.jan.supabase.integration.storage

import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.integration.IntegrationTestBase
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class VectorBucketTest: IntegrationTestBase() {

    @OptIn(SupabaseExperimental::class)
    @Test
    fun testCreateClient() = runTest {
        val client = createAuthenticatedClient()
        client.storage.vectors.createBucket(
            "myBucket"
        )
        client.storage.vectors.deleteBucket("myBucket")
    }

}
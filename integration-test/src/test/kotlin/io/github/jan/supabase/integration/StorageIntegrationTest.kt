package io.github.jan.supabase.integration

import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StorageIntegrationTest : IntegrationTestBase() {

    private val bucketId = "test-bucket"

    @Test
    fun testUploadAndDownload() = runTest {
        val client = createAuthenticatedClient()
        val bucket = client.storage.from(bucketId)
        val path = "test-upload-${System.nanoTime()}.txt"
        val content = "Hello, Supabase Storage!".toByteArray()

        try {
            bucket.upload(path, content)

            val downloaded = bucket.downloadAuthenticated(path)
            assertContentEquals(content, downloaded)
        } finally {
            runCatching { bucket.delete(path) }
        }
    }

    @Test
    fun testDeleteFile() = runTest {
        val client = createAuthenticatedClient()
        val bucket = client.storage.from(bucketId)
        val path = "test-delete-${System.nanoTime()}.txt"
        val content = "To be deleted".toByteArray()

        bucket.upload(path, content)
        bucket.delete(path)

        // Verify file is gone by attempting to download
        assertFailsWith<RestException> {
            bucket.downloadAuthenticated(path)
        }
    }

    @Test
    fun testListFiles() = runTest {
        val client = createAuthenticatedClient()
        val bucket = client.storage.from(bucketId)
        val prefix = "list-test-${System.nanoTime()}"
        val files = listOf("${prefix}/file1.txt", "${prefix}/file2.txt", "${prefix}/file3.txt")

        try {
            files.forEach { path ->
                bucket.upload(path, "content".toByteArray())
            }

            val listed = bucket.list(prefix = "$prefix/")
            assertTrue(listed.size >= 3, "Expected at least 3 files, got ${listed.size}")

            val listedNames = listed.map { it.name }.toSet()
            assertTrue("file1.txt" in listedNames)
            assertTrue("file2.txt" in listedNames)
            assertTrue("file3.txt" in listedNames)
        } finally {
            files.forEach { path ->
                runCatching { bucket.delete(path) }
            }
        }
    }

    @Test
    fun testUnauthenticatedUploadDenied() = runTest {
        val client = createTestClient()
        val bucket = client.storage.from(bucketId)
        val path = "unauth-test-${System.nanoTime()}.txt"

        assertFailsWith<RestException> {
            bucket.upload(path, "should fail".toByteArray())
        }
    }
}

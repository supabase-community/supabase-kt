package io.github.jan.supabase.integration.storage

import io.github.jan.supabase.storage.vectors.DistanceMetric
import io.github.jan.supabase.storage.vectors.index.MetadataConfiguration
import io.github.jan.supabase.storage.vectors.index.VectorDataType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VectorIndexIntegrationTest : VectorIntegrationTestBase() {

    @Test
    fun testCreateAndGetCosineIndex() = runTest {
        val bucket = createBucket()
        val name = createIndex(bucket, dimension = 3, metric = DistanceMetric.COSINE)

        val index = vectors.from(bucket).getIndex(name)

        assertEquals(name, index.indexName)
        assertEquals(bucket, index.vectorBucketName)
        assertEquals(VectorDataType.FLOAT32, index.dataType)
        assertEquals(3, index.dimension)
        assertEquals(DistanceMetric.COSINE, index.distanceMetric)
    }

    @Test
    fun testCreateAndGetEuclideanIndex() = runTest {
        val bucket = createBucket()
        val name = createIndex(bucket, dimension = 1536, metric = DistanceMetric.EUCLIDEAN)

        val index = vectors.from(bucket).getIndex(name)

        assertEquals(1536, index.dimension)
        assertEquals(DistanceMetric.EUCLIDEAN, index.distanceMetric)
    }

    @Test
    fun testCreateIndexWithMetadataConfiguration() = runTest {
        val bucket = createBucket()
        val name = uniqueName("idx-meta")
        vectors.from(bucket).createIndex {
            indexName = name
            dataType = VectorDataType.FLOAT32
            dimension = 4
            distanceMetric = DistanceMetric.COSINE
            metadataConfiguration = MetadataConfiguration(nonFilterableMetadataKeys = listOf("raw_text", "source"))
        }

        val index = vectors.from(bucket).getIndex(name)

        assertEquals(listOf("raw_text", "source"), assertNotNull(index.metadataConfiguration).nonFilterableMetadataKeys)
    }

    @Test
    fun testCreateDuplicateIndexFails() = runTest {
        val bucket = createBucket()
        val name = createIndex(bucket)

        assertStorageError(409, "ConflictException") {
            createIndex(bucket, name)
        }
    }

    @Test
    fun testCreateIndexInMissingBucketFails() = runTest {
        assertStorageError(404, "NotFoundException") {
            createIndex(uniqueName("it-vec-missing"))
        }
    }

    @Test
    fun testGetMissingIndexFails() = runTest {
        val bucket = createBucket()

        assertStorageError(404, "NotFoundException") {
            vectors.from(bucket).getIndex(uniqueName("idx-missing"))
        }
    }

    @Test
    fun testListIndexesWithPrefixAndPagination() = runTest {
        val bucket = createBucket()
        val other = createIndex(bucket, "other-index")
        val names = listOf("emb-a", "emb-b", "emb-c").onEach { createIndex(bucket, it) }
        val api = vectors.from(bucket)

        val all = api.listIndexes()
        assertEquals((names + other).sorted(), all.indexes.sorted())

        val firstPage = api.listIndexes {
            prefix = "emb-"
            maxResults = 2
        }
        assertEquals(2, firstPage.indexes.size)
        val token = assertNotNull(firstPage.nextToken)

        val secondPage = api.listIndexes {
            prefix = "emb-"
            maxResults = 2
            nextToken = token
        }
        assertEquals(1, secondPage.indexes.size)
        assertNull(secondPage.nextToken)
        assertEquals(names, (firstPage.indexes + secondPage.indexes).sorted())
    }

    @Test
    fun testDeleteIndex() = runTest {
        val bucket = createBucket()
        val name = createIndex(bucket)
        val api = vectors.from(bucket)

        api.deleteIndex(name)

        assertTrue(api.listIndexes().indexes.isEmpty())
        assertStorageError(404, "NotFoundException") {
            api.getIndex(name)
        }
    }

}

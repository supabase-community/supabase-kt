import io.github.jan.supabase.storage.BucketFilter
import io.github.jan.supabase.storage.SortOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BucketFilterTest {

    @Test
    fun testBucketFilterWithAllParameters() {
        val filter = BucketFilter().apply {
            limit = 10
            offset = 5
            search = "test"
            sortBy(BucketFilter.SortColumn.NAME, SortOrder.ASC)
        }
        val params = filter.build()
        assertEquals("10", params["limit"])
        assertEquals("5", params["offset"])
        assertEquals("test", params["search"])
        assertEquals("asc", params["sortOrder"])
        assertEquals("name", params["sortColumn"])
    }

    @Test
    fun testBucketFilterEmpty() {
        val filter = BucketFilter()
        val params = filter.build()
        assertNull(params["limit"])
        assertNull(params["offset"])
        assertNull(params["search"])
        assertNull(params["sortOrder"])
        assertNull(params["sortColumn"])
    }

    @Test
    fun testBucketFilterIndividualParameters() {
        // Test limit only
        var filter = BucketFilter().apply { limit = 20 }
        var params = filter.build()
        assertEquals("20", params["limit"])
        assertNull(params["offset"])

        // Test offset only
        filter = BucketFilter().apply { offset = 15 }
        params = filter.build()
        assertEquals("15", params["offset"])
        assertNull(params["limit"])

        // Test search only
        filter = BucketFilter().apply { search = "my-bucket" }
        params = filter.build()
        assertEquals("my-bucket", params["search"])
        assertNull(params["limit"])
    }

    @Test
    fun testBucketFilterSortColumns() {
        // Test all sort columns with both orders
        val columns = listOf(
            BucketFilter.SortColumn.ID to "id",
            BucketFilter.SortColumn.NAME to "name",
            BucketFilter.SortColumn.CREATED_AT to "created_at",
            BucketFilter.SortColumn.UPDATED_AT to "updated_at"
        )

        for ((column, expectedName) in columns) {
            // Test ascending
            var filter = BucketFilter().apply { sortBy(column, SortOrder.ASC) }
            var params = filter.build()
            assertEquals(expectedName, params["sortColumn"])
            assertEquals("asc", params["sortOrder"])

            // Test descending
            filter = BucketFilter().apply { sortBy(column, SortOrder.DESC) }
            params = filter.build()
            assertEquals(expectedName, params["sortColumn"])
            assertEquals("desc", params["sortOrder"])
        }
    }

    @Test
    fun testBucketFilterEdgeCases() {
        // Zero values
        var filter = BucketFilter().apply {
            limit = 0
            offset = 0
        }
        var params = filter.build()
        assertEquals("0", params["limit"])
        assertEquals("0", params["offset"])

        // Empty search string
        filter = BucketFilter().apply { search = "" }
        params = filter.build()
        assertEquals("", params["search"])

        // Special characters in search
        filter = BucketFilter().apply { search = "test-bucket_123" }
        params = filter.build()
        assertEquals("test-bucket_123", params["search"])

        // Large numbers
        filter = BucketFilter().apply {
            limit = 1000
            offset = 5000
        }
        params = filter.build()
        assertEquals("1000", params["limit"])
        assertEquals("5000", params["offset"])
    }

    @Test
    fun testBucketFilterCombinations() {
        // Limit and offset
        var filter = BucketFilter().apply {
            limit = 25
            offset = 50
        }
        var params = filter.build()
        assertEquals("25", params["limit"])
        assertEquals("50", params["offset"])
        assertNull(params["search"])

        // Search and sort
        filter = BucketFilter().apply {
            search = "images"
            sortBy(BucketFilter.SortColumn.UPDATED_AT, SortOrder.ASC)
        }
        params = filter.build()
        assertEquals("images", params["search"])
        assertEquals("updated_at", params["sortColumn"])
        assertEquals("asc", params["sortOrder"])

        // Pagination with sort
        filter = BucketFilter().apply {
            limit = 10
            offset = 30
            sortBy(BucketFilter.SortColumn.NAME, SortOrder.ASC)
        }
        params = filter.build()
        assertEquals("10", params["limit"])
        assertEquals("30", params["offset"])
        assertEquals("name", params["sortColumn"])
        assertEquals("asc", params["sortOrder"])
    }

}
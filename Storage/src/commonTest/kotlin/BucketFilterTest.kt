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
    fun testBucketFilterWithLimitOnly() {
        val filter = BucketFilter().apply {
            limit = 20
        }
        val params = filter.build()
        assertEquals("20", params["limit"])
        assertNull(params["offset"])
        assertNull(params["search"])
        assertNull(params["sortOrder"])
        assertNull(params["sortColumn"])
    }

    @Test
    fun testBucketFilterWithOffsetOnly() {
        val filter = BucketFilter().apply {
            offset = 15
        }
        val params = filter.build()
        assertNull(params["limit"])
        assertEquals("15", params["offset"])
        assertNull(params["search"])
        assertNull(params["sortOrder"])
        assertNull(params["sortColumn"])
    }

    @Test
    fun testBucketFilterWithSearchOnly() {
        val filter = BucketFilter().apply {
            search = "my-bucket"
        }
        val params = filter.build()
        assertNull(params["limit"])
        assertNull(params["offset"])
        assertEquals("my-bucket", params["search"])
        assertNull(params["sortOrder"])
        assertNull(params["sortColumn"])
    }

    @Test
    fun testBucketFilterWithLimitAndOffset() {
        val filter = BucketFilter().apply {
            limit = 25
            offset = 50
        }
        val params = filter.build()
        assertEquals("25", params["limit"])
        assertEquals("50", params["offset"])
        assertNull(params["search"])
        assertNull(params["sortOrder"])
        assertNull(params["sortColumn"])
    }

    @Test
    fun testBucketFilterSortByIdAscending() {
        val filter = BucketFilter().apply {
            sortBy(BucketFilter.SortColumn.ID, SortOrder.ASC)
        }
        val params = filter.build()
        assertEquals("id", params["sortColumn"])
        assertEquals("asc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterSortByIdDescending() {
        val filter = BucketFilter().apply {
            sortBy(BucketFilter.SortColumn.ID, SortOrder.DESC)
        }
        val params = filter.build()
        assertEquals("id", params["sortColumn"])
        assertEquals("desc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterSortByNameAscending() {
        val filter = BucketFilter().apply {
            sortBy(BucketFilter.SortColumn.NAME, SortOrder.ASC)
        }
        val params = filter.build()
        assertEquals("name", params["sortColumn"])
        assertEquals("asc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterSortByNameDescending() {
        val filter = BucketFilter().apply {
            sortBy(BucketFilter.SortColumn.NAME, SortOrder.DESC)
        }
        val params = filter.build()
        assertEquals("name", params["sortColumn"])
        assertEquals("desc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterSortByCreatedAtAscending() {
        val filter = BucketFilter().apply {
            sortBy(BucketFilter.SortColumn.CREATED_AT, SortOrder.ASC)
        }
        val params = filter.build()
        assertEquals("created_at", params["sortColumn"])
        assertEquals("asc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterSortByCreatedAtDescending() {
        val filter = BucketFilter().apply {
            sortBy(BucketFilter.SortColumn.CREATED_AT, SortOrder.DESC)
        }
        val params = filter.build()
        assertEquals("created_at", params["sortColumn"])
        assertEquals("desc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterSortByUpdatedAtAscending() {
        val filter = BucketFilter().apply {
            sortBy(BucketFilter.SortColumn.UPDATED_AT, SortOrder.ASC)
        }
        val params = filter.build()
        assertEquals("updated_at", params["sortColumn"])
        assertEquals("asc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterSortByUpdatedAtDescending() {
        val filter = BucketFilter().apply {
            sortBy(BucketFilter.SortColumn.UPDATED_AT, SortOrder.DESC)
        }
        val params = filter.build()
        assertEquals("updated_at", params["sortColumn"])
        assertEquals("desc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterSearchWithSpecialCharacters() {
        val filter = BucketFilter().apply {
            search = "test-bucket_123"
        }
        val params = filter.build()
        assertEquals("test-bucket_123", params["search"])
    }

    @Test
    fun testBucketFilterWithZeroLimit() {
        val filter = BucketFilter().apply {
            limit = 0
        }
        val params = filter.build()
        assertEquals("0", params["limit"])
    }

    @Test
    fun testBucketFilterWithZeroOffset() {
        val filter = BucketFilter().apply {
            offset = 0
        }
        val params = filter.build()
        assertEquals("0", params["offset"])
    }

    @Test
    fun testBucketFilterComplexScenario() {
        val filter = BucketFilter().apply {
            limit = 50
            offset = 100
            search = "production"
            sortBy(BucketFilter.SortColumn.CREATED_AT, SortOrder.DESC)
        }
        val params = filter.build()
        assertEquals("50", params["limit"])
        assertEquals("100", params["offset"])
        assertEquals("production", params["search"])
        assertEquals("created_at", params["sortColumn"])
        assertEquals("desc", params["sortOrder"])
    }

    @Test
    fun testBucketFilterWithLargeNumbers() {
        val filter = BucketFilter().apply {
            limit = 1000
            offset = 5000
        }
        val params = filter.build()
        assertEquals("1000", params["limit"])
        assertEquals("5000", params["offset"])
    }

    @Test
    fun testBucketFilterSearchWithEmptyString() {
        val filter = BucketFilter().apply {
            search = ""
        }
        val params = filter.build()
        assertEquals("", params["search"])
    }

    @Test
    fun testBucketFilterWithSearchAndSort() {
        val filter = BucketFilter().apply {
            search = "images"
            sortBy(BucketFilter.SortColumn.UPDATED_AT, SortOrder.ASC)
        }
        val params = filter.build()
        assertEquals("images", params["search"])
        assertEquals("updated_at", params["sortColumn"])
        assertEquals("asc", params["sortOrder"])
        assertNull(params["limit"])
        assertNull(params["offset"])
    }

    @Test
    fun testBucketFilterPaginationScenario() {
        val filter = BucketFilter().apply {
            limit = 10
            offset = 30
            sortBy(BucketFilter.SortColumn.NAME, SortOrder.ASC)
        }
        val params = filter.build()
        assertEquals("10", params["limit"])
        assertEquals("30", params["offset"])
        assertEquals("name", params["sortColumn"])
        assertEquals("asc", params["sortOrder"])
    }

}
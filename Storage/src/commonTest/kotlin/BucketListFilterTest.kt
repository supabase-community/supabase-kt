import io.github.jan.supabase.storage.BucketListFilter
import io.github.jan.supabase.storage.SortOrder
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BucketListFilterTest {

    @Test
    fun testBucketListFilterWithAllParameters() {
        val filter = BucketListFilter().apply {
            limit = 10
            offset = 0
            search = "string"
            sortBy("name", SortOrder.ASC)
        }
        val filterJson = filter.build()
        assertEquals(10, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(0, filterJson["offset"]!!.jsonPrimitive.int)
        assertEquals("string", filterJson["search"]!!.jsonPrimitive.content)
        assertEquals("name", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("asc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterEmpty() {
        val filter = BucketListFilter()
        val filterJson = filter.build()
        assertNull(filterJson["limit"])
        assertNull(filterJson["offset"])
        assertNull(filterJson["search"])
        assertNull(filterJson["sortBy"])
    }

    @Test
    fun testBucketListFilterWithLimitOnly() {
        val filter = BucketListFilter().apply {
            limit = 25
        }
        val filterJson = filter.build()
        assertEquals(25, filterJson["limit"]!!.jsonPrimitive.int)
        assertNull(filterJson["offset"])
        assertNull(filterJson["search"])
        assertNull(filterJson["sortBy"])
    }

    @Test
    fun testBucketListFilterWithOffsetOnly() {
        val filter = BucketListFilter().apply {
            offset = 50
        }
        val filterJson = filter.build()
        assertNull(filterJson["limit"])
        assertEquals(50, filterJson["offset"]!!.jsonPrimitive.int)
        assertNull(filterJson["search"])
        assertNull(filterJson["sortBy"])
    }

    @Test
    fun testBucketListFilterWithSearchOnly() {
        val filter = BucketListFilter().apply {
            search = "my-file"
        }
        val filterJson = filter.build()
        assertNull(filterJson["limit"])
        assertNull(filterJson["offset"])
        assertEquals("my-file", filterJson["search"]!!.jsonPrimitive.content)
        assertNull(filterJson["sortBy"])
    }

    @Test
    fun testBucketListFilterWithSortByOnly() {
        val filter = BucketListFilter().apply {
            sortBy("created_at", SortOrder.DESC)
        }
        val filterJson = filter.build()
        assertNull(filterJson["limit"])
        assertNull(filterJson["offset"])
        assertNull(filterJson["search"])
        assertEquals("created_at", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterWithLimitAndOffset() {
        val filter = BucketListFilter().apply {
            limit = 20
            offset = 40
        }
        val filterJson = filter.build()
        assertEquals(20, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(40, filterJson["offset"]!!.jsonPrimitive.int)
        assertNull(filterJson["search"])
        assertNull(filterJson["sortBy"])
    }

    @Test
    fun testBucketListFilterSortByNameAscending() {
        val filter = BucketListFilter().apply {
            sortBy("name", SortOrder.ASC)
        }
        val filterJson = filter.build()
        assertEquals("name", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("asc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSortByNameDescending() {
        val filter = BucketListFilter().apply {
            sortBy("name", SortOrder.DESC)
        }
        val filterJson = filter.build()
        assertEquals("name", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSortByCreatedAtAscending() {
        val filter = BucketListFilter().apply {
            sortBy("created_at", SortOrder.ASC)
        }
        val filterJson = filter.build()
        assertEquals("created_at", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("asc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSortByCreatedAtDescending() {
        val filter = BucketListFilter().apply {
            sortBy("created_at", SortOrder.DESC)
        }
        val filterJson = filter.build()
        assertEquals("created_at", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSortByUpdatedAtAscending() {
        val filter = BucketListFilter().apply {
            sortBy("updated_at", SortOrder.ASC)
        }
        val filterJson = filter.build()
        assertEquals("updated_at", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("asc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSortByUpdatedAtDescending() {
        val filter = BucketListFilter().apply {
            sortBy("updated_at", SortOrder.DESC)
        }
        val filterJson = filter.build()
        assertEquals("updated_at", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSortByIdAscending() {
        val filter = BucketListFilter().apply {
            sortBy("id", SortOrder.ASC)
        }
        val filterJson = filter.build()
        assertEquals("id", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("asc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSortByIdDescending() {
        val filter = BucketListFilter().apply {
            sortBy("id", SortOrder.DESC)
        }
        val filterJson = filter.build()
        assertEquals("id", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSearchWithSpecialCharacters() {
        val filter = BucketListFilter().apply {
            search = "file-name_123.png"
        }
        val filterJson = filter.build()
        assertEquals("file-name_123.png", filterJson["search"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterWithZeroLimit() {
        val filter = BucketListFilter().apply {
            limit = 0
        }
        val filterJson = filter.build()
        assertEquals(0, filterJson["limit"]!!.jsonPrimitive.int)
    }

    @Test
    fun testBucketListFilterWithZeroOffset() {
        val filter = BucketListFilter().apply {
            offset = 0
        }
        val filterJson = filter.build()
        assertEquals(0, filterJson["offset"]!!.jsonPrimitive.int)
    }

    @Test
    fun testBucketListFilterComplexScenario() {
        val filter = BucketListFilter().apply {
            limit = 100
            offset = 200
            search = "production-files"
            sortBy("updated_at", SortOrder.DESC)
        }
        val filterJson = filter.build()
        assertEquals(100, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(200, filterJson["offset"]!!.jsonPrimitive.int)
        assertEquals("production-files", filterJson["search"]!!.jsonPrimitive.content)
        assertEquals("updated_at", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterWithLargeNumbers() {
        val filter = BucketListFilter().apply {
            limit = 1000
            offset = 5000
        }
        val filterJson = filter.build()
        assertEquals(1000, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(5000, filterJson["offset"]!!.jsonPrimitive.int)
    }

    @Test
    fun testBucketListFilterSortByCustomColumn() {
        val filter = BucketListFilter().apply {
            sortBy("custom_field", SortOrder.ASC)
        }
        val filterJson = filter.build()
        assertEquals("custom_field", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("asc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterSearchWithEmptyString() {
        val filter = BucketListFilter().apply {
            search = ""
        }
        val filterJson = filter.build()
        assertEquals("", filterJson["search"]!!.jsonPrimitive.content)
    }

}
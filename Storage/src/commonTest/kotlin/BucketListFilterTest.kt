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
    fun testBucketListFilterIndividualParameters() {
        // Test limit only
        var filter = BucketListFilter().apply { limit = 25 }
        var filterJson = filter.build()
        assertEquals(25, filterJson["limit"]!!.jsonPrimitive.int)
        assertNull(filterJson["offset"])

        // Test offset only
        filter = BucketListFilter().apply { offset = 50 }
        filterJson = filter.build()
        assertEquals(50, filterJson["offset"]!!.jsonPrimitive.int)
        assertNull(filterJson["limit"])

        // Test search only
        filter = BucketListFilter().apply { search = "my-file" }
        filterJson = filter.build()
        assertEquals("my-file", filterJson["search"]!!.jsonPrimitive.content)
        assertNull(filterJson["limit"])

        // Test sort only
        filter = BucketListFilter().apply { sortBy("created_at", SortOrder.DESC) }
        filterJson = filter.build()
        assertEquals("created_at", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
        assertNull(filterJson["limit"])
    }

    @Test
    fun testBucketListFilterSortColumns() {
        // Test common sort columns with both orders
        val columns = listOf("id", "name", "created_at", "updated_at")

        for (column in columns) {
            // Test ascending
            var filter = BucketListFilter().apply { sortBy(column, SortOrder.ASC) }
            var filterJson = filter.build()
            assertEquals(column, filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
            assertEquals("asc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)

            // Test descending
            filter = BucketListFilter().apply { sortBy(column, SortOrder.DESC) }
            filterJson = filter.build()
            assertEquals(column, filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
            assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
        }
    }

    @Test
    fun testBucketListFilterEdgeCases() {
        // Zero values
        var filter = BucketListFilter().apply {
            limit = 0
            offset = 0
        }
        var filterJson = filter.build()
        assertEquals(0, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(0, filterJson["offset"]!!.jsonPrimitive.int)

        // Empty search string
        filter = BucketListFilter().apply { search = "" }
        filterJson = filter.build()
        assertEquals("", filterJson["search"]!!.jsonPrimitive.content)

        // Special characters in search
        filter = BucketListFilter().apply { search = "file-name_123.png" }
        filterJson = filter.build()
        assertEquals("file-name_123.png", filterJson["search"]!!.jsonPrimitive.content)

        // Large numbers
        filter = BucketListFilter().apply {
            limit = 1000
            offset = 5000
        }
        filterJson = filter.build()
        assertEquals(1000, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(5000, filterJson["offset"]!!.jsonPrimitive.int)

        // Custom column name
        filter = BucketListFilter().apply { sortBy("custom_field", SortOrder.ASC) }
        filterJson = filter.build()
        assertEquals("custom_field", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
    }

    @Test
    fun testBucketListFilterCombinations() {
        // Limit and offset
        var filter = BucketListFilter().apply {
            limit = 20
            offset = 40
        }
        var filterJson = filter.build()
        assertEquals(20, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(40, filterJson["offset"]!!.jsonPrimitive.int)
        assertNull(filterJson["search"])

        // All parameters together
        filter = BucketListFilter().apply {
            limit = 100
            offset = 200
            search = "production-files"
            sortBy("updated_at", SortOrder.DESC)
        }
        filterJson = filter.build()
        assertEquals(100, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(200, filterJson["offset"]!!.jsonPrimitive.int)
        assertEquals("production-files", filterJson["search"]!!.jsonPrimitive.content)
        assertEquals("updated_at", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("desc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

}
import io.github.jan.supabase.storage.BucketListFilter
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class BucketListFilterTest {

    @Test
    fun testBucketListFilter() {
        val filter = BucketListFilter().apply {
            limit = 10
            offset = 0
            search = "string"
            sortBy("name", "asc")
        }
        val filterJson = filter.build()
        assertEquals(10, filterJson["limit"]!!.jsonPrimitive.int)
        assertEquals(0, filterJson["offset"]!!.jsonPrimitive.int)
        assertEquals("string", filterJson["search"]!!.jsonPrimitive.content)
        assertEquals("name", filterJson["sortBy"]!!.jsonObject["column"]!!.jsonPrimitive.content)
        assertEquals("asc", filterJson["sortBy"]!!.jsonObject["order"]!!.jsonPrimitive.content)
    }

}
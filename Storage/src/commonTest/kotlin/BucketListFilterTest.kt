import io.github.jan.supabase.storage.BucketListFilter
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertContains

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
        assertContains(filterJson, "limit")
        assertContains(filterJson, "offset")
        assertContains(filterJson, "search")
        assertContains(filterJson, "sortBy")
        assertContains(filterJson["sortBy"]!!.jsonObject, "column")
        assertContains(filterJson["sortBy"]!!.jsonObject, "order")
    }

}
import io.github.jan.supabase.storage.vectors.DistanceMetric
import kotlin.test.Test
import kotlin.test.assertEquals

class DistanceMetricTest {

    @Test
    fun testCosine() {
        assertEquals("cosine", DistanceMetric.COSINE.value)
    }

    @Test
    fun testEuclidean() {
        assertEquals("euclidean", DistanceMetric.EUCLIDEAN.value)
    }

    @Test
    fun testDotProduct() {
        assertEquals("dotproduct", DistanceMetric.DOTPRODUCT.value)
    }

}
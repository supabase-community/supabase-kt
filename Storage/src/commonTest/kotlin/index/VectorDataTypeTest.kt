package index

import io.github.jan.supabase.storage.vectors.index.VectorDataType
import kotlin.test.Test
import kotlin.test.assertEquals

class VectorDataTypeTest {

    @Test
    fun testFloat32() {
        assertEquals("float32", VectorDataType.FLOAT32.value)
    }

}
package io.github.jan.supabase.coil

import com.github.panpf.sketch.util.toUri
import io.github.jan.supabase.storage.StorageItem
import kotlin.test.Test
import kotlin.test.assertEquals

class SketchUriTest {

    @Test
    fun testSketchUri() {
        val storageItem = StorageItem(
            bucketId = "testBucket",
            path = "test/path/to/file.jpg",
            authenticated = true
        )
        val sketchUri = storageItem.asSketchUri()
        assertEquals("supabase:///testBucket/test/path/to/file.jpg?authenticated=true", sketchUri)
        val parsedItem = sketchUri.toUri().toStorageItem()
        assertEquals(storageItem.bucketId, parsedItem.bucketId)
    }


}
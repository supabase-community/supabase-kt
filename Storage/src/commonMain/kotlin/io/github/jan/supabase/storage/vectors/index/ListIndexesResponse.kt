package io.github.jan.supabase.storage.vectors.index

import io.github.jan.supabase.serializer.mapValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonArray

/**
 * Response from listing indexes
 * @property indexes - Array of index names
 * @property nextToken - Token for fetching next page (if more results exist)
 */
data class ListIndexesResponse(
    @SerialName("indexes") private val indexesRaw: JsonArray,
    val nextToken: String? = null
) {

    val indexes by lazy { indexesRaw.mapValue<String>("indexName") }

}

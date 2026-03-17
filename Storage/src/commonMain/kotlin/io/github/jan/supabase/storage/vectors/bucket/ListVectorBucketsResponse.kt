package io.github.jan.supabase.storage.vectors.bucket

import io.github.jan.supabase.serializer.mapValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonArray

/**
 * Response from listing vector buckets
 * @property vectorBuckets Array of bucket names
 * @property nextToken Token for fetching next page (if more results exist)
 */
data class ListVectorBucketsResponse(
    @SerialName("vectorBuckets")
    private val vectorBucketsArray: JsonArray,
    val nextToken: String? = null
) {

    val vectorBuckets = vectorBucketsArray.mapValue("vectorBucketName")

}
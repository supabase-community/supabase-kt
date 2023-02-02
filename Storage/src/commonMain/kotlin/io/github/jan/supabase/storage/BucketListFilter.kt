package io.github.jan.supabase.storage

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class BucketListFilter {

    var limit: Int? = null
    var offset: Int? = null
    var search: String? = null
    private var column: String? = null
    private var order: String? = null

    fun sortBy(column: String, order: String) {
        this.column = column
        this.order = order
    }

    fun build() = buildJsonObject {
        limit?.let {
            put("limit", it)
        }
        offset?.let {
            put("offset", it)
        }
        search?.let {
            put("search", it)
        }
        column?.let {
            putJsonObject("sortBy") {
                put("column", it)
                put("order", it)
            }
        }
    }

}
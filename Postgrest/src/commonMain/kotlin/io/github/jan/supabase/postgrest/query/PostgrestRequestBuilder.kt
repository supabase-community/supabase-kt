@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.PostgrestFilterDSL
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import kotlin.js.JsName

/**
 * A filter builder for a postgrest query
 */
@PostgrestFilterDSL
class PostgrestRequestBuilder(@PublishedApi internal val propertyConversionMethod: PropertyConversionMethod) {

    var count: Count? = null
        private set
    var returning: Returning = Returning.REPRESENTATION
        private set
    @PublishedApi internal val _params: MutableMap<String, List<String>> = mutableMapOf()
    @PublishedApi internal val headers: HeadersBuilder = HeadersBuilder()
    val params: Map<String, List<String>>
        get() = _params.toMap()

    fun count(count: Count) {
        this.count = count
    }

    fun returning(returning: Returning) {
        this.returning = returning
    }

    @JsName("singleValue")
    fun single() {
        headers[HttpHeaders.Accept] = "application/vnd.pgrst.object+json"
    }

    fun geojson() {
        headers[HttpHeaders.Accept] = "application/geo+json"
    }

    fun csv() {
        headers[HttpHeaders.Accept] = "text/csv"
    }

    inline fun filter(block: PostgrestFilterBuilder.() -> Unit) {
        val filter = PostgrestFilterBuilder(propertyConversionMethod, _params)
        filter.block()
    }

}

@SupabaseInternal
inline fun postgrestRequest(propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE, block: PostgrestRequestBuilder.() -> Unit): PostgrestRequestBuilder {
    val filter = PostgrestRequestBuilder(propertyConversionMethod)
    filter.block()
    return filter
}



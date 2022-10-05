package io.github.jan.supacompose.postgrest.query

import io.github.jan.supacompose.postgrest.getColumnName
import kotlin.reflect.KProperty1

/**
 * A filter builder for a postgrest query
 */
class PostgrestFilterBuilder {

    @PublishedApi
    internal val _params = mutableMapOf<String, String>()
    val params: Map<String, String>
        get() = _params.toMap()

    fun filterNot(column: String, operator: FilterOperator, value: Any?) {
        _params[column] = "not.${operator.identifier}.$value"
    }

    fun filterNot(operation: FilterOperation) = filterNot(operation.column, operation.operator, operation.value)

    fun filter(column: String, operator: FilterOperator, value: Any?) {
        _params[column] = "${operator.identifier}.$value"
    }

    fun filter(operation: FilterOperation) = filter(operation.column, operation.operator, operation.value)

    fun eq(column: String, value: Any) = filter(column, FilterOperator.EQ, value)

    fun neq(column: String, value: Any) = filter(column, FilterOperator.NEQ, value)

    fun gt(column: String, value: Any) = filter(column, FilterOperator.GT, value)

    fun gte(column: String, value: Any) = filter(column, FilterOperator.GTE, value)

    fun lte(column: String, value: Any) = filter(column, FilterOperator.LTE, value)

    fun lt(column: String, value: Any) = filter(column, FilterOperator.LT, value)

    fun like(column: String, pattern: String) = filter(column, FilterOperator.LIKE, pattern)

    fun ilike(column: String, pattern: String) = filter(column, FilterOperator.ILIKE, pattern)

    fun exact(column: String, value: Boolean?) = filter(column, FilterOperator.IS, value)

    fun isIn(column: String, values: List<Any>) = filter(column, FilterOperator.IN, "(${values.joinToString(",")})")

    fun rangeLt(column: String, range: String) = filter(column, FilterOperator.SL, range)

    fun rangeGt(column: String, range: String) = filter(column, FilterOperator.SR, range)

    fun rangeGte(column: String, range: String) = filter(column, FilterOperator.NXL, range)

    fun rangeLte(column: String, range: String) = filter(column, FilterOperator.NXR, range)

    fun adjacent(column: String, range: String) = filter(column, FilterOperator.ADJ, range)

    fun or(filters: String) {
        _params["or"] = "($filters)"
    }

    fun textSearch(column: String, query: String, textSearchType: TextSearchType, config: String? = null): PostgrestFilterBuilder {
        val configPart = if (config === null) "" else "(${config})"
        _params[column] = "${textSearchType.identifier}${configPart}.${query}"
        return this
    }

    infix fun <T, V> KProperty1<T, V>.eq(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.EQ, value.toString()))

    infix fun <T, V> KProperty1<T, V>.neq(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.NEQ, value.toString()))

    infix fun <T, V> KProperty1<T, V>.gt(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.GT, value.toString()))

    infix fun <T, V> KProperty1<T, V>.gte(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.GTE, value.toString()))

    infix fun <T, V> KProperty1<T, V>.lt(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.LT, value.toString()))

    infix fun <T, V> KProperty1<T, V>.lte(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.LTE, value.toString()))

    infix fun <T, V> KProperty1<T, V>.like(pattern: String) = filter(FilterOperation(getColumnName(this), FilterOperator.LIKE, pattern))

    infix fun <T, V> KProperty1<T, V>.ilike(pattern: String) = filter(FilterOperation(getColumnName(this), FilterOperator.ILIKE, pattern))

    infix fun <T, V> KProperty1<T, V>.isExact(value: Boolean?) = filter(FilterOperation(getColumnName(this), FilterOperator.IS, value.toString()))

    infix fun <T, V> KProperty1<T, V>.isIn(list: List<V>) = filter(FilterOperation(getColumnName(this), FilterOperator.IN, "(${list.joinToString(",")})"))

    infix fun <T, V> KProperty1<T, V>.rangeLt(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.SL, range))

    infix fun <T, V> KProperty1<T, V>.rangeLte(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.NXR, range))

    infix fun <T, V> KProperty1<T, V>.rangeGt(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.SR, range))

    infix fun <T, V> KProperty1<T, V>.rangeGte(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.NXL, range))

    infix fun <T, V> KProperty1<T, V>.adjacent(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.ADJ, range))

}

inline fun buildPostgrestFilter(block: PostgrestFilterBuilder.() -> Unit): Map<String, String> {
    val filter = PostgrestFilterBuilder()
    filter.block()
    return filter.params
}

data class FilterOperation(val column: String, val operator: FilterOperator, val value: String)

enum class FilterOperator(val identifier: String) {
    EQ("eq"),
    NEQ("neq"),
    GT("gt"),
    GTE("gte"),
    LT("lt"),
    LTE("lte"),
    LIKE("like"),
    ILIKE("ilike"),
    IS("is"),
    IN("in"),
    CS("cs"),
    CD("cd"),
    SL("sl"),
    SR("sr"),
    NXL("nxl"),
    NXR("nxr"),
    ADJ("adj"),
    OV("ov"),
    FTS("fts"),
    PLFTS("plfts"),
    PHFTS("phfts"),
    WFTS("wfts"),
}

enum class TextSearchType(val identifier: String) {
    TSVECTOR("tsvector"),
    PLAINTO("plainto"),
    PHRASETO("phraseto"),
    WEBSEARCH("websearch")
}
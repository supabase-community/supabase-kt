package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.postgrest.getColumnName
import kotlinx.serialization.SerialName
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

    /**
     * Finds all rows where the value of the [column] is equal to [value]
     */
    fun eq(column: String, value: Any) = filter(column, FilterOperator.EQ, value)

    /**
     * Finds all rows where the value of the [column] is not equal to [value]
     */
    fun neq(column: String, value: Any) = filter(column, FilterOperator.NEQ, value)

    /**
     * Finds all rows where the value of the [column] is greater than [value]
     */
    fun gt(column: String, value: Any) = filter(column, FilterOperator.GT, value)

    /**
     * Finds all rows where the value of the [column] is greater than or equal to [value]
     */
    fun gte(column: String, value: Any) = filter(column, FilterOperator.GTE, value)

    /**
     * Finds all rows where the value of the [column] is less than or equal to [value]
     */
    fun lte(column: String, value: Any) = filter(column, FilterOperator.LTE, value)

    /**
     * Finds all rows where the value of the [column] is less than [value]
     */
    fun lt(column: String, value: Any) = filter(column, FilterOperator.LT, value)

    /**
     * Finds all rows where the value of the [column] matches the specified [pattern]
     */
    fun like(column: String, pattern: String) = filter(column, FilterOperator.LIKE, pattern)

    /**
     * Finds all rows where the value of the [column] matches the specified [pattern] (case-insensitive)
     */
    fun ilike(column: String, pattern: String) = filter(column, FilterOperator.ILIKE, pattern)

    /**
     * Finds all rows where the value of the [column] equals to one of these values: null,true,false,unknown
     */
    fun exact(column: String, value: Boolean?) = filter(column, FilterOperator.IS, value)

    /**
     * Finds all rows where the value of the [column] is a member of [values]
     */
    fun isIn(column: String, values: List<Any>) = filter(column, FilterOperator.IN, "(${values.joinToString(",")})")

    /**
     * Finds all rows where the value of the [column] is strictly left of [range]
     */
    fun rangeLt(column: String, range: String) = filter(column, FilterOperator.SL, range)

    /**
     * Finds all rows where the value of the [column] is strictly right of [range]
     */
    fun rangeGt(column: String, range: String) = filter(column, FilterOperator.SR, range)

    /**
     * Finds all rows where the value of the [column] does not extend to the left of [range]
     */
    fun rangeGte(column: String, range: String) = filter(column, FilterOperator.NXL, range)

    /**
     * Finds all rows where the value of the [column] does not extend to the right of [range]
     */
    fun rangeLte(column: String, range: String) = filter(column, FilterOperator.NXR, range)

    /**
     * Finds all rows where the value of the [column] is adjacent to [range]
     */
    fun adjacent(column: String, range: String) = filter(column, FilterOperator.ADJ, range)

    fun or(filters: String) {
        _params["or"] = "($filters)"
    }

    /**
     * Runs a full text search on [column] with the specified [query] and [textSearchType]
     */
    fun textSearch(column: String, query: String, textSearchType: TextSearchType, config: String? = null): PostgrestFilterBuilder {
        val configPart = if (config === null) "" else "(${config})"
        _params[column] = "${textSearchType.identifier}${configPart}.${query}"
        return this
    }

    /**
     * Orders the result by [column] in the specified [order].
     * @param nullsFirst If true, null values will be ordered first
     * @param foreignTable If the column is from a foreign table, specify the table name here
     */
    fun order(column: String, order: Order, nullsFirst: Boolean = false, foreignTable: String? = null) {
        val key = if (foreignTable == null) "order" else "\"$foreignTable\".order"
        _params[key] = "${column}.${order.value}.${if (nullsFirst) "nullsfirst" else "nullslast"}"
    }

    /**
     * Limits the result to [count] rows
     * @param foreignTable If the column is from a foreign table, specify the table name here
     */
    fun limit(count: Long, foreignTable: String? = null) {
        val key = if (foreignTable == null) "limit" else "\"$foreignTable\".limit"
        _params[key] = count.toString()
    }

    /**
     * Limits the result to rows from [from] to [to]
     * @param foreignTable If the column is from a foreign table, specify the table name here
     */
    fun range(from: Long, to: Long, foreignTable: String? = null) {
        val keyOffset = if (foreignTable == null) "offset" else "\"$foreignTable\".offset"
        val keyLimit = if (foreignTable == null) "limit" else "\"$foreignTable\".limit"

        _params[keyOffset] = from.toString()
        _params[keyLimit] = (to - from + 1).toString()
    }

    /**
     * Limits the result to rows from [range.first] to [range.last]
     * @param foreignTable If the column is from a foreign table, specify the table name here
     */
    fun range(range: LongRange, foreignTable: String? = null) = range(range.first, range.last, foreignTable)

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is equal to [value]
     */
    infix fun <T, V> KProperty1<T, V>.eq(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.EQ, value.toString()))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is not equal to [value]
     */
    infix fun <T, V> KProperty1<T, V>.neq(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.NEQ, value.toString()))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is greater than [value]
     */
    infix fun <T, V> KProperty1<T, V>.gt(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.GT, value.toString()))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is greater than or equal to [value]
     */
    infix fun <T, V> KProperty1<T, V>.gte(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.GTE, value.toString()))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is less than [value]
     */
    infix fun <T, V> KProperty1<T, V>.lt(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.LT, value.toString()))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is less than or equal to [value]
     */
    infix fun <T, V> KProperty1<T, V>.lte(value: V) = filter(FilterOperation(getColumnName(this), FilterOperator.LTE, value.toString()))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) matches the specified [pattern]
     */
    infix fun <T, V> KProperty1<T, V>.like(pattern: String) = filter(FilterOperation(getColumnName(this), FilterOperator.LIKE, pattern))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) matches the specified [pattern] (case-insensitive)
     */
    infix fun <T, V> KProperty1<T, V>.ilike(pattern: String) = filter(FilterOperation(getColumnName(this), FilterOperator.ILIKE, pattern))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) equals to one of these values: null,true,false,unknown
     */
    infix fun <T, V> KProperty1<T, V>.isExact(value: Boolean?) = filter(FilterOperation(getColumnName(this), FilterOperator.IS, value.toString()))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is in the specified [list]
     */
    infix fun <T, V> KProperty1<T, V>.isIn(list: List<V>) = filter(FilterOperation(getColumnName(this), FilterOperator.IN, "(${list.joinToString(",")})"))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is strictly left of [range]
     */
    infix fun <T, V> KProperty1<T, V>.rangeLt(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.SL, range))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) does not extend to the left of [range]
     */
    infix fun <T, V> KProperty1<T, V>.rangeLte(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.NXR, range))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) does not extend to the right of [range]
     */
    infix fun <T, V> KProperty1<T, V>.rangeGt(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.SR, range))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) does not strictly right of [range]
     */
    infix fun <T, V> KProperty1<T, V>.rangeGte(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.NXL, range))

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is adjacent to the specified [range]
     */
    infix fun <T, V> KProperty1<T, V>.adjacent(range: String) = filter(FilterOperation(getColumnName(this), FilterOperator.ADJ, range))

}

inline fun buildPostgrestFilter(block: PostgrestFilterBuilder.() -> Unit): Map<String, String> {
    val filter = PostgrestFilterBuilder()
    filter.block()
    return filter.params
}

/**
 * Represents a filter operation for a column using a specific operator and a value.
 */
data class FilterOperation(val column: String, val operator: FilterOperator, val value: String)

/**
 * Represents a single filter operation
 */
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

/**
 * Used to search rows using a full text search. See [Postgrest](https://postgrest.org/en/stable/api.html#full-text-search) for more information
 */
enum class TextSearchType(val identifier: String) {
    TSVECTOR("tsvector"),
    PLAINTO("plainto"),
    PHRASETO("phraseto"),
    WEBSEARCH("websearch")
}
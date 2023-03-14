package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.formatJoiningFilter
import io.github.jan.supabase.postgrest.getColumnName
import kotlinx.serialization.SerialName
import kotlin.reflect.KProperty1

/**
 * A filter builder for a postgrest query
 */
class PostgrestFilterBuilder(@PublishedApi internal val propertyConversionMethod: PropertyConversionMethod) {

    @PublishedApi
    internal val _params = mutableMapOf<String, List<String>>()
    val params: Map<String, List<String>>
        get() = _params.toMap()

    fun filterNot(column: String, operator: FilterOperator, value: Any?) {
        val columnValue = params[column] ?: emptyList()
        _params[column] = columnValue + listOf("not.${operator.identifier}.$value")
    }

    fun filterNot(operation: FilterOperation) = filterNot(operation.column, operation.operator, operation.value)

    fun filter(column: String, operator: FilterOperator, value: Any?) {
        val columnValue = params[column] ?: emptyList()
        _params[column] = columnValue + listOf("${operator.identifier}.$value")
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
    fun sl(column: String, range: LongRange) = filter(column, FilterOperator.SL, "(${range.first},${range.last})")

    /**
     * Finds all rows where the value of the [column] is strictly right of [range]
     */
    fun sr(column: String, range: LongRange) = filter(column, FilterOperator.SR, "(${range.first},${range.last})")

    /**
     * Finds all rows where the value of the [column] does not extend to the left of [range]
     */
    fun nxl(column: String, range: LongRange) = filter(column, FilterOperator.NXL, "(${range.first},${range.last})")

    /**
     * Finds all rows where the value of the [column] does not extend to the right of [range]
     */
    fun nxr(column: String, range: LongRange) = filter(column, FilterOperator.NXR, "(${range.first},${range.last})")

    /**
     * Finds all rows where the value of the [column] is strictly left of [range]
     */
    fun rangeLte(column: String, range: LongRange) = nxr(column, range)

    /**
     * Finds all rows where the value of the [column] is strictly right of [range]
     */
    fun rangeGte(column: String, range: LongRange) = nxl(column, range)

    /**
     * Finds all rows where the value of the [column] does not extend to the right of [range]
     */
    fun rangeLt(column: String, range: LongRange) = sl(column, range)

    /**
     * Finds all rows where the value of the [column] does not extend to the right of [range]
     */
    fun rangeGt(column: String, range: LongRange) = sr(column, range)

    /**
     * Finds all rows where the value of the [column] is adjacent to [range]
     */
    fun adjacent(column: String, range: LongRange) = filter(column, FilterOperator.ADJ, "(${range.first},${range.last})")

    inline fun or(negate: Boolean = false, filter: PostgrestFilterBuilder.() -> Unit) {
        val prefix = if(negate) "not." else ""
        _params[prefix + "or"] = listOf(formatJoiningFilter(filter))
    }

    inline fun and(negate: Boolean = false, filter: PostgrestFilterBuilder.() -> Unit) {
        val prefix = if (negate) "not." else ""
        _params[prefix + "and"] = listOf(formatJoiningFilter(filter))
    }

    /**
     * Runs a full text search on [column] with the specified [query] and [textSearchType]
     */
    fun textSearch(column: String, query: String, textSearchType: TextSearchType, config: String? = null): PostgrestFilterBuilder {
        val configPart = if (config === null) "" else "(${config})"
        _params[column] = listOf("${textSearchType.identifier}${configPart}.${query}")
        return this
    }

    /**
     * Orders the result by [column] in the specified [order].
     * @param nullsFirst If true, null values will be ordered first
     * @param foreignTable If the column is from a foreign table, specify the table name here
     */
    fun order(column: String, order: Order, nullsFirst: Boolean = false, foreignTable: String? = null) {
        val key = if (foreignTable == null) "order" else "\"$foreignTable\".order"
        _params[key] = listOf("${column}.${order.value}.${if (nullsFirst) "nullsfirst" else "nullslast"}")
    }

    /**
     * Limits the result to [count] rows
     * @param foreignTable If the column is from a foreign table, specify the table name here
     */
    fun limit(count: Long, foreignTable: String? = null) {
        val key = if (foreignTable == null) "limit" else "\"$foreignTable\".limit"
        _params[key] = listOf(count.toString())
    }

    /**
     * Limits the result to rows from [from] to [to]
     * @param foreignTable If the column is from a foreign table, specify the table name here
     */
    fun range(from: Long, to: Long, foreignTable: String? = null) {
        val keyOffset = if (foreignTable == null) "offset" else "\"$foreignTable\".offset"
        val keyLimit = if (foreignTable == null) "limit" else "\"$foreignTable\".limit"

        _params[keyOffset] = listOf(from.toString())
        _params[keyLimit] = listOf((to - from + 1).toString())
    }

    /**
     * Limits the result to rows from [range.first] to [range.last]
     * @param foreignTable If the column is from a foreign table, specify the table name here
     */
    fun range(range: LongRange, foreignTable: String? = null) = range(range.first, range.last, foreignTable)

    /**
     * Finds all rows where the value of the [column] contains [values]
     *
     * @param column The column name to filter on
     * @param values The values to filter on
     */
    fun contains(column: String, values: List<Any>) = filter(column, FilterOperator.CS, "{${values.joinToString(",")}}")

    /**
     * Finds all rows where the value of the [column] is contained in [values]
     *
     * @param column The column name to filter on
     * @param values The values to filter on
     */
    fun contained(column: String, values: List<Any>) = filter(column, FilterOperator.CD, "{${values.joinToString(",")}}")

    /**
     * Finds all rows where the value of the [column] contains [values]
     *
     * @param column The column name to filter on
     * @param values The values to filter on
     */
    fun cs(column: String, values: List<Any>) = contains(column, values)

    /**
     * Finds all rows where the value of the [column] is contained in [values]
     *
     * @param column The column name to filter on
     * @param values The values to filter on
     */
    fun cd(column: String, values: List<Any>) = contained(column, values)

    /**
     * Finds all rows where the value of the [column] overlaps with [values]
     *
     * @param column The column name to filter on
     * @param values The values to filter on
     */
    fun ov(column: String, values: List<Any>) = overlaps(column, values)

    /**
     * Finds all rows where the value of the [column] overlaps with [values]
     *
     * @param column The column name to filter on
     * @param values The values to filter on
     */
    fun overlaps(column: String, values: List<Any>) = filter(column, FilterOperator.OV, "{${values.joinToString(",")}}")

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
    infix fun <T, V> KProperty1<T, V>.rangeLt(range: LongRange) = this@PostgrestFilterBuilder.rangeLt(getColumnName(this), range)

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) does not extend to the left of [range]
     */
    infix fun <T, V> KProperty1<T, V>.rangeLte(range: LongRange) = this@PostgrestFilterBuilder.rangeLte(getColumnName(this), range)

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) does not extend to the right of [range]
     */
    infix fun <T, V> KProperty1<T, V>.rangeGt(range: LongRange) = this@PostgrestFilterBuilder.rangeGt(getColumnName(this), range)

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) does not strictly right of [range]
     */
    infix fun <T, V> KProperty1<T, V>.rangeGte(range: LongRange) = this@PostgrestFilterBuilder.rangeGte(getColumnName(this), range)

    /**
     * Finds all rows where the value of the column with the name of the [KProperty1] (or the value of the [SerialName] annotation on JVM) is adjacent to the specified [range]
     */
    infix fun <T, V> KProperty1<T, V>.adjacent(range: LongRange) = this@PostgrestFilterBuilder.adjacent(getColumnName(this), range)

}

inline fun buildPostgrestFilter(propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.SERIAL_NAME, block: PostgrestFilterBuilder.() -> Unit): Map<String, List<String>> {
    val filter = PostgrestFilterBuilder(propertyConversionMethod)
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
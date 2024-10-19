package io.github.jan.supabase.postgrest.query

open class BasicColumnsBuilder {

    internal val columns: MutableList<String> = mutableListOf<String>()

    fun named(vararg columns: String) {
        this.columns.addAll(columns)
    }

    fun all() {
        columns.add("*")
    }

    fun json(column: String, key: String, returnAsText: Boolean = false) {
        val operator = if(returnAsText) "->>" else "->"
        columns.add("$column$operator$key")
    }

    fun foreign(name: String, columnsBuilder: ForeignColumnsBuilder.() -> Unit = {}) {
        val foreignColumns = ForeignColumnsBuilder().apply(columnsBuilder)
        val spread = if(foreignColumns.spread) "..." else ""
        val key = if(foreignColumns.key != null) "!${foreignColumns.key}" else ""
        columns.add("$spread$name$key(${foreignColumns.build()})")
    }

    infix fun String.withAlias(alias: String) = "$alias:$this"

    infix fun String.withFunction(name: String) = "$this.$name"

    infix fun String.withType(type: String) = "$this::$type"

    fun avg() = "avg()"

    fun count() = "count()"

    fun max() = "max()"

    fun min() = "min()"

    fun sum() = "sum()"

    fun build() = columns.joinToString(",").also(::println)

}

class ForeignColumnsBuilder(): BasicColumnsBuilder() {

    var spread = false
    var key: String? = null

}

internal fun String.clean(): String {
    var quoted = false
    val regex = Regex("\\s")
    return this.map {
        if (it == '"') {
            quoted = !quoted
        }
        if (regex.matches(it.toString()) && !quoted) {
            ""
        } else {
            it
        }
    }.joinToString("")
}
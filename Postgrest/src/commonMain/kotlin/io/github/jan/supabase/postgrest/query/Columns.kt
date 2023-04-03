package io.github.jan.supabase.postgrest.query

import kotlin.jvm.JvmInline

@JvmInline
value class Columns @PublishedApi internal constructor(val value: String) {

    companion object {

        val ALL = Columns("*")

        fun raw(value: String) = Columns(value)

        fun list(vararg columns: String) = Columns(columns.joinToString(","))

        inline fun <reified T> type() = Columns(T::class.simpleName!!)

    }

}
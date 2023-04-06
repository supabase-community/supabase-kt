package io.github.jan.supabase.postgrest.query

import kotlin.jvm.JvmInline

@JvmInline
value class Columns @PublishedApi internal constructor(val value: String) {

    companion object {

        /**
         * Select all columns
         */
        val ALL = Columns("*")

        /**
         * Select all columns given in the [value] parameter
         * @param value The columns to select, separated by a comma
         */
        fun raw(value: String) = Columns(value)

        /**
         * Select all columns given in the [columns] parameter
         * @param columns The columns to select
         */
        fun list(vararg columns: String) = Columns(columns.joinToString(","))

        /**
         * Select all columns given in the [columns] parameter
         * @param columns The columns to select
         */
        fun list(columns: List<String>) = Columns(columns.joinToString(","))

        /**
         * Select all columns of type [T]'s class properties. Example: If you specify a class 'User' with the fields 'id' and 'name', this will select 'id,name'
         * @param T The type of the columns to select
         */
        inline fun <reified T> type() = Columns(T::class.simpleName!!)

    }

}
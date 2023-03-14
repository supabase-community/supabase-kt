package io.github.jan.supabase.postgrest

import kotlin.reflect.KProperty1

fun interface PropertyConversionMethod {

    operator fun invoke(property: KProperty1<*, *>): String

    companion object {
        val SERIAL_NAME = PropertyConversionMethod { getSerialName(it) }
        val CAMEL_CASE_TO_SNAKE_CASE = PropertyConversionMethod { it.name.camelToSnakeCase() }
    }

}
package io.github.jan.supabase.postgrest

import io.github.jan.supabase.CurrentPlatformTarget
import io.github.jan.supabase.PlatformTarget
import kotlin.reflect.KProperty1

/**
 * A method to convert a property name to a column name.
 */
fun interface PropertyConversionMethod {

    /**
     * Converts a property name to a column name.
     */
    operator fun invoke(property: KProperty1<*, *>): String

    companion object {

        /**
         * Converts a property name to a column name using the [SerialName] annotation.
         */
        val SERIAL_NAME = PropertyConversionMethod { getSerialName(it) }
            get() {
                if(CurrentPlatformTarget !in listOf(PlatformTarget.DESKTOP, PlatformTarget.ANDROID)) error("SerialName PropertyConversionMethod is only available on the JVM and Desktop. Use CAMEL_CASE_TO_SNAKE_CASE instead.")
                return field
            }

        /**
         * Converts a property name to a column name by converting camel case to snake case.
         */
        val CAMEL_CASE_TO_SNAKE_CASE = PropertyConversionMethod { it.name.camelToSnakeCase() }

        /**
         * Uses the property name as the column name.
         */
        val NONE = PropertyConversionMethod { it.name }
    }

}
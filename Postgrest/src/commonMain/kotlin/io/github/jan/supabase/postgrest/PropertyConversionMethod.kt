package io.github.jan.supabase.postgrest

import io.github.jan.supabase.CurrentPlatformTarget
import io.github.jan.supabase.PlatformTarget
import kotlin.reflect.KProperty1

fun interface PropertyConversionMethod {

    operator fun invoke(property: KProperty1<*, *>): String

    companion object {
        val SERIAL_NAME = PropertyConversionMethod { getSerialName(it) }
            get() {
                if(CurrentPlatformTarget !in listOf(PlatformTarget.DESKTOP, PlatformTarget.ANDROID)) error("SerialName PropertyConversionMethod is only available on the JVM and Desktop. Use CAMEL_CASE_TO_SNAKE_CASE instead.")
                return field
            }
        val CAMEL_CASE_TO_SNAKE_CASE = PropertyConversionMethod { it.name.camelToSnakeCase() }
        val NONE = PropertyConversionMethod { it.name }
    }

}
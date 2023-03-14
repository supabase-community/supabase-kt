package io.github.jan.supabase.postgrest

import kotlinx.serialization.SerialName
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

actual fun <T, V> getSerialName(property: KProperty1<T, V>): String {
    val serialName = property.findAnnotation<SerialName>()
    return serialName?.value ?: property.name
}
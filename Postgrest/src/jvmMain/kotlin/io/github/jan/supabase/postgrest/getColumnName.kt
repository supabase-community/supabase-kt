package io.github.jan.supabase.postgrest

import kotlinx.serialization.SerialName
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

actual fun <T, V> getSerialName(property: KProperty1<T, V>): String {
    val serialName = property.findAnnotation<SerialName>()
    return serialName?.value ?: property.name
}

actual inline fun <reified T> classPropertyNames(): List<String> = T::class.members.filterIsInstance<KProperty1<T, *>>().map { it.name }
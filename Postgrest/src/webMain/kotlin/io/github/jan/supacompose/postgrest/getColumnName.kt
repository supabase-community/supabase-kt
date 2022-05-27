package io.github.jan.supacompose.postgrest

import kotlin.reflect.KProperty1

actual fun <T, V> getColumnName(property: KProperty1<T, V>) = property.name
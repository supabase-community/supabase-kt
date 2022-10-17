package io.github.jan.supabase.postgrest

import kotlin.reflect.KProperty1

actual fun <T, V> getColumnName(property: KProperty1<T, V>) = property.name
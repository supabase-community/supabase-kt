package io.github.jan.supacompose.postgrest

import kotlin.reflect.KProperty1

expect fun <T, V> getColumnName(property: KProperty1<T, V>): String
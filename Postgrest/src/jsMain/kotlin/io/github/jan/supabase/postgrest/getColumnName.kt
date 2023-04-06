package io.github.jan.supabase.postgrest

import kotlin.reflect.KProperty1

actual fun <T, V> getSerialName(property: KProperty1<T, V>) = property.name

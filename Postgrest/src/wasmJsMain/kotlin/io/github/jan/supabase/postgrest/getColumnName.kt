package io.github.jan.supabase.postgrest

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlin.reflect.KProperty1

@SupabaseInternal
actual fun <T, V> getSerialName(property: KProperty1<T, V>) = property.name

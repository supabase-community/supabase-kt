package io.github.jan.supabase.postgrest

import io.github.jan.supabase.annotations.SupabaseInternal

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@SupabaseInternal
annotation class PostgrestDsl

package io.github.jan.supabase.gotrue

/**
 * Used to mark Postgrest filter DSL functions
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
annotation class PostgrestFilterDSL
package io.github.jan.supabase.realtime.annotiations

/**
 * Used to mark Channel DSL functions
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
annotation class ChannelDsl

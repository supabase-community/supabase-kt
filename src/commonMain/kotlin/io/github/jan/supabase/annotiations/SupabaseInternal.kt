package io.github.jan.supabase.annotiations

/**
 * Used to mark internal APIs
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "This API is internal and can change at any time")
annotation class SupabaseInternal

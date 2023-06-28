package io.github.jan.supabase.storage.resumable

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual fun createDefaultResumableCache(): ResumableCache = MemoryResumableCache()
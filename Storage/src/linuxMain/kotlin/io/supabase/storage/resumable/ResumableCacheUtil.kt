package io.supabase.storage.resumable

import io.supabase.annotations.SupabaseInternal
import io.supabase.storage.resumable.MemoryResumableCache
import io.supabase.storage.resumable.ResumableCache

@SupabaseInternal
actual fun createDefaultResumableCache(): ResumableCache = MemoryResumableCache()
package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual fun GoTrue.createDefaultSessionManager(): SessionManager = MemorySessionManager()

@SupabaseInternal
actual fun GoTrue.createDefaultCodeVerifierCache(): CodeVerifierCache = MemoryCodeVerifierCache()
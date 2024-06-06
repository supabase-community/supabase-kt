package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual fun Auth.createDefaultSessionManager(): SessionManager = MemorySessionManager()

@SupabaseInternal
actual fun Auth.createDefaultCodeVerifierCache(): CodeVerifierCache = MemoryCodeVerifierCache()
package io.supabase.auth

import io.supabase.annotations.SupabaseInternal
import io.supabase.auth.Auth
import io.supabase.auth.CodeVerifierCache
import io.supabase.auth.MemoryCodeVerifierCache
import io.supabase.auth.MemorySessionManager
import io.supabase.auth.SessionManager

@SupabaseInternal
actual fun Auth.createDefaultSessionManager(): SessionManager = MemorySessionManager()

@SupabaseInternal
actual fun Auth.createDefaultCodeVerifierCache(): CodeVerifierCache = MemoryCodeVerifierCache()
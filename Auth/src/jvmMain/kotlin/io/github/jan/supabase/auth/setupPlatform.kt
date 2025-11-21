package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal

@SupabaseInternal
actual suspend fun Auth.setupPlatform() = initDone()
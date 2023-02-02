package io.github.jan.supabase.gotrue

import io.github.jan.supabase.plugins.MainConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.time.Duration

actual class GoTrueConfig: MainConfig, GoTrueConfigDefaults()
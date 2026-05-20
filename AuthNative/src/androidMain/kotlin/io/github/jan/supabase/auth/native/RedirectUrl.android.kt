package io.github.jan.supabase.auth.native

import android.content.pm.PackageManager
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.external.applicationContext

@SupabaseInternal
internal actual fun Auth.defaultPlatformRedirectUrl(): String? {
    val context = applicationContext()
    val appInfo = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    )
    val scheme = appInfo.metaData.getString("supabase.auth.scheme") ?: return null
    val host = appInfo.metaData.getString("supabase.auth.host")
    return "$scheme://${host ?: ""}"
}
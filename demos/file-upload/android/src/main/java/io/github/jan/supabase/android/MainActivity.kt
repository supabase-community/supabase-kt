package io.github.jan.supabase.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import io.github.aakira.napier.DebugAntilog
import co.touchlab.kermit.Logger
import io.github.jan.supabase.common.App
import io.github.jan.supabase.common.UploadViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val viewModel: UploadViewModel by inject()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.base(DebugAntilog())
        setContent {
            val permissions = rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            when(permissions.status) {
                is PermissionStatus.Denied -> SideEffect {
                    permissions.launchPermissionRequest()
                }
                PermissionStatus.Granted -> MaterialTheme {
                    App(viewModel)
                }
            }
        }
    }

}
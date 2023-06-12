package io.github.jan.supabase.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import io.github.aakira.napier.DebugAntilog
import co.touchlab.kermit.Logger
import io.github.jan.supabase.common.App
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.gotrue.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.base(DebugAntilog())
        viewModel.supabaseClient.handleDeeplinks(intent)
        setContent {
            MaterialTheme {
                App(viewModel)
            }
        }
    }

}
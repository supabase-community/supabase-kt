package io.github.jan.supabase.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.jan.supabase.common.App
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.gotrue.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Napier.base(DebugAntilog())
        viewModel.supabaseClient.handleDeeplinks(intent)
        setContent {
            MaterialTheme {
                App(viewModel)
            }
        }
    }

}
package io.github.jan.supabase.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import io.supabase.auth.handleDeeplinks
import io.github.jan.supabase.common.App
import io.github.jan.supabase.common.AppViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.supabaseClient.handleDeeplinks(intent)
        setContent {
            MaterialTheme {
                App(viewModel)
            }
        }
    }

}
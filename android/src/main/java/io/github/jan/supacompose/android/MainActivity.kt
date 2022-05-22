package io.github.jan.supacompose.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.createSupabaseClient

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val supabaseClient = remember {
                    createSupabaseClient {
                        supabaseUrl = System.getenv("SUPABASE_URL")
                        supabaseKey = System.getenv("SUPABASE_KEY")

                        install(Auth)
                    }
                }
                println(supabaseClient)
            }
        }
    }
}
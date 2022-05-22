package io.github.jan.supacompose.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.createSupabaseClient

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val supabaseClient = remember {
                    createSupabaseClient {
                        supabaseUrl = "https://arnyfaeuskyqfxkvotgj.supabase.co"
                        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFybnlmYWV1c2t5cWZ4a3ZvdGdqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NTMwMzkxMTEsImV4cCI6MTk2ODYxNTExMX0.ItmL8lfnOL9oy7CEX9N6TnYt10VVhk-KTlwley4aq1M"

                        install(Auth)
                    }
                }
                println(supabaseClient)
            }
        }
    }
}
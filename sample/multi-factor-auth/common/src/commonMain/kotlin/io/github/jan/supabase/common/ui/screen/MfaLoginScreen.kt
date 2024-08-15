package io.github.jan.supabase.common.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.jan.supabase.common.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MfaLoginScreen(viewModel: AppViewModel) {
    var mfaCode by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = mfaCode,
            onValueChange = { mfaCode = it },
            label = { Text("MFA Code") }
        )
        Button({
            viewModel.createAndVerifyChallenge(mfaCode)
        }) {
            Text("Verify")
        }
    }
}
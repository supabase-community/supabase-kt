package io.github.jan.supabase.common.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.common.AppViewModel
import io.github.jan.supabase.common.ui.components.QRCode
import io.github.jan.supabase.gotrue.mfa.FactorType
import io.github.jan.supabase.gotrue.mfa.MfaFactor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MfaSetupScreen(viewModel: AppViewModel) {
    val enrolledFactor by viewModel.enrolledFactor.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(enrolledFactor != null) {
            FactorScreen(enrolledFactor!!, viewModel)
        } else {
            Text("Logged in using AAL1", style = MaterialTheme.typography.bodyMedium)
            Button({
                viewModel.enrollFactor()
            }) {
                Text("Enable Multi-Factor Authentication")
            }
            Button({
                viewModel.logout()
            }) {
                Text("Logout")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FactorScreen(enrolledFactor: MfaFactor<FactorType.TOTP.Response>, viewModel: AppViewModel) {
    val clipboard = LocalClipboardManager.current
    QRCode(enrolledFactor.data.qrCode, Modifier.size(200.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(enrolledFactor.data.secret, style = MaterialTheme.typography.bodyMedium)
        IconButton({
            clipboard.setText(AnnotatedString(enrolledFactor.data.secret))
        }) {
            Icon(Icons.Filled.ContentCopy, null)
        }
    }
    var code by remember { mutableStateOf("") }
    OutlinedTextField(
        value = code,
        onValueChange = { code = it },
        label = { Text("MFA Code") }
    )
    Button({
        viewModel.createAndVerifyChallenge(code)
    }) {
        Text("Verify")
    }
}
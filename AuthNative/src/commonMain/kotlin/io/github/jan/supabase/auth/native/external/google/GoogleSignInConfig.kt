package io.github.jan.supabase.auth.native.external.google

import io.github.jan.supabase.auth.IDTokenProvider
import io.github.jan.supabase.auth.IdTokenConfig

class GoogleSignInConfig(provider: IDTokenProvider, token: String): IdTokenConfig(provider, token) {

    var type: GoogleDialogType = GoogleDialogType.DIALOG

}
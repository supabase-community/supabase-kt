package io.github.jan.supabase.auth.native.external.google

import io.github.jan.supabase.auth.DefaultIdTokenConfig
import io.github.jan.supabase.auth.DefaultOAuthConfig
import io.github.jan.supabase.auth.IdTokenConfig
import io.github.jan.supabase.auth.OAuthConfig
import io.github.jan.supabase.auth.OAuthProviders

class GoogleSignInConfig(token: String):
    IdTokenConfig by DefaultIdTokenConfig(OAuthProviders.GOOGLE, token),
    OAuthConfig by DefaultOAuthConfig()
{

    var type: GoogleDialogType = GoogleDialogType.DIALOG

}
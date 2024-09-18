package io.github.jan.supabase.auth

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal fun JsonObjectBuilder.putCaptchaToken(token: String) {
    putJsonObject("gotrue_meta_security") {
        put("captcha_token", token)
    }
}

internal fun JsonObjectBuilder.putCodeChallenge(codeChallenge: String) {
    put("code_challenge", codeChallenge)
    put("code_challenge_method", PKCEConstants.CHALLENGE_METHOD)
}
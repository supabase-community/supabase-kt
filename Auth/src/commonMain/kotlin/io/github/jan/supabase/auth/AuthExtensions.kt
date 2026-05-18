package io.github.jan.supabase.auth

import io.github.jan.supabase.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Signs in the user without any credentials. This will create a new user session with a new access token.
 *
 * If you want to upgrade this anonymous user to a real user, use [Auth.linkIdentity] to link an OAuth identity or [Auth.updateUser] to add an email or phone.
 *
 * @param data Extra data for the user
 * @param captchaToken The captcha token to use
 */
suspend inline fun <reified T : Any> Auth.signInAnonymously(data: T, captchaToken: String? = null) = signInAnonymously(serializer.encodeToJsonElement(data).jsonObject, captchaToken)
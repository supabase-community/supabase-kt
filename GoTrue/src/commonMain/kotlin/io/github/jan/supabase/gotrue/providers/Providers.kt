@file:Suppress("UndocumentedPublicClass")
package io.github.jan.supabase.gotrue.providers

object Google : IDTokenProvider() {

    override val name = "google"

}

object Discord : OAuthProvider() {

    override val name = "discord"

}

object Github : OAuthProvider() {

    override val name = "github"

}

object Gitlab : OAuthProvider() {

    override val name = "gitlab"

}

object Keycloak : OAuthProvider() {

    override val name = "keycloak"

}

object LinkedIn : OAuthProvider() {

    override val name = "linkedin"

}

object Notion : OAuthProvider() {

    override val name = "notion"

}

object Slack : OAuthProvider() {

    override val name = "slack"

}

object Twitch : OAuthProvider() {

    override val name = "twitch"

}

object Twitter : OAuthProvider() {

    override val name = "twitter"

}

object WorkOS : OAuthProvider() {

    override val name = "workos"

}

object Zoom : OAuthProvider() {

    override val name = "zoom"

}

object Bitbucket : OAuthProvider() {

    override val name = "bitbucket"

}

object Azure : IDTokenProvider() {

    override val name = "azure"

}

object Apple : IDTokenProvider() {

    override val name = "apple"

}

object Spotify : OAuthProvider() {

    override val name = "spotify"

}

object Kakao : OAuthProvider() {

    override val name = "kakao"

}

object Facebook : IDTokenProvider() {

    override val name = "facebook"
  
}
  
object Figma : OAuthProvider() {

    override val name = "figma"

}

/**
 * Creates a new [OAuthProvider] with the given name
 */
operator fun OAuthProvider.Companion.invoke(provider: String) = object : OAuthProvider() {

    override val name = provider

}
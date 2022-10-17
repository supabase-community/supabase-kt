package io.github.jan.supabase.auth.providers

object Google : OAuthProvider() {

    override fun provider() = "google"

}

object Discord : OAuthProvider() {

    override fun provider() = "discord"

}

object Github : OAuthProvider() {

    override fun provider() = "github"

}

object Gitlab : OAuthProvider() {

    override fun provider() = "gitlab"

}

object Keycloak : OAuthProvider() {

    override fun provider() = "keycloak"

}

object LinkedIn : OAuthProvider() {

    override fun provider() = "linkedin"

}

object Notion : OAuthProvider() {

    override fun provider() = "notion"

}

object Slack : OAuthProvider() {

    override fun provider() = "slack"

}

object Twitch : OAuthProvider() {

    override fun provider() = "twitch"

}

object Twitter : OAuthProvider() {

    override fun provider() = "twitter"

}

object WorkOS : OAuthProvider() {

    override fun provider() = "workos"

}
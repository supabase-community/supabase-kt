@file:Suppress("UndocumentedPublicClass")
package io.github.jan.supabase.auth.providers

typealias OAuthProvider = String

typealias IDTokenProvider = OAuthProvider

data object  OAuthProviders {

    const val GOOGLE: IDTokenProvider = "google"
    const val DISCORD: OAuthProvider = "discord"
    const val GITHUB: OAuthProvider = "github"
    const val GITLAB: OAuthProvider = "gitlab"
    const val KEYCLOAK: OAuthProvider = "keycloak"
    const val LINKEDIN: OAuthProvider = "linkedin"
    const val LINKEDIN_OIDC: OAuthProvider = "linkedin_oidc"
    const val NOTION: OAuthProvider = "notion"
    const val SLACK: OAuthProvider = "slack"
    const val SLACK_OIDC: OAuthProvider = "slack_oidc"
    const val TWITCH: OAuthProvider = "twitch"
    const val TWITTER: OAuthProvider = "twitter"
    const val X: OAuthProvider = "x"
    const val WORKOS: OAuthProvider = "workos"
    const val ZOOM: OAuthProvider = "zoom"
    const val BITBUCKET: OAuthProvider = "bitbucket"
    const val AZURE: IDTokenProvider = "azure"
    const val APPLE: IDTokenProvider = "apple"
    const val SPOTIFY: OAuthProvider = "spotify"
    const val KAKAO: IDTokenProvider = "kakao"
    const val FACEBOOK: IDTokenProvider = "facebook"
    const val FIGMA: OAuthProvider = "figma"
    const val FLY: OAuthProvider = "fly"

}

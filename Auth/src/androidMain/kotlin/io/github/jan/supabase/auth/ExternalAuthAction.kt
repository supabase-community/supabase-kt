package io.github.jan.supabase.auth

import android.os.Parcelable
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Represents the available actions for external auth such as OAuth and SSO.
 * @see [AuthConfig.defaultExternalAuthAction]
 */
@Parcelize
sealed interface ExternalAuthAction: Parcelable {

    /**
     * Open the OAuth/SSO flow in an external browser
     */
    data object ExternalBrowser : ExternalAuthAction

    /**
     * Open the OAuth/SSO flow in a custom tab
     * @property intentBuilder The builder for the custom tabs intent
     */
    data class CustomTabs(
        @IgnoredOnParcel val intentBuilder: CustomTabsIntent.Builder.() -> Unit = {}
    ) : ExternalAuthAction

    companion object {

        /**
         * The default action to use for the OAuth flow
         */
        val DEFAULT: ExternalAuthAction = ExternalBrowser

    }

}

package io.github.jan.supabase.auth.native.external

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the available actions for external auth such as OAuth and SSO.
 */
@Parcelize
sealed interface ExternalAuthAction: Parcelable {

    /**
     * Open the OAuth/SSO flow in an external browser
     */
    data object ExternalBrowser : ExternalAuthAction

    /**
     * Open the OAuth/SSO flow in a custom tab
     */
    data object CustomTab : ExternalAuthAction

    companion object {

        /**
         * The default action to use for the OAuth flow
         */
        val DEFAULT: ExternalAuthAction = ExternalBrowser

    }

}

package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import org.w3c.dom.Window
import kotlin.js.ExperimentalWasmJsInterop

@SupabaseInternal
interface BrowserBridge {

    val currentHash: String
    val href: String

    fun replaceCurrentUrl(newUrl: String)

    fun onHashChange(callback: () -> Unit)

}

internal class BrowserBridgeImpl(
    private val window: Window = kotlinx.browser.window
): BrowserBridge {

    override val currentHash: String
        get() = window.location.hash

    override val href: String
        get() = window.location.href

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun replaceCurrentUrl(newUrl: String) {
        kotlinx.browser.window.history.replaceState(null, kotlinx.browser.window.document.title, newUrl)
    }


}
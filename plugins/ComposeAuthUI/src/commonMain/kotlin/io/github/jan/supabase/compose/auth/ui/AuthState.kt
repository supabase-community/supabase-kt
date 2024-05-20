package io.github.jan.supabase.compose.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental

/**
 * Represents the state of auth forms.
 */
@AuthUiExperimental
class AuthState(
    states: Map<String, Boolean> = emptyMap()
) {

    private val _states = mutableStateMapOf(*states.toList().toTypedArray())

    /**
     * The current states of the form.
     */
    val states: Map<String, Boolean> get() = _states.toMap()

    /**
     * Whether the form is valid or not.
     */
    val validForm: Boolean
        get() = states.values.all { it }

    @SupabaseInternal
    operator fun set(key: String, value: Boolean) {
        _states[key] = value
    }

    @SupabaseInternal
    fun remove(key: String) {
        _states.remove(key)
    }

    @SupabaseInternal
    operator fun get(key: String): Boolean? {
        return _states[key]
    }

    @SupabaseInternal
    fun clear() {
        _states.clear()
    }

    companion object {
        /**
         * A [Saver] implementation for [AuthState].
         */
        val SAVER = mapSaver(
            save = { it.states.toMap() },
            restore = { AuthState(it.mapValues { (_, value) -> (value as? Boolean) ?: error("Invalid state value") }) }
        )
    }

}

/**
 * Local composition for [AuthState]. Use [AuthForm] for automatic saving and restoring.
 */
@AuthUiExperimental
val LocalAuthState = compositionLocalOf {
    AuthState() //possibly to throw an error here
}

/**
 * Provides the [AuthState] to the content.
 * @param state The [AuthState] to provide.
 * @param content The content to provide the [AuthState] to.
 */
@AuthUiExperimental
@Composable
fun AuthForm(
    state: AuthState = rememberSaveable(saver = AuthState.SAVER) { AuthState() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAuthState provides state) {
        content()
    }
}
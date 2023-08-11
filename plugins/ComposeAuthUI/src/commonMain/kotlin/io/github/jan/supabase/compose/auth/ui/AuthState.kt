package io.github.jan.supabase.compose.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable

class AuthState(
    states: Map<String, Boolean> = emptyMap()
) {

    private val _states = mutableStateMapOf(*states.toList().toTypedArray())
    val states: Map<String, Boolean> get() = _states.toMap()

    val validForm: Boolean
        get() = states.values.all { it }

    operator fun set(key: String, value: Boolean) {
        _states[key] = value
    }

    fun remove(key: String) {
        _states.remove(key)
    }

    operator fun get(key: String): Boolean? {
        return _states[key]
    }

    fun clear() {
        _states.clear()
    }

    companion object {
        val SAVER = mapSaver(
            save = { it.states.toMap() },
            restore = { AuthState(it.mapValues { (_, value) ->  (value as? Boolean) ?: error("Invalid state value") }) }
        )
    }

}

val LocalAuthState = compositionLocalOf {
    AuthState() //possibly to throw an error here
}

@Composable
fun AuthForm(
    state: AuthState = rememberSaveable(saver = AuthState.SAVER) { AuthState() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAuthState provides state) {
        content()
    }
}
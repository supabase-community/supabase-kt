package io.github.jan.supabase.auth.native.native.url

import io.github.jan.supabase.buildUrl

internal fun consumeUrlParameter(parameters: List<String>, url: String): String {
    return buildUrl(url) {
        parameters.forEach { parameter ->
            this.parameters.remove(parameter)
        }
    }
}
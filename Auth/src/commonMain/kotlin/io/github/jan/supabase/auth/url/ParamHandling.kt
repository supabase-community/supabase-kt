package io.github.jan.supabase.auth.url

import io.github.jan.supabase.buildUrl

internal fun consumeUrlParameter(parameters: List<String>, url: String): String {
    return buildUrl(url) {
        parameters.forEach { parameter ->
            this.parameters.remove(parameter)
        }
    }
}
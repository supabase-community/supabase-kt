package io.github.jan.supabase.gotrue

var GoTrue.Config.scheme: String
    get() = (params["scheme"] as? String) ?: "supabase"
    set(value) {
        params["scheme"] = value
    }

var GoTrue.Config.host: String
    get() = (params["host"] as? String) ?: "login"
    set(value) {
        params["host"] = value
    }
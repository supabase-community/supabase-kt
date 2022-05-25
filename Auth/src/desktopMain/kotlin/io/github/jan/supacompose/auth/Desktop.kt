package io.github.jan.supacompose.auth

import java.io.File

var Auth.Config.sessionFile: File?
    get() = params["sessionFile"] as? File
    set(value) {
        if(value == null) {
            params.remove("sessionFile")
        } else {
            params["sessionFile"] = value
        }
    }
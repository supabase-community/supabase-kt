package io.github.jan.supabase.android

import android.app.Application
import io.github.jan.supabase.common.di.initKoin

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

}
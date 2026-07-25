package com.rodzina.wyjazdy

import android.app.Application
import com.rodzina.wyjazdy.di.AppContainer

class WyjazdyApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

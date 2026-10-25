package com.joy.sandbox.application

import android.app.Application
import com.joy.sandbox.di.AppComponent
import com.joy.sandbox.di.DaggerAppComponent

class SandBoxApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .applicationContext(this)
            .build()
        appComponent.inject(this)
        INSTANCE = this
    }

    companion object {
        internal lateinit var INSTANCE: SandBoxApplication
            private set
    }
}

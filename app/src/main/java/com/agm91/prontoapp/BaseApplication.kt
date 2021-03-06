package com.agm91.prontoapp

import android.app.Application
import com.agm91.prontoapp.model.dagger.DaggerApplicationComponent

class BaseApplication : Application() {
    var appComponent = DaggerApplicationComponent.create()
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: BaseApplication
            private set
    }
}
package com.agm91.prontoapp.model.dagger

import android.app.Application
import com.agm91.prontoapp.BaseApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val baseApplication: BaseApplication) {
    @Provides
    @Singleton
    //@PerApplication
    fun provideApplication(): Application {
        return baseApplication
    }
}
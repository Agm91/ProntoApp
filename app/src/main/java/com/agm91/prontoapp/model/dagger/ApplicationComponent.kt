package com.agm91.prontoapp.model.dagger

import com.agm91.prontoapp.BaseApplication
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: BaseApplication)
}
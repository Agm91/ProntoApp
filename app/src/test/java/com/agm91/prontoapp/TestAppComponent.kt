package com.agm91.prontoapp

import com.agm91.prontoapp.model.dagger.ActivityComponent
import com.agm91.prontoapp.model.dagger.ActivityModule
import com.agm91.prontoapp.model.dagger.NetworkModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ActivityModule::class, NetworkModule::class])
interface TestActivityComponent : ActivityComponent {
    fun into(appTest: UnitTest)
}
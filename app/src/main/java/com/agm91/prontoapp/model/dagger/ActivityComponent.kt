package com.agm91.prontoapp.model.dagger

import com.agm91.prontoapp.presentation.activity.PlacesMapActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ActivityModule::class, NetworkModule::class])
interface ActivityComponent {
    fun inject(activity: PlacesMapActivity)
}
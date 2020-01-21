package com.agm91.prontoapp.model.dagger

import com.agm91.prontoapp.data.PlacesViewModel
import com.agm91.prontoapp.presentation.activity.PlacesMapActivity
import com.agm91.prontoapp.presentation.activity.PlacesPresenter
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private var activity: PlacesMapActivity) {
    @Provides
    fun provideActivity(): PlacesMapActivity {
        return activity
    }

    @Provides
    fun providePresenter(viewModel: PlacesViewModel): PlacesPresenter {
        return PlacesPresenter(viewModel, activity)
    }
}
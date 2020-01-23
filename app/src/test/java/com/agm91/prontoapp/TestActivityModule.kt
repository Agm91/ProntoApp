package com.agm91.prontoapp

import com.agm91.prontoapp.data.PlacesViewModel
import com.agm91.prontoapp.model.dagger.ActivityModule
import com.agm91.prontoapp.presentation.activity.PlacesMapActivity
import com.agm91.prontoapp.presentation.activity.PlacesPresenter

class TestActivityModule(private val activity: PlacesMapActivity) : ActivityModule(activity) {
    override fun provideActivity(): PlacesMapActivity = activity

    override fun providePresenter(viewModel: PlacesViewModel): PlacesPresenter {
        return PlacesPresenter(viewModel, activity)
    }
}
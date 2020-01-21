package com.agm91.prontoapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.agm91.prontoapp.model.Places

class PlacesViewModel : ViewModel() {
    var repository = PlacesRepository()

    fun getPlaces(type: String, location: String, radius: Double): LiveData<ApiResponse<Places>> {
        return repository.getPlaces(type, location, radius)
    }
}
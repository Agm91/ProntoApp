package com.agm91.prontoapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.agm91.prontoapp.BaseApplication
import com.agm91.prontoapp.R
import com.agm91.prontoapp.model.Places
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class PlacesRepository @Inject constructor(private val api: MapsApi) {
    private var data = MutableLiveData<ApiResponse<Places>>()

    fun getPlaces(type: String, location: String, radius: Double): LiveData<ApiResponse<Places>> {
        val apiKey = BaseApplication.instance.applicationContext.getString(
            R.string.google_maps_key
        )
        api.getPlaces(type, location, radius, apiKey).enqueue(object : Callback<Places> {
            override fun onFailure(call: Call<Places>, t: Throwable) {
                data.value = ApiResponse(error = t)
            }

            override fun onResponse(call: Call<Places>, response: Response<Places>) {
                data.value = ApiResponse(data = response.body())
            }
        })
        return data
    }
}
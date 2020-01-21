package com.agm91.prontoapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.agm91.prontoapp.BaseApplication
import com.agm91.prontoapp.R
import com.agm91.prontoapp.model.Places
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlacesRepository {
    private var api: MapsApi
    private var data = MutableLiveData<ApiResponse<Places>>()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(MapsApi::class.java)
    }

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
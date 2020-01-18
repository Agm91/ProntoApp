package com.agm91.prontoapp.data

import com.agm91.prontoapp.model.Photos
import com.agm91.prontoapp.model.Places
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsApi {
    @GET("maps/api/place/textsearch/json")
    fun getPlaces(
        @Query("query") value: String,
        @Query("location") location: String,
        @Query("radius") radius: Double,
        @Query("key") key: String
    ): Call<Places>

    @GET("maps/api/place/photo?photoreference=PHOTO_REFERENCE&sensor=false&maxheight=MAX_HEIGHT&maxwidth=MAX_WIDTH&key=YOUR_API_KEY")
    fun getImage(
        @Query("photoreference") photoreference: String,
        @Query("sensor") sensor: Boolean,
        @Query("maxheight") maxheight: Int,
        @Query("maxwidth") maxwidth: Int,
        @Query("key") key: String
    ): Call<Photos>
}
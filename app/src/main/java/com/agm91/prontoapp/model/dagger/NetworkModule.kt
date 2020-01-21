package com.agm91.prontoapp.model.dagger

import com.agm91.prontoapp.data.MapsApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
open class NetworkModule {
    @Provides
    fun provideMapsApi(retrofit: Retrofit): MapsApi {
        return retrofit.create(MapsApi::class.java)
    }

    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
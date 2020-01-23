package com.agm91.prontoapp

import com.agm91.prontoapp.data.MapsApi
import com.agm91.prontoapp.model.dagger.NetworkModule
import io.mockk.mockk
import retrofit2.Retrofit

class TestNetworkModule : NetworkModule() {
    override fun provideMapsApi(retrofit: Retrofit): MapsApi = mockk()

    override fun provideRetrofit(): Retrofit = mockk()
}
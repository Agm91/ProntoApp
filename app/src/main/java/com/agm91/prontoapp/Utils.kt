package com.agm91.prontoapp

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun Location.asLatLong(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun com.agm91.prontoapp.model.Location.asLatLong(): LatLng {
    return LatLng(this.lat, this.lng)
}
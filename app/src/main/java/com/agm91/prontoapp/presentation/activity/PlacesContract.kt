package com.agm91.prontoapp.presentation.activity

import android.content.Intent
import com.agm91.prontoapp.data.ApiResponse
import com.agm91.prontoapp.model.Places
import com.google.android.gms.maps.model.*

interface PlacesContract {
    interface View {
        fun onSearchButtonClick()
        fun replaceFragment()
        fun requestPermissions()
        fun moveCameraTo(point: LatLng)
        fun askUserToTurnOnLocationIfNeeded()
        fun mapLatLong(): LatLng
        fun loadMarkersOnFragment(markers: List<Marker>)
        fun addMarker(markerOptions: MarkerOptions): Marker
        fun setupMap()
        fun drawCircleToSearch(latLng: LatLng?)
        fun drawCircleSearched(latLng: LatLng?)
        fun eraseCircle(circle: Circle)
        fun showError(t: Throwable?)
        fun showError()
    }

    interface Presenter {
        fun onViewModel()
        fun onDataReceived(apiResponse: ApiResponse<Places>)
        fun createMarkers(apiResponse: ApiResponse<Places>): List<Marker>
        fun checkPermissions()
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun addOnLocationSuccessListener()
        fun onCameraIdle(visibleRegion: VisibleRegion)
    }
}
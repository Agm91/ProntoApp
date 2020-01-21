package com.agm91.prontoapp.presentation.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agm91.prontoapp.asLatLong
import com.agm91.prontoapp.data.ApiResponse
import com.agm91.prontoapp.data.PlacesViewModel
import com.agm91.prontoapp.model.Places
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.SphericalUtil

class PlacesPresenter(val activity: FragmentActivity, private val view: PlacesContract.View) :
    PlacesContract.Presenter {
    private var markers = mutableListOf<Marker>()
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)
    var currentLatLng: LatLng? = null
    var radius: Double = 1000.toDouble()

    override fun onDataReceived(apiResponse: ApiResponse<Places>) {
        Log.d("Places", apiResponse.data.toString())
        if (apiResponse.error == null
            && apiResponse.data?.status == "OK"
            && apiResponse.data?.results != null
        ) {
            val markers = createMarkers(apiResponse)
            view.loadMarkersOnFragment(markers)
        } else if (apiResponse.error != null) {
            view.showError(apiResponse.error)
        }
    }

    override fun createMarkers(apiResponse: ApiResponse<Places>): List<Marker> {
        markers.forEach { it.remove() }
        markers = mutableListOf()
        for (result in apiResponse.data?.results!!) {
            val markerOptions =
                MarkerOptions().position(result.geometry.location.asLatLong())
                    .title(result.name)
            val marker = view.addMarker(markerOptions)
            marker.tag = result
            markers.add(marker)
        }

        view.loadMarkersOnFragment(markers)
        return markers
    }

    override fun checkPermissions() {
        when (PermissionChecker.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )) {
            PermissionChecker.PERMISSION_GRANTED -> {
                view.setupMap()
            }
            else -> view.requestPermissions()
        }
    }

    override fun onCameraIdle(visibleRegion: VisibleRegion) {
        val viewPortHeight =
            SphericalUtil.computeDistanceBetween(visibleRegion.nearLeft, visibleRegion.farLeft)
        val viewPortWidth =
            SphericalUtil.computeDistanceBetween(visibleRegion.nearLeft, visibleRegion.nearRight)

        Log.d("Map", "($viewPortWidth, $viewPortHeight)")
        radius = (if (viewPortWidth < viewPortHeight) viewPortWidth else viewPortHeight) * 0.5
        currentLatLng = view.mapLatLong()
        Log.d("Map", "$currentLatLng")
        view.drawCircleToSearch(currentLatLng)
    }

    override fun addOnLocationSuccessListener() {
        fusedLocationClient.lastLocation.addOnSuccessListener(activity) { location ->
            if (location != null) {
                currentLatLng = LatLng(location.latitude, location.longitude)
                view.moveCameraTo(currentLatLng!!)
            }
        }
    }

    override fun onViewModel() {
        val viewModel = ViewModelProviders.of(activity).get(PlacesViewModel::class.java)
        viewModel.getPlaces(
            "restaurant",
            "" + currentLatLng?.latitude + "," + currentLatLng?.longitude,
            radius
        ).observe(activity, Observer { apiResponse ->
            onDataReceived(apiResponse)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        // All required changes were successfully made
                        Log.i("PlacesMapActivity", "onActivityResult: GPS Enabled by user")
                        addOnLocationSuccessListener()
                    }
                    Activity.RESULT_CANCELED -> {
                        // The user was asked to change settings, but chose not to
                        Log.i(
                            "PlacesMapActivity",
                            "onActivityResult: User rejected GPS request"
                        )
                    }
                }
            }
        }
    }
}
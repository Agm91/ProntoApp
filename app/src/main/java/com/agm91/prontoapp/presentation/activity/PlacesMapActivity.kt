package com.agm91.prontoapp.presentation.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agm91.prontoapp.R
import com.agm91.prontoapp.asLatLong
import com.agm91.prontoapp.data.PlacesViewModel
import com.agm91.prontoapp.databinding.FragmentPlacesMapBinding
import com.agm91.prontoapp.presentation.fragment.PlacesRecyclerFragment
import com.agm91.prontoapp.presentation.view.CustomMarkerInfoView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.cos
import kotlin.math.pow

class PlacesMapActivity : FragmentActivity(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveListener {

    private lateinit var binding: FragmentPlacesMapBinding

    private val REQUEST_ACCESS_FINE_LOCATION = 12
    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLatLng: LatLng

    private var markers = mutableListOf<Marker>()

    val recyclerFragment = PlacesRecyclerFragment.newInstance(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.fragment_places_map
        )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.searchButton.setOnClickListener {
            binding.searchButton.visibility = View.GONE
            val viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)
            viewModel.getPlaces(
                "restaurant",
                "" + currentLatLng.latitude + "," + currentLatLng.longitude,
                1000.toDouble()
            ).observe(this, Observer { apiResponse ->
                Log.d("Places", apiResponse.data.toString())
                if (apiResponse.error == null
                    && apiResponse.data?.status == "OK"
                    && apiResponse.data?.results != null
                ) {
                    recyclerFragment.load(apiResponse.data!!.results)
                    markers.apply {
                        forEach { it.remove() }
                        emptyList<Marker>()
                    }
                    for (result in apiResponse.data?.results!!) {
                        val markerOptions =
                            MarkerOptions().position(result.geometry.location.asLatLong())
                                .title(result.name)
                        val marker = mMap.addMarker(markerOptions)
                        marker.tag = result
                        markers.add(marker)
                    }

                } else {
                    //ERROr
                }
            })
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, recyclerFragment)
        transaction.commit()
    }

    fun checkPermissions() {
        when (PermissionChecker.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )) {
            PermissionChecker.PERMISSION_GRANTED -> {
                setupMap()
            }
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ACCESS_FINE_LOCATION
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@PlacesMapActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ACCESS_FINE_LOCATION
                )
            }
        }
    }

    fun setupMap() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnCameraMoveListener(this)

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        askUserToTurnOnLocationIfNeeded()
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map

        val customInfoWindow = CustomMarkerInfoView(this)
        mMap.setInfoWindowAdapter(customInfoWindow)

        checkPermissions()
        addOnLocationSuccessListener()
    }

    fun addOnLocationSuccessListener() {
        //TODO: val dragged =
        if (false) {

            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

    fun askUserToTurnOnLocationIfNeeded() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val result =
            LocationServices.getSettingsClient(this@PlacesMapActivity)
                .checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(
                                this@PlacesMapActivity,
                                LocationRequest.PRIORITY_HIGH_ACCURACY
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }// Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
            }
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCameraMove() {
        binding.searchButton.visibility = View.VISIBLE
        currentLatLng = mMap.cameraPosition.target
    }

    fun fromCameraToRadius(): Double {
        Log.d("Zoom", mMap.cameraPosition.zoom.toString())
        val metersPerPixel =
            cos(currentLatLng.latitude * Math.PI / 180) * 2 * Math.PI * 6378137 / (256 * 2.toDouble().pow(
                mMap.cameraPosition.zoom.toDouble()
            ))
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        Log.d("Zoom", "radius: " + (metersPerPixel * width).toString())
        return (metersPerPixel * width)
    }
}
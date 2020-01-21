package com.agm91.prontoapp.presentation.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.os.Build
import android.os.Bundle
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
import com.agm91.prontoapp.presentation.adapter.PlacesAdapter
import com.agm91.prontoapp.presentation.fragment.PlacesRecyclerFragment
import com.agm91.prontoapp.presentation.view.CustomMarkerInfoView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil

class PlacesMapActivity : FragmentActivity(),
    OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener,
    PlacesAdapter.OnItemClick, GoogleMap.OnMarkerClickListener {
    private lateinit var binding: FragmentPlacesMapBinding

    private val REQUEST_ACCESS_FINE_LOCATION = 12
    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatLng: LatLng? = null

    private var circle: Circle? = null
    private var radius: Double = 1000.toDouble()

    private var markers = mutableListOf<Marker>()

    private var recyclerFragment: PlacesRecyclerFragment = PlacesRecyclerFragment.newInstance(null)

    init {
        recyclerFragment.setListener(this@PlacesMapActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.fragment_places_map
        )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.searchButton.setOnClickListener {
            binding.searchButton.visibility = View.GONE
            val viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)
            viewModel.getPlaces(
                "restaurant",
                "" + currentLatLng?.latitude + "," + currentLatLng?.longitude,
                radius
            ).observe(this, Observer { apiResponse ->
                Log.d("Places", apiResponse.data.toString())
                if (apiResponse.error == null
                    && apiResponse.data?.status == "OK"
                    && apiResponse.data?.results != null
                ) {
                    markers.forEach { it.remove() }
                    markers = mutableListOf()
                    for (result in apiResponse.data?.results!!) {
                        val markerOptions =
                            MarkerOptions().position(result.geometry.location.asLatLong())
                                .title(result.name)
                        val marker = mMap.addMarker(markerOptions)
                        marker.tag = result
                        markers.add(marker)
                    }

                    recyclerFragment.load(markers)

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

        val customMarker = CustomMarkerInfoView(this)
        mMap.setInfoWindowAdapter(customMarker)

        mMap.setOnCameraIdleListener(this)
        mMap.setOnMarkerClickListener(this)

        checkPermissions()
        addOnLocationSuccessListener()
    }

    fun addOnLocationSuccessListener() {
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
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

    override fun onCameraIdle() {
        val viewPort = mMap.projection.visibleRegion
        val viewPortHeight =
            SphericalUtil.computeDistanceBetween(viewPort.nearLeft, viewPort.farLeft)
        val viewPortWidth =
            SphericalUtil.computeDistanceBetween(viewPort.nearLeft, viewPort.nearRight)

        Log.d("Map", "($viewPortWidth, $viewPortHeight)")
        radius = if (viewPortWidth < viewPortHeight) viewPortWidth else viewPortHeight
        drawCircle(currentLatLng)
    }

    override fun onCameraMove() {
        binding.searchButton.visibility = View.VISIBLE
        currentLatLng = mMap.cameraPosition.target
    }

    override fun onItemClickListener(marker: Marker) {

    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        p0?.let {
            recyclerFragment.show()
            it.showInfoWindow()
            recyclerFragment.moveTo(it)
        }
        return true
    }

    private fun drawCircle(point: LatLng?) {
        circle?.remove()
        if (point != null) {
            // Instantiating CircleOptions to draw a circle around the marker
            val circleOptions = CircleOptions()
            circleOptions.apply {
                center(point)
                radius(radius)
                strokeColor(Color.BLACK)
                fillColor(0x30ff0000)
                strokeWidth(2.toFloat())
            }

            // Adding the circle to the GoogleMap
            circle = mMap.addCircle(circleOptions)
        }
    }
}
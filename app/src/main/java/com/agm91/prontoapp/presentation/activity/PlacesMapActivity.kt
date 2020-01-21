package com.agm91.prontoapp.presentation.activity

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.agm91.prontoapp.R
import com.agm91.prontoapp.databinding.FragmentPlacesMapBinding
import com.agm91.prontoapp.model.dagger.ActivityModule
import com.agm91.prontoapp.model.dagger.DaggerActivityComponent
import com.agm91.prontoapp.presentation.adapter.PlacesAdapter
import com.agm91.prontoapp.presentation.fragment.PlacesRecyclerFragment
import com.agm91.prontoapp.presentation.view.CustomMarkerInfoView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import javax.inject.Inject

class PlacesMapActivity : FragmentActivity(),
    OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener,
    PlacesAdapter.OnItemClick, GoogleMap.OnMarkerClickListener, PlacesContract.View {
    @Inject
    lateinit var presenter: PlacesPresenter

    private lateinit var binding: FragmentPlacesMapBinding

    private val REQUEST_ACCESS_FINE_LOCATION = 12
    private lateinit var mMap: GoogleMap

    private var circleToSearch: Circle? = null
    private var circleSearched: Circle? = null

    private var recyclerFragment: PlacesRecyclerFragment = PlacesRecyclerFragment.newInstance(null)

    init {
        recyclerFragment.setListener(this@PlacesMapActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerActivityComponent.builder().activityModule(ActivityModule(this)).build().inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.fragment_places_map
        )

        ArrayAdapter.createFromResource(
            this,
            R.array.place_type,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.searchButton.setOnClickListener {
            onSearchButtonClick()
        }

        replaceFragment()
    }

    override fun setupMap() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnCameraMoveListener(this)

        askUserToTurnOnLocationIfNeeded()
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map

        val customMarker = CustomMarkerInfoView(this)
        mMap.setInfoWindowAdapter(customMarker)

        mMap.setOnCameraIdleListener(this)
        mMap.setOnMarkerClickListener(this)

        presenter.checkPermissions()
        presenter.addOnLocationSuccessListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun askUserToTurnOnLocationIfNeeded() {
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
        val visibleRegion = mMap.projection.visibleRegion
        presenter.onCameraIdle(visibleRegion)
    }

    override fun eraseCircle(circle: Circle) {
        circle.remove()
    }

    override fun mapLatLong(): LatLng {
        return mMap.cameraPosition.target
    }

    override fun onCameraMove() {
        binding.searchButton.visibility = View.VISIBLE
        binding.spinner.visibility = View.VISIBLE
    }

    override fun onItemClickListener(marker: Marker) {
        marker.showInfoWindow()
        moveCameraTo(marker.position, 17f)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let {
            recyclerFragment.show()
            it.showInfoWindow()
            recyclerFragment.moveTo(it)
        }
        return true
    }

    private fun createCircleOptions(latLng: LatLng?, fillColor: Int): CircleOptions {
        val circleOptions = CircleOptions()
        circleOptions.apply {
            center(latLng)
            radius(presenter.radius)
            strokeColor(Color.BLACK)
            fillColor(fillColor)
            strokeWidth(2.toFloat())
        }
        return circleOptions
    }

    override fun drawCircleToSearch(latLng: LatLng?) {
        circleToSearch?.remove()
        if (latLng != null) {
            val circleOptions = createCircleOptions(latLng, 0x30ff0000)
            circleToSearch = mMap.addCircle(circleOptions)
        }
    }

    override fun drawCircleSearched(latLng: LatLng?) {
        circleSearched?.remove()
        if (latLng != null) {
            val circleOptions = createCircleOptions(latLng, 0x3000FFFF)
            circleSearched = mMap.addCircle(circleOptions)
        }
    }

    override fun onSearchButtonClick() {
        drawCircleSearched(presenter.currentLatLng)
        binding.searchButton.visibility = View.GONE
        binding.spinner.visibility = View.GONE
        presenter.onViewModel(binding.spinner.selectedItem.toString())
    }

    override fun replaceFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, recyclerFragment)
        transaction.commit()
    }

    override fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    override fun moveCameraTo(point: LatLng, zoom: Float) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoom))
    }

    override fun addMarker(markerOptions: MarkerOptions): Marker {
        return mMap.addMarker(markerOptions)
    }

    override fun loadMarkersOnFragment(markers: List<Marker>) {
        recyclerFragment.load(markers)
    }

    override fun showError(t: Throwable?) {
        Toast.makeText(
            this,
            getString(R.string.error_try_later) + "(${t?.message})",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showError() {
        Toast.makeText(this, R.string.error_try_later, Toast.LENGTH_LONG).show()
    }
}
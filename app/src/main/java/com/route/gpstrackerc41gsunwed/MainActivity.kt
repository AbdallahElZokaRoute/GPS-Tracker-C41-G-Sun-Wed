package com.route.gpstrackerc41gsunwed

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    // 1- Runtime Permission
    // 2- Google maps
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is Granted ->  Access The Feature
                getUserLocation()
            } else {
                // Permission is Denied -> Show Explanation to the user & Don't use the feature for now
                showRationaleDialog()
            }

        }// Contact App -> Show Contacts   Floating Action Button
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForPermission()
    }

    private var googleMap: GoogleMap? = null
    private fun checkForPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getUserLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showRationaleDialog()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    var marker: Marker? = null
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                Log.e("LOCATION", "${location.latitude}")
                Log.e("LOCATION", "${location.longitude}")
                if (marker == null) {
                    val markerOptions = MarkerOptions()
                    markerOptions.position(LatLng(location.latitude, location.longitude))
                    markerOptions.title("Current User Location")
                    marker = this@MainActivity.googleMap?.addMarker(markerOptions)
                } else {
                    marker?.position = LatLng(location.latitude, location.longitude)
                }
                this@MainActivity.googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude,
                        ),
                        14.0F
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setDurationMillis(5000)
            .build()
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000)
            .build()

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // Get a handle to the fragment and register the callback.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun showRationaleDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.why_we_need_this_permission))
        dialog.setMessage(getString(R.string.we_need_permission_to_find_and_track_nearest_drivers))
        dialog.setPositiveButton(
            getString(R.string.yes_i_understand)
        ) { dialog, which ->
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            dialog.dismiss()
        }
        dialog.setNegativeButton(getString(R.string.no_i_refuse)) { dialog, which ->
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

    }
}
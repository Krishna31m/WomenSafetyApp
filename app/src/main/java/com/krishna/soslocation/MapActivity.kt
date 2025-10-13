package com.krishna.soslocation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private val markerPlaceMap = mutableMapOf<Marker, LatLng>()

    private val apiKey = "AIzaSyD4-i3oGh4TWl56f3zQrsjSw2zbk5uV81Y"

    companion object {
        private const val TAG = "MapActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_map)
            Log.d(TAG, "MapActivity created successfully")

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as? SupportMapFragment

            if (mapFragment != null) {
                mapFragment.getMapAsync(this)
                Log.d(TAG, "Map fragment initialized")
            } else {
                Log.e(TAG, "Map fragment is null")
                Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show()
            }

            setupFABs()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupFABs() {
        try {
            findViewById<FloatingActionButton>(R.id.fab_hospitals)?.setOnClickListener {
                searchNearbyPlaces("hospital")
            }

            findViewById<FloatingActionButton>(R.id.fab_police)?.setOnClickListener {
                searchNearbyPlaces("police")
            }

            findViewById<FloatingActionButton>(R.id.fab_pharmacy)?.setOnClickListener {
                searchNearbyPlaces("pharmacy")
            }
            Log.d(TAG, "FABs setup successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up FABs: ${e.message}", e)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            map = googleMap
            map.setOnMarkerClickListener(this)
            Log.d(TAG, "Map is ready")

            if (checkLocationPermission()) {
                enableMyLocation()
            } else {
                requestLocationPermission()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onMapReady: ${e.message}", e)
            Toast.makeText(this, "Error initializing map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to show your location",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun enableMyLocation() {
        try {
            if (checkLocationPermission()) {
                map.isMyLocationEnabled = true

                // Get current location with high accuracy
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLocation = location
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")
                    } else {
                        Log.w(TAG, "Location is null, trying last known location")
                        getLastKnownLocation()
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get current location: ${e.message}")
                    getLastKnownLocation()
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}", e)
        }
    }

    private fun getLastKnownLocation() {
        try {
            if (checkLocationPermission()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = it
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        Log.d(TAG, "Last known location: ${it.latitude}, ${it.longitude}")
                    } ?: run {
                        Log.w(TAG, "No last known location available")
                        Toast.makeText(
                            this,
                            "Unable to get location. Please ensure GPS is enabled.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in getLastKnownLocation: ${e.message}", e)
        }
    }

    private fun searchNearbyPlaces(type: String) {
        currentLocation?.let { location ->
            map.clear()
            markerPlaceMap.clear()

            val locationString = "${location.latitude},${location.longitude}"
            val radius = 5000 // 5km radius

            Toast.makeText(this, "Searching for nearby ${type}s...", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                            "location=$locationString" +
                            "&radius=$radius" +
                            "&type=$type" +
                            "&key=$apiKey"

                    Log.d(TAG, "Searching URL: $urlString")

                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.connect()

                    val responseCode = connection.responseCode
                    Log.d(TAG, "Response code: $responseCode")

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val response = StringBuilder()
                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()

                        Log.d(TAG, "Response: ${response.toString().take(200)}...")

                        val jsonResponse = JSONObject(response.toString())
                        val status = jsonResponse.optString("status", "")

                        if (status == "REQUEST_DENIED") {
                            val errorMessage = jsonResponse.optString("error_message", "Unknown error")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MapActivity,
                                    "API Error: $errorMessage. Please check your API key.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@launch
                        }

                        val results = jsonResponse.getJSONArray("results")

                        withContext(Dispatchers.Main) {
                            for (i in 0 until results.length()) {
                                val place = results.getJSONObject(i)
                                val name = place.getString("name")
                                val locationObj = place.getJSONObject("geometry").getJSONObject("location")
                                val lat = locationObj.getDouble("lat")
                                val lng = locationObj.getDouble("lng")
                                val placeLatLng = LatLng(lat, lng)

                                val distance = calculateDistance(
                                    location.latitude,
                                    location.longitude,
                                    lat,
                                    lng
                                )

                                val markerColor = when (type) {
                                    "hospital" -> BitmapDescriptorFactory.HUE_RED
                                    "police" -> BitmapDescriptorFactory.HUE_BLUE
                                    "pharmacy" -> BitmapDescriptorFactory.HUE_GREEN
                                    else -> BitmapDescriptorFactory.HUE_RED
                                }

                                val marker = map.addMarker(
                                    MarkerOptions()
                                        .position(placeLatLng)
                                        .title(name)
                                        .snippet("Distance: %.2f km".format(distance))
                                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                                )

                                marker?.let {
                                    markerPlaceMap[it] = placeLatLng
                                }
                            }

                            if (results.length() == 0) {
                                Toast.makeText(
                                    this@MapActivity,
                                    "No ${type}s found nearby",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@MapActivity,
                                    "Found ${results.length()} ${type}s",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MapActivity,
                                "Failed to fetch places (Error: $responseCode). Please check your API key.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error searching places: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MapActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "Unable to get current location. Please wait...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        currentLocation?.let { location ->
            markerPlaceMap[marker]?.let { destination ->
                val origin = "${location.latitude},${location.longitude}"
                val dest = "${destination.latitude},${destination.longitude}"

                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$dest&travelmode=driving")
                )
                intent.setPackage("com.google.android.apps.maps")

                try {
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        // If Google Maps app is not installed, open in browser
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$dest&travelmode=driving")
                        )
                        startActivity(browserIntent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening maps: ${e.message}", e)
                    Toast.makeText(this, "Unable to open navigation", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }
}


package com.krishna.soslocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import android.view.inputmethod.EditorInfo
import android.widget.EditText



class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val TAG = "MapActivity"
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null
    private val markerPlaceMap = mutableMapOf<Marker, LatLng>()

    // UI Elements
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var tvDuration: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvDestination: TextView
    private lateinit var tvInstruction: TextView
    private lateinit var btnStartNavigation: Button

    // Navigation State
    private var currentDestination: LatLng? = null
    private var isNavigating = false
    private val activePolylines = mutableListOf<Polyline>()
    private var currentRoutePoints: List<LatLng> = emptyList()

    // API Key
    private val GOOGLE_API_KEY = "AIzaSyD4-i3oGh4TWl56f3zQrsjSw2zbk5uV81Y"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initViews()
        setupLocationClient()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initViews() {
        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val searchInput = findViewById<EditText>(R.id.search_input)
        searchInput.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchPlaceByName(query)
                } else {
                    Toast.makeText(this, "Please enter a place name", Toast.LENGTH_SHORT).show()
                }
                true
            } else false
        }


        tvDuration = findViewById(R.id.tv_duration)
        tvDistance = findViewById(R.id.tv_distance)
        tvDestination = findViewById(R.id.tv_destination_name)
        tvInstruction = findViewById(R.id.tv_navigation_instruction)
        btnStartNavigation = findViewById(R.id.btn_start_navigation)

        findViewById<FloatingActionButton>(R.id.fab_hospitals).setOnClickListener {
            searchNearbyPlaces("hospital")
        }
        findViewById<FloatingActionButton>(R.id.fab_police).setOnClickListener {
            searchNearbyPlaces("police")
        }
        findViewById<FloatingActionButton>(R.id.fab_pharmacy).setOnClickListener {
            searchNearbyPlaces("pharmacy")
        }
        findViewById<FloatingActionButton>(R.id.fab_recenter).setOnClickListener {
            recenterCamera()
        }

        btnStartNavigation.setOnClickListener {
            if (isNavigating) stopNavigation() else startNavigation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapStyle()
        checkPermissionsAndEnableLocation()

        map.setOnMapClickListener { latLng ->
            if (!isNavigating) setDestination(latLng, "Selected Location")
        }

        map.setOnPoiClickListener { poi ->
            if (!isNavigating) setDestination(poi.latLng, poi.name)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMapStyle() {
        map.uiSettings.apply {
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = false
        }
        map.setPadding(0, 0, 0, 300)
    }

    private fun setDestination(latLng: LatLng, name: String) {
        map.clear()
        activePolylines.clear()
        currentDestination = latLng
        map.addMarker(MarkerOptions().position(latLng).title(name))

        tvDestination.text = name
        tvDuration.text = "Loading..."
        tvDistance.text = ""
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        getCurrentLocation { location ->
            location?.let { fetchRoute(LatLng(it.latitude, it.longitude), latLng) }
        }
    }

    private fun fetchRoute(start: LatLng, end: LatLng) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&mode=driving&alternatives=true&key=$GOOGLE_API_KEY")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string()

                if (jsonData != null) {
                    withContext(Dispatchers.Main) {
                        parseRouteData(JSONObject(jsonData))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching route", e)
            }
        }
    }

    private fun checkPermissionsAndEnableLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        map.isMyLocationEnabled = true
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1000)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        // Immediately try to get current location once
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                Log.d(TAG, "Got last known location: ${location.latitude}, ${location.longitude}")
            } else {
                Log.w(TAG, "Last known location is null. Waiting for updates...")
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to get last known location: ${it.message}")
        }
    }

    private fun searchNearbyPlaces(type: String) {
        val location = currentLocation
        if (location == null) {
            Toast.makeText(this, "Unable to get current location. Please wait...", Toast.LENGTH_SHORT).show()
            return
        }

        map.clear()
        markerPlaceMap.clear()

        val locationString = "${location.latitude},${location.longitude}"
        val radius = 5000 // 5km

        Toast.makeText(this, "Searching for nearby ${type}s...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=$locationString&radius=$radius&type=$type&key=$GOOGLE_API_KEY"

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
                    val response = reader.use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optString("status", "")

                    if (status == "REQUEST_DENIED" || status == "INVALID_REQUEST") {
                        val errorMessage = jsonResponse.optString("error_message", "Unknown error")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MapActivity,
                                "API Error: $errorMessage. Check your API key and Places API setup.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }

                    val results = jsonResponse.optJSONArray("results") ?: return@launch
                    withContext(Dispatchers.Main) {
                        for (i in 0 until results.length()) {
                            val place = results.getJSONObject(i)
                            val name = place.optString("name", "Unknown")
                            val geometry = place.optJSONObject("geometry")?.optJSONObject("location")
                            val lat = geometry?.optDouble("lat") ?: continue
                            val lng = geometry.optDouble("lng")
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
                                else -> BitmapDescriptorFactory.HUE_ORANGE
                            }

                            val marker = map.addMarker(
                                MarkerOptions()
                                    .position(placeLatLng)
                                    .title(name)
                                    .snippet("Distance: %.2f km".format(distance))
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                            )
                            marker?.let { markerPlaceMap[it] = placeLatLng }
                        }

                        if (results.length() == 0) {
                            Toast.makeText(this@MapActivity, "No ${type}s found nearby", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MapActivity, "Found ${results.length()} ${type}s", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MapActivity,
                            "Failed to fetch places (HTTP $responseCode). Check API key or billing.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching places: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MapActivity,
                        "Error: ${e.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun searchPlaceByName(placeName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedQuery = java.net.URLEncoder.encode(placeName, "UTF-8")
                val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedQuery&key=$GOOGLE_API_KEY"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")

                val results = jsonResponse.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    val location = results.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location")
                    val lat = location.getDouble("lat")
                    val lng = location.getDouble("lng")
                    val placeLatLng = LatLng(lat, lng)
                    val address = results.getJSONObject(0).optString("formatted_address", placeName)

                    withContext(Dispatchers.Main) {
                        map.clear()
                        map.addMarker(MarkerOptions().position(placeLatLng).title(address))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15f))
                        Toast.makeText(this@MapActivity, "Showing results for: $address", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapActivity, "No results found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseRouteData(json: JSONObject) {
        val routes = json.getJSONArray("routes")
        if (routes.length() == 0) return

        var shortestIndex = 0
        var minDistance = Int.MAX_VALUE

        for (i in 0 until routes.length()) {
            val dist = routes.getJSONObject(i).getJSONArray("legs")
                .getJSONObject(0).getJSONObject("distance").getInt("value")
            if (dist < minDistance) {
                minDistance = dist
                shortestIndex = i
            }
        }

        for (i in 0 until routes.length()) {
            val isShortest = (i == shortestIndex)
            val route = routes.getJSONObject(i)
            val polyline = route.getJSONObject("overview_polyline").getString("points")
            val points = decodePolyline(polyline)
            if (isShortest) currentRoutePoints = points

            val line = map.addPolyline(
                PolylineOptions()
                    .addAll(points)
                    .color(if (isShortest) Color.parseColor("#4285F4") else Color.GRAY)
                    .width(if (isShortest) 16f else 12f)
            )
            activePolylines.add(line)
        }

        val shortestLeg = routes.getJSONObject(shortestIndex).getJSONArray("legs").getJSONObject(0)
        tvDuration.text = shortestLeg.getJSONObject("duration").getString("text")
        tvDistance.text = "(${shortestLeg.getJSONObject("distance").getString("text")})"

        try {
            val bounds = LatLngBounds.Builder().apply {
                currentRoutePoints.forEach { include(it) }
            }.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
        } catch (e: Exception) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentRoutePoints.first(), 15f))
        }
    }

    private fun startNavigation() {
        isNavigating = true
        btnStartNavigation.text = "Exit Navigation"
        btnStartNavigation.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.holo_red_dark)
        tvInstruction.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        getCurrentLocation { loc ->
            loc?.let {
                val camPos = CameraPosition.Builder()
                    .target(LatLng(it.latitude, it.longitude))
                    .zoom(19f)
                    .tilt(60f)
                    .bearing(it.bearing)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
            }
        }
    }

    private fun stopNavigation() {
        isNavigating = false
        btnStartNavigation.text = "Start Navigation"
        btnStartNavigation.backgroundTintList =
            ContextCompat.getColorStateList(this, com.google.android.material.R.color.design_default_color_primary)
        tvInstruction.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        getCurrentLocation { loc ->
            loc?.let {
                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(it.latitude, it.longitude))
                            .zoom(16f)
                            .tilt(0f)
                            .build()
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(onLocation: (Location?) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { onLocation(it) }
    }

    private fun recenterCamera() {
        getCurrentLocation { loc ->
            loc?.let {
                val update = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                map.animateCamera(update)
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            poly.add(LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                for (location in res.locations) {
                    if (isNavigating) updateNavigation(location)
                }
            }
        }
    }

    private fun updateNavigation(location: Location) {
        val camPos = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(19f)
            .tilt(60f)
            .bearing(location.bearing)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos), 1000, null)

        currentDestination?.let { dest ->
            val results = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, dest.latitude, dest.longitude, results)
            val distanceKm = results[0] / 1000
            tvInstruction.text = "Continue for ${String.format("%.1f", distanceKm)} km"

            if (results[0] < 50) {
                Toast.makeText(this, "You have arrived!", Toast.LENGTH_LONG).show()
                stopNavigation()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocation()
        }
    }
}


//package com.krishna.soslocation
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.Location
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.android.gms.tasks.CancellationTokenSource
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.json.JSONObject
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.net.HttpURLConnection
//import java.net.URL
//import kotlin.math.atan2
//import kotlin.math.cos
//import kotlin.math.sin
//import kotlin.math.sqrt
//
//class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
//
//    private lateinit var map: GoogleMap
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private var currentLocation: Location? = null
//    private val markerPlaceMap = mutableMapOf<Marker, LatLng>()
//
//    private val apiKey = "AIzaSyD4-i3oGh4TWl56f3zQrsjSw2zbk5uV81Y" // "AIzaSyCarxDw-8IzNS_IHR7Ms-6uJT4bFiGqlnk"
//
//    companion object {
//        private const val TAG = "MapActivity"
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        try {
//            setContentView(R.layout.activity_map)
//            Log.d(TAG, "MapActivity created successfully")
//
//            val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
//            setSupportActionBar(toolbar)
//            supportActionBar?.apply {
//                title = "Find Place"
//                setDisplayHomeAsUpEnabled(true)
//            }
//
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//            val mapFragment = supportFragmentManager
//                .findFragmentById(R.id.map) as? SupportMapFragment
//
//            if (mapFragment != null) {
//                mapFragment.getMapAsync(this)
//                Log.d(TAG, "Map fragment initialized")
//            } else {
//                Log.e(TAG, "Map fragment is null")
//                Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show()
//            }
//
//            setupFABs()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in onCreate: ${e.message}", e)
//            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//
//    }
//
//    private fun setupFABs() {
//        try {
//            findViewById<FloatingActionButton>(R.id.fab_hospitals)?.setOnClickListener {
//                searchNearbyPlaces("hospital")
//            }
//
//            findViewById<FloatingActionButton>(R.id.fab_police)?.setOnClickListener {
//                searchNearbyPlaces("police")
//            }
//
//            findViewById<FloatingActionButton>(R.id.fab_pharmacy)?.setOnClickListener {
//                searchNearbyPlaces("pharmacy")
//            }
//            Log.d(TAG, "FABs setup successfully")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up FABs: ${e.message}", e)
//        }
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        try {
//            map = googleMap
//            map.setOnMarkerClickListener(this)
//            Log.d(TAG, "Map is ready")
//
//            if (checkLocationPermission()) {
//                enableMyLocation()
//            } else {
//                requestLocationPermission()
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in onMapReady: ${e.message}", e)
//            Toast.makeText(this, "Error initializing map", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun checkLocationPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestLocationPermission() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ),
//            LOCATION_PERMISSION_REQUEST_CODE
//        )
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                enableMyLocation()
//            } else {
//                Toast.makeText(
//                    this,
//                    "Location permission is required to show your location",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }
//
//    private fun enableMyLocation() {
//        try {
//            if (checkLocationPermission()) {
//                map.isMyLocationEnabled = true
//
//                // Get current location with high accuracy
//                val cancellationTokenSource = CancellationTokenSource()
//                fusedLocationClient.getCurrentLocation(
//                    Priority.PRIORITY_HIGH_ACCURACY,
//                    cancellationTokenSource.token
//                ).addOnSuccessListener { location: Location? ->
//                    if (location != null) {
//                        currentLocation = location
//                        val currentLatLng = LatLng(location.latitude, location.longitude)
//                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
//                        Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")
//                    } else {
//                        Log.w(TAG, "Location is null, trying last known location")
//                        getLastKnownLocation()
//                    }
//                }.addOnFailureListener { e ->
//                    Log.e(TAG, "Failed to get current location: ${e.message}")
//                    getLastKnownLocation()
//                }
//            }
//        } catch (e: SecurityException) {
//            Log.e(TAG, "SecurityException: ${e.message}", e)
//        }
//    }
//
//    private fun getLastKnownLocation() {
//        try {
//            if (checkLocationPermission()) {
//                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//                    location?.let {
//                        currentLocation = it
//                        val currentLatLng = LatLng(it.latitude, it.longitude)
//                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
//                        Log.d(TAG, "Last known location: ${it.latitude}, ${it.longitude}")
//                    } ?: run {
//                        Log.w(TAG, "No last known location available")
//                        Toast.makeText(
//                            this,
//                            "Unable to get location. Please ensure GPS is enabled.",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            }
//        } catch (e: SecurityException) {
//            Log.e(TAG, "SecurityException in getLastKnownLocation: ${e.message}", e)
//        }
//    }
//
//    private fun searchNearbyPlaces(type: String) {
//        currentLocation?.let { location ->
//            map.clear()
//            markerPlaceMap.clear()
//
//            val locationString = "${location.latitude},${location.longitude}"
//            val radius = 5000 // 5km radius
//
//            Toast.makeText(this, "Searching for nearby ${type}s...", Toast.LENGTH_SHORT).show()
//
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
//                            "location=$locationString" +
//                            "&radius=$radius" +
//                            "&type=$type" +
//                            "&key=$apiKey"
//
//                    Log.d(TAG, "Searching URL: $urlString")
//
//                    val url = URL(urlString)
//                    val connection = url.openConnection() as HttpURLConnection
//                    connection.requestMethod = "GET"
//                    connection.connectTimeout = 10000
//                    connection.readTimeout = 10000
//                    connection.connect()
//
//                    val responseCode = connection.responseCode
//                    Log.d(TAG, "Response code: $responseCode")
//
//                    if (responseCode == HttpURLConnection.HTTP_OK) {
//                        val inputStream = connection.inputStream
//                        val reader = BufferedReader(InputStreamReader(inputStream))
//                        val response = StringBuilder()
//                        var line: String?
//
//                        while (reader.readLine().also { line = it } != null) {
//                            response.append(line)
//                        }
//                        reader.close()
//
//                        Log.d(TAG, "Response: ${response.toString().take(200)}...")
//
//                        val jsonResponse = JSONObject(response.toString())
//                        val status = jsonResponse.optString("status", "")
//
//                        if (status == "REQUEST_DENIED") {
//                            val errorMessage = jsonResponse.optString("error_message", "Unknown error")
//                            withContext(Dispatchers.Main) {
//                                Toast.makeText(
//                                    this@MapActivity,
//                                    "API Error: $errorMessage. Please check your API key.",
//                                    Toast.LENGTH_LONG
//                                ).show()
//                            }
//                            return@launch
//                        }
//
//                        val results = jsonResponse.getJSONArray("results")
//
//                        withContext(Dispatchers.Main) {
//                            for (i in 0 until results.length()) {
//                                val place = results.getJSONObject(i)
//                                val name = place.getString("name")
//                                val locationObj = place.getJSONObject("geometry").getJSONObject("location")
//                                val lat = locationObj.getDouble("lat")
//                                val lng = locationObj.getDouble("lng")
//                                val placeLatLng = LatLng(lat, lng)
//
//                                val distance = calculateDistance(
//                                    location.latitude,
//                                    location.longitude,
//                                    lat,
//                                    lng
//                                )
//
//                                val markerColor = when (type) {
//                                    "hospital" -> BitmapDescriptorFactory.HUE_RED
//                                    "police" -> BitmapDescriptorFactory.HUE_BLUE
//                                    "pharmacy" -> BitmapDescriptorFactory.HUE_GREEN
//                                    else -> BitmapDescriptorFactory.HUE_RED
//                                }
//
//                                val marker = map.addMarker(
//                                    MarkerOptions()
//                                        .position(placeLatLng)
//                                        .title(name)
//                                        .snippet("Distance: %.2f km".format(distance))
//                                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
//                                )
//
//                                marker?.let {
//                                    markerPlaceMap[it] = placeLatLng
//                                }
//                            }
//
//                            if (results.length() == 0) {
//                                Toast.makeText(
//                                    this@MapActivity,
//                                    "No ${type}s found nearby",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            } else {
//                                Toast.makeText(
//                                    this@MapActivity,
//                                    "Found ${results.length()} ${type}s",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                    } else {
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(
//                                this@MapActivity,
//                                "Failed to fetch places (Error: $responseCode). Please check your API key.",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error searching places: ${e.message}", e)
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            this@MapActivity,
//                            "Error: ${e.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            }
//        } ?: run {
//            Toast.makeText(this, "Unable to get current location. Please wait...", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
//        val R = 6371.0 // Earth's radius in kilometers
//        val dLat = Math.toRadians(lat2 - lat1)
//        val dLon = Math.toRadians(lon2 - lon1)
//        val a = sin(dLat / 2) * sin(dLat / 2) +
//                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
//                sin(dLon / 2) * sin(dLon / 2)
//        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
//        return R * c
//    }
//
//    override fun onMarkerClick(marker: Marker): Boolean {
//        currentLocation?.let { location ->
//            markerPlaceMap[marker]?.let { destination ->
//                val origin = "${location.latitude},${location.longitude}"
//                val dest = "${destination.latitude},${destination.longitude}"
//
//                val intent = Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$dest&travelmode=driving")
//                )
//                intent.setPackage("com.google.android.apps.maps")
//
//                try {
//                    if (intent.resolveActivity(packageManager) != null) {
//                        startActivity(intent)
//                    } else {
//                        // If Google Maps app is not installed, open in browser
//                        val browserIntent = Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$dest&travelmode=driving")
//                        )
//                        startActivity(browserIntent)
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error opening maps: ${e.message}", e)
//                    Toast.makeText(this, "Unable to open navigation", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        return true
//    }
//}
//

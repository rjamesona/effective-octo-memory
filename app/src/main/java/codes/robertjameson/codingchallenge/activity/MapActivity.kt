package codes.robertjameson.codingchallenge.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import codes.robertjameson.codingchallenge.R
import codes.robertjameson.codingchallenge.databinding.ActivityMapsBinding
import codes.robertjameson.codingchallenge.model.Place
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val permissionID = 42
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var _binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var currentLocation: LatLng
    private val binding get() = _binding

    // Code some of the code for this from the official Android developer website
    // https://developers.google.com/maps/documentation/android-sdk/overview?section=start

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMapsBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        // Fetch Google Maps API
        val applicationInfo: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = applicationInfo.metaData["com.google.android.maps.v2.API_KEY"]
        val apiKey = value.toString()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                googleMap.clear()

                // Search places must run on a IO thread in this context
                CoroutineScope(Dispatchers.Main.immediate).launch {
                    var places: MutableList<Place>

                    withContext(Dispatchers.IO) {
                        places = searchPlaces(query, apiKey)
                    }
                    if (places.isNotEmpty()) {

                        for (place in places) {
                            val latLng = LatLng(place.lat, place.lng)
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(
                                        latLng
                                    )
                                    .title(place.name)
                            )
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "No places found, please try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        getLastLocation()
    }

    // Hit Google Maps API to search for places nearby using the device current location
    private fun searchPlaces(query: String?, apiKey: String): MutableList<Place> {
        val places = mutableListOf<Place>()
        val url =
            URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${currentLocation.latitude},${currentLocation.longitude}&keyword=${query}&radius=1500&key=${apiKey}")
        val jsonObject = JSONObject(url.readText())
        val jsonArray = jsonObject.getJSONArray("results")

        // Loop through the places fetched from the API
        for (i in 0 until jsonArray.length()) {
            // Use only 5 places fetched
            if (i > 4) break
            val place = jsonArray.getJSONObject(i)
            val location = place.getJSONObject("geometry").getJSONObject("location")
            val lat = location.getDouble("lat")
            val lng = location.getDouble("lng")
            places.add(
                Place(
                    place.get("place_id").toString(),
                    place.get("name").toString(),
                    lat,
                    lng
                )
            )
        }
        return places
    }

    // Get the device last known location if GPS permissions are enabled
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        googleMap.clear()
                        // Mark current location on the map
                        googleMap.addMarker(MarkerOptions().position(currentLocation))
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLocation,
                                15F
                            )
                        )
                        googleMap.isMyLocationEnabled = true
                    }
                }
            } else {
                Toast.makeText(this, "I need location to work", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermissions()
        fusedLocationClient.requestLocationUpdates(
            locationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    // Callback used when the location needs update
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Request permissions if not granted before
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }
}
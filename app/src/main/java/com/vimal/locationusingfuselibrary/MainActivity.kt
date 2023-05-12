package com.vimal.locationusingfuselibrary

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.vimal.locationusingfuselibrary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private lateinit var binding: ActivityMainBinding
    //for location GPS
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var altitude: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var isGPS = false
    private val REQUEST_PERMISSIONS = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewBinding = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()){
                getGPS()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        val loc =ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val loc2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this,
                listPermissionsNeeded.toTypedArray<String>(),REQUEST_PERMISSIONS)
            return false
        }
        return true
    }

    private fun getGPS() {
        GpsUtils(this).turnGPSOn { isGPSEnable -> // turn on GPS
            isGPS = isGPSEnable
        }
        buildGoogleApiClient()
    }

    @Synchronized
    fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks((this as GoogleApiClient.ConnectionCallbacks))
            .addOnConnectionFailedListener((this as GoogleApiClient.OnConnectionFailedListener))
            .addApi(LocationServices.API)
            .build()
    }

    @SuppressLint("SetTextI18n")
    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.interval = 100 // Update location every second
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient!!, mLocationRequest!!,
            (this as LocationListener)
        )
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
            mGoogleApiClient!!
        )
        if (mLastLocation != null) {
            latitude = mLastLocation!!.latitude.toString()
            longitude = mLastLocation!!.longitude.toString()
            altitude = mLastLocation!!.altitude.toString()
            Log.e("LAT", "onConnected: $latitude")
            Log.e("LONG", "onConnected: $longitude")
            Log.e("ALTI", "onConnected: $altitude")

            binding.tvLatLong.text = "Lat-Long\n$latitude\n$longitude"
        }
    }

    override fun onConnectionSuspended(i: Int) {}

    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()
        println("latitude>>>>$latitude")

        binding.tvLatLongOnLocationChange.text = "On Location change Lat-Long\n$latitude\n$longitude"

        altitude = location.altitude.toString()
        //String Address=cf.getAddress(Double.parseDouble(latitude), Double.parseDouble(longitude));
        val pref = applicationContext.getSharedPreferences(
            "GCMSetting",
            MODE_PRIVATE
        ) // 0 - for private mode
        val editor = pref.edit()
        editor.putString("LATTITUDE>>>", latitude)
        editor.putString("LONGITUDE>>>", longitude)
        editor.putString("ALTITUDE>>>", altitude)

        Log.e("LAT", "onLocationChanged: $latitude")
        Log.e("LONG", "onLocationChanged: $longitude")
        Log.e("ALTI", "onLocationChanged: $altitude")

        editor.commit() // commit changes
    }

    override fun onStart() {
        super.onStart()
        if (!mGoogleApiClient!!.isConnected) mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
}
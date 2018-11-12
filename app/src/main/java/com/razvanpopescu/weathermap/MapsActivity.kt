package com.razvanpopescu.weathermap

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import khttp.responses.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.razvanpopescu.weathermap.R.id.map
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import kotlin.concurrent.thread

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val tm = LatLng(45.748217, 21.225476)
//        mMap.addMarker(MarkerOptions().position(sydney).title("De unde pleaca smecheria"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tm,12.0f))
        mMap.setOnMapClickListener {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(it))
            thread {
                val response: Response = khttp.get("https://maps.googleapis.com/maps/api/geocode/json?latlng=${it.latitude},${it.longitude}&key=%20AIzaSyBSWlLzXNqG8RxiZyy-ZVARw_ux6LXHYbg")
                val adressJson = response.jsonObject
                var adress = adressJson.getJSONArray("results").getJSONObject(0).getJSONArray("address_components")
                for(item in 0 until adress.length()){
                    val type = adress.getJSONObject(item).getJSONArray("types").getString(0)
                    if(type == "locality" || type == "postal_town"){
                        var city = adress.getJSONObject(item).getString("long_name")
                        city = URLEncoder.encode(city, "UTF-8")
                        getweather(city)
                        break
                    }
                }
            }
        }
    }

    fun displayTemm(tempJson: JSONObject, city : String){

        var temperature = (tempJson.get("main") as JSONObject).get("temp") as Double
        temperature -= 273
        runOnUiThread {
            Toast.makeText(applicationContext, "$city :$temperature", Toast.LENGTH_LONG).show()
        }
    }

    fun getweather(city : String){

        val response1: Response = khttp.get("http://api.openweathermap.org/data/2.5/weather?q=$city&APPID=9339bb97061b8aeef4241a95974604fb")
        val tempJson: JSONObject = response1.jsonObject
        displayTemm(tempJson, city)

    }

}

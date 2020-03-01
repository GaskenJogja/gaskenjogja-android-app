package id.herocode.gaskenjogja.activity

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import id.herocode.gaskenjogja.R
import id.herocode.gaskenjogja.utils.AppConstants
import id.herocode.gaskenjogja.utils.AppUtils
import id.herocode.gaskenjogja.utils.HttpRequest
import id.herocode.gaskenjogja.utils.PreferenceHelper
import kotlinx.android.synthetic.main.activity_wisata_terdekat.*
import org.json.JSONArray
import org.json.JSONObject

class WisataTerdekat : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wisata_terdekat)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_wisata) as SupportMapFragment
        mapFragment.getMapAsync(this)
        preferenceHelper = PreferenceHelper(this)
        radius_1km.setOnClickListener(this)
        radius_2km.setOnClickListener(this)
        radius_5km.setOnClickListener(this)
        radius_10km.setOnClickListener(this)
        radius_custom.setOnEditorActionListener(object: TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if  ((event!=null && event.keyCode === KeyEvent.KEYCODE_ENTER) || (actionId == EditorInfo.IME_ACTION_GO)) {
                    val rad: String = radius_custom.text.toString()
                    radius_1km.isClickable = true
                    radius_2km.isClickable = true
                    radius_5km.isClickable = true
                    radius_10km.isClickable = true
                    getWisata(rad)
                }
                return false
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.radius_1km -> {
                radius_1km.isClickable = false
                radius_2km.isClickable = true
                radius_5km.isClickable = true
                radius_10km.isClickable = true
                getWisata("1.0")
            }
            R.id.radius_2km -> {
                radius_1km.isClickable = true
                radius_2km.isClickable = false
                radius_5km.isClickable = true
                radius_10km.isClickable = true
                getWisata("2.0")
            }
            R.id.radius_5km -> {
                radius_1km.isClickable = true
                radius_2km.isClickable = true
                radius_5km.isClickable = false
                radius_10km.isClickable = true
                getWisata("5.0")
            }
            R.id.radius_10km -> {
                radius_1km.isClickable = true
                radius_2km.isClickable = true
                radius_5km.isClickable = true
                radius_10km.isClickable = false
                getWisata("10.0")
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        getWisata("0")
//        val userLocation = LatLng(preferenceHelper.lat.toDouble(), preferenceHelper.lon.toDouble())
//        val cameraPosition = CameraPosition.builder()
//            .target(userLocation)
//            .zoom(14f)
//            .bearing(0f)
//            .build()
//
//        mMap.addMarker(MarkerOptions().position(userLocation).title("Lokasi Anda"))
//
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null)
//        mMap.addCircle(CircleOptions()
//            .center(userLocation)
//            .radius(2000.0)
//            .fillColor(Color.argb(10,255,0,0))
//            .strokeColor(Color.TRANSPARENT)
//            .visible(true))
    }

    private fun getWisata(radius: String) {
        val rad: String
        rad = if(!radius.equals("0")) radius else "2"
        if (!AppUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Internet is required!", Toast.LENGTH_SHORT).show()
            return
        }
        AppUtils.showSimpleProgressDialog(this, "Update Lokasi", "Sedang memuat data, mohon tunggu sejenak...", true)
        val map: HashMap<String, String> = HashMap()
        map[AppConstants.Params.ID_USER] = preferenceHelper.id_user.toString()
        map["lat"] = preferenceHelper.lat.toString()
        map["lon"] = preferenceHelper.lon.toString()
        map[AppConstants.Params.RADIUS] = rad
        object: AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String {
                var response: String
                try {
                    val req = HttpRequest(AppConstants.ServiceType.LOKASI_TERDEKAT)
                    response = req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString()
                } catch (e: Exception) {
                    response = e.message.toString()
                }
                return response
            }

            override fun onPostExecute(result: String) {
                val isLogin: Boolean = preferenceHelper.isLogin
                onTaskComplete(result, isLogin, rad)
            }
        }.execute()
    }

    private fun onTaskComplete(response: String, task: Boolean, rad: String) {
        AppUtils.removeSimpleProgressDialog()
        if (task) {
            try {
                mMap.clear()
                val jsonObject = JSONObject(response)
                if  (jsonObject.getString("message").equals("tidak_ada_lokasi_terdekat")) {
                    println("Tidak ada lokasi terdekat!")
                    val snackbar = Snackbar.make(findViewById(R.id.map_wisata), "Lokasi Wisata terdekat tidak ada", Snackbar.LENGTH_LONG).setAction("OKE!") {}
                    val snackbarLayout = snackbar.view
                    snackbarLayout.setBackgroundResource(R.color.black)
                    snackbar.setTextColor(resources.getColor(R.color.white))
                    snackbar.setActionTextColor(resources.getColor(R.color.colorPrimaryDark))
                    snackbar.show()
                } else if (jsonObject.getString("message").equals("data_lokasi_tersedia")) {
                    val jsonArray: JSONArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val data: JSONObject = jsonArray.getJSONObject(i)
                        val posisi = LatLng(
                            data.getString("lat").toDouble(),
                            data.getString("lon").toDouble()
                        )
                        mMap.addMarker(MarkerOptions()
                            .position(posisi)
                            .title(data.getString("nama_wisata"))
                            .snippet("HTM: Rp." + data.getString("harga") +",-")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val userLocation = LatLng(preferenceHelper.lat.toDouble(), preferenceHelper.lon.toDouble())
            val cameraPosition = CameraPosition.builder()
                .target(userLocation)
                .zoom(14f)
                .bearing(5f)
                .build()

            mMap.addMarker(MarkerOptions().position(userLocation).title("Lokasi Anda"))

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null)
            mMap.addCircle(CircleOptions()
                .center(userLocation)
                .radius(rad.toDouble() * 1000)
                .fillColor(Color.argb(55,0,255,0))
                .strokeColor(Color.TRANSPARENT)
                .visible(true))
        }
    }

}

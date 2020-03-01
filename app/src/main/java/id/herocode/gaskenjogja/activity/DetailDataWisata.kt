package id.herocode.gaskenjogja.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import id.herocode.gaskenjogja.R
import id.herocode.gaskenjogja.iface.LokasiInteraction
import id.herocode.gaskenjogja.utils.*
import kotlinx.android.synthetic.main.activity_detail_data_wisata.*
import java.io.IOException

class DetailDataWisata : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener, LokasiInteraction {

    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var parseContent: ParseContent
    private lateinit var lokasiInteraction: LokasiInteraction

    private lateinit var mMap: GoogleMap
    private var idWisata: Int = 0
    private lateinit var namaWisata: String
    private lateinit var imgWisata: ByteArray
    private var htmWisata: Int = 0
    private lateinit var alamatWisata: String
    private var lat: Double = 0.0
    private var lon: Double = 0.0

    companion object {
        const val ID_WISATA = "id_wisata"
        const val NAMA_WISATA = "nama_wisata"
        const val IMG_WISATA = "img_wisata"
        const val HTM_WISATA = "htm_wisata"
        const val ALAMAT_WISATA = "alamat_wisata"
        const val LAT = "lat"
        const val LON = "lon"
    }



    override fun updateLokasi() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_data_wisata)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        preferenceHelper = PreferenceHelper(this)
        parseContent = ParseContent(this)
        lokasiInteraction = this

        idWisata = intent.getIntExtra(ID_WISATA,0)
        namaWisata = intent.getStringExtra(NAMA_WISATA)!!
        imgWisata = intent.getByteArrayExtra(IMG_WISATA)!!
        alamatWisata = intent.getStringExtra(ALAMAT_WISATA)!!
        htmWisata = intent.getIntExtra(HTM_WISATA, 0)
        lat = intent.getDoubleExtra(LAT, 0.0)
        lon = intent.getDoubleExtra(LON, 0.0)

        val imgBitMap = BitmapFactory.decodeByteArray(imgWisata, 0, imgWisata.size)
        Glide.with(this@DetailDataWisata)
            .load(imgBitMap)
            .centerCrop()
            .into(image_wisata)

        nama_wisata.text = namaWisata
        alamat_wisata.text = "Alamat: $alamatWisata"
        htm_wisata.text = "HTM Wisata: Rp.$htmWisata,-"

        btn_booking.setOnClickListener(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val location = LatLng(lat, lon)
        val zoomLevel = 10.0f
        mMap.addMarker(MarkerOptions().position(location).title(namaWisata))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_booking -> {
                if (preferenceHelper.saldo < htmWisata) {
                    val snackbar = Snackbar.make(findViewById(R.id.base_layout_detail_wisata), "Saldo tidak cukup!", Snackbar.LENGTH_LONG).setAction("ISI SALDO") {
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.putExtra("openFragment", 2)
                        startActivity(intent)
                        this.finish()
                    }
                    val snackbarLayout = snackbar.view
                    snackbarLayout.setBackgroundResource(R.drawable.bg_menu_atas)
                    snackbar.show()
                } else {
                    bookingWisata()
                }
            }
        }
    }

    private fun bookingWisata() {
        if (!AppUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Internet is required!", Toast.LENGTH_SHORT).show()
            return
        }
        AppUtils.showDialogPembayaran(this)
        val map: HashMap<String, String> = HashMap()
        map[AppConstants.Params.ID_USER] = preferenceHelper.id_user.toString()
        map[AppConstants.Params.ID_WISATA] = idWisata.toString()
        map[AppConstants.Params.BAYAR] = htmWisata.toString()
        map[AppConstants.Params.SALDO] = preferenceHelper.saldo.toString()

        object: AsyncTask<Void,Void,String>() {
            override fun doInBackground(params: Array<Void>): String {
                var response: String
                try {
                    val req = HttpRequest(AppConstants.ServiceType.BOOKING)
                    response = req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString()
                } catch (e : IOException) {
                    response = e.message.toString()
                } catch (e : Exception) {
                    response = e.message.toString()
                }
                return response
            }

            override fun onPostExecute(response: String) {
                val isLogin: Boolean = preferenceHelper.isLogin
                onTaskCompleted(response, isLogin)
            }
        }.execute()
    }

    private fun onTaskCompleted(response: String, task: Boolean) {
        AppUtils.removeSimpleProgressDialog()
        if (task) {
            if (parseContent.isSuccess(response)) {
                Toast.makeText(this, "Pesanan Sukses!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra("openFragment", 1)
                intent.putExtra("updateSaldo", true)
                intent.putExtra("updateTransaksi", true)
                startActivity(intent)
                this.finish()
            } else {
                Toast.makeText(this, "Pesanan Gagal, ulangi beberapa saat lagi!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

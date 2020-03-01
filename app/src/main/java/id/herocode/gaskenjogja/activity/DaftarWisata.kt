package id.herocode.gaskenjogja.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.herocode.gaskenjogja.R
import id.herocode.gaskenjogja.adapter.CardViewWisataAdapter
import id.herocode.gaskenjogja.model.ModelWisata
import id.herocode.gaskenjogja.utils.AppConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DaftarWisata : AppCompatActivity() {

    private lateinit var modelWisata: ArrayList<ModelWisata>
    private lateinit var wisataAdapter: CardViewWisataAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_wisata)
        this.recyclerView = findViewById(R.id.rv_daftar_wisata)
        this.modelWisata = ArrayList()

        val lineaLayoutManager = LinearLayoutManager(this@DaftarWisata)
        lineaLayoutManager.orientation = LinearLayoutManager.VERTICAL
        wisataAdapter = CardViewWisataAdapter(this@DaftarWisata, modelWisata)
        recyclerView.layoutManager = lineaLayoutManager
        recyclerView.adapter = wisataAdapter

        loadDataWisata()
    }

    private fun loadDataWisata() {
        val stringRequest = StringRequest(Request.Method.GET, API_DATA_WISATA,
            Response.Listener<String> { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val jsonArray: JSONArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val wisata: JSONObject = jsonArray.getJSONObject(i)
                        modelWisata.add(
                            ModelWisata(
                                wisata.getInt("id_wisata"),
                                wisata.getString("nama_wisata"),
                                wisata.getString("alamat"),
                                wisata.getInt("harga"),
                                wisata.getString("gambar"),
                                wisata.getDouble("lat"),
                                wisata.getDouble("lon")
                            )
                        )
                    }
                    wisataAdapter = CardViewWisataAdapter(this@DaftarWisata, modelWisata)
                    recyclerView.adapter = wisataAdapter

                    wisataAdapter.setClickItem(object : CardViewWisataAdapter.ClickItem {
                        override fun itemClicked(data: ModelWisata) {
                            showDataWisata(data)
                        }
                    })
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { })
        Volley.newRequestQueue(this@DaftarWisata).add(stringRequest)
    }

    private fun showDataWisata(modelWisata: ModelWisata) {
        val moveData = Intent(this@DaftarWisata, DetailDataWisata::class.java)
        moveData.putExtra(DetailDataWisata.ID_WISATA, modelWisata.iD_WISATA)
        moveData.putExtra(DetailDataWisata.NAMA_WISATA, modelWisata.namA_WISATA)
        moveData.putExtra(DetailDataWisata.ALAMAT_WISATA, modelWisata.alamaT_WISATA)
        moveData.putExtra(DetailDataWisata.HTM_WISATA, modelWisata.hargA_WISATA)
        moveData.putExtra(DetailDataWisata.IMG_WISATA, modelWisata.imG_WISATA)
        moveData.putExtra(DetailDataWisata.LAT, modelWisata.lat)
        moveData.putExtra(DetailDataWisata.LON, modelWisata.lon)
        startActivity(moveData)
    }

    companion object {
        private const val API_DATA_WISATA = AppConstants.ServiceType.DATA_WISATA
    }

}

package id.herocode.gaskenjogja.fragment


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import id.herocode.gaskenjogja.R
import id.herocode.gaskenjogja.activity.DashboardActivity
import id.herocode.gaskenjogja.activity.LoginActivity
import id.herocode.gaskenjogja.utils.*
import org.json.JSONException
import java.io.IOException

/**
 * A simple [DialogFragment] subclass.
 */
class PopupResultScan : DialogFragment() {

    private var METHOD = 0
    private var HTM = 0
    private var IDWISATA = 0
    private var NAMAWISATA = "nama_wisata"
    private var SALDO_AWAL = 0
    private var SALDO_AKHIR = 0
    private var IMG: ByteArray? = null
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var parseContent: ParseContent
    private lateinit var listener: FragmentActivity


    companion object {
        const val METHOD = "method"
        const val NAMA_WISATA = "nama_wisata"
        const val HTM_WISATA = "htm_wisata"
        const val IMG_WISATA = "img_wisata"
        const val SALDO_AWAL = "saldo"
        const val ID_WISATA = "id_wisata"
        fun newInstance(method: Int,name: String,htm: Int,img: String,saldo: Int,id_wisata: Int): PopupResultScan {
            val fragment = PopupResultScan()
            val gb: ByteArray = Base64.decode(img, Base64.DEFAULT)
            val bundle = Bundle().apply {
                putInt(METHOD, method)
                putString(NAMA_WISATA, name)
                putInt(HTM_WISATA, htm)
                putByteArray(IMG_WISATA, gb)
                putInt(SALDO_AWAL, saldo)
                putInt(ID_WISATA, id_wisata)
            }
            fragment.arguments = bundle
            println(bundle)
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            listener = context as FragmentActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_popup_booking, container, false)

        parseContent = ParseContent(listener)
        preferenceHelper = PreferenceHelper(listener)

        val tv_title = rootView.findViewById<TextView>(R.id.tv_popup_title)
        val btn_kembali = rootView.findViewById<Button>(R.id.btn_window_kembali)
        val btn_konfirmasi = rootView.findViewById<Button>(R.id.btn_window_konfirmasi)
        val btn_isi_saldo = rootView.findViewById<Button>(R.id.btn_window_isi_saldo)
        val tv_nama_wisata = rootView.findViewById<TextView>(R.id.tv_window_nama_wisata)
        val tv_htm_wisata = rootView.findViewById<TextView>(R.id.tv_detail_wisata_htm_wisata_isi)
        val tv_saldo_awal = rootView.findViewById<TextView>(R.id.tv_detail_wisata_saldo_anda_isi)
        val tv_saldo_akhir = rootView.findViewById<TextView>(R.id.tv_detail_wisata_sisa_saldo_isi)
        val tv_label_saldo_akhir = rootView.findViewById<TextView>(R.id.tv_detail_wisata_sisa_saldo_judul)
        val tv_label_saldo_awal = rootView.findViewById<TextView>(R.id.tv_detail_wisata_saldo_anda_judul)
        val tv_label_harga = rootView.findViewById<TextView>(R.id.tv_detail_wisata_htm_wisata_judul)
        val img_wisata = rootView.findViewById<ImageView>(R.id.bg_wisata)

        try {
            METHOD = arguments!!.getInt("method")
            NAMAWISATA = arguments!!.getString("nama_wisata")!!
            HTM = arguments!!.getInt("htm_wisata")
            SALDO_AWAL = arguments!!.getInt("saldo")
            SALDO_AKHIR = SALDO_AWAL - HTM
            IDWISATA = arguments!!.getInt(ID_WISATA)
            val imgBitmap: Bitmap =
                BitmapFactory.decodeByteArray(arguments?.getByteArray(
                    "img_wisata"),
                    0,
                    arguments?.getByteArray("img_wisata")!!.size)
            img_wisata.setImageBitmap(imgBitmap)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val intent = Intent(listener, DashboardActivity::class.java)

        when (METHOD) {
            1 -> {
                tv_title.text = "SUKSES"
                tv_nama_wisata.text = NAMAWISATA
                btn_kembali.visibility = View.GONE
                btn_isi_saldo.visibility = View.GONE
                tv_htm_wisata.visibility = View.GONE
                tv_saldo_awal.visibility = View.GONE
                tv_saldo_akhir.visibility = View.GONE
                tv_label_saldo_akhir.visibility = View.GONE
                tv_label_saldo_awal.visibility = View.GONE
                tv_label_harga.visibility = View.GONE

                btn_konfirmasi.setOnClickListener{
                    dismiss()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    listener.finish()
                }
            }
            2 -> {
                tv_title.text = "Konfirmasi Pembayaran"
                tv_nama_wisata.text = NAMAWISATA
                tv_htm_wisata.text = "Rp. " + HTM.toString()
                tv_saldo_awal.text = "Rp. " + SALDO_AWAL.toString()
                tv_saldo_akhir.text = "Rp. " + SALDO_AKHIR.toString()
                btn_isi_saldo.visibility = View.GONE

                btn_kembali.setOnClickListener {
                    dismiss()
                }
                btn_konfirmasi.setOnClickListener {
                    try {
                        bayarWisata()
                        dismiss()
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.putExtra("updateSaldo", true)
                        startActivity(intent)
                        listener.finish()
                    } catch (e : IOException) {
                        e.printStackTrace()
                    } catch (e : JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            3 -> {
                tv_title.text = "Konfirmasi Pembayaran"
                tv_nama_wisata.text = NAMAWISATA
                tv_htm_wisata.text = "Rp. " + HTM.toString()
                tv_saldo_awal.text = "Rp. " + SALDO_AWAL.toString()
                tv_saldo_akhir.visibility = View.GONE
                btn_konfirmasi.visibility = View.GONE
                btn_kembali.visibility = View.GONE
                tv_label_saldo_akhir.visibility = View.GONE

                btn_isi_saldo.setOnClickListener {
                    dismiss()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra("openFragment", 2)
                    startActivity(intent)
                    listener.finish()
                }

            }
            else -> {
                dismiss()
            }
        }
        return rootView
    }

    private fun bayarWisata() {
        if  (!AppUtils.isNetworkAvailable(listener)) {
            Toast.makeText(listener, "Internet is required", Toast.LENGTH_SHORT).show()
            return
        }
        AppUtils.showDialogPembayaran(listener)
        val map: HashMap<String,String> = HashMap()
        map[AppConstants.Params.ID_USER] = preferenceHelper.id_user.toString()
        map[AppConstants.Params.SALDO] = SALDO_AWAL.toString()
        map[AppConstants.Params.BAYAR] = HTM.toString()
        map[AppConstants.Params.ID_WISATA] = IDWISATA.toString()

        object: AsyncTask<Void, Void, String>() {
            protected override fun doInBackground(params: Array<Void>): String {
                var response : String?
                try {
                    val req = HttpRequest(AppConstants.ServiceType.UPDATE_SALDO)
                    response = req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString()
                } catch (e : IOException) {
                    response = e.message
                } catch (e : Exception) {
                    response = e.message
                }
                return response.toString()
            }

            protected override fun onPostExecute(response: String) {
                val isLogin: Boolean = preferenceHelper.isLogin
                onTaskCompleted(response, isLogin)
            }

        }.execute()
    }

    private fun onTaskCompleted(response : String, task : Boolean) {
        AppUtils.removeSimpleProgressDialog()
        if  (task) {
            parseContent.bayarWisata(response)
            var msg = parseContent.getMessageBayar(response)
            Toast.makeText(listener, msg, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(listener, "Anda harus login terlebih dahulu!", Toast.LENGTH_SHORT).show()
            val intent = Intent(listener, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            listener.finish()
        }
    }

}

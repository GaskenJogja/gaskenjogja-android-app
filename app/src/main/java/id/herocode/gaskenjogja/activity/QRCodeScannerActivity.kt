package id.herocode.gaskenjogja.activity

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import id.herocode.gaskenjogja.R
import id.herocode.gaskenjogja.fragment.PopupResultScan
import id.herocode.gaskenjogja.utils.*
import kotlinx.android.synthetic.main.activity_qrcode_scanner.*
import me.dm7.barcodescanner.core.IViewFinder
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class QRCodeScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler, View.OnClickListener {

    private lateinit var scannerView : ZXingScannerView
    private lateinit var preferenceHelper : PreferenceHelper
    private lateinit var parseContent : ParseContent
    private val QrCodeTask : Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scanner)
        preferenceHelper = PreferenceHelper(this)
        parseContent = ParseContent(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        btn_ulang_scan.visibility = View.GONE
        btn_ulang_scan.setOnClickListener(this)
        initScannerView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        scannerView.stopCamera()
        finish()
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun onStart() {
        super.onStart()
        scannerView.startCamera()
        doRequestPermission()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_ulang_scan -> {
                btn_ulang_scan.visibility = View.GONE
                scannerView.resumeCameraPreview(this)
                initScannerView()
            }
            else -> {

            }
        }
    }

    private fun doRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
            }
        }
    }

    private fun initScannerView() {
        scannerView = object : ZXingScannerView(this) {
            override fun createViewFinderView(context: Context?): IViewFinder {
                return CustomViewFinderView(context)
            }
        }
        scannerView.setAutoFocus(true)
        scannerView.setResultHandler(this)
        layout_camera.addView(scannerView)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                initScannerView()
            }
            else -> { }
        }
    }

    override fun handleResult(rawResult: Result?) {
        btn_ulang_scan.visibility = View.VISIBLE
        try {
            if  (!AppUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "Internet is required!", Toast.LENGTH_SHORT).show()
                return
            }
            AppUtils.showSimpleProgressDialog(this)
            val map : HashMap<String, String> = HashMap()
            map[AppConstants.Params.ID_USER] = preferenceHelper.id_user.toString()
            map[AppConstants.Params.QRCODEDATA] = rawResult.toString()

            object : AsyncTask<Void, Void, String>(){
                protected override fun doInBackground(params:Array<Void>): String {
                    var response : String
                    try {
                        val req = HttpRequest(AppConstants.ServiceType.QRCODE_DATA)
                        response =req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString()
                    } catch (e : Exception) {
                        response = e.message.toString()
                    }
                    return response
                }
                protected override fun onPostExecute(response : String) {
                    onTaskCompleted(response, QrCodeTask)
                }
            }.execute()
        } catch (e : IOException) {
            e.printStackTrace()
        } catch (e : JSONException) {
            e.printStackTrace()
        }
    }

    private fun onTaskCompleted(response : String, task : Int) {
        AppUtils.removeSimpleProgressDialog()
        if (task == QrCodeTask) {
            var method = 0
            when (parseContent.getMessageQRCode(response)) {
                "mode_booking" -> {
                    method = 1
//                    Toast.makeText(this, "Mode Booking", Toast.LENGTH_SHORT).show()
                }
                "mode_langsung" -> {
                    method = 2
//                    Toast.makeText(this, "Mode Langsung", Toast.LENGTH_SHORT).show()
                }
                "saldo_kurang" -> {
                    method = 3
//                    Toast.makeText(this, "Saldo Kurang", Toast.LENGTH_SHORT).show()
                }
                "not_found" -> {
                    Toast.makeText(this, "QRCode Tidak Ditemukan", Toast.LENGTH_SHORT).show()
//                    Handler().postDelayed({
//                        scannerView.resumeCameraPreview(this)
//                        initScannerView()
//                    },2000)
                }
                "tutup" -> {
                    Toast.makeText(this, "Yahh:( Tempat wisata sedang tutup", Toast.LENGTH_LONG).show()
//                    method = 4
                }
                else -> { }
            }
            if (method != 0) {
                    try {
                        val obj = JSONObject(response)
                        val data = obj.getJSONArray("data")
                        val dataBundle = Bundle()
                        for (i in 0 until data.length()) {
                            val dataObj = data.getJSONObject(i)
                            if  (method == 3) {
                                preferenceHelper.putSaldo(obj.getInt("saldo"))
                            }
                            dataBundle.apply {
                                putInt("method", method)
                                putString("nama_wisata", dataObj!!.getString("nama_wisata"))
                                putInt("htm_wisata", dataObj.getString("harga").toInt())
                                putString("img_wisata", dataObj.getString("gambar"))
                                putInt("saldo", preferenceHelper.saldo)
                                putInt("id_wisata", dataObj.getString("id_wisata").toInt())
                            }
                        }
                        val fm = supportFragmentManager
                        val popupFragment =
                            PopupResultScan.newInstance(
                                dataBundle.getInt("method"),
                                dataBundle.getString("nama_wisata")!!,
                                dataBundle.getInt("htm_wisata"),
                                dataBundle.getString("img_wisata")!!,
                                dataBundle.getInt("saldo"),
                                dataBundle.getInt("id_wisata")
                            )
                        popupFragment.show(fm, parseContent.getMessageQRCode(response))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

            }
        }
    }

    override fun onStop() {
        scannerView.stopCamera()
        super.onStop()
    }
}

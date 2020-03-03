package id.herocode.gaskenjogja.activity

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import id.herocode.gaskenjogja.R


class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                    ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                    CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                // Should we show an explanation?
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this@SplashScreen,
                        ACCESS_FINE_LOCATION) && !ActivityCompat.shouldShowRequestPermissionRationale(this@SplashScreen,
                        ACCESS_COARSE_LOCATION) && !ActivityCompat.shouldShowRequestPermissionRationale(this@SplashScreen,
                        CAMERA)
                ) {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, CAMERA),
                        200
                    )
                }
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
        }

        val splashTimeOut = 4000
        val homeIntent = Intent(this@SplashScreen, LoginActivity::class.java)
        Handler().postDelayed({
            startActivity(homeIntent)
            finish()
        }, splashTimeOut.toLong())
    }
}

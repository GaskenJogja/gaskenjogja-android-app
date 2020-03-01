package id.herocode.gaskenjogja.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import id.herocode.gaskenjogja.R

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val splashTimeOut = 2000
        val homeIntent = Intent(this@SplashScreen, LoginActivity::class.java)
        Handler().postDelayed({
            startActivity(homeIntent)
            finish()
        }, splashTimeOut.toLong())
    }
}

package id.herocode.gaskenjogja.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import me.dm7.barcodescanner.core.ViewFinderView

class CustomViewFinderView(context: Context?) : ViewFinderView(context) {

    private val paint = Paint()
    private val SCANNER_ALPHA = intArrayOf(0, 64, 128, 192, 255, 192, 128, 64)
    private var scannerAlpha: Int = 0
    private var cntr = 0
    private var goingup = false
    private val POINT_SIZE = 10
    private val ANIMATION_DELAY = 5L

    init {
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        setSquareViewFinder(true)
        setBorderColor(Color.parseColor("#35B781"))
        setLaserColor(Color.parseColor("#35B781"))
        setLaserEnabled(true)
    }

    override fun drawLaser(canvas: Canvas?) {
        /*super.drawLaser(canvas)*/
        paint.alpha = SCANNER_ALPHA[scannerAlpha]
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.size
        var middle = framingRect.height() / 2 + framingRect.top
        middle += cntr
        if (cntr < framingRect.top - mBorderLineLength - 10 && !goingup) {
            canvas?.drawRect((framingRect.left + 1).toFloat(), (middle - 3).toFloat(), (framingRect.right - 1).toFloat(), (middle + 3).toFloat(), mLaserPaint)
            cntr += 4
        }

        if (cntr >= framingRect.top - mBorderLineLength - 10 && !goingup) goingup = true

        if (cntr > -framingRect.top + mBorderLineLength && goingup) {
            canvas?.drawRect((framingRect.left + 1).toFloat(), (middle - 3).toFloat(), (framingRect.right - 1).toFloat(), (middle + 3).toFloat(), mLaserPaint)
            cntr -= 4
        }

        if (cntr <= -framingRect.top + mBorderLineLength && goingup) goingup = false

        postInvalidateDelayed(ANIMATION_DELAY,
            framingRect.left - 10,
            framingRect.top - 10,
            framingRect.right + 10,
            framingRect.bottom + 10)
    }

    public fun pauseLaser() {
        this.goingup = false
    }

}
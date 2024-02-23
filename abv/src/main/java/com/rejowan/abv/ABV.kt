package com.rejowan.abv

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.PaintDrawable
import android.os.BatteryManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import java.text.DecimalFormat

class ABV @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var topPaint = PaintDrawable()
    private var topPaintPercent = PaintDrawable()

    private var bodyPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private var bodyPaintPercent = PaintDrawable()

    private var topRect = Rect()
    private var topPercentRect = Rect()

    private var bodyRect = RectF()
    private var bodyPercentRect = Rect()

    private var iconRect = RectF()

    private var topHeightPercent = 10
    private var topWidthPercent = 50


    private var mWidth = 0
    private var mHeight = 0


    private var isPortraitMode = true


    var normalBackgroundColor: Int = Color.parseColor("#86B6F6")
    var normalLevelColor: Int = Color.parseColor("#4E94F1")
    var warningBackgroundColor: Int = Color.parseColor("#FFCF96")
    var warningLevelColor: Int = Color.parseColor("#F5AD56")
    var criticalBackgroundColor: Int = Color.parseColor("#EF5350")
    var criticalLevelColor: Int = Color.parseColor("#B71C1C")
    var chargingBackgroundColor: Int = Color.parseColor("#89EC9E")
    var chargingLevelColor: Int = Color.parseColor("#4DD86C")
    var chargingIcon: Int = R.drawable.ic_charge
    var warningIcon: Int = R.drawable.ic_warning
    var size = 50
    var mRadius = 10f
    var chargeLevel = 50
    var warningChargeLevel = 30
    var criticalChargeLevel = 10
    var batteyOrientation = BatteryOrientation.PORTRAIT
    var isCharging = false

    private var chargingBitmap: Bitmap? = null
    private var warningBitmap: Bitmap? = null

    init {
        init(attrs)
    }

    fun attachBatteryIntent(intent: Intent) {

        val deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val mIsCharging =
            deviceStatus == BatteryManager.BATTERY_STATUS_CHARGING || deviceStatus == BatteryManager.BATTERY_STATUS_FULL
        isCharging = mIsCharging


        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level / scale.toFloat()
        val df = DecimalFormat("#.##")
        chargeLevel = (df.format(batteryPct * 100)).toInt()

        invalidate()

    }

    private fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ABV)

        try {
            normalBackgroundColor =
                ta.getColor(R.styleable.ABV_abvNormalBgColor, normalBackgroundColor)
            normalLevelColor = ta.getColor(R.styleable.ABV_abvNormalLevelColor, normalLevelColor)
            warningBackgroundColor =
                ta.getColor(R.styleable.ABV_abvWarningBgColor, warningBackgroundColor)
            warningLevelColor = ta.getColor(R.styleable.ABV_abvWarningLevelColor, warningLevelColor)
            criticalBackgroundColor =
                ta.getColor(R.styleable.ABV_abvCriticalBgColor, criticalBackgroundColor)
            criticalLevelColor =
                ta.getColor(R.styleable.ABV_abvCriticalLevelColor, criticalLevelColor)
            chargingBackgroundColor =
                ta.getColor(R.styleable.ABV_abvChargingBgColor, chargingBackgroundColor)
            chargingLevelColor =
                ta.getColor(R.styleable.ABV_abvChargingLevelColor, chargingLevelColor)
            chargingIcon = ta.getResourceId(R.styleable.ABV_abvChargingIcon, chargingIcon)
            chargingBitmap = getBitmap(chargingIcon)
            warningIcon = ta.getResourceId(R.styleable.ABV_abvWarningIcon, warningIcon)
            warningBitmap = getBitmap(warningIcon)
            size = ta.getInt(R.styleable.ABV_abvSize, size)
            mRadius = ta.getInt(R.styleable.ABV_abvRadius, 0).toFloat()
            chargeLevel = ta.getInt(R.styleable.ABV_abvChargeLevel, chargeLevel)
            warningChargeLevel =
                ta.getInt(R.styleable.ABV_abvWarningChargeLevel, warningChargeLevel)
            criticalChargeLevel =
                ta.getInt(R.styleable.ABV_abvCriticalChargeLevel, criticalChargeLevel)
            batteyOrientation = when (ta.getInt(R.styleable.ABV_abvBatteryOrientation, 0)) {
                0 -> BatteryOrientation.PORTRAIT
                else -> BatteryOrientation.LANDSCAPE
            }

            isPortraitMode = batteyOrientation == BatteryOrientation.PORTRAIT
            isCharging = ta.getBoolean(R.styleable.ABV_abvIsCharging, false)


        } finally {
            ta.recycle()
        }

    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val finalWidth: Int
        val finalHeight: Int

        if (isPortraitMode) {
            finalWidth = (size * resources.displayMetrics.density).toInt() // Assuming size is in dp
            finalHeight =
                (size * 2 * resources.displayMetrics.density).toInt() // Assuming size is in dp

            setMeasuredDimension(finalWidth, finalHeight)
        } else {
            finalWidth =
                (size * 2 * resources.displayMetrics.density).toInt() // Assuming size is in dp
            finalHeight =
                (size * resources.displayMetrics.density).toInt() // Assuming size is in dp

            setMeasuredDimension(finalWidth, finalHeight)
        }



        mWidth = finalWidth
        mHeight = finalHeight

        if (isPortraitMode) {
            setTopRect(finalWidth, finalHeight)
            setBodyRect(finalWidth, finalHeight)
            setIconRect(finalWidth, finalHeight)
        } else {
            setRightRectLandscape(finalWidth, finalHeight)
            setBodyRectLandscape(finalWidth, finalHeight)
            setIconRectLandscape(finalWidth, finalHeight)
        }
    }


    private fun setIconRect(finalWidth: Int, finalHeight: Int) {
        val iconLeft = finalWidth * 25 / 100
        val iconRight = finalWidth * 75 / 100
        val iconTop = finalHeight * 25 / 100
        val iconBottom = finalHeight * 75 / 100
        iconRect.set(
            iconLeft.toFloat(),
            iconTop.toFloat(),
            iconRight.toFloat(),
            iconBottom.toFloat()
        )


    }

    private fun setIconRectLandscape(finalWidth: Int, finalHeight: Int) {

        val iconLeft = finalWidth * 35 / 100
        val iconRight = finalWidth * 65 / 100
        val iconTop = finalHeight * 5 / 100
        val iconBottom = finalHeight * 95 / 100
        iconRect.set(
            iconLeft.toFloat(),
            iconTop.toFloat(),
            iconRight.toFloat(),
            iconBottom.toFloat()
        )

    }

    private fun setBodyRect(width: Int, height: Int) {
        val bodyLeft = 0f
        val bodyRight = width.toFloat()
        val bodyTop = height * topHeightPercent / 100
        val bodyBottom = height.toFloat()
        bodyRect.set(bodyLeft, bodyTop.toFloat(), bodyRight, bodyBottom)
        bodyPercentRect.set(bodyLeft.toInt(), 0, bodyRight.toInt(), bodyBottom.toInt())

    }

    private fun setTopRect(width: Int, height: Int) {
        val topLeft = width * ((100 - topWidthPercent) / 2) / 100
        val topRight = width - topLeft
        val topBottom = height * topHeightPercent / 100
        topRect.set(topLeft, 0, topRight, topBottom)
        topPercentRect.set(topLeft, 0, topRight, topBottom)

    }

    private fun setBodyRectLandscape(width: Int, height: Int) {
        val bodyLeft = 0f
        val bodyRight = width * (100 - topHeightPercent) / 100.toFloat()
        val bodyTop = 0f
        val bodyBottom = height.toFloat()
        bodyRect.set(bodyLeft, bodyTop, bodyRight, bodyBottom)
        bodyPercentRect.set(0, 0, bodyRight.toInt(), bodyBottom.toInt())
    }

    private fun setRightRectLandscape(width: Int, height: Int) {
        val topRight = width.toFloat()
        val topBottom = height * ((100 - topWidthPercent) / 2) / 100
        topRect.set(0, topBottom, topRight.toInt(), (height - topBottom))
        topPercentRect.set(0, topBottom, topRight.toInt(), (height - topBottom))
    }

    override fun onDraw(canvas: Canvas) {
        if (isPortraitMode) {
            drawTop(canvas)
            drawBody(canvas)
        } else {
            drawBodyLandscape(canvas)
            drawRightLandscape(canvas)
        }

        if (chargeLevel > 0) {
            if (chargeLevel <= 90) {
                if (isPortraitMode) {
                    drawBodyPercent(canvas, chargeLevel)
                } else {
                    drawBodyPercentLandscape(canvas, chargeLevel)
                }

            } else {
                if (isPortraitMode) {
                    drawBodyPercent(canvas, 90)
                    drawTopPercent(canvas, chargeLevel)
                } else {
                    drawBodyPercentLandscape(canvas, 90)
                    drawRightPercentLandscape(canvas, chargeLevel)
                }
            }
        }

        if (isCharging) {
            drawCharging(canvas)
        }
        if (!isCharging && chargeLevel < warningChargeLevel) {
            drawWarning(canvas)
        }
    }

    private fun drawCharging(canvas: Canvas) {

        if (isPortraitMode){
            chargingBitmap?.let {
                canvas.drawBitmap(it, null, iconRect, null)
            }
        } else {
            chargingBitmap?.let { bitmap ->
                canvas.save()
                canvas.rotate(270f, iconRect.centerX(), iconRect.centerY())
                canvas.drawBitmap(bitmap, null, iconRect, null)
                canvas.restore()
            }
        }

    }

    private fun drawWarning(canvas: Canvas) {

        if (isPortraitMode){
            warningBitmap?.let {
                canvas.drawBitmap(it, null, iconRect, null)
            }
        } else {
            warningBitmap?.let { bitmap ->
                canvas.save()
                canvas.rotate(90f, iconRect.centerX(), iconRect.centerY())
                canvas.drawBitmap(bitmap, null, iconRect, null)
                canvas.restore()
            }
        }


    }

    private fun drawBody(canvas: Canvas) {

        if (chargeLevel <= criticalChargeLevel) {
            bodyPaint.colorFilter =
                PorterDuffColorFilter(criticalBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else if (chargeLevel < warningChargeLevel) {
            bodyPaint.colorFilter =
                PorterDuffColorFilter(warningBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else {
            bodyPaint.colorFilter =
                PorterDuffColorFilter(normalBackgroundColor, PorterDuff.Mode.SRC_IN)
        }

        if (isCharging){
            bodyPaint.colorFilter =
                PorterDuffColorFilter(chargingBackgroundColor, PorterDuff.Mode.SRC_IN)
        }

        canvas.drawRoundRect(bodyRect, mRadius, mRadius, bodyPaint)
    }

    private fun drawTop(canvas: Canvas) {

        if (chargeLevel <= criticalChargeLevel) {
            topPaint.colorFilter =
                PorterDuffColorFilter(criticalBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else if (chargeLevel < warningChargeLevel) {
            topPaint.colorFilter =
                PorterDuffColorFilter(warningBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else {
            topPaint.colorFilter =
                PorterDuffColorFilter(normalBackgroundColor, PorterDuff.Mode.SRC_IN)
        }

        if (isCharging){
            topPaint.colorFilter =
                PorterDuffColorFilter(chargingBackgroundColor, PorterDuff.Mode.SRC_IN)
        }

        topPaint.bounds = topRect
        topPaint.setCornerRadii(floatArrayOf(mRadius, mRadius, mRadius, mRadius, 0f, 0f, 0f, 0f))
        topPaint.draw(canvas)
    }

    private fun drawBodyLandscape(canvas: Canvas) {
        if (chargeLevel <= criticalChargeLevel) {
            bodyPaint.colorFilter =
                PorterDuffColorFilter(criticalBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else if (chargeLevel < warningChargeLevel) {
            bodyPaint.colorFilter =
                PorterDuffColorFilter(warningBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else {
            bodyPaint.colorFilter =
                PorterDuffColorFilter(normalBackgroundColor, PorterDuff.Mode.SRC_IN)
        }

        if (isCharging){
            bodyPaint.colorFilter =
                PorterDuffColorFilter(chargingBackgroundColor, PorterDuff.Mode.SRC_IN)
        }
        canvas.drawRoundRect(bodyRect, mRadius, mRadius, bodyPaint)
    }

    private fun drawRightLandscape(canvas: Canvas) {
        if (chargeLevel <= criticalChargeLevel) {
            topPaint.colorFilter =
                PorterDuffColorFilter(criticalBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else if (chargeLevel < warningChargeLevel) {
            topPaint.colorFilter =
                PorterDuffColorFilter(warningBackgroundColor, PorterDuff.Mode.SRC_IN)
        } else {
            topPaint.colorFilter =
                PorterDuffColorFilter(normalBackgroundColor, PorterDuff.Mode.SRC_IN)
        }

        if (isCharging){
            topPaint.colorFilter =
                PorterDuffColorFilter(chargingBackgroundColor, PorterDuff.Mode.SRC_IN)
        }

        topRect.left = (bodyRect.right).toInt()
        topPaint.bounds = topRect
        topPaint.setCornerRadii(floatArrayOf(0f, 0f, mRadius, mRadius, mRadius, mRadius, 0f, 0f))
        topPaint.draw(canvas)
    }

    private fun drawBodyPercent(canvas: Canvas, percent: Int) {

        if (percent <= 10) {
            bodyPaintPercent.colorFilter =
                PorterDuffColorFilter(criticalLevelColor, PorterDuff.Mode.SRC_IN)
        } else if (percent < 30) {
            bodyPaintPercent.colorFilter =
                PorterDuffColorFilter(warningLevelColor, PorterDuff.Mode.SRC_IN)
        } else {
            bodyPaintPercent.colorFilter =
                PorterDuffColorFilter(normalLevelColor, PorterDuff.Mode.SRC_IN)
        }

        if (isCharging){
            bodyPaintPercent.colorFilter =
                PorterDuffColorFilter(chargingLevelColor, PorterDuff.Mode.SRC_IN)
        }


        bodyPercentRect.top = (bodyRect.bottom - (mHeight * percent / 100)).toInt()
        bodyPaintPercent.bounds = bodyPercentRect

        if (percent >= 85) {
            bodyPaintPercent.setCornerRadii(
                floatArrayOf(
                    mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius
                )
            )
        } else {
            bodyPaintPercent.setCornerRadii(
                floatArrayOf(
                    0f, 0f, 0f, 0f, mRadius, mRadius, mRadius, mRadius
                )
            )
        }

        bodyPaintPercent.draw(canvas)

    }

    private fun drawBodyPercentLandscape(canvas: Canvas, percent: Int) {
        if (percent <= 10) {
            bodyPaintPercent.colorFilter =
                PorterDuffColorFilter(criticalLevelColor, PorterDuff.Mode.SRC_IN)
        } else if (percent < 30) {
            bodyPaintPercent.colorFilter =
                PorterDuffColorFilter(warningLevelColor, PorterDuff.Mode.SRC_IN)
        } else {
            bodyPaintPercent.colorFilter =
                PorterDuffColorFilter(normalLevelColor, PorterDuff.Mode.SRC_IN)
        }

        if (isCharging){
            bodyPaintPercent.colorFilter =
                PorterDuffColorFilter(chargingLevelColor, PorterDuff.Mode.SRC_IN)
        }


        bodyPercentRect.left = 0
        bodyPercentRect.right = (bodyRect.left + (mWidth * percent / 100)).toInt()

        bodyPaintPercent.bounds = bodyPercentRect

        if (percent >= 85) {
            bodyPaintPercent.setCornerRadii(
                floatArrayOf(
                    mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius, mRadius
                )
            )
        } else {
            bodyPaintPercent.setCornerRadii(
                floatArrayOf(
                    mRadius, mRadius, 0f, 0f, 0f, 0f, mRadius, mRadius

                )
            )
        }

        bodyPaintPercent.draw(canvas)
    }

    private fun drawTopPercent(canvas: Canvas, percent: Int) {
        val mPercent = percent - 90

        topPaintPercent.colorFilter =
            PorterDuffColorFilter(normalLevelColor, PorterDuff.Mode.SRC_IN)

        if (isCharging){
            topPaintPercent.colorFilter =
                PorterDuffColorFilter(chargingLevelColor, PorterDuff.Mode.SRC_IN)
        }

        topPercentRect.top = ((topRect.bottom - topRect.top) * (10 - mPercent) / 10)

        topPaintPercent.bounds = topPercentRect

        topPaintPercent.setCornerRadii(
            floatArrayOf(
                mRadius,
                mRadius,
                mRadius,
                mRadius,
                0f,
                0f,
                0f,
                0f
            )
        )
        topPaintPercent.draw(canvas)
    }

    private fun drawRightPercentLandscape(canvas: Canvas, percent: Int) {
        val mPercent = percent - 90

        topPaintPercent.colorFilter =
            PorterDuffColorFilter(normalLevelColor, PorterDuff.Mode.SRC_IN)

        if (isCharging){
            topPaintPercent.colorFilter =
                PorterDuffColorFilter(chargingLevelColor, PorterDuff.Mode.SRC_IN)
        }

        topPercentRect.left = (bodyRect.right).toInt()
        topPercentRect.right =
            (bodyRect.right).toInt() + ((topPercentRect.right - topRect.left) * (10 - mPercent) / 10)


        topPaintPercent.bounds = topPercentRect
        topPaintPercent.setCornerRadii(
            floatArrayOf(
                0f,
                0f,
                mRadius,
                mRadius,
                mRadius,
                mRadius,
                0f,
                0f
            )
        )
        topPaintPercent.draw(canvas)
    }

    private fun getBitmap(
        drawableId: Int,
        desireWidth: Int? = null,
        desireHeight: Int? = null
    ): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(
            desireWidth ?: drawable.intrinsicWidth,
            desireHeight ?: drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    enum class BatteryOrientation {
        PORTRAIT, LANDSCAPE
    }

}
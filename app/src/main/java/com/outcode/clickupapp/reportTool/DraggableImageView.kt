package com.outcode.clickupapp.reportTool

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.outcode.clickupapp.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("ClickableViewAccessibility")
class ReportLayout : FrameLayout {

    private var dX = 0f
    private var dY = 0f
    var bitmap: Bitmap? = null
    var isMoving = false


    fun captureScreenshot(context: Context): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val imageFileName = "screenshot_$timeStamp.jpg"
        val imageFile = File(storageDir, imageFileName)

        try {
            val rootView = (context as Activity).window.decorView
            rootView.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(rootView.drawingCache)
            rootView.isDrawingCacheEnabled = false

            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            return imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                this@ReportLayout.isVisible = false
                val imagePath = captureScreenshot(context) ?: ""
                val intent = Intent(context, InputActivity::class.java)
                intent.putExtra("imagePath", imagePath)

                context.startActivity(intent)
                this@ReportLayout.isVisible = true

                return true
            }
        })

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        isClickable = true
        isFocusable = true
        elevation = 300f

        setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    isMoving = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY
                    if (!isMoving) {
                        isMoving = true
                    }
                    v.x = newX
                    v.y = newY
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!isMoving) {
                        // Handle click event here
                    } else {
                        val newX = v.x
                        val newY = v.y
                        val nearestEdge = calculateNearestEdge(newX, newY, v.width, v.height)

                        when (nearestEdge) {
                            NearestEdge.LEFT -> v.x = 0f
                            NearestEdge.RIGHT -> v.x =
                                (v.context.resources.displayMetrics.widthPixels - v.width).toFloat()
                            NearestEdge.TOP -> v.y = 0f
                            NearestEdge.BOTTOM -> v.y =
                                (v.context.resources.displayMetrics.heightPixels - v.height).toFloat()
                        }
                    }
                }

            }
            true
        }

        val shapeableImageView = ShapeableImageView(context)

        val layoutParams = ViewGroup.LayoutParams(
            180,
            180
        )
        shapeableImageView.layoutParams = layoutParams
        shapeableImageView.scaleType = ImageView.ScaleType.FIT_XY
        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCorners(CornerFamily.ROUNDED, 100f)
            .build()

        shapeableImageView.shapeAppearanceModel = shapeAppearanceModel
        shapeableImageView.setImageResource(R.mipmap.ic_launcher)

        this.addView(shapeableImageView)

    }

    enum class NearestEdge {
        LEFT, RIGHT, TOP, BOTTOM
    }

    fun calculateNearestEdge(x: Float, y: Float, width: Int, height: Int): NearestEdge {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        val centerX = x + width / 2
        val centerY = y + height / 2

        val distances = listOf(
            centerX,
            screenWidth - centerX,
            centerY,
            screenHeight - centerY
        )

        val minDistance = distances.minOrNull() ?: 0f

        return when (minDistance) {
            distances[0] -> NearestEdge.LEFT
            distances[1] -> NearestEdge.RIGHT
            // distances[2] -> NearestEdge.TOP
            //distances[3] -> NearestEdge.BOTTOM
            else -> NearestEdge.LEFT
        }
    }
}

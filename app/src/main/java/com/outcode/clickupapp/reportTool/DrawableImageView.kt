package com.outcode.clickupapp.reportTool

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class DrawableImageView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    View(context, attrs) {

    var BRUSH_SIZE = 10
    val DEFAULT_COLOR = Color.BLACK
    val DEFAULT_BG_COLOR = Color.WHITE
    private val TOUCH_TOLERANCE = 4f
    private val backgroundColor: Int = DEFAULT_BG_COLOR
    private var mX = 0f
    private var mY = 0f
    private var mPath = Path()
    private var mPaint = Paint()
    private var paths = ArrayList<FingerPath>()
    private var currentColor = 0
    private var strokeWidth = 0
    private var emboss = false
    private var blur = false
    private val mEmboss: MaskFilter
    private val mBlur: MaskFilter
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    private var backgroundImage: Bitmap? = null
    private val backgroundPaint = Paint()

    init {
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = DEFAULT_COLOR
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.xfermode = null
        mPaint.alpha = 0xff

        mEmboss = EmbossMaskFilter(floatArrayOf(1f, 1f, 1f), 0.4f, 6F, 3.5f)
        mBlur = BlurMaskFilter(5F, BlurMaskFilter.Blur.NORMAL)
    }

    fun initSignature(metrics: Point, bitmap: Bitmap) {
        val height = metrics.y
        val width = metrics.x
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
        currentColor = DEFAULT_COLOR
        strokeWidth = BRUSH_SIZE
        backgroundImage = bitmap
        mCanvas?.drawBitmap(bitmap, 0f, 0f, backgroundPaint)
    }

    fun normal() {
        emboss = false
        blur = false
    }


    fun getSignature(): File? {
        return getBitMapPath(context, mBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        for (fp in paths) {
            mPaint.color = fp.color
            mPaint.strokeWidth = fp.strokeWidth.toFloat()
            mPaint.maskFilter = null
            if (fp.emboss) mPaint.maskFilter = mEmboss else if (fp.blur) mPaint.maskFilter = mBlur
            mCanvas!!.drawPath(fp.path, mPaint)
        }
        canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)
        canvas.restore()
    }

    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        val fp = FingerPath(currentColor, emboss, blur, strokeWidth, mPath!!)
        paths.add(fp)
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        mPath!!.lineTo(mX, mY)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    companion object {
        var BRUSH_SIZE = 20
        const val DEFAULT_COLOR = Color.RED
        const val DEFAULT_BG_COLOR = Color.WHITE
        private const val TOUCH_TOLERANCE = 4f
    }
}


class FingerPath(
    var color: Int,
    var emboss: Boolean,
    var blur: Boolean,
    var strokeWidth: Int,
    var path: Path
)

fun getBitMapPath(context: Context, bitmap: Bitmap): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    val imageFileName = "screenshot_$timeStamp.jpg"
    val imageFile = File(storageDir, imageFileName)

    imageFile.createNewFile()
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
    val bitmapdata = bos.toByteArray()

    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(imageFile)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    try {
        fos!!.write(bitmapdata)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return imageFile
}


package com.outcode.clickupapp.reportTool

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import com.outcode.clickupapp.databinding.ActivityDrawBinding

class DrawActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDrawBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePath = intent.getStringExtra("imagePath")
        val bitmap = BitmapFactory.decodeFile(imagePath)
        println("DrawActivity:$imagePath")
        binding.drawableImageView.initSignature(currentWindowMetricsPointCompat(),bitmap)
        binding.done.setOnClickListener {
         val returnImage=  binding.drawableImageView.getSignature().toString()
            println("returnImage:$returnImage")
            val returnIntent = Intent()
            returnIntent.putExtra("returnImage", returnImage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    private fun currentWindowMetricsPointCompat(): Point {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = windowManager.currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            windowInsets.displayCutout?.run {
                insets = Insets.max(insets, Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom))
            }
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(windowManager.currentWindowMetrics.bounds.width() - insetsWidth, windowManager.currentWindowMetrics.bounds.height() - insetsHeight)
        }else{
            Point().apply {
                windowManager.defaultDisplay.getSize(this)
            }
        }
    }
}
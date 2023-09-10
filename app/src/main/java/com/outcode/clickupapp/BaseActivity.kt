package com.outcode.clickupapp

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.outcode.clickupapp.reportTool.ReportLayout

open class BaseActivity: AppCompatActivity() {
    private  val reportLayout: ReportLayout by lazy { ReportLayout(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootLayout: ViewGroup = findViewById(android.R.id.content)
        rootLayout.addView(reportLayout)
    }
}
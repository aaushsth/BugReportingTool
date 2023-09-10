package com.outcode.clickupapp

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.outcode.clickupapp.reportTool.ClickUpTaskCreator
import com.outcode.clickupapp.reportTool.ReportLayout
import java.io.File
import java.io.FileOutputStream

class MainActivity : BaseActivity() {
    private  val reportLayout: ReportLayout by lazy { ReportLayout(this) }

    private var selectedUri: Uri? = null
    private lateinit var imageView: ImageView
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val mediaType = contentResolver.getType(uri)
                if (mediaType?.startsWith("image") == true) {
                    selectedUri = getFilePathFromUri(this, uri)?.toUri()
                    imageView.setImageURI(selectedUri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById<ImageView>(R.id.image)
        val rootLayout: ViewGroup = findViewById(android.R.id.content)
        rootLayout.addView(reportLayout)
        findViewById<Button>(R.id.btnPickImage).setOnClickListener {
            takeImageAndVideo()
        }


        findViewById<Button>(R.id.btnUpload).setOnClickListener {
            ClickUpTaskCreator.createTask(
                this,
                taskName = "this is test title from android",
                taskDescription = "this is description",
                tags = listOf("Android,iOS"),
                imageFile = selectedUri
            )
        }


    }


    private fun takeImageAndVideo() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }
}


fun getFilePathFromUri(context: Context, uri: Uri): String? {
    var filePath: String? = null

    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        val contentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    val fileName = it.getString(displayNameIndex)
                    val file = DocumentFile.fromSingleUri(context, uri)

                    if (file != null && file.exists()) {
                        val tempFile = File(context.cacheDir, fileName)

                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            FileOutputStream(tempFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        filePath = tempFile.absolutePath
                    }
                }
            }
        }
    }

    return filePath
}

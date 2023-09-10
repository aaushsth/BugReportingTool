package com.outcode.clickupapp.reportTool

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.outcode.clickupapp.R
import com.outcode.clickupapp.databinding.ActivityInputBinding

class InputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBinding
    var imagePath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imagePath = intent.getStringExtra("imagePath")
        val bitmap = BitmapFactory.decodeFile(imagePath)
        if (bitmap != null) {
            binding.bugScreenshotImageView.setImageBitmap(bitmap)
        }
        binding.bugDescriptionEditText.setText("hello honey")

        findViewById<Button>(R.id.reportButton).setOnClickListener {
            val listOfTag = binding.tagsEditText.text.toString().split(",").toList()

            ClickUpTaskCreator.createTask(
                this,
                taskName = binding.bugTitleEditText.text.toString(),
                taskDescription = binding.bugDescriptionEditText.text.toString(),
                tags = listOfTag,
                imageFile = Uri.parse(imagePath)
            )
            finish()
        }

        binding.editImage.setOnClickListener {
            val intent = Intent(this, DrawActivity::class.java)
            intent.putExtra("imagePath", imagePath)
            resultLauncher.launch(intent)
        }
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                imagePath = data?.getStringExtra("returnImage")
                val bitmap = BitmapFactory.decodeFile(imagePath)
                binding.bugScreenshotImageView.setImageBitmap(bitmap)
            }
        }
}
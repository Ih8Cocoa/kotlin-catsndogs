package com.meow.catsndogs

import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.meow.catsndogs.tflite.Classifier

class ImageClassifierActivity : AppCompatActivity() {
    private val inputSize = 224
    private val modelPath = "converted_model.tflite"
    private val labelPath = "label.txt"
    private lateinit var classifier: Classifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_classifier)
        initClassifier()
        initViews()
    }

    private fun initClassifier() {
        classifier = Classifier(assets, modelPath, labelPath, inputSize)
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.iv_1).setOnClickListener { onImageClick(it) }
        findViewById<ImageView>(R.id.iv_2).setOnClickListener { onImageClick(it) }
        findViewById<ImageView>(R.id.iv_3).setOnClickListener { onImageClick(it) }
        findViewById<ImageView>(R.id.iv_4).setOnClickListener { onImageClick(it) }
        findViewById<ImageView>(R.id.iv_5).setOnClickListener { onImageClick(it) }
        findViewById<ImageView>(R.id.iv_6).setOnClickListener { onImageClick(it) }
    }

    private fun onImageClick(view: View?) {
        if (view == null) return
        val imgView = view as ImageView
        val drawable = imgView.drawable as BitmapDrawable
        val bitmap = drawable.bitmap

        val result = classifier.recogniseImage(bitmap)

        runOnUiThread {
            Toast.makeText(this, result[0]?.title, Toast.LENGTH_SHORT).show()
        }
    }
}
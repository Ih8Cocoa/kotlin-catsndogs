package com.meow.catsndogs.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.meow.catsndogs.utils.loadLabels
import com.meow.catsndogs.utils.loadModelFile
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.min

class Classifier(
    assetManager: AssetManager,
    modelPath: String,
    labelPath: String,
    private val inputSize: Int
) {
    private val interpreter: Interpreter
    private val labelList: List<String>

    init {
        val options = Interpreter.Options()
        options.setNumThreads(5).setUseNNAPI(true)
        val modelFile = assetManager.loadModelFile(modelPath)
        interpreter = Interpreter(modelFile, options)
        labelList = assetManager.loadLabels(labelPath)
    }

    fun recogniseImage(bitmap: Bitmap): List<Recognition?> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val byteBuffer = scaledBitmap.asByteBuffer()
        val result = arrayOf(FloatArray(labelList.size))
        interpreter.run(byteBuffer, result)
        return result.getSortedResult()
    }

    data class Recognition(
        var id: String = "",
        var title: String = "",
        var confidence: Float = 0f
    ) {
        override fun toString() = "Title: $title, confidence: $confidence"
    }

    private fun Bitmap.asByteBuffer(): ByteBuffer {
        val bufferSize = 4 * inputSize * inputSize * pixelSize
        val byteBuffer = ByteBuffer.allocateDirect(bufferSize)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)

        getPixels(intValues, 0, width, 0, 0, width, height)
        var pixels = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val input = intValues[pixels++]
                byteBuffer.putFloat((((input.shr(16) and 0xFF)) - imageMean) / imageStd)
                byteBuffer.putFloat((((input.shr(8) and 0xFF)) - imageMean) / imageStd)
                byteBuffer.putFloat((((input and 0xFF)) - imageMean) / imageStd)
            }
        }

        return byteBuffer
    }

    private fun Array<FloatArray>.getSortedResult(): List<Recognition?> {
        Log.d("Classifier", "List Size:(%d, %d, %d)".format(size, this[0].size, labelList.size))

        val queue = PriorityQueue(
            maxResult,
            Comparator<Recognition> { (_, _, confidence1), (_, _, confidence2) ->
                confidence1.compareTo(confidence2) * -1
            })

        for (i in labelList.indices) {
            val confidence = this[0][i]
            if (confidence >= threshold) {
                val recognition = Recognition(
                    "$i", if (labelList.size > i) labelList[i] else "Unknown",
                    confidence
                )
                queue.add(recognition)
            }
        }

        Log.d("Classifier", "pqsize:(%d)".format(queue.size))
        val recognitionSize = min(queue.size, maxResult)
        val result = mutableListOf<Recognition?>()
        for (i in 0 until recognitionSize) {
            val item = queue.poll()
            result.add(item)
        }

        return result
    }

    companion object {
        private const val pixelSize = 3
        private const val imageMean = 0
        private const val imageStd = 255f
        private const val maxResult = 3
        private const val threshold = 0.4f
    }
}
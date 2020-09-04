package com.meow.catsndogs.utils

import android.content.res.AssetManager
import android.graphics.Bitmap
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

fun AssetManager.loadModelFile(modelPath: String): MappedByteBuffer {
    val fd = openFd(modelPath)
    val fileChannel = FileInputStream(fd.fileDescriptor).channel
    val startOffset = fd.startOffset
    val length = fd.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
}

fun AssetManager.loadLabels(labelPath: String): List<String> {
    return open(labelPath).bufferedReader().useLines { it.toList() }
}
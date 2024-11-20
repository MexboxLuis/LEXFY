package com.example.yoloapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.io.FileOutputStream


fun bindCameraUseCases(
    lifecycleOwner: LifecycleOwner,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    isBackCamera: Boolean
) {
    val cameraProvider = cameraProviderFuture.get()

    val preview = Preview.Builder()
        .build().apply {
            surfaceProvider = previewView.surfaceProvider
        }

    val cameraSelector = if (isBackCamera) {
        CameraSelector.DEFAULT_BACK_CAMERA
    } else {
        CameraSelector.DEFAULT_FRONT_CAMERA
    }

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun rotateImageFileIfRequired(context: Context, photoFile: File): File {
    val ei = context.contentResolver.openInputStream(Uri.fromFile(photoFile))?.let { ExifInterface(it) }

    val orientation = ei?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImageFile(photoFile, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImageFile(photoFile, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImageFile(photoFile, 270)
        else -> photoFile
    }
}

fun rotateImageFile(photoFile: File, degree: Int): File {
    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())

    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    FileOutputStream(photoFile).use { out ->
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }

    return photoFile
}
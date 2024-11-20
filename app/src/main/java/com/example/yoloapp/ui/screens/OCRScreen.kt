package com.example.yoloapp.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.yoloapp.utils.RequestCameraPermission
import com.example.yoloapp.utils.bindCameraUseCases
import com.example.yoloapp.utils.rotateImageFileIfRequired
import com.example.yoloapp.utils.sendImageForOCR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun OCRScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var isBackCamera by remember { mutableStateOf(true) }
    var flashEnabled by remember { mutableStateOf(false) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(false) }

    val toggleCamera = {
        isBackCamera = !isBackCamera
        previewView?.let { view ->
            bindCameraUseCases(
                lifecycleOwner,
                cameraProviderFuture,
                view,
                imageCapture,
                isBackCamera
            )
        }
    }


    fun takePhoto(navController: NavHostController) {
        val photoFile = File(context.externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.flashMode =
            if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        isLoading = true

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("OCRScreen", "Image saved: ${photoFile.absolutePath}")

                    CoroutineScope(Dispatchers.IO).launch {

                        val rotatedPhotoFile = rotateImageFileIfRequired(context, photoFile)
                        val extractedText = sendImageForOCR(rotatedPhotoFile.absolutePath)

                        withContext(Dispatchers.Main) {
                            isLoading = false
                            navController.navigate(
                                "photoPreviewScreen/${Uri.encode(photoFile.absolutePath)}/${
                                    Uri.encode(
                                        extractedText
                                    )
                                }"
                            )
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    isLoading = false
                    Log.e("OCRScreen", "Image capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    fun handleSelectedImage(imageUri: Uri, navController: NavHostController) {
        val photoFile =
            File(context.externalCacheDir, "photo_from_gallery_${System.currentTimeMillis()}.jpg")
        isLoading = true
        try {

            val inputStream = context.contentResolver.openInputStream(imageUri)
            val outputStream = FileOutputStream(photoFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val rotatedPhotoFile = rotateImageFileIfRequired(context, photoFile)

            CoroutineScope(Dispatchers.IO).launch {
                val extractedText = sendImageForOCR(rotatedPhotoFile.absolutePath)

                withContext(Dispatchers.Main) {
                    isLoading = false
                    navController.navigate(
                        "photoPreviewScreen/${Uri.encode(rotatedPhotoFile.absolutePath)}/${
                            Uri.encode(
                                extractedText
                            )
                        }"
                    )
                }
            }
        } catch (e: IOException) {
            isLoading = false
            Log.e("OCRScreen", "Error processing image: ${e.message}")
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri, navController)
            }
        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        launcher.launch(intent)
    }


    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    previewView = this
                    bindCameraUseCases(
                        lifecycleOwner,
                        cameraProviderFuture,
                        this,
                        imageCapture,
                        isBackCamera
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                )
                IconButton(
                    onClick = { navController.navigate("homeScreen") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                    )

                }
            }
        }

        if (isBackCamera)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    )
                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = null,
                            tint = if (flashEnabled) Color.Yellow else Color.White
                        )
                    }
                }
            }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp, start = 50.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            IconButton(onClick = { toggleCamera() }, modifier = Modifier.size(80.dp)) {
                Icon(
                    imageVector = Icons.Default.Cached,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .clickable { takePhoto(navController) }
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp, end = 50.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = { openGallery() },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
        }


        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }

    RequestCameraPermission {
        cameraProviderFuture.get()
    }
}














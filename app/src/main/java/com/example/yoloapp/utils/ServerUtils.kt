package com.example.yoloapp.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

const val BASE_URL = "http://YOUR_IP_ADDRESS_HOST:5000"
val client = OkHttpClient()

suspend fun sendImageForOCR(imagePath: String): String = withContext(Dispatchers.IO) {
    val url = "$BASE_URL/ocr"
    val file = File(imagePath)

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("image", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        .build()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            json.optString("text", "No text found")
        } else {
            "OCR Failed: ${response.code}"
        }
    } catch (e: Exception) {
        "Error: ${e.message ?: "Unknown error"}"
    }
}

suspend fun getGeneratedImageUrl(prompt: String, fireStoreManager: FireStoreManager): String = withContext(Dispatchers.IO) {
    val url = "$BASE_URL/generate_image"
    val jsonObject = JSONObject().apply {
        put("prompt", prompt)
    }
    val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    return@withContext try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val json = responseBody?.let { JSONObject(it) }
            val imageUrl = json?.optString("image_url")

            if (!imageUrl.isNullOrEmpty() && imageUrl.startsWith("http")) {
                val uploadResult = fireStoreManager.uploadGeneratedImageToStorage(imageUrl)
                if (uploadResult.isSuccess) {
                    uploadResult.getOrNull() ?: "Error: Unable to retrieve uploaded image URL"
                } else {
                    "Error: ${uploadResult.exceptionOrNull()?.localizedMessage}"
                }
            } else {
                "Error: Image generation failed or returned an invalid URL"
            }
        } else {
            "Error: Server responded with code ${response.code}"
        }
    } catch (e: Exception) {
        "Error: ${e.cause ?: "Unknown error"}"
    }
}

package com.example.yoloapp.utils

import com.example.yoloapp.data.ChatData
import com.example.yoloapp.data.DocumentData
import com.example.yoloapp.ui.model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.Date
import java.util.UUID

class FireStoreManager(
    private val authManager: AuthManager,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun saveTextAndImage(imagePath: String?, text: String): Result<Boolean> {
        val userResult = authManager.getCurrentUser()

        if (userResult.isFailure) {
            return Result.failure(Exception("User not authenticated"))
        }

        val user = userResult.getOrNull()
        val email = user?.email ?: return Result.failure(Exception("Failed to retrieve user email"))

        return try {
            val storageRef = storage.reference.child("$email/images/${UUID.randomUUID()}.jpg")
            val imageFile = File(imagePath ?: "")
            val uri = storageRef.putFile(imageFile.toUri()).await().storage.downloadUrl.await()

            val data = mapOf(
                "email" to email,
                "text" to text,
                "imageUrl" to uri.toString(),
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("saved_texts").add(data).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save data: ${e.localizedMessage}"))
        }
    }

    private fun File.toUri(): android.net.Uri {
        return android.net.Uri.fromFile(this)
    }

    suspend fun getDocumentsByEmail(email: String): List<DocumentData> {
        return try {
            val querySnapshot = firestore.collection("saved_texts")
                .whereEqualTo("email", email)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                val date = document.getDate("timestamp") ?: Date()
                val imageUrl = document.getString("imageUrl") ?: ""
                val text = document.getString("text") ?: ""
                val documentId = document.id

                DocumentData(
                    documentId = documentId,
                    date = date,
                    imageUrl = imageUrl,
                    text = text
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun updateDocumentText(documentId: String, newText: String): Result<Boolean> {
        return try {
            firestore.collection("saved_texts")
                .document(documentId)
                .update("text", newText)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update document: ${e.localizedMessage}"))
        }
    }

    suspend fun deleteDocument(documentId: String, imageUrl: String): Result<Boolean> {
        return try {
            firestore.collection("saved_texts")
                .document(documentId)
                .delete()
                .await()

            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(true)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete document and image: ${e.localizedMessage}"))
        }
    }


    suspend fun saveChat(chatData: ChatData): Result<String> {
        return try {
            if (chatData.title.isEmpty()) {
                return Result.failure(Exception("The chat title cannot be empty"))
            }

            val newChat = firestore.collection("saved_chats").add(chatData).await()
            Result.success(newChat.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChatTitle(chatId: String, newTitle: String): Result<Unit> {
        return try {

            firestore.collection("saved_chats").document(chatId).update(
                "title", newTitle,
                "lastModifiedAt", Timestamp.now()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            firestore.collection("saved_chats").document(chatId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    fun getChats(email: String, callback: (List<Pair<String, ChatData>>) -> Unit) {
        firestore.collection("saved_chats")
            .whereEqualTo("email", email)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    callback(emptyList())
                    return@addSnapshotListener
                }

                val chatList = querySnapshot?.documents?.mapNotNull { document ->
                    val chatData = document.toObject(ChatData::class.java)
                    chatData?.let { document.id to it }
                } ?: emptyList()

                callback(chatList)
            }
    }


    suspend fun getChatById(chatId: String): ChatData? {
        val chatDocument = firestore.collection("saved_chats").document(chatId).get().await()
        return if (chatDocument.exists()) {
            val data = chatDocument.data
            val messagesList = (data?.get("messages") as? List<*>)
                ?.mapNotNull { rawMessage ->

                    if (rawMessage is Map<*, *>) {
                        ChatMessage(
                            id = (rawMessage["id"] as? Number)?.toInt() ?: 0,
                            text = rawMessage["text"] as? String ?: ""
                        )
                    } else null
                } ?: emptyList()

            ChatData(
                email = data?.get("email") as? String ?: "",
                title = data?.get("title") as? String ?: "",
                createdAt = data?.get("createdAt") as? Timestamp ?: Timestamp.now(),
                lastModifiedAt = data?.get("lastModifiedAt") as? Timestamp ?: Timestamp.now(),
                messages = messagesList
            )
        } else {
            null
        }
    }


    suspend fun updateChatMessages(chatId: String, messages: List<ChatMessage>) {
        firestore.collection("saved_chats").document(chatId).update(
            "messages", messages.map { message ->
                mapOf(
                    "id" to message.id,
                    "text" to message.text
                )
            },
            "lastModifiedAt", Timestamp.now()
        ).await()
    }

    suspend fun uploadGeneratedImageToStorage(imageUrl: String): Result<String> {
        val userResult = authManager.getCurrentUser()

        if (userResult.isFailure) {
            return Result.failure(Exception("User not authenticated"))
        }

        val user = userResult.getOrNull()
        val email = user?.email ?: return Result.failure(Exception("Failed to retrieve user email"))

        return try {

            if (!imageUrl.startsWith("http")) {
                return Result.failure(Exception("Invalid image URL"))
            }

            // Download the image file from the URL
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("temp_image", ".jpg")
            }
            tempFile.outputStream().use { outputStream ->
                URL(imageUrl).openStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Upload the image to Firebase Storage
            val storageRef = storage.reference.child("$email/chat_images/${UUID.randomUUID()}.jpg")
            val uploadTask = storageRef.putFile(tempFile.toUri()).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            tempFile.delete()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload image: ${e.localizedMessage}"))
        }
    }



}




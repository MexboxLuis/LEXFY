package com.example.yoloapp.utils

import com.example.yoloapp.data.ChatData
import com.example.yoloapp.data.DocumentData
import com.example.yoloapp.ui.model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
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


    suspend fun getChats(email: String): List<Pair<String, ChatData>> {
        val chatList = mutableListOf<Pair<String, ChatData>>()
        return try {
            val querySnapshot = firestore.collection("saved_chats")
                .whereEqualTo("email", email)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                try {
                    val chatData = document.toObject(ChatData::class.java)
                    if (chatData != null) {
                        chatList.add(document.id to chatData)
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            chatList
        } catch (e: Exception) {
            emptyList()
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


}




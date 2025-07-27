package com.qppd.pesapp.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseImageUploader {
    suspend fun uploadImage(context: Context, uri: Uri, folder: String = "announcements"): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = "${folder}/${UUID.randomUUID()}"
            val imageRef = storageRef.child(fileName)
            val uploadTask = imageRef.putFile(uri)
            uploadTask.await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 
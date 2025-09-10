package com.qppd.pesapp.utils

import android.content.Context
import android.net.Uri
import com.qppd.pesapp.data.remote.SupabaseManager
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object SupabaseImageUploader {
    
    suspend fun uploadImage(
        context: Context, 
        imageUri: Uri, 
        folder: String = "images"
    ): String? = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.withClientSuspend(
                fallback = { null }
            ) { client ->
                val fileName = "${UUID.randomUUID()}.jpg"
                val filePath = "$folder/$fileName"
                
                // Read image bytes
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (imageBytes != null) {
                    // Upload to Supabase Storage
                    client.storage["images"].upload(filePath, imageBytes)
                    
                    // Return public URL
                    client.storage["images"].publicUrl(filePath)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun deleteImage(imagePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.withClientSuspend(
                fallback = { false }
            ) { client ->
                client.storage["images"].delete(imagePath)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// Deprecated Firebase implementation - kept for backward compatibility
@Deprecated("Use SupabaseImageUploader instead", ReplaceWith("SupabaseImageUploader"))
object FirebaseImageUploader {
    suspend fun uploadImage(context: Context, imageUri: Uri, folder: String = "images"): String? {
        return SupabaseImageUploader.uploadImage(context, imageUri, folder)
    }
}
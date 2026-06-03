package com.example.bodifyaifitness.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream

object CloudinaryUploader {

    private const val CLOUD_NAME    = "ddnzssyx4"
    private const val UPLOAD_PRESET = "bodify_avatars"
    private const val UPLOAD_URL    = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val client = OkHttpClient()

    /**
     * Compress ảnh rồi upload lên Cloudinary.
     * @return secure_url nếu thành công, null nếu lỗi
     */
    suspend fun uploadAvatar(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val bytes = compressImage(context, uri) ?: return@withContext null

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    name     = "file",
                    filename = "avatar.jpg",
                    body     = bytes.toRequestBody("image/jpeg".toMediaType())
                )
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", "avatars")
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val json = JSONObject(response.body?.string() ?: return@withContext null)
            json.optString("secure_url").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resize ảnh về tối đa 512x512 và compress JPEG 80%.
     * Giảm kích thước upload và tiết kiệm bandwidth Cloudinary.
     */
    private fun compressImage(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val maxSize = 512
            val (width, height) = original.width to original.height
            val scale = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height, 1f)

            val scaled = if (scale < 1f) {
                val matrix = Matrix().apply { postScale(scale, scale) }
                Bitmap.createBitmap(original, 0, 0, width, height, matrix, true)
                    .also { if (it != original) original.recycle() }
            } else {
                original
            }

            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
            scaled.recycle()
            out.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

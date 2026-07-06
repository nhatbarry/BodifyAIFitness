package com.example.bodifyaifitness.database

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Repository tra cứu GIF URL từ ExerciseDB API (oss.exercisedb.dev).
 * Sử dụng OkHttp (đã có sẵn trong dự án) + org.json (Android built-in).
 * In-memory cache để tránh gọi API lặp lại trong cùng một session.
 */
object ExerciseDbRepository {

    private const val TAG = "ExerciseDbRepo"
    private const val BASE_URL = "https://oss.exercisedb.dev/api/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Cache: exerciseName (lowercase) -> gifUrl
    private val gifCache = ConcurrentHashMap<String, String>()

    /**
     * Tìm GIF URL cho bài tập theo tên (tiếng Anh).
     * Trả về URL nếu tìm thấy, null nếu không có kết quả hoặc lỗi.
     */
    suspend fun getGifUrl(exerciseName: String): String? {
        if (exerciseName.isBlank()) return null

        val key = exerciseName.trim().lowercase()

        // Kiểm tra cache trước
        gifCache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val encoded = java.net.URLEncoder.encode(key, "UTF-8")
                val url = "$BASE_URL/exercises/search?search=$encoded&threshold=0.4"
                val request = Request.Builder().url(url).get().build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.w(TAG, "API error ${response.code} for: $exerciseName")
                    return@withContext null
                }

                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)

                if (!json.optBoolean("success", false)) return@withContext null

                val dataArray = json.optJSONArray("data") ?: return@withContext null
                if (dataArray.length() == 0) {
                    Log.d(TAG, "No results for: $exerciseName")
                    return@withContext null
                }

                val gifUrl = dataArray.getJSONObject(0).optString("gifUrl", "")
                if (gifUrl.isNotEmpty()) {
                    gifCache[key] = gifUrl
                    Log.d(TAG, "Found GIF for '$exerciseName': $gifUrl")
                }
                gifUrl.ifEmpty { null }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch GIF for '$exerciseName'", e)
                null
            }
        }
    }

    /**
     * Lấy chi tiết bài tập theo exerciseId của ExerciseDB.
     * Trả về Map chứa gifUrl và instructions.
     */
    suspend fun getExerciseDetail(exerciseDbId: String): ExerciseDbDetail? {
        if (exerciseDbId.isBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL/exercises/$exerciseDbId"
                val request = Request.Builder().url(url).get().build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                if (!json.optBoolean("success", false)) return@withContext null

                val data = json.optJSONObject("data") ?: return@withContext null
                val gifUrl = data.optString("gifUrl", "")
                val instructionsArr = data.optJSONArray("instructions")
                val instructions = buildList {
                    if (instructionsArr != null) {
                        for (i in 0 until instructionsArr.length()) {
                            add(instructionsArr.getString(i))
                        }
                    }
                }

                ExerciseDbDetail(
                    exerciseId = data.optString("exerciseId"),
                    gifUrl = gifUrl,
                    instructions = instructions
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch detail for exerciseId: $exerciseDbId", e)
                null
            }
        }
    }

    fun clearCache() = gifCache.clear()
}

data class ExerciseDbDetail(
    val exerciseId: String,
    val gifUrl: String,
    val instructions: List<String>
)

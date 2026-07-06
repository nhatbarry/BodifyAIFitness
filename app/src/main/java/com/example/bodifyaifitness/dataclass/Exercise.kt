package com.example.bodifyaifitness.dataclass

import com.google.firebase.firestore.PropertyName

data class Exercise(
    val id: String = "",
    val name: String = "",
    @get:PropertyName("name_en")
    @set:PropertyName("name_en")
    var nameEn: String = "",            // Tên tiếng Anh để search ExerciseDB (fallback = name)
    val category: String = "",
    val target: String = "",
    val equipment: String = "",
    @get:PropertyName("muscle_group")
    @set:PropertyName("muscle_group")
    var muscleGroup: String = "",
    @get:PropertyName("secondary_muscles")
    @set:PropertyName("secondary_muscles")
    var secondaryMuscles: List<String> = emptyList(),
    @get:PropertyName("image")
    @set:PropertyName("image")
    var thumbnail: String = "",
    @get:PropertyName("gif_url")
    @set:PropertyName("gif_url")
    var gif: String = "",
    @get:PropertyName("isAISupported")
    @set:PropertyName("isAISupported")
    var isAISupported: Boolean = false,
    // Thuật toán camera AI dùng để đếm rep/chấm form cho bài này, vd "squat" / "pushup".
    // Nhiều bài tập có thể dùng chung 1 thuật toán. Chỉ có ý nghĩa khi isAISupported = true;
    // set thủ công trong Firestore Console.
    @get:PropertyName("ai_algorithm")
    @set:PropertyName("ai_algorithm")
    var aiAlgorithm: String = "",
    @get:PropertyName("instruction_steps")
    @set:PropertyName("instruction_steps")
    var instructionSteps: Map<String, List<String>> = emptyMap()
) {
    /**
     * URL ảnh tĩnh / thumbnail.
     * Chỉ trả về nếu đã là URL hợp lệ (http/https), không thì rỗng.
     */
    val thumbnailUrl: String
        get() = if (thumbnail.startsWith("http")) thumbnail else ""

    /**
     * URL GIF bài tập từ Firestore.
     * Nếu rỗng thì UI sẽ tự fetch qua ExerciseDbRepository theo tên bài tập.
     */
    val gifUrl: String
        get() = if (gif.startsWith("http")) gif else ""

    /**
     * Tên dùng để search ExerciseDB API (tiếng Anh).
     * Ưu tiên nameEn, fallback về name.
     */
    val searchName: String
        get() = nameEn.ifBlank { name }
}

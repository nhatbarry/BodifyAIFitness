package com.example.bodifyaifitness.dataclass

import com.google.firebase.firestore.PropertyName

data class Exercise(
    val id: String = "",
    val name: String = "",
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
    @get:PropertyName("instruction_steps")
    @set:PropertyName("instruction_steps")
    var instructionSteps: Map<String, List<String>> = emptyMap()
) {
    val thumbnailUrl: String
        get() = thumbnail.toCloudinaryUrl()

    val gifUrl: String
        get() = gif.toCloudinaryUrl()

    companion object {
        private const val BASE = "https://raw.githubusercontent.com/hasaneyldrm/exercises-dataset/master"
        private fun String.toCloudinaryUrl() =
            if (startsWith("http")) this else "$BASE/$this"
    }
}

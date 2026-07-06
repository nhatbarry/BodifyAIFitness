package com.example.bodifyaifitness.posecoach

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Thin TextToSpeech wrapper for the AI Coach. Speaks [FormFeedback] cues coming out of an
 * [ExerciseProcessor]. Uses QUEUE_FLUSH for corrections so a stale cue never plays over a
 * fresher one, and a separate QUEUE_ADD lane for rep counting so numbers don't get cut off
 * by form feedback.
 */
class TtsCoach(context: Context) {

    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts.language = Locale.getDefault()
    }

    fun speakFeedback(feedback: FormFeedback) {
        if (!ready) return
        tts.speak(feedback.message, TextToSpeech.QUEUE_FLUSH, null, "form_feedback")
    }

    fun announceRep(count: Int) {
        if (!ready) return
        tts.speak(count.toString(), TextToSpeech.QUEUE_ADD, null, "rep_$count")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

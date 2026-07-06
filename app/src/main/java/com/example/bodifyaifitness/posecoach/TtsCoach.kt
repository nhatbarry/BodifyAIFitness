package com.example.bodifyaifitness.posecoach

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatDelegate

/**
 * Thin TextToSpeech wrapper for the AI Coach. Speaks form-feedback text resolved by the caller
 * (from [FeedbackKey] via Android string resources), so this stays language-agnostic. Uses
 * QUEUE_FLUSH for corrections so a stale cue never plays over a fresher one, and a separate
 * QUEUE_ADD lane for rep counting so numbers don't get cut off by form feedback.
 */
class TtsCoach(context: Context) {

    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            // Match the app's own Vietnamese/English toggle (AccountPage), not the device's
            // system locale — otherwise switching the app to English still speaks Vietnamese
            // (or vice versa) whenever it differs from the phone's system language.
            val appLocale = AppCompatDelegate.getApplicationLocales().get(0)
            if (appLocale != null) tts.language = appLocale
        }
    }

    fun speak(text: String) {
        if (!ready) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "form_feedback")
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

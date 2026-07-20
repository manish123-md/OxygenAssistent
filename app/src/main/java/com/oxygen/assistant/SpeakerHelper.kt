package com.oxygen.assistant

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object SpeakerHelper {
    private var tts: TextToSpeech? = null

    fun speak(context: Context, text: String) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("hi", "IN")
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "oxygen_utt")
                }
            }
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "oxygen_utt")
        }
    }
}

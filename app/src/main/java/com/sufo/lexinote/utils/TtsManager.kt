package com.sufo.lexinote.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsManager(
    context: Context,
    private val onInit: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TtsManager", "The Language specified is not supported!")
                onInit(false)
            } else {
                onInit(true)
            }
        } else {
            Log.e("TtsManager", "Initialization Failed!")
            onInit(false)
        }
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

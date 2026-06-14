package com.ttjapan.kaimonomemo.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

class ContinuousSpeechController(
    private val context: Context,
    private val onFinalText: (String) -> Unit
) {
    var isRunning by mutableStateOf(false)
        private set
    var partialText by mutableStateOf("")
        private set
    var isHearingSpeech by mutableStateOf(false)
        private set

    private var recognizer: SpeechRecognizer? = null
    private var restartRequested = false

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        if (isRunning) return
        isRunning = true
        restartRequested = true
        startRecognizer()
    }

    fun stop() {
        restartRequested = false
        isRunning = false
        partialText = ""
        isHearingSpeech = false
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    fun commitPartial() {
        val text = partialText.trim()
        if (text.isNotBlank()) {
            partialText = ""
            isHearingSpeech = false
            onFinalText(text)
        }
    }

    fun destroy() {
        stop()
    }

    private fun startRecognizer() {
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { speechRecognizer ->
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() {
                    isHearingSpeech = true
                }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onPartialResults(partialResults: Bundle?) {
                    partialText = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    if (partialText.isNotBlank()) isHearingSpeech = true
                }

                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()
                    partialText = ""
                    isHearingSpeech = false
                    if (text.isNotBlank()) onFinalText(text)
                    restartIfNeeded()
                }

                override fun onError(error: Int) {
                    partialText = ""
                    isHearingSpeech = false
                    restartIfNeeded()
                }
            })
            speechRecognizer.startListening(recognizerIntent())
        }
    }

    private fun restartIfNeeded() {
        if (!restartRequested) return
        recognizer?.destroy()
        recognizer = null
        android.os.Handler(context.mainLooper).postDelayed({
            if (restartRequested) startRecognizer()
        }, 350L)
    }

    private fun recognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 900L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 700L)
        }
    }
}

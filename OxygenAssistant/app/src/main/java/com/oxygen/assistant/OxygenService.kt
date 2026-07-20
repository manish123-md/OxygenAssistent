package com.oxygen.assistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat

/**
 * OXYGEN - background wala "hamesha chalne wala" service.
 *
 * IMPORTANT (samajhna zaroori hai):
 * Android me sach me "hamesha 24x7 free mic listening" karna Google/Amazon jaisi companies
 * ek special LOW-POWER wake-word chip / SDK (jaise Picovoice Porcupine) se karte hain, kyunki
 * normal SpeechRecognizer battery jyada khata hai aur screen-off / Doze mode me OS usko rok sakta hai.
 *
 * Ye code Android ke built-in SpeechRecognizer se ek "continuous restart loop" banata hai jo
 * FOREGROUND SERVICE (permanent notification ke sath) ke through chalta hai - ye sabse practical
 * free tarika hai bina kisi paid SDK ke. Better accuracy/battery ke liye baad me isme
 * Picovoice Porcupine (free tier, apna wake-word "Hey Oxygen" train karke) plug kar sakte ho.
 */
class OxygenService : Service() {

    private var recognizer: SpeechRecognizer? = null
    private lateinit var island: DynamicIsland
    private lateinit var commandProcessor: CommandProcessor
    private val handler = Handler(Looper.getMainLooper())

    private var listeningForCommand = false

    override fun onCreate() {
        super.onCreate()
        island = DynamicIsland(this)
        commandProcessor = CommandProcessor(this)
        startForegroundNotification()
        startListeningLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // OS dwara kill hone par service ko dubara start karne ki koshish
    }

    private fun startForegroundNotification() {
        val channelId = "oxygen_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Oxygen Assistant", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Oxygen chal raha hai")
            .setContentText("Bolo: Hey Oxygen")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
        startForeground(1, notif)
    }

    private fun startListeningLoop() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""

                if (!listeningForCommand) {
                    // Ham "wake word" mode me hain - dekho kya "hey oxygen" bola gaya
                    if (text.contains("oxygen")) {
                        onWakeWordDetected()
                    } else {
                        restartListening()
                    }
                } else {
                    // Ham command mode me hain - jo bhi bola gaya use process karo
                    handleCommand(text)
                }
            }

            override fun onError(error: Int) {
                // Chup chap dobara start kar do (network / no-match errors ignore)
                restartListening()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        restartListening()
    }

    private fun restartListening() {
        handler.postDelayed({
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            try {
                recognizer?.startListening(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 400)
    }

    private fun onWakeWordDetected() {
        listeningForCommand = true
        handler.post {
            island.show("Sun raha hoon...")
        }
        restartListening()
    }

    private fun handleCommand(text: String) {
        handler.post { island.update("Soch raha hoon...") }

        val reply = commandProcessor.process(text)

        handler.post {
            island.update(reply)
            SpeakerHelper.speak(this, reply)
        }

        // Kaam khatam hone ke baad island 2.5 second me chhup jayega aur wapas wake-word mode me
        handler.postDelayed({
            island.hide()
            listeningForCommand = false
            restartListening()
        }, 2500)
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer?.destroy()
    }

    override fun onBind(intent: Intent?) = null
}

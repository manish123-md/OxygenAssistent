package com.oxygen.assistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra("text") ?: "Reminder!"
        val channelId = "oxygen_reminders"

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Oxygen Reminders", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val notif = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Oxygen Reminder")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        nm.notify((System.currentTimeMillis() % 10000).toInt(), notif)

        // Reminder bolke bhi bata do
        SpeakerHelper.speak(context, "Reminder: $text")
    }
}

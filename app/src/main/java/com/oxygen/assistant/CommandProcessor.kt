package com.oxygen.assistant

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.provider.Settings
import java.util.Calendar

class CommandProcessor(private val context: Context) {

    private var torchOn = false

    /**
     * Ye function ek line ka text leta hai (jo speech-to-text se aaya)
     * aur uske hisaab se kaam karta hai. Return value = user ko bolne wala reply.
     */
    fun process(commandRaw: String): String {
        val cmd = commandRaw.lowercase()

        return when {
            cmd.contains("torch") || cmd.contains("flash") || cmd.contains("light on") || cmd.contains("light off") -> {
                toggleTorch(cmd.contains("off"))
                if (torchOn) "Torch on kar diya" else "Torch off kar diya"
            }

            cmd.contains("wifi") || cmd.contains("data") || cmd.contains("internet") -> {
                // Android 10+ me apps directly wifi/mobile-data on/off nahi kar sakte (security restriction).
                // Isliye hum seedha system ka Wifi/Network settings panel khol dete hain, user ek tap me on/off kar sakta hai.
                openNetworkPanel()
                "Network settings khol raha hoon, ek tap me on kar lo"
            }

            cmd.contains("time") || cmd.contains("samay") -> {
                val c = Calendar.getInstance()
                "Abhi time hai ${c.get(Calendar.HOUR)} baj kar ${c.get(Calendar.MINUTE)} minute"
            }

            cmd.contains("reminder") || cmd.contains("yaad dila") -> {
                // simple version: 1 minute baad reminder (production me tum natural language time-parsing add karna)
                setReminder(commandRaw, delayMinutes = 1)
                "Theek hai, main tumhe yaad dila dunga"
            }

            cmd.startsWith("open ") || cmd.contains("khol") -> {
                val appName = cmd.replace("open", "").replace("khol", "").trim()
                val opened = openApp(appName)
                if (opened) "$appName khol raha hoon" else "Mujhe $appName nahi mila"
            }

            cmd.contains("joke") || cmd.contains("chutkula") -> {
                Jokes.random()
            }

            else -> {
                // Default: koi bhi doosra command ho, uske sath ek joke bhi sunao (jaisa user ne kaha tha)
                "Ye kaam ho gaya. Waise ek joke suno - ${Jokes.random()}"
            }
        }
    }

    private fun toggleTorch(forceOff: Boolean) {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val camId = cm.cameraIdList[0]
            torchOn = if (forceOff) false else !torchOn
            cm.setTorchMode(camId, torchOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openNetworkPanel() {
        val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun openApp(name: String): Boolean {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        for (appInfo in apps) {
            val label = pm.getApplicationLabel(appInfo).toString()
            if (label.lowercase().contains(name)) {
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }
        return false
    }

    private fun setReminder(text: String, delayMinutes: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.putExtra("text", text)
        val pi = PendingIntent.getBroadcast(
            context, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + delayMinutes * 60_000
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
}

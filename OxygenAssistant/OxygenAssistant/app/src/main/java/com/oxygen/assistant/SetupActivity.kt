package com.oxygen.assistant

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat

/**
 * Ye activity sirf ek hi baar chalti hai (jab tum app install karke pehli baar khologe).
 * Ye 3 kaam karti hai:
 * 1) Zaroori permissions maangti hai (mic, overlay)
 * 2) OxygenService (background listener) start karti hai
 * 3) Apna khud ka launcher icon home screen se HIDE kar deti hai
 *
 * Iske baad tumhe home screen pe "Oxygen" ka icon nahi dikhega,
 * lekin service background me hamesha chalti rahegi.
 */
class SetupActivity : Activity() {

    private val REQ_PERMS = 101
    private val REQ_OVERLAY = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: normal runtime permissions
        val needed = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        val missing = needed.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQ_PERMS)
        } else {
            checkOverlayThenStart()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERMS) checkOverlayThenStart()
    }

    private fun checkOverlayThenStart() {
        // Step 2: overlay (dynamic island) dikhane ke liye "draw over other apps" permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQ_OVERLAY)
        } else {
            finishSetup()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_OVERLAY) finishSetup()
    }

    private fun finishSetup() {
        // Step 3: service start karo
        val serviceIntent = Intent(this, OxygenService::class.java)
        startForegroundService(serviceIntent)

        // Step 4: apna launcher icon home screen se hide kar do
        val alias = ComponentName(this, "com.oxygen.assistant.SetupActivity")
        packageManager.setComponentEnabledSetting(
            alias,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        Toast.makeText(this, "Oxygen active ho gaya. Ab bolo: Hey Oxygen", Toast.LENGTH_LONG).show()
        finish()
    }
}

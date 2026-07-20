package com.oxygen.assistant

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

/**
 * iPhone jaisa "Dynamic Island" overlay.
 * Screen ke top pe ek chota transparent pill dikhta hai jo:
 *  - "Hey Oxygen" sunte hi expand hoke animate hota hai
 *  - status text dikhata hai (Listening... / Processing... / Done)
 *  - kaam khatam hote hi shrink hoke gayab ho jata hai
 */
class DynamicIsland(private val context: Context) {

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var pillView: LinearLayout? = null
    private var label: TextView? = null

    private fun overlayType() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

    fun show(initialText: String) {
        if (pillView != null) {
            update(initialText)
            return
        }

        val bg = GradientDrawable().apply {
            cornerRadius = 60f
            setColor(Color.parseColor("#CC000000")) // semi-transparent black
        }

        label = TextView(context).apply {
            text = initialText
            setTextColor(Color.WHITE)
            textSize = 13f
        }

        pillView = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = bg
            setPadding(40, 18, 40, 18)
            addView(label)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 30

        pillView?.alpha = 0f
        pillView?.scaleX = 0.6f
        pillView?.scaleY = 0.6f
        wm.addView(pillView, params)

        // pop-in animation
        pillView?.animate()?.alpha(1f)?.scaleX(1f)?.scaleY(1f)?.setDuration(250)?.start()
    }

    fun update(text: String) {
        label?.text = text
        // chota pulse animation har update pe
        val anim = ValueAnimator.ofFloat(1f, 1.08f, 1f)
        anim.duration = 220
        anim.addUpdateListener {
            val v = it.animatedValue as Float
            pillView?.scaleX = v
            pillView?.scaleY = v
        }
        anim.start()
    }

    fun hide() {
        val view = pillView ?: return
        view.animate().alpha(0f).scaleX(0.6f).scaleY(0.6f).setDuration(200)
            .withEndAction {
                try { wm.removeView(view) } catch (e: Exception) { }
                pillView = null
                label = null
            }.start()
    }
}

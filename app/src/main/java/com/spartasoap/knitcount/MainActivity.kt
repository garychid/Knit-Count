package com.spartasoap.knitcount

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var counter = 0
    private lateinit var counterTextView: TextView
    private lateinit var toneGenerator: ToneGenerator

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.counterTextView)
        val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)
        val resetButton = findViewById<Button>(R.id.resetButton)
        // To replace the background image (R.drawable.background_image) with a color you can choose in code,
        // just remove the image line and use setBackgroundColor() instead.
        // 🎨 Set background COLOR instead of image
        // You can use a named color, Color.RED, or custom hex:
        rootLayout.setBackgroundColor(Color.parseColor("#DCEAFB")) // soft blue
        // Example alternatives:
        // rootLayout.setBackgroundColor(Color.LTGRAY)
        // rootLayout.setBackgroundColor(Color.parseColor("#FFF3E0")) // warm cream

        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        val prefs = getSharedPreferences("KnitCountPrefs", Context.MODE_PRIVATE)

        // Load saved counter value
        counter = prefs.getInt("counterValue", 0)
        updateCounter()
        Toast.makeText(this, "Restored count: $counter", Toast.LENGTH_SHORT).show()

        // Tap anywhere to increase counter
        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                counter++
                updateCounter()
                prefs.edit().putInt("counterValue", counter).apply()

                // Beep
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 400)

                // Animation
                animateCounter()
                true
            } else {
                false
            }
        }

        // Reset button
        resetButton.setOnClickListener {
            counter = 0
            updateCounter()
            prefs.edit().putInt("counterValue", counter).apply()

            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 10)

            // Animate on reset
            animateCounter()
        }
    }

    private fun updateCounter() {
        counterTextView.text = counter.toString()
        counterTextView.setTextColor(Color.BLACK) // solid black text
    }
        // code below causes numbers to expand deflate - animation
    private fun animateCounter() {
        val scaleAnimation = ScaleAnimation(
            1.0f, 1.3f,
            1.0f, 1.3f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 150
        scaleAnimation.repeatMode = Animation.REVERSE
        scaleAnimation.repeatCount = 1
        counterTextView.startAnimation(scaleAnimation)
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator.release()
    }
}


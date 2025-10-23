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
        val decrementButton = findViewById<Button>(R.id.decrementButton)
        val incrementButton = findViewById<Button>(R.id.incrementButton)

        rootLayout.setBackgroundColor(Color.parseColor("#DCEAFB")) // soft blue

        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        val prefs = getSharedPreferences("KnitCountPrefs", Context.MODE_PRIVATE)

        // Load saved counter
        counter = prefs.getInt("counterValue", 0)
        updateCounter()
        Toast.makeText(this, "Restored count: $counter", Toast.LENGTH_SHORT).show()

        // Tap anywhere to increase
        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                incrementCounter(prefs)
                true
            } else false
        }

        // Increment button
        incrementButton.setOnClickListener {
            incrementCounter(prefs)
        }

        // Decrement button
        decrementButton.setOnClickListener {
            if (counter > 0) {
                counter--
                updateCounter()
                prefs.edit().putInt("counterValue", counter).apply()
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                animateCounter()
            } else {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 100)
                Toast.makeText(this, "Already at zero", Toast.LENGTH_SHORT).show()
            }
        }

        // Reset button
        resetButton.setOnClickListener {
            counter = 0
            updateCounter()
            prefs.edit().putInt("counterValue", counter).apply()
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 10)
            animateCounter()
        }
    }

    private fun incrementCounter(prefs: android.content.SharedPreferences) {
        counter++
        updateCounter()
        prefs.edit().putInt("counterValue", counter).apply()
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        animateCounter()
    }

    private fun updateCounter() {
        counterTextView.text = counter.toString()
        counterTextView.setTextColor(Color.BLACK)
    }

    private fun animateCounter() {
        val scaleAnimation = ScaleAnimation(
            1.0f, 1.3f,
            1.0f, 1.3f,
            Animation.RELATIVE_TO_SELF, 0.7f,
            Animation.RELATIVE_TO_SELF, 0.7f
        )
        scaleAnimation.duration = 180
        scaleAnimation.repeatMode = Animation.REVERSE
        scaleAnimation.repeatCount = 1
        counterTextView.startAnimation(scaleAnimation)
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator.release()
    }
}

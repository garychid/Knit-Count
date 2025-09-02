package com.spartasoap.knitcount
import android.os.Bundle
import android.view.MotionEvent
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var counter = 0
    private lateinit var counterTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.counterTextView)
        val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)

        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                counter++
                updateCounter()
                true
            } else {
                false
            }
        }
    }

    private fun updateCounter() {
        counterTextView.text = counter.toString()
    }
}

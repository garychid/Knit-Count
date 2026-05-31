package com.spartasoap.knitcount

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val counters = IntArray(MAX_COUNTERS)
    private val counterTitles = Array(MAX_COUNTERS) { defaultCounterTitle(it) }
    private var visibleCounterCount = 1

    private lateinit var counterPanels: List<LinearLayout>
    private lateinit var counterTitleViews: List<TextView>
    private lateinit var counterTextViews: List<TextView>
    private lateinit var decrementButtons: List<Button>
    private lateinit var resetButtons: List<Button>
    private lateinit var incrementButtons: List<Button>
    private lateinit var counterCountButtons: List<RadioButton>

    private lateinit var prefs: SharedPreferences
    private lateinit var toneGenerator: ToneGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.rootLayout).setBackgroundColor(Color.parseColor("#DCEAFB"))

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        bindViews()
        loadSavedState()
        configureCounterCountSelector()
        configureCounterControls()
        updateAllCounters()
        updateVisibleCounters()
    }

    private fun bindViews() {
        counterPanels = listOf(
            findViewById(R.id.counterPanelOne),
            findViewById(R.id.counterPanelTwo),
            findViewById(R.id.counterPanelThree),
            findViewById(R.id.counterPanelFour)
        )
        counterTitleViews = listOf(
            findViewById(R.id.counterTitleOne),
            findViewById(R.id.counterTitleTwo),
            findViewById(R.id.counterTitleThree),
            findViewById(R.id.counterTitleFour)
        )
        counterTextViews = listOf(
            findViewById(R.id.counterTextViewOne),
            findViewById(R.id.counterTextViewTwo),
            findViewById(R.id.counterTextViewThree),
            findViewById(R.id.counterTextViewFour)
        )
        decrementButtons = listOf(
            findViewById(R.id.decrementButtonOne),
            findViewById(R.id.decrementButtonTwo),
            findViewById(R.id.decrementButtonThree),
            findViewById(R.id.decrementButtonFour)
        )
        resetButtons = listOf(
            findViewById(R.id.resetButtonOne),
            findViewById(R.id.resetButtonTwo),
            findViewById(R.id.resetButtonThree),
            findViewById(R.id.resetButtonFour)
        )
        incrementButtons = listOf(
            findViewById(R.id.incrementButtonOne),
            findViewById(R.id.incrementButtonTwo),
            findViewById(R.id.incrementButtonThree),
            findViewById(R.id.incrementButtonFour)
        )
        counterCountButtons = listOf(
            findViewById(R.id.counterCountOne),
            findViewById(R.id.counterCountTwo),
            findViewById(R.id.counterCountThree),
            findViewById(R.id.counterCountFour)
        )
    }

    private fun loadSavedState() {
        for (index in counters.indices) {
            counters[index] = if (index == 0 && shouldMigrateOldCounter()) {
                prefs.getInt(OLD_COUNTER_KEY, 0)
            } else {
                prefs.getInt(counterKey(index), 0)
            }
            counterTitles[index] = normalizeCounterTitle(
                index,
                prefs.getString(counterTitleKey(index), defaultCounterTitle(index)).orEmpty()
            )
        }

        if (shouldMigrateOldCounter()) {
            prefs.edit().putInt(counterKey(0), counters[0]).apply()
        }

        visibleCounterCount = prefs.getInt(VISIBLE_COUNTER_COUNT_KEY, 1)
            .coerceIn(1, MAX_COUNTERS)
    }

    private fun shouldMigrateOldCounter(): Boolean {
        return prefs.contains(OLD_COUNTER_KEY) && !prefs.contains(counterKey(0))
    }

    private fun configureCounterCountSelector() {
        counterCountButtons[visibleCounterCount - 1].isChecked = true

        findViewById<RadioGroup>(R.id.counterCountGroup).setOnCheckedChangeListener { _, checkedId ->
            val selectedCount = when (checkedId) {
                R.id.counterCountOne -> 1
                R.id.counterCountTwo -> 2
                R.id.counterCountThree -> 3
                R.id.counterCountFour -> 4
                else -> visibleCounterCount
            }
            setVisibleCounterCount(selectedCount)
        }
    }

    private fun configureCounterControls() {
        for (index in counters.indices) {
            counterTitleViews[index].setOnLongClickListener {
                showRenameCounterDialog(index)
                true
            }
            counterTextViews[index].setOnClickListener {
                incrementCounter(index)
            }
            incrementButtons[index].setOnClickListener {
                incrementCounter(index)
            }
            decrementButtons[index].setOnClickListener {
                decrementCounter(index)
            }
            resetButtons[index].setOnClickListener {
                resetCounter(index)
            }
        }
    }

    private fun setVisibleCounterCount(count: Int) {
        visibleCounterCount = count.coerceIn(1, MAX_COUNTERS)
        prefs.edit().putInt(VISIBLE_COUNTER_COUNT_KEY, visibleCounterCount).apply()
        updateVisibleCounters()
    }

    private fun incrementCounter(index: Int) {
        counters[index]++
        saveCounter(index)
        updateCounter(index)
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        animateCounter(index)
    }

    private fun decrementCounter(index: Int) {
        if (counters[index] > 0) {
            counters[index]--
            saveCounter(index)
            updateCounter(index)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            animateCounter(index)
        } else {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 100)
            Toast.makeText(this, "${counterTitles[index]} is already at zero", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetCounter(index: Int) {
        counters[index] = 0
        saveCounter(index)
        updateCounter(index)
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 10)
        animateCounter(index)
    }

    private fun showRenameCounterDialog(index: Int) {
        val input = EditText(this).apply {
            filters = arrayOf(InputFilter.LengthFilter(MAX_TITLE_LENGTH))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setSelectAllOnFocus(true)
            setText(counterTitles[index])
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Rename counter")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                saveCounterTitle(index, input.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCounterTitle(index: Int, title: String) {
        counterTitles[index] = normalizeCounterTitle(index, title)
        prefs.edit().putString(counterTitleKey(index), counterTitles[index]).apply()
        updateCounterTitle(index)
    }

    private fun saveCounter(index: Int) {
        prefs.edit().putInt(counterKey(index), counters[index]).apply()
    }

    private fun updateAllCounters() {
        for (index in counters.indices) {
            updateCounterTitle(index)
            updateCounter(index)
        }
    }

    private fun updateCounterTitle(index: Int) {
        counterTitleViews[index].text = counterTitles[index]
    }

    private fun updateCounter(index: Int) {
        counterTextViews[index].text = counters[index].toString()
        counterTextViews[index].setTextColor(Color.BLACK)
    }

    private fun updateVisibleCounters() {
        for (index in counterPanels.indices) {
            counterPanels[index].visibility = if (index < visibleCounterCount) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun animateCounter(index: Int) {
        val scaleAnimation = ScaleAnimation(
            1.0f, 1.15f,
            1.0f, 1.15f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 180
        scaleAnimation.repeatMode = Animation.REVERSE
        scaleAnimation.repeatCount = 1
        counterTextViews[index].startAnimation(scaleAnimation)
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator.release()
    }

    private fun counterKey(index: Int): String {
        return "counterValue$index"
    }

    private fun counterTitleKey(index: Int): String {
        return "counterTitle$index"
    }

    private fun defaultCounterTitle(index: Int): String {
        return "Counter ${index + 1}"
    }

    private fun normalizeCounterTitle(index: Int, title: String): String {
        val trimmedTitle = title.trim()
        return if (trimmedTitle.isBlank()) {
            defaultCounterTitle(index)
        } else {
            trimmedTitle.take(MAX_TITLE_LENGTH)
        }
    }

    companion object {
        private const val MAX_COUNTERS = 4
        private const val MAX_TITLE_LENGTH = 24
        private const val PREFS_NAME = "KnitCountPrefs"
        private const val OLD_COUNTER_KEY = "counterValue"
        private const val VISIBLE_COUNTER_COUNT_KEY = "visibleCounterCount"
    }
}

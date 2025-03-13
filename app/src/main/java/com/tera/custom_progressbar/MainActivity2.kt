package com.tera.custom_progressbar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tera.custom_progressbar.databinding.ActivityMain2Binding
import java.util.Timer
import java.util.TimerTask

class MainActivity2 : AppCompatActivity() {

    companion object{
        const val VALUE = "value"
    }

    private lateinit var binding: ActivityMain2Binding
    private lateinit var sp: SharedPreferences
    private var mValue = 0f
    private var mValueMax = 50f
    private var keyTimer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        sp = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        mValue = sp.getFloat(VALUE, 0f)

        setStart()
        setText()
        init()
    }

    private fun setStart() = with(binding) {
        slProgress.valueTo = mValueMax.toFloat()
        slProgress.value = mValue

        pb1.valueMax = mValueMax
        pb1.value = mValue
        pb2.valueMax = mValueMax
        pb2.value = mValue
        pb3.valueMax = mValueMax
        pb3.value = mValue
        pb4.valueMax = mValueMax
        pb4.value = mValue
        pb5.valueMax = mValueMax
        pb5.value = mValue
        pb6.valueMax = mValueMax
        pb6.value = mValue
    }

    private fun setText() = with(binding) {
        val str = mValue.toInt().toString()
        tvProgress.text = str
    }

    private fun init() = with(binding) {
        slProgress.addOnChangeListener { _, value, _ ->
            mValue = value
            pb1.value = mValue
            pb2.value = mValue
            pb3.value = mValue
            pb4.value = mValue
            pb5.value = mValue
            pb6.value = mValue
            setText()
        }

        bnBack.setOnClickListener {
            startActivity(Intent(this@MainActivity2, MainActivity::class.java))
        }

        bnTimer.setOnClickListener {
            keyTimer = !keyTimer
            if (keyTimer)
                startTimer()
            else
                timer?.cancel()
        }
    }

    private var timer: Timer? = null
    private val countMax = mValueMax.toInt() * 2 - 10
    private val countMid = countMax / 2
    private val countStart = 5
    private var value = countStart // Текущее значение
    private var counter = 0        // Счетчик

    private fun startTimer() = with(binding) {
        timer = Timer()
        timer?.schedule(object: TimerTask() {
            override fun run() {
                counter++
                if (counter == countMax) {
                    counter = 0
                    value = countStart
                }
                if (counter < countMid) {
                    value++
                } else {
                    value--
                }
                runOnUiThread {
                    pb1.value = value.toFloat()
                    pb2.value = value.toFloat()
                    pb3.value = value.toFloat()
                    pb4.value = value.toFloat()
                    pb5.value = value.toFloat()
                    pb6.value = value.toFloat()
                }
            }

        }, 0, 50)
    }

    override fun onPause() {
        super.onPause()
        val editor = sp.edit()
        editor.putFloat(VALUE, mValue)
        editor.apply()
        timer?.cancel()
    }
}
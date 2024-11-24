package com.tera.custom_progressbar

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.tera.custom_progressbar.databinding.ActivityMainBinding


const val HOUR = "hour"
const val MIN = "min"
const val SEC = "sec"
const val NUM_FONT = "num_font"
const val VALUE_MAX = "value_max"
const val VALUE_PROG = "value_prog"

class MainActivity : AppCompatActivity(), SetTimeDialog.ListenerDialog {

    private lateinit var binding: ActivityMainBinding

    private var timer: CountDownTimer? = null
    private var numFont = 1
    private var hour = 0
    private var min = 0
    private var sec = 0
    private var totalTime = 0 // Time in sec
    private var countTime = 0 // Time counter
    private var valueMax = 0f
    private var valueProg = 0
    private var keyPause = false

    private var sounds: SoundPool? = null
    private var soundId = 0
    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Orientation
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        sp = getSharedPreferences("settings", Context.MODE_PRIVATE)
        hour = sp.getInt(HOUR, 0)
        min = sp.getInt(MIN, 0)
        sec = sp.getInt(SEC, 0)
        numFont = sp.getInt(NUM_FONT, 1)
        valueMax = sp.getFloat(VALUE_MAX, 10f)
        valueProg = sp.getInt(VALUE_PROG, 0)

        setSliders()

        initOther()
        initButtons()
        initSlider()
        setFont(numFont)
    }

    private fun initOther() = with(binding) {
        totalTime = hour * 3600 + min * 60 + sec
        val time = formatTime(totalTime)
        var str = getString(R.string.setting)
        str = "$str $time"
        tvSet.text = str
        createNewSoundPool()
        loadSounds()
    }

    private fun setSliders() = with(binding) {
        slMax.value = valueMax
        pbTest.max = valueMax.toInt()
        slProgress.value = valueProg.toFloat()
        pbTest.value = valueProg
        pbTest.text = valueProg.toString()
    }

    private fun initSlider() = with(binding) {
        valueMax = pbTest.max.toFloat()
        slProgress.valueTo = valueMax

        // Maximum value
        slMax.addOnChangeListener { _, value, _ ->
            valueMax = value
            pbTest.max = value.toInt()
            if (value + 1 > slProgress.value) {
                slProgress.value = 0f
            }
            slProgress.valueTo = value
        }

        // Progress
        slProgress.addOnChangeListener { _, value, _ ->
            valueProg = value.toInt()
            pbTest.value = value.toInt()
            pbTest.text = valueProg.toString()

            if (value > slProgress.valueTo * 0.7) {
                pbTest.dashColorActive = Color.BLUE
                pbTest.dashColorInactive = getColor(R.color.red)
            } else {
                pbTest.dashColorActive = Color.BLACK
                pbTest.dashColorInactive = getColor(R.color.green)
            }
        }
    }

    private fun initButtons() = with(binding) {

        bnSetting.setOnClickListener {
            openDialog()
        }

        bnStart.setOnClickListener {
            startTimer()
        }

        bnPause.setOnClickListener {
            timer?.cancel()
            valueProg = pbTimer.value
            keyPause = true
        }

        bnStop.setOnClickListener {
            timer?.cancel()
            pbTimer.value = 0
            countTime = 0
            pbTimer.text = ("0")
            sounds?.stop(soundId)
            keyPause = false
        }

        rgFont.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbFont1 -> setFont(1)
                R.id.rbFont2 -> setFont(2)
                R.id.rbFont3 -> setFont(3)
            }
        }
    }

    // Installing the font
    private fun setFont(num: Int) = with(binding) {
        numFont = num
        val font1 = Typeface.DEFAULT
        val font2 = ResourcesCompat.getFont(this@MainActivity, R.font.hover)
        val font3 = ResourcesCompat.getFont(this@MainActivity, R.font.led_new_italic)
        when (num) {
            1 -> {
                pbTest.fontFamily = font1
                pbTimer.fontFamily = font1
                rbFont1.isChecked = true
            }

            2 -> {
                pbTest.fontFamily = font2
                pbTimer.fontFamily = font2
                rbFont2.isChecked = true
            }

            3 -> {
                pbTest.fontFamily = font3
                pbTimer.fontFamily = font3
                rbFont3.isChecked = true
            }
        }
    }

    // Time format
    private fun formatTime(time: Int): String {
        val h = time / 3600
        val dT = time % 3600
        val m = dT / 60
        val s = time - (h * 3600 + m * 60)
        var ms = ""
        var ss = ""

        ms = if (h != 0 && m < 10) "0$m"
        else m.toString()

        ss = if (m != 0 && s < 10) "0$s"
        else s.toString()

        if (h != 0 && s < 10) ss = "0$s"

        if (h == 0 && m != 0) return "$ms:$ss"
        if (h == 0) return ss

        return "$h:$ms:$ss"
    }

    // Progress for timer
    private fun getProgressTimer(maxTime: Int): Int {
        val maxValue = binding.pbTimer.max
        return if (maxTime != 0) countTime * maxValue / maxTime
        else 0
    }

    // Timer
    private fun startTimer() = with(binding) {
        if (countTime == 0)
            countTime = totalTime

        val maxTime = totalTime
        var periodMillis = totalTime * 1000.toLong() // milliSec

        var strTime = formatTime(maxTime)
        pbTimer.text = strTime

        if (keyPause) {
            periodMillis = countTime * 1000.toLong()
            keyPause = false
        }

        var progress = getProgressTimer(maxTime)
        pbTimer.value = progress

        timer?.cancel()
        timer = object : CountDownTimer(periodMillis, 1000) {
            override fun onTick(timeM: Long) {
                countTime--
                progress = getProgressTimer(maxTime)
                strTime = formatTime(countTime)
                runOnUiThread {
                    pbTimer.value = progress
                    pbTimer.text = strTime
                }
            }

            // Stop
            override fun onFinish() {
                playAlarm()
            }
        }.start()
    }

    // Signal
    private fun playAlarm() {
        sounds?.play(soundId, 1.0f, 1.0f, 1, 1, 1f)
    }

    private fun createNewSoundPool() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        sounds = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build()
    }

    private fun loadSounds() {
        soundId = sounds!!.load(this, R.raw.xiaomi, 1)
    }

    private fun openDialog() {
        val manager = supportFragmentManager
        val dialog = SetTimeDialog()
        val bundle = Bundle()
        bundle.putInt(HOUR, hour)
        bundle.putInt(MIN, min)
        bundle.putInt(SEC, sec)
        dialog.isCancelable = false
        dialog.arguments = bundle
        dialog.show(manager, "time fragment")
    }

    // Data from the dialogue
    override fun currentValue(h: Int, m: Int, s: Int) {
        if (timer != null)
            timer?.cancel()

        hour = h; min = m; sec = s

        totalTime = h * 3600 + m * 60 + s
        countTime = totalTime
        val time = formatTime(totalTime)
        binding.pbTimer.text = time

        val progress = getProgressTimer(totalTime)
        binding.pbTimer.value = progress

        initOther()
    }

    override fun onPause() {
        super.onPause()
        val editor = sp.edit()
        editor.putInt(HOUR, hour)
        editor.putInt(MIN, min)
        editor.putInt(SEC, sec)
        editor.putInt(NUM_FONT, numFont)
        editor.putFloat(VALUE_MAX, valueMax)
        editor.putInt(VALUE_PROG, valueProg)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

}

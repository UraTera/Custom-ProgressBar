package com.tera.custom_progressbar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.tera.custom_progressbar.databinding.ActivityMainBinding
import com.tera.timedialog.TimeDialog

const val SETTINGS = "settings"

class MainActivity : AppCompatActivity() {

    companion object {
        const val HOUR = "hour"
        const val MIN = "min"
        const val SEC = "sec"
        const val NUM_FONT = "num_font"
        const val MAX_PROG = "max_prog"
        const val VALUE_PROG = "value_prog"
        const val TOTAL_TIME = "total_time"
        const val COUNT_TIME = "count_time"
        const val KEY_START = "key_start"
        const val KEY_PAUSE = "key_pause"
    }

    private lateinit var binding: ActivityMainBinding

    private var timer: CountDownTimer? = null
    private var numFont = 1
    private var mHour = 0
    private var mMin = 0
    private var mSec = 0
    private var totalTime = 0 // Time in sec
    private var countTime = 0 // Time counter

    private var maxProgress = 0f // Максимальное значение
    private var valProgress = 0f // Текущее значение
    private var kValue = 0f      // Соотношение значений

    private var keyPause = false
    private var keyStart = false

    private var sounds: SoundPool? = null
    private var soundId = 0

    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sp = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        mHour = sp.getInt(HOUR, 0)
        mMin = sp.getInt(MIN, 0)
        mSec = sp.getInt(SEC, 0)
        numFont = sp.getInt(NUM_FONT, 1)
        maxProgress = sp.getFloat(MAX_PROG, 10f)
        valProgress = sp.getFloat(VALUE_PROG, 0f)

        setSliders()
        initSlider()
        initButtons()
        setFont(numFont)
        initTimer()
        regBack()
    }

    private fun setSliders() = with(binding) {
        // Slider Maximum
        slSetMax.value = maxProgress
        pbMax.valueMax = maxProgress

        // Slider Progress
        if (valProgress > maxProgress) valProgress = maxProgress
        kValue = valProgress / maxProgress
        slProgress.valueTo = maxProgress
        slProgress.value = valProgress
        setProgMax()
    }

    private fun initSlider() = with(binding) {
        // Slider Maximum
        slSetMax.addOnChangeListener { _, value, _ ->
            maxProgress = value
            slProgress.valueTo = maxProgress
            slProgress.value = maxProgress * kValue
            setProgMax()
        }
        // Slider Progress
        slProgress.addOnChangeListener { _, value, _ ->
            valProgress = value
            kValue = valProgress / maxProgress
            setProgMax()
        }
    }

    private fun setProgMax() = with(binding) {
        pbMax.valueMax = maxProgress
        pbMax.value = valProgress
    }

    private fun initButtons() = with(binding) {

        bnOpenDialog.setOnClickListener {
            openDialog()
        }

        bnStart.setOnClickListener {
            if (countTime == 0)
                countTime = totalTime
            startTimer()
            keyStart = true
        }

        bnPause.setOnClickListener {
            timer?.cancel()
            valProgress = pbTimer.value
            keyPause = true
        }

        bnStop.setOnClickListener {
            timer?.cancel()
            pbTimer.value = 0f
            countTime = 0
            pbTimer.text = ("0")
            sounds?.stop(soundId)
            keyPause = false
            keyStart = false
        }

        bnNext.setOnClickListener {
            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
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
                pbMax.fontFamily = font1
                pbTimer.fontFamily = font1
                rbFont1.isChecked = true
            }

            2 -> {
                pbMax.fontFamily = font2
                pbTimer.fontFamily = font2
                rbFont2.isChecked = true
            }

            3 -> {
                pbMax.fontFamily = font3
                pbTimer.fontFamily = font3
                rbFont3.isChecked = true
            }
        }
    }

    private fun initTimer() = with(binding) {
        totalTime = mHour * 3600 + mMin * 60 + mSec
        val time = formatTime(totalTime)
        var str = getString(R.string.setting)
        str = "$str $time"
        tvSet.text = str
        createNewSoundPool()
        loadSounds()
    }

    // Time format
    private fun formatTime(time: Int): String {
        val h = time / 3600
        val dT = time % 3600
        val m = dT / 60
        val s = time - (h * 3600 + m * 60)

        val ms = if (h != 0 && m < 10) "0$m"
        else m.toString()

        var ss = if (m != 0 && s < 10) "0$s"
        else s.toString()

        if (h != 0 && s < 10) ss = "0$s"

        if (h == 0 && m != 0) return "$ms:$ss"
        if (h == 0) return ss

        return "$h:$ms:$ss"
    }

    // Progress for timer
    private fun getProgressTimer(maxTime: Int): Int {
        val maxValue = binding.pbTimer.valueMax
        return if (maxTime != 0) (countTime * maxValue / maxTime).toInt()
        else 0
    }

    // Timer
    private fun startTimer() = with(binding) {
        val maxTime = totalTime
        var periodMillis = totalTime * 1000.toLong() // milliSec

        var strTime = formatTime(maxTime)
        pbTimer.text = strTime

        if (keyPause) {
            periodMillis = countTime * 1000.toLong()
            keyPause = false
        }

        var progress = getProgressTimer(maxTime)
        pbTimer.value = progress.toFloat()

        timer?.cancel()
        timer = object : CountDownTimer(periodMillis, 1000) {
            override fun onTick(timeM: Long) {
                countTime--
                progress = getProgressTimer(maxTime)
                strTime = formatTime(countTime)
                runOnUiThread {
                    pbTimer.value = progress.toFloat()
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

    private fun openDialog(){
        val font: Typeface? = ResourcesCompat.getFont(this, R.font.led_bold)
        val dialog = TimeDialog(this)
        dialog.hour = mHour
        dialog.min = mMin
        dialog.sec = mSec
//        dialog.buttonCancelColor = -272682
//        dialog.textCancelColor = -65536
        dialog.maxHours = 23
        dialog.fontFamily = font
        dialog.setOnChangeListener { hour, min, sec ->
            mHour = hour
            mMin = min
            mSec = sec
            setValue()
        }
    }

    private fun setValue() {
        if (timer != null)
            timer?.cancel()

        totalTime = mHour * 3600 + mMin * 60 + mSec
        countTime = totalTime
        val time = formatTime(totalTime)
        binding.pbTimer.text = time

        val progress = getProgressTimer(totalTime)
        binding.pbTimer.value = progress.toFloat()

        initTimer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putInt(TOTAL_TIME, totalTime)
            putInt(COUNT_TIME, countTime)
            putBoolean(KEY_START, keyStart)
            putBoolean(KEY_PAUSE, keyPause)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            totalTime = getInt(TOTAL_TIME)
            countTime = getInt(COUNT_TIME)
            keyStart = getBoolean(KEY_START)
            keyPause = getBoolean(KEY_PAUSE)
        }

        if (countTime == 0)
            countTime = totalTime

        val strTime = formatTime(countTime)

        if (keyStart) {
            binding.pbTimer.value = valProgress
            binding.pbTimer.text = strTime
        }

        if (keyStart && !keyPause)
            startTimer()
    }

    override fun onPause() {
        super.onPause()
        val editor = sp.edit()
        editor.putInt(HOUR, mHour)
        editor.putInt(MIN, mMin)
        editor.putInt(SEC, mSec)
        editor.putInt(NUM_FONT, numFont)
        editor.putFloat(MAX_PROG, maxProgress)
        editor.putFloat(VALUE_PROG, valProgress)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    // Регистрация кнопки Back
    private fun regBack() {
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                exitOnBackPressed()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        exitOnBackPressed()
                    }
                })
        }
    }

    // Кнопка Back
    fun exitOnBackPressed() {
        finishAffinity() // Закрыть все
    }

}

package com.tera.custom_progressbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.tera.custom_progressbar.databinding.FragmentSetTimeBinding


class SetTimeDialog: DialogFragment() {

    interface ListenerDialog {
        fun currentValue(h: Int, m: Int, s: Int)
    }

    private lateinit var binding: FragmentSetTimeBinding
    private var hour = 0
    private var min = 0
    private var sec = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetTimeBinding.inflate(layoutInflater)

        if (arguments != null) {
            hour = arguments?.getInt(HOUR,0)!!
            min = arguments?.getInt(MIN,0)!!
            sec = arguments?.getInt(SEC,0)!!
        }

        setNumberPicker()
        initButtons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set dialog size
        setFullScreen()
    }

    private fun initButtons() = with(binding) {
        npHour.setOnValueChangedListener { _, _, newVal ->
            hour = newVal
        }
        npMin.setOnValueChangedListener { _, _, newVal ->
            min = newVal
            val str = newVal.toString()
            tvTest.text = str
        }
        npSec.setOnValueChangedListener { _, _, newVal ->
            sec = newVal
        }
        bnCancel.setOnClickListener {
            dismiss()
        }
        bnOk.setOnClickListener {
            sendData()
            dismiss()
        }
    }

    // Transfer data
    private fun sendData() {
        val listener = activity as ListenerDialog
        listener.currentValue(hour, min, sec)
    }

    // Setting up NumberPicker
    private fun setNumberPicker() = with(binding) {
        npHour.wrapSelectorWheel = true
        npHour.displayedValues = time24
        npHour.minValue = 0
        npHour.maxValue = 23
        npHour.value = hour

        npMin.wrapSelectorWheel = true
        npMin.displayedValues = time60
        npMin.minValue = 0
        npMin.maxValue = 59
        npMin.value = min

        npSec.wrapSelectorWheel = true
        npSec.displayedValues = time60
        npSec.minValue = 0
        npSec.maxValue = 59
        npSec.value = sec
    }

     // Full width
     private fun setFullScreen() {
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object{
        var time24 = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")

        var time60 = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24",
            "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38",
            "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52",
            "53", "54", "55", "56", "57", "58", "59")
    }

}
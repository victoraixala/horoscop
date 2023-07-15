package com.aldarius.horoscop.timepicker

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.aldarius.horoscop.timepicker.view.MyTimePickerDialog
import com.aldarius.horoscop.timepicker.view.TimePicker
import java.util.*


class TimePickerActivity : Activity() {
    private var time: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateViews()
    }

    private fun updateViews() {
        time = findViewById<View>(R.id.time) as TextView?
    }

    fun showPicker(v: View) {
        val now = Calendar.getInstance()

        val mTimePicker = MyTimePickerDialog(this, addListener(),
            now[Calendar.HOUR_OF_DAY], now[Calendar.MINUTE], now[Calendar.SECOND], true)
        mTimePicker.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun addListener() : MyTimePickerDialog.OnTimeSetListener =
        object : MyTimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int) {
                time!!.text = getString(R.string.time,
                    String.format("%02d", hourOfDay), String.format("%02d", minute),
                        String.format("%02d", seconds))
            }
        }
}
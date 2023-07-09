package com.aldarius.timepicker

import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aldarius.timepicker.view.MyTimePickerDialog
import com.aldarius.timepicker.view.TimePicker
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.Test


@RunWith(AndroidJUnit4::class)
class MyTimePickerDialogTests {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.aldarius.timepicker", appContext.packageName)
    }

    @Test
    fun testSetTimeInTimePicker() {

        val activityScenario = ActivityScenario.launch(TimePickerActivity::class.java)
        lateinit var timeTextView: TextView
        lateinit var expectedText: String

        activityScenario.onActivity { activity ->
            activity.runOnUiThread {
                val now = Calendar.getInstance()
                val myTimePickerDialog = MyTimePickerDialog(
                    activity,
                    object : MyTimePickerDialog.OnTimeSetListener {
                        override fun onTimeSet(
                            view: TimePicker?,
                            hourOfDay: Int,
                            minute: Int,
                            seconds: Int
                        ) {
                            // Update the time text view
                            timeTextView = activity.findViewById(R.id.time)
                            timeTextView.text = "Time : $hourOfDay:$minute:$seconds"
                            expectedText = String.format("Time : %02d:%02d:%02d", hourOfDay, minute, seconds)
                        }
                    }, now[Calendar.HOUR_OF_DAY], now[Calendar.MINUTE], now[Calendar.SECOND], true
                )

                // Show the time picker
                myTimePickerDialog.show()

                // Set time in timepicker
                myTimePickerDialog.updateTime(now[Calendar.HOUR_OF_DAY],
                    now[Calendar.MINUTE],
                    now[Calendar.SECOND])

                // Confirm the time
                val bSetTimeButton = activity.findViewById<Button>(R.id.bSetTime)
                bSetTimeButton.performClick()

                // Wait for the dialog to be dismissed
                Thread.sleep(1000)

                // Check if the time result is displayed
                timeTextView = activity.findViewById(R.id.time)
                assertEquals(expectedText, timeTextView.text.toString())
            }
        }
    }
}
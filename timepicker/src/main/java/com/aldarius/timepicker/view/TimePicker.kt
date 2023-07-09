package com.aldarius.timepicker.view

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.aldarius.timepicker.R
import java.lang.String.format
import java.text.DateFormatSymbols
import java.util.*

/**
 * A view for selecting the time of day, in either 24 hour or AM/PM mode.
 *
 * The hour, each minute digit, each seconds digit, and AM/PM (if applicable) can be controlled by
 * vertical spinners.
 *
 * The hour can be entered by keyboard input.  Entering in two digit hours
 * can be accomplished by hitting two digits within a timeout of about a
 * second (e.g. '1' then '2' to select 12).
 *
 * The minutes can be entered by entering single digits.
 * The seconds can be entered by entering single digits.
 *
 * Under AM/PM mode, the user can hit 'a', 'A", 'p' or 'P' to pick.
 *
 * For a dialog using this view, see [android.app.TimePickerDialog].
 */
class TimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    // state
    var mCurrentHour = 0 // 0-23
    var mCurrentMinute = 0 // 0-59
    var currentSeconds = 0 // 0-59
    private var mIs24HourView = false
    private var mIsAm = false

    // ui components
    private val mHourPicker: NumberPicker
    private val mMinutePicker: NumberPicker
    private val mSecondPicker: NumberPicker
    private val mAmPmButton: Button
    private val mAmText: String
    private val mPmText: String

    // callbacks
    private var mOnTimeChangedListener: OnTimeChangedListener? = null

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    interface OnTimeChangedListener {
        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         * @param seconds The current second.
         */
        fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int)
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(
            R.layout.time_picker_widget,
            this,  // we are the parent
            true
        )

        // hour
        mHourPicker = findViewById<View>(R.id.hour) as NumberPicker
        mHourPicker.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            mCurrentHour = newVal
            if (!mIs24HourView) {
                // adjust from [1-12] to [0-11] internally, with the times
                // written "12:xx" being the start of the half-day
                if (mCurrentHour == 12) {
                    mCurrentHour = 0
                }
                if (!mIsAm) {
                    // PM means 12 hours later than nominal
                    mCurrentHour += 12
                }
            }
            onTimeChanged()
        }

        // digits of minute
        mMinutePicker = findViewById<View>(R.id.minute) as NumberPicker
        mMinutePicker.minValue = 0
        mMinutePicker.maxValue = 59
        mMinutePicker.setFormatter(TWO_DIGIT_FORMATTER)
        mMinutePicker.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            mCurrentMinute = newVal
            onTimeChanged()
        }

        // digits of seconds
        mSecondPicker = findViewById<View>(R.id.seconds) as NumberPicker
        mSecondPicker.minValue = 0
        mSecondPicker.maxValue = 59
        mSecondPicker.setFormatter(TWO_DIGIT_FORMATTER)
        mSecondPicker.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            currentSeconds = newVal
            onTimeChanged()
        }

        // am/pm
        mAmPmButton = findViewById<View>(R.id.amPm) as Button

        // now that the hour/minute picker objects have been initialized, set
        // the hour range properly based on the 12/24 hour display mode.
        configurePickerRanges()

        // initialize to current time
        val cal = Calendar.getInstance()
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER)

        // by default we're not in 24 hour mode
        setCurrentSecond(cal[Calendar.SECOND])
        mIsAm = mCurrentHour < 12

        /* Get the localized am/pm strings and use them in the spinner */
        val dfs = DateFormatSymbols()
        val dfsAmPm = dfs.amPmStrings
        mAmText = dfsAmPm[Calendar.AM]
        mPmText = dfsAmPm[Calendar.PM]
        mAmPmButton.text = if (mIsAm) mAmText else mPmText
        mAmPmButton.setOnClickListener {
            requestFocus()
            if (mIsAm) {

                // Currently AM switching to PM
                if (mCurrentHour < 12) {
                    mCurrentHour += 12
                }
            } else {

                // Currently PM switching to AM
                if (mCurrentHour >= 12) {
                    mCurrentHour -= 12
                }
            }
            mIsAm = !mIsAm
            mAmPmButton.text = if (mIsAm) mAmText else mPmText
            onTimeChanged()
        }
        if (!isEnabled) {
            isEnabled = false
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mMinutePicker.isEnabled = enabled
        mHourPicker.isEnabled = enabled
        mAmPmButton.isEnabled = enabled
    }

    /**
     * Used to save / restore state of time picker
     */
    private class SavedState(superState: Parcelable?, var hour: Int, var minute: Int) :
        BaseSavedState(superState) {

        /*
        private constructor(input: Parcel) : super(input) {
            hour = input.readInt()
            minute = input.readInt()
        }
        */


        fun getHour(): Int {
            return hour
        }

        fun getMinute(): Int {
            return minute
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(hour)
            dest.writeInt(minute)
        }

        /*
        companion object {
            @JvmField
            val CREATOR: Creator<SavedState?> = object : Creator<SavedState?> {
                override fun createFromParcel(input: Parcel): SavedState? {
                    return SavedState(input)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
        */

        override fun describeContents(): Int {
            return 0
        }

    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, mCurrentHour, mCurrentMinute)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        mCurrentHour = ss.getHour()
        mCurrentMinute = ss.getMinute()
        //mCurrentHour = ss.hour
        //mCurrentMinute = ss.minute
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     * @param onTimeChangedListener the callback, should not be null.
     */
    fun setOnTimeChangedListener(onTimeChangedListener: OnTimeChangedListener?) {
        mOnTimeChangedListener = onTimeChangedListener
    }
    /*
    /**
     * @return The current hour (0-23).
     */
    fun getCurrentHour(): Int {
        return mCurrentHour
    }
    */

    /**
     * Set the current hour.
     */
    fun setCurrentHour(currentHour: Int) {
        mCurrentHour = currentHour
        updateHourDisplay()
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    fun setIs24HourView(is24HourView: Boolean) {
        @Suppress("DEPRECATED_IDENTITY_EQUALS")
        if (mIs24HourView !== is24HourView) {
            mIs24HourView = is24HourView
            configurePickerRanges()
            updateHourDisplay()
        }
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    fun is24HourView(): Boolean {
        return mIs24HourView
    }

    /*
    /**
     * @return The current minute.
     */
    fun getCurrentMinute(): Int {
        return mCurrentMinute
    }
    */

    /**
     * Set the current minute (0-59).
     */
    fun setCurrentMinute(currentMinute: Int) {
        mCurrentMinute = currentMinute
        updateMinuteDisplay()
    }

    /**
     * Set the current second (0-59).
     */
    fun setCurrentSecond(currentSecond: Int) {
        currentSeconds = currentSecond
        updateSecondsDisplay()
    }

    override fun getBaseline(): Int {
        return mHourPicker.baseline
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private fun updateHourDisplay() {
        var currentHour = mCurrentHour
        if (!mIs24HourView) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour > 12) currentHour -= 12 else if (currentHour == 0) currentHour = 12
        }
        mHourPicker.value = currentHour
        mIsAm = mCurrentHour < 12
        mAmPmButton.text = if (mIsAm) mAmText else mPmText
        onTimeChanged()
    }

    private fun configurePickerRanges() {
        if (mIs24HourView) {
            mHourPicker.minValue = 0
            mHourPicker.maxValue = 23
            mHourPicker.setFormatter(TWO_DIGIT_FORMATTER)
            mAmPmButton.visibility = GONE
        } else {
            mHourPicker.minValue = 1
            mHourPicker.maxValue = 12
            mHourPicker.setFormatter(null)
            mAmPmButton.visibility = VISIBLE
        }
    }

    private fun onTimeChanged() {
        mOnTimeChangedListener!!.onTimeChanged(this, mCurrentHour, mCurrentMinute, currentSeconds)
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private fun updateMinuteDisplay() {
        mMinutePicker.value = mCurrentMinute
        mOnTimeChangedListener!!.onTimeChanged(this, mCurrentHour, mCurrentMinute, currentSeconds)
    }

    /**
     * Set the state of the spinners appropriate to the current second.
     */
    private fun updateSecondsDisplay() {
        mSecondPicker.value = currentSeconds
        mOnTimeChangedListener!!.onTimeChanged(this, mCurrentHour, mCurrentMinute, currentSeconds)
    }

    companion object {
        /**
         * A no-op callback used in the constructor to avoid null checks
         * later in the code.
         */
        private val NO_OP_CHANGE_LISTENER: OnTimeChangedListener =
            object : OnTimeChangedListener {
                override fun onTimeChanged(
                    view: TimePicker?,
                    hourOfDay: Int,
                    minute: Int,
                    seconds: Int
                ) {

                }
            }
        val TWO_DIGIT_FORMATTER =
            NumberPicker.Formatter { value: Int -> format("%02d", value) }
    }
}
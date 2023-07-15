package com.aldarius.horoscop.timepicker.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.aldarius.horoscop.timepicker.R
import java.lang.String.format

/**
 * A dialog that prompts the user for the time of day using a [TimePicker].
 */
class MyTimePickerDialog(
    context: Context,
    theme: Int,
    callBack: OnTimeSetListener?,
    hourOfDay: Int, minute: Int, seconds: Int, is24HourView: Boolean
) : AlertDialog(context, theme), DialogInterface.OnClickListener, TimePicker.OnTimeChangedListener {
    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    interface OnTimeSetListener {
        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int)
    }

    private val mTimePicker: TimePicker
    private val mCallback: OnTimeSetListener?
    private var mInitialHourOfDay: Int
    private var mInitialMinute: Int
    private var mInitialSeconds: Int
    private var mIs24HourView: Boolean

    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    constructor(
        context: Context,
        callBack: OnTimeSetListener?,
        hourOfDay: Int, minute: Int, seconds: Int, is24HourView: Boolean
    ) : this(
        context, 0,
        callBack, hourOfDay, minute, seconds, is24HourView
    )

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mCallback = callBack
        mInitialHourOfDay = hourOfDay
        mInitialMinute = minute
        mInitialSeconds = seconds
        mIs24HourView = is24HourView

        //java.text.DateFormat mDateFormat = DateFormat.getTimeFormat(context);
        //Calendar mCalendar = Calendar.getInstance();
        updateTitle(mInitialHourOfDay, mInitialMinute, mInitialSeconds)

        //setButton(context.getText(R.string.time_set), this);
        //setButton2(context.getText(R.string.cancel), (OnClickListener) null);
        //setIcon(android.R.drawable.ic_dialog_time);
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.time_picker_dialog, null)
        setView(view)
        mTimePicker = view.findViewById<View>(R.id.timePicker) as TimePicker

        // initialize state
        mTimePicker.mCurrentHour = mInitialHourOfDay
        mTimePicker.mCurrentMinute = mInitialMinute
        mTimePicker.setCurrentSecond(mInitialSeconds)
        mTimePicker.setIs24HourView(mIs24HourView)
        mTimePicker.setOnTimeChangedListener(this)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (mCallback != null) {
            mTimePicker.clearFocus()
            mCallback.onTimeSet(
                mTimePicker, mTimePicker.mCurrentHour,
                mTimePicker.mCurrentMinute, mTimePicker.currentSeconds
            )
        }
    }

    fun updateTime(hourOfDay: Int, minuteOfHour: Int, seconds: Int) {
        mTimePicker.setCurrentHour(hourOfDay)
        mTimePicker.setCurrentMinute(minuteOfHour)
        mTimePicker.setCurrentSecond(seconds)
    }

    private fun updateTitle(hour: Int, minute: Int, seconds: Int) {
        val sHour = format("%02d", hour)
        val sMin = format("%02d", minute)
        val sSec = format("%02d", seconds)
        setTitle("$sHour:$sMin:$sSec")
        mCallback?.onTimeSet(null, hour, minute, seconds)
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt(HOUR, mTimePicker.mCurrentHour)
        state.putInt(MINUTE, mTimePicker.mCurrentMinute)
        state.putInt(SECONDS, mTimePicker.currentSeconds)
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView())
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val hour = savedInstanceState.getInt(HOUR)
        val minute = savedInstanceState.getInt(MINUTE)
        val seconds = savedInstanceState.getInt(SECONDS)
        mTimePicker.mCurrentHour = hour
        mTimePicker.mCurrentMinute = minute
        mTimePicker.setCurrentSecond(seconds)
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR))
        mTimePicker.setOnTimeChangedListener(this)
        updateTitle(hour, minute, seconds)
    }

    companion object {
        private const val HOUR = "hour"
        private const val MINUTE = "minute"
        private const val SECONDS = "seconds"
        private const val IS_24_HOUR = "is24hour"
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int) {
        updateTitle(hourOfDay, minute, seconds)
    }
}
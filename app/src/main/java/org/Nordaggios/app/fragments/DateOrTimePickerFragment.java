/**
 *
 */
package org.Nordaggios.app.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.Nordaggios.app.interfaces.OnDateOrTimSetListener;

import java.util.Calendar;

/**
 * @author Touhid
 */
@SuppressLint("NewApi")
public class DateOrTimePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener,
        OnTimeSetListener
{

    public static final int DATE_PICKER = 0;
    public static final int TIME_PICKER = 1;

    private OnDateOrTimSetListener onDateOrTimeSetListener;
    private int type = 0;

    public static DateOrTimePickerFragment getInstance(OnDateOrTimSetListener oDoTSetListener, int type)
    {
        DateOrTimePickerFragment pickerFragment = new DateOrTimePickerFragment();
        pickerFragment.onDateOrTimeSetListener = oDoTSetListener;
        pickerFragment.type = type;
        return pickerFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of DatePickerDialog and return it
        if (type == DATE_PICKER)
            return new DatePickerDialog(getActivity(), this, year, month, day);
        else
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day)
    {
        // Do something with the date chosen by the user
        onDateOrTimeSetListener.dateOrTimeSet(year, month, day);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        onDateOrTimeSetListener.dateOrTimeSet(hourOfDay, minute, -1);
    }
}
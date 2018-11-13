package com.oppo.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.oppo.widget.OppoTimePicker;
import com.oppo.widget.OppoTimePicker.OnTimeChangedListener;

public class OppoTimePickerDialog extends AlertDialog implements OnClickListener, OnTimeChangedListener {
    private static final String HOUR = "hour";
    private static final String IS_24_HOUR = "is24hour";
    private static final String MINUTE = "minute";
    private final OnTimeSetListener mCallback;
    int mInitialHourOfDay;
    int mInitialMinute;
    boolean mIs24HourView;
    private final OppoTimePicker mOppoTimePicker;

    public interface OnTimeSetListener {
        void onTimeSet(OppoTimePicker oppoTimePicker, int i, int i2);
    }

    public OppoTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        this(context, 0, callBack, hourOfDay, minute, is24HourView);
    }

    public OppoTimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        super(context, theme);
        this.mCallback = callBack;
        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;
        this.mIs24HourView = is24HourView;
        setIcon(0);
        setTitle(201589835);
        Context themeContext = getContext();
        setButton(-1, themeContext.getText(201590054), this);
        setButton(-2, themeContext.getText(17039360), (OnClickListener) null);
        View view = ((LayoutInflater) themeContext.getSystemService("layout_inflater")).inflate(201917459, null);
        setView(view);
        this.mOppoTimePicker = (OppoTimePicker) view.findViewById(201458739);
        this.mOppoTimePicker.setIs24HourView(Boolean.valueOf(this.mIs24HourView));
        this.mOppoTimePicker.setCurrentHour(Integer.valueOf(this.mInitialHourOfDay));
        this.mOppoTimePicker.setCurrentMinute(Integer.valueOf(this.mInitialMinute));
        this.mOppoTimePicker.setOnTimeChangedListener(this);
        this.mOppoTimePicker.setTextVisibility(false);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCallback != null) {
            this.mOppoTimePicker.clearFocus();
            this.mCallback.onTimeSet(this.mOppoTimePicker, this.mOppoTimePicker.getCurrentHour().intValue(), this.mOppoTimePicker.getCurrentMinute().intValue());
        }
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        this.mOppoTimePicker.setCurrentHour(Integer.valueOf(hourOfDay));
        this.mOppoTimePicker.setCurrentMinute(Integer.valueOf(minutOfHour));
    }

    public void onTimeChanged(OppoTimePicker view, int hourOfDay, int minute) {
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, this.mOppoTimePicker.getCurrentHour().intValue());
        state.putInt(MINUTE, this.mOppoTimePicker.getCurrentMinute().intValue());
        state.putBoolean(IS_24_HOUR, this.mOppoTimePicker.is24HourView());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        this.mOppoTimePicker.setIs24HourView(Boolean.valueOf(savedInstanceState.getBoolean(IS_24_HOUR)));
        this.mOppoTimePicker.setCurrentHour(Integer.valueOf(hour));
        this.mOppoTimePicker.setCurrentMinute(Integer.valueOf(minute));
    }
}

package com.oppo.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.oppo.widget.OppoDatePicker;
import com.oppo.widget.OppoLunarDatePicker;
import com.oppo.widget.OppoLunarDatePicker.OnDateChangedListener;
import com.oppo.widget.OppoLunarUtil;

public class OppoDatePickerDialog extends AlertDialog implements OnClickListener, OnDateChangedListener, OppoDatePicker.OnDateChangedListener {
    private static final String DAY = "day";
    public static final int LUNAR_DEFAULT = 2;
    private static final String MONTH = "month";
    public static final int NO_BUTTON = 3;
    public static final int SOLAR_DEFAULT = 1;
    private static final String YEAR = "year";
    private final OnDateSetListener mCallBack;
    private OppoDatePicker mDatePicker;
    private boolean mIsLunarDate;
    private Button mLeftBtn;
    private final OppoLunarDatePicker mLunaDatePicker;
    private Button mRightBtn;
    private int mShowWhich;

    public interface OnDateSetListener {
        void onDateSet(OppoDatePicker oppoDatePicker, int i, int i2, int i3);

        void onLunarDateSet(OppoLunarDatePicker oppoLunarDatePicker, int i, int i2, int i3, boolean z);
    }

    public OppoDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, boolean isLunar) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth, isLunar, 3);
    }

    public OppoDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, boolean isLunar, int defaultShow) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth, isLunar, defaultShow);
    }

    public OppoDatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, boolean isLunar) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth, isLunar, 3);
    }

    public OppoDatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, boolean isLunar, int defaultShow) {
        super(context, theme);
        this.mIsLunarDate = isLunar;
        this.mShowWhich = defaultShow;
        this.mCallBack = callBack;
        Context themeContext = getContext();
        setButton(-1, themeContext.getText(201590054), this);
        setButton(-2, themeContext.getText(17039360), (OnClickListener) null);
        setIcon(0);
        setTitle(201589834);
        LinearLayout linearLayout = (LinearLayout) ((LayoutInflater) themeContext.getSystemService("layout_inflater")).inflate(201917455, null);
        this.mLeftBtn = (Button) linearLayout.findViewById(201458718);
        this.mRightBtn = (Button) linearLayout.findViewById(201458719);
        this.mRightBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int[] timeArray = new int[3];
                timeArray = OppoDatePickerDialog.this.mLunaDatePicker.getLunarDate();
                if (8 == OppoDatePickerDialog.this.mDatePicker.getVisibility()) {
                    OppoDatePickerDialog.this.mDatePicker.updateDate(OppoDatePickerDialog.this.mLunaDatePicker.getYear(), OppoDatePickerDialog.this.mLunaDatePicker.getMonth(), OppoDatePickerDialog.this.mLunaDatePicker.getDayOfMonth());
                    OppoDatePickerDialog.this.mLunaDatePicker.setVisibility(8);
                    OppoDatePickerDialog.this.mDatePicker.setVisibility(0);
                }
                OppoDatePickerDialog.this.mLeftBtn.setVisibility(0);
                OppoDatePickerDialog.this.mRightBtn.setVisibility(8);
            }
        });
        this.mLeftBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (8 == OppoDatePickerDialog.this.mLunaDatePicker.getVisibility()) {
                    OppoDatePickerDialog.this.mLunaDatePicker.updateDate(OppoDatePickerDialog.this.mDatePicker.getYear(), OppoDatePickerDialog.this.mDatePicker.getMonth(), OppoDatePickerDialog.this.mDatePicker.getDayOfMonth());
                    OppoDatePickerDialog.this.mLunaDatePicker.setVisibility(0);
                    OppoDatePickerDialog.this.mDatePicker.setVisibility(8);
                }
                OppoDatePickerDialog.this.mRightBtn.setVisibility(0);
                OppoDatePickerDialog.this.mLeftBtn.setVisibility(8);
            }
        });
        this.mLunaDatePicker = new OppoLunarDatePicker(getContext());
        this.mLunaDatePicker.setCalendarViewShown(false);
        LayoutParams p = new LayoutParams(-1, -2);
        linearLayout.addView(this.mLunaDatePicker, p);
        this.mDatePicker = new OppoDatePicker(getContext());
        this.mDatePicker.setCalendarViewShown(false);
        linearLayout.addView(this.mDatePicker, p);
        this.mLunaDatePicker.setVisibility(8);
        if (!this.mIsLunarDate) {
            this.mLeftBtn.setVisibility(8);
            this.mRightBtn.setVisibility(8);
        } else if (this.mShowWhich == 3) {
            this.mLeftBtn.setVisibility(8);
            this.mRightBtn.setVisibility(8);
            this.mLunaDatePicker.setVisibility(0);
            this.mDatePicker.setVisibility(8);
        } else if (this.mShowWhich == 2) {
            this.mLeftBtn.setVisibility(8);
            this.mRightBtn.setVisibility(0);
            this.mLunaDatePicker.setVisibility(0);
            this.mDatePicker.setVisibility(8);
        } else if (this.mShowWhich == 1) {
            this.mLeftBtn.setVisibility(0);
            this.mRightBtn.setVisibility(8);
            this.mLunaDatePicker.setVisibility(8);
            this.mDatePicker.setVisibility(0);
        }
        setView(linearLayout);
        this.mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        this.mLunaDatePicker.init(year, monthOfYear, dayOfMonth, this);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCallBack != null) {
            this.mLunaDatePicker.clearFocus();
            this.mDatePicker.clearFocus();
            int[] lunarDate = OppoLunarUtil.calculateLunarByGregorian(this.mLunaDatePicker.getYear(), this.mLunaDatePicker.getMonth() + 1, this.mLunaDatePicker.getDayOfMonth());
            if (this.mLunaDatePicker.getVisibility() == 0) {
                this.mCallBack.onLunarDateSet(this.mLunaDatePicker, lunarDate[0], lunarDate[1], lunarDate[2], this.mLunaDatePicker.isLeapMonth(lunarDate[1]));
            } else {
                this.mCallBack.onDateSet(this.mDatePicker, this.mDatePicker.getYear(), this.mDatePicker.getMonth(), this.mDatePicker.getDayOfMonth());
            }
        }
    }

    public void onLunarDateChanged(OppoLunarDatePicker view, int year, int month, int day) {
        this.mLunaDatePicker.init(year, month, day, null);
    }

    public void onDateChanged(OppoDatePicker view, int year, int month, int day) {
        this.mDatePicker.init(year, month, day, null);
    }

    public OppoLunarDatePicker getLunarDatePicker() {
        return this.mLunaDatePicker;
    }

    public OppoDatePicker getDatePicker() {
        return this.mDatePicker;
    }

    public void updateLunarDate(int year, int monthOfYear, int dayOfMonth) {
        this.mLunaDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        this.mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, this.mLunaDatePicker.getYear());
        state.putInt(MONTH, this.mLunaDatePicker.getMonth());
        state.putInt(DAY, this.mLunaDatePicker.getDayOfMonth());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        this.mLunaDatePicker.init(year, month, day, this);
        this.mDatePicker.init(year, month, day, this);
    }
}

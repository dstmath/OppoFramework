package com.oppo.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View.BaseSavedState;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.color.util.ColorAccessibilityUtil;
import com.oppo.widget.OppoNumberPicker.OnValueChangeListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class OppoLunarDatePicker extends FrameLayout {
    private static final boolean DEFAULT_CALENDAR_VIEW_SHOWN = true;
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int DEFAULT_END_YEAR = 2036;
    private static final boolean DEFAULT_SPINNERS_SHOWN = true;
    private static final int DEFAULT_START_YEAR = 1910;
    private static final int PICKER_CHILD_COUNT = 3;
    private static final String TAG = OppoLunarDatePicker.class.getSimpleName();
    private static String leapString;
    static final String[] sChineseNumber = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    private static Calendar sMaxDate = Calendar.getInstance();
    private static Calendar sMinDate = Calendar.getInstance();
    private Calendar mCurrentDate;
    private Locale mCurrentLocale;
    private final OppoNumberPicker mDaySpinner;
    private final EditText mDaySpinnerInput;
    private boolean mIsEnabled;
    private final OppoNumberPicker mMonthSpinner;
    private final EditText mMonthSpinnerInput;
    private int mNumberOfMonths;
    private OnDateChangedListener mOnDateChangedListener;
    private String[] mShortMonths;
    private final LinearLayout mSpinners;
    private Calendar mTempDate;
    private final OppoNumberPicker mYearSpinner;
    private final EditText mYearSpinnerInput;

    public interface OnDateChangedListener {
        void onLunarDateChanged(OppoLunarDatePicker oppoLunarDatePicker, int i, int i2, int i3);
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mDay;
        private final int mMonth;
        private final int mYear;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        /* synthetic */ SavedState(Parcelable superState, int year, int month, int day, SavedState -this4) {
            this(superState, year, month, day);
        }

        private SavedState(Parcelable superState, int year, int month, int day) {
            super(superState);
            this.mYear = year;
            this.mMonth = month;
            this.mDay = day;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mYear = in.readInt();
            this.mMonth = in.readInt();
            this.mDay = in.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mYear);
            dest.writeInt(this.mMonth);
            dest.writeInt(this.mDay);
        }
    }

    static {
        sMinDate.set(DEFAULT_START_YEAR, 0, 1, 0, 0);
        sMaxDate.set(DEFAULT_END_YEAR, 11, 31, 23, 59);
    }

    public OppoLunarDatePicker(Context context) {
        this(context, null);
    }

    public OppoLunarDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 16843612);
    }

    public OppoLunarDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mNumberOfMonths = 12;
        this.mIsEnabled = true;
        setCurrentLocale(Locale.getDefault());
        this.mShortMonths = getResources().getStringArray(201786392);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(201917442, this, true);
        leapString = getResources().getString(201590129);
        OnValueChangeListener onChangeListener = new OnValueChangeListener() {
            public void onValueChange(OppoNumberPicker picker, int oldVal, int newVal) {
                OppoLunarDatePicker.this.updateInputState();
                OppoLunarDatePicker.this.mTempDate.setTimeInMillis(OppoLunarDatePicker.this.mCurrentDate.getTimeInMillis());
                int[] lunarDates = OppoLunarUtil.calculateLunarByGregorian(OppoLunarDatePicker.this.mTempDate.get(1), OppoLunarDatePicker.this.mTempDate.get(2) + 1, OppoLunarDatePicker.this.mTempDate.get(5));
                if (picker == OppoLunarDatePicker.this.mDaySpinner) {
                    OppoLunarDatePicker.this.mTempDate.add(5, newVal - oldVal);
                } else if (picker == OppoLunarDatePicker.this.mMonthSpinner) {
                    int monthCountDays;
                    if (oldVal > 10 && newVal == 0) {
                        if (OppoLunarUtil.leapMonth(lunarDates[0]) == 12) {
                            monthCountDays = OppoLunarUtil.daysOfLeapMonthInLunarYear(lunarDates[0]);
                        } else {
                            monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDates[0], 12);
                        }
                        OppoLunarDatePicker.this.mTempDate.add(5, monthCountDays - OppoLunarUtil.daysOfLunarYear(lunarDates[0]));
                    } else if (oldVal != 0 || newVal <= 10) {
                        int leapMonth = OppoLunarUtil.leapMonth(lunarDates[0]);
                        if (newVal - oldVal < 0) {
                            if (leapMonth == 0) {
                                monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDates[0], newVal + 1);
                            } else if (newVal < leapMonth) {
                                monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDates[0], newVal + 1);
                            } else if (newVal == leapMonth) {
                                monthCountDays = OppoLunarUtil.daysOfLeapMonthInLunarYear(lunarDates[0]);
                            } else {
                                monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDates[0], newVal);
                            }
                            monthCountDays = -monthCountDays;
                        } else if (leapMonth == 0) {
                            monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDates[0], oldVal + 1);
                        } else if (oldVal < leapMonth) {
                            monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDates[0], oldVal + 1);
                        } else if (oldVal == leapMonth) {
                            monthCountDays = OppoLunarUtil.daysOfLeapMonthInLunarYear(lunarDates[0]);
                        } else {
                            monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDates[0], oldVal);
                        }
                        OppoLunarDatePicker.this.mTempDate.add(5, monthCountDays);
                    } else {
                        if (OppoLunarUtil.leapMonth(lunarDates[0]) == 12) {
                            monthCountDays = OppoLunarUtil.daysOfLeapMonthInLunarYear(lunarDates[0]);
                        } else {
                            monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDates[0], 12);
                        }
                        OppoLunarDatePicker.this.mTempDate.add(5, OppoLunarUtil.daysOfLunarYear(lunarDates[0]) - monthCountDays);
                    }
                } else if (picker == OppoLunarDatePicker.this.mYearSpinner) {
                    OppoLunarDatePicker.this.mTempDate = OppoLunarUtil.changeALunarYear(OppoLunarDatePicker.this.mTempDate, lunarDates[1], lunarDates[2], lunarDates[3], oldVal, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                OppoLunarDatePicker.this.setDate(OppoLunarDatePicker.this.mTempDate.get(1), OppoLunarDatePicker.this.mTempDate.get(2), OppoLunarDatePicker.this.mTempDate.get(5));
                OppoLunarDatePicker.this.updateSpinners();
                OppoLunarDatePicker.this.updateCalendarView();
                OppoLunarDatePicker.this.notifyDateChanged();
            }
        };
        this.mSpinners = (LinearLayout) findViewById(201458734);
        this.mDaySpinner = (OppoNumberPicker) findViewById(201458736);
        this.mDaySpinner.setOnLongPressUpdateInterval(100);
        this.mDaySpinner.setOnValueChangedListener(onChangeListener);
        if (this.mDaySpinner.getChildCount() == 3) {
            this.mDaySpinnerInput = (EditText) this.mDaySpinner.getChildAt(1);
            this.mDaySpinnerInput.setClickable(false);
            this.mDaySpinnerInput.setFocusable(false);
        } else {
            this.mDaySpinnerInput = new EditText(context);
            Log.e(TAG, "mDaySpinner.getChildCount() != 3,It isn't init ok.");
        }
        this.mMonthSpinner = (OppoNumberPicker) findViewById(201458735);
        this.mMonthSpinner.setMinValue(0);
        this.mMonthSpinner.setMaxValue(this.mNumberOfMonths - 1);
        this.mMonthSpinner.setDisplayedValues(this.mShortMonths);
        this.mMonthSpinner.setOnLongPressUpdateInterval(200);
        this.mMonthSpinner.setOnValueChangedListener(onChangeListener);
        if (this.mMonthSpinner.getChildCount() == 3) {
            this.mMonthSpinnerInput = (EditText) this.mMonthSpinner.getChildAt(1);
            this.mMonthSpinnerInput.setClickable(false);
            this.mMonthSpinnerInput.setFocusable(false);
        } else {
            this.mMonthSpinnerInput = new EditText(context);
            Log.e(TAG, "mMonthSpinner.getChildCount() != 3,It isn't init ok.");
        }
        this.mYearSpinner = (OppoNumberPicker) findViewById(201458737);
        this.mYearSpinner.setOnLongPressUpdateInterval(100);
        this.mYearSpinner.setOnValueChangedListener(onChangeListener);
        if (this.mYearSpinner.getChildCount() == 3) {
            this.mYearSpinnerInput = (EditText) this.mYearSpinner.getChildAt(1);
            this.mYearSpinnerInput.setClickable(false);
            this.mYearSpinnerInput.setFocusable(false);
        } else {
            this.mYearSpinnerInput = new EditText(context);
            Log.e(TAG, "mYearSpinner.getChildCount() != 3,It isn't init ok.");
        }
        setSpinnersShown(true);
        setCalendarViewShown(true);
        this.mTempDate.clear();
        this.mTempDate.set(DEFAULT_START_YEAR, 0, 1);
        setMinDate(this.mTempDate.getTimeInMillis());
        this.mTempDate.clear();
        this.mTempDate.set(DEFAULT_END_YEAR, 11, 31, 23, 59);
        setMaxDate(this.mTempDate.getTimeInMillis());
        this.mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(this.mCurrentDate.get(1), this.mCurrentDate.get(2), this.mCurrentDate.get(5), null);
        if (((AccessibilityManager) context.getSystemService("accessibility")).isEnabled()) {
            setContentDescriptions();
        }
    }

    public long getMinDate() {
        return sMinDate.getTimeInMillis();
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) != sMinDate.get(1) || this.mTempDate.get(6) == sMinDate.get(6)) {
            sMinDate.setTimeInMillis(minDate);
            if (this.mCurrentDate.before(sMinDate)) {
                this.mCurrentDate.setTimeInMillis(sMinDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
            return;
        }
        Log.w(TAG, "setMinDate failed!:" + this.mTempDate.get(1) + "<->" + sMinDate.get(1) + ":" + this.mTempDate.get(6) + "<->" + sMinDate.get(6));
    }

    public long getMaxDate() {
        return sMaxDate.getTimeInMillis();
    }

    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) != sMaxDate.get(1) || this.mTempDate.get(6) == sMaxDate.get(6)) {
            sMaxDate.setTimeInMillis(maxDate);
            if (this.mCurrentDate.after(sMaxDate)) {
                this.mCurrentDate.setTimeInMillis(sMaxDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
            return;
        }
        Log.w(TAG, "setMaxDate failed!:" + this.mTempDate.get(1) + "<->" + sMaxDate.get(1) + ":" + this.mTempDate.get(6) + "<->" + sMaxDate.get(6));
    }

    public void setEnabled(boolean enabled) {
        if (this.mIsEnabled != enabled) {
            super.setEnabled(enabled);
            this.mDaySpinner.setEnabled(enabled);
            this.mMonthSpinner.setEnabled(enabled);
            this.mYearSpinner.setEnabled(enabled);
            this.mIsEnabled = enabled;
        }
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        event.getText().add(DateUtils.formatDateTime(getContext(), this.mCurrentDate.getTimeInMillis(), 20));
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    public boolean getCalendarViewShown() {
        return false;
    }

    public CalendarView getCalendarView() {
        return null;
    }

    public void setCalendarViewShown(boolean shown) {
    }

    public boolean getSpinnersShown() {
        return this.mSpinners.isShown();
    }

    public void setSpinnersShown(boolean shown) {
        this.mSpinners.setVisibility(shown ? 0 : 8);
    }

    private void setCurrentLocale(Locale locale) {
        if (!locale.equals(this.mCurrentLocale)) {
            this.mCurrentLocale = locale;
            this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
            sMinDate = getCalendarForLocale(sMinDate, locale);
            sMaxDate = getCalendarForLocale(sMaxDate, locale);
            this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
        }
    }

    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        }
        long currentTimeMillis = oldCalendar.getTimeInMillis();
        Calendar newCalendar = Calendar.getInstance(locale);
        newCalendar.setTimeInMillis(currentTimeMillis);
        return newCalendar;
    }

    private void reorderSpinners() {
        this.mSpinners.removeAllViews();
        char[] order = DateFormat.getDateFormatOrder(getContext());
        int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case 'M':
                    this.mSpinners.addView(this.mMonthSpinner);
                    setImeOptions(this.mMonthSpinner, spinnerCount, i);
                    break;
                case 'd':
                    this.mSpinners.addView(this.mDaySpinner);
                    setImeOptions(this.mDaySpinner, spinnerCount, i);
                    break;
                case 'y':
                    this.mSpinners.addView(this.mYearSpinner);
                    setImeOptions(this.mYearSpinner, spinnerCount, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        if (isNewDate(year, month, dayOfMonth)) {
            setDate(year, month, dayOfMonth);
            updateSpinners();
            updateCalendarView();
            notifyDateChanged();
        }
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getYear(), getMonth(), getDayOfMonth(), null);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateSpinners();
        updateCalendarView();
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners();
        updateCalendarView();
        this.mOnDateChangedListener = onDateChangedListener;
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        if (this.mCurrentDate.get(1) == year && this.mCurrentDate.get(2) == dayOfMonth && this.mCurrentDate.get(5) == month) {
            return false;
        }
        return true;
    }

    private void setDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(year, month, dayOfMonth);
        if (this.mCurrentDate.before(sMinDate)) {
            this.mCurrentDate.setTimeInMillis(sMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(sMaxDate)) {
            this.mCurrentDate.setTimeInMillis(sMaxDate.getTimeInMillis());
        }
    }

    private void updateSpinners() {
        int i;
        boolean isLeapYear = false;
        int[] lunarDate = OppoLunarUtil.calculateLunarByGregorian(this.mCurrentDate.get(1), this.mCurrentDate.get(2) + 1, this.mCurrentDate.get(5));
        int leapMonth = OppoLunarUtil.leapMonth(lunarDate[0]);
        int monthIndexDisplay = lunarDate[1];
        String lunarDateString = getLunarDateString(this.mCurrentDate);
        if (leapMonth == 0) {
            monthIndexDisplay--;
        } else if (monthIndexDisplay < leapMonth && leapMonth != 0) {
            monthIndexDisplay--;
        } else if (monthIndexDisplay == leapMonth && (lunarDateString.contains(leapString) ^ 1) != 0) {
            monthIndexDisplay--;
        }
        if (leapMonth != 0) {
            this.mNumberOfMonths = 13;
            isLeapYear = true;
        } else {
            this.mNumberOfMonths = 12;
        }
        int monthCountDays = OppoLunarUtil.daysOfALunarMonth(lunarDate[0], lunarDate[1]);
        if (leapMonth != 0 && monthIndexDisplay == leapMonth && lunarDateString.contains(leapString)) {
            monthCountDays = OppoLunarUtil.daysOfLeapMonthInLunarYear(lunarDate[0]);
        }
        if (this.mCurrentDate.equals(sMinDate)) {
            this.mDaySpinner.setDisplayedValues(null);
            this.mDaySpinner.setMinValue(lunarDate[2]);
            this.mDaySpinner.setMaxValue(monthCountDays);
            this.mDaySpinner.setWrapSelectorWheel(false);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(monthIndexDisplay);
            this.mMonthSpinner.setMaxValue(this.mNumberOfMonths - 1);
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else if (this.mCurrentDate.equals(sMaxDate)) {
            this.mDaySpinner.setDisplayedValues(null);
            this.mDaySpinner.setMinValue(1);
            this.mDaySpinner.setMaxValue(lunarDate[2]);
            this.mDaySpinner.setWrapSelectorWheel(false);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(0);
            this.mMonthSpinner.setMaxValue(monthIndexDisplay);
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else {
            this.mDaySpinner.setDisplayedValues(null);
            this.mDaySpinner.setMinValue(1);
            this.mDaySpinner.setMaxValue(monthCountDays);
            this.mDaySpinner.setWrapSelectorWheel(true);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(0);
            this.mMonthSpinner.setMaxValue(this.mNumberOfMonths - 1);
            this.mMonthSpinner.setWrapSelectorWheel(true);
        }
        String[] displayedMonths = new String[this.mNumberOfMonths];
        if (isLeapYear) {
            i = 0;
            while (i < leapMonth) {
                displayedMonths[i] = this.mShortMonths[i];
                i++;
            }
            displayedMonths[leapMonth] = leapString + this.mShortMonths[leapMonth - 1];
            while (true) {
                i++;
                if (i >= 13) {
                    break;
                }
                displayedMonths[i] = this.mShortMonths[i - 1];
            }
        } else {
            displayedMonths = (String[]) Arrays.copyOfRange(this.mShortMonths, this.mMonthSpinner.getMinValue(), this.mMonthSpinner.getMaxValue() + 1);
        }
        this.mMonthSpinner.setDisplayedValues(displayedMonths);
        int max = this.mDaySpinner.getMaxValue();
        int min = this.mDaySpinner.getMinValue();
        String[] displayedDays = new String[((max - min) + 1)];
        for (i = min; i <= max; i++) {
            displayedDays[i - min] = OppoLunarUtil.chneseStringOfALunarDay(i);
        }
        this.mDaySpinner.setDisplayedValues(displayedDays);
        int[] minLunarDate = OppoLunarUtil.calculateLunarByGregorian(sMinDate.get(1), sMinDate.get(2) + 1, sMinDate.get(5));
        int maxGregorianMonth = sMaxDate.get(2) + 1;
        int[] maxLunarDate = OppoLunarUtil.calculateLunarByGregorian(sMaxDate.get(1), maxGregorianMonth, maxGregorianMonth);
        this.mYearSpinner.setMinValue(minLunarDate[0]);
        this.mYearSpinner.setMaxValue(maxLunarDate[0]);
        this.mYearSpinner.setWrapSelectorWheel(true);
        this.mYearSpinner.setValue(lunarDate[0]);
        this.mMonthSpinner.setValue(monthIndexDisplay);
        this.mDaySpinner.setValue(lunarDate[2]);
        if (ColorAccessibilityUtil.isTalkbackEnabled(this.mContext)) {
            announceForAccessibility(lunarDate[0] + "年" + (lunarDate[3] == 0 ? leapString : "") + this.mShortMonths[lunarDate[1] - 1] + OppoLunarUtil.chneseStringOfALunarDay(lunarDate[2]));
        }
    }

    private void updateCalendarView() {
    }

    public int getYear() {
        return this.mCurrentDate.get(1);
    }

    public int getMonth() {
        return this.mCurrentDate.get(2);
    }

    public static String getLunarDateString(Calendar cal) {
        int[] lunarDate = OppoLunarUtil.calculateLunarByGregorian(cal.get(1), cal.get(2) + 1, cal.get(5));
        return getLunarDateString(lunarDate[0], lunarDate[1], lunarDate[2], lunarDate[3]);
    }

    private static String getLunarDateString(int lunarYear, int lunarMonth, int LunarDay, int leapMonthCode) {
        return lunarYear + "年" + (leapMonthCode == 0 ? leapString : "") + sChineseNumber[lunarMonth - 1] + "月" + OppoLunarUtil.chneseStringOfALunarDay(LunarDay);
    }

    public int getDayOfMonth() {
        return this.mCurrentDate.get(5);
    }

    private void notifyDateChanged() {
        sendAccessibilityEvent(4);
        if (this.mOnDateChangedListener != null) {
            this.mOnDateChangedListener.onLunarDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    private void setImeOptions(OppoNumberPicker spinner, int spinnerCount, int spinnerIndex) {
        int imeOptions;
        if (spinnerIndex < spinnerCount - 1) {
            imeOptions = 5;
        } else {
            imeOptions = 6;
        }
        if (spinner.getChildCount() != 3) {
            Log.e(TAG, "spinner.getChildCount() != 3,It isn't init ok.return");
        } else {
            ((TextView) spinner.getChildAt(1)).setImeOptions(imeOptions);
        }
    }

    private void setContentDescriptions() {
        Context context = getContext();
        if (this.mDaySpinner.getChildCount() != 3) {
            Log.e(TAG, "mDaySpinner.getChildCount() != 3,It isn't init ok.return");
        } else if (this.mMonthSpinner.getChildCount() != 3) {
            Log.e(TAG, "mMonthSpinner.getChildCount() != 3,It isn't init ok.return");
        } else if (this.mYearSpinner.getChildCount() != 3) {
            Log.e(TAG, "mYearSpinner.getChildCount() != 3,It isn't init ok.return");
        } else {
            ((ImageButton) this.mDaySpinner.getChildAt(0)).setContentDescription(context.getString(201589843));
            ((ImageButton) this.mDaySpinner.getChildAt(2)).setContentDescription(context.getString(201589844));
            ((ImageButton) this.mMonthSpinner.getChildAt(0)).setContentDescription(context.getString(201589845));
            ((ImageButton) this.mMonthSpinner.getChildAt(2)).setContentDescription(context.getString(201589846));
            ((ImageButton) this.mYearSpinner.getChildAt(0)).setContentDescription(context.getString(201589847));
            ((ImageButton) this.mYearSpinner.getChildAt(2)).setContentDescription(context.getString(201589848));
        }
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        if (inputMethodManager == null) {
            return;
        }
        if (inputMethodManager.isActive(this.mYearSpinnerInput)) {
            this.mYearSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mMonthSpinnerInput)) {
            this.mMonthSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mDaySpinnerInput)) {
            this.mDaySpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    public int[] getLunarDate() {
        return OppoLunarUtil.calculateLunarByGregorian(this.mCurrentDate.get(1), this.mCurrentDate.get(2) + 1, this.mCurrentDate.get(5));
    }

    public boolean isLeapMonth(int month) {
        return month == OppoLunarUtil.leapMonth(this.mCurrentDate.get(1));
    }

    public int getLeapMonth() {
        return OppoLunarUtil.leapMonth(this.mCurrentDate.get(1));
    }
}

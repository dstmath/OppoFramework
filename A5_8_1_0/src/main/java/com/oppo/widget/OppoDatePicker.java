package com.oppo.widget;

import android.R;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.color.util.ColorAccessibilityUtil;
import com.oppo.widget.OppoNumberPicker.OnValueChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class OppoDatePicker extends FrameLayout {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final boolean DEFAULT_CALENDAR_VIEW_SHOWN = true;
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final boolean DEFAULT_SPINNERS_SHOWN = true;
    private static final int DEFAULT_START_YEAR = 1900;
    private static final String LOG_TAG = OppoDatePicker.class.getSimpleName();
    private static char[] orderEn = new char[]{'d', 'M', 'y'};
    private Calendar mCurrentDate;
    private Locale mCurrentLocale;
    private final DateFormat mDateFormat;
    private final OppoNumberPicker mDaySpinner;
    private final EditText mDaySpinnerInput;
    private String mDayString;
    private TextView mDayText;
    private boolean mIsChinese;
    private boolean mIsEnabled;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private final OppoNumberPicker mMonthSpinner;
    private final EditText mMonthSpinnerInput;
    private TextView mMonthText;
    private int mNumberOfMonths;
    private OnDateChangedListener mOnDateChangedListener;
    private int mPickerPadding;
    private String[] mShortMonths;
    private final LinearLayout mSpinners;
    private Calendar mTempDate;
    private final OppoNumberPicker mYearSpinner;
    private final EditText mYearSpinnerInput;
    private String mYearString;
    private TextView mYearText;

    public interface OnDateChangedListener {
        void onDateChanged(OppoDatePicker oppoDatePicker, int i, int i2, int i3);
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

    public OppoDatePicker(Context context) {
        this(context, null);
    }

    public OppoDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 201393158);
    }

    public OppoDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        this.mIsEnabled = true;
        this.mIsChinese = false;
        setCurrentLocale(Locale.getDefault());
        this.mIsChinese = isChineseLanguage();
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyle, 0);
        boolean spinnersShown = attributesArray.getBoolean(6, true);
        boolean calendarViewShown = attributesArray.getBoolean(7, true);
        int startYear = attributesArray.getInt(1, DEFAULT_START_YEAR);
        int endYear = attributesArray.getInt(2, DEFAULT_END_YEAR);
        String minDate = attributesArray.getString(4);
        String maxDate = attributesArray.getString(5);
        this.mShortMonths = getResources().getStringArray(201786393);
        this.mYearString = getResources().getString(201590130);
        this.mDayString = getResources().getString(201590131);
        attributesArray.recycle();
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(201917441, this, true);
        OnValueChangeListener onChangeListener = new OnValueChangeListener() {
            public void onValueChange(OppoNumberPicker picker, int oldVal, int newVal) {
                OppoDatePicker.this.updateInputState();
                OppoDatePicker.this.mTempDate.setTimeInMillis(OppoDatePicker.this.mCurrentDate.getTimeInMillis());
                if (picker == OppoDatePicker.this.mDaySpinner) {
                    int maxDayOfMonth = OppoDatePicker.this.mTempDate.getActualMaximum(5);
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        OppoDatePicker.this.mTempDate.set(5, 1);
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        OppoDatePicker.this.mTempDate.set(5, maxDayOfMonth);
                    } else {
                        OppoDatePicker.this.mTempDate.add(5, newVal - oldVal);
                    }
                } else if (picker == OppoDatePicker.this.mMonthSpinner) {
                    if (oldVal == 11 && newVal == 0) {
                        OppoDatePicker.this.mTempDate.set(2, 0);
                    } else if (oldVal == 0 && newVal == 11) {
                        OppoDatePicker.this.mTempDate.set(2, 11);
                    } else {
                        OppoDatePicker.this.mTempDate.add(2, newVal - oldVal);
                    }
                } else if (picker == OppoDatePicker.this.mYearSpinner) {
                    OppoDatePicker.this.mTempDate.set(1, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                OppoDatePicker.this.setDate(OppoDatePicker.this.mTempDate.get(1), OppoDatePicker.this.mTempDate.get(2), OppoDatePicker.this.mTempDate.get(5));
                OppoDatePicker.this.updateSpinners();
                OppoDatePicker.this.updateCalendarView();
                OppoDatePicker.this.notifyDateChanged();
                if (ColorAccessibilityUtil.isTalkbackEnabled(OppoDatePicker.this.mContext)) {
                    OppoDatePicker.this.announceForAccessibility(OppoDatePicker.this.mDateFormat.format(OppoDatePicker.this.mTempDate.getTime()));
                }
            }
        };
        this.mSpinners = (LinearLayout) findViewById(201458734);
        this.mYearText = (TextView) findViewById(201458906);
        this.mMonthText = (TextView) findViewById(201458904);
        this.mDayText = (TextView) findViewById(201458905);
        this.mDaySpinner = (OppoNumberPicker) findViewById(201458736);
        this.mDaySpinner.setFormatter(OppoNumberPicker.TWO_DIGIT_FORMATTER);
        this.mDaySpinner.setOnLongPressUpdateInterval(100);
        this.mDaySpinner.setOnValueChangedListener(onChangeListener);
        this.mDaySpinnerInput = (EditText) this.mDaySpinner.findViewById(201458728);
        this.mMonthSpinner = (OppoNumberPicker) findViewById(201458735);
        this.mMonthSpinner.setMinValue(0);
        this.mMonthSpinner.setMaxValue(this.mNumberOfMonths - 1);
        this.mMonthSpinner.setDisplayedValues(this.mShortMonths);
        this.mMonthSpinner.setOnLongPressUpdateInterval(200);
        this.mMonthSpinner.setOnValueChangedListener(onChangeListener);
        this.mMonthSpinnerInput = (EditText) this.mMonthSpinner.findViewById(201458728);
        this.mYearSpinner = (OppoNumberPicker) findViewById(201458737);
        this.mYearSpinner.setOnLongPressUpdateInterval(100);
        this.mYearSpinner.setOnValueChangedListener(onChangeListener);
        this.mYearSpinnerInput = (EditText) this.mYearSpinner.findViewById(201458728);
        if (spinnersShown || (calendarViewShown ^ 1) == 0) {
            setSpinnersShown(spinnersShown);
            setCalendarViewShown(calendarViewShown);
        } else {
            setSpinnersShown(true);
        }
        this.mTempDate.clear();
        if (TextUtils.isEmpty(minDate)) {
            this.mTempDate.set(startYear, 0, 1);
        } else {
            if (!parseDate(minDate, this.mTempDate)) {
                this.mTempDate.set(startYear, 0, 1);
            }
        }
        setMinDate(this.mTempDate.getTimeInMillis());
        this.mTempDate.clear();
        if (TextUtils.isEmpty(maxDate)) {
            this.mTempDate.set(endYear, 11, 31);
        } else {
            if (!parseDate(maxDate, this.mTempDate)) {
                this.mTempDate.set(endYear, 11, 31);
            }
        }
        setMaxDate(this.mTempDate.getTimeInMillis());
        this.mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(this.mCurrentDate.get(1), this.mCurrentDate.get(2), this.mCurrentDate.get(5), null);
        reorderSpinners();
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            setContentDescriptions();
        }
    }

    public long getMinDate() {
        return this.mMinDate.getTimeInMillis();
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) != this.mMinDate.get(1) || this.mTempDate.get(6) == this.mMinDate.get(6)) {
            this.mMinDate.setTimeInMillis(minDate);
            if (this.mCurrentDate.before(this.mMinDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
        }
    }

    public long getMaxDate() {
        return this.mMaxDate.getTimeInMillis();
    }

    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) != this.mMaxDate.get(1) || this.mTempDate.get(6) == this.mMaxDate.get(6)) {
            this.mMaxDate.setTimeInMillis(maxDate);
            if (this.mCurrentDate.after(this.mMaxDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
        }
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
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mCurrentDate.getTimeInMillis(), 20));
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
            this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
            this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
            this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
            this.mNumberOfMonths = this.mTempDate.getActualMaximum(2) + 1;
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
        char[] order = android.text.format.DateFormat.getDateFormatOrder(getContext());
        int spinnerCount = order.length;
        this.mSpinners.removeAllViews();
        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case 'M':
                    this.mSpinners.addView(this.mMonthSpinner);
                    this.mSpinners.addView(this.mMonthText);
                    resetSpinnerLayoutParams(this.mMonthSpinner, spinnerCount, i);
                    setImeOptions(this.mMonthSpinner, spinnerCount, i);
                    break;
                case 'd':
                    this.mSpinners.addView(this.mDaySpinner);
                    this.mSpinners.addView(this.mDayText);
                    resetSpinnerLayoutParams(this.mDaySpinner, spinnerCount, i);
                    setImeOptions(this.mDaySpinner, spinnerCount, i);
                    break;
                case 'y':
                    this.mSpinners.addView(this.mYearSpinner);
                    this.mSpinners.addView(this.mYearText);
                    resetSpinnerLayoutParams(this.mYearSpinner, spinnerCount, i);
                    setImeOptions(this.mYearSpinner, spinnerCount, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private void resetSpinnerLayoutParams(OppoNumberPicker picker, int count, int index) {
        LayoutParams params = (LayoutParams) picker.getLayoutParams();
        if (index == 0) {
            params.weight = 1.0f;
            picker.setAlignPosition(2);
        } else if (index == count - 1) {
            params.weight = 1.0f;
            picker.setAlignPosition(1);
        } else {
            params.weight = 0.0f;
            picker.setAlignPosition(0);
        }
        picker.setLayoutParams(params);
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

    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(this.mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        if (this.mCurrentDate.get(1) == year && this.mCurrentDate.get(2) == month && this.mCurrentDate.get(5) == dayOfMonth) {
            return false;
        }
        return true;
    }

    private void setDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(year, month, dayOfMonth);
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    private void updateSpinners() {
        int max;
        int min;
        int i;
        if (this.mCurrentDate.equals(this.mMinDate)) {
            this.mDaySpinner.setMinValue(this.mCurrentDate.get(5));
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mDaySpinner.setWrapSelectorWheel(false);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mCurrentDate.get(2));
            this.mMonthSpinner.setMaxValue(this.mCurrentDate.getActualMaximum(2));
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else if (this.mCurrentDate.get(1) == this.mMinDate.get(1) && this.mCurrentDate.get(2) == this.mMinDate.get(2)) {
            this.mDaySpinner.setMinValue(1);
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mDaySpinner.setWrapSelectorWheel(true);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mMinDate.get(2));
            this.mMonthSpinner.setMaxValue(this.mMinDate.getActualMaximum(2));
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else if (this.mCurrentDate.equals(this.mMaxDate)) {
            this.mDaySpinner.setMinValue(this.mCurrentDate.getActualMinimum(5));
            this.mDaySpinner.setMaxValue(this.mCurrentDate.get(5));
            this.mDaySpinner.setWrapSelectorWheel(false);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mCurrentDate.getActualMinimum(2));
            this.mMonthSpinner.setMaxValue(this.mCurrentDate.get(2));
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else {
            this.mDaySpinner.setMinValue(1);
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mDaySpinner.setWrapSelectorWheel(true);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(0);
            this.mMonthSpinner.setMaxValue(11);
            this.mMonthSpinner.setWrapSelectorWheel(true);
        }
        this.mMonthSpinner.setDisplayedValues((String[]) Arrays.copyOfRange(this.mShortMonths, this.mMonthSpinner.getMinValue(), this.mMonthSpinner.getMaxValue() + 1));
        if (this.mIsChinese) {
            max = this.mDaySpinner.getMaxValue();
            min = this.mDaySpinner.getMinValue();
            String[] displayedDays = new String[((max - min) + 1)];
            for (i = min; i <= max; i++) {
                displayedDays[i - min] = i + this.mDayString;
            }
            this.mDaySpinner.setDisplayedValues(displayedDays);
        }
        this.mYearSpinner.setMinValue(this.mMinDate.get(1));
        this.mYearSpinner.setMaxValue(this.mMaxDate.get(1));
        if (this.mIsChinese) {
            max = this.mYearSpinner.getMaxValue();
            min = this.mYearSpinner.getMinValue();
            String[] displayedYears = new String[((max - min) + 1)];
            for (i = min; i <= max; i++) {
                displayedYears[i - min] = i + this.mYearString;
            }
            this.mYearSpinner.setDisplayedValues(displayedYears);
        }
        this.mYearSpinner.setWrapSelectorWheel(true);
        this.mYearSpinner.setValue(this.mCurrentDate.get(1));
        this.mMonthSpinner.setValue(this.mCurrentDate.get(2));
        this.mDaySpinner.setValue(this.mCurrentDate.get(5));
        if (this.mDaySpinner.getValue() > 27) {
            this.mDaySpinner.invalidate();
        }
    }

    public boolean isChineseLanguage() {
        if ("zh".equals(Locale.getDefault().getLanguage())) {
            return true;
        }
        return false;
    }

    private void updateCalendarView() {
    }

    public int getYear() {
        return this.mCurrentDate.get(1);
    }

    public int getMonth() {
        return this.mCurrentDate.get(2);
    }

    public int getDayOfMonth() {
        return this.mCurrentDate.get(5);
    }

    private void notifyDateChanged() {
        sendAccessibilityEvent(4);
        if (this.mOnDateChangedListener != null) {
            this.mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    private void setImeOptions(OppoNumberPicker spinner, int spinnerCount, int spinnerIndex) {
        int imeOptions;
        if (spinnerIndex < spinnerCount - 1) {
            imeOptions = 5;
        } else {
            imeOptions = 6;
        }
        ((TextView) spinner.findViewById(201458728)).setImeOptions(imeOptions);
    }

    private void setContentDescriptions() {
        this.mDaySpinner.findViewById(201458726).setContentDescription(this.mContext.getString(201589843));
        this.mDaySpinner.findViewById(201458727).setContentDescription(this.mContext.getString(201589844));
        this.mMonthSpinner.findViewById(201458726).setContentDescription(this.mContext.getString(201589845));
        this.mMonthSpinner.findViewById(201458727).setContentDescription(this.mContext.getString(201589846));
        this.mYearSpinner.findViewById(201458726).setContentDescription(this.mContext.getString(201589847));
        this.mYearSpinner.findViewById(201458727).setContentDescription(this.mContext.getString(201589848));
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
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
}

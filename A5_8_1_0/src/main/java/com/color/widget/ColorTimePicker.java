package com.color.widget;

import android.content.Context;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import com.color.util.ColorAccessibilityUtil;
import com.oppo.widget.OppoNumberPicker;
import com.oppo.widget.OppoNumberPicker.Formatter;
import com.oppo.widget.OppoNumberPicker.OnValueChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ColorTimePicker extends FrameLayout {
    private static final int HOURS_OF_A_DAY = 23;
    private static final int HOURS_OF_HALF_DAY = 12;
    private static final long MILLISECOND_A_DAY = 86400000;
    private static final int MINUTES_OF_A_HOUR = 59;
    private static final int START_YEAR = 1900;
    private static final String TAG = "ColorTimePicker";
    private static final int TOTAL_YEAR = 100;
    private static final int YEAR_AMOUNT_LEAP = 366;
    private static final int YEAR_AMOUNT_NOT_LEAP = 365;
    private static int ampm = -1;
    private static int day = 0;
    private static Date endDate;
    private static int endDay;
    private static int endMonth;
    private static int endYear;
    private static int hour = 0;
    private static SimpleDateFormat informatter;
    private static Calendar mCalendar;
    private static int mDateAmount;
    private static Calendar mDefaultCalendar;
    private static Calendar mTodayCalendar;
    private static int minute = -1;
    private static int month = 0;
    private static SimpleDateFormat outformatter;
    private static long startTime;
    private static int todayDate;
    private static int todayIndex = -1;
    private static int todayMonth;
    private static int todayYear;
    private static int year = 0;
    private String[] mAMPM;
    private String mAccessDate;
    private int mAccessHour;
    private int mAccessMinute;
    private String mAccessPm;
    private int mAmPm;
    private ViewGroup mColorTimePicker;
    private Context mContext;
    private Date[] mDateArrays;
    private String[] mDateNames;
    private long[] mDates;
    private String mDay;
    private boolean mIsMinuteFiveStep;
    private OnTimeChangeListener mOnTimeChangeListener;
    private int mSetYear;
    private String mToday;
    private OppoNumberPicker pickerAmPm;
    private OppoNumberPicker pickerDate;
    private OppoNumberPicker pickerHour;
    private OppoNumberPicker pickerMinute;
    private String start;

    public interface OnTimeChangeListener {
        void OnTimeChange(Calendar calendar);
    }

    class mFormat implements Formatter {
        mFormat() {
        }

        public String format(int value) {
            if (ColorTimePicker.this.mDateNames[value - 1] == null) {
                ColorTimePicker.this.mDateNames[value - 1] = ColorTimePicker.this.getDateYMDW(value);
            }
            return ColorTimePicker.this.mDateNames[value - 1];
        }
    }

    public void setOnTimeChangeListener(OnTimeChangeListener e) {
        this.mOnTimeChangeListener = e;
    }

    public ColorTimePicker(Context context) {
        this(context, null);
    }

    public ColorTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAmPm = -1;
        this.mAccessDate = null;
        this.mAccessPm = null;
        this.mContext = context;
        this.mAMPM = this.mContext.getResources().getStringArray(201786382);
        this.mToday = this.mContext.getResources().getString(201590096);
        this.mDay = this.mContext.getResources().getString(201590097);
        mCalendar = Calendar.getInstance();
        mTodayCalendar = Calendar.getInstance();
        todayYear = mTodayCalendar.get(1);
        todayMonth = mTodayCalendar.get(2);
        todayDate = mTodayCalendar.get(5);
        this.mColorTimePicker = (ViewGroup) LayoutInflater.from(this.mContext).inflate(201917537, this, true);
        this.pickerDate = (OppoNumberPicker) this.mColorTimePicker.findViewById(201458912);
        this.pickerHour = (OppoNumberPicker) this.mColorTimePicker.findViewById(201458911);
        this.pickerMinute = (OppoNumberPicker) this.mColorTimePicker.findViewById(201458910);
        this.pickerAmPm = (OppoNumberPicker) this.mColorTimePicker.findViewById(201458909);
        reorderSpinners();
    }

    private void reorderSpinners() {
        char[] order = getTimeFormatOrder(DateFormat.getBestDateTimePattern(Locale.getDefault(), "hm"));
        int spinnerCount = order.length;
        if (this.pickerAmPm != null) {
            Log.d("OppoDatePicker", "reorderSpinners, mColorTimePicker is not null will remove view");
            ViewGroup view = (ViewGroup) this.pickerAmPm.getParent();
            if (spinnerCount > 0 && order[spinnerCount - 1] == 'a') {
                view.removeView(this.pickerAmPm);
                view.addView(this.pickerAmPm);
                reorderSpinners(is24Hours());
            }
        }
    }

    private void reorderSpinners(boolean is24Hours) {
        int i = 0;
        boolean isLayoutRtl = false;
        if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
            isLayoutRtl = true;
        }
        char[] order = getTimeFormatOrder(DateFormat.getBestDateTimePattern(Locale.getDefault(), "hm"));
        int spinnerCount = order.length;
        if (spinnerCount > 0 && order[spinnerCount - 1] == 'a') {
            LayoutParams paramsHour;
            int i2;
            LayoutParams paramsMin;
            LayoutParams paramsAm;
            if (isLayoutRtl) {
                paramsHour = (LayoutParams) this.pickerHour.getLayoutParams();
                if (is24Hours) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                paramsHour.weight = (float) i2;
                this.pickerHour.setLayoutParams(paramsHour);
                OppoNumberPicker oppoNumberPicker = this.pickerHour;
                if (is24Hours) {
                    i2 = 2;
                } else {
                    i2 = 0;
                }
                oppoNumberPicker.setAlignPosition(i2);
                paramsMin = (LayoutParams) this.pickerMinute.getLayoutParams();
                paramsMin.weight = 0.0f;
                this.pickerMinute.setLayoutParams(paramsMin);
                this.pickerMinute.setAlignPosition(2);
                paramsAm = (LayoutParams) this.pickerAmPm.getLayoutParams();
                if (!is24Hours) {
                    i = 1;
                }
                paramsAm.weight = (float) i;
                this.pickerAmPm.setLayoutParams(paramsAm);
                this.pickerAmPm.setAlignPosition(2);
                return;
            }
            paramsHour = (LayoutParams) this.pickerHour.getLayoutParams();
            paramsHour.weight = 0.0f;
            this.pickerHour.setLayoutParams(paramsHour);
            this.pickerHour.setAlignPosition(1);
            paramsMin = (LayoutParams) this.pickerMinute.getLayoutParams();
            if (is24Hours) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            paramsMin.weight = (float) i2;
            this.pickerMinute.setLayoutParams(paramsMin);
            OppoNumberPicker oppoNumberPicker2 = this.pickerMinute;
            if (is24Hours) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            oppoNumberPicker2.setAlignPosition(i2);
            paramsAm = (LayoutParams) this.pickerAmPm.getLayoutParams();
            if (!is24Hours) {
                i = 1;
            }
            paramsAm.weight = (float) i;
            this.pickerAmPm.setLayoutParams(paramsAm);
            this.pickerAmPm.setAlignPosition(1);
        }
    }

    public static char[] getTimeFormatOrder(String pattern) {
        char[] result = new char[3];
        int resultIndex = 0;
        boolean sawHour = false;
        boolean sawMin = false;
        boolean sawAmPm = false;
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (ch == 'h' || ch == 'm' || ch == 'a' || ch == 'H' || ch == 'K' || ch == 'k') {
                int resultIndex2;
                if (ch == 'm' && (sawMin ^ 1) != 0) {
                    resultIndex2 = resultIndex + 1;
                    result[resultIndex] = 'm';
                    sawMin = true;
                    resultIndex = resultIndex2;
                } else if ((ch == 'h' || ch == 'H' || ch == 'K' || ch == 'k') && (sawHour ^ 1) != 0) {
                    resultIndex2 = resultIndex + 1;
                    result[resultIndex] = 'h';
                    sawHour = true;
                    resultIndex = resultIndex2;
                } else if (ch == 'a' && (sawAmPm ^ 1) != 0) {
                    resultIndex2 = resultIndex + 1;
                    result[resultIndex] = 'a';
                    sawAmPm = true;
                    resultIndex = resultIndex2;
                }
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                throw new IllegalArgumentException("Bad pattern character '" + ch + "' in " + pattern);
            }
        }
        return result;
    }

    @Deprecated
    public void setColorTimePicker(int year, Calendar defaultCalendar) {
        this.mSetYear = year;
        mDefaultCalendar = defaultCalendar;
        getColorTimePicker();
    }

    public void setColorTimePicker(Calendar defaultCalendar) {
        mDefaultCalendar = defaultCalendar;
        getColorTimePicker();
    }

    public View getColorTimePicker() {
        Calendar defaultCalendar;
        int i;
        if (mDefaultCalendar != null) {
            defaultCalendar = mDefaultCalendar;
            mTodayCalendar = Calendar.getInstance();
            todayYear = mTodayCalendar.get(1);
            todayMonth = mTodayCalendar.get(2);
            todayDate = mTodayCalendar.get(5);
            year = defaultCalendar.get(1);
        } else {
            defaultCalendar = mTodayCalendar;
            year = mTodayCalendar.get(1);
        }
        mCalendar = Calendar.getInstance();
        informatter = new SimpleDateFormat("yyyy-MM-dd");
        month = defaultCalendar.get(2) + 1;
        day = defaultCalendar.get(5);
        hour = defaultCalendar.get(11);
        ampm = defaultCalendar.get(9);
        minute = defaultCalendar.get(HOURS_OF_HALF_DAY);
        mCalendar.set(year, month - 1, day, hour, minute);
        mDateAmount = 36500;
        int half = 0;
        for (i = 0; i < TOTAL_YEAR; i++) {
            mDateAmount += getDaysAmountOfYear((year - 50) + i);
        }
        for (i = 0; i < 50; i++) {
            half += getDaysAmountOfYear((year - 50) + i);
        }
        this.mDateNames = new String[mDateAmount];
        this.mDateArrays = new Date[mDateAmount];
        this.mDates = new long[mDateAmount];
        if (month > 2 && (isLeapYear(year - 50) ^ 1) != 0 && isLeapYear(year)) {
            half++;
        }
        if (month > 2 && isLeapYear(year - 50)) {
            half--;
        }
        Log.d("luoqy", "half  = " + half);
        this.start = (year - 50) + "-" + month + "-" + day;
        try {
            startTime = informatter.parse(this.start).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        endDate = new Date();
        if (is24Hours()) {
            this.pickerHour.setMaxValue(23);
            this.pickerHour.setMinValue(0);
            this.pickerAmPm.setVisibility(8);
            this.pickerHour.setFormatter(OppoNumberPicker.TWO_DIGIT_FORMATTER);
            reorderSpinners(true);
        } else {
            this.pickerHour.setMaxValue(HOURS_OF_HALF_DAY);
            this.pickerHour.setMinValue(1);
            this.pickerAmPm.setMaxValue(this.mAMPM.length - 1);
            this.pickerAmPm.setMinValue(0);
            this.pickerAmPm.setDisplayedValues(this.mAMPM);
            this.pickerAmPm.setVisibility(0);
            reorderSpinners(false);
            this.pickerHour.setFormatter(null);
        }
        if (hour >= 0) {
            if (is24Hours()) {
                this.pickerHour.setValue(hour);
            } else {
                if (ampm > 0) {
                    this.pickerHour.setValue(hour - 12);
                } else {
                    this.pickerHour.setValue(hour);
                }
                this.pickerAmPm.setValue(ampm);
                this.mAmPm = ampm;
            }
        }
        if (!is24Hours() && ColorAccessibilityUtil.isTalkbackEnabled(this.mContext)) {
            this.mAccessPm = this.mAMPM[this.pickerAmPm.getValue()];
        }
        this.pickerAmPm.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(OppoNumberPicker spinner, int oldVal, int newVal) {
                ColorTimePicker.this.mAmPm = spinner.getValue();
                ColorTimePicker.mCalendar.set(9, spinner.getValue());
                if (!ColorTimePicker.this.is24Hours() && ColorAccessibilityUtil.isTalkbackEnabled(ColorTimePicker.this.mContext)) {
                    ColorTimePicker.this.mAccessPm = ColorTimePicker.this.mAMPM[ColorTimePicker.this.mAmPm];
                    ColorTimePicker.this.announceForAccessibility(ColorTimePicker.this.mAccessDate + ColorTimePicker.this.mAccessPm + ColorTimePicker.this.mAccessHour + " " + ColorTimePicker.this.mAccessMinute);
                }
                if (ColorTimePicker.this.mOnTimeChangeListener != null) {
                    ColorTimePicker.this.mOnTimeChangeListener.OnTimeChange(ColorTimePicker.mCalendar);
                }
            }
        });
        if (ColorAccessibilityUtil.isTalkbackEnabled(this.mContext)) {
            this.mAccessHour = this.pickerHour.getValue();
        }
        this.pickerHour.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(OppoNumberPicker spinner, int oldVal, int newVal) {
                if (!ColorTimePicker.this.is24Hours() && ((oldVal == ColorTimePicker.HOURS_OF_HALF_DAY && newVal == 11) || (oldVal == 11 && newVal == ColorTimePicker.HOURS_OF_HALF_DAY))) {
                    ColorTimePicker.this.mAmPm = 1 - ColorTimePicker.this.mAmPm;
                    ColorTimePicker.this.pickerAmPm.setValue(ColorTimePicker.this.mAmPm);
                }
                if (ColorTimePicker.this.is24Hours()) {
                    ColorTimePicker.mCalendar.set(11, spinner.getValue());
                } else if (ColorTimePicker.this.mAmPm == 0) {
                    if (newVal == ColorTimePicker.HOURS_OF_HALF_DAY) {
                        ColorTimePicker.mCalendar.set(11, newVal - 12);
                    } else {
                        ColorTimePicker.mCalendar.set(11, newVal);
                    }
                } else if (ColorTimePicker.this.mAmPm == 1) {
                    if (newVal == ColorTimePicker.HOURS_OF_HALF_DAY) {
                        ColorTimePicker.mCalendar.set(11, ColorTimePicker.HOURS_OF_HALF_DAY);
                    } else {
                        ColorTimePicker.mCalendar.set(11, newVal + ColorTimePicker.HOURS_OF_HALF_DAY);
                    }
                }
                if (ColorAccessibilityUtil.isTalkbackEnabled(ColorTimePicker.this.mContext)) {
                    String accessDate;
                    ColorTimePicker.this.mAccessHour = ColorTimePicker.this.pickerHour.getValue();
                    if (ColorTimePicker.this.is24Hours()) {
                        accessDate = ColorTimePicker.this.mAccessDate + ColorTimePicker.this.mAccessHour + " " + ColorTimePicker.this.mAccessMinute;
                    } else {
                        accessDate = ColorTimePicker.this.mAccessDate + ColorTimePicker.this.mAccessPm + ColorTimePicker.this.mAccessHour + " " + ColorTimePicker.this.mAccessMinute;
                    }
                    ColorTimePicker.this.announceForAccessibility(accessDate);
                }
                if (ColorTimePicker.this.mOnTimeChangeListener != null) {
                    ColorTimePicker.this.mOnTimeChangeListener.OnTimeChange(ColorTimePicker.mCalendar);
                }
            }
        });
        this.pickerMinute.setMinValue(0);
        this.pickerMinute.setFormatter(OppoNumberPicker.TWO_DIGIT_FORMATTER);
        if (this.mIsMinuteFiveStep) {
            this.pickerMinute.setMinValue(0);
            this.pickerMinute.setMaxValue(11);
            String[] minutes = new String[HOURS_OF_HALF_DAY];
            for (i = 0; i < HOURS_OF_HALF_DAY; i++) {
                minutes[i] = i * 5 < 10 ? "0" + (i * 5) : (i * 5) + "";
            }
            this.pickerMinute.setDisplayedValues(minutes);
            if (minute / 5 > 0) {
                this.pickerMinute.setValue(minute / 5);
                mCalendar.set(HOURS_OF_HALF_DAY, Integer.parseInt(minutes[minute / 5]));
            }
        } else {
            this.pickerMinute.setMaxValue(MINUTES_OF_A_HOUR);
            if (minute >= 0) {
                this.pickerMinute.setValue(minute);
            }
        }
        if (ColorAccessibilityUtil.isTalkbackEnabled(this.mContext)) {
            this.mAccessMinute = this.pickerMinute.getValue();
        }
        this.pickerMinute.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(OppoNumberPicker spinner, int oldVal, int newVal) {
                if (ColorTimePicker.this.mIsMinuteFiveStep) {
                    ColorTimePicker.mCalendar.set(ColorTimePicker.HOURS_OF_HALF_DAY, spinner.getValue() * 5);
                } else {
                    ColorTimePicker.mCalendar.set(ColorTimePicker.HOURS_OF_HALF_DAY, spinner.getValue());
                }
                if (ColorAccessibilityUtil.isTalkbackEnabled(ColorTimePicker.this.mContext)) {
                    String accessDate;
                    ColorTimePicker.this.mAccessMinute = ColorTimePicker.this.pickerMinute.getValue();
                    if (ColorTimePicker.this.is24Hours()) {
                        accessDate = ColorTimePicker.this.mAccessDate + ColorTimePicker.this.mAccessHour + " " + ColorTimePicker.this.mAccessMinute;
                    } else {
                        accessDate = ColorTimePicker.this.mAccessDate + ColorTimePicker.this.mAccessPm + ColorTimePicker.this.mAccessHour + " " + ColorTimePicker.this.mAccessMinute;
                    }
                    ColorTimePicker.this.announceForAccessibility(accessDate);
                }
                if (ColorTimePicker.this.mOnTimeChangeListener != null) {
                    ColorTimePicker.this.mOnTimeChangeListener.OnTimeChange(ColorTimePicker.mCalendar);
                }
            }
        });
        this.pickerDate.setMinValue(1);
        this.pickerDate.setMaxValue(mDateAmount);
        this.pickerDate.setWrapSelectorWheel(false);
        this.pickerDate.setValue(half);
        this.pickerDate.setFormatter(new mFormat());
        this.pickerDate.invalidate();
        this.pickerDate.requestLayout();
        this.pickerDate.updateString();
        if (ColorAccessibilityUtil.isTalkbackEnabled(this.mContext)) {
            this.mAccessDate = new SimpleDateFormat("MMM dd" + this.mDay + " E").format(getDateFromValue(this.pickerDate.getValue()));
        }
        this.pickerDate.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(OppoNumberPicker spinner, int oldVal, int newVal) {
                Date pickeddate = ColorTimePicker.this.getDateFromValue(spinner.getValue());
                ColorTimePicker.mCalendar.set(2, pickeddate.getMonth());
                ColorTimePicker.mCalendar.set(5, pickeddate.getDate());
                ColorTimePicker.mCalendar.set(1, pickeddate.getYear() + ColorTimePicker.START_YEAR);
                if (ColorAccessibilityUtil.isTalkbackEnabled(ColorTimePicker.this.mContext)) {
                    String accessDate;
                    ColorTimePicker.this.mAccessDate = new SimpleDateFormat("MMM dd" + ColorTimePicker.this.mDay + " E").format(pickeddate);
                    if (ColorTimePicker.this.is24Hours()) {
                        accessDate = ColorTimePicker.this.mAccessDate + ColorTimePicker.this.mAccessHour + " " + ColorTimePicker.this.mAccessMinute;
                    } else {
                        accessDate = ColorTimePicker.this.mAccessDate + ColorTimePicker.this.mAccessPm + ColorTimePicker.this.mAccessHour + " " + ColorTimePicker.this.mAccessMinute;
                    }
                    ColorTimePicker.this.announceForAccessibility(accessDate);
                }
                if (ColorTimePicker.this.mOnTimeChangeListener != null) {
                    ColorTimePicker.this.mOnTimeChangeListener.OnTimeChange(ColorTimePicker.mCalendar);
                }
            }
        });
        return this;
    }

    private boolean is24Hours() {
        if ("24".equals(System.getString(this.mContext.getContentResolver(), "time_12_24"))) {
            return true;
        }
        return false;
    }

    private static int getDaysAmountOfYear(int year) {
        return isLeapYear(year) ? YEAR_AMOUNT_LEAP : YEAR_AMOUNT_NOT_LEAP;
    }

    private String getDateYMDW(int elapsetime) {
        endDate.setTime((startTime + (((long) elapsetime) * MILLISECOND_A_DAY)) + 43200000);
        endYear = endDate.getYear() + START_YEAR;
        endMonth = endDate.getMonth();
        endDay = endDate.getDate();
        this.mDates[elapsetime - 1] = endDate.getTime();
        return isToday(endYear, endMonth, endDay) ? this.mToday : DateUtils.formatDateTime(this.mContext, this.mDates[elapsetime - 1], 98330);
    }

    private static boolean isToday(int y, int m, int d) {
        if (y == todayYear && m == todayMonth && d == todayDate) {
            return true;
        }
        return false;
    }

    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % TOTAL_YEAR != 0) || year % 400 == 0;
    }

    private Date getDateFromValue(int value) {
        if (this.mDateArrays[value - 1] == null) {
            this.mDateArrays[value - 1] = new Date(this.mDates[value - 1]);
        }
        return this.mDateArrays[value - 1];
    }

    public void setMinuteStepToFive() {
        this.mIsMinuteFiveStep = true;
    }
}

package com.oppo.widget;

import android.R;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.oppo.widget.OppoNumberPicker.OnValueChangeListener;
import java.util.Calendar;
import java.util.Locale;

public class OppoTimePickerCustom extends FrameLayout {
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int HOURS_IN_HALF_DAY = 12;
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(OppoTimePickerCustom view, int hourOfDay, int minute) {
        }
    };
    private final Button mAmPmButton;
    private final String[] mAmPmStrings;
    private Locale mCurrentLocale;
    private ViewGroup mHourLayout;
    private TextView mHourText;
    private boolean mIs24HourView;
    private boolean mIsAm;
    private boolean mIsCountDown;
    private boolean mIsEnabled;
    private ViewGroup mMinuteLayout;
    private TextView mMinuteText;
    private OnTimeChangedListener mOnTimeChangedListener;
    private final OppoNumberPicker mOppoAmPmSpinner;
    private final EditText mOppoAmPmSpinnerInput;
    private final OppoNumberPicker mOppoHourSpinner;
    private final EditText mOppoHourSpinnerInput;
    private final OppoNumberPicker mOppoMinuteSpinner;
    private final EditText mOppoMinuteSpinnerInput;
    private int mPickerPadding;
    private Calendar mTempCalendar;

    public interface OnTimeChangedListener {
        void onTimeChanged(OppoTimePickerCustom oppoTimePickerCustom, int i, int i2);
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
        private final int mHour;
        private final int mMinute;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        /* synthetic */ SavedState(Parcelable superState, int hour, int minute, SavedState -this3) {
            this(superState, hour, minute);
        }

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            this.mHour = hour;
            this.mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mHour = in.readInt();
            this.mMinute = in.readInt();
        }

        public int getHour() {
            return this.mHour;
        }

        public int getMinute() {
            return this.mMinute;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mHour);
            dest.writeInt(this.mMinute);
        }
    }

    public OppoTimePickerCustom(Context context) {
        this(context, null);
    }

    public OppoTimePickerCustom(Context context, AttributeSet attrs) {
        this(context, attrs, 201393154);
    }

    public OppoTimePickerCustom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsEnabled = DEFAULT_ENABLED_STATE;
        this.mIsCountDown = false;
        setCurrentLocale(Locale.getDefault());
        this.mPickerPadding = context.getResources().getDimensionPixelSize(201655460);
        context.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyle, 0).recycle();
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(201917588, this, DEFAULT_ENABLED_STATE);
        this.mMinuteText = (TextView) findViewById(201458913);
        this.mHourText = (TextView) findViewById(201458914);
        this.mMinuteLayout = (ViewGroup) findViewById(201458967);
        this.mHourLayout = (ViewGroup) findViewById(201458985);
        this.mOppoHourSpinner = (OppoNumberPicker) findViewById(201458731);
        this.mOppoHourSpinner.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(OppoNumberPicker spinner, int oldVal, int newVal) {
                OppoTimePickerCustom.this.updateInputState();
                OppoTimePickerCustom.this.onTimeChanged();
            }
        });
        if (VERSION.SDK_INT > 16) {
            this.mMinuteText.setTextAlignment(5);
            this.mHourText.setTextAlignment(5);
        }
        this.mOppoHourSpinnerInput = (EditText) this.mOppoHourSpinner.findViewById(201458728);
        this.mOppoHourSpinnerInput.setImeOptions(5);
        this.mOppoMinuteSpinner = (OppoNumberPicker) findViewById(201458729);
        this.mOppoMinuteSpinner.setMinValue(0);
        this.mOppoMinuteSpinner.setMaxValue(59);
        this.mOppoMinuteSpinner.setOnLongPressUpdateInterval(100);
        this.mOppoMinuteSpinner.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(OppoNumberPicker spinner, int oldVal, int newVal) {
                OppoTimePickerCustom.this.updateInputState();
                OppoTimePickerCustom.this.onTimeChanged();
            }
        });
        this.mOppoMinuteSpinnerInput = (EditText) this.mOppoMinuteSpinner.findViewById(201458728);
        this.mOppoMinuteSpinnerInput.setImeOptions(5);
        this.mAmPmStrings = context.getResources().getStringArray(201786382);
        View amPmView = findViewById(201458732);
        if (amPmView instanceof Button) {
            this.mOppoAmPmSpinner = null;
            this.mOppoAmPmSpinnerInput = null;
            this.mAmPmButton = (Button) amPmView;
            this.mAmPmButton.setOnClickListener(new OnClickListener() {
                public void onClick(View button) {
                    button.requestFocus();
                    OppoTimePickerCustom.this.mIsAm = OppoTimePickerCustom.this.mIsAm ^ 1;
                    OppoTimePickerCustom.this.updateAmPmControl();
                }
            });
        } else {
            this.mAmPmButton = null;
            this.mOppoAmPmSpinner = (OppoNumberPicker) amPmView;
            this.mOppoAmPmSpinner.setMinValue(0);
            this.mOppoAmPmSpinner.setMaxValue(1);
            this.mOppoAmPmSpinner.setDisplayedValues(this.mAmPmStrings);
            this.mOppoAmPmSpinner.setOnValueChangedListener(new OnValueChangeListener() {
                public void onValueChange(OppoNumberPicker picker, int oldVal, int newVal) {
                    OppoTimePickerCustom.this.updateInputState();
                    picker.requestFocus();
                    OppoTimePickerCustom.this.mIsAm = OppoTimePickerCustom.this.mIsAm ^ 1;
                    OppoTimePickerCustom.this.updateAmPmControl();
                    OppoTimePickerCustom.this.onTimeChanged();
                }
            });
            this.mOppoAmPmSpinnerInput = (EditText) this.mOppoAmPmSpinner.findViewById(201458728);
            this.mOppoAmPmSpinnerInput.setImeOptions(6);
        }
        updateHourControl();
        updateAmPmControl();
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);
        setCurrentHour(Integer.valueOf(this.mTempCalendar.get(11)));
        setCurrentMinute(Integer.valueOf(this.mTempCalendar.get(HOURS_IN_HALF_DAY)));
        if (!isEnabled()) {
            setEnabled(false);
        }
        reorderSpinners();
        updateFormatter();
        setContentDescriptions();
    }

    private void reorderSpinners() {
        if (Locale.getDefault().getLanguage().equals("en") && this.mOppoAmPmSpinner != null) {
            ViewGroup view = (ViewGroup) this.mOppoAmPmSpinner.getParent();
            view.removeView(this.mOppoAmPmSpinner);
            this.mOppoAmPmSpinner.setAlignPosition(2);
            view.addView(this.mOppoAmPmSpinner);
        }
    }

    public void setTextVisibility(boolean isVisible) {
        if (isVisible) {
            this.mMinuteText.setVisibility(0);
            this.mHourText.setVisibility(0);
            return;
        }
        this.mMinuteText.setVisibility(8);
        this.mHourText.setVisibility(4);
    }

    public void setIsCountDown(boolean isCountDown) {
        this.mIsCountDown = isCountDown;
        updateFormatter();
    }

    private void updateFormatter() {
        if (this.mIsCountDown) {
            this.mOppoHourSpinner.setFormatter(null);
            this.mOppoMinuteSpinner.setFormatter(null);
        }
        if (!this.mIsCountDown && is24HourView()) {
            this.mOppoHourSpinner.setFormatter(OppoNumberPicker.TWO_DIGIT_FORMATTER);
            this.mOppoMinuteSpinner.setFormatter(OppoNumberPicker.TWO_DIGIT_FORMATTER);
        }
        if (!(this.mIsCountDown || (is24HourView() ^ 1) == 0)) {
            this.mOppoHourSpinner.setFormatter(null);
            this.mOppoMinuteSpinner.setFormatter(OppoNumberPicker.TWO_DIGIT_FORMATTER);
        }
        this.mOppoHourSpinner.requestLayout();
        this.mOppoMinuteSpinner.requestLayout();
    }

    public void setEnabled(boolean enabled) {
        if (this.mIsEnabled != enabled) {
            super.setEnabled(enabled);
            this.mOppoMinuteSpinner.setEnabled(enabled);
            this.mOppoHourSpinner.setEnabled(enabled);
            if (this.mOppoAmPmSpinner != null) {
                this.mOppoAmPmSpinner.setEnabled(enabled);
            } else {
                this.mAmPmButton.setEnabled(enabled);
            }
            this.mIsEnabled = enabled;
        }
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    private void setCurrentLocale(Locale locale) {
        if (!locale.equals(this.mCurrentLocale)) {
            this.mCurrentLocale = locale;
            this.mTempCalendar = Calendar.getInstance(locale);
        }
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getCurrentHour().intValue(), getCurrentMinute().intValue(), null);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(Integer.valueOf(ss.getHour()));
        setCurrentMinute(Integer.valueOf(ss.getMinute()));
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.mOnTimeChangedListener = onTimeChangedListener;
    }

    public Integer getCurrentHour() {
        int currentHour = this.mOppoHourSpinner.getValue();
        if (is24HourView()) {
            return Integer.valueOf(currentHour);
        }
        if (this.mIsAm) {
            return Integer.valueOf(currentHour % HOURS_IN_HALF_DAY);
        }
        return Integer.valueOf((currentHour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY);
    }

    public void setCurrentHour(Integer currentHour) {
        if (currentHour != null && currentHour != getCurrentHour()) {
            if (!is24HourView()) {
                if (currentHour.intValue() >= HOURS_IN_HALF_DAY) {
                    this.mIsAm = false;
                    if (currentHour.intValue() > HOURS_IN_HALF_DAY) {
                        currentHour = Integer.valueOf(currentHour.intValue() - 12);
                    }
                } else {
                    this.mIsAm = DEFAULT_ENABLED_STATE;
                    if (currentHour.intValue() == 0) {
                        currentHour = Integer.valueOf(HOURS_IN_HALF_DAY);
                    }
                }
                updateAmPmControl();
            }
            this.mOppoHourSpinner.setValue(currentHour.intValue());
            onTimeChanged();
        }
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (this.mIs24HourView != is24HourView.booleanValue()) {
            int currentHour = getCurrentHour().intValue();
            this.mIs24HourView = is24HourView.booleanValue();
            updateHourControl();
            setCurrentHour(Integer.valueOf(currentHour));
            updateAmPmControl();
            updateFormatter();
            this.mOppoHourSpinner.requestLayout();
        }
    }

    public boolean is24HourView() {
        return this.mIs24HourView;
    }

    public Integer getCurrentMinute() {
        return Integer.valueOf(this.mOppoMinuteSpinner.getValue());
    }

    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute != getCurrentMinute()) {
            this.mOppoMinuteSpinner.setValue(currentMinute.intValue());
            onTimeChanged();
        }
    }

    public int getBaseline() {
        return this.mOppoHourSpinner.getBaseline();
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return DEFAULT_ENABLED_STATE;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        super.onPopulateAccessibilityEvent(event);
        if (this.mIs24HourView) {
            flags = 129;
        } else {
            flags = 65;
        }
        this.mTempCalendar.set(11, getCurrentHour().intValue());
        this.mTempCalendar.set(HOURS_IN_HALF_DAY, getCurrentMinute().intValue());
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags));
    }

    private void updateHourControl() {
        if (is24HourView()) {
            this.mOppoHourSpinner.setMinValue(0);
            this.mOppoHourSpinner.setMaxValue(23);
            return;
        }
        this.mOppoHourSpinner.setMinValue(1);
        this.mOppoHourSpinner.setMaxValue(HOURS_IN_HALF_DAY);
    }

    private void updateAmPmControl() {
        if (!is24HourView()) {
            int index = this.mIsAm ? 0 : 1;
            if (this.mOppoAmPmSpinner != null) {
                this.mOppoAmPmSpinner.setValue(index);
                this.mOppoAmPmSpinner.setVisibility(0);
                setWeight();
            } else {
                this.mAmPmButton.setText(this.mAmPmStrings[index]);
                this.mAmPmButton.setVisibility(0);
            }
        } else if (this.mOppoAmPmSpinner != null) {
            this.mOppoAmPmSpinner.setVisibility(8);
            setWeight();
        } else {
            this.mAmPmButton.setVisibility(8);
        }
        setWeight();
        sendAccessibilityEvent(4);
    }

    private void setWeight() {
        LayoutParams paramsHour = (LayoutParams) this.mHourLayout.getLayoutParams();
        LayoutParams paramsMin = (LayoutParams) this.mMinuteLayout.getLayoutParams();
        if (is24HourView()) {
            paramsHour.weight = 1.0f;
            paramsMin.weight = 1.0f;
            this.mOppoHourSpinner.setAlignPosition(2);
            this.mOppoMinuteSpinner.setAlignPosition(1);
            this.mHourLayout.setLayoutParams(paramsHour);
            this.mMinuteLayout.setLayoutParams(paramsMin);
            return;
        }
        LayoutParams paramsAmpm = (LayoutParams) this.mOppoAmPmSpinner.getLayoutParams();
        if (Locale.getDefault().getLanguage().equals("en")) {
            paramsHour.weight = 1.0f;
            paramsMin.weight = 0.0f;
            paramsAmpm.weight = 1.0f;
            this.mOppoHourSpinner.setAlignPosition(2);
            this.mOppoMinuteSpinner.setAlignPosition(0);
            this.mOppoAmPmSpinner.setAlignPosition(2);
        } else {
            paramsAmpm.weight = 1.0f;
            paramsHour.weight = 0.0f;
            paramsMin.weight = 1.0f;
            this.mOppoHourSpinner.setAlignPosition(0);
            this.mOppoMinuteSpinner.setAlignPosition(1);
            this.mOppoAmPmSpinner.setAlignPosition(2);
        }
        this.mHourLayout.setLayoutParams(paramsHour);
        this.mMinuteLayout.setLayoutParams(paramsMin);
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(4);
        if (getCurrentHour().intValue() == 1 && Locale.getDefault().getLanguage().equals("en")) {
            this.mHourText.setText(201590146);
        } else {
            this.mHourText.setText(201590145);
        }
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this, getCurrentHour().intValue(), getCurrentMinute().intValue());
        }
    }

    private void setContentDescriptions() {
        this.mOppoMinuteSpinner.findViewById(201458726).setContentDescription(this.mContext.getString(201589857));
        this.mOppoMinuteSpinner.findViewById(201458727).setContentDescription(this.mContext.getString(201589858));
        this.mOppoHourSpinner.findViewById(201458726).setContentDescription(this.mContext.getString(201589859));
        this.mOppoHourSpinner.findViewById(201458727).setContentDescription(this.mContext.getString(201589860));
        if (this.mOppoAmPmSpinner != null) {
            this.mOppoAmPmSpinner.findViewById(201458726).setContentDescription(this.mContext.getString(201589861));
            this.mOppoAmPmSpinner.findViewById(201458727).setContentDescription(this.mContext.getString(201589862));
        }
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
        if (inputMethodManager == null) {
            return;
        }
        if (inputMethodManager.isActive(this.mOppoHourSpinnerInput)) {
            this.mOppoHourSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mOppoMinuteSpinnerInput)) {
            this.mOppoMinuteSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mOppoAmPmSpinnerInput)) {
            this.mOppoAmPmSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }
}

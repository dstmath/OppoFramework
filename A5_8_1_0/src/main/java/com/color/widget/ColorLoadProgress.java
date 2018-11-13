package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.View.BaseSavedState;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import oppo.R;

public class ColorLoadProgress extends Button {
    public static final int DEFAULT_UP_OR_DOWN = 0;
    private static final int[] FAIL_SET = new int[]{201392650};
    private static final int[] ING_SET = new int[]{201392648};
    public static final int INSTALL_HAVE_GIFT = 4;
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 20;
    public static final int UPING_OR_DOWNING = 1;
    public static final int UP_OR_DOWN_FAIL = 3;
    public static final int UP_OR_DOWN_WAIT = 2;
    private static final int[] WAIT_SET = new int[]{201392649};
    private final boolean DEBUG;
    private final String TAG;
    private AccessibilityEventSender mAccessibilityEventSender;
    private boolean mBroadcasting;
    private Drawable mButtonDrawable;
    private int mButtonResource;
    private Context mContext;
    public int mMax;
    private OnStateChangeListener mOnStateChangeListener;
    private OnStateChangeListener mOnStateChangeWidgetListener;
    public int mProgress;
    public int mState;

    private class AccessibilityEventSender implements Runnable {
        /* synthetic */ AccessibilityEventSender(ColorLoadProgress this$0, AccessibilityEventSender -this1) {
            this();
        }

        private AccessibilityEventSender() {
        }

        public void run() {
            ColorLoadProgress.this.sendAccessibilityEvent(4);
        }
    }

    public interface OnStateChangeListener {
        void onStateChanged(ColorLoadProgress colorLoadProgress, int i);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int progress;
        int state;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.state = ((Integer) in.readValue(null)).intValue();
            this.progress = ((Integer) in.readValue(null)).intValue();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(Integer.valueOf(this.state));
            out.writeValue(Integer.valueOf(this.progress));
        }

        public String toString() {
            return "CompoundButton.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " state=" + this.state + " progress= " + this.progress + "}";
        }
    }

    public ColorLoadProgress(Context context) {
        this(context, null);
    }

    public ColorLoadProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 201393250);
    }

    public ColorLoadProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = "ColorLoadProgress";
        this.DEBUG = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorLoadProgress, defStyle, 0);
        int state = a.getInteger(1, 0);
        Drawable d = a.getDrawable(0);
        if (d != null) {
            setButtonDrawable(d);
        }
        setProgress(a.getInt(2, this.mProgress));
        setState(state);
        a.recycle();
        init();
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > this.mMax) {
            progress = this.mMax;
        }
        if (progress != this.mProgress) {
            this.mProgress = progress;
        }
        invalidate();
        onProgressRefresh(progress);
    }

    public int getProgress() {
        return this.mProgress;
    }

    public void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != this.mMax) {
            this.mMax = max;
            if (this.mProgress > max) {
                this.mProgress = max;
            }
            invalidate();
        }
    }

    public int getMax(int max) {
        return this.mMax;
    }

    public int getMax() {
        return this.mMax;
    }

    public void setButtonDrawable(int resid) {
        if (resid == 0 || resid != this.mButtonResource) {
            this.mButtonResource = resid;
            Drawable d = null;
            if (this.mButtonResource != 0) {
                d = getResources().getDrawable(this.mButtonResource);
            }
            setButtonDrawable(d);
        }
    }

    public void setButtonDrawable(Drawable d) {
        if (d != null) {
            if (this.mButtonDrawable != null) {
                this.mButtonDrawable.setCallback(null);
                unscheduleDrawable(this.mButtonDrawable);
            }
            d.setCallback(this);
            d.setState(getDrawableState());
            d.setVisible(getVisibility() == 0, false);
            this.mButtonDrawable = d;
            this.mButtonDrawable.setState(null);
            setMinHeight(this.mButtonDrawable.getIntrinsicHeight());
        }
        refreshDrawableState();
    }

    public void setState(int state) {
        if (this.mState != state) {
            this.mState = state;
            refreshDrawableState();
            notifyViewAccessibilityStateChangedIfNeeded(0);
            if (!this.mBroadcasting) {
                this.mBroadcasting = true;
                if (this.mOnStateChangeListener != null) {
                    this.mOnStateChangeListener.onStateChanged(this, this.mState);
                }
                if (this.mOnStateChangeWidgetListener != null) {
                    this.mOnStateChangeWidgetListener.onStateChanged(this, this.mState);
                }
                this.mBroadcasting = false;
            }
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.mOnStateChangeListener = listener;
    }

    void setOnStateChangeWidgetListener(OnStateChangeListener listener) {
        this.mOnStateChangeWidgetListener = listener;
    }

    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    public void toggle() {
        if (this.mState == 0) {
            setState(1);
        } else if (this.mState == 1) {
            setState(2);
        } else if (this.mState == 2) {
            setState(1);
        } else if (this.mState == 3) {
            setState(1);
        }
    }

    public int getState() {
        return this.mState;
    }

    void onProgressRefresh(int progress) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            scheduleAccessibilityEventSender();
        }
    }

    private void scheduleAccessibilityEventSender() {
        if (this.mAccessibilityEventSender == null) {
            this.mAccessibilityEventSender = new AccessibilityEventSender(this, null);
        } else {
            removeCallbacks(this.mAccessibilityEventSender);
        }
        postDelayed(this.mAccessibilityEventSender, 20);
    }

    protected void onDetachedFromWindow() {
        if (this.mAccessibilityEventSender != null) {
            removeCallbacks(this.mAccessibilityEventSender);
        }
        super.onDetachedFromWindow();
    }

    private void init() {
        this.mProgress = 0;
        this.mMax = 100;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (getState() == 1) {
            mergeDrawableStates(drawableState, ING_SET);
        }
        if (getState() == 2) {
            mergeDrawableStates(drawableState, WAIT_SET);
        }
        if (getState() == 3) {
            mergeDrawableStates(drawableState, FAIL_SET);
        }
        return drawableState;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mButtonDrawable != null) {
            this.mButtonDrawable.setState(getDrawableState());
            invalidate();
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mButtonDrawable;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mButtonDrawable != null) {
            this.mButtonDrawable.jumpToCurrentState();
        }
    }

    public Parcelable onSaveInstanceState() {
        setFreezesText(true);
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.state = getState();
        ss.progress = this.mProgress;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setState(ss.state);
        setProgress(ss.progress);
        requestLayout();
    }
}

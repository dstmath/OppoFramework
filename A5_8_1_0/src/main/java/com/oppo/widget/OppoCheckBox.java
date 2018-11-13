package com.oppo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.View.BaseSavedState;
import android.view.ViewDebug.ExportedProperty;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import com.color.util.ColorDialogUtil;
import oppo.R;

public class OppoCheckBox extends Button {
    private static final int[] ALLSELECT_SET = new int[]{201392223};
    private static final int MAX_INDEX = 2;
    private static final int[] PARTSELECT_SET = new int[]{201392222};
    public static final int SELECT_ALL = 2;
    public static final int SELECT_NONE = 0;
    public static final int SELECT_PART = 1;
    private boolean mBroadcasting;
    private Drawable mButtonDrawable;
    private int mButtonResource;
    private OnStateChangeListener mOnStateChangeListener;
    private OnStateChangeListener mOnStateChangeWidgetListener;
    private int mState;

    public interface OnStateChangeListener {
        void onStateChanged(OppoCheckBox oppoCheckBox, int i);
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
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(Integer.valueOf(this.state));
        }

        public String toString() {
            return "CompoundButton.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " state=" + this.state + "}";
        }
    }

    public OppoCheckBox(Context context) {
        this(context, null);
    }

    public OppoCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 201393214);
    }

    public OppoCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OppoCheckBox, defStyle, 0);
        Drawable d = a.getDrawable(0);
        if (d != null) {
            setButtonDrawable(d);
        }
        setState(a.getInteger(1, 0));
        a.recycle();
    }

    public void toggle() {
        int i = 2;
        if (this.mState >= 2) {
            i = 0;
        }
        setState(i);
    }

    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @ExportedProperty
    public int getState() {
        return this.mState;
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

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(OppoCheckBox.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(OppoCheckBox.class.getName());
    }

    public int getCompoundPaddingLeft() {
        int padding = super.getCompoundPaddingLeft();
        if (isLayoutRtl()) {
            return padding;
        }
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            return padding + buttonDrawable.getIntrinsicWidth();
        }
        return padding;
    }

    public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight();
        if (!isLayoutRtl()) {
            return padding;
        }
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            return padding + buttonDrawable.getIntrinsicWidth();
        }
        return padding;
    }

    public int getHorizontalOffsetForDrawables() {
        Drawable buttonDrawable = this.mButtonDrawable;
        return buttonDrawable != null ? buttonDrawable.getIntrinsicWidth() : 0;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            int verticalGravity = getGravity() & 112;
            int drawableHeight = buttonDrawable.getIntrinsicHeight();
            int drawableWidth = buttonDrawable.getIntrinsicWidth();
            int top = 0;
            switch (verticalGravity) {
                case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                    top = (getHeight() - drawableHeight) / 2;
                    break;
                case 80:
                    top = getHeight() - drawableHeight;
                    break;
            }
            buttonDrawable.setBounds(isLayoutRtl() ? getWidth() - drawableWidth : 0, top, isLayoutRtl() ? getWidth() : drawableWidth, top + drawableHeight);
            buttonDrawable.draw(canvas);
        }
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (getState() == 1) {
            mergeDrawableStates(drawableState, PARTSELECT_SET);
        }
        if (getState() == 2) {
            mergeDrawableStates(drawableState, ALLSELECT_SET);
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
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setState(ss.state);
        requestLayout();
    }
}

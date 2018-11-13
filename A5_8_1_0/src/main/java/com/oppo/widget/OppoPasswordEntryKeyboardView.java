package com.oppo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import com.color.screenshot.ColorLongshotUnsupported;
import oppo.R;

public class OppoPasswordEntryKeyboardView extends KeyboardView implements ColorLongshotUnsupported {
    public static final int KEYCODE_F1 = -103;
    static final int KEYCODE_NEXT_LANGUAGE = -104;
    static final int KEYCODE_OPTIONS = -100;
    static final int KEYCODE_SHIFT_LONGPRESS = -101;
    static final int KEYCODE_VOICE = -102;
    private boolean mShowCompleteKey;
    private boolean mShowWellKey;

    public OppoPasswordEntryKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OppoPasswordEntryKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mShowCompleteKey = false;
        this.mShowWellKey = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OppoPasswordEntryKeyboardView);
        if (a != null) {
            this.mShowCompleteKey = a.getBoolean(0, false);
            this.mShowWellKey = a.getBoolean(1, false);
        }
        a.recycle();
        setVariableLength(this.mShowCompleteKey);
        setShowWellKey(this.mShowWellKey);
    }

    public boolean setShifted(boolean shifted) {
        boolean result = super.setShifted(shifted);
        for (int index : getKeyboard().getShiftKeyIndices()) {
            invalidateKey(index);
        }
        return result;
    }

    public boolean isLongshotUnsupported() {
        return true;
    }

    public void showCompleteKey(boolean show) {
        if (show != this.mShowCompleteKey) {
            this.mShowCompleteKey = show;
            setVariableLength(this.mShowCompleteKey);
            invalidateAllKeys();
        }
    }

    public void showWellKey(boolean show) {
        if (show != this.mShowWellKey) {
            this.mShowWellKey = show;
            setShowWellKey(this.mShowWellKey);
            invalidateAllKeys();
        }
    }
}

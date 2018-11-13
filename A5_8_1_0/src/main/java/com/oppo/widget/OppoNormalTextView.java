package com.oppo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class OppoNormalTextView extends TextView {
    private static final boolean DBG = true;
    private static final String TAG = "OppoNormalTextView";

    public OppoNormalTextView(Context context) {
        super(context);
    }

    public OppoNormalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OppoNormalTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text.toString(), type);
    }
}

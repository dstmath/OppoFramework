package com.color.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class ColorAutofillSaveTitleTextView extends TextView {
    public ColorAutofillSaveTitleTextView(Context context) {
        this(context, null);
    }

    public ColorAutofillSaveTitleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorAutofillSaveTitleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs();
    }

    private void initAttrs() {
        setTextColor(getResources().getColor(201720998));
    }
}

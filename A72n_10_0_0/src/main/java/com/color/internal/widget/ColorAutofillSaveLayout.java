package com.color.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ColorAutofillSaveLayout extends LinearLayout {
    public ColorAutofillSaveLayout(Context context) {
        this(context, null);
    }

    public ColorAutofillSaveLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorAutofillSaveLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBackground();
    }

    private void initBackground() {
        setBackgroundResource(201852330);
    }
}

package com.color.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class ColorAutofillSaveTextView extends TextView {
    public ColorAutofillSaveTextView(Context context) {
        this(context, null);
    }

    public ColorAutofillSaveTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorAutofillSaveTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs();
    }

    private void initAttrs() {
        setTextColor(getResources().getColor(201720997));
        setBackgroundResource(201852331);
    }
}

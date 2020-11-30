package com.color.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ColorAutofillSaveLineView extends View {
    public ColorAutofillSaveLineView(Context context) {
        this(context, null);
    }

    public ColorAutofillSaveLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorAutofillSaveLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBackground();
    }

    private void initBackground() {
        setBackgroundColor(getResources().getColor(201720999));
    }
}

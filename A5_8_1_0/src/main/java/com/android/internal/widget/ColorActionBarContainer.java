package com.android.internal.widget;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class ColorActionBarContainer extends ActionBarContainer {
    private Drawable mBackground;
    protected final Class<?> mTagClass;

    public ColorActionBarContainer(Context context) {
        this(context, null);
    }

    public ColorActionBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTagClass = getClass();
        this.mBackground = null;
        setImportantForAccessibility(2);
        setContentDescription(null);
        if (isOppoStyle()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar);
            this.mBackground = a.getDrawable(2);
            a.recycle();
            setBackground(this.mBackground);
        }
    }

    public void setPrimaryBackground(Drawable bg) {
        if (isOppoStyle()) {
            if (this.mBackground != null) {
                this.mBackground.setCallback(null);
                unscheduleDrawable(this.mBackground);
            }
            this.mBackground = bg;
            if (bg != null) {
                bg.setCallback(this);
            }
            setBackground(bg);
            return;
        }
        super.setPrimaryBackground(bg);
    }

    public void setStackedBackground(Drawable bg) {
        if (!isOppoStyle()) {
            super.setStackedBackground(bg);
        }
    }
}

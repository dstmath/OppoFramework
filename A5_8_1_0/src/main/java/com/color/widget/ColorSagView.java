package com.color.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.LinearLayout;
import java.util.Locale;

public class ColorSagView extends LinearLayout {
    private Context mContext;
    private int mDefaultWidth;
    private Locale mLocale;

    public ColorSagView(Context context) {
        this(context, null);
    }

    public ColorSagView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorSagView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = null;
        this.mDefaultWidth = 96;
        this.mLocale = null;
        this.mContext = context;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        switch (((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRotation()) {
            case 0:
                setMeasuredDimension(1080, 80);
                return;
            case 1:
                setMeasuredDimension(80, 1080);
                return;
            case 2:
                setMeasuredDimension(1080, 80);
                return;
            case 3:
                setMeasuredDimension(80, 1080);
                return;
            default:
                return;
        }
    }

    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        super.onLayout(b, left, top, right, bottom);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

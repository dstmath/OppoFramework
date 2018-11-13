package com.color.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import oppo.R;

public class ColorMarkPreference extends CheckBoxPreference {
    public static final int HEAD_MARK = 1;
    public static final int TAIL_MARK = 0;
    int mMarkStyle;

    public ColorMarkPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMarkStyle = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorMarkPreference, defStyleAttr, 0);
        this.mMarkStyle = a.getInt(0, 0);
        a.recycle();
        setChecked(true);
    }

    public ColorMarkPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorMarkPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 201393255);
    }

    public ColorMarkPreference(Context context) {
        this(context, null);
    }

    public void setMarkStyle(int style) {
        this.mMarkStyle = style;
    }

    public int getMarkStyle() {
        return this.mMarkStyle;
    }

    protected void onBindView(View view) {
        View checkableView = view.findViewById(201458886);
        if (checkableView != null && (checkableView instanceof Checkable)) {
            if (this.mMarkStyle == 0) {
                checkableView.setVisibility(0);
                ((Checkable) checkableView).setChecked(isChecked());
            } else {
                checkableView.setVisibility(8);
            }
        }
        View checkableHeadView = view.findViewById(201458887);
        if (checkableHeadView != null && (checkableHeadView instanceof Checkable)) {
            if (this.mMarkStyle == 1) {
                checkableHeadView.setVisibility(0);
                ((Checkable) checkableHeadView).setChecked(isChecked());
            } else {
                checkableHeadView.setVisibility(8);
            }
        }
        super.onBindView(view);
    }
}

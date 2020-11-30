package com.color.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class ColorCheckableLayout extends LinearLayout implements Checkable {
    private Checkable mCheckable;

    public ColorCheckableLayout(Context context) {
        super(context);
    }

    public ColorCheckableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorCheckableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child instanceof Checkable) {
                    this.mCheckable = (Checkable) child;
                    return;
                }
            }
        }
    }

    @Override // android.widget.Checkable
    public void setChecked(boolean isChecked) {
        Checkable checkable = this.mCheckable;
        if (checkable != null) {
            checkable.setChecked(isChecked);
        }
    }

    @Override // android.widget.Checkable
    public boolean isChecked() {
        Checkable checkable = this.mCheckable;
        return checkable != null && checkable.isChecked();
    }

    @Override // android.widget.Checkable
    public void toggle() {
        Checkable checkable = this.mCheckable;
        if (checkable != null) {
            checkable.toggle();
        }
    }
}

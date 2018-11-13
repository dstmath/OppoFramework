package com.color.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class ColorClickableSpan extends ClickableSpan {
    SpannableStrClickListener mClickReference;
    private ColorStateList mTextColor;

    public interface SpannableStrClickListener {
        void onClick();
    }

    public ColorClickableSpan(Context context) {
        this.mTextColor = context.getColorStateList(201720887);
    }

    public void onClick(View widget) {
        if (this.mClickReference != null) {
            this.mClickReference.onClick();
        }
    }

    public void updateDrawState(TextPaint ds) {
        ds.setColor(this.mTextColor.getColorForState(ds.drawableState, 0));
    }

    public void setStatusBarClickListener(SpannableStrClickListener listener) {
        this.mClickReference = listener;
    }
}

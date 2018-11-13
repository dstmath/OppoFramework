package com.color.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ColorSubtitleView extends RelativeLayout {
    private static final String TAG = "ColorSubtitleView";
    private ImageView mDivider;
    private TextView mLeftTextView;
    private TextView mRightTextView;

    public ColorSubtitleView(Context context) {
        this(context, null);
    }

    public ColorSubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(201917554, this, true);
        this.mLeftTextView = (TextView) findViewById(201458953);
        this.mRightTextView = (TextView) findViewById(201458954);
        this.mDivider = (ImageView) findViewById(201458955);
    }

    public TextView getLeftTextView() {
        return this.mLeftTextView;
    }

    public TextView getRightTextView() {
        return this.mRightTextView;
    }

    public void setDividerHoritalMargin(int marginLeft, int marginRight) {
        if (this.mDivider != null) {
            LayoutParams lp = (LayoutParams) this.mDivider.getLayoutParams();
            lp.setMargins(marginLeft, 0, marginRight, 0);
            this.mDivider.setLayoutParams(lp);
        }
    }

    public void setImage(int resId) {
    }

    public void setImage(Drawable d) {
    }

    public void setMessage(int id) {
    }

    public void setMessage(String msg) {
    }

    public void setTextColor(int color) {
    }

    public void setTextSize(int size) {
    }
}

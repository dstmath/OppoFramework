package com.color.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

@RemoteViews.RemoteView
public class ColorNotificationProgressBar extends ProgressBar {
    public ColorNotificationProgressBar(Context context) {
        super(context);
        initProgressDrawable();
    }

    public ColorNotificationProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initProgressDrawable();
    }

    public ColorNotificationProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initProgressDrawable();
    }

    public ColorNotificationProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initProgressDrawable();
    }

    private void initProgressDrawable() {
        setProgressDrawable(getResources().getDrawable(201852332));
    }
}

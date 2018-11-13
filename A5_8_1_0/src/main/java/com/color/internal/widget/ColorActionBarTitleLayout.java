package com.color.internal.widget;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.color.util.ColorContextUtil;

public class ColorActionBarTitleLayout extends LinearLayout {
    private static final boolean DBG = false;
    private static final String TAG = "ColorActionBarTitleLayout";
    private int mIdSubTitle;
    private int mIdTitle;
    private int mMaxWidth;

    public ColorActionBarTitleLayout(Context context) {
        this(context, null);
    }

    public ColorActionBarTitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMaxWidth = 0;
        this.mIdTitle = -1;
        this.mIdSubTitle = -1;
        setImportantForAccessibility(2);
        if (isOppoStyle()) {
            setId(201458689);
            this.mIdTitle = ColorContextUtil.getResId(context, 201458902);
            this.mIdSubTitle = ColorContextUtil.getResId(context, 201458903);
            int padding = getResources().getDimensionPixelSize(201655507);
            setPadding(padding, 0, padding, 0);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isOppoStyle()) {
            TextView title = (TextView) findViewById(this.mIdTitle);
            if (title != null) {
                title.setGravity(17);
            }
            TextView subtitle = (TextView) findViewById(this.mIdSubTitle);
            if (subtitle != null) {
                LayoutParams lp = (LayoutParams) subtitle.getLayoutParams();
                if (lp != null) {
                    lp.topMargin = 0;
                }
            }
        }
    }

    private void setTitleView(TextView titleView) {
        titleView.setSelected(true);
        titleView.setHorizontalFadingEdgeEnabled(true);
        titleView.setEllipsize(TruncateAt.MARQUEE);
    }
}

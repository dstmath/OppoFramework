package com.color.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

public class ColorAppFullscreenLayoutPortrait extends LinearLayout {
    private Context mContext;
    private int mDefaultHeight;
    private Locale mLocale;

    public ColorAppFullscreenLayoutPortrait(Context context) {
        this(context, null);
    }

    public ColorAppFullscreenLayoutPortrait(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorAppFullscreenLayoutPortrait(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = null;
        this.mDefaultHeight = 96;
        this.mLocale = null;
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mLocale = context.getResources().getConfiguration().locale;
        boolean hasHeteroFeature = false;
        if (!(this.mContext == null || this.mContext.getPackageManager() == null)) {
            hasHeteroFeature = this.mContext.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        }
        this.mDefaultHeight = hasHeteroFeature ? 136 : 96;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mDefaultHeight, 1073741824));
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig != null && newConfig.locale != this.mLocale) {
            this.mLocale = newConfig.locale;
            TextView textView = (TextView) findViewById(201458991);
            if (textView != null) {
                textView.setText(201590172);
            }
        }
    }
}

package com.color.widget;

import android.content.Context;
import android.content.res.ColorBaseConfiguration;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.color.util.ColorTypeCastingHelper;
import java.util.Locale;

public class ColorAppFullscreenLayoutPortrait extends LinearLayout {
    private Context mContext;
    private int mDefaultHeight;
    private int mLastFontFlipFlag;
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
        this.mLastFontFlipFlag = -1;
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mLocale = context.getResources().getConfiguration().locale;
        boolean hasHeteroFeature = false;
        Context context2 = this.mContext;
        if (!(context2 == null || context2.getPackageManager() == null)) {
            hasHeteroFeature = this.mContext.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        }
        this.mDefaultHeight = hasHeteroFeature ? 136 : 96;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(this.mDefaultHeight, 1073741824));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig != null) {
            TextView textView = (TextView) findViewById(201458991);
            if (newConfig.locale != this.mLocale) {
                this.mLocale = newConfig.locale;
                if (textView != null) {
                    textView.setText(201590172);
                    return;
                }
                return;
            }
            ColorBaseConfiguration baseConfiguration = (ColorBaseConfiguration) ColorTypeCastingHelper.typeCasting(ColorBaseConfiguration.class, newConfig);
            if (baseConfiguration != null && baseConfiguration.mOppoExtraConfiguration.mFlipFont != this.mLastFontFlipFlag) {
                if (textView != null) {
                    textView.setTypeface(Typeface.DEFAULT);
                }
                this.mLastFontFlipFlag = baseConfiguration.mOppoExtraConfiguration.mFlipFont;
            }
        }
    }
}

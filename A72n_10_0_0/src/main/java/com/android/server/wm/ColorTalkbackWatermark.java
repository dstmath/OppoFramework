package com.android.server.wm;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.server.display.OppoBrightUtils;
import com.color.util.ColorDarkModeUtil;

class ColorTalkbackWatermark {
    private static float BASELINE_POSITION_RATIO_LANDSCAPE = 0.55f;
    private static float BASELINE_POSITION_RATIO_PORTRAIT = 0.77f;
    private static float GUIDE_TEXT_SIZE_DP = 16.0f;
    private static float STATUS_TEXT_SIZE_DP = 20.0f;
    private static final String TAG = "ColorTalkbackWatermark";
    private static final int TEXT_SIDE_MARGIN_PX = 72;
    private static final int TEXT_STATUS_GUIDE_MARGIN_PX = 30;
    private Context mContext;
    private Display mDisplay;
    private DisplayContent mDisplayContent;
    private StaticLayout mGuideTextLayout;
    private int mMarginTop;
    private TextPaint mPaintForGuide;
    private TextPaint mPaintForStatus;
    private int mScreenHeight;
    private int mScreenWidth;
    private Surface mSurface = new Surface();
    private SurfaceControl mSurfaceControl;
    private Color mTextColorForDark;
    private Color mTextColorForNormal;
    private String mTextForGuide;
    private String mTextForStatus;
    private int mWidthForStatus;

    ColorTalkbackWatermark(Context context, DisplayContent dc) {
        this.mDisplayContent = dc;
        this.mContext = context;
        this.mDisplay = dc.getDisplay();
        initResource(this.mContext.getResources().getConfiguration());
    }

    /* access modifiers changed from: package-private */
    public void drawIfNeeded() {
        Canvas c = null;
        try {
            c = this.mSurface.lockCanvas(new Rect(0, 0, this.mScreenWidth, this.mScreenHeight));
        } catch (Exception e) {
            Log.d(TAG, "drawIfNeeded: " + e);
        }
        if (c != null) {
            Paint.FontMetricsInt fmForStatus = this.mPaintForStatus.getFontMetricsInt();
            int heightForStatus = fmForStatus.descent - fmForStatus.ascent;
            int baselineForStatus = this.mMarginTop - fmForStatus.top;
            c.drawColor(0, PorterDuff.Mode.CLEAR);
            c.drawText(this.mTextForStatus, (float) ((this.mScreenWidth / 2) - (this.mWidthForStatus / 2)), (float) baselineForStatus, this.mPaintForStatus);
            c.save();
            c.translate(72.0f, (float) (this.mMarginTop + heightForStatus + 30));
            this.mGuideTextLayout.draw(c);
            c.restore();
            this.mSurface.unlockCanvasAndPost(c);
        }
    }

    /* access modifiers changed from: package-private */
    public void showWatermark() {
        initSurface(this.mDisplayContent);
        drawIfNeeded();
    }

    /* access modifiers changed from: package-private */
    public void hideWatermark() {
        destroySurface();
    }

    /* access modifiers changed from: package-private */
    public void destroySurface() {
        Surface surface = this.mSurface;
        if (surface != null) {
            surface.destroy();
            this.mSurface = null;
        }
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.hide();
            this.mSurfaceControl.remove();
            this.mSurfaceControl = null;
        }
    }

    private void initSurface(DisplayContent dc) {
        SurfaceControl ctrl = null;
        try {
            ctrl = dc.makeOverlay().setName(TAG).setBufferSize(this.mScreenWidth, this.mScreenHeight).setFormat(-2).build();
            ctrl.detachChildren();
            ctrl.setLayerStack(this.mDisplay.getLayerStack());
            ctrl.setLayer(1000001);
            ctrl.setPosition(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            ctrl.show();
            this.mSurface.copyFrom(ctrl);
        } catch (Surface.OutOfResourcesException e) {
            Log.d(TAG, e.toString());
        }
        this.mSurfaceControl = ctrl;
    }

    private void initResource(Configuration configuration) {
        int maxTextWidth;
        if (configuration != null) {
            DisplayMetrics metrics = this.mContext.getResources().getDisplayMetrics();
            this.mScreenWidth = metrics.widthPixels;
            this.mScreenHeight = metrics.heightPixels;
            float density = metrics.density;
            this.mTextColorForNormal = Color.valueOf(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, 0.2f);
            this.mTextColorForDark = Color.valueOf(1.0f, 1.0f, 1.0f, 0.2f);
            boolean isRtl = true;
            this.mPaintForStatus = new TextPaint(1);
            this.mPaintForGuide = new TextPaint(1);
            this.mPaintForStatus.setTextSize(STATUS_TEXT_SIZE_DP * density);
            this.mPaintForGuide.setTextSize(GUIDE_TEXT_SIZE_DP * density);
            if (configuration.orientation == 2) {
                this.mMarginTop = (int) (((float) this.mScreenHeight) * BASELINE_POSITION_RATIO_LANDSCAPE);
                maxTextWidth = this.mScreenWidth - 144;
            } else {
                this.mMarginTop = (int) (((float) this.mScreenHeight) * BASELINE_POSITION_RATIO_PORTRAIT);
                maxTextWidth = this.mScreenWidth - 144;
            }
            this.mPaintForStatus.setTypeface(Typeface.create("sans-serif-medium", 0));
            this.mTextForStatus = this.mContext.getResources().getString(201590228);
            this.mTextForGuide = this.mContext.getResources().getString(201590229);
            this.mWidthForStatus = (int) this.mPaintForStatus.measureText(this.mTextForStatus);
            Color textColor = ColorDarkModeUtil.isNightMode(this.mContext) ? this.mTextColorForDark : this.mTextColorForNormal;
            this.mPaintForStatus.setColor(textColor.toArgb());
            this.mPaintForGuide.setColor(textColor.toArgb());
            if (configuration.getLayoutDirection() != 1) {
                isRtl = false;
            }
            String str = this.mTextForGuide;
            this.mGuideTextLayout = StaticLayout.Builder.obtain(str, 0, str.length(), this.mPaintForGuide, maxTextWidth).setAlignment(moreThanOneLine(this.mTextForGuide, this.mPaintForGuide, maxTextWidth) ? Layout.Alignment.ALIGN_LEFT : Layout.Alignment.ALIGN_CENTER).setTextDirection(isRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR).build();
        }
    }

    private boolean moreThanOneLine(CharSequence sequence, TextPaint paint, int width) {
        StaticLayout temp = StaticLayout.Builder.obtain(sequence, 0, sequence.length(), paint, width).build();
        if (temp == null || temp.getLineCount() <= 1) {
            return false;
        }
        return true;
    }
}

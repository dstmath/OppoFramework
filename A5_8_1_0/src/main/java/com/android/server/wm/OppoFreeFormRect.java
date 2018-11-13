package com.android.server.wm;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.server.display.OppoBrightUtils;

class OppoFreeFormRect {
    private static final String TAG = "OppoFreeFormRect";
    private int mAlphaFull = 255;
    private int mAlphaHalf = 24;
    private final Rect mBounds = new Rect();
    private final Rect mClipRect = new Rect();
    private int mDisplayId;
    private int mLineWidth = 2;
    private Point mMaxPoint = new Point();
    private int mMinHeight;
    private int mMinWidth;
    private final PorterDuffXfermode mModeClear = new PorterDuffXfermode(Mode.DST);
    private final PorterDuffXfermode mModeSrc = new PorterDuffXfermode(Mode.SRC);
    private String mName;
    private Paint mPaint;
    private Point mPoint;
    private int mRoundWidth = 5;
    private final WindowManagerService mService;
    private Surface mSurface = new Surface();
    private SurfaceControl mSurfaceControl;

    public OppoFreeFormRect(Display display, WindowManagerService service, String name, int minWidth, int minHeight, Point maxPoint) {
        this.mService = service;
        this.mDisplayId = display.getDisplayId();
        this.mName = name;
        this.mPoint = new Point();
        display.getSize(this.mPoint);
        this.mMaxPoint.set(maxPoint.x, maxPoint.y);
        this.mMinWidth = minWidth;
        this.mMinHeight = minHeight;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        this.mLineWidth = WindowManagerService.dipToPixel(this.mLineWidth, displayMetrics);
        this.mRoundWidth = WindowManagerService.dipToPixel(this.mRoundWidth, displayMetrics);
    }

    private void createSurface(int layer, Rect rect) {
        try {
            this.mSurfaceControl = new SurfaceControl(this.mService.mFxSession, TAG, this.mPoint.x, this.mPoint.y, -3, 4);
            this.mSurfaceControl.setLayerStack(this.mDisplayId);
            this.mSurfaceControl.setLayer(layer);
            this.mSurfaceControl.setPosition(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            this.mSurfaceControl.show();
            this.mSurface.copyFrom(this.mSurfaceControl);
        } catch (Exception e) {
        }
        this.mPaint = new Paint();
        this.mPaint.setColor(-16711936);
        this.mPaint.setXfermode(this.mModeSrc);
        this.mBounds.set(0, 0, this.mPoint.x, this.mPoint.y);
    }

    private boolean isMinOrMax(Rect rect) {
        if (rect.width() == this.mMinWidth && rect.height() == this.mMinHeight) {
            return true;
        }
        if (rect.width() == this.mMaxPoint.x && rect.height() == this.mMaxPoint.y) {
            return true;
        }
        return false;
    }

    private void setClipRect(Rect rect) {
        this.mClipRect.set(rect);
        this.mClipRect.inset(-this.mLineWidth, -this.mLineWidth);
    }

    void showSurface(int layer, Rect rect) {
        if (this.mSurfaceControl == null) {
            SurfaceControl.openTransaction();
            createSurface(layer, rect);
            SurfaceControl.closeTransaction();
        }
        try {
            Canvas c = this.mSurface.lockCanvas(this.mBounds);
            if (c != null) {
                Log.d(TAG, "oppo freeform: showSurface drawRect! Canvas:" + c + " rect:" + rect);
                c.drawColor(0, Mode.CLEAR);
                setClipRect(rect);
                c.clipRect(this.mClipRect, Op.REPLACE);
                if (isMinOrMax(rect)) {
                    this.mPaint.setColor(-65536);
                } else {
                    this.mPaint.setColor(-16711936);
                }
                this.mPaint.setAlpha(this.mAlphaHalf);
                this.mPaint.setXfermode(this.mModeSrc);
                this.mPaint.setStyle(Style.FILL);
                c.drawRoundRect((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, (float) this.mRoundWidth, (float) this.mRoundWidth, this.mPaint);
                this.mPaint.setAlpha(this.mAlphaFull);
                this.mPaint.setStyle(Style.STROKE);
                this.mPaint.setStrokeWidth((float) this.mLineWidth);
                this.mPaint.setAntiAlias(false);
                c.drawRoundRect((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, (float) this.mRoundWidth, (float) this.mRoundWidth, this.mPaint);
                this.mSurface.unlockCanvasAndPost(c);
            }
        } catch (Exception e) {
        }
    }

    void destroySurface() {
        if (this.mSurface != null) {
            this.mSurface.destroy();
            this.mSurface = null;
        }
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.hide();
            this.mSurfaceControl.destroy();
            this.mSurfaceControl = null;
        }
    }
}

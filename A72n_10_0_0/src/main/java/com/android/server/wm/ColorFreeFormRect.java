package com.android.server.wm;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;

public class ColorFreeFormRect {
    private static final String TAG = "ColorFreeFormRect";
    private int mAlphaFull = 255;
    private int mAlphaHalf = 24;
    private final Rect mBounds = new Rect();
    private final Rect mClipRect = new Rect();
    private int mDisplayId;
    private int mLineWidth = 2;
    private Point mMaxPoint = new Point();
    private int mMinHeight;
    private int mMinWidth;
    private final PorterDuffXfermode mModeClear = new PorterDuffXfermode(PorterDuff.Mode.DST);
    private final PorterDuffXfermode mModeSrc = new PorterDuffXfermode(PorterDuff.Mode.SRC);
    private String mName;
    private Paint mPaint;
    private Point mPoint;
    private int mRoundWidth = 5;
    private final WindowManagerService mService;
    private Surface mSurface = new Surface();
    private SurfaceControl mSurfaceControl;

    public ColorFreeFormRect(Display display2, WindowManagerService service, String name, int minWidth, int minHeight, Point maxPoint) {
        this.mService = service;
        this.mDisplayId = display2.getDisplayId();
        this.mName = name;
        this.mPoint = new Point();
        display2.getSize(this.mPoint);
        this.mMaxPoint.set(maxPoint.x, maxPoint.y);
        this.mMinWidth = minWidth;
        this.mMinHeight = minHeight;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display2.getMetrics(displayMetrics);
        this.mLineWidth = WindowManagerService.dipToPixel(this.mLineWidth, displayMetrics);
        this.mRoundWidth = WindowManagerService.dipToPixel(this.mRoundWidth, displayMetrics);
    }

    private void createSurface(int layer, Rect rect) {
        try {
            this.mSurfaceControl = this.mService.makeSurfaceBuilder(this.mService.getDefaultDisplayContentLocked().getSession()).setBufferSize(this.mPoint.x, this.mPoint.y).setName(TAG).setFormat(-3).build();
            this.mSurfaceControl.setLayerStack(this.mDisplayId);
            this.mSurfaceControl.setLayer(layer);
            this.mSurfaceControl.setPosition(0.0f, 0.0f);
            this.mSurfaceControl.show();
            this.mSurface.copyFrom(this.mSurfaceControl);
        } catch (Exception e) {
            Log.v(TAG, "createSurface here exception: ");
        }
        this.mPaint = new Paint();
        this.mPaint.setColor(-16711936);
        this.mPaint.setXfermode(this.mModeSrc);
        this.mBounds.set(0, 0, this.mPoint.x, this.mPoint.y);
    }

    private boolean isMinOrMax(Rect rect) {
        return (rect.width() == this.mMinWidth && rect.height() == this.mMinHeight) || (rect.width() == this.mMaxPoint.x && rect.height() == this.mMaxPoint.y);
    }

    private void setClipRect(Rect rect) {
        this.mClipRect.set(rect);
        Rect rect2 = this.mClipRect;
        int i = this.mLineWidth;
        rect2.inset(-i, -i);
    }

    /* access modifiers changed from: package-private */
    public void showSurface(int layer, Rect rect) {
        if (this.mSurfaceControl == null) {
            SurfaceControl.openTransaction();
            createSurface(layer, rect);
            SurfaceControl.closeTransaction();
        }
        try {
            Canvas c = this.mSurface.lockCanvas(this.mBounds);
            if (c != null) {
                Log.d(TAG, "oppo freeform: showSurface drawRect! Canvas:" + c + " rect:" + rect);
                c.drawColor(0, PorterDuff.Mode.CLEAR);
                setClipRect(rect);
                c.clipRect(this.mClipRect, Region.Op.INTERSECT);
                if (isMinOrMax(rect)) {
                    this.mPaint.setColor(-65536);
                } else {
                    this.mPaint.setColor(-16711936);
                }
                this.mPaint.setAlpha(this.mAlphaHalf);
                this.mPaint.setXfermode(this.mModeSrc);
                this.mPaint.setStyle(Paint.Style.FILL);
                c.drawRoundRect((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, (float) this.mRoundWidth, (float) this.mRoundWidth, this.mPaint);
                this.mPaint.setAlpha(this.mAlphaFull);
                this.mPaint.setStyle(Paint.Style.STROKE);
                this.mPaint.setStrokeWidth((float) this.mLineWidth);
                this.mPaint.setAntiAlias(false);
                c.drawRoundRect((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, (float) this.mRoundWidth, (float) this.mRoundWidth, this.mPaint);
                this.mSurface.unlockCanvasAndPost(c);
            }
        } catch (Exception e) {
            Log.v(TAG, "showSurface here exception: " + e);
        }
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
}

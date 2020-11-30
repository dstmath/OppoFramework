package com.android.server.wm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.server.display.OppoBrightUtils;

/* access modifiers changed from: package-private */
public class EmulatorDisplayOverlay {
    private static final String TAG = "WindowManager";
    private boolean mDrawNeeded;
    private int mLastDH;
    private int mLastDW;
    private Drawable mOverlay;
    private int mRotation;
    private Point mScreenSize;
    private final Surface mSurface = new Surface();
    private final SurfaceControl mSurfaceControl;
    private boolean mVisible;

    public EmulatorDisplayOverlay(Context context, DisplayContent dc, int zOrder) {
        Display display = dc.getDisplay();
        this.mScreenSize = new Point();
        display.getSize(this.mScreenSize);
        SurfaceControl ctrl = null;
        try {
            ctrl = dc.makeOverlay().setName("EmulatorDisplayOverlay").setBufferSize(this.mScreenSize.x, this.mScreenSize.y).setFormat(-3).build();
            ctrl.setLayer(zOrder);
            ctrl.setPosition(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            ctrl.show();
            this.mSurface.copyFrom(ctrl);
        } catch (Surface.OutOfResourcesException e) {
        }
        this.mSurfaceControl = ctrl;
        this.mDrawNeeded = true;
        this.mOverlay = context.getDrawable(17302219);
    }

    private void drawIfNeeded() {
        if (this.mDrawNeeded && this.mVisible) {
            this.mDrawNeeded = false;
            Canvas c = null;
            try {
                c = this.mSurface.lockCanvas(new Rect(0, 0, this.mScreenSize.x, this.mScreenSize.y));
            } catch (Surface.OutOfResourcesException | IllegalArgumentException e) {
            }
            if (c != null) {
                c.drawColor(0, PorterDuff.Mode.SRC);
                this.mSurfaceControl.setPosition(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
                int size = Math.max(this.mScreenSize.x, this.mScreenSize.y);
                this.mOverlay.setBounds(0, 0, size, size);
                this.mOverlay.draw(c);
                this.mSurface.unlockCanvasAndPost(c);
            }
        }
    }

    public void setVisibility(boolean on) {
        if (this.mSurfaceControl != null) {
            this.mVisible = on;
            drawIfNeeded();
            if (on) {
                this.mSurfaceControl.show();
            } else {
                this.mSurfaceControl.hide();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void positionSurface(int dw, int dh, int rotation) {
        if (this.mLastDW != dw || this.mLastDH != dh || this.mRotation != rotation) {
            this.mLastDW = dw;
            this.mLastDH = dh;
            this.mDrawNeeded = true;
            this.mRotation = rotation;
            drawIfNeeded();
        }
    }
}

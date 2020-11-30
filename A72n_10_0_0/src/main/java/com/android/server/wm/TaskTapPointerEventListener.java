package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import com.android.server.oppo.OppoUsageService;

public class TaskTapPointerEventListener implements WindowManagerPolicyConstants.PointerEventListener {
    private final DisplayContent mDisplayContent;
    private int mPointerIconType = 1;
    private final WindowManagerService mService;
    private final Rect mTmpRect = new Rect();
    private final Region mTouchExcludeRegion = new Region();

    public TaskTapPointerEventListener(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            synchronized (this) {
                OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).debugLogUtil(ColorZoomWindowDebugUtil.KEY_TTPEVENTLISTENER_ONPOINTEREVENT, this.mTouchExcludeRegion, Integer.valueOf(x), Integer.valueOf(y));
                if (!this.mTouchExcludeRegion.contains(x, y)) {
                    this.mService.mTaskPositioningController.handleTapOutsideTask(this.mDisplayContent, x, y);
                }
            }
        } else if (actionMasked == 7 || actionMasked == 9) {
            int x2 = (int) motionEvent.getX();
            int y2 = (int) motionEvent.getY();
            Task task = this.mDisplayContent.findTaskForResizePoint(x2, y2);
            int iconType = 1;
            if (task != null) {
                task.getDimBounds(this.mTmpRect);
                if (!this.mTmpRect.isEmpty() && !this.mTmpRect.contains(x2, y2)) {
                    int i = this.mTmpRect.left;
                    int i2 = OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_PHOENIX;
                    if (x2 < i) {
                        if (y2 < this.mTmpRect.top) {
                            i2 = 1017;
                        } else if (y2 > this.mTmpRect.bottom) {
                            i2 = 1016;
                        }
                        iconType = i2;
                    } else if (x2 > this.mTmpRect.right) {
                        if (y2 < this.mTmpRect.top) {
                            i2 = 1016;
                        } else if (y2 > this.mTmpRect.bottom) {
                            i2 = 1017;
                        }
                        iconType = i2;
                    } else if (y2 < this.mTmpRect.top || y2 > this.mTmpRect.bottom) {
                        iconType = OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_RECOVERY_INFO;
                    }
                }
            }
            if (this.mPointerIconType != iconType) {
                this.mPointerIconType = iconType;
                if (this.mPointerIconType == 1) {
                    this.mService.mH.removeMessages(55);
                    this.mService.mH.obtainMessage(55, x2, y2, this.mDisplayContent).sendToTarget();
                    return;
                }
                InputManager.getInstance().setPointerIconType(this.mPointerIconType);
            }
        } else if (actionMasked == 10) {
            int x3 = (int) motionEvent.getX();
            int y3 = (int) motionEvent.getY();
            if (this.mPointerIconType != 1) {
                this.mPointerIconType = 1;
                this.mService.mH.removeMessages(55);
                this.mService.mH.obtainMessage(55, x3, y3, this.mDisplayContent).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mTouchExcludeRegion.set(newRegion);
        }
    }
}

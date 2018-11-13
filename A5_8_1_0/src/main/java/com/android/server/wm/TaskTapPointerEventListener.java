package com.android.server.wm;

import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.util.BoostFramework;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy.PointerEventListener;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityStackSupervisor;
import com.oppo.debug.InputLog;

public class TaskTapPointerEventListener implements PointerEventListener {
    private final DisplayContent mDisplayContent;
    public BoostFramework mPerfObj = null;
    private int mPointerIconType = 1;
    private final WindowManagerService mService;
    private final Rect mTmpRect = new Rect();
    private final Region mTouchExcludeRegion = new Region();

    public TaskTapPointerEventListener(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
        if (this.mPerfObj == null) {
            this.mPerfObj = new BoostFramework();
        }
    }

    public void onPointerEvent(MotionEvent motionEvent, int displayId) {
        if (displayId == getDisplayId()) {
            onPointerEvent(motionEvent);
        }
    }

    private boolean isSwipeDownFromBottom(MotionEvent event) {
        if (this.mService.mPolicy.getNavigationBarStatus() != 2 || event.getAction() != 0) {
            return false;
        }
        int downY = (int) event.getRawY();
        int downX = (int) event.getRawX();
        int screenHeight = this.mService.mScreenRect.bottom;
        int screenWidth = this.mService.mScreenRect.right;
        if (this.mService.getDefaultDisplayRotation() == 0 || this.mService.getDefaultDisplayRotation() == 2) {
            if (downY >= screenHeight - this.mService.mGestrueAreaHeight && event.getDownTime() == event.getEventTime()) {
                return true;
            }
        } else if (this.mService.getDefaultDisplayRotation() != 1) {
            return this.mService.getDefaultDisplayRotation() == 3 && downX <= this.mService.mGestrueAreaHeight && event.getDownTime() == event.getEventTime();
        } else {
            if (downX >= screenWidth - this.mService.mGestrueAreaHeight && event.getDownTime() == event.getEventTime()) {
                return true;
            }
        }
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        int x;
        int y;
        switch (motionEvent.getAction() & 255) {
            case 0:
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                if (isSwipeDownFromBottom(motionEvent)) {
                    if (InputLog.DEBUG) {
                        Slog.v("TaskTapPointerEventListener", "isSwipeFromBottom true motionEvent" + motionEvent);
                        break;
                    }
                }
                synchronized (this) {
                    if (!this.mTouchExcludeRegion.contains(x, y)) {
                        this.mService.mH.obtainMessage(31, x, y, this.mDisplayContent).sendToTarget();
                    }
                }
                break;
            case 7:
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                Task task = this.mDisplayContent.findTaskForResizePoint(x, y);
                int iconType = 1;
                if (task != null) {
                    task.getDimBounds(this.mTmpRect);
                    if (!(this.mTmpRect.isEmpty() || (this.mTmpRect.contains(x, y) ^ 1) == 0)) {
                        if (x < this.mTmpRect.left) {
                            iconType = y < this.mTmpRect.top ? 1017 : y > this.mTmpRect.bottom ? 1016 : 1014;
                        } else if (x > this.mTmpRect.right) {
                            iconType = y < this.mTmpRect.top ? 1016 : y > this.mTmpRect.bottom ? 1017 : 1014;
                        } else if (y < this.mTmpRect.top || y > this.mTmpRect.bottom) {
                            iconType = 1015;
                        }
                    }
                }
                if (this.mPointerIconType != iconType) {
                    this.mPointerIconType = iconType;
                    if (this.mPointerIconType != 1) {
                        InputManager.getInstance().setPointerIconType(this.mPointerIconType);
                        break;
                    } else {
                        this.mService.mH.obtainMessage(55, x, y, this.mDisplayContent).sendToTarget();
                        break;
                    }
                }
                break;
        }
        if (ActivityManagerService.mIsPerfLockAcquired) {
            ActivityManagerService.mPerf.perfLockRelease();
            ActivityManagerService.mIsPerfLockAcquired = false;
        }
        if (ActivityStackSupervisor.mPerfSendTapHint && this.mPerfObj != null) {
            this.mPerfObj.perfHint(4163, null);
            ActivityStackSupervisor.mPerfSendTapHint = false;
        }
    }

    void setTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mTouchExcludeRegion.set(newRegion);
        }
    }

    private int getDisplayId() {
        return this.mDisplayContent.getDisplayId();
    }
}

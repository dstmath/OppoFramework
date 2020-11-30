package com.android.server.wm;

import android.os.Looper;
import android.util.Slog;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;
import com.mediatek.server.wm.WmsExt;

/* access modifiers changed from: package-private */
public class DragInputEventReceiver extends InputEventReceiver {
    private final DragDropController mDragDropController;
    private boolean mIsStartEvent = true;
    private boolean mMuteInput = false;
    private boolean mStylusButtonDownAtStart;

    DragInputEventReceiver(InputChannel inputChannel, Looper looper, DragDropController controller) {
        super(inputChannel, looper);
        this.mDragDropController = controller;
    }

    public void onInputEvent(InputEvent event) {
        boolean handled = false;
        try {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                if (!this.mMuteInput) {
                    MotionEvent motionEvent = (MotionEvent) event;
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    boolean z = false;
                    boolean isStylusButtonDown = (motionEvent.getButtonState() & 32) != 0;
                    if (this.mIsStartEvent) {
                        this.mStylusButtonDownAtStart = isStylusButtonDown;
                        this.mIsStartEvent = false;
                    }
                    int action = motionEvent.getAction();
                    if (action != 0) {
                        if (action == 1) {
                            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                                Slog.d(WmsExt.TAG, "Got UP on move channel; dropping at " + newX + "," + newY);
                            }
                            this.mMuteInput = true;
                        } else if (action != 2) {
                            if (action != 3) {
                                finishInputEvent(event, false);
                                return;
                            }
                            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                                Slog.d(WmsExt.TAG, "Drag cancelled!");
                            }
                            this.mMuteInput = true;
                        } else if (this.mStylusButtonDownAtStart && !isStylusButtonDown) {
                            if (WindowManagerDebugConfig.DEBUG_DRAG) {
                                Slog.d(WmsExt.TAG, "Button no longer pressed; dropping at " + newX + "," + newY);
                            }
                            this.mMuteInput = true;
                        }
                        DragDropController dragDropController = this.mDragDropController;
                        if (!this.mMuteInput) {
                            z = true;
                        }
                        dragDropController.handleMotionEvent(z, newX, newY);
                        handled = true;
                        finishInputEvent(event, handled);
                        return;
                    }
                    if (WindowManagerDebugConfig.DEBUG_DRAG) {
                        Slog.w(WmsExt.TAG, "Unexpected ACTION_DOWN in drag layer");
                    }
                    finishInputEvent(event, false);
                    return;
                }
            }
            finishInputEvent(event, false);
        } catch (Exception e) {
            Slog.e(WmsExt.TAG, "Exception caught by drag handleMotion", e);
        } catch (Throwable th) {
            finishInputEvent(event, false);
            throw th;
        }
    }
}

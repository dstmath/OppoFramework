package com.android.server.accessibility;

import android.accessibilityservice.IAccessibilityServiceClient;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.os.SomeArgs;
import com.android.server.display.OppoBrightUtils;
import java.util.List;

public class MotionEventInjector implements EventStreamTransformation {
    private static final String LOG_TAG = "MotionEventInjector";
    private static final int MAX_POINTERS = 11;
    private static final int MESSAGE_INJECT_EVENTS = 2;
    private static final int MESSAGE_SEND_MOTION_EVENT = 1;
    private final Handler mHandler;
    private boolean mIsDestroyed = false;
    private EventStreamTransformation mNext;
    private final SparseArray<Boolean> mOpenGesturesInProgress = new SparseArray();
    private PointerCoords[] mPointerCoords = new PointerCoords[11];
    private PointerProperties[] mPointerProperties = new PointerProperties[11];
    private int mSequenceForCurrentGesture;
    private IAccessibilityServiceClient mServiceInterfaceForCurrentGesture;
    private int mSourceOfInjectedGesture = 0;

    private class Callback implements android.os.Handler.Callback {
        /* synthetic */ Callback(MotionEventInjector this$0, Callback callback) {
            this();
        }

        private Callback() {
        }

        public boolean handleMessage(Message message) {
            if (message.what == 2) {
                SomeArgs args = message.obj;
                MotionEventInjector.this.injectEventsMainThread((List) args.arg1, (IAccessibilityServiceClient) args.arg2, args.argi1);
                args.recycle();
                return true;
            } else if (message.what != 1) {
                throw new IllegalArgumentException("Unknown message: " + message.what);
            } else {
                MotionEvent motionEvent = message.obj;
                MotionEventInjector.this.sendMotionEventToNext(motionEvent, motionEvent, 1073741824);
                if (!MotionEventInjector.this.mHandler.hasMessages(1)) {
                    MotionEventInjector.this.notifyService(true);
                }
                return true;
            }
        }
    }

    public MotionEventInjector(Looper looper) {
        this.mHandler = new Handler(looper, new Callback(this, null));
    }

    public void injectEvents(List<MotionEvent> events, IAccessibilityServiceClient serviceInterface, int sequence) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = events;
        args.arg2 = serviceInterface;
        args.argi1 = sequence;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, args));
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        cancelAnyPendingInjectedEvents();
        sendMotionEventToNext(event, rawEvent, policyFlags);
    }

    public void onKeyEvent(KeyEvent event, int policyFlags) {
        if (this.mNext != null) {
            this.mNext.onKeyEvent(event, policyFlags);
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (this.mNext != null) {
            this.mNext.onAccessibilityEvent(event);
        }
    }

    public void setNext(EventStreamTransformation next) {
        this.mNext = next;
    }

    public void clearEvents(int inputSource) {
        if (!this.mHandler.hasMessages(1)) {
            this.mOpenGesturesInProgress.put(inputSource, Boolean.valueOf(false));
        }
    }

    public void onDestroy() {
        cancelAnyPendingInjectedEvents();
        this.mIsDestroyed = true;
    }

    private void injectEventsMainThread(List<MotionEvent> events, IAccessibilityServiceClient serviceInterface, int sequence) {
        if (this.mIsDestroyed) {
            try {
                serviceInterface.onPerformGestureResult(sequence, false);
            } catch (Throwable re) {
                Slog.e(LOG_TAG, "Error sending status with mIsDestroyed to " + serviceInterface, re);
            }
            return;
        }
        cancelAnyPendingInjectedEvents();
        this.mSourceOfInjectedGesture = ((MotionEvent) events.get(0)).getSource();
        cancelAnyGestureInProgress(this.mSourceOfInjectedGesture);
        this.mServiceInterfaceForCurrentGesture = serviceInterface;
        this.mSequenceForCurrentGesture = sequence;
        if (this.mNext == null) {
            notifyService(false);
            return;
        }
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < events.size(); i++) {
            MotionEvent event = (MotionEvent) events.get(i);
            int numPointers = event.getPointerCount();
            if (numPointers > this.mPointerCoords.length) {
                this.mPointerCoords = new PointerCoords[numPointers];
                this.mPointerProperties = new PointerProperties[numPointers];
            }
            for (int j = 0; j < numPointers; j++) {
                if (this.mPointerCoords[j] == null) {
                    this.mPointerCoords[j] = new PointerCoords();
                    this.mPointerProperties[j] = new PointerProperties();
                }
                event.getPointerCoords(j, this.mPointerCoords[j]);
                event.getPointerProperties(j, this.mPointerProperties[j]);
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, MotionEvent.obtain(event.getDownTime() + startTime, event.getEventTime() + startTime, event.getAction(), numPointers, this.mPointerProperties, this.mPointerCoords, event.getMetaState(), event.getButtonState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), event.getSource(), event.getFlags())), event.getEventTime());
        }
    }

    private void sendMotionEventToNext(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (this.mNext != null) {
            this.mNext.onMotionEvent(event, rawEvent, policyFlags);
            if (event.getActionMasked() == 0) {
                this.mOpenGesturesInProgress.put(event.getSource(), Boolean.valueOf(true));
            }
            if (event.getActionMasked() == 1 || event.getActionMasked() == 3) {
                this.mOpenGesturesInProgress.put(event.getSource(), Boolean.valueOf(false));
            }
        }
    }

    private void cancelAnyGestureInProgress(int source) {
        if (this.mNext != null && ((Boolean) this.mOpenGesturesInProgress.get(source, Boolean.valueOf(false))).booleanValue()) {
            long now = SystemClock.uptimeMillis();
            MotionEvent cancelEvent = MotionEvent.obtain(now, now, 3, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, 0);
            sendMotionEventToNext(cancelEvent, cancelEvent, 1073741824);
        }
    }

    private void cancelAnyPendingInjectedEvents() {
        if (this.mHandler.hasMessages(1)) {
            cancelAnyGestureInProgress(this.mSourceOfInjectedGesture);
            this.mHandler.removeMessages(1);
            notifyService(false);
        }
    }

    private void notifyService(boolean success) {
        try {
            this.mServiceInterfaceForCurrentGesture.onPerformGestureResult(this.mSequenceForCurrentGesture, success);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error sending motion event injection status to " + this.mServiceInterfaceForCurrentGesture, re);
        }
    }
}

package com.android.server.policy;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.OppoScreenDragUtil;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.widget.OverScroller;
import com.android.server.display.OppoBrightUtils;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.usb.descriptors.UsbACInterface;

public class SystemGesturesPointerEventListener implements PointerEventListener {
    private static final boolean DEBUG = false;
    private static final int MAX_FLING_TIME_MILLIS = 5000;
    private static final int MAX_TRACKED_POINTERS = 32;
    private static final int SWIPE_FROM_BOTTOM = 2;
    private static final int SWIPE_FROM_LEFT = 4;
    private static final int SWIPE_FROM_RIGHT = 3;
    private static final int SWIPE_FROM_TOP = 1;
    private static final int SWIPE_NONE = 0;
    private static final long SWIPE_TIMEOUT_MS = 500;
    private static final String TAG = "SystemGestures";
    private static final int UNTRACKED_POINTER = -1;
    private final Callbacks mCallbacks;
    private final Context mContext;
    private boolean mDebugFireable;
    private final int[] mDownPointerId = new int[32];
    private int mDownPointers;
    private final long[] mDownTime = new long[32];
    private final float[] mDownX = new float[32];
    private final float[] mDownY = new float[32];
    private GestureDetector mGestureDetector;
    private long mLastFlingTime;
    private boolean mMouseHoveringAtEdge;
    private OverScroller mOverscroller;
    private boolean mScrollFired;
    private final int mSwipeDistanceThreshold;
    private boolean mSwipeFireable;
    private final int mSwipeStartThreshold;
    int screenHeight;
    int screenWidth;

    interface Callbacks {
        void onDebug();

        void onDown();

        void onFling(int i);

        void onMouseHoverAtBottom();

        void onMouseHoverAtTop();

        void onMouseLeaveFromEdge();

        void onScroll(boolean z);

        void onSwipeFromBottom();

        void onSwipeFromLeft();

        void onSwipeFromRight();

        void onSwipeFromTop();

        void onUpOrCancel();
    }

    private final class FlingGestureDetector extends SimpleOnGestureListener {
        /* synthetic */ FlingGestureDetector(SystemGesturesPointerEventListener this$0, FlingGestureDetector -this1) {
            this();
        }

        private FlingGestureDetector() {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            if (!SystemGesturesPointerEventListener.this.mOverscroller.isFinished()) {
                SystemGesturesPointerEventListener.this.mOverscroller.forceFinished(true);
            }
            return true;
        }

        public boolean onFling(MotionEvent down, MotionEvent up, float velocityX, float velocityY) {
            SystemGesturesPointerEventListener.this.mOverscroller.computeScrollOffset();
            long now = SystemClock.uptimeMillis();
            if (SystemGesturesPointerEventListener.this.mLastFlingTime != 0 && now > SystemGesturesPointerEventListener.this.mLastFlingTime + FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK) {
                SystemGesturesPointerEventListener.this.mOverscroller.forceFinished(true);
            }
            SystemGesturesPointerEventListener.this.mOverscroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            int duration = SystemGesturesPointerEventListener.this.mOverscroller.getDuration();
            if (duration > 5000) {
                duration = 5000;
            }
            SystemGesturesPointerEventListener.this.mLastFlingTime = now;
            SystemGesturesPointerEventListener.this.mCallbacks.onFling(duration);
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!SystemGesturesPointerEventListener.this.mScrollFired) {
                SystemGesturesPointerEventListener.this.mCallbacks.onScroll(true);
                SystemGesturesPointerEventListener.this.mScrollFired = true;
            }
            return true;
        }
    }

    public SystemGesturesPointerEventListener(Context context, Callbacks callbacks) {
        this.mContext = context;
        this.mCallbacks = (Callbacks) checkNull("callbacks", callbacks);
        this.mSwipeStartThreshold = ((Context) checkNull("context", context)).getResources().getDimensionPixelSize(17105258);
        this.mSwipeDistanceThreshold = this.mSwipeStartThreshold;
    }

    private static <T> T checkNull(String name, T arg) {
        if (arg != null) {
            return arg;
        }
        throw new IllegalArgumentException(name + " must not be null");
    }

    public void systemReady() {
        this.mGestureDetector = new GestureDetector(this.mContext, new FlingGestureDetector(this, null), new Handler(Looper.myLooper()));
        this.mOverscroller = new OverScroller(this.mContext);
    }

    public void onPointerEvent(MotionEvent event) {
        boolean z = true;
        boolean z2 = false;
        if (OppoScreenDragUtil.isOffsetState()) {
            OppoScreenDragUtil.setEventLocation(event);
        }
        if (this.mGestureDetector != null && event.isTouchEvent()) {
            this.mGestureDetector.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case 0:
                this.mSwipeFireable = true;
                this.mDebugFireable = true;
                this.mScrollFired = false;
                this.mDownPointers = 0;
                captureDown(event, 0);
                if (this.mMouseHoveringAtEdge) {
                    this.mMouseHoveringAtEdge = false;
                    this.mCallbacks.onMouseLeaveFromEdge();
                }
                this.mCallbacks.onDown();
                return;
            case 1:
            case 3:
                this.mSwipeFireable = false;
                this.mDebugFireable = false;
                if (this.mScrollFired) {
                    this.mCallbacks.onScroll(false);
                }
                this.mScrollFired = false;
                this.mCallbacks.onUpOrCancel();
                return;
            case 2:
                if (this.mSwipeFireable) {
                    int swipe = detectSwipe(event);
                    if (swipe == 0) {
                        z2 = true;
                    }
                    this.mSwipeFireable = z2;
                    if (swipe == 1) {
                        this.mCallbacks.onSwipeFromTop();
                        return;
                    } else if (swipe == 2) {
                        this.mCallbacks.onSwipeFromBottom();
                        return;
                    } else if (swipe == 3) {
                        this.mCallbacks.onSwipeFromRight();
                        return;
                    } else if (swipe == 4) {
                        this.mCallbacks.onSwipeFromLeft();
                        return;
                    } else {
                        return;
                    }
                }
                return;
            case 5:
                captureDown(event, event.getActionIndex());
                if (this.mDebugFireable) {
                    if (event.getPointerCount() >= 5) {
                        z = false;
                    }
                    this.mDebugFireable = z;
                    if (!this.mDebugFireable) {
                        this.mCallbacks.onDebug();
                        return;
                    }
                    return;
                }
                return;
            case 7:
                if (!event.isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
                    return;
                }
                if (!this.mMouseHoveringAtEdge && event.getY() == OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mCallbacks.onMouseHoverAtTop();
                    this.mMouseHoveringAtEdge = true;
                    return;
                } else if (!this.mMouseHoveringAtEdge && event.getY() >= ((float) (this.screenHeight - 1))) {
                    this.mCallbacks.onMouseHoverAtBottom();
                    this.mMouseHoveringAtEdge = true;
                    return;
                } else if (this.mMouseHoveringAtEdge && event.getY() > OppoBrightUtils.MIN_LUX_LIMITI && event.getY() < ((float) (this.screenHeight - 1))) {
                    this.mCallbacks.onMouseLeaveFromEdge();
                    this.mMouseHoveringAtEdge = false;
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    private void captureDown(MotionEvent event, int pointerIndex) {
        int i = findIndex(event.getPointerId(pointerIndex));
        if (i != -1) {
            this.mDownX[i] = event.getX(pointerIndex);
            this.mDownY[i] = event.getY(pointerIndex);
            this.mDownTime[i] = event.getEventTime();
        }
    }

    private int findIndex(int pointerId) {
        for (int i = 0; i < this.mDownPointers; i++) {
            if (this.mDownPointerId[i] == pointerId) {
                return i;
            }
        }
        if (this.mDownPointers == 32 || pointerId == -1) {
            return -1;
        }
        int[] iArr = this.mDownPointerId;
        int i2 = this.mDownPointers;
        this.mDownPointers = i2 + 1;
        iArr[i2] = pointerId;
        return this.mDownPointers - 1;
    }

    private int detectSwipe(MotionEvent move) {
        int historySize = move.getHistorySize();
        int pointerCount = move.getPointerCount();
        for (int p = 0; p < pointerCount; p++) {
            int i = findIndex(move.getPointerId(p));
            if (i != -1) {
                int swipe;
                for (int h = 0; h < historySize; h++) {
                    swipe = detectSwipe(i, move.getHistoricalEventTime(h), move.getHistoricalX(p, h), move.getHistoricalY(p, h));
                    if (swipe != 0) {
                        return swipe;
                    }
                }
                swipe = detectSwipe(i, move.getEventTime(), move.getX(p), move.getY(p));
                if (swipe != 0) {
                    return swipe;
                }
            }
        }
        return 0;
    }

    private int detectSwipe(int i, long time, float x, float y) {
        float fromX = this.mDownX[i];
        float fromY = this.mDownY[i];
        long elapsed = time - this.mDownTime[i];
        if (x < OppoBrightUtils.MIN_LUX_LIMITI || y < OppoBrightUtils.MIN_LUX_LIMITI || x > ((float) this.screenWidth)) {
            return 0;
        }
        if (fromY <= ((float) this.mSwipeStartThreshold) && y > ((float) this.mSwipeDistanceThreshold) + fromY && elapsed < 500) {
            return 1;
        }
        if (fromY >= ((float) (this.screenHeight - this.mSwipeStartThreshold)) && y < fromY - ((float) this.mSwipeDistanceThreshold) && elapsed < 500) {
            return 2;
        }
        if (fromX >= ((float) (this.screenWidth - this.mSwipeStartThreshold)) && x < fromX - ((float) this.mSwipeDistanceThreshold) && elapsed < 500) {
            return 3;
        }
        if (fromX > ((float) this.mSwipeStartThreshold) || x <= ((float) this.mSwipeDistanceThreshold) + fromX || elapsed >= 500) {
            return 0;
        }
        return 4;
    }
}

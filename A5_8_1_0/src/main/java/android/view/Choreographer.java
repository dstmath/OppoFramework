package android.view;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.TimeUtils;
import java.io.PrintWriter;

public final class Choreographer {
    public static final int CALLBACK_ANIMATION = 1;
    public static final int CALLBACK_COMMIT = 3;
    public static final int CALLBACK_INPUT = 0;
    private static final int CALLBACK_LAST = 3;
    private static final String[] CALLBACK_TRACE_TITLES = new String[]{"input", "animation", "traversal", "commit"};
    public static final int CALLBACK_TRAVERSAL = 2;
    private static final boolean DEBUG_FRAMES = false;
    private static final boolean DEBUG_JANK = false;
    private static final long DEFAULT_FRAME_DELAY = 10;
    private static final Object FRAME_CALLBACK_TOKEN = new Object() {
        public String toString() {
            return "FRAME_CALLBACK_TOKEN";
        }
    };
    private static final int MOTION_EVENT_ACTION_CANCEL = 3;
    private static final int MOTION_EVENT_ACTION_DOWN = 0;
    private static final int MOTION_EVENT_ACTION_MOVE = 2;
    private static final int MOTION_EVENT_ACTION_UP = 1;
    private static final int MSG_DO_FRAME = 0;
    private static final int MSG_DO_SCHEDULE_CALLBACK = 2;
    private static final int MSG_DO_SCHEDULE_VSYNC = 1;
    private static final boolean OPTS_INPUT = SystemProperties.getBoolean("persist.vendor.qti.inputopts.enable", false);
    private static final String TAG = "Choreographer";
    private static final boolean USE_FRAME_TIME = SystemProperties.getBoolean("debug.choreographer.frametime", true);
    private static final boolean USE_VSYNC = SystemProperties.getBoolean("debug.choreographer.vsync", true);
    private static volatile long sFrameDelay = DEFAULT_FRAME_DELAY;
    private static final ThreadLocal<Choreographer> sSfThreadInstance = new ThreadLocal<Choreographer>() {
        protected Choreographer initialValue() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                return new Choreographer(looper, 1, null);
            }
            throw new IllegalStateException("The current thread must have a looper!");
        }
    };
    private static final ThreadLocal<Choreographer> sThreadInstance = new ThreadLocal<Choreographer>() {
        protected Choreographer initialValue() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                return new Choreographer(looper, 0, null);
            }
            throw new IllegalStateException("The current thread must have a looper!");
        }
    };
    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "Jianhua.Lin@Plf.SDK : [-static] Modify for monitor skippedFrames", property = OppoRomType.ROM)
    private final int SKIPPED_FRAME_WARNING_LIMIT;
    private CallbackRecord mCallbackPool;
    private final CallbackQueue[] mCallbackQueues;
    private boolean mCallbacksRunning;
    private boolean mConsumedDown;
    private boolean mConsumedMove;
    private boolean mDebugPrintNextFrameTimeDelta;
    private final FrameDisplayEventReceiver mDisplayEventReceiver;
    FrameInfo mFrameInfo;
    private long mFrameIntervalNanos;
    private boolean mFrameScheduled;
    private final FrameHandler mHandler;
    private long mLastFrameTimeNanos;
    private final Object mLock;
    private final Looper mLooper;
    private int mMotionEventType;
    private int mTouchMoveNum;

    private final class CallbackQueue {
        private CallbackRecord mHead;

        /* synthetic */ CallbackQueue(Choreographer this$0, CallbackQueue -this1) {
            this();
        }

        private CallbackQueue() {
        }

        public boolean hasDueCallbacksLocked(long now) {
            return this.mHead != null && this.mHead.dueTime <= now;
        }

        public CallbackRecord extractDueCallbacksLocked(long now) {
            CallbackRecord callbacks = this.mHead;
            if (callbacks == null || callbacks.dueTime > now) {
                return null;
            }
            CallbackRecord last = callbacks;
            CallbackRecord next = callbacks.next;
            while (next != null) {
                if (next.dueTime > now) {
                    last.next = null;
                    break;
                }
                last = next;
                next = next.next;
            }
            this.mHead = next;
            return callbacks;
        }

        public void addCallbackLocked(long dueTime, Object action, Object token) {
            CallbackRecord callback = Choreographer.this.obtainCallbackLocked(dueTime, action, token);
            CallbackRecord entry = this.mHead;
            if (entry == null) {
                this.mHead = callback;
            } else if (dueTime < entry.dueTime) {
                callback.next = entry;
                this.mHead = callback;
            } else {
                while (entry.next != null) {
                    if (dueTime < entry.next.dueTime) {
                        callback.next = entry.next;
                        break;
                    }
                    entry = entry.next;
                }
                entry.next = callback;
            }
        }

        public void removeCallbacksLocked(Object action, Object token) {
            CallbackRecord predecessor = null;
            CallbackRecord callback = this.mHead;
            while (callback != null) {
                CallbackRecord next = callback.next;
                if ((action == null || callback.action == action) && (token == null || callback.token == token)) {
                    if (predecessor != null) {
                        predecessor.next = next;
                    } else {
                        this.mHead = next;
                    }
                    Choreographer.this.recycleCallbackLocked(callback);
                } else {
                    predecessor = callback;
                }
                callback = next;
            }
        }
    }

    private static final class CallbackRecord {
        public Object action;
        public long dueTime;
        public CallbackRecord next;
        public Object token;

        /* synthetic */ CallbackRecord(CallbackRecord -this0) {
            this();
        }

        private CallbackRecord() {
        }

        public void run(long frameTimeNanos) {
            if (this.token == Choreographer.FRAME_CALLBACK_TOKEN) {
                ((FrameCallback) this.action).doFrame(frameTimeNanos);
            } else {
                ((Runnable) this.action).run();
            }
        }
    }

    public interface FrameCallback {
        void doFrame(long j);
    }

    private final class FrameDisplayEventReceiver extends DisplayEventReceiver implements Runnable {
        private int mFrame;
        private boolean mHavePendingVsync;
        private long mTimestampNanos;

        public FrameDisplayEventReceiver(Looper looper, int vsyncSource) {
            super(looper, vsyncSource);
        }

        public void onVsync(long timestampNanos, int builtInDisplayId, int frame) {
            if (builtInDisplayId != 0) {
                Log.d(Choreographer.TAG, "Received vsync from secondary display, but we don't support this case yet.  Choreographer needs a way to explicitly request vsync for a specific display to ensure it doesn't lose track of its scheduled vsync.");
                scheduleVsync();
                return;
            }
            long now = System.nanoTime();
            if (timestampNanos > now) {
                Log.w(Choreographer.TAG, "Frame time is " + (((float) (timestampNanos - now)) * 1.0E-6f) + " ms in the future!  Check that graphics HAL is generating vsync " + "timestamps using the correct timebase.");
                timestampNanos = now;
            }
            if (this.mHavePendingVsync) {
                Log.w(Choreographer.TAG, "Already have a pending vsync event.  There should only be one at a time.");
            } else {
                this.mHavePendingVsync = true;
            }
            this.mTimestampNanos = timestampNanos;
            this.mFrame = frame;
            Message msg = Message.obtain(Choreographer.this.mHandler, this);
            msg.setAsynchronous(true);
            Choreographer.this.mHandler.sendMessageAtTime(msg, timestampNanos / TimeUtils.NANOS_PER_MS);
        }

        public void run() {
            this.mHavePendingVsync = false;
            Choreographer.this.doFrame(this.mTimestampNanos, this.mFrame);
        }
    }

    private final class FrameHandler extends Handler {
        public FrameHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Choreographer.this.doFrame(System.nanoTime(), 0);
                    return;
                case 1:
                    Choreographer.this.doScheduleVsync();
                    return;
                case 2:
                    Choreographer.this.doScheduleCallback(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    /* synthetic */ Choreographer(Looper looper, int vsyncSource, Choreographer -this2) {
        this(looper, vsyncSource);
    }

    private Choreographer(Looper looper, int vsyncSource) {
        FrameDisplayEventReceiver frameDisplayEventReceiver;
        this.SKIPPED_FRAME_WARNING_LIMIT = SystemProperties.getInt("debug.choreographer.skipwarning", 30);
        this.mLock = new Object();
        this.mTouchMoveNum = -1;
        this.mMotionEventType = -1;
        this.mConsumedMove = false;
        this.mConsumedDown = false;
        this.mFrameInfo = new FrameInfo();
        this.mLooper = looper;
        this.mHandler = new FrameHandler(looper);
        if (USE_VSYNC) {
            frameDisplayEventReceiver = new FrameDisplayEventReceiver(looper, vsyncSource);
        } else {
            frameDisplayEventReceiver = null;
        }
        this.mDisplayEventReceiver = frameDisplayEventReceiver;
        this.mLastFrameTimeNanos = Long.MIN_VALUE;
        this.mFrameIntervalNanos = (long) (1.0E9f / getRefreshRate());
        this.mCallbackQueues = new CallbackQueue[4];
        for (int i = 0; i <= 3; i++) {
            this.mCallbackQueues[i] = new CallbackQueue(this, null);
        }
    }

    private static float getRefreshRate() {
        return DisplayManagerGlobal.getInstance().getDisplayInfo(0).getMode().getRefreshRate();
    }

    public static Choreographer getInstance() {
        return (Choreographer) sThreadInstance.get();
    }

    public static Choreographer getSfInstance() {
        return (Choreographer) sSfThreadInstance.get();
    }

    public void setMotionEventInfo(int motionEventType, int touchMoveNum) {
        synchronized (this) {
            this.mTouchMoveNum = touchMoveNum;
            this.mMotionEventType = motionEventType;
        }
    }

    public static void releaseInstance() {
        Choreographer old = (Choreographer) sThreadInstance.get();
        sThreadInstance.remove();
        old.dispose();
    }

    private void dispose() {
        this.mDisplayEventReceiver.dispose();
    }

    public static long getFrameDelay() {
        return sFrameDelay;
    }

    public static void setFrameDelay(long frameDelay) {
        sFrameDelay = frameDelay;
    }

    public static long subtractFrameDelay(long delayMillis) {
        long frameDelay = sFrameDelay;
        return delayMillis <= frameDelay ? 0 : delayMillis - frameDelay;
    }

    public long getFrameIntervalNanos() {
        return this.mFrameIntervalNanos;
    }

    void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.println("Choreographer:");
        writer.print(innerPrefix);
        writer.print("mFrameScheduled=");
        writer.println(this.mFrameScheduled);
        writer.print(innerPrefix);
        writer.print("mLastFrameTime=");
        writer.println(TimeUtils.formatUptime(this.mLastFrameTimeNanos / TimeUtils.NANOS_PER_MS));
    }

    public void postCallback(int callbackType, Runnable action, Object token) {
        postCallbackDelayed(callbackType, action, token, 0);
    }

    public void postCallbackDelayed(int callbackType, Runnable action, Object token, long delayMillis) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        } else if (callbackType < 0 || callbackType > 3) {
            throw new IllegalArgumentException("callbackType is invalid");
        } else {
            postCallbackDelayedInternal(callbackType, action, token, delayMillis);
        }
    }

    private void postCallbackDelayedInternal(int callbackType, Object action, Object token, long delayMillis) {
        synchronized (this.mLock) {
            long now = SystemClock.uptimeMillis();
            long dueTime = now + delayMillis;
            this.mCallbackQueues[callbackType].addCallbackLocked(dueTime, action, token);
            if (dueTime <= now) {
                scheduleFrameLocked(now);
            } else {
                Message msg = this.mHandler.obtainMessage(2, action);
                msg.arg1 = callbackType;
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtTime(msg, dueTime);
            }
        }
    }

    public void removeCallbacks(int callbackType, Runnable action, Object token) {
        if (callbackType < 0 || callbackType > 3) {
            throw new IllegalArgumentException("callbackType is invalid");
        }
        removeCallbacksInternal(callbackType, action, token);
    }

    private void removeCallbacksInternal(int callbackType, Object action, Object token) {
        synchronized (this.mLock) {
            this.mCallbackQueues[callbackType].removeCallbacksLocked(action, token);
            if (action != null && token == null) {
                this.mHandler.removeMessages(2, action);
            }
        }
    }

    public void postFrameCallback(FrameCallback callback) {
        postFrameCallbackDelayed(callback, 0);
    }

    public void postFrameCallbackDelayed(FrameCallback callback, long delayMillis) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        postCallbackDelayedInternal(1, callback, FRAME_CALLBACK_TOKEN, delayMillis);
    }

    public void removeFrameCallback(FrameCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        removeCallbacksInternal(1, callback, FRAME_CALLBACK_TOKEN);
    }

    public long getFrameTime() {
        return getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
    }

    public long getFrameTimeNanos() {
        long nanoTime;
        synchronized (this.mLock) {
            if (this.mCallbacksRunning) {
                nanoTime = USE_FRAME_TIME ? this.mLastFrameTimeNanos : System.nanoTime();
            } else {
                throw new IllegalStateException("This method must only be called as part of a callback while a frame is in progress.");
            }
        }
        return nanoTime;
    }

    public long getLastFrameTimeNanos() {
        long nanoTime;
        synchronized (this.mLock) {
            nanoTime = USE_FRAME_TIME ? this.mLastFrameTimeNanos : System.nanoTime();
        }
        return nanoTime;
    }

    private void scheduleFrameLocked(long now) {
        if (!this.mFrameScheduled) {
            Message msg;
            this.mFrameScheduled = true;
            if (OPTS_INPUT) {
                Trace.traceBegin(8, "scheduleFrameLocked-mMotionEventType:" + this.mMotionEventType + " mTouchMoveNum:" + this.mTouchMoveNum + " mConsumedDown:" + this.mConsumedDown + " mConsumedMove:" + this.mConsumedMove);
                Trace.traceEnd(8);
                synchronized (this) {
                    switch (this.mMotionEventType) {
                        case 0:
                            this.mConsumedMove = false;
                            if (!this.mConsumedDown) {
                                msg = this.mHandler.obtainMessage(0);
                                msg.setAsynchronous(true);
                                this.mHandler.sendMessageAtFrontOfQueue(msg);
                                this.mConsumedDown = true;
                                return;
                            }
                            break;
                        case 1:
                        case 3:
                            this.mConsumedMove = false;
                            this.mConsumedDown = false;
                            break;
                        case 2:
                            this.mConsumedDown = false;
                            if (this.mTouchMoveNum == 1 && (this.mConsumedMove ^ 1) != 0) {
                                msg = this.mHandler.obtainMessage(0);
                                msg.setAsynchronous(true);
                                this.mHandler.sendMessageAtFrontOfQueue(msg);
                                this.mConsumedMove = true;
                                return;
                            }
                    }
                }
            }
            if (!USE_VSYNC) {
                long nextFrameTime = Math.max((this.mLastFrameTimeNanos / TimeUtils.NANOS_PER_MS) + sFrameDelay, now);
                msg = this.mHandler.obtainMessage(0);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtTime(msg, nextFrameTime);
            } else if (isRunningOnLooperThreadLocked()) {
                scheduleVsyncLocked();
            } else {
                msg = this.mHandler.obtainMessage(1);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtFrontOfQueue(msg);
            }
        }
    }

    /* JADX WARNING: Missing block: B:24:?, code:
            android.os.Trace.traceBegin(8, "Choreographer#doFrame");
            android.view.animation.AnimationUtils.lockAnimationClock(r20 / android.util.TimeUtils.NANOS_PER_MS);
            r19.mFrameInfo.markInputHandlingStart();
            doCallbacks(0, r20);
            r19.mFrameInfo.markAnimationsStart();
            doCallbacks(1, r20);
            r19.mFrameInfo.markPerformTraversalsStart();
            doCallbacks(2, r20);
            doCallbacks(3, r20);
     */
    /* JADX WARNING: Missing block: B:30:0x00e0, code:
            android.view.animation.AnimationUtils.unlockAnimationClock();
            android.os.Trace.traceEnd(8);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void doFrame(long frameTimeNanos, int frame) {
        synchronized (this.mLock) {
            if (this.mFrameScheduled) {
                long intendedFrameTimeNanos = frameTimeNanos;
                long startNanos = System.nanoTime();
                long jitterNanos = startNanos - frameTimeNanos;
                if (jitterNanos >= this.mFrameIntervalNanos) {
                    long skippedFrames = jitterNanos / this.mFrameIntervalNanos;
                    if (skippedFrames >= ((long) this.SKIPPED_FRAME_WARNING_LIMIT)) {
                        Log.i(TAG, "Skipped " + skippedFrames + " frames!  " + "The application may be doing too much work on its main thread.");
                    }
                    frameTimeNanos = startNanos - (jitterNanos % this.mFrameIntervalNanos);
                }
                if (frameTimeNanos < this.mLastFrameTimeNanos) {
                    scheduleVsyncLocked();
                    return;
                }
                this.mFrameInfo.setVsync(intendedFrameTimeNanos, frameTimeNanos);
                this.mFrameScheduled = false;
                this.mLastFrameTimeNanos = frameTimeNanos;
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:?, code:
            android.os.Trace.traceBegin(8, CALLBACK_TRACE_TITLES[r21]);
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:17:0x0066, code:
            if (r4 == null) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:18:0x0068, code:
            r4.run(r22);
            r4 = r4.next;
     */
    /* JADX WARNING: Missing block: B:23:0x0073, code:
            r14 = r20.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0077, code:
            monitor-enter(r14);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r20.mCallbacksRunning = false;
     */
    /* JADX WARNING: Missing block: B:28:0x007d, code:
            r10 = r5.next;
            recycleCallbackLocked(r5);
     */
    /* JADX WARNING: Missing block: B:29:0x0084, code:
            r5 = r10;
     */
    /* JADX WARNING: Missing block: B:30:0x0085, code:
            if (r10 != null) goto L_0x007d;
     */
    /* JADX WARNING: Missing block: B:31:0x0087, code:
            monitor-exit(r14);
     */
    /* JADX WARNING: Missing block: B:32:0x0088, code:
            android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Missing block: B:33:0x008d, code:
            return;
     */
    /* JADX WARNING: Missing block: B:39:0x0096, code:
            monitor-enter(r20.mLock);
     */
    /* JADX WARNING: Missing block: B:42:?, code:
            r20.mCallbacksRunning = false;
     */
    /* JADX WARNING: Missing block: B:43:0x009c, code:
            r10 = r5.next;
            recycleCallbackLocked(r5);
     */
    /* JADX WARNING: Missing block: B:44:0x00a3, code:
            r5 = r10;
     */
    /* JADX WARNING: Missing block: B:45:0x00a4, code:
            if (r10 != null) goto L_0x009c;
     */
    /* JADX WARNING: Missing block: B:47:0x00a7, code:
            android.os.Trace.traceEnd(8);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void doCallbacks(int callbackType, long frameTimeNanos) {
        synchronized (this.mLock) {
            long now = System.nanoTime();
            CallbackRecord callbacks = this.mCallbackQueues[callbackType].extractDueCallbacksLocked(now / TimeUtils.NANOS_PER_MS);
            if (callbacks == null) {
                return;
            }
            this.mCallbacksRunning = true;
            if (callbackType == 3) {
                long jitterNanos = now - frameTimeNanos;
                Trace.traceCounter(8, "jitterNanos", (int) jitterNanos);
                if (jitterNanos >= this.mFrameIntervalNanos * 2) {
                    frameTimeNanos = now - ((jitterNanos % this.mFrameIntervalNanos) + this.mFrameIntervalNanos);
                    this.mLastFrameTimeNanos = frameTimeNanos;
                }
            }
        }
    }

    void doScheduleVsync() {
        synchronized (this.mLock) {
            if (this.mFrameScheduled) {
                scheduleVsyncLocked();
            }
        }
    }

    void doScheduleCallback(int callbackType) {
        synchronized (this.mLock) {
            if (!this.mFrameScheduled) {
                long now = SystemClock.uptimeMillis();
                if (this.mCallbackQueues[callbackType].hasDueCallbacksLocked(now)) {
                    scheduleFrameLocked(now);
                }
            }
        }
    }

    private void scheduleVsyncLocked() {
        this.mDisplayEventReceiver.scheduleVsync();
    }

    private boolean isRunningOnLooperThreadLocked() {
        return Looper.myLooper() == this.mLooper;
    }

    private CallbackRecord obtainCallbackLocked(long dueTime, Object action, Object token) {
        CallbackRecord callback = this.mCallbackPool;
        if (callback == null) {
            callback = new CallbackRecord();
        } else {
            this.mCallbackPool = callback.next;
            callback.next = null;
        }
        callback.dueTime = dueTime;
        callback.action = action;
        callback.token = token;
        return callback;
    }

    private void recycleCallbackLocked(CallbackRecord callback) {
        callback.action = null;
        callback.token = null;
        callback.next = this.mCallbackPool;
        this.mCallbackPool = callback;
    }
}

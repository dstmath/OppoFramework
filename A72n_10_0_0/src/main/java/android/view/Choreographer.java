package android.view;

import android.annotation.OppoHook;
import android.annotation.UnsupportedAppUsage;
import android.graphics.FrameInfo;
import android.hardware.display.DisplayManagerGlobal;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.TimeUtils;
import com.mediatek.view.ViewDebugManager;
import java.io.PrintWriter;

public final class Choreographer {
    public static final int CALLBACK_ANIMATION = 1;
    public static final int CALLBACK_COMMIT = 4;
    public static final int CALLBACK_INPUT = 0;
    public static final int CALLBACK_INSETS_ANIMATION = 2;
    private static final int CALLBACK_LAST = 4;
    private static final String[] CALLBACK_TRACE_TITLES = {"input", "animation", "insets_animation", "traversal", "commit"};
    public static final int CALLBACK_TRAVERSAL = 3;
    private static final boolean DEBUG_FRAMES = ViewDebugManager.DEBUG_CHOREOGRAPHER_FRAMES;
    private static final boolean DEBUG_JANK = ViewDebugManager.DEBUG_CHOREOGRAPHER_JANK;
    private static final long DEFAULT_FRAME_DELAY = 10;
    private static final Object FRAME_CALLBACK_TOKEN = new Object() {
        /* class android.view.Choreographer.AnonymousClass3 */

        public String toString() {
            return "FRAME_CALLBACK_TOKEN";
        }
    };
    private static final int MSG_DO_FRAME = 0;
    private static final int MSG_DO_SCHEDULE_CALLBACK = 2;
    private static final int MSG_DO_SCHEDULE_VSYNC = 1;
    private static final String TAG = "Choreographer";
    private static final boolean USE_FRAME_TIME = SystemProperties.getBoolean("debug.choreographer.frametime", true);
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769497)
    private static final boolean USE_VSYNC = SystemProperties.getBoolean("debug.choreographer.vsync", true);
    private static volatile Choreographer mMainInstance;
    private static volatile long sFrameDelay = DEFAULT_FRAME_DELAY;
    private static final ThreadLocal<Choreographer> sSfThreadInstance = new ThreadLocal<Choreographer>() {
        /* class android.view.Choreographer.AnonymousClass2 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Choreographer initialValue() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                return new Choreographer(looper, 1);
            }
            throw new IllegalStateException("The current thread must have a looper!");
        }
    };
    private static final ThreadLocal<Choreographer> sThreadInstance = new ThreadLocal<Choreographer>() {
        /* class android.view.Choreographer.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Choreographer initialValue() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                Choreographer choreographer = new Choreographer(looper, 0);
                if (looper == Looper.getMainLooper()) {
                    Choreographer unused = Choreographer.mMainInstance = choreographer;
                }
                return choreographer;
            }
            throw new IllegalStateException("The current thread must have a looper!");
        }
    };
    private static boolean sTouchPointView = false;
    private static boolean sTouchView = false;
    @OppoHook(level = OppoHook.OppoHookType.CHANGE_ACCESS, note = "Jianhua.Lin@ROM.SDK : [-static] Modify for monitor skippedFrames", property = OppoHook.OppoRomType.ROM)
    private final int SKIPPED_FRAME_WARNING_LIMIT;
    private CallbackRecord mCallbackPool;
    @UnsupportedAppUsage
    private final CallbackQueue[] mCallbackQueues;
    private boolean mCallbacksRunning;
    private boolean mDebugPrintNextFrameTimeDelta;
    @UnsupportedAppUsage
    private final FrameDisplayEventReceiver mDisplayEventReceiver;
    private int mFPSDivisor;
    FrameInfo mFrameInfo;
    @UnsupportedAppUsage
    private long mFrameIntervalNanos;
    private boolean mFrameScheduled;
    private final FrameHandler mHandler;
    @UnsupportedAppUsage
    private long mLastFrameTimeNanos;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final Object mLock;
    private final Looper mLooper;
    private int sIsSFChoregrapher;

    public interface FrameCallback {
        void doFrame(long j);
    }

    private Choreographer(Looper looper, int vsyncSource) {
        this.sIsSFChoregrapher = 0;
        this.SKIPPED_FRAME_WARNING_LIMIT = SystemProperties.getInt("debug.choreographer.skipwarning", 30);
        this.mLock = new Object();
        this.mFPSDivisor = 1;
        this.mFrameInfo = new FrameInfo();
        this.mLooper = looper;
        this.mHandler = new FrameHandler(looper);
        this.mDisplayEventReceiver = USE_VSYNC ? new FrameDisplayEventReceiver(looper, vsyncSource) : null;
        this.mLastFrameTimeNanos = Long.MIN_VALUE;
        this.mFrameIntervalNanos = (long) (1.0E9f / getRefreshRate());
        this.mCallbackQueues = new CallbackQueue[5];
        for (int i = 0; i <= 4; i++) {
            this.mCallbackQueues[i] = new CallbackQueue();
        }
        setFPSDivisor(SystemProperties.getInt(ThreadedRenderer.DEBUG_FPS_DIVISOR, 1));
    }

    private static float getRefreshRate() {
        return DisplayManagerGlobal.getInstance().getDisplayInfo(0).getMode().getRefreshRate();
    }

    public static Choreographer getInstance() {
        return sThreadInstance.get();
    }

    @UnsupportedAppUsage
    public static Choreographer getSfInstance() {
        return sSfThreadInstance.get();
    }

    public static Choreographer getMainThreadInstance() {
        return mMainInstance;
    }

    public static void releaseInstance() {
        sThreadInstance.remove();
        sThreadInstance.get().dispose();
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
        if (delayMillis <= frameDelay) {
            return 0;
        }
        return delayMillis - frameDelay;
    }

    public long getFrameIntervalNanos() {
        return this.mFrameIntervalNanos;
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter writer) {
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

    /* access modifiers changed from: package-private */
    public void postTouchPointerViewMessage(boolean touchPointView) {
        if (ViewDebugManager.debugHighFrameRateTouchLowLatency) {
            if (ViewDebugManager.DEBUG_INPUT) {
                Log.d(TAG, "postTouchPointerViewMessage TouchPointView = " + touchPointView);
            }
            sTouchPointView = touchPointView;
        }
    }

    /* access modifiers changed from: package-private */
    public void postTouchViewMessage(boolean touchView) {
        if (ViewDebugManager.debugHighFrameRateTouchLowLatency) {
            if (ViewDebugManager.DEBUG_INPUT) {
                Log.d(TAG, "postTouchViewMessage TouchView = " + touchView);
            }
            sTouchView = touchView;
        }
    }

    public void postCallbackDelayed(int callbackType, Runnable action, Object token, long delayMillis) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        } else if (callbackType < 0 || callbackType > 4) {
            throw new IllegalArgumentException("callbackType is invalid");
        } else {
            postCallbackDelayedInternal(callbackType, action, token, delayMillis);
        }
    }

    private void postCallbackDelayedInternal(int callbackType, Object action, Object token, long delayMillis) {
        if (DEBUG_FRAMES) {
            Log.d(TAG, "PostCallback: type=" + callbackType + ", action=" + action + ", token=" + token + ", delayMillis=" + delayMillis, new Throwable());
        }
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

    public void doFrameImmediately() {
        synchronized (this.mLock) {
            Message msg = this.mHandler.obtainMessage(0);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageAtFrontOfQueue(msg);
        }
    }

    public void postCallbackImmediately(int callbackType, Object action, Object token, long delayMillis) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        } else if (callbackType < 0 || callbackType > 4) {
            throw new IllegalArgumentException("callbackType is invalid");
        } else {
            synchronized (this.mLock) {
                this.mCallbackQueues[callbackType].addCallbackLocked(SystemClock.uptimeMillis() + delayMillis, action, token);
            }
        }
    }

    public void removeCallbacks(int callbackType, Runnable action, Object token) {
        if (callbackType < 0 || callbackType > 4) {
            throw new IllegalArgumentException("callbackType is invalid");
        }
        removeCallbacksInternal(callbackType, action, token);
    }

    private void removeCallbacksInternal(int callbackType, Object action, Object token) {
        if (DEBUG_FRAMES) {
            Log.d(TAG, "RemoveCallbacks: type=" + callbackType + ", action=" + action + ", token=" + token);
        }
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
        if (callback != null) {
            postCallbackDelayedInternal(1, callback, FRAME_CALLBACK_TOKEN, delayMillis);
            return;
        }
        throw new IllegalArgumentException("callback must not be null");
    }

    public void removeFrameCallback(FrameCallback callback) {
        if (callback != null) {
            removeCallbacksInternal(1, callback, FRAME_CALLBACK_TOKEN);
            return;
        }
        throw new IllegalArgumentException("callback must not be null");
    }

    @UnsupportedAppUsage
    public long getFrameTime() {
        return getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
    }

    @UnsupportedAppUsage
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
            this.mFrameScheduled = true;
            if ((sTouchPointView || sTouchView) && ViewDebugManager.debugHighFrameRateTouchLowLatency) {
                if (ViewDebugManager.DEBUG_INPUT) {
                    Log.d(TAG, "FirstLaunch.. skip vsync TouchPointView= " + sTouchPointView + ",TouchView= " + sTouchView);
                }
                Message msg = this.mHandler.obtainMessage(0);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtFrontOfQueue(msg);
                if (sTouchView) {
                    sTouchView = false;
                    if (ViewDebugManager.DEBUG_INPUT) {
                        Log.d(TAG, "TouchView set false");
                    }
                }
                if (sTouchPointView) {
                    sTouchPointView = false;
                    if (ViewDebugManager.DEBUG_INPUT) {
                        Log.d(TAG, "TouchPointView set false");
                    }
                }
            } else if (USE_VSYNC) {
                if (DEBUG_FRAMES) {
                    Log.d(TAG, "Scheduling next frame on vsync.");
                }
                if (isRunningOnLooperThreadLocked()) {
                    scheduleVsyncLocked();
                    return;
                }
                Message msg2 = this.mHandler.obtainMessage(1);
                msg2.setAsynchronous(true);
                this.mHandler.sendMessageAtFrontOfQueue(msg2);
            } else {
                long nextFrameTime = Math.max((this.mLastFrameTimeNanos / TimeUtils.NANOS_PER_MS) + sFrameDelay, now);
                if (DEBUG_FRAMES) {
                    Log.d(TAG, "Scheduling next frame in " + (nextFrameTime - now) + " ms.");
                }
                Message msg3 = this.mHandler.obtainMessage(0);
                msg3.setAsynchronous(true);
                this.mHandler.sendMessageAtTime(msg3, nextFrameTime);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setFPSDivisor(int divisor) {
        if (divisor <= 0) {
            divisor = 1;
        }
        this.mFPSDivisor = divisor;
        ThreadedRenderer.setFPSDivisor(divisor);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:65:?, code lost:
        android.os.Trace.traceBegin(8, "Choreographer#doFrame");
        android.view.animation.AnimationUtils.lockAnimationClock(r12 / android.util.TimeUtils.NANOS_PER_MS);
        r21.mFrameInfo.markInputHandlingStart();
        doCallbacks(0, r12);
        r21.mFrameInfo.markAnimationsStart();
        doCallbacks(1, r12);
        doCallbacks(2, r12);
        r21.mFrameInfo.markPerformTraversalsStart();
        doCallbacks(3, r12);
        doCallbacks(4, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x01a7, code lost:
        android.view.animation.AnimationUtils.unlockAnimationClock();
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x01b1, code lost:
        if (r21.sIsSFChoregrapher != 1) goto L_0x01d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x01b3, code lost:
        r0 = (int) (((float) (java.lang.System.nanoTime() - r19)) * 1.0E-6f);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x01c1, code lost:
        if (r0 <= 20) goto L_0x01d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x01c3, code lost:
        android.util.Log.p("Quality", "01 10 " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01db, code lost:
        if (android.view.Choreographer.DEBUG_FRAMES == false) goto L_0x021a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01dd, code lost:
        r2 = java.lang.System.nanoTime();
        android.util.Log.d(android.view.Choreographer.TAG, "Frame " + r24 + ": Finished, took " + (((float) (r2 - r19)) * 1.0E-6f) + " ms, latency " + (((float) (r19 - r12)) * 1.0E-6f) + " ms.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x021d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x021e, code lost:
        android.view.animation.AnimationUtils.unlockAnimationClock();
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0226, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        return;
     */
    @UnsupportedAppUsage
    public void doFrame(long frameTimeNanos, int frame) {
        Throwable th;
        long intendedFrameTimeNanos;
        long frameTimeNanos2;
        long startNanos;
        int threshold;
        synchronized (this.mLock) {
            try {
                if (!this.mFrameScheduled) {
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        throw th;
                    }
                } else {
                    if (DEBUG_JANK && this.mDebugPrintNextFrameTimeDelta) {
                        this.mDebugPrintNextFrameTimeDelta = false;
                        Log.d(TAG, "Frame time delta: " + (((float) (frameTimeNanos - this.mLastFrameTimeNanos)) * 1.0E-6f) + " ms");
                    }
                    long startNanos2 = System.nanoTime();
                    long jitterNanos = startNanos2 - frameTimeNanos;
                    if (jitterNanos >= this.mFrameIntervalNanos) {
                        long skippedFrames = jitterNanos / this.mFrameIntervalNanos;
                        intendedFrameTimeNanos = frameTimeNanos;
                        if (skippedFrames >= ((long) this.SKIPPED_FRAME_WARNING_LIMIT)) {
                            Log.i(TAG, "Skipped " + skippedFrames + " frames!  The application may be doing too much work on its main thread.");
                            boolean animationWithoutTraversal = this.mCallbackQueues[1].hasCallbacks() && !this.mCallbackQueues[3].hasCallbacks();
                            Log.p("Quality", "Skipped: " + animationWithoutTraversal + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + skippedFrames);
                            if (SystemProperties.getBoolean("debug.track_frame_skip", false)) {
                                if (animationWithoutTraversal) {
                                    threshold = SystemProperties.getInt("debug.track_frame_skip.threshold_anim", 4);
                                } else {
                                    threshold = SystemProperties.getInt("debug.track_frame_skip.threshold", 15);
                                }
                                if (skippedFrames >= ((long) threshold) && !FrameSkipReporter.checkDuplicate(startNanos2, jitterNanos)) {
                                    FrameSkipReporter.report(animationWithoutTraversal, skippedFrames);
                                }
                            }
                        }
                        long lastFrameOffset = jitterNanos % this.mFrameIntervalNanos;
                        if (DEBUG_JANK) {
                            Log.d(TAG, "Missed vsync by " + (((float) jitterNanos) * 1.0E-6f) + " ms which is more than the frame interval of " + (((float) this.mFrameIntervalNanos) * 1.0E-6f) + " ms!  Skipping " + skippedFrames + " frames and setting frame time to " + (((float) lastFrameOffset) * 1.0E-6f) + " ms in the past.");
                        }
                        frameTimeNanos2 = startNanos2 - lastFrameOffset;
                    } else {
                        intendedFrameTimeNanos = frameTimeNanos;
                        frameTimeNanos2 = frameTimeNanos;
                    }
                    try {
                        if (frameTimeNanos2 < this.mLastFrameTimeNanos) {
                            if (DEBUG_JANK) {
                                Log.d(TAG, "Frame time appears to be going backwards.  May be due to a previously skipped frame.  Waiting for next vsync.");
                            }
                            scheduleVsyncLocked();
                            return;
                        }
                        if (this.mFPSDivisor > 1) {
                            long timeSinceVsync = frameTimeNanos2 - this.mLastFrameTimeNanos;
                            startNanos = startNanos2;
                            if (timeSinceVsync < this.mFrameIntervalNanos * ((long) this.mFPSDivisor) && timeSinceVsync > 0) {
                                scheduleVsyncLocked();
                                return;
                            }
                        } else {
                            startNanos = startNanos2;
                        }
                        this.mFrameInfo.setVsync(intendedFrameTimeNanos, frameTimeNanos2);
                        this.mFrameScheduled = false;
                        this.mLastFrameTimeNanos = frameTimeNanos2;
                    } catch (Throwable th4) {
                        th = th4;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        android.os.Trace.traceBegin(8, android.view.Choreographer.CALLBACK_TRACE_TITLES[r18]);
        r0 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008f, code lost:
        if (r0 == null) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0093, code lost:
        if (android.view.Choreographer.DEBUG_FRAMES == false) goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0095, code lost:
        android.util.Log.d(android.view.Choreographer.TAG, "RunCallback: type=" + r18 + ", action=" + r0.action + ", token=" + r0.token + ", latencyMillis=" + (android.os.SystemClock.uptimeMillis() - r0.dueTime));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00ce, code lost:
        r0.run(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00d3, code lost:
        r0 = r0.next;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00d5, code lost:
        r4 = r17.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00d7, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r17.mCallbacksRunning = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00da, code lost:
        r0 = r6.next;
        recycleCallbackLocked(r6);
        r6 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00e0, code lost:
        if (r6 != null) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00e2, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00e3, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00e9, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ed, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f0, code lost:
        monitor-enter(r17.mLock);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        r17.mCallbacksRunning = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f3, code lost:
        r3 = r6.next;
        recycleCallbackLocked(r6);
        r6 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f9, code lost:
        if (r6 != null) goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00fd, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0102, code lost:
        throw r0;
     */
    public void doCallbacks(int callbackType, long frameTimeNanos) {
        Throwable th;
        long frameTimeNanos2;
        synchronized (this.mLock) {
            try {
                long now = System.nanoTime();
                CallbackRecord callbacks = this.mCallbackQueues[callbackType].extractDueCallbacksLocked(now / TimeUtils.NANOS_PER_MS);
                if (callbacks != null) {
                    this.mCallbacksRunning = true;
                    if (callbackType == 4) {
                        long jitterNanos = now - frameTimeNanos;
                        Trace.traceCounter(8, "jitterNanos", (int) jitterNanos);
                        if (jitterNanos >= this.mFrameIntervalNanos * 2) {
                            long lastFrameOffset = (jitterNanos % this.mFrameIntervalNanos) + this.mFrameIntervalNanos;
                            if (DEBUG_JANK) {
                                Log.d(TAG, "Commit callback delayed by " + (((float) jitterNanos) * 1.0E-6f) + " ms which is more than twice the frame interval of " + (((float) this.mFrameIntervalNanos) * 1.0E-6f) + " ms!  Setting frame time to " + (((float) lastFrameOffset) * 1.0E-6f) + " ms in the past.");
                                this.mDebugPrintNextFrameTimeDelta = true;
                            }
                            frameTimeNanos2 = now - lastFrameOffset;
                            try {
                                this.mLastFrameTimeNanos = frameTimeNanos2;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    }
                    frameTimeNanos2 = frameTimeNanos;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void doScheduleVsync() {
        synchronized (this.mLock) {
            if (this.mFrameScheduled) {
                scheduleVsyncLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void doScheduleCallback(int callbackType) {
        synchronized (this.mLock) {
            if (!this.mFrameScheduled) {
                long now = SystemClock.uptimeMillis();
                if (this.mCallbackQueues[callbackType].hasDueCallbacksLocked(now)) {
                    scheduleFrameLocked(now);
                }
            }
        }
    }

    @UnsupportedAppUsage
    private void scheduleVsyncLocked() {
        this.mDisplayEventReceiver.scheduleVsync();
    }

    private boolean isRunningOnLooperThreadLocked() {
        return Looper.myLooper() == this.mLooper;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recycleCallbackLocked(CallbackRecord callback) {
        callback.action = null;
        callback.token = null;
        callback.next = this.mCallbackPool;
        this.mCallbackPool = callback;
    }

    /* access modifiers changed from: private */
    public final class FrameHandler extends Handler {
        public FrameHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                Choreographer.this.doFrame(System.nanoTime(), 0);
            } else if (i == 1) {
                Choreographer.this.doScheduleVsync();
            } else if (i == 2) {
                Choreographer.this.doScheduleCallback(msg.arg1);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class FrameDisplayEventReceiver extends DisplayEventReceiver implements Runnable {
        private int mFrame;
        private boolean mHavePendingVsync;
        private long mTimestampNanos;

        public FrameDisplayEventReceiver(Looper looper, int vsyncSource) {
            super(looper, vsyncSource);
        }

        @Override // android.view.DisplayEventReceiver
        public void onVsync(long timestampNanos, long physicalDisplayId, int frame) {
            long now = System.nanoTime();
            if (timestampNanos > now) {
                Log.w(Choreographer.TAG, "Frame time is " + (((float) (timestampNanos - now)) * 1.0E-6f) + " ms in the future!  Check that graphics HAL is generating vsync timestamps using the correct timebase.");
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

    /* access modifiers changed from: private */
    public static final class CallbackRecord {
        public Object action;
        public long dueTime;
        public CallbackRecord next;
        public Object token;

        private CallbackRecord() {
        }

        @UnsupportedAppUsage
        public void run(long frameTimeNanos) {
            if (this.token == Choreographer.FRAME_CALLBACK_TOKEN) {
                ((FrameCallback) this.action).doFrame(frameTimeNanos);
            } else {
                ((Runnable) this.action).run();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class CallbackQueue {
        private CallbackRecord mHead;

        private CallbackQueue() {
        }

        public boolean hasCallbacks() {
            return this.mHead != null;
        }

        public boolean hasDueCallbacksLocked(long now) {
            CallbackRecord callbackRecord = this.mHead;
            return callbackRecord != null && callbackRecord.dueTime <= now;
        }

        public CallbackRecord extractDueCallbacksLocked(long now) {
            CallbackRecord callbacks = this.mHead;
            if (callbacks == null || callbacks.dueTime > now) {
                return null;
            }
            CallbackRecord last = callbacks;
            CallbackRecord next = last.next;
            while (true) {
                if (next == null) {
                    break;
                } else if (next.dueTime > now) {
                    last.next = null;
                    break;
                } else {
                    last = next;
                    next = next.next;
                }
            }
            this.mHead = next;
            return callbacks;
        }

        @UnsupportedAppUsage
        public void addCallbackLocked(long dueTime, Object action, Object token) {
            CallbackRecord callback = Choreographer.this.obtainCallbackLocked(dueTime, action, token);
            CallbackRecord entry = this.mHead;
            if (entry == null) {
                this.mHead = callback;
            } else if (dueTime < entry.dueTime) {
                callback.next = entry;
                this.mHead = callback;
            } else {
                while (true) {
                    if (entry.next == null) {
                        break;
                    } else if (dueTime < entry.next.dueTime) {
                        callback.next = entry.next;
                        break;
                    } else {
                        entry = entry.next;
                    }
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

    public void setIsSFChoregrapher(boolean isSFChoregrapher) {
        this.sIsSFChoregrapher = isSFChoregrapher ? 1 : 0;
    }
}

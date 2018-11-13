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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class Choreographer {
    public static final int CALLBACK_ANIMATION = 1;
    public static final int CALLBACK_COMMIT = 3;
    public static final int CALLBACK_INPUT = 0;
    private static final int CALLBACK_LAST = 3;
    private static final String[] CALLBACK_TRACE_TITLES = null;
    public static final int CALLBACK_TRAVERSAL = 2;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_FRAMES = false;
    private static final boolean DEBUG_JANK = false;
    private static final long DEFAULT_FRAME_DELAY = 10;
    private static final Object FRAME_CALLBACK_TOKEN = null;
    private static final int MSG_DO_FRAME = 0;
    private static final int MSG_DO_SCHEDULE_CALLBACK = 2;
    private static final int MSG_DO_SCHEDULE_VSYNC = 1;
    private static final String TAG = "Choreographer";
    private static final boolean USE_FRAME_TIME = false;
    private static final boolean USE_VSYNC = false;
    private static volatile long sFrameDelay;
    private static final ThreadLocal<Choreographer> sThreadInstance = null;
    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "Jianhua.Lin@Plf.SDK : [-static] Modify for monitor skippedFrames", property = OppoRomType.ROM)
    private final int SKIPPED_FRAME_WARNING_LIMIT;
    private CallbackRecord mCallbackPool;
    private final CallbackQueue[] mCallbackQueues;
    private boolean mCallbacksRunning;
    private boolean mDebugPrintNextFrameTimeDelta;
    private final FrameDisplayEventReceiver mDisplayEventReceiver;
    PerfFrameInfo mFrameInfo;
    private long mFrameIntervalNanos;
    private boolean mFrameScheduled;
    private final FrameHandler mHandler;
    private long mLastFrameTimeNanos;
    private final Object mLock;
    private final Looper mLooper;

    private final class CallbackQueue {
        private CallbackRecord mHead;

        /* synthetic */ CallbackQueue(Choreographer this$0, CallbackQueue callbackQueue) {
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

        /* synthetic */ CallbackRecord(CallbackRecord callbackRecord) {
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

        public FrameDisplayEventReceiver(Looper looper) {
            super(looper);
        }

        public void onVsync(long timestampNanos, int builtInDisplayId, int frame) {
            if (builtInDisplayId != 0) {
                Log.d(Choreographer.TAG, "Received vsync from secondary display, but we don't support this case yet.  Choreographer needs a way to explicitly request vsync for a specific display to ensure it doesn't lose track of its scheduled vsync.");
                scheduleVsync();
                return;
            }
            long now = System.nanoTime();
            if (Choreographer.DEBUG) {
                Log.d(Choreographer.TAG, "onVsync: timestampNanos = " + timestampNanos + ",frame = " + frame + ",now = " + now + ",mHavePendingVsync = " + this.mHavePendingVsync + ",this = " + Choreographer.this);
            }
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
            if (Choreographer.DEBUG) {
                Log.d(Choreographer.TAG, "FrameHandler handleMessage: msg = " + msg.what + ",this = " + this);
            }
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.Choreographer.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.Choreographer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.Choreographer.<clinit>():void");
    }

    /* synthetic */ Choreographer(Looper looper, Choreographer choreographer) {
        this(looper);
    }

    private Choreographer(Looper looper) {
        FrameDisplayEventReceiver frameDisplayEventReceiver;
        this.SKIPPED_FRAME_WARNING_LIMIT = SystemProperties.getInt("debug.choreographer.skipwarning", 30);
        this.mLock = new Object();
        this.mFrameInfo = new PerfFrameInfo();
        this.mLooper = looper;
        this.mHandler = new FrameHandler(looper);
        if (USE_VSYNC) {
            frameDisplayEventReceiver = new FrameDisplayEventReceiver(looper);
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
        if (DEBUG) {
            Log.d(TAG, "Choreographer: mDisplayEventReceiver = " + this.mDisplayEventReceiver + ",USE_VSYNC = " + USE_VSYNC + ",this = " + this);
        }
    }

    private static float getRefreshRate() {
        return DisplayManagerGlobal.getInstance().getDisplayInfo(0).getMode().getRefreshRate();
    }

    public static Choreographer getInstance() {
        return (Choreographer) sThreadInstance.get();
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
        if (DEBUG_FRAMES) {
            Log.d(TAG, "PostCallback: type=" + callbackType + ", action=" + action + ", token=" + token + ", delayMillis=" + delayMillis + ",this = " + this);
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

    public void removeCallbacks(int callbackType, Runnable action, Object token) {
        if (callbackType < 0 || callbackType > 3) {
            throw new IllegalArgumentException("callbackType is invalid");
        }
        removeCallbacksInternal(callbackType, action, token);
    }

    private void removeCallbacksInternal(int callbackType, Object action, Object token) {
        if (DEBUG_FRAMES) {
            Log.d(TAG, "RemoveCallbacks: type=" + callbackType + ", action=" + action + ", token=" + token + ",this = " + this);
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

    private void scheduleFrameLocked(long now) {
        if (!this.mFrameScheduled) {
            this.mFrameScheduled = true;
            Message msg;
            if (USE_VSYNC) {
                if (DEBUG_FRAMES) {
                    Log.d(TAG, "Scheduling next frame on vsync case 1, this = " + this);
                }
                if (isRunningOnLooperThreadLocked()) {
                    scheduleVsyncLocked();
                    return;
                }
                msg = this.mHandler.obtainMessage(1);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtFrontOfQueue(msg);
                if (DEBUG) {
                    Log.d(TAG, "Scheduling next frame on vsync case 2, this = " + this);
                    return;
                }
                return;
            }
            long nextFrameTime = Math.max((this.mLastFrameTimeNanos / TimeUtils.NANOS_PER_MS) + sFrameDelay, now);
            if (DEBUG_FRAMES) {
                Log.d(TAG, "Scheduling next frame in " + (nextFrameTime - now) + " ms, this = " + this);
            }
            msg = this.mHandler.obtainMessage(0);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageAtTime(msg, nextFrameTime);
        }
    }

    /* JADX WARNING: Missing block: B:37:?, code:
            android.os.Trace.traceBegin(8, "Choreographer#doFrame");
            android.view.animation.AnimationUtils.lockAnimationClock(r24 / android.util.TimeUtils.NANOS_PER_MS);
            r23.mFrameInfo.markInputHandlingStart();
            doCallbacks(0, r24);
            r23.mFrameInfo.markAnimationsStart();
            doCallbacks(1, r24);
            r23.mFrameInfo.markPerformTraversalsStart();
            doCallbacks(2, r24);
            doCallbacks(3, r24);
     */
    /* JADX WARNING: Missing block: B:45:0x02a3, code:
            android.view.animation.AnimationUtils.unlockAnimationClock();
            android.os.Trace.traceEnd(8);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void doFrame(long frameTimeNanos, int frame) {
        long startNanos;
        synchronized (this.mLock) {
            if (DEBUG) {
                Log.d(TAG, "doFrame:  frameTimeNanos = " + frameTimeNanos + ",frame = " + frame + ",mFrameScheduled = " + this.mFrameScheduled + ",this = " + this);
            }
            if (this.mFrameScheduled) {
                if (DEBUG_JANK && this.mDebugPrintNextFrameTimeDelta) {
                    this.mDebugPrintNextFrameTimeDelta = false;
                    Log.d(TAG, "Frame time delta: " + (((float) (frameTimeNanos - this.mLastFrameTimeNanos)) * 1.0E-6f) + " ms");
                }
                long intendedFrameTimeNanos = frameTimeNanos;
                startNanos = System.nanoTime();
                long jitterNanos = startNanos - frameTimeNanos;
                if (jitterNanos >= this.mFrameIntervalNanos) {
                    long skippedFrames = jitterNanos / this.mFrameIntervalNanos;
                    if (skippedFrames >= ((long) this.SKIPPED_FRAME_WARNING_LIMIT)) {
                        Log.i(TAG, "Skipped " + skippedFrames + " frames!  " + "The application may be doing too much work on its main thread.");
                    }
                    long lastFrameOffset = jitterNanos % this.mFrameIntervalNanos;
                    if (DEBUG_JANK) {
                        Log.d(TAG, "Missed vsync by " + (((float) jitterNanos) * 1.0E-6f) + " ms " + "which is more than the frame interval of " + (((float) this.mFrameIntervalNanos) * 1.0E-6f) + " ms!  " + "Skipping " + skippedFrames + " frames and setting frame " + "time to " + (((float) lastFrameOffset) * 1.0E-6f) + " ms in the past.");
                    }
                    frameTimeNanos = startNanos - lastFrameOffset;
                }
                if (frameTimeNanos < this.mLastFrameTimeNanos) {
                    if (DEBUG_JANK) {
                        Log.d(TAG, "Frame time appears to be going backwards.  May be due to a previously skipped frame.  Waiting for next vsync.");
                    }
                    scheduleVsyncLocked();
                    return;
                }
                this.mFrameInfo.setVsync(intendedFrameTimeNanos, frameTimeNanos);
                this.mFrameScheduled = false;
                this.mLastFrameTimeNanos = frameTimeNanos;
            } else {
                return;
            }
        }
        this.mFrameInfo.markDoFrameEnd();
        if (DEBUG_FRAMES) {
            Log.d(TAG, "Frame " + frame + ": Finished, took " + (((float) (System.nanoTime() - startNanos)) * 1.0E-6f) + " ms, latency " + (((float) (startNanos - frameTimeNanos)) * 1.0E-6f) + " ms, this = " + this);
        }
    }

    /* JADX WARNING: Missing block: B:19:?, code:
            android.os.Trace.traceBegin(8, CALLBACK_TRACE_TITLES[r21]);
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:20:0x00cd, code:
            if (r4 == null) goto L_0x0138;
     */
    /* JADX WARNING: Missing block: B:22:0x00d1, code:
            if (DEBUG_FRAMES == false) goto L_0x012d;
     */
    /* JADX WARNING: Missing block: B:23:0x00d3, code:
            android.util.Log.d(TAG, "RunCallback: type=" + r21 + ", action=" + r4.action + ", token=" + r4.token + ", latencyMillis=" + (android.os.SystemClock.uptimeMillis() - r4.dueTime) + ",this = " + r20);
     */
    /* JADX WARNING: Missing block: B:24:0x012d, code:
            r4.run(r22);
            r4 = r4.next;
     */
    /* JADX WARNING: Missing block: B:29:0x0138, code:
            r14 = r20.mLock;
     */
    /* JADX WARNING: Missing block: B:30:0x013c, code:
            monitor-enter(r14);
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            r20.mCallbacksRunning = false;
     */
    /* JADX WARNING: Missing block: B:34:0x0142, code:
            r10 = r5.next;
            recycleCallbackLocked(r5);
     */
    /* JADX WARNING: Missing block: B:35:0x0149, code:
            r5 = r10;
     */
    /* JADX WARNING: Missing block: B:36:0x014a, code:
            if (r10 != null) goto L_0x0142;
     */
    /* JADX WARNING: Missing block: B:37:0x014c, code:
            monitor-exit(r14);
     */
    /* JADX WARNING: Missing block: B:38:0x014d, code:
            android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Missing block: B:39:0x0152, code:
            return;
     */
    /* JADX WARNING: Missing block: B:45:0x015b, code:
            monitor-enter(r20.mLock);
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            r20.mCallbacksRunning = false;
     */
    /* JADX WARNING: Missing block: B:49:0x0161, code:
            r10 = r5.next;
            recycleCallbackLocked(r5);
     */
    /* JADX WARNING: Missing block: B:50:0x0168, code:
            r5 = r10;
     */
    /* JADX WARNING: Missing block: B:51:0x0169, code:
            if (r10 != null) goto L_0x0161;
     */
    /* JADX WARNING: Missing block: B:53:0x016c, code:
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
                    long lastFrameOffset = (jitterNanos % this.mFrameIntervalNanos) + this.mFrameIntervalNanos;
                    if (DEBUG_JANK) {
                        Log.d(TAG, "Commit callback delayed by " + (((float) jitterNanos) * 1.0E-6f) + " ms which is more than twice the frame interval of " + (((float) this.mFrameIntervalNanos) * 1.0E-6f) + " ms!  " + "Setting frame time to " + (((float) lastFrameOffset) * 1.0E-6f) + " ms in the past.");
                        this.mDebugPrintNextFrameTimeDelta = true;
                    }
                    frameTimeNanos = now - lastFrameOffset;
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

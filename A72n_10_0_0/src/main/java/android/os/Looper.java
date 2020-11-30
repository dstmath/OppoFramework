package android.os;

import android.annotation.UnsupportedAppUsage;
import android.app.job.JobInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.provider.Telephony;
import android.util.Log;
import android.util.Printer;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;

public final class Looper {
    private static final String TAG = "Looper";
    @UnsupportedAppUsage
    private static Looper sMainLooper;
    private static Observer sObserver;
    @UnsupportedAppUsage
    static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
    @UnsupportedAppUsage
    private Printer mLogging;
    @UnsupportedAppUsage
    final MessageQueue mQueue;
    private long mSlowDeliveryThresholdMs;
    private long mSlowDispatchThresholdMs;
    final Thread mThread = Thread.currentThread();
    private long mTraceTag;

    public interface Observer {
        void dispatchingThrewException(Object obj, Message message, Exception exc);

        Object messageDispatchStarting();

        void messageDispatched(Object obj, Message message);
    }

    public static void prepare() {
        prepare(true);
    }

    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() == null) {
            sThreadLocal.set(new Looper(quitAllowed));
            return;
        }
        throw new RuntimeException("Only one Looper may be created per thread");
    }

    public static void prepareMainLooper() {
        prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper == null) {
                sMainLooper = myLooper();
            } else {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
        }
    }

    public static Looper getMainLooper() {
        Looper looper;
        synchronized (Looper.class) {
            looper = sMainLooper;
        }
        return looper;
    }

    public static void setObserver(Observer observer) {
        sObserver = observer;
    }

    /* JADX INFO: Multiple debug info for r1v18 'thresholdOverride'  int: [D('me' android.os.Looper), D('thresholdOverride' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0277 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00ce  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00f5  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0104  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x011d  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0130 A[SYNTHETIC, Splitter:B:53:0x0130] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0158 A[Catch:{ Exception -> 0x0145, all -> 0x0134 }] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x015d  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0166  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x016b  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0170  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0177  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01c8  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01d9  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0209  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x022f  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x02ac A[SYNTHETIC, Splitter:B:99:0x02ac] */
    public static void loop() {
        LooperMessageSuperviser looperMessageSuperviser;
        LooperMsgTimeTracker msgTimeTracker;
        Looper me;
        int thresholdOverride;
        long ident;
        boolean logSlowDelivery;
        int thresholdOverride2;
        boolean logSlowDispatch;
        Object token;
        long origWorkSource;
        long traceTag;
        Exception exception;
        Message msg;
        Observer observer;
        Exception exception2;
        LooperMessageSuperviser looperMessageSuperviser2;
        LooperMsgTimeTracker msgTimeTracker2;
        Message msg2;
        String str;
        Printer logging;
        long newIdent;
        Looper me2 = myLooper();
        if (me2 != null) {
            MessageQueue queue = me2.mQueue;
            boolean mainThread = "main".equals(Thread.currentThread().getName());
            if (mainThread) {
                LooperMsgTimeTracker msgTimeTracker3 = new LooperMsgTimeTracker();
                looperMessageSuperviser = new LooperMessageSuperviser();
                msgTimeTracker = msgTimeTracker3;
            } else {
                looperMessageSuperviser = null;
                msgTimeTracker = null;
            }
            Binder.clearCallingIdentity();
            long ident2 = Binder.clearCallingIdentity();
            int thresholdOverride3 = SystemProperties.getInt("log.looper." + Process.myUid() + "." + Thread.currentThread().getName() + ".slow", 0);
            boolean slowDeliveryDetected = false;
            while (true) {
                Message msg3 = queue.next();
                if (msg3 != null) {
                    Printer logging2 = me2.mLogging;
                    if (logging2 != null) {
                        logging2.println(">>>>> Dispatching to " + msg3.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg3.callback + ": " + msg3.what);
                    }
                    Observer observer2 = sObserver;
                    long traceTag2 = me2.mTraceTag;
                    long slowDispatchThresholdMs = me2.mSlowDispatchThresholdMs;
                    long slowDeliveryThresholdMs = me2.mSlowDeliveryThresholdMs;
                    if (thresholdOverride3 > 0) {
                        me = me2;
                        thresholdOverride = thresholdOverride3;
                        slowDispatchThresholdMs = (long) thresholdOverride;
                        slowDeliveryThresholdMs = (long) thresholdOverride;
                    } else {
                        me = me2;
                        thresholdOverride = thresholdOverride3;
                    }
                    boolean needStartTime = true;
                    if (slowDeliveryThresholdMs > 0) {
                        ident = ident2;
                        if (msg3.when > 0) {
                            logSlowDelivery = true;
                            boolean logSlowDispatch2 = slowDispatchThresholdMs <= 0;
                            if (!logSlowDelivery && !logSlowDispatch2) {
                                needStartTime = false;
                            }
                            if (traceTag2 != 0 || !Trace.isTagEnabled(traceTag2)) {
                                thresholdOverride2 = thresholdOverride;
                            } else {
                                thresholdOverride2 = thresholdOverride;
                                Trace.traceBegin(traceTag2, msg3.target.getTraceName(msg3));
                            }
                            if (msgTimeTracker != null) {
                                msgTimeTracker.start(msg3);
                            }
                            long timeFirst = SystemClock.uptimeMillis();
                            int pid = Process.myPid();
                            if (looperMessageSuperviser != null) {
                                looperMessageSuperviser.beginLooperMessage(msg3, pid);
                            }
                            long dispatchStart = !needStartTime ? SystemClock.uptimeMillis() : 0;
                            if (observer2 == null) {
                                logSlowDispatch = logSlowDispatch2;
                                token = observer2.messageDispatchStarting();
                            } else {
                                logSlowDispatch = logSlowDispatch2;
                                token = null;
                            }
                            origWorkSource = ThreadLocalWorkSource.setUid(msg3.workSourceUid);
                            msg3.target.dispatchMessage(msg3);
                            if (observer2 != null) {
                                try {
                                    observer2.messageDispatched(token, msg3);
                                } catch (Exception e) {
                                    exception2 = e;
                                    traceTag = traceTag2;
                                    observer = observer2;
                                    msg = msg3;
                                    if (observer != null) {
                                        try {
                                            observer.dispatchingThrewException(token, msg, exception2);
                                        } catch (Throwable th) {
                                            exception = th;
                                            ThreadLocalWorkSource.restore(origWorkSource);
                                            if (traceTag != 0) {
                                                Trace.traceEnd(traceTag);
                                            }
                                            throw exception;
                                        }
                                    }
                                    throw exception2;
                                } catch (Throwable th2) {
                                    exception = th2;
                                    traceTag = traceTag2;
                                    ThreadLocalWorkSource.restore(origWorkSource);
                                    if (traceTag != 0) {
                                    }
                                    throw exception;
                                }
                            }
                            long dispatchEnd = !logSlowDispatch2 ? SystemClock.uptimeMillis() : 0;
                            ThreadLocalWorkSource.restore(origWorkSource);
                            if (traceTag2 != 0) {
                                Trace.traceEnd(traceTag2);
                            }
                            if (msgTimeTracker != null) {
                                msgTimeTracker.stop();
                            }
                            if (looperMessageSuperviser != null) {
                                looperMessageSuperviser.endLooperMessage(msg3, timeFirst, pid);
                            }
                            if (logSlowDelivery) {
                                msgTimeTracker2 = msgTimeTracker;
                                looperMessageSuperviser2 = looperMessageSuperviser;
                                str = TAG;
                                msg2 = msg3;
                                logging = logging2;
                            } else if (slowDeliveryDetected) {
                                msgTimeTracker2 = msgTimeTracker;
                                looperMessageSuperviser2 = looperMessageSuperviser;
                                if (dispatchStart - msg3.when <= 10) {
                                    Slog.w(TAG, "Drained");
                                    slowDeliveryDetected = false;
                                    str = TAG;
                                    msg2 = msg3;
                                    logging = logging2;
                                } else {
                                    str = TAG;
                                    msg2 = msg3;
                                    logging = logging2;
                                }
                            } else {
                                msgTimeTracker2 = msgTimeTracker;
                                looperMessageSuperviser2 = looperMessageSuperviser;
                                long j = msg3.when;
                                str = TAG;
                                logging = logging2;
                                msg2 = msg3;
                                if (showSlowLog(slowDeliveryThresholdMs, j, dispatchStart, Telephony.RcsColumns.RcsMessageDeliveryColumns.DELIVERY_URI_PART, msg2)) {
                                    slowDeliveryDetected = true;
                                }
                            }
                            if (logSlowDispatch) {
                                showSlowLog(slowDispatchThresholdMs, dispatchStart, dispatchEnd, "dispatch", msg2);
                                if (dispatchEnd - dispatchStart > JobInfo.MIN_BACKOFF_MILLIS || (dispatchEnd - dispatchStart > 4000 && "android.view.Choreographer$FrameHandler".equals(msg2.target))) {
                                    Log.p("Quality", "07 01 blocked");
                                }
                            }
                            if (logging != null) {
                                logging.println("<<<<< Finished to " + msg2.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg2.callback);
                            }
                            newIdent = Binder.clearCallingIdentity();
                            if (ident == newIdent) {
                                Log.wtf(str, "Thread identity changed from 0x" + Long.toHexString(ident) + " to 0x" + Long.toHexString(newIdent) + " while dispatching to " + msg2.target.getClass().getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg2.callback + " what=" + msg2.what);
                            }
                            msg2.recycleUnchecked();
                            queue = queue;
                            mainThread = mainThread;
                            me2 = me;
                            ident2 = ident;
                            thresholdOverride3 = thresholdOverride2;
                            msgTimeTracker = msgTimeTracker2;
                            looperMessageSuperviser = looperMessageSuperviser2;
                        }
                    } else {
                        ident = ident2;
                    }
                    logSlowDelivery = false;
                    if (slowDispatchThresholdMs <= 0) {
                    }
                    needStartTime = false;
                    if (traceTag2 != 0) {
                    }
                    thresholdOverride2 = thresholdOverride;
                    if (msgTimeTracker != null) {
                    }
                    long timeFirst2 = SystemClock.uptimeMillis();
                    int pid2 = Process.myPid();
                    if (looperMessageSuperviser != null) {
                    }
                    if (!needStartTime) {
                    }
                    if (observer2 == null) {
                    }
                    origWorkSource = ThreadLocalWorkSource.setUid(msg3.workSourceUid);
                    try {
                        msg3.target.dispatchMessage(msg3);
                        if (observer2 != null) {
                        }
                        if (!logSlowDispatch2) {
                        }
                        ThreadLocalWorkSource.restore(origWorkSource);
                        if (traceTag2 != 0) {
                        }
                        if (msgTimeTracker != null) {
                        }
                        if (looperMessageSuperviser != null) {
                        }
                        if (logSlowDelivery) {
                        }
                        if (logSlowDispatch) {
                        }
                        if (logging != null) {
                        }
                        newIdent = Binder.clearCallingIdentity();
                        if (ident == newIdent) {
                        }
                        msg2.recycleUnchecked();
                        queue = queue;
                        mainThread = mainThread;
                        me2 = me;
                        ident2 = ident;
                        thresholdOverride3 = thresholdOverride2;
                        msgTimeTracker = msgTimeTracker2;
                        looperMessageSuperviser = looperMessageSuperviser2;
                    } catch (Exception e2) {
                        exception2 = e2;
                        traceTag = traceTag2;
                        observer = observer2;
                        msg = msg3;
                        if (observer != null) {
                        }
                        throw exception2;
                    } catch (Throwable th3) {
                        exception = th3;
                        traceTag = traceTag2;
                        ThreadLocalWorkSource.restore(origWorkSource);
                        if (traceTag != 0) {
                        }
                        throw exception;
                    }
                } else {
                    return;
                }
            }
        } else {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
    }

    private static boolean showSlowLog(long threshold, long measureStart, long measureEnd, String what, Message msg) {
        long actualTime = measureEnd - measureStart;
        if (actualTime < threshold) {
            return false;
        }
        Slog.w(TAG, "Slow " + what + " took " + actualTime + "ms " + Thread.currentThread().getName() + " h=" + msg.target.getClass().getName() + " c=" + msg.callback + " m=" + msg.what);
        return true;
    }

    public static Looper myLooper() {
        return sThreadLocal.get();
    }

    public static MessageQueue myQueue() {
        return myLooper().mQueue;
    }

    private Looper(boolean quitAllowed) {
        this.mQueue = new MessageQueue(quitAllowed);
    }

    public boolean isCurrentThread() {
        return Thread.currentThread() == this.mThread;
    }

    public void setMessageLogging(Printer printer) {
        this.mLogging = printer;
    }

    @UnsupportedAppUsage
    public void setTraceTag(long traceTag) {
        this.mTraceTag = traceTag;
    }

    public void setSlowLogThresholdMs(long slowDispatchThresholdMs, long slowDeliveryThresholdMs) {
        this.mSlowDispatchThresholdMs = slowDispatchThresholdMs;
        this.mSlowDeliveryThresholdMs = slowDeliveryThresholdMs;
    }

    public void quit() {
        this.mQueue.quit(false);
    }

    public void quitSafely() {
        this.mQueue.quit(true);
    }

    public Thread getThread() {
        return this.mThread;
    }

    public MessageQueue getQueue() {
        return this.mQueue;
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + toString());
        MessageQueue messageQueue = this.mQueue;
        messageQueue.dump(pw, prefix + "  ", null);
    }

    public void dump(Printer pw, String prefix, Handler handler) {
        pw.println(prefix + toString());
        MessageQueue messageQueue = this.mQueue;
        messageQueue.dump(pw, prefix + "  ", handler);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long looperToken = proto.start(fieldId);
        proto.write(1138166333441L, this.mThread.getName());
        proto.write(1112396529666L, this.mThread.getId());
        MessageQueue messageQueue = this.mQueue;
        if (messageQueue != null) {
            messageQueue.writeToProto(proto, 1146756268035L);
        }
        proto.end(looperToken);
    }

    public String toString() {
        return "Looper (" + this.mThread.getName() + ", tid " + this.mThread.getId() + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }
}

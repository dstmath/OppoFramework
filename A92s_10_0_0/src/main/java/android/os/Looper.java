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

    /* JADX WARNING: Removed duplicated region for block: B:105:0x023a A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00c4  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ea  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00ef  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x010c A[SYNTHETIC, Splitter:B:49:0x010c] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x012e A[Catch:{ Exception -> 0x011e, all -> 0x0110 }] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0133  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x013c  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0141  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0148  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x018f  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x019d  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x01cc  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01f2  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0267 A[SYNTHETIC, Splitter:B:93:0x0267] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0274  */
    public static void loop() {
        LooperMsgTimeTracker msgTimeTracker;
        int thresholdOverride;
        long ident;
        boolean logSlowDelivery;
        boolean logSlowDispatch;
        Looper me;
        Object token;
        long origWorkSource;
        long traceTag;
        Message msg;
        Observer observer;
        LooperMsgTimeTracker msgTimeTracker2;
        Printer logging;
        Message msg2;
        String str;
        long newIdent;
        Looper me2 = myLooper();
        if (me2 != null) {
            MessageQueue queue = me2.mQueue;
            boolean mainThread = "main".equals(Thread.currentThread().getName());
            if (mainThread) {
                msgTimeTracker = new LooperMsgTimeTracker();
            } else {
                msgTimeTracker = null;
            }
            Binder.clearCallingIdentity();
            long ident2 = Binder.clearCallingIdentity();
            int thresholdOverride2 = SystemProperties.getInt("log.looper." + Process.myUid() + "." + Thread.currentThread().getName() + ".slow", 0);
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
                    if (thresholdOverride2 > 0) {
                        thresholdOverride = thresholdOverride2;
                        slowDispatchThresholdMs = (long) thresholdOverride;
                        slowDeliveryThresholdMs = (long) thresholdOverride;
                    } else {
                        thresholdOverride = thresholdOverride2;
                    }
                    boolean needStartTime = true;
                    if (slowDeliveryThresholdMs > 0) {
                        ident = ident2;
                        if (msg3.when > 0) {
                            logSlowDelivery = true;
                            logSlowDispatch = slowDispatchThresholdMs <= 0;
                            if (!logSlowDelivery && !logSlowDispatch) {
                                needStartTime = false;
                            }
                            if (traceTag2 != 0 || !Trace.isTagEnabled(traceTag2)) {
                                me = me2;
                            } else {
                                me = me2;
                                Trace.traceBegin(traceTag2, msg3.target.getTraceName(msg3));
                            }
                            if (msgTimeTracker != null) {
                                msgTimeTracker.start(msg3);
                            }
                            long dispatchStart = !needStartTime ? SystemClock.uptimeMillis() : 0;
                            token = null;
                            if (observer2 != null) {
                                token = observer2.messageDispatchStarting();
                            }
                            origWorkSource = ThreadLocalWorkSource.setUid(msg3.workSourceUid);
                            msg3.target.dispatchMessage(msg3);
                            if (observer2 != null) {
                                try {
                                    observer2.messageDispatched(token, msg3);
                                } catch (Exception e) {
                                    exception = e;
                                    traceTag = traceTag2;
                                    observer = observer2;
                                    msg = msg3;
                                    if (observer != null) {
                                    }
                                    throw exception;
                                } catch (Throwable th) {
                                    exception = th;
                                    traceTag = traceTag2;
                                    ThreadLocalWorkSource.restore(origWorkSource);
                                    if (traceTag != 0) {
                                    }
                                    throw exception;
                                }
                            }
                            long dispatchEnd = !logSlowDispatch ? SystemClock.uptimeMillis() : 0;
                            ThreadLocalWorkSource.restore(origWorkSource);
                            if (traceTag2 != 0) {
                                Trace.traceEnd(traceTag2);
                            }
                            if (msgTimeTracker != null) {
                                msgTimeTracker.stop();
                            }
                            if (logSlowDelivery) {
                                msgTimeTracker2 = msgTimeTracker;
                                str = TAG;
                                msg2 = msg3;
                                logging = logging2;
                            } else if (slowDeliveryDetected) {
                                msgTimeTracker2 = msgTimeTracker;
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
                                long j = msg3.when;
                                str = TAG;
                                msg2 = msg3;
                                logging = logging2;
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
                            ident2 = ident;
                            me2 = me;
                            thresholdOverride2 = thresholdOverride;
                            msgTimeTracker = msgTimeTracker2;
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
                    me = me2;
                    if (msgTimeTracker != null) {
                    }
                    if (!needStartTime) {
                    }
                    token = null;
                    if (observer2 != null) {
                    }
                    origWorkSource = ThreadLocalWorkSource.setUid(msg3.workSourceUid);
                    try {
                        msg3.target.dispatchMessage(msg3);
                        if (observer2 != null) {
                        }
                        if (!logSlowDispatch) {
                        }
                        ThreadLocalWorkSource.restore(origWorkSource);
                        if (traceTag2 != 0) {
                        }
                        if (msgTimeTracker != null) {
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
                        ident2 = ident;
                        me2 = me;
                        thresholdOverride2 = thresholdOverride;
                        msgTimeTracker = msgTimeTracker2;
                    } catch (Exception e2) {
                        exception = e2;
                        traceTag = traceTag2;
                        observer = observer2;
                        msg = msg3;
                        if (observer != null) {
                            try {
                                observer.dispatchingThrewException(token, msg, exception);
                            } catch (Throwable th2) {
                                exception = th2;
                                ThreadLocalWorkSource.restore(origWorkSource);
                                if (traceTag != 0) {
                                    Trace.traceEnd(traceTag);
                                }
                                throw exception;
                            }
                        }
                        throw exception;
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

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.util.proto.ProtoOutputStream.write(long, long):void
     arg types: [int, long]
     candidates:
      android.util.proto.ProtoOutputStream.write(long, double):void
      android.util.proto.ProtoOutputStream.write(long, float):void
      android.util.proto.ProtoOutputStream.write(long, int):void
      android.util.proto.ProtoOutputStream.write(long, java.lang.String):void
      android.util.proto.ProtoOutputStream.write(long, boolean):void
      android.util.proto.ProtoOutputStream.write(long, byte[]):void
      android.util.proto.ProtoOutputStream.write(long, long):void */
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

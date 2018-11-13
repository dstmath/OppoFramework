package android.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Printer;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;

public final class Looper {
    private static final String DEBUG_TAG = "ANR_LOG";
    private static final long DISPATCH_TIMEOUT = 1500;
    private static final String TAG = "Looper";
    private static Looper sMainLooper;
    static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal();
    private Printer mLogging;
    final MessageQueue mQueue;
    private long mSlowDispatchThresholdMs;
    final Thread mThread = Thread.currentThread();
    private long mTraceTag;

    public static void prepare() {
        prepare(true);
    }

    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper(quitAllowed));
    }

    public static void prepareMainLooper() {
        prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper != null) {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
            sMainLooper = myLooper();
        }
    }

    public static Looper getMainLooper() {
        Looper looper;
        synchronized (Looper.class) {
            looper = sMainLooper;
        }
        return looper;
    }

    public static void loop() {
        Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
        MessageQueue queue = me.mQueue;
        boolean mainThread = "main".equals(Thread.currentThread().getName());
        Binder.clearCallingIdentity();
        long ident = Binder.clearCallingIdentity();
        while (true) {
            Message msg = queue.next();
            if (msg != null) {
                Printer logging = me.mLogging;
                if (logging != null) {
                    logging.println(">>>>> Dispatching to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + ": " + msg.what);
                }
                long slowDispatchThresholdMs = me.mSlowDispatchThresholdMs;
                long traceTag = me.mTraceTag;
                if (traceTag != 0 && Trace.isTagEnabled(traceTag)) {
                    Trace.traceBegin(traceTag, msg.target.getTraceName(msg));
                }
                long timeFirst = SystemClock.uptimeMillis();
                long start = slowDispatchThresholdMs == 0 ? 0 : SystemClock.uptimeMillis();
                try {
                    msg.target.dispatchMessage(msg);
                    long end = slowDispatchThresholdMs == 0 ? 0 : SystemClock.uptimeMillis();
                    if (traceTag != 0) {
                        Trace.traceEnd(traceTag);
                    }
                    if (slowDispatchThresholdMs > 0) {
                        long time = end - start;
                        if (time > slowDispatchThresholdMs) {
                            Slog.w(TAG, "Dispatch took " + time + "ms on " + Thread.currentThread().getName() + ", h=" + msg.target + " cb=" + msg.callback + " msg=" + msg.what);
                        }
                    }
                    if (mainThread) {
                        long cost = SystemClock.uptimeMillis() - timeFirst;
                        if (cost >= DISPATCH_TIMEOUT) {
                            try {
                                String temp = "Blocked msg = " + msg.toStringLite(cost + timeFirst, true) + " , cost  = " + cost + " ms";
                                Log.e(DEBUG_TAG, ">>> msg's executing time is too long");
                                Log.e(DEBUG_TAG, temp);
                                int n = 0;
                                Log.e(DEBUG_TAG, ">>>Current msg List is:");
                                for (Message tmp = queue.mMessages; tmp != null; tmp = tmp.next) {
                                    n++;
                                    if (n > 10) {
                                        break;
                                    }
                                    Log.e(DEBUG_TAG, "Current msg <" + n + "> " + " = " + tmp.toStringLite(cost + timeFirst, true));
                                }
                                Log.e(DEBUG_TAG, ">>>CURRENT MSG DUMP OVER<<<");
                            } catch (NullPointerException e) {
                                Log.e(TAG, "Failure log ANR msg." + e);
                            }
                        }
                    }
                    if (logging != null) {
                        logging.println("<<<<< Finished to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback);
                    }
                    long newIdent = Binder.clearCallingIdentity();
                    if (ident != newIdent) {
                        Log.wtf(TAG, "Thread identity changed from 0x" + Long.toHexString(ident) + " to 0x" + Long.toHexString(newIdent) + " while dispatching to " + msg.target.getClass().getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + " what=" + msg.what);
                    }
                    msg.recycleUnchecked();
                } catch (Throwable th) {
                    if (traceTag != 0) {
                        Trace.traceEnd(traceTag);
                    }
                }
            } else {
                return;
            }
        }
    }

    public static Looper myLooper() {
        return (Looper) sThreadLocal.get();
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

    public void setTraceTag(long traceTag) {
        this.mTraceTag = traceTag;
    }

    public void setSlowDispatchThresholdMs(long slowDispatchThresholdMs) {
        this.mSlowDispatchThresholdMs = slowDispatchThresholdMs;
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
        this.mQueue.dump(pw, prefix + "  ", null);
    }

    public void dump(Printer pw, String prefix, Handler handler) {
        pw.println(prefix + toString());
        this.mQueue.dump(pw, prefix + "  ", handler);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long looperToken = proto.start(fieldId);
        proto.write(1159641169921L, this.mThread.getName());
        proto.write(LooperProto.THREAD_ID, this.mThread.getId());
        proto.write(1112396529667L, System.identityHashCode(this));
        this.mQueue.writeToProto(proto, LooperProto.QUEUE);
        proto.end(looperToken);
    }

    public String toString() {
        return "Looper (" + this.mThread.getName() + ", tid " + this.mThread.getId() + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }
}

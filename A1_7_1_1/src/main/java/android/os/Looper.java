package android.os;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.hardware.Camera.Parameters;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.MessageMonitorLogger.MonitorMSGInfo;
import android.util.Log;
import android.util.Printer;
import com.android.internal.util.FastPrintWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

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
public final class Looper {
    private static final String DEBUG_TAG = "ANR_LOG";
    private static final long DISPATCH_TIMEOUT = 1500;
    private static final boolean IS_USER_BUILD = false;
    private static final String TAG = "Looper";
    private static Looper sMainLooper;
    static final ThreadLocal<Looper> sThreadLocal = null;
    private Printer mLogging;
    private Printer mMsgMonitorLogging;
    final MessageQueue mQueue;
    final Thread mThread;
    private long mTraceTag;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.os.Looper.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.os.Looper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Looper.<clinit>():void");
    }

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

    /* JADX WARNING: Removed duplicated region for block: B:50:0x02b4  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x02f8  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0416 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03aa  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x02b4  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x02f8  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03aa  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0416 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void loop() {
        NullPointerException e;
        long newIdent;
        Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
        MessageQueue queue = me.mQueue;
        boolean mainThread = Parameters.SENSOR_DEV_MAIN.equals(Thread.currentThread().getName());
        StringWriter sw = new StringWriter();
        PrintWriter fastPrintWriter = new FastPrintWriter(sw, false, 128);
        Binder.clearCallingIdentity();
        long ident = Binder.clearCallingIdentity();
        while (true) {
            Message msg = queue.next();
            if (msg != null) {
                Printer msglogging;
                MonitorMSGInfo monitorMsg;
                Printer logging = me.mLogging;
                if (logging != null) {
                    logging.println(">>>>> Dispatching to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + ": " + msg.what);
                }
                if (!IS_USER_BUILD) {
                    msglogging = me.mMsgMonitorLogging;
                    if (msglogging != null) {
                        msglogging.println(">>>>> Dispatching to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + ": " + msg.what);
                    }
                    if (MessageMonitorLogger.monitorMsg.containsKey(msg)) {
                        monitorMsg = (MonitorMSGInfo) MessageMonitorLogger.monitorMsg.get(msg);
                        if (MessageMonitorLogger.mMsgLoggerHandler.hasMessages(MessageMonitorLogger.START_MONITOR_PENDING_TIMEOUT_MSG, monitorMsg)) {
                            Log.d(TAG, "RemoveMessages PENDING_TIMEOUT_MSG msg= " + msg);
                            MessageMonitorLogger.mMsgLoggerHandler.removeMessages(MessageMonitorLogger.START_MONITOR_PENDING_TIMEOUT_MSG, monitorMsg);
                            try {
                                if (monitorMsg.executionTimeout > 100) {
                                    Message msg1 = MessageMonitorLogger.mMsgLoggerHandler.obtainMessage(MessageMonitorLogger.START_MONITOR_EXECUTION_TIMEOUT_MSG, monitorMsg);
                                    MessageMonitorLogger.mMsgLoggerHandler.sendMessageDelayed(msg1, monitorMsg.executionTimeout);
                                } else {
                                    MessageMonitorLogger.monitorMsg.remove(msg);
                                    if (monitorMsg.executionTimeout != -1) {
                                        throw new IllegalArgumentException("Execution timeout <100 ms!");
                                    }
                                }
                            } catch (IllegalArgumentException e2) {
                                Log.d(TAG, "Execution timeout exception " + e2);
                            }
                        }
                    }
                }
                long traceTag = me.mTraceTag;
                if (traceTag != 0 && Trace.isTagEnabled(traceTag)) {
                    Trace.traceBegin(traceTag, msg.target.getTraceName(msg));
                }
                long time = SystemClock.uptimeMillis();
                try {
                    msg.target.dispatchMessage(msg);
                    if (mainThread) {
                        long cost = SystemClock.uptimeMillis() - time;
                        if (cost >= DISPATCH_TIMEOUT) {
                            try {
                                PrintWriter pw;
                                String temp = "Blocked msg = " + msg.toStringLite(cost + time, true) + " , cost  = " + cost + " ms";
                                pw.println(temp);
                                Log.e(DEBUG_TAG, ">>> msg's executing time is too long");
                                Log.e(DEBUG_TAG, temp);
                                int n = 0;
                                Log.e(DEBUG_TAG, ">>>Current msg List is:");
                                for (Message tmp = queue.mMessages; tmp != null; tmp = tmp.next) {
                                    n++;
                                    if (n > 20) {
                                        break;
                                    }
                                    temp = "Current msg <" + n + "> " + " = " + tmp.toStringLite(cost + time, true);
                                    pw.println(temp);
                                    Log.e(DEBUG_TAG, temp);
                                }
                                Log.e(DEBUG_TAG, ">>>CURRENT MSG DUMP OVER<<<");
                                pw.flush();
                                try {
                                    ActivityManagerNative.getDefault().reportJunkFromApp(TAG, ActivityThread.currentPackageName(), sw.toString(), false);
                                } catch (RemoteException e3) {
                                    Log.e(DEBUG_TAG, e3.toString());
                                }
                                Writer sw2 = new StringWriter();
                                try {
                                    pw = new FastPrintWriter(sw2, false, 128);
                                    sw = sw2;
                                } catch (NullPointerException e4) {
                                    e = e4;
                                    Writer sw3 = sw2;
                                    Log.e(TAG, "Failure log ANR msg." + e);
                                    if (logging != null) {
                                    }
                                    if (!IS_USER_BUILD) {
                                    }
                                    newIdent = Binder.clearCallingIdentity();
                                    if (ident != newIdent) {
                                    }
                                    msg.recycleUnchecked();
                                }
                            } catch (NullPointerException e5) {
                                e = e5;
                                Log.e(TAG, "Failure log ANR msg." + e);
                                if (logging != null) {
                                }
                                if (IS_USER_BUILD) {
                                }
                                newIdent = Binder.clearCallingIdentity();
                                if (ident != newIdent) {
                                }
                                msg.recycleUnchecked();
                            }
                        }
                    }
                    if (logging != null) {
                        logging.println("<<<<< Finished to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback);
                    }
                    if (IS_USER_BUILD) {
                        msglogging = me.mMsgMonitorLogging;
                        if (msglogging != null) {
                            msglogging.println("<<<<< Finished to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback);
                        }
                        if (MessageMonitorLogger.monitorMsg.containsKey(msg)) {
                            monitorMsg = (MonitorMSGInfo) MessageMonitorLogger.monitorMsg.get(msg);
                            if (MessageMonitorLogger.mMsgLoggerHandler.hasMessages(MessageMonitorLogger.START_MONITOR_EXECUTION_TIMEOUT_MSG, monitorMsg)) {
                                Log.d(TAG, "RemoveMessages EXECUTION_TIMEOUT msg=" + msg);
                                MessageMonitorLogger.mMsgLoggerHandler.removeMessages(MessageMonitorLogger.START_MONITOR_EXECUTION_TIMEOUT_MSG, monitorMsg);
                                MessageMonitorLogger.monitorMsg.remove(msg);
                            }
                        }
                    }
                    newIdent = Binder.clearCallingIdentity();
                    if (ident != newIdent) {
                        Log.wtf(TAG, "Thread identity changed from 0x" + Long.toHexString(ident) + " to 0x" + Long.toHexString(newIdent) + " while dispatching to " + msg.target.getClass().getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + " what=" + msg.what);
                    }
                    msg.recycleUnchecked();
                } finally {
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
        this.mThread = Thread.currentThread();
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
        this.mQueue.dump(pw, prefix + "  ");
    }

    public String toString() {
        return "Looper (" + this.mThread.getName() + ", tid " + this.mThread.getId() + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }

    public void setMonitorMessageLogging(Printer printer) {
        this.mMsgMonitorLogging = printer;
    }
}

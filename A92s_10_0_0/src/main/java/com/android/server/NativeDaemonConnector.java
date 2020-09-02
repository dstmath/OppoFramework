package com.android.server;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.LocalLog;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.Watchdog;
import com.android.server.power.ShutdownThread;
import com.google.android.collect.Lists;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class NativeDaemonConnector implements Runnable, Handler.Callback, Watchdog.Monitor {
    private static final long DEFAULT_TIMEOUT = 60000;
    private static final boolean VDBG = false;
    private static final long WARN_EXECUTE_DELAY_MS = 500;
    private final int BUFFER_SIZE;
    private final String TAG;
    private Handler mCallbackHandler;
    private INativeDaemonConnectorCallbacks mCallbacks;
    private final Object mDaemonLock;
    private volatile boolean mDebug;
    private LocalLog mLocalLog;
    private final Looper mLooper;
    private OutputStream mOutputStream;
    private final ResponseQueue mResponseQueue;
    private AtomicInteger mSequenceNumber;
    private String mSocket;
    private final PowerManager.WakeLock mWakeLock;
    private volatile Object mWarnIfHeld;

    NativeDaemonConnector(INativeDaemonConnectorCallbacks callbacks, String socket, int responseQueueSize, String logTag, int maxLogSize, PowerManager.WakeLock wl) {
        this(callbacks, socket, responseQueueSize, logTag, maxLogSize, wl, FgThread.get().getLooper());
    }

    NativeDaemonConnector(INativeDaemonConnectorCallbacks callbacks, String socket, int responseQueueSize, String logTag, int maxLogSize, PowerManager.WakeLock wl, Looper looper) {
        this.mDebug = false;
        this.mDaemonLock = new Object();
        this.BUFFER_SIZE = 4096;
        this.mCallbacks = callbacks;
        this.mSocket = socket;
        this.mResponseQueue = new ResponseQueue(responseQueueSize);
        this.mWakeLock = wl;
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            wakeLock.setReferenceCounted(true);
        }
        this.mLooper = looper;
        this.mSequenceNumber = new AtomicInteger(0);
        this.TAG = logTag != null ? logTag : "NativeDaemonConnector";
        this.mLocalLog = new LocalLog(maxLogSize);
    }

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }

    private int uptimeMillisInt() {
        return ((int) SystemClock.uptimeMillis()) & Integer.MAX_VALUE;
    }

    public void setWarnIfHeld(Object warnIfHeld) {
        Preconditions.checkState(this.mWarnIfHeld == null);
        this.mWarnIfHeld = Preconditions.checkNotNull(warnIfHeld);
    }

    public void run() {
        this.mCallbackHandler = new Handler(this.mLooper, this);
        while (!isShuttingDown()) {
            try {
                listenToSocket();
            } catch (Exception e) {
                loge("Error in NativeDaemonConnector: " + e);
                if (!isShuttingDown()) {
                    SystemClock.sleep(5000);
                } else {
                    return;
                }
            }
        }
    }

    private static boolean isShuttingDown() {
        String shutdownAct = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, "");
        return shutdownAct != null && shutdownAct.length() > 0;
    }

    public boolean handleMessage(Message msg) {
        Object[] objArr;
        PowerManager.WakeLock wakeLock;
        PowerManager.WakeLock wakeLock2;
        PowerManager.WakeLock wakeLock3;
        String event = (String) msg.obj;
        int start = uptimeMillisInt();
        int sent = msg.arg1;
        try {
            if (!this.mCallbacks.onEvent(msg.what, event, NativeDaemonEvent.unescapeArgs(event))) {
                log(String.format("Unhandled event '%s'", event));
            }
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && (wakeLock3 = this.mWakeLock) != null) {
                wakeLock3.release();
            }
            int end = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", event, Integer.valueOf(start - sent)));
            }
            if (end > start && ((long) (end - start)) > 500) {
                objArr = new Object[]{event, Integer.valueOf(end - start)};
                loge(String.format("NDC event {%s} took too long: %dms", objArr));
            }
        } catch (Exception e) {
            loge("Error handling '" + event + "': " + e);
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && (wakeLock = this.mWakeLock) != null) {
                wakeLock.release();
            }
            int end2 = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", event, Integer.valueOf(start - sent)));
            }
            if (end2 > start && ((long) (end2 - start)) > 500) {
                objArr = new Object[]{event, Integer.valueOf(end2 - start)};
            }
        } catch (Throwable th) {
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && (wakeLock2 = this.mWakeLock) != null) {
                wakeLock2.release();
            }
            int end3 = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", event, Integer.valueOf(start - sent)));
            }
            if (end3 > start && ((long) (end3 - start)) > 500) {
                loge(String.format("NDC event {%s} took too long: %dms", event, Integer.valueOf(end3 - start)));
            }
            throw th;
        }
        return true;
    }

    private LocalSocketAddress determineSocketAddress() {
        if (!this.mSocket.startsWith("__test__") || !Build.IS_DEBUGGABLE) {
            return new LocalSocketAddress(this.mSocket, LocalSocketAddress.Namespace.RESERVED);
        }
        return new LocalSocketAddress(this.mSocket);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01aa, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x01d4 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x015e  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0168  */
    private void listenToSocket() throws IOException {
        LocalSocket socket;
        Throwable th;
        int count;
        boolean z;
        char c;
        int i;
        PowerManager.WakeLock wakeLock;
        LocalSocket socket2 = null;
        OutputStream outputStream = null;
        try {
            socket2 = new LocalSocket();
            try {
                socket2.connect(determineSocketAddress());
                InputStream inputStream = socket2.getInputStream();
                synchronized (this.mDaemonLock) {
                    this.mOutputStream = socket2.getOutputStream();
                }
                this.mCallbacks.onDaemonConnected();
                byte[] buffer = new byte[4096];
                int start = 0;
                while (true) {
                    count = inputStream.read(buffer, start, 4096 - start);
                    if (count < 0) {
                        break;
                    }
                    FileDescriptor[] fdList = socket2.getAncillaryFileDescriptors();
                    int count2 = count + start;
                    int i2 = 0;
                    int start2 = 0;
                    while (i2 < count2) {
                        if (buffer[i2] == 0) {
                            boolean releaseWl = false;
                            try {
                                NativeDaemonEvent event = NativeDaemonEvent.parseRawEvent(new String(buffer, start2, i2 - start2, StandardCharsets.UTF_8), fdList);
                                log("RCV <- {" + event + "}");
                                if (event.isClassUnsolicited()) {
                                    if (this.mCallbacks.onCheckHoldWakeLock(event.getCode())) {
                                        try {
                                            if (this.mWakeLock != null) {
                                                this.mWakeLock.acquire();
                                                releaseWl = true;
                                            }
                                        } catch (IllegalArgumentException e) {
                                            e = e;
                                            socket = socket2;
                                            try {
                                                log("Problem parsing message " + e);
                                                if (releaseWl) {
                                                }
                                                start2 = i2 + 1;
                                                i2++;
                                                socket2 = socket;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                if (releaseWl) {
                                                    this.mWakeLock.release();
                                                }
                                                throw th;
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            if (releaseWl) {
                                            }
                                            throw th;
                                        }
                                    }
                                    socket = socket2;
                                    try {
                                        if (this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(event.getCode(), uptimeMillisInt(), 0, event.getRawEvent()))) {
                                            releaseWl = false;
                                        }
                                    } catch (IllegalArgumentException e2) {
                                        e = e2;
                                        log("Problem parsing message " + e);
                                        if (releaseWl) {
                                            wakeLock = this.mWakeLock;
                                            wakeLock.release();
                                        }
                                        start2 = i2 + 1;
                                        i2++;
                                        socket2 = socket;
                                    }
                                } else {
                                    socket = socket2;
                                    this.mResponseQueue.add(event.getCmdNumber(), event);
                                }
                                if (releaseWl) {
                                    try {
                                        wakeLock = this.mWakeLock;
                                        wakeLock.release();
                                    } catch (IOException e3) {
                                        ex = e3;
                                        socket2 = socket;
                                        try {
                                            loge("Communications error: " + ex);
                                            throw ex;
                                        } catch (Throwable th4) {
                                            socket = socket2;
                                            th = th4;
                                            synchronized (this.mDaemonLock) {
                                            }
                                        }
                                    } catch (Throwable th5) {
                                        th = th5;
                                        synchronized (this.mDaemonLock) {
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException e4) {
                                e = e4;
                                socket = socket2;
                                log("Problem parsing message " + e);
                                if (releaseWl) {
                                }
                                start2 = i2 + 1;
                                i2++;
                                socket2 = socket;
                            } catch (Throwable th6) {
                                th = th6;
                                if (releaseWl) {
                                }
                                throw th;
                            }
                            start2 = i2 + 1;
                        } else {
                            socket = socket2;
                        }
                        i2++;
                        socket2 = socket;
                    }
                    if (start2 == 0) {
                        log("RCV incomplete");
                    }
                    if (start2 != count2) {
                        c = 4096;
                        int remaining = 4096 - start2;
                        z = false;
                        System.arraycopy(buffer, start2, buffer, 0, remaining);
                        i = remaining;
                    } else {
                        c = 4096;
                        z = false;
                        i = 0;
                    }
                    start = i;
                    socket2 = socket2;
                    outputStream = null;
                }
                loge("got " + count + " reading with start = " + start);
                synchronized (this.mDaemonLock) {
                    if (this.mOutputStream != null) {
                        try {
                            loge("closing stream for " + this.mSocket);
                            this.mOutputStream.close();
                        } catch (IOException e5) {
                            loge("Failed closing output stream: " + e5);
                        }
                        this.mOutputStream = outputStream;
                    }
                }
                try {
                    socket2.close();
                    return;
                } catch (IOException ex) {
                    loge("Failed closing socket: " + ex);
                    return;
                }
                while (true) {
                }
            } catch (IOException e6) {
                ex = e6;
                loge("Communications error: " + ex);
                throw ex;
            } catch (Throwable th7) {
                socket = socket2;
                th = th7;
                synchronized (this.mDaemonLock) {
                    if (this.mOutputStream != null) {
                        try {
                            loge("closing stream for " + this.mSocket);
                            this.mOutputStream.close();
                        } catch (IOException e7) {
                            loge("Failed closing output stream: " + e7);
                        }
                        this.mOutputStream = null;
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex2) {
                        loge("Failed closing socket: " + ex2);
                    }
                }
                throw th;
            }
        } catch (IOException e8) {
            ex = e8;
            loge("Communications error: " + ex);
            throw ex;
        }
    }

    public static class SensitiveArg {
        private final Object mArg;

        public SensitiveArg(Object arg) {
            this.mArg = arg;
        }

        public String toString() {
            return String.valueOf(this.mArg);
        }
    }

    @VisibleForTesting
    static void makeCommand(StringBuilder rawBuilder, StringBuilder logBuilder, int sequenceNumber, String cmd, Object... args) {
        if (cmd.indexOf(0) >= 0) {
            throw new IllegalArgumentException("Unexpected command: " + cmd);
        } else if (cmd.indexOf(32) < 0) {
            rawBuilder.append(sequenceNumber);
            rawBuilder.append(' ');
            rawBuilder.append(cmd);
            logBuilder.append(sequenceNumber);
            logBuilder.append(' ');
            logBuilder.append(cmd);
            int length = args.length;
            int i = 0;
            while (i < length) {
                Object arg = args[i];
                String argString = String.valueOf(arg);
                if (argString.indexOf(0) < 0) {
                    rawBuilder.append(' ');
                    logBuilder.append(' ');
                    appendEscaped(rawBuilder, argString);
                    if (arg instanceof SensitiveArg) {
                        logBuilder.append("[scrubbed]");
                    } else {
                        appendEscaped(logBuilder, argString);
                    }
                    i++;
                } else {
                    throw new IllegalArgumentException("Unexpected argument: " + arg);
                }
            }
            rawBuilder.append(0);
        } else {
            throw new IllegalArgumentException("Arguments must be separate from command");
        }
    }

    public void waitForCallbacks() {
        if (Thread.currentThread() != this.mLooper.getThread()) {
            final CountDownLatch latch = new CountDownLatch(1);
            this.mCallbackHandler.post(new Runnable() {
                /* class com.android.server.NativeDaemonConnector.AnonymousClass1 */

                public void run() {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Slog.wtf(this.TAG, "Interrupted while waiting for unsolicited response handling", e);
            }
        } else {
            throw new IllegalStateException("Must not call this method on callback thread");
        }
    }

    public NativeDaemonEvent execute(Command cmd) throws NativeDaemonConnectorException {
        return execute(cmd.mCmd, cmd.mArguments.toArray());
    }

    public NativeDaemonEvent execute(String cmd, Object... args) throws NativeDaemonConnectorException {
        return execute(60000, cmd, args);
    }

    public NativeDaemonEvent execute(long timeoutMs, String cmd, Object... args) throws NativeDaemonConnectorException {
        NativeDaemonEvent[] events = executeForList(timeoutMs, cmd, args);
        if (events.length == 1) {
            return events[0];
        }
        throw new NativeDaemonConnectorException("Expected exactly one response, but received " + events.length);
    }

    public NativeDaemonEvent[] executeForList(Command cmd) throws NativeDaemonConnectorException {
        return executeForList(cmd.mCmd, cmd.mArguments.toArray());
    }

    public NativeDaemonEvent[] executeForList(String cmd, Object... args) throws NativeDaemonConnectorException {
        return executeForList(60000, cmd, args);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x009b, code lost:
        r0 = r21.mResponseQueue.remove(r7, r22, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x00a3, code lost:
        if (r0 == null) goto L_0x0112;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00a5, code lost:
        r4.add(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00ac, code lost:
        if (r0.isClassContinue() != false) goto L_0x010d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00ae, code lost:
        r15 = android.os.SystemClock.elapsedRealtime();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00b8, code lost:
        if ((r15 - r2) <= 500) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00ba, code lost:
        loge("NDC Command {" + r11 + "} took too long (" + (r15 - r2) + "ms)");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00ec, code lost:
        if (r0.isClassClientError() != false) goto L_0x0107;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00f2, code lost:
        if (r0.isClassServerError() != false) goto L_0x0101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0100, code lost:
        return (com.android.server.NativeDaemonEvent[]) r4.toArray(new com.android.server.NativeDaemonEvent[r4.size()]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0106, code lost:
        throw new com.android.server.NativeDaemonConnector.NativeDaemonFailureException(r11, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x010c, code lost:
        throw new com.android.server.NativeDaemonConnector.NativeDaemonArgumentException(r11, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0112, code lost:
        loge("timed-out waiting for response to " + r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0130, code lost:
        throw new com.android.server.NativeDaemonTimeoutException(r11, r0);
     */
    public NativeDaemonEvent[] executeForList(long timeoutMs, String cmd, Object... args) throws NativeDaemonConnectorException {
        if (this.mWarnIfHeld != null && Thread.holdsLock(this.mWarnIfHeld)) {
            String str = this.TAG;
            Slog.wtf(str, "Calling thread " + Thread.currentThread().getName() + " is holding 0x" + Integer.toHexString(System.identityHashCode(this.mWarnIfHeld)), new Throwable());
        }
        long startTime = SystemClock.elapsedRealtime();
        ArrayList<NativeDaemonEvent> events = Lists.newArrayList();
        StringBuilder rawBuilder = new StringBuilder();
        StringBuilder logBuilder = new StringBuilder();
        int sequenceNumber = this.mSequenceNumber.incrementAndGet();
        makeCommand(rawBuilder, logBuilder, sequenceNumber, cmd, args);
        String rawCmd = rawBuilder.toString();
        String logCmd = logBuilder.toString();
        log("SND -> {" + logCmd + "}");
        synchronized (this.mDaemonLock) {
            try {
                if (this.mOutputStream != null) {
                    try {
                        this.mOutputStream.write(rawCmd.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new NativeDaemonConnectorException("problem sending command", e);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    throw new NativeDaemonConnectorException("missing output stream");
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    @VisibleForTesting
    static void appendEscaped(StringBuilder builder, String arg) {
        boolean hasSpaces = arg.indexOf(32) >= 0;
        if (hasSpaces) {
            builder.append('\"');
        }
        int length = arg.length();
        for (int i = 0; i < length; i++) {
            char c = arg.charAt(i);
            if (c == '\"') {
                builder.append("\\\"");
            } else if (c == '\\') {
                builder.append("\\\\");
            } else {
                builder.append(c);
            }
        }
        if (hasSpaces) {
            builder.append('\"');
        }
    }

    private static class NativeDaemonArgumentException extends NativeDaemonConnectorException {
        public NativeDaemonArgumentException(String command, NativeDaemonEvent event) {
            super(command, event);
        }

        @Override // com.android.server.NativeDaemonConnectorException
        public IllegalArgumentException rethrowAsParcelableException() {
            throw new IllegalArgumentException(getMessage(), this);
        }
    }

    private static class NativeDaemonFailureException extends NativeDaemonConnectorException {
        public NativeDaemonFailureException(String command, NativeDaemonEvent event) {
            super(command, event);
        }
    }

    public static class Command {
        /* access modifiers changed from: private */
        public ArrayList<Object> mArguments = Lists.newArrayList();
        /* access modifiers changed from: private */
        public String mCmd;

        public Command(String cmd, Object... args) {
            this.mCmd = cmd;
            for (Object arg : args) {
                appendArg(arg);
            }
        }

        public Command appendArg(Object arg) {
            this.mArguments.add(arg);
            return this;
        }
    }

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
        synchronized (this.mDaemonLock) {
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mLocalLog.dump(fd, pw, args);
        pw.println();
        this.mResponseQueue.dump(fd, pw, args);
    }

    private void log(String logstring) {
        if (this.mDebug) {
            Slog.d(this.TAG, logstring);
        }
        this.mLocalLog.log(logstring);
    }

    private void loge(String logstring) {
        Slog.e(this.TAG, logstring);
        this.mLocalLog.log(logstring);
    }

    private static class ResponseQueue {
        private int mMaxCount;
        private final LinkedList<PendingCmd> mPendingCmds = new LinkedList<>();

        private static class PendingCmd {
            public int availableResponseCount;
            public final int cmdNum;
            public final String logCmd;
            public BlockingQueue<NativeDaemonEvent> responses = new ArrayBlockingQueue(10);

            public PendingCmd(int cmdNum2, String logCmd2) {
                this.cmdNum = cmdNum2;
                this.logCmd = logCmd2;
            }
        }

        ResponseQueue(int maxCount) {
            this.mMaxCount = maxCount;
        }

        public void add(int cmdNum, NativeDaemonEvent response) {
            PendingCmd found = null;
            synchronized (this.mPendingCmds) {
                Iterator<PendingCmd> it = this.mPendingCmds.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PendingCmd pendingCmd = it.next();
                    if (pendingCmd.cmdNum == cmdNum) {
                        found = pendingCmd;
                        break;
                    }
                }
                if (found == null) {
                    while (this.mPendingCmds.size() >= this.mMaxCount) {
                        Slog.e("NativeDaemonConnector.ResponseQueue", "more buffered than allowed: " + this.mPendingCmds.size() + " >= " + this.mMaxCount);
                        PendingCmd pendingCmd2 = this.mPendingCmds.remove();
                        Slog.e("NativeDaemonConnector.ResponseQueue", "Removing request: " + pendingCmd2.logCmd + " (" + pendingCmd2.cmdNum + ")");
                    }
                    found = new PendingCmd(cmdNum, null);
                    this.mPendingCmds.add(found);
                }
                found.availableResponseCount++;
                if (found.availableResponseCount == 0) {
                    this.mPendingCmds.remove(found);
                }
            }
            try {
                found.responses.put(response);
            } catch (InterruptedException e) {
            }
        }

        public NativeDaemonEvent remove(int cmdNum, long timeoutMs, String logCmd) {
            PendingCmd found = null;
            synchronized (this.mPendingCmds) {
                Iterator<PendingCmd> it = this.mPendingCmds.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PendingCmd pendingCmd = it.next();
                    if (pendingCmd.cmdNum == cmdNum) {
                        found = pendingCmd;
                        break;
                    }
                }
                if (found == null) {
                    found = new PendingCmd(cmdNum, logCmd);
                    this.mPendingCmds.add(found);
                }
                found.availableResponseCount--;
                if (found.availableResponseCount == 0) {
                    this.mPendingCmds.remove(found);
                }
            }
            NativeDaemonEvent result = null;
            try {
                result = found.responses.poll(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }
            if (result == null) {
                Slog.e("NativeDaemonConnector.ResponseQueue", "Timeout waiting for response");
            }
            return result;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("Pending requests:");
            synchronized (this.mPendingCmds) {
                Iterator<PendingCmd> it = this.mPendingCmds.iterator();
                while (it.hasNext()) {
                    PendingCmd pendingCmd = it.next();
                    pw.println("  Cmd " + pendingCmd.cmdNum + " - " + pendingCmd.logCmd);
                }
            }
        }
    }
}

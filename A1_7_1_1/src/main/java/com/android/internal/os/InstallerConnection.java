package com.android.internal.os;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import libcore.io.IoUtils;
import libcore.io.Streams;

public class InstallerConnection {
    private static final boolean LOCAL_DEBUG = false;
    private static final String TAG = "InstallerConnection";
    private final byte[] buf = new byte[1024];
    private InputStream mIn;
    private OutputStream mOut;
    private LocalSocket mSocket;
    private volatile Object mWarnIfHeld;

    public static class InstallerException extends Exception {
        public InstallerException(String detailMessage) {
            super(detailMessage);
        }
    }

    public void setWarnIfHeld(Object warnIfHeld) {
        Preconditions.checkState(this.mWarnIfHeld == null);
        this.mWarnIfHeld = Preconditions.checkNotNull(warnIfHeld);
    }

    public synchronized String transact(String cmd) {
        if (this.mWarnIfHeld != null && Thread.holdsLock(this.mWarnIfHeld)) {
            Slog.wtf(TAG, "Calling thread " + Thread.currentThread().getName() + " is holding 0x" + Integer.toHexString(System.identityHashCode(this.mWarnIfHeld)), new Throwable());
        }
        if (connect()) {
            if (!writeCommand(cmd)) {
                Slog.e(TAG, "write command failed? reconnect!");
                if (!(connect() && writeCommand(cmd))) {
                    return CallerInfo.UNKNOWN_NUMBER;
                }
            }
            int replyLength = readReply();
            if (replyLength > 0) {
                return new String(this.buf, 0, replyLength);
            }
            return CallerInfo.UNKNOWN_NUMBER;
        }
        Slog.e(TAG, "connection failed");
        return CallerInfo.UNKNOWN_NUMBER;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x00b8 A:{ExcHandler: java.lang.ArrayIndexOutOfBoundsException (e java.lang.ArrayIndexOutOfBoundsException), Splitter: B:19:0x007b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String[] execute(String cmd, Object... args) throws InstallerException {
        StringBuilder builder = new StringBuilder(cmd);
        int length = args.length;
        int i = 0;
        while (i < length) {
            String escaped;
            Object arg = args[i];
            if (arg == null) {
                escaped = PhoneConstants.MVNO_TYPE_NONE;
            } else {
                escaped = String.valueOf(arg);
            }
            if (escaped.indexOf(0) == -1 && escaped.indexOf(32) == -1 && !"!".equals(escaped)) {
                if (TextUtils.isEmpty(escaped)) {
                    escaped = "!";
                }
                builder.append(' ').append(escaped);
                i++;
            } else {
                throw new InstallerException("Invalid argument while executing " + cmd + " " + Arrays.toString(args));
            }
        }
        String[] resRaw = transact(builder.toString()).split(" ");
        int res = -1;
        try {
            res = Integer.parseInt(resRaw[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        if (res == 0) {
            return resRaw;
        }
        throw new InstallerException("Failed to execute " + cmd + " " + Arrays.toString(args) + ": " + res);
    }

    public void dexopt(String apkPath, int uid, String instructionSet, int dexoptNeeded, int dexFlags, String compilerFilter, String volumeUuid, String sharedLibraries) throws InstallerException {
        dexopt(apkPath, uid, PhoneConstants.APN_TYPE_ALL, instructionSet, dexoptNeeded, null, dexFlags, compilerFilter, volumeUuid, sharedLibraries);
    }

    public void dexopt(String apkPath, int uid, String pkgName, String instructionSet, int dexoptNeeded, String outputPath, int dexFlags, String compilerFilter, String volumeUuid, String sharedLibraries) throws InstallerException {
        execute("dexopt", apkPath, Integer.valueOf(uid), pkgName, instructionSet, Integer.valueOf(dexoptNeeded), outputPath, Integer.valueOf(dexFlags), compilerFilter, volumeUuid, sharedLibraries);
    }

    private boolean safeParseBooleanResult(String[] res) throws InstallerException {
        if (res == null || res.length != 2) {
            throw new InstallerException("Invalid size result: " + Arrays.toString(res));
        } else if (res[1].equals("true") || res[1].equals("false")) {
            return Boolean.parseBoolean(res[1]);
        } else {
            throw new InstallerException("Invalid boolean result: " + Arrays.toString(res));
        }
    }

    public boolean mergeProfiles(int uid, String pkgName) throws InstallerException {
        return safeParseBooleanResult(execute("merge_profiles", Integer.valueOf(uid), pkgName));
    }

    public boolean dumpProfiles(String gid, String packageName, String codePaths) throws InstallerException {
        return safeParseBooleanResult(execute("dump_profiles", gid, packageName, codePaths));
    }

    private boolean connect() {
        if (this.mSocket != null) {
            return true;
        }
        Slog.i(TAG, "connecting...");
        try {
            this.mSocket = new LocalSocket();
            this.mSocket.connect(new LocalSocketAddress("installd", Namespace.RESERVED));
            this.mIn = this.mSocket.getInputStream();
            this.mOut = this.mSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            disconnect();
            return false;
        }
    }

    public void disconnect() {
        Slog.i(TAG, "disconnecting...");
        IoUtils.closeQuietly(this.mSocket);
        IoUtils.closeQuietly(this.mIn);
        IoUtils.closeQuietly(this.mOut);
        this.mSocket = null;
        this.mIn = null;
        this.mOut = null;
    }

    private boolean readFully(byte[] buffer, int len) {
        try {
            Streams.readFully(this.mIn, buffer, 0, len);
            return true;
        } catch (IOException e) {
            Slog.e(TAG, "read exception");
            disconnect();
            return false;
        }
    }

    private int readReply() {
        if (!readFully(this.buf, 2)) {
            return -1;
        }
        int len = (this.buf[0] & 255) | ((this.buf[1] & 255) << 8);
        if (len < 1 || len > this.buf.length) {
            Slog.e(TAG, "invalid reply length (" + len + ")");
            disconnect();
            return -1;
        } else if (readFully(this.buf, len)) {
            return len;
        } else {
            return -1;
        }
    }

    private boolean writeCommand(String cmdString) {
        byte[] cmd = cmdString.getBytes();
        int len = cmd.length;
        if (len < 1 || len > this.buf.length) {
            return false;
        }
        this.buf[0] = (byte) (len & 255);
        this.buf[1] = (byte) ((len >> 8) & 255);
        try {
            this.mOut.write(this.buf, 0, 2);
            this.mOut.write(cmd, 0, len);
            return true;
        } catch (IOException e) {
            Slog.e(TAG, "write error");
            disconnect();
            return false;
        }
    }

    public void waitForConnection() {
        while (true) {
            try {
                execute("ping", new Object[0]);
                break;
            } catch (InstallerException e) {
                Slog.w(TAG, "installd not ready");
                SystemClock.sleep(1000);
            }
        }
    }
}

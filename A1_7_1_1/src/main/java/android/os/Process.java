package android.os;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.StrictMode.ThreadPolicy;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.Slog;
import dalvik.system.VMRuntime;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class Process {
    public static final int AUDIOSERVER_UID = 1041;
    public static final int BLUETOOTH_UID = 1002;
    public static final int CAMERASERVER_UID = 1047;
    private static boolean DEBUG_ZYGOTE_ON_DEMAND = false;
    public static final int DRM_UID = 1019;
    public static final int FIRST_APPLICATION_CACHE_GID = 20000;
    public static final int FIRST_APPLICATION_UID = 10000;
    public static final int FIRST_ISOLATED_UID = 99000;
    public static final int FIRST_SHARED_APPLICATION_GID = 50000;
    public static final int LAST_APPLICATION_CACHE_GID = 29999;
    public static final int LAST_APPLICATION_UID = 19999;
    public static final int LAST_ISOLATED_UID = 99999;
    public static final int LAST_SHARED_APPLICATION_GID = 59999;
    private static final String LOG_TAG = "Process";
    public static final int LOG_UID = 1007;
    public static final int MEDIA_RW_GID = 1023;
    public static final int MEDIA_UID = 1013;
    public static final int NFC_UID = 1027;
    public static final int PACKAGE_INFO_GID = 1032;
    public static final int PHONE_UID = 1001;
    public static final int PROC_CHAR = 2048;
    public static final int PROC_COMBINE = 256;
    public static final int PROC_OUT_FLOAT = 16384;
    public static final int PROC_OUT_LONG = 8192;
    public static final int PROC_OUT_STRING = 4096;
    public static final int PROC_PARENS = 512;
    public static final int PROC_QUOTES = 1024;
    public static final int PROC_SPACE_TERM = 32;
    public static final int PROC_TAB_TERM = 9;
    public static final int PROC_TERM_MASK = 255;
    public static final int PROC_ZERO_TERM = 0;
    private static final String PROP_ZYGOTE_ON_DEMAND_CONTROL = "sys.mtk_zygote_secondary";
    private static final String PROP_ZYGOTE_ON_DEMAND_DEBUG = "persist.sys.mtk_zygote_debug";
    private static final String PROP_ZYGOTE_ON_DEMAND_ENABLE = "ro.mtk_gmo_zygote_on_demand";
    public static final int ROOT_UID = 0;
    public static final int SCHED_BATCH = 3;
    public static final int SCHED_FIFO = 1;
    public static final int SCHED_IDLE = 5;
    public static final int SCHED_OTHER = 0;
    public static final int SCHED_RESET_ON_FORK = 1073741824;
    public static final int SCHED_RR = 2;
    public static final String SECONDARY_ZYGOTE_SOCKET = "zygote_secondary";
    public static final int SHARED_RELRO_UID = 1037;
    public static final int SHARED_USER_GID = 9997;
    public static final int SHELL_UID = 2000;
    public static final int SIGNAL_KILL = 9;
    public static final int SIGNAL_QUIT = 3;
    public static final int SIGNAL_USR1 = 10;
    public static final int SYSTEM_UID = 1000;
    public static final int THREAD_GROUP_AUDIO_APP = 3;
    public static final int THREAD_GROUP_AUDIO_SYS = 4;
    public static final int THREAD_GROUP_BG_NONINTERACTIVE = 0;
    public static final int THREAD_GROUP_DEFAULT = -1;
    private static final int THREAD_GROUP_FOREGROUND = 1;
    public static final int THREAD_GROUP_SYSTEM = 2;
    public static final int THREAD_GROUP_TOP_APP = 5;
    public static final int THREAD_PRIORITY_AUDIO = -16;
    public static final int THREAD_PRIORITY_BACKGROUND = 10;
    public static final int THREAD_PRIORITY_DEFAULT = 0;
    public static final int THREAD_PRIORITY_DISPLAY = -4;
    public static final int THREAD_PRIORITY_FOREGROUND = -2;
    public static final int THREAD_PRIORITY_LESS_FAVORABLE = 1;
    public static final int THREAD_PRIORITY_LOWEST = 19;
    public static final int THREAD_PRIORITY_MORE_FAVORABLE = -1;
    public static final int THREAD_PRIORITY_URGENT_AUDIO = -19;
    public static final int THREAD_PRIORITY_URGENT_DISPLAY = -8;
    public static final int VPN_UID = 1016;
    public static final int WIFI_UID = 1010;
    static final int ZYGOTE_RETRY_MILLIS = 500;
    public static final String ZYGOTE_SOCKET = "zygote";
    static ZygoteState primaryZygoteState;
    private static long sStartElapsedRealtime;
    private static long sStartUptimeMillis;
    private static final boolean sZygoteOnDemandEnabled = false;
    static ZygoteState secondaryZygoteState;

    public static final class ProcessStartResult {
        public int pid;
        public boolean usingWrapper;
    }

    public static class ZygoteState {
        final List<String> abiList;
        final DataInputStream inputStream;
        boolean mClosed;
        final LocalSocket socket;
        final BufferedWriter writer;

        private ZygoteState(LocalSocket socket, DataInputStream inputStream, BufferedWriter writer, List<String> abiList) {
            this.socket = socket;
            this.inputStream = inputStream;
            this.writer = writer;
            this.abiList = abiList;
        }

        public static ZygoteState connect(String socketAddress) throws IOException {
            IOException ex;
            LocalSocket zygoteSocket = new LocalSocket();
            try {
                zygoteSocket.connect(new LocalSocketAddress(socketAddress, Namespace.RESERVED));
                DataInputStream zygoteInputStream = new DataInputStream(zygoteSocket.getInputStream());
                try {
                    BufferedWriter zygoteWriter = new BufferedWriter(new OutputStreamWriter(zygoteSocket.getOutputStream()), 256);
                    String abiListString = Process.getAbiList(zygoteWriter, zygoteInputStream);
                    Log.i("Zygote", "Process: zygote socket opened, supported ABIS: " + abiListString);
                    return new ZygoteState(zygoteSocket, zygoteInputStream, zygoteWriter, Arrays.asList(abiListString.split(",")));
                } catch (IOException e) {
                    ex = e;
                    try {
                        zygoteSocket.close();
                    } catch (IOException e2) {
                    }
                    throw ex;
                }
            } catch (IOException e3) {
                ex = e3;
                zygoteSocket.close();
                throw ex;
            }
        }

        boolean matches(String abi) {
            return this.abiList.contains(abi);
        }

        public void close() {
            try {
                this.socket.close();
            } catch (IOException ex) {
                Log.e(Process.LOG_TAG, "I/O exception on routine close", ex);
            }
            this.mClosed = true;
        }

        boolean isClosed() {
            return this.mClosed;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.os.Process.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.os.Process.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Process.<clinit>():void");
    }

    public static final native long getElapsedCpuTime();

    public static final native int[] getExclusiveCores();

    public static final native long getFreeMemory();

    public static final native int getGidForName(String str);

    public static final native long getLruAnonMemory();

    public static final native int[] getPids(String str, int[] iArr);

    public static final native int[] getPidsForCommands(String[] strArr);

    public static final native String getProcessCmdlineByPid(int i);

    public static final native int getProcessGroup(int i) throws IllegalArgumentException, SecurityException;

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for cts testSELinuxPolicyFile", property = OppoRomType.ROM)
    public static final native String getProcessNameByPid(int i);

    public static final native int getProcessState(int i);

    public static final native long getPss(int i);

    public static final native int getThreadPriority(int i) throws IllegalArgumentException;

    public static final native int getThreadScheduler(int i) throws IllegalArgumentException;

    public static final native long getTotalMemory();

    public static final native int getUidForName(String str);

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoRomType.ROM)
    public static final native boolean isProcessSuspend(int i);

    public static final native int killProcessGroup(int i, int i2);

    public static final native boolean parseProcLine(byte[] bArr, int i, int i2, int[] iArr, String[] strArr, long[] jArr, float[] fArr);

    public static final native boolean readProcFile(String str, int[] iArr, String[] strArr, long[] jArr, float[] fArr);

    public static final native void readProcLines(String str, String[] strArr, long[] jArr);

    public static final native void removeAllProcessGroups();

    public static final native void sendSignal(int i, int i2);

    public static final native void sendSignalQuiet(int i, int i2);

    public static final native void setArgV0(String str);

    public static final native void setCanSelfBackground(boolean z);

    public static final native int setGid(int i);

    public static final native void setProcessGroup(int i, int i2) throws IllegalArgumentException, SecurityException;

    public static final native boolean setSwappiness(int i, boolean z);

    public static final native void setThreadGroup(int i, int i2) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadPriority(int i) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadPriority(int i, int i2) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadScheduler(int i, int i2, int i3) throws IllegalArgumentException;

    public static final native int setUid(int i);

    public static final ProcessStartResult start(String processClass, String niceName, int uid, int gid, int[] gids, int debugFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String[] zygoteArgs) {
        try {
            return startViaZygote(processClass, niceName, uid, gid, gids, debugFlags, mountExternal, targetSdkVersion, seInfo, abi, instructionSet, appDataDir, zygoteArgs);
        } catch (ZygoteStartFailedEx ex) {
            Log.e(LOG_TAG, "Starting VM process through Zygote failed");
            throw new RuntimeException("Starting VM process through Zygote failed", ex);
        }
    }

    private static String getAbiList(BufferedWriter writer, DataInputStream inputStream) throws IOException {
        writer.write(WifiEnterpriseConfig.ENGINE_ENABLE);
        writer.newLine();
        writer.write("--query-abi-list");
        writer.newLine();
        writer.flush();
        byte[] bytes = new byte[inputStream.readInt()];
        inputStream.readFully(bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private static ProcessStartResult zygoteSendArgsAndGetResult(ZygoteState zygoteState, ArrayList<String> args) throws ZygoteStartFailedEx {
        try {
            int i;
            int sz = args.size();
            for (i = 0; i < sz; i++) {
                if (((String) args.get(i)).indexOf(10) >= 0) {
                    throw new ZygoteStartFailedEx("embedded newlines not allowed");
                }
            }
            BufferedWriter writer = zygoteState.writer;
            DataInputStream inputStream = zygoteState.inputStream;
            writer.write(Integer.toString(args.size()));
            writer.newLine();
            for (i = 0; i < sz; i++) {
                writer.write((String) args.get(i));
                writer.newLine();
            }
            writer.flush();
            ProcessStartResult result = new ProcessStartResult();
            long startTime = SystemClock.elapsedRealtime();
            Slog.w(LOG_TAG, "zygoteSendArgsAndGetResult: start read result...");
            result.pid = inputStream.readInt();
            result.usingWrapper = inputStream.readBoolean();
            Slog.w(LOG_TAG, (SystemClock.uptimeMillis() - startTime) + "ms so far, end read result");
            if (result.pid >= 0) {
                return result;
            }
            throw new ZygoteStartFailedEx("fork() failed");
        } catch (Throwable ex) {
            zygoteState.close();
            throw new ZygoteStartFailedEx(ex);
        }
    }

    private static ProcessStartResult startViaZygote(String processClass, String niceName, int uid, int gid, int[] gids, int debugFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String[] extraArgs) throws ZygoteStartFailedEx {
        ArrayList<String> argsForZygote;
        synchronized (Process.class) {
            argsForZygote = new ArrayList();
            argsForZygote.add("--runtime-args");
            argsForZygote.add("--setuid=" + uid);
            argsForZygote.add("--setgid=" + gid);
            if ((debugFlags & 16) != 0) {
                argsForZygote.add("--enable-jni-logging");
            }
            if ((debugFlags & 8) != 0) {
                argsForZygote.add("--enable-safemode");
            }
            if ((debugFlags & 1) != 0) {
                argsForZygote.add("--enable-debugger");
            }
            if ((debugFlags & 2) != 0) {
                argsForZygote.add("--enable-checkjni");
            }
            if ((debugFlags & 32) != 0) {
                argsForZygote.add("--generate-debug-info");
            }
            if ((debugFlags & 64) != 0) {
                argsForZygote.add("--always-jit");
            }
            if ((debugFlags & 128) != 0) {
                argsForZygote.add("--native-debuggable");
            }
            if ((debugFlags & 4) != 0) {
                argsForZygote.add("--enable-assert");
            }
            if (mountExternal == 1) {
                argsForZygote.add("--mount-external-default");
            } else if (mountExternal == 2) {
                argsForZygote.add("--mount-external-read");
            } else if (mountExternal == 3) {
                argsForZygote.add("--mount-external-write");
            }
            argsForZygote.add("--target-sdk-version=" + targetSdkVersion);
            if (gids != null && gids.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("--setgroups=");
                int sz = gids.length;
                for (int i = 0; i < sz; i++) {
                    if (i != 0) {
                        sb.append(',');
                    }
                    sb.append(gids[i]);
                }
                argsForZygote.add(sb.toString());
            }
            if (niceName != null) {
                argsForZygote.add("--nice-name=" + niceName);
            }
            if (seInfo != null) {
                argsForZygote.add("--seinfo=" + seInfo);
            }
            if (instructionSet != null) {
                argsForZygote.add("--instruction-set=" + instructionSet);
            }
            if (appDataDir != null) {
                argsForZygote.add("--app-data-dir=" + appDataDir);
            }
            argsForZygote.add(processClass);
            if (extraArgs != null) {
                for (String arg : extraArgs) {
                    argsForZygote.add(arg);
                }
            }
        }
        return zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi), argsForZygote);
    }

    public static void debugZygoteOnDemand(boolean on) {
        DEBUG_ZYGOTE_ON_DEMAND = on;
        if (on) {
            SystemProperties.set(PROP_ZYGOTE_ON_DEMAND_DEBUG, WifiEnterpriseConfig.ENGINE_ENABLE);
        } else {
            SystemProperties.set(PROP_ZYGOTE_ON_DEMAND_DEBUG, WifiEnterpriseConfig.ENGINE_DISABLE);
        }
    }

    /* JADX WARNING: Missing block: B:26:0x0061, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void startSecondaryZygote(String abi) {
        if (sZygoteOnDemandEnabled) {
            if (DEBUG_ZYGOTE_ON_DEMAND) {
                Log.d(LOG_TAG, "ZygoteOnDemand: startSecondaryZygote for " + abi);
            }
            synchronized (Process.class) {
                if (waitSecondaryZygoteChangedLocked("stopped")) {
                    if (primaryZygoteState == null || primaryZygoteState.isClosed()) {
                        try {
                            primaryZygoteState = ZygoteState.connect(ZYGOTE_SOCKET);
                        } catch (IOException ioe) {
                            Log.d(LOG_TAG, "ZygoteOnDemand: Error connecting to primary zygote: " + ioe);
                        }
                    }
                    if (primaryZygoteState.matches(abi)) {
                        Log.d(LOG_TAG, "ZygoteOnDemand: startSecondaryZygote match primary Zygote");
                    } else {
                        SystemProperties.set(PROP_ZYGOTE_ON_DEMAND_CONTROL, WifiEnterpriseConfig.ENGINE_ENABLE);
                        if (!waitSecondaryZygoteChangedLocked("running")) {
                            Log.d(LOG_TAG, "ZygoteOnDemand: service is not running");
                            return;
                        } else if (DEBUG_ZYGOTE_ON_DEMAND) {
                            Log.d(LOG_TAG, "ZygoteOnDemand: startSecondaryZygote done");
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "ZygoteOnDemand: service is not stopped");
                    return;
                }
            }
        }
        return;
    }

    /* JADX WARNING: Missing block: B:25:0x0054, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void stopSecondaryZygote() {
        if (sZygoteOnDemandEnabled) {
            if (DEBUG_ZYGOTE_ON_DEMAND) {
                Log.d(LOG_TAG, "ZygoteOnDemand: stopSecondaryZygote");
            }
            synchronized (Process.class) {
                if (waitSecondaryZygoteChangedLocked("running")) {
                    SystemProperties.set(PROP_ZYGOTE_ON_DEMAND_CONTROL, WifiEnterpriseConfig.ENGINE_DISABLE);
                    if (!waitSecondaryZygoteChangedLocked("stopped")) {
                        Log.d(LOG_TAG, "ZygoteOnDemand: service is not stopped");
                    } else if (DEBUG_ZYGOTE_ON_DEMAND) {
                        Log.d(LOG_TAG, "ZygoteOnDemand: stopSecondaryZygote done");
                    }
                } else {
                    Log.d(LOG_TAG, "ZygoteOnDemand: service is not running");
                }
            }
        }
    }

    public static boolean isSecondaryZygoteRunning() {
        boolean isRunning = SystemProperties.get(PROP_ZYGOTE_ON_DEMAND_CONTROL, WifiEnterpriseConfig.ENGINE_ENABLE).equals(WifiEnterpriseConfig.ENGINE_ENABLE);
        if (DEBUG_ZYGOTE_ON_DEMAND) {
            Log.d(LOG_TAG, "ZygoteOnDemand: isSecondaryZygoteRunning: " + isRunning);
        }
        return isRunning;
    }

    public static ProcessStartResult waitForSecondaryZygote(String abi) {
        if (!sZygoteOnDemandEnabled) {
            return null;
        }
        if (DEBUG_ZYGOTE_ON_DEMAND) {
            Log.d(LOG_TAG, "ZygoteOnDemand: waitForSecondaryZygote for " + abi);
        }
        synchronized (Process.class) {
            ProcessStartResult result = null;
            if (waitSecondaryZygoteChangedLocked("running")) {
                ArrayList<String> argsForZygote = new ArrayList();
                argsForZygote.add("--try-secondary-zygote");
                int i = 0;
                while (i < 25) {
                    if (i != 0) {
                        try {
                            Thread.sleep(100);
                            if (DEBUG_ZYGOTE_ON_DEMAND) {
                                Log.d(LOG_TAG, "ZygoteOnDemand: waitForSecondaryZygote retry: " + i);
                            }
                        } catch (InterruptedException ie) {
                            Log.d(LOG_TAG, "ZygoteOnDemand: waitForSecondaryZygote: " + ie);
                            return result;
                        }
                    }
                    try {
                        result = zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi), argsForZygote);
                        if (result.usingWrapper) {
                            break;
                        }
                        i++;
                    } catch (ZygoteStartFailedEx ex) {
                        Log.d(LOG_TAG, "ZygoteOnDemand: waitForSecondaryZygote exception " + ex);
                    }
                }
            } else {
                Log.d(LOG_TAG, "ZygoteOnDemand: service is not running");
                return null;
            }
        }
    }

    private static boolean waitSecondaryZygoteChangedLocked(String to) {
        if (DEBUG_ZYGOTE_ON_DEMAND) {
            Log.d(LOG_TAG, "ZygoteOnDemand: waitSecondaryZygoteChangedLocked to " + to + ", start time: " + SystemClock.elapsedRealtime());
        }
        boolean result = false;
        for (int i = 0; i < 25; i++) {
            if (i != 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Log.d(LOG_TAG, "ZygoteOnDemand: waitSecondaryZygoteChangedLocked exception: " + ie);
                }
            }
            if (SystemProperties.get("init.svc.zygote_secondary", "stopped").equals(to)) {
                result = true;
                break;
            }
        }
        if (DEBUG_ZYGOTE_ON_DEMAND) {
            Log.d(LOG_TAG, "ZygoteOnDemand: waitSecondaryZygoteChangedLocked result = " + result + ", end time: " + SystemClock.elapsedRealtime());
        }
        return result;
    }

    public static void establishZygoteConnectionForAbi(String abi) {
        try {
            openZygoteSocketIfNeeded(abi);
        } catch (ZygoteStartFailedEx ex) {
            throw new RuntimeException("Unable to connect to zygote for abi: " + abi, ex);
        }
    }

    private static ZygoteState openZygoteSocketIfNeeded(String abi) throws ZygoteStartFailedEx {
        if (primaryZygoteState == null || primaryZygoteState.isClosed()) {
            try {
                primaryZygoteState = ZygoteState.connect(ZYGOTE_SOCKET);
            } catch (IOException ioe) {
                throw new ZygoteStartFailedEx("Error connecting to primary zygote", ioe);
            }
        }
        if (primaryZygoteState.matches(abi)) {
            return primaryZygoteState;
        }
        if (secondaryZygoteState == null || secondaryZygoteState.isClosed()) {
            try {
                secondaryZygoteState = ZygoteState.connect(SECONDARY_ZYGOTE_SOCKET);
            } catch (IOException ioe2) {
                throw new ZygoteStartFailedEx("Error connecting to secondary zygote", ioe2);
            }
        }
        if (secondaryZygoteState.matches(abi)) {
            return secondaryZygoteState;
        }
        throw new ZygoteStartFailedEx("Unsupported zygote ABI: " + abi);
    }

    public static final long getStartElapsedRealtime() {
        return sStartElapsedRealtime;
    }

    public static final long getStartUptimeMillis() {
        return sStartUptimeMillis;
    }

    public static final void setStartTimes(long elapsedRealtime, long uptimeMillis) {
        sStartElapsedRealtime = elapsedRealtime;
        sStartUptimeMillis = uptimeMillis;
    }

    public static final boolean is64Bit() {
        return VMRuntime.getRuntime().is64Bit();
    }

    public static final int myPid() {
        return Os.getpid();
    }

    public static final int myPpid() {
        return Os.getppid();
    }

    public static final int myTid() {
        return Os.gettid();
    }

    public static final int myUid() {
        return Os.getuid();
    }

    public static UserHandle myUserHandle() {
        return UserHandle.of(UserHandle.getUserId(myUid()));
    }

    public static boolean isApplicationUid(int uid) {
        return UserHandle.isApp(uid);
    }

    public static final boolean isIsolated() {
        return isIsolated(myUid());
    }

    public static final boolean isIsolated(int uid) {
        uid = UserHandle.getAppId(uid);
        if (uid < FIRST_ISOLATED_UID || uid > LAST_ISOLATED_UID) {
            return false;
        }
        return true;
    }

    public static final int getUidForPid(int pid) {
        String[] procStatusLabels = new String[1];
        procStatusLabels[0] = "Uid:";
        long[] procStatusValues = new long[1];
        procStatusValues[0] = -1;
        readProcLines("/proc/" + pid + "/status", procStatusLabels, procStatusValues);
        return (int) procStatusValues[0];
    }

    public static final int getParentPid(int pid) {
        String[] procStatusLabels = new String[1];
        procStatusLabels[0] = "PPid:";
        long[] procStatusValues = new long[1];
        procStatusValues[0] = -1;
        readProcLines("/proc/" + pid + "/status", procStatusLabels, procStatusValues);
        return (int) procStatusValues[0];
    }

    public static final int getThreadGroupLeader(int tid) {
        String[] procStatusLabels = new String[1];
        procStatusLabels[0] = "Tgid:";
        long[] procStatusValues = new long[1];
        procStatusValues[0] = -1;
        readProcLines("/proc/" + tid + "/status", procStatusLabels, procStatusValues);
        return (int) procStatusValues[0];
    }

    @Deprecated
    public static final boolean supportsProcesses() {
        return true;
    }

    public static final void killProcess(int pid) {
        sendSignal(pid, 9);
    }

    public static final void killProcessQuiet(int pid) {
        sendSignalQuiet(pid, 9);
    }

    public static final float getZramCompressRatio() {
        long compZram = Debug.getCompZram();
        long origZram = Debug.getOrigZram();
        if (0 == compZram) {
            return 1.0f;
        }
        if (compZram >= 3145728) {
            return ((float) origZram) / ((float) compZram);
        }
        if ((short) 1 == Debug.getZramCompressMethod()) {
            return 3.2f;
        }
        return 2.63f;
    }

    public static final long getZramExtraTotalSize() {
        if (Debug.getTotalZram() == 0) {
            return 0;
        }
        long compTotalSize = getTotalMemory() / 4;
        return ((long) (((float) compTotalSize) * getZramCompressRatio())) - compTotalSize;
    }

    public static final long getZramExtraAvailableSize() {
        if (Debug.getTotalZram() == 0 || SystemProperties.getBoolean("ro.default_cache_free", false)) {
            return 0;
        }
        long anonToCompress = getLruAnonMemory() - 15728640;
        if (anonToCompress < 0) {
            anonToCompress = 0;
        }
        return (long) (((float) anonToCompress) * (1.0f - (1.0f / getZramCompressRatio())));
    }

    public static final boolean isThreadInProcess(int tid, int pid) {
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            if (Os.access("/proc/" + tid + "/task/" + pid, OsConstants.F_OK)) {
                boolean z = true;
                return z;
            }
            StrictMode.setThreadPolicy(oldPolicy);
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }
}

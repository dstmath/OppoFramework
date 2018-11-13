package com.mediatek.anrmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SELinux;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.LocationManagerService;
import com.android.server.oppo.IElsaManager;
import com.mediatek.aee.ExceptionLog;
import com.mediatek.anrappframeworks.ANRAppFrameworks;
import com.mediatek.anrappmanager.ANRAppManager;
import com.mediatek.anrappmanager.ANRManagerNative;
import com.mediatek.common.jpe.a;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public final class ANRManager extends ANRManagerNative {
    public static int AnrOption = 0;
    public static final int DISABLE_ALL_ANR_MECHANISM = 0;
    public static final int DISABLE_PARTIAL_ANR_MECHANISM = 1;
    public static final int ENABLE_ALL_ANR_MECHANISM = 2;
    public static final int EVENT_BOOT_COMPLETED = 9001;
    public static final boolean IS_USER_BUILD = false;
    protected static final int MAX_MTK_TRACE_COUNT = 10;
    private static String[] NATIVE_STACKS_OF_INTEREST = null;
    protected static final int REMOVE_KEYDISPATCHING_TIMEOUT_MSG = 1005;
    public static final int RENAME_TRACE_FILES_MSG = 1006;
    protected static final int START_ANR_DUMP_MSG = 1003;
    public static final int START_MONITOR_BROADCAST_TIMEOUT_MSG = 1001;
    protected static final int START_MONITOR_KEYDISPATCHING_TIMEOUT_MSG = 1004;
    public static final int START_MONITOR_SERVICE_TIMEOUT_MSG = 1002;
    protected static ArrayList<Integer> additionNBTList;
    private static IAnrActivityManagerService b;
    private static final Object c = null;
    private static final ProcessCpuTracker d = null;
    public static ConcurrentHashMap<Integer, String> mMessageMap;
    public static int[] mZygotePids;
    private int a;
    private final AtomicLong e;
    private Context f;
    private long g;
    private long h;
    private int i;
    public AnrDumpMgr mAnrDumpMgr;
    public AnrMonitorHandler mAnrHandler;

    public interface IAnrActivityManagerService {
        File dumpStackTraces(boolean z, ArrayList<Integer> arrayList, ProcessCpuTracker processCpuTracker, SparseArray<Boolean> sparseArray, String[] strArr);

        ArrayList<Integer> getInterestingPids();

        boolean getMonitorCpuUsage();

        void getPidFromLruProcesses(int i, int i2, ArrayList<Integer> arrayList, SparseArray<Boolean> sparseArray);

        ProcessCpuTracker getProcessCpuTracker();

        int getProcessRecordPid(Object obj);

        boolean getShuttingDown();

        void updateCpuStatsNow();
    }

    public interface IAnrBroadcastQueue {
        int getOrderedBroadcastsPid();
    }

    public class AnrDumpMgr {
        public HashMap<Integer, AnrDumpRecord> mDumpList = new HashMap();

        public void cancelDump(AnrDumpRecord anrDumpRecord) {
            if (anrDumpRecord != null && anrDumpRecord.mAppPid != -1) {
                synchronized (this.mDumpList) {
                    AnrDumpRecord anrDumpRecord2 = (AnrDumpRecord) this.mDumpList.remove(Integer.valueOf(anrDumpRecord.mAppPid));
                    if (anrDumpRecord2 != null) {
                        anrDumpRecord2.mIsCancelled = true;
                    }
                }
            }
        }

        public void removeDumpRecord(AnrDumpRecord anrDumpRecord) {
            if (anrDumpRecord != null && anrDumpRecord.mAppPid != -1) {
                synchronized (this.mDumpList) {
                    AnrDumpRecord anrDumpRecord2 = (AnrDumpRecord) this.mDumpList.remove(Integer.valueOf(anrDumpRecord.mAppPid));
                }
            }
        }

        public void startAsyncDump(AnrDumpRecord anrDumpRecord) {
            if (anrDumpRecord != null && anrDumpRecord.mAppPid != -1) {
                Slog.i("ANRManager", "startAsyncDump: " + anrDumpRecord);
                int i = anrDumpRecord.mAppPid;
                synchronized (this.mDumpList) {
                    if (this.mDumpList.containsKey(Integer.valueOf(i))) {
                        return;
                    }
                    this.mDumpList.put(Integer.valueOf(i), anrDumpRecord);
                    ANRManager.this.mAnrHandler.sendMessageAtTime(ANRManager.this.mAnrHandler.obtainMessage(ANRManager.START_ANR_DUMP_MSG, anrDumpRecord), SystemClock.uptimeMillis() + 500);
                }
            }
        }

        private boolean a(AnrDumpRecord anrDumpRecord) {
            synchronized (this.mDumpList) {
                if (anrDumpRecord != null && this.mDumpList.containsKey(Integer.valueOf(anrDumpRecord.mAppPid)) && anrDumpRecord.isValid()) {
                    return true;
                }
                return false;
            }
        }

        public void dumpAnrDebugInfo(AnrDumpRecord anrDumpRecord, boolean z) {
            if (anrDumpRecord != null) {
                Slog.i("ANRManager", "dumpAnrDebugInfo begin: " + anrDumpRecord + ", isAsyncDump = " + z);
                if (ANRManager.b.getShuttingDown()) {
                    Slog.i("ANRManager", "dumpAnrDebugInfo During shutdown skipping ANR: " + anrDumpRecord.mAppString);
                    return;
                } else if (anrDumpRecord.mAppCrashing) {
                    Slog.i("ANRManager", "dumpAnrDebugInfo Crashing app skipping ANR: " + anrDumpRecord.mAppString);
                    return;
                } else if (a(anrDumpRecord)) {
                    ANRManager.this.setZramTag("6");
                    ANRManager.this.setZramMonitor(false);
                    dumpAnrDebugInfoLocked(anrDumpRecord, z);
                    Slog.i("ANRManager", "dumpAnrDebugInfo end: " + anrDumpRecord + ", isAsyncDump = " + z);
                    return;
                } else {
                    Slog.i("ANRManager", "dumpAnrDebugInfo dump stopped: " + anrDumpRecord);
                    return;
                }
            }
            Slog.i("ANRManager", "dumpAnrDebugInfo: " + anrDumpRecord);
        }

        protected void dumpAnrDebugInfoLocked(AnrDumpRecord anrDumpRecord, boolean z) {
            ArrayList arrayList = null;
            synchronized (anrDumpRecord) {
                Slog.i("ANRManager", "dumpAnrDebugInfoLocked: " + anrDumpRecord + ", isAsyncDump = " + z);
                if (a(anrDumpRecord)) {
                    int i;
                    int intValue;
                    int i2 = anrDumpRecord.mAppPid;
                    int i3 = anrDumpRecord.mParentAppPid;
                    ArrayList arrayList2 = new ArrayList();
                    SparseArray sparseArray = new SparseArray(20);
                    if (i2 != -1) {
                        arrayList = BinderWatchdog.getTimeoutBinderPidList(i2, i2);
                    }
                    arrayList2.add(Integer.valueOf(i2));
                    if (i3 <= 0) {
                        i = i2;
                    } else {
                        i = i3;
                    }
                    if (i != i2) {
                        arrayList2.add(Integer.valueOf(i));
                    }
                    if (ANRManager.this.a != i2) {
                        if (ANRManager.this.a != i) {
                            arrayList2.add(Integer.valueOf(ANRManager.this.a));
                        }
                    }
                    if (!z) {
                        ANRManager.b.getPidFromLruProcesses(i2, i, arrayList2, sparseArray);
                    }
                    if (arrayList != null && arrayList.size() > 0) {
                        Iterator it = arrayList.iterator();
                        while (it.hasNext()) {
                            Integer num = (Integer) it.next();
                            if (num != null) {
                                intValue = num.intValue();
                                if (!(intValue == i2 || intValue == i || intValue == ANRManager.this.a || arrayList2.contains(Integer.valueOf(intValue)))) {
                                    arrayList2.add(Integer.valueOf(intValue));
                                    sparseArray.remove(intValue);
                                }
                            }
                        }
                    }
                    if (i2 != -1) {
                        BinderWatchdog.a(i2, arrayList2, sparseArray);
                    }
                    ANRManager.b.getInterestingPids().clear();
                    ANRManager.b.getInterestingPids().add(Integer.valueOf(i2));
                    if (i3 <= 0) {
                        intValue = i;
                    } else {
                        intValue = i3;
                    }
                    if (!(intValue == i2 || ANRManager.this.isJavaProcess(intValue))) {
                        ANRManager.b.getInterestingPids().add(Integer.valueOf(intValue));
                    }
                    Iterator it2 = ANRManager.additionNBTList.iterator();
                    while (it2.hasNext()) {
                        intValue = ((Integer) it2.next()).intValue();
                        if (!ANRManager.b.getInterestingPids().contains(Integer.valueOf(intValue))) {
                            ANRManager.b.getInterestingPids().add(Integer.valueOf(intValue));
                        }
                    }
                    String str = anrDumpRecord.mAnnotation;
                    StringBuilder stringBuilder = anrDumpRecord.mInfo;
                    stringBuilder.setLength(0);
                    stringBuilder.append("ANR in ").append(anrDumpRecord.mProcessName);
                    if (anrDumpRecord.mShortComponentName != null) {
                        stringBuilder.append(" (").append(anrDumpRecord.mShortComponentName).append(")");
                    }
                    stringBuilder.append(", time=").append(anrDumpRecord.mAnrTime);
                    stringBuilder.append("\n");
                    if (str != null) {
                        stringBuilder.append("Reason: ").append(str).append("\n");
                    }
                    if (!(anrDumpRecord.mParentAppPid == -1 || anrDumpRecord.mParentAppPid == anrDumpRecord.mAppPid)) {
                        stringBuilder.append("Parent: ").append(anrDumpRecord.mParentShortComponentName).append("\n");
                    }
                    ProcessCpuTracker processCpuTracker = new ProcessCpuTracker(true);
                    if (a(anrDumpRecord)) {
                        File dumpStackTraces = ANRManager.b.dumpStackTraces(true, arrayList2, processCpuTracker, sparseArray, ANRManager.NATIVE_STACKS_OF_INTEREST);
                        if (a(anrDumpRecord)) {
                            if (ANRManager.b.getMonitorCpuUsage()) {
                                String str2;
                                synchronized (ANRManager.b.getProcessCpuTracker()) {
                                    str2 = ANRManager.this.getAndroidTime() + ANRManager.b.getProcessCpuTracker().printCurrentState(anrDumpRecord.mAnrTime);
                                    anrDumpRecord.mCpuInfo += str2;
                                }
                                ANRManager.b.updateCpuStatsNow();
                                stringBuilder.append(processCpuTracker.printCurrentLoad());
                                stringBuilder.append(str2);
                            }
                            Slog.e("ANRManager", stringBuilder.toString());
                            if (a(anrDumpRecord)) {
                                if (dumpStackTraces == null) {
                                    Process.sendSignal(i2, 3);
                                }
                                anrDumpRecord.mIsCompleted = true;
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        }
    }

    public class AnrDumpRecord {
        protected String mAnnotation;
        protected long mAnrTime;
        protected boolean mAppCrashing;
        protected int mAppPid;
        protected String mAppString;
        public String mCpuInfo = null;
        public StringBuilder mInfo = new StringBuilder(256);
        protected boolean mIsCancelled;
        protected boolean mIsCompleted;
        protected int mParentAppPid;
        protected String mParentShortComponentName;
        protected String mProcessName;
        protected String mShortComponentName;

        public AnrDumpRecord(int i, boolean z, String str, String str2, String str3, int i2, String str4, String str5, long j) {
            this.mAppPid = i;
            this.mAppCrashing = z;
            this.mProcessName = str;
            this.mAppString = str2;
            this.mShortComponentName = str3;
            this.mParentAppPid = i2;
            this.mParentShortComponentName = str4;
            this.mAnnotation = str5;
            this.mAnrTime = j;
        }

        private boolean isValid() {
            if (this.mAppPid <= 0 || this.mIsCancelled || this.mIsCompleted) {
                return false;
            }
            return true;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("AnrDumpRecord{ ");
            stringBuilder.append(this.mAnnotation);
            stringBuilder.append(" ");
            stringBuilder.append(this.mAppString);
            stringBuilder.append(" IsCompleted:" + this.mIsCompleted);
            stringBuilder.append(" IsCancelled:" + this.mIsCancelled);
            stringBuilder.append(" }");
            return stringBuilder.toString();
        }
    }

    public class AnrMonitorHandler extends Handler {
        public AnrMonitorHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1001:
                    IAnrBroadcastQueue iAnrBroadcastQueue = (IAnrBroadcastQueue) message.obj;
                    if (iAnrBroadcastQueue != null) {
                        Slog.i("ANRManager", "monitor Broadcast ANR process (" + iAnrBroadcastQueue.getOrderedBroadcastsPid() + ")");
                        ANRManager.this.updateProcessStats();
                        ANRManager.this.setZramTag("5");
                        break;
                    }
                    Slog.i("ANRManager", "monitor Broadcast ANR process failed");
                    break;
                case 1002:
                    Slog.i("ANRManager", "monitor Service ANR process (" + ANRManager.b.getProcessRecordPid(message.obj) + ")");
                    ANRManager.this.updateProcessStats();
                    ANRManager.this.setZramTag("5");
                    break;
                case ANRManager.START_ANR_DUMP_MSG /*1003*/:
                    AnrDumpRecord anrDumpRecord = (AnrDumpRecord) message.obj;
                    Slog.i("ANRManager", "START_ANR_DUMP_MSG: " + anrDumpRecord);
                    ANRManager.this.mAnrDumpMgr.dumpAnrDebugInfo(anrDumpRecord, true);
                    break;
                case ANRManager.START_MONITOR_KEYDISPATCHING_TIMEOUT_MSG /*1004*/:
                    Slog.i("ANRManager", "Monitor KeyDispatching ANR process (" + message.arg1 + ")");
                    ANRManager.this.updateProcessStats();
                    ANRManager.this.setZramTag("5");
                    break;
                case ANRManager.RENAME_TRACE_FILES_MSG /*1006*/:
                    String str = SystemProperties.get("dalvik.vm.stack-trace-file", null);
                    if (str != null && str.length() != 0) {
                        ANRManager.renameFiles(true, str, "/data/anr/traces_");
                        ANRManager.renameFiles(true, "/data/anr/native1.txt", "/data/anr/native1_");
                        ANRManager.renameFiles(true, "/data/anr/native2.txt", "/data/anr/native2_");
                        break;
                    }
                    return;
                    break;
            }
        }
    }

    public class BinderDumpThread extends Thread {
        private int mPid;

        public BinderDumpThread(int i) {
            this.mPid = i;
        }

        public void run() {
            ANRManager.this.dumpBinderInfo(this.mPid);
        }
    }

    protected static final class BinderWatchdog {

        protected static class BinderInfo {
            protected static final int INDEX_FROM = 1;
            protected static final int INDEX_TO = 3;
            protected int mDstPid;
            protected int mDstTid;
            protected int mSrcPid;
            protected int mSrcTid;
            protected String mText;

            protected BinderInfo(String str) {
                if (str != null && str.length() > 0) {
                    this.mText = new String(str);
                    String[] split = str.split(" ");
                    String[] split2 = split[1].split(":");
                    if (split2 != null && split2.length == 2) {
                        this.mSrcPid = Integer.parseInt(split2[0]);
                        this.mSrcTid = Integer.parseInt(split2[1]);
                    }
                    split = split[3].split(":");
                    if (split != null && split.length == 2) {
                        this.mDstPid = Integer.parseInt(split[0]);
                        this.mDstTid = Integer.parseInt(split[1]);
                    }
                }
            }
        }

        protected static class TransactionInfo {
            protected String atime;
            protected String direction;
            protected String ktime;
            protected String rcv_pid;
            protected String rcv_tid;
            protected String snd_pid;
            protected String snd_tid;
            protected long spent_time;

            protected TransactionInfo() {
            }
        }

        protected BinderWatchdog() {
        }

        public static final ArrayList<Integer> getTimeoutBinderPidList(int i, int i2) {
            int i3 = 0;
            if (i <= 0) {
                return null;
            }
            ArrayList d = d();
            ArrayList<Integer> arrayList = new ArrayList();
            for (BinderInfo a = a(i, i2, d); a != null; a = a(a.mDstPid, a.mDstTid, d)) {
                if (a.mDstPid > 0) {
                    i3++;
                    if (arrayList.contains(Integer.valueOf(a.mDstPid))) {
                        Slog.d("ANRManager", "getTimeoutBinderPidList pid existed: " + a.mDstPid + " " + a.mText);
                    } else {
                        Slog.d("ANRManager", "getTimeoutBinderPidList pid added: " + a.mDstPid + " " + a.mText);
                        arrayList.add(Integer.valueOf(a.mDstPid));
                    }
                    if (i3 >= 5) {
                        break;
                    }
                }
            }
            if (arrayList == null || arrayList.size() == 0) {
                return getTimeoutBinderFromPid(i, d);
            }
            return arrayList;
        }

        public static final ArrayList<Integer> getTimeoutBinderFromPid(int i, ArrayList<BinderInfo> arrayList) {
            int i2 = 0;
            if (i <= 0 || arrayList == null) {
                return null;
            }
            Slog.d("ANRManager", "getTimeoutBinderFromPid " + i + " list size: " + arrayList.size());
            ArrayList<Integer> arrayList2 = new ArrayList();
            Iterator it = arrayList.iterator();
            while (true) {
                int i3 = i2;
                if (!it.hasNext()) {
                    break;
                }
                BinderInfo binderInfo = (BinderInfo) it.next();
                if (binderInfo != null && binderInfo.mSrcPid == i) {
                    i3++;
                    if (arrayList2.contains(Integer.valueOf(binderInfo.mDstPid))) {
                        Slog.d("ANRManager", "getTimeoutBinderFromPid pid existed: " + binderInfo.mDstPid + " " + binderInfo.mText);
                    } else {
                        Slog.d("ANRManager", "getTimeoutBinderFromPid pid added: " + binderInfo.mDstPid + " " + binderInfo.mText);
                        arrayList2.add(Integer.valueOf(binderInfo.mDstPid));
                    }
                    if (i3 >= 5) {
                        break;
                    }
                }
                i2 = i3;
            }
            return arrayList2;
        }

        private static BinderInfo a(int i, int i2, ArrayList<BinderInfo> arrayList) {
            if (arrayList == null || arrayList.size() == 0 || i == 0) {
                return null;
            }
            arrayList.size();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                BinderInfo binderInfo = (BinderInfo) it.next();
                if (binderInfo.mSrcPid == i && binderInfo.mSrcTid == i2) {
                    Slog.d("ANRManager", "Timeout binder pid found: " + binderInfo.mDstPid + " " + binderInfo.mText);
                    return binderInfo;
                }
            }
            return null;
        }

        /* JADX WARNING: Removed duplicated region for block: B:33:0x005f A:{SYNTHETIC, Splitter: B:33:0x005f} */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x007c A:{SYNTHETIC, Splitter: B:43:0x007c} */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0090 A:{SYNTHETIC, Splitter: B:51:0x0090} */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0090 A:{SYNTHETIC, Splitter: B:51:0x0090} */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0090 A:{SYNTHETIC, Splitter: B:51:0x0090} */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x005f A:{SYNTHETIC, Splitter: B:33:0x005f} */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x007c A:{SYNTHETIC, Splitter: B:43:0x007c} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static final ArrayList<BinderInfo> d() {
            Throwable e;
            BufferedReader bufferedReader;
            ArrayList<BinderInfo> arrayList = null;
            BufferedReader bufferedReader2;
            ArrayList<BinderInfo> arrayList2;
            try {
                File file = new File("/sys/kernel/debug/binder/timeout_log");
                if (file == null || !file.exists()) {
                    return null;
                }
                bufferedReader2 = new BufferedReader(new FileReader(file));
                try {
                    arrayList2 = new ArrayList();
                    while (true) {
                        try {
                            String readLine = bufferedReader2.readLine();
                            if (readLine != null) {
                                BinderInfo binderInfo = new BinderInfo(readLine);
                                if (binderInfo != null && binderInfo.mSrcPid > 0) {
                                    arrayList2.add(binderInfo);
                                }
                                if (arrayList2.size() > 64) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } catch (FileNotFoundException e2) {
                            e = e2;
                            bufferedReader = bufferedReader2;
                            try {
                                Slog.e("ANRManager", "FileNotFoundException", e);
                                if (bufferedReader != null) {
                                    try {
                                        bufferedReader.close();
                                    } catch (Throwable e3) {
                                        Slog.e("ANRManager", "IOException when close buffer reader:", e3);
                                    }
                                }
                                return arrayList2;
                            } catch (Throwable th) {
                                bufferedReader2 = bufferedReader;
                                arrayList = arrayList2;
                                if (bufferedReader2 != null) {
                                }
                                return arrayList;
                            }
                        } catch (IOException e4) {
                            e3 = e4;
                            arrayList = arrayList2;
                            try {
                                Slog.e("ANRManager", "IOException when gettting Binder. ", e3);
                                if (bufferedReader2 != null) {
                                    try {
                                        bufferedReader2.close();
                                    } catch (Throwable e32) {
                                        Slog.e("ANRManager", "IOException when close buffer reader:", e32);
                                    }
                                }
                                return arrayList;
                            } catch (Throwable th2) {
                                if (bufferedReader2 != null) {
                                }
                                return arrayList;
                            }
                        } catch (Throwable th3) {
                            arrayList = arrayList2;
                            if (bufferedReader2 != null) {
                                try {
                                    bufferedReader2.close();
                                } catch (Throwable e322) {
                                    Slog.e("ANRManager", "IOException when close buffer reader:", e322);
                                }
                            }
                            return arrayList;
                        }
                    }
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (Throwable e3222) {
                            Slog.e("ANRManager", "IOException when close buffer reader:", e3222);
                        }
                    }
                    return arrayList2;
                } catch (FileNotFoundException e5) {
                    e3222 = e5;
                    arrayList2 = null;
                    bufferedReader = bufferedReader2;
                    Slog.e("ANRManager", "FileNotFoundException", e3222);
                    if (bufferedReader != null) {
                    }
                    return arrayList2;
                } catch (IOException e6) {
                    e3222 = e6;
                    Slog.e("ANRManager", "IOException when gettting Binder. ", e3222);
                    if (bufferedReader2 != null) {
                    }
                    return arrayList;
                }
            } catch (FileNotFoundException e7) {
                e3222 = e7;
                arrayList2 = null;
                Slog.e("ANRManager", "FileNotFoundException", e3222);
                if (bufferedReader != null) {
                }
                return arrayList2;
            } catch (IOException e8) {
                e3222 = e8;
                bufferedReader2 = null;
                Slog.e("ANRManager", "IOException when gettting Binder. ", e3222);
                if (bufferedReader2 != null) {
                }
                return arrayList;
            } catch (Throwable th4) {
                bufferedReader2 = null;
                if (bufferedReader2 != null) {
                }
                return arrayList;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:65:0x0204 A:{SYNTHETIC, Splitter: B:65:0x0204} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static final void a(int i, ArrayList<Integer> arrayList) {
            Throwable e;
            Pattern compile = Pattern.compile("(\\S+.+transaction).+from\\s+(\\d+):(\\d+)\\s+to\\s+(\\d+):(\\d+).+start\\s+(\\d+\\.+\\d+).+android\\s+(\\d+-\\d+-\\d+\\s+\\d+:\\d+:\\d+\\.\\d+)");
            ArrayList arrayList2 = new ArrayList();
            ArrayList arrayList3 = new ArrayList();
            BufferedReader bufferedReader;
            try {
                File file = new File("/sys/kernel/debug/binder/proc/" + Integer.toString(i));
                if (file != null && file.exists()) {
                    bufferedReader = new BufferedReader(new FileReader(file));
                    while (true) {
                        try {
                            CharSequence readLine = bufferedReader.readLine();
                            if (readLine == null) {
                                break;
                            } else if (!readLine.contains("transaction")) {
                                if (readLine.indexOf("node") != -1) {
                                    if (readLine.indexOf("node") < 20) {
                                        break;
                                    }
                                } else {
                                    continue;
                                }
                            } else {
                                Matcher matcher = compile.matcher(readLine);
                                if (matcher.find()) {
                                    TransactionInfo transactionInfo = new TransactionInfo();
                                    transactionInfo.direction = matcher.group(1);
                                    transactionInfo.snd_pid = matcher.group(2);
                                    transactionInfo.snd_tid = matcher.group(3);
                                    transactionInfo.rcv_pid = matcher.group(4);
                                    transactionInfo.rcv_tid = matcher.group(5);
                                    transactionInfo.ktime = matcher.group(6);
                                    transactionInfo.atime = matcher.group(7);
                                    transactionInfo.spent_time = SystemClock.uptimeMillis() - ((long) (Float.valueOf(transactionInfo.ktime).floatValue() * 1000.0f));
                                    arrayList2.add(transactionInfo);
                                    if ((transactionInfo.spent_time < 1000 ? 1 : null) == null && !arrayList.contains(Integer.valueOf(transactionInfo.rcv_pid))) {
                                        arrayList.add(Integer.valueOf(transactionInfo.rcv_pid));
                                        if (!arrayList3.contains(Integer.valueOf(transactionInfo.rcv_pid))) {
                                            arrayList3.add(Integer.valueOf(transactionInfo.rcv_pid));
                                            Log.d("ANRManager", "Transcation binderList pid=" + transactionInfo.rcv_pid);
                                        }
                                    }
                                    Log.d("ANRManager", transactionInfo.direction + " from " + transactionInfo.snd_pid + ":" + transactionInfo.snd_tid + " to " + transactionInfo.rcv_pid + ":" + transactionInfo.rcv_tid + " start " + transactionInfo.ktime + " android time " + transactionInfo.atime + " spent time " + transactionInfo.spent_time + " ms");
                                }
                            }
                        } catch (FileNotFoundException e2) {
                            e = e2;
                        } catch (IOException e3) {
                            e = e3;
                        }
                    }
                    Iterator it = arrayList3.iterator();
                    while (it.hasNext()) {
                        a(((Integer) it.next()).intValue(), arrayList);
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable e4) {
                            Slog.e("ANRManager", "IOException when close buffer reader:", e4);
                        }
                    }
                }
                Log.d("ANRManager", "Filepath isn't exist");
            } catch (FileNotFoundException e5) {
                e4 = e5;
                bufferedReader = null;
                try {
                    Log.e("ANRManager", "FileNotFoundException", e4);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable e42) {
                            Slog.e("ANRManager", "IOException when close buffer reader:", e42);
                        }
                    }
                } catch (Throwable th) {
                    e42 = th;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable e6) {
                            Slog.e("ANRManager", "IOException when close buffer reader:", e6);
                        }
                    }
                    throw e42;
                }
            } catch (IOException e7) {
                e42 = e7;
                bufferedReader = null;
                Log.e("ANRManager", "IOException when gettting Binder. ", e42);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable e422) {
                        Slog.e("ANRManager", "IOException when close buffer reader:", e422);
                    }
                }
            } catch (Throwable th2) {
                e422 = th2;
                bufferedReader = null;
                if (bufferedReader != null) {
                }
                throw e422;
            }
        }

        private static final void a(int i, ArrayList<Integer> arrayList, SparseArray<Boolean> sparseArray) {
            ArrayList arrayList2 = new ArrayList();
            a(i, arrayList2);
            if (arrayList2 != null && arrayList2.size() > 0) {
                Iterator it = arrayList2.iterator();
                while (it.hasNext()) {
                    Integer num = (Integer) it.next();
                    if (num != null) {
                        int intValue = num.intValue();
                        if (!(intValue == i || arrayList.contains(Integer.valueOf(intValue)))) {
                            arrayList.add(Integer.valueOf(intValue));
                            if (sparseArray != null) {
                                sparseArray.remove(intValue);
                            }
                        }
                    }
                }
            }
        }
    }

    public class DumpThread extends Thread {
        private int[] k;
        private String l;
        public boolean mResult = false;

        public DumpThread(int[] iArr, String str) {
            this.k = iArr;
            this.l = str;
        }

        public void run() {
            for (int i : this.k) {
                if (!(ANRManager.this.isJavaProcess(i) || ANRManager.this.isProcDoCoredump(i).booleanValue())) {
                    Slog.i("ANRManager", "[DumpNative] DumpThread native process =" + i);
                    Debug.dumpNativeBacktraceToFile(i, this.l);
                }
            }
            this.mResult = true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.anrmanager.ANRManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.anrmanager.ANRManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.anrmanager.ANRManager.<clinit>():void");
    }

    private ANRManager() {
        this.e = new AtomicLong(0);
        this.g = 0;
        this.h = 0;
        this.i = -1;
    }

    public ANRManager(IAnrActivityManagerService iAnrActivityManagerService, int i, Context context) {
        this.e = new AtomicLong(0);
        this.g = 0;
        this.h = 0;
        this.i = -1;
        this.a = i;
        b = iAnrActivityManagerService;
        this.f = context;
        if (!IS_USER_BUILD) {
            Looper.myLooper().setMessageLogging(ANRAppManager.getDefault(new ANRAppFrameworks()).newMessageLogger(false, Thread.currentThread().getName()));
        }
    }

    public void startANRManager() {
        new a().a();
        HandlerThread handlerThread = new HandlerThread("AnrMonitorThread");
        handlerThread.start();
        this.mAnrHandler = new AnrMonitorHandler(handlerThread.getLooper());
        this.mAnrDumpMgr = new AnrDumpMgr();
        d.init();
        prepareStackTraceFile(SystemProperties.get("dalvik.vm.mtk-stack-trace-file", null));
        prepareStackTraceFile(SystemProperties.get("dalvik.vm.stack-trace-file", null));
        prepareStackTraceFile("/data/anr/native1.txt");
        prepareStackTraceFile("/data/anr/native2.txt");
        File parentFile = new File(SystemProperties.get("dalvik.vm.stack-trace-file", null)).getParentFile();
        if (parentFile != null && !SELinux.restoreconRecursive(parentFile)) {
            Slog.d("ANRManager", "startANRManager SELinux.restoreconRecursive fail dir = " + parentFile.toString());
        }
    }

    public void prepareStackTraceFile(String str) {
        Slog.i("ANRManager", "prepareStackTraceFile: " + str);
        if (str != null && str.length() != 0) {
            File file = new File(str);
            try {
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    FileUtils.setPermissions(parentFile.getPath(), 509, -1, -1);
                }
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileUtils.setPermissions(file.getPath(), 438, -1, -1);
            } catch (Throwable e) {
                Slog.w("ANRManager", "Unable to prepare stack trace file: " + str, e);
            }
        }
    }

    public void delayRenameTraceFiles(int i) {
        this.mAnrHandler.removeMessages(RENAME_TRACE_FILES_MSG);
        this.mAnrHandler.sendEmptyMessageDelayed(RENAME_TRACE_FILES_MSG, (long) i);
    }

    public static File renameFiles(boolean z, String str, String str2) {
        File file = new File(str);
        Slog.d("ANRManager", "renameFiles Begin, clearTraces=" + z + ", nativetracesPath=" + str + ", subnativetracesPath=" + str2);
        try {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                FileUtils.setPermissions(parentFile.getPath(), 509, -1, -1);
            }
            if (z && file.exists()) {
                synchronized (c) {
                    for (int i = 8; i > 0; i--) {
                        File file2 = new File(str2 + Integer.toString(i) + ".txt");
                        if (file2.exists()) {
                            file2.renameTo(new File(str2 + Integer.toString(i + 1) + ".txt"));
                        }
                    }
                    file.renameTo(new File(str2 + "1.txt"));
                }
            }
            file.createNewFile();
            FileUtils.setPermissions(file.getPath(), 438, -1, -1);
            Slog.d("ANRManager", "renameFiles End");
            return file;
        } catch (Throwable e) {
            Slog.w("ANRManager", "Unable to prepare ANR traces file: " + str, e);
            return null;
        }
    }

    public Boolean isProcDoCoredump(int i) {
        ExceptionLog exceptionLog = null;
        try {
            if (SystemProperties.get("ro.have_aee_feature").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                exceptionLog = new ExceptionLog();
            }
        } catch (Exception e) {
        }
        if (exceptionLog == null || !exceptionLog.isNativeException(i)) {
            return Boolean.valueOf(false);
        }
        Slog.i("ANRManager", "[coredump] Process " + i + " is doing coredump");
        return Boolean.valueOf(true);
    }

    private Boolean a() {
        ExceptionLog exceptionLog = null;
        try {
            if (SystemProperties.get("ro.have_aee_feature").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                exceptionLog = new ExceptionLog();
            }
        } catch (Exception e) {
            Slog.d("ANRManager", "AEE is disabled or failed to allocate AEE object");
        }
        if (exceptionLog != null && exceptionLog.isException()) {
            return Boolean.valueOf(true);
        }
        return Boolean.valueOf(false);
    }

    public void informMessageDump(String str, int i) {
        if (mMessageMap.containsKey(Integer.valueOf(i))) {
            String str2 = (String) mMessageMap.get(Integer.valueOf(i));
            if (str2.length() > 50000) {
                str2 = IElsaManager.EMPTY_PACKAGE;
            }
            mMessageMap.put(Integer.valueOf(i), str2 + str);
        } else {
            if (mMessageMap.size() > 5) {
                mMessageMap.clear();
            }
            mMessageMap.put(Integer.valueOf(i), str);
        }
        Slog.i("ANRManager", "informMessageDump pid= " + i);
    }

    public static int enableANRDebuggingMechanism() {
        switch (AnrOption) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return 2;
        }
    }

    public boolean isJavaProcess(int i) {
        if (i <= 0) {
            return false;
        }
        if (mZygotePids == null) {
            String[] strArr = new String[2];
            strArr[0] = "zygote64";
            strArr[1] = "zygote";
            mZygotePids = Process.getPidsForCommands(strArr);
        }
        if (mZygotePids != null) {
            int parentPid = Process.getParentPid(i);
            for (int i2 : mZygotePids) {
                if (parentPid == i2) {
                    return true;
                }
            }
        }
        Slog.i("ANRManager", "pid: " + i + " is not a Java process");
        return false;
    }

    public void writeEvent(int i) {
        switch (i) {
            case EVENT_BOOT_COMPLETED /*9001*/:
                this.g = SystemClock.uptimeMillis();
                return;
            default:
                return;
        }
    }

    public boolean isAnrDeferrable() {
        if (enableANRDebuggingMechanism() == 0) {
            return false;
        }
        if ("dexopt".equals(SystemProperties.get("anr.autotest"))) {
            Slog.d("ANRManager", "We are doing TestDexOptSkipANR; return true in this case");
            return true;
        } else if ("enable".equals(SystemProperties.get("anr.autotest"))) {
            Slog.d("ANRManager", "Do Auto Test, don't skip ANR");
            return false;
        } else {
            long uptimeMillis = SystemClock.uptimeMillis();
            if (!IS_USER_BUILD) {
                if (this.g != 0) {
                    if (uptimeMillis - this.g >= 30000) {
                        if (a().booleanValue()) {
                            Slog.d("ANRManager", "isAnrDeferrable(): true since exception");
                            return true;
                        }
                        float totalCpuPercent = d.getTotalCpuPercent();
                        updateProcessStats();
                        float totalCpuPercent2 = d.getTotalCpuPercent();
                        if (totalCpuPercent > 90.0f && totalCpuPercent2 > 90.0f) {
                            if (this.h == 0) {
                                this.h = uptimeMillis;
                                Slog.d("ANRManager", "isAnrDeferrable(): true since CpuUsage = " + totalCpuPercent2 + ", mCpuDeferred = " + this.h);
                                return true;
                            }
                            boolean z;
                            if (uptimeMillis - this.h >= 8000) {
                                z = true;
                            } else {
                                z = false;
                            }
                            if (!z) {
                                Slog.d("ANRManager", "isAnrDeferrable(): true since CpuUsage = " + totalCpuPercent2 + ", mCpuDeferred = " + this.h + ", now = " + uptimeMillis);
                                return true;
                            }
                        }
                        this.h = 0;
                    }
                }
                Slog.d("ANRManager", "isAnrDeferrable(): true since mEventBootCompleted = " + this.g + " now = " + uptimeMillis);
                return true;
            }
            return false;
        }
    }

    public boolean isANRFlowSkipped(int i, String str, String str2, boolean z, boolean z2, boolean z3) {
        if (this.i == -1) {
            this.i = SystemProperties.getInt("persist.dbg.anrflow", 0);
        }
        Slog.d("ANRManager", "isANRFlowSkipped() AnrFlow = " + this.i);
        switch (this.i) {
            case 0:
                return false;
            case 1:
                Slog.i("ANRManager", "Skipping ANR flow: " + i + " " + str + " " + str2);
                return true;
            case 2:
                if (i != Process.myPid()) {
                    Slog.i("ANRManager", "Skipping ANR flow: " + i + " " + str + " " + str2);
                    if (z) {
                        Slog.i("ANRManager", "During shutdown skipping ANR: " + i + " " + str + " " + str2);
                        return true;
                    } else if (z2) {
                        Slog.i("ANRManager", "Skipping duplicate ANR: " + i + " " + str + " " + str2);
                        return true;
                    } else if (z3) {
                        Slog.i("ANRManager", "Crashing app skipping ANR: " + i + " " + str + " " + str2);
                        return true;
                    } else {
                        Slog.w("ANRManager", "Kill process (" + i + ") due to ANR");
                        Process.killProcess(i);
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public void notifyLightWeightANR(int i, String str, int i2) {
        if (2 == enableANRDebuggingMechanism()) {
            switch (i2) {
                case START_MONITOR_KEYDISPATCHING_TIMEOUT_MSG /*1004*/:
                    this.mAnrHandler.sendMessageAtTime(this.mAnrHandler.obtainMessage(START_MONITOR_KEYDISPATCHING_TIMEOUT_MSG, i, 0), SystemClock.uptimeMillis() + 5000);
                    break;
                case REMOVE_KEYDISPATCHING_TIMEOUT_MSG /*1005*/:
                    if (this.mAnrHandler.hasMessages(START_MONITOR_KEYDISPATCHING_TIMEOUT_MSG)) {
                        this.mAnrHandler.removeMessages(START_MONITOR_KEYDISPATCHING_TIMEOUT_MSG);
                        break;
                    }
                    break;
            }
        }
    }

    public void updateProcessStats() {
        synchronized (d) {
            long uptimeMillis = SystemClock.uptimeMillis();
            if ((uptimeMillis - this.e.get() <= 2500 ? 1 : null) == null) {
                this.e.set(uptimeMillis);
                d.update();
            }
        }
    }

    public String getProcessState() {
        String printCurrentState;
        synchronized (d) {
            Slog.i("ANRManager", "getProcessState");
            printCurrentState = d.printCurrentState(SystemClock.uptimeMillis());
        }
        return printCurrentState;
    }

    public String getAndroidTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        Date date = new Date(System.currentTimeMillis());
        Formatter formatter = new Formatter();
        StringBuilder append = new StringBuilder().append("Android time :[").append(simpleDateFormat.format(date)).append("] [");
        Object[] objArr = new Object[1];
        objArr[0] = Float.valueOf(((float) SystemClock.uptimeMillis()) / 1000.0f);
        return append.append(formatter.format("%.3f", objArr)).append("]\n").toString();
    }

    public void registerDumpNBTReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_ADD_NBT_DUMP_PID");
        intentFilter.addAction("android.intent.action.ACTION_REMOVE_NBT_DUMP_PID");
        this.f.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String stringExtra = intent.getStringExtra("NBT_DUMP_PROCESS");
                if (stringExtra == null) {
                    Slog.i("ANRManager", "Process name is null");
                    return;
                }
                String[] strArr = new String[1];
                strArr[0] = IElsaManager.EMPTY_PACKAGE;
                strArr[0] = stringExtra;
                int[] pidsForCommands = Process.getPidsForCommands(strArr);
                if (pidsForCommands.length <= 0) {
                    Slog.i("ANRManager", "No process corresponds to " + stringExtra);
                } else if ("android.intent.action.ACTION_ADD_NBT_DUMP_PID".equals(intent.getAction())) {
                    ANRManager.this.checkNBTDumpPid(pidsForCommands[0]);
                } else if ("android.intent.action.ACTION_REMOVE_NBT_DUMP_PID".equals(intent.getAction())) {
                    ANRManager.this.removeNBTDumpPid(pidsForCommands[0]);
                }
            }
        }, intentFilter);
    }

    public void checkNBTDumpPid(int i) {
        if (!isJavaProcess(i) && !additionNBTList.contains(Integer.valueOf(i))) {
            additionNBTList.add(Integer.valueOf(i));
            Slog.i("ANRManager", "Add NBTDumpPid pid=" + i);
        }
    }

    public void removeNBTDumpPid(int i) {
        if (additionNBTList.contains(Integer.valueOf(i))) {
            additionNBTList.remove(additionNBTList.indexOf(Integer.valueOf(i)));
            Slog.i("ANRManager", "Remove NBTDumpPid pid=" + i);
        }
    }

    public File createFile(String str) {
        File file = new File(str);
        if (file != null && file.exists()) {
            return file;
        }
        Log.d("ANRManager", "file isn't exist");
        return null;
    }

    public boolean copyFile(File file, File file2) {
        InputStream fileInputStream;
        boolean copyToFile;
        try {
            if (!file.exists()) {
                return false;
            }
            if (!file2.exists()) {
                file2.createNewFile();
                FileUtils.setPermissions(file2.getPath(), 438, -1, -1);
            }
            fileInputStream = new FileInputStream(file);
            copyToFile = copyToFile(fileInputStream, file2);
            fileInputStream.close();
            return copyToFile;
        } catch (IOException e) {
            Log.d("ANRManager", "createFile fail");
            copyToFile = false;
        } catch (Throwable th) {
            fileInputStream.close();
        }
    }

    /* JADX WARNING: Missing block: B:9:?, code:
            r1.flush();
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            r1.getFD().sync();
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            android.util.Log.d("ANRManager", "copyToFile: getFD fail");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean copyToFile(InputStream inputStream, File file) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file, true);
            byte[] bArr = new byte[4096];
            while (true) {
                int read = inputStream.read(bArr);
                if (read < 0) {
                    break;
                }
                fileOutputStream.write(bArr, 0, read);
            }
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.d("ANRManager", "copyToFile fail");
            return false;
        } catch (Throwable th) {
            fileOutputStream.flush();
            try {
                fileOutputStream.getFD().sync();
            } catch (IOException e2) {
                Log.d("ANRManager", "copyToFile: getFD fail");
            }
            fileOutputStream.close();
        }
    }

    public void stringToFile(String str, String str2) throws IOException {
        FileWriter fileWriter = new FileWriter(str, true);
        try {
            fileWriter.write(str2);
        } finally {
            fileWriter.close();
        }
    }

    public void dumpBinderInfo(int i) {
        try {
            File file = new File("/data/anr/binderinfo");
            if (file.exists()) {
                if (!file.delete()) {
                    Log.d("ANRManager", "dumpBinderInfo fail due to file likely to be locked by others");
                    return;
                } else if (file.createNewFile()) {
                    FileUtils.setPermissions(file.getPath(), 438, -1, -1);
                } else {
                    Log.d("ANRManager", "dumpBinderInfo fail due to file cannot be created");
                    return;
                }
            }
            File createFile = createFile("/sys/kernel/debug/binder/failed_transaction_log");
            if (createFile != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER FAILED TRANSACTION LOG ------\n");
                copyFile(createFile, file);
            }
            createFile = createFile("sys/kernel/debug/binder/timeout_log");
            if (createFile != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER TIMEOUT LOG ------\n");
                copyFile(createFile, file);
            }
            createFile = createFile("/sys/kernel/debug/binder/transaction_log");
            if (createFile != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER TRANSACTION LOG ------\n");
                copyFile(createFile, file);
            }
            createFile = createFile("/sys/kernel/debug/binder/transactions");
            if (createFile != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER TRANSACTIONS ------\n");
                copyFile(createFile, file);
            }
            createFile = createFile("/sys/kernel/debug/binder/stats");
            if (createFile != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER STATS ------\n");
                copyFile(createFile, file);
            }
            File file2 = new File("/sys/kernel/debug/binder/proc/" + Integer.toString(i));
            if (file2 != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER PROCESS STATE: $i ------\n");
                copyFile(file2, file);
            }
        } catch (IOException e) {
            Log.d("ANRManager", "dumpBinderInfo fail");
        }
    }

    public void enableBinderLog(boolean z) {
        String str;
        Slog.i("ANRManager", "enableBinderLog: " + z);
        String str2 = "/sys/kernel/debug/binder/transaction_log_enable";
        if (z) {
            str = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON;
        } else {
            str = "2";
        }
        a(str2, str);
    }

    public void enableTraceLog(boolean z) {
        String str;
        Slog.i("ANRManager", "enableTraceLog: " + z);
        String str2 = "/sys/kernel/debug/tracing/tracing_on";
        if (z) {
            str = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON;
        } else {
            str = "0";
        }
        a(str2, str);
    }

    public void setZramMonitor(boolean z) {
        String str;
        Slog.i("ANRManager", "setZramMonitor: " + z);
        String str2 = "/sys/module/mlog/parameters/timer_intval";
        if (z) {
            str = "100";
        } else {
            str = "6000";
        }
        a(str2, str);
    }

    public void setZramTag(String str) {
        Slog.i("ANRManager", "setZramTag: " + str);
        a("/sys/module/mlog/parameters/do_mlog", str);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0085 A:{SYNTHETIC, Splitter: B:21:0x0085} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00bc A:{SYNTHETIC, Splitter: B:30:0x00bc} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(String str, String str2) {
        IOException e;
        Throwable th;
        if (str != null) {
            File file = new File(str);
            ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
            StrictMode.allowThreadDiskWrites();
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file);
                try {
                    fileOutputStream.write(str2.getBytes());
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e2) {
                            Slog.e("ANRManager", "writeStringToFile close error: " + str + " " + e2.toString());
                        }
                    }
                    StrictMode.setThreadPolicy(allowThreadDiskReads);
                } catch (IOException e3) {
                    e2 = e3;
                    try {
                        Slog.e("ANRManager", "writeStringToFile error: " + str + " " + e2.toString());
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e22) {
                                Slog.e("ANRManager", "writeStringToFile close error: " + str + " " + e22.toString());
                            }
                        }
                        StrictMode.setThreadPolicy(allowThreadDiskReads);
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                        }
                        StrictMode.setThreadPolicy(allowThreadDiskReads);
                        throw th;
                    }
                }
            } catch (IOException e4) {
                e22 = e4;
                fileOutputStream = null;
                Slog.e("ANRManager", "writeStringToFile error: " + str + " " + e22.toString());
                if (fileOutputStream != null) {
                }
                StrictMode.setThreadPolicy(allowThreadDiskReads);
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = null;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e5) {
                        Slog.e("ANRManager", "writeStringToFile close error: " + str + " " + e5.toString());
                    }
                }
                StrictMode.setThreadPolicy(allowThreadDiskReads);
                throw th;
            }
        }
    }
}

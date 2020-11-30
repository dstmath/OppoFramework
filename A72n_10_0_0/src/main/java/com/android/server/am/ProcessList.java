package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.AppZygote;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.ChildZygoteProcess;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageManagerInternal;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StatsLog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.ProcessMap;
import com.android.internal.app.procstats.ProcessStats;
import com.android.internal.os.Zygote;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.MemInfoReader;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.Watchdog;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.OppoUsageService;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.DumpState;
import com.android.server.pm.dex.DexManager;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.theia.NoFocusWindow;
import com.android.server.usage.AppStandbyController;
import com.android.server.wm.ActivityServiceConnectionsHolder;
import com.android.server.wm.IColorAthenaManager;
import com.android.server.wm.WindowManagerService;
import com.mediatek.server.MtkSystemServer;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import libcore.io.IoUtils;

public final class ProcessList {
    static final int BACKUP_APP_ADJ = 300;
    static final int CACHED_APP_IMPORTANCE_LEVELS = 5;
    static final int CACHED_APP_LMK_FIRST_ADJ = 950;
    static final int CACHED_APP_MAX_ADJ = 999;
    static final int CACHED_APP_MIN_ADJ = 900;
    static final int FOREGROUND_APP_ADJ = 0;
    static final int HEAVY_WEIGHT_APP_ADJ = 400;
    static final int HOME_APP_ADJ = 600;
    static final int INVALID_ADJ = -10000;
    static final byte LMK_GETKILLCNT = 4;
    static final byte LMK_PROCPRIO = 1;
    static final byte LMK_PROCPURGE = 3;
    static final byte LMK_PROCREMOVE = 2;
    static final byte LMK_TARGET = 0;
    static final long MAX_EMPTY_TIME = 1800000;
    static final int MIN_CACHED_APPS = 2;
    static final int MIN_CRASH_INTERVAL = 60000;
    static final int NATIVE_ADJ = -1000;
    static final int PAGE_SIZE = 4096;
    public static final int PERCEPTIBLE_APP_ADJ = 200;
    static final int PERCEPTIBLE_LOW_APP_ADJ = 250;
    static final int PERCEPTIBLE_RECENT_FOREGROUND_APP_ADJ = 50;
    static final int PERSISTENT_PROC_ADJ = -800;
    static final int PERSISTENT_SERVICE_ADJ = -700;
    static final int PREVIOUS_APP_ADJ = 700;
    public static final int PROC_MEM_CACHED = 4;
    public static final int PROC_MEM_IMPORTANT = 2;
    public static final int PROC_MEM_NUM = 5;
    public static final int PROC_MEM_PERSISTENT = 0;
    public static final int PROC_MEM_SERVICE = 3;
    public static final int PROC_MEM_TOP = 1;
    private static final String PROPERTY_USE_APP_IMAGE_STARTUP_CACHE = "persist.device_config.runtime_native.use_app_image_startup_cache";
    public static final int PSS_ALL_INTERVAL = 1200000;
    private static final int PSS_FIRST_ASLEEP_BACKGROUND_INTERVAL = 30000;
    private static final int PSS_FIRST_ASLEEP_CACHED_INTERVAL = 60000;
    private static final int PSS_FIRST_ASLEEP_PERSISTENT_INTERVAL = 60000;
    private static final int PSS_FIRST_ASLEEP_TOP_INTERVAL = 20000;
    private static final int PSS_FIRST_BACKGROUND_INTERVAL = 20000;
    private static final int PSS_FIRST_CACHED_INTERVAL = 20000;
    private static final int PSS_FIRST_PERSISTENT_INTERVAL = 30000;
    private static final int PSS_FIRST_TOP_INTERVAL = 10000;
    public static final int PSS_MAX_INTERVAL = 3600000;
    public static final int PSS_MIN_TIME_FROM_STATE_CHANGE = 15000;
    public static final int PSS_SAFE_TIME_FROM_STATE_CHANGE = 1000;
    private static final int PSS_SAME_CACHED_INTERVAL = 600000;
    private static final int PSS_SAME_IMPORTANT_INTERVAL = 600000;
    private static final int PSS_SAME_PERSISTENT_INTERVAL = 600000;
    private static final int PSS_SAME_SERVICE_INTERVAL = 300000;
    private static final int PSS_SAME_TOP_INTERVAL = 60000;
    private static final int PSS_TEST_FIRST_BACKGROUND_INTERVAL = 5000;
    private static final int PSS_TEST_FIRST_TOP_INTERVAL = 3000;
    public static final int PSS_TEST_MIN_TIME_FROM_STATE_CHANGE = 10000;
    private static final int PSS_TEST_SAME_BACKGROUND_INTERVAL = 15000;
    private static final int PSS_TEST_SAME_IMPORTANT_INTERVAL = 10000;
    static final int SCHED_GROUP_BACKGROUND = 0;
    static final int SCHED_GROUP_DEFAULT = 2;
    static final int SCHED_GROUP_RESTRICTED = 1;
    public static final int SCHED_GROUP_TOP_APP = 3;
    static final int SCHED_GROUP_TOP_APP_BOUND = 4;
    static final int SERVICE_ADJ = 500;
    static final int SERVICE_B_ADJ = 800;
    static final int SYSTEM_ADJ = -900;
    static final String TAG = "ActivityManager";
    static final int TRIM_CRITICAL_THRESHOLD = 3;
    static final int TRIM_LOW_THRESHOLD = 5;
    static final int UNKNOWN_ADJ = 1001;
    static final int VISIBLE_APP_ADJ = 100;
    static final int VISIBLE_APP_LAYER_MAX = 99;
    private static final long[] sFirstAsleepPssTimes = {60000, 20000, 30000, 30000, 60000};
    private static final long[] sFirstAwakePssTimes = {30000, 10000, 20000, 20000, 20000};
    static KillHandler sKillHandler = null;
    static ServiceThread sKillThread = null;
    @GuardedBy({"sLmkdSocketLock"})
    private static InputStream sLmkdInputStream;
    @GuardedBy({"sLmkdSocketLock"})
    private static OutputStream sLmkdOutputStream;
    @GuardedBy({"sLmkdSocketLock"})
    private static LocalSocket sLmkdSocket;
    private static Object sLmkdSocketLock = new Object();
    private static final int[] sProcStateToProcMem = {0, 0, 1, 2, 2, 1, 2, 2, 2, 2, 2, 3, 4, 1, 2, 4, 4, 4, 4, 4, 4};
    private static final long[] sSameAsleepPssTimes = {600000, 60000, 600000, BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS, 600000};
    private static final long[] sSameAwakePssTimes = {600000, 60000, 600000, BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS, 600000};
    private static final long[] sTestFirstPssTimes = {BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS, 5000, 5000, 5000};
    private static final long[] sTestSamePssTimes = {15000, 10000, 10000, 15000, 15000};
    ActiveUids mActiveUids;
    @VisibleForTesting
    IsolatedUidRangeAllocator mAppIsolatedUidRangeAllocator = new IsolatedUidRangeAllocator(90000, 98999, 100);
    final ArrayMap<AppZygote, ArrayList<ProcessRecord>> mAppZygoteProcesses = new ArrayMap<>();
    final ProcessMap<AppZygote> mAppZygotes = new ProcessMap<>();
    private long mCachedRestoreLevel;
    @VisibleForTesting
    IsolatedUidRange mGlobalIsolatedUids = new IsolatedUidRange(99000, 99999);
    private boolean mHaveDisplaySize;
    final SparseArray<ProcessRecord> mIsolatedProcesses = new SparseArray<>();
    int mLruProcessActivityStart = 0;
    int mLruProcessServiceStart = 0;
    final ArrayList<ProcessRecord> mLruProcesses = new ArrayList<>();
    int mLruSeq = 0;
    private final int[] mOomAdj = {0, 100, 200, PERCEPTIBLE_LOW_APP_ADJ, CACHED_APP_MIN_ADJ, CACHED_APP_LMK_FIRST_ADJ};
    private final int[] mOomMinFree = new int[this.mOomAdj.length];
    private final int[] mOomMinFreeHigh = {73728, 92160, 110592, 129024, 147456, 184320};
    private final int[] mOomMinFreeLow = {12288, 18432, 24576, 36864, 43008, 49152};
    @GuardedBy({"mService"})
    final LongSparseArray<ProcessRecord> mPendingStarts = new LongSparseArray<>();
    @GuardedBy({"mService"})
    private long mProcStartSeqCounter = 0;
    @GuardedBy({"mService"})
    @VisibleForTesting
    long mProcStateSeqCounter = 0;
    final MyProcessMap mProcessNames = new MyProcessMap();
    final ArrayList<ProcessRecord> mRemovedProcesses = new ArrayList<>();
    ActivityManagerService mService = null;
    @GuardedBy({"mService"})
    final StringBuilder mStringBuilder = new StringBuilder(256);
    private final long mTotalMemMb;

    /* access modifiers changed from: package-private */
    public final class IsolatedUidRange {
        @VisibleForTesting
        public final int mFirstUid;
        @VisibleForTesting
        public final int mLastUid;
        @GuardedBy({"ProcessList.this.mService"})
        private int mNextUid;
        @GuardedBy({"ProcessList.this.mService"})
        private final SparseBooleanArray mUidUsed = new SparseBooleanArray();

        IsolatedUidRange(int firstUid, int lastUid) {
            this.mFirstUid = firstUid;
            this.mLastUid = lastUid;
            this.mNextUid = firstUid;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"ProcessList.this.mService"})
        public int allocateIsolatedUidLocked(int userId) {
            int stepsLeft = (this.mLastUid - this.mFirstUid) + 1;
            for (int i = 0; i < stepsLeft; i++) {
                int i2 = this.mNextUid;
                if (i2 < this.mFirstUid || i2 > this.mLastUid) {
                    this.mNextUid = this.mFirstUid;
                }
                int uid = UserHandle.getUid(userId, this.mNextUid);
                this.mNextUid++;
                if (!this.mUidUsed.get(uid, false)) {
                    this.mUidUsed.put(uid, true);
                    return uid;
                }
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"ProcessList.this.mService"})
        public void freeIsolatedUidLocked(int uid) {
            this.mUidUsed.delete(UserHandle.getAppId(uid));
        }
    }

    /* access modifiers changed from: package-private */
    public final class IsolatedUidRangeAllocator {
        @GuardedBy({"ProcessList.this.mService"})
        private final ProcessMap<IsolatedUidRange> mAppRanges = new ProcessMap<>();
        @GuardedBy({"ProcessList.this.mService"})
        private final BitSet mAvailableUidRanges;
        private final int mFirstUid;
        private final int mNumUidRanges;
        private final int mNumUidsPerRange;

        IsolatedUidRangeAllocator(int firstUid, int lastUid, int numUidsPerRange) {
            this.mFirstUid = firstUid;
            this.mNumUidsPerRange = numUidsPerRange;
            this.mNumUidRanges = ((lastUid - firstUid) + 1) / numUidsPerRange;
            this.mAvailableUidRanges = new BitSet(this.mNumUidRanges);
            this.mAvailableUidRanges.set(0, this.mNumUidRanges);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"ProcessList.this.mService"})
        public IsolatedUidRange getIsolatedUidRangeLocked(String processName, int uid) {
            return (IsolatedUidRange) this.mAppRanges.get(processName, uid);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"ProcessList.this.mService"})
        public IsolatedUidRange getOrCreateIsolatedUidRangeLocked(String processName, int uid) {
            IsolatedUidRange range = getIsolatedUidRangeLocked(processName, uid);
            if (range != null) {
                return range;
            }
            int uidRangeIndex = this.mAvailableUidRanges.nextSetBit(0);
            if (uidRangeIndex < 0) {
                return null;
            }
            this.mAvailableUidRanges.clear(uidRangeIndex);
            int i = this.mFirstUid;
            int i2 = this.mNumUidsPerRange;
            int actualUid = i + (uidRangeIndex * i2);
            IsolatedUidRange range2 = new IsolatedUidRange(actualUid, (i2 + actualUid) - 1);
            this.mAppRanges.put(processName, uid, range2);
            return range2;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"ProcessList.this.mService"})
        public void freeUidRangeLocked(ApplicationInfo info) {
            IsolatedUidRange range = (IsolatedUidRange) this.mAppRanges.get(info.processName, info.uid);
            if (range != null) {
                this.mAvailableUidRanges.set((range.mFirstUid - this.mFirstUid) / this.mNumUidsPerRange);
                this.mAppRanges.remove(info.processName, info.uid);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class MyProcessMap extends ProcessMap<ProcessRecord> {
        MyProcessMap() {
        }

        public ProcessRecord put(String name, int uid, ProcessRecord value) {
            ProcessRecord r = (ProcessRecord) ProcessList.super.put(name, uid, value);
            ProcessList.this.mService.mAtmInternal.onProcessAdded(r.getWindowProcessController());
            return r;
        }

        public ProcessRecord remove(String name, int uid) {
            ProcessRecord r = (ProcessRecord) ProcessList.super.remove(name, uid);
            ProcessList.this.mService.mAtmInternal.onProcessRemoved(name, uid);
            return r;
        }
    }

    /* access modifiers changed from: package-private */
    public final class KillHandler extends Handler {
        static final int KILL_PROCESS_GROUP_MSG = 4000;

        public KillHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what != KILL_PROCESS_GROUP_MSG) {
                super.handleMessage(msg);
                ProcessList.this.mService.handleOppoMessage(msg, 4);
                return;
            }
            Trace.traceBegin(64, "killProcessGroup " + msg.arg2);
            Process.killProcessGroup(msg.arg1, msg.arg2);
            Trace.traceEnd(64);
        }
    }

    ProcessList() {
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        this.mTotalMemMb = minfo.getTotalSize() / 1048576;
        updateOomLevels(0, 0, false);
    }

    /* access modifiers changed from: package-private */
    public void init(ActivityManagerService service, ActiveUids activeUids) {
        this.mService = service;
        this.mActiveUids = activeUids;
        if (sKillHandler == null) {
            sKillThread = new ServiceThread("ActivityManager:kill", 10, true);
            sKillThread.start();
            sKillHandler = new KillHandler(sKillThread.getLooper());
        }
    }

    /* access modifiers changed from: package-private */
    public void applyDisplaySize(WindowManagerService wm) {
        if (!this.mHaveDisplaySize) {
            Point p = new Point();
            wm.getBaseDisplaySize(0, p);
            if (p.x != 0 && p.y != 0) {
                updateOomLevels(p.x, p.y, true);
                this.mHaveDisplaySize = true;
            }
        }
    }

    private void updateOomLevels(int displayWidth, int displayHeight, boolean write) {
        float scaleMem = ((float) (this.mTotalMemMb - 350)) / 350.0f;
        float scaleDisp = (((float) (displayWidth * displayHeight)) - ((float) 384000)) / ((float) (1024000 - 384000));
        float scale = scaleMem > scaleDisp ? scaleMem : scaleDisp;
        if (scale < OppoBrightUtils.MIN_LUX_LIMITI) {
            scale = OppoBrightUtils.MIN_LUX_LIMITI;
        } else if (scale > 1.0f) {
            scale = 1.0f;
        }
        int minfree_adj = Resources.getSystem().getInteger(17694831);
        int minfree_abs = Resources.getSystem().getInteger(17694830);
        boolean is64bit = Build.SUPPORTED_64_BIT_ABIS.length > 0;
        for (int i = 0; i < this.mOomAdj.length; i++) {
            int low = this.mOomMinFreeLow[i];
            int high = this.mOomMinFreeHigh[i];
            if (is64bit) {
                if (i == 4) {
                    high = (high * 3) / 2;
                } else if (i == 5) {
                    high = (high * 7) / 4;
                }
            }
            this.mOomMinFree[i] = (int) (((float) low) + (((float) (high - low)) * scale));
        }
        if (minfree_abs >= 0) {
            int i2 = 0;
            while (true) {
                int[] iArr = this.mOomAdj;
                if (i2 >= iArr.length) {
                    break;
                }
                int[] iArr2 = this.mOomMinFree;
                iArr2[i2] = (int) ((((float) minfree_abs) * ((float) iArr2[i2])) / ((float) iArr2[iArr.length - 1]));
                i2++;
            }
        }
        if (minfree_adj != 0) {
            int i3 = 0;
            while (true) {
                int[] iArr3 = this.mOomAdj;
                if (i3 >= iArr3.length) {
                    break;
                }
                int[] iArr4 = this.mOomMinFree;
                iArr4[i3] = iArr4[i3] + ((int) ((((float) minfree_adj) * ((float) iArr4[i3])) / ((float) iArr4[iArr3.length - 1])));
                if (iArr4[i3] < 0) {
                    iArr4[i3] = 0;
                }
                i3++;
            }
        }
        this.mCachedRestoreLevel = (getMemLevel(999) / 1024) / 3;
        int reserve = (((displayWidth * displayHeight) * 4) * 3) / 1024;
        int reserve_adj = Resources.getSystem().getInteger(17694811);
        int reserve_abs = Resources.getSystem().getInteger(17694810);
        if (reserve_abs >= 0) {
            reserve = reserve_abs;
        }
        if (reserve_adj != 0 && (reserve = reserve + reserve_adj) < 0) {
            reserve = 0;
        }
        if (write) {
            ByteBuffer buf = ByteBuffer.allocate(((this.mOomAdj.length * 2) + 1) * 4);
            buf.putInt(0);
            for (int i4 = 0; i4 < this.mOomAdj.length; i4++) {
                buf.putInt((this.mOomMinFree[i4] * 1024) / 4096);
                buf.putInt(this.mOomAdj[i4]);
            }
            writeLmkd(buf, null);
            SystemProperties.set("sys.sysctl.extra_free_kbytes", Integer.toString((reserve * 2) + (reserve / 10)));
        }
    }

    public static int computeEmptyProcessLimit(int totalProcessLimit) {
        return totalProcessLimit / 2;
    }

    private static String buildOomTag(String prefix, String compactPrefix, String space, int val, int base, boolean compact) {
        int diff = val - base;
        if (diff != 0) {
            String str = "+";
            if (diff < 10) {
                StringBuilder sb = new StringBuilder();
                sb.append(prefix);
                if (!compact) {
                    str = "+ ";
                }
                sb.append(str);
                sb.append(Integer.toString(diff));
                return sb.toString();
            }
            return prefix + str + Integer.toString(diff);
        } else if (compact) {
            return compactPrefix;
        } else {
            if (space == null) {
                return prefix;
            }
            return prefix + space;
        }
    }

    public static String makeOomAdjString(int setAdj, boolean compact) {
        if (setAdj >= CACHED_APP_MIN_ADJ) {
            return buildOomTag("cch", "cch", "   ", setAdj, CACHED_APP_MIN_ADJ, compact);
        }
        if (setAdj >= SERVICE_B_ADJ) {
            return buildOomTag("svcb  ", "svcb", null, setAdj, SERVICE_B_ADJ, compact);
        }
        if (setAdj >= 700) {
            return buildOomTag("prev  ", "prev", null, setAdj, 700, compact);
        }
        if (setAdj >= 600) {
            return buildOomTag("home  ", "home", null, setAdj, 600, compact);
        }
        if (setAdj >= 500) {
            return buildOomTag("svc   ", "svc", null, setAdj, 500, compact);
        }
        if (setAdj >= 400) {
            return buildOomTag("hvy   ", "hvy", null, setAdj, 400, compact);
        }
        if (setAdj >= 300) {
            return buildOomTag("bkup  ", "bkup", null, setAdj, 300, compact);
        }
        if (setAdj >= PERCEPTIBLE_LOW_APP_ADJ) {
            return buildOomTag("prcl  ", "prcl", null, setAdj, PERCEPTIBLE_LOW_APP_ADJ, compact);
        }
        if (setAdj >= 200) {
            return buildOomTag("prcp  ", "prcp", null, setAdj, 200, compact);
        }
        if (setAdj >= 100) {
            return buildOomTag("vis", "vis", "   ", setAdj, 100, compact);
        }
        if (setAdj >= 0) {
            return buildOomTag("fore  ", "fore", null, setAdj, 0, compact);
        }
        if (setAdj >= PERSISTENT_SERVICE_ADJ) {
            return buildOomTag("psvc  ", "psvc", null, setAdj, PERSISTENT_SERVICE_ADJ, compact);
        }
        if (setAdj >= PERSISTENT_PROC_ADJ) {
            return buildOomTag("pers  ", "pers", null, setAdj, PERSISTENT_PROC_ADJ, compact);
        }
        if (setAdj >= SYSTEM_ADJ) {
            return buildOomTag("sys   ", "sys", null, setAdj, SYSTEM_ADJ, compact);
        }
        if (setAdj >= -1000) {
            return buildOomTag("ntv  ", "ntv", null, setAdj, -1000, compact);
        }
        return Integer.toString(setAdj);
    }

    public static String makeProcStateString(int curProcState) {
        switch (curProcState) {
            case 0:
                return "PER ";
            case 1:
                return "PERU";
            case 2:
                return "TOP ";
            case 3:
                return "FGSL";
            case 4:
                return "BTOP";
            case 5:
                return "FGS ";
            case 6:
                return "BFGS";
            case 7:
                return "IMPF";
            case 8:
                return "IMPB";
            case 9:
                return "TRNB";
            case 10:
                return "BKUP";
            case 11:
                return "SVC ";
            case 12:
                return "RCVR";
            case 13:
                return "TPSL";
            case 14:
                return "HVY ";
            case 15:
                return "HOME";
            case 16:
                return "LAST";
            case 17:
                return "CAC ";
            case 18:
                return "CACC";
            case 19:
                return "CRE ";
            case 20:
                return "CEM ";
            case 21:
                return "NONE";
            default:
                return "??";
        }
    }

    public static int makeProcStateProtoEnum(int curProcState) {
        switch (curProcState) {
            case -1:
                return 999;
            case 0:
                return 1000;
            case 1:
                return 1001;
            case 2:
                return 1002;
            case 3:
                return OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_RADIO;
            case 4:
                return 1020;
            case 5:
                return OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_RADIO;
            case 6:
                return OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_SYSTEM;
            case 7:
                return OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_LOST_FOUND;
            case 8:
                return OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_PHOENIX;
            case 9:
                return OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_RECOVERY_INFO;
            case 10:
                return OppoUsageService.IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_MEDIA_LOG_USAGE;
            case 11:
                return OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_CONNECTIVITY;
            case 12:
                return OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_MEDIA;
            case 13:
                return OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_RADIO;
            case 14:
                return OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_SYSTEM;
            case 15:
                return OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_LOST_FOUND;
            case 16:
                return OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_PHOENIX;
            case 17:
                return OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_RECOVERY_INFO;
            case 18:
                return OppoUsageService.IntergrateReserveManager.READ_OPPORESEVE2_TYPE_MEDIA_LOG_USAGE;
            case 19:
                return OppoUsageService.IntergrateReserveManager.OPPORESEVE2_TYPE_END;
            case 20:
                return 1018;
            case 21:
                return 1019;
            default:
                return 998;
        }
    }

    public static void appendRamKb(StringBuilder sb, long ramKb) {
        int j = 0;
        int fact = 10;
        while (j < 6) {
            if (ramKb < ((long) fact)) {
                sb.append(' ');
            }
            j++;
            fact *= 10;
        }
        sb.append(ramKb);
    }

    public static final class ProcStateMemTracker {
        final int[] mHighestMem = new int[5];
        int mPendingHighestMemState;
        int mPendingMemState;
        float mPendingScalingFactor;
        final float[] mScalingFactor = new float[5];
        int mTotalHighestMem = 4;

        public ProcStateMemTracker() {
            for (int i = 0; i < 5; i++) {
                this.mHighestMem[i] = 5;
                this.mScalingFactor[i] = 1.0f;
            }
            this.mPendingMemState = -1;
        }

        public void dumpLine(PrintWriter pw) {
            pw.print("best=");
            pw.print(this.mTotalHighestMem);
            pw.print(" (");
            boolean needSep = false;
            for (int i = 0; i < 5; i++) {
                if (this.mHighestMem[i] < 5) {
                    if (needSep) {
                        pw.print(", ");
                    }
                    pw.print(i);
                    pw.print("=");
                    pw.print(this.mHighestMem[i]);
                    pw.print(StringUtils.SPACE);
                    pw.print(this.mScalingFactor[i]);
                    pw.print("x");
                    needSep = true;
                }
            }
            pw.print(")");
            if (this.mPendingMemState >= 0) {
                pw.print(" / pending state=");
                pw.print(this.mPendingMemState);
                pw.print(" highest=");
                pw.print(this.mPendingHighestMemState);
                pw.print(StringUtils.SPACE);
                pw.print(this.mPendingScalingFactor);
                pw.print("x");
            }
            pw.println();
        }
    }

    public static boolean procStatesDifferForMem(int procState1, int procState2) {
        int[] iArr = sProcStateToProcMem;
        return iArr[procState1] != iArr[procState2];
    }

    public static long minTimeFromStateChange(boolean test) {
        return test ? 10000 : 15000;
    }

    public static void commitNextPssTime(ProcStateMemTracker tracker) {
        if (tracker.mPendingMemState >= 0) {
            tracker.mHighestMem[tracker.mPendingMemState] = tracker.mPendingHighestMemState;
            tracker.mScalingFactor[tracker.mPendingMemState] = tracker.mPendingScalingFactor;
            tracker.mTotalHighestMem = tracker.mPendingHighestMemState;
            tracker.mPendingMemState = -1;
        }
    }

    public static void abortNextPssTime(ProcStateMemTracker tracker) {
        tracker.mPendingMemState = -1;
    }

    public static long computeNextPssTime(int procState, ProcStateMemTracker tracker, boolean test, boolean sleeping, long now) {
        float scalingFactor;
        boolean first;
        long[] table;
        int memState = sProcStateToProcMem[procState];
        if (tracker != null) {
            int highestMemState = memState < tracker.mTotalHighestMem ? memState : tracker.mTotalHighestMem;
            first = highestMemState < tracker.mHighestMem[memState];
            tracker.mPendingMemState = memState;
            tracker.mPendingHighestMemState = highestMemState;
            if (first) {
                scalingFactor = 1.0f;
                tracker.mPendingScalingFactor = 1.0f;
            } else {
                scalingFactor = tracker.mScalingFactor[memState];
                tracker.mPendingScalingFactor = 1.5f * scalingFactor;
            }
        } else {
            first = true;
            scalingFactor = 1.0f;
        }
        if (test) {
            table = first ? sTestFirstPssTimes : sTestSamePssTimes;
        } else if (first) {
            table = sleeping ? sFirstAsleepPssTimes : sFirstAwakePssTimes;
        } else {
            table = sleeping ? sSameAsleepPssTimes : sSameAwakePssTimes;
        }
        long delay = (long) (((float) table[memState]) * scalingFactor);
        if (delay > AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT) {
            delay = AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
        }
        return now + delay;
    }

    /* access modifiers changed from: package-private */
    public long getMemLevel(int adjustment) {
        int i = 0;
        while (true) {
            int[] iArr = this.mOomAdj;
            if (i >= iArr.length) {
                return (long) (this.mOomMinFree[iArr.length - 1] * 1024);
            }
            if (adjustment <= iArr[i]) {
                return (long) (this.mOomMinFree[i] * 1024);
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    public long getCachedRestoreThresholdKb() {
        return this.mCachedRestoreLevel;
    }

    public static void setOomAdj(int pid, int uid, int amt) {
        if (pid <= 0 || amt == 1001) {
            return;
        }
        if (Process.getUidForPid(pid) != uid) {
            Slog.w(TAG, "Process " + pid + " does not match uid " + uid + " ignore setting hits oom adj");
        } else if (Process.getThreadGroupLeader(pid) != pid) {
            Slog.w(TAG, "Process " + pid + " is not thread groud tid, ignore setting hits oom adj");
        } else {
            long start = SystemClock.elapsedRealtime();
            ByteBuffer buf = ByteBuffer.allocate(16);
            buf.putInt(1);
            buf.putInt(pid);
            buf.putInt(uid);
            buf.putInt(amt);
            writeLmkd(buf, null);
            long now = SystemClock.elapsedRealtime();
            if (now - start > 250) {
                Slog.w(TAG, "SLOW OOM ADJ: " + (now - start) + "ms for pid " + pid + " = " + amt);
            }
        }
    }

    public static final void remove(int pid) {
        if (pid > 0) {
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.putInt(2);
            buf.putInt(pid);
            writeLmkd(buf, null);
        }
    }

    public static final Integer getLmkdKillCount(int min_oom_adj, int max_oom_adj) {
        ByteBuffer buf = ByteBuffer.allocate(12);
        ByteBuffer repl = ByteBuffer.allocate(8);
        buf.putInt(4);
        buf.putInt(min_oom_adj);
        buf.putInt(max_oom_adj);
        if (!writeLmkd(buf, repl)) {
            return null;
        }
        if (repl.getInt() == 4) {
            return new Integer(repl.getInt());
        }
        Slog.e(TAG, "Failed to get kill count, code mismatch");
        return null;
    }

    @GuardedBy({"sLmkdSocketLock"})
    private static boolean openLmkdSocketLS() {
        try {
            sLmkdSocket = new LocalSocket(3);
            sLmkdSocket.connect(new LocalSocketAddress("lmkd", LocalSocketAddress.Namespace.RESERVED));
            sLmkdOutputStream = sLmkdSocket.getOutputStream();
            sLmkdInputStream = sLmkdSocket.getInputStream();
            return true;
        } catch (IOException e) {
            Slog.w(TAG, "lowmemorykiller daemon socket open failed");
            sLmkdSocket = null;
            return false;
        }
    }

    @GuardedBy({"sLmkdSocketLock"})
    private static boolean writeLmkdCommandLS(ByteBuffer buf) {
        try {
            sLmkdOutputStream.write(buf.array(), 0, buf.position());
            return true;
        } catch (IOException e) {
            Slog.w(TAG, "Error writing to lowmemorykiller socket");
            IoUtils.closeQuietly(sLmkdSocket);
            sLmkdSocket = null;
            return false;
        }
    }

    @GuardedBy({"sLmkdSocketLock"})
    private static boolean readLmkdReplyLS(ByteBuffer buf) {
        try {
            if (sLmkdInputStream.read(buf.array(), 0, buf.array().length) == buf.array().length) {
                return true;
            }
        } catch (IOException e) {
            Slog.w(TAG, "Error reading from lowmemorykiller socket");
        }
        IoUtils.closeQuietly(sLmkdSocket);
        sLmkdSocket = null;
        return false;
    }

    private static boolean writeLmkd(ByteBuffer buf, ByteBuffer repl) {
        synchronized (sLmkdSocketLock) {
            for (int i = 0; i < 3; i++) {
                if (sLmkdSocket == null) {
                    if (!openLmkdSocketLS()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        ByteBuffer purge_buf = ByteBuffer.allocate(4);
                        purge_buf.putInt(3);
                        if (!writeLmkdCommandLS(purge_buf)) {
                            continue;
                        }
                    }
                }
                if (writeLmkdCommandLS(buf) && (repl == null || readLmkdReplyLS(repl))) {
                    return true;
                }
            }
            return false;
        }
    }

    static void killProcessGroup(int uid, int pid) {
        KillHandler killHandler = sKillHandler;
        if (killHandler != null) {
            killHandler.sendMessage(killHandler.obtainMessage(4000, uid, pid));
            return;
        }
        Slog.w(TAG, "Asked to kill process group before system bringup!");
        Process.killProcessGroup(uid, pid);
    }

    /* access modifiers changed from: package-private */
    public final ProcessRecord getProcessRecordLocked(String processName, int uid, boolean keepIfLarge) {
        if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
            Slog.v(TAG, "processName: " + processName + " uid " + uid + " keepIfLarge " + keepIfLarge);
        }
        if (uid == 1000) {
            SparseArray<ProcessRecord> procs = (SparseArray) this.mProcessNames.getMap().get(processName);
            if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                Slog.v(TAG, "procs: " + procs);
            }
            if (procs == null) {
                return null;
            }
            int procCount = procs.size();
            for (int i = 0; i < procCount; i++) {
                int procUid = procs.keyAt(i);
                if (!UserHandle.isCore(procUid) || !UserHandle.isSameUser(procUid, uid)) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.v(TAG, "procUid: " + procUid);
                    }
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.v(TAG, "i: " + i + " proc " + procs.valueAt(i));
                    }
                    return procs.valueAt(i);
                }
            }
        }
        ProcessRecord proc = (ProcessRecord) this.mProcessNames.get(processName, uid);
        if (proc != null && !keepIfLarge && this.mService.mLastMemoryLevel > 0 && proc.setProcState >= 20 && !"com.tencent.mm".equals(proc.processName)) {
            if (ActivityManagerDebugConfig.DEBUG_PSS) {
                Slog.d(ActivityManagerService.TAG_PSS, "May not keep " + proc + ": pss=" + proc.lastCachedPss);
            }
            if (proc.lastCachedPss >= getCachedRestoreThresholdKb()) {
                if (proc.baseProcessTracker != null) {
                    proc.baseProcessTracker.reportCachedKill(proc.pkgList.mPkgList, proc.lastCachedPss);
                    for (int ipkg = proc.pkgList.size() - 1; ipkg >= 0; ipkg--) {
                        ProcessStats.ProcessStateHolder holder = proc.pkgList.valueAt(ipkg);
                        StatsLog.write(17, proc.info.uid, holder.state.getName(), holder.state.getPackage(), proc.lastCachedPss, holder.appVersion);
                    }
                }
                proc.kill(Long.toString(proc.lastCachedPss) + "k from cached", true);
            }
        }
        return proc;
    }

    /* access modifiers changed from: package-private */
    public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
        long homeAppMem = getMemLevel(600);
        long cachedAppMem = getMemLevel(CACHED_APP_MIN_ADJ);
        outInfo.availMem = Process.getFreeMemory();
        outInfo.totalMem = Process.getTotalMemory();
        outInfo.threshold = homeAppMem;
        outInfo.lowMemory = outInfo.availMem < ((cachedAppMem - homeAppMem) / 2) + homeAppMem;
        outInfo.hiddenAppThreshold = cachedAppMem;
        outInfo.secondaryServerThreshold = getMemLevel(500);
        outInfo.visibleAppThreshold = getMemLevel(100);
        outInfo.foregroundAppThreshold = getMemLevel(0);
    }

    /* access modifiers changed from: package-private */
    public ProcessRecord findAppProcessLocked(IBinder app, String reason) {
        int NP = this.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            for (int ia = 0; ia < NA; ia++) {
                ProcessRecord p = apps.valueAt(ia);
                if (p.thread != null && p.thread.asBinder() == app) {
                    return p;
                }
            }
        }
        Slog.w(TAG, "Can't find mystery application for " + reason + " from pid=" + Binder.getCallingPid() + " uid=" + Binder.getCallingUid() + ": " + app);
        return null;
    }

    private void checkSlow(long startTime, String where) {
        long now = SystemClock.uptimeMillis();
        if (now - startTime > 50) {
            Slog.w(TAG, "Slow operation: " + (now - startTime) + "ms so far, now at " + where);
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:20:0x00a2 */
    /* JADX DEBUG: Multi-variable search result rejected for r15v1, resolved type: com.android.server.am.ProcessRecord */
    /* JADX DEBUG: Multi-variable search result rejected for r15v6, resolved type: com.android.server.am.ProcessList */
    /* JADX DEBUG: Multi-variable search result rejected for r15v10, resolved type: com.android.server.am.ProcessRecord */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public boolean startProcessLocked(ProcessRecord app, HostingRecord hostingRecord, boolean disableHiddenApiChecks, boolean mountExtStorageFull, String abiOverride) {
        String str;
        boolean z;
        ProcessRecord processRecord;
        RuntimeException e;
        ProcessRecord processRecord2;
        int mountExternal;
        int uid;
        int runtimeFlags;
        String invokeWith;
        String requiredAbi;
        String requiredAbi2;
        String str2;
        int mountExternal2;
        ProcessList processList = this;
        if (app.pendingStart) {
            return true;
        }
        long startTime = SystemClock.elapsedRealtime();
        if (app.pid > 0 && app.pid != ActivityManagerService.MY_PID) {
            processList.checkSlow(startTime, "startProcess: removing from pids map");
            processList.mService.mPidsSelfLocked.remove(app);
            processList.mService.mHandler.removeMessages(20, app);
            processList.checkSlow(startTime, "startProcess: done removing from pids map");
            app.setPid(0);
            app.startSeq = 0;
        }
        if (ActivityManagerDebugConfig.DEBUG_PROCESSES && processList.mService.mProcessesOnHold.contains(app)) {
            Slog.v(ActivityManagerService.TAG_PROCESSES, "startProcessLocked removing on hold: " + app);
        }
        processList.mService.mProcessesOnHold.remove(app);
        processList.checkSlow(startTime, "startProcess: starting to update cpu stats");
        processList.mService.updateCpuStats();
        processList.checkSlow(startTime, "startProcess: done updating cpu stats");
        if (!(hostingRecord == null || hostingRecord.getType() == null || !hostingRecord.getType().equals(IColorAppStartupManager.TYPE_ACTIVITY))) {
            OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).setPackageUnRestricted(app.info.packageName);
        }
        try {
            AppGlobals.getPackageManager().checkPackageStartable(app.info.packageName, UserHandle.getUserId(app.uid));
            try {
                int uid2 = app.uid;
                int[] gids = null;
                if (!app.isolated) {
                    try {
                        processList.checkSlow(startTime, "startProcess: getting gids from package manager");
                        int[] permGids = AppGlobals.getPackageManager().getPackageGids(app.info.packageName, 268435456, app.userId);
                        if (!StorageManager.hasIsolatedStorage() || !mountExtStorageFull) {
                            mountExternal2 = ((StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class)).getExternalStorageMountMode(uid2, app.info.packageName);
                        } else {
                            mountExternal2 = 6;
                        }
                        if (ArrayUtils.isEmpty(permGids)) {
                            gids = new int[3];
                        } else {
                            gids = new int[(permGids.length + 3)];
                            System.arraycopy(permGids, 0, gids, 3, permGids.length);
                        }
                        gids[0] = UserHandle.getSharedAppGid(UserHandle.getAppId(uid2));
                        gids[1] = UserHandle.getCacheAppGid(UserHandle.getAppId(uid2));
                        gids[2] = UserHandle.getUserGid(UserHandle.getUserId(uid2));
                        if (gids[0] == -1) {
                            gids[0] = gids[2];
                        }
                        if (gids[1] == -1) {
                            gids[1] = gids[2];
                        }
                        mountExternal = mountExternal2;
                    } catch (RemoteException e2) {
                        throw e2.rethrowAsRuntimeException();
                    } catch (RuntimeException e3) {
                        e = e3;
                        z = false;
                        processRecord = app;
                        str = TAG;
                        Slog.e(str, "Failure starting process " + processRecord.processName, e);
                        this.mService.forceStopPackageLocked(processRecord.info.packageName, UserHandle.getAppId(processRecord.uid), false, false, true, false, false, processRecord.userId, "start failure");
                        return z;
                    }
                } else {
                    mountExternal = 0;
                }
                app.mountMode = mountExternal;
                processList.checkSlow(startTime, "startProcess: building args");
                if (processList.mService.mAtmInternal.isFactoryTestProcess(app.getWindowProcessController())) {
                    uid = 0;
                } else {
                    uid = uid2;
                }
                int runtimeFlags2 = 0;
                if ((app.info.flags & 2) != 0) {
                    runtimeFlags2 = 0 | 1 | 256 | 2;
                    if (Settings.Global.getInt(processList.mService.mContext.getContentResolver(), "art_verifier_verify_debuggable", 1) == 0) {
                        runtimeFlags2 |= 512;
                        Slog.w(ActivityManagerService.TAG_PROCESSES, app + ": ART verification disabled");
                    }
                }
                if ((app.info.flags & 16384) != 0 || processList.mService.mSafeMode) {
                    runtimeFlags2 |= 8;
                }
                if ((app.info.privateFlags & DumpState.DUMP_VOLUMES) != 0) {
                    runtimeFlags2 |= 32768;
                }
                if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("debug.checkjni"))) {
                    runtimeFlags2 |= 2;
                }
                String genDebugInfoProperty = SystemProperties.get("debug.generate-debug-info");
                if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(genDebugInfoProperty) || TemperatureProvider.SWITCH_ON.equals(genDebugInfoProperty)) {
                    runtimeFlags2 |= 32;
                }
                String genMiniDebugInfoProperty = SystemProperties.get("dalvik.vm.minidebuginfo");
                if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(genMiniDebugInfoProperty) || TemperatureProvider.SWITCH_ON.equals(genMiniDebugInfoProperty)) {
                    runtimeFlags2 |= 2048;
                }
                if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("debug.jni.logging"))) {
                    runtimeFlags2 |= 16;
                }
                if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("debug.assert"))) {
                    runtimeFlags2 |= 4;
                }
                if (processList.mService.mNativeDebuggingApp != null && processList.mService.mNativeDebuggingApp.equals(app.processName)) {
                    runtimeFlags2 = runtimeFlags2 | 64 | 32 | 128;
                    processList.mService.mNativeDebuggingApp = null;
                }
                if (app.info.isEmbeddedDexUsed() || (app.info.isPrivilegedApp() && DexManager.isPackageSelectedToRunOob(app.pkgList.mPkgList.keySet()))) {
                    runtimeFlags2 |= 1024;
                }
                if (!disableHiddenApiChecks && !processList.mService.mHiddenApiBlacklist.isDisabled()) {
                    app.info.maybeUpdateHiddenApiEnforcementPolicy(processList.mService.mHiddenApiBlacklist.getPolicy());
                    int policy = app.info.getHiddenApiEnforcementPolicy();
                    int policyBits = policy << Zygote.API_ENFORCEMENT_POLICY_SHIFT;
                    if ((policyBits & 12288) == policyBits) {
                        runtimeFlags2 |= policyBits;
                    } else {
                        throw new IllegalStateException("Invalid API policy: " + policy);
                    }
                }
                String useAppImageCache = SystemProperties.get(PROPERTY_USE_APP_IMAGE_STARTUP_CACHE, "");
                if (TextUtils.isEmpty(useAppImageCache) || useAppImageCache.equals(TemperatureProvider.SWITCH_OFF)) {
                    runtimeFlags = runtimeFlags2;
                } else {
                    runtimeFlags = 65536 | runtimeFlags2;
                }
                String invokeWith2 = null;
                if ((app.info.flags & 2) != 0) {
                    String wrapperFileName = app.info.nativeLibraryDir + "/wrap.sh";
                    StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
                    try {
                        if (new File(wrapperFileName).exists()) {
                            invokeWith2 = "/system/bin/logwrapper " + wrapperFileName;
                        }
                        StrictMode.setThreadPolicy(oldPolicy);
                        invokeWith = invokeWith2;
                    } catch (RuntimeException e4) {
                        e = e4;
                        processRecord = app;
                        str = TAG;
                        z = false;
                        Slog.e(str, "Failure starting process " + processRecord.processName, e);
                        this.mService.forceStopPackageLocked(processRecord.info.packageName, UserHandle.getAppId(processRecord.uid), false, false, true, false, false, processRecord.userId, "start failure");
                        return z;
                    } catch (Throwable th) {
                        StrictMode.setThreadPolicy(oldPolicy);
                        throw th;
                    }
                } else {
                    invokeWith = null;
                }
                if (abiOverride != null) {
                    requiredAbi = abiOverride;
                } else {
                    try {
                        requiredAbi = app.info.primaryCpuAbi;
                    } catch (RuntimeException e5) {
                        e = e5;
                        processRecord2 = app;
                        str = TAG;
                        z = false;
                        processRecord = processRecord2;
                        Slog.e(str, "Failure starting process " + processRecord.processName, e);
                        this.mService.forceStopPackageLocked(processRecord.info.packageName, UserHandle.getAppId(processRecord.uid), false, false, true, false, false, processRecord.userId, "start failure");
                        return z;
                    }
                }
                if (requiredAbi == null) {
                    requiredAbi2 = Build.SUPPORTED_ABIS[0];
                } else {
                    requiredAbi2 = requiredAbi;
                }
                String instructionSet = null;
                if (app.info.primaryCpuAbi != null) {
                    instructionSet = VMRuntime.getInstructionSet(app.info.primaryCpuAbi);
                }
                app.gids = gids;
                app.setRequiredAbi(requiredAbi2);
                app.instructionSet = instructionSet;
                int[] gids2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getMultiUserGids(app, gids);
                if (TextUtils.isEmpty(app.info.seInfoUser)) {
                    StringBuilder sb = new StringBuilder();
                    str2 = "";
                    sb.append("SELinux tag not defined for ");
                    sb.append(app.info.packageName);
                    sb.append(" (uid ");
                    sb.append(app.uid);
                    sb.append(")");
                    Slog.wtf(TAG, "SELinux tag not defined", new IllegalStateException(sb.toString()));
                } else {
                    str2 = "";
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append(app.info.seInfo);
                sb2.append(TextUtils.isEmpty(app.info.seInfoUser) ? str2 : app.info.seInfoUser);
                String seInfo = sb2.toString();
                z = false;
                processList = app;
                str = TAG;
                return startProcessLocked(hostingRecord, "android.app.ActivityThread", app, uid, gids2, runtimeFlags, mountExternal, seInfo, requiredAbi2, instructionSet, invokeWith, startTime);
            } catch (RuntimeException e6) {
                e = e6;
                z = false;
                processRecord2 = app;
                str = TAG;
                processRecord = processRecord2;
                Slog.e(str, "Failure starting process " + processRecord.processName, e);
                this.mService.forceStopPackageLocked(processRecord.info.packageName, UserHandle.getAppId(processRecord.uid), false, false, true, false, false, processRecord.userId, "start failure");
                return z;
            }
        } catch (RemoteException e7) {
            throw e7.rethrowAsRuntimeException();
        } catch (RuntimeException e8) {
            e = e8;
            processRecord = processList;
            Slog.e(str, "Failure starting process " + processRecord.processName, e);
            this.mService.forceStopPackageLocked(processRecord.info.packageName, UserHandle.getAppId(processRecord.uid), false, false, true, false, false, processRecord.userId, "start failure");
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0101  */
    @GuardedBy({"mService"})
    public boolean startProcessLocked(HostingRecord hostingRecord, String entryPoint, ProcessRecord app, int uid, int[] gids, int runtimeFlags, int mountExternal, String seInfo, String requiredAbi, String instructionSet, String invokeWith, long startTime) {
        boolean z;
        if (!this.mService.mAmsExt.checkAutoBootPermission(this.mService.mContext, app.processName, app.userId, this.mLruProcesses, this.mService.mCallingPid)) {
            this.mService.mCallingPid = 0;
            return true;
        }
        app.pendingStart = true;
        app.killedByAm = false;
        app.removed = false;
        app.killed = false;
        if (app.startSeq != 0) {
            Slog.wtf(TAG, "startProcessLocked processName:" + app.processName + " with non-zero startSeq:" + app.startSeq);
        }
        if (app.pid != 0) {
            Slog.wtf(TAG, "startProcessLocked processName:" + app.processName + " with non-zero pid:" + app.pid);
        }
        long startSeq = this.mProcStartSeqCounter + 1;
        this.mProcStartSeqCounter = startSeq;
        app.startSeq = startSeq;
        app.setStartParams(uid, hostingRecord, seInfo, startTime);
        if (invokeWith == null) {
            if (SystemProperties.get("wrap." + app.processName) == null) {
                z = false;
                app.setUsingWrapper(z);
                this.mPendingStarts.put(startSeq, app);
                if (!this.mService.mConstants.FLAG_PROCESS_START_ASYNC) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.i(ActivityManagerService.TAG_PROCESSES, "Posting procStart msg for " + app.toShortString());
                    }
                    this.mService.mProcStartHandler.post(new Runnable(app, entryPoint, gids, runtimeFlags, mountExternal, requiredAbi, instructionSet, invokeWith, startSeq) {
                        /* class com.android.server.am.$$Lambda$ProcessList$vtq7LF5jIHO4t5NE03c8g7BT7Jc */
                        private final /* synthetic */ ProcessRecord f$1;
                        private final /* synthetic */ String f$2;
                        private final /* synthetic */ int[] f$3;
                        private final /* synthetic */ int f$4;
                        private final /* synthetic */ int f$5;
                        private final /* synthetic */ String f$6;
                        private final /* synthetic */ String f$7;
                        private final /* synthetic */ String f$8;
                        private final /* synthetic */ long f$9;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                            this.f$5 = r6;
                            this.f$6 = r7;
                            this.f$7 = r8;
                            this.f$8 = r9;
                            this.f$9 = r10;
                        }

                        public final void run() {
                            ProcessList.this.lambda$startProcessLocked$0$ProcessList(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9);
                        }
                    });
                    return true;
                }
                try {
                    Process.ProcessStartResult startResult = startProcess(hostingRecord, entryPoint, app, uid, gids, runtimeFlags, mountExternal, seInfo, requiredAbi, instructionSet, invokeWith, startTime);
                    handleProcessStartedLocked(app, startResult.pid, startResult.usingWrapper, startSeq, false);
                } catch (RuntimeException e) {
                    Slog.e(TAG, "Failure starting process " + app.processName, e);
                    app.pendingStart = false;
                    this.mService.forceStopPackageLocked(app.info.packageName, UserHandle.getAppId(app.uid), false, false, true, false, false, app.userId, "start failure");
                }
                return app.pid > 0;
            }
        }
        z = true;
        app.setUsingWrapper(z);
        this.mPendingStarts.put(startSeq, app);
        if (!this.mService.mConstants.FLAG_PROCESS_START_ASYNC) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0056 A[SYNTHETIC, Splitter:B:28:0x0056] */
    public /* synthetic */ void lambda$startProcessLocked$0$ProcessList(ProcessRecord app, String entryPoint, int[] gids, int runtimeFlags, int mountExternal, String requiredAbi, String instructionSet, String invokeWith, long startSeq) {
        long j;
        ProcessRecord processRecord;
        RuntimeException e;
        Throwable th;
        try {
            try {
                Process.ProcessStartResult startResult = startProcess(app.hostingRecord, entryPoint, app, app.startUid, gids, runtimeFlags, mountExternal, app.seInfo, requiredAbi, instructionSet, invokeWith, app.startTime);
                synchronized (this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        processRecord = app;
                        j = startSeq;
                        try {
                            handleProcessStartedLocked(processRecord, startResult, j);
                            try {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                            } catch (RuntimeException e2) {
                                e = e2;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (RuntimeException e3) {
                e = e3;
                processRecord = app;
                j = startSeq;
                synchronized (this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        Slog.e(TAG, "Failure starting process " + processRecord.processName, e);
                        this.mPendingStarts.remove(j);
                        processRecord.pendingStart = false;
                        this.mService.forceStopPackageLocked(processRecord.info.packageName, UserHandle.getAppId(processRecord.uid), false, false, true, false, false, processRecord.userId, "start failure");
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        } catch (RuntimeException e4) {
            e = e4;
            j = startSeq;
            processRecord = app;
            synchronized (this.mService) {
            }
        }
    }

    @GuardedBy({"mService"})
    public void killAppZygoteIfNeededLocked(AppZygote appZygote) {
        ApplicationInfo appInfo = appZygote.getAppInfo();
        ArrayList<ProcessRecord> zygoteProcesses = this.mAppZygoteProcesses.get(appZygote);
        if (zygoteProcesses != null && zygoteProcesses.size() == 0) {
            this.mAppZygotes.remove(appInfo.processName, appInfo.uid);
            this.mAppZygoteProcesses.remove(appZygote);
            this.mAppIsolatedUidRangeAllocator.freeUidRangeLocked(appInfo);
            appZygote.stopZygote();
        }
    }

    @GuardedBy({"mService"})
    private void removeProcessFromAppZygoteLocked(ProcessRecord app) {
        IsolatedUidRange appUidRange = this.mAppIsolatedUidRangeAllocator.getIsolatedUidRangeLocked(app.info.processName, app.hostingRecord.getDefiningUid());
        if (appUidRange != null) {
            appUidRange.freeIsolatedUidLocked(app.uid);
        }
        AppZygote appZygote = (AppZygote) this.mAppZygotes.get(app.info.processName, app.hostingRecord.getDefiningUid());
        if (appZygote != null) {
            ArrayList<ProcessRecord> zygoteProcesses = this.mAppZygoteProcesses.get(appZygote);
            zygoteProcesses.remove(app);
            if (zygoteProcesses.size() == 0) {
                this.mService.mHandler.removeMessages(71);
                if (app.removed) {
                    killAppZygoteIfNeededLocked(appZygote);
                    return;
                }
                Message msg = this.mService.mHandler.obtainMessage(71);
                msg.obj = appZygote;
                this.mService.mHandler.sendMessageDelayed(msg, 5000);
            }
        }
    }

    private AppZygote createAppZygoteForProcessIfNeeded(ProcessRecord app) {
        AppZygote appZygote;
        ArrayList<ProcessRecord> zygoteProcessList;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int uid = app.hostingRecord.getDefiningUid();
                appZygote = (AppZygote) this.mAppZygotes.get(app.info.processName, uid);
                if (appZygote == null) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.d(ActivityManagerService.TAG_PROCESSES, "Creating new app zygote.");
                    }
                    IsolatedUidRange uidRange = this.mAppIsolatedUidRangeAllocator.getIsolatedUidRangeLocked(app.info.processName, app.hostingRecord.getDefiningUid());
                    int userId = UserHandle.getUserId(uid);
                    int firstUid = UserHandle.getUid(userId, uidRange.mFirstUid);
                    int lastUid = UserHandle.getUid(userId, uidRange.mLastUid);
                    ApplicationInfo appInfo = new ApplicationInfo(app.info);
                    appInfo.packageName = app.hostingRecord.getDefiningPackageName();
                    appInfo.uid = uid;
                    appZygote = new AppZygote(appInfo, uid, firstUid, lastUid);
                    this.mAppZygotes.put(app.info.processName, uid, appZygote);
                    zygoteProcessList = new ArrayList<>();
                    this.mAppZygoteProcesses.put(appZygote, zygoteProcessList);
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.d(ActivityManagerService.TAG_PROCESSES, "Reusing existing app zygote.");
                    }
                    this.mService.mHandler.removeMessages(71, appZygote);
                    zygoteProcessList = this.mAppZygoteProcesses.get(appZygote);
                }
                zygoteProcessList.add(app);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return appZygote;
    }

    private Process.ProcessStartResult startProcess(HostingRecord hostingRecord, String entryPoint, ProcessRecord app, int uid, int[] gids, int runtimeFlags, int mountExternal, String seInfo, String requiredAbi, String instructionSet, String invokeWith, long startTime) {
        Process.ProcessStartResult startResult;
        try {
            Trace.traceBegin(64, "Start proc: " + app.processName);
            checkSlow(startTime, "startProcess: asking zygote to start proc");
            if (hostingRecord.usesWebviewZygote()) {
                String str = app.processName;
                int i = app.info.targetSdkVersion;
                String str2 = app.info.dataDir;
                String str3 = app.info.packageName;
                startResult = Process.startWebView(entryPoint, str, uid, uid, gids, runtimeFlags, mountExternal, i, seInfo, requiredAbi, instructionSet, str2, null, str3, new String[]{"seq=" + app.startSeq});
            } else if (hostingRecord.usesAppZygote()) {
                ChildZygoteProcess process = createAppZygoteForProcessIfNeeded(app).getProcess();
                String str4 = app.processName;
                int i2 = app.info.targetSdkVersion;
                String str5 = app.info.dataDir;
                String str6 = app.info.packageName;
                startResult = process.start(entryPoint, str4, uid, uid, gids, runtimeFlags, mountExternal, i2, seInfo, requiredAbi, instructionSet, str5, (String) null, str6, false, new String[]{"seq=" + app.startSeq});
            } else {
                String str7 = app.processName;
                int i3 = app.info.targetSdkVersion;
                String str8 = app.info.dataDir;
                String str9 = app.info.packageName;
                startResult = Process.start(entryPoint, str7, uid, uid, gids, runtimeFlags, mountExternal, i3, seInfo, requiredAbi, instructionSet, str8, invokeWith, str9, new String[]{"seq=" + app.startSeq});
            }
            checkSlow(startTime, "startProcess: returned from zygote!");
            return startResult;
        } finally {
            Trace.traceEnd(64);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final void startProcessLocked(ProcessRecord app, HostingRecord hostingRecord) {
        startProcessLocked(app, hostingRecord, null);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final boolean startProcessLocked(ProcessRecord app, HostingRecord hostingRecord, String abiOverride) {
        return startProcessLocked(app, hostingRecord, false, false, abiOverride);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final ProcessRecord startProcessLocked(String processName, ApplicationInfo info, boolean knownToBeDead, int intentFlags, HostingRecord hostingRecord, boolean allowWhileBooting, boolean isolated, int isolatedUid, boolean keepIfLarge, String abiOverride, String entryPoint, String[] entryPointArgs, Runnable crashHandler) {
        ProcessRecord app;
        long startTime = SystemClock.elapsedRealtime();
        if (this.mService.mActivityTaskManager.getOppoArmyController() == null || !this.mService.mActivityTaskManager.getOppoArmyController().isRunningDisallowed(info.packageName)) {
            if (!isolated) {
                ProcessRecord app2 = getProcessRecordLocked(processName, info.uid, keepIfLarge);
                checkSlow(startTime, "startProcess: after getProcessRecord");
                if ((intentFlags & 4) == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.v(TAG, "Clearing bad process: " + info.uid + SliceClientPermissions.SliceAuthority.DELIMITER + info.processName);
                    }
                    this.mService.mAppErrors.resetProcessCrashTimeLocked(info);
                    if (this.mService.mAppErrors.isBadProcessLocked(info)) {
                        EventLog.writeEvent((int) EventLogTags.AM_PROC_GOOD, Integer.valueOf(UserHandle.getUserId(info.uid)), Integer.valueOf(info.uid), info.processName);
                        this.mService.mAppErrors.clearBadProcessLocked(info);
                        if (app2 != null) {
                            app2.bad = false;
                        }
                    }
                } else if (this.mService.mAppErrors.isBadProcessLocked(info)) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.v(TAG, "Bad process: " + info.uid + SliceClientPermissions.SliceAuthority.DELIMITER + info.processName);
                    }
                    return null;
                }
                app = app2;
            } else {
                app = null;
            }
            if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                String str = ActivityManagerService.TAG_PROCESSES;
                StringBuilder sb = new StringBuilder();
                sb.append("startProcess: name=");
                sb.append(processName);
                sb.append(" app=");
                sb.append(app);
                sb.append(" knownToBeDead=");
                sb.append(knownToBeDead);
                sb.append(" thread=");
                sb.append(app != null ? app.thread : null);
                sb.append(" pid=");
                sb.append(app != null ? app.pid : -1);
                Slog.v(str, sb.toString());
            }
            if (app != null && app.pid > 0) {
                if ((knownToBeDead || app.killed) && app.thread != null) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.v(ActivityManagerService.TAG_PROCESSES, "App died: " + app);
                    }
                    checkSlow(startTime, "startProcess: bad proc running, killing");
                    killProcessGroup(app.uid, app.pid);
                    this.mService.handleAppDiedLocked(app, true, true);
                    checkSlow(startTime, "startProcess: done killing old proc");
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                        Slog.v(ActivityManagerService.TAG_PROCESSES, "App already running: " + app);
                    }
                    app.addPackage(info.packageName, info.longVersionCode, this.mService.mProcessStats);
                    checkSlow(startTime, "startProcess: done, added package to proc");
                    return app;
                }
            }
            if (app == null) {
                checkSlow(startTime, "startProcess: creating new process record");
                ProcessRecord app3 = newProcessRecordLocked(info, processName, isolated, isolatedUid, hostingRecord);
                if (app3 == null) {
                    Slog.w(TAG, "Failed making new process record for " + processName + SliceClientPermissions.SliceAuthority.DELIMITER + info.uid + " isolated=" + isolated);
                    return null;
                }
                app3.crashHandler = crashHandler;
                app3.isolatedEntryPoint = entryPoint;
                app3.isolatedEntryPointArgs = entryPointArgs;
                checkSlow(startTime, "startProcess: done creating new process record");
                app = app3;
            } else {
                app.addPackage(info.packageName, info.longVersionCode, this.mService.mProcessStats);
                checkSlow(startTime, "startProcess: added package to existing proc");
            }
            if (this.mService.mProcessesReady || this.mService.isAllowedWhileBooting(info) || allowWhileBooting) {
                checkSlow(startTime, "startProcess: stepping in to startProcess");
                boolean success = startProcessLocked(app, hostingRecord, abiOverride);
                checkSlow(startTime, "startProcess: done starting proc!");
                if (success) {
                    return app;
                }
                return null;
            }
            if (!this.mService.mProcessesOnHold.contains(app)) {
                this.mService.mProcessesOnHold.add(app);
            }
            if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                Slog.v(ActivityManagerService.TAG_PROCESSES, "System not ready, putting on hold: " + app);
            }
            checkSlow(startTime, "startProcess: returning with proc on hold");
            return app;
        }
        Slog.v(TAG, "do not start process for army requirement");
        return null;
    }

    @GuardedBy({"mService"})
    private String isProcStartValidLocked(ProcessRecord app, long expectedStartSeq) {
        StringBuilder sb = null;
        if (app.killedByAm) {
            if (0 == 0) {
                sb = new StringBuilder();
            }
            sb.append("killedByAm=true;");
        }
        if (this.mProcessNames.get(app.processName, app.uid) != app) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append("No entry in mProcessNames;");
        }
        if (!app.pendingStart) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append("pendingStart=false;");
        }
        if (app.startSeq > expectedStartSeq) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append("seq=" + app.startSeq + ",expected=" + expectedStartSeq + ";");
        }
        if (sb == null) {
            return null;
        }
        return sb.toString();
    }

    @GuardedBy({"mService"})
    private boolean handleProcessStartedLocked(ProcessRecord pending, Process.ProcessStartResult startResult, long expectedStartSeq) {
        if (this.mPendingStarts.get(expectedStartSeq) != null) {
            return handleProcessStartedLocked(pending, startResult.pid, startResult.usingWrapper, expectedStartSeq, false);
        }
        if (pending.pid != startResult.pid) {
            return false;
        }
        pending.setUsingWrapper(startResult.usingWrapper);
        return false;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public boolean handleProcessStartedLocked(ProcessRecord app, int pid, boolean usingWrapper, long expectedStartSeq, boolean procAttached) {
        String str;
        ProcessRecord oldApp;
        this.mPendingStarts.remove(expectedStartSeq);
        String reason = isProcStartValidLocked(app, expectedStartSeq);
        if (reason != null) {
            Slog.w(ActivityManagerService.TAG_PROCESSES, app + " start not valid, killing pid=" + pid + ", " + reason);
            app.pendingStart = false;
            Process.killProcessQuiet(pid);
            Process.killProcessGroup(app.uid, app.pid);
            return false;
        }
        this.mService.mBatteryStatsService.noteProcessStart(app.processName, app.info.uid);
        checkSlow(app.startTime, "startProcess: done updating battery stats");
        Object[] objArr = new Object[6];
        objArr[0] = Integer.valueOf(UserHandle.getUserId(app.startUid));
        objArr[1] = Integer.valueOf(pid);
        objArr[2] = Integer.valueOf(app.startUid);
        objArr[3] = app.processName;
        objArr[4] = app.hostingRecord.getType();
        objArr[5] = app.hostingRecord.getName() != null ? app.hostingRecord.getName() : "";
        EventLog.writeEvent((int) EventLogTags.AM_PROC_START, objArr);
        try {
            AppGlobals.getPackageManager().logAppProcessStartIfNeeded(app.processName, app.uid, app.seInfo, app.info.sourceDir, pid);
        } catch (RemoteException e) {
        }
        ActivityManagerService activityManagerService = this.mService;
        MtkSystemServer mtkSystemServer = ActivityManagerService.sMtkSystemServerIns;
        StringBuilder sb = new StringBuilder();
        sb.append("AP_Init:[");
        sb.append(app.hostingRecord.getType());
        sb.append("]:[");
        sb.append(app.processName);
        if (app.hostingRecord.getName() != null) {
            str = "]:[" + app.hostingRecord.getName();
        } else {
            str = "";
        }
        sb.append(str);
        sb.append("]:pid:");
        sb.append(pid);
        sb.append(app.isPersistent() ? ":(PersistAP)" : "");
        mtkSystemServer.addBootEvent(sb.toString());
        if (app.isPersistent()) {
            Watchdog.getInstance().processStarted(app.processName, pid);
        }
        checkSlow(app.startTime, "startProcess: building log message");
        StringBuilder buf = this.mStringBuilder;
        buf.setLength(0);
        buf.append("Start proc ");
        buf.append(pid);
        buf.append(':');
        buf.append(app.processName);
        buf.append('/');
        UserHandle.formatUid(buf, app.startUid);
        if (app.isolatedEntryPoint != null) {
            buf.append(" [");
            buf.append(app.isolatedEntryPoint);
            buf.append("]");
        }
        buf.append(" for ");
        buf.append(app.hostingRecord.getType());
        if (app.hostingRecord.getName() != null) {
            buf.append(StringUtils.SPACE);
            buf.append(app.hostingRecord.getName());
        }
        this.mService.reportUidInfoMessageLocked(TAG, buf.toString(), app.startUid);
        app.setPid(pid);
        app.setUsingWrapper(usingWrapper);
        app.pendingStart = false;
        if (this.mService.mColorAmsEx != null) {
            this.mService.mColorAmsEx.putProcInfoArray(app.pid, app.uid);
        }
        checkSlow(app.startTime, "startProcess: starting to update pids map");
        synchronized (this.mService.mPidsSelfLocked) {
            oldApp = this.mService.mPidsSelfLocked.get(pid);
        }
        if (oldApp != null && !app.isolated) {
            Slog.wtf(TAG, "handleProcessStartedLocked process:" + app.processName + " startSeq:" + app.startSeq + " pid:" + pid + " belongs to another existing app:" + oldApp.processName + " startSeq:" + oldApp.startSeq);
            this.mService.cleanUpApplicationRecordLocked(oldApp, false, false, -1, true);
        }
        this.mService.mPidsSelfLocked.put(app);
        synchronized (this.mService.mPidsSelfLocked) {
            if (!procAttached) {
                Message msg = this.mService.mHandler.obtainMessage(20);
                msg.obj = app;
                this.mService.mHandler.sendMessageDelayed(msg, usingWrapper ? 1200000 : 10000);
            }
        }
        checkSlow(app.startTime, "startProcess: done updating pids map");
        OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).handleStartAppInfo(app, app.hostingRecord.getType());
        return true;
    }

    /* access modifiers changed from: package-private */
    public final void removeLruProcessLocked(ProcessRecord app) {
        int lrui = this.mLruProcesses.lastIndexOf(app);
        if (lrui >= 0) {
            if (!app.killed) {
                if (app.isPersistent()) {
                    Slog.w(TAG, "Removing persistent process that hasn't been killed: " + app);
                } else {
                    Slog.wtfStack(TAG, "Removing process that hasn't been killed: " + app);
                    if (app.pid > 0) {
                        Process.killProcessQuiet(app.pid);
                        killProcessGroup(app.uid, app.pid);
                    } else {
                        app.pendingStart = false;
                    }
                }
            }
            int i = this.mLruProcessActivityStart;
            if (lrui < i) {
                this.mLruProcessActivityStart = i - 1;
            }
            int i2 = this.mLruProcessServiceStart;
            if (lrui < i2) {
                this.mLruProcessServiceStart = i2 - 1;
            }
            this.mLruProcesses.remove(lrui);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public boolean killPackageProcessesLocked(String packageName, int appId, int userId, int minOomAdj, String reason) {
        return killPackageProcessesLocked(packageName, appId, userId, minOomAdj, false, true, true, false, false, reason);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final boolean killPackageProcessesLocked(String packageName, int appId, int userId, int minOomAdj, boolean callerWillRestart, boolean allowRestart, boolean doit, boolean evenPersistent, boolean setRemoved, String reason) {
        int i = appId;
        ArrayList<ProcessRecord> procs = new ArrayList<>();
        int NP = this.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            for (int ia = 0; ia < NA; ia++) {
                ProcessRecord app = apps.valueAt(ia);
                if (!app.isPersistent() || evenPersistent) {
                    if (app.removed) {
                        if (doit) {
                            procs.add(app);
                        }
                    } else if (app.setAdj >= minOomAdj) {
                        if (packageName != null) {
                            boolean isDep = app.pkgDeps != null && app.pkgDeps.contains(packageName);
                            if (!OppoFeatureCache.get(IColorAthenaManager.DEFAULT).killPackageProcessesFilter(app.getWindowProcessController(), packageName, isDep, i)) {
                                if (isDep || UserHandle.getAppId(app.uid) == i) {
                                    if (userId == -1 || app.userId == userId) {
                                        if (!app.pkgList.containsKey(packageName) && !isDep) {
                                        }
                                    }
                                }
                            }
                        } else if (userId == -1 || app.userId == userId) {
                            if (i >= 0 && UserHandle.getAppId(app.uid) != i) {
                            }
                        }
                        if (!doit) {
                            return true;
                        }
                        if (setRemoved) {
                            app.removed = true;
                        }
                        procs.add(app);
                    }
                }
            }
        }
        int N = procs.size();
        for (int i2 = 0; i2 < N; i2++) {
            removeProcessLocked(procs.get(i2), callerWillRestart, allowRestart, reason);
        }
        ArrayList<AppZygote> zygotesToKill = new ArrayList<>();
        for (SparseArray<AppZygote> appZygotes : this.mAppZygotes.getMap().values()) {
            int i3 = 0;
            while (i3 < appZygotes.size()) {
                int appZygoteUid = appZygotes.keyAt(i3);
                if ((userId == -1 || UserHandle.getUserId(appZygoteUid) == userId) && (i < 0 || UserHandle.getAppId(appZygoteUid) == i)) {
                    AppZygote appZygote = appZygotes.valueAt(i3);
                    if (packageName == null || packageName.equals(appZygote.getAppInfo().packageName)) {
                        zygotesToKill.add(appZygote);
                    }
                }
                i3++;
                i = appId;
                procs = procs;
            }
            i = appId;
        }
        Iterator<AppZygote> it = zygotesToKill.iterator();
        while (it.hasNext()) {
            killAppZygoteIfNeededLocked(it.next());
        }
        this.mService.updateOomAdjLocked("updateOomAdj_processEnd");
        return N > 0;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public boolean removeProcessLocked(ProcessRecord app, boolean callerWillRestart, boolean allowRestart, String reason) {
        String name = app.processName;
        int uid = app.uid;
        if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
            String str = ActivityManagerService.TAG_PROCESSES;
            Slog.d(str, "Force removing proc " + app.toShortString() + " (" + name + SliceClientPermissions.SliceAuthority.DELIMITER + uid + ")");
        }
        if (((ProcessRecord) this.mProcessNames.get(name, uid)) != app) {
            Slog.w(TAG, "Ignoring remove of inactive process: " + app);
            return false;
        }
        removeProcessNameLocked(name, uid);
        this.mService.mAtmInternal.clearHeavyWeightProcessIfEquals(app.getWindowProcessController());
        boolean needRestart = false;
        if ((app.pid <= 0 || app.pid == ActivityManagerService.MY_PID) && (app.pid != 0 || !app.pendingStart)) {
            this.mRemovedProcesses.add(app);
        } else {
            if (app.pid > 0) {
                this.mService.mPidsSelfLocked.remove(app);
                this.mService.mHandler.removeMessages(20, app);
                this.mService.mBatteryStatsService.noteProcessFinish(app.processName, app.info.uid);
                if (app.isolated) {
                    this.mService.mBatteryStatsService.removeIsolatedUid(app.uid, app.info.uid);
                    this.mService.getPackageManagerInternalLocked().removeIsolatedUid(app.uid);
                    OppoFeatureCache.get(IColorHansManager.DEFAULT).removeIsolatedUid(app.uid, app.info.uid);
                }
            }
            boolean willRestart = false;
            if (app.isPersistent() && !app.isolated) {
                if (!callerWillRestart) {
                    willRestart = true;
                } else {
                    needRestart = true;
                }
            }
            app.kill(reason, true);
            this.mService.handleAppDiedLocked(app, willRestart, allowRestart);
            if (willRestart) {
                removeLruProcessLocked(app);
                this.mService.addAppLocked(app.info, null, false, null);
            }
        }
        return needRestart;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final void addProcessNameLocked(ProcessRecord proc) {
        ProcessRecord old = removeProcessNameLocked(proc.processName, proc.uid);
        if (old == proc && proc.isPersistent()) {
            Slog.w(TAG, "Re-adding persistent process " + proc);
        } else if (old != null) {
            Slog.wtf(TAG, "Already have existing proc " + old + " when adding " + proc);
        }
        UidRecord uidRec = this.mActiveUids.get(proc.uid);
        if (uidRec == null) {
            uidRec = new UidRecord(proc.uid);
            if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                Slog.i(ActivityManagerService.TAG_UID_OBSERVERS, "Creating new process uid: " + uidRec);
            }
            if (Arrays.binarySearch(this.mService.mDeviceIdleTempWhitelist, UserHandle.getAppId(proc.uid)) >= 0 || this.mService.mPendingTempWhitelist.indexOfKey(proc.uid) >= 0) {
                uidRec.curWhitelist = true;
                uidRec.setWhitelist = true;
            }
            uidRec.updateHasInternetPermission();
            this.mActiveUids.put(proc.uid, uidRec);
            EventLogTags.writeAmUidRunning(uidRec.uid);
            this.mService.noteUidProcessState(uidRec.uid, uidRec.getCurProcState());
        }
        proc.uidRecord = uidRec;
        proc.renderThreadTid = 0;
        uidRec.numProcs++;
        this.mProcessNames.put(proc.processName, proc.uid, proc);
        if (proc.isolated) {
            this.mIsolatedProcesses.put(proc.uid, proc);
        }
    }

    @GuardedBy({"mService"})
    private IsolatedUidRange getOrCreateIsolatedUidRangeLocked(ApplicationInfo info, HostingRecord hostingRecord) {
        if (hostingRecord == null || !hostingRecord.usesAppZygote()) {
            return this.mGlobalIsolatedUids;
        }
        return this.mAppIsolatedUidRangeAllocator.getOrCreateIsolatedUidRangeLocked(info.processName, hostingRecord.getDefiningUid());
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final ProcessRecord newProcessRecordLocked(ApplicationInfo info, String customProcess, boolean isolated, int isolatedUid, HostingRecord hostingRecord) {
        String proc = customProcess != null ? customProcess : info.processName;
        int userId = UserHandle.getUserId(info.uid);
        int uid = info.uid;
        if (isolated) {
            if (isolatedUid == 0) {
                IsolatedUidRange uidRange = getOrCreateIsolatedUidRangeLocked(info, hostingRecord);
                if (uidRange == null || (uid = uidRange.allocateIsolatedUidLocked(userId)) == -1) {
                    return null;
                }
            } else {
                uid = isolatedUid;
            }
            this.mService.getPackageManagerInternalLocked().addIsolatedUid(uid, info.uid);
            this.mService.mBatteryStatsService.addIsolatedUid(uid, info.uid);
            OppoFeatureCache.get(IColorHansManager.DEFAULT).addIsolatedUid(uid, info.uid);
            StatsLog.write(43, info.uid, uid, 1);
        }
        ProcessRecord r = new ProcessRecord(this.mService, info, proc, uid);
        if (!this.mService.mBooted && !this.mService.mBooting && userId == 0 && (info.flags & 9) == 9) {
            r.setCurrentSchedulingGroup(2);
            r.setSchedGroup = 2;
            r.setPersistent(true);
            r.maxAdj = PERSISTENT_PROC_ADJ;
        }
        if (isolated && isolatedUid != 0) {
            r.maxAdj = PERSISTENT_SERVICE_ADJ;
        }
        addProcessNameLocked(r);
        return r;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final ProcessRecord removeProcessNameLocked(String name, int uid) {
        return removeProcessNameLocked(name, uid, null);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final ProcessRecord removeProcessNameLocked(String name, int uid, ProcessRecord expecting) {
        ProcessRecord old = (ProcessRecord) this.mProcessNames.get(name, uid);
        if (expecting == null || old == expecting) {
            this.mProcessNames.remove(name, uid);
        }
        if (!(old == null || old.uidRecord == null)) {
            old.uidRecord.numProcs--;
            if (old.uidRecord.numProcs == 0) {
                if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                    Slog.i(ActivityManagerService.TAG_UID_OBSERVERS, "No more processes in " + old.uidRecord);
                }
                this.mService.enqueueUidChangeLocked(old.uidRecord, -1, 1);
                EventLogTags.writeAmUidStopped(uid);
                this.mActiveUids.remove(uid);
                this.mService.noteUidProcessState(uid, 21);
            }
            old.uidRecord = null;
        }
        this.mIsolatedProcesses.remove(uid);
        this.mGlobalIsolatedUids.freeIsolatedUidLocked(uid);
        ProcessRecord record = expecting != null ? expecting : old;
        if (record != null && record.appZygote) {
            removeProcessFromAppZygoteLocked(record);
        }
        return old;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void updateCoreSettingsLocked(Bundle settings) {
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord processRecord = this.mLruProcesses.get(i);
            try {
                if (processRecord.thread != null) {
                    processRecord.thread.setCoreSettings(settings);
                }
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void killAllBackgroundProcessesExceptLocked(int minTargetSdk, int maxProcState) {
        ArrayList<ProcessRecord> procs = new ArrayList<>();
        int NP = this.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            for (int ia = 0; ia < NA; ia++) {
                ProcessRecord app = apps.valueAt(ia);
                if (app.removed || ((minTargetSdk < 0 || app.info.targetSdkVersion < minTargetSdk) && (maxProcState < 0 || app.setProcState > maxProcState))) {
                    procs.add(app);
                }
            }
        }
        int N = procs.size();
        for (int i = 0; i < N; i++) {
            removeProcessLocked(procs.get(i), false, true, "kill all background except");
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void updateAllTimePrefsLocked(int timePref) {
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord r = this.mLruProcesses.get(i);
            if (r.thread != null) {
                try {
                    r.thread.updateTimePrefs(timePref);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to update preferences for: " + r.info.processName);
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void setAllHttpProxy() {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord r = this.mLruProcesses.get(i);
                    if (!(r.pid == ActivityManagerService.MY_PID || r.thread == null || r.isolated)) {
                        try {
                            r.thread.updateHttpProxy();
                        } catch (RemoteException e) {
                            Slog.w(TAG, "Failed to update http proxy for: " + r.info.processName);
                        }
                    }
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        ActivityThread.updateHttpProxy(this.mService.mContext);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void clearAllDnsCacheLocked() {
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord r = this.mLruProcesses.get(i);
            if (r.thread != null) {
                try {
                    r.thread.clearDnsCache();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to clear dns cache for: " + r.info.processName);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void handleAllTrustStorageUpdateLocked() {
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord r = this.mLruProcesses.get(i);
            if (r.thread != null) {
                try {
                    r.thread.handleTrustStorageUpdate();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to handle trust storage update for: " + r.info.processName);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public int updateLruProcessInternalLocked(ProcessRecord app, long now, int index, int lruSeq, String what, Object obj, ProcessRecord srcApp) {
        app.lastActivityTime = now;
        if (app.hasActivitiesOrRecentTasks()) {
            return index;
        }
        int lrui = this.mLruProcesses.lastIndexOf(app);
        if (lrui < 0) {
            Slog.wtf(TAG, "Adding dependent process " + app + " not on LRU list: " + what + StringUtils.SPACE + obj + " from " + srcApp);
            return index;
        } else if (lrui >= index) {
            return index;
        } else {
            int i = this.mLruProcessActivityStart;
            if (lrui >= i && index < i) {
                return index;
            }
            this.mLruProcesses.remove(lrui);
            if (index > 0) {
                index--;
            }
            if (ActivityManagerDebugConfig.DEBUG_LRU) {
                Slog.d(ActivityManagerService.TAG_LRU, "Moving dep from " + lrui + " to " + index + " in LRU list: " + app);
            }
            this.mLruProcesses.add(index, app);
            app.lruSeq = lruSeq;
            return index;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:81:0x020c, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_LRU == false) goto L_0x02a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x020e, code lost:
        android.util.Slog.d(com.android.server.am.ActivityManagerService.TAG_LRU, "Already found a different group: connGroup=" + r10 + " group=" + r4.connectionGroup);
     */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x02e3  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0321 A[EDGE_INSN: B:148:0x0321->B:117:0x0321 ?: BREAK  , SYNTHETIC] */
    private void updateClientActivitiesOrdering(ProcessRecord topApp, int topI, int bottomI, int endIndex) {
        int endIndex2;
        ProcessRecord nextEndProc;
        int endImportance;
        ProcessRecord processRecord = topApp;
        if (!topApp.hasActivitiesOrRecentTasks() && !processRecord.treatLikeActivity && topApp.hasClientActivities()) {
            int uid = processRecord.info.uid;
            if (processRecord.connectionGroup > 0) {
                int endImportance2 = processRecord.connectionImportance;
                int i = endIndex;
                endIndex2 = endIndex;
                while (i >= bottomI) {
                    ProcessRecord subProc = this.mLruProcesses.get(i);
                    if (subProc.info.uid != uid || subProc.connectionGroup != processRecord.connectionGroup) {
                        endImportance2 = endImportance2;
                    } else if (i != endIndex2 || subProc.connectionImportance < endImportance2) {
                        if (ActivityManagerDebugConfig.DEBUG_LRU) {
                            Slog.d(ActivityManagerService.TAG_LRU, "Pulling up " + subProc + " to position in group with importance=" + subProc.connectionImportance);
                        }
                        boolean moved = false;
                        int pos = topI;
                        while (true) {
                            if (pos <= endIndex2) {
                                endImportance = endImportance2;
                                break;
                            }
                            ProcessRecord posProc = this.mLruProcesses.get(pos);
                            if (subProc.connectionImportance <= posProc.connectionImportance) {
                                this.mLruProcesses.remove(i);
                                this.mLruProcesses.add(pos, subProc);
                                if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                    String str = ActivityManagerService.TAG_LRU;
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Moving ");
                                    sb.append(subProc);
                                    sb.append(" from position ");
                                    sb.append(i);
                                    endImportance = endImportance2;
                                    sb.append(" to above ");
                                    sb.append(posProc);
                                    sb.append(" @ ");
                                    sb.append(pos);
                                    Slog.d(str, sb.toString());
                                } else {
                                    endImportance = endImportance2;
                                }
                                moved = true;
                                endIndex2--;
                            } else {
                                pos--;
                            }
                        }
                        if (!moved) {
                            this.mLruProcesses.remove(i);
                            this.mLruProcesses.add(endIndex2 - 1, subProc);
                            if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                Slog.d(ActivityManagerService.TAG_LRU, "Moving " + subProc + " from position " + i + " to end of group @ " + endIndex2);
                            }
                            endIndex2--;
                            endImportance2 = subProc.connectionImportance;
                        } else {
                            endImportance2 = endImportance;
                        }
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_LRU) {
                            Slog.d(ActivityManagerService.TAG_LRU, "Keeping in-place above " + subProc + " endImportance=" + endImportance2 + " group=" + subProc.connectionGroup + " importance=" + subProc.connectionImportance);
                        }
                        endIndex2--;
                        endImportance2 = subProc.connectionImportance;
                    }
                    i--;
                    processRecord = topApp;
                }
            } else {
                endIndex2 = endIndex;
            }
            int i2 = endIndex2;
            while (i2 >= bottomI) {
                ProcessRecord subProc2 = this.mLruProcesses.get(i2);
                if (ActivityManagerDebugConfig.DEBUG_LRU) {
                    Slog.d(ActivityManagerService.TAG_LRU, "Looking to spread old procs, at " + subProc2 + " @ " + i2);
                }
                if (subProc2.info.uid != uid) {
                    if (i2 < endIndex2) {
                        boolean hasActivity = false;
                        int connUid = 0;
                        int connGroup = 0;
                        while (true) {
                            if (i2 < bottomI) {
                                break;
                            }
                            this.mLruProcesses.remove(i2);
                            this.mLruProcesses.add(endIndex2, subProc2);
                            if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                Slog.d(ActivityManagerService.TAG_LRU, "Different app, moving to " + endIndex2);
                            }
                            i2--;
                            if (i2 < bottomI) {
                                break;
                            }
                            subProc2 = this.mLruProcesses.get(i2);
                            if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                Slog.d(ActivityManagerService.TAG_LRU, "Looking at next app at " + i2 + ": " + subProc2);
                            }
                            if (subProc2.hasActivitiesOrRecentTasks() || subProc2.treatLikeActivity) {
                                if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                    Slog.d(ActivityManagerService.TAG_LRU, "This is hosting an activity!");
                                }
                                if (!hasActivity) {
                                    hasActivity = true;
                                } else if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                    Slog.d(ActivityManagerService.TAG_LRU, "Already found an activity, done");
                                }
                            } else if (!subProc2.hasClientActivities()) {
                                continue;
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                    Slog.d(ActivityManagerService.TAG_LRU, "This is a client of an activity");
                                }
                                if (!hasActivity) {
                                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                        Slog.d(ActivityManagerService.TAG_LRU, "This is an activity client!  uid=" + subProc2.info.uid + " group=" + subProc2.connectionGroup);
                                    }
                                    hasActivity = true;
                                    connUid = subProc2.info.uid;
                                    connGroup = subProc2.connectionGroup;
                                } else if (connUid != 0 && connUid == subProc2.info.uid) {
                                    if (connGroup == 0 || connGroup != subProc2.connectionGroup) {
                                    }
                                }
                            }
                            endIndex2--;
                        }
                        if (ActivityManagerDebugConfig.DEBUG_LRU) {
                            Slog.d(ActivityManagerService.TAG_LRU, "Already found a different activity: connUid=" + connUid + " uid=" + subProc2.info.uid);
                        }
                    }
                    while (true) {
                        endIndex2--;
                        if (endIndex2 < bottomI) {
                            break;
                        }
                        ProcessRecord endProc = this.mLruProcesses.get(endIndex2);
                        if (endProc.info.uid == uid) {
                            if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                Slog.d(ActivityManagerService.TAG_LRU, "Found next group of app: " + endProc + " @ " + endIndex2);
                            }
                        }
                    }
                    if (endIndex2 >= bottomI) {
                        ProcessRecord endProc2 = this.mLruProcesses.get(endIndex2);
                        while (true) {
                            endIndex2--;
                            if (endIndex2 < bottomI) {
                                nextEndProc = this.mLruProcesses.get(endIndex2);
                                if (!(nextEndProc.info.uid == uid && nextEndProc.connectionGroup == endProc2.connectionGroup)) {
                                }
                                endIndex2--;
                                if (endIndex2 < bottomI) {
                                    break;
                                }
                            }
                        }
                        if (ActivityManagerDebugConfig.DEBUG_LRU) {
                            Slog.d(ActivityManagerService.TAG_LRU, "Found next group or app: " + nextEndProc + " @ " + endIndex2 + " group=" + nextEndProc.connectionGroup);
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                        Slog.d(ActivityManagerService.TAG_LRU, "Bumping scan position to " + endIndex2);
                    }
                    i2 = endIndex2;
                } else {
                    i2--;
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r2v4 int: [D('index' int), D('nextIndex' int)] */
    /* access modifiers changed from: package-private */
    public final void updateLruProcessLocked(ProcessRecord app, boolean activityChange, ProcessRecord client) {
        int nextIndex;
        long now;
        int j;
        boolean hasActivity = app.hasActivitiesOrRecentTasks() || app.hasClientActivities() || app.treatLikeActivity;
        if (activityChange || !hasActivity) {
            this.mLruSeq++;
            long now2 = SystemClock.uptimeMillis();
            app.lastActivityTime = now2;
            if (hasActivity) {
                int N = this.mLruProcesses.size();
                if (N > 0 && this.mLruProcesses.get(N - 1) == app) {
                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                        Slog.d(ActivityManagerService.TAG_LRU, "Not moving, already top activity: " + app);
                        return;
                    }
                    return;
                }
            } else {
                int i = this.mLruProcessServiceStart;
                if (i > 0 && this.mLruProcesses.get(i - 1) == app) {
                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                        Slog.d(ActivityManagerService.TAG_LRU, "Not moving, already top other: " + app);
                        return;
                    }
                    return;
                }
            }
            int lrui = this.mLruProcesses.lastIndexOf(app);
            if (lrui < 0 && app.killedByAm && app.pid == 0) {
                Slog.w(ActivityManagerService.TAG_LRU, "skip " + app + " which has been stopped and removed by am");
            } else if (!app.isPersistent() || lrui < 0) {
                if (lrui >= 0) {
                    int i2 = this.mLruProcessActivityStart;
                    if (lrui < i2) {
                        this.mLruProcessActivityStart = i2 - 1;
                    }
                    int i3 = this.mLruProcessServiceStart;
                    if (lrui < i3) {
                        this.mLruProcessServiceStart = i3 - 1;
                    }
                    this.mLruProcesses.remove(lrui);
                }
                int nextActivityIndex = -1;
                if (hasActivity) {
                    int N2 = this.mLruProcesses.size();
                    nextIndex = this.mLruProcessServiceStart;
                    if (app.hasActivitiesOrRecentTasks() || app.treatLikeActivity || this.mLruProcessActivityStart >= N2 - 1) {
                        if (ActivityManagerDebugConfig.DEBUG_LRU) {
                            Slog.d(ActivityManagerService.TAG_LRU, "Adding to top of LRU activity list: " + app);
                        }
                        this.mLruProcesses.add(app);
                        nextActivityIndex = this.mLruProcesses.size() - 1;
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_LRU) {
                            Slog.d(ActivityManagerService.TAG_LRU, "Adding to second-top of LRU activity list: " + app + " group=" + app.connectionGroup + " importance=" + app.connectionImportance);
                        }
                        int pos = N2 - 1;
                        while (pos > this.mLruProcessActivityStart && this.mLruProcesses.get(pos).info.uid != app.info.uid) {
                            pos--;
                        }
                        this.mLruProcesses.add(pos, app);
                        int i4 = this.mLruProcessActivityStart;
                        if (pos == i4) {
                            this.mLruProcessActivityStart = i4 + 1;
                        }
                        int i5 = this.mLruProcessServiceStart;
                        if (pos == i5) {
                            this.mLruProcessServiceStart = i5 + 1;
                        }
                        int endIndex = pos - 1;
                        if (endIndex < this.mLruProcessActivityStart) {
                            endIndex = this.mLruProcessActivityStart;
                        }
                        nextActivityIndex = endIndex;
                        updateClientActivitiesOrdering(app, pos, this.mLruProcessActivityStart, endIndex);
                    }
                } else {
                    int index = this.mLruProcessServiceStart;
                    if (client != null) {
                        int clientIndex = this.mLruProcesses.lastIndexOf(client);
                        if (ActivityManagerDebugConfig.DEBUG_LRU && clientIndex < 0) {
                            Slog.d(ActivityManagerService.TAG_LRU, "Unknown client " + client + " when updating " + app);
                        }
                        if (clientIndex <= lrui) {
                            clientIndex = lrui;
                        }
                        if (clientIndex >= 0 && index > clientIndex) {
                            index = clientIndex;
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                        Slog.d(ActivityManagerService.TAG_LRU, "Adding at " + index + " of LRU list: " + app);
                    }
                    this.mLruProcesses.add(index, app);
                    int nextIndex2 = index - 1;
                    this.mLruProcessActivityStart++;
                    this.mLruProcessServiceStart++;
                    if (index > 1) {
                        updateClientActivitiesOrdering(app, this.mLruProcessServiceStart - 1, 0, index - 1);
                    }
                    nextIndex = nextIndex2;
                }
                app.lruSeq = this.mLruSeq;
                int nextActivityIndex2 = nextActivityIndex;
                int j2 = app.connections.size() - 1;
                int nextIndex3 = nextIndex;
                while (j2 >= 0) {
                    ConnectionRecord cr = app.connections.valueAt(j2);
                    if (cr.binding == null || cr.serviceDead || cr.binding.service == null || cr.binding.service.app == null || cr.binding.service.app.lruSeq == this.mLruSeq || (cr.flags & 1073742128) != 0) {
                        j = j2;
                        now = now2;
                    } else if (cr.binding.service.app.isPersistent()) {
                        j = j2;
                        now = now2;
                    } else if (!cr.binding.service.app.hasClientActivities()) {
                        j = j2;
                        now = now2;
                        nextIndex3 = updateLruProcessInternalLocked(cr.binding.service.app, now, nextIndex3, this.mLruSeq, "service connection", cr, app);
                    } else if (nextActivityIndex2 >= 0) {
                        j = j2;
                        now = now2;
                        nextActivityIndex2 = updateLruProcessInternalLocked(cr.binding.service.app, now2, nextActivityIndex2, this.mLruSeq, "service connection", cr, app);
                    } else {
                        j = j2;
                        now = now2;
                    }
                    j2 = j - 1;
                    now2 = now;
                }
                for (int j3 = app.conProviders.size() - 1; j3 >= 0; j3--) {
                    ContentProviderRecord cpr = app.conProviders.get(j3).provider;
                    if (cpr.proc != null && cpr.proc.lruSeq != this.mLruSeq && !cpr.proc.isPersistent()) {
                        nextIndex3 = updateLruProcessInternalLocked(cpr.proc, now2, nextIndex3, this.mLruSeq, "provider reference", cpr, app);
                    }
                }
            } else if (ActivityManagerDebugConfig.DEBUG_LRU) {
                Slog.d(ActivityManagerService.TAG_LRU, "Not moving, persistent: " + app);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final ProcessRecord getLRURecordForAppLocked(IApplicationThread thread) {
        IBinder threadBinder = thread.asBinder();
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord rec = this.mLruProcesses.get(i);
            if (rec.thread != null && rec.thread.asBinder() == threadBinder) {
                return rec;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean haveBackgroundProcessLocked() {
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord rec = this.mLruProcesses.get(i);
            if (rec.thread != null && rec.setProcState >= 17) {
                return true;
            }
        }
        return false;
    }

    private static int procStateToImportance(int procState, int memAdj, ActivityManager.RunningAppProcessInfo currApp, int clientTargetSdk) {
        int imp = ActivityManager.RunningAppProcessInfo.procStateToImportanceForTargetSdk(procState, clientTargetSdk);
        if (imp == 400) {
            currApp.lru = memAdj;
        } else {
            currApp.lru = 0;
        }
        return imp;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void fillInProcMemInfoLocked(ProcessRecord app, ActivityManager.RunningAppProcessInfo outInfo, int clientTargetSdk) {
        outInfo.pid = app.pid;
        outInfo.uid = app.info.uid;
        boolean z = true;
        if (this.mService.mAtmInternal.isHeavyWeightProcess(app.getWindowProcessController())) {
            outInfo.flags |= 1;
        }
        if (app.isPersistent()) {
            outInfo.flags |= 2;
        }
        if (app.hasActivities()) {
            outInfo.flags |= 4;
        }
        outInfo.lastTrimLevel = app.trimMemoryLevel;
        outInfo.importance = procStateToImportance(app.getCurProcState(), app.curAdj, outInfo, clientTargetSdk);
        outInfo.importanceReasonCode = app.adjTypeCode;
        outInfo.processState = app.getCurProcState();
        if (app != this.mService.getTopAppLocked()) {
            z = false;
        }
        outInfo.isFocused = z;
        outInfo.lastActivityTime = app.lastActivityTime;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcessesLocked(boolean allUsers, int userId, boolean allUids, int callingUid, int clientTargetSdk) {
        int pid;
        List<ActivityManager.RunningAppProcessInfo> runList = null;
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord app = this.mLruProcesses.get(i);
            if ((allUsers || app.userId == userId) && ((allUids || app.uid == callingUid) && app.thread != null && !app.isCrashing() && !app.isNotResponding())) {
                ActivityManager.RunningAppProcessInfo currApp = new ActivityManager.RunningAppProcessInfo(app.processName, app.pid, app.getPackageList());
                fillInProcMemInfoLocked(app, currApp, clientTargetSdk);
                if (app.adjSource instanceof ProcessRecord) {
                    currApp.importanceReasonPid = ((ProcessRecord) app.adjSource).pid;
                    currApp.importanceReasonImportance = ActivityManager.RunningAppProcessInfo.procStateToImportance(app.adjSourceProcState);
                } else if ((app.adjSource instanceof ActivityServiceConnectionsHolder) && (pid = ((ActivityServiceConnectionsHolder) app.adjSource).getActivityPid()) != -1) {
                    currApp.importanceReasonPid = pid;
                }
                if (app.adjTarget instanceof ComponentName) {
                    currApp.importanceReasonComponent = (ComponentName) app.adjTarget;
                }
                if (runList == null) {
                    runList = new ArrayList<>();
                }
                runList.add(currApp);
            }
        }
        return runList;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public int getLruSizeLocked() {
        return this.mLruProcesses.size();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpLruListHeaderLocked(PrintWriter pw) {
        pw.print("  Process LRU list (sorted by oom_adj, ");
        pw.print(this.mLruProcesses.size());
        pw.print(" total, non-act at ");
        pw.print(this.mLruProcesses.size() - this.mLruProcessActivityStart);
        pw.print(", non-svc at ");
        pw.print(this.mLruProcesses.size() - this.mLruProcessServiceStart);
        pw.println("):");
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public ArrayList<ProcessRecord> collectProcessesLocked(int start, boolean allPkgs, String[] args) {
        if (args == null || args.length <= start || args[start].charAt(0) == '-') {
            return new ArrayList<>(this.mLruProcesses);
        }
        ArrayList<ProcessRecord> procs = new ArrayList<>();
        int pid = -1;
        try {
            pid = Integer.parseInt(args[start]);
        } catch (NumberFormatException e) {
        }
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord proc = this.mLruProcesses.get(i);
            if (proc.pid > 0 && proc.pid == pid) {
                procs.add(proc);
            } else if (allPkgs && proc.pkgList != null && proc.pkgList.containsKey(args[start])) {
                procs.add(proc);
            } else if (proc.processName.equals(args[start])) {
                procs.add(proc);
            }
        }
        if (procs.size() <= 0) {
            return null;
        }
        return procs;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void updateApplicationInfoLocked(List<String> packagesToUpdate, int userId, boolean updateFrameworkRes) {
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord app = this.mLruProcesses.get(i);
            if (app.thread != null && (userId == -1 || app.userId == userId)) {
                int packageCount = app.pkgList.size();
                for (int j = 0; j < packageCount; j++) {
                    String packageName = app.pkgList.keyAt(j);
                    if (updateFrameworkRes || packagesToUpdate.contains(packageName)) {
                        try {
                            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 1024, app.userId);
                            if (ai != null) {
                                app.thread.scheduleApplicationInfoChanged(ai);
                            }
                        } catch (RemoteException e) {
                            Slog.w(TAG, String.format("Failed to update %s ApplicationInfo for %s", packageName, app));
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void sendPackageBroadcastLocked(int cmd, String[] packages, int userId) {
        if (!OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).shouldStopBroadcast(cmd, userId)) {
            boolean foundProcess = false;
            for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord r = this.mLruProcesses.get(i);
                if (r.thread != null && (userId == -1 || r.userId == userId)) {
                    try {
                        for (int index = packages.length - 1; index >= 0 && !foundProcess; index--) {
                            if (packages[index].equals(r.info.packageName)) {
                                foundProcess = true;
                            }
                        }
                        r.thread.dispatchPackageBroadcast(cmd, packages);
                    } catch (RemoteException e) {
                    }
                }
            }
            if (!foundProcess) {
                try {
                    AppGlobals.getPackageManager().notifyPackagesReplacedReceived(packages);
                } catch (RemoteException e2) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public int getUidProcStateLocked(int uid) {
        UidRecord uidRec = this.mActiveUids.get(uid);
        if (uidRec == null) {
            return 21;
        }
        return uidRec.getCurProcState();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public UidRecord getUidRecordLocked(int uid) {
        return this.mActiveUids.get(uid);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void doStopUidForIdleUidsLocked() {
        int size = this.mActiveUids.size();
        for (int i = 0; i < size; i++) {
            if (!UserHandle.isCore(this.mActiveUids.keyAt(i))) {
                UidRecord uidRec = this.mActiveUids.valueAt(i);
                if (uidRec.idle) {
                    this.mService.doStopUidLocked(uidRec.uid, uidRec);
                }
            }
        }
    }
}

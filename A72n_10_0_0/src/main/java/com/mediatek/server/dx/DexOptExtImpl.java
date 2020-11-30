package com.mediatek.server.dx;

import android.app.AppGlobals;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.pm.InstructionSets;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.dex.DexoptOptions;
import com.mediatek.dx.DexOptExt;
import com.mediatek.omadm.PalConstDefs;
import dalvik.system.DexFile;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class DexOptExtImpl extends DexOptExt {
    private static final String COMPILERFILTER_SPEED_PROFILE = "speed-profile";
    private static final int MAX_TRY_COUNTS = 4;
    private static final int MSG_BASE = 10000;
    private static final int MSG_DO_DEXOPT = 10002;
    private static final int MSG_ON_PROCESS_START = 10001;
    private static final String PROPERTY_FEATURE_ENABLE = "pm.dexopt.aggressive_dex2oat.enable";
    private static final String PROPERTY_TRY_INTERVAL = "pm.dexopt.aggressive_dex2oat.interval";
    private static final String TAG = "DexOptExtImpl";
    private static final int TRY_DEX2OAT_INTERVAL_MS = 45000;
    private static Object lock = new Object();
    private static DexOptExtImpl sInstance = null;
    private static boolean sIsEnable;
    private Handler mDexoptExtHandler;
    private HandlerThread mHandlerThread;
    private HashSet<String> mMointorPkgs = new HashSet<>();
    private Object mMointorPkgsLock = new Object();
    private PackageManagerService mPm = null;
    private int mTryDex2oatInterval = TRY_DEX2OAT_INTERVAL_MS;

    static {
        boolean z = true;
        if (!SystemProperties.getBoolean(PROPERTY_FEATURE_ENABLE, true) || Build.IS_ENG) {
            z = false;
        }
        sIsEnable = z;
    }

    private DexOptExtImpl() {
        setTryDex2oatInterval(SystemProperties.getInt(PROPERTY_TRY_INTERVAL, (int) TRY_DEX2OAT_INTERVAL_MS));
        if (isDexOptExtEnable()) {
            initHandlerAndStartHandlerThread();
        }
    }

    private void initHandlerAndStartHandlerThread() {
        this.mHandlerThread = new HandlerThread("DexOptExt");
        this.mHandlerThread.start();
        this.mDexoptExtHandler = new Handler(this.mHandlerThread.getLooper(), new DexOptExtHandler());
    }

    /* access modifiers changed from: package-private */
    public class DexOptExtHandler implements Handler.Callback {
        DexOptExtHandler() {
        }

        public boolean handleMessage(Message msg) {
            String pkg = (String) msg.obj;
            int i = msg.what;
            if (i == DexOptExtImpl.MSG_ON_PROCESS_START) {
                DexOptExtImpl.this.handleProcessStart(pkg);
                return true;
            } else if (i != DexOptExtImpl.MSG_DO_DEXOPT) {
                return true;
            } else {
                DexOptExtImpl.this.handleDoDexopt(msg);
                return true;
            }
        }
    }

    public void setTryDex2oatInterval(int durationMillionSeconds) {
        if (durationMillionSeconds >= 0) {
            this.mTryDex2oatInterval = durationMillionSeconds;
        }
    }

    public static DexOptExtImpl getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    sInstance = new DexOptExtImpl();
                }
            }
        }
        return sInstance;
    }

    public void onStartProcess(String hostingType, String pkg) {
        if (shouldSendProcessStartMessage(hostingType, pkg)) {
            synchronized (this) {
                if (!isInMonitorList(pkg)) {
                    Message msg = Message.obtain();
                    msg.what = MSG_ON_PROCESS_START;
                    msg.obj = pkg;
                    this.mDexoptExtHandler.sendMessage(msg);
                }
            }
        }
    }

    private boolean shouldSendProcessStartMessage(String hostingType, String pkg) {
        if (isDexOptExtEnable() && hostingType != null && hostingType.equals("activity") && getPackageManager() != null && SystemProperties.get("dev.bootcomplete").equals("1")) {
            return true;
        }
        return false;
    }

    private PackageManagerService getPackageManager() {
        PackageManagerService packageManagerService = this.mPm;
        if (packageManagerService != null) {
            return packageManagerService;
        }
        this.mPm = AppGlobals.getPackageManager();
        return this.mPm;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDoDexopt(Message msg) {
        String pkg = (String) msg.obj;
        int result = this.mPm.performDexOptWithStatusByOption(new DexoptOptions(pkg, (int) MSG_BASE, COMPILERFILTER_SPEED_PROFILE, (String) null, 5));
        Slog.d(TAG, "try dex2oat for " + pkg + " result=" + result + " cnt = " + msg.arg1);
        if (result != 0 || msg.arg1 >= MAX_TRY_COUNTS) {
            removeMonitorPkg(pkg);
            return;
        }
        Message againMsg = Message.obtain(msg);
        againMsg.arg1++;
        this.mDexoptExtHandler.sendMessageDelayed(againMsg, (long) (this.mTryDex2oatInterval / 2));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProcessStart(String pkg) {
        if (isDexoptReasonInstall(pkg)) {
            addPkgToMonitor(pkg);
            Message msg = Message.obtain();
            msg.what = MSG_DO_DEXOPT;
            msg.obj = pkg;
            msg.arg1 = 0;
            this.mDexoptExtHandler.sendMessageDelayed(msg, (long) this.mTryDex2oatInterval);
        }
    }

    private boolean isDexoptReasonInstall(String pkg) {
        if (getDexoptReason(pkg).equals("install")) {
            return true;
        }
        return false;
    }

    private String getFirstCodePath(PackageParser.Package pkgParser) {
        List<String> pathList = pkgParser.getAllCodePathsExcludingResourceOnly();
        if (pathList.size() > 0) {
            return pathList.get(0);
        }
        return null;
    }

    private String getFirstCodeIsa(PackageParser.Package pkgParser) {
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkgParser.applicationInfo));
        if (dexCodeInstructionSets.length > 0) {
            return dexCodeInstructionSets[0];
        }
        return null;
    }

    private String getDexoptReason(String pkg) {
        PackageParser.Package pkgParser = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackage(pkg);
        if (pkgParser == null) {
            return PalConstDefs.EMPTY_STRING;
        }
        String path = getFirstCodePath(pkgParser);
        String isa = getFirstCodeIsa(pkgParser);
        if (path == null || isa == null) {
            return PalConstDefs.EMPTY_STRING;
        }
        DexFile.OptimizationInfo info = null;
        try {
            info = DexFile.getDexFileOptimizationInfo(path, isa);
        } catch (IOException e) {
        }
        if (info != null) {
            return info.getReason();
        }
        return PalConstDefs.EMPTY_STRING;
    }

    private void addPkgToMonitor(String pkg) {
        synchronized (this.mMointorPkgsLock) {
            this.mMointorPkgs.add(pkg);
        }
    }

    private boolean isInMonitorList(String pkg) {
        boolean result;
        synchronized (this.mMointorPkgsLock) {
            result = this.mMointorPkgs.contains(pkg);
        }
        return result;
    }

    private void removeMonitorPkg(String pkg) {
        synchronized (this.mMointorPkgsLock) {
            this.mMointorPkgs.remove(pkg);
        }
    }

    public boolean isDexOptExtEnable() {
        return sIsEnable;
    }
}

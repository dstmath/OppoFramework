package com.android.server.am;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.OppoAlarmManagerServiceInternal;
import com.android.server.OppoNetworkManagementInternal;
import com.android.server.am.ColorHansManager;
import com.android.server.am.ColorHansPackageSelector;
import com.android.server.power.OppoPowerManagerInternal;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansManager */
public class HansSceneBase {
    public static final String DEV_FREEZE_PATH = "/dev/freezer/frozen/cgroup.procs";
    public static final String DEV_UNFREEZE_PATH = "/dev/freezer/thaw/cgroup.procs";
    private static final int FIREWALL_CHAIN_HANS = 4;
    private static final String FIREWALL_CHAIN_NAME_HANS = "hans";
    private static final int SIGNAL_FREEZE = 1;
    protected static final int SIGNAL_UNFREEZE = 2;
    protected static final int TYPE_ADD_UID = 1;
    protected static final int TYPE_CONFIG_CHECKING_UID = 3;
    protected static final int TYPE_DELETE_UIDS = 2;
    protected ArrayList<String> logFilterList;
    protected ActivityManagerService mAms;
    protected ColorHansManager.CommonConfig mCommonConfig;
    protected Context mContext;
    protected SparseArray<ColorHansPackageSelector.HansPackage> mFreezedManagedMap;
    protected Object mHansLock;
    protected ColorHansManager.HansLogger mHansLogger;
    protected int mImportantFlag;
    protected Handler mMainHandler;
    protected SparseArray<ColorHansPackageSelector.HansPackage> mManagedMap;
    private INetworkManagementService mNMs;
    protected Handler mNativeHandler;
    private OppoAlarmManagerServiceInternal mOppoAlarmManagerServiceInternal;
    private OppoPowerManagerInternal mOppoLocalPowerManager;
    private OppoNetworkManagementInternal mOppoNetworkManagementInternal;
    protected ColorHansRestriction mRestriction;
    protected ColorHansManager.StateMachineHandler mStateMachineHandler;

    public HansSceneBase() {
        this.mNMs = null;
        this.mOppoNetworkManagementInternal = null;
        this.mOppoLocalPowerManager = null;
        this.mOppoAlarmManagerServiceInternal = null;
        this.mMainHandler = ColorHansManager.getInstance().getMainHandler();
        this.mNativeHandler = ColorHansManager.getInstance().getNativeHandler();
        this.mHansLock = ColorHansManager.getInstance().getHansLock();
        this.mCommonConfig = ColorHansManager.getInstance().getCommonConfig();
        this.mStateMachineHandler = ColorHansManager.getInstance().getStateMachineHandler();
        this.mAms = null;
        this.mHansLogger = null;
        this.mManagedMap = null;
        this.mFreezedManagedMap = null;
        this.mRestriction = null;
        this.mImportantFlag = 0;
        this.mContext = ColorHansManager.getInstance().getContext();
        this.logFilterList = null;
        this.mAms = ColorHansManager.getInstance().getActivityManagerService();
        this.mFreezedManagedMap = new SparseArray<>();
        this.mHansLogger = ColorHansManager.getInstance().getHansLogger();
    }

    public SparseArray<ColorHansPackageSelector.HansPackage> getManagedMap() {
        return this.mManagedMap;
    }

    /* access modifiers changed from: protected */
    public boolean isFreezed(int uid) {
        boolean frozen = false;
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage hansPackage = this.mManagedMap.get(uid);
            if (hansPackage != null) {
                frozen = hansPackage.getFreezed();
            }
        }
        return frozen;
    }

    /* access modifiers changed from: protected */
    public boolean sendHansSignal(ColorHansPackageSelector.HansPackage ps, int signal) {
        if (signal == 1) {
            return writeDevFile(ps, DEV_FREEZE_PATH);
        }
        if (signal == 2) {
            return writeDevFile(ps, DEV_UNFREEZE_PATH);
        }
        this.mHansLogger.i("no matched signal");
        return false;
    }

    private boolean writeDevFile(ColorHansPackageSelector.HansPackage ps, String devPath) {
        ArrayList<Integer> runningPids;
        boolean ret = false;
        File file = new File(devPath);
        FileOutputStream fos = null;
        ArrayList<Integer> frozenList = new ArrayList<>();
        if (!file.exists() || !file.canWrite()) {
            this.mHansLogger.d("file.exists() == false or file.canWrite() == false");
            return false;
        }
        if (DEV_FREEZE_PATH.equals(devPath)) {
            runningPids = getRunningPidsByUid(ps.getUid());
            ArrayList<Integer> isolatedUids = ColorHansManager.getInstance().getIsolatedUids(ps.getUid());
            if (isolatedUids != null) {
                for (int i = 0; i < isolatedUids.size(); i++) {
                    runningPids.addAll(getRunningPidsByUid(isolatedUids.get(i).intValue()));
                }
            }
        } else {
            runningPids = ps.getFrozenPidList();
        }
        try {
            FileOutputStream fos2 = new FileOutputStream(file);
            for (int i2 = runningPids.size() - 1; i2 >= 0; i2--) {
                Integer pid = runningPids.get(i2);
                fos2.write(String.valueOf(pid.intValue()).getBytes("UTF-8"));
                fos2.flush();
                ret = true;
                if (DEV_FREEZE_PATH.equals(devPath) && !frozenList.contains(pid)) {
                    frozenList.add(pid);
                } else if (DEV_UNFREEZE_PATH.equals(devPath)) {
                    ps.getFrozenPidList().remove(pid);
                }
            }
            if (DEV_FREEZE_PATH.equals(devPath)) {
                ps.setFrozenPidList(frozenList);
            }
            try {
                fos2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            ret = false;
            e2.printStackTrace();
            if (DEV_FREEZE_PATH.equals(devPath)) {
                ps.setFrozenPidList(frozenList);
            }
            if (0 != 0) {
                fos.close();
            }
        } catch (Throwable th) {
            if (DEV_FREEZE_PATH.equals(devPath)) {
                ps.setFrozenPidList(frozenList);
            }
            if (0 != 0) {
                try {
                    fos.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
        return ret;
    }

    private ArrayList<Integer> getRunningPidsByUid(int uid) {
        String[] files;
        ArrayList<Integer> pidList = new ArrayList<>();
        String acctPath = "/acct/uid_" + uid;
        File file = new File(acctPath);
        if (file.isDirectory() && (files = file.list()) != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].contains("pid")) {
                    BufferedReader br = null;
                    try {
                        BufferedReader br2 = new BufferedReader(new FileReader(acctPath + "/" + files[i] + "/cgroup.procs"));
                        for (String result = br2.readLine(); result != null; result = br2.readLine()) {
                            Integer pid = Integer.valueOf(result);
                            if (!pidList.contains(pid)) {
                                pidList.add(pid);
                            }
                        }
                        try {
                            br2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        if (0 != 0) {
                            br.close();
                        }
                    } catch (Throwable th) {
                        if (0 != 0) {
                            try {
                                br.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
            }
        }
        return pidList;
    }

    private void checkPidReuse(int uid, ArrayList<Integer> frozenList) {
        ArrayList<Integer> originalList = getRunningPidsByUid(uid);
        for (int i = 0; i < frozenList.size(); i++) {
            boolean flag = false;
            for (int j = 0; j < originalList.size(); j++) {
                if (originalList.get(j).intValue() == frozenList.get(i).intValue()) {
                    flag = true;
                }
            }
            if (!flag) {
                writePidToDevFile(frozenList.get(i).intValue(), DEV_UNFREEZE_PATH);
            }
        }
    }

    private void writePidToDevFile(int pid, String devPath) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(devPath));
            fos.write(String.valueOf(pid).getBytes("UTF-8"));
            fos.flush();
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (fos != null) {
                fos.close();
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void unfreezeForFrozenPids(ColorHansPackageSelector.HansPackage ps) {
        ArrayList<Integer> runningPids = ps.getFrozenPidList();
        if (runningPids != null) {
            for (int j = runningPids.size() - 1; j >= 0; j--) {
                writePidToDevFile(runningPids.get(j).intValue(), DEV_UNFREEZE_PATH);
                runningPids.remove(j);
            }
        }
    }

    public void updateHansUidFirewall(int uid, boolean allow) {
        long token = Binder.clearCallingIdentity();
        if (this.mNMs == null) {
            this.mNMs = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        }
        INetworkManagementService iNetworkManagementService = this.mNMs;
        if (iNetworkManagementService != null) {
            if (allow) {
                try {
                    iNetworkManagementService.setFirewallUidRule(4, uid, 1);
                } catch (Exception e) {
                    ColorHansManager.HansLogger hansLogger = this.mHansLogger;
                    hansLogger.d("Error occured while updateHansUidFirewall: " + e);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } else {
                iNetworkManagementService.setFirewallUidRule(4, uid, 2);
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public void closeSocketsForHansFirewallChain() {
        if (this.mOppoNetworkManagementInternal == null) {
            this.mOppoNetworkManagementInternal = (OppoNetworkManagementInternal) LocalServices.getService(OppoNetworkManagementInternal.class);
        }
        OppoNetworkManagementInternal oppoNetworkManagementInternal = this.mOppoNetworkManagementInternal;
        if (oppoNetworkManagementInternal != null) {
            oppoNetworkManagementInternal.closeSocketsForHans(4, FIREWALL_CHAIN_NAME_HANS);
        }
    }

    /* access modifiers changed from: protected */
    public void unproxyAlarms(int uid) {
        if (this.mOppoAlarmManagerServiceInternal == null) {
            this.mOppoAlarmManagerServiceInternal = (OppoAlarmManagerServiceInternal) LocalServices.getService(OppoAlarmManagerServiceInternal.class);
        }
        OppoAlarmManagerServiceInternal oppoAlarmManagerServiceInternal = this.mOppoAlarmManagerServiceInternal;
        if (oppoAlarmManagerServiceInternal != null) {
            oppoAlarmManagerServiceInternal.unproxyAlarmsForHans(uid, (String) null);
        }
    }

    public void hansTalkWithNative(int uid, int type) {
        if (type == 1) {
            ColorHansManager.getInstance().getHansNativeService().addPacketMonitoredUid(uid);
        } else if (type == 2) {
            ColorHansManager.getInstance().getHansNativeService().deletePacketMonitoredUids();
        } else if (type != 3) {
            this.mHansLogger.i("no matchded type.");
        } else {
            ColorHansManager.getInstance().getHansNativeService().addBinderTransactionUid(uid);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c3, code lost:
        r2 = r10.mOppoLocalPowerManager.restoreJobs(r11, true ^ r10.mCommonConfig.isScreenOn(), r10.mCommonConfig.isCharging(), "hansFreeze");
        r3 = r10.mHansLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00da, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r1 = com.android.server.am.ColorHansManager.getInstance().getHansScene(com.android.server.am.ColorHansManager.getInstance().mCurSceneId).getManagedMap().get(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f3, code lost:
        if (r1 == null) goto L_0x00fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00f5, code lost:
        r1.setJobWakelock(r2 + 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00fa, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        return;
     */
    public void handleWakeLockForHans(int uid, boolean disable) {
        if (this.mOppoLocalPowerManager == null) {
            this.mOppoLocalPowerManager = (OppoPowerManagerInternal) LocalServices.getService(OppoPowerManagerInternal.class);
        }
        OppoPowerManagerInternal oppoPowerManagerInternal = this.mOppoLocalPowerManager;
        if (oppoPowerManagerInternal != null) {
            int num = oppoPowerManagerInternal.setWakeLockStateForHans(uid, disable);
            if (num == 1) {
                ColorHansManager.HansLogger hansLogger = this.mHansLogger;
                StringBuilder sb = new StringBuilder();
                sb.append(disable ? "disable" : "enable");
                sb.append(" app (");
                sb.append(uid);
                sb.append(") wakelock for hans.");
                hansLogger.d(sb.toString());
            } else if (num == 2 || num == 3) {
                unfreeze(uid, ColorHansManager.HANS_UFZ_REASON_WAKELOCK);
            }
            if (disable && num == 2 && this.mOppoLocalPowerManager.pendingJobs(uid, !this.mCommonConfig.isScreenOn(), this.mCommonConfig.isCharging(), "hansFreeze") == 2) {
                synchronized (this.mHansLock) {
                    ColorHansPackageSelector.HansPackage ps = ColorHansManager.getInstance().getHansScene(ColorHansManager.getInstance().mCurSceneId).getManagedMap().get(uid);
                    if (ps != null) {
                        ps.setJobWakelock(1);
                    }
                }
                ColorHansManager.getInstance().getMainHandler().sendMessage(21, uid, 0, 500);
            }
            if (!disable) {
                synchronized (this.mHansLock) {
                    ColorHansPackageSelector.HansPackage ps2 = ColorHansManager.getInstance().getHansScene(ColorHansManager.getInstance().mCurSceneId).getManagedMap().get(uid);
                    if (ps2 != null && ps2.getJobWakelock() != 1) {
                    }
                }
            }
        }
    }

    private ProcessRecord getProcessRecordFromPidLocked(int pid) {
        return this.mAms.mPidsSelfLocked.get(pid);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0057, code lost:
        r5 = r5 + 1;
     */
    public void handleExecutingComponent(int uid) {
        synchronized (this.mAms) {
            synchronized (this.mHansLock) {
                ColorHansPackageSelector.HansPackage ps = this.mManagedMap.get(uid);
                if (ps != null) {
                    ArrayList<Integer> frozenPidList = ps.getFrozenPidList();
                    int i = 0;
                    while (i < frozenPidList.size()) {
                        synchronized (this.mAms.mPidsSelfLocked) {
                            ProcessRecord pr = getProcessRecordFromPidLocked(frozenPidList.get(i).intValue());
                            if (pr != null && (!pr.executingServices.isEmpty() || !pr.curReceivers.isEmpty() || this.mAms.checkAppInLaunchingProvidersLocked(pr))) {
                                unfreeze(uid, ColorHansManager.HANS_UFZ_REASON_EXECUTING_COMPONENT);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateFreezedPkgMap(ColorHansPackageSelector.HansPackage ps, boolean flag) {
        SparseArray<ColorHansPackageSelector.HansPackage> sparseArray = this.mFreezedManagedMap;
        if (sparseArray != null) {
            if (flag) {
                sparseArray.put(ps.getUid(), ps);
            } else {
                sparseArray.remove(ps.getUid());
            }
        }
    }

    public boolean postFreeze(String pkgname, int uid, int level) {
        boolean isMonitoredUid;
        boolean isNeedCloseSocket = false;
        if (level < 3 || ColorHansManager.getInstance().isOnDeviceIdleWhitelist(uid) || ColorHansManager.getInstance().isAllowCpn(uid, 10) || this.mRestriction.isAllowStart(pkgname, uid) || this.mRestriction.isGameApp(pkgname, uid) || ColorHansPackageSelector.getInstance().isInNetPacketWhiteList(pkgname)) {
            isMonitoredUid = true;
        } else {
            isMonitoredUid = false;
            isNeedCloseSocket = true;
            updateHansUidFirewall(uid, false);
        }
        this.mNativeHandler.post(new Runnable(isMonitoredUid, uid) {
            /* class com.android.server.am.$$Lambda$HansSceneBase$N7wJb8ycwkn24N7TeeFseVvI_Y */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                HansSceneBase.this.lambda$postFreeze$0$HansSceneBase(this.f$1, this.f$2);
            }
        });
        this.mMainHandler.post(new Runnable(uid) {
            /* class com.android.server.am.$$Lambda$HansSceneBase$QS8Dydj5C9yprgJtWegNlkgyf0Y */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                HansSceneBase.this.lambda$postFreeze$1$HansSceneBase(this.f$1);
            }
        });
        this.mNativeHandler.postDelayed(new Runnable(uid) {
            /* class com.android.server.am.$$Lambda$HansSceneBase$3GzLh6in1rSYtItbkA4bLj8xLA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                HansSceneBase.this.lambda$postFreeze$2$HansSceneBase(this.f$1);
            }
        }, 2000);
        return isNeedCloseSocket;
    }

    public /* synthetic */ void lambda$postFreeze$0$HansSceneBase(boolean isMonitoredUid, int uid) {
        if (isMonitoredUid) {
            hansTalkWithNative(uid, 1);
        }
    }

    public /* synthetic */ void lambda$postFreeze$1$HansSceneBase(int uid) {
        handleExecutingComponent(uid);
        handleWakeLockForHans(uid, true);
    }

    public /* synthetic */ void lambda$postFreeze$2$HansSceneBase(int uid) {
        hansTalkWithNative(uid, 3);
    }

    /* access modifiers changed from: protected */
    public void freeze(String reason) {
        boolean isNeedCloseSocket = false;
        ArrayList<String> freezedUids = new ArrayList<>();
        ArrayList<String> importantUids = new ArrayList<>();
        DynamicImportantAppList dynamicImportantAppList = new DynamicImportantAppList();
        dynamicImportantAppList.setAudioList(ColorCommonListManager.getInstance().getAudioFocus());
        dynamicImportantAppList.setNavigationList(ColorCommonListManager.getInstance().getNavigationList());
        synchronized (this.mHansLock) {
            Iterator<Integer> it = ColorHansManager.getInstance().getHansRunningList().iterator();
            while (it.hasNext()) {
                int uid = it.next().intValue();
                ColorHansPackageSelector.HansPackage ps = this.mManagedMap.get(uid);
                if (ps != null) {
                    if (!ps.getFreezed()) {
                        if (ColorHansImportance.getInstance().isHansImportantCase(ps, this.mImportantFlag, dynamicImportantAppList)) {
                            ColorHansManager.HansLogger hansLogger = this.mHansLogger;
                            hansLogger.d("isHansImportantCase uid: " + uid + " pkg: " + ps.getPkgName() + " reason: " + ps.getImportantReason());
                            ColorHansManager.getInstance().notifyNotFreezeReason(uid, ps.getPkgName(), ps.getImportantReason());
                            StringBuilder sb = new StringBuilder();
                            sb.append(ps.getImportantReason());
                            sb.append("|");
                            sb.append(ps.getUid());
                            importantUids.add(sb.toString());
                        } else if (sendHansSignal(ps, 1)) {
                            ColorHansManager.HansLogger hansLogger2 = this.mHansLogger;
                            hansLogger2.i("freeze uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + reason + " scene: " + ColorHansManager.getInstance().coverSceneIDtoStr(getScene()));
                            freezedUids.add(String.valueOf(ps.getUid()));
                            ps.setFreezed(true);
                            ps.setFreezeTime(System.currentTimeMillis());
                            ps.setFreezeElapsedTime(SystemClock.elapsedRealtime());
                            ps.setScene(getScene());
                            updateFreezedPkgMap(ps, true);
                            isNeedCloseSocket |= postFreeze(ps.getPkgName(), ps.getUid(), ps.getFreezeLevel());
                        } else {
                            ColorHansManager.HansLogger hansLogger3 = this.mHansLogger;
                            hansLogger3.i("freeze failed uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + reason);
                            unfreezeForFrozenPids(ps);
                            updateFreezedPkgMap(ps, false);
                        }
                    }
                }
            }
        }
        if (isNeedCloseSocket && !this.mCommonConfig.isScreenOn()) {
            closeSocketsForHansFirewallChain();
        }
        if (freezedUids.size() != 0 || importantUids.size() != 0) {
            this.mHansLogger.addAllFZInfo(reason, freezedUids, importantUids);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00d5, code lost:
        if (r1 == false) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00dd, code lost:
        if (r9.mCommonConfig.isScreenOn() != false) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00df, code lost:
        closeSocketsForHansFirewallChain();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00e2, code lost:
        return r6;
     */
    public boolean freeze(int uid, String reason) {
        boolean isNeedCloseSocket = false;
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage ps = this.mManagedMap.get(uid);
            if (ps != null) {
                if (!ps.getFreezed()) {
                    boolean isSuccessful = sendHansSignal(ps, 1);
                    if (isSuccessful) {
                        if (this.logFilterList == null) {
                            ColorHansManager.HansLogger hansLogger = this.mHansLogger;
                            hansLogger.i("freeze uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + reason + " scene: " + ColorHansManager.getInstance().coverSceneIDtoStr(getScene()));
                        }
                        this.mHansLogger.addFZInfo(reason, ps.getPkgName(), ps.getUid());
                        ps.setFreezed(true);
                        ps.setFreezeTime(System.currentTimeMillis());
                        ps.setFreezeElapsedTime(SystemClock.elapsedRealtime());
                        ps.setScene(getScene());
                        updateFreezedPkgMap(ps, true);
                        isNeedCloseSocket = postFreeze(ps.getPkgName(), ps.getUid(), ps.getFreezeLevel());
                    } else {
                        ColorHansManager.HansLogger hansLogger2 = this.mHansLogger;
                        hansLogger2.i("freeze failed uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + reason);
                        unfreezeForFrozenPids(ps);
                        updateFreezedPkgMap(ps, false);
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void postUnFreeze(int uid, String pkgName, String reason) {
        this.mMainHandler.post(new Runnable(uid) {
            /* class com.android.server.am.$$Lambda$HansSceneBase$FN39ihDWPFCahBAKMqKe7v1a40E */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                HansSceneBase.this.lambda$postUnFreeze$3$HansSceneBase(this.f$1);
            }
        });
        this.mMainHandler.post(new Runnable(uid, pkgName) {
            /* class com.android.server.am.$$Lambda$HansSceneBase$vyuSOcQRgElU35tG5jZdQlmR7AI */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                HansSceneBase.this.lambda$postUnFreeze$4$HansSceneBase(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$postUnFreeze$3$HansSceneBase(int uid) {
        handleWakeLockForHans(uid, false);
    }

    public /* synthetic */ void lambda$postUnFreeze$4$HansSceneBase(int uid, String pkgName) {
        unproxyAlarms(uid);
        ColorHansManager.getInstance().getHansBroadcastProxy().unProxyBroadcast(pkgName, UserHandle.getUserId(uid));
    }

    /* access modifiers changed from: protected */
    public boolean unfreeze(int uid, String reason) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage packageState = this.mManagedMap.get(uid);
            if (packageState != null) {
                if (packageState.getFreezed()) {
                    updateHansUidFirewall(uid, true);
                    boolean isSuccessful = sendHansSignal(packageState, 2);
                    if (!isSuccessful) {
                        unfreezeForFrozenPids(packageState);
                        isSuccessful = true;
                    }
                    if (isSuccessful) {
                        ColorHansManager.HansLogger hansLogger = this.mHansLogger;
                        hansLogger.i("unfreeze uid: " + uid + " package: " + packageState.getPkgName() + " reason: " + reason + " scene: " + ColorHansManager.getInstance().coverSceneIDtoStr(getScene()));
                        this.mHansLogger.addUFZInfo(reason, uid, packageState.getPkgName(), packageState.getFreezeTime());
                        ColorHansManager.getInstance().notifyUnFreezeReason(uid, packageState.getPkgName(), reason, ColorHansManager.getInstance().getHansSceneName());
                        ColorHansManager.getInstance().notifyFreezeTime(uid, packageState.getPkgName(), SystemClock.elapsedRealtime() - packageState.getFreezeElapsedTime());
                        packageState.setFreezed(false);
                        packageState.setUnFreezeTime(System.currentTimeMillis());
                        packageState.setUnFreezeReason(reason);
                        updateFreezedPkgMap(packageState, false);
                        postUnFreeze(packageState.getUid(), packageState.getPkgName(), reason);
                        if (this.mCommonConfig != null && this.mCommonConfig.isChinaRegion()) {
                            if (this.mCommonConfig.isScreenOn() || !ColorHansManager.getInstance().getFreqUnFreezeList().contains(reason)) {
                                packageState.clearUnFreezeQueueTime();
                            } else if (packageState.recordUnFreezeQueueTime(SystemClock.elapsedRealtime())) {
                                packageState.clearUnFreezeQueueTime();
                                Bundle data = new Bundle();
                                data.putInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, uid);
                                data.putString("pkg", packageState.getPkgName());
                                ColorHansManager.getInstance().getMainHandler().sendMessage(20, data, 1000);
                            }
                        }
                    }
                    return isSuccessful;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void unfreeze(String reason) {
        ArrayList<String> uids = new ArrayList<>();
        synchronized (this.mHansLock) {
            for (int i = 0; i < this.mManagedMap.size(); i++) {
                ColorHansPackageSelector.HansPackage ps = this.mManagedMap.valueAt(i);
                if (ps != null && ps.getFreezed()) {
                    if (!reason.equals(ColorHansManager.HANS_UFZ_REASON_CHARGING) || !ColorHansManager.getInstance().isPreloadPkg(ps.getPkgName(), UserHandle.getUserId(ps.getUid()))) {
                        updateHansUidFirewall(ps.getUid(), true);
                        boolean isSuccessful = sendHansSignal(ps, 2);
                        if (!isSuccessful) {
                            unfreezeForFrozenPids(ps);
                            isSuccessful = true;
                        }
                        if (isSuccessful) {
                            int uid = ps.getUid();
                            this.mHansLogger.i("unfreeze uid: " + uid + " package: " + ps.getPkgName() + " reason: " + reason + " scene: " + ColorHansManager.getInstance().coverSceneIDtoStr(getScene()));
                            ColorHansManager.getInstance().notifyUnFreezeReason(uid, ps.getPkgName(), reason, ColorHansManager.getInstance().getHansSceneName());
                            ColorHansManager.getInstance().notifyFreezeTime(uid, ps.getPkgName(), SystemClock.elapsedRealtime() - ps.getFreezeElapsedTime());
                            uids.add(String.valueOf(uid));
                            ps.setFreezed(false);
                            ps.setUnFreezeTime(System.currentTimeMillis());
                            ps.setUnFreezeReason(reason);
                            updateFreezedPkgMap(ps, false);
                            postUnFreeze(ps.getUid(), ps.getPkgName(), reason);
                        }
                    }
                }
            }
        }
        if (uids.size() != 0) {
            this.mHansLogger.addAllUFZInfo(reason, uids);
        }
    }

    /* access modifiers changed from: protected */
    public int getScene() {
        return 0;
    }
}

package com.mediatek.server;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkIdentity;
import android.net.NetworkStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.server.net.NetworkStatsFactory;
import com.android.server.net.NetworkStatsObservers;
import com.android.server.net.NetworkStatsService;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.io.File;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;

public class MtkNetworkStatsService extends NetworkStatsService {
    private static final int PHONE_MESSAGE_DELAYED = 10000;
    private static final int PHONE_STATE_CHANGED = 1;
    private static final int REMOVE_PHONE_STATE_LISTENER = 3;
    private static final int SUBSCRIPTION_OR_SIM_CHANGED = 0;
    /* access modifiers changed from: private */
    public static final String TAG = MtkNetworkStatsService.class.getSimpleName();
    private static final int UPDATE_LATENCY_STATS = 2;
    private Context mContext;
    private long mEmGlobalAlert = 2097152;
    /* access modifiers changed from: private */
    public InternalHandler mHandler;
    private HandlerThread mHandlerThread;
    private NetworkStats mLatencyStats;
    private final Object mLatencyStatsLock = new Object();
    private BroadcastReceiver mMobileReceiver = new BroadcastReceiver() {
        /* class com.mediatek.server.MtkNetworkStatsService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("com.mediatek.intent.action.ACTION_BACKGROUND_MOBILE_DATA_USAGE".equals(intent.getAction())) {
                Slog.i(MtkNetworkStatsService.TAG, "mMobileReceiver, update LatencyStats");
                MtkNetworkStatsService.this.mHandler.sendEmptyMessage(2);
            }
        }
    };
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.mediatek.server.MtkNetworkStatsService.AnonymousClass2 */

        public void onSubscriptionsChanged() {
            MtkNetworkStatsService.this.mHandler.sendEmptyMessage(0);
        }
    };
    /* access modifiers changed from: private */
    public int mPhoneState = 0;
    private HashMap<Integer, StatsPhoneStateListener> mPhoneStateListeners = new HashMap<>();
    private volatile NetworkStats mUidVtDataUsage;
    private final Object mXtUidStatsLock = new Object();
    private volatile NetworkStats mXtVtDataUsage;

    public MtkNetworkStatsService(Context context, INetworkManagementService networkManager, AlarmManager alarmManager, PowerManager.WakeLock wakeLock, Clock clock, TelephonyManager teleManager, NetworkStatsService.NetworkStatsSettings settings, NetworkStatsObservers statsObservers, File systemDir, File baseDir) {
        super(context, networkManager, alarmManager, wakeLock, clock, teleManager, settings, statsObservers, systemDir, baseDir);
        Slog.d(TAG, "MtkNetworkStatsService starting up");
        this.mContext = context;
        initDataUsageIntent(context);
    }

    private void initDataUsageIntent(Context context) {
        this.mHandlerThread = new HandlerThread("NetworkStatInternalHandler");
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        SubscriptionManager.from(context).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
    }

    /* access modifiers changed from: protected */
    public void rebuildActiveVilteIfaceMap() {
    }

    /* access modifiers changed from: protected */
    public boolean findOrCreateMultipleVilteNetworkIdentitySets(NetworkIdentity vtIdent) {
        findOrCreateNetworkIdentitySet(this.mActiveIfaces, getVtInterface(vtIdent.getSubscriberId())).add(vtIdent);
        findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, getVtInterface(vtIdent.getSubscriberId())).add(vtIdent);
        return true;
    }

    private String getVtInterface(String subscribeId) {
        return "vt_data0" + subscribeId;
    }

    public void systemReady() {
        MtkNetworkStatsService.super.systemReady();
        this.mContext.registerReceiver(this.mMobileReceiver, new IntentFilter("com.mediatek.intent.action.ACTION_BACKGROUND_MOBILE_DATA_USAGE"), "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
        this.mHandler.sendEmptyMessage(0);
    }

    /* access modifiers changed from: protected */
    public void shutdownLocked() {
        MtkNetworkStatsService.super.shutdownLocked();
        this.mContext.unregisterReceiver(this.mMobileReceiver);
        this.mHandler.sendEmptyMessage(REMOVE_PHONE_STATE_LISTENER);
    }

    /* access modifiers changed from: protected */
    public NetworkStats getNetworkStatsUidDetail(String[] ifaces) throws RemoteException {
        NetworkStats uidSnapshot = this.mNetworkManager.getNetworkStatsUidDetail(-1, ifaces);
        NetworkStats tetherSnapshot = getNetworkStatsTethering(1);
        tetherSnapshot.filter(-1, ifaces, -1);
        NetworkStatsFactory.apply464xlatAdjustments(uidSnapshot, tetherSnapshot, this.mUseBpfTrafficStats);
        uidSnapshot.combineAllValues(tetherSnapshot);
        NetworkStats vtStats = getVtDataUsageInternal(1);
        if (vtStats != null) {
            vtStats.filter(-1, ifaces, -1);
            NetworkStatsFactory.apply464xlatAdjustments(uidSnapshot, vtStats, this.mUseBpfTrafficStats);
            uidSnapshot.combineAllValues(vtStats);
        }
        uidSnapshot.combineAllValues(this.mUidOperations);
        NetworkStats latencyStats = getLatencyStats();
        if (latencyStats != null) {
            latencyStats.filter(-1, ifaces, -1);
            NetworkStatsFactory.apply464xlatAdjustments(uidSnapshot, latencyStats, this.mUseBpfTrafficStats);
            uidSnapshot.combineAllValues(latencyStats);
        }
        uidSnapshot.combineAllValues(this.mUidOperations);
        return uidSnapshot;
    }

    /* access modifiers changed from: protected */
    public NetworkStats getNetworkStatsXt() throws RemoteException {
        NetworkStats xtSnapshot = this.mNetworkManager.getNetworkStatsSummaryXt();
        NetworkStats vtSnapshot = getVtDataUsageInternal(0);
        if (vtSnapshot != null) {
            xtSnapshot.combineAllValues(vtSnapshot);
        }
        NetworkStats latencyStats = getLatencyStats();
        if (latencyStats != null) {
            xtSnapshot.combineAllValues(latencyStats);
        }
        return xtSnapshot;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0024, code lost:
        if (r11 == null) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0026, code lost:
        if (r3 == null) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        r0 = 0;
        r12 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
        if (r0 >= r12.size()) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0030, code lost:
        r9 = r12.getValues(r0, (android.net.NetworkStats.Entry) null);
        r14 = r11.findIndex(r9.iface, r9.uid, r9.set, r9.tag, r9.metered, r9.roaming, r9.defaultNetwork);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0055, code lost:
        if (r14 != -1) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0057, code lost:
        r17 = r11;
        r18 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005c, code lost:
        r15 = r11.getValues(r14, (android.net.NetworkStats.Entry) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0066, code lost:
        if (r9.txBytes >= r15.txBytes) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0068, code lost:
        android.util.Slog.e(com.mediatek.server.MtkNetworkStatsService.TAG, "updateLatencyStats found nagative netstats!iface = " + r9.iface + "entry.txBytes = " + r9.txBytes + "hentry.txBytes = " + r15.txBytes);
        r17 = r11;
        com.mediatek.telephony.MtkTelephonyManagerEx.getDefault().setMobileDataUsageSum(r13, r15.txBytes, r15.txPackets, r15.rxBytes, r15.rxPackets);
        r12 = com.mediatek.telephony.MtkTelephonyManagerEx.getDefault().getMobileDataUsage(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b3, code lost:
        r17 = r11;
        r18 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00b9, code lost:
        r12 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00bb, code lost:
        r0 = r0 + 1;
        r11 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00c1, code lost:
        r4 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00c8, code lost:
        r4 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00cb, code lost:
        android.util.Slog.i(com.mediatek.server.MtkNetworkStatsService.TAG, "updateLatencyStats subId:" + r2 + ", phoneId:" + r13 + ", NetworkStats : " + r4);
        r6 = r22.mLatencyStatsLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00f3, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r22.mLatencyStats = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00f6, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00f7, code lost:
        return;
     */
    public void updateLatencyStats() {
        NetworkStats latencyStats;
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        int phoneId = SubscriptionManager.getPhoneId(subId);
        NetworkStats stats = MtkTelephonyManagerEx.getDefault().getMobileDataUsage(phoneId);
        synchronized (this.mLatencyStatsLock) {
            try {
                if (this.mLatencyStats != null) {
                    latencyStats = this.mLatencyStats.clone();
                } else {
                    latencyStats = null;
                }
                try {
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    private NetworkStats getLatencyStats() {
        NetworkStats latencyStats = null;
        synchronized (this.mLatencyStatsLock) {
            if (this.mLatencyStats != null) {
                latencyStats = this.mLatencyStats.clone();
            }
        }
        return latencyStats;
    }

    /* access modifiers changed from: private */
    public void getVtDataUsageFromTelephony() {
        NetworkStats xtVtDataUsage = new NetworkStats(SystemClock.elapsedRealtime(), 10);
        NetworkStats uidVtDataUsage = new NetworkStats(SystemClock.elapsedRealtime(), 10);
        for (Integer subId : this.mPhoneStateListeners.keySet()) {
            StatsPhoneStateListener listener = this.mPhoneStateListeners.get(subId);
            NetworkStats stats = listener.getTelephonyManager().getVtDataUsage(0);
            if (stats != null) {
                xtVtDataUsage.combineAllValues(stats);
            }
            String str = TAG;
            Slog.d(str, "getVtDataUsage, IFACE, subId = " + subId + ", stas = " + stats);
            NetworkStats stats2 = listener.getTelephonyManager().getVtDataUsage(1);
            if (stats2 != null) {
                uidVtDataUsage.combineAllValues(stats2);
            }
        }
        synchronized (this.mXtUidStatsLock) {
            this.mXtVtDataUsage = xtVtDataUsage;
            this.mUidVtDataUsage = uidVtDataUsage;
        }
    }

    private NetworkStats getVtDataUsageInternal(int how) {
        NetworkStats stats = null;
        synchronized (this.mXtUidStatsLock) {
            if (how == 0) {
                try {
                    if (this.mXtVtDataUsage != null) {
                        stats = this.mXtVtDataUsage.clone();
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else if (how != 1) {
                String str = TAG;
                Slog.e(str, "getVtDataUsageInternal, invailed how = " + how);
            } else if (this.mUidVtDataUsage != null) {
                stats = this.mUidVtDataUsage.clone();
            }
        }
        return stats;
    }

    /* access modifiers changed from: private */
    public void registeAndUpdateStateListener() {
        Slog.d(TAG, "registerStateListener");
        List<SubscriptionInfo> infos = SubscriptionManager.from(this.mContext).getActiveSubscriptionInfoList();
        if (infos != null) {
            removePhoneStateListener();
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
            for (SubscriptionInfo info : infos) {
                int subId = info.getSubscriptionId();
                if (!this.mPhoneStateListeners.containsKey(Integer.valueOf(subId))) {
                    TelephonyManager telephonyManager = tm.createForSubscriptionId(subId);
                    StatsPhoneStateListener listener = new StatsPhoneStateListener(this.mHandlerThread.getLooper(), telephonyManager);
                    this.mPhoneStateListeners.put(Integer.valueOf(subId), listener);
                    telephonyManager.listen(listener, 32);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void removePhoneStateListener() {
        for (Integer id : this.mPhoneStateListeners.keySet()) {
            id.intValue();
            StatsPhoneStateListener listener = this.mPhoneStateListeners.get(id);
            listener.getTelephonyManager().listen(listener, 0);
        }
        this.mPhoneStateListeners.clear();
    }

    private class StatsPhoneStateListener extends PhoneStateListener {
        private TelephonyManager telephonyManager;

        public StatsPhoneStateListener(Looper looper, TelephonyManager telephonyManager2) {
            super(looper);
            this.telephonyManager = telephonyManager2;
        }

        public void onCallStateChanged(int state, String phoneNumber) {
            String access$000 = MtkNetworkStatsService.TAG;
            Slog.i(access$000, "onCallStateChanged state:" + state);
            int unused = MtkNetworkStatsService.this.mPhoneState = state;
            if (MtkNetworkStatsService.this.mPhoneState == 2) {
                MtkNetworkStatsService.this.mHandler.sendEmptyMessageDelayed(1, 10000);
            }
        }

        public TelephonyManager getTelephonyManager() {
            return this.telephonyManager;
        }
    }

    /* access modifiers changed from: private */
    public class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                MtkNetworkStatsService.this.handleSimChange();
                MtkNetworkStatsService.this.registeAndUpdateStateListener();
            } else if (i == 1) {
                MtkNetworkStatsService.this.getVtDataUsageFromTelephony();
                if (MtkNetworkStatsService.this.mPhoneState == 2) {
                    MtkNetworkStatsService.this.mHandler.sendEmptyMessageDelayed(1, 10000);
                }
            } else if (i == 2) {
                MtkNetworkStatsService.this.updateLatencyStats();
            } else if (i == MtkNetworkStatsService.REMOVE_PHONE_STATE_LISTENER) {
                MtkNetworkStatsService.this.removePhoneStateListener();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSimChange() {
        boolean isTestSim = isTestSim();
        this.mEmGlobalAlert = Settings.Global.getLong(this.mContext.getContentResolver(), "netstats_global_alert_bytes", 0);
        if (isTestSim) {
            if (this.mEmGlobalAlert != 2251799813685248L) {
                Settings.Global.putLong(this.mContext.getContentResolver(), "netstats_global_alert_bytes", 2251799813685248L);
                advisePersistThreshold(9223372036854775L);
                Slog.d(TAG, "Configure for test sim with 2TB");
            }
        } else if (this.mEmGlobalAlert == 2251799813685248L) {
            Settings.Global.putLong(this.mContext.getContentResolver(), "netstats_global_alert_bytes", 2097152);
            advisePersistThreshold(9223372036854775L);
            Slog.d(TAG, "Restore for test sim with 2MB");
        }
    }

    public static boolean isTestSim() {
        return SystemProperties.get("vendor.gsm.sim.ril.testsim").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.2").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.3").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.4").equals("1");
    }
}

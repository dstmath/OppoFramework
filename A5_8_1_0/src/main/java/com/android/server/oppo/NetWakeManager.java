package com.android.server.oppo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsService.Stub;
import android.net.INetworkStatsSession;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkTemplate;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.util.Slog;
import com.android.internal.util.ProcFileReader;
import com.android.server.ColorOSDeviceIdleHelper;
import com.android.server.NetworkManagementSocketTagger;
import com.android.server.backup.RefactoredBackupManagerService;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.net.NetworkStatsCollection;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import libcore.io.IoUtils;

public class NetWakeManager {
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR = "android.intent.action.OPPO_GUARD_ELF_MONITOR";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final String FEATURE_OPPO_ADJUST_NETWAKE = "oppo.performance.power.netwake";
    private static Long IDLE_TIME = Long.valueOf(RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL);
    private static final String LATEST_NET_PROC_FILE_PATH = "/proc/net/xt_qtaguid/stats_oppobackup";
    private static final String MODERN_UEVENT_STATUS = "1";
    private static final String NET_PROC_FILE_PATH = "/proc/net/xt_qtaguid/stats";
    private static Long OBSERVE_START_TIME = Long.valueOf(0);
    private static final String TAG = "NetWakeManager";
    private static final String WIFI_UEVENT_STATUS = "0";
    private static HashMap<String, Integer> mWakeupCountMap = new HashMap();
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private NetworkStats mBeforeStats;
    private long mBucketDuration;
    private Context mContext;
    private CoverObserver mCoverObserver;
    private boolean mEnableAdjustNetWake = false;
    private String mNetType;
    private NetworkStatsCollection mPending;
    public boolean mPlugged = false;
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                NetWakeManager.this.mPlugged = intent.getIntExtra("mPlugged", 0) != 0;
                if (NetWakeManager.this.mPlugged) {
                    NetWakeManager.this.updateLocked();
                }
            }
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                NetWakeManager.this.mScreenOn = true;
                NetWakeManager.this.updateLocked();
            } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                NetWakeManager.this.mScreenOn = false;
                NetWakeManager.mWakeupCountMap.clear();
                NetWakeManager.OBSERVE_START_TIME = Long.valueOf(System.currentTimeMillis());
            }
        }
    };
    public boolean mScreenOn = false;
    public ConcurrentHashMap<String, StacOfWakeUId> mStacMap = new ConcurrentHashMap();
    private NetworkStats mWakeStats;

    class CoverObserver extends UEventObserver implements DeathRecipient {
        private final Object mObserverLock = new Object();
        String mPath = "";

        public CoverObserver(String path) {
            this.mPath = path;
        }

        void start() {
            synchronized (this.mObserverLock) {
                startObserving("DEVPATH=" + this.mPath);
                if (NetWakeManager.this.DEBUG) {
                    Slog.d(NetWakeManager.TAG, " Start CoverObserver, the observer path is " + this.mPath);
                }
            }
        }

        void stop() {
            stopObserving();
            if (NetWakeManager.this.DEBUG) {
                Slog.d(NetWakeManager.TAG, " Stop CoverObserver....");
            }
        }

        public void onUEvent(UEvent event) {
            synchronized (this.mObserverLock) {
                Long currentTime = Long.valueOf(System.currentTimeMillis());
                if (NetWakeManager.OBSERVE_START_TIME.longValue() == 0) {
                    NetWakeManager.OBSERVE_START_TIME = currentTime;
                }
                if (currentTime.longValue() - NetWakeManager.OBSERVE_START_TIME.longValue() > NetWakeManager.IDLE_TIME.longValue() && (NetWakeManager.this.mScreenOn ^ 1) != 0 && (NetWakeManager.this.mPlugged ^ 1) != 0 && SystemProperties.getBoolean("sys.opponetwake.enable", true)) {
                    String wakeReason = event.toString();
                    String modern_status = event.get("CHANNEL");
                    if (Integer.parseInt(modern_status) == 0) {
                        NetWakeManager.this.mNetType = "netwake_wifi";
                    } else if (Integer.parseInt(modern_status) == 1) {
                        NetWakeManager.this.mNetType = "netwake_mobile";
                    } else {
                        if (NetWakeManager.this.DEBUG) {
                            Slog.d(NetWakeManager.TAG, "it is wrong connection parameter!");
                        }
                        NetWakeManager.this.mNetType = "wrong";
                    }
                    if (NetWakeManager.this.DEBUG) {
                        Slog.d(NetWakeManager.TAG, "Observing Channel Statu is change to state, UEvent: " + event.toString());
                    }
                    if ("1".equals(modern_status) || NetWakeManager.WIFI_UEVENT_STATUS.equals(modern_status)) {
                        ArrayList<String> uidList = TrafficUtil.getNetUsingList(NetWakeManager.this.mContext, Integer.parseInt(modern_status));
                        if (uidList != null) {
                            for (int i = 0; i < uidList.size(); i++) {
                                NetWakeManager.this.UpdateWakeUid((String) uidList.get(i));
                                if (NetWakeManager.mWakeupCountMap.containsKey(uidList.get(i))) {
                                    NetWakeManager.mWakeupCountMap.put((String) uidList.get(i), Integer.valueOf(((Integer) NetWakeManager.mWakeupCountMap.get(uidList.get(i))).intValue() + 1));
                                } else {
                                    NetWakeManager.mWakeupCountMap.put((String) uidList.get(i), Integer.valueOf(1));
                                }
                            }
                        }
                    }
                } else if (NetWakeManager.this.DEBUG) {
                    Slog.d(NetWakeManager.TAG, "Observer in idle time!! ");
                }
            }
        }

        public void binderDied() {
            stop();
        }
    }

    class StacOfWakeUId {
        public static final String TAG = "StacOfWakeUId";
        public final int LIMIT_COUNT = 5;
        public final long MAX_INTERVAL_TIME = RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL;
        public final long PER_INTERVAL_TIME = ColorOSDeviceIdleHelper.ALARM_WINDOW_LENGTH;
        public int mCount = 0;
        public long mFirstTime;
        public boolean mIsAbnomal = false;
        public boolean mIsDel = false;
        public long mLastTime;
        public String mOwnerUid;
        public long mThisTime;
        public long mWakeInterval;

        public StacOfWakeUId(String uid, long firstime) {
            this.mOwnerUid = uid;
            this.mFirstTime = firstime;
            this.mThisTime = firstime;
            this.mLastTime = firstime;
            this.mCount = 1;
        }

        public String getmOwnerUid() {
            return this.mOwnerUid;
        }

        public void setmOwnerUid(String mOwnerUid) {
            this.mOwnerUid = mOwnerUid;
        }

        public long getmFirstTime() {
            return this.mFirstTime;
        }

        public void setmFirstTime(long mFirstTime) {
            this.mFirstTime = mFirstTime;
        }

        public long getmLastTime() {
            return this.mLastTime;
        }

        public void setmLastTime(long mLastTime) {
            this.mLastTime = mLastTime;
        }

        public int getmCount() {
            return this.mCount;
        }

        public void setmCount() {
            this.mCount++;
        }

        public long getmWakeInterval() {
            if (!(this.mFirstTime == 0 || this.mLastTime == 0 || this.mCount == 0)) {
                this.mWakeInterval = (this.mLastTime - this.mFirstTime) / ((long) this.mCount);
            }
            return this.mWakeInterval;
        }

        public void setmWakeInterval(long mWakeInterval) {
            this.mWakeInterval = mWakeInterval;
        }

        public boolean ismIsAbnomal() {
            if (getmWakeInterval() != 0 && getmCount() >= 5 && getmWakeInterval() < ColorOSDeviceIdleHelper.ALARM_WINDOW_LENGTH) {
                this.mIsAbnomal = true;
                if (NetWakeManager.this.DEBUG) {
                    Slog.d(TAG, "Get the app" + this.mOwnerUid + "is abnormal , interval is " + this.mWakeInterval);
                }
            }
            return this.mIsAbnomal;
        }

        public void setmIsAbnomal(boolean mIsAbnomal) {
            this.mIsAbnomal = mIsAbnomal;
        }

        public long getmThisTime() {
            return this.mThisTime;
        }

        public void setmThisTime(long mThisTime) {
            this.mThisTime = mThisTime;
        }

        public boolean ismIsDel() {
            if (this.mThisTime - this.mLastTime > RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL) {
                this.mIsDel = true;
            } else {
                this.mLastTime = this.mThisTime;
            }
            return this.mIsDel;
        }

        public void setmIsDel(boolean mIsDel) {
            this.mIsDel = mIsDel;
        }

        public long getInterval() {
            return this.mWakeInterval;
        }
    }

    public static class TrafficUtil {
        public static boolean DEBUG = true;
        private static final int NETWORK_DELAY = 1000;
        public static final int NETWORK_MOBILE = 1;
        public static final int NETWORK_WIFI = 0;
        public static final String TAG = "TrafficUtil";
        private static final int THRESH_MOBILE = 61440;
        private static final int THRESH_MOBILE_SPEED = 4;
        private static final int THRESH_WIFI = 153600;
        private static final int THRESH_WIFI_SPEED = 10;
        private static final int UID_SYSTEM = 1000;
        private static NetworkStats mBeforeStats = null;

        public static void clearStats() {
            mBeforeStats = null;
        }

        public static ArrayList<String> getNetUsingList(Context context, int netType) {
            ArrayList<String> changeList = new ArrayList();
            try {
                return getTrafficChangeList(context, NetWakeManager.getUidNetStatsFromProc(new File(NetWakeManager.LATEST_NET_PROC_FILE_PATH)), NetWakeManager.getUidNetStatsFromProc(new File(NetWakeManager.NET_PROC_FILE_PATH)), netType);
            } catch (IOException e) {
                return null;
            }
        }

        public static NetworkStats getUidNetStats(Context context) {
            INetworkStatsService ssService = Stub.asInterface(ServiceManager.getService("netstats"));
            if (ssService == null) {
                return null;
            }
            long endTime = System.currentTimeMillis();
            long startTime = endTime - 1000;
            int type = 0;
            try {
                NetworkTemplate template;
                ssService.forceUpdate();
                INetworkStatsSession mStatsSession = ssService.openSession();
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    State wifiState = cm.getNetworkInfo(1).getState();
                    State mobileState = cm.getNetworkInfo(0).getState();
                    if (wifiState == State.CONNECTED || wifiState == State.CONNECTING) {
                        type = 0;
                    } else if (mobileState == State.CONNECTED || mobileState == State.CONNECTING) {
                        type = 1;
                    }
                }
                if (type == 0) {
                    template = NetworkTemplate.buildTemplateWifi();
                } else {
                    template = NetworkTemplate.buildTemplateMobileWildcard();
                }
                NetworkStats stats = null;
                if (mStatsSession != null) {
                    try {
                        stats = mStatsSession.getSummaryForAllUid(template, startTime, endTime, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (stats != null) {
                    int size = stats.size();
                    for (int i = 0; i < size; i++) {
                        Entry entry = stats.getValues(i, null);
                        int uid = entry.uid;
                        long data = entry.rxBytes + entry.txBytes;
                    }
                }
                return stats;
            } catch (RemoteException e2) {
                throw new RuntimeException(e2);
            }
        }

        public static ArrayList<String> getTrafficChangeList(Context context, NetworkStats stats1, NetworkStats stats2, int type) {
            if (stats1 == null || stats2 == null) {
                return null;
            }
            int thresh;
            int i;
            Entry entry;
            int uid;
            long data;
            int uid1;
            int j;
            ArrayList<String> tempList = new ArrayList();
            ArrayList<Integer> uidList1 = new ArrayList();
            ArrayList<Long> dataList1 = new ArrayList();
            ArrayList<Integer> uidList2 = new ArrayList();
            ArrayList<Long> dataList2 = new ArrayList();
            PackageManager pm = context.getPackageManager();
            if (type == 1) {
                thresh = THRESH_MOBILE;
            } else if (type == 0) {
                thresh = THRESH_WIFI;
            } else {
                if (DEBUG) {
                    Slog.d(TAG, "wrong connection parameter!");
                }
                return null;
            }
            int size1 = stats1.size();
            int size2 = stats2.size();
            for (i = 0; i < size1; i++) {
                entry = stats1.getValues(i, null);
                uid = entry.uid;
                data = entry.rxBytes;
                uidList1.add(Integer.valueOf(uid));
                dataList1.add(Long.valueOf(data));
            }
            for (i = 0; i < uidList1.size() - 1; i++) {
                uid1 = ((Integer) uidList1.get(i)).intValue();
                j = i + 1;
                while (j < uidList1.size()) {
                    if (uid1 == ((Integer) uidList1.get(j)).intValue()) {
                        dataList1.set(i, Long.valueOf(((Long) dataList1.get(i)).longValue() + ((Long) dataList1.get(j)).longValue()));
                        uidList1.remove(j);
                        dataList1.remove(j);
                        j--;
                    }
                    j++;
                }
            }
            for (i = 0; i < size2; i++) {
                entry = stats2.getValues(i, null);
                uid = entry.uid;
                data = entry.rxBytes;
                uidList2.add(Integer.valueOf(uid));
                dataList2.add(Long.valueOf(data));
            }
            for (i = 0; i < uidList2.size() - 1; i++) {
                uid1 = ((Integer) uidList2.get(i)).intValue();
                j = i + 1;
                while (j < uidList2.size()) {
                    if (uid1 == ((Integer) uidList2.get(j)).intValue()) {
                        dataList2.set(i, Long.valueOf(((Long) dataList2.get(i)).longValue() + ((Long) dataList2.get(j)).longValue()));
                        uidList2.remove(j);
                        dataList2.remove(j);
                        j--;
                    }
                    j++;
                }
            }
            size1 = uidList1.size();
            size2 = uidList2.size();
            for (i = 0; i < size1; i++) {
                uid1 = ((Integer) uidList1.get(i)).intValue();
                long data1 = ((Long) dataList1.get(i)).longValue();
                if (uid1 > 0 && uid1 != 1000) {
                    j = 0;
                    while (j < size2) {
                        if (uid1 == ((Integer) uidList2.get(j)).intValue()) {
                            long diff = ((Long) dataList2.get(j)).longValue() - data1;
                            if (((long) thresh) > diff && diff > 0) {
                                String[] packageName = pm.getPackagesForUid(uid1);
                                if (DEBUG) {
                                    Slog.d(TAG, "Using network!  uid: " + uid1 + " data:(15s) " + diff + "bit" + ", packageName = " + pm.getNameForUid(uid1));
                                }
                                if (packageName != null) {
                                    for (String add : packageName) {
                                        tempList.add(add);
                                    }
                                }
                            } else if (((long) thresh) < diff && DEBUG) {
                                Slog.d(TAG, "Using network downLoad:  uid: " + uid1 + " data:(15s) " + diff + "bit" + ", packageName = " + pm.getNameForUid(uid1));
                            }
                        } else {
                            j++;
                        }
                    }
                }
            }
            return tempList;
        }
    }

    public NetWakeManager(Context context) {
        boolean z = false;
        this.mContext = context;
        if (this.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_ADJUST_NETWAKE)) {
            z = SystemProperties.getBoolean("sys.opponetwake.enable", true);
        }
        this.mEnableAdjustNetWake = z;
    }

    public void CoverObservse_init() {
        if (this.mEnableAdjustNetWake) {
            this.mCoverObserver = new CoverObserver("/kernel/wakeup_reason_uevent");
            this.mCoverObserver.start();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_CHANGED");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mReceiver, filter);
        }
    }

    public void updateLocked() {
        this.mStacMap.clear();
        OBSERVE_START_TIME = Long.valueOf(0);
        TrafficUtil.clearStats();
        if (this.DEBUG) {
            Slog.d(TAG, "Becuase of :mScreenOn = " + this.mScreenOn + ",  mPlugged = " + this.mPlugged + "; System is wakeup, so Hashmap(mStacMap), OBSERVE_START_TIME and TrafficUtil.clearStats is clean all ! ");
        }
    }

    private static NetworkStats getUidNetStatsFromProc(File detailPath) throws IOException {
        NullPointerException e;
        NumberFormatException e2;
        Throwable th;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        if (detailPath.exists()) {
            NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
            Entry entry = new Entry();
            int idx = 1;
            int lastIdx = 1;
            AutoCloseable reader = null;
            try {
                ProcFileReader reader2 = new ProcFileReader(new FileInputStream(detailPath));
                try {
                    reader2.finishLine();
                    while (reader2.hasMoreData()) {
                        idx = reader2.nextInt();
                        if (idx != lastIdx + 1) {
                            throw new ProtocolException("inconsistent idx=" + idx + " after lastIdx=" + lastIdx);
                        }
                        lastIdx = idx;
                        entry.iface = reader2.nextString();
                        entry.tag = NetworkManagementSocketTagger.kernelToTag(reader2.nextString());
                        entry.uid = reader2.nextInt();
                        entry.set = reader2.nextInt();
                        entry.rxBytes = reader2.nextLong();
                        entry.rxPackets = reader2.nextLong();
                        entry.txBytes = reader2.nextLong();
                        entry.txPackets = reader2.nextLong();
                        stats.addValues(entry);
                        reader2.finishLine();
                    }
                    IoUtils.closeQuietly(reader2);
                    StrictMode.setThreadPolicy(savedPolicy);
                    return stats;
                } catch (NullPointerException e3) {
                    e = e3;
                    reader = reader2;
                } catch (NumberFormatException e4) {
                    e2 = e4;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    Object reader3 = reader2;
                }
            } catch (NullPointerException e5) {
                e = e5;
                try {
                    throw new ProtocolException("problem parsing idx " + idx, e);
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(reader);
                    StrictMode.setThreadPolicy(savedPolicy);
                    throw th;
                }
            } catch (NumberFormatException e6) {
                e2 = e6;
                throw new ProtocolException("problem parsing idx " + idx, e2);
            }
        }
        Slog.d(TAG, detailPath + " not exists");
        return null;
    }

    public static String getMaxWakeupApp() {
        String maxWakeupApp = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : mWakeupCountMap.entrySet()) {
            String key = (String) entry.getKey();
            int val = ((Integer) entry.getValue()).intValue();
            Slog.d(TAG, "getMaxWakeupApp, APP = " + key + "; wakeup count = " + val);
            if (val > maxCount) {
                maxWakeupApp = key;
                maxCount = val;
            }
        }
        return maxWakeupApp;
    }

    public void UpdateWakeUid(String uid) {
        long time = System.currentTimeMillis() - FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK;
        if (this.mStacMap != null) {
            if (this.mStacMap.containsKey(uid)) {
                StacOfWakeUId old = (StacOfWakeUId) this.mStacMap.get(uid);
                old.setmThisTime(time);
                Iterator<String> iterator;
                String key;
                if (old.ismIsDel()) {
                    iterator = this.mStacMap.keySet().iterator();
                    while (iterator.hasNext()) {
                        key = (String) iterator.next();
                        if (uid.equals(key)) {
                            iterator.remove();
                            this.mStacMap.remove(key);
                            if (this.DEBUG) {
                                Slog.d(TAG, "last Wakeup time too far, so del : " + uid);
                            }
                        }
                    }
                } else {
                    old.setmCount();
                    if (old.ismIsAbnomal()) {
                        Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                        ArrayList<String> uidList = new ArrayList();
                        uidList.add(getReportString(uid, old.getInterval()));
                        intent.putStringArrayListExtra("data", uidList);
                        intent.putExtra(SoundModelContract.KEY_TYPE, this.mNetType);
                        this.mContext.sendBroadcast(intent);
                        Intent intentMonitor = new Intent(ACTION_OPPO_GUARD_ELF_MONITOR);
                        intent.putStringArrayListExtra("data", uidList);
                        intent.putExtra(SoundModelContract.KEY_TYPE, "netwake_serious");
                        this.mContext.sendBroadcast(intentMonitor);
                        iterator = this.mStacMap.keySet().iterator();
                        while (iterator.hasNext()) {
                            key = (String) iterator.next();
                            if (uid.equals(key)) {
                                iterator.remove();
                                this.mStacMap.remove(key);
                                if (this.DEBUG) {
                                    Slog.d(TAG, "Wake up too many times , kill it and  delete stacofwakeUId: " + uid);
                                }
                            }
                        }
                    }
                }
            } else {
                StacOfWakeUId newUid = new StacOfWakeUId(uid, time);
                if (this.DEBUG) {
                    Slog.d(TAG, " add new stacofwakeUId: " + uid);
                }
                this.mStacMap.putIfAbsent(uid, newUid);
            }
        }
    }

    private String getReportString(String packageName, long cycle) {
        String str = "";
        StringBuilder sb = new StringBuilder();
        sb.append("[ ").append(packageName).append(" ]    ").append("  cycle : ").append(cycle);
        return sb.toString();
    }
}

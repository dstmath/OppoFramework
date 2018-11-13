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
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.util.Slog;
import com.android.server.ColorOSDeviceIdleHelper;
import com.android.server.net.NetworkStatsCollection;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class NetWakeManager {
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR = "android.intent.action.OPPO_GUARD_ELF_MONITOR";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final String FEATURE_OPPO_ADJUST_NETWAKE = "oppo.performance.power.netwake";
    private static Long IDLE_TIME = null;
    private static final String MODERN_UEVENT_STATUS = "20";
    private static Long OBSERVE_START_TIME = null;
    private static final String TAG = "NetWakeManager";
    private static final String UEVENT_PATH = "/kernel/wakeup_reason_uevent";
    private static final String WIFI_UEVENT_STATUS = "10";
    private boolean DEBUG;
    private NetworkStats mBeforeStats;
    private long mBucketDuration;
    private Context mContext;
    private CoverObserver mCoverObserver;
    private boolean mEnableAdjustNetWake;
    private NetworkStatsCollection mPending;
    public boolean mPlugged;
    public final BroadcastReceiver mReceiver;
    public boolean mScreenOn;
    public ConcurrentHashMap<String, StacOfWakeUId> mStacMap;
    private NetworkStats mWakeStats;

    class CoverObserver extends UEventObserver implements DeathRecipient {
        private final Object mObserverLock = new Object();
        String mPath = IElsaManager.EMPTY_PACKAGE;

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
                if (currentTime.longValue() - NetWakeManager.OBSERVE_START_TIME.longValue() > NetWakeManager.IDLE_TIME.longValue() && !NetWakeManager.this.mScreenOn) {
                    if (!NetWakeManager.this.mPlugged) {
                        String wakeReason = event.toString();
                        String modern_status = event.get("CHANNEL");
                        if (NetWakeManager.this.DEBUG) {
                            Slog.d(NetWakeManager.TAG, "Observing Channel Statu is change to state, UEvent: " + event.toString());
                        }
                        if (NetWakeManager.MODERN_UEVENT_STATUS.equals(modern_status)) {
                            ArrayList<String> uidList = TrafficUtil.getNetUsingList(NetWakeManager.this.mContext);
                            if (uidList != null) {
                                for (int i = 0; i < uidList.size(); i++) {
                                    NetWakeManager.this.UpdateWakeUid((String) uidList.get(i));
                                }
                            }
                        }
                    }
                }
                if (NetWakeManager.this.DEBUG) {
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
        public final long MAX_INTERVAL_TIME = 300000;
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
            if (this.mThisTime - this.mLastTime > 300000) {
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
        public static boolean DEBUG = false;
        private static final int NETWORK_DELAY = 1000;
        private static final int NETWORK_MOBILE = 1;
        private static final int NETWORK_WIFI = 0;
        public static final String TAG = "TrafficUtil";
        private static final int THRESH_MOBILE = 46080;
        private static final int THRESH_MOBILE_SPEED = 3;
        private static final int THRESH_WIFI = 153600;
        private static final int THRESH_WIFI_SPEED = 10;
        private static final int UID_SYSTEM = 1000;
        private static NetworkStats mBeforeStats;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.NetWakeManager.TrafficUtil.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.NetWakeManager.TrafficUtil.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.NetWakeManager.TrafficUtil.<clinit>():void");
        }

        public static void clearStats() {
            mBeforeStats = null;
        }

        public static ArrayList<String> getNetUsingList(Context context) {
            ArrayList<String> changeList = new ArrayList();
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                State mobileState = cm.getNetworkInfo(0).getState();
                State wifiState = cm.getNetworkInfo(1).getState();
                NetworkStats stats1;
                NetworkStats stats2;
                if (mobileState == State.CONNECTED || mobileState == State.CONNECTING) {
                    if (DEBUG) {
                        Slog.d(TAG, " mobile connected! So getNetUsingList : state 1------  ");
                    }
                    if (mBeforeStats == null) {
                        stats1 = getUidNetStats(context);
                        if (stats1 == null) {
                            return null;
                        }
                    }
                    if (DEBUG) {
                        Slog.d(TAG, "getNetUsingList : ---------------- state is equal before ------  ");
                    }
                    stats1 = mBeforeStats;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    if (DEBUG) {
                        Slog.d(TAG, "getNetUsingList : state 2------  ");
                    }
                    stats2 = getUidNetStats(context);
                    if (stats2 == null) {
                        return null;
                    }
                    mBeforeStats = stats2;
                    changeList = getTrafficChangeList(context, stats1, stats2, 1);
                } else if (wifiState == State.CONNECTED || wifiState == State.CONNECTING) {
                    if (DEBUG) {
                        Slog.d(TAG, " wifi connected! So getNetUsingList : state 1------  ");
                    }
                    stats1 = null;
                    if (mBeforeStats == null) {
                        stats1 = getUidNetStats(context);
                        if (stats1 == null) {
                            return null;
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                    if (DEBUG) {
                        Slog.d(TAG, "getNetUsingList : state 2------  ");
                    }
                    stats2 = getUidNetStats(context);
                    if (stats2 == null) {
                        return null;
                    }
                    changeList = getTrafficChangeList(context, stats1, stats2, 0);
                } else {
                    changeList = null;
                }
            }
            return changeList;
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.NetWakeManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.NetWakeManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.NetWakeManager.<clinit>():void");
    }

    public NetWakeManager(Context context) {
        this.DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mEnableAdjustNetWake = false;
        this.mPlugged = false;
        this.mScreenOn = false;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    boolean z;
                    NetWakeManager netWakeManager = NetWakeManager.this;
                    if (intent.getIntExtra("mPlugged", 0) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    netWakeManager.mPlugged = z;
                    if (NetWakeManager.this.mPlugged) {
                        NetWakeManager.this.updateLocked();
                    }
                }
                if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    NetWakeManager.this.mScreenOn = true;
                    NetWakeManager.this.updateLocked();
                } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    NetWakeManager.this.mScreenOn = false;
                }
                if (!NetWakeManager.this.mScreenOn && !NetWakeManager.this.mPlugged) {
                    NetWakeManager.OBSERVE_START_TIME = Long.valueOf(System.currentTimeMillis());
                }
            }
        };
        this.mStacMap = new ConcurrentHashMap();
        this.mContext = context;
        this.mEnableAdjustNetWake = this.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_ADJUST_NETWAKE);
    }

    public void CoverObservse_init() {
        if (this.mEnableAdjustNetWake) {
            this.mCoverObserver = new CoverObserver(UEVENT_PATH);
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

    public void UpdateWakeUid(String uid) {
        long time = System.currentTimeMillis() - 5000;
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
                        intent.putExtra(SoundModelContract.KEY_TYPE, "netwake_serious");
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
        String str = IElsaManager.EMPTY_PACKAGE;
        StringBuilder sb = new StringBuilder();
        sb.append("[ ").append(packageName).append(" ]    ").append("  cycle : ").append(cycle);
        return sb.toString();
    }
}

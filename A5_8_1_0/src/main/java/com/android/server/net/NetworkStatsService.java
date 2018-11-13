package com.android.server.net;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DataUsageRequest;
import android.net.IConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkStatsService.Stub;
import android.net.INetworkStatsSession;
import android.net.LinkProperties;
import android.net.NetworkIdentity;
import android.net.NetworkState;
import android.net.NetworkStats;
import android.net.NetworkStats.NonMonotonicObserver;
import android.net.NetworkStatsHistory;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Log;
import android.util.MathUtils;
import android.util.NtpTrustedTime;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.TrustedTime;
import android.util.Xml;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.net.VpnInfo;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FileRotator;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.EventLogTags;
import com.android.server.NetworkManagementService;
import com.android.server.NetworkManagementSocketTagger;
import com.android.server.content.SyncStorageEngine;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usage.UnixCalendar;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class NetworkStatsService extends Stub {
    private static final String ACTION_MULTI_APP_REMOVED = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED";
    public static final String ACTION_NETWORK_STATS_POLL = "com.android.server.action.NETWORK_STATS_POLL";
    public static final String ACTION_NETWORK_STATS_UPDATED = "com.android.server.action.NETWORK_STATS_UPDATED";
    private static boolean DEBUG_CONFIG_LOAD = false;
    private static final int FLAG_PERSIST_ALL = 3;
    private static final int FLAG_PERSIST_FORCE = 256;
    private static final int FLAG_PERSIST_NETWORK = 1;
    private static final int FLAG_PERSIST_UID = 2;
    private static final int LOCAL_PER_USER_RANGE = 100000;
    static final boolean LOGV = false;
    static boolean LOG_NET_STATS = false;
    static boolean LOG_NET_STATS_PROC = false;
    private static final int MSG_PERFORM_POLL = 1;
    private static final int MSG_REGISTER_GLOBAL_ALERT = 3;
    private static final int MSG_REMOVE_MULTI_APP_UID = 6;
    private static final int MSG_RESPONSE_POLL_BROADCAST = 4;
    private static final int MSG_SET_UID_FOREGROUND = 5;
    private static final int MSG_UPDATE_IFACES = 2;
    private static final int OPPO_MULTI_APP_UID_PREFIX = 999;
    private static final String PREFIX_DEV = "dev";
    private static final String PREFIX_PID = "pid";
    private static final String PREFIX_UID = "uid";
    private static final String PREFIX_UID_TAG = "uid_tag";
    private static final String PREFIX_XT = "xt";
    private static final String PROP_DEBUG_UID_RECORD_DETAIL = "persist.sys.assert.netstats";
    private static final String SHARE_UID_FOR_NET_CONFIG_FILE = "/system/etc/oppo_shareuid_for_net.xml";
    private static final boolean SUPPORT_NET_STATS_PID = false;
    static final String TAG = "NetworkStats";
    private static final String TAG_NETSTATS_ERROR = "netstats_error";
    private static final String TAG_OPPO_SHAREUID = "oppo-shareuid";
    private static final String TAG_SHARE_UID_FOR_NET = "ShareUidForNet";
    public static final String VT_INTERFACE = "vt_data0";
    private String mActiveIface;
    private final ArrayMap<String, NetworkIdentitySet> mActiveIfaces = new ArrayMap();
    private boolean mActiveIfacesHasInit = false;
    private SparseIntArray mActiveUidCounterSet = new SparseIntArray();
    private final ArrayMap<String, NetworkIdentitySet> mActiveUidIfaces = new ArrayMap();
    private final AlarmManager mAlarmManager;
    private INetworkManagementEventObserver mAlertObserver = new BaseNetworkObserver() {
        public void limitReached(String limitName, String iface) {
            NetworkStatsService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkStatsService.TAG);
            if (NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                NetworkStatsService.this.mHandler.obtainMessage(1, 1, 0).sendToTarget();
                NetworkStatsService.this.mHandler.obtainMessage(3).sendToTarget();
            }
        }
    };
    private final File mBaseDir;
    private IConnectivityManager mConnManager;
    private final Context mContext;
    private boolean mDebugUidRecordDetail = false;
    @GuardedBy("mStatsLock")
    private NetworkStatsRecorder mDevRecorder;
    private long mGlobalAlertBytes;
    private final ContentObserver mGlobalAlertBytesObserver = new ContentObserver(this.mStatsHandler) {
        public void onChange(boolean selfChange) {
            long globalAlertBytes = NetworkStatsService.this.mSettings.getGlobalAlertBytes(NetworkStatsService.this.mPersistThreshold);
            if (globalAlertBytes > 0) {
                NetworkStatsService.this.mGlobalAlertBytes = globalAlertBytes;
            } else {
                NetworkStatsService.this.mGlobalAlertBytes = NetworkStatsService.this.mPersistThreshold;
            }
        }
    };
    private Handler mHandler;
    private Callback mHandlerCallback;
    private String[] mMobileIfaces = new String[0];
    private BroadcastReceiver mMultiAppPkgActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (NetworkStatsService.LOG_NET_STATS) {
                Slog.d(NetworkStatsService.TAG, "mMultiAppPkgActionReceiver intent:" + intent);
            }
            if (intent != null) {
                String action = intent.getAction();
                if (NetworkStatsService.LOG_NET_STATS) {
                    Slog.d(NetworkStatsService.TAG, "mMultiAppPkgActionReceiver action:" + action);
                }
                if (NetworkStatsService.ACTION_MULTI_APP_REMOVED.equals(action)) {
                    int deleteUid = intent.getIntExtra("android.intent.extra.UID", -1);
                    if (deleteUid == -1) {
                        Slog.w(NetworkStatsService.TAG, "ACTION_MULTI_APP_REMOVED deleteUid -1.");
                        return;
                    }
                    String pkgName = "";
                    if (intent.getData() != null) {
                        pkgName = intent.getData().getSchemeSpecificPart();
                    }
                    int multiAppUid = NetworkStatsService.getUid(999, deleteUid);
                    Slog.i(NetworkStatsService.TAG, "ACTION_MULTI_APP_REMOVED, deleteUid:" + deleteUid + ", pkgName:" + pkgName + ", multiAppUid:" + multiAppUid);
                    NetworkStatsService.this.mHandler.sendMessage(NetworkStatsService.this.mHandler.obtainMessage(6, multiAppUid, 0));
                }
            }
        }
    };
    private final INetworkManagementService mNetworkManager;
    private NetworkStats mNetworkStats = null;
    private final DropBoxNonMonotonicObserver mNonMonotonicObserver = new DropBoxNonMonotonicObserver(this, null);
    private OppoRefreshTimeTask mOppoRefreshTimeTask = null;
    private long mPersistThreshold = 2097152;
    private NetworkStatsRecorderWithProcInfo mPidRecorder;
    private NetworkStatsRecorderWithProcInfo mPidTagRecorder;
    private PendingIntent mPollIntent;
    private BroadcastReceiver mPollReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkStatsService.this.processPollBroadcast();
        }
    };
    private Object mRefreshTimeLocker = new Object();
    private BroadcastReceiver mRemovedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("android.intent.extra.UID", -1) != -1) {
                synchronized (NetworkStatsService.this.mStatsLock) {
                    NetworkStatsService.this.mWakeLock.acquire();
                    try {
                        NetworkStatsService.this.removeUidsLocked(uid);
                        NetworkStatsService.this.mWakeLock.release();
                    } catch (Throwable th) {
                        NetworkStatsService.this.mWakeLock.release();
                    }
                }
            }
        }
    };
    private final NetworkStatsSettings mSettings;
    private BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            synchronized (NetworkStatsService.this.mStatsLock) {
                NetworkStatsService.this.shutdownLocked();
            }
        }
    };
    private Handler mStatsHandler = null;
    private final Object mStatsLock = new Object();
    private final NetworkStatsObservers mStatsObservers;
    private final File mSystemDir;
    private boolean mSystemReady;
    private final TelephonyManager mTeleManager;
    private BroadcastReceiver mTetherReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (NetworkStatsService.LOG_NET_STATS) {
                Slog.d(NetworkStatsService.TAG, "mTetherReceiver receive ACTION_TETHER_STATE_CHANGED broadcast.");
            }
            NetworkStatsService.this.performPoll(1);
        }
    };
    private final TrustedTime mTime;
    private NetworkStats mUidOperations = new NetworkStats(0, 10);
    @GuardedBy("mStatsLock")
    private NetworkStatsRecorder mUidRecorder;
    @GuardedBy("mStatsLock")
    private NetworkStatsRecorder mUidTagRecorder;
    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            if (userId != -1) {
                synchronized (NetworkStatsService.this.mStatsLock) {
                    NetworkStatsService.this.mWakeLock.acquire();
                    try {
                        NetworkStatsService.this.removeUserLocked(userId);
                        NetworkStatsService.this.mWakeLock.release();
                    } catch (Throwable th) {
                        NetworkStatsService.this.mWakeLock.release();
                    }
                }
            }
        }
    };
    private final WakeLock mWakeLock;
    @GuardedBy("mStatsLock")
    private NetworkStatsRecorder mXtRecorder;
    @GuardedBy("mStatsLock")
    private NetworkStatsCollection mXtStatsCached;

    public interface NetworkStatsSettings {

        public static class Config {
            public final long bucketDuration;
            public final long deleteAgeMillis;
            public final long rotateAgeMillis;

            public Config(long bucketDuration, long rotateAgeMillis, long deleteAgeMillis) {
                this.bucketDuration = bucketDuration;
                this.rotateAgeMillis = rotateAgeMillis;
                this.deleteAgeMillis = deleteAgeMillis;
            }
        }

        boolean getAugmentEnabled();

        Config getDevConfig();

        long getDevPersistBytes(long j);

        long getGlobalAlertBytes(long j);

        Config getPidConfig();

        long getPidPersistBytes(long j);

        Config getPidTagConfig();

        long getPidTagPersistBytes(long j);

        long getPollInterval();

        boolean getSampleEnabled();

        long getTimeCacheMaxAge();

        Config getUidConfig();

        long getUidPersistBytes(long j);

        Config getUidTagConfig();

        long getUidTagPersistBytes(long j);

        Config getXtConfig();

        long getXtPersistBytes(long j);
    }

    private static class DefaultNetworkStatsSettings implements NetworkStatsSettings {
        private final ContentResolver mResolver;

        public DefaultNetworkStatsSettings(Context context) {
            this.mResolver = (ContentResolver) Preconditions.checkNotNull(context.getContentResolver());
        }

        private long getGlobalLong(String name, long def) {
            return Global.getLong(this.mResolver, name, def);
        }

        private boolean getGlobalBoolean(String name, boolean def) {
            if (Global.getInt(this.mResolver, name, def ? 1 : 0) != 0) {
                return true;
            }
            return false;
        }

        public long getPollInterval() {
            return getGlobalLong("netstats_poll_interval", 1800000);
        }

        public long getTimeCacheMaxAge() {
            return getGlobalLong("netstats_time_cache_max_age", UnixCalendar.DAY_IN_MILLIS);
        }

        public long getGlobalAlertBytes(long def) {
            return getGlobalLong("netstats_global_alert_bytes", def);
        }

        public boolean getSampleEnabled() {
            return getGlobalBoolean("netstats_sample_enabled", true);
        }

        public boolean getAugmentEnabled() {
            return getGlobalBoolean("netstats_augment_enabled", true);
        }

        public Config getDevConfig() {
            return new Config(getGlobalLong("netstats_dev_bucket_duration", 3600000), getGlobalLong("netstats_dev_rotate_age", 1296000000), getGlobalLong("netstats_dev_delete_age", 7776000000L));
        }

        public Config getXtConfig() {
            return getDevConfig();
        }

        public Config getUidConfig() {
            return new Config(getGlobalLong("netstats_uid_bucket_duration", 7200000), getGlobalLong("netstats_uid_rotate_age", 1296000000), getGlobalLong("netstats_uid_delete_age", 7776000000L));
        }

        public Config getUidTagConfig() {
            return new Config(getGlobalLong("netstats_uid_tag_bucket_duration", 7200000), getGlobalLong("netstats_uid_tag_rotate_age", 432000000), getGlobalLong("netstats_uid_tag_delete_age", 1296000000));
        }

        public long getDevPersistBytes(long def) {
            return getGlobalLong("netstats_dev_persist_bytes", def);
        }

        public long getXtPersistBytes(long def) {
            return getDevPersistBytes(def);
        }

        public long getUidPersistBytes(long def) {
            return getGlobalLong("netstats_uid_persist_bytes", def);
        }

        public long getUidTagPersistBytes(long def) {
            return getGlobalLong("netstats_uid_tag_persist_bytes", def);
        }

        public Config getPidConfig() {
            return new Config(getGlobalLong("netstats_uid_bucket_duration", 7200000), getGlobalLong("netstats_uid_rotate_age", 1296000000), getGlobalLong("netstats_uid_delete_age", 7776000000L));
        }

        public Config getPidTagConfig() {
            return new Config(getGlobalLong("netstats_uid_tag_bucket_duration", 7200000), getGlobalLong("netstats_uid_tag_rotate_age", 432000000), getGlobalLong("netstats_uid_tag_delete_age", 1296000000));
        }

        public long getPidPersistBytes(long def) {
            return getGlobalLong("netstats_uid_persist_bytes", def);
        }

        public long getPidTagPersistBytes(long def) {
            return getGlobalLong("netstats_uid_tag_persist_bytes", def);
        }
    }

    private class DropBoxNonMonotonicObserver implements NonMonotonicObserver<String> {
        /* synthetic */ DropBoxNonMonotonicObserver(NetworkStatsService this$0, DropBoxNonMonotonicObserver -this1) {
            this();
        }

        private DropBoxNonMonotonicObserver() {
        }

        public void foundNonMonotonic(NetworkStats left, int leftIndex, NetworkStats right, int rightIndex, String cookie) {
            Log.w(NetworkStatsService.TAG, "found non-monotonic values; saving to dropbox");
            StringBuilder builder = new StringBuilder();
            builder.append("found non-monotonic ").append(cookie).append(" values at left[").append(leftIndex).append("] - right[").append(rightIndex).append("]\n");
            builder.append("left=").append(left).append(10);
            builder.append("right=").append(right).append(10);
            ((DropBoxManager) NetworkStatsService.this.mContext.getSystemService("dropbox")).addText(NetworkStatsService.TAG_NETSTATS_ERROR, builder.toString());
        }
    }

    static class HandlerCallback implements Callback {
        private final NetworkStatsService mService;

        HandlerCallback(NetworkStatsService service) {
            this.mService = service;
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.mService.performPoll(msg.arg1);
                    return true;
                case 2:
                    this.mService.updateIfaces();
                    return true;
                case 3:
                    this.mService.registerGlobalAlert();
                    return true;
                case 4:
                    this.mService.procPollBroadcastMsg();
                    return true;
                case 5:
                    this.mService.processSetUidForeground(msg.arg1, msg.arg2);
                    return true;
                case 6:
                    this.mService.removeMultiAppUid(msg.arg1);
                    break;
            }
            return false;
        }
    }

    private static File getDefaultSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    private static File getDefaultBaseDir() {
        File baseDir = new File(getDefaultSystemDir(), "netstats");
        baseDir.mkdirs();
        return baseDir;
    }

    public static NetworkStatsService create(Context context, INetworkManagementService networkManager) {
        Context context2 = context;
        INetworkManagementService iNetworkManagementService = networkManager;
        NetworkStatsService service = new NetworkStatsService(context2, iNetworkManagementService, (AlarmManager) context.getSystemService("alarm"), ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG), NtpTrustedTime.getInstance(context), TelephonyManager.getDefault(), new DefaultNetworkStatsSettings(context), new NetworkStatsObservers(), getDefaultSystemDir(), getDefaultBaseDir());
        HandlerThread handlerThread = new HandlerThread(TAG);
        Callback callback = new HandlerCallback(service);
        handlerThread.start();
        service.setHandler(new Handler(handlerThread.getLooper(), callback), callback);
        HandlerThread handlerThread2 = new HandlerThread("StatsObserver");
        handlerThread2.start();
        Handler mStatsHandler = new Handler(handlerThread2.getLooper());
        return service;
    }

    NetworkStatsService(Context context, INetworkManagementService networkManager, AlarmManager alarmManager, WakeLock wakeLock, TrustedTime time, TelephonyManager teleManager, NetworkStatsSettings settings, NetworkStatsObservers statsObservers, File systemDir, File baseDir) {
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing Context");
        this.mNetworkManager = (INetworkManagementService) Preconditions.checkNotNull(networkManager, "missing INetworkManagementService");
        this.mAlarmManager = (AlarmManager) Preconditions.checkNotNull(alarmManager, "missing AlarmManager");
        this.mTime = (TrustedTime) Preconditions.checkNotNull(time, "missing TrustedTime");
        this.mSettings = (NetworkStatsSettings) Preconditions.checkNotNull(settings, "missing NetworkStatsSettings");
        this.mTeleManager = (TelephonyManager) Preconditions.checkNotNull(teleManager, "missing TelephonyManager");
        this.mWakeLock = (WakeLock) Preconditions.checkNotNull(wakeLock, "missing WakeLock");
        this.mStatsObservers = (NetworkStatsObservers) Preconditions.checkNotNull(statsObservers, "missing NetworkStatsObservers");
        this.mSystemDir = (File) Preconditions.checkNotNull(systemDir, "missing systemDir");
        this.mBaseDir = (File) Preconditions.checkNotNull(baseDir, "missing baseDir");
        context.getContentResolver().registerContentObserver(Global.getUriFor("netstats_global_alert_bytes"), false, this.mGlobalAlertBytesObserver);
        this.mOppoRefreshTimeTask = OppoRefreshTimeTask.getInstall(this.mTime);
        this.mDebugUidRecordDetail = SystemProperties.getBoolean(PROP_DEBUG_UID_RECORD_DETAIL, false);
    }

    void setHandler(Handler handler, Callback callback) {
        this.mHandler = handler;
        this.mHandlerCallback = callback;
    }

    public void bindConnectivityManager(IConnectivityManager connManager) {
        this.mConnManager = (IConnectivityManager) Preconditions.checkNotNull(connManager, "missing IConnectivityManager");
    }

    public void systemReady() {
        this.mSystemReady = true;
        if (isBandwidthControlEnabled()) {
            synchronized (this.mStatsLock) {
                this.mDevRecorder = buildRecorder(PREFIX_DEV, this.mSettings.getDevConfig(), false);
                this.mXtRecorder = buildRecorder(PREFIX_XT, this.mSettings.getXtConfig(), false);
                this.mUidRecorder = buildRecorder(PREFIX_UID, this.mSettings.getUidConfig(), false);
                this.mUidTagRecorder = buildRecorder(PREFIX_UID_TAG, this.mSettings.getUidTagConfig(), true);
                updatePersistThresholdsLocked();
                maybeUpgradeLegacyStatsLocked();
                this.mXtStatsCached = this.mXtRecorder.getOrLoadCompleteLocked();
                bootstrapStatsLocked();
            }
            this.mContext.registerReceiver(this.mTetherReceiver, new IntentFilter("android.net.conn.TETHER_STATE_CHANGED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mPollReceiver, new IntentFilter(ACTION_NETWORK_STATS_POLL), "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
            this.mContext.registerReceiver(this.mRemovedReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mUserReceiver, new IntentFilter("android.intent.action.USER_REMOVED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mShutdownReceiver, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
            try {
                this.mNetworkManager.registerObserver(this.mAlertObserver);
            } catch (RemoteException e) {
            }
            registerPollAlarmLocked();
            registerGlobalAlert();
            registerReceiverForMultiAppPkgAction();
            return;
        }
        Slog.w(TAG, "bandwidth controls disabled, unable to track stats");
    }

    private NetworkStatsRecorder buildRecorder(String prefix, Config config, boolean includeTags) {
        return new NetworkStatsRecorder(new FileRotator(this.mBaseDir, prefix, config.rotateAgeMillis, config.deleteAgeMillis), this.mNonMonotonicObserver, (DropBoxManager) this.mContext.getSystemService("dropbox"), prefix, config.bucketDuration, includeTags);
    }

    private NetworkStatsRecorderWithProcInfo buildRecorderWithProcInfo(String prefix, Config config, boolean includeTags) {
        return new NetworkStatsRecorderWithProcInfo(new FileRotator(this.mBaseDir, prefix, config.rotateAgeMillis, config.deleteAgeMillis), this.mNonMonotonicObserver, (DropBoxManager) this.mContext.getSystemService("dropbox"), prefix, config.bucketDuration, includeTags);
    }

    private void initShareUidConfig() {
        PackageManager pkgMgr = this.mContext.getPackageManager();
        try {
            ArrayList<String> configList = loadShareUidConfig(SHARE_UID_FOR_NET_CONFIG_FILE);
            int size = configList.size();
            if (DEBUG_CONFIG_LOAD) {
                Slog.d(TAG, "initShareUidConfig, configList.size:" + size);
            }
            for (int index = 0; index < size; index++) {
                if (DEBUG_CONFIG_LOAD) {
                    Slog.d(TAG, "initShareUidConfig, configList[" + index + "]:" + ((String) configList.get(index)));
                }
                String shareUidName = (String) configList.get(index);
                if (shareUidName != null && shareUidName.length() > 0) {
                    int shareUid = pkgMgr.getUidForSharedUser(shareUidName);
                    if (shareUid > 0) {
                        boolean res = NetworkManagementSocketTagger.addShareUid(shareUid);
                        if (DEBUG_CONFIG_LOAD) {
                            Slog.d(TAG, "initShareUidConfig, shareUidName:" + shareUidName + ", shareUid:" + shareUid + ", addRes:" + res);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Slog.w(TAG, "initShareUidConfig failed", ex);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b5 A:{SYNTHETIC, Splitter: B:29:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0156 A:{SYNTHETIC, Splitter: B:71:0x0156} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x013b A:{SYNTHETIC, Splitter: B:63:0x013b} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0122 A:{SYNTHETIC, Splitter: B:55:0x0122} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0109 A:{SYNTHETIC, Splitter: B:47:0x0109} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x016a A:{SYNTHETIC, Splitter: B:79:0x016a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<String> loadShareUidConfig(String path) {
        IOException e;
        NullPointerException e2;
        Throwable th;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        ArrayList<String> configList = new ArrayList();
        File file = new File(path);
        if (file.exists()) {
            FileInputStream stream = null;
            boolean success = false;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, null);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        switch (eventType) {
                            case 2:
                                String tagName = parser.getName();
                                if (!tagName.equals(TAG_OPPO_SHAREUID) && tagName.equals(TAG_SHARE_UID_FOR_NET)) {
                                    eventType = parser.next();
                                    String value = parser.getText();
                                    if (DEBUG_CONFIG_LOAD) {
                                        Slog.d(TAG, "TAG_SHARE_UID_FOR_NET, value:" + value);
                                    }
                                    if (!(value == null || value == "")) {
                                        configList.add(value);
                                        break;
                                    }
                                }
                            default:
                                break;
                        }
                    }
                    success = true;
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                    }
                } catch (NullPointerException e7) {
                    e2 = e7;
                    stream = stream2;
                    try {
                        Slog.w(TAG, "failed parsing ", e2);
                        if (stream != null) {
                        }
                        if (success) {
                        }
                        Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                        return configList;
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e62) {
                                e62.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (NumberFormatException e8) {
                    e3 = e8;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e3);
                    if (stream != null) {
                    }
                    if (success) {
                    }
                    Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                    return configList;
                } catch (XmlPullParserException e9) {
                    e4 = e9;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e4);
                    if (stream != null) {
                    }
                    if (success) {
                    }
                    Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                    return configList;
                } catch (IOException e10) {
                    e62 = e10;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e62);
                    if (stream != null) {
                    }
                    if (success) {
                    }
                    Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                    return configList;
                } catch (IndexOutOfBoundsException e11) {
                    e5 = e11;
                    stream = stream2;
                    Slog.w(TAG, "failed parsing ", e5);
                    if (stream != null) {
                    }
                    if (success) {
                    }
                    Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                    return configList;
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (NullPointerException e12) {
                e2 = e12;
                Slog.w(TAG, "failed parsing ", e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        e622.printStackTrace();
                    }
                }
                if (success) {
                }
                Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                return configList;
            } catch (NumberFormatException e13) {
                e3 = e13;
                Slog.w(TAG, "failed parsing ", e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        e6222.printStackTrace();
                    }
                }
                if (success) {
                }
                Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                return configList;
            } catch (XmlPullParserException e14) {
                e4 = e14;
                Slog.w(TAG, "failed parsing ", e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        e62222.printStackTrace();
                    }
                }
                if (success) {
                }
                Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                return configList;
            } catch (IOException e15) {
                e62222 = e15;
                Slog.w(TAG, "failed parsing ", e62222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622222) {
                        e622222.printStackTrace();
                    }
                }
                if (success) {
                }
                Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                return configList;
            } catch (IndexOutOfBoundsException e16) {
                e5 = e16;
                Slog.w(TAG, "failed parsing ", e5);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        e6222222.printStackTrace();
                    }
                }
                if (success) {
                }
                Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
                return configList;
            }
            Slog.d(TAG, path + " config file parse res:" + (success ? SyncStorageEngine.MESG_SUCCESS : "failed"));
            return configList;
        }
        Slog.w(TAG, path + " file not exist!");
        return configList;
    }

    private void shutdownLocked() {
        long currentTime;
        this.mContext.unregisterReceiver(this.mTetherReceiver);
        this.mContext.unregisterReceiver(this.mPollReceiver);
        this.mContext.unregisterReceiver(this.mRemovedReceiver);
        this.mContext.unregisterReceiver(this.mUserReceiver);
        this.mContext.unregisterReceiver(this.mShutdownReceiver);
        unregisterReceiverForMultiAppPkgAction();
        if (this.mTime.hasCache()) {
            currentTime = this.mTime.currentTimeMillis();
        } else {
            currentTime = System.currentTimeMillis();
        }
        try {
            recordSnapshotLocked(currentTime);
        } catch (RemoteException e) {
        }
        this.mDevRecorder.forcePersistLocked(currentTime);
        this.mXtRecorder.forcePersistLocked(currentTime);
        this.mUidRecorder.forcePersistLocked(currentTime);
        this.mUidTagRecorder.forcePersistLocked(currentTime);
        this.mSystemReady = false;
        this.mDevRecorder = null;
        this.mXtRecorder = null;
        this.mUidRecorder = null;
        this.mUidTagRecorder = null;
        this.mPidRecorder = null;
        Slog.d(TAG, "NetworkStatsService shutdown.");
        this.mXtStatsCached = null;
        this.mNetworkStats = null;
    }

    private void maybeUpgradeLegacyStatsLocked() {
        try {
            File file = new File(this.mSystemDir, "netstats.bin");
            if (file.exists()) {
                this.mDevRecorder.importLegacyNetworkLocked(file);
                file.delete();
            }
            file = new File(this.mSystemDir, "netstats_xt.bin");
            if (file.exists()) {
                file.delete();
            }
            file = new File(this.mSystemDir, "netstats_uid.bin");
            if (file.exists()) {
                this.mUidRecorder.importLegacyUidLocked(file);
                this.mUidTagRecorder.importLegacyUidLocked(file);
                file.delete();
            }
        } catch (IOException e) {
            Log.wtf(TAG, "problem during legacy upgrade", e);
        } catch (OutOfMemoryError e2) {
            Log.wtf(TAG, "problem during legacy upgrade", e2);
        }
    }

    private void registerPollAlarmLocked() {
        if (this.mPollIntent != null) {
            this.mAlarmManager.cancel(this.mPollIntent);
        }
        this.mPollIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_NETWORK_STATS_POLL), 0);
        this.mAlarmManager.setInexactRepeating(3, SystemClock.elapsedRealtime(), this.mSettings.getPollInterval(), this.mPollIntent);
    }

    private void registerGlobalAlert() {
        try {
            this.mNetworkManager.setGlobalAlert(this.mGlobalAlertBytes);
        } catch (IllegalStateException e) {
            Slog.w(TAG, "problem registering for global alert: " + e);
        } catch (RemoteException e2) {
        }
    }

    public INetworkStatsSession openSession() {
        return openSessionInternal(2, null);
    }

    public INetworkStatsSession openSessionForUsageStats(int flags, String callingPackage) {
        return openSessionInternal(flags, callingPackage);
    }

    private INetworkStatsSession openSessionInternal(final int flags, final String callingPackage) {
        assertBandwidthControlEnabled();
        if ((flags & 1) != 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                performPoll(3);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return new INetworkStatsSession.Stub() {
            private final int mAccessLevel = NetworkStatsService.this.checkAccessLevel(callingPackage);
            private final String mCallingPackage = callingPackage;
            private final int mCallingUid = Binder.getCallingUid();
            private NetworkStatsCollectionWithProcInfo mPidComplete;
            private NetworkStatsCollectionWithProcInfo mPidTagComplete;
            private NetworkStatsCollection mUidComplete;
            private NetworkStatsCollection mUidTagComplete;

            private NetworkStatsCollection getUidComplete() {
                synchronized (NetworkStatsService.this.mStatsLock) {
                    if (NetworkStatsService.this.mSystemReady) {
                        if (this.mUidComplete == null && NetworkStatsService.this.mUidRecorder != null) {
                            if (NetworkStatsService.LOG_NET_STATS) {
                                Slog.d(NetworkStatsService.TAG, "getUidComplete, mUidComplete is null, need to reload.");
                            }
                            this.mUidComplete = NetworkStatsService.this.mUidRecorder.getOrLoadCompleteLocked();
                        }
                        if (NetworkStatsService.LOG_NET_STATS && this.mUidComplete != null) {
                            this.mUidComplete.logoutData("uidComp");
                        }
                        NetworkStatsCollection networkStatsCollection = this.mUidComplete;
                        return networkStatsCollection;
                    }
                    return null;
                }
            }

            private NetworkStatsCollection getUidTagComplete() {
                NetworkStatsCollection networkStatsCollection;
                synchronized (NetworkStatsService.this.mStatsLock) {
                    if (this.mUidTagComplete == null) {
                        this.mUidTagComplete = NetworkStatsService.this.mUidTagRecorder.getOrLoadCompleteLocked();
                    }
                    networkStatsCollection = this.mUidTagComplete;
                }
                return networkStatsCollection;
            }

            public int[] getRelevantUids() {
                NetworkStatsCollection collection = getUidComplete();
                if (collection != null) {
                    return collection.getRelevantUids(this.mAccessLevel);
                }
                return new IntArray().toArray();
            }

            public NetworkStats getDeviceSummaryForNetwork(NetworkTemplate template, long start, long end) {
                return NetworkStatsService.this.internalGetSummaryForNetwork(template, flags, start, end, this.mAccessLevel, this.mCallingUid);
            }

            public NetworkStats getSummaryForNetwork(NetworkTemplate template, long start, long end) {
                NetworkStats res = NetworkStatsService.this.internalGetSummaryForNetwork(template, flags, start, end, this.mAccessLevel, this.mCallingUid);
                if (NetworkStatsService.LOG_NET_STATS) {
                    if (res != null) {
                        Slog.d(NetworkStatsService.TAG, "getSummaryForNetwork:");
                        res.logoutData("SummaryForNetwork");
                    } else {
                        Slog.d(NetworkStatsService.TAG, "getSummaryForNetwork: res null.");
                    }
                }
                return res;
            }

            public NetworkStatsHistory getHistoryForNetwork(NetworkTemplate template, int fields) {
                return NetworkStatsService.this.internalGetHistoryForNetwork(template, flags, fields, this.mAccessLevel, this.mCallingUid);
            }

            public NetworkStats getSummaryForAllUid(NetworkTemplate template, long start, long end, boolean includeTags) {
                try {
                    NetworkStats stats = getUidComplete().getSummary(template, start, end, this.mAccessLevel, this.mCallingUid);
                    if (includeTags) {
                        stats.combineAllValues(getUidTagComplete().getSummary(template, start, end, this.mAccessLevel, this.mCallingUid));
                    }
                    if (NetworkStatsService.LOG_NET_STATS) {
                        Slog.d(NetworkStatsService.TAG, "getSummaryForAllUid, start:" + start + ", end:" + end + ", mAccessLevel:" + this.mAccessLevel + ", mCallingUid:" + this.mCallingUid);
                        if (stats != null) {
                            Slog.d(NetworkStatsService.TAG, "getSummaryForAllUid:");
                            stats.logoutData("SummaryForUid");
                        } else {
                            Slog.d(NetworkStatsService.TAG, "getSummaryForAllUid: stats null.");
                        }
                    }
                    return stats;
                } catch (NullPointerException e) {
                    Slog.wtf(NetworkStatsService.TAG, "NullPointerException in getSummaryForAllUid", e);
                    throw e;
                }
            }

            public NetworkStatsHistory getHistoryForUid(NetworkTemplate template, int uid, int set, int tag, int fields) {
                NetworkStatsCollection collection;
                if (tag == 0) {
                    collection = getUidComplete();
                } else {
                    collection = getUidTagComplete();
                }
                if (collection == null) {
                    return null;
                }
                return collection.getHistory(template, null, uid, set, tag, fields, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, this.mAccessLevel, this.mCallingUid);
            }

            public NetworkStatsHistory getHistoryIntervalForUid(NetworkTemplate template, int uid, int set, int tag, int fields, long start, long end) {
                if (tag == 0) {
                    return getUidComplete().getHistory(template, null, uid, set, tag, fields, start, end, this.mAccessLevel, this.mCallingUid);
                } else if (uid == Binder.getCallingUid()) {
                    return getUidTagComplete().getHistory(template, null, uid, set, tag, fields, start, end, this.mAccessLevel, this.mCallingUid);
                } else {
                    throw new SecurityException("Calling package " + this.mCallingPackage + " cannot access tag information from a different uid");
                }
            }

            public NetworkStats getIncrementForNetwork(NetworkTemplate template) {
                NetworkStats stats;
                synchronized (NetworkStatsService.this.mStatsLock) {
                    stats = NetworkStatsService.this.internalGetIncrementForNetwork(template);
                }
                return stats;
            }

            public void close() {
                this.mUidComplete = null;
                this.mUidTagComplete = null;
                if (NetworkStatsService.LOG_NET_STATS) {
                    Slog.d(NetworkStatsService.TAG, "close INetworkStatsSession");
                }
                this.mPidComplete = null;
                this.mPidTagComplete = null;
            }

            private NetworkStatsCollectionWithProcInfo getPidComplete() {
                return null;
            }

            private NetworkStatsCollectionWithProcInfo getPidTagComplete() {
                Slog.w(NetworkStatsService.TAG, "getPidTagComplete:not support any more, just return null.");
                return null;
            }

            public NetworkStats getSummaryForAllUidWithPids(NetworkTemplate template, long start, long end, boolean includeTags) {
                return null;
            }

            public NetworkStatsHistory getHistoryForUidWithPids(NetworkTemplate template, int uid, int set, int tag, int fields) {
                Slog.w(NetworkStatsService.TAG, "!!!!!!!!!! getHistoryForUidWithPids:not support !!!!!!!!!!!");
                return null;
            }
        };
    }

    private int checkAccessLevel(String callingPackage) {
        return NetworkStatsAccess.checkAccessLevel(this.mContext, Binder.getCallingUid(), callingPackage);
    }

    private SubscriptionPlan resolveSubscriptionPlan(NetworkTemplate template, int flags) {
        SubscriptionPlan subscriptionPlan = null;
        if ((flags & 2) != 0 && template.getMatchRule() == 1 && this.mSettings.getAugmentEnabled()) {
            if (LOG_NET_STATS) {
                Slog.d(TAG, "Resolving plan for " + template);
            }
            long token = Binder.clearCallingIdentity();
            try {
                SubscriptionManager sm = (SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class);
                TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(TelephonyManager.class);
                for (int subId : sm.getActiveSubscriptionIdList()) {
                    if (template.matchesSubscriberId(tm.getSubscriberId(subId))) {
                        if (LOG_NET_STATS) {
                            Slog.d(TAG, "Found active matching subId " + subId);
                        }
                        List<SubscriptionPlan> plans = sm.getSubscriptionPlans(subId);
                        if (!plans.isEmpty()) {
                            subscriptionPlan = (SubscriptionPlan) plans.get(0);
                        }
                    }
                }
                if (LOG_NET_STATS) {
                    Slog.d(TAG, "Resolved to plan " + subscriptionPlan);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return subscriptionPlan;
    }

    private NetworkStats internalGetSummaryForNetwork(NetworkTemplate template, int flags, long start, long end, int accessLevel, int callingUid) {
        NetworkStatsHistory history = internalGetHistoryForNetwork(template, flags, -1, accessLevel, callingUid);
        Entry entry = history.getValues(start, end, System.currentTimeMillis(), null);
        if (LOG_NET_STATS) {
            history.logoutData("internalGetSummaryForNetwork");
            Log.d(TAG, "internalGetSummaryForNetwork history entry: " + entry);
        }
        NetworkStats networkStats = new NetworkStats(end - start, 1);
        networkStats.addValues(new NetworkStats.Entry(NetworkStats.IFACE_ALL, -1, -1, 0, -1, -1, entry.rxBytes, entry.rxPackets, entry.txBytes, entry.txPackets, entry.operations));
        return networkStats;
    }

    private void createAndCopyNetworkStatus(NetworkStats stats) {
        if (this.mNetworkStats == null) {
            this.mNetworkStats = new NetworkStats(SystemClock.elapsedRealtime(), 8);
        }
        if (LOG_NET_STATS) {
            if (stats != null) {
                Slog.d(TAG, "createAndCopyNetworkStatus stats:");
                stats.logoutData("increament-cur");
            } else {
                Slog.d(TAG, "createAndCopyNetworkStatus stats null.");
            }
        }
        if (stats != null) {
            for (int i = 0; i < stats.size(); i++) {
                NetworkStats.Entry entry = stats.getValues(i, null);
                if (!(entry.iface.contains("lo") || entry.iface.contains("p2p"))) {
                    this.mNetworkStats.replaceValues(entry);
                }
            }
            if (LOG_NET_STATS) {
                if (this.mNetworkStats != null) {
                    Slog.d(TAG, "createAndCopyNetworkStatus mNetworkStats:");
                    this.mNetworkStats.logoutData("create-mNetworkStats");
                } else {
                    Slog.d(TAG, "createAndCopyNetworkStatus mNetworkStats null.");
                }
            }
        }
    }

    private NetworkStats internalGetIncrementForNetwork(NetworkTemplate template) {
        NetworkStats.Entry entry = null;
        String iface = "";
        try {
            NetworkStats xtSnapshot = getNetworkStatsXt();
            if (LOG_NET_STATS) {
                if (xtSnapshot != null) {
                    Slog.d(TAG, "increament xtSnapshot:");
                    xtSnapshot.logoutData("increament");
                } else {
                    Slog.d(TAG, "increament xtSnapshot null.");
                }
            }
            try {
                int i;
                NetworkState[] states = this.mConnManager.getAllNetworkState();
                if (LOG_NET_STATS) {
                    if (this.mNetworkStats != null) {
                        Slog.d(TAG, "internalGetIncrementForNetwork mNetworkStats:");
                        this.mNetworkStats.logoutData("mNetworkStats-last");
                    } else {
                        Slog.d(TAG, "internalGetIncrementForNetwork mNetworkStats null.");
                    }
                }
                NetworkStats networkStats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
                if (xtSnapshot != null) {
                    for (i = 0; i < xtSnapshot.size(); i++) {
                        entry = xtSnapshot.getValues(i, entry);
                        int j = this.mNetworkStats.findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming);
                        if (j != -1) {
                            entry = this.mNetworkStats.getValues(j, entry);
                            if (LOG_NET_STATS) {
                                Log.d(TAG, "get increment 01 for: " + entry);
                            }
                            xtSnapshot.getIncrement(i, entry, 0);
                        }
                    }
                }
                if (states != null) {
                    int i2 = 0;
                    int length = states.length;
                    while (i2 < length) {
                        NetworkState state = states[i2];
                        if (template.matches(NetworkIdentity.buildNetworkIdentity(this.mContext, state))) {
                            iface = state.linkProperties.getInterfaceName();
                            if (LOG_NET_STATS) {
                                Log.d(TAG, "internalGetIncrementForNetwork template matches iface is " + iface);
                            }
                        } else {
                            i2++;
                        }
                    }
                }
                if (xtSnapshot != null) {
                    i = 0;
                    while (i < xtSnapshot.size()) {
                        entry = xtSnapshot.getValues(i, entry);
                        if (iface.equals(entry.iface)) {
                            if (LOG_NET_STATS) {
                                Log.d(TAG, "get increment 02 for: " + entry);
                            }
                            networkStats.addValues(entry);
                        } else {
                            i++;
                        }
                    }
                }
                if (LOG_NET_STATS) {
                    if (networkStats != null) {
                        Slog.d(TAG, "at last, increment network state:");
                        networkStats.logoutData("increRes");
                    } else {
                        Slog.d(TAG, "at last, increment network state null.");
                    }
                }
                return networkStats;
            } catch (RemoteException e) {
                if (LOG_NET_STATS) {
                    Slog.w(TAG, "problem reading network stats when internalGetIncrementForNetwork: " + e);
                }
                return null;
            }
        } catch (IllegalStateException e2) {
            Slog.w(TAG, "problem reading network stats when internalGetIncrementForNetwork: " + e2);
            return null;
        } catch (RemoteException e3) {
            if (LOG_NET_STATS) {
                Slog.w(TAG, "problem reading network stats when internalGetIncrementForNetwork: " + e3);
            }
            return null;
        }
    }

    private NetworkStatsHistory internalGetHistoryForNetwork(NetworkTemplate template, int flags, int fields, int accessLevel, int callingUid) {
        NetworkStatsHistory history;
        SubscriptionPlan augmentPlan = resolveSubscriptionPlan(template, flags);
        synchronized (this.mStatsLock) {
            history = this.mXtStatsCached.getHistory(template, augmentPlan, -1, -1, 0, fields, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, accessLevel, callingUid);
        }
        return history;
    }

    public long getNetworkTotalBytes(NetworkTemplate template, long start, long end) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY", TAG);
        assertBandwidthControlEnabled();
        return internalGetSummaryForNetwork(template, 2, start, end, 3, Binder.getCallingUid()).getTotalBytes();
    }

    public NetworkStats getDataLayerSnapshotForUid(int uid) throws RemoteException {
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        }
        assertBandwidthControlEnabled();
        long token = Binder.clearCallingIdentity();
        try {
            NetworkStats networkLayer = this.mNetworkManager.getNetworkStatsUidDetail(uid);
            networkLayer.spliceOperationsFrom(this.mUidOperations);
            NetworkStats dataLayer = new NetworkStats(networkLayer.getElapsedRealtime(), networkLayer.size());
            NetworkStats.Entry entry = null;
            for (int i = 0; i < networkLayer.size(); i++) {
                entry = networkLayer.getValues(i, entry);
                entry.iface = NetworkStats.IFACE_ALL;
                dataLayer.combineValues(entry);
            }
            return dataLayer;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public String[] getMobileIfaces() {
        return this.mMobileIfaces;
    }

    public void incrementOperationCount(int uid, int tag, int operationCount) {
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", TAG);
        }
        if (operationCount < 0) {
            throw new IllegalArgumentException("operation count can only be incremented");
        } else if (tag == 0) {
            throw new IllegalArgumentException("operation count must have specific tag");
        } else {
            synchronized (this.mStatsLock) {
                int set = this.mActiveUidCounterSet.get(uid, 0);
                this.mUidOperations.combineValues(this.mActiveIface, uid, set, tag, 0, 0, 0, 0, (long) operationCount);
                this.mUidOperations.combineValues(this.mActiveIface, uid, set, 0, 0, 0, 0, 0, (long) operationCount);
            }
        }
    }

    public void setUidForeground(int uid, boolean uidForeground) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, uid, uidForeground ? 1 : 0));
    }

    public void forceUpdateIfaces() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY", TAG);
        assertBandwidthControlEnabled();
        long token = Binder.clearCallingIdentity();
        try {
            updateIfaces();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void forceUpdate() {
        if (this.mOppoRefreshTimeTask.forceUpdateFreqControl()) {
            if (LOG_NET_STATS) {
                Slog.d(TAG, "forceUpdate call from pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY", TAG);
            assertBandwidthControlEnabled();
            long token = Binder.clearCallingIdentity();
            try {
                performPoll(3);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void advisePersistThreshold(long thresholdBytes) {
        long currentTime;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        assertBandwidthControlEnabled();
        this.mPersistThreshold = MathUtils.constrain(thresholdBytes, 131072, 2097152);
        if (this.mTime.hasCache()) {
            currentTime = this.mTime.currentTimeMillis();
        } else {
            currentTime = System.currentTimeMillis();
        }
        synchronized (this.mStatsLock) {
            if (this.mSystemReady) {
                updatePersistThresholdsLocked();
                this.mDevRecorder.maybePersistLocked(currentTime);
                this.mXtRecorder.maybePersistLocked(currentTime);
                this.mUidRecorder.maybePersistLocked(currentTime);
                this.mUidTagRecorder.maybePersistLocked(currentTime);
                registerGlobalAlert();
                return;
            }
        }
    }

    public DataUsageRequest registerUsageCallback(String callingPackage, DataUsageRequest request, Messenger messenger, IBinder binder) {
        Preconditions.checkNotNull(callingPackage, "calling package is null");
        Preconditions.checkNotNull(request, "DataUsageRequest is null");
        Preconditions.checkNotNull(request.template, "NetworkTemplate is null");
        Preconditions.checkNotNull(messenger, "messenger is null");
        Preconditions.checkNotNull(binder, "binder is null");
        int callingUid = Binder.getCallingUid();
        int accessLevel = checkAccessLevel(callingPackage);
        long token = Binder.clearCallingIdentity();
        try {
            DataUsageRequest normalizedRequest = this.mStatsObservers.register(request, messenger, binder, callingUid, accessLevel);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, Integer.valueOf(3)));
            return normalizedRequest;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void unregisterUsageRequest(DataUsageRequest request) {
        Preconditions.checkNotNull(request, "DataUsageRequest is null");
        int callingUid = Binder.getCallingUid();
        long token = Binder.clearCallingIdentity();
        try {
            this.mStatsObservers.unregister(request, callingUid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void updatePersistThresholdsLocked() {
        this.mDevRecorder.setPersistThreshold(this.mSettings.getDevPersistBytes(this.mPersistThreshold));
        this.mXtRecorder.setPersistThreshold(this.mSettings.getXtPersistBytes(this.mPersistThreshold));
        this.mUidRecorder.setPersistThreshold(this.mSettings.getUidPersistBytes(this.mPersistThreshold));
        this.mUidTagRecorder.setPersistThreshold(this.mSettings.getUidTagPersistBytes(this.mPersistThreshold));
        this.mGlobalAlertBytes = this.mSettings.getGlobalAlertBytes(this.mPersistThreshold);
    }

    private void processPollBroadcast() {
        this.mHandler.obtainMessage(4).sendToTarget();
    }

    public void procPollBroadcastMsg() {
        performPoll(3);
        registerGlobalAlert();
    }

    private void processSetUidForeground(int uid, int uidFgValue) {
        synchronized (this.mStatsLock) {
            int set = 1 == uidFgValue ? 1 : 0;
            if (this.mActiveUidCounterSet.get(uid, 0) != set) {
                this.mActiveUidCounterSet.put(uid, set);
                NetworkManagementSocketTagger.setKernelCounterSet(uid, set);
            }
        }
    }

    private void updateIfaces() {
        synchronized (this.mStatsLock) {
            this.mWakeLock.acquire();
            try {
                updateIfacesLocked();
                this.mWakeLock.release();
            } catch (Throwable th) {
                this.mWakeLock.release();
            }
        }
    }

    private void updateIfacesLocked() {
        if (this.mSystemReady) {
            performPollLocked(1);
            try {
                NetworkState[] states = this.mConnManager.getAllNetworkState();
                LinkProperties activeLink = this.mConnManager.getActiveLinkProperties();
                this.mActiveIface = activeLink != null ? activeLink.getInterfaceName() : null;
                this.mActiveIfaces.clear();
                this.mActiveUidIfaces.clear();
                ArraySet<String> mobileIfaces = new ArraySet();
                for (NetworkState state : states) {
                    if (state.networkInfo.isConnected()) {
                        boolean isMobile = ConnectivityManager.isNetworkTypeMobile(state.networkInfo.getType());
                        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
                        String baseIface = state.linkProperties.getInterfaceName();
                        if (baseIface != null) {
                            if (!state.networkCapabilities.hasCapability(4) || ident.getMetered()) {
                                findOrCreateNetworkIdentitySet(this.mActiveIfaces, baseIface).add(ident);
                                findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, baseIface).add(ident);
                            }
                            if (isMobile) {
                                mobileIfaces.add(baseIface);
                            }
                        }
                        for (LinkProperties stackedLink : state.linkProperties.getStackedLinks()) {
                            String stackedIface = stackedLink.getInterfaceName();
                            if (stackedIface != null) {
                                findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, stackedIface).add(ident);
                                if (isMobile) {
                                    mobileIfaces.add(stackedIface);
                                }
                            }
                        }
                    }
                }
                this.mMobileIfaces = (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
                if (!this.mActiveIfacesHasInit) {
                    this.mActiveIfacesHasInit = true;
                }
            } catch (RemoteException e) {
            }
        }
    }

    private static <K> NetworkIdentitySet findOrCreateNetworkIdentitySet(ArrayMap<K, NetworkIdentitySet> map, K key) {
        NetworkIdentitySet ident = (NetworkIdentitySet) map.get(key);
        if (ident != null) {
            return ident;
        }
        ident = new NetworkIdentitySet();
        map.put(key, ident);
        return ident;
    }

    private void recordSnapshotLocked(long currentTime) throws RemoteException {
        NetworkStats uidSnapshot = getNetworkStatsUidDetail();
        NetworkStats xtSnapshot = getNetworkStatsXt();
        NetworkStats devSnapshot = this.mNetworkManager.getNetworkStatsSummaryDev();
        createAndCopyNetworkStatus(xtSnapshot);
        NetworkStats tetherSnapshot = getNetworkStatsTethering(0);
        xtSnapshot.combineAllValues(tetherSnapshot);
        devSnapshot.combineAllValues(tetherSnapshot);
        this.mDevRecorder.recordSnapshotLocked(devSnapshot, this.mActiveIfaces, null, currentTime);
        this.mXtRecorder.recordSnapshotLocked(xtSnapshot, this.mActiveIfaces, null, currentTime);
        VpnInfo[] vpnArray = this.mConnManager.getAllVpnInfo();
        this.mUidRecorder.recordSnapshotLocked(uidSnapshot, this.mActiveUidIfaces, vpnArray, currentTime);
        this.mUidTagRecorder.recordSnapshotLocked(uidSnapshot, this.mActiveUidIfaces, vpnArray, currentTime);
        this.mStatsObservers.updateStats(xtSnapshot, uidSnapshot, new ArrayMap(this.mActiveIfaces), new ArrayMap(this.mActiveUidIfaces), vpnArray, currentTime);
    }

    private void bootstrapStatsLocked() {
        long currentTime;
        if (this.mTime.hasCache()) {
            currentTime = this.mTime.currentTimeMillis();
        } else {
            currentTime = System.currentTimeMillis();
        }
        try {
            recordSnapshotLocked(currentTime);
        } catch (IllegalStateException e) {
            Slog.w(TAG, "problem reading network stats: " + e);
        } catch (RemoteException e2) {
        }
    }

    private void performPoll(int flags) {
        if (this.mTime.getCacheAge() > this.mSettings.getTimeCacheMaxAge()) {
            synchronized (this.mRefreshTimeLocker) {
                this.mOppoRefreshTimeTask.triggerRefreshTime();
            }
        }
        synchronized (this.mStatsLock) {
            this.mWakeLock.acquire();
            try {
                performPollLocked(flags);
                this.mWakeLock.release();
            } catch (Throwable th) {
                this.mWakeLock.release();
            }
        }
    }

    private void performPollLocked(int flags) {
        if (this.mSystemReady) {
            long currentTime;
            long startRealtime = SystemClock.elapsedRealtime();
            boolean persistNetwork = (flags & 1) != 0;
            boolean persistUid = (flags & 2) != 0;
            boolean persistForce = (flags & 256) != 0;
            if (LOG_NET_STATS) {
                Slog.d(TAG, "performPollLocked, persistNetwork:" + persistNetwork + ", persistUid:" + persistUid + ", persistForce:" + persistForce);
                RuntimeException exce = new RuntimeException();
                exce.fillInStackTrace();
                Slog.d(TAG, "performPollLocked call from:", exce);
            }
            if (this.mTime.hasCache()) {
                currentTime = this.mTime.currentTimeMillis();
            } else {
                currentTime = System.currentTimeMillis();
            }
            try {
                recordSnapshotLocked(currentTime);
                if (persistForce) {
                    this.mDevRecorder.forcePersistLocked(currentTime);
                    this.mXtRecorder.forcePersistLocked(currentTime);
                    this.mUidRecorder.forcePersistLocked(currentTime);
                    this.mUidTagRecorder.forcePersistLocked(currentTime);
                } else {
                    if (persistNetwork) {
                        this.mDevRecorder.maybePersistLocked(currentTime);
                        this.mXtRecorder.maybePersistLocked(currentTime);
                    }
                    if (persistUid) {
                        this.mUidRecorder.maybePersistLocked(currentTime);
                        this.mUidTagRecorder.maybePersistLocked(currentTime);
                    }
                }
                if (this.mSettings.getSampleEnabled()) {
                    performSampleLocked();
                }
                Intent updatedIntent = new Intent(ACTION_NETWORK_STATS_UPDATED);
                updatedIntent.setFlags(1073741824);
                this.mContext.sendBroadcastAsUser(updatedIntent, UserHandle.ALL, "android.permission.READ_NETWORK_USAGE_HISTORY");
            } catch (IllegalStateException e) {
                Slog.w(TAG, "catch Exception:problem reading network stats", e);
            } catch (RemoteException e2) {
            }
        }
    }

    private void performSampleLocked() {
        long trustedTime;
        if (this.mTime.hasCache()) {
            trustedTime = this.mTime.currentTimeMillis();
        } else {
            trustedTime = -1;
        }
        NetworkTemplate template = NetworkTemplate.buildTemplateMobileWildcard();
        NetworkStats.Entry devTotal = this.mDevRecorder.getTotalSinceBootLocked(template);
        NetworkStats.Entry xtTotal = this.mXtRecorder.getTotalSinceBootLocked(template);
        NetworkStats.Entry uidTotal = this.mUidRecorder.getTotalSinceBootLocked(template);
        EventLogTags.writeNetstatsMobileSample(devTotal.rxBytes, devTotal.rxPackets, devTotal.txBytes, devTotal.txPackets, xtTotal.rxBytes, xtTotal.rxPackets, xtTotal.txBytes, xtTotal.txPackets, uidTotal.rxBytes, uidTotal.rxPackets, uidTotal.txBytes, uidTotal.txPackets, trustedTime);
        template = NetworkTemplate.buildTemplateWifiWildcard();
        devTotal = this.mDevRecorder.getTotalSinceBootLocked(template);
        xtTotal = this.mXtRecorder.getTotalSinceBootLocked(template);
        uidTotal = this.mUidRecorder.getTotalSinceBootLocked(template);
        EventLogTags.writeNetstatsWifiSample(devTotal.rxBytes, devTotal.rxPackets, devTotal.txBytes, devTotal.txPackets, xtTotal.rxBytes, xtTotal.rxPackets, xtTotal.txBytes, xtTotal.txPackets, uidTotal.rxBytes, uidTotal.rxPackets, uidTotal.txBytes, uidTotal.txPackets, trustedTime);
    }

    private void removeUidsLocked(int... uids) {
        performPollLocked(3);
        this.mUidRecorder.removeUidsLocked(uids);
        this.mUidTagRecorder.removeUidsLocked(uids);
        for (int uid : uids) {
            NetworkManagementSocketTagger.resetKernelUidStats(uid);
        }
    }

    private void removeUserLocked(int userId) {
        int[] uids = new int[0];
        for (ApplicationInfo app : this.mContext.getPackageManager().getInstalledApplications(4194816)) {
            uids = ArrayUtils.appendInt(uids, UserHandle.getUid(userId, app.uid));
        }
        removeUidsLocked(uids);
    }

    /* JADX WARNING: Missing block: B:62:0x0197, code:
            return;
     */
    /* JADX WARNING: Missing block: B:78:0x0285, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dump(FileDescriptor fd, PrintWriter rawWriter, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, rawWriter)) {
            long duration = UnixCalendar.DAY_IN_MILLIS;
            HashSet<String> argSet = new HashSet();
            for (String arg : args) {
                argSet.add(arg);
                if (arg.startsWith("--duration=")) {
                    try {
                        duration = Long.parseLong(arg.substring(11));
                    } catch (NumberFormatException e) {
                    }
                }
            }
            if (argSet.contains("--help")) {
                IndentingPrintWriter helpPw = new IndentingPrintWriter(rawWriter, "  ");
                helpPw.println("----------------------------------");
                helpPw.println("dumpData: dumpsys netstats --full --uid --tag --poll --checkin");
                helpPw.println("----------------------------------");
                helpPw.println("open log for debug:  dumpsys netstats --openlog");
                helpPw.println("close log for debug: dumpsys netstats --closelog");
                helpPw.println("open log for record detail: adb shell setprop persist.sys.assert.netstats true  ; then reboot.");
                return;
            }
            boolean poll = !argSet.contains("--poll") ? argSet.contains("poll") : true;
            boolean checkin = argSet.contains("--checkin");
            boolean fullHistory = !argSet.contains("--full") ? argSet.contains("full") : true;
            boolean includeUid = !argSet.contains("--uid") ? argSet.contains("detail") : true;
            boolean includeTag = !argSet.contains("--tag") ? argSet.contains("detail") : true;
            IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(rawWriter, "  ");
            boolean openlog = argSet.contains("--openlog");
            boolean closelog = argSet.contains("--closelog");
            if (openlog) {
                LOG_NET_STATS = true;
                LOG_NET_STATS_PROC = true;
                indentingPrintWriter.println("open log ok.");
            } else if (closelog) {
                LOG_NET_STATS = false;
                LOG_NET_STATS_PROC = false;
                indentingPrintWriter.println("close log ok.");
            } else {
                synchronized (this.mStatsLock) {
                    if (args.length > 0 && "--proto".equals(args[0])) {
                        dumpProtoLocked(fd);
                    } else if (poll) {
                        performPollLocked(259);
                        indentingPrintWriter.println("Forced poll");
                    } else if (checkin) {
                        long end = System.currentTimeMillis();
                        long start = end - duration;
                        indentingPrintWriter.print("v1,");
                        indentingPrintWriter.print(start / 1000);
                        indentingPrintWriter.print(',');
                        indentingPrintWriter.print(end / 1000);
                        indentingPrintWriter.println();
                        indentingPrintWriter.println(PREFIX_XT);
                        this.mXtRecorder.dumpCheckin(rawWriter, start, end);
                        if (includeUid) {
                            indentingPrintWriter.println(PREFIX_UID);
                            this.mUidRecorder.dumpCheckin(rawWriter, start, end);
                        }
                        if (includeTag) {
                            indentingPrintWriter.println("tag");
                            this.mUidTagRecorder.dumpCheckin(rawWriter, start, end);
                        }
                    } else {
                        int i;
                        indentingPrintWriter.println("Active interfaces:");
                        indentingPrintWriter.increaseIndent();
                        for (i = 0; i < this.mActiveIfaces.size(); i++) {
                            indentingPrintWriter.printPair("iface", this.mActiveIfaces.keyAt(i));
                            indentingPrintWriter.printPair("ident", this.mActiveIfaces.valueAt(i));
                            indentingPrintWriter.println();
                        }
                        indentingPrintWriter.decreaseIndent();
                        indentingPrintWriter.println("Active UID interfaces:");
                        indentingPrintWriter.increaseIndent();
                        for (i = 0; i < this.mActiveUidIfaces.size(); i++) {
                            indentingPrintWriter.printPair("iface", this.mActiveUidIfaces.keyAt(i));
                            indentingPrintWriter.printPair("ident", this.mActiveUidIfaces.valueAt(i));
                            indentingPrintWriter.println();
                        }
                        indentingPrintWriter.decreaseIndent();
                        indentingPrintWriter.println("Dev stats:");
                        indentingPrintWriter.increaseIndent();
                        this.mDevRecorder.dumpLocked(indentingPrintWriter, fullHistory);
                        indentingPrintWriter.decreaseIndent();
                        indentingPrintWriter.println("Xt stats:");
                        indentingPrintWriter.increaseIndent();
                        this.mXtRecorder.dumpLocked(indentingPrintWriter, fullHistory);
                        indentingPrintWriter.decreaseIndent();
                        if (includeUid) {
                            indentingPrintWriter.println("UID stats:");
                            indentingPrintWriter.increaseIndent();
                            this.mUidRecorder.dumpLocked(indentingPrintWriter, fullHistory);
                            indentingPrintWriter.decreaseIndent();
                        }
                        if (includeTag) {
                            indentingPrintWriter.println("UID tag stats:");
                            indentingPrintWriter.increaseIndent();
                            this.mUidTagRecorder.dumpLocked(indentingPrintWriter, fullHistory);
                            indentingPrintWriter.decreaseIndent();
                        }
                    }
                }
            }
        }
    }

    private void dumpProtoLocked(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        dumpInterfaces(proto, 2272037699585L, this.mActiveIfaces);
        dumpInterfaces(proto, 2272037699586L, this.mActiveUidIfaces);
        this.mDevRecorder.writeToProtoLocked(proto, 1172526071811L);
        this.mXtRecorder.writeToProtoLocked(proto, 1172526071812L);
        this.mUidRecorder.writeToProtoLocked(proto, 1172526071813L);
        this.mUidTagRecorder.writeToProtoLocked(proto, 1172526071814L);
        proto.flush();
    }

    private static void dumpInterfaces(ProtoOutputStream proto, long tag, ArrayMap<String, NetworkIdentitySet> ifaces) {
        for (int i = 0; i < ifaces.size(); i++) {
            long start = proto.start(tag);
            proto.write(1159641169921L, (String) ifaces.keyAt(i));
            ((NetworkIdentitySet) ifaces.valueAt(i)).writeToProto(proto, 1172526071810L);
            proto.end(start);
        }
    }

    private NetworkStats getNetworkStatsUidDetail() throws RemoteException {
        boolean z = false;
        NetworkStats uidSnapshot = this.mNetworkManager.getNetworkStatsUidDetail(-1);
        NetworkStats tetherSnapshot = getNetworkStatsTethering(1);
        if (LOG_NET_STATS) {
            if (tetherSnapshot == null || tetherSnapshot.size() <= 0) {
                String str = TAG;
                StringBuilder append = new StringBuilder().append("getNetworkStatsUidDetail, tetherSnapshot is empty. null ? ");
                if (tetherSnapshot == null) {
                    z = true;
                }
                Slog.d(str, append.append(z).toString());
            } else {
                Slog.d(TAG, "getNetworkStatsUidDetail, tetherSnapshot is:");
                tetherSnapshot.logoutData("tetherSnapshot");
            }
        }
        uidSnapshot.combineAllValues(tetherSnapshot);
        NetworkStats vtStats = ((TelephonyManager) this.mContext.getSystemService("phone")).getVtDataUsage(1);
        if (vtStats != null) {
            uidSnapshot.combineAllValues(vtStats);
        }
        uidSnapshot.combineAllValues(this.mUidOperations);
        return uidSnapshot;
    }

    private NetworkStats getNetworkStatsXt() throws RemoteException {
        NetworkStats xtSnapshotRead = this.mNetworkManager.getNetworkStatsSummaryXt();
        if (xtSnapshotRead == null) {
            xtSnapshotRead = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        }
        NetworkStats xtSnapshot = xtSnapshotRead;
        NetworkStats vtSnapshot = ((TelephonyManager) this.mContext.getSystemService("phone")).getVtDataUsage(0);
        if (vtSnapshot != null) {
            xtSnapshot.combineAllValues(vtSnapshot);
        }
        return xtSnapshot;
    }

    private NetworkStats getNetworkStatsUidDetailWithPids() throws RemoteException {
        return null;
    }

    private NetworkStats getNetworkStatsTethering(int how) throws RemoteException {
        try {
            return this.mNetworkManager.getNetworkStatsTethering(how);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem reading network stats", e);
            return new NetworkStats(0, 10);
        }
    }

    private void assertBandwidthControlEnabled() {
        if (!isBandwidthControlEnabled()) {
            throw new IllegalStateException("Bandwidth module disabled");
        }
    }

    private boolean isBandwidthControlEnabled() {
        long token = Binder.clearCallingIdentity();
        boolean isBandwidthControlEnabled;
        try {
            isBandwidthControlEnabled = this.mNetworkManager.isBandwidthControlEnabled();
            return isBandwidthControlEnabled;
        } catch (RemoteException e) {
            isBandwidthControlEnabled = false;
            return isBandwidthControlEnabled;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private static int getUid(int userId, int appId) {
        return (userId * LOCAL_PER_USER_RANGE) + (appId % LOCAL_PER_USER_RANGE);
    }

    private void registerReceiverForMultiAppPkgAction() {
        if (LOG_NET_STATS) {
            Slog.d(TAG, "registerReceiverForMultiAppPkgAction begin");
        }
        IntentFilter multiAppPkgActionFilter = new IntentFilter(ACTION_MULTI_APP_REMOVED);
        multiAppPkgActionFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mMultiAppPkgActionReceiver, multiAppPkgActionFilter, null, this.mHandler);
        if (LOG_NET_STATS) {
            Slog.d(TAG, "registerReceiverForMultiAppPkgAction end");
        }
    }

    private void unregisterReceiverForMultiAppPkgAction() {
        this.mContext.unregisterReceiver(this.mMultiAppPkgActionReceiver);
        if (LOG_NET_STATS) {
            Slog.d(TAG, "unregisterReceiverForMultiAppPkgAction");
        }
    }

    private void removeMultiAppUid(int multiAppUid) {
        if (LOG_NET_STATS) {
            Slog.d(TAG, "removeMultiAppUid:" + multiAppUid);
        }
        synchronized (this.mStatsLock) {
            this.mWakeLock.acquire();
            try {
                removeUidsLocked(multiAppUid);
                this.mWakeLock.release();
            } catch (Throwable th) {
                this.mWakeLock.release();
            }
        }
    }
}

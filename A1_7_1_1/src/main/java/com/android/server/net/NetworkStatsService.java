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
import android.net.NetworkStats.Entry;
import android.net.NetworkStats.NonMonotonicObserver;
import android.net.NetworkStatsHistory;
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
import com.android.internal.net.VpnInfo;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FileRotator;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.ColorOSDeviceIdleHelper;
import com.android.server.EventLogTags;
import com.android.server.NetworkManagementService;
import com.android.server.NetworkManagementSocketTagger;
import com.android.server.content.SyncStorageEngine;
import com.android.server.oppo.IElsaManager;
import com.android.server.oppo.OppoJunkRecorder;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
public class NetworkStatsService extends Stub {
    public static final String ACTION_IGNORE_DATA_USAGE_ALERT = "android.intent.action.IGNORE_DATA_USAGE_ALERT";
    private static final String ACTION_MULTI_APP_REMOVED = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED";
    public static final String ACTION_NETWORK_STATS_POLL = "com.android.server.action.NETWORK_STATS_POLL";
    public static final String ACTION_NETWORK_STATS_UPDATED = "com.android.server.action.NETWORK_STATS_UPDATED";
    private static final String ACTION_OPPO_CTS_APP_INSTALLED = "oppo.intent.action.CTS_APP_INSTALLED";
    private static boolean DEBUG_CONFIG_LOAD = false;
    private static final boolean ENG_DBG = false;
    public static final String EXTRA_IGNORE_ENABLED = "ignore";
    private static final int FLAG_PERSIST_ALL = 3;
    private static final int FLAG_PERSIST_FORCE = 256;
    private static final int FLAG_PERSIST_NETWORK = 1;
    private static final int FLAG_PERSIST_UID = 2;
    private static final int LOCAL_PER_USER_RANGE = 100000;
    private static final boolean LOGV = true;
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
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final String SHARE_UID_FOR_NET_CONFIG_FILE = "/system/etc/oppo_shareuid_for_net.xml";
    private static final boolean SUPPORT_NET_STATS_PID = false;
    private static final String TAG = "NetworkStats";
    private static final String TAG_NETSTATS_ERROR = "netstats_error";
    private static final String TAG_OPPO_SHAREUID = "oppo-shareuid";
    private static final String TAG_SHARE_UID_FOR_NET = "ShareUidForNet";
    public static boolean USE_TRUESTED_TIME = false;
    public static final String VT_INTERFACE = "vt_data0";
    private String mActiveIface;
    private final ArrayMap<String, NetworkIdentitySet> mActiveIfaces;
    private boolean mActiveIfacesHasInit;
    private SparseIntArray mActiveUidCounterSet;
    private final ArrayMap<String, NetworkIdentitySet> mActiveUidIfaces;
    private final AlarmManager mAlarmManager;
    private INetworkManagementEventObserver mAlertObserver;
    private final File mBaseDir;
    private IConnectivityManager mConnManager;
    private final Context mContext;
    private BroadcastReceiver mCtsAppInstalledReceiver;
    private boolean mDebugUidRecordDetail;
    private NetworkStatsRecorder mDevRecorder;
    private long mGlobalAlertBytes;
    private Handler mHandler;
    private Callback mHandlerCallback;
    private boolean mIgnoreAlert;
    private BroadcastReceiver mIgnoreAlertReceiver;
    private String[] mMobileIfaces;
    private BroadcastReceiver mMultiAppPkgActionReceiver;
    private NetworkLostStatUtil mNetworkLostUtil;
    private final INetworkManagementService mNetworkManager;
    private NetworkStats mNetworkStats;
    private final DropBoxNonMonotonicObserver mNonMonotonicObserver;
    private OppoRefreshTimeTask mOppoRefreshTimeTask;
    private long mPersistThreshold;
    private NetworkStatsRecorderWithProcInfo mPidRecorder;
    private NetworkStatsRecorderWithProcInfo mPidTagRecorder;
    private PendingIntent mPollIntent;
    private BroadcastReceiver mPollReceiver;
    private Object mRefreshTimeLocker;
    private BroadcastReceiver mRemovedReceiver;
    private final NetworkStatsSettings mSettings;
    private BroadcastReceiver mShutdownReceiver;
    private final Object mStatsLock;
    private final NetworkStatsObservers mStatsObservers;
    private final File mSystemDir;
    private boolean mSystemReady;
    private final TelephonyManager mTeleManager;
    private BroadcastReceiver mTetherReceiver;
    private final TrustedTime mTime;
    private NetworkStats mUidOperations;
    private NetworkStatsRecorder mUidRecorder;
    private NetworkStatsRecorder mUidTagRecorder;
    private BroadcastReceiver mUserReceiver;
    private final WakeLock mWakeLock;
    private NetworkStatsRecorder mXtRecorder;
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
            return getGlobalLong("netstats_poll_interval", ColorOSDeviceIdleHelper.DEFAULT_TOTAL_INTERVAL_TO_IDLE);
        }

        public long getTimeCacheMaxAge() {
            return getGlobalLong("netstats_time_cache_max_age", 86400000);
        }

        public long getGlobalAlertBytes(long def) {
            return getGlobalLong("netstats_global_alert_bytes", def);
        }

        public boolean getSampleEnabled() {
            return getGlobalBoolean("netstats_sample_enabled", true);
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
            return new Config(getGlobalLong("netstats_uid_tag_bucket_duration", 7200000), getGlobalLong("netstats_uid_tag_rotate_age", OppoJunkRecorder.JUNK_RECORD_DELETE_AGE), getGlobalLong("netstats_uid_tag_delete_age", 1296000000));
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
            return new Config(getGlobalLong("netstats_uid_tag_bucket_duration", 7200000), getGlobalLong("netstats_uid_tag_rotate_age", OppoJunkRecorder.JUNK_RECORD_DELETE_AGE), getGlobalLong("netstats_uid_tag_delete_age", 1296000000));
        }

        public long getPidPersistBytes(long def) {
            return getGlobalLong("netstats_uid_persist_bytes", def);
        }

        public long getPidTagPersistBytes(long def) {
            return getGlobalLong("netstats_uid_tag_persist_bytes", def);
        }
    }

    private class DropBoxNonMonotonicObserver implements NonMonotonicObserver<String> {
        /* synthetic */ DropBoxNonMonotonicObserver(NetworkStatsService this$0, DropBoxNonMonotonicObserver dropBoxNonMonotonicObserver) {
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
            Log.v(NetworkStatsService.TAG, "handleMessage(): msg=" + msg.what);
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.net.NetworkStatsService.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.net.NetworkStatsService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.NetworkStatsService.<clinit>():void");
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
        NetworkStatsService service = new NetworkStatsService(context, networkManager, (AlarmManager) context.getSystemService("alarm"), ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG), NtpTrustedTime.getInstance(context), TelephonyManager.getDefault(), new DefaultNetworkStatsSettings(context), new NetworkStatsObservers(), getDefaultSystemDir(), getDefaultBaseDir());
        HandlerThread handlerThread = new HandlerThread(TAG);
        Callback callback = new HandlerCallback(service);
        handlerThread.start();
        service.setHandler(new Handler(handlerThread.getLooper(), callback), callback);
        return service;
    }

    NetworkStatsService(Context context, INetworkManagementService networkManager, AlarmManager alarmManager, WakeLock wakeLock, TrustedTime time, TelephonyManager teleManager, NetworkStatsSettings settings, NetworkStatsObservers statsObservers, File systemDir, File baseDir) {
        this.mDebugUidRecordDetail = false;
        this.mStatsLock = new Object();
        this.mActiveIfaces = new ArrayMap();
        this.mActiveUidIfaces = new ArrayMap();
        this.mMobileIfaces = new String[0];
        this.mNonMonotonicObserver = new DropBoxNonMonotonicObserver(this, null);
        this.mActiveIfacesHasInit = false;
        this.mOppoRefreshTimeTask = null;
        this.mRefreshTimeLocker = new Object();
        this.mNetworkStats = null;
        this.mNetworkLostUtil = null;
        this.mActiveUidCounterSet = new SparseIntArray();
        this.mUidOperations = new NetworkStats(0, 10);
        this.mPersistThreshold = 2097152;
        this.mIgnoreAlert = false;
        this.mTetherReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (NetworkStatsService.LOG_NET_STATS) {
                    Slog.d(NetworkStatsService.TAG, "mTetherReceiver receive ACTION_TETHER_STATE_CHANGED broadcast.");
                }
                NetworkStatsService.this.performPoll(1);
            }
        };
        this.mPollReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkStatsService.this.processPollBroadcast();
            }
        };
        this.mRemovedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1) {
                    synchronized (NetworkStatsService.this.mStatsLock) {
                        NetworkStatsService.this.mWakeLock.acquire();
                        try {
                            NetworkStatsService networkStatsService = NetworkStatsService.this;
                            int[] iArr = new int[1];
                            iArr[0] = uid;
                            networkStatsService.removeUidsLocked(iArr);
                            NetworkStatsService.this.mWakeLock.release();
                        } catch (Throwable th) {
                            NetworkStatsService.this.mWakeLock.release();
                        }
                    }
                }
            }
        };
        this.mUserReceiver = new BroadcastReceiver() {
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
        this.mIgnoreAlertReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(NetworkStatsService.TAG, "mIgnoreAlertReceiver onReceive action:" + action);
                if (action.equals(NetworkStatsService.ACTION_IGNORE_DATA_USAGE_ALERT)) {
                    Log.e(NetworkStatsService.TAG, "[powerdebug]ignoreAlertFilter received");
                }
                if (intent.getBooleanExtra(NetworkStatsService.EXTRA_IGNORE_ENABLED, true)) {
                    NetworkStatsService.this.mIgnoreAlert = true;
                    return;
                }
                NetworkStatsService.this.mIgnoreAlert = false;
                NetworkStatsService.this.mHandler.obtainMessage(1, 1, 0).sendToTarget();
                NetworkStatsService.this.mHandler.obtainMessage(3).sendToTarget();
            }
        };
        this.mShutdownReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkStatsService.this.mStatsLock) {
                    NetworkStatsService.this.shutdownLocked();
                }
            }
        };
        this.mAlertObserver = new BaseNetworkObserver() {
            public void limitReached(String limitName, String iface) {
                NetworkStatsService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkStatsService.TAG);
                if (NetworkStatsService.this.mIgnoreAlert) {
                    Log.d(NetworkStatsService.TAG, "[powerdebug]IGNORE_DATA_USAGE_ALERT received, mIgnoreAlert=" + NetworkStatsService.this.mIgnoreAlert);
                }
                if (NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName) && !NetworkStatsService.this.mIgnoreAlert) {
                    NetworkStatsService.this.mHandler.obtainMessage(1, 1, 0).sendToTarget();
                    NetworkStatsService.this.mHandler.obtainMessage(3).sendToTarget();
                }
            }
        };
        this.mCtsAppInstalledReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && NetworkStatsService.ACTION_OPPO_CTS_APP_INSTALLED.equals(intent.getAction())) {
                    if (NetworkStatsService.LOG_NET_STATS) {
                        Slog.d(NetworkStatsService.TAG, "ACTION_OPPO_CTS_APP_INSTALLED");
                    }
                    NetworkManagementSocketTagger.updateRecordQueryPolicy(0);
                }
            }
        };
        this.mMultiAppPkgActionReceiver = new BroadcastReceiver() {
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
                        String pkgName = IElsaManager.EMPTY_PACKAGE;
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
        this.mIgnoreAlert = false;
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
            this.mDevRecorder = buildRecorder(PREFIX_DEV, this.mSettings.getDevConfig(), false);
            this.mXtRecorder = buildRecorder(PREFIX_XT, this.mSettings.getXtConfig(), false);
            this.mUidRecorder = buildRecorder("uid", this.mSettings.getUidConfig(), false);
            this.mUidTagRecorder = buildRecorder(PREFIX_UID_TAG, this.mSettings.getUidTagConfig(), true);
            updatePersistThresholds();
            synchronized (this.mStatsLock) {
                maybeUpgradeLegacyStatsLocked();
                this.mXtStatsCached = this.mXtRecorder.getOrLoadCompleteLocked();
                bootstrapStatsLocked();
            }
            this.mContext.registerReceiver(this.mTetherReceiver, new IntentFilter("android.net.conn.TETHER_STATE_CHANGED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mPollReceiver, new IntentFilter(ACTION_NETWORK_STATS_POLL), "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
            this.mContext.registerReceiver(this.mRemovedReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mUserReceiver, new IntentFilter("android.intent.action.USER_REMOVED"), null, this.mHandler);
            IntentFilter shutdownFilter = new IntentFilter("android.intent.action.ACTION_SHUTDOWN");
            shutdownFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
            this.mContext.registerReceiver(this.mShutdownReceiver, shutdownFilter);
            IntentFilter ignoreAlertFilter = new IntentFilter(ACTION_IGNORE_DATA_USAGE_ALERT);
            ignoreAlertFilter.addAction(ACTION_IGNORE_DATA_USAGE_ALERT);
            this.mContext.registerReceiver(this.mIgnoreAlertReceiver, ignoreAlertFilter);
            try {
                this.mNetworkManager.registerObserver(this.mAlertObserver);
            } catch (RemoteException e) {
            }
            registerPollAlarmLocked();
            registerGlobalAlert();
            initShareUidConfig();
            registerReceiverForCtsAppInstallState();
            registerReceiverForMultiAppPkgAction();
            if (this.mNetworkLostUtil == null) {
                this.mNetworkLostUtil = NetworkLostStatUtil.getInstall(this.mContext);
            }
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
                                    if (!(value == null || value == IElsaManager.EMPTY_PACKAGE)) {
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
        unregisterReceiverForCtsAppInstallState();
        long currentTime = (this.mTime.hasCache() && USE_TRUESTED_TIME) ? this.mTime.currentTimeMillis() : System.currentTimeMillis();
        try {
            recordSnapshotLocked(currentTime);
        } catch (IllegalStateException e) {
            Slog.w(TAG, "problem reading network stats: " + e);
        } catch (RemoteException e2) {
        }
        this.mDevRecorder.forcePersistLocked(currentTime);
        this.mXtRecorder.forcePersistLocked(currentTime);
        this.mUidRecorder.forcePersistLocked(currentTime);
        this.mUidTagRecorder.forcePersistLocked(currentTime);
        this.mPidRecorder = null;
        Slog.d(TAG, "NetworkStatsService shutdown.");
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
            Log.e(TAG, "problem during legacy upgrade", e);
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
        return createSession(null, false);
    }

    public INetworkStatsSession openSessionForUsageStats(String callingPackage) {
        return createSession(callingPackage, true);
    }

    private INetworkStatsSession createSession(final String callingPackage, boolean pollOnCreate) {
        assertBandwidthControlEnabled();
        if (pollOnCreate) {
            long ident = Binder.clearCallingIdentity();
            try {
                performPoll(3);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return new INetworkStatsSession.Stub() {
            private String mCallingPackage = callingPackage;
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
                    if (this.mUidTagComplete == null && NetworkStatsService.this.mUidTagRecorder != null) {
                        this.mUidTagComplete = NetworkStatsService.this.mUidTagRecorder.getOrLoadCompleteLocked();
                    }
                    networkStatsCollection = this.mUidTagComplete;
                }
                return networkStatsCollection;
            }

            public int[] getRelevantUids() {
                NetworkStatsCollection collection = getUidComplete();
                if (collection != null) {
                    return collection.getRelevantUids(NetworkStatsService.this.checkAccessLevel(this.mCallingPackage));
                }
                return new IntArray().toArray();
            }

            public NetworkStats getDeviceSummaryForNetwork(NetworkTemplate template, long start, long end) {
                if (NetworkStatsService.this.checkAccessLevel(this.mCallingPackage) < 2) {
                    throw new SecurityException("Calling package " + this.mCallingPackage + " cannot access device summary network stats");
                }
                NetworkStats result = new NetworkStats(end - start, 1);
                long ident = Binder.clearCallingIdentity();
                try {
                    result.combineAllValues(NetworkStatsService.this.internalGetSummaryForNetwork(template, start, end, 3));
                    return result;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }

            public NetworkStats getSummaryForNetwork(NetworkTemplate template, long start, long end) {
                NetworkStats res = NetworkStatsService.this.internalGetSummaryForNetwork(template, start, end, NetworkStatsService.this.checkAccessLevel(this.mCallingPackage));
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
                return NetworkStatsService.this.internalGetHistoryForNetwork(template, fields, NetworkStatsService.this.checkAccessLevel(this.mCallingPackage));
            }

            public NetworkStats getSummaryForAllUid(NetworkTemplate template, long start, long end, boolean includeTags) {
                int accessLevel = NetworkStatsService.this.checkAccessLevel(this.mCallingPackage);
                NetworkStatsCollection collection = getUidComplete();
                if (collection == null) {
                    return null;
                }
                NetworkStats stats = collection.getSummary(template, start, end, accessLevel);
                if (includeTags) {
                    NetworkStatsCollection uidTagCollection = getUidTagComplete();
                    if (!(uidTagCollection == null || stats == null)) {
                        stats.combineAllValues(uidTagCollection.getSummary(template, start, end, accessLevel));
                    }
                }
                if (NetworkStatsService.LOG_NET_STATS) {
                    Slog.d(NetworkStatsService.TAG, "getSummaryForAllUid, start:" + start + ", end:" + end + ", includeTags:" + includeTags + ", accessLevel:" + accessLevel);
                    if (stats != null) {
                        Slog.d(NetworkStatsService.TAG, "getSummaryForAllUid:");
                        stats.logoutData("SummaryForUid");
                    } else {
                        Slog.d(NetworkStatsService.TAG, "getSummaryForAllUid: stats null.");
                    }
                }
                return stats;
            }

            public NetworkStatsHistory getHistoryForUid(NetworkTemplate template, int uid, int set, int tag, int fields) {
                NetworkStatsCollection collection;
                int accessLevel = NetworkStatsService.this.checkAccessLevel(this.mCallingPackage);
                if (tag == 0) {
                    collection = getUidComplete();
                } else {
                    collection = getUidTagComplete();
                }
                if (collection == null) {
                    return null;
                }
                return collection.getHistory(template, uid, set, tag, fields, accessLevel);
            }

            public NetworkStatsHistory getHistoryIntervalForUid(NetworkTemplate template, int uid, int set, int tag, int fields, long start, long end) {
                NetworkStatsCollection collection;
                int accessLevel = NetworkStatsService.this.checkAccessLevel(this.mCallingPackage);
                if (tag == 0) {
                    collection = getUidComplete();
                } else if (uid == Binder.getCallingUid()) {
                    collection = getUidTagComplete();
                } else {
                    throw new SecurityException("Calling package " + this.mCallingPackage + " cannot access tag information from a different uid");
                }
                return collection == null ? null : collection.getHistory(template, uid, set, tag, fields, start, end, accessLevel);
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

            public NetworkStats getIncrementForNetwork(NetworkTemplate template) {
                NetworkStats stats;
                synchronized (NetworkStatsService.this.mStatsLock) {
                    stats = NetworkStatsService.this.internalGetIncrementForNetwork(template);
                }
                return stats;
            }

            public long getMobileTotalBytes(int subSim, long start_time, long end_time) {
                String subscriberId = null;
                long totalBytes = 0;
                TelephonyManager tele = TelephonyManager.from(NetworkStatsService.this.mContext);
                if (tele != null) {
                    try {
                        if (tele.getSimState(subSim) == 5) {
                            int subId = -1;
                            int[] subIds = SubscriptionManager.getSubId(subSim);
                            if (!(subIds == null || subIds.length == 0)) {
                                subId = subIds[0];
                            }
                            if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                                Log.e(NetworkStatsService.TAG, "Dual sim subSim:" + subSim + " fetch subID fail, always fetch default");
                                subId = SubscriptionManager.getDefaultDataSubscriptionId();
                            }
                            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                                subscriberId = tele.getSubscriberId(subId);
                                totalBytes = NetworkStatsService.this.getNetworkTotalBytes(NetworkTemplate.buildTemplateMobileAll(subscriberId), start_time, end_time);
                            } else {
                                Log.e(NetworkStatsService.TAG, "getMobileTotalBytes subId not valid");
                            }
                        } else {
                            Log.e(NetworkStatsService.TAG, "getMobileTotalBytes SimState != SIM_STATE_READY");
                        }
                    } catch (Exception e) {
                        Log.e(NetworkStatsService.TAG, "getMobileTotalBytes Exception" + e);
                    }
                } else {
                    Log.e(NetworkStatsService.TAG, "getMobileTotalBytes TelephonyManager is null");
                }
                Log.v(NetworkStatsService.TAG, "getMobileTotalBytes subSim=" + subSim + " subscriberId" + subscriberId + " start_time=" + start_time + " end_time" + end_time + " totalBytes=" + totalBytes);
                return totalBytes;
            }
        };
    }

    private int checkAccessLevel(String callingPackage) {
        return NetworkStatsAccess.checkAccessLevel(this.mContext, Binder.getCallingUid(), callingPackage);
    }

    private NetworkStats internalGetSummaryForNetwork(NetworkTemplate template, long start, long end, int accessLevel) {
        if (ENG_DBG) {
            Log.d(TAG, "internalGetSummaryForNetwork template:" + template);
        }
        return this.mXtStatsCached.getSummary(template, start, end, accessLevel);
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
                Entry entry = stats.getValues(i, null);
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
        Entry entry = null;
        String iface = IElsaManager.EMPTY_PACKAGE;
        try {
            NetworkStats xtSnapshot = getNetworkStatsXtAndVt();
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
                        int j = this.mNetworkStats.findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.roaming);
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

    private NetworkStatsHistory internalGetHistoryForNetwork(NetworkTemplate template, int fields, int accessLevel) {
        if (ENG_DBG) {
            Log.d(TAG, "internalGetHistoryForNetwork template:" + template);
        }
        return this.mXtStatsCached.getHistory(template, -1, -1, 0, fields, accessLevel);
    }

    public long getNetworkTotalBytes(NetworkTemplate template, long start, long end) {
        if (ENG_DBG) {
            Log.d(TAG, "getNetworkTotalBytes template:" + template);
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY", TAG);
        assertBandwidthControlEnabled();
        return internalGetSummaryForNetwork(template, start, end, 3).getTotalBytes();
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
            Entry entry = null;
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
            this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_NETWORK_ACCOUNTING", TAG);
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
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_NETWORK_ACCOUNTING", TAG);
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
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_NETWORK_ACCOUNTING", TAG);
        assertBandwidthControlEnabled();
        this.mPersistThreshold = MathUtils.constrain(thresholdBytes, 131072, 2097152);
        Slog.v(TAG, "advisePersistThreshold() given " + thresholdBytes + ", clamped to " + this.mPersistThreshold);
        if (this.mTime.hasCache() && USE_TRUESTED_TIME) {
            currentTime = this.mTime.currentTimeMillis();
        } else {
            currentTime = System.currentTimeMillis();
        }
        synchronized (this.mStatsLock) {
            if (this.mSystemReady) {
                updatePersistThresholds();
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

    private void updatePersistThresholds() {
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
            Slog.v(TAG, "updateIfacesLocked()");
            performPollLocked(1);
            try {
                NetworkState[] states = this.mConnManager.getAllNetworkState();
                LinkProperties activeLink = this.mConnManager.getActiveLinkProperties();
                this.mActiveIface = activeLink != null ? activeLink.getInterfaceName() : null;
                this.mActiveIfaces.clear();
                this.mActiveUidIfaces.clear();
                ArraySet<String> mobileIfaces = new ArraySet();
                int i = 0;
                int length = states.length;
                while (true) {
                    int i2 = i;
                    if (i2 >= length) {
                        break;
                    }
                    NetworkState state = states[i2];
                    if (state.networkInfo.isConnected()) {
                        boolean isMobile = ConnectivityManager.isNetworkTypeMobile(state.networkInfo.getType());
                        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
                        Slog.i(TAG, "NetworkIdentity: " + ident);
                        String baseIface = state.linkProperties.getInterfaceName();
                        if (baseIface != null) {
                            findOrCreateNetworkIdentitySet(this.mActiveIfaces, baseIface).add(ident);
                            findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, baseIface).add(ident);
                            Slog.i(TAG, "state.networkCapabilities: " + state.networkCapabilities);
                            if (state.networkCapabilities.hasCapability(4) && !ident.getMetered()) {
                                NetworkIdentity vtIdent = new NetworkIdentity(ident.getType(), ident.getSubType(), ident.getSubscriberId(), ident.getNetworkId(), ident.getRoaming(), true);
                                findOrCreateNetworkIdentitySet(this.mActiveIfaces, VT_INTERFACE).add(vtIdent);
                                findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, VT_INTERFACE).add(vtIdent);
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
                    i = i2 + 1;
                }
                for (int i3 = 0; i3 < this.mActiveIfaces.size(); i3++) {
                    Slog.i(TAG, "iface:" + ((String) this.mActiveIfaces.keyAt(i3)));
                    for (NetworkIdentity id : (NetworkIdentitySet) this.mActiveIfaces.valueAt(i3)) {
                        Slog.i(TAG, "ident:" + id);
                    }
                }
                for (String iface : mobileIfaces) {
                    if (!(iface == null || ArrayUtils.contains(this.mMobileIfaces, iface))) {
                        this.mMobileIfaces = (String[]) ArrayUtils.appendElement(String.class, this.mMobileIfaces, iface);
                    }
                }
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
        NetworkStats xtSnapshot = getNetworkStatsXtAndVt();
        NetworkStats devSnapshot = getNetworkStatsSummaryDevAndVt();
        createAndCopyNetworkStatus(xtSnapshot);
        this.mDevRecorder.recordSnapshotLocked(devSnapshot, this.mActiveIfaces, null, currentTime);
        this.mXtRecorder.recordSnapshotLocked(xtSnapshot, this.mActiveIfaces, null, currentTime);
        VpnInfo[] vpnArray = this.mConnManager.getAllVpnInfo();
        this.mUidRecorder.recordSnapshotLocked(uidSnapshot, this.mActiveUidIfaces, vpnArray, currentTime);
        this.mUidTagRecorder.recordSnapshotLocked(uidSnapshot, this.mActiveUidIfaces, vpnArray, currentTime);
        this.mStatsObservers.updateStats(xtSnapshot, uidSnapshot, new ArrayMap(this.mActiveIfaces), new ArrayMap(this.mActiveUidIfaces), vpnArray, currentTime);
    }

    private void bootstrapStatsLocked() {
        long currentTime;
        if (this.mTime.hasCache() && USE_TRUESTED_TIME) {
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
        if (this.mTime.getCacheAge() > this.mSettings.getTimeCacheMaxAge() && USE_TRUESTED_TIME) {
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
            Slog.v(TAG, "performPollLocked(flags=0x" + Integer.toHexString(flags) + ")");
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
            long currentTime = (this.mTime.hasCache() && USE_TRUESTED_TIME) ? this.mTime.currentTimeMillis() : System.currentTimeMillis();
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
                Slog.v(TAG, "performPollLocked() took " + (SystemClock.elapsedRealtime() - startRealtime) + "ms");
                if (this.mSettings.getSampleEnabled()) {
                    performSampleLocked();
                }
                Intent updatedIntent = new Intent(ACTION_NETWORK_STATS_UPDATED);
                updatedIntent.setFlags(1073741824);
                this.mContext.sendBroadcastAsUser(updatedIntent, UserHandle.ALL, "android.permission.READ_NETWORK_USAGE_HISTORY");
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem reading network stats", e);
            } catch (RemoteException e2) {
            }
        }
    }

    private void performSampleLocked() {
        long trustedTime;
        if (this.mTime.hasCache() && USE_TRUESTED_TIME) {
            trustedTime = this.mTime.currentTimeMillis();
        } else {
            trustedTime = -1;
        }
        NetworkTemplate template = NetworkTemplate.buildTemplateMobileWildcard();
        Entry devTotal = this.mDevRecorder.getTotalSinceBootLocked(template);
        Entry xtTotal = this.mXtRecorder.getTotalSinceBootLocked(template);
        Entry uidTotal = this.mUidRecorder.getTotalSinceBootLocked(template);
        EventLogTags.writeNetstatsMobileSample(devTotal.rxBytes, devTotal.rxPackets, devTotal.txBytes, devTotal.txPackets, xtTotal.rxBytes, xtTotal.rxPackets, xtTotal.txBytes, xtTotal.txPackets, uidTotal.rxBytes, uidTotal.rxPackets, uidTotal.txBytes, uidTotal.txPackets, trustedTime);
        template = NetworkTemplate.buildTemplateWifiWildcard();
        devTotal = this.mDevRecorder.getTotalSinceBootLocked(template);
        xtTotal = this.mXtRecorder.getTotalSinceBootLocked(template);
        uidTotal = this.mUidRecorder.getTotalSinceBootLocked(template);
        EventLogTags.writeNetstatsWifiSample(devTotal.rxBytes, devTotal.rxPackets, devTotal.txBytes, devTotal.txPackets, xtTotal.rxBytes, xtTotal.rxPackets, xtTotal.txBytes, xtTotal.txPackets, uidTotal.rxBytes, uidTotal.rxPackets, uidTotal.txBytes, uidTotal.txPackets, trustedTime);
    }

    private void removeUidsLocked(int... uids) {
        Slog.v(TAG, "removeUidsLocked() for UIDs " + Arrays.toString(uids));
        performPollLocked(3);
        this.mUidRecorder.removeUidsLocked(uids);
        this.mUidTagRecorder.removeUidsLocked(uids);
        for (int uid : uids) {
            NetworkManagementSocketTagger.resetKernelUidStats(uid);
        }
    }

    private void removeUserLocked(int userId) {
        Slog.v(TAG, "removeUserLocked() for userId=" + userId);
        int[] uids = new int[0];
        for (ApplicationInfo app : this.mContext.getPackageManager().getInstalledApplications(8704)) {
            uids = ArrayUtils.appendInt(uids, UserHandle.getUid(userId, app.uid));
        }
        removeUidsLocked(uids);
    }

    /* JADX WARNING: Missing block: B:51:0x0180, code:
            return;
     */
    /* JADX WARNING: Missing block: B:67:0x026e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dump(FileDescriptor fd, PrintWriter rawWriter, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        long duration = 86400000;
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
                if (poll) {
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
                        indentingPrintWriter.println("uid");
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

    private NetworkStats getNetworkStatsUidDetail() throws RemoteException {
        boolean z = true;
        NetworkStats uidSnapshot = this.mNetworkManager.getNetworkStatsUidDetail(-1);
        long usage = ((TelephonyManager) this.mContext.getSystemService("phone")).getVtDataUsage();
        Slog.d(TAG, "VT call data usage = " + usage);
        NetworkStats vtSnapshot = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        Entry entry = new Entry();
        entry.iface = VT_INTERFACE;
        entry.uid = -6;
        entry.set = 0;
        entry.tag = 0;
        entry.rxBytes = usage / 2;
        entry.rxPackets = 0;
        entry.txBytes = usage - entry.rxBytes;
        entry.txPackets = 0;
        vtSnapshot.combineValues(entry);
        if (uidSnapshot != null) {
            uidSnapshot.combineAllValues(vtSnapshot);
        }
        NetworkStats tetherSnapshot = getNetworkStatsTethering();
        if (LOG_NET_STATS) {
            if (tetherSnapshot == null || tetherSnapshot.size() <= 0) {
                String str = TAG;
                StringBuilder append = new StringBuilder().append("getNetworkStatsUidDetail, tetherSnapshot is empty. null ? ");
                if (tetherSnapshot != null) {
                    z = false;
                }
                Slog.d(str, append.append(z).toString());
            } else {
                Slog.d(TAG, "getNetworkStatsUidDetail, tetherSnapshot is:");
                tetherSnapshot.logoutData("tetherSnapshot");
            }
        }
        uidSnapshot.combineAllValues(tetherSnapshot);
        uidSnapshot.combineAllValues(this.mUidOperations);
        if (ENG_DBG) {
            Slog.d(TAG, "uidSnapshot = " + uidSnapshot);
        }
        return uidSnapshot;
    }

    private NetworkStats getNetworkStatsUidDetailWithPids() throws RemoteException {
        return null;
    }

    private NetworkStats getNetworkStatsSummaryDevAndVt() throws RemoteException {
        NetworkStats devSnapshot = this.mNetworkManager.getNetworkStatsSummaryDev();
        long usage = ((TelephonyManager) this.mContext.getSystemService("phone")).getVtDataUsage();
        Slog.d(TAG, "VT call data usage = " + usage);
        NetworkStats vtSnapshot = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        Entry entry = new Entry();
        entry.iface = VT_INTERFACE;
        entry.uid = -1;
        entry.set = -1;
        entry.tag = 0;
        entry.rxBytes = usage / 2;
        entry.rxPackets = 0;
        entry.txBytes = usage - entry.rxBytes;
        entry.txPackets = 0;
        vtSnapshot.combineValues(entry);
        if (devSnapshot != null) {
            devSnapshot.combineAllValues(vtSnapshot);
        }
        if (ENG_DBG) {
            Slog.d(TAG, "devSnapshot = " + devSnapshot);
        }
        return devSnapshot;
    }

    private NetworkStats getNetworkStatsXtAndVt() throws RemoteException {
        NetworkStats xtSnapshotRead = this.mNetworkManager.getNetworkStatsSummaryXt();
        if (xtSnapshotRead == null) {
            xtSnapshotRead = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        }
        NetworkStats xtSnapshot = xtSnapshotRead;
        long usage = ((TelephonyManager) this.mContext.getSystemService("phone")).getVtDataUsage();
        Slog.d(TAG, "VT call data usage = " + usage);
        NetworkStats vtSnapshot = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        Entry entry = new Entry();
        entry.iface = VT_INTERFACE;
        entry.uid = -1;
        entry.set = -1;
        entry.tag = 0;
        entry.rxBytes = usage / 2;
        entry.rxPackets = 0;
        entry.txBytes = usage - entry.rxBytes;
        entry.txPackets = 0;
        vtSnapshot.combineValues(entry);
        if (xtSnapshot != null) {
            xtSnapshot.combineAllValues(vtSnapshot);
        }
        if (ENG_DBG) {
            Slog.d(TAG, "xtSnapshot = " + xtSnapshot);
        }
        return xtSnapshot;
    }

    private NetworkStats getNetworkStatsTethering() throws RemoteException {
        try {
            return this.mNetworkManager.getNetworkStatsTethering();
        } catch (IllegalStateException e) {
            Log.e(TAG, "problem reading network stats" + e);
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

    public void setUseTrustedTime(boolean b) {
        USE_TRUESTED_TIME = b;
    }

    private void registerReceiverForCtsAppInstallState() {
        this.mContext.registerReceiver(this.mCtsAppInstalledReceiver, new IntentFilter(ACTION_OPPO_CTS_APP_INSTALLED), null, this.mHandler);
    }

    private void unregisterReceiverForCtsAppInstallState() {
        this.mContext.unregisterReceiver(this.mCtsAppInstalledReceiver);
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
                int[] iArr = new int[1];
                iArr[0] = multiAppUid;
                removeUidsLocked(iArr);
                this.mWakeLock.release();
            } catch (Throwable th) {
                this.mWakeLock.release();
            }
        }
    }
}

package com.android.server.net;

import android.annotation.IntDef;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.IUidObserver;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager.Stub;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkIdentity;
import android.net.NetworkInfo;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.net.NetworkTemplate;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IDeviceIdleController;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.DebugUtils;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TrustedTime;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.NetworkManagementService;
import com.android.server.SystemConfig;
import com.android.server.am.OppoGameSpaceManager;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.job.controllers.JobStatus;
import com.android.server.oppo.IElsaManager;
import com.google.android.collect.Lists;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

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
public class NetworkPolicyManagerService extends Stub {
    private static final String ACTION_ALLOW_BACKGROUND = "com.android.server.net.action.ALLOW_BACKGROUND";
    private static final String ACTION_SNOOZE_WARNING = "com.android.server.net.action.SNOOZE_WARNING";
    private static final String ATTR_APP_ID = "appId";
    private static final String ATTR_CYCLE_DAY = "cycleDay";
    private static final String ATTR_CYCLE_TIMEZONE = "cycleTimezone";
    private static final String ATTR_INFERRED = "inferred";
    private static final String ATTR_LAST_LIMIT_SNOOZE = "lastLimitSnooze";
    private static final String ATTR_LAST_SNOOZE = "lastSnooze";
    private static final String ATTR_LAST_WARNING_SNOOZE = "lastWarningSnooze";
    private static final String ATTR_LIMIT_BYTES = "limitBytes";
    private static final String ATTR_METERED = "metered";
    private static final String ATTR_NETWORK_ID = "networkId";
    private static final String ATTR_NETWORK_TEMPLATE = "networkTemplate";
    private static final String ATTR_POLICY = "policy";
    private static final String ATTR_RESTRICT_BACKGROUND = "restrictBackground";
    private static final String ATTR_SUBSCRIBER_ID = "subscriberId";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_WARNING_BYTES = "warningBytes";
    private static final int CHAIN_TOGGLE_DISABLE = 2;
    private static final int CHAIN_TOGGLE_ENABLE = 1;
    private static final int CHAIN_TOGGLE_NONE = 0;
    private static final boolean ENG_DBG = false;
    private static final String INVALID_SUBSCRIBER_ID = "FFFFFFFFFFFFFFF";
    private static boolean LOGD = false;
    private static boolean LOGV = false;
    private static final int MSG_ADVISE_PERSIST_THRESHOLD = 7;
    private static final int MSG_INTERFACE_DOWN = 14;
    private static final int MSG_LIMIT_REACHED = 5;
    private static final int MSG_METERED_IFACES_CHANGED = 2;
    private static final int MSG_REMOVE_INTERFACE_QUOTA = 11;
    private static final int MSG_RESTRICT_BACKGROUND_BLACKLIST_CHANGED = 12;
    private static final int MSG_RESTRICT_BACKGROUND_CHANGED = 6;
    private static final int MSG_RESTRICT_BACKGROUND_WHITELIST_CHANGED = 9;
    private static final int MSG_RULES_CHANGED = 1;
    private static final int MSG_SET_FIREWALL_RULES = 13;
    private static final int MSG_UPDATE_INTERFACE_QUOTA = 10;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    static final String TAG = "NetworkPolicy";
    private static final String TAG_APP_POLICY = "app-policy";
    private static final String TAG_NETWORK_POLICY = "network-policy";
    private static final String TAG_POLICY_LIST = "policy-list";
    private static final String TAG_RESTRICT_BACKGROUND = "restrict-background";
    private static final String TAG_REVOKED_RESTRICT_BACKGROUND = "revoked-restrict-background";
    private static final String TAG_UID_POLICY = "uid-policy";
    private static final String TAG_WHITELIST = "whitelist";
    private static final long TIME_CACHE_MAX_AGE = 86400000;
    public static final int TYPE_LIMIT = 2;
    public static final int TYPE_LIMIT_SNOOZED = 3;
    private static final int TYPE_RESTRICT_BACKGROUND = 1;
    private static final int TYPE_RESTRICT_POWER = 2;
    public static final int TYPE_WARNING = 1;
    private static final int VERSION_ADDED_INFERRED = 7;
    private static final int VERSION_ADDED_METERED = 4;
    private static final int VERSION_ADDED_NETWORK_ID = 9;
    private static final int VERSION_ADDED_RESTRICT_BACKGROUND = 3;
    private static final int VERSION_ADDED_SNOOZE = 2;
    private static final int VERSION_ADDED_TIMEZONE = 6;
    private static final int VERSION_INIT = 1;
    private static final int VERSION_LATEST = 10;
    private static final int VERSION_SPLIT_SNOOZE = 5;
    private static final int VERSION_SWITCH_APP_ID = 8;
    private static final int VERSION_SWITCH_UID = 10;
    @GuardedBy("mNetworkPoliciesSecondLock")
    private final ArraySet<String> mActiveNotifs;
    private final IActivityManager mActivityManager;
    private final INetworkManagementEventObserver mAlertObserver;
    private final BroadcastReceiver mAllowReceiver;
    private final AppOpsManager mAppOps;
    private IConnectivityManager mConnManager;
    private BroadcastReceiver mConnReceiver;
    private INetworkPolicyListener mConnectivityListener;
    private final Context mContext;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mDefaultRestrictBackgroundWhitelistUids;
    private IDeviceIdleController mDeviceIdleController;
    @GuardedBy("mUidRulesFirstLock")
    volatile boolean mDeviceIdleMode;
    @GuardedBy("mUidRulesFirstLock")
    final SparseBooleanArray mFirewallChainStates;
    private boolean mGameSpaceMode;
    final Handler mHandler;
    private Callback mHandlerCallback;
    private final IPackageManager mIPm;
    private final RemoteCallbackList<INetworkPolicyListener> mListeners;
    @GuardedBy("mNetworkPoliciesSecondLock")
    private ArraySet<String> mMeteredIfaces;
    private final INetworkManagementService mNetworkManager;
    final Object mNetworkPoliciesSecondLock;
    final ArrayMap<NetworkTemplate, NetworkPolicy> mNetworkPolicy;
    final ArrayMap<NetworkPolicy, String[]> mNetworkRules;
    private final INetworkStatsService mNetworkStats;
    final ArrayMap<String, Boolean> mNetworkTemplateOverLimitState;
    final ArrayMap<String, Boolean> mNetworkTemplateOverWarningState;
    private INotificationManager mNotifManager;
    @GuardedBy("mNetworkPoliciesSecondLock")
    private final ArraySet<NetworkTemplate> mOverLimitNotified;
    private final BroadcastReceiver mPackageReceiver;
    @GuardedBy("allLocks")
    private final AtomicFile mPolicyFile;
    private PowerManagerInternal mPowerManagerInternal;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mPowerSaveTempWhitelistAppIds;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mPowerSaveWhitelistAppIds;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mPowerSaveWhitelistExceptIdleAppIds;
    private final BroadcastReceiver mPowerSaveWhitelistReceiver;
    @GuardedBy("mUidRulesFirstLock")
    volatile boolean mRestrictBackground;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mRestrictBackgroundWhitelistRevokedUids;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mRestrictBackgroundWhitelistUids;
    @GuardedBy("mUidRulesFirstLock")
    volatile boolean mRestrictPower;
    private final BroadcastReceiver mSimStateReceiver;
    private final BroadcastReceiver mSnoozeWarningReceiver;
    private final BroadcastReceiver mStatsReceiver;
    private final boolean mSuppressDefaultPolicy;
    @GuardedBy("allLocks")
    volatile boolean mSystemReady;
    private final Runnable mTempPowerSaveChangedCallback;
    private final TrustedTime mTime;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidFirewallDozableRules;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidFirewallPowerSaveRules;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidFirewallStandbyRules;
    private final IUidObserver mUidObserver;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidPolicy;
    private final BroadcastReceiver mUidRemovedReceiver;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidRules;
    final Object mUidRulesFirstLock;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidState;
    private UsageStatsManagerInternal mUsageStats;
    private final UserManager mUserManager;
    private final BroadcastReceiver mUserReceiver;
    private final BroadcastReceiver mWifiConfigReceiver;
    private final BroadcastReceiver mWifiStateReceiver;

    private class AppIdleStateChangeListener extends android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener {
        /* synthetic */ AppIdleStateChangeListener(NetworkPolicyManagerService this$0, AppIdleStateChangeListener appIdleStateChangeListener) {
            this();
        }

        private AppIdleStateChangeListener() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle) {
            try {
                int uid = NetworkPolicyManagerService.this.mContext.getPackageManager().getPackageUidAsUser(packageName, DumpState.DUMP_PREFERRED_XML, userId);
                if (NetworkPolicyManagerService.LOGV && NetworkPolicyManagerService.ENG_DBG) {
                    Log.v(NetworkPolicyManagerService.TAG, "onAppIdleStateChanged(): uid=" + uid + ", idle=" + idle);
                }
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.updateRuleForAppIdleUL(uid);
                    NetworkPolicyManagerService.this.updateRulesForPowerRestrictionsUL(uid);
                }
            } catch (NameNotFoundException e) {
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                NetworkPolicyManagerService.this.updateRulesForAppIdleParoleUL();
            }
        }
    }

    @IntDef(flag = false, value = {0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChainToggleType {
    }

    private class NetworkPolicyManagerInternalImpl extends NetworkPolicyManagerInternal {
        /* synthetic */ NetworkPolicyManagerInternalImpl(NetworkPolicyManagerService this$0, NetworkPolicyManagerInternalImpl networkPolicyManagerInternalImpl) {
            this();
        }

        private NetworkPolicyManagerInternalImpl() {
        }

        public void resetUserState(int userId) {
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                boolean changed = NetworkPolicyManagerService.this.removeUserStateUL(userId, false);
                if (NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsUL(userId)) {
                    changed = true;
                }
                if (changed) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        NetworkPolicyManagerService.this.writePolicyAL();
                    }
                }
            }
        }
    }

    @IntDef(flag = false, value = {1, 2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RestrictType {
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.net.NetworkPolicyManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.net.NetworkPolicyManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.NetworkPolicyManagerService.<clinit>():void");
    }

    public void setDebugModeEnabled() {
        LOGD = true;
        LOGV = true;
        Slog.d(TAG, "LOGD=" + LOGD + " LOGV=" + LOGV);
    }

    public void setDebugModeDisabled() {
        LOGD = false;
        LOGV = false;
        Slog.d(TAG, "LOGD=" + LOGD + " LOGV=" + LOGV);
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, INetworkStatsService networkStats, INetworkManagementService networkManagement) {
        this(context, activityManager, networkStats, networkManagement, NtpTrustedTime.getInstance(context), getSystemDir(), false);
    }

    private static File getSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, INetworkStatsService networkStats, INetworkManagementService networkManagement, TrustedTime time, File systemDir, boolean suppressDefaultPolicy) {
        this.mUidRulesFirstLock = new Object();
        this.mNetworkPoliciesSecondLock = new Object();
        this.mGameSpaceMode = false;
        this.mNetworkPolicy = new ArrayMap();
        this.mNetworkRules = new ArrayMap();
        this.mNetworkTemplateOverWarningState = new ArrayMap();
        this.mNetworkTemplateOverLimitState = new ArrayMap();
        this.mUidPolicy = new SparseIntArray();
        this.mUidRules = new SparseIntArray();
        this.mUidFirewallStandbyRules = new SparseIntArray();
        this.mUidFirewallDozableRules = new SparseIntArray();
        this.mUidFirewallPowerSaveRules = new SparseIntArray();
        this.mFirewallChainStates = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistAppIds = new SparseBooleanArray();
        this.mPowerSaveTempWhitelistAppIds = new SparseBooleanArray();
        this.mRestrictBackgroundWhitelistUids = new SparseBooleanArray();
        this.mDefaultRestrictBackgroundWhitelistUids = new SparseBooleanArray();
        this.mRestrictBackgroundWhitelistRevokedUids = new SparseBooleanArray();
        this.mMeteredIfaces = new ArraySet();
        this.mOverLimitNotified = new ArraySet();
        this.mActiveNotifs = new ArraySet();
        this.mUidState = new SparseIntArray();
        this.mListeners = new RemoteCallbackList();
        this.mUidObserver = new IUidObserver.Stub() {
            public void onUidStateChanged(int uid, int procState) throws RemoteException {
                Trace.traceBegin(8388608, "onUidStateChanged");
                try {
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        NetworkPolicyManagerService.this.updateUidStateUL(uid, procState);
                    }
                } finally {
                    Trace.traceEnd(8388608);
                }
            }

            public void onUidGone(int uid) throws RemoteException {
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.removeUidStateUL(uid);
                }
            }

            public void onUidActive(int uid) throws RemoteException {
            }

            public void onUidIdle(int uid) throws RemoteException {
            }
        };
        this.mPowerSaveWhitelistReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveWhitelistUL();
                    NetworkPolicyManagerService.this.updateRulesForRestrictPowerUL();
                    NetworkPolicyManagerService.this.updateRulesForAppIdleUL();
                }
            }
        };
        this.mTempPowerSaveChangedCallback = new Runnable() {
            public void run() {
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveTempWhitelistUL();
                    NetworkPolicyManagerService.this.updateRulesForTempWhitelistChangeUL();
                    NetworkPolicyManagerService.this.purgePowerSaveTempWhitelistUL();
                }
            }
        };
        this.mPackageReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1 && "android.intent.action.PACKAGE_ADDED".equals(action)) {
                    if (NetworkPolicyManagerService.LOGV) {
                        Slog.v(NetworkPolicyManagerService.TAG, "ACTION_PACKAGE_ADDED for uid=" + uid);
                    }
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        if (NetworkPolicyManagerService.this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && !NetworkPolicyManagerService.this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                            if (NetworkPolicyManagerService.LOGV) {
                                Slog.v(NetworkPolicyManagerService.TAG, "add default white list back, uid=" + uid);
                            }
                            NetworkPolicyManagerService.this.addRestrictBackgroundWhitelistedUid(uid);
                        }
                        NetworkPolicyManagerService.this.updateRestrictionRulesForUidUL(uid);
                    }
                }
            }
        };
        this.mUidRemovedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1) {
                    if (NetworkPolicyManagerService.LOGV) {
                        Slog.v(NetworkPolicyManagerService.TAG, "ACTION_UID_REMOVED for uid=" + uid);
                    }
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        NetworkPolicyManagerService.this.mUidPolicy.delete(uid);
                        NetworkPolicyManagerService.this.removeRestrictBackgroundWhitelistedUidUL(uid, true, true);
                        NetworkPolicyManagerService.this.updateRestrictionRulesForUidUL(uid);
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            NetworkPolicyManagerService.this.writePolicyAL();
                        }
                    }
                }
            }
        };
        this.mUserReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId != -1) {
                    if (action.equals("android.intent.action.USER_REMOVED") || action.equals("android.intent.action.USER_ADDED")) {
                        synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                            NetworkPolicyManagerService.this.removeUserStateUL(userId, true);
                            if (action == "android.intent.action.USER_ADDED") {
                                NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsUL(userId);
                            }
                            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                                NetworkPolicyManagerService.this.updateRulesForGlobalChangeAL(true);
                            }
                        }
                    }
                }
            }
        };
        this.mStatsReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                    NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                    NetworkPolicyManagerService.this.updateNotificationsNL();
                }
            }
        };
        this.mAllowReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.setRestrictBackground(false);
            }
        };
        this.mSnoozeWarningReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.performSnooze((NetworkTemplate) intent.getParcelableExtra("android.net.NETWORK_TEMPLATE"), 1);
            }
        };
        this.mWifiConfigReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("changeReason", 0) == 1) {
                    WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                    if (config.SSID != null) {
                        NetworkTemplate template = NetworkTemplate.buildTemplateWifi(config.SSID);
                        synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                                if (NetworkPolicyManagerService.this.mNetworkPolicy.containsKey(template)) {
                                    NetworkPolicyManagerService.this.mNetworkPolicy.remove(template);
                                    NetworkPolicyManagerService.this.writePolicyAL();
                                }
                            }
                        }
                    }
                }
            }
        };
        this.mWifiStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (((NetworkInfo) intent.getParcelableExtra("networkInfo")).isConnected()) {
                    WifiInfo info = (WifiInfo) intent.getParcelableExtra("wifiInfo");
                    boolean meteredHint = info.getMeteredHint();
                    NetworkTemplate template = NetworkTemplate.buildTemplateWifi(info.getSSID());
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            NetworkPolicy policy = (NetworkPolicy) NetworkPolicyManagerService.this.mNetworkPolicy.get(template);
                            if (policy == null && meteredHint) {
                                NetworkPolicyManagerService.this.addNetworkPolicyNL(NetworkPolicyManagerService.newWifiPolicy(template, meteredHint));
                            } else if (policy != null) {
                                if (policy.inferred) {
                                    policy.metered = meteredHint;
                                    new Handler().post(new Runnable() {
                                        public void run() {
                                            Log.i(NetworkPolicyManagerService.TAG, "tyl runnable updateNetworkRulesLocked");
                                            NetworkPolicyManagerService.this.updateNetworkRulesNL();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        };
        this.mAlertObserver = new BaseNetworkObserver() {
            public void limitReached(String limitName, String iface) {
                NetworkPolicyManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkPolicyManagerService.TAG);
                if (!NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                    NetworkPolicyManagerService.this.mHandler.obtainMessage(5, iface).sendToTarget();
                }
            }

            public void interfaceRemoved(String iface) {
                Slog.d(NetworkPolicyManagerService.TAG, "interfaceRemoved: " + iface);
                if (iface.contains("ccmni") || iface.contains("ppp") || iface.contains("cc2mni") || iface.contains("ccemni")) {
                    NetworkPolicyManagerService.this.mHandler.obtainMessage(14, iface).sendToTarget();
                } else {
                    Slog.d(NetworkPolicyManagerService.TAG, "interfaceRemoved: ignore " + iface);
                }
            }
        };
        this.mConnReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("com.mediatek.conn.MMS_CONNECTIVITY".equals(intent.getAction())) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        NetworkPolicyManagerService.this.normalizePoliciesNL();
                        NetworkPolicyManagerService.this.updateNetworkRulesNL();
                    }
                    return;
                }
                NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        NetworkPolicyManagerService.this.ensureActiveMobilePolicyNL();
                        NetworkPolicyManagerService.this.normalizePoliciesNL();
                        NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                        NetworkPolicyManagerService.this.updateNetworkRulesNL();
                        NetworkPolicyManagerService.this.updateNotificationsNL();
                    }
                }
            }
        };
        this.mSimStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    String simState = intent.getStringExtra("ss");
                    if ("LOADED".equals(simState) || "LOCKED".equals(simState) || "ABSENT".equals(simState)) {
                        Slog.d(NetworkPolicyManagerService.TAG, "receive ACTION_SIM_STATE_CHANGED");
                        NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                        synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                                NetworkPolicyManagerService.this.ensureActiveMobilePolicyNL();
                                NetworkPolicyManagerService.this.normalizePoliciesNL();
                                NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                                NetworkPolicyManagerService.this.updateNetworkRulesNL();
                                NetworkPolicyManagerService.this.updateNotificationsNL();
                            }
                        }
                    }
                }
            }
        };
        this.mHandlerCallback = new Callback() {
            public boolean handleMessage(Message msg) {
                if (NetworkPolicyManagerService.LOGV) {
                    Log.v(NetworkPolicyManagerService.TAG, "handleMessage(): msg=" + msg.what);
                }
                int uid;
                int length;
                int i;
                String iface;
                Intent intent;
                switch (msg.what) {
                    case 1:
                        uid = msg.arg1;
                        int uidRules = msg.arg2;
                        NetworkPolicyManagerService.this.dispatchUidRulesChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, uidRules);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchUidRulesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, uidRules);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 2:
                        String[] meteredIfaces = msg.obj;
                        NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged(NetworkPolicyManagerService.this.mConnectivityListener, meteredIfaces);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), meteredIfaces);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 5:
                        iface = msg.obj;
                        NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            if (NetworkPolicyManagerService.this.mMeteredIfaces.contains(iface)) {
                                try {
                                    NetworkPolicyManagerService.this.mNetworkStats.forceUpdate();
                                } catch (RemoteException e) {
                                }
                                NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                                NetworkPolicyManagerService.this.updateNotificationsNL();
                            }
                        }
                        return true;
                    case 6:
                        boolean restrictBackground = msg.arg1 != 0;
                        NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged(NetworkPolicyManagerService.this.mConnectivityListener, restrictBackground);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), restrictBackground);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                        intent.setFlags(1073741824);
                        NetworkPolicyManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                        return true;
                    case 7:
                        try {
                            NetworkPolicyManagerService.this.mNetworkStats.advisePersistThreshold(((Long) msg.obj).longValue() / 1000);
                        } catch (RemoteException e2) {
                        }
                        return true;
                    case 9:
                        uid = msg.arg1;
                        boolean changed = msg.arg2 == 1;
                        Boolean whitelisted = msg.obj;
                        if (whitelisted != null) {
                            boolean whitelistedBool = whitelisted.booleanValue();
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundWhitelistChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, whitelistedBool);
                            length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                            for (i = 0; i < length; i++) {
                                NetworkPolicyManagerService.this.dispatchRestrictBackgroundWhitelistChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, whitelistedBool);
                            }
                            NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        }
                        String[] packages = NetworkPolicyManagerService.this.mContext.getPackageManager().getPackagesForUid(uid);
                        if (changed && packages != null) {
                            int userId = UserHandle.getUserId(uid);
                            for (String packageName : packages) {
                                intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                                intent.setPackage(packageName);
                                intent.setFlags(1073741824);
                                NetworkPolicyManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId));
                            }
                        }
                        return true;
                    case 10:
                        NetworkPolicyManagerService.this.setInterfaceQuota((String) msg.obj, (((long) msg.arg1) << 32) | (((long) msg.arg2) & 4294967295L));
                        return true;
                    case 11:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        return true;
                    case 12:
                        uid = msg.arg1;
                        boolean blacklisted = msg.arg2 == 1;
                        NetworkPolicyManagerService.this.dispatchRestrictBackgroundBlacklistChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, blacklisted);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundBlacklistChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, blacklisted);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 13:
                        int chain = msg.arg1;
                        int toggle = msg.arg2;
                        SparseIntArray uidRules2 = msg.obj;
                        if (uidRules2 != null) {
                            NetworkPolicyManagerService.this.setUidFirewallRules(chain, uidRules2);
                        }
                        if (toggle != 0) {
                            NetworkPolicyManagerService.this.enableFirewallChainUL(chain, toggle == 1);
                        }
                        return true;
                    case 14:
                        iface = (String) msg.obj;
                        NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            Slog.d(NetworkPolicyManagerService.TAG, " MSG_INTERFACE_DOWN call updateNetworkRulesNL");
                            NetworkPolicyManagerService.this.updateNetworkRulesNL();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        };
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing context");
        this.mActivityManager = (IActivityManager) Preconditions.checkNotNull(activityManager, "missing activityManager");
        this.mNetworkStats = (INetworkStatsService) Preconditions.checkNotNull(networkStats, "missing networkStats");
        this.mNetworkManager = (INetworkManagementService) Preconditions.checkNotNull(networkManagement, "missing networkManagement");
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mTime = (TrustedTime) Preconditions.checkNotNull(time, "missing TrustedTime");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mIPm = AppGlobals.getPackageManager();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new Handler(thread.getLooper(), this.mHandlerCallback);
        this.mSuppressDefaultPolicy = suppressDefaultPolicy;
        this.mPolicyFile = new AtomicFile(new File(systemDir, "netpolicy.xml"));
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        LocalServices.addService(NetworkPolicyManagerInternal.class, new NetworkPolicyManagerInternalImpl(this, null));
    }

    public void bindConnectivityManager(IConnectivityManager connManager) {
        this.mConnManager = (IConnectivityManager) Preconditions.checkNotNull(connManager, "missing IConnectivityManager");
    }

    public void bindNotificationManager(INotificationManager notifManager) {
        this.mNotifManager = (INotificationManager) Preconditions.checkNotNull(notifManager, "missing INotificationManager");
    }

    void updatePowerSaveWhitelistUL() {
        int i = 0;
        try {
            int length;
            int[] whitelist = this.mDeviceIdleController.getAppIdWhitelistExceptIdle();
            this.mPowerSaveWhitelistExceptIdleAppIds.clear();
            if (whitelist != null) {
                for (int uid : whitelist) {
                    this.mPowerSaveWhitelistExceptIdleAppIds.put(uid, true);
                }
            }
            whitelist = this.mDeviceIdleController.getAppIdWhitelist();
            this.mPowerSaveWhitelistAppIds.clear();
            if (whitelist != null) {
                length = whitelist.length;
                while (i < length) {
                    this.mPowerSaveWhitelistAppIds.put(whitelist[i], true);
                    i++;
                }
            }
        } catch (RemoteException e) {
        }
    }

    boolean addDefaultRestrictBackgroundWhitelistUidsUL() {
        List<UserInfo> users = this.mUserManager.getUsers();
        int numberUsers = users.size();
        boolean changed = false;
        for (int i = 0; i < numberUsers; i++) {
            if (addDefaultRestrictBackgroundWhitelistUidsUL(((UserInfo) users.get(i)).id)) {
                changed = true;
            }
        }
        return changed;
    }

    private boolean addDefaultRestrictBackgroundWhitelistUidsUL(int userId) {
        SystemConfig sysConfig = SystemConfig.getInstance();
        PackageManager pm = this.mContext.getPackageManager();
        ArraySet<String> allowDataUsage = sysConfig.getAllowInDataUsageSave();
        boolean changed = false;
        for (int i = 0; i < allowDataUsage.size(); i++) {
            String pkg = (String) allowDataUsage.valueAt(i);
            if (LOGD) {
                Slog.d(TAG, "checking restricted background whitelisting for package " + pkg + " and user " + userId);
            }
            try {
                ApplicationInfo app = pm.getApplicationInfoAsUser(pkg, DumpState.DUMP_DEXOPT, userId);
                if (app.isPrivilegedApp()) {
                    int uid = UserHandle.getUid(userId, app.uid);
                    this.mDefaultRestrictBackgroundWhitelistUids.append(uid, true);
                    if (LOGD) {
                        Slog.d(TAG, "Adding uid " + uid + " (user " + userId + ") to default restricted " + "background whitelist. Revoked status: " + this.mRestrictBackgroundWhitelistRevokedUids.get(uid));
                    }
                    if (!this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                        if (LOGD) {
                            Slog.d(TAG, "adding default package " + pkg + " (uid " + uid + " for user " + userId + ") to restrict background whitelist");
                        }
                        this.mRestrictBackgroundWhitelistUids.append(uid, true);
                        changed = true;
                    }
                } else {
                    Slog.e(TAG, "addDefaultRestrictBackgroundWhitelistUidsUL(): skipping non-privileged app  " + pkg);
                }
            } catch (NameNotFoundException e) {
                if (LOGD) {
                    Slog.d(TAG, "No ApplicationInfo for package " + pkg);
                }
            }
        }
        return changed;
    }

    void updatePowerSaveTempWhitelistUL() {
        try {
            int N = this.mPowerSaveTempWhitelistAppIds.size();
            for (int i = 0; i < N; i++) {
                this.mPowerSaveTempWhitelistAppIds.setValueAt(i, false);
            }
            int[] whitelist = this.mDeviceIdleController.getAppIdTempWhitelist();
            if (whitelist != null) {
                for (int uid : whitelist) {
                    this.mPowerSaveTempWhitelistAppIds.put(uid, true);
                }
            }
        } catch (RemoteException e) {
        }
    }

    void purgePowerSaveTempWhitelistUL() {
        for (int i = this.mPowerSaveTempWhitelistAppIds.size() - 1; i >= 0; i--) {
            if (!this.mPowerSaveTempWhitelistAppIds.valueAt(i)) {
                this.mPowerSaveTempWhitelistAppIds.removeAt(i);
            }
        }
    }

    public void systemReady() {
        Trace.traceBegin(8388608, "systemReady");
        try {
            if (isBandwidthControlEnabled()) {
                this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
                synchronized (this.mUidRulesFirstLock) {
                    synchronized (this.mNetworkPoliciesSecondLock) {
                        updatePowerSaveWhitelistUL();
                        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
                            public void onLowPowerModeChanged(boolean enabled) {
                                if (NetworkPolicyManagerService.LOGD) {
                                    Slog.d(NetworkPolicyManagerService.TAG, "onLowPowerModeChanged(" + enabled + ")");
                                }
                                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                                    if (NetworkPolicyManagerService.this.mRestrictPower != enabled) {
                                        NetworkPolicyManagerService.this.mRestrictPower = enabled;
                                        NetworkPolicyManagerService.this.updateRulesForRestrictPowerUL();
                                    }
                                }
                            }
                        });
                        this.mRestrictPower = this.mPowerManagerInternal.getLowPowerModeEnabled();
                        this.mSystemReady = true;
                        readPolicyAL();
                        if (addDefaultRestrictBackgroundWhitelistUidsUL()) {
                            writePolicyAL();
                        }
                        setRestrictBackgroundUL(this.mRestrictBackground);
                        updateRulesForGlobalChangeAL(false);
                        updateNotificationsNL();
                    }
                }
                try {
                    this.mActivityManager.registerUidObserver(this.mUidObserver, 3);
                    this.mNetworkManager.registerObserver(this.mAlertObserver);
                } catch (RemoteException e) {
                }
                IntentFilter whitelistFilter = new IntentFilter("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
                this.mContext.registerReceiver(this.mPowerSaveWhitelistReceiver, whitelistFilter, null, this.mHandler);
                ((LocalService) LocalServices.getService(LocalService.class)).setNetworkPolicyTempWhitelistCallback(this.mTempPowerSaveChangedCallback);
                IntentFilter connFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
                connFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
                connFilter.addAction("com.mediatek.conn.MMS_CONNECTIVITY");
                this.mContext.registerReceiver(this.mConnReceiver, connFilter, "android.permission.CONNECTIVITY_INTERNAL", this.mHandler);
                IntentFilter mSimFilter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
                this.mContext.registerReceiver(this.mSimStateReceiver, mSimFilter);
                IntentFilter packageFilter = new IntentFilter();
                packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
                packageFilter.addDataScheme("package");
                this.mContext.registerReceiver(this.mPackageReceiver, packageFilter, null, this.mHandler);
                this.mContext.registerReceiver(this.mUidRemovedReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
                IntentFilter userFilter = new IntentFilter();
                userFilter.addAction("android.intent.action.USER_ADDED");
                userFilter.addAction("android.intent.action.USER_REMOVED");
                this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
                IntentFilter statsFilter = new IntentFilter(NetworkStatsService.ACTION_NETWORK_STATS_UPDATED);
                this.mContext.registerReceiver(this.mStatsReceiver, statsFilter, "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
                IntentFilter allowFilter = new IntentFilter(ACTION_ALLOW_BACKGROUND);
                this.mContext.registerReceiver(this.mAllowReceiver, allowFilter, "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
                IntentFilter snoozeWarningFilter = new IntentFilter(ACTION_SNOOZE_WARNING);
                this.mContext.registerReceiver(this.mSnoozeWarningReceiver, snoozeWarningFilter, "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
                IntentFilter intentFilter = new IntentFilter("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
                this.mContext.registerReceiver(this.mWifiConfigReceiver, intentFilter, null, this.mHandler);
                IntentFilter wifiStateFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
                this.mContext.registerReceiver(this.mWifiStateReceiver, wifiStateFilter, null, this.mHandler);
                this.mUsageStats.addAppIdleStateChangeListener(new AppIdleStateChangeListener(this, null));
                Trace.traceEnd(8388608);
                return;
            }
            Slog.w(TAG, "bandwidth controls disabled, unable to enforce policy");
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    static NetworkPolicy newWifiPolicy(NetworkTemplate template, boolean metered) {
        return new NetworkPolicy(template, -1, "UTC", -1, -1, -1, -1, metered, true);
    }

    void updateNotificationsNL() {
        int i;
        if (LOGV) {
            Slog.v(TAG, "updateNotificationsNL()");
        }
        ArraySet<String> beforeNotifs = new ArraySet(this.mActiveNotifs);
        this.mActiveNotifs.clear();
        long currentTime = currentTimeMillis();
        for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (isTemplateRelevant(policy.template) && policy.hasCycle()) {
                long start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                long end = currentTime;
                long totalBytes = getTotalBytes(policy.template, start, currentTime);
                if (!policy.isOverLimit(totalBytes)) {
                    notifyUnderLimitNL(policy.template);
                    if (policy.isOverWarning(totalBytes) && policy.lastWarningSnooze < start) {
                        enqueueNotification(policy, 1, totalBytes);
                    }
                } else if (policy.lastLimitSnooze >= start) {
                    enqueueNotification(policy, 3, totalBytes);
                } else {
                    enqueueNotification(policy, 2, totalBytes);
                    notifyOverLimitNL(policy.template);
                    if (!((TelecomManager) this.mContext.getSystemService("telecom")).isInVideoCall()) {
                        notifyOverLimitNL(policy.template);
                    } else if (!"OP12".equals(SystemProperties.get("ro.operator.optr", IElsaManager.EMPTY_PACKAGE))) {
                        notifyOverLimitNL(policy.template);
                    }
                }
            }
        }
        for (i = beforeNotifs.size() - 1; i >= 0; i--) {
            String tag = (String) beforeNotifs.valueAt(i);
            if (!this.mActiveNotifs.contains(tag)) {
                cancelNotification(tag);
            }
        }
    }

    private boolean isTemplateRelevant(NetworkTemplate template) {
        if (!template.isMatchRuleMobile()) {
            return true;
        }
        TelephonyManager tele = TelephonyManager.from(this.mContext);
        for (int subId : SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList()) {
            if (template.matches(new NetworkIdentity(0, 0, tele.getSubscriberId(subId), null, false, true))) {
                return true;
            }
        }
        return false;
    }

    private void notifyOverLimitNL(NetworkTemplate template) {
        if (!this.mOverLimitNotified.contains(template)) {
            if (ENG_DBG) {
                Slog.v(TAG, "notifyOverLimitNL for" + template);
            }
            this.mContext.startActivityAsUser(buildNetworkOverLimitIntent(template), UserHandle.CURRENT);
            this.mOverLimitNotified.add(template);
        }
    }

    private void notifyUnderLimitNL(NetworkTemplate template) {
        this.mOverLimitNotified.remove(template);
    }

    private String buildNotificationTag(NetworkPolicy policy, int type) {
        return "NetworkPolicy:" + policy.template.hashCode() + ":" + type;
    }

    private void enqueueNotification(NetworkPolicy policy, int type, long totalBytes) {
        String tag = buildNotificationTag(policy, type);
        Builder builder = new Builder(this.mContext);
        builder.setOnlyAlertOnce(true);
        builder.setWhen(0);
        builder.setColor(this.mContext.getColor(17170523));
        Resources res = this.mContext.getResources();
        CharSequence title;
        CharSequence body;
        switch (type) {
            case 1:
                title = res.getText(17040603);
                body = res.getString(17040604);
                builder.setSmallIcon(17301624);
                builder.setTicker(title);
                builder.setContentTitle(title);
                builder.setContentText(body);
                builder.setDefaults(-1);
                builder.setPriority(1);
                builder.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, buildSnoozeWarningIntent(policy.template), 134217728));
                builder = builder;
                builder.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, buildViewDataUsageIntent(policy.template), 134217728, null, UserHandle.CURRENT));
                break;
            case 2:
                body = res.getText(17040609);
                int icon = 17303253;
                switch (policy.template.getMatchRule()) {
                    case 1:
                        title = res.getText(17040607);
                        break;
                    case 2:
                        title = res.getText(17040605);
                        break;
                    case 3:
                        title = res.getText(17040606);
                        break;
                    case 4:
                        title = res.getText(17040608);
                        icon = 17301624;
                        break;
                    default:
                        title = null;
                        break;
                }
                builder.setOngoing(true);
                builder.setSmallIcon(icon);
                builder.setTicker(title);
                builder.setContentTitle(title);
                builder.setContentText(body);
                builder = builder;
                builder.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, buildNetworkOverLimitIntent(policy.template), 134217728, null, UserHandle.CURRENT));
                break;
            case 3:
                Object[] objArr = new Object[1];
                objArr[0] = Formatter.formatFileSize(this.mContext, totalBytes - policy.limitBytes);
                body = res.getString(17040614, objArr);
                switch (policy.template.getMatchRule()) {
                    case 1:
                        title = res.getText(17040612);
                        break;
                    case 2:
                        title = res.getText(17040610);
                        break;
                    case 3:
                        title = res.getText(17040611);
                        break;
                    case 4:
                        title = res.getText(17040613);
                        break;
                    default:
                        title = null;
                        break;
                }
                builder.setOngoing(true);
                builder.setSmallIcon(17301624);
                builder.setTicker(title);
                builder.setContentTitle(title);
                builder.setContentText(body);
                builder = builder;
                builder.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, buildViewDataUsageIntent(policy.template), 134217728, null, UserHandle.CURRENT));
                break;
        }
        try {
            String packageName = this.mContext.getPackageName();
            String str = packageName;
            this.mNotifManager.enqueueNotificationWithTag(packageName, str, tag, 0, builder.getNotification(), new int[1], -1);
            this.mActiveNotifs.add(tag);
        } catch (RemoteException e) {
        }
    }

    private void cancelNotification(String tag) {
        try {
            this.mNotifManager.cancelNotificationWithTag(this.mContext.getPackageName(), tag, 0, -1);
        } catch (RemoteException e) {
        }
    }

    void updateNetworkEnabledNL() {
        if (LOGV) {
            Slog.v(TAG, "updateNetworkEnabledNL()");
        }
        long currentTime = currentTimeMillis();
        boolean overWarningLimitStateChanged = false;
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            Slog.v(TAG, "cp:" + policy);
            if (policy.limitBytes == -1 || !policy.hasCycle()) {
                setNetworkTemplateEnabled(policy.template, true);
                overWarningLimitStateChanged = !overWarningLimitStateChanged ? setNetworkTemplateOverLimit(policy.template, false) : true;
            } else {
                long start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                long end = currentTime;
                long totalBytes = getTotalBytes(policy.template, start, currentTime);
                boolean overLimitWithoutSnooze = policy.isOverLimit(totalBytes) ? policy.lastLimitSnooze < start : false;
                setNetworkTemplateEnabled(policy.template, !overLimitWithoutSnooze);
                boolean overWarningWithoutSnooze = policy.isOverWarning(totalBytes) ? policy.lastLimitSnooze < start : false;
                overWarningLimitStateChanged = !(!overWarningLimitStateChanged ? setNetworkTemplateOverWarning(policy.template, overWarningWithoutSnooze) : true) ? setNetworkTemplateOverLimit(policy.template, overLimitWithoutSnooze) : true;
            }
        }
        if (overWarningLimitStateChanged) {
            sendNetworkOverLimitChanged();
        }
    }

    public boolean getNetworkTemplateOverWarning(String subscriberId) {
        Slog.v(TAG, "Call getNetworkTemplateOverWarning:" + subscriberId);
        if (this.mNetworkTemplateOverWarningState.containsKey(subscriberId)) {
            Slog.v(TAG, "return:" + this.mNetworkTemplateOverWarningState.get(subscriberId));
            return ((Boolean) this.mNetworkTemplateOverWarningState.get(subscriberId)).booleanValue();
        }
        Slog.v(TAG, "return false due to not exist.");
        return false;
    }

    public boolean getNetworkTemplateOverLimit(String subscriberId) {
        Slog.v(TAG, "Call getNetworkTemplateOverLimit:" + subscriberId);
        if (this.mNetworkTemplateOverLimitState.containsKey(subscriberId)) {
            Slog.v(TAG, "return:" + this.mNetworkTemplateOverLimitState.get(subscriberId));
            return ((Boolean) this.mNetworkTemplateOverLimitState.get(subscriberId)).booleanValue();
        }
        Slog.v(TAG, "return false due to not exist.");
        return false;
    }

    private boolean setNetworkTemplateOverWarning(NetworkTemplate template, boolean enabled) {
        switch (template.getMatchRule()) {
            case 1:
            case 2:
            case 3:
                String subscriberId = template.getSubscriberId();
                Slog.v(TAG, "setNetworkTemplateOverWarning subscriberId:" + subscriberId + ":" + enabled);
                if (!this.mNetworkTemplateOverWarningState.containsKey(subscriberId)) {
                    this.mNetworkTemplateOverWarningState.put(subscriberId, Boolean.valueOf(enabled));
                    return true;
                } else if (((Boolean) this.mNetworkTemplateOverWarningState.get(subscriberId)).equals(Boolean.valueOf(enabled))) {
                    return false;
                } else {
                    this.mNetworkTemplateOverWarningState.put(subscriberId, Boolean.valueOf(enabled));
                    return true;
                }
            default:
                return false;
        }
    }

    private boolean setNetworkTemplateOverLimit(NetworkTemplate template, boolean enabled) {
        boolean changed = false;
        switch (template.getMatchRule()) {
            case 1:
            case 2:
            case 3:
                String subscriberId = template.getSubscriberId();
                Slog.v(TAG, "setNetworkTemplateOverLimit subscriberId:" + subscriberId + ":" + enabled);
                if (this.mNetworkTemplateOverLimitState.containsKey(subscriberId)) {
                    if (!((Boolean) this.mNetworkTemplateOverLimitState.get(subscriberId)).equals(Boolean.valueOf(enabled))) {
                        this.mNetworkTemplateOverLimitState.put(subscriberId, Boolean.valueOf(enabled));
                        changed = true;
                        break;
                    }
                    changed = false;
                    break;
                }
                this.mNetworkTemplateOverLimitState.put(subscriberId, Boolean.valueOf(enabled));
                changed = true;
                break;
        }
        if (changed) {
            Slog.v(TAG, "setNetworkTemplateOverLimit return:" + changed);
        }
        return changed;
    }

    private void setNetworkTemplateEnabled(NetworkTemplate template, boolean enabled) {
        if (LOGD) {
            Log.d(TAG, "setNetworkTemplateEnabled:" + enabled + ":on:" + template);
        }
        if (template.getMatchRule() == 1) {
            SubscriptionManager sm = SubscriptionManager.from(this.mContext);
            TelephonyManager tm = TelephonyManager.from(this.mContext);
            for (int subId : sm.getActiveSubscriptionIdList()) {
                if (template.matches(new NetworkIdentity(0, 0, tm.getSubscriberId(subId), null, false, true))) {
                    if (LOGD) {
                        Log.d(TAG, "setPolicyDataEnabled:" + enabled + ":on:" + subId);
                    }
                    tm.setPolicyDataEnabled(enabled, subId);
                }
            }
        }
    }

    void updateNetworkRulesNL() {
        if (LOGV) {
            Slog.v(TAG, "updateNetworkRulesNL()");
        }
        String mmsifacename = null;
        try {
            int i;
            NetworkPolicy policy;
            String iface;
            NetworkState[] states = this.mConnManager.getAllNetworkState();
            Network mmsNetwork = this.mConnManager.getNetworkIfCreated(new NetworkRequest.Builder().addTransportType(0).addCapability(0).build());
            if (ENG_DBG) {
                Slog.d(TAG, "getNetworkIfCreated: mmsNetwork =" + mmsNetwork);
            }
            if (mmsNetwork != null) {
                LinkProperties netProperties = this.mConnManager.getLinkProperties(mmsNetwork);
                if (netProperties != null) {
                    mmsifacename = netProperties.getInterfaceName();
                }
                Slog.v(TAG, "updateNetworkRulesLocked() mmsifacename=" + mmsifacename);
            }
            ArrayList<Pair<String, NetworkIdentity>> connIdents = new ArrayList(states.length);
            ArraySet<String> connIfaces = new ArraySet(states.length);
            for (NetworkState state : states) {
                if (!(state == null || state.networkInfo == null || !state.networkInfo.isConnected())) {
                    NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
                    String baseIface = state.linkProperties.getInterfaceName();
                    if (baseIface != null) {
                        connIdents.add(Pair.create(baseIface, ident));
                    }
                    for (LinkProperties stackedLink : state.linkProperties.getStackedLinks()) {
                        String stackedIface = stackedLink.getInterfaceName();
                        if (stackedIface != null) {
                            connIdents.add(Pair.create(stackedIface, ident));
                        }
                    }
                }
            }
            this.mNetworkRules.clear();
            ArrayList<String> ifaceList = Lists.newArrayList();
            for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
                policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
                ifaceList.clear();
                for (int j = connIdents.size() - 1; j >= 0; j--) {
                    Pair<String, NetworkIdentity> ident2 = (Pair) connIdents.get(j);
                    if (policy.template.matches((NetworkIdentity) ident2.second)) {
                        Slog.d(TAG, "add iface for policy:" + policy);
                        ifaceList.add((String) ident2.first);
                    }
                }
                if (ifaceList.size() > 0) {
                    this.mNetworkRules.put(policy, (String[]) ifaceList.toArray(new String[ifaceList.size()]));
                }
            }
            long lowestRule = JobStatus.NO_LATEST_RUNTIME;
            ArraySet<String> arraySet = new ArraySet(states.length);
            long currentTime = currentTimeMillis();
            for (i = this.mNetworkRules.size() - 1; i >= 0; i--) {
                long start;
                long totalBytes;
                policy = (NetworkPolicy) this.mNetworkRules.keyAt(i);
                String[] ifaces = (String[]) this.mNetworkRules.valueAt(i);
                if (policy.hasCycle()) {
                    start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                    totalBytes = getTotalBytes(policy.template, start, currentTime);
                } else {
                    start = JobStatus.NO_LATEST_RUNTIME;
                    totalBytes = 0;
                }
                if (LOGD) {
                    Slog.d(TAG, "applying policy " + policy + " to ifaces " + Arrays.toString(ifaces));
                }
                boolean hasWarning = policy.warningBytes != -1;
                boolean hasLimit = policy.limitBytes != -1;
                if (hasLimit || policy.metered) {
                    long quotaBytes;
                    if (!hasLimit) {
                        quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                    } else if (policy.lastLimitSnooze >= start) {
                        quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                    } else {
                        quotaBytes = Math.max(1, policy.limitBytes - totalBytes);
                    }
                    if (ifaces.length > 1) {
                        Slog.w(TAG, "shared quota unsupported; generating rule for each iface");
                    }
                    for (String iface2 : ifaces) {
                        if (!(iface2 == null || iface2.length() == 0)) {
                            if (iface2.equals(mmsifacename)) {
                                Slog.d(TAG, "mmsifacename set quota mms ifacename=" + mmsifacename);
                                quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                            }
                            this.mHandler.obtainMessage(10, (int) (quotaBytes >> 32), (int) (-1 & quotaBytes), iface2).sendToTarget();
                            arraySet.add(iface2);
                        }
                    }
                }
                if (hasWarning && policy.warningBytes < lowestRule) {
                    lowestRule = policy.warningBytes;
                }
                if (hasLimit && policy.limitBytes < lowestRule) {
                    lowestRule = policy.limitBytes;
                }
            }
            for (i = connIfaces.size() - 1; i >= 0; i--) {
                iface2 = (String) connIfaces.valueAt(i);
                this.mHandler.obtainMessage(10, Integer.MAX_VALUE, -1, iface2).sendToTarget();
                arraySet.add(iface2);
            }
            this.mHandler.obtainMessage(7, Long.valueOf(lowestRule)).sendToTarget();
            for (i = this.mMeteredIfaces.size() - 1; i >= 0; i--) {
                iface2 = (String) this.mMeteredIfaces.valueAt(i);
                if (!arraySet.contains(iface2)) {
                    this.mHandler.obtainMessage(11, iface2).sendToTarget();
                }
            }
            this.mMeteredIfaces = arraySet;
            this.mHandler.obtainMessage(2, (String[]) this.mMeteredIfaces.toArray(new String[this.mMeteredIfaces.size()])).sendToTarget();
        } catch (RemoteException e) {
        }
    }

    private void ensureActiveMobilePolicyNL() {
        if (LOGV) {
            Slog.v(TAG, "ensureActiveMobilePolicyNL()");
        }
        if (!this.mSuppressDefaultPolicy) {
            TelephonyManager tele = TelephonyManager.from(this.mContext);
            for (int subId : SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList()) {
                int slotId = SubscriptionManager.getSlotId(subId);
                String subscriberId = INVALID_SUBSCRIBER_ID;
                if (SubscriptionManager.isValidSubscriptionId(subId) && "READY".equals(tele.getOemSimState(slotId))) {
                    ensureActiveMobilePolicyNL(tele.getSubscriberId(subId));
                }
            }
        }
    }

    private void ensureActiveMobilePolicyNL(String subscriberId) {
        if (ENG_DBG) {
            Slog.v(TAG, "ensureActiveMobilePolicyLocked subscriberId(" + subscriberId + ")");
        }
        if (subscriberId == null) {
            Slog.v(TAG, "skip ensureActiveMobilePolicyNL due to subscriberId = null");
            return;
        }
        long warningBytes;
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, subscriberId, null, false, true);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkTemplate template = (NetworkTemplate) this.mNetworkPolicy.keyAt(i);
            if (template.matches(probeIdent)) {
                if (LOGD) {
                    Slog.d(TAG, "Found template " + template + " which matches subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId));
                }
                return;
            }
        }
        Slog.i(TAG, "No policy for subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId) + "; generating default policy");
        int dataWarningConfig = this.mContext.getResources().getInteger(17694853);
        if (((long) dataWarningConfig) == -1) {
            warningBytes = -1;
        } else {
            warningBytes = ((long) dataWarningConfig) * 1048576;
        }
        Time time = new Time();
        time.setToNow();
        addNetworkPolicyNL(new NetworkPolicy(NetworkTemplate.buildTemplateMobileAll(subscriberId), time.monthDay, time.timezone, warningBytes, -1, -1, -1, true, true));
        sendPolicyCreatedBroadcast();
    }

    private void readPolicyAL() {
        if (LOGV) {
            Slog.v(TAG, "readPolicyAL()");
        }
        this.mNetworkPolicy.clear();
        this.mUidPolicy.clear();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mPolicyFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            int version = 1;
            boolean insideWhitelist = false;
            while (true) {
                int type = in.next();
                if (type != 1) {
                    String tag = in.getName();
                    if (type == 2) {
                        int uid;
                        int policy;
                        if (TAG_POLICY_LIST.equals(tag)) {
                            boolean oldValue = this.mRestrictBackground;
                            version = XmlUtils.readIntAttribute(in, "version");
                            if (version >= 3) {
                                this.mRestrictBackground = XmlUtils.readBooleanAttribute(in, ATTR_RESTRICT_BACKGROUND);
                            } else {
                                this.mRestrictBackground = false;
                            }
                            if (this.mRestrictBackground != oldValue) {
                                this.mHandler.obtainMessage(6, this.mRestrictBackground ? 1 : 0, 0).sendToTarget();
                            }
                        } else if (TAG_NETWORK_POLICY.equals(tag)) {
                            String networkId;
                            String cycleTimezone;
                            long lastLimitSnooze;
                            boolean metered;
                            long lastWarningSnooze;
                            boolean inferred;
                            int networkTemplate = XmlUtils.readIntAttribute(in, ATTR_NETWORK_TEMPLATE);
                            String subscriberId = in.getAttributeValue(null, ATTR_SUBSCRIBER_ID);
                            if (version >= 9) {
                                networkId = in.getAttributeValue(null, ATTR_NETWORK_ID);
                            } else {
                                networkId = null;
                            }
                            int cycleDay = XmlUtils.readIntAttribute(in, ATTR_CYCLE_DAY);
                            if (version >= 6) {
                                cycleTimezone = in.getAttributeValue(null, ATTR_CYCLE_TIMEZONE);
                            } else {
                                cycleTimezone = "UTC";
                            }
                            long warningBytes = XmlUtils.readLongAttribute(in, ATTR_WARNING_BYTES);
                            long limitBytes = XmlUtils.readLongAttribute(in, ATTR_LIMIT_BYTES);
                            if (version >= 5) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_LIMIT_SNOOZE);
                            } else if (version >= 2) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_SNOOZE);
                            } else {
                                lastLimitSnooze = -1;
                            }
                            if (version < 4) {
                                switch (networkTemplate) {
                                    case 1:
                                    case 2:
                                    case 3:
                                        metered = true;
                                        break;
                                    default:
                                        metered = false;
                                        break;
                                }
                            }
                            metered = XmlUtils.readBooleanAttribute(in, ATTR_METERED);
                            if (version >= 5) {
                                lastWarningSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_WARNING_SNOOZE);
                            } else {
                                lastWarningSnooze = -1;
                            }
                            if (version >= 7) {
                                inferred = XmlUtils.readBooleanAttribute(in, ATTR_INFERRED);
                            } else {
                                inferred = false;
                            }
                            NetworkTemplate template = new NetworkTemplate(networkTemplate, subscriberId, networkId);
                            if (template.isPersistable()) {
                                this.mNetworkPolicy.put(template, new NetworkPolicy(template, cycleDay, cycleTimezone, warningBytes, limitBytes, lastWarningSnooze, lastLimitSnooze, metered, inferred));
                            }
                        } else if (TAG_UID_POLICY.equals(tag)) {
                            uid = XmlUtils.readIntAttribute(in, "uid");
                            policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                            if (UserHandle.isApp(uid)) {
                                setUidPolicyUncheckedUL(uid, policy, false);
                            } else {
                                Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                            }
                        } else if (TAG_APP_POLICY.equals(tag)) {
                            int appId = XmlUtils.readIntAttribute(in, ATTR_APP_ID);
                            policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                            uid = UserHandle.getUid(0, appId);
                            if (UserHandle.isApp(uid)) {
                                setUidPolicyUncheckedUL(uid, policy, false);
                            } else {
                                Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                            }
                        } else if (TAG_WHITELIST.equals(tag)) {
                            insideWhitelist = true;
                        } else if (TAG_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                            this.mRestrictBackgroundWhitelistUids.put(XmlUtils.readIntAttribute(in, "uid"), true);
                        } else if (TAG_REVOKED_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                            this.mRestrictBackgroundWhitelistRevokedUids.put(XmlUtils.readIntAttribute(in, "uid"), true);
                        }
                    } else if (type == 3 && TAG_WHITELIST.equals(tag)) {
                        insideWhitelist = false;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            upgradeLegacyBackgroundDataUL();
        } catch (Throwable e2) {
            Log.wtf(TAG, "problem reading network policy", e2);
        } catch (Throwable e3) {
            Log.wtf(TAG, "problem reading network policy", e3);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void upgradeLegacyBackgroundDataUL() {
        boolean z = true;
        if (Secure.getInt(this.mContext.getContentResolver(), "background_data", 1) == 1) {
            z = false;
        }
        this.mRestrictBackground = z;
        if (this.mRestrictBackground) {
            this.mContext.sendBroadcastAsUser(new Intent("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED"), UserHandle.ALL);
        }
    }

    void writePolicyAL() {
        if (LOGV) {
            Slog.v(TAG, "writePolicyAL()");
        }
        FileOutputStream fos = null;
        try {
            int i;
            int uid;
            fos = this.mPolicyFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_POLICY_LIST);
            XmlUtils.writeIntAttribute(out, "version", 10);
            XmlUtils.writeBooleanAttribute(out, ATTR_RESTRICT_BACKGROUND, this.mRestrictBackground);
            for (i = 0; i < this.mNetworkPolicy.size(); i++) {
                NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
                NetworkTemplate template = policy.template;
                if (template.isPersistable()) {
                    out.startTag(null, TAG_NETWORK_POLICY);
                    XmlUtils.writeIntAttribute(out, ATTR_NETWORK_TEMPLATE, template.getMatchRule());
                    String subscriberId = template.getSubscriberId();
                    if (subscriberId != null) {
                        out.attribute(null, ATTR_SUBSCRIBER_ID, subscriberId);
                    }
                    String networkId = template.getNetworkId();
                    if (networkId != null) {
                        out.attribute(null, ATTR_NETWORK_ID, networkId);
                    }
                    XmlUtils.writeIntAttribute(out, ATTR_CYCLE_DAY, policy.cycleDay);
                    out.attribute(null, ATTR_CYCLE_TIMEZONE, policy.cycleTimezone);
                    XmlUtils.writeLongAttribute(out, ATTR_WARNING_BYTES, policy.warningBytes);
                    XmlUtils.writeLongAttribute(out, ATTR_LIMIT_BYTES, policy.limitBytes);
                    XmlUtils.writeLongAttribute(out, ATTR_LAST_WARNING_SNOOZE, policy.lastWarningSnooze);
                    XmlUtils.writeLongAttribute(out, ATTR_LAST_LIMIT_SNOOZE, policy.lastLimitSnooze);
                    XmlUtils.writeBooleanAttribute(out, ATTR_METERED, policy.metered);
                    XmlUtils.writeBooleanAttribute(out, ATTR_INFERRED, policy.inferred);
                    out.endTag(null, TAG_NETWORK_POLICY);
                }
            }
            for (i = 0; i < this.mUidPolicy.size(); i++) {
                uid = this.mUidPolicy.keyAt(i);
                int policy2 = this.mUidPolicy.valueAt(i);
                if (policy2 != 0) {
                    out.startTag(null, TAG_UID_POLICY);
                    XmlUtils.writeIntAttribute(out, "uid", uid);
                    XmlUtils.writeIntAttribute(out, ATTR_POLICY, policy2);
                    out.endTag(null, TAG_UID_POLICY);
                }
            }
            out.endTag(null, TAG_POLICY_LIST);
            out.startTag(null, TAG_WHITELIST);
            int size = this.mRestrictBackgroundWhitelistUids.size();
            for (i = 0; i < size; i++) {
                uid = this.mRestrictBackgroundWhitelistUids.keyAt(i);
                out.startTag(null, TAG_RESTRICT_BACKGROUND);
                XmlUtils.writeIntAttribute(out, "uid", uid);
                out.endTag(null, TAG_RESTRICT_BACKGROUND);
            }
            size = this.mRestrictBackgroundWhitelistRevokedUids.size();
            for (i = 0; i < size; i++) {
                uid = this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i);
                out.startTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
                XmlUtils.writeIntAttribute(out, "uid", uid);
                out.endTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
            }
            out.endTag(null, TAG_WHITELIST);
            out.endDocument();
            this.mPolicyFile.finishWrite(fos);
        } catch (IOException e) {
            if (fos != null) {
                this.mPolicyFile.failWrite(fos);
            }
        }
    }

    public void setUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mUidRulesFirstLock) {
                long token = Binder.clearCallingIdentity();
                try {
                    int oldPolicy = this.mUidPolicy.get(uid, 0);
                    if (oldPolicy != policy) {
                        setUidPolicyUncheckedUL(uid, oldPolicy, policy, true);
                    }
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    public void addUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mUidRulesFirstLock) {
                int oldPolicy = this.mUidPolicy.get(uid, 0);
                policy |= oldPolicy;
                if (oldPolicy != policy) {
                    setUidPolicyUncheckedUL(uid, oldPolicy, policy, true);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    public void removeUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mUidRulesFirstLock) {
                int oldPolicy = this.mUidPolicy.get(uid, 0);
                policy = oldPolicy & (~policy);
                if (oldPolicy != policy) {
                    setUidPolicyUncheckedUL(uid, oldPolicy, policy, true);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    private void setUidPolicyUncheckedUL(int uid, int oldPolicy, int policy, boolean persist) {
        int i = 0;
        setUidPolicyUncheckedUL(uid, policy, persist);
        boolean isBlacklisted = policy == 1;
        Handler handler = this.mHandler;
        if (isBlacklisted) {
            i = 1;
        }
        handler.obtainMessage(12, uid, i).sendToTarget();
        boolean wasBlacklisted = oldPolicy == 1;
        if ((oldPolicy == 0 && isBlacklisted) || (wasBlacklisted && policy == 0)) {
            this.mHandler.obtainMessage(9, uid, 1, null).sendToTarget();
        }
    }

    private void setUidPolicyUncheckedUL(int uid, int policy, boolean persist) {
        this.mUidPolicy.put(uid, policy);
        updateRulesForDataUsageRestrictionsUL(uid);
        if (persist) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                writePolicyAL();
            }
        }
    }

    public int getUidPolicy(int uid) {
        int i;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            i = this.mUidPolicy.get(uid, 0);
        }
        return i;
    }

    public int[] getUidsWithPolicy(int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        int[] uids = new int[0];
        synchronized (this.mUidRulesFirstLock) {
            for (int i = 0; i < this.mUidPolicy.size(); i++) {
                int uid = this.mUidPolicy.keyAt(i);
                if (this.mUidPolicy.valueAt(i) == policy) {
                    uids = ArrayUtils.appendInt(uids, uid);
                }
            }
        }
        return uids;
    }

    boolean removeUserStateUL(int userId, boolean writePolicy) {
        int i;
        int uid;
        int length;
        int i2 = 0;
        if (LOGV) {
            Slog.v(TAG, "removeUserStateUL()");
        }
        boolean changed = false;
        int[] wlUids = new int[0];
        for (i = 0; i < this.mRestrictBackgroundWhitelistUids.size(); i++) {
            uid = this.mRestrictBackgroundWhitelistUids.keyAt(i);
            if (UserHandle.getUserId(uid) == userId) {
                wlUids = ArrayUtils.appendInt(wlUids, uid);
            }
        }
        if (wlUids.length > 0) {
            for (int uid2 : wlUids) {
                removeRestrictBackgroundWhitelistedUidUL(uid2, false, false);
            }
            changed = true;
        }
        for (i = this.mRestrictBackgroundWhitelistRevokedUids.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i)) == userId) {
                this.mRestrictBackgroundWhitelistRevokedUids.removeAt(i);
                changed = true;
            }
        }
        int[] uids = new int[0];
        for (i = 0; i < this.mUidPolicy.size(); i++) {
            uid2 = this.mUidPolicy.keyAt(i);
            if (UserHandle.getUserId(uid2) == userId) {
                uids = ArrayUtils.appendInt(uids, uid2);
            }
        }
        if (uids.length > 0) {
            length = uids.length;
            while (i2 < length) {
                this.mUidPolicy.delete(uids[i2]);
                i2++;
            }
            changed = true;
        }
        synchronized (this.mNetworkPoliciesSecondLock) {
            updateRulesForGlobalChangeAL(true);
            if (writePolicy && changed) {
                writePolicyAL();
            }
        }
        return changed;
    }

    public void setConnectivityListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mConnectivityListener != null) {
            throw new IllegalStateException("Connectivity listener already registered");
        }
        this.mConnectivityListener = listener;
    }

    public void registerListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mListeners.register(listener);
    }

    public void unregisterListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mListeners.unregister(listener);
    }

    public void setNetworkPolicies(NetworkPolicy[] policies) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            maybeRefreshTrustedTime();
            synchronized (this.mUidRulesFirstLock) {
                for (NetworkPolicy policy : policies) {
                    if (7 == policy.template.getMatchRule()) {
                        throw new IllegalArgumentException("unexpected template in setNetworkPolicies");
                    }
                }
                synchronized (this.mNetworkPoliciesSecondLock) {
                    normalizePoliciesNL(policies);
                    updateNetworkEnabledNL();
                    updateNetworkRulesNL();
                    updateNotificationsNL();
                    writePolicyAL();
                }
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    void addNetworkPolicyNL(NetworkPolicy policy) {
        Slog.v(TAG, "addNetworkPolicyLocked(" + policy + ")");
        NetworkPolicy[] policies = getNetworkPolicies(this.mContext.getOpPackageName());
        if (7 == policy.template.getMatchRule()) {
            Slog.e(TAG, " Error!! addNetworkPolicyLocked( MATCH_WIFI_WILDCARD )");
        }
        setNetworkPolicies((NetworkPolicy[]) ArrayUtils.appendElement(NetworkPolicy.class, policies, policy));
    }

    public NetworkPolicy[] getNetworkPolicies(String callingPackage) {
        NetworkPolicy[] policies;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", TAG);
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE, TAG);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                return new NetworkPolicy[0];
            }
        }
        synchronized (this.mNetworkPoliciesSecondLock) {
            int size = this.mNetworkPolicy.size();
            policies = new NetworkPolicy[size];
            for (int i = 0; i < size; i++) {
                policies[i] = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            }
        }
        return policies;
    }

    private void normalizePoliciesNL() {
        normalizePoliciesNL(getNetworkPolicies(this.mContext.getOpPackageName()));
    }

    private void normalizePoliciesNL(NetworkPolicy[] policies) {
        String[] merged = TelephonyManager.from(this.mContext).getMergedSubscriberIds();
        this.mNetworkPolicy.clear();
        for (NetworkPolicy policy : policies) {
            policy.template = NetworkTemplate.normalize(policy.template, merged);
            NetworkPolicy existing = (NetworkPolicy) this.mNetworkPolicy.get(policy.template);
            if (existing == null || existing.compareTo(policy) > 0) {
                if (existing != null) {
                    Slog.d(TAG, "Normalization replaced " + existing + " with " + policy);
                }
                this.mNetworkPolicy.put(policy.template, policy);
            }
        }
    }

    public void snoozeLimit(NetworkTemplate template) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            performSnooze(template, 2);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void performSnooze(NetworkTemplate template, int type) {
        if (LOGD) {
            Log.d(TAG, "performSnooze on:" + template);
        }
        maybeRefreshTrustedTime();
        long currentTime = currentTimeMillis();
        synchronized (this.mUidRulesFirstLock) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.get(template);
                if (policy == null) {
                    throw new IllegalArgumentException("unable to find policy for " + template);
                }
                switch (type) {
                    case 1:
                        policy.lastWarningSnooze = currentTime;
                        break;
                    case 2:
                        policy.lastLimitSnooze = currentTime;
                        break;
                    default:
                        throw new IllegalArgumentException("unexpected type");
                }
                normalizePoliciesNL();
                updateNetworkEnabledNL();
                updateNetworkRulesNL();
                updateNotificationsNL();
                writePolicyAL();
            }
        }
    }

    public void onTetheringChanged(String iface, boolean tethering) {
        if (LOGD) {
            Log.d(TAG, "onTetherStateChanged(" + iface + ", " + tethering + ")");
        }
        synchronized (this.mUidRulesFirstLock) {
            if (this.mRestrictBackground && tethering) {
                Log.d(TAG, "Tethering on (" + iface + "); disable Data Saver");
                setRestrictBackground(false);
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x005e, code:
            android.os.Binder.restoreCallingIdentity(r0);
            r4 = r6.mHandler;
     */
    /* JADX WARNING: Missing block: B:16:0x0063, code:
            if (r7 == false) goto L_0x0077;
     */
    /* JADX WARNING: Missing block: B:17:0x0065, code:
            r2 = 1;
     */
    /* JADX WARNING: Missing block: B:18:0x0066, code:
            r4.obtainMessage(6, r2, 0).sendToTarget();
     */
    /* JADX WARNING: Missing block: B:19:0x006e, code:
            return;
     */
    /* JADX WARNING: Missing block: B:26:0x0077, code:
            r2 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRestrictBackground(boolean restrictBackground) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        Slog.d(TAG, "setRestrictBackground(" + restrictBackground + ")");
        try {
            maybeRefreshTrustedTime();
            synchronized (this.mUidRulesFirstLock) {
                if (restrictBackground == this.mRestrictBackground) {
                    Slog.w(TAG, "setRestrictBackground: already " + restrictBackground);
                } else {
                    setRestrictBackgroundUL(restrictBackground);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void setRestrictBackgroundUL(boolean restrictBackground) {
        Slog.d(TAG, "setRestrictBackgroundUL(): " + restrictBackground);
        boolean oldRestrictBackground = this.mRestrictBackground;
        this.mRestrictBackground = restrictBackground;
        updateRulesForRestrictBackgroundUL();
        try {
            if (!this.mNetworkManager.setDataSaverModeEnabled(this.mRestrictBackground)) {
                Slog.e(TAG, "Could not change Data Saver Mode on NMS to " + this.mRestrictBackground);
                this.mRestrictBackground = oldRestrictBackground;
                return;
            }
        } catch (RemoteException e) {
        }
        synchronized (this.mNetworkPoliciesSecondLock) {
            updateNotificationsNL();
            writePolicyAL();
        }
    }

    /* JADX WARNING: Missing block: B:9:0x003c, code:
            return;
     */
    /* JADX WARNING: Missing block: B:32:0x00b6, code:
            r7.mHandler.obtainMessage(9, r8, r0, java.lang.Boolean.TRUE).sendToTarget();
     */
    /* JADX WARNING: Missing block: B:33:0x00c3, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addRestrictBackgroundWhitelistedUid(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            boolean oldStatus = this.mRestrictBackgroundWhitelistUids.get(uid);
            if (!oldStatus) {
                boolean needFirewallRules = isUidValidForWhitelistRules(uid);
                Slog.i(TAG, "adding uid " + uid + " to restrict background whitelist");
                this.mRestrictBackgroundWhitelistUids.append(uid, true);
                if (this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                    if (LOGD) {
                        Slog.d(TAG, "Removing uid " + uid + " from revoked restrict background whitelist");
                    }
                    this.mRestrictBackgroundWhitelistRevokedUids.delete(uid);
                }
                if (needFirewallRules) {
                    updateRulesForDataUsageRestrictionsUL(uid);
                }
                synchronized (this.mNetworkPoliciesSecondLock) {
                    writePolicyAL();
                }
                int changed = (this.mRestrictBackground && !oldStatus && needFirewallRules) ? 1 : 0;
            } else if (LOGD) {
                Slog.d(TAG, "uid " + uid + " is already whitelisted");
            }
        }
    }

    public void removeRestrictBackgroundWhitelistedUid(int uid) {
        boolean changed;
        int i = 1;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            changed = removeRestrictBackgroundWhitelistedUidUL(uid, false, true);
        }
        Handler handler = this.mHandler;
        if (!changed) {
            i = 0;
        }
        handler.obtainMessage(9, uid, i, Boolean.FALSE).sendToTarget();
    }

    private boolean removeRestrictBackgroundWhitelistedUidUL(int uid, boolean uidDeleted, boolean updateNow) {
        boolean oldStatus = this.mRestrictBackgroundWhitelistUids.get(uid);
        if (oldStatus || uidDeleted) {
            boolean needFirewallRules = !uidDeleted ? isUidValidForWhitelistRules(uid) : true;
            if (oldStatus) {
                Slog.i(TAG, "removing uid " + uid + " from restrict background whitelist");
                this.mRestrictBackgroundWhitelistUids.delete(uid);
            }
            if (!uidDeleted && this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && !this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                if (LOGD) {
                    Slog.d(TAG, "Adding uid " + uid + " to revoked restrict background whitelist");
                }
                this.mRestrictBackgroundWhitelistRevokedUids.append(uid, true);
            } else if (LOGD) {
                Slog.d(TAG, "Skip revoking " + uid + " from restrict background whitelist");
            }
            if (needFirewallRules) {
                updateRulesForDataUsageRestrictionsUL(uid, uidDeleted);
            }
            if (updateNow) {
                synchronized (this.mNetworkPoliciesSecondLock) {
                    writePolicyAL();
                }
            }
            if (!this.mRestrictBackground) {
                needFirewallRules = false;
            }
            return needFirewallRules;
        }
        if (LOGD) {
            Slog.d(TAG, "uid " + uid + " was not whitelisted before");
        }
        return false;
    }

    public int[] getRestrictBackgroundWhitelistedUids() {
        int[] whitelist;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            int size = this.mRestrictBackgroundWhitelistUids.size();
            whitelist = new int[size];
            for (int i = 0; i < size; i++) {
                whitelist[i] = this.mRestrictBackgroundWhitelistUids.keyAt(i);
            }
            if (LOGV) {
                Slog.v(TAG, "getRestrictBackgroundWhitelistedUids(): " + this.mRestrictBackgroundWhitelistUids);
            }
        }
        return whitelist;
    }

    /* JADX WARNING: Missing block: B:28:0x003b, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getRestrictBackgroundByCaller() {
        int i = 3;
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        int uid = Binder.getCallingUid();
        synchronized (this.mUidRulesFirstLock) {
            long token = Binder.clearCallingIdentity();
            try {
                int policy = getUidPolicy(uid);
                Binder.restoreCallingIdentity(token);
                if (policy == 1) {
                    return 3;
                } else if (!this.mRestrictBackground) {
                    return 1;
                } else if (this.mRestrictBackgroundWhitelistUids.get(uid)) {
                    i = 2;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public boolean getRestrictBackground() {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            z = this.mRestrictBackground;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:16:0x002a, code:
            if (r7 == false) goto L_0x003e;
     */
    /* JADX WARNING: Missing block: B:17:0x002c, code:
            com.android.server.EventLogTags.writeDeviceIdleOnPhase("net");
     */
    /* JADX WARNING: Missing block: B:18:0x0032, code:
            android.os.Trace.traceEnd(8388608);
     */
    /* JADX WARNING: Missing block: B:19:0x0035, code:
            return;
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            com.android.server.EventLogTags.writeDeviceIdleOffPhase("net");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDeviceIdleMode(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        Trace.traceBegin(8388608, "setDeviceIdleMode");
        try {
            synchronized (this.mUidRulesFirstLock) {
                if (this.mDeviceIdleMode != enabled) {
                    this.mDeviceIdleMode = enabled;
                    if (this.mSystemReady) {
                        updateRulesForRestrictPowerUL();
                    }
                }
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private NetworkPolicy findPolicyForNetworkNL(NetworkIdentity ident) {
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (policy.template.matches(ident)) {
                return policy;
            }
        }
        return null;
    }

    public NetworkQuotaInfo getNetworkQuotaInfo(NetworkState state) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            NetworkQuotaInfo networkQuotaInfoUnchecked = getNetworkQuotaInfoUnchecked(state);
            return networkQuotaInfoUnchecked;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private NetworkQuotaInfo getNetworkQuotaInfoUnchecked(NetworkState state) {
        NetworkPolicy policy;
        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
        synchronized (this.mNetworkPoliciesSecondLock) {
            policy = findPolicyForNetworkNL(ident);
        }
        if (policy == null || !policy.hasCycle()) {
            return null;
        }
        long softLimitBytes;
        long hardLimitBytes;
        long currentTime = currentTimeMillis();
        long end = currentTime;
        long totalBytes = getTotalBytes(policy.template, NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy), currentTime);
        if (policy.warningBytes != -1) {
            softLimitBytes = policy.warningBytes;
        } else {
            softLimitBytes = -1;
        }
        if (policy.limitBytes != -1) {
            hardLimitBytes = policy.limitBytes;
        } else {
            hardLimitBytes = -1;
        }
        return new NetworkQuotaInfo(totalBytes, softLimitBytes, hardLimitBytes);
    }

    public boolean isNetworkMetered(NetworkState state) {
        if (state.networkInfo == null) {
            return false;
        }
        NetworkPolicy policy;
        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
        synchronized (this.mNetworkPoliciesSecondLock) {
            policy = findPolicyForNetworkNL(ident);
        }
        if (policy != null) {
            return policy.metered;
        }
        int type = state.networkInfo.getType();
        if ((ConnectivityManager.isNetworkTypeMobile(type) && ident.getMetered()) || type == 6) {
            return true;
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        IndentingPrintWriter fout = new IndentingPrintWriter(writer, "  ");
        ArraySet<String> argSet = new ArraySet(args.length);
        for (String arg : args) {
            argSet.add(arg);
        }
        synchronized (this.mUidRulesFirstLock) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                int i;
                if (argSet.contains("--unsnooze")) {
                    for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
                        ((NetworkPolicy) this.mNetworkPolicy.valueAt(i)).clearSnooze();
                    }
                    normalizePoliciesNL();
                    updateNetworkEnabledNL();
                    updateNetworkRulesNL();
                    updateNotificationsNL();
                    writePolicyAL();
                    fout.println("Cleared snooze timestamps");
                    return;
                }
                int uid;
                int uidRules;
                fout.print("System ready: ");
                fout.println(this.mSystemReady);
                fout.print("Restrict background: ");
                fout.println(this.mRestrictBackground);
                fout.print("Restrict power: ");
                fout.println(this.mRestrictPower);
                fout.print("Device idle: ");
                fout.println(this.mDeviceIdleMode);
                fout.println("Network policies:");
                fout.increaseIndent();
                for (i = 0; i < this.mNetworkPolicy.size(); i++) {
                    fout.println(((NetworkPolicy) this.mNetworkPolicy.valueAt(i)).toString());
                }
                fout.decreaseIndent();
                fout.print("Metered ifaces: ");
                fout.println(String.valueOf(this.mMeteredIfaces));
                fout.println("Policy for UIDs:");
                fout.increaseIndent();
                int size = this.mUidPolicy.size();
                for (i = 0; i < size; i++) {
                    uid = this.mUidPolicy.keyAt(i);
                    int policy = this.mUidPolicy.valueAt(i);
                    fout.print("UID=");
                    fout.print(uid);
                    fout.print(" policy=");
                    fout.print(DebugUtils.flagsToString(NetworkPolicyManager.class, "POLICY_", policy));
                    fout.println();
                }
                fout.decreaseIndent();
                size = this.mPowerSaveWhitelistExceptIdleAppIds.size();
                if (size > 0) {
                    fout.println("Power save whitelist (except idle) app ids:");
                    fout.increaseIndent();
                    for (i = 0; i < size; i++) {
                        fout.print("UID=");
                        fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i));
                        fout.print(": ");
                        fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.valueAt(i));
                        fout.println();
                    }
                    fout.decreaseIndent();
                }
                size = this.mPowerSaveWhitelistAppIds.size();
                if (size > 0) {
                    fout.println("Power save whitelist app ids:");
                    fout.increaseIndent();
                    for (i = 0; i < size; i++) {
                        fout.print("UID=");
                        fout.print(this.mPowerSaveWhitelistAppIds.keyAt(i));
                        fout.print(": ");
                        fout.print(this.mPowerSaveWhitelistAppIds.valueAt(i));
                        fout.println();
                    }
                    fout.decreaseIndent();
                }
                size = this.mRestrictBackgroundWhitelistUids.size();
                if (size > 0) {
                    fout.println("Restrict background whitelist uids:");
                    fout.increaseIndent();
                    for (i = 0; i < size; i++) {
                        fout.print("UID=");
                        fout.print(this.mRestrictBackgroundWhitelistUids.keyAt(i));
                        fout.println();
                    }
                    fout.decreaseIndent();
                }
                size = this.mDefaultRestrictBackgroundWhitelistUids.size();
                if (size > 0) {
                    fout.println("Default restrict background whitelist uids:");
                    fout.increaseIndent();
                    for (i = 0; i < size; i++) {
                        fout.print("UID=");
                        fout.print(this.mDefaultRestrictBackgroundWhitelistUids.keyAt(i));
                        fout.println();
                    }
                    fout.decreaseIndent();
                }
                size = this.mRestrictBackgroundWhitelistRevokedUids.size();
                if (size > 0) {
                    fout.println("Default restrict background whitelist uids revoked by users:");
                    fout.increaseIndent();
                    for (i = 0; i < size; i++) {
                        fout.print("UID=");
                        fout.print(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i));
                        fout.println();
                    }
                    fout.decreaseIndent();
                }
                SparseBooleanArray knownUids = new SparseBooleanArray();
                collectKeys(this.mUidState, knownUids);
                collectKeys(this.mUidRules, knownUids);
                fout.println("Status for all known UIDs:");
                fout.increaseIndent();
                size = knownUids.size();
                for (i = 0; i < size; i++) {
                    uid = knownUids.keyAt(i);
                    fout.print("UID=");
                    fout.print(uid);
                    int state = this.mUidState.get(uid, 16);
                    fout.print(" state=");
                    fout.print(state);
                    if (state <= 2) {
                        fout.print(" (fg)");
                    } else {
                        fout.print(state <= 4 ? " (fg svc)" : " (bg)");
                    }
                    uidRules = this.mUidRules.get(uid, 0);
                    fout.print(" rules=");
                    fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                    fout.println();
                }
                fout.decreaseIndent();
                fout.println("Status for just UIDs with rules:");
                fout.increaseIndent();
                size = this.mUidRules.size();
                for (i = 0; i < size; i++) {
                    uid = this.mUidRules.keyAt(i);
                    fout.print("UID=");
                    fout.print(uid);
                    uidRules = this.mUidRules.get(uid, 0);
                    fout.print(" rules=");
                    fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                    fout.println();
                }
                fout.decreaseIndent();
                fout.println("Status for appID with rules: ");
                fout.increaseIndent();
                boolean gameSpace = OppoGameSpaceManager.getInstance().isGameSpaceMode();
                fout.print("Status for gs mode: ");
                fout.print(gameSpace);
                fout.println();
                List<Integer> appIdWhiteList = OppoGameSpaceManager.getInstance().getNetWhiteAppIdlist();
                size = appIdWhiteList.size();
                for (i = 0; i < size; i++) {
                    int appid = ((Integer) appIdWhiteList.get(i)).intValue();
                    fout.print("appID=");
                    fout.print(appid);
                    fout.println();
                }
                fout.print("appID=");
                fout.print(OppoGameSpaceManager.getInstance().getDefaultInputMethodAppId());
                fout.println();
                fout.decreaseIndent();
            }
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        new NetworkPolicyManagerShellCommand(this.mContext, this).exec(this, in, out, err, args, resultReceiver);
    }

    public boolean isUidForeground(int uid) {
        boolean isUidForegroundUL;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            isUidForegroundUL = isUidForegroundUL(uid);
        }
        return isUidForegroundUL;
    }

    private boolean isUidForegroundUL(int uid) {
        return isUidStateForegroundUL(this.mUidState.get(uid, 16));
    }

    private boolean isUidForegroundOnRestrictBackgroundUL(int uid) {
        return isProcStateAllowedWhileOnRestrictBackground(this.mUidState.get(uid, 16));
    }

    private boolean isUidForegroundOnRestrictPowerUL(int uid) {
        return isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid, 16));
    }

    private boolean isUidStateForegroundUL(int state) {
        return state <= 2;
    }

    private void updateUidStateUL(int uid, int uidState) {
        Trace.traceBegin(8388608, "updateUidStateUL");
        try {
            int oldUidState = this.mUidState.get(uid, 16);
            if (oldUidState != uidState) {
                this.mUidState.put(uid, uidState);
                updateRestrictBackgroundRulesOnUidStatusChangedUL(uid, oldUidState, uidState);
                if (isProcStateAllowedWhileIdleOrPowerSaveMode(oldUidState) != isProcStateAllowedWhileIdleOrPowerSaveMode(uidState)) {
                    if (isUidIdle(uid)) {
                        updateRuleForAppIdleUL(uid);
                    }
                    if (this.mDeviceIdleMode) {
                        updateRuleForDeviceIdleUL(uid);
                    }
                    if (this.mRestrictPower) {
                        updateRuleForRestrictPowerUL(uid);
                    }
                    updateRulesForPowerRestrictionsUL(uid);
                }
                updateNetworkStats(uid, isUidStateForegroundUL(uidState));
            }
            Trace.traceEnd(8388608);
        } catch (Throwable th) {
            Trace.traceEnd(8388608);
        }
    }

    private void removeUidStateUL(int uid) {
        int index = this.mUidState.indexOfKey(uid);
        if (index >= 0) {
            int oldUidState = this.mUidState.valueAt(index);
            this.mUidState.removeAt(index);
            if (oldUidState != 16) {
                updateRestrictBackgroundRulesOnUidStatusChangedUL(uid, oldUidState, 16);
                if (this.mDeviceIdleMode) {
                    updateRuleForDeviceIdleUL(uid);
                }
                if (this.mRestrictPower) {
                    updateRuleForRestrictPowerUL(uid);
                }
                updateRulesForPowerRestrictionsUL(uid);
                updateNetworkStats(uid, false);
            }
        }
    }

    private void updateNetworkStats(int uid, boolean uidForeground) {
        try {
            this.mNetworkStats.setUidForeground(uid, uidForeground);
        } catch (RemoteException e) {
        }
    }

    private void updateRestrictBackgroundRulesOnUidStatusChangedUL(int uid, int oldUidState, int newUidState) {
        if (isProcStateAllowedWhileOnRestrictBackground(oldUidState) != isProcStateAllowedWhileOnRestrictBackground(newUidState)) {
            updateRulesForDataUsageRestrictionsUL(uid);
        }
    }

    static boolean isProcStateAllowedWhileIdleOrPowerSaveMode(int procState) {
        return procState <= 4;
    }

    static boolean isProcStateAllowedWhileOnRestrictBackground(int procState) {
        return procState <= 4;
    }

    void updateRulesForPowerSaveUL() {
        Trace.traceBegin(8388608, "updateRulesForPowerSaveUL");
        try {
            updateRulesForWhitelistedPowerSaveUL(this.mRestrictPower, 3, this.mUidFirewallPowerSaveRules);
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    void updateRuleForRestrictPowerUL(int uid) {
        updateRulesForWhitelistedPowerSaveUL(uid, this.mRestrictPower, 3);
    }

    void updateRulesForDeviceIdleUL() {
        Trace.traceBegin(8388608, "updateRulesForDeviceIdleUL");
        try {
            updateRulesForWhitelistedPowerSaveUL(this.mDeviceIdleMode, 1, this.mUidFirewallDozableRules);
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    void updateRuleForDeviceIdleUL(int uid) {
        updateRulesForWhitelistedPowerSaveUL(uid, this.mDeviceIdleMode, 1);
    }

    private void updateRulesForWhitelistedPowerSaveUL(boolean enabled, int chain, SparseIntArray rules) {
        if (enabled) {
            int i;
            SparseIntArray uidRules = rules;
            rules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                UserInfo user = (UserInfo) users.get(ui);
                if (OppoGameSpaceManager.getInstance().isGameSpaceMode()) {
                    List<Integer> appIdWhiteList = OppoGameSpaceManager.getInstance().getNetWhiteAppIdlist();
                    if (!(appIdWhiteList == null && appIdWhiteList.isEmpty())) {
                        for (Integer intValue : appIdWhiteList) {
                            int uid = UserHandle.getUid(user.id, intValue.intValue());
                            if (LOGD) {
                                Log.v(TAG, "updateRulesForWhitelistedPowerSaveUL  add uid = " + uid);
                            }
                            rules.put(uid, 1);
                        }
                    }
                    rules.put(UserHandle.getUid(user.id, OppoGameSpaceManager.getInstance().getDefaultInputMethodAppId()), 1);
                    List<Integer> appIdDozeRuleWhiteList = OppoGameSpaceManager.getInstance().getDozeRuleWhiteAppIdlist();
                    if (!(appIdDozeRuleWhiteList == null || appIdDozeRuleWhiteList.isEmpty())) {
                        for (Integer intValue2 : appIdDozeRuleWhiteList) {
                            int appUid = UserHandle.getUid(user.id, intValue2.intValue());
                            if (LOGD) {
                                Log.v(TAG, "updateRulesForWhitelistedPowerSaveUL  add uid = " + appUid);
                            }
                            rules.put(appUid, 1);
                        }
                    }
                } else {
                    for (i = this.mPowerSaveTempWhitelistAppIds.size() - 1; i >= 0; i--) {
                        if (this.mPowerSaveTempWhitelistAppIds.valueAt(i)) {
                            rules.put(UserHandle.getUid(user.id, this.mPowerSaveTempWhitelistAppIds.keyAt(i)), 1);
                        }
                    }
                    for (i = this.mPowerSaveWhitelistAppIds.size() - 1; i >= 0; i--) {
                        rules.put(UserHandle.getUid(user.id, this.mPowerSaveWhitelistAppIds.keyAt(i)), 1);
                    }
                }
            }
            for (i = this.mUidState.size() - 1; i >= 0; i--) {
                if (isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.valueAt(i))) {
                    rules.put(this.mUidState.keyAt(i), 1);
                }
            }
            setUidFirewallRulesAsync(chain, rules, 1);
            return;
        }
        setUidFirewallRulesAsync(chain, null, 2);
    }

    private boolean isWhitelistedBatterySaverUL(int uid) {
        boolean z = true;
        int appId = UserHandle.getAppId(uid);
        if (OppoGameSpaceManager.getInstance().isGameSpaceMode()) {
            if (!(OppoGameSpaceManager.getInstance().inNetWhiteAppIdList(appId) || OppoGameSpaceManager.getInstance().isDefaultInputMethodAppId(appId))) {
                z = OppoGameSpaceManager.getInstance().inDozeRuleAppIdList(appId);
            }
            return z;
        }
        if (!this.mPowerSaveTempWhitelistAppIds.get(appId)) {
            z = this.mPowerSaveWhitelistAppIds.get(appId);
        }
        return z;
    }

    private void updateRulesForWhitelistedPowerSaveUL(int uid, boolean enabled, int chain) {
        if (!enabled) {
            return;
        }
        if (isWhitelistedBatterySaverUL(uid) || isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid, 16))) {
            setUidFirewallRule(chain, uid, 1);
        } else {
            setUidFirewallRule(chain, uid, 0);
        }
    }

    void updateRulesForAppIdleUL() {
        Trace.traceBegin(8388608, "updateRulesForAppIdleUL");
        try {
            SparseIntArray uidRules = this.mUidFirewallStandbyRules.clone();
            uidRules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                for (int uid : this.mUsageStats.getIdleUidsForUser(((UserInfo) users.get(ui)).id)) {
                    if (!this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid), false) && hasInternetPermissions(uid)) {
                        uidRules.put(uid, 2);
                    }
                }
            }
            setUidFirewallRulesAsync(2, uidRules, 0);
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    void updateRuleForAppIdleUL(int uid) {
        if (isUidValidForBlacklistRules(uid)) {
            if (this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid)) || !isUidIdle(uid) || isUidForegroundOnRestrictPowerUL(uid)) {
                setUidFirewallRule(2, uid, 0);
            } else {
                setUidFirewallRule(2, uid, 2);
            }
        }
    }

    void updateRulesForAppIdleParoleUL() {
        boolean enableChain = false;
        boolean paroled = this.mUsageStats.isAppIdleParoleOn();
        if (!paroled) {
            enableChain = true;
        }
        enableFirewallChainUL(2, enableChain);
        int ruleCount = this.mUidFirewallStandbyRules.size();
        for (int i = 0; i < ruleCount; i++) {
            int uid = this.mUidFirewallStandbyRules.keyAt(i);
            int oldRules = this.mUidRules.get(uid);
            if (enableChain) {
                oldRules &= 15;
            } else if ((oldRules & 240) == 0) {
            }
            updateRulesForPowerRestrictionsUL(uid, oldRules, paroled);
        }
    }

    private void updateRulesForGlobalChangeAL(boolean restrictedNetworksChanged) {
        Trace.traceBegin(8388608, "updateRulesForGlobalChangeAL");
        try {
            updateRulesForAppIdleUL();
            updateRulesForRestrictPowerUL();
            updateRulesForRestrictBackgroundUL();
            if (restrictedNetworksChanged) {
                normalizePoliciesNL();
                updateNetworkRulesNL();
            }
            Trace.traceEnd(8388608);
        } catch (Throwable th) {
            Trace.traceEnd(8388608);
        }
    }

    private void updateRulesForRestrictPowerUL() {
        Trace.traceBegin(8388608, "updateRulesForRestrictPowerUL");
        try {
            updateRulesForDeviceIdleUL();
            updateRulesForPowerSaveUL();
            updateRulesForAllAppsUL(2);
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private void updateRulesForRestrictBackgroundUL() {
        Trace.traceBegin(8388608, "updateRulesForRestrictBackgroundUL");
        try {
            updateRulesForAllAppsUL(1);
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private void updateRulesForAllAppsUL(int type) {
        if (Trace.isTagEnabled(8388608)) {
            Trace.traceBegin(8388608, "updateRulesForRestrictPowerUL-" + type);
        }
        try {
            PackageManager pm = this.mContext.getPackageManager();
            List<UserInfo> users = this.mUserManager.getUsers();
            List<ApplicationInfo> apps = pm.getInstalledApplications(795136);
            int usersSize = users.size();
            int appsSize = apps.size();
            for (int i = 0; i < usersSize; i++) {
                UserInfo user = (UserInfo) users.get(i);
                for (int j = 0; j < appsSize; j++) {
                    int uid = UserHandle.getUid(user.id, ((ApplicationInfo) apps.get(j)).uid);
                    switch (type) {
                        case 1:
                            updateRulesForDataUsageRestrictionsUL(uid);
                            break;
                        case 2:
                            updateRulesForPowerRestrictionsUL(uid);
                            break;
                        default:
                            Slog.w(TAG, "Invalid type for updateRulesForAllApps: " + type);
                            break;
                    }
                }
            }
        } finally {
            if (Trace.isTagEnabled(8388608)) {
                Trace.traceEnd(8388608);
            }
        }
    }

    private void updateRulesForTempWhitelistChangeUL() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = 0; i < users.size(); i++) {
            UserInfo user = (UserInfo) users.get(i);
            for (int j = this.mPowerSaveTempWhitelistAppIds.size() - 1; j >= 0; j--) {
                int uid = UserHandle.getUid(user.id, this.mPowerSaveTempWhitelistAppIds.keyAt(j));
                updateRuleForAppIdleUL(uid);
                updateRuleForDeviceIdleUL(uid);
                updateRuleForRestrictPowerUL(uid);
                updateRulesForPowerRestrictionsUL(uid);
            }
        }
    }

    private boolean isUidValidForBlacklistRules(int uid) {
        if (uid == 1013 || uid == 1019 || (UserHandle.isApp(uid) && hasInternetPermissions(uid))) {
            return true;
        }
        return false;
    }

    private boolean isUidValidForWhitelistRules(int uid) {
        return UserHandle.isApp(uid) ? hasInternetPermissions(uid) : false;
    }

    private boolean isUidIdle(int uid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        int userId = UserHandle.getUserId(uid);
        if (!ArrayUtils.isEmpty(packages)) {
            for (String packageName : packages) {
                if (!this.mUsageStats.isAppIdle(packageName, uid, userId)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasInternetPermissions(int uid) {
        try {
            if (this.mIPm.checkUidPermission(OppoPermissionConstants.PERMISSION_SEND_MMS_INTERNET, uid) != 0) {
                return false;
            }
        } catch (RemoteException e) {
        }
        return true;
    }

    private void updateRestrictionRulesForUidUL(int uid) {
        updateRuleForDeviceIdleUL(uid);
        updateRuleForAppIdleUL(uid);
        updateRuleForRestrictPowerUL(uid);
        updateRulesForPowerRestrictionsUL(uid);
        updateRulesForDataUsageRestrictionsUL(uid);
    }

    private void updateRulesForDataUsageRestrictionsUL(int uid) {
        updateRulesForDataUsageRestrictionsUL(uid, false);
    }

    private void updateRulesForDataUsageRestrictionsUL(int uid, boolean uidDeleted) {
        if (uidDeleted || isUidValidForWhitelistRules(uid)) {
            int uidPolicy = this.mUidPolicy.get(uid, 0);
            int oldUidRules = this.mUidRules.get(uid, 0);
            boolean isForeground = isUidForegroundOnRestrictBackgroundUL(uid);
            boolean isBlacklisted = (uidPolicy & 1) != 0;
            boolean isWhitelisted = this.mRestrictBackgroundWhitelistUids.get(uid);
            int oldRule = oldUidRules & 15;
            int newRule = 0;
            if (isForeground) {
                if (isBlacklisted || (this.mRestrictBackground && !isWhitelisted)) {
                    newRule = 2;
                } else if (isWhitelisted) {
                    newRule = 1;
                }
            } else if (isBlacklisted) {
                newRule = 4;
            } else if (this.mRestrictBackground && isWhitelisted) {
                newRule = 1;
            }
            int newUidRules = newRule | (oldUidRules & 240);
            if (LOGV) {
                Log.v(TAG, "updateRuleForRestrictBackgroundUL(" + uid + ")" + ": isForeground=" + isForeground + ", isBlacklisted=" + isBlacklisted + ", isWhitelisted=" + isWhitelisted + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldRule) + ", newRule=" + NetworkPolicyManager.uidRulesToString(newRule) + ", newUidRules=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldUidRules=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
            }
            if (newUidRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newUidRules);
            }
            if (newRule != oldRule) {
                if ((newRule & 2) != 0) {
                    setMeteredNetworkWhitelist(uid, true);
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, false);
                    }
                } else if ((oldRule & 2) != 0) {
                    if (!isWhitelisted) {
                        setMeteredNetworkWhitelist(uid, false);
                    }
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, true);
                    }
                } else if ((newRule & 4) != 0 || (oldRule & 4) != 0) {
                    setMeteredNetworkBlacklist(uid, isBlacklisted);
                    if ((oldRule & 4) != 0 && isWhitelisted) {
                        setMeteredNetworkWhitelist(uid, isWhitelisted);
                    }
                } else if ((newRule & 1) == 0 && (oldRule & 1) == 0) {
                    Log.wtf(TAG, "Unexpected change of metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", blacklisted=" + isBlacklisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
                } else {
                    setMeteredNetworkWhitelist(uid, isWhitelisted);
                }
                this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
            }
            return;
        }
        if (LOGD && ENG_DBG) {
            Slog.d(TAG, "no need to update restrict data rules for uid " + uid);
        }
    }

    private void updateRulesForPowerRestrictionsUL(int uid) {
        int newUidRules = updateRulesForPowerRestrictionsUL(uid, this.mUidRules.get(uid, 0), false);
        if (newUidRules == 0) {
            this.mUidRules.delete(uid);
        } else {
            this.mUidRules.put(uid, newUidRules);
        }
    }

    private int updateRulesForPowerRestrictionsUL(int uid, int oldUidRules, boolean paroled) {
        boolean isIdle = false;
        if (!isUidValidForBlacklistRules(uid) && LOGD && ENG_DBG) {
            Slog.d(TAG, "no need to update restrict power rules for uid " + uid);
            return 0;
        }
        if (!paroled) {
            isIdle = isUidIdle(uid);
        }
        boolean restrictMode = (isIdle || this.mRestrictPower) ? true : this.mDeviceIdleMode;
        boolean isForeground = isUidForegroundOnRestrictPowerUL(uid);
        boolean isWhitelisted = isWhitelistedBatterySaverUL(uid);
        int oldRule = oldUidRules & 240;
        int newRule = 0;
        if (isForeground) {
            if (restrictMode) {
                newRule = 32;
            }
        } else if (restrictMode) {
            newRule = isWhitelisted ? 32 : 64;
        }
        int newUidRules = (oldUidRules & 15) | newRule;
        if (LOGV) {
            Log.v(TAG, "updateRulesForPowerRestrictionsUL(" + uid + ")" + ", isIdle: " + isIdle + ", mRestrictPower: " + this.mRestrictPower + ", mDeviceIdleMode: " + this.mDeviceIdleMode + ", isForeground=" + isForeground + ", isWhitelisted=" + isWhitelisted + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldRule) + ", newRule=" + NetworkPolicyManager.uidRulesToString(newRule) + ", newUidRules=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldUidRules=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
        }
        if (newRule != oldRule) {
            if (newRule == 0 || (newRule & 32) != 0) {
                if (LOGV) {
                    Log.v(TAG, "Allowing non-metered access for UID " + uid);
                }
            } else if ((newRule & 64) == 0) {
                Log.wtf(TAG, "Unexpected change of non-metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
            } else if (LOGV) {
                Log.v(TAG, "Rejecting non-metered access for UID " + uid);
            }
            this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
        }
        return newUidRules;
    }

    private void dispatchUidRulesChanged(INetworkPolicyListener listener, int uid, int uidRules) {
        if (listener != null) {
            try {
                listener.onUidRulesChanged(uid, uidRules);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchMeteredIfacesChanged(INetworkPolicyListener listener, String[] meteredIfaces) {
        if (listener != null) {
            try {
                listener.onMeteredIfacesChanged(meteredIfaces);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundChanged(INetworkPolicyListener listener, boolean restrictBackground) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundChanged(restrictBackground);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundWhitelistChanged(INetworkPolicyListener listener, int uid, boolean whitelisted) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundWhitelistChanged(uid, whitelisted);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundBlacklistChanged(INetworkPolicyListener listener, int uid, boolean blacklisted) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundBlacklistChanged(uid, blacklisted);
            } catch (RemoteException e) {
            }
        }
    }

    private void setInterfaceQuota(String iface, long quotaBytes) {
        try {
            this.mNetworkManager.setInterfaceQuota(iface, quotaBytes);
        } catch (IllegalStateException e) {
            Log.e(TAG, "problem setting interface quota:" + e);
        } catch (RemoteException e2) {
        }
    }

    private void removeInterfaceQuota(String iface) {
        try {
            this.mNetworkManager.removeInterfaceQuota(iface);
        } catch (IllegalStateException e) {
            Log.e(TAG, "problem removing interface quota", e);
        } catch (RemoteException e2) {
        }
    }

    private void setMeteredNetworkBlacklist(int uid, boolean enable) {
        if (LOGV) {
            Slog.v(TAG, "setMeteredNetworkBlacklist " + uid + ": " + enable);
        }
        try {
            this.mNetworkManager.setUidMeteredNetworkBlacklist(uid, enable);
        } catch (IllegalStateException e) {
            Log.e(TAG, "problem setting blacklist (" + enable + ") rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    private void setMeteredNetworkWhitelist(int uid, boolean enable) {
        if (LOGV) {
            Slog.v(TAG, "setMeteredNetworkWhitelist " + uid + ": " + enable);
        }
        try {
            this.mNetworkManager.setUidMeteredNetworkWhitelist(uid, enable);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting whitelist (" + enable + ") rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    private void setUidFirewallRulesAsync(int chain, SparseIntArray uidRules, int toggle) {
        this.mHandler.obtainMessage(13, chain, toggle, uidRules).sendToTarget();
    }

    private void setUidFirewallRules(int chain, SparseIntArray uidRules) {
        try {
            int size = uidRules.size();
            int[] uids = new int[size];
            int[] rules = new int[size];
            for (int index = size - 1; index >= 0; index--) {
                uids[index] = uidRules.keyAt(index);
                rules[index] = uidRules.valueAt(index);
            }
            this.mNetworkManager.setFirewallUidRules(chain, uids, rules);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting firewall uid rules", e);
        } catch (RemoteException e2) {
        }
    }

    private void setUidFirewallRule(int chain, int uid, int rule) {
        if (chain == 1) {
            this.mUidFirewallDozableRules.put(uid, rule);
        } else if (chain == 2) {
            this.mUidFirewallStandbyRules.put(uid, rule);
        } else if (chain == 3) {
            this.mUidFirewallPowerSaveRules.put(uid, rule);
        }
        try {
            this.mNetworkManager.setFirewallUidRule(chain, uid, rule);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting firewall uid rules", e);
        } catch (RemoteException e2) {
        }
    }

    private void enableFirewallChainUL(int chain, boolean enable) {
        if (this.mFirewallChainStates.indexOfKey(chain) < 0 || this.mFirewallChainStates.get(chain) != enable) {
            this.mFirewallChainStates.put(chain, enable);
            try {
                this.mNetworkManager.setFirewallChainEnabled(chain, enable);
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem enable firewall chain", e);
            } catch (RemoteException e2) {
            }
        }
    }

    private long getTotalBytes(NetworkTemplate template, long start, long end) {
        try {
            return this.mNetworkStats.getNetworkTotalBytes(template, start, end);
        } catch (RuntimeException e) {
            Slog.w(TAG, "problem reading network stats: " + e);
            return 0;
        } catch (RemoteException e2) {
            return 0;
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

    void maybeRefreshTrustedTime() {
        if (this.mTime.getCacheAge() > 86400000 && NetworkStatsService.USE_TRUESTED_TIME) {
            this.mTime.forceRefresh();
        }
    }

    private long currentTimeMillis() {
        return (this.mTime.hasCache() && NetworkStatsService.USE_TRUESTED_TIME) ? this.mTime.currentTimeMillis() : System.currentTimeMillis();
    }

    private static Intent buildAllowBackgroundDataIntent() {
        return new Intent(ACTION_ALLOW_BACKGROUND);
    }

    private static Intent buildSnoozeWarningIntent(NetworkTemplate template) {
        Intent intent = new Intent(ACTION_SNOOZE_WARNING);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildNetworkOverLimitIntent(NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.net.NetworkOverLimitActivity"));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildViewDataUsageIntent(NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    public void addIdleHandler(IdleHandler handler) {
        this.mHandler.getLooper().getQueue().addIdleHandler(handler);
    }

    private static void collectKeys(SparseIntArray source, SparseBooleanArray target) {
        int size = source.size();
        for (int i = 0; i < size; i++) {
            target.put(source.keyAt(i), true);
        }
    }

    public void factoryReset(String subscriber) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            NetworkPolicy[] policies = getNetworkPolicies(this.mContext.getOpPackageName());
            NetworkTemplate template = NetworkTemplate.buildTemplateMobileAll(subscriber);
            for (NetworkPolicy policy : policies) {
                if (policy.template.equals(template)) {
                    policy.limitBytes = -1;
                    policy.inferred = false;
                    policy.clearSnooze();
                }
            }
            setNetworkPolicies(policies);
            setRestrictBackground(false);
            if (!this.mUserManager.hasUserRestriction("no_control_apps")) {
                for (int uid : getUidsWithPolicy(1)) {
                    setUidPolicy(uid, 0);
                }
            }
        }
    }

    public boolean getGameSpaceMode() {
        return this.mGameSpaceMode;
    }

    public void setGameSpaceMode(boolean gameMode) {
        if (gameMode) {
            SystemProperties.set("debug.gamemode.value", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        } else {
            SystemProperties.set("debug.gamemode.value", "0");
        }
        setDeviceIdleMode(gameMode);
        this.mGameSpaceMode = gameMode;
    }

    private void sendPolicyCreatedBroadcast() {
        if (LOGV) {
            Slog.v(TAG, "sendPolicyCreatedBroadcast ACTION_POLICY_CREATED");
        }
        Intent intent = new Intent("com.mediatek.server.action.ACTION_POLICY_CREATED");
        intent.addFlags(536870912);
        intent.addFlags(67108864);
        this.mContext.sendBroadcast(intent);
    }

    private void sendNetworkOverLimitChanged() {
        if (LOGV) {
            Slog.v(TAG, "sendPolicyCreatedBroadcast ACTION_NETWORK_OVERLIMIT_CHANGED");
        }
        Intent intent = new Intent("com.mediatek.server.action.ACTION_NETWORK_OVERLIMIT_CHANGED");
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
    }
}

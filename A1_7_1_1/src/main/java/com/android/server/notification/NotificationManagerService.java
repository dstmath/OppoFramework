package com.android.server.notification;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AutomaticZenRule;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.ITransientNotification;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.app.OppoActivityManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.backup.BackupManager;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.IRingtonePlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.INotificationListener.Stub;
import android.service.notification.IStatusBarNotificationHolder;
import android.service.notification.NotificationRankingUpdate;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.WindowManagerInternal;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.SystemService;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.am.OppoPhoneStateReceiver;
import com.android.server.am.OppoProcessManager;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.notification.ManagedServices.Config;
import com.android.server.notification.ManagedServices.ManagedServiceInfo;
import com.android.server.notification.ManagedServices.UserProfiles;
import com.android.server.notification.ZenModeHelper.Callback;
import com.android.server.oppo.IElsaManager;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.vr.VrManagerInternal;
import com.mediatek.common.dm.DmAgent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import oppo.util.OppoMultiLauncherUtil;
import oppo.util.OppoStatistics;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
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
public class NotificationManagerService extends SystemService {
    private static final String ATTR_VERSION = "version";
    static final boolean DBG = true;
    private static final int DB_VERSION = 1;
    private static boolean DEBUG_PANIC = false;
    static final float DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE = 10.0f;
    static final int DEFAULT_STREAM_TYPE = 5;
    static final long[] DEFAULT_VIBRATE_PATTERN = null;
    private static final String DISTURB_FOR_GAME_SPACE_MODE = "disturb_for_game_space_mode";
    static final boolean ENABLE_BLOCKED_NOTIFICATIONS = true;
    static final boolean ENABLE_BLOCKED_TOASTS = true;
    public static final boolean ENABLE_CHILD_NOTIFICATIONS = false;
    static final String EVENTID_FOR_BLOCKED_BY_BLACKLIST = "notification_blacklist";
    static final String EVENTID_FOR_BLOCKED_BY_DYNAMIC = "notification_dynamic";
    static final String EVENTID_FOR_BLOCKED_BY_NORMAL = "notification_normal";
    static final String EVENTID_FOR_BLOCKED_BY_UNNORMAL = "notification_unnormal";
    private static final int EVENTLOG_ENQUEUE_STATUS_IGNORED = 2;
    private static final int EVENTLOG_ENQUEUE_STATUS_NEW = 0;
    private static final int EVENTLOG_ENQUEUE_STATUS_UPDATE = 1;
    static final String EVENT_PACKAGE = "package_name";
    private static final String[] GAME_SPACE_WHITELIST = null;
    static final int LONG_DELAY = 3500;
    static final int MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS = 3000;
    static final float MATCHES_CALL_FILTER_TIMEOUT_AFFINITY = 1.0f;
    static final int MAX_NOTIFICATION_IS_BLOCKED_BY_BLACKLIST = 20;
    static final int MAX_NOTIFICATION_IS_BLOCKED_BY_DYNAMIC = 0;
    static final int MAX_NOTIFICATION_IS_BLOCKED_BY_NORMAL = 20;
    static final int MAX_NOTIFICATION_IS_BLOCKED_BY_UNNORMAL = 20;
    static final int MAX_PACKAGE_NOTIFICATIONS = 50;
    static final int MESSAGE_LISTENER_HINTS_CHANGED = 5;
    static final int MESSAGE_LISTENER_NOTIFICATION_FILTER_CHANGED = 6;
    private static final int MESSAGE_RANKING_SORT = 1001;
    private static final int MESSAGE_RECONSIDER_RANKING = 1000;
    static final int MESSAGE_SAVE_POLICY_FILE = 3;
    static final int MESSAGE_SEND_RANKING_UPDATE = 4;
    static final int MESSAGE_TIMEOUT = 2;
    private static final long MIN_PACKAGE_OVERRATE_LOG_INTERVAL = 5000;
    private static final int MY_PID = 0;
    private static final int MY_UID = 0;
    static final int NOTIFICATION_FILTER_SIZE = 3;
    static final int NOTIFICATION_PACKAGE_NAME = 0;
    static final int NOTIFICATION_SUMMARY_NAME = 2;
    static final int NOTIFICATION_TITLE_NAME = 1;
    public static final String NOTIFICATON_TITLE_NAME = "notification";
    public static final String OMADM_LAWMO_LOCK = "com.mediatek.dm.LAWMO_LOCK";
    public static final String OMADM_LAWMO_UNLOCK = "com.mediatek.dm.LAWMO_UNLOCK";
    private static final String OPPO_NOTIFICATION_BLACKLIST_DIRECTORY = "/data/system/config";
    private static final String OPPO_NOTIFICATION_BLACKLIST_FILE_PATH = "/data/system/config/sys_nms_intercept_blacklist.xml";
    private static final String OPPO_VERSION_EXP = "oppo.version.exp";
    private static final String PKG_GAMECENTER = "com.nearme.gamecenter";
    public static final String PPL_LOCK = "com.mediatek.ppl.NOTIFY_LOCK";
    public static final String PPL_UNLOCK = "com.mediatek.ppl.NOTIFY_UNLOCK";
    static final int SHORT_DELAY = 2000;
    public static final int STEP = 1;
    static final String TAG = "NotificationService";
    private static final String TAG_NOTIFICATION_POLICY = "notification-policy";
    public static final long TIME_UPLOAD_THRESHOLD = 10800000;
    static final int VIBRATE_PATTERN_MAXLEN = 17;
    public static long mLastUploadStaticsDataTime;
    public static Map<String, String> sEventMap;
    private int currentFlag;
    private int lastFlag;
    private IActivityManager mAm;
    private AppOpsManager mAppOps;
    private UsageStatsManagerInternal mAppUsageStats;
    private Archive mArchive;
    Light mAttentionLight;
    AudioManager mAudioManager;
    AudioManagerInternal mAudioManagerInternal;
    final ArrayMap<Integer, ArrayMap<String, String>> mAutobundledSummaries;
    private ArrayList<String> mBlacklistNotificationStatisticList;
    private final Runnable mBuzzBeepBlinked;
    private int mCallState;
    private ConditionProviders mConditionProviders;
    private int mDefaultNotificationColor;
    private int mDefaultNotificationLedOff;
    private int mDefaultNotificationLedOn;
    private long[] mDefaultVibrationPattern;
    private boolean mDisableNotificationEffects;
    int mDisabledNotifications;
    private boolean mDmLock;
    public List<String[]> mDynamicFilterNotificationList;
    private ArrayList<String> mDynamicNotificationStatisticList;
    private List<ComponentName> mEffectsSuppressors;
    private long[] mFallbackVibrationPattern;
    final IBinder mForegroundToken;
    private boolean mGameSpaceMode;
    private Handler mHandler;
    private boolean mInCall;
    private final BroadcastReceiver mIntentReceiver;
    private final NotificationManagerInternal mInternalService;
    private int mInterruptionFilter;
    private boolean mIsShutDown;
    private long mLastOverRateLogTime;
    ArrayList<String> mLights;
    private int mListenerHints;
    private NotificationListeners mListeners;
    private final SparseArray<ArraySet<ManagedServiceInfo>> mListenersDisablingEffects;
    private float mMaxPackageEnqueueRate;
    private ArrayList<String> mNormalNotificationStatisticList;
    private final NotificationDelegate mNotificationDelegate;
    private FileObserverPolicy mNotificationFileObserver;
    private Light mNotificationLight;
    final ArrayList<NotificationRecord> mNotificationList;
    private ArrayList<String> mNotificationNoClear;
    private boolean mNotificationPulseEnabled;
    final ArrayMap<String, NotificationRecord> mNotificationsByKey;
    private boolean mOppoLock;
    private OppoSettingsObserver mOppoSettingsObserver;
    private final BroadcastReceiver mPackageIntentReceiver;
    PhoneStateListener mPhoneStateListener;
    final PolicyAccess mPolicyAccess;
    private AtomicFile mPolicyFile;
    private boolean mPplLock;
    private String mRankerServicePackageName;
    private NotificationRankers mRankerServices;
    private RankingHandler mRankingHandler;
    private RankingHelper mRankingHelper;
    private final HandlerThread mRankingThread;
    private boolean mScreenOn;
    private final IBinder mService;
    private SettingsObserver mSettingsObserver;
    private String mSoundNotificationKey;
    StatusBarManagerInternal mStatusBar;
    final ArrayMap<String, NotificationRecord> mSummaryByGroupKey;
    private String mSystemNotificationSound;
    boolean mSystemReady;
    final ArrayList<ToastRecord> mToastQueue;
    private ArrayList<String> mUnNormalNotificationStatisticList;
    private NotificationUsageStats mUsageStats;
    private boolean mUseAttentionLight;
    private final UserProfiles mUserProfiles;
    private String mVibrateNotificationKey;
    private boolean mVibrateWhenRingingEnabled;
    Vibrator mVibrator;
    private VrManagerInternal mVrManagerInternal;
    private WindowManagerInternal mWindowManagerInternal;
    private ZenModeHelper mZenModeHelper;

    private static class Archive {
        final ArrayDeque<StatusBarNotification> mBuffer = new ArrayDeque(this.mBufferSize);
        final int mBufferSize;

        public Archive(int size) {
            this.mBufferSize = size;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            int N = this.mBuffer.size();
            sb.append("Archive (");
            sb.append(N);
            sb.append(" notification");
            sb.append(N == 1 ? ")" : "s)");
            return sb.toString();
        }

        public void record(StatusBarNotification nr) {
            if (this.mBuffer.size() == this.mBufferSize) {
                this.mBuffer.removeFirst();
            }
            this.mBuffer.addLast(nr.cloneLight());
        }

        public Iterator<StatusBarNotification> descendingIterator() {
            return this.mBuffer.descendingIterator();
        }

        public StatusBarNotification[] getArray(int count) {
            if (count == 0) {
                count = this.mBufferSize;
            }
            StatusBarNotification[] a = new StatusBarNotification[Math.min(count, this.mBuffer.size())];
            Iterator<StatusBarNotification> iter = descendingIterator();
            int i = 0;
            while (iter.hasNext() && i < count) {
                int i2 = i + 1;
                a[i] = (StatusBarNotification) iter.next();
                i = i2;
            }
            return a;
        }
    }

    public static final class DumpFilter {
        public boolean filtered = false;
        public String pkgFilter;
        public boolean redact = true;
        public long since;
        public boolean stats;
        public boolean zen;

        public static DumpFilter parseFromArguments(String[] args) {
            DumpFilter filter = new DumpFilter();
            int ai = 0;
            while (ai < args.length) {
                String a = args[ai];
                if ("--noredact".equals(a) || "--reveal".equals(a)) {
                    filter.redact = false;
                } else if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(a) || "pkg".equals(a) || "--package".equals(a)) {
                    if (ai < args.length - 1) {
                        ai++;
                        filter.pkgFilter = args[ai].trim().toLowerCase();
                        if (filter.pkgFilter.isEmpty()) {
                            filter.pkgFilter = null;
                        } else {
                            filter.filtered = true;
                        }
                    }
                } else if ("--zen".equals(a) || "zen".equals(a)) {
                    filter.filtered = true;
                    filter.zen = true;
                } else if ("--stats".equals(a)) {
                    filter.stats = true;
                    if (ai < args.length - 1) {
                        ai++;
                        filter.since = Long.valueOf(args[ai]).longValue();
                    } else {
                        filter.since = 0;
                    }
                }
                ai++;
            }
            return filter;
        }

        public boolean matches(StatusBarNotification sbn) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen) {
                if (sbn == null) {
                    z = false;
                } else if (!matches(sbn.getPackageName())) {
                    z = matches(sbn.getOpPkg());
                }
            }
            return z;
        }

        public boolean matches(ComponentName component) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen) {
                z = component != null ? matches(component.getPackageName()) : false;
            }
            return z;
        }

        public boolean matches(String pkg) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen) {
                z = pkg != null ? pkg.toLowerCase().contains(this.pkgFilter) : false;
            }
            return z;
        }

        public String toString() {
            if (this.stats) {
                return "stats";
            }
            return this.zen ? "zen" : '\'' + this.pkgFilter + '\'';
        }
    }

    private class EnqueueNotificationRunnable implements Runnable {
        private final NotificationRecord r;
        private final int userId;

        EnqueueNotificationRunnable(int userId, NotificationRecord r) {
            this.userId = userId;
            this.r = r;
        }

        /* JADX WARNING: Removed duplicated region for block: B:87:0x0255  */
        /* JADX WARNING: Missing block: B:25:0x00ea, code:
            return;
     */
        /* JADX WARNING: Missing block: B:103:0x0312, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (NotificationManagerService.this.mNotificationList) {
                StatusBarNotification n = this.r.sbn;
                Slog.d(NotificationManagerService.TAG, "EnqueueNotificationRunnable.run for: " + n.getKey());
                NotificationRecord old = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(n.getKey());
                if (old != null) {
                    this.r.copyRankingInformation(old);
                }
                int callingUid = n.getUid();
                int callingPid = n.getInitialPid();
                Notification notification = n.getNotification();
                String pkg = n.getPackageName();
                int id = n.getId();
                String tag = n.getTag();
                if (!NotificationManagerService.isUidSystem(callingUid)) {
                    boolean equals = "android".equals(pkg);
                }
                NotificationManagerService.this.handleGroupedNotificationLocked(this.r, old, callingUid, callingPid);
                if (!pkg.equals("com.android.providers.downloads") || Log.isLoggable("DownloadManager", 2)) {
                    int enqueueStatus = 0;
                    if (old != null) {
                        enqueueStatus = 1;
                    }
                    EventLogTags.writeNotificationEnqueue(callingUid, callingPid, pkg, id, tag, this.userId, notification.toString(), enqueueStatus);
                }
                NotificationManagerService.this.mRankingHelper.extractSignals(this.r);
                boolean isPackageSuspended = NotificationManagerService.this.isPackageSuspendedForUser(pkg, callingUid);
                if (this.r.getImportance() != 0 && NotificationManagerService.this.noteNotificationOp(pkg, callingUid) && !isPackageSuspended) {
                    int index;
                    if (NotificationManagerService.this.mRankerServices.isEnabled()) {
                        NotificationManagerService.this.mRankerServices.onNotificationEnqueued(this.r);
                    }
                    if (!NotificationManagerService.this.getContext().getPackageManager().hasSystemFeature(NotificationManagerService.OPPO_VERSION_EXP)) {
                        boolean isStandardNotification = true;
                        if (!pkg.contains("com.android.cts.robot")) {
                            boolean isOngoing = (notification.flags & 2) != 0;
                            boolean isForegroundService = (notification.flags & 64) != 0;
                            if (!(notification == null || notification.contentView == null || NotificationManagerService.this.isSystemApp(pkg) || notification.contentView.toString().contains("android.app.Notification") || isOngoing || isForegroundService)) {
                                isStandardNotification = false;
                                Slog.d(NotificationManagerService.TAG, pkg + " is not standard notification,so we discard it! we statistic it also!");
                                NotificationManagerService.this.mUnNormalNotificationStatisticList.add(pkg);
                                if (NotificationManagerService.this.mUnNormalNotificationStatisticList.size() > 20) {
                                    NotificationManagerService.this.sendDataToDcs(NotificationManagerService.EVENTID_FOR_BLOCKED_BY_UNNORMAL, NotificationManagerService.this.mUnNormalNotificationStatisticList);
                                }
                                if (NotificationManagerService.DEBUG_PANIC) {
                                    Slog.d(NotificationManagerService.TAG, "we discard it, because " + pkg + " is unstandard notification!");
                                }
                            }
                            if (isStandardNotification) {
                                NotificationManagerService.this.mNormalNotificationStatisticList.add(pkg);
                                if (NotificationManagerService.this.mNormalNotificationStatisticList.size() > 20) {
                                    NotificationManagerService.this.sendDataToDcs(NotificationManagerService.EVENTID_FOR_BLOCKED_BY_NORMAL, NotificationManagerService.this.mNormalNotificationStatisticList);
                                }
                            }
                        }
                    }
                    if (notification != null) {
                        String title = notification.extras.getString("android.title");
                        String text = notification.extras.getString("android.text");
                        if (!(title == null || NotificationManagerService.this.mDynamicFilterNotificationList == null || NotificationManagerService.this.mDynamicFilterNotificationList.size() <= 0)) {
                            boolean isNeedBlock = false;
                            for (index = 0; index < NotificationManagerService.this.mDynamicFilterNotificationList.size(); index++) {
                                String[] tempArray = (String[]) NotificationManagerService.this.mDynamicFilterNotificationList.get(index);
                                if (tempArray != null && tempArray.length == 3 && ("null".equals(tempArray[0]) || pkg.equals(tempArray[0]))) {
                                    if (!"null".equals(tempArray[1])) {
                                        if (!title.equals(tempArray[1])) {
                                            continue;
                                        }
                                    }
                                    if (!"null".equals(tempArray[2])) {
                                        if (text.contains(tempArray[2])) {
                                        }
                                    }
                                    isNeedBlock = true;
                                    if (NotificationManagerService.DEBUG_PANIC) {
                                        Slog.d(NotificationManagerService.TAG, "notification.sendTitleData_hasMatched!");
                                    }
                                    if (isNeedBlock) {
                                        String resultString = pkg + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + title + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + text;
                                        if (!resultString.isEmpty()) {
                                            if (NotificationManagerService.DEBUG_PANIC) {
                                                Slog.d(NotificationManagerService.TAG, "resultString = " + resultString);
                                            }
                                            NotificationManagerService.this.mDynamicNotificationStatisticList.add(resultString);
                                        }
                                        if (NotificationManagerService.this.mDynamicNotificationStatisticList.size() > 0) {
                                            if (NotificationManagerService.DEBUG_PANIC) {
                                                Slog.d(NotificationManagerService.TAG, "notification.sendDynamicInterceptData");
                                            }
                                            NotificationManagerService.this.sendDataToDcs(NotificationManagerService.EVENTID_FOR_BLOCKED_BY_DYNAMIC, NotificationManagerService.this.mDynamicNotificationStatisticList);
                                        }
                                        if (NotificationManagerService.DEBUG_PANIC) {
                                            Slog.d(NotificationManagerService.TAG, "we discard it, because " + pkg + " is in list of Dynamic intercept notification!");
                                        }
                                    }
                                }
                            }
                            if (isNeedBlock) {
                            }
                        }
                    }
                    index = NotificationManagerService.this.indexOfNotificationLocked(n.getKey());
                    if (index < 0) {
                        NotificationManagerService.this.mNotificationList.add(this.r);
                        NotificationManagerService.this.mUsageStats.registerPostedByApp(this.r);
                    } else {
                        old = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(index);
                        NotificationManagerService.this.mNotificationList.set(index, this.r);
                        NotificationManagerService.this.mUsageStats.registerUpdatedByApp(this.r, old);
                        notification.flags |= old.getNotification().flags & 64;
                        this.r.isUpdate = true;
                    }
                    NotificationManagerService.this.mNotificationsByKey.put(n.getKey(), this.r);
                    if ((notification.flags & 64) != 0) {
                        notification.flags |= 34;
                    }
                    NotificationManagerService.this.applyZenModeLocked(this.r);
                    NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                    if (notification.getSmallIcon() == null || (notification.flags & 268435456) != 0) {
                        Slog.e(NotificationManagerService.TAG, "Not posting notification without small icon: " + notification);
                        if (!(old == null || old.isCanceled)) {
                            NotificationManagerService.this.mListeners.notifyRemovedLocked(n);
                        }
                        Slog.e(NotificationManagerService.TAG, "WARNING: In a future release this will crash the app: " + n.getPackageName());
                    } else {
                        NotificationManagerService.this.mListeners.notifyPostedLocked(n, old != null ? old.sbn : null);
                    }
                    NotificationManagerService.this.buzzBeepBlinkLocked(this.r);
                } else if (isPackageSuspended) {
                    Slog.e(NotificationManagerService.TAG, "Suppressing notification from package due to package suspended by administrator.");
                    NotificationManagerService.this.mUsageStats.registerSuspendedByAdmin(this.r);
                } else {
                    Slog.e(NotificationManagerService.TAG, "Suppressing notification from package by user request.");
                    NotificationManagerService.this.mUsageStats.registerBlocked(this.r);
                }
            }
        }
    }

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
            if (NotificationManagerService.DEBUG_PANIC) {
                Log.d(NotificationManagerService.TAG, "FileObserverPolicy_path = " + path);
            }
        }

        public void onEvent(int event, String path) {
            if (NotificationManagerService.DEBUG_PANIC) {
                Log.d(NotificationManagerService.TAG, "onEvent: event = " + event + ",focusPath = " + this.focusPath);
            }
            if (event == 8 && this.focusPath.equals(NotificationManagerService.OPPO_NOTIFICATION_BLACKLIST_FILE_PATH)) {
                if (NotificationManagerService.DEBUG_PANIC) {
                    Log.d(NotificationManagerService.TAG, "onEvent: focusPath = OPPO_NOTIFICATION_BLACKLIST_FILE_PATH");
                }
                NotificationManagerService.this.readConfigFile();
            }
        }
    }

    public class NotificationListeners extends ManagedServices {
        private final ArraySet<ManagedServiceInfo> mLightTrimListeners = new ArraySet();

        public NotificationListeners() {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mHandler, NotificationManagerService.this.mNotificationList, NotificationManagerService.this.mUserProfiles);
        }

        protected Config getConfig() {
            Config c = new Config();
            c.caption = "notification listener";
            c.serviceInterface = "android.service.notification.NotificationListenerService";
            c.secureSettingName = "enabled_notification_listeners";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
            c.settingsAction = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
            c.clientLabel = 17040511;
            return c;
        }

        protected IInterface asInterface(IBinder binder) {
            return Stub.asInterface(binder);
        }

        protected boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        public void onServiceAdded(ManagedServiceInfo info) {
            NotificationRankingUpdate update;
            INotificationListener listener = info.service;
            synchronized (NotificationManagerService.this.mNotificationList) {
                update = NotificationManagerService.this.makeRankingUpdateLocked(info);
            }
            try {
                listener.onListenerConnected(update);
            } catch (RemoteException e) {
            }
        }

        protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
            if (NotificationManagerService.this.removeDisabledHints(removed)) {
                NotificationManagerService.this.updateListenerHintsLocked();
                NotificationManagerService.this.updateEffectsSuppressorLocked();
            }
            this.mLightTrimListeners.remove(removed);
        }

        public void setOnNotificationPostedTrimLocked(ManagedServiceInfo info, int trim) {
            if (trim == 1) {
                this.mLightTrimListeners.add(info);
            } else {
                this.mLightTrimListeners.remove(info);
            }
        }

        public int getOnNotificationPostedTrim(ManagedServiceInfo info) {
            return this.mLightTrimListeners.contains(info) ? 1 : 0;
        }

        public void notifyPostedLocked(StatusBarNotification sbn, StatusBarNotification oldSbn) {
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : this.mServices) {
                boolean sbnVisible = NotificationManagerService.this.isVisibleToListener(sbn, info);
                boolean oldSbnVisible = oldSbn != null ? NotificationManagerService.this.isVisibleToListener(oldSbn, info) : false;
                if (oldSbnVisible || sbnVisible) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    if (!oldSbnVisible || sbnVisible) {
                        final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                NotificationListeners.this.notifyPosted(info, sbnToPost, update);
                            }
                        });
                    } else {
                        final StatusBarNotification oldSbnLightClone = oldSbn.cloneLight();
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                NotificationListeners.this.notifyRemoved(info, oldSbnLightClone, update);
                            }
                        });
                    }
                }
            }
        }

        public void notifyRemovedLocked(StatusBarNotification sbn) {
            final StatusBarNotification sbnLight = sbn.cloneLight();
            for (final ManagedServiceInfo info : this.mServices) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyRemoved(info, sbnLight, update);
                        }
                    });
                }
            }
        }

        public void notifyRankingUpdateLocked() {
            for (final ManagedServiceInfo serviceInfo : this.mServices) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(serviceInfo);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyRankingUpdate(serviceInfo, update);
                        }
                    });
                }
            }
        }

        public void notifyListenerHintsChangedLocked(final int hints) {
            for (final ManagedServiceInfo serviceInfo : this.mServices) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyListenerHintsChanged(serviceInfo, hints);
                        }
                    });
                }
            }
        }

        public void notifyInterruptionFilterChanged(final int interruptionFilter) {
            for (final ManagedServiceInfo serviceInfo : this.mServices) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyInterruptionFilterChanged(serviceInfo, interruptionFilter);
                        }
                    });
                }
            }
        }

        private void notifyPosted(ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationPosted(new StatusBarNotificationHolder(sbn), rankingUpdate);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (posted): " + listener, ex);
            }
        }

        private void notifyRemoved(ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate) {
            if (info.enabledAndUserMatches(sbn.getUserId())) {
                INotificationListener listener = info.service;
                try {
                    listener.onNotificationRemoved(new StatusBarNotificationHolder(sbn), rankingUpdate);
                } catch (RemoteException ex) {
                    Log.e(this.TAG, "unable to notify listener (removed): " + listener, ex);
                }
            }
        }

        private void notifyRankingUpdate(ManagedServiceInfo info, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationRankingUpdate(rankingUpdate);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (ranking update): " + listener, ex);
            }
        }

        private void notifyListenerHintsChanged(ManagedServiceInfo info, int hints) {
            INotificationListener listener = info.service;
            try {
                listener.onListenerHintsChanged(hints);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (listener hints): " + listener, ex);
            }
        }

        private void notifyInterruptionFilterChanged(ManagedServiceInfo info, int interruptionFilter) {
            INotificationListener listener = info.service;
            try {
                listener.onInterruptionFilterChanged(interruptionFilter);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (interruption filter): " + listener, ex);
            }
        }

        private boolean isListenerPackage(String packageName) {
            if (packageName == null) {
                return false;
            }
            synchronized (NotificationManagerService.this.mNotificationList) {
                for (ManagedServiceInfo serviceInfo : this.mServices) {
                    if (packageName.equals(serviceInfo.component.getPackageName())) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public class NotificationRankers extends ManagedServices {
        public NotificationRankers() {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mHandler, NotificationManagerService.this.mNotificationList, NotificationManagerService.this.mUserProfiles);
        }

        protected Config getConfig() {
            Config c = new Config();
            c.caption = "notification ranker service";
            c.serviceInterface = "android.service.notification.NotificationRankerService";
            c.secureSettingName = null;
            c.bindPermission = "android.permission.BIND_NOTIFICATION_RANKER_SERVICE";
            c.settingsAction = "android.settings.MANAGE_DEFAULT_APPS_SETTINGS";
            c.clientLabel = 17040514;
            return c;
        }

        protected IInterface asInterface(IBinder binder) {
            return Stub.asInterface(binder);
        }

        protected boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        protected void onServiceAdded(ManagedServiceInfo info) {
            NotificationManagerService.this.mListeners.registerGuestService(info);
        }

        protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
            NotificationManagerService.this.mListeners.unregisterService(removed.service, removed.userid);
        }

        public void onNotificationEnqueued(NotificationRecord r) {
            StatusBarNotification sbn = r.sbn;
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : this.mServices) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    final int importance = r.getImportance();
                    final boolean fromUser = r.isImportanceFromUser();
                    final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationRankers.this.notifyEnqueued(info, sbnToPost, importance, fromUser);
                        }
                    });
                }
            }
        }

        private void notifyEnqueued(ManagedServiceInfo info, StatusBarNotification sbn, int importance, boolean fromUser) {
            INotificationListener ranker = info.service;
            try {
                ranker.onNotificationEnqueued(new StatusBarNotificationHolder(sbn), importance, fromUser);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify ranker (enqueued): " + ranker, ex);
            }
        }

        public boolean isEnabled() {
            return !this.mServices.isEmpty();
        }

        public void onUserSwitched(int user) {
            synchronized (NotificationManagerService.this.mNotificationList) {
                int i = this.mServices.size() - 1;
                while (true) {
                    int i2 = i - 1;
                    if (i > 0) {
                        ManagedServiceInfo info = (ManagedServiceInfo) this.mServices.get(i2);
                        unregisterService(info.service, info.userid);
                        i = i2;
                    }
                }
            }
            registerRanker();
        }

        public void onPackagesChanged(boolean removingPackage, String[] pkgList) {
            Object obj = null;
            if (this.DEBUG) {
                String str = this.TAG;
                StringBuilder append = new StringBuilder().append("onPackagesChanged removingPackage=").append(removingPackage).append(" pkgList=");
                if (pkgList != null) {
                    obj = Arrays.asList(pkgList);
                }
                Slog.d(str, append.append(obj).toString());
            }
            if (!(NotificationManagerService.this.mRankerServicePackageName == null || pkgList == null || pkgList.length <= 0 || removingPackage)) {
                for (String pkgName : pkgList) {
                    if (NotificationManagerService.this.mRankerServicePackageName.equals(pkgName)) {
                        registerRanker();
                    }
                }
            }
        }

        protected void registerRanker() {
            if (NotificationManagerService.this.mRankerServicePackageName == null) {
                Slog.w(this.TAG, "could not start ranker service: no package specified!");
                return;
            }
            Set<ComponentName> rankerComponents = queryPackageForServices(NotificationManagerService.this.mRankerServicePackageName, 0);
            Iterator<ComponentName> iterator = rankerComponents.iterator();
            if (iterator.hasNext()) {
                ComponentName rankerComponent = (ComponentName) iterator.next();
                if (iterator.hasNext()) {
                    Slog.e(this.TAG, "found multiple ranker services:" + rankerComponents);
                } else {
                    registerSystemService(rankerComponent, 0);
                }
            } else {
                Slog.w(this.TAG, "could not start ranker service: none found");
            }
        }
    }

    class OppoSettingsObserver extends ContentObserver {
        private static final String OPPO_LOCK_FLAG = "oppo_lock_flag";
        private final Uri DISTURB_FOR_GAME_SPACE_MODE_URI = Global.getUriFor(NotificationManagerService.DISTURB_FOR_GAME_SPACE_MODE);
        private final Uri OPPO_LOCK_URI = Secure.getUriFor(OPPO_LOCK_FLAG);
        private final Uri VIBRATE_WHEN_RINGING_URI = System.getUriFor("vibrate_when_ringing");

        OppoSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            try {
                ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
                resolver.registerContentObserver(this.VIBRATE_WHEN_RINGING_URI, false, this, -1);
                resolver.registerContentObserver(this.DISTURB_FOR_GAME_SPACE_MODE_URI, false, this, -1);
                resolver.registerContentObserver(this.OPPO_LOCK_URI, false, this, -1);
                update(null);
            } catch (Exception e) {
                Slog.w(NotificationManagerService.TAG, "OppoSettingsObserver error", e);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            if (uri == null || this.VIBRATE_WHEN_RINGING_URI.equals(uri)) {
                boolean vibreateEnabled = System.getInt(resolver, "vibrate_when_ringing", 0) != 0;
                if (NotificationManagerService.this.mVibrateWhenRingingEnabled != vibreateEnabled) {
                    NotificationManagerService.this.mVibrateWhenRingingEnabled = vibreateEnabled;
                }
            }
            if (uri == null || this.DISTURB_FOR_GAME_SPACE_MODE_URI.equals(uri)) {
                boolean gameSpaceMode = Global.getInt(resolver, NotificationManagerService.DISTURB_FOR_GAME_SPACE_MODE, 1) != 1;
                if (NotificationManagerService.this.mGameSpaceMode != gameSpaceMode) {
                    NotificationManagerService.this.mGameSpaceMode = gameSpaceMode;
                }
            }
            if (uri == null || this.OPPO_LOCK_URI.equals(uri)) {
                boolean oppoLock = Secure.getInt(resolver, OPPO_LOCK_FLAG, 0) != 0;
                if (NotificationManagerService.this.mOppoLock != oppoLock) {
                    NotificationManagerService.this.mOppoLock = oppoLock;
                }
            }
        }
    }

    private final class PolicyAccess {
        private static final String SEPARATOR = ":";
        private final String[] PERM;

        /* synthetic */ PolicyAccess(NotificationManagerService this$0, PolicyAccess policyAccess) {
            this();
        }

        private PolicyAccess() {
            String[] strArr = new String[1];
            strArr[0] = "android.permission.ACCESS_NOTIFICATION_POLICY";
            this.PERM = strArr;
        }

        public boolean isPackageGranted(String pkg) {
            return pkg != null ? getGrantedPackages().contains(pkg) : false;
        }

        public void put(String pkg, boolean granted) {
            if (pkg != null) {
                boolean changed;
                ArraySet<String> pkgs = getGrantedPackages();
                if (granted) {
                    changed = pkgs.add(pkg);
                } else {
                    changed = pkgs.remove(pkg);
                }
                if (changed) {
                    String setting = TextUtils.join(SEPARATOR, pkgs);
                    int currentUser = ActivityManager.getCurrentUser();
                    Secure.putStringForUser(NotificationManagerService.this.getContext().getContentResolver(), "enabled_notification_policy_access_packages", setting, currentUser);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(pkg).addFlags(1073741824), new UserHandle(currentUser), null);
                }
            }
        }

        public ArraySet<String> getGrantedPackages() {
            ArraySet<String> pkgs = new ArraySet();
            long identity = Binder.clearCallingIdentity();
            try {
                String setting = Secure.getStringForUser(NotificationManagerService.this.getContext().getContentResolver(), "enabled_notification_policy_access_packages", ActivityManager.getCurrentUser());
                if (setting != null) {
                    String[] tokens = setting.split(SEPARATOR);
                    for (String token : tokens) {
                        String token2;
                        if (token2 != null) {
                            token2 = token2.trim();
                        }
                        if (!TextUtils.isEmpty(token2)) {
                            pkgs.add(token2);
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
                return pkgs;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public String[] getRequestingPackages() throws RemoteException {
            List<PackageInfo> pkgs = AppGlobals.getPackageManager().getPackagesHoldingPermissions(this.PERM, 0, ActivityManager.getCurrentUser()).getList();
            if (pkgs == null || pkgs.isEmpty()) {
                return new String[0];
            }
            int N = pkgs.size();
            String[] rt = new String[N];
            for (int i = 0; i < N; i++) {
                rt[i] = ((PackageInfo) pkgs.get(i)).packageName;
            }
            return rt;
        }
    }

    private final class RankingHandlerWorker extends Handler implements RankingHandler {
        public RankingHandlerWorker(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    NotificationManagerService.this.handleRankingReconsideration(msg);
                    return;
                case 1001:
                    NotificationManagerService.this.handleRankingSort();
                    return;
                default:
                    return;
            }
        }

        public void requestSort() {
            removeMessages(1001);
            sendEmptyMessage(1001);
        }

        public void requestReconsideration(RankingReconsideration recon) {
            sendMessageDelayed(Message.obtain(this, 1000, recon), recon.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri NOTIFICATION_LIGHT_PULSE_URI = System.getUriFor("notification_light_pulse");
        private final Uri NOTIFICATION_RATE_LIMIT_URI = Global.getUriFor("max_notification_enqueue_rate");
        private final Uri NOTIFICATION_SOUND_URI = System.getUriFor("notification_sound");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            resolver.registerContentObserver(this.NOTIFICATION_LIGHT_PULSE_URI, false, this, -1);
            resolver.registerContentObserver(this.NOTIFICATION_SOUND_URI, false, this, -1);
            resolver.registerContentObserver(this.NOTIFICATION_RATE_LIMIT_URI, false, this, -1);
            update(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            if (uri == null || this.NOTIFICATION_LIGHT_PULSE_URI.equals(uri)) {
                boolean pulseEnabled = System.getInt(resolver, "notification_light_pulse", 0) != 0;
                if (NotificationManagerService.this.mNotificationPulseEnabled != pulseEnabled) {
                    NotificationManagerService.this.mNotificationPulseEnabled = pulseEnabled;
                    NotificationManagerService.this.updateNotificationPulse();
                }
            }
            if (uri == null || this.NOTIFICATION_RATE_LIMIT_URI.equals(uri)) {
                NotificationManagerService.this.mMaxPackageEnqueueRate = Global.getFloat(resolver, "max_notification_enqueue_rate", NotificationManagerService.this.mMaxPackageEnqueueRate);
            }
            if (uri == null || this.NOTIFICATION_SOUND_URI.equals(uri)) {
                NotificationManagerService.this.mSystemNotificationSound = System.getString(resolver, "notification_sound");
            }
        }
    }

    private static final class StatusBarNotificationHolder extends IStatusBarNotificationHolder.Stub {
        private StatusBarNotification mValue;

        public StatusBarNotificationHolder(StatusBarNotification value) {
            this.mValue = value;
        }

        public StatusBarNotification get() {
            StatusBarNotification value = this.mValue;
            this.mValue = null;
            return value;
        }
    }

    private static final class ToastRecord {
        final ITransientNotification callback;
        int duration;
        final int pid;
        final String pkg;
        Binder token;

        ToastRecord(int pid, String pkg, ITransientNotification callback, int duration, Binder token) {
            this.pid = pid;
            this.pkg = pkg;
            this.callback = callback;
            this.duration = duration;
            this.token = token;
        }

        void update(int duration) {
            this.duration = duration;
        }

        void dump(PrintWriter pw, String prefix, DumpFilter filter) {
            if (filter == null || filter.matches(this.pkg)) {
                pw.println(prefix + this);
            }
        }

        public final String toString() {
            return "ToastRecord{" + Integer.toHexString(System.identityHashCode(this)) + " pkg=" + this.pkg + " callback=" + this.callback + " duration=" + this.duration;
        }
    }

    private class TrimCache {
        StatusBarNotification heavy;
        StatusBarNotification sbnClone;
        StatusBarNotification sbnCloneLight;

        TrimCache(StatusBarNotification sbn) {
            this.heavy = sbn;
        }

        StatusBarNotification ForListener(ManagedServiceInfo info) {
            if (NotificationManagerService.this.mListeners.getOnNotificationPostedTrim(info) == 1) {
                if (this.sbnCloneLight == null) {
                    this.sbnCloneLight = this.heavy.cloneLight();
                }
                return this.sbnCloneLight;
            }
            if (this.sbnClone == null) {
                this.sbnClone = this.heavy.clone();
            }
            return this.sbnClone;
        }
    }

    private final class WorkerHandler extends Handler {
        /* synthetic */ WorkerHandler(NotificationManagerService this$0, WorkerHandler workerHandler) {
            this();
        }

        private WorkerHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    NotificationManagerService.this.handleTimeout((ToastRecord) msg.obj);
                    return;
                case 3:
                    NotificationManagerService.this.handleSavePolicyFile();
                    return;
                case 4:
                    NotificationManagerService.this.handleSendRankingUpdate();
                    return;
                case 5:
                    NotificationManagerService.this.handleListenerHintsChanged(msg.arg1);
                    return;
                case 6:
                    NotificationManagerService.this.handleListenerInterruptionFilterChanged(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.notification.NotificationManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.notification.NotificationManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.NotificationManagerService.<clinit>():void");
    }

    private void readPolicyXml(InputStream stream, boolean forRestore) throws XmlPullParserException, NumberFormatException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        while (parser.next() != 1) {
            this.mZenModeHelper.readXml(parser, forRestore);
            this.mRankingHelper.readXml(parser, forRestore);
        }
    }

    private void loadPolicyFile() {
        Slog.d(TAG, "loadPolicyFile");
        synchronized (this.mPolicyFile) {
            AutoCloseable autoCloseable = null;
            try {
                autoCloseable = this.mPolicyFile.openRead();
                readPolicyXml(autoCloseable, false);
            } catch (FileNotFoundException e) {
            } catch (IOException e2) {
                Log.wtf(TAG, "Unable to read notification policy", e2);
            } catch (NumberFormatException e3) {
                Log.wtf(TAG, "Unable to parse notification policy", e3);
            } catch (XmlPullParserException e4) {
                Log.wtf(TAG, "Unable to parse notification policy", e4);
            } finally {
                IoUtils.closeQuietly(autoCloseable);
            }
        }
    }

    public void savePolicyFile() {
        this.mHandler.removeMessages(3);
        this.mHandler.sendEmptyMessage(3);
    }

    private void handleSavePolicyFile() {
        Slog.d(TAG, "handleSavePolicyFile");
        synchronized (this.mPolicyFile) {
            try {
                FileOutputStream stream = this.mPolicyFile.startWrite();
                try {
                    writePolicyXml(stream, false);
                    this.mPolicyFile.finishWrite(stream);
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to save policy file, restoring backup", e);
                    this.mPolicyFile.failWrite(stream);
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed to save policy file", e2);
                return;
            }
        }
        BackupManager.dataChanged(getContext().getPackageName());
        return;
    }

    private void writePolicyXml(OutputStream stream, boolean forBackup) throws IOException {
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, Boolean.valueOf(true));
        out.startTag(null, TAG_NOTIFICATION_POLICY);
        out.attribute(null, "version", Integer.toString(1));
        this.mZenModeHelper.writeXml(out, forBackup);
        this.mRankingHelper.writeXml(out, forBackup);
        out.endTag(null, TAG_NOTIFICATION_POLICY);
        out.endDocument();
    }

    private boolean noteNotificationOp(String pkg, int uid) {
        if (this.mAppOps.noteOpNoThrow(11, uid, pkg) == 0) {
            return true;
        }
        Slog.v(TAG, "notifications are disabled by AppOps for " + pkg);
        return false;
    }

    private boolean checkNotificationOp(String pkg, int uid) {
        if (this.mAppOps.checkOp(11, uid, pkg) != 0 || isPackageSuspendedForUser(pkg, uid)) {
            return false;
        }
        return true;
    }

    private void clearSoundLocked() {
        this.mSoundNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
            if (player != null) {
                player.stopAsync();
            }
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException e) {
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private void clearVibrateLocked() {
        this.mVibrateNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            this.mVibrator.cancel();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void clearLightsLocked() {
        this.mLights.clear();
        updateLightsLocked();
    }

    static long[] getLongArray(Resources r, int resid, int maxlen, long[] def) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return def;
        }
        int len = ar.length > maxlen ? maxlen : ar.length;
        long[] out = new long[len];
        for (int i = 0; i < len; i++) {
            out[i] = (long) ar[i];
        }
        return out;
    }

    public NotificationManagerService(Context context) {
        super(context);
        this.mForegroundToken = new Binder();
        this.mRankingThread = new HandlerThread("ranker", 10);
        this.mListenersDisablingEffects = new SparseArray();
        this.mEffectsSuppressors = new ArrayList();
        this.mInterruptionFilter = 0;
        this.mScreenOn = true;
        this.mInCall = false;
        this.mBlacklistNotificationStatisticList = new ArrayList();
        this.mDynamicNotificationStatisticList = new ArrayList();
        this.mNormalNotificationStatisticList = new ArrayList();
        this.mUnNormalNotificationStatisticList = new ArrayList();
        this.mDynamicFilterNotificationList = new ArrayList();
        this.mNotificationFileObserver = null;
        this.mNotificationList = new ArrayList();
        this.mNotificationsByKey = new ArrayMap();
        this.mAutobundledSummaries = new ArrayMap();
        this.mToastQueue = new ArrayList();
        this.mSummaryByGroupKey = new ArrayMap();
        this.mPolicyAccess = new PolicyAccess(this, null);
        this.mNotificationNoClear = new ArrayList();
        this.mLights = new ArrayList();
        this.mUserProfiles = new UserProfiles();
        this.mDmLock = false;
        this.mPplLock = false;
        this.mMaxPackageEnqueueRate = 10.0f;
        this.mIsShutDown = false;
        this.mOppoLock = false;
        this.mNotificationDelegate = new NotificationDelegate() {
            public void onSetDisabled(int status) {
                boolean z = false;
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationManagerService.this.mDisabledNotifications = status;
                    NotificationManagerService notificationManagerService = NotificationManagerService.this;
                    if ((DumpState.DUMP_DOMAIN_PREFERRED & status) != 0) {
                        z = true;
                    }
                    notificationManagerService.mDisableNotificationEffects = z;
                    if (NotificationManagerService.this.disableNotificationEffects(null) != null) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            IRingtonePlayer player = NotificationManagerService.this.mAudioManager.getRingtonePlayer();
                            if (player != null) {
                                player.stopAsync();
                            }
                        } catch (RemoteException e) {
                        } catch (Throwable th) {
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                        identity = Binder.clearCallingIdentity();
                        NotificationManagerService.this.mVibrator.cancel();
                    }
                }
            }

            public void onClearAll(int callingUid, int callingPid, int userId) {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, userId, 3, null, true);
                }
            }

            public void onNotificationClick(int callingUid, int callingPid, String key) {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                    if (r == null) {
                        Log.w(NotificationManagerService.TAG, "No notification with key: " + key);
                        return;
                    }
                    long now = System.currentTimeMillis();
                    EventLogTags.writeNotificationClicked(key, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                    StatusBarNotification sbn = r.sbn;
                    NotificationManagerService.this.cancelNotification(callingUid, callingPid, sbn.getPackageName(), sbn.getTag(), sbn.getId(), 16, 64, false, r.getUserId(), 1, null);
                }
            }

            public void onNotificationActionClick(int callingUid, int callingPid, String key, int actionIndex) {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                    if (r == null) {
                        Log.w(NotificationManagerService.TAG, "No notification with key: " + key);
                        return;
                    }
                    long now = System.currentTimeMillis();
                    EventLogTags.writeNotificationActionClicked(key, actionIndex, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                }
            }

            public void onNotificationClear(int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
                NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 2, null);
            }

            public void onPanelRevealed(boolean clearEffects, int items) {
                EventLogTags.writeNotificationPanelRevealed(items);
                if (clearEffects) {
                    clearEffects();
                }
            }

            public void onPanelHidden() {
                EventLogTags.writeNotificationPanelHidden();
            }

            public void clearEffects() {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    Slog.d(NotificationManagerService.TAG, "clearEffects");
                    NotificationManagerService.this.clearSoundLocked();
                    NotificationManagerService.this.clearVibrateLocked();
                }
            }

            public void onNotificationError(int callingUid, int callingPid, String pkg, String tag, int id, int uid, int initialPid, String message, int userId) {
                Slog.d(NotificationManagerService.TAG, "onNotification error pkg=" + pkg + " tag=" + tag + " id=" + id + "; will crashApplication(uid=" + uid + ", pid=" + initialPid + ")");
                NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 0, false, userId, 4, null);
                long ident = Binder.clearCallingIdentity();
                try {
                    ActivityManagerNative.getDefault().crashApplication(uid, initialPid, pkg, "Bad notification posted from package " + pkg + ": " + message);
                } catch (RemoteException e) {
                }
                Binder.restoreCallingIdentity(ident);
            }

            public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) {
                int i = 0;
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationRecord r;
                    NotificationVisibility nv;
                    for (NotificationVisibility nv2 : newlyVisibleKeys) {
                        r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(nv2.key);
                        if (r != null) {
                            r.setVisibility(true, nv2.rank);
                            nv2.recycle();
                        }
                    }
                    int length = noLongerVisibleKeys.length;
                    while (i < length) {
                        nv2 = noLongerVisibleKeys[i];
                        r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(nv2.key);
                        if (r != null) {
                            r.setVisibility(false, nv2.rank);
                            nv2.recycle();
                        }
                        i++;
                    }
                }
            }

            public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded) {
                int i = 1;
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                    if (r != null) {
                        int i2;
                        r.stats.onExpansionChanged(userAction, expanded);
                        long now = System.currentTimeMillis();
                        if (userAction) {
                            i2 = 1;
                        } else {
                            i2 = 0;
                        }
                        if (!expanded) {
                            i = 0;
                        }
                        EventLogTags.writeNotificationExpansion(key, i2, i, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                    }
                }
            }
        };
        this.mPackageIntentReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Missing block: B:16:0x004d, code:
            if (r12.equals("android.intent.action.PACKAGES_SUSPENDED") == false) goto L_0x019f;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    boolean removingPackage;
                    String[] pkgList;
                    String pkgName;
                    int i;
                    boolean queryRestart = false;
                    boolean queryRemove = false;
                    boolean packageChanged = false;
                    boolean cancelNotifications = true;
                    int reason = 5;
                    if (!action.equals("android.intent.action.PACKAGE_ADDED")) {
                        queryRemove = action.equals("android.intent.action.PACKAGE_REMOVED");
                        if (!(queryRemove || action.equals("android.intent.action.PACKAGE_RESTARTED"))) {
                            packageChanged = action.equals("android.intent.action.PACKAGE_CHANGED");
                            if (!packageChanged) {
                                queryRestart = action.equals("android.intent.action.QUERY_PACKAGE_RESTART");
                                if (!queryRestart) {
                                    if (!action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                                    }
                                }
                            }
                        }
                    }
                    int changeUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (queryRemove) {
                        removingPackage = !intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                    } else {
                        removingPackage = false;
                    }
                    Slog.i(NotificationManagerService.TAG, "action=" + action + " removing=" + removingPackage);
                    if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                        pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                        pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                        reason = 14;
                    } else if (queryRestart) {
                        pkgList = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                    } else {
                        Uri uri = intent.getData();
                        if (uri != null) {
                            pkgName = uri.getSchemeSpecificPart();
                            if (pkgName != null) {
                                if (packageChanged) {
                                    try {
                                        IPackageManager pm = AppGlobals.getPackageManager();
                                        if (changeUserId != -1) {
                                            i = changeUserId;
                                        } else {
                                            i = 0;
                                        }
                                        int enabled = pm.getApplicationEnabledSetting(pkgName, i);
                                        if (enabled == 1 || enabled == 0) {
                                            cancelNotifications = false;
                                        }
                                    } catch (IllegalArgumentException e) {
                                        Slog.i(NotificationManagerService.TAG, "Exception trying to look up app enabled setting", e);
                                    } catch (RemoteException e2) {
                                    }
                                }
                                pkgList = new String[1];
                                pkgList[0] = pkgName;
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                    if (pkgList != null && pkgList.length > 0) {
                        i = 0;
                        int length = pkgList.length;
                        while (true) {
                            int i2 = i;
                            if (i2 >= length) {
                                break;
                            }
                            pkgName = pkgList[i2];
                            if (cancelNotifications) {
                                if (action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkgName, 0, 0, !queryRestart, changeUserId, 20, null);
                                } else {
                                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkgName, 0, 0, !queryRestart, changeUserId, reason, null);
                                }
                            }
                            i = i2 + 1;
                        }
                    }
                    NotificationManagerService.this.mListeners.onPackagesChanged(removingPackage, pkgList);
                    NotificationManagerService.this.mRankerServices.onPackagesChanged(removingPackage, pkgList);
                    NotificationManagerService.this.mConditionProviders.onPackagesChanged(removingPackage, pkgList);
                    NotificationManagerService.this.mRankingHelper.onPackagesChanged(removingPackage, pkgList);
                }
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userHandle;
                int user;
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    NotificationManagerService.this.mScreenOn = true;
                    NotificationManagerService.this.updateNotificationPulse();
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    NotificationManagerService.this.mScreenOn = false;
                    NotificationManagerService.this.updateNotificationPulse();
                } else if (action.equals(OppoPhoneStateReceiver.ACTION_PHONE_STATE_CHANGED)) {
                    NotificationManagerService.this.mInCall = TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra("state"));
                    NotificationManagerService.this.updateNotificationPulse();
                    String state = intent.getStringExtra("state");
                    if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                        NotificationManagerService.this.mCallState = 0;
                    } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                        NotificationManagerService.this.mCallState = 1;
                    } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                        NotificationManagerService.this.mCallState = 2;
                    }
                    if (NotificationManagerService.DEBUG_PANIC) {
                        Slog.d(NotificationManagerService.TAG, "mCallState: " + NotificationManagerService.this.mCallState);
                    }
                } else if (action.equals("android.intent.action.USER_STOPPED")) {
                    userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (userHandle >= 0) {
                        NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, 0, 0, true, userHandle, 6, null);
                    }
                } else if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                    userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (userHandle >= 0) {
                        NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, 0, 0, true, userHandle, 15, null);
                    }
                } else if (action.equals("android.intent.action.USER_PRESENT")) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    if (NotificationManagerService.this.mStatusBar != null) {
                        NotificationManagerService.this.mStatusBar.notificationLightOff();
                    }
                    NotificationManagerService.this.mLights.clear();
                } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                    user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                    NotificationManagerService.this.mSettingsObserver.update(null);
                    NotificationManagerService.this.mUserProfiles.updateCache(context);
                    NotificationManagerService.this.mConditionProviders.onUserSwitched(user);
                    NotificationManagerService.this.mListeners.onUserSwitched(user);
                    NotificationManagerService.this.mRankerServices.onUserSwitched(user);
                    NotificationManagerService.this.mZenModeHelper.onUserSwitched(user);
                } else if (action.equals("android.intent.action.USER_ADDED")) {
                    NotificationManagerService.this.mUserProfiles.updateCache(context);
                } else if (action.equals("android.intent.action.USER_REMOVED")) {
                    NotificationManagerService.this.mZenModeHelper.onUserRemoved(intent.getIntExtra("android.intent.extra.user_handle", -10000));
                } else if (action.equals("android.intent.action.USER_UNLOCKED")) {
                    user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                    NotificationManagerService.this.mConditionProviders.onUserUnlocked(user);
                    NotificationManagerService.this.mListeners.onUserUnlocked(user);
                    NotificationManagerService.this.mRankerServices.onUserUnlocked(user);
                    NotificationManagerService.this.mZenModeHelper.onUserUnlocked(user);
                } else if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                    NotificationManagerService.this.mIsShutDown = true;
                    NotificationManagerService.this.updateNotificationPulse();
                } else if (action.equals(NotificationManagerService.OMADM_LAWMO_LOCK)) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    NotificationManagerService.this.mDmLock = true;
                } else if (action.equals(NotificationManagerService.OMADM_LAWMO_UNLOCK)) {
                    NotificationManagerService.this.mDmLock = false;
                } else if (action.equals(NotificationManagerService.PPL_LOCK)) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    NotificationManagerService.this.mPplLock = true;
                } else if (action.equals(NotificationManagerService.PPL_UNLOCK)) {
                    NotificationManagerService.this.mPplLock = false;
                }
            }
        };
        this.mBuzzBeepBlinked = new Runnable() {
            public void run() {
                if (NotificationManagerService.this.mStatusBar != null) {
                    NotificationManagerService.this.mStatusBar.buzzBeepBlinked();
                }
            }
        };
        this.mService = new INotificationManager.Stub() {
            @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jianhua.Lin@Plf.SDK : Modify for Cancel the current Toast", property = OppoRomType.ROM)
            public void enqueueToast(String pkg, ITransientNotification callback, int duration) {
                Slog.i(NotificationManagerService.TAG, "enqueueToast pkg=" + pkg + " callback=" + callback + " duration=" + duration);
                if (pkg == null || callback == null) {
                    Slog.e(NotificationManagerService.TAG, "Not doing toast. pkg=" + pkg + " callback=" + callback);
                    return;
                }
                boolean isSystemToast = !NotificationManagerService.isCallerSystem() ? "android".equals(pkg) : true;
                boolean isPackageSuspended = NotificationManagerService.this.isPackageSuspendedForUser(pkg, Binder.getCallingUid());
                String pakageName = NotificationManagerService.this.getForegroundPackage();
                if (NotificationManagerService.this.getContext().getPackageManager().isFullFunctionMode() || NotificationManagerService.this.isSystemApp(pkg) || pkg.equals(pakageName)) {
                    synchronized (NotificationManagerService.this.mToastQueue) {
                        int callingPid = Binder.getCallingPid();
                        long callingId = Binder.clearCallingIdentity();
                        try {
                            int index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                            if (index >= 0) {
                                ((ToastRecord) NotificationManagerService.this.mToastQueue.get(index)).update(duration);
                            } else {
                                if (!isSystemToast) {
                                    int count = 0;
                                    int N = NotificationManagerService.this.mToastQueue.size();
                                    for (int i = 0; i < N; i++) {
                                        if (((ToastRecord) NotificationManagerService.this.mToastQueue.get(i)).pkg.equals(pkg)) {
                                            count++;
                                            if (count >= 50) {
                                                Slog.e(NotificationManagerService.TAG, "Package has already posted " + count + " toasts. Not showing more. Package=" + pkg);
                                                Binder.restoreCallingIdentity(callingId);
                                                return;
                                            }
                                        }
                                    }
                                }
                                Binder token = new Binder();
                                NotificationManagerService.this.mWindowManagerInternal.addWindowToken(token, 2005);
                                NotificationManagerService.this.mToastQueue.add(new ToastRecord(callingPid, pkg, callback, duration, token));
                                index = NotificationManagerService.this.mToastQueue.size() - 1;
                                NotificationManagerService.this.keepProcessAliveIfNeededLocked(callingPid);
                            }
                            if (index == 0) {
                                NotificationManagerService.this.showNextToastLocked();
                            }
                            Binder.restoreCallingIdentity(callingId);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(callingId);
                        }
                    }
                } else {
                    Slog.d(NotificationManagerService.TAG, "toast app is not on top, we discard it: " + pkg);
                }
            }

            public void cancelToast(String pkg, ITransientNotification callback) {
                Slog.i(NotificationManagerService.TAG, "cancelToast pkg=" + pkg + " callback=" + callback);
                if (pkg == null || callback == null) {
                    Slog.e(NotificationManagerService.TAG, "Not cancelling notification. pkg=" + pkg + " callback=" + callback);
                    return;
                }
                synchronized (NotificationManagerService.this.mToastQueue) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        int index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                        if (index >= 0) {
                            NotificationManagerService.this.cancelToastLocked(index, false);
                        } else {
                            Slog.w(NotificationManagerService.TAG, "Toast already cancelled. pkg=" + pkg + " callback=" + callback);
                        }
                        Binder.restoreCallingIdentity(callingId);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            }

            public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int[] idOut, int userId) throws RemoteException {
                NotificationManagerService.this.enqueueNotificationInternal(pkg, opPkg, Binder.getCallingUid(), Binder.getCallingPid(), tag, id, notification, idOut, userId);
            }

            public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) {
                NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
                NotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, (Binder.getCallingUid() == 1000 ? 0 : 64) | (Binder.getCallingUid() == 1000 ? 0 : 1024), false, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelNotificationWithTag", pkg), 8, null);
            }

            public void cancelAllNotifications(String pkg, int userId) {
                NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
                NotificationManagerService.this.cancelAllNotificationsInt(Binder.getCallingUid(), Binder.getCallingPid(), pkg, 0, 64, true, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelAllNotifications", pkg), 9, null);
            }

            public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) {
                NotificationManagerService.checkCallerIsSystem();
                NotificationManagerService.this.setNotificationsEnabledForPackageImpl(pkg, uid, enabled);
                NotificationManagerService.this.mRankingHelper.setEnabled(pkg, uid, enabled);
                if (OppoMultiLauncherUtil.getInstance().isMultiApp(pkg)) {
                    try {
                        NotificationManagerService.this.mRankingHelper.setEnabled(pkg, NotificationManagerService.this.getContext().getPackageManager().getPackageUidAsUser(pkg, OppoMultiAppManager.USER_ID), enabled);
                    } catch (NameNotFoundException e) {
                        Log.e(NotificationManagerService.TAG, "setNotificationsEnabledForPackage mRankingHelper.setEnabled NameNotFoundException");
                    }
                }
                NotificationManagerService.this.savePolicyFile();
            }

            public boolean areNotificationsEnabled(String pkg) {
                return areNotificationsEnabledForPackage(pkg, Binder.getCallingUid());
            }

            public boolean areNotificationsEnabledForPackage(String pkg, int uid) {
                NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
                if (NotificationManagerService.this.mAppOps.checkOpNoThrow(11, uid, pkg) != 0 || NotificationManagerService.this.isPackageSuspendedForUser(pkg, uid)) {
                    return false;
                }
                return true;
            }

            public void setPriority(String pkg, int uid, int priority) {
                NotificationManagerService.checkCallerIsSystem();
                NotificationManagerService.this.mRankingHelper.setPriority(pkg, uid, priority);
                if (OppoMultiLauncherUtil.getInstance().isMultiApp(pkg)) {
                    try {
                        NotificationManagerService.this.mRankingHelper.setPriority(pkg, NotificationManagerService.this.getContext().getPackageManager().getPackageUidAsUser(pkg, OppoMultiAppManager.USER_ID), priority);
                    } catch (NameNotFoundException e) {
                        Log.e(NotificationManagerService.TAG, "setNotificationsEnabledForPackage mRankingHelper.setEnabled NameNotFoundException");
                    }
                }
                NotificationManagerService.this.savePolicyFile();
            }

            public int getPriority(String pkg, int uid) {
                NotificationManagerService.checkCallerIsSystem();
                return NotificationManagerService.this.mRankingHelper.getPriority(pkg, uid);
            }

            public void setVisibilityOverride(String pkg, int uid, int visibility) {
                NotificationManagerService.checkCallerIsSystem();
                NotificationManagerService.this.mRankingHelper.setVisibilityOverride(pkg, uid, visibility);
                NotificationManagerService.this.savePolicyFile();
            }

            public int getVisibilityOverride(String pkg, int uid) {
                NotificationManagerService.checkCallerIsSystem();
                return NotificationManagerService.this.mRankingHelper.getVisibilityOverride(pkg, uid);
            }

            public void setImportance(String pkg, int uid, int importance) {
                boolean z = false;
                enforceSystemOrSystemUI("Caller not system or systemui");
                NotificationManagerService notificationManagerService = NotificationManagerService.this;
                if (importance != 0) {
                    z = true;
                }
                notificationManagerService.setNotificationsEnabledForPackageImpl(pkg, uid, z);
                NotificationManagerService.this.mRankingHelper.setImportance(pkg, uid, importance);
                NotificationManagerService.this.savePolicyFile();
            }

            public int getPackageImportance(String pkg) {
                NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
                return NotificationManagerService.this.mRankingHelper.getImportance(pkg, Binder.getCallingUid());
            }

            public int getImportance(String pkg, int uid) {
                enforceSystemOrSystemUI("Caller not system or systemui");
                return NotificationManagerService.this.mRankingHelper.getImportance(pkg, uid);
            }

            public StatusBarNotification[] getActiveNotifications(String callingPkg) {
                NotificationManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.ACCESS_NOTIFICATIONS", "NotificationManagerService.getActiveNotifications");
                StatusBarNotification[] tmp = null;
                if (NotificationManagerService.this.mAppOps.noteOpNoThrow(25, Binder.getCallingUid(), callingPkg) == 0) {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        tmp = new StatusBarNotification[NotificationManagerService.this.mNotificationList.size()];
                        int N = NotificationManagerService.this.mNotificationList.size();
                        for (int i = 0; i < N; i++) {
                            tmp[i] = ((NotificationRecord) NotificationManagerService.this.mNotificationList.get(i)).sbn;
                        }
                    }
                }
                return tmp;
            }

            public ParceledListSlice<StatusBarNotification> getAppActiveNotifications(String pkg, int incomingUserId) {
                NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
                int userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), incomingUserId, true, false, "getAppActiveNotifications", pkg);
                ArrayList<StatusBarNotification> arrayList = new ArrayList(NotificationManagerService.this.mNotificationList.size());
                synchronized (NotificationManagerService.this.mNotificationList) {
                    int N = NotificationManagerService.this.mNotificationList.size();
                    for (int i = 0; i < N; i++) {
                        StatusBarNotification sbn = ((NotificationRecord) NotificationManagerService.this.mNotificationList.get(i)).sbn;
                        if (sbn.getPackageName().equals(pkg) && sbn.getUserId() == userId && (sbn.getNotification().flags & 1024) == 0) {
                            arrayList.add(new StatusBarNotification(sbn.getPackageName(), sbn.getOpPkg(), sbn.getId(), sbn.getTag(), sbn.getUid(), sbn.getInitialPid(), 0, sbn.getNotification().clone(), sbn.getUser(), sbn.getPostTime()));
                        }
                    }
                }
                return new ParceledListSlice(arrayList);
            }

            public StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count) {
                NotificationManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.ACCESS_NOTIFICATIONS", "NotificationManagerService.getHistoricalNotifications");
                StatusBarNotification[] tmp = null;
                if (NotificationManagerService.this.mAppOps.noteOpNoThrow(25, Binder.getCallingUid(), callingPkg) == 0) {
                    synchronized (NotificationManagerService.this.mArchive) {
                        tmp = NotificationManagerService.this.mArchive.getArray(count);
                    }
                }
                return tmp;
            }

            public void registerListener(INotificationListener listener, ComponentName component, int userid) {
                enforceSystemOrSystemUI("INotificationManager.registerListener");
                NotificationManagerService.this.mListeners.registerService(listener, component, userid);
            }

            public void unregisterListener(INotificationListener token, int userid) {
                NotificationManagerService.this.mListeners.unregisterService((IInterface) token, userid);
            }

            public void cancelNotificationsFromListener(INotificationListener token, String[] keys) {
                int callingUid = Binder.getCallingUid();
                int callingPid = Binder.getCallingPid();
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                        if (keys != null) {
                            for (Object obj : keys) {
                                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(obj);
                                if (r != null) {
                                    int userId = r.sbn.getUserId();
                                    if (userId == OppoMultiAppManager.USER_ID || userId == info.userid || userId == -1 || NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId)) {
                                        cancelNotificationFromListenerLocked(info, callingUid, callingPid, r.sbn.getPackageName(), r.sbn.getTag(), r.sbn.getId(), userId);
                                    } else {
                                        throw new SecurityException("Disallowed call from listener: " + info.service);
                                    }
                                }
                            }
                        } else {
                            NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, info.userid, 11, info, info.supportsProfiles());
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public void requestBindListener(ComponentName component) {
                NotificationManagerService.checkCallerIsSystemOrSameApp(component.getPackageName());
                long identity = Binder.clearCallingIdentity();
                try {
                    ManagedServices manager;
                    if (NotificationManagerService.this.mRankerServices.isComponentEnabledForCurrentProfiles(component)) {
                        manager = NotificationManagerService.this.mRankerServices;
                    } else {
                        manager = NotificationManagerService.this.mListeners;
                    }
                    manager.setComponentState(component, true);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public void requestUnbindListener(INotificationListener token) {
                long identity = Binder.clearCallingIdentity();
                try {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    info.getOwner().setComponentState(info.component, false);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public void setNotificationsShownFromListener(INotificationListener token, String[] keys) {
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                        if (keys != null) {
                            int N = keys.length;
                            for (int i = 0; i < N; i++) {
                                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                                if (r != null) {
                                    int userId = r.sbn.getUserId();
                                    if (userId != info.userid && userId != -1 && !NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId)) {
                                        throw new SecurityException("Disallowed call from listener: " + info.service);
                                    } else if (!r.isSeen()) {
                                        Slog.d(NotificationManagerService.TAG, "Marking notification as seen " + keys[i]);
                                        UsageStatsManagerInternal -get4 = NotificationManagerService.this.mAppUsageStats;
                                        String packageName = r.sbn.getPackageName();
                                        if (userId == -1) {
                                            userId = 0;
                                        }
                                        -get4.reportEvent(packageName, userId, 7);
                                        r.setSeen();
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            private void cancelNotificationFromListenerLocked(ManagedServiceInfo info, int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
                NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 10, info);
            }

            public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) {
                int callingUid = Binder.getCallingUid();
                int callingPid = Binder.getCallingPid();
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                        if (info.supportsProfiles()) {
                            Log.e(NotificationManagerService.TAG, "Ignoring deprecated cancelNotification(pkg, tag, id) from " + info.component + " use cancelNotification(key) instead.");
                        } else {
                            cancelNotificationFromListenerLocked(info, callingUid, callingPid, pkg, tag, id, info.userid);
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public ParceledListSlice<StatusBarNotification> getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim) {
                ParceledListSlice<StatusBarNotification> parceledListSlice;
                synchronized (NotificationManagerService.this.mNotificationList) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    boolean getKeys = keys != null;
                    int N = getKeys ? keys.length : NotificationManagerService.this.mNotificationList.size();
                    ArrayList<StatusBarNotification> list = new ArrayList(N);
                    for (int i = 0; i < N; i++) {
                        NotificationRecord r;
                        if (getKeys) {
                            r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                        } else {
                            r = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(i);
                        }
                        if (r != null) {
                            StatusBarNotification sbn = r.sbn;
                            if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                                list.add(trim == 0 ? sbn : sbn.cloneLight());
                            } else {
                                continue;
                            }
                        }
                    }
                    parceledListSlice = new ParceledListSlice(list);
                }
                return parceledListSlice;
            }

            public void requestHintsFromListener(INotificationListener token, int hints) {
                boolean disableEffects = false;
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                        if ((hints & 7) != 0) {
                            disableEffects = true;
                        }
                        if (disableEffects) {
                            NotificationManagerService.this.addDisabledHints(info, hints);
                        } else {
                            NotificationManagerService.this.removeDisabledHints(info, hints);
                        }
                        NotificationManagerService.this.updateListenerHintsLocked();
                        NotificationManagerService.this.updateEffectsSuppressorLocked();
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public int getHintsFromListener(INotificationListener token) {
                int -get13;
                synchronized (NotificationManagerService.this.mNotificationList) {
                    -get13 = NotificationManagerService.this.mListenerHints;
                }
                return -get13;
            }

            public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        NotificationManagerService.this.mZenModeHelper.requestFromListener(NotificationManagerService.this.mListeners.checkServiceTokenLocked(token).component, interruptionFilter);
                        NotificationManagerService.this.updateInterruptionFilterLocked();
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
                int -get12;
                synchronized (NotificationManagerService.this.mNotificationLight) {
                    -get12 = NotificationManagerService.this.mInterruptionFilter;
                }
                return -get12;
            }

            public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (info == null) {
                        return;
                    }
                    NotificationManagerService.this.mListeners.setOnNotificationPostedTrimLocked(info, trim);
                }
            }

            public int getZenMode() {
                return NotificationManagerService.this.mZenModeHelper.getZenMode();
            }

            public ZenModeConfig getZenModeConfig() {
                enforceSystemOrSystemUIOrVolume("INotificationManager.getZenModeConfig");
                return NotificationManagerService.this.mZenModeHelper.getConfig();
            }

            public void setZenMode(int mode, Uri conditionId, String reason) throws RemoteException {
                enforceSystemOrSystemUIOrVolume("INotificationManager.setZenMode");
                long identity = Binder.clearCallingIdentity();
                try {
                    NotificationManagerService.this.mZenModeHelper.setManualZenMode(mode, conditionId, null, reason);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public List<ZenRule> getZenRules() throws RemoteException {
                enforcePolicyAccess(Binder.getCallingUid(), "getAutomaticZenRules");
                return NotificationManagerService.this.mZenModeHelper.getZenRules();
            }

            public AutomaticZenRule getAutomaticZenRule(String id) throws RemoteException {
                Preconditions.checkNotNull(id, "Id is null");
                enforcePolicyAccess(Binder.getCallingUid(), "getAutomaticZenRule");
                return NotificationManagerService.this.mZenModeHelper.getAutomaticZenRule(id);
            }

            public String addAutomaticZenRule(AutomaticZenRule automaticZenRule) throws RemoteException {
                Preconditions.checkNotNull(automaticZenRule, "automaticZenRule is null");
                Preconditions.checkNotNull(automaticZenRule.getName(), "Name is null");
                Preconditions.checkNotNull(automaticZenRule.getOwner(), "Owner is null");
                Preconditions.checkNotNull(automaticZenRule.getConditionId(), "ConditionId is null");
                enforcePolicyAccess(Binder.getCallingUid(), "addAutomaticZenRule");
                return NotificationManagerService.this.mZenModeHelper.addAutomaticZenRule(automaticZenRule, "addAutomaticZenRule");
            }

            public boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule) throws RemoteException {
                Preconditions.checkNotNull(automaticZenRule, "automaticZenRule is null");
                Preconditions.checkNotNull(automaticZenRule.getName(), "Name is null");
                Preconditions.checkNotNull(automaticZenRule.getOwner(), "Owner is null");
                Preconditions.checkNotNull(automaticZenRule.getConditionId(), "ConditionId is null");
                enforcePolicyAccess(Binder.getCallingUid(), "updateAutomaticZenRule");
                return NotificationManagerService.this.mZenModeHelper.updateAutomaticZenRule(id, automaticZenRule, "updateAutomaticZenRule");
            }

            public boolean removeAutomaticZenRule(String id) throws RemoteException {
                Preconditions.checkNotNull(id, "Id is null");
                enforcePolicyAccess(Binder.getCallingUid(), "removeAutomaticZenRule");
                return NotificationManagerService.this.mZenModeHelper.removeAutomaticZenRule(id, "removeAutomaticZenRule");
            }

            public boolean removeAutomaticZenRules(String packageName) throws RemoteException {
                Preconditions.checkNotNull(packageName, "Package name is null");
                enforceSystemOrSystemUI("removeAutomaticZenRules");
                return NotificationManagerService.this.mZenModeHelper.removeAutomaticZenRules(packageName, "removeAutomaticZenRules");
            }

            public int getRuleInstanceCount(ComponentName owner) throws RemoteException {
                Preconditions.checkNotNull(owner, "Owner is null");
                enforceSystemOrSystemUI("getRuleInstanceCount");
                return NotificationManagerService.this.mZenModeHelper.getCurrentInstanceCount(owner);
            }

            public void setInterruptionFilter(String pkg, int filter) throws RemoteException {
                enforcePolicyAccess(pkg, "setInterruptionFilter");
                int zen = NotificationManager.zenModeFromInterruptionFilter(NotificationManagerService.this.getContext(), filter, -1);
                if (zen == -1) {
                    throw new IllegalArgumentException("Invalid filter: " + filter);
                }
                long identity = Binder.clearCallingIdentity();
                try {
                    NotificationManagerService.this.mZenModeHelper.setManualZenMode(zen, null, pkg, "setInterruptionFilter");
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public void notifyConditions(final String pkg, IConditionProvider provider, final Condition[] conditions) {
                final ManagedServiceInfo info = NotificationManagerService.this.mConditionProviders.checkServiceToken(provider);
                NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
                NotificationManagerService.this.mHandler.post(new Runnable() {
                    public void run() {
                        NotificationManagerService.this.mConditionProviders.notifyConditions(pkg, info, conditions);
                    }
                });
            }

            private void enforceSystemOrSystemUIOrVolume(String message) {
                if (NotificationManagerService.this.mAudioManagerInternal != null) {
                    int vcuid = NotificationManagerService.this.mAudioManagerInternal.getVolumeControllerUid();
                    if (vcuid > 0 && Binder.getCallingUid() == vcuid) {
                        return;
                    }
                }
                enforceSystemOrSystemUI(message);
            }

            private void enforceSystemOrSystemUI(String message) {
                if (!NotificationManagerService.isCallerSystem()) {
                    NotificationManagerService.this.getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", message);
                }
            }

            private void enforceSystemOrSystemUIOrSamePackage(String pkg, String message) {
                try {
                    NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
                } catch (SecurityException e) {
                    NotificationManagerService.this.getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", message);
                }
            }

            private void enforcePolicyAccess(int uid, String method) {
                if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") != 0) {
                    boolean accessAllowed = false;
                    for (String checkPolicyAccess : NotificationManagerService.this.getContext().getPackageManager().getPackagesForUid(uid)) {
                        if (checkPolicyAccess(checkPolicyAccess)) {
                            accessAllowed = true;
                        }
                    }
                    if (!accessAllowed) {
                        Slog.w(NotificationManagerService.TAG, "Notification policy access denied calling " + method);
                        throw new SecurityException("Notification policy access denied");
                    }
                }
            }

            private void enforcePolicyAccess(String pkg, String method) {
                if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") != 0) {
                    NotificationManagerService.checkCallerIsSameApp(pkg);
                    if (!checkPolicyAccess(pkg)) {
                        Slog.w(NotificationManagerService.TAG, "Notification policy access denied calling " + method);
                        throw new SecurityException("Notification policy access denied");
                    }
                }
            }

            private boolean checkPackagePolicyAccess(String pkg) {
                return NotificationManagerService.this.mPolicyAccess.isPackageGranted(pkg);
            }

            private boolean checkPolicyAccess(String pkg) {
                boolean z = true;
                try {
                    if (ActivityManager.checkComponentPermission("android.permission.MANAGE_NOTIFICATIONS", NotificationManagerService.this.getContext().getPackageManager().getPackageUidAsUser(pkg, UserHandle.getCallingUserId()), -1, true) == 0) {
                        return true;
                    }
                    if (!checkPackagePolicyAccess(pkg)) {
                        z = NotificationManagerService.this.mListeners.isComponentEnabledForPackage(pkg);
                    }
                    return z;
                } catch (NameNotFoundException e) {
                    return false;
                }
            }

            protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (NotificationManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    pw.println("Permission Denial: can't dump NotificationManager from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                } else if (!NotificationManagerService.this.dumpNoClearNotification(pw, args)) {
                    DumpFilter filter = DumpFilter.parseFromArguments(args);
                    if (filter == null || !filter.stats) {
                        NotificationManagerService.this.dumpImpl(pw, filter);
                    } else {
                        NotificationManagerService.this.dumpJson(pw, filter);
                    }
                }
            }

            public void removeAllNotifications(String pkg, int userId) {
                NotificationManagerService.checkCallerIsSystem();
                Slog.v(NotificationManagerService.TAG, " removeAllNotifications, for " + pkg);
                synchronized (NotificationManagerService.this.mNotificationList) {
                    int N = NotificationManagerService.this.mNotificationList.size();
                    for (int i = 0; i < N; i++) {
                        NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(i);
                        if (r.sbn.getPackageName().equals(pkg)) {
                            NotificationManagerService.this.cancelNotificationLocked(r, false, 2);
                        }
                    }
                }
            }

            public ComponentName getEffectsSuppressor() {
                enforceSystemOrSystemUIOrVolume("INotificationManager.getEffectsSuppressor");
                return !NotificationManagerService.this.mEffectsSuppressors.isEmpty() ? (ComponentName) NotificationManagerService.this.mEffectsSuppressors.get(0) : null;
            }

            public boolean matchesCallFilter(Bundle extras) {
                enforceSystemOrSystemUI("INotificationManager.matchesCallFilter");
                return NotificationManagerService.this.mZenModeHelper.matchesCallFilter(Binder.getCallingUserHandle(), extras, (ValidateNotificationPeople) NotificationManagerService.this.mRankingHelper.findExtractor(ValidateNotificationPeople.class), 3000, NotificationManagerService.MATCHES_CALL_FILTER_TIMEOUT_AFFINITY);
            }

            public boolean isSystemConditionProviderEnabled(String path) {
                enforceSystemOrSystemUIOrVolume("INotificationManager.isSystemConditionProviderEnabled");
                return NotificationManagerService.this.mConditionProviders.isSystemProviderEnabled(path);
            }

            public byte[] getBackupPayload(int user) {
                Slog.d(NotificationManagerService.TAG, "getBackupPayload u=" + user);
                if (user != 0) {
                    Slog.w(NotificationManagerService.TAG, "getBackupPayload: cannot backup policy for user " + user);
                    return null;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    NotificationManagerService.this.writePolicyXml(baos, true);
                    return baos.toByteArray();
                } catch (IOException e) {
                    Slog.w(NotificationManagerService.TAG, "getBackupPayload: error writing payload for user " + user, e);
                    return null;
                }
            }

            /* JADX WARNING: Removed duplicated region for block: B:14:0x007a A:{Splitter: B:11:0x006e, ExcHandler: java.lang.NumberFormatException (r1_0 'e' java.lang.Exception)} */
            /* JADX WARNING: Removed duplicated region for block: B:14:0x007a A:{Splitter: B:11:0x006e, ExcHandler: java.lang.NumberFormatException (r1_0 'e' java.lang.Exception)} */
            /* JADX WARNING: Missing block: B:14:0x007a, code:
            r1 = move-exception;
     */
            /* JADX WARNING: Missing block: B:15:0x007b, code:
            android.util.Slog.w(com.android.server.notification.NotificationManagerService.TAG, "applyRestore: error reading payload", r1);
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void applyRestore(byte[] payload, int user) {
                String str = null;
                String str2 = NotificationManagerService.TAG;
                StringBuilder append = new StringBuilder().append("applyRestore u=").append(user).append(" payload=");
                if (payload != null) {
                    str = new String(payload, StandardCharsets.UTF_8);
                }
                Slog.d(str2, append.append(str).toString());
                if (payload == null) {
                    Slog.w(NotificationManagerService.TAG, "applyRestore: no payload to restore for user " + user);
                } else if (user != 0) {
                    Slog.w(NotificationManagerService.TAG, "applyRestore: cannot restore policy for user " + user);
                } else {
                    try {
                        NotificationManagerService.this.readPolicyXml(new ByteArrayInputStream(payload), true);
                        NotificationManagerService.this.savePolicyFile();
                    } catch (Exception e) {
                    }
                }
            }

            public boolean isNotificationPolicyAccessGranted(String pkg) {
                return checkPolicyAccess(pkg);
            }

            public boolean isNotificationPolicyAccessGrantedForPackage(String pkg) {
                enforceSystemOrSystemUIOrSamePackage(pkg, "request policy access status for another package");
                return checkPolicyAccess(pkg);
            }

            public String[] getPackagesRequestingNotificationPolicyAccess() throws RemoteException {
                enforceSystemOrSystemUI("request policy access packages");
                long identity = Binder.clearCallingIdentity();
                try {
                    String[] requestingPackages = NotificationManagerService.this.mPolicyAccess.getRequestingPackages();
                    return requestingPackages;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
                enforceSystemOrSystemUI("grant notification policy access");
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        NotificationManagerService.this.mPolicyAccess.put(pkg, granted);
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public Policy getNotificationPolicy(String pkg) {
                enforcePolicyAccess(pkg, "getNotificationPolicy");
                long identity = Binder.clearCallingIdentity();
                try {
                    Policy notificationPolicy = NotificationManagerService.this.mZenModeHelper.getNotificationPolicy();
                    return notificationPolicy;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public void setNotificationPolicy(String pkg, Policy policy) {
                enforcePolicyAccess(pkg, "setNotificationPolicy");
                long identity = Binder.clearCallingIdentity();
                try {
                    NotificationManagerService.this.mZenModeHelper.setNotificationPolicy(policy);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public boolean shouldInterceptSound(String pkg, int uid) {
                boolean bIsInImportantInterruptions = NotificationManagerService.this.mZenModeHelper.isInImportantInterruptions();
                boolean bAllowReminders = NotificationManagerService.this.mZenModeHelper.getConfig().allowReminders;
                boolean bIsPriorityInterruption = 2 == NotificationManagerService.this.mRankingHelper.getPriority(pkg, uid);
                if (NotificationManagerService.DEBUG_PANIC) {
                    Log.e(NotificationManagerService.TAG, "bIsInImportantInterruptions:" + bIsInImportantInterruptions + ", bIsPriorityInterruption : " + bIsPriorityInterruption + ", bAllowReminders : " + bAllowReminders);
                }
                if ((!bIsInImportantInterruptions || bIsPriorityInterruption || bAllowReminders) && NotificationManagerService.this.noteNotificationOp(pkg, uid) && NotificationManagerService.this.noteNotificationSoundOp(pkg, uid) && !NotificationManagerService.this.suppressedByGameSpace(pkg)) {
                    return false;
                }
                return true;
            }

            public void applyAdjustmentFromRankerService(INotificationListener token, Adjustment adjustment) throws RemoteException {
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        NotificationManagerService.this.mRankerServices.checkServiceTokenLocked(token);
                        NotificationManagerService.this.applyAdjustmentLocked(adjustment);
                    }
                    NotificationManagerService.this.maybeAddAutobundleSummary(adjustment);
                    NotificationManagerService.this.mRankingHandler.requestSort();
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public void applyAdjustmentsFromRankerService(INotificationListener token, List<Adjustment> adjustments) throws RemoteException {
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (NotificationManagerService.this.mNotificationList) {
                        NotificationManagerService.this.mRankerServices.checkServiceTokenLocked(token);
                        for (Adjustment adjustment : adjustments) {
                            NotificationManagerService.this.applyAdjustmentLocked(adjustment);
                        }
                    }
                    for (Adjustment adjustment2 : adjustments) {
                        NotificationManagerService.this.maybeAddAutobundleSummary(adjustment2);
                    }
                    NotificationManagerService.this.mRankingHandler.requestSort();
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        };
        this.mInternalService = new NotificationManagerInternal() {
            public void enqueueNotification(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int[] idReceived, int userId) {
                NotificationManagerService.this.enqueueNotificationInternal(pkg, opPkg, callingUid, callingPid, tag, id, notification, idReceived, userId);
            }

            public void removeForegroundServiceFlagFromNotification(String pkg, int notificationId, int userId) {
                NotificationManagerService.checkCallerIsSystem();
                synchronized (NotificationManagerService.this.mNotificationList) {
                    int i = NotificationManagerService.this.indexOfNotificationLocked(pkg, null, notificationId, userId);
                    if (i < 0) {
                        Log.d(NotificationManagerService.TAG, "stripForegroundServiceFlag: Could not find notification with pkg=" + pkg + " / id=" + notificationId + " / userId=" + userId);
                        return;
                    }
                    NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(i);
                    StatusBarNotification sbn = r.sbn;
                    sbn.getNotification().flags = r.mOriginalFlags & -65;
                    NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                    NotificationManagerService.this.mListeners.notifyPostedLocked(sbn, sbn);
                }
            }

            @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoRomType.ROM)
            public boolean checkProcessToast(int pid) {
                synchronized (NotificationManagerService.this.mToastQueue) {
                    ArrayList<ToastRecord> list = NotificationManagerService.this.mToastQueue;
                    int len = list.size();
                    for (int i = 0; i < len; i++) {
                        if (((ToastRecord) list.get(i)).pid == pid) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            @OppoHook(level = OppoHookType.NEW_METHOD, note = "baoqibiao@ROM.SysApp, add for don't clear notification when bpm suspend", property = OppoRomType.ROM)
            public void cancelAllNotificationsFromBMP(String pkg, int userId) {
                NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
                NotificationManagerService.this.cancelAllNotificationsInt(Binder.getCallingUid(), Binder.getCallingPid(), pkg, 0, 0, true, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelAllNotifications", pkg), 21, null);
            }
        };
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (NotificationManagerService.this.mCallState != state) {
                    if (NotificationManagerService.DEBUG_PANIC) {
                        Slog.d(NotificationManagerService.TAG, "Call state changed: " + NotificationManagerService.callStateToString(state));
                    }
                    NotificationManagerService.this.mCallState = state;
                }
            }
        };
    }

    void setAudioManager(AudioManager audioMananger) {
        this.mAudioManager = audioMananger;
    }

    void setVibrator(Vibrator vibrator) {
        this.mVibrator = vibrator;
    }

    void setSystemReady(boolean systemReady) {
        this.mSystemReady = systemReady;
    }

    void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    void setSystemNotificationSound(String systemNotificationSound) {
        this.mSystemNotificationSound = systemNotificationSound;
    }

    public void onStart() {
        String[] extractorNames;
        Resources resources = getContext().getResources();
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mMaxPackageEnqueueRate = Global.getFloat(getContext().getContentResolver(), "max_notification_enqueue_rate", 10.0f);
        this.mAm = ActivityManagerNative.getDefault();
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        this.mAppUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mRankerServicePackageName = getContext().getPackageManager().getServicesSystemSharedLibraryPackageName();
        this.mHandler = new WorkerHandler(this, null);
        this.mRankingThread.start();
        try {
            extractorNames = resources.getStringArray(17236028);
        } catch (NotFoundException e) {
            extractorNames = new String[0];
        }
        this.mUsageStats = new NotificationUsageStats(getContext());
        this.mRankingHandler = new RankingHandlerWorker(this.mRankingThread.getLooper());
        this.mRankingHelper = new RankingHelper(getContext(), this.mRankingHandler, this.mUsageStats, extractorNames);
        this.mConditionProviders = new ConditionProviders(getContext(), this.mHandler, this.mUserProfiles);
        this.mZenModeHelper = new ZenModeHelper(getContext(), this.mHandler.getLooper(), this.mConditionProviders);
        this.mZenModeHelper.addCallback(new Callback() {
            public void onConfigChanged() {
                NotificationManagerService.this.savePolicyFile();
            }

            void onZenModeChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.INTERRUPTION_FILTER_CHANGED");
                NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL").addFlags(67108864), UserHandle.ALL, "android.permission.MANAGE_NOTIFICATIONS");
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
            }

            void onPolicyChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.NOTIFICATION_POLICY_CHANGED");
            }
        });
        this.mPolicyFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "notification_policy.xml"));
        syncBlockDb();
        this.mListeners = new NotificationListeners();
        this.mRankerServices = new NotificationRankers();
        this.mRankerServices.registerRanker();
        this.mStatusBar = (StatusBarManagerInternal) -wrap1(StatusBarManagerInternal.class);
        if (this.mStatusBar != null) {
            this.mStatusBar.setNotificationDelegate(this.mNotificationDelegate);
        }
        LightsManager lights = (LightsManager) -wrap1(LightsManager.class);
        this.mNotificationLight = lights.getLight(4);
        this.mAttentionLight = lights.getLight(5);
        this.mDefaultNotificationColor = resources.getColor(17170696);
        this.mDefaultNotificationLedOn = resources.getInteger(17694808);
        this.mDefaultNotificationLedOff = resources.getInteger(17694809);
        this.mDefaultVibrationPattern = getLongArray(resources, 17236024, 17, DEFAULT_VIBRATE_PATTERN);
        this.mFallbackVibrationPattern = getLongArray(resources, 17236025, 17, DEFAULT_VIBRATE_PATTERN);
        this.mUseAttentionLight = resources.getBoolean(17956901);
        if (Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) == 0) {
            this.mDisableNotificationEffects = true;
        }
        this.mZenModeHelper.initZenMode();
        this.mInterruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        this.mUserProfiles.updateCache(getContext());
        listenForCallState();
        try {
            IBinder binder = ServiceManager.getService("DmAgent");
            if (binder != null) {
                this.mDmLock = DmAgent.Stub.asInterface(binder).isLockFlagSet();
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "failed to get DM status!");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(OppoPhoneStateReceiver.ACTION_PHONE_STATE_CHANGED);
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.intent.action.USER_STOPPED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        filter.addAction(OMADM_LAWMO_LOCK);
        filter.addAction(OMADM_LAWMO_UNLOCK);
        filter.addAction(PPL_LOCK);
        filter.addAction(PPL_UNLOCK);
        getContext().registerReceiver(this.mIntentReceiver, filter);
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction("android.intent.action.PACKAGE_ADDED");
        pkgFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        pkgFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        pkgFilter.addAction("android.intent.action.PACKAGE_RESTARTED");
        pkgFilter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
        pkgFilter.addDataScheme("package");
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, pkgFilter, null, null);
        IntentFilter suspendedPkgFilter = new IntentFilter();
        suspendedPkgFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, suspendedPkgFilter, null, null);
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE"), null, null);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mOppoSettingsObserver = new OppoSettingsObserver(this.mHandler);
        this.mArchive = new Archive(resources.getInteger(17694815));
        publishBinderService(NOTIFICATON_TITLE_NAME, this.mService);
        publishLocalService(NotificationManagerInternal.class, this.mInternalService);
        initDir();
        this.mNotificationFileObserver = new FileObserverPolicy(OPPO_NOTIFICATION_BLACKLIST_FILE_PATH);
        this.mNotificationFileObserver.startWatching();
        if (DEBUG_PANIC) {
            Log.d(TAG, "initFileObserver_startWatching");
        }
        readConfigFile();
    }

    private void sendRegisteredOnlyBroadcast(String action) {
        getContext().sendBroadcastAsUser(new Intent(action).addFlags(1073741824), UserHandle.ALL, null);
    }

    private void syncBlockDb() {
        loadPolicyFile();
        Map<Integer, String> packageBans = this.mRankingHelper.getPackageBans();
        for (Entry<Integer, String> ban : packageBans.entrySet()) {
            setNotificationsEnabledForPackageImpl((String) ban.getValue(), ((Integer) ban.getKey()).intValue(), false);
        }
        packageBans.clear();
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            PackageManager packageManager = getContext().getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackagesAsUser(0, userId);
            int packageCount = packages.size();
            for (int p = 0; p < packageCount; p++) {
                String packageName = ((PackageInfo) packages.get(p)).packageName;
                try {
                    int uid = packageManager.getPackageUidAsUser(packageName, userId);
                    if (!checkNotificationOp(packageName, uid)) {
                        packageBans.put(Integer.valueOf(uid), packageName);
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        for (Entry<Integer, String> ban2 : packageBans.entrySet()) {
            this.mRankingHelper.setImportance((String) ban2.getValue(), ((Integer) ban2.getKey()).intValue(), 0);
        }
        savePolicyFile();
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mSystemReady = true;
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            this.mAudioManagerInternal = (AudioManagerInternal) -wrap1(AudioManagerInternal.class);
            this.mVrManagerInternal = (VrManagerInternal) -wrap1(VrManagerInternal.class);
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mZenModeHelper.onSystemReady();
        } else if (phase == 600) {
            this.mSettingsObserver.observe();
            this.mOppoSettingsObserver.observe();
            this.mListeners.onBootPhaseAppsCanStart();
            this.mRankerServices.onBootPhaseAppsCanStart();
            this.mConditionProviders.onBootPhaseAppsCanStart();
        }
    }

    void setNotificationsEnabledForPackageImpl(String pkg, int uid, boolean enabled) {
        int i;
        Slog.v(TAG, (enabled ? "en" : "dis") + "abling notifications for " + pkg);
        AppOpsManager appOpsManager = this.mAppOps;
        if (enabled) {
            i = 0;
        } else {
            i = 1;
        }
        appOpsManager.setMode(11, uid, pkg, i);
        if (!enabled) {
            cancelAllNotificationsInt(MY_UID, MY_PID, pkg, 0, 0, true, UserHandle.getUserId(uid), 7, null);
            if (OppoMultiLauncherUtil.getInstance().isMultiApp(pkg)) {
                cancelAllNotificationsInt(MY_UID, MY_PID, pkg, 0, 0, true, OppoMultiAppManager.USER_ID, 7, null);
            }
        }
    }

    private void updateListenerHintsLocked() {
        int hints = calculateHints();
        if (hints != this.mListenerHints) {
            ZenLog.traceListenerHintsChanged(this.mListenerHints, hints, this.mEffectsSuppressors.size());
            this.mListenerHints = hints;
            scheduleListenerHintsChanged(hints);
        }
    }

    private void updateEffectsSuppressorLocked() {
        long updatedSuppressedEffects = calculateSuppressedEffects();
        if (updatedSuppressedEffects != this.mZenModeHelper.getSuppressedEffects()) {
            List<ComponentName> suppressors = getSuppressors();
            ZenLog.traceEffectsSuppressorChanged(this.mEffectsSuppressors, suppressors, updatedSuppressedEffects);
            this.mEffectsSuppressors = suppressors;
            this.mZenModeHelper.setSuppressedEffects(updatedSuppressedEffects);
            sendRegisteredOnlyBroadcast("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
        }
    }

    private ArrayList<ComponentName> getSuppressors() {
        ArrayList<ComponentName> names = new ArrayList();
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            for (ManagedServiceInfo info : (ArraySet) this.mListenersDisablingEffects.valueAt(i)) {
                names.add(info.component);
            }
        }
        return names;
    }

    private boolean removeDisabledHints(ManagedServiceInfo info) {
        return removeDisabledHints(info, 0);
    }

    private boolean removeDisabledHints(ManagedServiceInfo info, int hints) {
        boolean removed = false;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            ArraySet<ManagedServiceInfo> listeners = (ArraySet) this.mListenersDisablingEffects.valueAt(i);
            if (hints == 0 || (hint & hints) == hint) {
                if (removed) {
                    removed = true;
                } else {
                    removed = listeners.remove(info);
                }
            }
        }
        return removed;
    }

    private void addDisabledHints(ManagedServiceInfo info, int hints) {
        if ((hints & 1) != 0) {
            addDisabledHint(info, 1);
        }
        if ((hints & 2) != 0) {
            addDisabledHint(info, 2);
        }
        if ((hints & 4) != 0) {
            addDisabledHint(info, 4);
        }
    }

    private void addDisabledHint(ManagedServiceInfo info, int hint) {
        if (this.mListenersDisablingEffects.indexOfKey(hint) < 0) {
            this.mListenersDisablingEffects.put(hint, new ArraySet());
        }
        ((ArraySet) this.mListenersDisablingEffects.get(hint)).add(info);
    }

    private int calculateHints() {
        int hints = 0;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            if (!((ArraySet) this.mListenersDisablingEffects.valueAt(i)).isEmpty()) {
                hints |= hint;
            }
        }
        return hints;
    }

    private long calculateSuppressedEffects() {
        int hints = calculateHints();
        long suppressedEffects = 0;
        if ((hints & 1) != 0) {
            suppressedEffects = 3;
        }
        if ((hints & 2) != 0) {
            suppressedEffects |= 1;
        }
        if ((hints & 4) != 0) {
            return suppressedEffects | 2;
        }
        return suppressedEffects;
    }

    private void updateInterruptionFilterLocked() {
        int interruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        if (interruptionFilter != this.mInterruptionFilter) {
            this.mInterruptionFilter = interruptionFilter;
            scheduleInterruptionFilterChanged(interruptionFilter);
        }
    }

    private String getForegroundPackage() {
        ComponentName cn;
        try {
            cn = new OppoActivityManager().getTopActivityComponentName();
        } catch (Exception e) {
            Log.w(TAG, "getTopActivityComponentName exception");
            cn = null;
        }
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    private void applyAdjustmentLocked(Adjustment adjustment) {
        maybeClearAutobundleSummaryLocked(adjustment);
        NotificationRecord n = (NotificationRecord) this.mNotificationsByKey.get(adjustment.getKey());
        if (n != null) {
            if (adjustment.getImportance() != 0) {
                n.setImportance(adjustment.getImportance(), adjustment.getExplanation());
            }
            if (adjustment.getSignals() != null) {
                Bundle.setDefusable(adjustment.getSignals(), true);
                String autoGroupKey = adjustment.getSignals().getString("group_key_override", null);
                if (autoGroupKey == null) {
                    EventLogTags.writeNotificationUnautogrouped(adjustment.getKey());
                } else {
                    EventLogTags.writeNotificationAutogrouped(adjustment.getKey());
                }
                n.sbn.setOverrideGroupKey(autoGroupKey);
            }
        }
    }

    private void maybeClearAutobundleSummaryLocked(Adjustment adjustment) {
        if (adjustment.getSignals() != null) {
            Bundle.setDefusable(adjustment.getSignals(), true);
            if (adjustment.getSignals().containsKey("autogroup_needed") && !adjustment.getSignals().getBoolean("autogroup_needed", false)) {
                ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(adjustment.getUser()));
                if (summaries != null && summaries.containsKey(adjustment.getPackage())) {
                    NotificationRecord removed = (NotificationRecord) this.mNotificationsByKey.get(summaries.remove(adjustment.getPackage()));
                    if (removed != null) {
                        this.mNotificationList.remove(removed);
                        cancelNotificationLocked(removed, false, 16);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:26:0x014a, code:
            if (r25 == null) goto L_0x015e;
     */
    /* JADX WARNING: Missing block: B:27:0x014c, code:
            r29.mHandler.post(new com.android.server.notification.NotificationManagerService.EnqueueNotificationRunnable(r29, r27, r25));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void maybeAddAutobundleSummary(Adjustment adjustment) {
        Throwable th;
        if (adjustment.getSignals() != null) {
            Bundle.setDefusable(adjustment.getSignals(), true);
            if (adjustment.getSignals().getBoolean("autogroup_needed", false)) {
                String newAutoBundleKey = adjustment.getSignals().getString("group_key_override", null);
                NotificationRecord notificationRecord = null;
                synchronized (this.mNotificationList) {
                    try {
                        NotificationRecord notificationRecord2 = (NotificationRecord) this.mNotificationsByKey.get(adjustment.getKey());
                        if (notificationRecord2 == null) {
                            return;
                        }
                        StatusBarNotification adjustedSbn = notificationRecord2.sbn;
                        int userId = adjustedSbn.getUser().getIdentifier();
                        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(userId));
                        if (summaries == null) {
                            summaries = new ArrayMap();
                        }
                        this.mAutobundledSummaries.put(Integer.valueOf(userId), summaries);
                        if (!(summaries.containsKey(adjustment.getPackage()) || newAutoBundleKey == null)) {
                            ApplicationInfo appInfo = (ApplicationInfo) adjustedSbn.getNotification().extras.getParcelable("android.appInfo");
                            Bundle extras = new Bundle();
                            extras.putParcelable("android.appInfo", appInfo);
                            Notification summaryNotification = new Builder(getContext()).setSmallIcon(adjustedSbn.getNotification().getSmallIcon()).setGroupSummary(true).setGroup(newAutoBundleKey).setFlag(1024, true).setFlag(512, true).setColor(adjustedSbn.getNotification().color).setLocalOnly(true).build();
                            summaryNotification.extras.putAll(extras);
                            Intent appIntent = getContext().getPackageManager().getLaunchIntentForPackage(adjustment.getPackage());
                            if (appIntent != null) {
                                summaryNotification.contentIntent = PendingIntent.getActivityAsUser(getContext(), 0, appIntent, 0, null, UserHandle.of(userId));
                            }
                            StatusBarNotification summarySbn = new StatusBarNotification(adjustedSbn.getPackageName(), adjustedSbn.getOpPkg(), Integer.MAX_VALUE, "group_key_override", adjustedSbn.getUid(), adjustedSbn.getInitialPid(), summaryNotification, adjustedSbn.getUser(), newAutoBundleKey, System.currentTimeMillis());
                            NotificationRecord notificationRecord3 = new NotificationRecord(getContext(), summarySbn);
                            try {
                                summaries.put(adjustment.getPackage(), summarySbn.getKey());
                                notificationRecord = notificationRecord3;
                            } catch (Throwable th2) {
                                th = th2;
                                notificationRecord = notificationRecord3;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
        }
    }

    private String disableNotificationEffects(NotificationRecord record) {
        if (this.mDisableNotificationEffects) {
            return "booleanState";
        }
        if ((this.mListenerHints & 1) != 0) {
            return "listenerHints";
        }
        if (this.mCallState == 0 || this.mZenModeHelper.isCall(record)) {
            return null;
        }
        return "callState";
    }

    private void dumpJson(PrintWriter pw, DumpFilter filter) {
        JSONObject dump = new JSONObject();
        try {
            dump.put(OppoProcessManager.RESUME_REASON_SERVICE_STR, "Notification Manager");
            dump.put("bans", this.mRankingHelper.dumpBansJson(filter));
            dump.put("ranking", this.mRankingHelper.dumpJson(filter));
            dump.put("stats", this.mUsageStats.dumpJson(filter));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pw.println(dump);
    }

    void dumpImpl(PrintWriter pw, DumpFilter filter) {
        int N;
        int i;
        pw.print("Current Notification Manager state");
        if (filter.filtered) {
            pw.print(" (filtered to ");
            pw.print(filter);
            pw.print(")");
        }
        pw.println(':');
        boolean zenOnly = filter.filtered ? filter.zen : false;
        if (!zenOnly) {
            synchronized (this.mToastQueue) {
                N = this.mToastQueue.size();
                if (N > 0) {
                    pw.println("  Toast Queue:");
                    for (i = 0; i < N; i++) {
                        ((ToastRecord) this.mToastQueue.get(i)).dump(pw, "    ", filter);
                    }
                    pw.println("  ");
                }
            }
        }
        synchronized (this.mNotificationList) {
            if (!zenOnly) {
                N = this.mNotificationList.size();
                if (N > 0) {
                    pw.println("  Notification List:");
                    for (i = 0; i < N; i++) {
                        NotificationRecord nr = (NotificationRecord) this.mNotificationList.get(i);
                        if (filter.filtered) {
                            if (!filter.matches(nr.sbn)) {
                            }
                        }
                        nr.dump(pw, "    ", getContext(), filter.redact);
                    }
                    pw.println("  ");
                }
                if (!filter.filtered) {
                    N = this.mLights.size();
                    if (N > 0) {
                        pw.println("  Lights List:");
                        for (i = 0; i < N; i++) {
                            if (i == N - 1) {
                                pw.print("  > ");
                            } else {
                                pw.print("    ");
                            }
                            pw.println((String) this.mLights.get(i));
                        }
                        pw.println("  ");
                    }
                    pw.println("  mUseAttentionLight=" + this.mUseAttentionLight);
                    pw.println("  mNotificationPulseEnabled=" + this.mNotificationPulseEnabled);
                    pw.println("  mSoundNotificationKey=" + this.mSoundNotificationKey);
                    pw.println("  mVibrateNotificationKey=" + this.mVibrateNotificationKey);
                    pw.println("  mDisableNotificationEffects=" + this.mDisableNotificationEffects);
                    pw.println("  mCallState=" + callStateToString(this.mCallState));
                    pw.println("  mSystemReady=" + this.mSystemReady);
                    pw.println("  mMaxPackageEnqueueRate=" + this.mMaxPackageEnqueueRate);
                }
                pw.println("  mArchive=" + this.mArchive.toString());
                Iterator<StatusBarNotification> iter = this.mArchive.descendingIterator();
                i = 0;
                while (iter.hasNext()) {
                    StatusBarNotification sbn = (StatusBarNotification) iter.next();
                    if (filter == null || filter.matches(sbn)) {
                        pw.println("    " + sbn);
                        i++;
                        if (i >= 5) {
                            if (iter.hasNext()) {
                                pw.println("    ...");
                            }
                        }
                    }
                }
            }
            if (!zenOnly) {
                pw.println("\n  Usage Stats:");
                this.mUsageStats.dump(pw, "    ", filter);
            }
            if (!filter.filtered || zenOnly) {
                pw.println("\n  Zen Mode:");
                pw.print("    mInterruptionFilter=");
                pw.println(this.mInterruptionFilter);
                this.mZenModeHelper.dump(pw, "    ");
                pw.println("\n  Zen Log:");
                ZenLog.dump(pw, "    ");
            }
            if (!zenOnly) {
                pw.println("\n  Ranking Config:");
                this.mRankingHelper.dump(pw, "    ", filter);
                pw.println("\n  Notification listeners:");
                this.mListeners.dump(pw, filter);
                pw.print("    mListenerHints: ");
                pw.println(this.mListenerHints);
                pw.print("    mListenersDisablingEffects: (");
                N = this.mListenersDisablingEffects.size();
                for (i = 0; i < N; i++) {
                    int hint = this.mListenersDisablingEffects.keyAt(i);
                    if (i > 0) {
                        pw.print(';');
                    }
                    pw.print("hint[" + hint + "]:");
                    ArraySet<ManagedServiceInfo> listeners = (ArraySet) this.mListenersDisablingEffects.valueAt(i);
                    int listenerSize = listeners.size();
                    for (int j = 0; j < listenerSize; j++) {
                        if (i > 0) {
                            pw.print(',');
                        }
                        pw.print(((ManagedServiceInfo) listeners.valueAt(i)).component);
                    }
                }
                pw.println(')');
                pw.println("\n  mRankerServicePackageName: " + this.mRankerServicePackageName);
                pw.println("\n  Notification ranker services:");
                this.mRankerServices.dump(pw, filter);
            }
            pw.println("\n  Policy access:");
            pw.print("    mPolicyAccess: ");
            pw.println(this.mPolicyAccess);
            pw.println("\n  Condition providers:");
            this.mConditionProviders.dump(pw, filter);
            pw.println("\n  Group summaries:");
            for (Entry<String, NotificationRecord> entry : this.mSummaryByGroupKey.entrySet()) {
                NotificationRecord r = (NotificationRecord) entry.getValue();
                pw.println("    " + ((String) entry.getKey()) + " -> " + r.getKey());
                if (this.mNotificationsByKey.get(r.getKey()) != r) {
                    pw.println("!!!!!!LEAK: Record not found in mNotificationsByKey.");
                    r.dump(pw, "      ", getContext(), filter.redact);
                }
            }
            pw.println("\n  mNotificationNoClear:");
            synchronized (this.mNotificationNoClear) {
                for (String pkg : this.mNotificationNoClear) {
                    pw.println("    NoClearNotification:" + pkg);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:101:0x0369, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void enqueueNotificationInternal(String oriPkg, String oriOpPkg, int oriCallingUid, int callingPid, String tag, int id, Notification notification, int[] idOut, int incomingUserId) {
        String pkg;
        String opPkg;
        int callingUid;
        Slog.v(TAG, "enqueueNotificationInternal: oriPkg=" + oriPkg + ",oriOpPkg:" + oriOpPkg + ",id=" + id + " notification=" + notification);
        if ("com.coloros.mcs".equals(oriPkg)) {
            pkg = notification.extras.getString("appPackage");
            opPkg = notification.extras.getString("appPackage");
            callingUid = -1;
            try {
                ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(pkg, 268435456, 0);
                if (appInfo != null) {
                    callingUid = appInfo.uid;
                }
            } catch (Throwable e) {
                Slog.w(TAG, "Could not contact PackageManager", e);
            } catch (Throwable e2) {
                Slog.w(TAG, "get pkg uid exception", e2);
            }
        } else {
            pkg = oriPkg;
            opPkg = oriOpPkg;
            callingUid = oriCallingUid;
        }
        boolean foundTarget = false;
        if (!(pkg == null || !pkg.contains(".stub") || notification == null)) {
            String contentTitle = notification.extras != null ? notification.extras.getString("android.title") : " ";
            if (contentTitle != null) {
                if (contentTitle.startsWith("notify#")) {
                    foundTarget = true;
                    Slog.d(TAG, "enqueueNotification, found notification, callingUid: " + callingUid + ", callingPid: " + callingPid + ", pkg: " + pkg + ", id: " + id + ", tag: " + tag);
                }
            }
        }
        if (!getContext().getPackageManager().isFullFunctionMode()) {
            try {
                ApplicationInfo noticeAppInfo = getContext().getPackageManager().getApplicationInfo(pkg, 0);
                if (noticeAppInfo.icon != 0) {
                    notification.setSmallIcon(Icon.createWithResource(pkg, noticeAppInfo.icon));
                    if (DEBUG_PANIC) {
                        Log.d(TAG, "enqueueNotificationInternal_we use app icon, and noticeAppInfo.icon = " + noticeAppInfo.icon);
                    }
                }
            } catch (Exception e3) {
                Log.e(TAG, "enqueueNotificationInternal_Exception = " + e3);
            }
        }
        if (incomingUserId != 999 || OppoMultiLauncherUtil.getInstance().isMultiApp(pkg)) {
            checkCallerIsSystemOrSameApp(pkg);
            synchronized (this.mNotificationNoClear) {
                if (!((notification.flags & 98) == 0 || this.mNotificationNoClear.contains(pkg))) {
                    this.mNotificationNoClear.add(pkg);
                    Slog.d(TAG, "enqueueNotificationInternal: add no clear notification : " + pkg);
                }
            }
            boolean isSystemNotification = !isUidSystem(callingUid) ? "android".equals(pkg) : true;
            boolean isNotificationFromListener = this.mListeners.isListenerPackage(pkg);
            int userId = ActivityManager.handleIncomingUser(callingPid, callingUid, incomingUserId, true, false, "enqueueNotification", pkg);
            UserHandle userHandle = new UserHandle(userId);
            try {
                int i;
                PackageManager packageManager = getContext().getPackageManager();
                if (userId == -1) {
                    i = 0;
                } else {
                    i = userId;
                }
                Notification.addFieldsFromContext(packageManager.getApplicationInfoAsUser(pkg, 268435456, i), userId, notification);
                this.mUsageStats.registerEnqueuedByApp(pkg);
                if (pkg == null || notification == null) {
                    throw new IllegalArgumentException("null not allowed: pkg=" + pkg + " id=" + id + " notification=" + notification);
                } else if (!this.mOppoLock) {
                    int i2;
                    StatusBarNotification n = new StatusBarNotification(pkg, opPkg, id, tag, callingUid, callingPid, 0, notification, userHandle);
                    if (!(isSystemNotification || isNotificationFromListener)) {
                        synchronized (this.mNotificationList) {
                            if (this.mNotificationsByKey.get(n.getKey()) != null) {
                                float appEnqueueRate = this.mUsageStats.getAppEnqueueRate(pkg);
                                if (appEnqueueRate > this.mMaxPackageEnqueueRate) {
                                    this.mUsageStats.registerOverRateQuota(pkg);
                                    long now = SystemClock.elapsedRealtime();
                                    if (now - this.mLastOverRateLogTime > 5000) {
                                        Slog.e(TAG, "Package enqueue rate is " + appEnqueueRate + ". Shedding events. package=" + pkg);
                                        this.mLastOverRateLogTime = now;
                                    }
                                }
                            }
                            int count = 0;
                            int N = this.mNotificationList.size();
                            for (i2 = 0; i2 < N; i2++) {
                                NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i2);
                                if (r.sbn.getPackageName().equals(pkg) && r.sbn.getUserId() == userId) {
                                    if (r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag)) {
                                        break;
                                    }
                                    count++;
                                    if (count >= 50) {
                                        this.mUsageStats.registerOverCountQuota(pkg);
                                        Slog.e(TAG, "Package has already posted " + count + " notifications.  Not showing more.  package=" + pkg);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    if (notification.allPendingIntents != null) {
                        int intentCount = notification.allPendingIntents.size();
                        if (intentCount > 0) {
                            ActivityManagerInternal am = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                            long duration = ((LocalService) LocalServices.getService(LocalService.class)).getNotificationWhitelistDuration();
                            for (i2 = 0; i2 < intentCount; i2++) {
                                PendingIntent pendingIntent = (PendingIntent) notification.allPendingIntents.valueAt(i2);
                                if (pendingIntent != null) {
                                    am.setPendingIntentWhitelistDuration(pendingIntent.getTarget(), duration);
                                }
                            }
                        }
                    }
                    notification.priority = clamp(notification.priority, -2, 2);
                    this.mHandler.post(new EnqueueNotificationRunnable(userId, new NotificationRecord(getContext(), n)));
                    idOut[0] = id;
                    if (foundTarget) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e4) {
                        }
                    }
                    return;
                } else {
                    return;
                }
            } catch (Throwable e5) {
                Slog.e(TAG, "Cannot create a context for sending app", e5);
                return;
            }
        }
        Slog.v(TAG, "enqueueNotificationInternal Not showing " + pkg + " userId:" + incomingUserId);
    }

    private boolean isSystemApp(String pkg) {
        try {
            if ((getContext().getPackageManager().getApplicationInfo(pkg, 0).flags & 1) != 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleGroupedNotificationLocked(NotificationRecord r, NotificationRecord old, int callingUid, int callingPid) {
        StatusBarNotification sbn = r.sbn;
        Notification n = sbn.getNotification();
        if (n.isGroupSummary() && !sbn.isAppGroup()) {
            n.flags &= -513;
        }
        String group = sbn.getGroupKey();
        boolean isSummary = n.isGroupSummary();
        Notification oldN = old != null ? old.sbn.getNotification() : null;
        String oldGroup = old != null ? old.sbn.getGroupKey() : null;
        boolean oldIsSummary = old != null ? oldN.isGroupSummary() : false;
        if (oldIsSummary) {
            NotificationRecord removedSummary = (NotificationRecord) this.mSummaryByGroupKey.remove(oldGroup);
            if (removedSummary != old) {
                Slog.w(TAG, "Removed summary didn't match old notification: old=" + old.getKey() + ", removed=" + (removedSummary != null ? removedSummary.getKey() : "<null>"));
            }
        }
        if (isSummary) {
            this.mSummaryByGroupKey.put(group, r);
        }
        if (!oldIsSummary) {
            return;
        }
        if (!isSummary || !oldGroup.equals(group)) {
            cancelGroupChildrenLocked(old, callingUid, callingPid, null, 12, false);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:190:0x03c4  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01fb  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void buzzBeepBlinkLocked(NotificationRecord record) {
        boolean buzz = false;
        boolean beep = false;
        boolean blink = false;
        Notification notification = record.sbn.getNotification();
        String key = record.getKey();
        boolean aboveThreshold = record.getImportance() >= 3;
        boolean canInterrupt = aboveThreshold && !record.isIntercepted();
        boolean canInterruptLight = aboveThreshold ? record.isIntercepted() ? this.mZenModeHelper.getZenMode() == 3 : true : false;
        Slog.v(TAG, "pkg=" + record.sbn.getPackageName() + " canInterrupt=" + canInterrupt + " intercept=" + record.isIntercepted());
        long token = Binder.clearCallingIdentity();
        try {
            boolean enableAlerts;
            int currentUser = ActivityManager.getCurrentUser();
            Binder.restoreCallingIdentity(token);
            String disableEffects = disableNotificationEffects(record);
            if (disableEffects != null) {
                ZenLog.traceDisableEffects(record, disableEffects);
            }
            boolean wasBeep = key != null ? key.equals(this.mSoundNotificationKey) : false;
            boolean wasBuzz = key != null ? key.equals(this.mVibrateNotificationKey) : false;
            boolean hasValidVibrate = false;
            boolean hasValidSound = false;
            boolean suppressedByGameSpace = suppressedByGameSpace(record.sbn.getPackageName());
            if (((this.mDisabledNotifications & DumpState.DUMP_DOMAIN_PREFERRED) == 0 || record.sbn.getPackageName().equals("com.android.mms")) && !this.mDmLock) {
                enableAlerts = !this.mPplLock;
            } else {
                enableAlerts = false;
            }
            if (enableAlerts && disableEffects == null && ((record.getUserId() == -1 || record.getUserId() == currentUser || ((record.sbn != null && record.getUserId() == OppoMultiAppManager.USER_ID && OppoMultiLauncherUtil.getInstance().isMultiApp(record.sbn.getPackageName())) || this.mUserProfiles.isCurrentProfile(record.getUserId()))) && canInterrupt && this.mSystemReady && this.mAudioManager != null)) {
                boolean useDefaultSound;
                Slog.v(TAG, "Interrupting!");
                if ((notification.defaults & 1) == 0) {
                    useDefaultSound = System.DEFAULT_NOTIFICATION_URI.equals(notification.sound);
                } else {
                    useDefaultSound = true;
                }
                Uri soundUri = null;
                if (useDefaultSound) {
                    soundUri = System.DEFAULT_NOTIFICATION_URI;
                    hasValidSound = this.mSystemNotificationSound != null;
                } else if (notification.sound != null) {
                    soundUri = notification.sound;
                    hasValidSound = soundUri != null;
                }
                boolean hasCustomVibrate = notification.vibrate != null;
                boolean convertSoundToVibration = (hasCustomVibrate || !hasValidSound) ? false : this.mAudioManager.getRingerModeInternal() == 1;
                boolean useDefaultVibrate = (notification.defaults & 2) != 0;
                if (useDefaultVibrate || convertSoundToVibration) {
                    hasValidVibrate = true;
                } else {
                    hasValidVibrate = hasCustomVibrate;
                }
                Object obj = record.isUpdate ? (notification.flags & 8) != 0 ? 1 : null : null;
                if (obj == null) {
                    long identity;
                    sendAccessibilityEvent(notification, record.sbn.getPackageName());
                    if (hasValidSound) {
                        if (noteNotificationSoundOp(record.sbn.getOpPkg(), record.sbn.getUid()) && !suppressedByGameSpace) {
                            boolean looping = (notification.flags & 4) != 0;
                            AudioAttributes audioAttributes = audioAttributesForNotification(notification);
                            this.mSoundNotificationKey = key;
                            if (!(this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(audioAttributes)) == 0 || this.mAudioManager.isAudioFocusExclusive())) {
                                identity = Binder.clearCallingIdentity();
                                try {
                                    IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                                    if (player != null) {
                                        Slog.v(TAG, "Playing sound " + soundUri + " with attributes " + audioAttributes);
                                        player.playAsync(soundUri, record.sbn.getUser(), looping, audioAttributes);
                                        beep = true;
                                    }
                                    Binder.restoreCallingIdentity(identity);
                                } catch (RemoteException e) {
                                    Binder.restoreCallingIdentity(identity);
                                } catch (Throwable th) {
                                    Binder.restoreCallingIdentity(identity);
                                    throw th;
                                }
                            }
                        }
                    }
                    if (hasValidVibrate) {
                        if (!(!noteNotificationSoundOp(record.sbn.getOpPkg(), record.sbn.getUid()) || this.mAudioManager.getRingerModeInternal() == 0 || suppressedByGameSpace)) {
                            this.mVibrateNotificationKey = key;
                            if (useDefaultVibrate || convertSoundToVibration) {
                                identity = Binder.clearCallingIdentity();
                                try {
                                    long[] jArr;
                                    Vibrator vibrator = this.mVibrator;
                                    int uid = record.sbn.getUid();
                                    String opPkg = record.sbn.getOpPkg();
                                    if (useDefaultVibrate) {
                                        jArr = this.mDefaultVibrationPattern;
                                    } else {
                                        jArr = this.mFallbackVibrationPattern;
                                    }
                                    vibrator.vibrate(uid, opPkg, jArr, (notification.flags & 4) != 0 ? 0 : -1, audioAttributesForNotification(notification));
                                    buzz = true;
                                    Binder.restoreCallingIdentity(identity);
                                } catch (Throwable th2) {
                                    Binder.restoreCallingIdentity(identity);
                                    throw th2;
                                }
                            } else if (notification.vibrate.length > 1) {
                                this.mVibrator.vibrate(record.sbn.getUid(), record.sbn.getOpPkg(), notification.vibrate, (notification.flags & 4) != 0 ? 0 : -1, audioAttributesForNotification(notification));
                                buzz = true;
                            }
                        }
                    }
                }
            }
            if (wasBeep && !hasValidSound) {
                clearSoundLocked();
            }
            if (wasBuzz && !hasValidVibrate) {
                clearVibrateLocked();
            }
            boolean wasShowLights = this.mLights.remove(key);
            if ((notification.flags & 1) != 0 && canInterruptLight && (record.getSuppressedVisualEffects() & 1) == 0) {
                if (noteNotificationLightsOp(record.sbn.getOpPkg(), record.sbn.getUid()) && !suppressedByGameSpace) {
                    this.mLights.add(key);
                    updateLightsLocked();
                    if (this.mUseAttentionLight) {
                        this.mAttentionLight.pulse();
                    }
                    blink = true;
                    if (buzz && !beep && !blink) {
                        return;
                    }
                    if ((record.getSuppressedVisualEffects() & 1) == 0) {
                        Slog.v(TAG, "Suppressed SystemUI from triggering screen on");
                        return;
                    }
                    EventLogTags.writeNotificationAlert(key, buzz ? 1 : 0, beep ? 1 : 0, blink ? 1 : 0);
                    this.mHandler.post(this.mBuzzBeepBlinked);
                    return;
                }
            }
            if (wasShowLights) {
                updateLightsLocked();
            }
            if (buzz) {
            }
            if ((record.getSuppressedVisualEffects() & 1) == 0) {
            }
        } catch (Throwable th22) {
            Binder.restoreCallingIdentity(token);
            throw th22;
        }
    }

    private boolean suppressedByGameSpace(String pkgName) {
        if (!this.mGameSpaceMode) {
            return false;
        }
        for (String pkg : GAME_SPACE_WHITELIST) {
            if (pkg.equals(pkgName)) {
                return false;
            }
        }
        return true;
    }

    private boolean noteNotificationLightsOp(String pkg, int uid) {
        if (this.mAppOps.noteOpNoThrow(69, uid, pkg) == 0) {
            return true;
        }
        Slog.v(TAG, "notifications light are disabled by AppOps for " + pkg);
        return false;
    }

    private boolean noteNotificationSoundOp(String pkg, int uid) {
        if (this.mAppOps.noteOpNoThrow(70, uid, pkg) == 0) {
            return true;
        }
        Slog.v(TAG, "notifications sound are disabled by AppOps for " + pkg);
        return false;
    }

    private static AudioAttributes audioAttributesForNotification(Notification n) {
        if (n.audioAttributes != null && !Notification.AUDIO_ATTRIBUTES_DEFAULT.equals(n.audioAttributes)) {
            return n.audioAttributes;
        }
        if (n.audioStreamType >= 0 && n.audioStreamType < AudioSystem.getNumStreamTypes()) {
            return new AudioAttributes.Builder().setInternalLegacyStreamType(n.audioStreamType).build();
        }
        if (n.audioStreamType == -1) {
            return Notification.AUDIO_ATTRIBUTES_DEFAULT;
        }
        String str = TAG;
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(n.audioStreamType);
        Log.w(str, String.format("Invalid stream type: %d", objArr));
        return Notification.AUDIO_ATTRIBUTES_DEFAULT;
    }

    void showNextToastLocked() {
        ToastRecord record = (ToastRecord) this.mToastQueue.get(0);
        while (record != null) {
            Slog.d(TAG, "Show pkg=" + record.pkg + " callback=" + record.callback);
            try {
                record.callback.show(record.token);
                scheduleTimeoutLocked(record);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Object died trying to show notification " + record.callback + " in package " + record.pkg);
                int index = this.mToastQueue.indexOf(record);
                if (index >= 0) {
                    this.mToastQueue.remove(index);
                }
                keepProcessAliveIfNeededLocked(record.pid);
                if (this.mToastQueue.size() > 0) {
                    record = (ToastRecord) this.mToastQueue.get(0);
                } else {
                    record = null;
                }
            }
        }
    }

    void cancelToastLocked(int index, boolean forceRemoveWin) {
        boolean removeWindow = forceRemoveWin;
        ToastRecord record = (ToastRecord) this.mToastQueue.get(index);
        try {
            record.callback.hide();
        } catch (RemoteException e) {
            Slog.w(TAG, "Object died trying to hide notification " + record.callback + " in package " + record.pkg);
            removeWindow = true;
        }
        this.mWindowManagerInternal.removeWindowToken(((ToastRecord) this.mToastQueue.remove(index)).token, removeWindow);
        keepProcessAliveIfNeededLocked(record.pid);
        if (this.mToastQueue.size() > 0) {
            showNextToastLocked();
        }
    }

    private void scheduleTimeoutLocked(ToastRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 2, r), (long) (r.duration == 1 ? 3500 : SHORT_DELAY));
    }

    private void handleTimeout(ToastRecord record) {
        Slog.d(TAG, "Timeout pkg=" + record.pkg + " callback=" + record.callback);
        synchronized (this.mToastQueue) {
            int index = indexOfToastLocked(record.pkg, record.callback);
            if (index >= 0) {
                cancelToastLocked(index, true);
            }
        }
    }

    int indexOfToastLocked(String pkg, ITransientNotification callback) {
        IBinder cbak = callback.asBinder();
        ArrayList<ToastRecord> list = this.mToastQueue;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            ToastRecord r = (ToastRecord) list.get(i);
            if (r.pkg.equals(pkg) && r.callback.asBinder() == cbak) {
                return i;
            }
        }
        return -1;
    }

    void keepProcessAliveIfNeededLocked(int pid) {
        boolean z = false;
        int toastCount = 0;
        ArrayList<ToastRecord> list = this.mToastQueue;
        int N = list.size();
        for (int i = 0; i < N; i++) {
            if (((ToastRecord) list.get(i)).pid == pid) {
                toastCount++;
            }
        }
        try {
            IActivityManager iActivityManager = this.mAm;
            IBinder iBinder = this.mForegroundToken;
            if (toastCount > 0) {
                z = true;
            }
            iActivityManager.setProcessForeground(iBinder, pid, z);
        } catch (RemoteException e) {
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0050, code:
            if (r0 == false) goto L_0x0055;
     */
    /* JADX WARNING: Missing block: B:19:0x0052, code:
            scheduleSendRankingUpdate();
     */
    /* JADX WARNING: Missing block: B:20:0x0055, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleRankingReconsideration(Message message) {
        if (message.obj instanceof RankingReconsideration) {
            RankingReconsideration recon = message.obj;
            recon.run();
            synchronized (this.mNotificationList) {
                NotificationRecord record = (NotificationRecord) this.mNotificationsByKey.get(recon.getKey());
                if (record == null) {
                    return;
                }
                int indexBefore = findNotificationRecordIndexLocked(record);
                boolean interceptBefore = record.isIntercepted();
                int visibilityBefore = record.getPackageVisibilityOverride();
                recon.applyChangesLocked(record);
                applyZenModeLocked(record);
                this.mRankingHelper.sort(this.mNotificationList);
                int indexAfter = findNotificationRecordIndexLocked(record);
                boolean interceptAfter = record.isIntercepted();
                boolean changed = (indexBefore == indexAfter && interceptBefore == interceptAfter) ? visibilityBefore != record.getPackageVisibilityOverride() : true;
                if (interceptBefore && !interceptAfter) {
                    buzzBeepBlinkLocked(record);
                }
            }
        }
    }

    private void handleRankingSort() {
        synchronized (this.mNotificationList) {
            int i;
            NotificationRecord r;
            int N = this.mNotificationList.size();
            ArrayList<String> orderBefore = new ArrayList(N);
            ArrayList<String> groupOverrideBefore = new ArrayList(N);
            int[] visibilities = new int[N];
            int[] importances = new int[N];
            for (i = 0; i < N; i++) {
                r = (NotificationRecord) this.mNotificationList.get(i);
                orderBefore.add(r.getKey());
                groupOverrideBefore.add(r.sbn.getGroupKey());
                visibilities[i] = r.getPackageVisibilityOverride();
                importances[i] = r.getImportance();
                this.mRankingHelper.extractSignals(r);
            }
            this.mRankingHelper.sort(this.mNotificationList);
            i = 0;
            while (i < N) {
                r = (NotificationRecord) this.mNotificationList.get(i);
                if (((String) orderBefore.get(i)).equals(r.getKey()) && visibilities[i] == r.getPackageVisibilityOverride()) {
                    if (importances[i] == r.getImportance() && ((String) groupOverrideBefore.get(i)).equals(r.sbn.getGroupKey())) {
                        i++;
                    }
                }
                scheduleSendRankingUpdate();
                return;
            }
        }
    }

    private void recordCallerLocked(NotificationRecord record) {
        if (this.mZenModeHelper.isCall(record)) {
            this.mZenModeHelper.recordCaller(record);
        }
    }

    private void applyZenModeLocked(NotificationRecord record) {
        int i = 0;
        record.setIntercepted(this.mZenModeHelper.shouldIntercept(record));
        if (record.isIntercepted()) {
            int i2;
            if (this.mZenModeHelper.shouldSuppressWhenScreenOff()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (this.mZenModeHelper.shouldSuppressWhenScreenOn()) {
                i = 2;
            }
            record.setSuppressedVisualEffects(i2 | i);
        }
    }

    private int findNotificationRecordIndexLocked(NotificationRecord target) {
        return this.mRankingHelper.indexOf(this.mNotificationList, target);
    }

    private void scheduleSendRankingUpdate() {
        if (!this.mHandler.hasMessages(4)) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 4));
        }
    }

    private void handleSendRankingUpdate() {
        synchronized (this.mNotificationList) {
            this.mListeners.notifyRankingUpdateLocked();
        }
    }

    private void scheduleListenerHintsChanged(int state) {
        this.mHandler.removeMessages(5);
        this.mHandler.obtainMessage(5, state, 0).sendToTarget();
    }

    private void scheduleInterruptionFilterChanged(int listenerInterruptionFilter) {
        this.mHandler.removeMessages(6);
        this.mHandler.obtainMessage(6, listenerInterruptionFilter, 0).sendToTarget();
    }

    private void handleListenerHintsChanged(int hints) {
        synchronized (this.mNotificationList) {
            this.mListeners.notifyListenerHintsChangedLocked(hints);
        }
    }

    private void handleListenerInterruptionFilterChanged(int interruptionFilter) {
        synchronized (this.mNotificationList) {
            this.mListeners.notifyInterruptionFilterChanged(interruptionFilter);
        }
    }

    public void sendDataToDcs(String eventId, ArrayList<String> dataStatisticList) {
        if (eventId.equals(EVENTID_FOR_BLOCKED_BY_BLACKLIST)) {
            for (int i = 0; i < dataStatisticList.size(); i++) {
                sendDataToDcsAfterLocalHandle(eventId, (String) dataStatisticList.get(i));
            }
            return;
        }
        Map<String, String> eventMap = new HashMap();
        for (int j = 0; j < dataStatisticList.size(); j++) {
            String strPkg = (String) dataStatisticList.get(j);
            if (eventMap.containsKey(strPkg)) {
                eventMap.put(strPkg, String.valueOf(Integer.parseInt((String) eventMap.get(strPkg)) + 1));
            } else {
                eventMap.put(strPkg, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            }
        }
        if (eventId.equals(EVENTID_FOR_BLOCKED_BY_DYNAMIC)) {
            this.mDynamicNotificationStatisticList.clear();
        } else if (eventId.equals(EVENTID_FOR_BLOCKED_BY_NORMAL)) {
            this.mNormalNotificationStatisticList.clear();
        } else if (eventId.equals(EVENTID_FOR_BLOCKED_BY_UNNORMAL)) {
            this.mUnNormalNotificationStatisticList.clear();
        }
        if (DEBUG_PANIC) {
            Slog.d(TAG, "sendDataToDcs_eventId = " + eventId);
        }
        OppoStatistics.onCommon(getContext(), TAG, eventId, eventMap, false);
    }

    private void sendDataToDcsAfterLocalHandle(String eventID, String eventTag) {
        if (DEBUG_PANIC) {
            Log.d(TAG, "sendEventDataAfterLocalHandle_eventID = " + eventID + ", eventTag = " + eventTag);
        }
        if (sEventMap.containsKey(eventTag)) {
            sEventMap.put(eventTag, String.valueOf(Integer.valueOf((String) sEventMap.get(eventTag)).intValue() + 1));
            if (DEBUG_PANIC) {
                Log.d(TAG, "containsKey = " + eventTag + ", and new value = " + ((String) sEventMap.get(eventTag)));
            }
        } else {
            sEventMap.put(eventTag, String.valueOf(1));
            if (DEBUG_PANIC) {
                Log.d(TAG, "not contains " + eventTag);
            }
        }
        if (DEBUG_PANIC) {
            Log.d(TAG, "value =" + ((String) sEventMap.get(eventTag)) + ",eventMap = " + sEventMap);
        }
        long currentTime = System.currentTimeMillis();
        if (mLastUploadStaticsDataTime <= 0) {
            mLastUploadStaticsDataTime = currentTime;
        }
        if (DEBUG_PANIC) {
            Log.d(TAG, " onKVEvent, durring = " + (currentTime - mLastUploadStaticsDataTime));
        }
        if (currentTime - mLastUploadStaticsDataTime > TIME_UPLOAD_THRESHOLD && sEventMap.size() > 0) {
            OppoStatistics.onCommon(getContext(), TAG, eventID, sEventMap, false);
            mLastUploadStaticsDataTime = currentTime;
            sEventMap.clear();
        }
    }

    static int clamp(int x, int low, int high) {
        if (x < low) {
            return low;
        }
        return x > high ? high : x;
    }

    void sendAccessibilityEvent(Notification notification, CharSequence packageName) {
        AccessibilityManager manager = AccessibilityManager.getInstance(getContext());
        if (manager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(64);
            event.setPackageName(packageName);
            event.setClassName(Notification.class.getName());
            event.setParcelableData(notification);
            CharSequence tickerText = notification.tickerText;
            if (!TextUtils.isEmpty(tickerText)) {
                event.getText().add(tickerText);
            }
            manager.sendAccessibilityEvent(event);
        }
    }

    private void cancelNotificationLocked(NotificationRecord r, boolean sendDelete, int reason) {
        long identity;
        recordCallerLocked(r);
        if (sendDelete && r.getNotification().deleteIntent != null) {
            try {
                r.getNotification().deleteIntent.send();
            } catch (CanceledException ex) {
                Slog.w(TAG, "canceled PendingIntent for " + r.sbn.getPackageName(), ex);
            }
        }
        if (r.getNotification().getSmallIcon() != null) {
            r.isCanceled = true;
            this.mListeners.notifyRemovedLocked(r.sbn);
        }
        String canceledKey = r.getKey();
        if (canceledKey.equals(this.mSoundNotificationKey)) {
            this.mSoundNotificationKey = null;
            identity = Binder.clearCallingIdentity();
            try {
                IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                if (player != null) {
                    player.stopAsync();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
        if (canceledKey.equals(this.mVibrateNotificationKey)) {
            this.mVibrateNotificationKey = null;
            identity = Binder.clearCallingIdentity();
            try {
                this.mVibrator.cancel();
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(identity);
                throw th2;
            }
        }
        this.mLights.remove(canceledKey);
        switch (reason) {
            case 2:
            case 3:
            case 10:
            case 11:
                this.mUsageStats.registerDismissedByUser(r);
                break;
            case 8:
            case 9:
                this.mUsageStats.registerRemovedByApp(r);
                break;
        }
        this.mNotificationsByKey.remove(r.sbn.getKey());
        String groupKey = r.getGroupKey();
        NotificationRecord groupSummary = (NotificationRecord) this.mSummaryByGroupKey.get(groupKey);
        if (groupSummary != null && groupSummary.getKey().equals(r.getKey())) {
            this.mSummaryByGroupKey.remove(groupKey);
        }
        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(r.sbn.getUserId()));
        if (summaries != null && r.sbn.getKey().equals(summaries.get(r.sbn.getPackageName()))) {
            summaries.remove(r.sbn.getPackageName());
        }
        this.mArchive.record(r.sbn);
        long now = System.currentTimeMillis();
        EventLogTags.writeNotificationCanceled(canceledKey, reason, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
    }

    void cancelNotification(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, ManagedServiceInfo listener) {
        boolean foundTarget = false;
        if (id == 9 && tag == null && pkg != null) {
            if (pkg.contains("stub") && reason == 8) {
                Slog.d(TAG, "cancelNotification, pkg: " + pkg + ", id: " + id + ", tag: " + tag);
                synchronized (this.mNotificationList) {
                    int index = indexOfNotificationLocked(pkg, tag, id, userId);
                    if (index >= 0 && ((NotificationRecord) this.mNotificationList.get(index)).getNotification() != null) {
                        Notification target = ((NotificationRecord) this.mNotificationList.get(index)).getNotification();
                        String contentTitle = target.extras != null ? target.extras.getString("android.title") : IElsaManager.EMPTY_PACKAGE;
                        Slog.d(TAG, "contentTitle: " + contentTitle);
                        if ("notify#9".equalsIgnoreCase(contentTitle)) {
                            foundTarget = true;
                            Slog.d(TAG, "Found notification, callingUid: " + callingUid + ", callingPid: " + callingPid + ", pkg: " + pkg + ", id: " + id + ", tag: " + tag);
                        }
                    }
                }
            }
        }
        final ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final String str = pkg;
        final int i3 = id;
        final String str2 = tag;
        final int i4 = userId;
        final int i5 = mustHaveFlags;
        final int i6 = mustNotHaveFlags;
        final int i7 = reason;
        final boolean z = sendDelete;
        this.mHandler.post(new Runnable() {
            /* JADX WARNING: Missing block: B:25:0x008f, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                EventLogTags.writeNotificationCancel(i, i2, str, i3, str2, i4, i5, i6, i7, listenerName);
                synchronized (NotificationManagerService.this.mNotificationList) {
                    int index = NotificationManagerService.this.indexOfNotificationLocked(str, str2, i3, i4);
                    if (index >= 0) {
                        NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(index);
                        if (i7 == 1) {
                            NotificationManagerService.this.mUsageStats.registerClickedByUser(r);
                        }
                        if ((r.getNotification().flags & i5) != i5) {
                        } else if ((r.getNotification().flags & i6) != 0) {
                        } else {
                            NotificationManagerService.this.mNotificationList.remove(index);
                            NotificationManagerService.this.cancelNotificationLocked(r, z, i7);
                            NotificationManagerService.this.cancelGroupChildrenLocked(r, i, i2, listenerName, 12, z);
                            NotificationManagerService.this.updateLightsLocked();
                        }
                    }
                }
            }
        });
        if (foundTarget) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private boolean notificationMatchesUserId(NotificationRecord r, int userId) {
        boolean z = true;
        if (userId == OppoMultiAppManager.USER_ID && r != null && r.sbn != null && userId == r.sbn.getUserId()) {
            return true;
        }
        if (!(userId == -1 || r.getUserId() == -1 || r.getUserId() == userId)) {
            z = false;
        }
        return z;
    }

    private boolean notificationMatchesCurrentProfiles(NotificationRecord r, int userId) {
        if (notificationMatchesUserId(r, userId)) {
            return true;
        }
        return this.mUserProfiles.isCurrentProfile(r.getUserId());
    }

    /* JADX WARNING: Missing block: B:62:0x0115, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean cancelAllNotificationsInt(int callingUid, int callingPid, String pkg, int mustHaveFlags, int mustNotHaveFlags, boolean doit, int userId, int reason, ManagedServiceInfo listener) {
        String listenerName;
        if (listener == null) {
            listenerName = null;
        } else {
            listenerName = listener.component.toShortString();
        }
        EventLogTags.writeNotificationCancelAll(callingUid, callingPid, pkg, userId, mustHaveFlags, mustNotHaveFlags, reason, listenerName);
        synchronized (this.mNotificationList) {
            int i;
            ArrayList canceledNotifications = null;
            for (i = this.mNotificationList.size() - 1; i >= 0; i--) {
                NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i);
                if (notificationMatchesUserId(r, userId)) {
                    if (!(r.getUserId() == -1 && pkg == null) && (r.getFlags() & mustHaveFlags) == mustHaveFlags && (r.getFlags() & mustNotHaveFlags) == 0 && (pkg == null || r.sbn.getPackageName().equals(pkg))) {
                        if (reason == 21 || reason == 20) {
                            if (!(r.sbn.isClearable() && (isSystemApp(pkg) || PKG_GAMECENTER.equals(pkg)))) {
                                Notification notification = r.getNotification();
                                if (notification != null) {
                                    String appPackage = notification.extras.getString("appPackage");
                                    if (pkg != null && pkg.equals(appPackage)) {
                                    }
                                }
                            }
                        }
                        if (canceledNotifications == null) {
                            canceledNotifications = new ArrayList();
                        }
                        canceledNotifications.add(r);
                        if (doit) {
                            this.mNotificationList.remove(i);
                            cancelNotificationLocked(r, false, reason);
                        } else {
                            return true;
                        }
                    }
                }
            }
            if (doit && canceledNotifications != null) {
                int M = canceledNotifications.size();
                for (i = 0; i < M; i++) {
                    cancelGroupChildrenLocked((NotificationRecord) canceledNotifications.get(i), callingUid, callingPid, listenerName, 12, false);
                }
            }
            if (canceledNotifications != null) {
                updateLightsLocked();
            }
            boolean z = canceledNotifications != null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0038 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0058  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void cancelAllLocked(int callingUid, int callingPid, int userId, int reason, ManagedServiceInfo listener, boolean includeCurrentProfiles) {
        String listenerName;
        int i;
        if (listener == null) {
            listenerName = null;
        } else {
            listenerName = listener.component.toShortString();
        }
        EventLogTags.writeNotificationCancelAll(callingUid, callingPid, null, userId, 0, 0, reason, listenerName);
        ArrayList canceledNotifications = null;
        for (i = this.mNotificationList.size() - 1; i >= 0; i--) {
            NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i);
            if (includeCurrentProfiles) {
                if (!notificationMatchesCurrentProfiles(r, userId)) {
                }
                if ((r.getFlags() & 34) != 0) {
                    this.mNotificationList.remove(i);
                    cancelNotificationLocked(r, true, reason);
                    if (canceledNotifications == null) {
                        canceledNotifications = new ArrayList();
                    }
                    canceledNotifications.add(r);
                }
            } else {
                if (!notificationMatchesUserId(r, userId)) {
                }
                if ((r.getFlags() & 34) != 0) {
                }
            }
        }
        int M = canceledNotifications != null ? canceledNotifications.size() : 0;
        for (i = 0; i < M; i++) {
            cancelGroupChildrenLocked((NotificationRecord) canceledNotifications.get(i), callingUid, callingPid, listenerName, 12, false);
        }
        updateLightsLocked();
    }

    private void cancelGroupChildrenLocked(NotificationRecord r, int callingUid, int callingPid, String listenerName, int reason, boolean sendDelete) {
        if (r.getNotification().isGroupSummary()) {
            String pkg = r.sbn.getPackageName();
            int userId = r.getUserId();
            if (pkg == null) {
                Log.e(TAG, "No package for group summary: " + r.getKey());
                return;
            }
            for (int i = this.mNotificationList.size() - 1; i >= 0; i--) {
                NotificationRecord childR = (NotificationRecord) this.mNotificationList.get(i);
                StatusBarNotification childSbn = childR.sbn;
                if (childSbn.isGroup() && !childSbn.getNotification().isGroupSummary() && childR.getGroupKey().equals(r.getGroupKey())) {
                    EventLogTags.writeNotificationCancel(callingUid, callingPid, pkg, childSbn.getId(), childSbn.getTag(), userId, 0, 0, reason, listenerName);
                    this.mNotificationList.remove(i);
                    cancelNotificationLocked(childR, sendDelete, reason);
                }
            }
        }
    }

    void updateLightsLocked() {
        NotificationRecord ledNotification = null;
        while (ledNotification == null && !this.mLights.isEmpty()) {
            String owner = (String) this.mLights.get(this.mLights.size() - 1);
            ledNotification = (NotificationRecord) this.mNotificationsByKey.get(owner);
            if (ledNotification == null) {
                Slog.wtfStack(TAG, "LED Notification does not exist: " + owner);
                this.mLights.remove(owner);
            }
        }
        if (ledNotification == null || this.mInCall || this.mScreenOn || this.mDmLock || this.mPplLock || this.mIsShutDown) {
            this.mNotificationLight.turnOff();
            if (this.mStatusBar != null) {
                this.mStatusBar.notificationLightOff();
                return;
            }
            return;
        }
        Notification ledno = ledNotification.sbn.getNotification();
        int ledARGB = this.mDefaultNotificationColor;
        int ledOnMS = this.mDefaultNotificationLedOn;
        int ledOffMS = this.mDefaultNotificationLedOff;
        if (this.mNotificationPulseEnabled) {
            this.mNotificationLight.setFlashing(ledARGB, 1, ledOnMS, ledOffMS);
        }
        if (this.mStatusBar != null) {
            this.mStatusBar.notificationLightPulse(ledARGB, ledOnMS, ledOffMS);
        }
    }

    int indexOfNotificationLocked(String pkg, String tag, int id, int userId) {
        ArrayList<NotificationRecord> list = this.mNotificationList;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = (NotificationRecord) list.get(i);
            if (notificationMatchesUserId(r, userId) && r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag) && r.sbn.getPackageName().equals(pkg)) {
                return i;
            }
        }
        return -1;
    }

    int indexOfNotificationLocked(String key) {
        int N = this.mNotificationList.size();
        for (int i = 0; i < N; i++) {
            if (key.equals(((NotificationRecord) this.mNotificationList.get(i)).getKey())) {
                return i;
            }
        }
        return -1;
    }

    private void updateNotificationPulse() {
        synchronized (this.mNotificationList) {
            updateLightsLocked();
        }
    }

    private static boolean isUidSystem(int uid) {
        int appid = UserHandle.getAppId(uid);
        if (appid == 1000 || appid == 1001 || uid == 0) {
            return true;
        }
        return false;
    }

    private static boolean isCallerSystem() {
        return isUidSystem(Binder.getCallingUid());
    }

    private static void checkCallerIsSystem() {
        if (!isCallerSystem()) {
            throw new SecurityException("Disallowed call for uid " + Binder.getCallingUid());
        }
    }

    private static void checkCallerIsSystemOrSameApp(String pkg) {
        if (!isCallerSystem()) {
            checkCallerIsSameApp(pkg);
        }
    }

    private static void checkCallerIsSameApp(String pkg) {
        int uid = Binder.getCallingUid();
        try {
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
            if (ai == null) {
                throw new SecurityException("Unknown package " + pkg);
            } else if (!UserHandle.isSameApp(ai.uid, uid)) {
                throw new SecurityException("Calling uid " + uid + " gave package" + pkg + " which is owned by uid " + ai.uid);
            }
        } catch (RemoteException re) {
            throw new SecurityException("Unknown package " + pkg + "\n" + re);
        }
    }

    private static String callStateToString(int state) {
        switch (state) {
            case 0:
                return "CALL_STATE_IDLE";
            case 1:
                return "CALL_STATE_RINGING";
            case 2:
                return "CALL_STATE_OFFHOOK";
            default:
                return "CALL_STATE_UNKNOWN_" + state;
        }
    }

    private void listenForCallState() {
        TelephonyManager.from(getContext()).listen(this.mPhoneStateListener, 32);
    }

    private NotificationRankingUpdate makeRankingUpdateLocked(ManagedServiceInfo info) {
        int i;
        int N = this.mNotificationList.size();
        ArrayList<String> arrayList = new ArrayList(N);
        ArrayList<String> interceptedKeys = new ArrayList(N);
        ArrayList<Integer> importance = new ArrayList(N);
        Bundle overrideGroupKeys = new Bundle();
        Bundle visibilityOverrides = new Bundle();
        Bundle suppressedVisualEffects = new Bundle();
        Bundle explanation = new Bundle();
        for (i = 0; i < N; i++) {
            NotificationRecord record = (NotificationRecord) this.mNotificationList.get(i);
            if (isVisibleToListener(record.sbn, info)) {
                String key = record.sbn.getKey();
                arrayList.add(key);
                importance.add(Integer.valueOf(record.getImportance()));
                if (record.getImportanceExplanation() != null) {
                    explanation.putCharSequence(key, record.getImportanceExplanation());
                }
                if (record.isIntercepted()) {
                    interceptedKeys.add(key);
                }
                suppressedVisualEffects.putInt(key, record.getSuppressedVisualEffects());
                if (record.getPackageVisibilityOverride() != -1000) {
                    visibilityOverrides.putInt(key, record.getPackageVisibilityOverride());
                }
                overrideGroupKeys.putString(key, record.sbn.getOverrideGroupKey());
            }
        }
        int M = arrayList.size();
        String[] keysAr = (String[]) arrayList.toArray(new String[M]);
        String[] interceptedKeysAr = (String[]) interceptedKeys.toArray(new String[interceptedKeys.size()]);
        int[] importanceAr = new int[M];
        for (i = 0; i < M; i++) {
            importanceAr[i] = ((Integer) importance.get(i)).intValue();
        }
        return new NotificationRankingUpdate(keysAr, interceptedKeysAr, visibilityOverrides, suppressedVisualEffects, importanceAr, explanation, overrideGroupKeys);
    }

    private boolean isVisibleToListener(StatusBarNotification sbn, ManagedServiceInfo listener) {
        if (listener.enabledAndUserMatches(sbn.getUserId())) {
            return true;
        }
        return false;
    }

    private boolean isPackageSuspendedForUser(String pkg, int uid) {
        try {
            return AppGlobals.getPackageManager().isPackageSuspendedForUser(pkg, UserHandle.getUserId(uid));
        } catch (RemoteException e) {
            throw new SecurityException("Could not talk to package manager service");
        } catch (IllegalArgumentException e2) {
            return false;
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Jianhua.Lin@Plf.SDK : Add for Cancel the current Toast", property = OppoRomType.ROM)
    void cancelCurrentToastLocked() {
        long callingId = Binder.clearCallingIdentity();
        try {
            if (this.mToastQueue.size() != 0) {
                cancelToastLocked(0, false);
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private boolean dumpNoClearNotification(PrintWriter pw, String[] args) {
        if (args.length != 1) {
            return false;
        }
        if (!"noClear".equals(args[0])) {
            return false;
        }
        pw.println("\n  mNotificationNoClear:");
        synchronized (this.mNotificationNoClear) {
            for (String pkg : this.mNotificationNoClear) {
                pw.println("    NoClearNotification:" + pkg);
            }
        }
        return true;
    }

    private void initDir() {
        File notificationBlacklistDirectory = new File(OPPO_NOTIFICATION_BLACKLIST_DIRECTORY);
        File notificationBlacklistFilePath = new File(OPPO_NOTIFICATION_BLACKLIST_FILE_PATH);
        try {
            if (!notificationBlacklistDirectory.exists()) {
                notificationBlacklistDirectory.mkdirs();
            }
            if (!notificationBlacklistFilePath.exists()) {
                notificationBlacklistFilePath.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "init notificationBlacklistFilePath Dir failed!!!");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x0096 A:{SYNTHETIC, Splitter: B:41:0x0096} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        Exception e;
        Throwable th;
        File xmlFile = new File(OPPO_NOTIFICATION_BLACKLIST_FILE_PATH);
        if (xmlFile.exists()) {
            FileReader xmlReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader2 = new FileReader(xmlFile);
                    try {
                        parser.setInput(xmlReader2);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (parser.getName().equals(NOTIFICATON_TITLE_NAME)) {
                                        eventType = parser.next();
                                        updateDynamicInterceptList(parser.getAttributeValue(null, "value"));
                                        break;
                                    }
                                    break;
                            }
                        }
                        if (xmlReader2 != null) {
                            try {
                                xmlReader2.close();
                            } catch (IOException e2) {
                                Log.w(TAG, "Got execption close permReader.", e2);
                            }
                        }
                    } catch (Exception e3) {
                        e = e3;
                        xmlReader = xmlReader2;
                        try {
                            Log.w(TAG, "Got execption parsing permissions.", e);
                            if (xmlReader != null) {
                                try {
                                    xmlReader.close();
                                } catch (IOException e22) {
                                    Log.w(TAG, "Got execption close permReader.", e22);
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (xmlReader != null) {
                                try {
                                    xmlReader.close();
                                } catch (IOException e222) {
                                    Log.w(TAG, "Got execption close permReader.", e222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        xmlReader = xmlReader2;
                        if (xmlReader != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Log.w(TAG, "Couldn't find or open alarm_filter_packages file " + xmlFile);
                }
            } catch (Exception e5) {
                e = e5;
            }
        }
    }

    private void updateDynamicInterceptList(String tagName) {
        if (DEBUG_PANIC) {
            Slog.i(TAG, "updateNotifcationTitleName_tagName = " + tagName);
        }
        if (tagName != null && tagName != IElsaManager.EMPTY_PACKAGE) {
            String[] stringArray = tagName.split("/");
            if (DEBUG_PANIC) {
                Slog.d(TAG, "stringArray = " + stringArray + ",stringArray.length = " + stringArray.length);
            }
            for (int i = 0; i < stringArray.length; i++) {
                if (DEBUG_PANIC) {
                    Slog.i(TAG, "updateNotifcationTitleName_stringArray[" + i + "] = " + stringArray[i]);
                }
            }
            this.mDynamicFilterNotificationList.add(stringArray);
        }
    }
}

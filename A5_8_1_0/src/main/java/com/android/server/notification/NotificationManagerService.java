package com.android.server.notification;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AutomaticZenRule;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.app.ITransientNotification;
import android.app.Notification;
import android.app.Notification.TvExtender;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.backup.BackupManager;
import android.app.usage.UsageStatsManagerInternal;
import android.companion.ICompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.IRingtonePlayer;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.IStatusBarNotificationHolder;
import android.service.notification.NotificationRankingUpdate;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.telecom.TelecomManager;
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
import android.util.proto.ProtoOutputStream;
import android.view.WindowManagerInternal;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.OppoBPMUtils;
import com.android.server.SystemService;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.am.OppoMultiAppManagerUtil;
import com.android.server.am.OppoPhoneStateReceiver;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.notification.ManagedServices.Config;
import com.android.server.notification.ManagedServices.ManagedServiceInfo;
import com.android.server.notification.ManagedServices.UserProfiles;
import com.android.server.notification.ZenModeHelper.Callback;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.statusbar.StatusBarManagerInternal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NotificationManagerService extends SystemService {
    private static final String ACTION_NOTIFICATION_TIMEOUT = (NotificationManagerService.class.getSimpleName() + ".TIMEOUT");
    private static final String ATTR_VERSION = "version";
    static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int DB_VERSION = 1;
    private static boolean DEBUG_PANIC = false;
    static final float DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE = 5.0f;
    static final int DEFAULT_STREAM_TYPE = 5;
    static final long[] DEFAULT_VIBRATE_PATTERN = new long[]{0, 250, 250, 250};
    private static final long DELAY_FOR_ASSISTANT_TIME = 100;
    static final boolean ENABLE_BLOCKED_TOASTS = true;
    public static final boolean ENABLE_CHILD_NOTIFICATIONS = SystemProperties.getBoolean("debug.child_notifs", true);
    private static final int EVENTLOG_ENQUEUE_STATUS_IGNORED = 2;
    private static final int EVENTLOG_ENQUEUE_STATUS_NEW = 0;
    private static final int EVENTLOG_ENQUEUE_STATUS_UPDATE = 1;
    private static final String EXTRA_KEY = "key";
    static final int LONG_DELAY = 3500;
    static final int MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS = 3000;
    static final float MATCHES_CALL_FILTER_TIMEOUT_AFFINITY = 1.0f;
    static final int MAX_PACKAGE_NOTIFICATIONS = 50;
    static final int MESSAGE_LISTENER_HINTS_CHANGED = 5;
    static final int MESSAGE_LISTENER_NOTIFICATION_FILTER_CHANGED = 6;
    private static final int MESSAGE_RANKING_SORT = 1001;
    private static final int MESSAGE_RECONSIDER_RANKING = 1000;
    static final int MESSAGE_SAVE_POLICY_FILE = 3;
    static final int MESSAGE_SEND_RANKING_UPDATE = 4;
    static final int MESSAGE_TIMEOUT = 2;
    private static final long MIN_PACKAGE_OVERRATE_LOG_INTERVAL = 5000;
    private static final int MY_PID = Process.myPid();
    private static final int MY_UID = Process.myUid();
    private static final int REQUEST_CODE_TIMEOUT = 1;
    private static final String SCHEME_TIMEOUT = "timeout";
    static final int SHORT_DELAY = 2000;
    static final long SNOOZE_UNTIL_UNSPECIFIED = -1;
    static final String TAG = "NotificationService";
    private static final String TAG_NOTIFICATION_POLICY = "notification-policy";
    static final int VIBRATE_PATTERN_MAXLEN = 17;
    private static final IBinder WHITELIST_TOKEN = new Binder();
    private AccessibilityManager mAccessibilityManager;
    private ActivityManager mActivityManager;
    private AlarmManager mAlarmManager;
    private IActivityManager mAm;
    private AppOpsManager mAppOps;
    private UsageStatsManagerInternal mAppUsageStats;
    private Archive mArchive;
    private NotificationAssistants mAssistants;
    Light mAttentionLight;
    AudioManager mAudioManager;
    AudioManagerInternal mAudioManagerInternal;
    @GuardedBy("mNotificationLock")
    final ArrayMap<Integer, ArrayMap<String, String>> mAutobundledSummaries = new ArrayMap();
    private int mCallState;
    private ICompanionDeviceManager mCompanionManager;
    private ConditionProviders mConditionProviders;
    private boolean mDisableNotificationEffects;
    private List<ComponentName> mEffectsSuppressors = new ArrayList();
    @GuardedBy("mNotificationLock")
    final ArrayList<NotificationRecord> mEnqueuedNotifications = new ArrayList();
    private long[] mFallbackVibrationPattern;
    final IBinder mForegroundToken = new Binder();
    private GroupHelper mGroupHelper;
    private WorkerHandler mHandler;
    protected boolean mInCall = false;
    private AudioAttributes mInCallNotificationAudioAttributes;
    private Uri mInCallNotificationUri;
    private float mInCallNotificationVolume;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
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
            } else if (action.equals("android.intent.action.USER_STOPPED")) {
                userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, null, 0, 0, true, userHandle, 6, null);
                }
            } else if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, null, 0, 0, true, userHandle, 15, null);
                }
            } else if (action.equals("android.intent.action.USER_PRESENT")) {
                NotificationManagerService.this.mNotificationLight.turnOff();
                NotificationManagerService.this.mLights.clear();
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mSettingsObserver.update(null);
                NotificationManagerService.this.mUserProfiles.updateCache(context);
                NotificationManagerService.this.mConditionProviders.onUserSwitched(user);
                NotificationManagerService.this.mListeners.onUserSwitched(user);
                NotificationManagerService.this.mAssistants.onUserSwitched(user);
                NotificationManagerService.this.mZenModeHelper.onUserSwitched(user);
            } else if (action.equals("android.intent.action.USER_ADDED")) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (userId != -10000) {
                    NotificationManagerService.this.mUserProfiles.updateCache(context);
                    if (!NotificationManagerService.this.mUserProfiles.isManagedProfile(userId)) {
                        NotificationManagerService.this.readDefaultApprovedServices(userId);
                    }
                }
            } else if (action.equals("android.intent.action.USER_REMOVED")) {
                user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mZenModeHelper.onUserRemoved(user);
                NotificationManagerService.this.mRankingHelper.onUserRemoved(user);
                NotificationManagerService.this.mListeners.onUserRemoved(user);
                NotificationManagerService.this.mConditionProviders.onUserRemoved(user);
                NotificationManagerService.this.mAssistants.onUserRemoved(user);
                NotificationManagerService.this.savePolicyFile();
            } else if (action.equals("android.intent.action.USER_UNLOCKED")) {
                user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mConditionProviders.onUserUnlocked(user);
                NotificationManagerService.this.mListeners.onUserUnlocked(user);
                NotificationManagerService.this.mAssistants.onUserUnlocked(user);
                NotificationManagerService.this.mZenModeHelper.onUserUnlocked(user);
            } else if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                OppoNotificationManager.getInstance().setShutdown(true);
                NotificationManagerService.this.updateNotificationPulse();
            }
        }
    };
    private final NotificationManagerInternal mInternalService = new NotificationManagerInternal() {
        public NotificationChannel getNotificationChannel(String pkg, int uid, String channelId) {
            return NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, uid, channelId, false);
        }

        public void enqueueNotification(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int userId) {
            NotificationManagerService.this.enqueueNotificationInternal(pkg, opPkg, callingUid, callingPid, tag, id, notification, userId);
        }

        public void removeForegroundServiceFlagFromNotification(final String pkg, final int notificationId, final int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mHandler.post(new Runnable() {
                public void run() {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        AnonymousClass8.this.removeForegroundServiceFlagByListLocked(NotificationManagerService.this.mEnqueuedNotifications, pkg, notificationId, userId);
                        AnonymousClass8.this.removeForegroundServiceFlagByListLocked(NotificationManagerService.this.mNotificationList, pkg, notificationId, userId);
                    }
                }
            });
        }

        @GuardedBy("mNotificationLock")
        private void removeForegroundServiceFlagByListLocked(ArrayList<NotificationRecord> notificationList, String pkg, int notificationId, int userId) {
            NotificationRecord r = NotificationManagerService.this.findNotificationByListLocked(notificationList, pkg, null, notificationId, userId);
            if (r != null) {
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

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "baoqibiao@ROM.SysApp.Notification, add for don't clear notification when bpm suspend", property = OppoRomType.ROM)
        public void cancelAllNotificationsFromBMP(String pkg, int userId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            NotificationManagerService.this.cancelAllNotificationsInt(Binder.getCallingUid(), Binder.getCallingPid(), pkg, null, 0, 0, true, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelAllNotifications", pkg), 21, null);
        }
    };
    private int mInterruptionFilter = 0;
    private boolean mIsTelevision;
    private long mLastOverRateLogTime;
    ArrayList<String> mLights = new ArrayList();
    private int mListenerHints;
    private NotificationListeners mListeners;
    private final SparseArray<ArraySet<ManagedServiceInfo>> mListenersDisablingEffects = new SparseArray();
    protected final BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                NotificationManagerService.this.mZenModeHelper.updateDefaultZenRules();
                NotificationManagerService.this.mRankingHelper.onLocaleChanged(context, ActivityManager.getCurrentUser());
            }
        }
    };
    private float mMaxPackageEnqueueRate = 5.0f;
    final NotificationDelegate mNotificationDelegate = new NotificationDelegate() {
        public void onSetDisabled(int status) {
            boolean z = false;
            synchronized (NotificationManagerService.this.mNotificationLock) {
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
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, userId, 3, null, true);
            }
        }

        public void onNotificationClick(int callingUid, int callingPid, String key) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r == null) {
                    Log.w(NotificationManagerService.TAG, "No notification with key: " + key);
                    return;
                }
                long now = System.currentTimeMillis();
                MetricsLogger.action(r.getLogMaker(now).setCategory(128).setType(4));
                EventLogTags.writeNotificationClicked(key, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                StatusBarNotification sbn = r.sbn;
                NotificationManagerService.this.cancelNotification(callingUid, callingPid, sbn.getPackageName(), sbn.getTag(), sbn.getId(), 16, 64, false, r.getUserId(), 1, null);
            }
        }

        public void onNotificationActionClick(int callingUid, int callingPid, String key, int actionIndex) {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r == null) {
                    Log.w(NotificationManagerService.TAG, "No notification with key: " + key);
                    return;
                }
                long now = System.currentTimeMillis();
                MetricsLogger.action(r.getLogMaker(now).setCategory(129).setType(4).setSubtype(actionIndex));
                EventLogTags.writeNotificationActionClicked(key, actionIndex, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
            }
        }

        public void onNotificationClear(int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 2, null);
        }

        public void onPanelRevealed(boolean clearEffects, int items) {
            MetricsLogger.visible(NotificationManagerService.this.getContext(), 127);
            MetricsLogger.histogram(NotificationManagerService.this.getContext(), "note_load", items);
            EventLogTags.writeNotificationPanelRevealed(items);
            if (clearEffects) {
                clearEffects();
            }
        }

        public void onPanelHidden() {
            MetricsLogger.hidden(NotificationManagerService.this.getContext(), 127);
            EventLogTags.writeNotificationPanelHidden();
        }

        public void clearEffects() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                if (NotificationManagerService.DBG) {
                    Slog.d(NotificationManagerService.TAG, "clearEffects");
                }
                NotificationManagerService.this.clearSoundLocked();
                NotificationManagerService.this.clearVibrateLocked();
            }
        }

        public void onNotificationError(int callingUid, int callingPid, String pkg, String tag, int id, int uid, int initialPid, String message, int userId) {
            Slog.d(NotificationManagerService.TAG, "onNotification error pkg=" + pkg + " tag=" + tag + " id=" + id + "; will crashApplication(uid=" + uid + ", pid=" + initialPid + ")");
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 0, false, userId, 4, null);
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityManager.getService().crashApplication(uid, initialPid, pkg, -1, "Bad notification posted from package " + pkg + ": " + message);
            } catch (RemoteException e) {
            }
            Binder.restoreCallingIdentity(ident);
        }

        public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) {
            int i = 0;
            synchronized (NotificationManagerService.this.mNotificationLock) {
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
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    int i2;
                    r.stats.onExpansionChanged(userAction, expanded);
                    long now = System.currentTimeMillis();
                    if (userAction) {
                        LogMaker category = r.getLogMaker(now).setCategory(128);
                        if (expanded) {
                            i2 = 3;
                        } else {
                            i2 = 14;
                        }
                        MetricsLogger.action(category.setType(i2));
                    }
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
    private Light mNotificationLight;
    @GuardedBy("mNotificationLock")
    final ArrayList<NotificationRecord> mNotificationList = new ArrayList();
    final Object mNotificationLock = new Object();
    private boolean mNotificationPulseEnabled;
    private final BroadcastReceiver mNotificationTimeoutReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && NotificationManagerService.ACTION_NOTIFICATION_TIMEOUT.equals(action)) {
                NotificationRecord record;
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    record = NotificationManagerService.this.findNotificationByKeyLocked(intent.getStringExtra(NotificationManagerService.EXTRA_KEY));
                }
                if (record != null) {
                    NotificationManagerService.this.cancelNotification(record.sbn.getUid(), record.sbn.getInitialPid(), record.sbn.getPackageName(), record.sbn.getTag(), record.sbn.getId(), 0, 64, true, record.getUserId(), 19, null);
                }
            }
        }
    };
    @GuardedBy("mNotificationLock")
    final ArrayMap<String, NotificationRecord> mNotificationsByKey = new ArrayMap();
    private final BroadcastReceiver mPackageIntentReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Missing block: B:16:0x004d, code:
            if (r14.equals("android.intent.action.PACKAGES_SUSPENDED") == false) goto L_0x01e1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                boolean removingPackage;
                String[] pkgList;
                String pkgName;
                int i;
                int queryRestart = 0;
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
                            if (queryRestart == 0) {
                                if (!action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                                }
                            }
                        }
                    }
                }
                int changeUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                int[] uidList = null;
                if (queryRemove) {
                    removingPackage = intent.getBooleanExtra("android.intent.extra.REPLACING", false) ^ 1;
                } else {
                    removingPackage = false;
                }
                if (NotificationManagerService.DBG) {
                    Slog.i(NotificationManagerService.TAG, "action=" + action + " removing=" + removingPackage);
                }
                if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    uidList = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    reason = 14;
                } else if (queryRestart != 0) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                    uidList = new int[1];
                    uidList[0] = intent.getIntExtra("android.intent.extra.UID", -1);
                } else {
                    Uri uri = intent.getData();
                    if (uri != null) {
                        pkgName = uri.getSchemeSpecificPart();
                        if (pkgName != null) {
                            if (packageChanged) {
                                try {
                                    IPackageManager -get23 = NotificationManagerService.this.mPackageManager;
                                    if (changeUserId != -1) {
                                        i = changeUserId;
                                    } else {
                                        i = 0;
                                    }
                                    int enabled = -get23.getApplicationEnabledSetting(pkgName, i);
                                    if (enabled == 1 || enabled == 0) {
                                        cancelNotifications = false;
                                    }
                                } catch (Throwable e) {
                                    if (NotificationManagerService.DBG) {
                                        Slog.i(NotificationManagerService.TAG, "Exception trying to look up app enabled setting", e);
                                    }
                                } catch (RemoteException e2) {
                                }
                            }
                            pkgList = new String[]{pkgName};
                            uidList = new int[1];
                            uidList[0] = intent.getIntExtra("android.intent.extra.UID", -1);
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
                                NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkgName, null, 0, 0, queryRestart ^ 1, changeUserId, 20, null);
                            } else {
                                NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkgName, null, 0, 0, queryRestart ^ 1, changeUserId, reason, null);
                            }
                        }
                        i = i2 + 1;
                    }
                }
                NotificationManagerService.this.mListeners.onPackagesChanged(removingPackage, pkgList, uidList);
                NotificationManagerService.this.mAssistants.onPackagesChanged(removingPackage, pkgList, uidList);
                NotificationManagerService.this.mConditionProviders.onPackagesChanged(removingPackage, pkgList, uidList);
                NotificationManagerService.this.mRankingHelper.onPackagesChanged(removingPackage, changeUserId, pkgList, uidList);
                NotificationManagerService.this.savePolicyFile();
            }
        }
    };
    private IPackageManager mPackageManager;
    private PackageManager mPackageManagerClient;
    private AtomicFile mPolicyFile;
    private RankingHandler mRankingHandler;
    private RankingHelper mRankingHelper;
    private final HandlerThread mRankingThread = new HandlerThread("ranker", 10);
    private final BroadcastReceiver mRestoreReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.os.action.SETTING_RESTORED".equals(intent.getAction())) {
                try {
                    String element = intent.getStringExtra("setting_name");
                    String newValue = intent.getStringExtra("new_value");
                    int restoredFromSdkInt = intent.getIntExtra("restored_from_sdk_int", 0);
                    NotificationManagerService.this.mListeners.onSettingRestored(element, newValue, restoredFromSdkInt, getSendingUserId());
                    NotificationManagerService.this.mConditionProviders.onSettingRestored(element, newValue, restoredFromSdkInt, getSendingUserId());
                } catch (Exception e) {
                    Slog.wtf(NotificationManagerService.TAG, "Cannot restore managed services from settings", e);
                }
            }
        }
    };
    private boolean mScreenOn = true;
    private final IBinder mService = new Stub() {
        /* JADX WARNING: Removed duplicated region for block: B:44:0x015f A:{Catch:{ all -> 0x0193 }} */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0107 A:{Catch:{ all -> 0x0193 }} */
        /* JADX WARNING: Removed duplicated region for block: B:34:0x0126 A:{Catch:{ all -> 0x0193 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void enqueueToast(String pkg, ITransientNotification callback, int duration) {
            if (NotificationManagerService.DBG) {
                Slog.i(NotificationManagerService.TAG, "enqueueToast pkg=" + pkg + " callback=" + callback + " duration=" + duration);
            }
            if (pkg == null || callback == null) {
                Slog.e(NotificationManagerService.TAG, "Not doing toast. pkg=" + pkg + " callback=" + callback);
                return;
            }
            boolean isSystemToast = !NotificationManagerService.this.isCallerSystemOrPhone() ? "android".equals(pkg) : true;
            boolean isPackageSuspended = NotificationManagerService.this.isPackageSuspendedForUser(pkg, Binder.getCallingUid());
            String pakageName = OppoNotificationManager.getInstance().getForegroundPackage();
            if (NotificationManagerService.this.getContext().getPackageManager().isFullFunctionMode() || OppoNotificationManager.getInstance().isSystemApp(pkg) || (pkg.equals(pakageName) ^ 1) == 0 || (NotificationManagerService.this.getContext().getPackageManager().hasSystemFeature("oppo.business.custom") && OppoBPMUtils.getInstance().getCustomizeAppList().contains(pkg))) {
                synchronized (NotificationManagerService.this.mToastQueue) {
                    int index;
                    int callingPid = Binder.getCallingPid();
                    long callingId = Binder.clearCallingIdentity();
                    if (!isSystemToast) {
                        try {
                            if ((ActivityThread.inCptWhiteList(CompatibilityHelper.FORCE_TOAST_USING_OLD_STYLE, pkg) ^ 1) != 0) {
                                index = NotificationManagerService.this.indexOfToastPackageLocked(pkg);
                                if (index < 0) {
                                    ToastRecord record = (ToastRecord) NotificationManagerService.this.mToastQueue.get(index);
                                    record.update(duration);
                                    record.update(callback);
                                } else {
                                    Binder token = new Binder();
                                    NotificationManagerService.this.mWindowManagerInternal.addWindowToken(token, 2005, 0);
                                    NotificationManagerService.this.mToastQueue.add(new ToastRecord(callingPid, pkg, callback, duration, token));
                                    index = NotificationManagerService.this.mToastQueue.size() - 1;
                                }
                                NotificationManagerService.this.keepProcessAliveIfNeededLocked(callingPid);
                                if (index == 0) {
                                    NotificationManagerService.this.showNextToastLocked();
                                }
                                Binder.restoreCallingIdentity(callingId);
                            }
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(callingId);
                        }
                    }
                    index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                    if (index < 0) {
                    }
                    NotificationManagerService.this.keepProcessAliveIfNeededLocked(callingPid);
                    if (index == 0) {
                    }
                    Binder.restoreCallingIdentity(callingId);
                }
                return;
            }
            Slog.d(NotificationManagerService.TAG, "toast app is not on top, we discard it: " + pkg);
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
                        NotificationManagerService.this.cancelToastLocked(index);
                    } else {
                        Slog.w(NotificationManagerService.TAG, "Toast already cancelled. pkg=" + pkg + " callback=" + callback);
                    }
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }

        public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId) throws RemoteException {
            NotificationManagerService.this.enqueueNotificationInternal(pkg, opPkg, Binder.getCallingUid(), Binder.getCallingPid(), tag, id, notification, userId);
        }

        public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) {
            int mustNotHaveFlags;
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelNotificationWithTag", pkg);
            if (NotificationManagerService.this.isCallingUidSystem()) {
                mustNotHaveFlags = 0;
            } else {
                mustNotHaveFlags = 1088;
            }
            NotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, mustNotHaveFlags, false, userId, 8, null);
        }

        public void cancelAllNotifications(String pkg, int userId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            NotificationManagerService.this.cancelAllNotificationsInt(Binder.getCallingUid(), Binder.getCallingPid(), pkg, null, 0, 64, true, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelAllNotifications", pkg), 9, null);
        }

        public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mRankingHelper.setEnabled(pkg, uid, enabled);
            if (OppoMultiAppManagerUtil.getInstance().isMultiApp(pkg)) {
                try {
                    NotificationManagerService.this.mRankingHelper.setEnabled(pkg, NotificationManagerService.this.getContext().getPackageManager().getPackageUidAsUser(pkg, OppoMultiAppManager.USER_ID), enabled);
                } catch (NameNotFoundException e) {
                    Log.e(NotificationManagerService.TAG, "setNotificationsEnabledForPackage mRankingHelper.setEnabled NameNotFoundException");
                }
            }
            if (!enabled) {
                NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, null, 0, 0, true, UserHandle.getUserId(uid), 7, null);
                if (OppoMultiAppManagerUtil.getInstance().isMultiApp(pkg)) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, null, 0, 0, true, OppoMultiAppManager.USER_ID, 7, null);
                }
            }
            NotificationManagerService.this.savePolicyFile();
        }

        public boolean areNotificationsEnabled(String pkg) {
            return areNotificationsEnabledForPackage(pkg, Binder.getCallingUid());
        }

        public boolean areNotificationsEnabledForPackage(String pkg, int uid) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            if (NotificationManagerService.this.mRankingHelper.getImportance(pkg, uid) != 0) {
                return true;
            }
            return false;
        }

        public int getPackageImportance(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getImportance(pkg, Binder.getCallingUid());
        }

        public boolean canShowBadge(String pkg, int uid) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.canShowBadge(pkg, uid);
        }

        public void setShowBadge(String pkg, int uid, boolean showBadge) {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.mRankingHelper.setShowBadge(pkg, uid, showBadge);
            NotificationManagerService.this.savePolicyFile();
        }

        public void createNotificationChannelGroups(String pkg, ParceledListSlice channelGroupList) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            List<NotificationChannelGroup> groups = channelGroupList.getList();
            int groupSize = groups.size();
            for (int i = 0; i < groupSize; i++) {
                NotificationChannelGroup group = (NotificationChannelGroup) groups.get(i);
                Preconditions.checkNotNull(group, "group in list is null");
                NotificationManagerService.this.mRankingHelper.createNotificationChannelGroup(pkg, Binder.getCallingUid(), group, true);
                NotificationManagerService.this.mListeners.notifyNotificationChannelGroupChanged(pkg, UserHandle.of(UserHandle.getCallingUserId()), group, 1);
            }
            NotificationManagerService.this.savePolicyFile();
        }

        private void createNotificationChannelsImpl(String pkg, int uid, ParceledListSlice channelsList) {
            List<NotificationChannel> channels = channelsList.getList();
            int channelsSize = channels.size();
            if (!OppoNotificationManager.getInstance().shouldLimitChannels(NotificationManagerService.this.mRankingHelper, pkg, uid, channelsSize)) {
                for (int i = 0; i < channelsSize; i++) {
                    NotificationChannel channel = (NotificationChannel) channels.get(i);
                    Preconditions.checkNotNull(channel, "channel in list is null");
                    NotificationManagerService.this.mRankingHelper.createNotificationChannel(pkg, uid, channel, true);
                    NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(uid), NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, uid, channel.getId(), false), 1);
                }
                NotificationManagerService.this.savePolicyFile();
            }
        }

        public void createNotificationChannels(String pkg, ParceledListSlice channelsList) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            createNotificationChannelsImpl(pkg, Binder.getCallingUid(), channelsList);
        }

        public void createNotificationChannelsForPackage(String pkg, int uid, ParceledListSlice channelsList) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystem();
            createNotificationChannelsImpl(pkg, uid, channelsList);
        }

        public NotificationChannel getNotificationChannel(String pkg, String channelId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, Binder.getCallingUid(), channelId, false);
        }

        public NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, uid, channelId, includeDeleted);
        }

        public void deleteNotificationChannel(String pkg, String channelId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            int callingUid = Binder.getCallingUid();
            if ("miscellaneous".equals(channelId)) {
                throw new IllegalArgumentException("Cannot delete default channel");
            }
            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, channelId, 0, 0, true, UserHandle.getUserId(callingUid), 17, null);
            NotificationManagerService.this.mRankingHelper.deleteNotificationChannel(pkg, callingUid, channelId);
            NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(callingUid), NotificationManagerService.this.mRankingHelper.getNotificationChannel(pkg, callingUid, channelId, true), 3);
            NotificationManagerService.this.savePolicyFile();
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroups(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return new ParceledListSlice(new ArrayList(NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, Binder.getCallingUid())));
        }

        public void deleteNotificationChannelGroup(String pkg, String groupId) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            int callingUid = Binder.getCallingUid();
            NotificationChannelGroup groupToDelete = NotificationManagerService.this.mRankingHelper.getNotificationChannelGroup(groupId, pkg, callingUid);
            if (groupToDelete != null) {
                List<NotificationChannel> deletedChannels = NotificationManagerService.this.mRankingHelper.deleteNotificationChannelGroup(pkg, callingUid, groupId);
                for (int i = 0; i < deletedChannels.size(); i++) {
                    NotificationChannel deletedChannel = (NotificationChannel) deletedChannels.get(i);
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkg, deletedChannel.getId(), 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
                    NotificationManagerService.this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(callingUid), deletedChannel, 3);
                }
                NotificationManagerService.this.mListeners.notifyNotificationChannelGroupChanged(pkg, UserHandle.getUserHandleForUid(callingUid), groupToDelete, 3);
                NotificationManagerService.this.savePolicyFile();
            }
        }

        public void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel) {
            enforceSystemOrSystemUI("Caller not system or systemui");
            Preconditions.checkNotNull(channel);
            NotificationManagerService.this.updateNotificationChannelInt(pkg, uid, channel, false);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) {
            enforceSystemOrSystemUI("getNotificationChannelsForPackage");
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, uid, includeDeleted);
        }

        public int getNumNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) {
            enforceSystemOrSystemUI("getNumNotificationChannelsForPackage");
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, uid, includeDeleted).getList().size();
        }

        public boolean onlyHasDefaultChannel(String pkg, int uid) {
            enforceSystemOrSystemUI("onlyHasDefaultChannel");
            return NotificationManagerService.this.mRankingHelper.onlyHasDefaultChannel(pkg, uid);
        }

        public int getDeletedChannelCount(String pkg, int uid) {
            enforceSystemOrSystemUI("getDeletedChannelCount");
            return NotificationManagerService.this.mRankingHelper.getDeletedChannelCount(pkg, uid);
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, uid, includeDeleted);
        }

        public NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid) {
            enforceSystemOrSystemUI("getNotificationChannelGroupForPackage");
            return NotificationManagerService.this.mRankingHelper.getNotificationChannelGroup(groupId, pkg, uid);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannels(String pkg) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, Binder.getCallingUid(), false);
        }

        public void clearData(String packageName, int uid, boolean fromApp) throws RemoteException {
            NotificationManagerService.this.checkCallerIsSystem();
            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, packageName, null, 0, 0, true, UserHandle.getUserId(Binder.getCallingUid()), 17, null);
            String[] packages = new String[]{packageName};
            int[] uids = new int[]{uid};
            NotificationManagerService.this.mListeners.onPackagesChanged(true, packages, uids);
            NotificationManagerService.this.mAssistants.onPackagesChanged(true, packages, uids);
            NotificationManagerService.this.mConditionProviders.onPackagesChanged(true, packages, uids);
            if (!fromApp) {
                NotificationManagerService.this.mRankingHelper.onPackagesChanged(true, UserHandle.getCallingUserId(), packages, uids);
            }
            NotificationManagerService.this.savePolicyFile();
        }

        public StatusBarNotification[] getActiveNotifications(String callingPkg) {
            NotificationManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.ACCESS_NOTIFICATIONS", "NotificationManagerService.getActiveNotifications");
            StatusBarNotification[] tmp = null;
            if (NotificationManagerService.this.mAppOps.noteOpNoThrow(25, Binder.getCallingUid(), callingPkg) == 0) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
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
            ParceledListSlice<StatusBarNotification> parceledListSlice;
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            int userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), incomingUserId, true, false, "getAppActiveNotifications", pkg);
            synchronized (NotificationManagerService.this.mNotificationLock) {
                int i;
                StatusBarNotification sbn;
                ArrayMap<String, StatusBarNotification> map = new ArrayMap(NotificationManagerService.this.mNotificationList.size() + NotificationManagerService.this.mEnqueuedNotifications.size());
                int N = NotificationManagerService.this.mNotificationList.size();
                for (i = 0; i < N; i++) {
                    sbn = sanitizeSbn(pkg, userId, ((NotificationRecord) NotificationManagerService.this.mNotificationList.get(i)).sbn);
                    if (sbn != null) {
                        map.put(sbn.getKey(), sbn);
                    }
                }
                for (NotificationRecord snoozed : NotificationManagerService.this.mSnoozeHelper.getSnoozed(userId, pkg)) {
                    sbn = sanitizeSbn(pkg, userId, snoozed.sbn);
                    if (sbn != null) {
                        map.put(sbn.getKey(), sbn);
                    }
                }
                int M = NotificationManagerService.this.mEnqueuedNotifications.size();
                for (i = 0; i < M; i++) {
                    sbn = sanitizeSbn(pkg, userId, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).sbn);
                    if (sbn != null) {
                        map.put(sbn.getKey(), sbn);
                    }
                }
                ArrayList<StatusBarNotification> list = new ArrayList(map.size());
                list.addAll(map.values());
                parceledListSlice = new ParceledListSlice(list);
            }
            return parceledListSlice;
        }

        private StatusBarNotification sanitizeSbn(String pkg, int userId, StatusBarNotification sbn) {
            if (sbn.getPackageName().equals(pkg) && sbn.getUserId() == userId) {
                return new StatusBarNotification(sbn.getPackageName(), sbn.getOpPkg(), sbn.getId(), sbn.getTag(), sbn.getUid(), sbn.getInitialPid(), sbn.getNotification().clone(), sbn.getUser(), sbn.getOverrideGroupKey(), sbn.getPostTime());
            }
            return null;
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
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        for (Object obj : keys) {
                            NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(obj);
                            if (r != null) {
                                int userId = r.sbn.getUserId();
                                if (userId == OppoMultiAppManager.USER_ID || userId == info.userid || userId == -1 || (NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId) ^ 1) == 0) {
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
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(component.getPackageName());
            long identity = Binder.clearCallingIdentity();
            try {
                ManagedServices manager;
                if (NotificationManagerService.this.mAssistants.isComponentEnabledForCurrentProfiles(component)) {
                    manager = NotificationManagerService.this.mAssistants;
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
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    info.getOwner().setComponentState(info.component, false);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationsShownFromListener(INotificationListener token, String[] keys) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        int N = keys.length;
                        for (int i = 0; i < N; i++) {
                            NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                            if (r != null) {
                                int userId = r.sbn.getUserId();
                                if (userId != info.userid && userId != -1 && (NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId) ^ 1) != 0) {
                                    throw new SecurityException("Disallowed call from listener: " + info.service);
                                } else if (!r.isSeen()) {
                                    if (NotificationManagerService.DBG) {
                                        Slog.d(NotificationManagerService.TAG, "Marking notification as seen " + keys[i]);
                                    }
                                    UsageStatsManagerInternal -get6 = NotificationManagerService.this.mAppUsageStats;
                                    String packageName = r.sbn.getPackageName();
                                    if (userId == -1) {
                                        userId = 0;
                                    }
                                    -get6.reportEvent(packageName, userId, 7);
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

        @GuardedBy("mNotificationLock")
        private void cancelNotificationFromListenerLocked(ManagedServiceInfo info, int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 10, info);
        }

        public void snoozeNotificationUntilContextFromListener(INotificationListener token, String key, String snoozeCriterionId) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.snoozeNotificationInt(key, -1, snoozeCriterionId, NotificationManagerService.this.mListeners.checkServiceTokenLocked(token));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void snoozeNotificationUntilFromListener(INotificationListener token, String key, long duration) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.snoozeNotificationInt(key, duration, null, NotificationManagerService.this.mListeners.checkServiceTokenLocked(token));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void unsnoozeNotificationFromAssistant(INotificationListener token, String key) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.unsnoozeNotificationInt(key, NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
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
            synchronized (NotificationManagerService.this.mNotificationLock) {
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

        public ParceledListSlice<StatusBarNotification> getSnoozedNotificationsFromListener(INotificationListener token, int trim) {
            ParceledListSlice<StatusBarNotification> parceledListSlice;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                List<NotificationRecord> snoozedRecords = NotificationManagerService.this.mSnoozeHelper.getSnoozed();
                int N = snoozedRecords.size();
                ArrayList<StatusBarNotification> list = new ArrayList(N);
                for (int i = 0; i < N; i++) {
                    NotificationRecord r = (NotificationRecord) snoozedRecords.get(i);
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
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if ((hints & 7) != 0) {
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
            int -get18;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                -get18 = NotificationManagerService.this.mListenerHints;
            }
            return -get18;
        }

        public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mZenModeHelper.requestFromListener(NotificationManagerService.this.mListeners.checkServiceTokenLocked(token).component, interruptionFilter);
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
            int -get17;
            synchronized (NotificationManagerService.this.mNotificationLight) {
                -get17 = NotificationManagerService.this.mInterruptionFilter;
            }
            return -get17;
        }

        public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
            synchronized (NotificationManagerService.this.mNotificationLock) {
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
            enforceSystemOrSystemUI("INotificationManager.getZenModeConfig");
            return NotificationManagerService.this.mZenModeHelper.getConfig();
        }

        public void setZenMode(int mode, Uri conditionId, String reason) throws RemoteException {
            enforceSystemOrSystemUI("INotificationManager.setZenMode");
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
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            NotificationManagerService.this.mHandler.post(new Runnable() {
                public void run() {
                    NotificationManagerService.this.mConditionProviders.notifyConditions(pkg, info, conditions);
                }
            });
        }

        public void requestUnbindProvider(IConditionProvider provider) {
            long identity = Binder.clearCallingIdentity();
            try {
                ManagedServiceInfo info = NotificationManagerService.this.mConditionProviders.checkServiceToken(provider);
                info.getOwner().setComponentState(info.component, false);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void requestBindProvider(ComponentName component) {
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(component.getPackageName());
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.mConditionProviders.setComponentState(component, true);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private void enforceSystemOrSystemUI(String message) {
            if (!NotificationManagerService.this.isCallerSystemOrPhone()) {
                NotificationManagerService.this.getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", message);
            }
        }

        private void enforceSystemOrSystemUIOrSamePackage(String pkg, String message) {
            try {
                NotificationManagerService.this.checkCallerIsSystemOrSameApp(pkg);
            } catch (SecurityException e) {
                NotificationManagerService.this.getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", message);
            }
        }

        private void enforcePolicyAccess(int uid, String method) {
            if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") != 0) {
                boolean accessAllowed = false;
                for (String isPackageOrComponentAllowed : NotificationManagerService.this.getContext().getPackageManager().getPackagesForUid(uid)) {
                    if (NotificationManagerService.this.mConditionProviders.isPackageOrComponentAllowed(isPackageOrComponentAllowed, UserHandle.getUserId(uid))) {
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
                NotificationManagerService.this.checkCallerIsSameApp(pkg);
                if (!checkPolicyAccess(pkg)) {
                    Slog.w(NotificationManagerService.TAG, "Notification policy access denied calling " + method);
                    throw new SecurityException("Notification policy access denied");
                }
            }
        }

        private boolean checkPackagePolicyAccess(String pkg) {
            return NotificationManagerService.this.mConditionProviders.isPackageOrComponentAllowed(pkg, AnonymousClass7.getCallingUserHandle().getIdentifier());
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
            if (DumpUtils.checkDumpAndUsageStatsPermission(NotificationManagerService.this.getContext(), NotificationManagerService.TAG, pw) && !OppoNotificationManager.getInstance().dumpOppoNotificationInfo(fd, pw, args)) {
                DumpFilter filter = DumpFilter.parseFromArguments(args);
                if (filter != null && filter.stats) {
                    NotificationManagerService.this.dumpJson(pw, filter);
                } else if (filter == null || !filter.proto) {
                    NotificationManagerService.this.dumpImpl(pw, filter);
                } else {
                    NotificationManagerService.this.dumpProto(fd, filter);
                }
            }
        }

        public ComponentName getEffectsSuppressor() {
            return !NotificationManagerService.this.mEffectsSuppressors.isEmpty() ? (ComponentName) NotificationManagerService.this.mEffectsSuppressors.get(0) : null;
        }

        public boolean matchesCallFilter(Bundle extras) {
            enforceSystemOrSystemUI("INotificationManager.matchesCallFilter");
            return NotificationManagerService.this.mZenModeHelper.matchesCallFilter(Binder.getCallingUserHandle(), extras, (ValidateNotificationPeople) NotificationManagerService.this.mRankingHelper.findExtractor(ValidateNotificationPeople.class), 3000, 1.0f);
        }

        public boolean isSystemConditionProviderEnabled(String path) {
            enforceSystemOrSystemUI("INotificationManager.isSystemConditionProviderEnabled");
            return NotificationManagerService.this.mConditionProviders.isSystemProviderEnabled(path);
        }

        public byte[] getBackupPayload(int user) {
            if (NotificationManagerService.DBG) {
                Slog.d(NotificationManagerService.TAG, "getBackupPayload u=" + user);
            }
            if (user != 0) {
                Slog.w(NotificationManagerService.TAG, "getBackupPayload: cannot backup policy for user " + user);
                return null;
            }
            byte[] toByteArray;
            synchronized (NotificationManagerService.this.mPolicyFile) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    NotificationManagerService.this.writePolicyXml(baos, true);
                    toByteArray = baos.toByteArray();
                } catch (IOException e) {
                    Slog.w(NotificationManagerService.TAG, "getBackupPayload: error writing payload for user " + user, e);
                    return null;
                }
            }
            return toByteArray;
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x0086 A:{Splitter: B:16:0x0079, ExcHandler: java.lang.NumberFormatException (r1_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0086 A:{Splitter: B:16:0x0079, ExcHandler: java.lang.NumberFormatException (r1_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:20:0x0086, code:
            r1 = move-exception;
     */
        /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.w(com.android.server.notification.NotificationManagerService.TAG, "applyRestore: error reading payload", r1);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void applyRestore(byte[] payload, int user) {
            String str = null;
            if (NotificationManagerService.DBG) {
                String str2 = NotificationManagerService.TAG;
                StringBuilder append = new StringBuilder().append("applyRestore u=").append(user).append(" payload=");
                if (payload != null) {
                    str = new String(payload, StandardCharsets.UTF_8);
                }
                Slog.d(str2, append.append(str).toString());
            }
            if (payload == null) {
                Slog.w(NotificationManagerService.TAG, "applyRestore: no payload to restore for user " + user);
            } else if (user != 0) {
                Slog.w(NotificationManagerService.TAG, "applyRestore: cannot restore policy for user " + user);
            } else {
                synchronized (NotificationManagerService.this.mPolicyFile) {
                    try {
                        NotificationManagerService.this.readPolicyXml(new ByteArrayInputStream(payload), true);
                        NotificationManagerService.this.savePolicyFile();
                    } catch (Exception e) {
                    }
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

        public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
            setNotificationPolicyAccessGrantedForUser(pkg, AnonymousClass7.getCallingUserHandle().getIdentifier(), granted);
        }

        public void setNotificationPolicyAccessGrantedForUser(String pkg, int userId, boolean granted) {
            NotificationManagerService.this.checkCallerIsSystemOrShell();
            long identity = Binder.clearCallingIdentity();
            try {
                if (!NotificationManagerService.this.mActivityManager.isLowRamDevice()) {
                    NotificationManagerService.this.mConditionProviders.setPackageOrComponentEnabled(pkg, userId, true, granted);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(pkg).addFlags(1073741824), UserHandle.of(userId), null);
                    NotificationManagerService.this.savePolicyFile();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
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

        public boolean shouldKeepAlive(String pkg, int userId) {
            return OppoNotificationManager.getInstance().shouldKeepAlive(pkg, userId);
        }

        public int getNavigationMode(String pkg, int userId) {
            return OppoNotificationManager.getInstance().getNavigationMode(pkg, userId);
        }

        public boolean isDriveNavigationMode(String pkg, int userId) {
            return OppoNotificationManager.getInstance().isDriveNavigationMode(pkg, userId);
        }

        public boolean isNavigationMode(int userId) {
            return OppoNotificationManager.getInstance().isNavigationMode(userId);
        }

        public String[] getEnableNavigationApps(int userId) {
            return OppoNotificationManager.getInstance().getEnableNavigationApps(userId);
        }

        public boolean isSuppressedByDriveMode(int userId) {
            return OppoNotificationManager.getInstance().isSuppressedByDriveMode(userId);
        }

        public void setSuppressedByDriveMode(boolean mode, int userId) {
            OppoNotificationManager.getInstance().setSuppressedByDriveMode(mode, userId);
        }

        public boolean shouldInterceptSound(String pkg, int uid) {
            return OppoNotificationManager.getInstance().shouldInterceptSound(NotificationManagerService.this.mRankingHelper, NotificationManagerService.this.mZenModeHelper, pkg, uid);
        }

        public List<String> getEnabledNotificationListenerPackages() {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mListeners.getAllowedPackages(AnonymousClass7.getCallingUserHandle().getIdentifier());
        }

        public List<ComponentName> getEnabledNotificationListeners(int userId) {
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mListeners.getAllowedComponents(userId);
        }

        public boolean isNotificationListenerAccessGranted(ComponentName listener) {
            Preconditions.checkNotNull(listener);
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(listener.getPackageName());
            return NotificationManagerService.this.mListeners.isPackageOrComponentAllowed(listener.flattenToString(), AnonymousClass7.getCallingUserHandle().getIdentifier());
        }

        public boolean isNotificationListenerAccessGrantedForUser(ComponentName listener, int userId) {
            Preconditions.checkNotNull(listener);
            NotificationManagerService.this.checkCallerIsSystem();
            return NotificationManagerService.this.mListeners.isPackageOrComponentAllowed(listener.flattenToString(), userId);
        }

        public boolean isNotificationAssistantAccessGranted(ComponentName assistant) {
            Preconditions.checkNotNull(assistant);
            NotificationManagerService.this.checkCallerIsSystemOrSameApp(assistant.getPackageName());
            return NotificationManagerService.this.mAssistants.isPackageOrComponentAllowed(assistant.flattenToString(), AnonymousClass7.getCallingUserHandle().getIdentifier());
        }

        public void setNotificationListenerAccessGranted(ComponentName listener, boolean granted) throws RemoteException {
            setNotificationListenerAccessGrantedForUser(listener, AnonymousClass7.getCallingUserHandle().getIdentifier(), granted);
        }

        public void setNotificationAssistantAccessGranted(ComponentName assistant, boolean granted) throws RemoteException {
            setNotificationAssistantAccessGrantedForUser(assistant, AnonymousClass7.getCallingUserHandle().getIdentifier(), granted);
        }

        public void setNotificationListenerAccessGrantedForUser(ComponentName listener, int userId, boolean granted) throws RemoteException {
            Preconditions.checkNotNull(listener);
            NotificationManagerService.this.checkCallerIsSystemOrShell();
            long identity = Binder.clearCallingIdentity();
            try {
                if (!NotificationManagerService.this.mActivityManager.isLowRamDevice()) {
                    NotificationManagerService.this.mConditionProviders.setPackageOrComponentEnabled(listener.flattenToString(), userId, false, granted);
                    NotificationManagerService.this.mListeners.setPackageOrComponentEnabled(listener.flattenToString(), userId, true, granted);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(listener.getPackageName()).addFlags(1073741824), UserHandle.of(userId), null);
                    NotificationManagerService.this.savePolicyFile();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationAssistantAccessGrantedForUser(ComponentName assistant, int userId, boolean granted) throws RemoteException {
            Preconditions.checkNotNull(assistant);
            NotificationManagerService.this.checkCallerIsSystemOrShell();
            long identity = Binder.clearCallingIdentity();
            try {
                if (!NotificationManagerService.this.mActivityManager.isLowRamDevice()) {
                    NotificationManagerService.this.mConditionProviders.setPackageOrComponentEnabled(assistant.flattenToString(), userId, false, granted);
                    NotificationManagerService.this.mAssistants.setPackageOrComponentEnabled(assistant.flattenToString(), userId, true, granted);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(assistant.getPackageName()).addFlags(1073741824), UserHandle.of(userId), null);
                    NotificationManagerService.this.savePolicyFile();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyEnqueuedAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token);
                    int N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    for (int i = 0; i < N; i++) {
                        NotificationRecord n = (NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i);
                        if (Objects.equals(adjustment.getKey(), n.getKey()) && Objects.equals(Integer.valueOf(adjustment.getUser()), Integer.valueOf(n.getUserId()))) {
                            NotificationManagerService.this.applyAdjustment(n, adjustment);
                            break;
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token);
                    NotificationManagerService.this.applyAdjustment((NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(adjustment.getKey()), adjustment);
                }
                NotificationManagerService.this.mRankingHandler.requestSort();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyAdjustmentsFromAssistant(INotificationListener token, List<Adjustment> adjustments) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.mAssistants.checkServiceTokenLocked(token);
                    for (Adjustment adjustment : adjustments) {
                        NotificationManagerService.this.applyAdjustment((NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(adjustment.getKey()), adjustment);
                    }
                }
                NotificationManagerService.this.mRankingHandler.requestSort();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void updateNotificationChannelFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannel channel) throws RemoteException {
            Preconditions.checkNotNull(channel);
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user);
            NotificationManagerService.this.updateNotificationChannelInt(pkg, getUidForPackageAndUser(pkg, user), channel, true);
        }

        public ParceledListSlice<NotificationChannel> getNotificationChannelsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user);
            return NotificationManagerService.this.mRankingHelper.getNotificationChannels(pkg, getUidForPackageAndUser(pkg, user), false);
        }

        public ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user) throws RemoteException {
            Preconditions.checkNotNull(pkg);
            Preconditions.checkNotNull(user);
            verifyPrivilegedListener(token, user);
            List<NotificationChannelGroup> groups = new ArrayList();
            groups.addAll(NotificationManagerService.this.mRankingHelper.getNotificationChannelGroups(pkg, getUidForPackageAndUser(pkg, user)));
            return new ParceledListSlice(groups);
        }

        private void verifyPrivilegedListener(INotificationListener token, UserHandle user) {
            ManagedServiceInfo info;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
            }
            if (!NotificationManagerService.this.hasCompanionDevice(info)) {
                throw new SecurityException(info + " does not have access");
            } else if (!info.enabledAndUserMatches(user.getIdentifier())) {
                throw new SecurityException(info + " does not have access");
            }
        }

        private int getUidForPackageAndUser(String pkg, UserHandle user) throws RemoteException {
            int uid = 0;
            long identity = Binder.clearCallingIdentity();
            try {
                uid = NotificationManagerService.this.mPackageManager.getPackageUid(pkg, 0, user.getIdentifier());
                return uid;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
            new ShellCmd(NotificationManagerService.this, null).exec(this, in, out, err, args, callback, resultReceiver);
        }
    };
    private SettingsObserver mSettingsObserver;
    private SnoozeHelper mSnoozeHelper;
    private String mSoundNotificationKey;
    StatusBarManagerInternal mStatusBar;
    final ArrayMap<String, NotificationRecord> mSummaryByGroupKey = new ArrayMap();
    boolean mSystemReady;
    final ArrayList<ToastRecord> mToastQueue = new ArrayList();
    private NotificationUsageStats mUsageStats;
    private boolean mUseAttentionLight;
    private final UserProfiles mUserProfiles = new UserProfiles();
    private String mVibrateNotificationKey;
    Vibrator mVibrator;
    private WindowManagerInternal mWindowManagerInternal;
    protected ZenModeHelper mZenModeHelper;

    private interface FlagChecker {
        boolean apply(int i);
    }

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
        public boolean proto = false;
        public boolean redact = true;
        public long since;
        public boolean stats;
        public boolean zen;

        public static DumpFilter parseFromArguments(String[] args) {
            DumpFilter filter = new DumpFilter();
            int ai = 0;
            while (ai < args.length) {
                String a = args[ai];
                if ("--proto".equals(args[0])) {
                    filter.proto = true;
                }
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
                        filter.since = Long.parseLong(args[ai]);
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

    protected class EnqueueNotificationRunnable implements Runnable {
        private final NotificationRecord r;
        private final int userId;

        EnqueueNotificationRunnable(int userId, NotificationRecord r) {
            this.userId = userId;
            this.r = r;
        }

        public void run() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationManagerService.this.mEnqueuedNotifications.add(this.r);
                NotificationManagerService.this.scheduleTimeoutLocked(this.r);
                StatusBarNotification n = this.r.sbn;
                if (NotificationManagerService.DBG) {
                    Slog.d(NotificationManagerService.TAG, "EnqueueNotificationRunnable.run for: " + n.getKey());
                }
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
                NotificationManagerService.this.handleGroupedNotificationLocked(this.r, old, callingUid, callingPid);
                if (n.isGroup() && notification.isGroupChild()) {
                    NotificationManagerService.this.mSnoozeHelper.repostGroupSummary(pkg, this.r.getUserId(), n.getGroupKey());
                }
                if (!pkg.equals("com.android.providers.downloads") || Log.isLoggable("DownloadManager", 2)) {
                    int enqueueStatus = 0;
                    if (old != null) {
                        enqueueStatus = 1;
                    }
                    EventLogTags.writeNotificationEnqueue(callingUid, callingPid, pkg, id, tag, this.userId, notification.toString(), enqueueStatus);
                }
                NotificationManagerService.this.mRankingHelper.extractSignals(this.r);
                if (NotificationManagerService.this.mAssistants.isEnabled()) {
                    NotificationManagerService.this.mAssistants.onNotificationEnqueued(this.r);
                    NotificationManagerService.this.mHandler.postDelayed(new PostNotificationRunnable(this.r.getKey()), NotificationManagerService.DELAY_FOR_ASSISTANT_TIME);
                } else {
                    NotificationManagerService.this.mHandler.post(new PostNotificationRunnable(this.r.getKey()));
                }
            }
        }
    }

    public class NotificationAssistants extends ManagedServices {
        static final String TAG_ENABLED_NOTIFICATION_ASSISTANTS = "enabled_assistants";

        public NotificationAssistants(IPackageManager pm) {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mNotificationLock, NotificationManagerService.this.mUserProfiles, pm);
        }

        protected Config getConfig() {
            Config c = new Config();
            c.caption = "notification assistant service";
            c.serviceInterface = "android.service.notification.NotificationAssistantService";
            c.xmlTag = TAG_ENABLED_NOTIFICATION_ASSISTANTS;
            c.secureSettingName = "enabled_notification_assistant";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_ASSISTANT_SERVICE";
            c.settingsAction = "android.settings.MANAGE_DEFAULT_APPS_SETTINGS";
            c.clientLabel = 17040395;
            return c;
        }

        protected IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        protected boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        protected void onServiceAdded(ManagedServiceInfo info) {
            NotificationManagerService.this.mListeners.registerGuestService(info);
        }

        @GuardedBy("mNotificationLock")
        protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
            NotificationManagerService.this.mListeners.unregisterService(removed.service, removed.userid);
        }

        public void onNotificationEnqueued(NotificationRecord r) {
            StatusBarNotification sbn = r.sbn;
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : getServices()) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    int importance = r.getImportance();
                    boolean fromUser = r.isImportanceFromUser();
                    final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationAssistants.this.notifyEnqueued(info, sbnToPost);
                        }
                    });
                }
            }
        }

        private void notifyEnqueued(ManagedServiceInfo info, StatusBarNotification sbn) {
            INotificationListener assistant = info.service;
            try {
                assistant.onNotificationEnqueued(new StatusBarNotificationHolder(sbn));
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify assistant (enqueued): " + assistant, ex);
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyAssistantSnoozedLocked(StatusBarNotification sbn, final String snoozeCriterionId) {
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : getServices()) {
                final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                NotificationManagerService.this.mHandler.post(new Runnable() {
                    public void run() {
                        INotificationListener assistant = info.service;
                        try {
                            assistant.onNotificationSnoozedUntilContext(new StatusBarNotificationHolder(sbnToPost), snoozeCriterionId);
                        } catch (RemoteException ex) {
                            Log.e(NotificationAssistants.this.TAG, "unable to notify assistant (snoozed): " + assistant, ex);
                        }
                    }
                });
            }
        }

        public boolean isEnabled() {
            return getServices().isEmpty() ^ 1;
        }
    }

    public class NotificationListeners extends ManagedServices {
        static final String TAG_ENABLED_NOTIFICATION_LISTENERS = "enabled_listeners";
        private final ArraySet<ManagedServiceInfo> mLightTrimListeners = new ArraySet();

        public NotificationListeners(IPackageManager pm) {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mNotificationLock, NotificationManagerService.this.mUserProfiles, pm);
        }

        protected Config getConfig() {
            Config c = new Config();
            c.caption = "notification listener";
            c.serviceInterface = "android.service.notification.NotificationListenerService";
            c.xmlTag = TAG_ENABLED_NOTIFICATION_LISTENERS;
            c.secureSettingName = "enabled_notification_listeners";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
            c.settingsAction = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
            c.clientLabel = 17040393;
            return c;
        }

        protected IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        protected boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        public void onServiceAdded(ManagedServiceInfo info) {
            NotificationRankingUpdate update;
            INotificationListener listener = info.service;
            synchronized (NotificationManagerService.this.mNotificationLock) {
                update = NotificationManagerService.this.makeRankingUpdateLocked(info);
            }
            try {
                listener.onListenerConnected(update);
            } catch (RemoteException e) {
            }
        }

        @GuardedBy("mNotificationLock")
        protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
            if (NotificationManagerService.this.removeDisabledHints(removed)) {
                NotificationManagerService.this.updateListenerHintsLocked();
                NotificationManagerService.this.updateEffectsSuppressorLocked();
            }
            this.mLightTrimListeners.remove(removed);
        }

        @GuardedBy("mNotificationLock")
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

        @GuardedBy("mNotificationLock")
        public void notifyPostedLocked(StatusBarNotification sbn, StatusBarNotification oldSbn) {
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : getServices()) {
                boolean sbnVisible = NotificationManagerService.this.isVisibleToListener(sbn, info);
                boolean oldSbnVisible = oldSbn != null ? NotificationManagerService.this.isVisibleToListener(oldSbn, info) : false;
                if (oldSbnVisible || (sbnVisible ^ 1) == 0) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    if (!oldSbnVisible || (sbnVisible ^ 1) == 0) {
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
                                NotificationListeners.this.notifyRemoved(info, oldSbnLightClone, update, 6);
                            }
                        });
                    }
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyRemovedLocked(StatusBarNotification sbn, int reason) {
            final StatusBarNotification sbnLight = sbn.cloneLight();
            for (final ManagedServiceInfo info : getServices()) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    final int i = reason;
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyRemoved(info, sbnLight, update, i);
                        }
                    });
                } else if (NotificationManagerService.DEBUG_PANIC) {
                    Log.w(this.TAG, "removing sbn:" + sbn + " is not visible to" + info);
                }
            }
        }

        @GuardedBy("mNotificationLock")
        public void notifyRankingUpdateLocked() {
            for (final ManagedServiceInfo serviceInfo : getServices()) {
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

        @GuardedBy("mNotificationLock")
        public void notifyListenerHintsChangedLocked(final int hints) {
            for (final ManagedServiceInfo serviceInfo : getServices()) {
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
            for (final ManagedServiceInfo serviceInfo : getServices()) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyInterruptionFilterChanged(serviceInfo, interruptionFilter);
                        }
                    });
                }
            }
        }

        protected void notifyNotificationChannelChanged(String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            if (channel != null) {
                for (ManagedServiceInfo serviceInfo : getServices()) {
                    if (serviceInfo.enabledAndUserMatches(UserHandle.getCallingUserId())) {
                        BackgroundThread.getHandler().post(new com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA.AnonymousClass2((byte) 0, modificationType, this, serviceInfo, pkg, user, channel));
                    }
                }
            }
        }

        /* renamed from: lambda$-com_android_server_notification_NotificationManagerService$NotificationListeners_270380 */
        /* synthetic */ void m184xbe46aa06(ManagedServiceInfo serviceInfo, String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            if (NotificationManagerService.this.hasCompanionDevice(serviceInfo)) {
                notifyNotificationChannelChanged(serviceInfo, pkg, user, channel, modificationType);
            }
        }

        protected void notifyNotificationChannelGroupChanged(String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            if (group != null) {
                for (ManagedServiceInfo serviceInfo : getServices()) {
                    if (serviceInfo.enabledAndUserMatches(UserHandle.getCallingUserId())) {
                        BackgroundThread.getHandler().post(new com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA.AnonymousClass2((byte) 1, modificationType, this, serviceInfo, pkg, user, group));
                    }
                }
            }
        }

        /* renamed from: lambda$-com_android_server_notification_NotificationManagerService$NotificationListeners_271192 */
        /* synthetic */ void m185xbe471704(ManagedServiceInfo serviceInfo, String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            if (NotificationManagerService.this.hasCompanionDevice(serviceInfo)) {
                notifyNotificationChannelGroupChanged(serviceInfo, pkg, user, group, modificationType);
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

        private void notifyRemoved(ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate, int reason) {
            if (info.enabledAndUserMatches(sbn.getUserId())) {
                if (sbn == null) {
                    Log.w(this.TAG, "notifyRemoved Info:" + info + " sbn is null");
                }
                INotificationListener listener = info.service;
                try {
                    listener.onNotificationRemoved(new StatusBarNotificationHolder(sbn), rankingUpdate, reason);
                } catch (RemoteException ex) {
                    Log.e(this.TAG, "unable to notify listener (removed): " + listener, ex);
                }
                return;
            }
            if (NotificationManagerService.DEBUG_PANIC) {
                Log.w(this.TAG, "removed sbn:" + sbn + " is not visible to" + info);
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

        void notifyNotificationChannelChanged(ManagedServiceInfo info, String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationChannelModification(pkg, user, channel, modificationType);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (channel changed): " + listener, ex);
            }
        }

        private void notifyNotificationChannelGroupChanged(ManagedServiceInfo info, String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationChannelGroupModification(pkg, user, group, modificationType);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (channel group changed): " + listener, ex);
            }
        }

        public boolean isListenerPackage(String packageName) {
            if (packageName == null) {
                return false;
            }
            synchronized (NotificationManagerService.this.mNotificationLock) {
                for (ManagedServiceInfo serviceInfo : getServices()) {
                    if (packageName.equals(serviceInfo.component.getPackageName())) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    protected class PostNotificationRunnable implements Runnable {
        private final String key;

        PostNotificationRunnable(String key) {
            this.key = key;
        }

        /* JADX WARNING: Missing block: B:18:0x006f, code:
            return;
     */
        /* JADX WARNING: Missing block: B:45:0x012e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = null;
                int N;
                int i;
                try {
                    N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    for (i = 0; i < N; i++) {
                        NotificationRecord enqueued = (NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i);
                        if (Objects.equals(this.key, enqueued.getKey())) {
                            r = enqueued;
                            break;
                        }
                    }
                    if (r == null) {
                        Slog.i(NotificationManagerService.TAG, "Cannot find enqueued record for key: " + this.key);
                        N = NotificationManagerService.this.mEnqueuedNotifications.size();
                        for (i = 0; i < N; i++) {
                            if (Objects.equals(this.key, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i);
                                break;
                            }
                        }
                    } else {
                        NotificationRecord old = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(this.key);
                        final StatusBarNotification n = r.sbn;
                        Notification notification = n.getNotification();
                        int index = NotificationManagerService.this.indexOfNotificationLocked(n.getKey());
                        if (index < 0) {
                            NotificationManagerService.this.mNotificationList.add(r);
                            NotificationManagerService.this.mUsageStats.registerPostedByApp(r);
                        } else {
                            old = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(index);
                            NotificationManagerService.this.mNotificationList.set(index, r);
                            NotificationManagerService.this.mUsageStats.registerUpdatedByApp(r, old);
                            notification.flags |= old.getNotification().flags & 64;
                            r.isUpdate = true;
                        }
                        NotificationManagerService.this.mNotificationsByKey.put(n.getKey(), r);
                        if ((notification.flags & 64) != 0) {
                            notification.flags |= 34;
                        }
                        NotificationManagerService.this.applyZenModeLocked(r);
                        NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                        if (notification.getSmallIcon() != null) {
                            StatusBarNotification oldSbn = old != null ? old.sbn : null;
                            NotificationManagerService.this.mListeners.notifyPostedLocked(n, oldSbn);
                            if (oldSbn == null || (Objects.equals(oldSbn.getGroup(), n.getGroup()) ^ 1) != 0) {
                                NotificationManagerService.this.mHandler.post(new Runnable() {
                                    public void run() {
                                        NotificationManagerService.this.mGroupHelper.onNotificationPosted(n, NotificationManagerService.this.hasAutoGroupSummaryLocked(n));
                                    }
                                });
                            }
                        } else {
                            Slog.e(NotificationManagerService.TAG, "Not posting notification without small icon: " + notification);
                            if (!(old == null || (old.isCanceled ^ 1) == 0)) {
                                NotificationManagerService.this.mListeners.notifyRemovedLocked(n, 4);
                                NotificationManagerService.this.mHandler.post(new Runnable() {
                                    public void run() {
                                        NotificationManagerService.this.mGroupHelper.onNotificationRemoved(n);
                                    }
                                });
                            }
                            Slog.e(NotificationManagerService.TAG, "WARNING: In a future release this will crash the app: " + n.getPackageName());
                        }
                        NotificationManagerService.this.buzzBeepBlinkLocked(r);
                        N = NotificationManagerService.this.mEnqueuedNotifications.size();
                        for (i = 0; i < N; i++) {
                            if (Objects.equals(this.key, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).getKey())) {
                                NotificationManagerService.this.mEnqueuedNotifications.remove(i);
                                break;
                            }
                        }
                    }
                } catch (Throwable th) {
                    N = NotificationManagerService.this.mEnqueuedNotifications.size();
                    for (i = 0; i < N; i++) {
                        if (Objects.equals(this.key, ((NotificationRecord) NotificationManagerService.this.mEnqueuedNotifications.get(i)).getKey())) {
                            NotificationManagerService.this.mEnqueuedNotifications.remove(i);
                            break;
                        }
                    }
                }
            }
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
            Message msg = Message.obtain();
            msg.what = 1001;
            sendMessage(msg);
        }

        public void requestReconsideration(RankingReconsideration recon) {
            sendMessageDelayed(Message.obtain(this, 1000, recon), recon.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri NOTIFICATION_BADGING_URI = Secure.getUriFor("notification_badging");
        private final Uri NOTIFICATION_LIGHT_PULSE_URI = System.getUriFor("notification_light_pulse");
        private final Uri NOTIFICATION_RATE_LIMIT_URI = Global.getUriFor("max_notification_enqueue_rate");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            resolver.registerContentObserver(this.NOTIFICATION_BADGING_URI, false, this, -1);
            resolver.registerContentObserver(this.NOTIFICATION_LIGHT_PULSE_URI, false, this, -1);
            resolver.registerContentObserver(this.NOTIFICATION_RATE_LIMIT_URI, false, this, -1);
            update(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            if (uri == null || this.NOTIFICATION_LIGHT_PULSE_URI.equals(uri)) {
                boolean pulseEnabled = System.getIntForUser(resolver, "notification_light_pulse", 0, -2) != 0;
                if (NotificationManagerService.this.mNotificationPulseEnabled != pulseEnabled) {
                    NotificationManagerService.this.mNotificationPulseEnabled = pulseEnabled;
                    NotificationManagerService.this.updateNotificationPulse();
                }
            }
            if (uri == null || this.NOTIFICATION_RATE_LIMIT_URI.equals(uri)) {
                NotificationManagerService.this.mMaxPackageEnqueueRate = Global.getFloat(resolver, "max_notification_enqueue_rate", NotificationManagerService.this.mMaxPackageEnqueueRate);
            }
            if (uri == null || this.NOTIFICATION_BADGING_URI.equals(uri)) {
                NotificationManagerService.this.mRankingHelper.updateBadgingEnabled();
            }
        }
    }

    private class ShellCmd extends ShellCommand {
        public static final String USAGE = "help\nallow_listener COMPONENT [user_id]\ndisallow_listener COMPONENT [user_id]\nset_assistant COMPONENT\nremove_assistant COMPONENT\nallow_dnd PACKAGE\ndisallow_dnd PACKAGE";

        /* synthetic */ ShellCmd(NotificationManagerService this$0, ShellCmd -this1) {
            this();
        }

        private ShellCmd() {
        }

        public int onCommand(String cmd) {
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            try {
                ComponentName cn;
                String userId;
                if (cmd.equals("allow_dnd")) {
                    NotificationManagerService.this.getBinderService().setNotificationPolicyAccessGranted(getNextArgRequired(), true);
                } else if (cmd.equals("disallow_dnd")) {
                    NotificationManagerService.this.getBinderService().setNotificationPolicyAccessGranted(getNextArgRequired(), false);
                } else if (cmd.equals("allow_listener")) {
                    cn = ComponentName.unflattenFromString(getNextArgRequired());
                    if (cn == null) {
                        pw.println("Invalid listener - must be a ComponentName");
                        return -1;
                    }
                    userId = getNextArg();
                    if (userId == null) {
                        NotificationManagerService.this.getBinderService().setNotificationListenerAccessGranted(cn, true);
                    } else {
                        NotificationManagerService.this.getBinderService().setNotificationListenerAccessGrantedForUser(cn, Integer.parseInt(userId), true);
                    }
                } else if (cmd.equals("disallow_listener")) {
                    cn = ComponentName.unflattenFromString(getNextArgRequired());
                    if (cn == null) {
                        pw.println("Invalid listener - must be a ComponentName");
                        return -1;
                    }
                    userId = getNextArg();
                    if (userId == null) {
                        NotificationManagerService.this.getBinderService().setNotificationListenerAccessGranted(cn, false);
                    } else {
                        NotificationManagerService.this.getBinderService().setNotificationListenerAccessGrantedForUser(cn, Integer.parseInt(userId), false);
                    }
                } else if (cmd.equals("allow_assistant")) {
                    cn = ComponentName.unflattenFromString(getNextArgRequired());
                    if (cn == null) {
                        pw.println("Invalid assistant - must be a ComponentName");
                        return -1;
                    }
                    NotificationManagerService.this.getBinderService().setNotificationAssistantAccessGranted(cn, true);
                } else if (!cmd.equals("disallow_assistant")) {
                    return handleDefaultCommands(cmd);
                } else {
                    cn = ComponentName.unflattenFromString(getNextArgRequired());
                    if (cn == null) {
                        pw.println("Invalid assistant - must be a ComponentName");
                        return -1;
                    }
                    NotificationManagerService.this.getBinderService().setNotificationAssistantAccessGranted(cn, false);
                }
            } catch (Exception e) {
                pw.println("Error occurred. Check logcat for details. " + e.getMessage());
                Slog.e(NotificationManagerService.TAG, "Error running shell command", e);
            }
            return 0;
        }

        public void onHelp() {
            getOutPrintWriter().println(USAGE);
        }
    }

    protected class SnoozeNotificationRunnable implements Runnable {
        private final long mDuration;
        private final String mKey;
        private final String mSnoozeCriterionId;

        SnoozeNotificationRunnable(String key, long duration, String snoozeCriterionId) {
            this.mKey = key;
            this.mDuration = duration;
            this.mSnoozeCriterionId = snoozeCriterionId;
        }

        public void run() {
            synchronized (NotificationManagerService.this.mNotificationLock) {
                NotificationRecord r = NotificationManagerService.this.findNotificationByKeyLocked(this.mKey);
                if (r != null) {
                    snoozeLocked(r);
                }
            }
        }

        @GuardedBy("mNotificationLock")
        void snoozeLocked(NotificationRecord r) {
            if (r.sbn.isGroup()) {
                List<NotificationRecord> groupNotifications = NotificationManagerService.this.findGroupNotificationsLocked(r.sbn.getPackageName(), r.sbn.getGroupKey(), r.sbn.getUserId());
                int i;
                if (r.getNotification().isGroupSummary()) {
                    for (i = 0; i < groupNotifications.size(); i++) {
                        snoozeNotificationLocked((NotificationRecord) groupNotifications.get(i));
                    }
                    return;
                } else if (!NotificationManagerService.this.mSummaryByGroupKey.containsKey(r.sbn.getGroupKey())) {
                    snoozeNotificationLocked(r);
                    return;
                } else if (groupNotifications.size() != 2) {
                    snoozeNotificationLocked(r);
                    return;
                } else {
                    for (i = 0; i < groupNotifications.size(); i++) {
                        snoozeNotificationLocked((NotificationRecord) groupNotifications.get(i));
                    }
                    return;
                }
            }
            snoozeNotificationLocked(r);
        }

        @GuardedBy("mNotificationLock")
        void snoozeNotificationLocked(NotificationRecord r) {
            MetricsLogger.action(r.getLogMaker().setCategory(831).setType(2).addTaggedData(1139, Long.valueOf(this.mDuration)).addTaggedData(832, Integer.valueOf(this.mSnoozeCriterionId == null ? 0 : 1)));
            NotificationManagerService.this.cancelNotificationLocked(r, false, 18, NotificationManagerService.this.removeFromNotificationListsLocked(r), null);
            NotificationManagerService.this.updateLightsLocked();
            if (this.mSnoozeCriterionId != null) {
                NotificationManagerService.this.mAssistants.notifyAssistantSnoozedLocked(r.sbn, this.mSnoozeCriterionId);
                NotificationManagerService.this.mSnoozeHelper.snooze(r);
            } else {
                NotificationManagerService.this.mSnoozeHelper.snooze(r, this.mDuration);
            }
            NotificationManagerService.this.savePolicyFile();
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
        ITransientNotification callback;
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

        void update(ITransientNotification callback) {
            this.callback = callback;
        }

        void dump(PrintWriter pw, String prefix, DumpFilter filter) {
            if (filter == null || (filter.matches(this.pkg) ^ 1) == 0) {
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

    protected class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
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

        protected void scheduleSendRankingUpdate() {
            if (!hasMessages(4)) {
                sendMessage(Message.obtain(this, 4));
            }
        }
    }

    protected void readDefaultApprovedServices(int userId) {
        int i = 0;
        String defaultListenerAccess = getContext().getResources().getString(17039663);
        if (defaultListenerAccess != null) {
            for (String whitelisted : defaultListenerAccess.split(":")) {
                for (ComponentName cn : this.mListeners.queryPackageForServices(whitelisted, 786432, userId)) {
                    try {
                        getBinderService().setNotificationListenerAccessGrantedForUser(cn, userId, true);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        String defaultDndAccess = getContext().getResources().getString(17039662);
        if (defaultListenerAccess != null) {
            String[] split = defaultDndAccess.split(":");
            int length = split.length;
            while (i < length) {
                try {
                    getBinderService().setNotificationPolicyAccessGranted(split[i], true);
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                }
                i++;
            }
        }
    }

    void readPolicyXml(InputStream stream, boolean forRestore) throws XmlPullParserException, NumberFormatException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        XmlUtils.beginDocument(parser, TAG_NOTIFICATION_POLICY);
        boolean migratedManagedServices = false;
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if ("zen".equals(parser.getName())) {
                this.mZenModeHelper.readXml(parser, forRestore);
            } else if ("ranking".equals(parser.getName())) {
                this.mRankingHelper.readXml(parser, forRestore);
            }
            if (!ActivityManager.isLowRamDeviceStatic()) {
                if (this.mListeners.getConfig().xmlTag.equals(parser.getName())) {
                    this.mListeners.readXml(parser);
                    migratedManagedServices = true;
                } else if (this.mAssistants.getConfig().xmlTag.equals(parser.getName())) {
                    this.mAssistants.readXml(parser);
                    migratedManagedServices = true;
                } else if (this.mConditionProviders.getConfig().xmlTag.equals(parser.getName())) {
                    this.mConditionProviders.readXml(parser);
                    migratedManagedServices = true;
                }
            }
        }
        if (!migratedManagedServices) {
            this.mListeners.migrateToXml();
            this.mAssistants.migrateToXml();
            this.mConditionProviders.migrateToXml();
            savePolicyFile();
        }
    }

    private void loadPolicyFile() {
        if (DBG) {
            Slog.d(TAG, "loadPolicyFile");
        }
        synchronized (this.mPolicyFile) {
            AutoCloseable autoCloseable = null;
            try {
                autoCloseable = this.mPolicyFile.openRead();
                readPolicyXml(autoCloseable, false);
            } catch (FileNotFoundException e) {
                readDefaultApprovedServices(0);
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
        if (DBG) {
            Slog.d(TAG, "handleSavePolicyFile");
        }
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
        this.mListeners.writeXml(out, forBackup);
        this.mAssistants.writeXml(out, forBackup);
        this.mConditionProviders.writeXml(out, forBackup);
        out.endTag(null, TAG_NOTIFICATION_POLICY);
        out.endDocument();
    }

    private boolean checkNotificationOp(String pkg, int uid) {
        if (this.mAppOps.checkOp(11, uid, pkg) == 0) {
            return isPackageSuspendedForUser(pkg, uid) ^ 1;
        }
        return false;
    }

    @GuardedBy("mNotificationLock")
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

    @GuardedBy("mNotificationLock")
    private void clearVibrateLocked() {
        this.mVibrateNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            this.mVibrator.cancel();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @GuardedBy("mNotificationLock")
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
        Notification.processWhitelistToken = WHITELIST_TOKEN;
    }

    void setAudioManager(AudioManager audioMananger) {
        this.mAudioManager = audioMananger;
    }

    void setVibrator(Vibrator vibrator) {
        this.mVibrator = vibrator;
    }

    void setLights(Light light) {
        this.mNotificationLight = light;
        this.mAttentionLight = light;
        this.mNotificationPulseEnabled = true;
    }

    void setScreenOn(boolean on) {
        this.mScreenOn = on;
    }

    int getNotificationRecordCount() {
        int count;
        synchronized (this.mNotificationLock) {
            count = ((this.mNotificationList.size() + this.mNotificationsByKey.size()) + this.mSummaryByGroupKey.size()) + this.mEnqueuedNotifications.size();
            for (NotificationRecord posted : this.mNotificationList) {
                if (this.mNotificationsByKey.containsKey(posted.getKey())) {
                    count--;
                }
                if (posted.sbn.isGroup() && posted.getNotification().isGroupSummary()) {
                    count--;
                }
            }
        }
        return count;
    }

    void clearNotifications() {
        this.mEnqueuedNotifications.clear();
        this.mNotificationList.clear();
        this.mNotificationsByKey.clear();
        this.mSummaryByGroupKey.clear();
    }

    void addNotification(NotificationRecord r) {
        this.mNotificationList.add(r);
        this.mNotificationsByKey.put(r.sbn.getKey(), r);
        if (r.sbn.isGroup()) {
            this.mSummaryByGroupKey.put(r.getGroupKey(), r);
        }
    }

    void addEnqueuedNotification(NotificationRecord r) {
        this.mEnqueuedNotifications.add(r);
    }

    NotificationRecord getNotificationRecord(String key) {
        return (NotificationRecord) this.mNotificationsByKey.get(key);
    }

    void setSystemReady(boolean systemReady) {
        this.mSystemReady = systemReady;
    }

    void setHandler(WorkerHandler handler) {
        this.mHandler = handler;
    }

    void setFallbackVibrationPattern(long[] vibrationPattern) {
        this.mFallbackVibrationPattern = vibrationPattern;
    }

    void setPackageManager(IPackageManager packageManager) {
        this.mPackageManager = packageManager;
    }

    void setRankingHelper(RankingHelper rankingHelper) {
        this.mRankingHelper = rankingHelper;
    }

    void setRankingHandler(RankingHandler rankingHandler) {
        this.mRankingHandler = rankingHandler;
    }

    void setIsTelevision(boolean isTelevision) {
        this.mIsTelevision = isTelevision;
    }

    void setUsageStats(NotificationUsageStats us) {
        this.mUsageStats = us;
    }

    void setAccessibilityManager(AccessibilityManager am) {
        this.mAccessibilityManager = am;
    }

    void init(Looper looper, IPackageManager packageManager, PackageManager packageManagerClient, LightsManager lightsManager, NotificationListeners notificationListeners, NotificationAssistants notificationAssistants, ConditionProviders conditionProviders, ICompanionDeviceManager companionManager, SnoozeHelper snoozeHelper, NotificationUsageStats usageStats, AtomicFile policyFile, ActivityManager activityManager, GroupHelper groupHelper) {
        String[] extractorNames;
        boolean z;
        Resources resources = getContext().getResources();
        OppoNotificationManager.getInstance().init(getContext(), this.mHandler);
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mMaxPackageEnqueueRate = Global.getFloat(getContext().getContentResolver(), "max_notification_enqueue_rate", 5.0f);
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        this.mAm = ActivityManager.getService();
        this.mPackageManager = packageManager;
        this.mPackageManagerClient = packageManagerClient;
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        this.mAppUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mAlarmManager = (AlarmManager) getContext().getSystemService("alarm");
        this.mCompanionManager = companionManager;
        this.mActivityManager = activityManager;
        this.mHandler = new WorkerHandler(looper);
        this.mRankingThread.start();
        try {
            extractorNames = resources.getStringArray(17236026);
        } catch (NotFoundException e) {
            extractorNames = new String[0];
        }
        this.mUsageStats = usageStats;
        this.mRankingHandler = new RankingHandlerWorker(this.mRankingThread.getLooper());
        this.mRankingHelper = new RankingHelper(getContext(), this.mPackageManagerClient, this.mRankingHandler, this.mUsageStats, extractorNames);
        this.mConditionProviders = conditionProviders;
        this.mZenModeHelper = new ZenModeHelper(getContext(), this.mHandler.getLooper(), this.mConditionProviders);
        this.mZenModeHelper.addCallback(new Callback() {
            public void onConfigChanged() {
                NotificationManagerService.this.savePolicyFile();
            }

            void onZenModeChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.INTERRUPTION_FILTER_CHANGED");
                NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL").addFlags(67108864), UserHandle.ALL, "android.permission.MANAGE_NOTIFICATIONS");
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
            }

            void onPolicyChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.NOTIFICATION_POLICY_CHANGED");
            }
        });
        this.mSnoozeHelper = snoozeHelper;
        this.mGroupHelper = groupHelper;
        this.mListeners = notificationListeners;
        this.mAssistants = notificationAssistants;
        this.mPolicyFile = policyFile;
        loadPolicyFile();
        this.mStatusBar = (StatusBarManagerInternal) -wrap1(StatusBarManagerInternal.class);
        if (this.mStatusBar != null) {
            this.mStatusBar.setNotificationDelegate(this.mNotificationDelegate);
        }
        this.mNotificationLight = lightsManager.getLight(4);
        this.mAttentionLight = lightsManager.getLight(5);
        this.mFallbackVibrationPattern = getLongArray(resources, 17236025, 17, DEFAULT_VIBRATE_PATTERN);
        this.mInCallNotificationUri = Uri.parse("file://" + resources.getString(17039695));
        this.mInCallNotificationAudioAttributes = new Builder().setContentType(4).setUsage(2).build();
        this.mInCallNotificationVolume = resources.getFloat(17104951);
        this.mUseAttentionLight = resources.getBoolean(17957048);
        if (Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) == 0) {
            this.mDisableNotificationEffects = true;
        }
        this.mZenModeHelper.initZenMode();
        this.mInterruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        this.mUserProfiles.updateCache(getContext());
        listenForCallState();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mArchive = new Archive(resources.getInteger(17694827));
        if (this.mPackageManagerClient.hasSystemFeature("android.software.leanback")) {
            z = true;
        } else {
            z = this.mPackageManagerClient.hasSystemFeature("android.hardware.type.television");
        }
        this.mIsTelevision = z;
    }

    public void onStart() {
        SnoozeHelper snoozeHelper = new SnoozeHelper(getContext(), new Callback() {
            public void repost(int userId, NotificationRecord r) {
                try {
                    if (NotificationManagerService.DBG) {
                        Slog.d(NotificationManagerService.TAG, "Reposting " + r.getKey());
                    }
                    NotificationManagerService.this.enqueueNotificationInternal(r.sbn.getPackageName(), r.sbn.getOpPkg(), r.sbn.getUid(), r.sbn.getInitialPid(), r.sbn.getTag(), r.sbn.getId(), r.sbn.getNotification(), userId);
                } catch (Exception e) {
                    Slog.e(NotificationManagerService.TAG, "Cannot un-snooze notification", e);
                }
            }
        }, this.mUserProfiles);
        File file = new File(Environment.getDataDirectory(), "system");
        init(Looper.myLooper(), AppGlobals.getPackageManager(), getContext().getPackageManager(), (LightsManager) -wrap1(LightsManager.class), new NotificationListeners(AppGlobals.getPackageManager()), new NotificationAssistants(AppGlobals.getPackageManager()), new ConditionProviders(getContext(), this.mUserProfiles, AppGlobals.getPackageManager()), null, snoozeHelper, new NotificationUsageStats(getContext()), new AtomicFile(new File(file, "notification_policy.xml")), (ActivityManager) getContext().getSystemService(OppoAppStartupManager.TYPE_ACTIVITY), getGroupHelper());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(OppoPhoneStateReceiver.ACTION_PHONE_STATE_CHANGED);
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.intent.action.USER_STOPPED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
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
        IntentFilter intentFilter = new IntentFilter(ACTION_NOTIFICATION_TIMEOUT);
        intentFilter.addDataScheme(SCHEME_TIMEOUT);
        getContext().registerReceiver(this.mNotificationTimeoutReceiver, intentFilter);
        getContext().registerReceiver(this.mRestoreReceiver, new IntentFilter("android.os.action.SETTING_RESTORED"));
        getContext().registerReceiver(this.mLocaleChangeReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
        publishBinderService("notification", this.mService);
        publishLocalService(NotificationManagerInternal.class, this.mInternalService);
    }

    private GroupHelper getGroupHelper() {
        return new GroupHelper(new Callback() {
            public void addAutoGroup(String key) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.addAutogroupKeyLocked(key);
                }
            }

            public void removeAutoGroup(String key) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.removeAutogroupKeyLocked(key);
                }
            }

            public void addAutoGroupSummary(int userId, String pkg, String triggeringKey) {
                NotificationManagerService.this.createAutoGroupSummary(userId, pkg, triggeringKey);
            }

            public void removeAutoGroupSummary(int userId, String pkg) {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationManagerService.this.clearAutogroupSummaryLocked(userId, pkg);
                }
            }
        });
    }

    private void sendRegisteredOnlyBroadcast(String action) {
        getContext().sendBroadcastAsUser(new Intent(action).addFlags(1073741824), UserHandle.ALL, null);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mSystemReady = true;
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            this.mAudioManagerInternal = (AudioManagerInternal) -wrap1(AudioManagerInternal.class);
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mZenModeHelper.onSystemReady();
        } else if (phase == 600) {
            this.mSettingsObserver.observe();
            OppoNotificationManager.getInstance().onPhaseThrirdPartyAppsCanStart();
            this.mListeners.onBootPhaseAppsCanStart();
            this.mAssistants.onBootPhaseAppsCanStart();
            this.mConditionProviders.onBootPhaseAppsCanStart();
        }
    }

    @GuardedBy("mNotificationLock")
    private void updateListenerHintsLocked() {
        int hints = calculateHints();
        if (hints != this.mListenerHints) {
            ZenLog.traceListenerHintsChanged(this.mListenerHints, hints, this.mEffectsSuppressors.size());
            this.mListenerHints = hints;
            scheduleListenerHintsChanged(hints);
        }
    }

    @GuardedBy("mNotificationLock")
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

    private void updateNotificationChannelInt(String pkg, int uid, NotificationChannel channel, boolean fromListener) {
        if (channel.getImportance() == 0) {
            cancelAllNotificationsInt(MY_UID, MY_PID, pkg, channel.getId(), 0, 0, true, UserHandle.getUserId(uid), 17, null);
            if (isUidSystemOrPhone(uid)) {
                for (int profileId : this.mUserProfiles.getCurrentProfileIds()) {
                    cancelAllNotificationsInt(MY_UID, MY_PID, pkg, channel.getId(), 0, 0, true, profileId, 17, null);
                }
            }
        }
        this.mRankingHelper.updateNotificationChannel(pkg, uid, channel, true);
        if (!fromListener) {
            this.mListeners.notifyNotificationChannelChanged(pkg, UserHandle.getUserHandleForUid(uid), this.mRankingHelper.getNotificationChannel(pkg, uid, channel.getId(), false), 2);
        }
        savePolicyFile();
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

    @GuardedBy("mNotificationLock")
    private void updateInterruptionFilterLocked() {
        int interruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        if (interruptionFilter != this.mInterruptionFilter) {
            this.mInterruptionFilter = interruptionFilter;
            scheduleInterruptionFilterChanged(interruptionFilter);
        }
    }

    INotificationManager getBinderService() {
        return Stub.asInterface(this.mService);
    }

    NotificationManagerInternal getInternalService() {
        return this.mInternalService;
    }

    private void applyAdjustment(NotificationRecord r, Adjustment adjustment) {
        if (!(r == null || adjustment.getSignals() == null)) {
            Bundle.setDefusable(adjustment.getSignals(), true);
            r.addAdjustment(adjustment);
        }
    }

    @GuardedBy("mNotificationLock")
    void addAutogroupKeyLocked(String key) {
        NotificationRecord r = (NotificationRecord) this.mNotificationsByKey.get(key);
        if (r != null && r.sbn.getOverrideGroupKey() == null) {
            addAutoGroupAdjustment(r, "ranker_group");
            EventLogTags.writeNotificationAutogrouped(key);
            this.mRankingHandler.requestSort();
        }
    }

    @GuardedBy("mNotificationLock")
    void removeAutogroupKeyLocked(String key) {
        NotificationRecord r = (NotificationRecord) this.mNotificationsByKey.get(key);
        if (!(r == null || r.sbn.getOverrideGroupKey() == null)) {
            addAutoGroupAdjustment(r, null);
            EventLogTags.writeNotificationUnautogrouped(key);
            this.mRankingHandler.requestSort();
        }
    }

    private void addAutoGroupAdjustment(NotificationRecord r, String overrideGroupKey) {
        Bundle signals = new Bundle();
        signals.putString("key_group_key", overrideGroupKey);
        r.addAdjustment(new Adjustment(r.sbn.getPackageName(), r.getKey(), signals, "", r.sbn.getUserId()));
    }

    @GuardedBy("mNotificationLock")
    private void clearAutogroupSummaryLocked(int userId, String pkg) {
        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(userId));
        if (summaries != null && summaries.containsKey(pkg)) {
            NotificationRecord removed = findNotificationByKeyLocked((String) summaries.remove(pkg));
            if (removed != null) {
                cancelNotificationLocked(removed, false, 16, removeFromNotificationListsLocked(removed), null);
            }
        }
    }

    @GuardedBy("mNotificationLock")
    private boolean hasAutoGroupSummaryLocked(StatusBarNotification sbn) {
        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(sbn.getUserId()));
        return summaries != null ? summaries.containsKey(sbn.getPackageName()) : false;
    }

    /* JADX WARNING: Missing block: B:21:0x012d, code:
            if (r21 == null) goto L_0x015f;
     */
    /* JADX WARNING: Missing block: B:23:0x014b, code:
            if (checkDisqualifyingFeatures(r31, MY_UID, r21.sbn.getId(), r21.sbn.getTag(), r21, true) == false) goto L_0x015f;
     */
    /* JADX WARNING: Missing block: B:24:0x014d, code:
            r30.mHandler.post(new com.android.server.notification.NotificationManagerService.EnqueueNotificationRunnable(r30, r31, r21));
     */
    /* JADX WARNING: Missing block: B:25:0x015f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void createAutoGroupSummary(int userId, String pkg, String triggeringKey) {
        Throwable th;
        NotificationRecord notificationRecord = null;
        synchronized (this.mNotificationLock) {
            try {
                NotificationRecord notificationRecord2 = (NotificationRecord) this.mNotificationsByKey.get(triggeringKey);
                if (notificationRecord2 == null) {
                    return;
                }
                StatusBarNotification adjustedSbn = notificationRecord2.sbn;
                userId = adjustedSbn.getUser().getIdentifier();
                ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(userId));
                if (summaries == null) {
                    summaries = new ArrayMap();
                }
                this.mAutobundledSummaries.put(Integer.valueOf(userId), summaries);
                if (!summaries.containsKey(pkg)) {
                    ApplicationInfo appInfo = (ApplicationInfo) adjustedSbn.getNotification().extras.getParcelable("android.appInfo");
                    Bundle extras = new Bundle();
                    extras.putParcelable("android.appInfo", appInfo);
                    Notification summaryNotification = new Notification.Builder(getContext(), notificationRecord2.getChannel().getId()).setSmallIcon(adjustedSbn.getNotification().getSmallIcon()).setGroupSummary(true).setGroupAlertBehavior(2).setGroup("ranker_group").setFlag(1024, true).setFlag(512, true).setColor(adjustedSbn.getNotification().color).setLocalOnly(true).build();
                    summaryNotification.extras.putAll(extras);
                    Intent appIntent = getContext().getPackageManager().getLaunchIntentForPackage(pkg);
                    if (appIntent != null) {
                        summaryNotification.contentIntent = PendingIntent.getActivityAsUser(getContext(), 0, appIntent, 0, null, UserHandle.of(userId));
                    }
                    StatusBarNotification summarySbn = new StatusBarNotification(adjustedSbn.getPackageName(), adjustedSbn.getOpPkg(), Integer.MAX_VALUE, "ranker_group", adjustedSbn.getUid(), adjustedSbn.getInitialPid(), summaryNotification, adjustedSbn.getUser(), "ranker_group", System.currentTimeMillis());
                    NotificationRecord notificationRecord3 = new NotificationRecord(getContext(), summarySbn, notificationRecord2.getChannel());
                    try {
                        summaries.put(pkg, summarySbn.getKey());
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

    private String disableNotificationEffects(NotificationRecord record) {
        if (this.mDisableNotificationEffects) {
            return "booleanState";
        }
        if ((this.mListenerHints & 1) != 0) {
            return "listenerHints";
        }
        if (this.mCallState == 0 || (this.mZenModeHelper.isCall(record) ^ 1) == 0) {
            return null;
        }
        return "callState";
    }

    private void dumpJson(PrintWriter pw, DumpFilter filter) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Notification Manager");
            dump.put("bans", this.mRankingHelper.dumpBansJson(filter));
            dump.put("ranking", this.mRankingHelper.dumpJson(filter));
            dump.put("stats", this.mUsageStats.dumpJson(filter));
            dump.put("channels", this.mRankingHelper.dumpChannelsJson(filter));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pw.println(dump);
    }

    private void dumpProto(FileDescriptor fd, DumpFilter filter) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mNotificationLock) {
            int i;
            NotificationRecord nr;
            long records = proto.start(2272037699585L);
            int N = this.mNotificationList.size();
            if (N > 0) {
                for (i = 0; i < N; i++) {
                    nr = (NotificationRecord) this.mNotificationList.get(i);
                    if (filter.filtered) {
                        if ((filter.matches(nr.sbn) ^ 1) != 0) {
                        }
                    }
                    nr.dump(proto, filter.redact);
                    proto.write(1168231104514L, 1);
                }
            }
            N = this.mEnqueuedNotifications.size();
            if (N > 0) {
                for (i = 0; i < N; i++) {
                    nr = (NotificationRecord) this.mEnqueuedNotifications.get(i);
                    if (filter.filtered) {
                        if ((filter.matches(nr.sbn) ^ 1) != 0) {
                        }
                    }
                    nr.dump(proto, filter.redact);
                    proto.write(1168231104514L, 0);
                }
            }
            List<NotificationRecord> snoozed = this.mSnoozeHelper.getSnoozed();
            N = snoozed.size();
            if (N > 0) {
                for (i = 0; i < N; i++) {
                    nr = (NotificationRecord) snoozed.get(i);
                    if (filter.filtered) {
                        if ((filter.matches(nr.sbn) ^ 1) != 0) {
                        }
                    }
                    nr.dump(proto, filter.redact);
                    proto.write(1168231104514L, 2);
                }
            }
            proto.end(records);
        }
        long zenLog = proto.start(1172526071810L);
        this.mZenModeHelper.dump(proto);
        for (ComponentName suppressor : this.mEffectsSuppressors) {
            proto.write(2259152797700L, suppressor.toString());
        }
        proto.end(zenLog);
        proto.flush();
    }

    /* JADX WARNING: Removed duplicated region for block: B:68:0x033c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        synchronized (this.mNotificationLock) {
            int j;
            if (!zenOnly) {
                NotificationRecord nr;
                N = this.mNotificationList.size();
                if (N > 0) {
                    pw.println("  Notification List:");
                    for (i = 0; i < N; i++) {
                        nr = (NotificationRecord) this.mNotificationList.get(i);
                        if (filter.filtered) {
                            if ((filter.matches(nr.sbn) ^ 1) != 0) {
                                continue;
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
                j = 0;
                while (iter.hasNext()) {
                    StatusBarNotification sbn = (StatusBarNotification) iter.next();
                    if (filter == null || (filter.matches(sbn) ^ 1) == 0) {
                        pw.println("    " + sbn);
                        j++;
                        if (j >= 5) {
                            if (iter.hasNext()) {
                                pw.println("    ...");
                            }
                            if (!zenOnly) {
                                N = this.mEnqueuedNotifications.size();
                                if (N > 0) {
                                    pw.println("  Enqueued Notification List:");
                                    for (i = 0; i < N; i++) {
                                        nr = (NotificationRecord) this.mEnqueuedNotifications.get(i);
                                        if (filter.filtered) {
                                            if ((filter.matches(nr.sbn) ^ 1) != 0) {
                                            }
                                        }
                                        nr.dump(pw, "    ", getContext(), filter.redact);
                                    }
                                    pw.println("  ");
                                }
                                this.mSnoozeHelper.dump(pw, filter);
                            }
                        }
                    }
                }
                if (zenOnly) {
                }
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
                    for (j = 0; j < listenerSize; j++) {
                        if (i > 0) {
                            pw.print(',');
                        }
                        pw.print(((ManagedServiceInfo) listeners.valueAt(i)).component);
                    }
                }
                pw.println(')');
                pw.println("\n  Notification assistant services:");
                this.mAssistants.dump(pw, filter);
            }
            if (!filter.filtered || zenOnly) {
                pw.println("\n  Zen Mode:");
                pw.print("    mInterruptionFilter=");
                pw.println(this.mInterruptionFilter);
                this.mZenModeHelper.dump(pw, "    ");
                pw.println("\n  Zen Log:");
                ZenLog.dump(pw, "    ");
            }
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
            if (!zenOnly) {
                pw.println("\n  Usage Stats:");
                this.mUsageStats.dump(pw, "    ", filter);
            }
        }
    }

    void enqueueNotificationInternal(String oriPkg, String oriOpPkg, int oriCallingUid, int callingPid, String tag, int id, Notification notification, int incomingUserId) {
        String pkg;
        String opPkg;
        int callingUid;
        if (DEBUG_PANIC) {
            Slog.v(TAG, "Notification--enqueueNotificationInternal: oriPkg=" + oriPkg + ",oriOpPkg:" + oriOpPkg + ",id=" + id + " notification=" + notification);
        }
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
        if (OppoNotificationManager.getInstance().isHidePkg(oriPkg, incomingUserId)) {
            if (DEBUG_PANIC) {
                Slog.v(TAG, "Notification--enqueueNotificationInternal-isHidePkg: oriPkg=" + oriPkg + ",incomingUserId:" + incomingUserId);
            }
            return;
        }
        if (!getContext().getPackageManager().isFullFunctionMode()) {
            try {
                ApplicationInfo noticeAppInfo = getContext().getPackageManager().getApplicationInfo(pkg, 0);
                if (noticeAppInfo.icon != 0) {
                    notification.setSmallIcon(Icon.createWithResource(pkg, noticeAppInfo.icon));
                    Log.d(TAG, "enqueueNotificationInternal_we use app icon, and noticeAppInfo.icon = " + noticeAppInfo.icon);
                }
            } catch (Exception e3) {
                Log.e(TAG, "enqueueNotificationInternal_Exception = " + e3);
            }
        }
        if (incomingUserId != 999 || (OppoMultiAppManagerUtil.getInstance().isMultiApp(pkg) ^ 1) == 0) {
            checkCallerIsSystemOrSameApp(pkg);
            OppoNotificationManager.getInstance().updateNoClearNotification(notification, pkg);
            int userId = ActivityManager.handleIncomingUser(callingPid, callingUid, incomingUserId, true, false, "enqueueNotification", pkg);
            UserHandle userHandle = new UserHandle(userId);
            if (pkg == null || notification == null) {
                throw new IllegalArgumentException("null not allowed: pkg=" + pkg + " id=" + id + " notification=" + notification);
            } else if (OppoNotificationManager.getInstance().shouldInterceptNotification(pkg, notification)) {
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "shouldInterceptNotification: pkg: " + pkg + ",notification:" + notification);
                }
                return;
            } else {
                int notificationUid = resolveNotificationUid(opPkg, callingUid, userId);
                try {
                    int i;
                    PackageManager packageManager = this.mPackageManagerClient;
                    if (userId == -1) {
                        i = 0;
                    } else {
                        i = userId;
                    }
                    Notification.addFieldsFromContext(packageManager.getApplicationInfoAsUser(pkg, 268435456, i), notification);
                    if (this.mPackageManagerClient.checkPermission("android.permission.USE_COLORIZED_NOTIFICATIONS", pkg) == 0) {
                        notification.flags |= 2048;
                    } else {
                        notification.flags &= -2049;
                    }
                    this.mUsageStats.registerEnqueuedByApp(pkg);
                    String channelId = notification.getChannelId();
                    if (this.mIsTelevision && new TvExtender(notification).getChannelId() != null) {
                        channelId = new TvExtender(notification).getChannelId();
                    }
                    NotificationChannel channel = this.mRankingHelper.getNotificationChannel(pkg, notificationUid, channelId, false);
                    if (channel == null) {
                        Log.e(TAG, "No Channel found for pkg=" + pkg + ", channelId=" + channelId + ", id=" + id + ", tag=" + tag + ", opPkg=" + opPkg + ", callingUid=" + callingUid + ", userId=" + userId + ", incomingUserId=" + incomingUserId + ", notificationUid=" + notificationUid + ", notification=" + notification);
                        if (OppoNotificationManager.getInstance().shouldShowNotificationToast()) {
                            doChannelWarningToast("Developer warning for package \"" + pkg + "\"\n" + "Failed to post notification on channel \"" + channelId + "\"\n" + "See log for more details");
                        }
                        return;
                    }
                    NotificationRecord notificationRecord = new NotificationRecord(getContext(), new StatusBarNotification(pkg, opPkg, id, tag, notificationUid, callingPid, notification, userHandle, null, System.currentTimeMillis()), channel);
                    if ((notification.flags & 64) != 0 && (channel.getUserLockedFields() & 4) == 0 && (notificationRecord.getImportance() == 1 || notificationRecord.getImportance() == 0)) {
                        if (TextUtils.isEmpty(channelId) || "miscellaneous".equals(channelId)) {
                            notificationRecord.setImportance(2, "Bumped for foreground service");
                        } else {
                            channel.setImportance(2);
                            this.mRankingHelper.updateNotificationChannel(pkg, notificationUid, channel, false);
                            notificationRecord.updateNotificationChannel(channel);
                        }
                    }
                    OppoNotificationManager.getInstance().setKeepAliveAppIfNeed(pkg, id, true);
                    if (checkDisqualifyingFeatures(userId, notificationUid, id, tag, notificationRecord, notificationRecord.sbn.getOverrideGroupKey() != null)) {
                        if (notification.allPendingIntents != null) {
                            int intentCount = notification.allPendingIntents.size();
                            if (intentCount > 0) {
                                ActivityManagerInternal am = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                                long duration = ((LocalService) LocalServices.getService(LocalService.class)).getNotificationWhitelistDuration();
                                for (int i2 = 0; i2 < intentCount; i2++) {
                                    PendingIntent pendingIntent = (PendingIntent) notification.allPendingIntents.valueAt(i2);
                                    if (pendingIntent != null) {
                                        am.setPendingIntentWhitelistDuration(pendingIntent.getTarget(), WHITELIST_TOKEN, duration);
                                    }
                                }
                            }
                        }
                        this.mHandler.post(new EnqueueNotificationRunnable(userId, notificationRecord));
                        return;
                    }
                    return;
                } catch (Throwable e4) {
                    Slog.e(TAG, "Cannot create a context for sending app", e4);
                    return;
                }
            }
        }
        Slog.v(TAG, "enqueueNotificationInternal Not showing " + pkg + " userId:" + incomingUserId);
    }

    private void doChannelWarningToast(CharSequence toastText) {
        if (Global.getInt(getContext().getContentResolver(), "show_notification_channel_warnings", Build.IS_DEBUGGABLE ? 1 : 0) != 0) {
            Toast.makeText(getContext(), this.mHandler.getLooper(), toastText, 0).show();
        }
    }

    private int resolveNotificationUid(String opPackageName, int callingUid, int userId) {
        if (!(!isCallerSystemOrPhone() || opPackageName == null || ("android".equals(opPackageName) ^ 1) == 0)) {
            try {
                return getContext().getPackageManager().getPackageUidAsUser(opPackageName, userId);
            } catch (NameNotFoundException e) {
            }
        }
        return callingUid;
    }

    private boolean checkDisqualifyingFeatures(int userId, int callingUid, int id, String tag, NotificationRecord r, boolean isAutogroup) {
        boolean isSystemNotification;
        String pkg = r.sbn.getPackageName();
        String dialerPackage = ((TelecomManager) getContext().getSystemService(TelecomManager.class)).getSystemDialerPackage();
        if (isUidSystemOrPhone(callingUid) || "android".equals(pkg)) {
            isSystemNotification = true;
        } else {
            isSystemNotification = TextUtils.equals(pkg, dialerPackage);
        }
        boolean isNotificationFromListener = this.mListeners.isListenerPackage(pkg);
        if (!(isSystemNotification || (isNotificationFromListener ^ 1) == 0)) {
            synchronized (this.mNotificationLock) {
                if (this.mNotificationsByKey.get(r.sbn.getKey()) == null && isCallerInstantApp(pkg)) {
                    throw new SecurityException("Instant app " + pkg + " cannot create notifications");
                }
                if (!(this.mNotificationsByKey.get(r.sbn.getKey()) == null || (r.getNotification().hasCompletedProgress() ^ 1) == 0 || (isAutogroup ^ 1) == 0)) {
                    float appEnqueueRate = this.mUsageStats.getAppEnqueueRate(pkg);
                    if (appEnqueueRate > this.mMaxPackageEnqueueRate) {
                        this.mUsageStats.registerOverRateQuota(pkg);
                        long now = SystemClock.elapsedRealtime();
                        if (now - this.mLastOverRateLogTime > 5000) {
                            Slog.e(TAG, "Package enqueue rate is " + appEnqueueRate + ". Shedding " + r.sbn.getKey() + ". package=" + pkg);
                            this.mLastOverRateLogTime = now;
                        }
                        return false;
                    }
                }
                int count = getNotificationCountLocked(pkg, userId, id, tag);
                if (count >= 50) {
                    this.mUsageStats.registerOverCountQuota(pkg);
                    Slog.e(TAG, "Package has already posted or enqueued " + count + " notifications.  Not showing more.  package=" + pkg);
                    return false;
                }
            }
        }
        if (this.mSnoozeHelper.isSnoozed(userId, pkg, r.getKey())) {
            MetricsLogger.action(r.getLogMaker().setType(6).setCategory(831));
            if (DBG) {
                Slog.d(TAG, "Ignored enqueue for snoozed notification " + r.getKey());
            }
            this.mSnoozeHelper.update(userId, r);
            savePolicyFile();
            return false;
        }
        if (isBlocked(r, this.mUsageStats)) {
            return false;
        }
        return true;
    }

    protected int getNotificationCountLocked(String pkg, int userId, int excludedId, String excludedTag) {
        int i;
        NotificationRecord existing;
        int count = 0;
        int N = this.mNotificationList.size();
        for (i = 0; i < N; i++) {
            existing = (NotificationRecord) this.mNotificationList.get(i);
            if (existing.sbn.getPackageName().equals(pkg) && existing.sbn.getUserId() == userId && !(existing.sbn.getId() == excludedId && TextUtils.equals(existing.sbn.getTag(), excludedTag))) {
                count++;
            }
        }
        int M = this.mEnqueuedNotifications.size();
        for (i = 0; i < M; i++) {
            existing = (NotificationRecord) this.mEnqueuedNotifications.get(i);
            if (existing.sbn.getPackageName().equals(pkg) && existing.sbn.getUserId() == userId) {
                count++;
            }
        }
        return count;
    }

    protected boolean isBlocked(NotificationRecord r, NotificationUsageStats usageStats) {
        String pkg = r.sbn.getPackageName();
        int callingUid = r.sbn.getUid();
        boolean isPackageSuspended = isPackageSuspendedForUser(pkg, callingUid);
        if (isPackageSuspended) {
            Slog.e(TAG, "Suppressing notification from package due to package suspended by administrator.");
            usageStats.registerSuspendedByAdmin(r);
            return isPackageSuspended;
        }
        boolean isBlocked = this.mRankingHelper.getImportance(pkg, callingUid) != 0 ? r.getChannel().getImportance() == 0 : true;
        if (isBlocked) {
            Slog.e(TAG, "Suppressing notification from package by user request.");
            usageStats.registerBlocked(r);
        }
        return isBlocked;
    }

    @GuardedBy("mNotificationLock")
    private void handleGroupedNotificationLocked(NotificationRecord r, NotificationRecord old, int callingUid, int callingPid) {
        StatusBarNotification sbn = r.sbn;
        Notification n = sbn.getNotification();
        if (n.isGroupSummary() && (sbn.isAppGroup() ^ 1) != 0) {
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
        if (!isSummary || (oldGroup.equals(group) ^ 1) != 0) {
            cancelGroupChildrenLocked(old, callingUid, callingPid, null, false, null);
        }
    }

    @GuardedBy("mNotificationLock")
    void scheduleTimeoutLocked(NotificationRecord record) {
        if (record.getNotification().getTimeoutAfter() > 0) {
            this.mAlarmManager.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + record.getNotification().getTimeoutAfter(), PendingIntent.getBroadcast(getContext(), 1, new Intent(ACTION_NOTIFICATION_TIMEOUT).setData(new Uri.Builder().scheme(SCHEME_TIMEOUT).appendPath(record.getKey()).build()).addFlags(268435456).putExtra(EXTRA_KEY, record.getKey()), 134217728));
        }
    }

    @GuardedBy("mNotificationLock")
    void buzzBeepBlinkLocked(NotificationRecord record) {
        boolean buzz = false;
        boolean beep = false;
        boolean blink = false;
        Notification notification = record.sbn.getNotification();
        String key = record.getKey();
        boolean aboveThreshold = record.getImportance() >= 3;
        boolean canInterruptLight = aboveThreshold ? record.isIntercepted() ? this.mZenModeHelper.getZenMode() == 3 : true : false;
        boolean suppressEffect = OppoNotificationManager.getInstance().shouldSuppressEffect(record.sbn.getPackageName());
        if (DEBUG_PANIC) {
            Slog.v(TAG, "buzzBeepBlinkLocked--aboveThreshold:" + aboveThreshold + ",canInterruptLight:" + canInterruptLight + ",suppressEffect:" + suppressEffect + ",isCurrentUser:" + isNotificationForCurrentUser(record));
        }
        boolean wasBeep = key != null ? key.equals(this.mSoundNotificationKey) : false;
        boolean wasBuzz = key != null ? key.equals(this.mVibrateNotificationKey) : false;
        boolean hasValidVibrate = false;
        int hasValidSound = 0;
        boolean sentAccessibilityEvent = false;
        if (!record.isUpdate && record.getImportance() > 1) {
            sendAccessibilityEvent(notification, record.sbn.getPackageName());
            sentAccessibilityEvent = true;
        }
        if (aboveThreshold && isNotificationForCurrentUser(record) && this.mSystemReady && this.mAudioManager != null) {
            Uri soundUri = record.getSound();
            hasValidSound = soundUri != null ? Uri.EMPTY.equals(soundUri) ^ 1 : 0;
            long[] vibration = record.getVibration();
            if (vibration == null && hasValidSound != 0 && this.mAudioManager.getRingerModeInternal() == 1 && this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(record.getAudioAttributes())) == 0) {
                vibration = this.mFallbackVibrationPattern;
            }
            hasValidVibrate = vibration != null;
            if ((hasValidSound == 0 ? hasValidVibrate : true) && (shouldMuteNotificationLocked(record) ^ 1) != 0) {
                if (!sentAccessibilityEvent) {
                    sendAccessibilityEvent(notification, record.sbn.getPackageName());
                }
                if (DBG) {
                    Slog.v(TAG, "Interrupting!");
                }
                if (!(hasValidSound == 0 || (suppressEffect ^ 1) == 0)) {
                    this.mSoundNotificationKey = key;
                    if (this.mInCall) {
                        playInCallNotification();
                        beep = true;
                    } else {
                        beep = playSound(record, soundUri);
                    }
                }
                boolean ringerModeSilent = this.mAudioManager.getRingerModeInternal() == 0;
                if (!(this.mInCall || !hasValidVibrate || (ringerModeSilent ^ 1) == 0 || this.mAudioManager.getRingerModeInternal() == 0 || (suppressEffect ^ 1) == 0)) {
                    this.mVibrateNotificationKey = key;
                    buzz = playVibration(record, vibration, hasValidSound);
                }
            }
        }
        if (wasBeep && (hasValidSound ^ 1) != 0) {
            clearSoundLocked();
        }
        if (wasBuzz && (hasValidVibrate ^ 1) != 0) {
            clearVibrateLocked();
        }
        boolean wasShowLights = this.mLights.remove(key);
        if (record.getLight() != null && aboveThreshold && (record.getSuppressedVisualEffects() & 1) == 0 && (suppressEffect ^ 1) != 0 && canInterruptLight) {
            this.mLights.add(key);
            updateLightsLocked();
            if (this.mUseAttentionLight) {
                this.mAttentionLight.pulse();
            }
            blink = true;
        } else if (wasShowLights) {
            updateLightsLocked();
        }
        if (buzz || beep || blink) {
            MetricsLogger.action(record.getLogMaker().setCategory(199).setType(1).setSubtype((blink ? 4 : 0) | ((buzz ? 1 : 0) | (beep ? 2 : 0))));
            EventLogTags.writeNotificationAlert(key, buzz ? 1 : 0, beep ? 1 : 0, blink ? 1 : 0);
        }
    }

    @GuardedBy("mNotificationLock")
    boolean shouldMuteNotificationLocked(NotificationRecord record) {
        Notification notification = record.getNotification();
        if (record.isUpdate && (notification.flags & 8) != 0) {
            return true;
        }
        String disableEffects = disableNotificationEffects(record);
        if (disableEffects != null) {
            ZenLog.traceDisableEffects(record, disableEffects);
            return true;
        } else if (record.isIntercepted()) {
            return true;
        } else {
            if (record.sbn.isGroup()) {
                return notification.suppressAlertingDueToGrouping();
            }
            if (!this.mUsageStats.isAlertRateLimited(record.sbn.getPackageName())) {
                return false;
            }
            Slog.e(TAG, "Muting recently noisy " + record.getKey());
            return true;
        }
    }

    private boolean playSound(NotificationRecord record, Uri soundUri) {
        boolean looping = (record.getNotification().flags & 4) != 0;
        if (!(this.mAudioManager.isAudioFocusExclusive() || (this.mAudioManager.getRingerModeInternal() == 1 && this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(record.getAudioAttributes())) == 0))) {
            long identity = Binder.clearCallingIdentity();
            try {
                IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                if (player != null) {
                    if (DBG) {
                        Slog.v(TAG, "Playing sound " + soundUri + " with attributes " + record.getAudioAttributes());
                    }
                    UserHandle user = record.sbn.getUser();
                    if (OppoNotificationManager.getInstance().isMultiAppUserIdMatch(record, record.getUserId())) {
                        user = UserHandle.OWNER;
                    }
                    player.playAsync(soundUri, user, looping, record.getAudioAttributes());
                    return true;
                }
                Binder.restoreCallingIdentity(identity);
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        return false;
    }

    private boolean playVibration(NotificationRecord record, long[] vibration, boolean delayVibForSound) {
        long identity = Binder.clearCallingIdentity();
        try {
            VibrationEffect effect = VibrationEffect.createWaveform(vibration, (record.getNotification().flags & 4) != 0 ? 0 : -1);
            if (delayVibForSound) {
                new Thread(new com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA.AnonymousClass1(this, record, effect)).start();
            } else {
                this.mVibrator.vibrate(record.sbn.getUid(), record.sbn.getOpPkg(), effect, record.getAudioAttributes());
            }
            Binder.restoreCallingIdentity(identity);
            return true;
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Error creating vibration waveform with pattern: " + Arrays.toString(vibration));
            Binder.restoreCallingIdentity(identity);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* renamed from: lambda$-com_android_server_notification_NotificationManagerService_205701 */
    /* synthetic */ void m181xd527677(NotificationRecord record, VibrationEffect effect) {
        int waitMs = this.mAudioManager.getFocusRampTimeMs(3, record.getAudioAttributes());
        if (DBG) {
            Slog.v(TAG, "Delaying vibration by " + waitMs + "ms");
        }
        try {
            Thread.sleep((long) waitMs);
        } catch (InterruptedException e) {
        }
        this.mVibrator.vibrate(record.sbn.getUid(), record.sbn.getOpPkg(), effect, record.getAudioAttributes());
    }

    private boolean isNotificationForCurrentUser(NotificationRecord record) {
        boolean z = true;
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            if (OppoNotificationManager.getInstance().isMultiAppUserIdMatch(record, record.getUserId())) {
                return true;
            }
            if (!(record.getUserId() == -1 || record.getUserId() == currentUser)) {
                z = this.mUserProfiles.isCurrentProfile(record.getUserId());
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    protected void playInCallNotification() {
        new Thread() {
            public void run() {
                long identity = Binder.clearCallingIdentity();
                try {
                    IRingtonePlayer player = NotificationManagerService.this.mAudioManager.getRingtonePlayer();
                    if (player != null) {
                        player.play(new Binder(), NotificationManagerService.this.mInCallNotificationUri, NotificationManagerService.this.mInCallNotificationAudioAttributes, NotificationManagerService.this.mInCallNotificationVolume, false);
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
        }.start();
    }

    @GuardedBy("mToastQueue")
    void showNextToastLocked() {
        ToastRecord record = (ToastRecord) this.mToastQueue.get(0);
        while (record != null) {
            if (DBG) {
                Slog.d(TAG, "Show pkg=" + record.pkg + " callback=" + record.callback);
            }
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

    @GuardedBy("mToastQueue")
    void cancelToastLocked(int index) {
        ToastRecord record = (ToastRecord) this.mToastQueue.get(index);
        try {
            record.callback.hide();
        } catch (RemoteException e) {
            Slog.w(TAG, "Object died trying to hide notification " + record.callback + " in package " + record.pkg);
        }
        this.mWindowManagerInternal.removeWindowToken(((ToastRecord) this.mToastQueue.remove(index)).token, true, 0);
        keepProcessAliveIfNeededLocked(record.pid);
        if (this.mToastQueue.size() > 0) {
            showNextToastLocked();
        }
    }

    @GuardedBy("mToastQueue")
    private void scheduleTimeoutLocked(ToastRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 2, r), (long) (r.duration == 1 ? 3500 : 2000));
    }

    private void handleTimeout(ToastRecord record) {
        if (DBG) {
            Slog.d(TAG, "Timeout pkg=" + record.pkg + " callback=" + record.callback);
        }
        synchronized (this.mToastQueue) {
            int index = indexOfToastLocked(record.pkg, record.callback);
            if (index >= 0) {
                try {
                    cancelToastLocked(index);
                } catch (Exception ex) {
                    Slog.w(TAG, "handleTimeout cancelToastLocked exception!", ex);
                }
            }
        }
        return;
    }

    @GuardedBy("mToastQueue")
    int indexOfToastLocked(String pkg, ITransientNotification callback) {
        IBinder cbak = callback.asBinder();
        ArrayList<ToastRecord> list = this.mToastQueue;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            ToastRecord r = (ToastRecord) list.get(i);
            if (r.pkg.equals(pkg) && r.callback.asBinder().equals(cbak)) {
                return i;
            }
        }
        return -1;
    }

    @GuardedBy("mToastQueue")
    int indexOfToastPackageLocked(String pkg) {
        ArrayList<ToastRecord> list = this.mToastQueue;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (((ToastRecord) list.get(i)).pkg.equals(pkg)) {
                return i;
            }
        }
        return -1;
    }

    @GuardedBy("mToastQueue")
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
            iActivityManager.setProcessImportant(iBinder, pid, z, "toast");
        } catch (RemoteException e) {
        }
    }

    /* JADX WARNING: Missing block: B:22:0x0063, code:
            if (r0 == false) goto L_0x006a;
     */
    /* JADX WARNING: Missing block: B:23:0x0065, code:
            r14.mHandler.scheduleSendRankingUpdate();
     */
    /* JADX WARNING: Missing block: B:24:0x006a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleRankingReconsideration(Message message) {
        if (message.obj instanceof RankingReconsideration) {
            RankingReconsideration recon = message.obj;
            recon.run();
            synchronized (this.mNotificationLock) {
                NotificationRecord record = (NotificationRecord) this.mNotificationsByKey.get(recon.getKey());
                if (record == null) {
                    return;
                }
                int indexBefore = findNotificationRecordIndexLocked(record);
                boolean interceptBefore = record.isIntercepted();
                float contactAffinityBefore = record.getContactAffinity();
                int visibilityBefore = record.getPackageVisibilityOverride();
                recon.applyChangesLocked(record);
                applyZenModeLocked(record);
                this.mRankingHelper.sort(this.mNotificationList);
                int indexAfter = findNotificationRecordIndexLocked(record);
                boolean interceptAfter = record.isIntercepted();
                float contactAffinityAfter = record.getContactAffinity();
                boolean changed = (indexBefore == indexAfter && interceptBefore == interceptAfter) ? visibilityBefore != record.getPackageVisibilityOverride() : true;
                if (!(!interceptBefore || (interceptAfter ^ 1) == 0 || Float.compare(contactAffinityBefore, contactAffinityAfter) == 0)) {
                    buzzBeepBlinkLocked(record);
                }
            }
        }
    }

    void handleRankingSort() {
        if (this.mRankingHelper != null) {
            synchronized (this.mNotificationLock) {
                int i;
                NotificationRecord r;
                int N = this.mNotificationList.size();
                ArrayList<String> orderBefore = new ArrayList(N);
                int[] visibilities = new int[N];
                boolean[] showBadges = new boolean[N];
                ArrayList<NotificationChannel> channelBefore = new ArrayList(N);
                ArrayList<String> groupKeyBefore = new ArrayList(N);
                ArrayList<ArrayList<String>> overridePeopleBefore = new ArrayList(N);
                ArrayList<ArrayList<SnoozeCriterion>> snoozeCriteriaBefore = new ArrayList(N);
                for (i = 0; i < N; i++) {
                    r = (NotificationRecord) this.mNotificationList.get(i);
                    orderBefore.add(r.getKey());
                    visibilities[i] = r.getPackageVisibilityOverride();
                    showBadges[i] = r.canShowBadge();
                    channelBefore.add(r.getChannel());
                    groupKeyBefore.add(r.getGroupKey());
                    overridePeopleBefore.add(r.getPeopleOverride());
                    snoozeCriteriaBefore.add(r.getSnoozeCriteria());
                    this.mRankingHelper.extractSignals(r);
                }
                this.mRankingHelper.sort(this.mNotificationList);
                i = 0;
                while (i < N) {
                    r = (NotificationRecord) this.mNotificationList.get(i);
                    if (((String) orderBefore.get(i)).equals(r.getKey()) && visibilities[i] == r.getPackageVisibilityOverride()) {
                        if (showBadges[i] == r.canShowBadge() && (Objects.equals(channelBefore.get(i), r.getChannel()) ^ 1) == 0 && (Objects.equals(groupKeyBefore.get(i), r.getGroupKey()) ^ 1) == 0 && (Objects.equals(overridePeopleBefore.get(i), r.getPeopleOverride()) ^ 1) == 0 && (Objects.equals(snoozeCriteriaBefore.get(i), r.getSnoozeCriteria()) ^ 1) == 0) {
                            i++;
                        }
                    }
                    this.mHandler.scheduleSendRankingUpdate();
                    return;
                }
            }
        }
    }

    @GuardedBy("mNotificationLock")
    private void recordCallerLocked(NotificationRecord record) {
        if (this.mZenModeHelper.isCall(record)) {
            this.mZenModeHelper.recordCaller(record);
        }
    }

    @GuardedBy("mNotificationLock")
    private void applyZenModeLocked(NotificationRecord record) {
        int i = 0;
        record.setIntercepted(this.mZenModeHelper.shouldIntercept(record));
        if (record.isIntercepted()) {
            int i2 = this.mZenModeHelper.shouldSuppressWhenScreenOff() ? 1 : 0;
            if (this.mZenModeHelper.shouldSuppressWhenScreenOn()) {
                i = 2;
            }
            record.setSuppressedVisualEffects(i2 | i);
            return;
        }
        record.setSuppressedVisualEffects(0);
    }

    @GuardedBy("mNotificationLock")
    private int findNotificationRecordIndexLocked(NotificationRecord target) {
        return this.mRankingHelper.indexOf(this.mNotificationList, target);
    }

    private void handleSendRankingUpdate() {
        synchronized (this.mNotificationLock) {
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
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyListenerHintsChangedLocked(hints);
        }
    }

    private void handleListenerInterruptionFilterChanged(int interruptionFilter) {
        synchronized (this.mNotificationLock) {
            this.mListeners.notifyInterruptionFilterChanged(interruptionFilter);
        }
    }

    static int clamp(int x, int low, int high) {
        if (x < low) {
            return low;
        }
        return x > high ? high : x;
    }

    void sendAccessibilityEvent(Notification notification, CharSequence packageName) {
        if (this.mAccessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(64);
            event.setPackageName(packageName);
            event.setClassName(Notification.class.getName());
            event.setParcelableData(notification);
            CharSequence tickerText = notification.tickerText;
            if (!TextUtils.isEmpty(tickerText)) {
                event.getText().add(tickerText);
            }
            this.mAccessibilityManager.sendAccessibilityEvent(event);
        }
    }

    @GuardedBy("mNotificationLock")
    private boolean removeFromNotificationListsLocked(NotificationRecord r) {
        boolean wasPosted = false;
        NotificationRecord recordInList = findNotificationByListLocked(this.mNotificationList, r.getKey());
        if (recordInList != null) {
            this.mNotificationList.remove(recordInList);
            this.mNotificationsByKey.remove(recordInList.sbn.getKey());
            wasPosted = true;
        }
        while (true) {
            recordInList = findNotificationByListLocked(this.mEnqueuedNotifications, r.getKey());
            if (recordInList == null) {
                return wasPosted;
            }
            this.mEnqueuedNotifications.remove(recordInList);
        }
    }

    @GuardedBy("mNotificationLock")
    private void cancelNotificationLocked(NotificationRecord r, boolean sendDelete, int reason, boolean wasPosted, String listenerName) {
        String canceledKey = r.getKey();
        recordCallerLocked(r);
        if (sendDelete && r.getNotification().deleteIntent != null) {
            try {
                r.getNotification().deleteIntent.send();
            } catch (CanceledException ex) {
                Slog.w(TAG, "canceled PendingIntent for " + r.sbn.getPackageName(), ex);
            }
        }
        if (wasPosted) {
            long identity;
            if (r.getNotification().getSmallIcon() != null) {
                if (reason != 18) {
                    r.isCanceled = true;
                }
                this.mListeners.notifyRemovedLocked(r.sbn, reason);
                final NotificationRecord notificationRecord = r;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        NotificationManagerService.this.mGroupHelper.onNotificationRemoved(notificationRecord.sbn);
                    }
                });
            }
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
        }
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
        String groupKey = r.getGroupKey();
        NotificationRecord groupSummary = (NotificationRecord) this.mSummaryByGroupKey.get(groupKey);
        if (groupSummary != null && groupSummary.getKey().equals(canceledKey)) {
            this.mSummaryByGroupKey.remove(groupKey);
        }
        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(r.sbn.getUserId()));
        if (summaries != null) {
            if (r.sbn.getKey().equals(summaries.get(r.sbn.getPackageName()))) {
                summaries.remove(r.sbn.getPackageName());
            }
        }
        this.mArchive.record(r.sbn);
        long now = System.currentTimeMillis();
        MetricsLogger.action(r.getLogMaker(now).setCategory(128).setType(5).setSubtype(reason));
        EventLogTags.writeNotificationCanceled(canceledKey, reason, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now), listenerName);
        if (DEBUG_PANIC) {
            Slog.v(TAG, "Notification--cancelNotificationLocked--canceledKey:" + canceledKey + ",reason=" + reason + ",sendDelete:" + sendDelete + ",wasPosted:" + wasPosted + ",record:" + r);
        }
    }

    void cancelNotification(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, ManagedServiceInfo listener) {
        if (DEBUG_PANIC) {
            Slog.v(TAG, "Notification--cancelNotification: pkg=" + pkg + ",callingUid:" + callingUid + ",callingPid=" + callingPid + ",tag:" + tag + ",id:" + id + ",userId:" + userId + ",reason:" + reason);
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
            /* JADX WARNING: Missing block: B:28:0x00da, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                if (NotificationManagerService.DBG) {
                    EventLogTags.writeNotificationCancel(i, i2, str, i3, str2, i4, i5, i6, i7, listenerName);
                }
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    NotificationRecord r = NotificationManagerService.this.findNotificationLocked(str, str2, i3, i4);
                    if (r != null) {
                        if (i7 == 1) {
                            NotificationManagerService.this.mUsageStats.registerClickedByUser(r);
                        }
                        if ((r.getNotification().flags & i5) != i5) {
                        } else if ((r.getNotification().flags & i6) != 0) {
                        } else {
                            OppoNotificationManager.getInstance().setKeepAliveAppIfNeed(str, i3, false);
                            NotificationManagerService.this.cancelNotificationLocked(r, z, i7, NotificationManagerService.this.removeFromNotificationListsLocked(r), listenerName);
                            NotificationManagerService.this.cancelGroupChildrenLocked(r, i, i2, listenerName, z, null);
                            NotificationManagerService.this.updateLightsLocked();
                        }
                    } else {
                        if (i7 != 18 && NotificationManagerService.this.mSnoozeHelper.cancel(i4, str, str2, i3)) {
                            NotificationManagerService.this.savePolicyFile();
                        }
                        OppoNotificationManager.getInstance().setKeepAliveAppIfNeed(str, i3, false);
                    }
                }
            }
        });
    }

    private boolean notificationMatchesUserId(NotificationRecord r, int userId) {
        boolean z = true;
        if (OppoNotificationManager.getInstance().isMultiAppUserIdMatch(r, userId)) {
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

    void cancelAllNotificationsInt(int callingUid, int callingPid, String pkg, String channelId, int mustHaveFlags, int mustNotHaveFlags, boolean doit, int userId, int reason, ManagedServiceInfo listener) {
        OppoNotificationManager.getInstance().setNavigationStatus(pkg, channelId, callingUid, callingPid, reason);
        final ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final String str = pkg;
        final int i3 = userId;
        final int i4 = mustHaveFlags;
        final int i5 = mustNotHaveFlags;
        final int i6 = reason;
        final boolean z = doit;
        final String str2 = channelId;
        this.mHandler.post(new Runnable() {
            public void run() {
                String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                EventLogTags.writeNotificationCancelAll(i, i2, str, i3, i4, i5, i6, listenerName);
                if (z) {
                    synchronized (NotificationManagerService.this.mNotificationLock) {
                        FlagChecker anonymousClass3 = new com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA.AnonymousClass3(i4, i5);
                        NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mNotificationList, i, i2, str, true, str2, anonymousClass3, false, i3, false, i6, listenerName, true);
                        NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, i, i2, str, true, str2, anonymousClass3, false, i3, false, i6, listenerName, false);
                        NotificationManagerService.this.mSnoozeHelper.cancel(i3, str);
                    }
                }
            }

            /* renamed from: lambda$-com_android_server_notification_NotificationManagerService$15_234086 */
            static /* synthetic */ boolean m182x1ae61dbb(int mustHaveFlags, int mustNotHaveFlags, int flags) {
                if ((flags & mustHaveFlags) == mustHaveFlags && (flags & mustNotHaveFlags) == 0) {
                    return true;
                }
                return false;
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0019 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @GuardedBy("mNotificationLock")
    private void cancelAllNotificationsByListLocked(ArrayList<NotificationRecord> notificationList, int callingUid, int callingPid, String pkg, boolean nullPkgIndicatesUserSwitch, String channelId, FlagChecker flagChecker, boolean includeCurrentProfiles, int userId, boolean sendDelete, int reason, String listenerName, boolean wasPosted) {
        int i;
        ArrayList canceledNotifications = null;
        for (i = notificationList.size() - 1; i >= 0; i--) {
            NotificationRecord r = (NotificationRecord) notificationList.get(i);
            if (includeCurrentProfiles) {
                if (!notificationMatchesCurrentProfiles(r, userId)) {
                }
                if (nullPkgIndicatesUserSwitch || pkg != null || r.getUserId() != -1) {
                    if (flagChecker.apply(r.getFlags()) && (pkg == null || (r.sbn.getPackageName().equals(pkg) ^ 1) == 0)) {
                        if (channelId != null) {
                            if ((channelId.equals(r.getChannel().getId()) ^ 1) != 0) {
                            }
                        }
                        if (!OppoNotificationManager.getInstance().shouldKeepNotifcationWhenForceStop(pkg, r, reason)) {
                            if (canceledNotifications == null) {
                                canceledNotifications = new ArrayList();
                            }
                            notificationList.remove(i);
                            this.mNotificationsByKey.remove(r.getKey());
                            canceledNotifications.add(r);
                            cancelNotificationLocked(r, sendDelete, reason, wasPosted, listenerName);
                        }
                    }
                }
            } else {
                if (!notificationMatchesUserId(r, userId)) {
                }
                if (nullPkgIndicatesUserSwitch) {
                }
                if (channelId != null) {
                }
                if (!OppoNotificationManager.getInstance().shouldKeepNotifcationWhenForceStop(pkg, r, reason)) {
                }
            }
        }
        if (canceledNotifications != null) {
            int M = canceledNotifications.size();
            for (i = 0; i < M; i++) {
                cancelGroupChildrenLocked((NotificationRecord) canceledNotifications.get(i), callingUid, callingPid, listenerName, false, flagChecker);
            }
            updateLightsLocked();
        }
    }

    void snoozeNotificationInt(String key, long duration, String snoozeCriterionId, ManagedServiceInfo listener) {
        String listenerName = listener == null ? null : listener.component.toShortString();
        if ((duration > 0 || snoozeCriterionId != null) && key != null) {
            if (DBG) {
                Slog.d(TAG, String.format("snooze event(%s, %d, %s, %s)", new Object[]{key, Long.valueOf(duration), snoozeCriterionId, listenerName}));
            }
            this.mHandler.post(new SnoozeNotificationRunnable(key, duration, snoozeCriterionId));
        }
    }

    void unsnoozeNotificationInt(String key, ManagedServiceInfo listener) {
        String listenerName = listener == null ? null : listener.component.toShortString();
        if (DBG) {
            Slog.d(TAG, String.format("unsnooze event(%s, %s)", new Object[]{key, listenerName}));
        }
        this.mSnoozeHelper.repost(key);
        savePolicyFile();
    }

    @GuardedBy("mNotificationLock")
    void cancelAllLocked(int callingUid, int callingPid, int userId, int reason, ManagedServiceInfo listener, boolean includeCurrentProfiles) {
        if (DEBUG_PANIC) {
            Slog.v(TAG, "Notification--cancelAllLocked: callingUid=" + callingUid + ",callingPid:" + callingPid + ",userId=" + userId + ",includeCurrentProfiles:" + includeCurrentProfiles);
        }
        final ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final int i3 = userId;
        final int i4 = reason;
        final boolean z = includeCurrentProfiles;
        this.mHandler.post(new Runnable() {
            public void run() {
                synchronized (NotificationManagerService.this.mNotificationLock) {
                    String listenerName = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                    EventLogTags.writeNotificationCancelAll(i, i2, null, i3, 0, 0, i4, listenerName);
                    FlagChecker flagChecker = -$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA.$INST$0;
                    NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mNotificationList, i, i2, null, false, null, flagChecker, z, i3, true, i4, listenerName, true);
                    NotificationManagerService.this.cancelAllNotificationsByListLocked(NotificationManagerService.this.mEnqueuedNotifications, i, i2, null, false, null, flagChecker, z, i3, true, i4, listenerName, false);
                    NotificationManagerService.this.mSnoozeHelper.cancel(i3, z);
                }
            }

            /* renamed from: lambda$-com_android_server_notification_NotificationManagerService$16_239962 */
            static /* synthetic */ boolean m183x82c9b1fc(int flags) {
                if ((flags & 34) != 0) {
                    return false;
                }
                return true;
            }
        });
    }

    @GuardedBy("mNotificationLock")
    private void cancelGroupChildrenLocked(NotificationRecord r, int callingUid, int callingPid, String listenerName, boolean sendDelete, FlagChecker flagChecker) {
        if (!r.getNotification().isGroupSummary()) {
            return;
        }
        if (r.sbn.getPackageName() == null) {
            if (DBG) {
                Log.e(TAG, "No package for group summary: " + r.getKey());
            }
            return;
        }
        cancelGroupChildrenByListLocked(this.mNotificationList, r, callingUid, callingPid, listenerName, sendDelete, true, flagChecker);
        cancelGroupChildrenByListLocked(this.mEnqueuedNotifications, r, callingUid, callingPid, listenerName, sendDelete, false, flagChecker);
    }

    @GuardedBy("mNotificationLock")
    private void cancelGroupChildrenByListLocked(ArrayList<NotificationRecord> notificationList, NotificationRecord parentNotification, int callingUid, int callingPid, String listenerName, boolean sendDelete, boolean wasPosted, FlagChecker flagChecker) {
        String pkg = parentNotification.sbn.getPackageName();
        int userId = parentNotification.getUserId();
        for (int i = notificationList.size() - 1; i >= 0; i--) {
            NotificationRecord childR = (NotificationRecord) notificationList.get(i);
            StatusBarNotification childSbn = childR.sbn;
            if (childSbn.isGroup() && (childSbn.getNotification().isGroupSummary() ^ 1) != 0 && childR.getGroupKey().equals(parentNotification.getGroupKey()) && (childR.getFlags() & 64) == 0) {
                if (flagChecker != null) {
                    if (!flagChecker.apply(childR.getFlags())) {
                    }
                }
                EventLogTags.writeNotificationCancel(callingUid, callingPid, pkg, childSbn.getId(), childSbn.getTag(), userId, 0, 0, 12, listenerName);
                notificationList.remove(i);
                this.mNotificationsByKey.remove(childR.getKey());
                cancelNotificationLocked(childR, sendDelete, 12, wasPosted, listenerName);
            }
        }
    }

    @GuardedBy("mNotificationLock")
    void updateLightsLocked() {
        NotificationRecord ledNotification = null;
        while (ledNotification == null && (this.mLights.isEmpty() ^ 1) != 0) {
            String owner = (String) this.mLights.get(this.mLights.size() - 1);
            ledNotification = (NotificationRecord) this.mNotificationsByKey.get(owner);
            if (ledNotification == null) {
                Slog.wtfStack(TAG, "LED Notification does not exist: " + owner);
                this.mLights.remove(owner);
            }
        }
        if (ledNotification == null || this.mInCall || this.mScreenOn || OppoNotificationManager.getInstance().isShutdown()) {
            this.mNotificationLight.turnOff();
            return;
        }
        Light light = ledNotification.getLight();
        if (light != null && this.mNotificationPulseEnabled) {
            this.mNotificationLight.setFlashing(light.color, 1, light.onMs, light.offMs);
        }
    }

    @GuardedBy("mNotificationLock")
    List<NotificationRecord> findGroupNotificationsLocked(String pkg, String groupKey, int userId) {
        List<NotificationRecord> records = new ArrayList();
        records.addAll(findGroupNotificationByListLocked(this.mNotificationList, pkg, groupKey, userId));
        records.addAll(findGroupNotificationByListLocked(this.mEnqueuedNotifications, pkg, groupKey, userId));
        return records;
    }

    @GuardedBy("mNotificationLock")
    private List<NotificationRecord> findGroupNotificationByListLocked(ArrayList<NotificationRecord> list, String pkg, String groupKey, int userId) {
        List<NotificationRecord> records = new ArrayList();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = (NotificationRecord) list.get(i);
            if (notificationMatchesUserId(r, userId) && r.getGroupKey().equals(groupKey) && r.sbn.getPackageName().equals(pkg)) {
                records.add(r);
            }
        }
        return records;
    }

    @GuardedBy("mNotificationLock")
    private NotificationRecord findNotificationByKeyLocked(String key) {
        NotificationRecord r = findNotificationByListLocked(this.mNotificationList, key);
        if (r != null) {
            return r;
        }
        r = findNotificationByListLocked(this.mEnqueuedNotifications, key);
        if (r != null) {
            return r;
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
    NotificationRecord findNotificationLocked(String pkg, String tag, int id, int userId) {
        NotificationRecord r = findNotificationByListLocked(this.mNotificationList, pkg, tag, id, userId);
        if (r != null) {
            return r;
        }
        r = findNotificationByListLocked(this.mEnqueuedNotifications, pkg, tag, id, userId);
        if (r != null) {
            return r;
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
    private NotificationRecord findNotificationByListLocked(ArrayList<NotificationRecord> list, String pkg, String tag, int id, int userId) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = (NotificationRecord) list.get(i);
            if (notificationMatchesUserId(r, userId) && r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag) && r.sbn.getPackageName().equals(pkg)) {
                return r;
            }
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
    private NotificationRecord findNotificationByListLocked(ArrayList<NotificationRecord> list, String key) {
        int N = list.size();
        for (int i = 0; i < N; i++) {
            if (key.equals(((NotificationRecord) list.get(i)).getKey())) {
                return (NotificationRecord) list.get(i);
            }
        }
        return null;
    }

    @GuardedBy("mNotificationLock")
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
        synchronized (this.mNotificationLock) {
            updateLightsLocked();
        }
    }

    protected boolean isCallingUidSystem() {
        return Binder.getCallingUid() == 1000;
    }

    protected boolean isUidSystemOrPhone(int uid) {
        int appid = UserHandle.getAppId(uid);
        if (appid == 1000 || appid == 1001 || uid == 0) {
            return true;
        }
        return false;
    }

    protected boolean isCallerSystemOrPhone() {
        return isUidSystemOrPhone(Binder.getCallingUid());
    }

    private void checkCallerIsSystemOrShell() {
        if (Binder.getCallingUid() != 2000) {
            checkCallerIsSystem();
        }
    }

    private void checkCallerIsSystem() {
        if (!isCallerSystemOrPhone()) {
            throw new SecurityException("Disallowed call for uid " + Binder.getCallingUid());
        }
    }

    private void checkCallerIsSystemOrSameApp(String pkg) {
        if (!isCallerSystemOrPhone()) {
            checkCallerIsSameApp(pkg);
        }
    }

    private boolean isCallerInstantApp(String pkg) {
        if (isCallerSystemOrPhone()) {
            return false;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), pkg);
        try {
            ApplicationInfo ai = this.mPackageManager.getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
            if (ai != null) {
                return ai.isInstantApp();
            }
            throw new SecurityException("Unknown package " + pkg);
        } catch (RemoteException re) {
            throw new SecurityException("Unknown package " + pkg, re);
        }
    }

    private void checkCallerIsSameApp(String pkg) {
        int uid = Binder.getCallingUid();
        try {
            ApplicationInfo ai = this.mPackageManager.getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
            if (ai == null) {
                throw new SecurityException("Unknown package " + pkg);
            } else if (!UserHandle.isSameApp(ai.uid, uid)) {
                throw new SecurityException("Calling uid " + uid + " gave package " + pkg + " which is owned by uid " + ai.uid);
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
        TelephonyManager.from(getContext()).listen(new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (NotificationManagerService.this.mCallState != state) {
                    if (NotificationManagerService.DBG) {
                        Slog.d(NotificationManagerService.TAG, "Call state changed: " + NotificationManagerService.callStateToString(state));
                    }
                    NotificationManagerService.this.mCallState = state;
                }
            }
        }, 32);
    }

    @GuardedBy("mNotificationLock")
    private NotificationRankingUpdate makeRankingUpdateLocked(ManagedServiceInfo info) {
        int i;
        int N = this.mNotificationList.size();
        ArrayList<String> arrayList = new ArrayList(N);
        arrayList = new ArrayList(N);
        ArrayList<Integer> arrayList2 = new ArrayList(N);
        Bundle overrideGroupKeys = new Bundle();
        Bundle visibilityOverrides = new Bundle();
        Bundle suppressedVisualEffects = new Bundle();
        Bundle explanation = new Bundle();
        Bundle channels = new Bundle();
        Bundle overridePeople = new Bundle();
        Bundle snoozeCriteria = new Bundle();
        Bundle showBadge = new Bundle();
        for (i = 0; i < N; i++) {
            NotificationRecord record = (NotificationRecord) this.mNotificationList.get(i);
            if (isVisibleToListener(record.sbn, info)) {
                String key = record.sbn.getKey();
                arrayList.add(key);
                arrayList2.add(Integer.valueOf(record.getImportance()));
                if (record.getImportanceExplanation() != null) {
                    explanation.putCharSequence(key, record.getImportanceExplanation());
                }
                if (record.isIntercepted()) {
                    arrayList.add(key);
                }
                suppressedVisualEffects.putInt(key, record.getSuppressedVisualEffects());
                if (record.getPackageVisibilityOverride() != -1000) {
                    visibilityOverrides.putInt(key, record.getPackageVisibilityOverride());
                }
                overrideGroupKeys.putString(key, record.sbn.getOverrideGroupKey());
                channels.putParcelable(key, record.getChannel());
                overridePeople.putStringArrayList(key, record.getPeopleOverride());
                snoozeCriteria.putParcelableArrayList(key, record.getSnoozeCriteria());
                showBadge.putBoolean(key, record.canShowBadge());
            }
        }
        int M = arrayList.size();
        String[] keysAr = (String[]) arrayList.toArray(new String[M]);
        String[] interceptedKeysAr = (String[]) arrayList.toArray(new String[arrayList.size()]);
        int[] importanceAr = new int[M];
        for (i = 0; i < M; i++) {
            importanceAr[i] = ((Integer) arrayList2.get(i)).intValue();
        }
        return new NotificationRankingUpdate(keysAr, interceptedKeysAr, visibilityOverrides, suppressedVisualEffects, importanceAr, explanation, overrideGroupKeys, channels, overridePeople, snoozeCriteria, showBadge);
    }

    boolean hasCompanionDevice(ManagedServiceInfo info) {
        if (this.mCompanionManager == null) {
            this.mCompanionManager = getCompanionManager();
        }
        if (this.mCompanionManager == null) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (!ArrayUtils.isEmpty(this.mCompanionManager.getAssociations(info.component.getPackageName(), info.userid))) {
                return true;
            }
            if (OppoNotificationManager.getInstance().canListenNotificationChannelChange(info.component.getPackageName())) {
                Binder.restoreCallingIdentity(identity);
                return true;
            }
            Binder.restoreCallingIdentity(identity);
            return false;
        } catch (SecurityException e) {
        } catch (RemoteException re) {
            Slog.e(TAG, "Cannot reach companion device service", re);
        } catch (Exception e2) {
            Slog.e(TAG, "Cannot verify listener " + info, e2);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected ICompanionDeviceManager getCompanionManager() {
        return ICompanionDeviceManager.Stub.asInterface(ServiceManager.getService("companiondevice"));
    }

    private boolean isVisibleToListener(StatusBarNotification sbn, ManagedServiceInfo listener) {
        if (listener.enabledAndUserMatches(sbn.getUserId())) {
            return true;
        }
        return false;
    }

    private boolean isPackageSuspendedForUser(String pkg, int uid) {
        try {
            return this.mPackageManager.isPackageSuspendedForUser(pkg, UserHandle.getUserId(uid));
        } catch (RemoteException e) {
            throw new SecurityException("Could not talk to package manager service");
        } catch (IllegalArgumentException e2) {
            return false;
        }
    }
}

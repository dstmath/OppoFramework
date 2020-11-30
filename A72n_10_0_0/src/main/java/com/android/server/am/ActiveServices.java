package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ServiceStartArgs;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.webkit.WebViewZygote;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.server.AppStateTracker;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ServiceRecord;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.engineer.OppoEngineerFunctionManager;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.AppFrozenWhiteListHelper;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.theia.NoFocusWindow;
import com.android.server.uri.NeededUriGrants;
import com.android.server.usage.AppStandbyController;
import com.android.server.wm.ActivityServiceConnectionsHolder;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class ActiveServices extends OppoBaseActiveServices {
    static final boolean DEBUG_ACTIVE_SERVICES = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    static final boolean DEBUG_AGING_VERSION = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG", "0"));
    private static boolean DEBUG_DELAYED_SERVICE = ActivityManagerDebugConfig.DEBUG_SERVICE;
    private static boolean DEBUG_DELAYED_STARTS = DEBUG_DELAYED_SERVICE;
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    private static boolean LOG_SERVICE_START_STOP = false;
    static final int SERVICE_BACKGROUND_TIMEOUT = 200000;
    static final int SERVICE_START_FOREGROUND_TIMEOUT = 10000;
    static final int SERVICE_TIMEOUT = 20000;
    private static final boolean SHOW_DUNGEON_NOTIFICATION = false;
    private static final String TAG = "ActivityManager";
    private static final String TAG_MU = "ActivityManager_MU";
    private static final String TAG_SERVICE = (TAG + ActivityManagerDebugConfig.POSTFIX_SERVICE);
    private static final String TAG_SERVICE_EXECUTING = (TAG + ActivityManagerDebugConfig.POSTFIX_SERVICE_EXECUTING);
    final ActivityManagerService mAm;
    final ArrayList<ServiceRecord> mDestroyingServices = new ArrayList<>();
    String mLastAnrDump;
    final Runnable mLastAnrDumpClearer;
    final int mMaxStartingBackground;
    final ArrayList<ServiceRecord> mPendingServices = new ArrayList<>();
    final ArrayList<ServiceRecord> mRestartingServices = new ArrayList<>();
    boolean mScreenOn;
    final ArrayMap<IBinder, ArrayList<ConnectionRecord>> mServiceConnections = new ArrayMap<>();
    final SparseArray<ServiceMap> mServiceMap = new SparseArray<>();
    private ArrayList<ServiceRecord> mTmpCollectionResults = null;

    /* access modifiers changed from: package-private */
    public class ForcedStandbyListener extends AppStateTracker.Listener {
        ForcedStandbyListener() {
        }

        @Override // com.android.server.AppStateTracker.Listener
        public void stopForegroundServicesForUidPackage(int uid, String packageName) {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ServiceMap smap = ActiveServices.this.getServiceMapLocked(UserHandle.getUserId(uid));
                    int N = smap.mServicesByInstanceName.size();
                    ArrayList<ServiceRecord> toStop = new ArrayList<>(N);
                    for (int i = 0; i < N; i++) {
                        ServiceRecord r = smap.mServicesByInstanceName.valueAt(i);
                        if ((uid == r.serviceInfo.applicationInfo.uid || packageName.equals(r.serviceInfo.packageName)) && r.isForeground) {
                            toStop.add(r);
                        }
                    }
                    int numToStop = toStop.size();
                    if (numToStop > 0 && ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                        Slog.i(ActiveServices.TAG, "Package " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + uid + " entering FAS with foreground services");
                    }
                    for (int i2 = 0; i2 < numToStop; i2++) {
                        ServiceRecord r2 = toStop.get(i2);
                        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                            Slog.i(ActiveServices.TAG, "  Stopping fg for service " + r2);
                        }
                        ActiveServices.this.setServiceForegroundInnerLocked(r2, 0, null, 0, 0);
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ActiveForegroundApp {
        boolean mAppOnTop;
        long mEndTime;
        long mHideTime;
        CharSequence mLabel;
        int mNumActive;
        String mPackageName;
        boolean mShownWhileScreenOn;
        boolean mShownWhileTop;
        long mStartTime;
        long mStartVisibleTime;
        int mUid;

        ActiveForegroundApp() {
        }
    }

    /* access modifiers changed from: package-private */
    public final class ServiceMap extends Handler {
        static final int MSG_BG_START_TIMEOUT = 1;
        static final int MSG_UPDATE_FOREGROUND_APPS = 2;
        final ArrayMap<String, ActiveForegroundApp> mActiveForegroundApps = new ArrayMap<>();
        boolean mActiveForegroundAppsChanged;
        final ArrayList<ServiceRecord> mDelayedStartList = new ArrayList<>();
        final ArrayMap<ComponentName, ServiceRecord> mServicesByInstanceName = new ArrayMap<>();
        final ArrayMap<Intent.FilterComparison, ServiceRecord> mServicesByIntent = new ArrayMap<>();
        final ArrayList<ServiceRecord> mStartingBackground = new ArrayList<>();
        final int mUserId;

        ServiceMap(Looper looper, int userId) {
            super(looper);
            this.mUserId = userId;
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                synchronized (ActiveServices.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        rescheduleDelayedStartsLocked();
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 2) {
                ActiveServices.this.updateForegroundApps(this);
            }
        }

        /* access modifiers changed from: package-private */
        public void ensureNotStartingBackgroundLocked(ServiceRecord r) {
            if (this.mStartingBackground.remove(r)) {
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    String str = ActiveServices.TAG_SERVICE;
                    Slog.v(str, "No longer background starting: " + r);
                }
                rescheduleDelayedStartsLocked();
            }
            if (this.mDelayedStartList.remove(r) && ActiveServices.DEBUG_DELAYED_STARTS) {
                String str2 = ActiveServices.TAG_SERVICE;
                Slog.v(str2, "No longer delaying start: " + r);
            }
        }

        /* access modifiers changed from: package-private */
        public void rescheduleDelayedStartsLocked() {
            removeMessages(1);
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int N = this.mStartingBackground.size();
            while (i < N) {
                ServiceRecord r = this.mStartingBackground.get(i);
                if (r.startingBgTimeout <= now) {
                    Slog.i(ActiveServices.TAG, "Waited long enough for: " + r);
                    this.mStartingBackground.remove(i);
                    N += -1;
                    i += -1;
                }
                i++;
            }
            while (this.mDelayedStartList.size() > 0 && this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                ServiceRecord r2 = this.mDelayedStartList.remove(0);
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    String str = ActiveServices.TAG_SERVICE;
                    Slog.v(str, "REM FR DELAY LIST (exec next): " + r2);
                }
                if (ActiveServices.DEBUG_DELAYED_SERVICE && this.mDelayedStartList.size() > 0) {
                    Slog.v(ActiveServices.TAG_SERVICE, "Remaining delayed list:");
                    for (int i2 = 0; i2 < this.mDelayedStartList.size(); i2++) {
                        String str2 = ActiveServices.TAG_SERVICE;
                        Slog.v(str2, "  #" + i2 + ": " + this.mDelayedStartList.get(i2));
                    }
                }
                r2.delayed = false;
                if (r2.pendingStarts.size() <= 0) {
                    Slog.wtf(ActiveServices.TAG, "**** NO PENDING STARTS! " + r2 + " startReq=" + r2.startRequested + " delayedStop=" + r2.delayedStop);
                } else {
                    try {
                        ActiveServices.this.startServiceInnerLocked(this, r2.pendingStarts.get(0).intent, r2, false, true);
                    } catch (TransactionTooLargeException e) {
                    }
                }
            }
            if (this.mStartingBackground.size() > 0) {
                ServiceRecord next = this.mStartingBackground.get(0);
                long when = next.startingBgTimeout > now ? next.startingBgTimeout : now;
                if (ActiveServices.DEBUG_DELAYED_SERVICE) {
                    String str3 = ActiveServices.TAG_SERVICE;
                    Slog.v(str3, "Top bg start is " + next + ", can delay others up to " + when);
                }
                sendMessageAtTime(obtainMessage(1), when);
            }
            if (this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                ActiveServices.this.mAm.backgroundServicesFinishedLocked(this.mUserId);
            }
        }
    }

    public ActiveServices(ActivityManagerService service) {
        super(service);
        int i = 1;
        this.mScreenOn = true;
        this.mLastAnrDumpClearer = new Runnable() {
            /* class com.android.server.am.ActiveServices.AnonymousClass1 */

            public void run() {
                synchronized (ActiveServices.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ActiveServices.this.mLastAnrDump = null;
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        };
        this.mAm = service;
        int maxBg = 0;
        try {
            maxBg = Integer.parseInt(SystemProperties.get("ro.config.max_starting_bg", "0"));
        } catch (RuntimeException e) {
        }
        if (maxBg > 0) {
            i = maxBg;
        } else if (!ActivityManager.isLowRamDeviceStatic()) {
            i = 8;
        }
        this.mMaxStartingBackground = i;
    }

    /* access modifiers changed from: package-private */
    public void systemServicesReady() {
        ((AppStateTracker) LocalServices.getService(AppStateTracker.class)).addListener(new ForcedStandbyListener());
    }

    /* access modifiers changed from: package-private */
    public ServiceRecord getServiceByNameLocked(ComponentName name, int callingUser) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "getServiceByNameLocked(" + name + "), callingUser = " + callingUser);
        }
        return getServiceMapLocked(callingUser).mServicesByInstanceName.get(name);
    }

    /* access modifiers changed from: package-private */
    public boolean hasBackgroundServicesLocked(int callingUser) {
        ServiceMap smap = this.mServiceMap.get(callingUser);
        return smap != null && smap.mStartingBackground.size() >= this.mMaxStartingBackground;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ServiceMap getServiceMapLocked(int callingUser) {
        ServiceMap smap = this.mServiceMap.get(callingUser);
        if (smap != null) {
            return smap;
        }
        ServiceMap smap2 = new ServiceMap(this.mAm.mHandler.getLooper(), callingUser);
        this.mServiceMap.put(callingUser, smap2);
        return smap2;
    }

    /* access modifiers changed from: package-private */
    public ArrayMap<ComponentName, ServiceRecord> getServicesLocked(int callingUser) {
        return getServiceMapLocked(callingUser).mServicesByInstanceName;
    }

    private boolean appRestrictedAnyInBackground(int uid, String packageName) {
        return this.mAm.mAppOpsService.checkOperation(70, uid, packageName) != 0;
    }

    /* access modifiers changed from: package-private */
    public ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int callingPid, int callingUid, boolean fgRequired, String callingPackage, int userId) throws TransactionTooLargeException {
        return startServiceLocked(caller, service, resolvedType, callingPid, callingUid, fgRequired, callingPackage, userId, false);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0223  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x02ae  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x02d0 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02d1  */
    public ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int callingPid, int callingUid, boolean fgRequired, String callingPackage, int userId, boolean allowBackgroundActivityStarts) throws TransactionTooLargeException {
        boolean callerFg;
        boolean forcedStandby;
        boolean callerFg2;
        boolean callerFg3;
        boolean forceSilentAbort;
        String str;
        boolean forceSilentAbort2;
        boolean callerFg4;
        boolean fgRequired2;
        String str2;
        boolean addToStarting;
        int allowed;
        if (DEBUG_DELAYED_STARTS) {
            Slog.v(TAG_SERVICE, "startService: " + service + " type=" + resolvedType + " args=" + service.getExtras());
        }
        AppFrozenWhiteListHelper.getInstance(this.mAm.mContext).handleBindOrStartService(service, userId);
        if (caller != null) {
            ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
            if (callerApp != null) {
                boolean callerFg5 = callerApp.setSchedGroup != 0;
                if (OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventStartService(null, 0, 0, callerApp, callingPackage, service, "actionBlk") || OppoEngineerFunctionManager.shouldPreventStartService(service)) {
                    return null;
                }
                callerFg = callerFg5;
            } else {
                throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting service " + service);
            }
        } else {
            callerFg = true;
        }
        ServiceLookupResult res = retrieveServiceLocked(service, null, resolvedType, callingPackage, callingPid, callingUid, userId, true, callerFg, false, false);
        if (res == null) {
            return null;
        }
        if (res.record == null) {
            return new ComponentName("!", res.permission != null ? res.permission : "private to package");
        }
        ServiceRecord r = res.record;
        if (!this.mAm.mUserController.exists(r.userId)) {
            Slog.w(TAG, "Trying to start service with non-existent user! " + r.userId);
            return null;
        }
        boolean bgLaunch = !this.mAm.isUidActiveLocked(r.appInfo.uid);
        if (!bgLaunch || !appRestrictedAnyInBackground(r.appInfo.uid, r.packageName)) {
            callerFg2 = callerFg;
            forcedStandby = false;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                StringBuilder sb = new StringBuilder();
                sb.append("Forcing bg-only service start only for ");
                sb.append(r.shortInstanceName);
                sb.append(" : bgLaunch=");
                sb.append(bgLaunch);
                sb.append(" callerFg=");
                callerFg2 = callerFg;
                sb.append(callerFg2);
                Slog.d(TAG, sb.toString());
            } else {
                callerFg2 = callerFg;
            }
            forcedStandby = true;
        }
        if (fgRequired) {
            forceSilentAbort = false;
            callerFg3 = callerFg2;
            int mode = this.mAm.mAppOpsService.checkOperation(76, r.appInfo.uid, r.packageName);
            if (mode != 0) {
                if (mode == 1) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("startForegroundService not allowed due to app op: service ");
                    sb2.append(service);
                    sb2.append(" to ");
                    sb2.append(r.shortInstanceName);
                    sb2.append(" from pid=");
                    sb2.append(callingPid);
                    sb2.append(" uid=");
                    sb2.append(callingUid);
                    sb2.append(" pkg=");
                    str = callingPackage;
                    sb2.append(str);
                    Slog.w(TAG, sb2.toString());
                    forceSilentAbort = true;
                    forceSilentAbort2 = false;
                    if (!forcedStandby || (!r.startRequested && !forceSilentAbort2)) {
                        callerFg4 = callerFg3;
                        allowed = this.mAm.getAppStartModeLocked(r.appInfo.uid, r.packageName, r.appInfo.targetSdkVersion, callingPid, false, false, forcedStandby);
                        if (allowed != 0) {
                            str2 = " uid=";
                            fgRequired2 = forceSilentAbort2;
                        } else if (!OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).isAllowBackgroundRestrict(str, service)) {
                            Slog.w(TAG, "Background start not allowed: service " + service + " to " + r.shortInstanceName + " from pid=" + callingPid + " uid=" + callingUid + " pkg=" + str + " startFg?=" + forceSilentAbort2);
                            if (allowed == 1 || forceSilentAbort) {
                                return null;
                            }
                            if (!forcedStandby || !forceSilentAbort2) {
                                return new ComponentName("?", "app is in background uid " + this.mAm.mProcessList.getUidRecordLocked(r.appInfo.uid));
                            }
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.v(TAG, "Silently dropping foreground service launch due to FAS");
                            }
                            return null;
                        } else {
                            str2 = " uid=";
                            fgRequired2 = forceSilentAbort2;
                        }
                    } else {
                        callerFg4 = callerFg3;
                        fgRequired2 = forceSilentAbort2;
                        str2 = " uid=";
                    }
                    if (!OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventStartService(r, callingPid, callingUid, null, callingPackage, service, "startService")) {
                        return null;
                    }
                    OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).monitorAppStartupInfo(callingPid, callingUid, null, service, r.appInfo, "startService");
                    if (!OppoFeatureCache.get(IColorHansManager.DEFAULT).hansServiceIfNeeded(callingUid, callingPackage, r.appInfo.uid, r.packageName, r.name.getClassName(), false)) {
                        return null;
                    }
                    if (r.appInfo.targetSdkVersion < 26 && fgRequired2) {
                        if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK || ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                            Slog.i(TAG, "startForegroundService() but host targets " + r.appInfo.targetSdkVersion + " - not requiring startForeground()");
                        }
                        fgRequired2 = false;
                    }
                    NeededUriGrants neededGrants = this.mAm.mUgmInternal.checkGrantUriPermissionFromIntent(callingUid, r.packageName, service, service.getFlags(), null, r.userId);
                    if (!requestStartTargetPermissionsReviewIfNeededLocked(r, callingPackage, callingUid, service, callerFg4, userId)) {
                        return null;
                    }
                    if (unscheduleServiceRestartLocked(r, callingUid, false) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "START SERVICE WHILE RESTART PENDING: " + r);
                    }
                    r.lastActivity = SystemClock.uptimeMillis();
                    r.startRequested = true;
                    r.delayedStop = false;
                    r.fgRequired = fgRequired2;
                    r.pendingStarts.add(new ServiceRecord.StartItem(r, false, r.makeNextStartId(), service, neededGrants, callingUid));
                    if (fgRequired2) {
                        ServiceState stracker = r.getTracker();
                        if (stracker != null) {
                            stracker.setForeground(true, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                        }
                        this.mAm.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName, true);
                    }
                    ServiceMap smap = getServiceMapLocked(r.userId);
                    boolean addToStarting2 = false;
                    if (callerFg4 || fgRequired2 || r.app != null || !this.mAm.mUserController.hasStartedUserState(r.userId)) {
                        if (DEBUG_DELAYED_STARTS) {
                            if (callerFg4 || fgRequired2) {
                                Slog.v(TAG_SERVICE, "Not potential delay (callerFg=" + callerFg4 + str2 + callingUid + " pid=" + callingPid + " fgRequired=" + fgRequired2 + "): " + r);
                            } else if (r.app != null) {
                                Slog.v(TAG_SERVICE, "Not potential delay (cur app=" + r.app + "): " + r);
                            } else {
                                Slog.v(TAG_SERVICE, "Not potential delay (user " + r.userId + " not started): " + r);
                            }
                        }
                        addToStarting = false;
                    } else {
                        ProcessRecord proc = this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid, false);
                        if (proc == null || proc.getCurProcState() > 12) {
                            if (DEBUG_DELAYED_SERVICE) {
                                Slog.v(TAG_SERVICE, "Potential start delay of " + r + " in " + proc);
                            }
                            if (r.delayed) {
                                if (DEBUG_DELAYED_STARTS) {
                                    Slog.v(TAG_SERVICE, "Continuing to delay: " + r);
                                }
                                return r.name;
                            } else if (smap.mStartingBackground.size() >= this.mMaxStartingBackground) {
                                Slog.i(TAG_SERVICE, "Delaying start of: " + r);
                                smap.mDelayedStartList.add(r);
                                r.delayed = true;
                                return r.name;
                            } else {
                                if (DEBUG_DELAYED_STARTS) {
                                    Slog.v(TAG_SERVICE, "Not delaying: " + r);
                                }
                                addToStarting2 = true;
                            }
                        } else if (proc.getCurProcState() >= 11) {
                            addToStarting2 = true;
                            if (DEBUG_DELAYED_STARTS) {
                                Slog.v(TAG_SERVICE, "Not delaying, but counting as bg: " + r);
                            }
                        } else if (DEBUG_DELAYED_STARTS) {
                            StringBuilder sb3 = new StringBuilder(128);
                            sb3.append("Not potential delay (state=");
                            sb3.append(proc.getCurProcState());
                            sb3.append(' ');
                            sb3.append(proc.adjType);
                            String reason = proc.makeAdjReason();
                            if (reason != null) {
                                sb3.append(' ');
                                sb3.append(reason);
                            }
                            sb3.append("): ");
                            sb3.append(r.toString());
                            Slog.v(TAG_SERVICE, sb3.toString());
                        }
                        addToStarting = addToStarting2;
                    }
                    if (allowBackgroundActivityStarts) {
                        r.whitelistBgActivityStartsOnServiceStart();
                    }
                    return startServiceInnerLocked(smap, service, r, callerFg4, addToStarting);
                } else if (mode != 3) {
                    return new ComponentName("!!", "foreground not allowed as per app op");
                }
            }
            str = callingPackage;
        } else {
            str = callingPackage;
            forceSilentAbort = false;
            callerFg3 = callerFg2;
        }
        forceSilentAbort2 = fgRequired;
        if (!forcedStandby) {
        }
        callerFg4 = callerFg3;
        allowed = this.mAm.getAppStartModeLocked(r.appInfo.uid, r.packageName, r.appInfo.targetSdkVersion, callingPid, false, false, forcedStandby);
        if (allowed != 0) {
        }
        if (!OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventStartService(r, callingPid, callingUid, null, callingPackage, service, "startService")) {
        }
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(ServiceRecord r, String callingPackage, int callingUid, Intent service, boolean callerFg, final int userId) {
        if (!this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(r.packageName, r.userId)) {
            return true;
        }
        if (!callerFg) {
            Slog.w(TAG, "u" + r.userId + " Starting a service in package" + r.packageName + " requires a permissions review");
            return false;
        }
        IIntentSender target = this.mAm.mPendingIntentController.getIntentSender(4, callingPackage, callingUid, userId, null, null, 0, new Intent[]{service}, new String[]{service.resolveType(this.mAm.mContext.getContentResolver())}, 1409286144, null);
        final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
        intent.addFlags(411041792);
        intent.putExtra("android.intent.extra.PACKAGE_NAME", r.packageName);
        intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
        if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
            Slog.i(TAG, "u" + r.userId + " Launching permission review for package " + r.packageName);
        }
        this.mAm.mHandler.post(new Runnable() {
            /* class com.android.server.am.ActiveServices.AnonymousClass2 */

            public void run() {
                ActiveServices.this.mAm.mContext.startActivityAsUser(intent, new UserHandle(userId));
            }
        });
        return false;
    }

    /* access modifiers changed from: package-private */
    public ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r, boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
        ServiceState stracker = r.getTracker();
        if (stracker != null) {
            stracker.setStarted(true, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
        }
        boolean first = false;
        r.callStart = false;
        StatsLog.write(99, r.appInfo.uid, r.name.getPackageName(), r.name.getClassName(), 1);
        synchronized (r.stats.getBatteryStats()) {
            r.stats.startRunningLocked();
        }
        String error = bringUpServiceLocked(r, service.getFlags(), callerFg, false, false);
        if (error != null) {
            return new ComponentName("!!", error);
        }
        if (r.startRequested && addToStarting) {
            if (smap.mStartingBackground.size() == 0) {
                first = true;
            }
            smap.mStartingBackground.add(r);
            r.startingBgTimeout = SystemClock.uptimeMillis() + this.mAm.mConstants.BG_START_TIMEOUT;
            if (DEBUG_DELAYED_SERVICE) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r, here);
            } else if (DEBUG_DELAYED_STARTS) {
                Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r);
            }
            if (first) {
                smap.rescheduleDelayedStartsLocked();
            }
        } else if (callerFg || r.fgRequired) {
            smap.ensureNotStartingBackgroundLocked(r);
        }
        return r.name;
    }

    private void stopServiceLocked(ServiceRecord service) {
        if (service.delayed) {
            if (DEBUG_DELAYED_STARTS) {
                String str = TAG_SERVICE;
                Slog.v(str, "Delaying stop of pending: " + service);
            }
            service.delayedStop = true;
            return;
        }
        StatsLog.write(99, service.appInfo.uid, service.name.getPackageName(), service.name.getClassName(), 2);
        synchronized (service.stats.getBatteryStats()) {
            service.stats.stopRunningLocked();
        }
        service.startRequested = false;
        if (service.tracker != null) {
            service.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        service.callStart = false;
        bringDownServiceIfNeededLocked(service, false, false);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public int stopServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            String str = TAG_SERVICE;
            Slog.v(str, "stopService: " + service + " type=" + resolvedType);
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (caller == null || callerApp != null) {
            ServiceLookupResult r = retrieveServiceLocked(service, null, resolvedType, null, Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, false, false);
            if (r == null) {
                return 0;
            }
            if (r.record == null) {
                return -1;
            }
            long origId = Binder.clearCallingIdentity();
            try {
                stopServiceLocked(r.record);
                Binder.restoreCallingIdentity(origId);
                return 1;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        } else {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when stopping service " + service);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopInBackgroundLocked(int uid) {
        ServiceMap services = this.mServiceMap.get(UserHandle.getUserId(uid));
        ArrayList<ServiceRecord> stopping = null;
        if (services != null) {
            for (int i = services.mServicesByInstanceName.size() - 1; i >= 0; i--) {
                ServiceRecord service = services.mServicesByInstanceName.valueAt(i);
                if (service.appInfo.uid == uid && service.startRequested && this.mAm.getAppStartModeLocked(service.appInfo.uid, service.packageName, service.appInfo.targetSdkVersion, -1, false, false, false) != 0) {
                    if (stopping == null) {
                        stopping = new ArrayList<>();
                    }
                    String compName = service.shortInstanceName;
                    EventLogTags.writeAmStopIdleService(service.appInfo.uid, compName);
                    StringBuilder sb = new StringBuilder(64);
                    sb.append("Stopping service due to app idle: ");
                    UserHandle.formatUid(sb, service.appInfo.uid);
                    sb.append(StringUtils.SPACE);
                    TimeUtils.formatDuration(service.createRealTime - SystemClock.elapsedRealtime(), sb);
                    sb.append(StringUtils.SPACE);
                    sb.append(compName);
                    Slog.w(TAG, sb.toString());
                    stopping.add(service);
                    if (appRestrictedAnyInBackground(service.appInfo.uid, service.packageName)) {
                        cancelForegroundNotificationLocked(service);
                    }
                }
            }
            if (stopping != null) {
                for (int i2 = stopping.size() - 1; i2 >= 0; i2--) {
                    ServiceRecord service2 = stopping.get(i2);
                    service2.delayed = false;
                    services.ensureNotStartingBackgroundLocked(service2);
                    stopServiceLocked(service2);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void killMisbehavingService(ServiceRecord r, int appUid, int appPid, String localPackageName) {
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                stopServiceLocked(r);
                this.mAm.crashApplication(appUid, appPid, localPackageName, -1, "Bad notification for startForeground", true);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public IBinder peekServiceLocked(Intent service, String resolvedType, String callingPackage) {
        ServiceLookupResult r = retrieveServiceLocked(service, null, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.getCallingUserId(), false, false, false, false);
        if (r == null) {
            return null;
        }
        if (r.record != null) {
            IntentBindRecord ib = r.record.bindings.get(r.record.intent);
            if (ib != null) {
                return ib.binder;
            }
            return null;
        }
        throw new SecurityException("Permission Denial: Accessing service from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + r.permission);
    }

    /* access modifiers changed from: package-private */
    public boolean stopServiceTokenLocked(ComponentName className, IBinder token, int startId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            String str = TAG_SERVICE;
            Slog.v(str, "stopServiceToken: " + className + StringUtils.SPACE + token + " startId=" + startId);
        }
        ServiceRecord r = findServiceLocked(className, token, UserHandle.getCallingUserId());
        if (r == null) {
            return false;
        }
        if (startId >= 0) {
            ServiceRecord.StartItem si = r.findDeliveredStart(startId, false, false);
            if (si != null) {
                while (r.deliveredStarts.size() > 0) {
                    ServiceRecord.StartItem cur = r.deliveredStarts.remove(0);
                    cur.removeUriPermissionsLocked();
                    if (cur == si) {
                        break;
                    }
                }
            }
            if (r.getLastStartId() != startId) {
                return false;
            }
            if (r.deliveredStarts.size() > 0) {
                Slog.w(TAG, "stopServiceToken startId " + startId + " is last, but have " + r.deliveredStarts.size() + " remaining args");
            }
        }
        StatsLog.write(99, r.appInfo.uid, r.name.getPackageName(), r.name.getClassName(), 2);
        synchronized (r.stats.getBatteryStats()) {
            r.stats.stopRunningLocked();
        }
        r.startRequested = false;
        if (r.tracker != null) {
            r.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        r.callStart = false;
        long origId = Binder.clearCallingIdentity();
        bringDownServiceIfNeededLocked(r, false, false);
        Binder.restoreCallingIdentity(origId);
        return true;
    }

    public void setServiceForegroundLocked(ComponentName className, IBinder token, int id, Notification notification, int flags, int foregroundServiceType) {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        try {
            ServiceRecord r = findServiceLocked(className, token, userId);
            if (r != null) {
                setServiceForegroundInnerLocked(r, id, notification, flags, foregroundServiceType);
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public int getForegroundServiceTypeLocked(ComponentName className, IBinder token) {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        int ret = 0;
        try {
            ServiceRecord r = findServiceLocked(className, token, userId);
            if (r != null) {
                ret = r.foregroundServiceType;
            }
            return ret;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean foregroundAppShownEnoughLocked(ActiveForegroundApp aa, long nowElapsed) {
        long j;
        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
            Slog.d(TAG, "Shown enough: pkg=" + aa.mPackageName + ", uid=" + aa.mUid);
        }
        aa.mHideTime = JobStatus.NO_LATEST_RUNTIME;
        if (aa.mShownWhileTop) {
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return true;
            }
            Slog.d(TAG, "YES - shown while on top");
            return true;
        } else if (this.mScreenOn || aa.mShownWhileScreenOn) {
            long minTime = aa.mStartVisibleTime;
            if (aa.mStartTime != aa.mStartVisibleTime) {
                j = this.mAm.mConstants.FGSERVICE_SCREEN_ON_AFTER_TIME;
            } else {
                j = this.mAm.mConstants.FGSERVICE_MIN_SHOWN_TIME;
            }
            long minTime2 = minTime + j;
            if (nowElapsed >= minTime2) {
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "YES - shown long enough with screen on");
                }
                return true;
            }
            long reportTime = this.mAm.mConstants.FGSERVICE_MIN_REPORT_TIME + nowElapsed;
            aa.mHideTime = reportTime > minTime2 ? reportTime : minTime2;
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return false;
            }
            Slog.d(TAG, "NO -- wait " + (aa.mHideTime - nowElapsed) + " with screen on");
            return false;
        } else {
            long minTime3 = aa.mEndTime + this.mAm.mConstants.FGSERVICE_SCREEN_ON_BEFORE_TIME;
            if (nowElapsed >= minTime3) {
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "YES - gone long enough with screen off");
                }
                return true;
            }
            aa.mHideTime = minTime3;
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return false;
            }
            Slog.d(TAG, "NO -- wait " + (aa.mHideTime - nowElapsed) + " with screen off");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateForegroundApps(ServiceMap smap) {
        ArrayList<ActiveForegroundApp> active = null;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long now = SystemClock.elapsedRealtime();
                long nextUpdateTime = JobStatus.NO_LATEST_RUNTIME;
                if (smap != null) {
                    if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                        Slog.d(TAG, "Updating foreground apps for user " + smap.mUserId);
                    }
                    for (int i = smap.mActiveForegroundApps.size() - 1; i >= 0; i--) {
                        ActiveForegroundApp aa = smap.mActiveForegroundApps.valueAt(i);
                        if (aa.mEndTime != 0) {
                            if (foregroundAppShownEnoughLocked(aa, now)) {
                                smap.mActiveForegroundApps.removeAt(i);
                                smap.mActiveForegroundAppsChanged = true;
                            } else if (aa.mHideTime < nextUpdateTime) {
                                nextUpdateTime = aa.mHideTime;
                            }
                        }
                        if (!aa.mAppOnTop) {
                            if (active == null) {
                                active = new ArrayList<>();
                            }
                            if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                                Slog.d(TAG, "Adding active: pkg=" + aa.mPackageName + ", uid=" + aa.mUid);
                            }
                            active.add(aa);
                        }
                    }
                    smap.removeMessages(2);
                    if (nextUpdateTime < JobStatus.NO_LATEST_RUNTIME) {
                        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                            Slog.d(TAG, "Next update time in: " + (nextUpdateTime - now));
                        }
                        smap.sendMessageAtTime(smap.obtainMessage(2), (SystemClock.uptimeMillis() + nextUpdateTime) - SystemClock.elapsedRealtime());
                    }
                }
                if (smap.mActiveForegroundAppsChanged) {
                    smap.mActiveForegroundAppsChanged = false;
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void requestUpdateActiveForegroundAppsLocked(ServiceMap smap, long timeElapsed) {
        Message msg = smap.obtainMessage(2);
        if (timeElapsed != 0) {
            smap.sendMessageAtTime(msg, (SystemClock.uptimeMillis() + timeElapsed) - SystemClock.elapsedRealtime());
            return;
        }
        smap.mActiveForegroundAppsChanged = true;
        smap.sendMessage(msg);
    }

    private void decActiveForegroundAppLocked(ServiceMap smap, ServiceRecord r) {
        ActiveForegroundApp active = smap.mActiveForegroundApps.get(r.packageName);
        if (active != null) {
            active.mNumActive--;
            if (active.mNumActive <= 0) {
                active.mEndTime = SystemClock.elapsedRealtime();
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "Ended running of service");
                }
                if (foregroundAppShownEnoughLocked(active, active.mEndTime)) {
                    smap.mActiveForegroundApps.remove(r.packageName);
                    smap.mActiveForegroundAppsChanged = true;
                    requestUpdateActiveForegroundAppsLocked(smap, 0);
                } else if (active.mHideTime < JobStatus.NO_LATEST_RUNTIME) {
                    requestUpdateActiveForegroundAppsLocked(smap, active.mHideTime);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateScreenStateLocked(boolean screenOn) {
        if (this.mScreenOn != screenOn) {
            this.mScreenOn = screenOn;
            if (screenOn) {
                long nowElapsed = SystemClock.elapsedRealtime();
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "Screen turned on");
                }
                for (int i = this.mServiceMap.size() - 1; i >= 0; i--) {
                    ServiceMap smap = this.mServiceMap.valueAt(i);
                    long nextUpdateTime = JobStatus.NO_LATEST_RUNTIME;
                    boolean changed = false;
                    for (int j = smap.mActiveForegroundApps.size() - 1; j >= 0; j--) {
                        ActiveForegroundApp active = smap.mActiveForegroundApps.valueAt(j);
                        if (active.mEndTime != 0) {
                            if (!active.mShownWhileScreenOn && active.mStartVisibleTime == active.mStartTime) {
                                active.mStartVisibleTime = nowElapsed;
                                active.mEndTime = nowElapsed;
                            }
                            if (foregroundAppShownEnoughLocked(active, nowElapsed)) {
                                smap.mActiveForegroundApps.remove(active.mPackageName);
                                smap.mActiveForegroundAppsChanged = true;
                                changed = true;
                            } else if (active.mHideTime < nextUpdateTime) {
                                nextUpdateTime = active.mHideTime;
                            }
                        } else if (!active.mShownWhileScreenOn) {
                            active.mShownWhileScreenOn = true;
                            active.mStartVisibleTime = nowElapsed;
                        }
                    }
                    if (changed) {
                        requestUpdateActiveForegroundAppsLocked(smap, 0);
                    } else if (nextUpdateTime < JobStatus.NO_LATEST_RUNTIME) {
                        requestUpdateActiveForegroundAppsLocked(smap, nextUpdateTime);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void foregroundServiceProcStateChangedLocked(UidRecord uidRec) {
        ServiceMap smap = this.mServiceMap.get(UserHandle.getUserId(uidRec.uid));
        if (smap != null) {
            boolean changed = false;
            for (int j = smap.mActiveForegroundApps.size() - 1; j >= 0; j--) {
                ActiveForegroundApp active = smap.mActiveForegroundApps.valueAt(j);
                if (active.mUid == uidRec.uid) {
                    if (uidRec.getCurProcState() <= 2) {
                        if (!active.mAppOnTop) {
                            active.mAppOnTop = true;
                            changed = true;
                        }
                        active.mShownWhileTop = true;
                    } else if (active.mAppOnTop) {
                        active.mAppOnTop = false;
                        changed = true;
                    }
                }
            }
            if (changed) {
                requestUpdateActiveForegroundAppsLocked(smap, 0);
            }
        }
    }

    private boolean appIsTopLocked(int uid) {
        return this.mAm.getUidState(uid) <= 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setServiceForegroundInnerLocked(ServiceRecord r, int id, Notification notification, int flags, int foregroundServiceType) {
        int foregroundServiceType2;
        ServiceState stracker;
        if (id == 0) {
            if (r.isForeground) {
                ServiceMap smap = getServiceMapLocked(r.userId);
                if (smap != null) {
                    decActiveForegroundAppLocked(smap, r);
                }
                r.isForeground = false;
                ServiceState stracker2 = r.getTracker();
                if (stracker2 != null) {
                    stracker2.setForeground(false, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                }
                this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName);
                StatsLog.write(60, r.appInfo.uid, r.shortInstanceName, 2);
                this.mAm.updateForegroundServiceUsageStats(r.name, r.userId, false);
                if (r.app != null) {
                    ProcessRecord proc = r.app;
                    for (int i = proc.services.size() - 1; i >= 0; i--) {
                        ServiceRecord sr = proc.services.valueAt(i);
                        if (sr.isForeground && sr.foregroundId == r.foregroundId) {
                            sr.isForeground = false;
                            Slog.d(TAG, String.format("%s(foregroundId=%d) not foreground anymore", sr, Integer.valueOf(sr.foregroundId)));
                        }
                    }
                    this.mAm.updateLruProcessLocked(r.app, false, null);
                    updateServiceForegroundLocked(r.app, true);
                }
            }
            if ((flags & 1) != 0) {
                r.cancelNotification();
                r.foregroundId = 0;
                r.foregroundNoti = null;
            } else if (r.appInfo.targetSdkVersion >= 21) {
                r.stripForegroundServiceFlagFromNotification();
                if ((flags & 2) != 0) {
                    r.foregroundId = 0;
                    r.foregroundNoti = null;
                }
            }
        } else if (notification != null) {
            if (r.appInfo.isInstantApp()) {
                int mode = this.mAm.mAppOpsService.checkOperation(68, r.appInfo.uid, r.appInfo.packageName);
                if (mode != 0) {
                    if (mode == 1) {
                        Slog.w(TAG, "Instant app " + r.appInfo.packageName + " does not have permission to create foreground services, ignoring.");
                        return;
                    } else if (mode != 2) {
                        this.mAm.enforcePermission("android.permission.INSTANT_APP_FOREGROUND_SERVICE", r.app.pid, r.appInfo.uid, "startForeground");
                    } else {
                        throw new SecurityException("Instant app " + r.appInfo.packageName + " does not have permission to create foreground services");
                    }
                }
                foregroundServiceType2 = foregroundServiceType;
            } else {
                if (r.appInfo.targetSdkVersion >= 28) {
                    this.mAm.enforcePermission("android.permission.FOREGROUND_SERVICE", r.app.pid, r.appInfo.uid, "startForeground");
                }
                int manifestType = r.serviceInfo.getForegroundServiceType();
                if (foregroundServiceType == -1) {
                    foregroundServiceType2 = manifestType;
                } else {
                    foregroundServiceType2 = foregroundServiceType;
                }
                if ((foregroundServiceType2 & manifestType) != foregroundServiceType2) {
                    throw new IllegalArgumentException("foregroundServiceType " + String.format("0x%08X", Integer.valueOf(foregroundServiceType2)) + " is not a subset of foregroundServiceType attribute " + String.format("0x%08X", Integer.valueOf(manifestType)) + " in service element of manifest file");
                }
            }
            boolean alreadyStartedOp = false;
            boolean stopProcStatsOp = false;
            if (r.fgRequired) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                    Slog.i(TAG, "Service called startForeground() as required: " + r);
                }
                r.fgRequired = false;
                r.fgWaiting = false;
                stopProcStatsOp = true;
                alreadyStartedOp = true;
                this.mAm.mHandler.removeMessages(66, r);
            }
            boolean ignoreForeground = false;
            try {
                int mode2 = this.mAm.mAppOpsService.checkOperation(76, r.appInfo.uid, r.packageName);
                if (mode2 != 0) {
                    if (mode2 == 1) {
                        Slog.w(TAG, "Service.startForeground() not allowed due to app op: service " + r.shortInstanceName);
                        ignoreForeground = true;
                    } else if (mode2 != 3) {
                        throw new SecurityException("Foreground not allowed as per app op");
                    }
                }
                if (!ignoreForeground && !appIsTopLocked(r.appInfo.uid) && appRestrictedAnyInBackground(r.appInfo.uid, r.packageName)) {
                    Slog.w(TAG, "Service.startForeground() not allowed due to bg restriction: service " + r.shortInstanceName);
                    updateServiceForegroundLocked(r.app, false);
                    ignoreForeground = true;
                }
                if (!ignoreForeground) {
                    if (r.foregroundId != id) {
                        cancelForegroundNotificationLocked(r);
                        r.foregroundId = id;
                    }
                    notification.flags |= 64;
                    r.foregroundNoti = notification;
                    r.foregroundServiceType = foregroundServiceType2;
                    if (!r.isForeground) {
                        ServiceMap smap2 = getServiceMapLocked(r.userId);
                        if (smap2 != null) {
                            ActiveForegroundApp active = smap2.mActiveForegroundApps.get(r.packageName);
                            if (active == null) {
                                active = new ActiveForegroundApp();
                                active.mPackageName = r.packageName;
                                active.mUid = r.appInfo.uid;
                                active.mShownWhileScreenOn = this.mScreenOn;
                                if (r.app != null) {
                                    boolean z = r.app.uidRecord.getCurProcState() <= 2;
                                    active.mShownWhileTop = z;
                                    active.mAppOnTop = z;
                                }
                                long elapsedRealtime = SystemClock.elapsedRealtime();
                                active.mStartVisibleTime = elapsedRealtime;
                                active.mStartTime = elapsedRealtime;
                                smap2.mActiveForegroundApps.put(r.packageName, active);
                                requestUpdateActiveForegroundAppsLocked(smap2, 0);
                            }
                            active.mNumActive++;
                        }
                        r.isForeground = true;
                        if (!stopProcStatsOp) {
                            ServiceState stracker3 = r.getTracker();
                            if (stracker3 != null) {
                                stracker3.setForeground(true, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                            }
                        } else {
                            stopProcStatsOp = false;
                        }
                        this.mAm.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName, true);
                        StatsLog.write(60, r.appInfo.uid, r.shortInstanceName, 1);
                        this.mAm.updateForegroundServiceUsageStats(r.name, r.userId, true);
                    }
                    r.postNotification();
                    if (r.app != null) {
                        updateServiceForegroundLocked(r.app, true);
                    }
                    getServiceMapLocked(r.userId).ensureNotStartingBackgroundLocked(r);
                    this.mAm.notifyPackageUse(r.serviceInfo.packageName, 2);
                } else if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "Suppressing startForeground() for FAS " + r);
                }
            } finally {
                if (stopProcStatsOp && (stracker = r.getTracker()) != null) {
                    stracker.setForeground(false, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                }
                if (alreadyStartedOp) {
                    this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName);
                }
            }
        } else {
            throw new IllegalArgumentException("null notification");
        }
    }

    private void cancelForegroundNotificationLocked(ServiceRecord r) {
        if (r.foregroundId != 0) {
            ServiceMap sm = getServiceMapLocked(r.userId);
            if (sm != null) {
                for (int i = sm.mServicesByInstanceName.size() - 1; i >= 0; i--) {
                    ServiceRecord other = sm.mServicesByInstanceName.valueAt(i);
                    if (other != r && other.foregroundId == r.foregroundId && other.packageName.equals(r.packageName)) {
                        return;
                    }
                }
            }
            r.cancelNotification();
        }
    }

    private void updateServiceForegroundLocked(ProcessRecord proc, boolean oomAdj) {
        boolean anyForeground = false;
        int fgServiceTypes = 0;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            ServiceRecord sr = proc.services.valueAt(i);
            if (sr.isForeground || sr.fgRequired) {
                anyForeground = true;
                fgServiceTypes |= sr.foregroundServiceType;
            }
        }
        this.mAm.updateProcessForegroundLocked(proc, anyForeground, fgServiceTypes, oomAdj);
    }

    private void updateWhitelistManagerLocked(ProcessRecord proc) {
        proc.whitelistManager = false;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            if (proc.services.valueAt(i).whitelistManager) {
                proc.whitelistManager = true;
                return;
            }
        }
    }

    public void updateServiceConnectionActivitiesLocked(ProcessRecord clientProc) {
        ArraySet<ProcessRecord> updatedProcesses = null;
        for (int i = 0; i < clientProc.connections.size(); i++) {
            ProcessRecord proc = clientProc.connections.valueAt(i).binding.service.app;
            if (!(proc == null || proc == clientProc)) {
                if (updatedProcesses == null) {
                    updatedProcesses = new ArraySet<>();
                } else if (updatedProcesses.contains(proc)) {
                }
                updatedProcesses.add(proc);
                updateServiceClientActivitiesLocked(proc, null, false);
            }
        }
    }

    private boolean updateServiceClientActivitiesLocked(ProcessRecord proc, ConnectionRecord modCr, boolean updateLru) {
        if (!(modCr == null || modCr.binding.client == null || modCr.binding.client.hasActivities())) {
            return false;
        }
        boolean anyClientActivities = false;
        for (int i = proc.services.size() - 1; i >= 0 && !anyClientActivities; i--) {
            ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = proc.services.valueAt(i).getConnections();
            for (int conni = connections.size() - 1; conni >= 0 && !anyClientActivities; conni--) {
                ArrayList<ConnectionRecord> clist = connections.valueAt(conni);
                int cri = clist.size() - 1;
                while (true) {
                    if (cri < 0) {
                        break;
                    }
                    ConnectionRecord cr = clist.get(cri);
                    if (!(cr.binding.client == null || cr.binding.client == proc || !cr.binding.client.hasActivities())) {
                        anyClientActivities = true;
                        break;
                    }
                    cri--;
                }
            }
        }
        if (anyClientActivities == proc.hasClientActivities()) {
            return false;
        }
        proc.setHasClientActivities(anyClientActivities);
        if (updateLru) {
            this.mAm.updateLruProcessLocked(proc, anyClientActivities, null);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int bindServiceLocked(IApplicationThread caller, IBinder token, Intent service, String resolvedType, final IServiceConnection connection, int flags, String instanceName, String callingPackage, final int userId) throws TransactionTooLargeException {
        ActivityServiceConnectionsHolder<ConnectionRecord> activity;
        int clientLabel;
        PendingIntent clientIntent;
        final Intent service2;
        boolean callerFg;
        boolean permissionsReviewRequired;
        ServiceRecord s;
        String str;
        Throwable th;
        AppBindRecord b;
        ConnectionRecord c;
        IBinder binder;
        ArrayList<ConnectionRecord> clist;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "bindService: " + service + " type=" + resolvedType + " conn=" + connection.asBinder() + " flags=0x" + Integer.toHexString(flags));
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (callerApp != null) {
            AppFrozenWhiteListHelper.getInstance(this.mAm.mContext).handleBindOrStartService(service, userId);
            if (OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventStartService(null, 0, 0, callerApp, callingPackage, service, "actionBlk") || OppoEngineerFunctionManager.shouldPreventStartService(service)) {
                return 0;
            }
            if (token != null) {
                ActivityServiceConnectionsHolder<ConnectionRecord> activity2 = this.mAm.mAtmInternal.getServiceConnectionsHolder(token);
                if (activity2 == null) {
                    Slog.w(TAG, "Binding with unknown activity: " + token);
                    return 0;
                }
                activity = activity2;
            } else {
                activity = null;
            }
            boolean isCallerSystem = callerApp.info.uid == 1000;
            if (isCallerSystem) {
                service.setDefusable(true);
                PendingIntent clientIntent2 = (PendingIntent) service.getParcelableExtra("android.intent.extra.client_intent");
                if (clientIntent2 != null) {
                    int clientLabel2 = service.getIntExtra("android.intent.extra.client_label", 0);
                    if (clientLabel2 != 0) {
                        service2 = service.cloneFilter();
                        clientLabel = clientLabel2;
                        clientIntent = clientIntent2;
                    } else {
                        service2 = service;
                        clientLabel = clientLabel2;
                        clientIntent = clientIntent2;
                    }
                } else {
                    service2 = service;
                    clientLabel = 0;
                    clientIntent = clientIntent2;
                }
            } else {
                service2 = service;
                clientLabel = 0;
                clientIntent = null;
            }
            if ((flags & 134217728) != 0) {
                this.mAm.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "BIND_TREAT_LIKE_ACTIVITY");
            }
            if ((flags & DumpState.DUMP_FROZEN) != 0 && !isCallerSystem) {
                throw new SecurityException("Non-system caller (pid=" + Binder.getCallingPid() + ") set BIND_SCHEDULE_LIKE_TOP_APP when binding service " + service2);
            } else if ((flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 && !isCallerSystem) {
                throw new SecurityException("Non-system caller " + caller + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_WHITELIST_MANAGEMENT when binding service " + service2);
            } else if ((flags & DumpState.DUMP_CHANGES) == 0 || isCallerSystem) {
                if ((flags & DumpState.DUMP_DEXOPT) != 0) {
                    this.mAm.enforceCallingPermission("android.permission.START_ACTIVITIES_FROM_BACKGROUND", "BIND_ALLOW_BACKGROUND_ACTIVITY_STARTS");
                }
                final boolean callerFg2 = callerApp.setSchedGroup != 0;
                ServiceLookupResult res = retrieveServiceLocked(service2, instanceName, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), userId, true, callerFg2, (flags & Integer.MIN_VALUE) != 0, (flags & DumpState.DUMP_CHANGES) != 0);
                if (res == null) {
                    return 0;
                }
                if (res.record == null) {
                    return -1;
                }
                final ServiceRecord s2 = res.record;
                if (OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventStartService(s2, Binder.getCallingPid(), Binder.getCallingUid(), callerApp, callingPackage, service2, "bindService")) {
                    return 0;
                }
                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).monitorAppStartupInfo(Binder.getCallingPid(), Binder.getCallingUid(), callerApp, service2, s2.appInfo, "bindService");
                if (!OppoFeatureCache.get(IColorHansManager.DEFAULT).hansServiceIfNeeded(Binder.getCallingUid(), callingPackage, s2.appInfo.uid, s2.packageName, s2.name.getClassName(), true)) {
                    return 0;
                }
                if (!this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s2.packageName, s2.userId)) {
                    callerFg = callerFg2;
                    s = s2;
                    str = TAG;
                    permissionsReviewRequired = false;
                } else if (!callerFg2) {
                    Slog.w(TAG, "u" + s2.userId + " Binding to a service in package" + s2.packageName + " requires a permissions review");
                    return 0;
                } else {
                    str = TAG;
                    s = s2;
                    permissionsReviewRequired = true;
                    callerFg = callerFg2;
                    RemoteCallback callback = new RemoteCallback(new RemoteCallback.OnResultListener() {
                        /* class com.android.server.am.ActiveServices.AnonymousClass3 */

                        public void onResult(Bundle result) {
                            synchronized (ActiveServices.this.mAm) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    long identity = Binder.clearCallingIdentity();
                                    try {
                                        if (ActiveServices.this.mPendingServices.contains(s2)) {
                                            if (!ActiveServices.this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s2.packageName, s2.userId)) {
                                                try {
                                                    ActiveServices.this.bringUpServiceLocked(s2, service2.getFlags(), callerFg2, false, false);
                                                } catch (RemoteException e) {
                                                }
                                            } else {
                                                ActiveServices.this.unbindServiceLocked(connection);
                                            }
                                            Binder.restoreCallingIdentity(identity);
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                        }
                                    } finally {
                                        Binder.restoreCallingIdentity(identity);
                                    }
                                } finally {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                        }
                    });
                    final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                    intent.addFlags(411041792);
                    intent.putExtra("android.intent.extra.PACKAGE_NAME", s.packageName);
                    intent.putExtra("android.intent.extra.REMOTE_CALLBACK", (Parcelable) callback);
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                        Slog.i(str, "u" + s.userId + " Launching permission review for package " + s.packageName);
                    }
                    this.mAm.mHandler.post(new Runnable() {
                        /* class com.android.server.am.ActiveServices.AnonymousClass4 */

                        public void run() {
                            ActiveServices.this.mAm.mContext.startActivityAsUser(intent, new UserHandle(userId));
                        }
                    });
                }
                long origId = Binder.clearCallingIdentity();
                try {
                    if (unscheduleServiceRestartLocked(s, callerApp.info.uid, false)) {
                        try {
                            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                Slog.v(TAG_SERVICE, "BIND SERVICE WHILE RESTART PENDING: " + s);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                    if ((flags & 1) != 0) {
                        s.lastActivity = SystemClock.uptimeMillis();
                        if (!s.hasAutoCreateConnections()) {
                            ServiceState stracker = s.getTracker();
                            if (stracker != null) {
                                stracker.setBound(true, this.mAm.mProcessStats.getMemFactorLocked(), s.lastActivity);
                            }
                        }
                    }
                    if ((flags & DumpState.DUMP_COMPILER_STATS) != 0) {
                        this.mAm.requireAllowedAssociationsLocked(s.appInfo.packageName);
                    }
                    this.mAm.startAssociationLocked(callerApp.uid, callerApp.processName, callerApp.getCurProcState(), s.appInfo.uid, s.appInfo.longVersionCode, s.instanceName, s.processName);
                    try {
                        this.mAm.grantEphemeralAccessLocked(callerApp.userId, service2, UserHandle.getAppId(s.appInfo.uid), UserHandle.getAppId(callerApp.uid));
                        b = s.retrieveAppBindingLocked(service2, callerApp);
                        try {
                            c = new ConnectionRecord(b, activity, connection, flags, clientLabel, clientIntent, callerApp.uid, callerApp.processName, callingPackage);
                            binder = connection.asBinder();
                            s.addConnection(binder, c);
                            if (activity != null) {
                                try {
                                    activity.addConnection(c);
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                        try {
                            b.client.connections.add(c);
                            c.startAssociationIfNeeded();
                            OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).uploadServiceConnected(b.client);
                            if ((c.flags & 8) != 0) {
                                b.client.hasAboveClient = true;
                            }
                            if ((c.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
                                s.whitelistManager = true;
                            }
                            if ((flags & DumpState.DUMP_DEXOPT) != 0) {
                                s.setHasBindingWhitelistingBgActivityStarts(true);
                            }
                            if (s.app != null) {
                                updateServiceClientActivitiesLocked(s.app, c, true);
                            }
                            ArrayList<ConnectionRecord> clist2 = this.mServiceConnections.get(binder);
                            if (clist2 == null) {
                                ArrayList<ConnectionRecord> clist3 = new ArrayList<>();
                                this.mServiceConnections.put(binder, clist3);
                                clist = clist3;
                            } else {
                                clist = clist2;
                            }
                            clist.add(c);
                            if ((flags & 1) != 0) {
                                s.lastActivity = SystemClock.uptimeMillis();
                                if (bringUpServiceLocked(s, service2.getFlags(), callerFg, false, permissionsReviewRequired) != null) {
                                    Binder.restoreCallingIdentity(origId);
                                    return 0;
                                }
                            }
                            if (s.app != null) {
                                if ((flags & 134217728) != 0) {
                                    s.app.treatLikeActivity = true;
                                }
                                if (s.whitelistManager) {
                                    s.app.whitelistManager = true;
                                }
                                this.mAm.updateLruProcessLocked(s.app, (callerApp.hasActivitiesOrRecentTasks() && s.app.hasClientActivities()) || (callerApp.getCurProcState() <= 2 && (flags & 134217728) != 0), b.client);
                                this.mAm.updateOomAdjLocked("updateOomAdj_bindService");
                            }
                            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                Slog.v(TAG_SERVICE, "Bind " + s + " with " + b + ": received=" + b.intent.received + " apps=" + b.intent.apps.size() + " doRebind=" + b.intent.doRebind);
                            }
                            if (s.app != null && b.intent.received) {
                                try {
                                    c.conn.connected(s.name, b.intent.binder, false);
                                } catch (Exception e) {
                                    Slog.w(str, "Failure sending service " + s.shortInstanceName + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
                                }
                                if (b.intent.apps.size() == 1 && b.intent.doRebind) {
                                    try {
                                        requestServiceBindingLocked(s, b.intent, callerFg, true);
                                    } catch (Throwable th5) {
                                        th = th5;
                                        Binder.restoreCallingIdentity(origId);
                                        throw th;
                                    }
                                }
                            } else if (!b.intent.requested) {
                                requestServiceBindingLocked(s, b.intent, callerFg, false);
                            }
                            getServiceMapLocked(s.userId).ensureNotStartingBackgroundLocked(s);
                            Binder.restoreCallingIdentity(origId);
                            OppoFeatureCache.get(IColorCommonListManager.DEFAULT).updateAppInfo(Binder.getCallingUid(), service2.getAction(), s.appInfo.uid, s.appInfo.packageName, true);
                            return 1;
                        } catch (Throwable th6) {
                            th = th6;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                    try {
                        b.connections.add(c);
                    } catch (Exception e2) {
                        Slog.wtf(str, "WTF:add ConnectionRecord failed!", e2);
                        b.logOutIntentBindWithTypeInfo();
                    } catch (Throwable th8) {
                        th = th8;
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            } else {
                throw new SecurityException("Non-system caller " + caller + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_INSTANT when binding service " + service2);
            }
        } else {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when binding service " + service);
        }
    }

    /* access modifiers changed from: package-private */
    public void publishServiceLocked(ServiceRecord r, Intent intent, IBinder service) {
        long origId;
        Throwable th;
        long origId2;
        boolean tooManyConn;
        boolean tooManyClist;
        Intent.FilterComparison filter;
        long origId3 = Binder.clearCallingIdentity();
        try {
            String str = ": ";
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                try {
                    Slog.v(TAG_SERVICE, "PUBLISHING " + r + StringUtils.SPACE + intent + str + service);
                } catch (Throwable th2) {
                    th = th2;
                    origId = origId3;
                }
            }
            if (r != null) {
                Intent.FilterComparison filter2 = new Intent.FilterComparison(intent);
                IntentBindRecord b = r.bindings.get(filter2);
                if (b == null || b.received) {
                    origId2 = origId3;
                } else {
                    b.binder = service;
                    b.requested = true;
                    b.received = true;
                    ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
                    if (connections.size() > 1000) {
                        Slog.v(TAG, "too many connections: PUBLISHING " + r + StringUtils.SPACE + intent + str + service);
                        tooManyConn = true;
                    } else {
                        tooManyConn = false;
                    }
                    int conni = connections.size() - 1;
                    while (conni >= 0) {
                        ArrayList<ConnectionRecord> clist = connections.valueAt(conni);
                        if (clist.size() > 1000) {
                            Slog.v(TAG, "too many clist: PUBLISHING " + r + StringUtils.SPACE + intent + str + service);
                            tooManyClist = true;
                        } else {
                            tooManyClist = false;
                        }
                        int i = 0;
                        while (i < clist.size()) {
                            ConnectionRecord c = clist.get(i);
                            origId = origId3;
                            try {
                                if (!filter2.equals(c.binding.intent.intent)) {
                                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                        String str2 = TAG_SERVICE;
                                        StringBuilder sb = new StringBuilder();
                                        filter = filter2;
                                        sb.append("Not publishing to: ");
                                        sb.append(c);
                                        Slog.v(str2, sb.toString());
                                    } else {
                                        filter = filter2;
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                        Slog.v(TAG_SERVICE, "Bound intent: " + c.binding.intent.intent);
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                        Slog.v(TAG_SERVICE, "Published intent: " + intent);
                                    }
                                } else {
                                    filter = filter2;
                                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                        Slog.v(TAG_SERVICE, "Publishing to: " + c);
                                    }
                                    if (tooManyConn && conni > connections.size() - 20 && conni < connections.size() - 1) {
                                        Slog.v(TAG, "Publishing to: " + conni + StringUtils.SPACE + c);
                                    }
                                    if (tooManyClist && i > clist.size() - 20) {
                                        if (i < clist.size() - 1) {
                                            Slog.v(TAG, "Publishing to: " + clist + StringUtils.SPACE + c);
                                        }
                                    }
                                    try {
                                        c.conn.connected(r.name, service, false);
                                    } catch (Exception e) {
                                        Slog.w(TAG, "Failure sending service " + r.shortInstanceName + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
                                    }
                                }
                                i++;
                                filter2 = filter;
                                origId3 = origId;
                                clist = clist;
                                str = str;
                            } catch (Throwable th3) {
                                th = th3;
                                Binder.restoreCallingIdentity(origId);
                                throw th;
                            }
                        }
                        conni--;
                        b = b;
                        str = str;
                    }
                    origId2 = origId3;
                }
                serviceDoneExecutingLocked(r, this.mDestroyingServices.contains(r), false);
            } else {
                origId2 = origId3;
            }
            Binder.restoreCallingIdentity(origId2);
        } catch (Throwable th4) {
            th = th4;
            origId = origId3;
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateServiceGroupLocked(IServiceConnection connection, int group, int importance) {
        IBinder binder = connection.asBinder();
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "updateServiceGroup: conn=" + binder);
        }
        ArrayList<ConnectionRecord> clist = this.mServiceConnections.get(binder);
        if (clist != null) {
            for (int i = clist.size() - 1; i >= 0; i--) {
                ServiceRecord srec = clist.get(i).binding.service;
                if (!(srec == null || (srec.serviceInfo.flags & 2) == 0)) {
                    if (srec.app != null) {
                        if (group > 0) {
                            srec.app.connectionService = srec;
                            srec.app.connectionGroup = group;
                            srec.app.connectionImportance = importance;
                        } else {
                            srec.app.connectionService = null;
                            srec.app.connectionGroup = 0;
                            srec.app.connectionImportance = 0;
                        }
                    } else if (group > 0) {
                        srec.pendingConnectionGroup = group;
                        srec.pendingConnectionImportance = importance;
                    } else {
                        srec.pendingConnectionGroup = 0;
                        srec.pendingConnectionImportance = 0;
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("Could not find connection for " + connection.asBinder());
    }

    /* access modifiers changed from: package-private */
    public boolean unbindServiceLocked(IServiceConnection connection) {
        IBinder binder = connection.asBinder();
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "unbindService: conn=" + binder);
        }
        ArrayList<ConnectionRecord> clist = this.mServiceConnections.get(binder);
        if (clist == null) {
            Slog.w(TAG, "Unbind failed: could not find connection for " + connection.asBinder());
            return false;
        }
        long origId = Binder.clearCallingIdentity();
        while (true) {
            try {
                boolean z = true;
                if (clist.size() > 0) {
                    ConnectionRecord r = clist.get(0);
                    removeConnectionLocked(r, null, null);
                    if (clist.size() > 0 && clist.get(0) == r) {
                        Slog.wtf(TAG, "Connection " + r + " not removed for binder " + binder);
                        clist.remove(0);
                    }
                    if (r.binding.service.app != null) {
                        OppoFeatureCache.get(IColorCommonListManager.DEFAULT).updateAppInfo(0, r.binding.intent.intent.getIntent().getAction(), r.binding.service.appInfo.uid, r.binding.service.packageName, false);
                        if (r.binding.service.app.whitelistManager) {
                            updateWhitelistManagerLocked(r.binding.service.app);
                        }
                        if ((r.flags & 134217728) != 0) {
                            r.binding.service.app.treatLikeActivity = true;
                            ActivityManagerService activityManagerService = this.mAm;
                            ProcessRecord processRecord = r.binding.service.app;
                            if (!r.binding.service.app.hasClientActivities()) {
                                if (!r.binding.service.app.treatLikeActivity) {
                                    z = false;
                                }
                            }
                            activityManagerService.updateLruProcessLocked(processRecord, z, null);
                        }
                    }
                } else {
                    this.mAm.updateOomAdjLocked("updateOomAdj_unbindService");
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindFinishedLocked(ServiceRecord r, Intent intent, boolean doRebind) {
        long origId = Binder.clearCallingIdentity();
        if (r != null) {
            try {
                IntentBindRecord b = r.bindings.get(new Intent.FilterComparison(intent));
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    String str = TAG_SERVICE;
                    StringBuilder sb = new StringBuilder();
                    sb.append("unbindFinished in ");
                    sb.append(r);
                    sb.append(" at ");
                    sb.append(b);
                    sb.append(": apps=");
                    sb.append(b != null ? b.apps.size() : 0);
                    Slog.v(str, sb.toString());
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                if (b != null) {
                    if (b.apps.size() <= 0 || inDestroying) {
                        b.doRebind = true;
                    } else {
                        boolean inFg = false;
                        int i = b.apps.size() - 1;
                        while (true) {
                            if (i >= 0) {
                                ProcessRecord client = b.apps.valueAt(i).client;
                                if (client != null && client.setSchedGroup != 0) {
                                    inFg = true;
                                    break;
                                }
                                i--;
                            }
                        }
                        try {
                            requestServiceBindingLocked(r, b, inFg, true);
                            break;
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
                serviceDoneExecutingLocked(r, inDestroying, false);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    private final ServiceRecord findServiceLocked(ComponentName name, IBinder token, int userId) {
        ServiceRecord r = getServiceByNameLocked(name, userId);
        if (r == token) {
            return r;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public final class ServiceLookupResult {
        final String permission;
        final ServiceRecord record;

        ServiceLookupResult(ServiceRecord _record, String _permission) {
            this.record = _record;
            this.permission = _permission;
        }
    }

    /* access modifiers changed from: private */
    public class ServiceRestarter implements Runnable {
        private ServiceRecord mService;

        private ServiceRestarter() {
        }

        /* access modifiers changed from: package-private */
        public void setService(ServiceRecord service) {
            this.mService = service;
        }

        public void run() {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActiveServices.this.performServiceRestartLocked(this.mService);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:112:?, code lost:
        r14 = new com.android.server.am.ServiceRecord(r30.mAm, r20, r3, r4, r2, r2, r0, r2, r39, r0);
        r14.oppostats = getServiceStatsLocked(r30.mAm.mBatteryStatsService.getActiveOppoStatistics(), r2.applicationInfo.uid, r4.getPackageName(), r4.getClassName());
        r0.setService(r14);
        r12.mServicesByInstanceName.put(r4, r14);
        r12.mServicesByIntent.put(r0, r14);
        r0 = r30.mPendingServices.size() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x03ab, code lost:
        if (r0 < 0) goto L_0x03f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x03ad, code lost:
        r3 = r30.mPendingServices.get(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x03bf, code lost:
        if (r3.serviceInfo.applicationInfo.uid != r2.applicationInfo.uid) goto L_0x03f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x03c7, code lost:
        if (r3.instanceName.equals(r4) == false) goto L_0x03ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x03cb, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_SERVICE == false) goto L_0x03e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x03cd, code lost:
        r9 = com.android.server.am.ActiveServices.TAG_SERVICE;
        r11 = new java.lang.StringBuilder();
        r19 = r2;
        r11.append("Remove pending: ");
        r11.append(r3);
        android.util.Slog.v(r9, r11.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x03e6, code lost:
        r19 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x03e8, code lost:
        r30.mPendingServices.remove(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x03ee, code lost:
        r19 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x03f1, code lost:
        r19 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x03f3, code lost:
        r0 = r0 - 1;
        r2 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x03fc, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_SERVICE == false) goto L_0x0430;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x03fe, code lost:
        android.util.Slog.v(com.android.server.am.ActiveServices.TAG_SERVICE, "Retrieve created new service: " + r14);
     */
    private ServiceLookupResult retrieveServiceLocked(Intent service, String instanceName, String resolvedType, String callingPackage, int callingPid, int callingUid, int userId, boolean createIfNeeded, boolean callingFromFg, boolean isBindExternal, boolean allowInstant) {
        ComponentName comp;
        int i;
        String str;
        int flags;
        ServiceInfo sInfo;
        int userId2;
        Throwable th;
        ServiceInfo sInfo2;
        String str2;
        ServiceRecord r = null;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "retrieveServiceLocked: " + service + " type=" + resolvedType + " callingUid=" + callingUid);
        }
        int userId3 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUserId(this.mAm.mUserController.handleIncomingUser(callingPid, callingUid, userId, false, 1, IColorAppStartupManager.TYPE_SERVICE, callingPackage), service);
        ServiceMap smap = getServiceMapLocked(userId3);
        if (instanceName == null) {
            comp = service.getComponent();
        } else {
            ComponentName realComp = service.getComponent();
            if (realComp != null) {
                comp = new ComponentName(realComp.getPackageName(), realComp.getClassName() + ":" + instanceName);
            } else {
                throw new IllegalArgumentException("Can't use custom instance name '" + instanceName + "' without expicit component in Intent");
            }
        }
        if (comp != null) {
            r = smap.mServicesByInstanceName.get(comp);
            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r != null) {
                Slog.v(TAG_SERVICE, "Retrieved by component: " + r);
            }
        }
        if (r == null && !isBindExternal && instanceName == null) {
            r = smap.mServicesByIntent.get(new Intent.FilterComparison(service));
            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r != null) {
                Slog.v(TAG_SERVICE, "Retrieved by intent: " + r);
            }
        }
        if (!(r == null || (r.serviceInfo.flags & 4) == 0 || callingPackage.equals(r.packageName))) {
            r = null;
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Whoops, can't use existing external service");
            }
        }
        ServiceRecord r2 = r;
        if (r2 == null) {
            if (allowInstant) {
                flags = 268436480 | DumpState.DUMP_VOLUMES;
            } else {
                flags = 268436480;
            }
            try {
                try {
                    ResolveInfo rInfo = this.mAm.getPackageManagerInternalLocked().resolveService(service, resolvedType, flags, userId3, callingUid);
                    ServiceInfo sInfo3 = rInfo != null ? rInfo.serviceInfo : null;
                    if (sInfo3 == null) {
                        Slog.w(TAG_SERVICE, "Unable to start service " + service + " U=" + userId3 + ": not found");
                        return null;
                    }
                    if (instanceName != null) {
                        if ((sInfo3.flags & 2) == 0) {
                            throw new IllegalArgumentException("Can't use instance name '" + instanceName + "' with non-isolated service '" + sInfo3.name + "'");
                        }
                    }
                    ComponentName className = new ComponentName(sInfo3.applicationInfo.packageName, sInfo3.name);
                    ComponentName name = comp != null ? comp : className;
                    i = callingUid;
                    try {
                        if (!this.mAm.validateAssociationAllowedLocked(callingPackage, i, name.getPackageName(), sInfo3.applicationInfo.uid)) {
                            String msg = "association not allowed between packages " + callingPackage + " and " + name.getPackageName();
                            Slog.w(TAG, "Service lookup failed: " + msg);
                            return new ServiceLookupResult(null, msg);
                        }
                        String definingPackageName = sInfo3.applicationInfo.packageName;
                        int definingUid = sInfo3.applicationInfo.uid;
                        if ((sInfo3.flags & 4) != 0) {
                            if (!isBindExternal) {
                                throw new SecurityException("BIND_EXTERNAL_SERVICE required for " + name);
                            } else if (!sInfo3.exported) {
                                throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + className + " is not exported");
                            } else if ((sInfo3.flags & 2) != 0) {
                                ApplicationInfo aInfo = AppGlobals.getPackageManager().getApplicationInfo(callingPackage, 1024, userId3);
                                if (aInfo != null) {
                                    ServiceInfo sInfo4 = new ServiceInfo(sInfo3);
                                    sInfo4.applicationInfo = new ApplicationInfo(sInfo4.applicationInfo);
                                    sInfo4.applicationInfo.packageName = aInfo.packageName;
                                    sInfo4.applicationInfo.uid = aInfo.uid;
                                    name = new ComponentName(aInfo.packageName, name.getClassName());
                                    String str3 = aInfo.packageName;
                                    if (instanceName == null) {
                                        str2 = className.getClassName();
                                        sInfo2 = sInfo4;
                                    } else {
                                        StringBuilder sb = new StringBuilder();
                                        sInfo2 = sInfo4;
                                        sb.append(className.getClassName());
                                        sb.append(":");
                                        sb.append(instanceName);
                                        str2 = sb.toString();
                                    }
                                    className = new ComponentName(str3, str2);
                                    service.setComponent(name);
                                    sInfo3 = sInfo2;
                                } else {
                                    throw new SecurityException("BIND_EXTERNAL_SERVICE failed, could not resolve client package " + callingPackage);
                                }
                            } else {
                                throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + className + " is not an isolatedProcess");
                            }
                        } else if (isBindExternal) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an externalService");
                        }
                        if (userId3 > 0) {
                            if (this.mAm.isSingleton(sInfo3.processName, sInfo3.applicationInfo, sInfo3.name, sInfo3.flags) && this.mAm.isValidSingletonCall(i, sInfo3.applicationInfo.uid)) {
                                userId3 = 0;
                                smap = getServiceMapLocked(0);
                            }
                            ServiceInfo sInfo5 = new ServiceInfo(sInfo3);
                            sInfo5.applicationInfo = this.mAm.getAppInfoForUser(sInfo5.applicationInfo, userId3);
                            sInfo = sInfo5;
                        } else {
                            sInfo = sInfo3;
                        }
                        try {
                            r2 = smap.mServicesByInstanceName.get(name);
                            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r2 != null) {
                                Slog.v(TAG_SERVICE, "Retrieved via pm by intent: " + r2);
                            }
                            if (r2 != null || !createIfNeeded) {
                                userId2 = userId3;
                            } else {
                                Intent.FilterComparison filter = new Intent.FilterComparison(service.cloneFilter());
                                ServiceRestarter res = new ServiceRestarter();
                                BatteryStatsImpl stats = this.mAm.mBatteryStatsService.getActiveStatistics();
                                synchronized (stats) {
                                    try {
                                        userId2 = userId3;
                                        try {
                                            BatteryStatsImpl.Uid.Pkg.Serv ss = stats.getServiceStatsLocked(sInfo.applicationInfo.uid, name.getPackageName(), name.getClassName());
                                        } catch (Throwable th2) {
                                            th = th2;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                }
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            }
                        } catch (RemoteException e) {
                        }
                    } catch (RemoteException e2) {
                    }
                } catch (RemoteException e3) {
                    i = callingUid;
                }
            } catch (RemoteException e4) {
                i = callingUid;
            }
        } else {
            i = callingUid;
        }
        if (r2 == null) {
            return null;
        }
        if (!this.mAm.validateAssociationAllowedLocked(callingPackage, i, r2.packageName, r2.appInfo.uid)) {
            String msg2 = "association not allowed between packages " + callingPackage + " and " + r2.packageName;
            Slog.w(TAG, "Service lookup failed: " + msg2);
            return new ServiceLookupResult(null, msg2);
        } else if (!this.mAm.mIntentFirewall.checkService(r2.name, service, callingUid, callingPid, resolvedType, r2.appInfo)) {
            return new ServiceLookupResult(null, "blocked by firewall");
        } else {
            ActivityManagerService activityManagerService = this.mAm;
            if (ActivityManagerService.checkComponentPermission(r2.permission, callingPid, i, r2.appInfo.uid, r2.exported) == 0) {
                if (r2.permission == null || callingPackage == null) {
                    str = null;
                } else {
                    int opCode = AppOpsManager.permissionToOpCode(r2.permission);
                    if (opCode == -1 || this.mAm.mAppOpsService.checkOperation(opCode, i, callingPackage) == 0) {
                        str = null;
                    } else {
                        Slog.w(TAG, "Appop Denial: Accessing service " + r2.shortInstanceName + " from pid=" + callingPid + ", uid=" + i + " requires appop " + AppOpsManager.opToName(opCode));
                        return null;
                    }
                }
                return new ServiceLookupResult(r2, str);
            } else if (!r2.exported) {
                Slog.w(TAG, "Permission Denial: Accessing service " + r2.shortInstanceName + " from pid=" + callingPid + ", uid=" + i + " that is not exported from uid " + r2.appInfo.uid);
                return new ServiceLookupResult(null, "not exported from uid " + r2.appInfo.uid);
            } else {
                Slog.w(TAG, "Permission Denial: Accessing service " + r2.shortInstanceName + " from pid=" + callingPid + ", uid=" + i + " requires " + r2.permission);
                return new ServiceLookupResult(null, r2.permission);
            }
        }
    }

    private final void bumpServiceExecutingLocked(ServiceRecord r, boolean fg, String why) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, ">>> EXECUTING " + why + " of " + r + " in app " + r.app);
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(TAG_SERVICE_EXECUTING, ">>> EXECUTING " + why + " of " + r.shortInstanceName);
        }
        boolean timeoutNeeded = true;
        if (this.mAm.mBootPhase < 600 && r.app != null && r.app.pid == Process.myPid()) {
            Slog.w(TAG, "Too early to start/bind service in system_server: Phase=" + this.mAm.mBootPhase + StringUtils.SPACE + r.getComponentName());
            timeoutNeeded = false;
        }
        OppoFeatureCache.get(IColorHansManager.DEFAULT).hansBumpService(r.appInfo.uid, r.appInfo.packageName);
        long now = SystemClock.uptimeMillis();
        if (r.executeNesting == 0) {
            r.executeFg = fg;
            ServiceState stracker = r.getTracker();
            if (stracker != null) {
                stracker.setExecuting(true, this.mAm.mProcessStats.getMemFactorLocked(), now);
            }
            if (r.app != null) {
                r.app.executingServices.add(r);
                r.app.execServicesFg |= fg;
                if (timeoutNeeded && r.app.executingServices.size() == 1) {
                    scheduleServiceTimeoutLocked(r.app);
                }
            }
        } else if (r.app != null && fg && !r.app.execServicesFg) {
            r.app.execServicesFg = true;
            if (timeoutNeeded) {
                scheduleServiceTimeoutLocked(r.app);
            }
        }
        r.executeFg |= fg;
        r.executeNesting++;
        r.executingStart = now;
    }

    private final boolean requestServiceBindingLocked(ServiceRecord r, IntentBindRecord i, boolean execInFg, boolean rebind) throws TransactionTooLargeException {
        if (r.app == null || r.app.thread == null) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            String str = TAG_SERVICE;
            Slog.d(str, "requestBind " + i + ": requested=" + i.requested + " rebind=" + rebind);
        }
        if ((!i.requested || rebind) && i.apps.size() > 0) {
            try {
                bumpServiceExecutingLocked(r, execInFg, "bind");
                r.app.forceProcessStateUpTo(11);
                r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind, r.app.getReportedProcState());
                if (!rebind) {
                    i.requested = true;
                }
                i.hasBound = true;
                i.doRebind = false;
            } catch (TransactionTooLargeException e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    String str2 = TAG_SERVICE;
                    Slog.v(str2, "Crashed while binding " + r, e);
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                throw e;
            } catch (RemoteException e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    String str3 = TAG_SERVICE;
                    Slog.v(str3, "Crashed while binding " + r);
                }
                boolean inDestroying2 = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying2, inDestroying2);
                return false;
            }
        }
        return true;
    }

    private final boolean scheduleServiceRestartLocked(ServiceRecord r, boolean allowCancel) {
        boolean z;
        long resetTime;
        int N;
        boolean canceled;
        int N2;
        long minDuration;
        int N3;
        ServiceRecord r2;
        int N4;
        boolean canceled2;
        boolean canceled3 = false;
        if (this.mAm.mAtmInternal.isShuttingDown()) {
            Slog.w(TAG, "Not scheduling restart of crashed service " + r.shortInstanceName + " - system is shutting down");
            return false;
        }
        ServiceMap smap = getServiceMapLocked(r.userId);
        if (smap.mServicesByInstanceName.get(r.instanceName) != r) {
            Slog.wtf(TAG, "Attempting to schedule restart of " + r + " when found in map: " + smap.mServicesByInstanceName.get(r.instanceName));
            return false;
        }
        long now = SystemClock.uptimeMillis();
        if ((r.serviceInfo.applicationInfo.flags & 8) == 0) {
            long minDuration2 = this.mAm.mConstants.SERVICE_RESTART_DURATION;
            long resetTime2 = this.mAm.mConstants.SERVICE_RESET_RUN_DURATION;
            int N5 = r.deliveredStarts.size();
            if (DEBUG_DELAYED_SERVICE) {
                Slog.w(TAG, " scheduleServiceRestartLocked  N " + N5 + " now " + now + " r " + r);
            }
            if (N5 > 0) {
                int i = N5 - 1;
                while (i >= 0) {
                    ServiceRecord.StartItem si = r.deliveredStarts.get(i);
                    si.removeUriPermissionsLocked();
                    if (si.intent == null) {
                        N4 = N5;
                    } else {
                        if (allowCancel) {
                            canceled2 = canceled3;
                            if (si.deliveryCount >= 3 || si.doneExecutingCount >= 6) {
                                Slog.w(TAG, "Canceling start item " + si.intent + " in service " + r.shortInstanceName);
                                canceled3 = true;
                                N4 = N5;
                            }
                        } else {
                            canceled2 = canceled3;
                        }
                        r.pendingStarts.add(0, si);
                        N4 = N5;
                        long dur = (SystemClock.uptimeMillis() - si.deliveredTime) * 2;
                        if (minDuration2 < dur) {
                            minDuration2 = dur;
                        }
                        if (resetTime2 < dur) {
                            resetTime2 = dur;
                        }
                        canceled3 = canceled2;
                    }
                    i--;
                    N5 = N4;
                }
                N = N5;
                r.deliveredStarts.clear();
                canceled = canceled3;
                resetTime = resetTime2;
            } else {
                N = N5;
                canceled = false;
                resetTime = resetTime2;
            }
            r.totalRestartCount++;
            if (DEBUG_DELAYED_SERVICE) {
                Slog.w(TAG, " scheduleServiceRestartLocked  r.totalRestartCount " + r.totalRestartCount + " r " + r);
            }
            if (r.restartDelay == 0) {
                r.restartCount++;
                r.restartDelay = minDuration2;
                N2 = N;
            } else if (r.crashCount > 1) {
                N2 = N;
                r.restartDelay = this.mAm.mConstants.BOUND_SERVICE_CRASH_RESTART_DURATION * ((long) (r.crashCount - 1));
            } else {
                N2 = N;
                if (now > r.restartTime + resetTime) {
                    r.restartCount = 1;
                    r.restartDelay = minDuration2;
                } else {
                    r.restartDelay *= (long) this.mAm.mConstants.SERVICE_RESTART_DURATION_FACTOR;
                    if (r.restartDelay < minDuration2) {
                        r.restartDelay = minDuration2;
                    }
                }
            }
            r.nextRestartTime = r.restartDelay + now;
            while (true) {
                long restartTimeBetween = this.mAm.mConstants.SERVICE_MIN_RESTART_TIME_BETWEEN;
                int i2 = this.mRestartingServices.size() - 1;
                while (true) {
                    if (i2 < 0) {
                        minDuration = minDuration2;
                        N3 = N2;
                        r2 = null;
                        break;
                    }
                    ServiceRecord r22 = this.mRestartingServices.get(i2);
                    if (r22 != r) {
                        minDuration = minDuration2;
                        N3 = N2;
                        if (r.nextRestartTime >= r22.nextRestartTime - restartTimeBetween && r.nextRestartTime < r22.nextRestartTime + restartTimeBetween) {
                            r.nextRestartTime = r22.nextRestartTime + restartTimeBetween;
                            r.restartDelay = r.nextRestartTime - now;
                            r2 = 1;
                            break;
                        }
                    } else {
                        minDuration = minDuration2;
                        N3 = N2;
                    }
                    i2--;
                    minDuration2 = minDuration;
                    N2 = N3;
                }
                if (r2 == null) {
                    break;
                }
                minDuration2 = minDuration;
                N2 = N3;
            }
            OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).resetSpecServiceRestartTime(r, now, minDuration);
            ajustRestartTime(r, minDuration);
            canceled3 = canceled;
            z = false;
        } else {
            r.totalRestartCount++;
            z = false;
            r.restartCount = 0;
            r.restartDelay = 0;
            r.nextRestartTime = now;
        }
        if (!this.mRestartingServices.contains(r)) {
            r.createdFromFg = z;
            this.mRestartingServices.add(r);
            r.makeRestarting(this.mAm.mProcessStats.getMemFactorLocked(), now);
        }
        cancelForegroundNotificationLocked(r);
        if (DEBUG_DELAYED_SERVICE) {
            Slog.w(TAG, "r " + r + " r.restartDelay " + r.restartDelay + " r.nextRestartTime " + r.nextRestartTime);
        }
        this.mAm.mHandler.removeCallbacks(r.restarter);
        this.mAm.mHandler.postAtTime(r.restarter, r.nextRestartTime);
        r.nextRestartTime = SystemClock.uptimeMillis() + r.restartDelay;
        Slog.w(TAG, "Scheduling restart of crashed service " + r.shortInstanceName + " in " + r.restartDelay + "ms");
        EventLog.writeEvent((int) EventLogTags.AM_SCHEDULE_SERVICE_RESTART, Integer.valueOf(r.userId), r.shortInstanceName, Long.valueOf(r.restartDelay));
        if (DEBUG_DELAYED_SERVICE) {
            Slog.v(TAG, "scheduleServiceRestartLocked r " + r + " call by " + Debug.getCallers(8));
        }
        return canceled3;
    }

    /* access modifiers changed from: package-private */
    public final void performServiceRestartLocked(ServiceRecord r) {
        if (this.mRestartingServices.contains(r)) {
            if (!isServiceNeededLocked(r, false, false)) {
                Slog.wtf(TAG, "Restarting service that is not needed: " + r);
                return;
            }
            try {
                bringUpServiceLocked(r, r.intent.getIntent().getFlags(), r.createdFromFg, true, false);
            } catch (TransactionTooLargeException e) {
            }
        }
    }

    private final boolean unscheduleServiceRestartLocked(ServiceRecord r, int callingUid, boolean force) {
        if (!force && r.restartDelay == 0) {
            return false;
        }
        boolean removed = this.mRestartingServices.remove(r);
        if (removed || callingUid != r.appInfo.uid) {
            r.resetRestartCounter();
        }
        if (removed) {
            clearRestartingIfNeededLocked(r);
        }
        this.mAm.mHandler.removeCallbacks(r.restarter);
        return true;
    }

    private void clearRestartingIfNeededLocked(ServiceRecord r) {
        if (r.restartTracker != null) {
            boolean stillTracking = false;
            int i = this.mRestartingServices.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (this.mRestartingServices.get(i).restartTracker == r.restartTracker) {
                    stillTracking = true;
                    break;
                } else {
                    i--;
                }
            }
            if (!stillTracking) {
                r.restartTracker.setRestarting(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                r.restartTracker = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg, boolean whileRestarting, boolean permissionsReviewRequired) throws TransactionTooLargeException {
        HostingRecord hostingRecord;
        ProcessRecord app;
        String suppressAction;
        if (r.app != null && r.app.thread != null) {
            sendServiceArgsLocked(r, execInFg, false);
            return null;
        } else if (!whileRestarting && this.mRestartingServices.contains(r)) {
            return null;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Bringing up " + r + StringUtils.SPACE + r.intent + " fg=" + r.fgRequired);
            }
            if (this.mRestartingServices.remove(r)) {
                clearRestartingIfNeededLocked(r);
            }
            if (r.delayed) {
                if (DEBUG_DELAYED_STARTS) {
                    Slog.v(TAG_SERVICE, "REM FR DELAY LIST (bring up): " + r);
                }
                getServiceMapLocked(r.userId).mDelayedStartList.remove(r);
                r.delayed = false;
            }
            if (!this.mAm.mUserController.hasStartedUserState(r.userId)) {
                String msg = "Unable to launch app " + r.appInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + r.appInfo.uid + " for service " + r.intent.getIntent() + ": user " + r.userId + " is stopped";
                Slog.w(TAG, msg);
                bringDownServiceLocked(r);
                return msg;
            } else if (OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).validStartService(r.packageName, r.userId)) {
                Slog.d(IColorAbnormalAppManager.TAG, "UL app " + r.appInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + r.appInfo.uid + " for service " + r.intent.getIntent() + ": user " + r.userId + " is R");
                bringDownServiceLocked(r);
                return null;
            } else {
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(r.packageName, false, r.userId);
                } catch (RemoteException e) {
                } catch (IllegalArgumentException e2) {
                    Slog.w(TAG, "Failed trying to unstop package " + r.packageName + ": " + e2);
                }
                boolean isolated = (r.serviceInfo.flags & 2) != 0;
                String procName = r.processName;
                HostingRecord hostingRecord2 = new HostingRecord(IColorAppStartupManager.TYPE_SERVICE, r.instanceName);
                if (!isolated) {
                    ProcessRecord app2 = this.mAm.getProcessRecordLocked(procName, r.appInfo.uid, false);
                    if (ActivityManagerDebugConfig.DEBUG_MU) {
                        Slog.v(TAG_MU, "bringUpServiceLocked: appInfo.uid=" + r.appInfo.uid + " app=" + app2);
                    }
                    if (!(app2 == null || app2.thread == null)) {
                        try {
                            app2.addPackage(r.appInfo.packageName, r.appInfo.longVersionCode, this.mAm.mProcessStats);
                            realStartServiceLocked(r, app2, execInFg);
                            return null;
                        } catch (TransactionTooLargeException e3) {
                            throw e3;
                        } catch (RemoteException e4) {
                            Slog.w(TAG, "Exception when starting service " + r.shortInstanceName, e4);
                        }
                    }
                    hostingRecord = hostingRecord2;
                    app = app2;
                } else {
                    ProcessRecord app3 = r.isolatedProc;
                    if (WebViewZygote.isMultiprocessEnabled() && r.serviceInfo.packageName.equals(WebViewZygote.getPackageName())) {
                        hostingRecord2 = HostingRecord.byWebviewZygote(r.instanceName);
                    }
                    if ((r.serviceInfo.flags & 8) != 0) {
                        hostingRecord = HostingRecord.byAppZygote(r.instanceName, r.definingPackageName, r.definingUid);
                        app = app3;
                    } else {
                        hostingRecord = hostingRecord2;
                        app = app3;
                    }
                }
                if (app == null && !permissionsReviewRequired) {
                    if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("persist.vendor.duraspeed.support"))) {
                        suppressAction = this.mAm.mAmsExt.onReadyToStartComponent(r.appInfo.packageName, r.appInfo.uid, IColorAppStartupManager.TYPE_SERVICE, null);
                    } else {
                        suppressAction = "allowed";
                    }
                    if ((suppressAction == null || !suppressAction.equals("skipped")) && this.mAm.mAmsExt.isComponentNeedsStart(r.appInfo.packageName, IColorAppStartupManager.TYPE_SERVICE)) {
                        ProcessRecord startProcessLocked = this.mAm.startProcessLocked(procName, r.appInfo, true, intentFlags, hostingRecord, false, isolated, false);
                        app = startProcessLocked;
                        if (startProcessLocked == null) {
                            String msg2 = "Unable to launch app " + r.appInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + r.appInfo.uid + " for service " + r.intent.getIntent() + ": process is bad";
                            Slog.w(TAG, msg2);
                            bringDownServiceLocked(r);
                            return msg2;
                        }
                    } else {
                        Slog.d(TAG, "bringUpServiceLocked, suppress to start service!");
                        try {
                            AppGlobals.getPackageManager().setPackageStoppedState(r.packageName, true, r.userId);
                        } catch (Exception e5) {
                            Slog.w(TAG, "Exception: " + e5);
                        }
                    }
                    if (isolated) {
                        r.isolatedProc = app;
                    }
                }
                if (r.fgRequired) {
                    if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                        Slog.v(TAG, "Whitelisting " + UserHandle.formatUid(r.appInfo.uid) + " for fg-service launch");
                    }
                    this.mAm.tempWhitelistUidLocked(r.appInfo.uid, 10000, "fg-service-launch");
                }
                if (!this.mPendingServices.contains(r)) {
                    this.mPendingServices.add(r);
                }
                if (r.delayedStop) {
                    r.delayedStop = false;
                    if (r.startRequested) {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(TAG_SERVICE, "Applying delayed stop (in bring up): " + r);
                        }
                        stopServiceLocked(r);
                    }
                }
                return null;
            }
        }
    }

    private final void requestServiceBindingsLocked(ServiceRecord r, boolean execInFg) throws TransactionTooLargeException {
        int i = r.bindings.size() - 1;
        while (i >= 0 && requestServiceBindingLocked(r, r.bindings.valueAt(i), execInFg, false)) {
            i--;
        }
    }

    private final void realStartServiceLocked(ServiceRecord r, ProcessRecord app, boolean execInFg) throws RemoteException {
        boolean z;
        String nameTerm;
        if (app.thread != null) {
            if (ActivityManagerDebugConfig.DEBUG_MU) {
                Slog.v(TAG_MU, "realStartServiceLocked, ServiceRecord.uid = " + r.appInfo.uid + ", ProcessRecord.uid = " + app.uid);
            }
            r.setProcess(app);
            long uptimeMillis = SystemClock.uptimeMillis();
            r.lastActivity = uptimeMillis;
            r.restartTime = uptimeMillis;
            boolean newService = app.services.add(r);
            bumpServiceExecutingLocked(r, execInFg, "create");
            this.mAm.updateLruProcessLocked(app, false, null);
            updateServiceForegroundLocked(r.app, false);
            this.mAm.updateOomAdjLocked("updateOomAdj_startService");
            try {
                if (LOG_SERVICE_START_STOP) {
                    int lastPeriod = r.shortInstanceName.lastIndexOf(46);
                    if (lastPeriod >= 0) {
                        nameTerm = r.shortInstanceName.substring(lastPeriod);
                    } else {
                        nameTerm = r.shortInstanceName;
                    }
                    EventLogTags.writeAmCreateService(r.userId, System.identityHashCode(r), nameTerm, r.app.uid, r.app.pid);
                }
                StatsLog.write(100, r.appInfo.uid, r.name.getPackageName(), r.name.getClassName());
                synchronized (r.stats.getBatteryStats()) {
                    r.stats.startLaunchedLocked();
                }
                this.mAm.notifyPackageUse(r.serviceInfo.packageName, 1);
                app.forceProcessStateUpTo(11);
                app.thread.scheduleCreateService(r, r.serviceInfo, this.mAm.compatibilityInfoForPackage(r.serviceInfo.applicationInfo), app.getReportedProcState());
                r.postNotification();
                if (1 == 0) {
                    boolean inDestroying = this.mDestroyingServices.contains(r);
                    serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                    if (newService) {
                        app.services.remove(r);
                        r.setProcess(null);
                    }
                    if (!inDestroying) {
                        scheduleServiceRestartLocked(r, false);
                    }
                }
                if (r.whitelistManager) {
                    app.whitelistManager = true;
                }
                requestServiceBindingsLocked(r, execInFg);
                updateServiceClientActivitiesLocked(app, null, true);
                if (newService && 1 != 0) {
                    app.addBoundClientUidsOfNewService(r);
                }
                if (r.startRequested && r.callStart && r.pendingStarts.size() == 0) {
                    r.pendingStarts.add(new ServiceRecord.StartItem(r, false, r.makeNextStartId(), null, null, 0));
                }
                sendServiceArgsLocked(r, execInFg, true);
                if (r.delayed) {
                    if (DEBUG_DELAYED_STARTS) {
                        String str = TAG_SERVICE;
                        Slog.v(str, "REM FR DELAY LIST (new proc): " + r);
                    }
                    getServiceMapLocked(r.userId).mDelayedStartList.remove(r);
                    z = false;
                    r.delayed = false;
                } else {
                    z = false;
                }
                if (r.delayedStop) {
                    r.delayedStop = z;
                    if (r.startRequested) {
                        if (DEBUG_DELAYED_STARTS) {
                            String str2 = TAG_SERVICE;
                            Slog.v(str2, "Applying delayed stop (from start): " + r);
                        }
                        stopServiceLocked(r);
                    }
                }
            } catch (DeadObjectException e) {
                Slog.w(TAG, "Application dead when creating service " + r);
                this.mAm.appDiedLocked(app);
                throw e;
            } catch (Throwable th) {
                if (0 == 0) {
                    boolean inDestroying2 = this.mDestroyingServices.contains(r);
                    serviceDoneExecutingLocked(r, inDestroying2, inDestroying2);
                    if (newService) {
                        app.services.remove(r);
                        r.setProcess(null);
                    }
                    if (!inDestroying2) {
                        scheduleServiceRestartLocked(r, false);
                    }
                }
                throw th;
            }
        } else {
            throw new RemoteException();
        }
    }

    private final void sendServiceArgsLocked(ServiceRecord r, boolean execInFg, boolean oomAdjusted) throws TransactionTooLargeException {
        int N = r.pendingStarts.size();
        if (N != 0) {
            ArrayList<ServiceStartArgs> args = new ArrayList<>();
            while (r.pendingStarts.size() > 0) {
                ServiceRecord.StartItem si = r.pendingStarts.remove(0);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Sending arguments to: " + r + StringUtils.SPACE + r.intent + " args=" + si.intent);
                }
                if (si.intent != null || N <= 1) {
                    si.deliveredTime = SystemClock.uptimeMillis();
                    r.deliveredStarts.add(si);
                    si.deliveryCount++;
                    if (si.neededGrants != null) {
                        this.mAm.mUgmInternal.grantUriPermissionUncheckedFromIntent(si.neededGrants, si.getUriPermissionsLocked());
                    }
                    this.mAm.grantEphemeralAccessLocked(r.userId, si.intent, UserHandle.getAppId(r.appInfo.uid), UserHandle.getAppId(si.callingId));
                    bumpServiceExecutingLocked(r, execInFg, "start");
                    if (!oomAdjusted) {
                        oomAdjusted = true;
                        this.mAm.updateOomAdjLocked(r.app, true, "updateOomAdj_startService");
                    }
                    if (r.fgRequired && !r.fgWaiting) {
                        if (!r.isForeground) {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(TAG, "Launched service must call startForeground() within timeout: " + r);
                            }
                            scheduleServiceForegroundTransitionTimeoutLocked(r);
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(TAG, "Service already foreground; no new timeout: " + r);
                            }
                            r.fgRequired = false;
                        }
                    }
                    int flags = 0;
                    if (si.deliveryCount > 1) {
                        flags = 0 | 2;
                    }
                    if (si.doneExecutingCount > 0) {
                        flags |= 1;
                    }
                    args.add(new ServiceStartArgs(si.taskRemoved, si.id, flags, si.intent));
                }
            }
            ParceledListSlice<ServiceStartArgs> slice = new ParceledListSlice<>(args);
            slice.setInlineCountLimit(4);
            Exception caughtException = null;
            try {
                r.app.thread.scheduleServiceArgs(r, slice);
            } catch (TransactionTooLargeException e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Transaction too large for " + args.size() + " args, first: " + args.get(0).args);
                }
                Slog.w(TAG, "Failed delivering service starts", e);
                caughtException = e;
            } catch (RemoteException e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Crashed while sending args: " + r);
                }
                Slog.w(TAG, "Failed delivering service starts", e2);
                caughtException = e2;
            } catch (Exception e3) {
                Slog.w(TAG, "Unexpected exception", e3);
                caughtException = e3;
            }
            if (caughtException != null) {
                boolean inDestroying = this.mDestroyingServices.contains(r);
                for (int i = 0; i < args.size(); i++) {
                    serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                }
                if (caughtException instanceof TransactionTooLargeException) {
                    throw ((TransactionTooLargeException) caughtException);
                }
            }
        }
    }

    private final boolean isServiceNeededLocked(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (r.startRequested) {
            return true;
        }
        if (!knowConn) {
            hasConn = r.hasAutoCreateConnections();
        }
        if (hasConn) {
            return true;
        }
        return false;
    }

    private final void bringDownServiceIfNeededLocked(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (!isServiceNeededLocked(r, knowConn, hasConn) && !this.mPendingServices.contains(r)) {
            ServiceRecord found = getServiceMapLocked(r.userId).mServicesByInstanceName.remove(r.instanceName);
            if (found == null || found == r) {
                bringDownServiceLocked(r);
                return;
            }
            Slog.i(TAG, "trying to bring down a service record that has been brought down once " + r);
        }
    }

    private final void bringDownServiceLocked(ServiceRecord r) {
        ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
        boolean tooManyConn = false;
        boolean targetDied = false;
        if (connections.size() > 1000) {
            Slog.v(TAG, "too many connections: Bring down service " + r);
            tooManyConn = true;
        }
        for (int conni = connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> c = connections.valueAt(conni);
            boolean tooManyClist = false;
            if (c.size() > 1000) {
                Slog.v(TAG, "too many connections: Bring down service " + r);
                tooManyClist = true;
            }
            for (int i = 0; i < c.size(); i++) {
                ConnectionRecord cr = c.get(i);
                cr.serviceDead = true;
                cr.stopAssociation();
                if (tooManyConn && conni > connections.size() - 20 && conni < connections.size() - 1) {
                    Slog.v(TAG, "Bring down service  to: " + conni + StringUtils.SPACE + cr);
                }
                if (tooManyClist && i > c.size() - 20 && i < c.size() - 1) {
                    Slog.v(TAG, "Bring down service  to: " + i + StringUtils.SPACE + cr);
                }
                try {
                    cr.conn.connected(r.name, (IBinder) null, true);
                } catch (Exception e) {
                    Slog.w(TAG, "Failure disconnecting service " + r.shortInstanceName + " to connection " + c.get(i).conn.asBinder() + " (in " + c.get(i).binding.client.processName + ")", e);
                    targetDied = true;
                }
            }
        }
        if (r.app != null) {
            if ((r.app.thread != null) && (!targetDied)) {
                boolean needOomAdj = false;
                for (int i2 = r.bindings.size() - 1; i2 >= 0; i2--) {
                    IntentBindRecord ibr = r.bindings.valueAt(i2);
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Bringing down binding " + ibr + ": hasBound=" + ibr.hasBound);
                    }
                    if (ibr.hasBound) {
                        try {
                            bumpServiceExecutingLocked(r, false, "bring down unbind");
                            needOomAdj = true;
                            ibr.hasBound = false;
                            ibr.requested = false;
                            r.app.thread.scheduleUnbindService(r, ibr.intent.getIntent());
                        } catch (Exception e2) {
                            Slog.w(TAG, "Exception when unbinding service " + r.shortInstanceName, e2);
                            serviceProcessGoneLocked(r);
                        }
                    }
                }
                if (needOomAdj && r.app != null) {
                    this.mAm.updateOomAdjLocked(r.app, true, "updateOomAdj_unbindService");
                }
            }
        }
        if (r.fgRequired) {
            Slog.w(TAG_SERVICE, "Bringing down service while still waiting for start foreground: " + r);
            r.fgRequired = false;
            r.fgWaiting = false;
            ServiceState stracker = r.getTracker();
            if (stracker != null) {
                stracker.setForeground(false, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
            }
            this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName);
            this.mAm.mHandler.removeMessages(66, r);
            if (r.app != null) {
                Message msg = this.mAm.mHandler.obtainMessage(69);
                msg.obj = r.app;
                msg.getData().putCharSequence("servicerecord", r.toString());
                this.mAm.mHandler.sendMessage(msg);
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            RuntimeException here = new RuntimeException();
            here.fillInStackTrace();
            Slog.v(TAG_SERVICE, "Bringing down " + r + StringUtils.SPACE + r.intent, here);
        }
        r.destroyTime = SystemClock.uptimeMillis();
        if (LOG_SERVICE_START_STOP) {
            EventLogTags.writeAmDestroyService(r.userId, System.identityHashCode(r), r.app != null ? r.app.pid : -1);
        }
        ServiceMap smap = getServiceMapLocked(r.userId);
        ServiceRecord found = smap.mServicesByInstanceName.remove(r.instanceName);
        if (found == null || found == r) {
            smap.mServicesByIntent.remove(r.intent);
            r.totalRestartCount = 0;
            unscheduleServiceRestartLocked(r, 0, true);
            for (int i3 = this.mPendingServices.size() - 1; i3 >= 0; i3--) {
                if (this.mPendingServices.get(i3) == r) {
                    this.mPendingServices.remove(i3);
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Removed pending: " + r);
                    }
                }
            }
            cancelForegroundNotificationLocked(r);
            if (r.isForeground) {
                decActiveForegroundAppLocked(smap, r);
                ServiceState stracker2 = r.getTracker();
                if (stracker2 != null) {
                    stracker2.setForeground(false, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
                }
                this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName);
                StatsLog.write(60, r.appInfo.uid, r.shortInstanceName, 2);
                this.mAm.updateForegroundServiceUsageStats(r.name, r.userId, false);
            }
            r.isForeground = false;
            r.foregroundId = 0;
            r.foregroundNoti = null;
            r.clearDeliveredStartsLocked();
            r.pendingStarts.clear();
            smap.mDelayedStartList.remove(r);
            if (r.app != null) {
                synchronized (r.stats.getBatteryStats()) {
                    r.stats.stopLaunchedLocked();
                }
                r.app.services.remove(r);
                r.app.updateBoundClientUids();
                if (r.whitelistManager) {
                    updateWhitelistManagerLocked(r.app);
                }
                if (r.app.thread != null) {
                    updateServiceForegroundLocked(r.app, false);
                    try {
                        bumpServiceExecutingLocked(r, false, "destroy");
                        this.mDestroyingServices.add(r);
                        r.destroying = true;
                        this.mAm.updateOomAdjLocked(r.app, true, "updateOomAdj_unbindService");
                        r.app.thread.scheduleStopService(r);
                    } catch (Exception e3) {
                        Slog.w(TAG, "Exception when destroying service " + r.shortInstanceName, e3);
                        serviceProcessGoneLocked(r);
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Removed service that has no process: " + r);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Removed service that is not running: " + r);
            }
            if (r.bindings.size() > 0) {
                r.bindings.clear();
            }
            if (r.restarter instanceof ServiceRestarter) {
                ((ServiceRestarter) r.restarter).setService(null);
            }
            int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
            long now = SystemClock.uptimeMillis();
            if (r.tracker != null) {
                r.tracker.setStarted(false, memFactor, now);
                r.tracker.setBound(false, memFactor, now);
                if (r.executeNesting == 0) {
                    r.tracker.clearCurrentOwner(r, false);
                    r.tracker = null;
                }
            }
            smap.ensureNotStartingBackgroundLocked(r);
            return;
        }
        smap.mServicesByInstanceName.put(r.instanceName, found);
        throw new IllegalStateException("Bringing down " + r + " but actually running " + found);
    }

    /* access modifiers changed from: package-private */
    public void removeConnectionLocked(ConnectionRecord c, ProcessRecord skipApp, ActivityServiceConnectionsHolder skipAct) {
        IBinder binder = c.conn.asBinder();
        AppBindRecord b = c.binding;
        ServiceRecord s = b.service;
        ArrayList<ConnectionRecord> clist = s.getConnections().get(binder);
        if (clist != null) {
            clist.remove(c);
            if (clist.size() == 0) {
                s.removeConnection(binder);
            }
        }
        b.connections.remove(c);
        c.stopAssociation();
        if (c.activity != null) {
            if (c.activity != skipAct) {
                c.activity.removeConnection(c);
            }
        }
        if (b.client != skipApp) {
            b.client.connections.remove(c);
            if ((c.flags & 8) != 0) {
                b.client.updateHasAboveClientLocked();
            }
            if ((c.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
                s.updateWhitelistManager();
                if (!s.whitelistManager && s.app != null) {
                    updateWhitelistManagerLocked(s.app);
                }
            }
            if ((c.flags & DumpState.DUMP_DEXOPT) != 0) {
                s.updateHasBindingWhitelistingBgActivityStarts();
            }
            if (s.app != null) {
                updateServiceClientActivitiesLocked(s.app, c, true);
            }
        }
        ArrayList<ConnectionRecord> clist2 = this.mServiceConnections.get(binder);
        if (clist2 != null) {
            clist2.remove(c);
            if (clist2.size() == 0) {
                this.mServiceConnections.remove(binder);
            }
        }
        this.mAm.stopAssociationLocked(b.client.uid, b.client.processName, s.appInfo.uid, s.appInfo.longVersionCode, s.instanceName, s.processName);
        if (b.connections.size() == 0) {
            b.intent.apps.remove(b.client);
        }
        if (!c.serviceDead) {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                String str = TAG_SERVICE;
                Slog.v(str, "Disconnecting binding " + b.intent + ": shouldUnbind=" + b.intent.hasBound);
            }
            if (s.app != null && s.app.thread != null && b.intent.apps.size() == 0 && b.intent.hasBound) {
                try {
                    bumpServiceExecutingLocked(s, false, "unbind");
                    if (b.client != s.app && (c.flags & 32) == 0 && s.app.setProcState <= 14) {
                        this.mAm.updateLruProcessLocked(s.app, false, null);
                    }
                    this.mAm.updateOomAdjLocked(s.app, true, "updateOomAdj_unbindService");
                    b.intent.hasBound = false;
                    b.intent.doRebind = false;
                    s.app.thread.scheduleUnbindService(s, b.intent.intent.getIntent());
                } catch (Exception e) {
                    Slog.w(TAG, "Exception when unbinding service " + s.shortInstanceName, e);
                    serviceProcessGoneLocked(s);
                }
            }
            this.mPendingServices.remove(s);
            if ((c.flags & 1) != 0) {
                boolean hasAutoCreate = s.hasAutoCreateConnections();
                if (!hasAutoCreate && s.tracker != null) {
                    s.tracker.setBound(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                }
                bringDownServiceIfNeededLocked(s, true, hasAutoCreate);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void serviceDoneExecutingLocked(ServiceRecord r, int type, int startId, int res) {
        boolean inDestroying = this.mDestroyingServices.contains(r);
        if (r != null) {
            if (type == 1) {
                r.callStart = true;
                if (res == 0 || res == 1) {
                    r.findDeliveredStart(startId, false, true);
                    r.stopIfKilled = false;
                } else if (res == 2) {
                    r.findDeliveredStart(startId, false, true);
                    if (r.getLastStartId() == startId) {
                        r.stopIfKilled = true;
                    }
                } else if (res == 3) {
                    ServiceRecord.StartItem si = r.findDeliveredStart(startId, false, false);
                    if (si != null) {
                        si.deliveryCount = 0;
                        si.doneExecutingCount++;
                        r.stopIfKilled = true;
                    }
                } else if (res == 1000) {
                    r.findDeliveredStart(startId, true, true);
                } else {
                    throw new IllegalArgumentException("Unknown service start result: " + res);
                }
                if (res == 0) {
                    r.callStart = false;
                }
            } else if (type == 2) {
                if (!inDestroying) {
                    if (r.app != null) {
                        Slog.w(TAG, "Service done with onDestroy, but not inDestroying: " + r + ", app=" + r.app);
                    }
                } else if (r.executeNesting != 1) {
                    Slog.w(TAG, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                    r.executeNesting = 1;
                }
            }
            long origId = Binder.clearCallingIdentity();
            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
            Binder.restoreCallingIdentity(origId);
            return;
        }
        Slog.w(TAG, "Done executing unknown service from pid " + Binder.getCallingPid());
    }

    private void serviceProcessGoneLocked(ServiceRecord r) {
        if (r.tracker != null) {
            int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
            long now = SystemClock.uptimeMillis();
            r.tracker.setExecuting(false, memFactor, now);
            r.tracker.setForeground(false, memFactor, now);
            r.tracker.setBound(false, memFactor, now);
            r.tracker.setStarted(false, memFactor, now);
        }
        serviceDoneExecutingLocked(r, true, true);
    }

    private void serviceDoneExecutingLocked(ServiceRecord r, boolean inDestroying, boolean finishing) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "<<< DONE EXECUTING " + r + ": nesting=" + r.executeNesting + ", inDestroying=" + inDestroying + ", app=" + r.app);
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(TAG_SERVICE_EXECUTING, "<<< DONE EXECUTING " + r.shortInstanceName);
        }
        r.executeNesting--;
        if (r.executeNesting <= 0) {
            if (r.app != null) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Nesting at 0 of " + r.shortInstanceName);
                }
                r.app.execServicesFg = false;
                r.app.executingServices.remove(r);
                if (r.app.executingServices.size() == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
                        Slog.v(TAG_SERVICE_EXECUTING, "No more executingServices of " + r.shortInstanceName);
                    }
                    this.mAm.mHandler.removeMessages(12, r.app);
                    this.mAm.mAnrManager.removeServiceMonitorMessage();
                } else if (r.executeFg) {
                    int i = r.app.executingServices.size() - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        } else if (r.app.executingServices.valueAt(i).executeFg) {
                            r.app.execServicesFg = true;
                            break;
                        } else {
                            i--;
                        }
                    }
                }
                if (inDestroying) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "doneExecuting remove destroying " + r);
                    }
                    this.mDestroyingServices.remove(r);
                    r.bindings.clear();
                }
                this.mAm.updateOomAdjLocked(r.app, true, "updateOomAdj_unbindService");
            }
            r.executeFg = false;
            if (r.tracker != null) {
                int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
                long now = SystemClock.uptimeMillis();
                r.tracker.setExecuting(false, memFactor, now);
                r.tracker.setForeground(false, memFactor, now);
                if (finishing) {
                    r.tracker.clearCurrentOwner(r, false);
                    r.tracker = null;
                }
            }
            if (finishing) {
                if (r.app != null && !r.app.isPersistent()) {
                    r.app.services.remove(r);
                    r.app.updateBoundClientUids();
                    if (r.whitelistManager) {
                        updateWhitelistManagerLocked(r.app);
                    }
                }
                r.setProcess(null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean attachApplicationLocked(ProcessRecord proc, String processName) throws RemoteException {
        boolean didSomething = false;
        if (this.mPendingServices.size() > 0) {
            ServiceRecord sr = null;
            int i = 0;
            while (i < this.mPendingServices.size()) {
                try {
                    sr = this.mPendingServices.get(i);
                    if (proc != sr.isolatedProc) {
                        if (proc.uid == sr.appInfo.uid) {
                            if (!processName.equals(sr.processName)) {
                            }
                        }
                        i++;
                    }
                    this.mPendingServices.remove(i);
                    i--;
                    proc.addPackage(sr.appInfo.packageName, sr.appInfo.longVersionCode, this.mAm.mProcessStats);
                    realStartServiceLocked(sr, proc, sr.createdFromFg);
                    didSomething = true;
                    if (!isServiceNeededLocked(sr, false, false)) {
                        bringDownServiceLocked(sr);
                    }
                    i++;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception in new application when starting service " + sr.shortInstanceName, e);
                    throw e;
                }
            }
        }
        if (this.mRestartingServices.size() > 0) {
            for (int i2 = 0; i2 < this.mRestartingServices.size(); i2++) {
                ServiceRecord sr2 = this.mRestartingServices.get(i2);
                if (proc == sr2.isolatedProc || (proc.uid == sr2.appInfo.uid && processName.equals(sr2.processName))) {
                    this.mAm.mHandler.removeCallbacks(sr2.restarter);
                    this.mAm.mHandler.post(sr2.restarter);
                }
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public void processStartTimedOutLocked(ProcessRecord proc) {
        int i = 0;
        while (i < this.mPendingServices.size()) {
            ServiceRecord sr = this.mPendingServices.get(i);
            if ((proc.uid == sr.appInfo.uid && proc.processName.equals(sr.processName)) || sr.isolatedProc == proc) {
                Slog.w(TAG, "Forcing bringing down service: " + sr);
                sr.isolatedProc = null;
                this.mPendingServices.remove(i);
                i += -1;
                bringDownServiceLocked(sr);
            }
            i++;
        }
    }

    private boolean collectPackageServicesLocked(String packageName, Set<String> filterByClasses, boolean evenPersistent, boolean doit, ArrayMap<ComponentName, ServiceRecord> services) {
        boolean didSomething = false;
        for (int i = services.size() - 1; i >= 0; i--) {
            ServiceRecord service = services.valueAt(i);
            if ((packageName == null || (service.packageName.equals(packageName) && (filterByClasses == null || filterByClasses.contains(service.name.getClassName())))) && (service.app == null || evenPersistent || !service.app.isPersistent())) {
                if (!doit) {
                    return true;
                }
                didSomething = true;
                Slog.i(TAG, "  Force stopping service " + service);
                if (service.app != null && !service.app.isPersistent()) {
                    service.app.services.remove(service);
                    service.app.updateBoundClientUids();
                    if (service.whitelistManager) {
                        updateWhitelistManagerLocked(service.app);
                    }
                }
                service.setProcess(null);
                service.isolatedProc = null;
                if (this.mTmpCollectionResults == null) {
                    this.mTmpCollectionResults = new ArrayList<>();
                }
                this.mTmpCollectionResults.add(service);
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public boolean bringDownDisabledPackageServicesLocked(String packageName, Set<String> filterByClasses, int userId, boolean evenPersistent, boolean doit) {
        boolean didSomething = false;
        ArrayList<ServiceRecord> arrayList = this.mTmpCollectionResults;
        if (arrayList != null) {
            arrayList.clear();
        }
        if (userId == -1) {
            for (int i = this.mServiceMap.size() - 1; i >= 0; i--) {
                didSomething |= collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, this.mServiceMap.valueAt(i).mServicesByInstanceName);
                if (!doit && didSomething) {
                    return true;
                }
                if (doit && filterByClasses == null) {
                    forceStopPackageLocked(packageName, this.mServiceMap.valueAt(i).mUserId);
                }
            }
        } else {
            ServiceMap smap = this.mServiceMap.get(userId);
            if (smap != null) {
                didSomething = collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, smap.mServicesByInstanceName);
            }
            if (doit && filterByClasses == null) {
                forceStopPackageLocked(packageName, userId);
            }
        }
        ArrayList<ServiceRecord> arrayList2 = this.mTmpCollectionResults;
        if (arrayList2 != null) {
            for (int i2 = arrayList2.size() - 1; i2 >= 0; i2--) {
                bringDownServiceLocked(this.mTmpCollectionResults.get(i2));
            }
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.i(TAG, "packageName " + packageName + " size " + this.mTmpCollectionResults.size() + StringUtils.SPACE + Debug.getCallers(8));
            }
            for (int i3 = this.mTmpCollectionResults.size() - 1; i3 >= 0; i3--) {
                if (i3 < this.mTmpCollectionResults.size()) {
                    bringDownServiceLocked(this.mTmpCollectionResults.get(i3));
                } else if (DEBUG_AGING_VERSION || DEBUG_ACTIVE_SERVICES || ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.w(TAG, "Index is error " + i3 + StringUtils.SPACE + Debug.getCallers(8));
                }
            }
            this.mTmpCollectionResults.clear();
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public void forceStopPackageLocked(String packageName, int userId) {
        ServiceMap smap = this.mServiceMap.get(userId);
        if (smap != null && smap.mActiveForegroundApps.size() > 0) {
            for (int i = smap.mActiveForegroundApps.size() - 1; i >= 0; i--) {
                if (smap.mActiveForegroundApps.valueAt(i).mPackageName.equals(packageName)) {
                    smap.mActiveForegroundApps.removeAt(i);
                    smap.mActiveForegroundAppsChanged = true;
                }
            }
            if (smap.mActiveForegroundAppsChanged) {
                requestUpdateActiveForegroundAppsLocked(smap, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanUpServices(int userId, ComponentName component, Intent baseIntent) {
        ArrayList<ServiceRecord> services = new ArrayList<>();
        ArrayMap<ComponentName, ServiceRecord> alls = getServicesLocked(userId);
        for (int i = alls.size() - 1; i >= 0; i--) {
            ServiceRecord sr = alls.valueAt(i);
            if (sr.packageName.equals(component.getPackageName())) {
                services.add(sr);
            }
        }
        for (int i2 = services.size() - 1; i2 >= 0; i2--) {
            ServiceRecord sr2 = services.get(i2);
            if (sr2.startRequested) {
                if ((sr2.serviceInfo.flags & 1) != 0) {
                    Slog.i(TAG, "Stopping service " + sr2.shortInstanceName + ": remove task");
                    stopServiceLocked(sr2);
                } else {
                    sr2.pendingStarts.add(new ServiceRecord.StartItem(sr2, true, sr2.getLastStartId(), baseIntent, null, 0));
                    if (!(sr2.app == null || sr2.app.thread == null)) {
                        try {
                            sendServiceArgsLocked(sr2, true, false);
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v36, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r6v38 */
    /* JADX WARN: Type inference failed for: r6v45 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0153, code lost:
        r0 = th;
     */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final void killServicesLocked(ProcessRecord app, boolean allowRestart) {
        ProcessRecord processRecord;
        long startTime;
        long startTime2 = SystemClock.uptimeMillis();
        int i = app.connections.size() - 1;
        while (true) {
            processRecord = null;
            if (i < 0) {
                break;
            }
            removeConnectionLocked(app.connections.valueAt(i), app, null);
            i--;
        }
        OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).uploadRemoveServiceConnection(app, SystemClock.uptimeMillis() - startTime2);
        updateServiceConnectionActivitiesLocked(app);
        app.connections.clear();
        boolean z = false;
        app.whitelistManager = false;
        int i2 = app.services.size() - 1;
        while (i2 >= 0) {
            ServiceRecord sr = app.services.valueAt(i2);
            synchronized (sr.stats.getBatteryStats()) {
                sr.stats.stopLaunchedLocked();
            }
            if (!(sr.app == app || sr.app == null || sr.app.isPersistent())) {
                sr.app.services.remove(sr);
                sr.app.updateBoundClientUids();
            }
            sr.setProcess(processRecord);
            sr.isolatedProc = processRecord;
            int i3 = z ? 1 : 0;
            int i4 = z ? 1 : 0;
            int i5 = z ? 1 : 0;
            int i6 = z ? 1 : 0;
            sr.executeNesting = i3;
            sr.forceClearTracker();
            if (this.mDestroyingServices.remove(sr) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "killServices remove destroying " + sr);
            }
            int bindingi = sr.bindings.size() - 1;
            ?? r6 = processRecord;
            while (bindingi >= 0) {
                IntentBindRecord b = sr.bindings.valueAt(bindingi);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Killing binding " + b + ": shouldUnbind=" + b.hasBound);
                }
                b.binder = r6;
                b.hasBound = z;
                b.received = z;
                b.requested = z;
                int appi = b.apps.size() - 1;
                while (appi >= 0) {
                    ProcessRecord proc = b.apps.keyAt(appi);
                    if (proc.killedByAm) {
                        startTime = startTime2;
                    } else if (proc.thread == null) {
                        startTime = startTime2;
                    } else {
                        AppBindRecord abind = b.apps.valueAt(appi);
                        boolean hasCreate = false;
                        int conni = abind.connections.size() - 1;
                        while (true) {
                            if (conni < 0) {
                                startTime = startTime2;
                                break;
                            }
                            startTime = startTime2;
                            if ((abind.connections.valueAt(conni).flags & 49) == 1) {
                                hasCreate = true;
                                break;
                            } else {
                                conni--;
                                startTime2 = startTime;
                            }
                        }
                        if (!hasCreate) {
                        }
                    }
                    appi--;
                    startTime2 = startTime;
                }
                bindingi--;
                z = false;
                r6 = 0;
            }
            i2--;
            z = false;
            processRecord = null;
        }
        ServiceMap smap = getServiceMapLocked(app.userId);
        for (int i7 = app.services.size() - 1; i7 >= 0; i7--) {
            ServiceRecord sr2 = app.services.valueAt(i7);
            if (!app.isPersistent()) {
                app.services.removeAt(i7);
                app.updateBoundClientUids();
            }
            ServiceRecord curRec = smap.mServicesByInstanceName.get(sr2.instanceName);
            if (curRec != sr2) {
                if (curRec != null) {
                    Slog.e(TAG, "Service " + sr2 + " in process " + app + " not same as in map: " + curRec);
                }
            } else if (!allowRestart || ((long) sr2.crashCount) < this.mAm.mConstants.BOUND_SERVICE_MAX_CRASH_RETRY || (sr2.serviceInfo.applicationInfo.flags & 8) != 0) {
                if (allowRestart) {
                    if (this.mAm.mUserController.isUserRunning(sr2.userId, 0)) {
                        boolean canceled = scheduleServiceRestartLocked(sr2, true);
                        if (sr2.startRequested) {
                            if (sr2.stopIfKilled || canceled) {
                                if (sr2.pendingStarts.size() == 0) {
                                    sr2.startRequested = false;
                                    if (sr2.tracker != null) {
                                        sr2.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                                    }
                                    if (!sr2.hasAutoCreateConnections()) {
                                        bringDownServiceLocked(sr2);
                                    }
                                }
                            }
                        }
                    }
                }
                bringDownServiceLocked(sr2);
            } else {
                Slog.w(TAG, "Service crashed " + sr2.crashCount + " times, stopping: " + sr2);
                EventLog.writeEvent((int) EventLogTags.AM_SERVICE_CRASHED_TOO_MUCH, Integer.valueOf(sr2.userId), Integer.valueOf(sr2.crashCount), sr2.shortInstanceName, Integer.valueOf(app.pid));
                bringDownServiceLocked(sr2);
            }
        }
        if (!allowRestart) {
            app.services.clear();
            app.clearBoundClientUids();
            for (int i8 = this.mRestartingServices.size() - 1; i8 >= 0; i8--) {
                ServiceRecord r = this.mRestartingServices.get(i8);
                if (r.processName.equals(app.processName) && r.serviceInfo.applicationInfo.uid == app.info.uid) {
                    this.mRestartingServices.remove(i8);
                    clearRestartingIfNeededLocked(r);
                }
            }
            for (int i9 = this.mPendingServices.size() - 1; i9 >= 0; i9--) {
                ServiceRecord r2 = this.mPendingServices.get(i9);
                if (r2.processName.equals(app.processName) && r2.serviceInfo.applicationInfo.uid == app.info.uid) {
                    this.mPendingServices.remove(i9);
                }
            }
        }
        int i10 = this.mDestroyingServices.size();
        while (i10 > 0) {
            i10--;
            ServiceRecord sr3 = this.mDestroyingServices.get(i10);
            if (sr3.app == app) {
                sr3.forceClearTracker();
                this.mDestroyingServices.remove(i10);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "killServices remove destroying " + sr3);
                }
            }
        }
        app.executingServices.clear();
        return;
        while (true) {
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.RunningServiceInfo makeRunningServiceInfoLocked(ServiceRecord r) {
        ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
        info.service = r.name;
        if (r.app != null) {
            info.pid = r.app.pid;
        }
        info.uid = r.appInfo.uid;
        info.process = r.processName;
        info.foreground = r.isForeground;
        info.activeSince = r.createRealTime;
        info.started = r.startRequested;
        info.clientCount = r.getConnections().size();
        info.crashCount = r.crashCount;
        info.lastActivityTime = r.lastActivity;
        if (r.isForeground) {
            info.flags |= 2;
        }
        if (r.startRequested) {
            info.flags |= 1;
        }
        if (r.app != null && r.app.pid == ActivityManagerService.MY_PID) {
            info.flags |= 4;
        }
        if (r.app != null && r.app.isPersistent()) {
            info.flags |= 8;
        }
        ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
        for (int conni = connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> connl = connections.valueAt(conni);
            for (int i = 0; i < connl.size(); i++) {
                ConnectionRecord conn = connl.get(i);
                if (conn.clientLabel != 0) {
                    info.clientPackage = conn.binding.client.info.packageName;
                    info.clientLabel = conn.clientLabel;
                    return info;
                }
            }
        }
        return info;
    }

    /* access modifiers changed from: package-private */
    public List<ActivityManager.RunningServiceInfo> getRunningServiceInfoLocked(int maxNum, int flags, int callingUid, boolean allowed, boolean canInteractAcrossUsers) {
        ArrayList<ActivityManager.RunningServiceInfo> res = new ArrayList<>();
        long ident = Binder.clearCallingIdentity();
        int i = 0;
        if (canInteractAcrossUsers) {
            try {
                int[] users = this.mAm.mUserController.getUsers();
                for (int ui = 0; ui < users.length && res.size() < maxNum; ui++) {
                    ArrayMap<ComponentName, ServiceRecord> alls = getServicesLocked(users[ui]);
                    for (int i2 = 0; i2 < alls.size() && res.size() < maxNum; i2++) {
                        res.add(makeRunningServiceInfoLocked(alls.valueAt(i2)));
                    }
                }
                while (i < this.mRestartingServices.size() && res.size() < maxNum) {
                    ServiceRecord r = this.mRestartingServices.get(i);
                    ActivityManager.RunningServiceInfo info = makeRunningServiceInfoLocked(r);
                    info.restarting = r.nextRestartTime;
                    res.add(info);
                    i++;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            int userId = UserHandle.getUserId(callingUid);
            ArrayMap<ComponentName, ServiceRecord> alls2 = getServicesLocked(userId);
            for (int i3 = 0; i3 < alls2.size() && res.size() < maxNum; i3++) {
                ServiceRecord sr = alls2.valueAt(i3);
                if (allowed || (sr.app != null && sr.app.uid == callingUid)) {
                    res.add(makeRunningServiceInfoLocked(sr));
                }
            }
            while (i < this.mRestartingServices.size() && res.size() < maxNum) {
                ServiceRecord r2 = this.mRestartingServices.get(i);
                if (r2.userId == userId && (allowed || (r2.app != null && r2.app.uid == callingUid))) {
                    ActivityManager.RunningServiceInfo info2 = makeRunningServiceInfoLocked(r2);
                    info2.restarting = r2.nextRestartTime;
                    res.add(info2);
                }
                i++;
            }
        }
        Binder.restoreCallingIdentity(ident);
        return res;
    }

    public PendingIntent getRunningServiceControlPanelLocked(ComponentName name) {
        ServiceRecord r = getServiceByNameLocked(name, UserHandle.getUserId(Binder.getCallingUid()));
        if (r == null) {
            return null;
        }
        ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
        for (int conni = connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> conn = connections.valueAt(conni);
            for (int i = 0; i < conn.size(); i++) {
                if (conn.get(i).clientIntent != null) {
                    return conn.get(i).clientIntent;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ef, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00f2, code lost:
        if (r10 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00f4, code lost:
        r18.appNotResponding(null, null, null, null, false, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        return;
     */
    public void serviceTimeout(ProcessRecord proc) {
        Throwable th;
        String anrMessage;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (proc.isDebugging()) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (proc.executingServices.size() == 0 || proc.thread == null) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else {
                    long maxTime = SystemClock.uptimeMillis() - ((long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT));
                    ServiceRecord timeout = null;
                    long nextTime = 0;
                    int i = proc.executingServices.size() - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        }
                        ServiceRecord sr = proc.executingServices.valueAt(i);
                        if (sr.executingStart < maxTime) {
                            timeout = sr;
                            break;
                        }
                        if (sr.executingStart > nextTime) {
                            nextTime = sr.executingStart;
                        }
                        i--;
                    }
                    if (timeout == null || !this.mAm.mProcessList.mLruProcesses.contains(proc)) {
                        Message msg = this.mAm.mHandler.obtainMessage(12);
                        msg.obj = proc;
                        this.mAm.mHandler.sendMessageAtTime(msg, proc.execServicesFg ? 20000 + nextTime : 200000 + nextTime);
                        anrMessage = null;
                    } else {
                        Slog.w(TAG, "Timeout executing service: " + timeout);
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                        pw.println(timeout);
                        timeout.dump(pw, "    ");
                        pw.close();
                        this.mLastAnrDump = sw.toString();
                        this.mAm.mHandler.removeCallbacks(this.mLastAnrDumpClearer);
                        this.mAm.mHandler.postDelayed(this.mLastAnrDumpClearer, AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT);
                        anrMessage = "executing service " + timeout.shortInstanceName;
                    }
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003f, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0042, code lost:
        if (r1 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0044, code lost:
        r1.appNotResponding(null, null, null, null, false, "Context.startForegroundService() did not then call Service.startForeground(): " + r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    public void serviceForegroundTimeout(ServiceRecord r) {
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (r.fgRequired) {
                    if (!r.destroying) {
                        ProcessRecord app = r.app;
                        if (app == null || !app.isDebugging()) {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(TAG, "Service foreground-required timeout for " + r);
                            }
                            r.fgWaiting = false;
                            stopServiceLocked(r);
                        } else {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void updateServiceApplicationInfoLocked(ApplicationInfo applicationInfo) {
        ServiceMap serviceMap = this.mServiceMap.get(UserHandle.getUserId(applicationInfo.uid));
        if (serviceMap != null) {
            ArrayMap<ComponentName, ServiceRecord> servicesByName = serviceMap.mServicesByInstanceName;
            for (int j = servicesByName.size() - 1; j >= 0; j--) {
                ServiceRecord serviceRecord = servicesByName.valueAt(j);
                if (applicationInfo.packageName.equals(serviceRecord.appInfo.packageName)) {
                    serviceRecord.appInfo = applicationInfo;
                    serviceRecord.serviceInfo.applicationInfo = applicationInfo;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void serviceForegroundCrash(ProcessRecord app, CharSequence serviceRecord) {
        ActivityManagerService activityManagerService = this.mAm;
        int i = app.uid;
        int i2 = app.pid;
        String str = app.info.packageName;
        int i3 = app.userId;
        activityManagerService.crashApplication(i, i2, str, i3, "Context.startForegroundService() did not then call Service.startForeground(): " + ((Object) serviceRecord), false);
    }

    /* access modifiers changed from: package-private */
    public void scheduleServiceTimeoutLocked(ProcessRecord proc) {
        if (proc.executingServices.size() != 0 && proc.thread != null) {
            Message msg = this.mAm.mHandler.obtainMessage(12);
            msg.obj = proc;
            this.mAm.mHandler.sendMessageDelayed(msg, proc.execServicesFg ? 20000 : 200000);
            this.mAm.mAnrManager.sendServiceMonitorMessage();
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleServiceForegroundTransitionTimeoutLocked(ServiceRecord r) {
        if (r.app.executingServices.size() != 0 && r.app.thread != null) {
            Message msg = this.mAm.mHandler.obtainMessage(66);
            msg.obj = r;
            r.fgWaiting = true;
            this.mAm.mHandler.sendMessageDelayed(msg, 10000);
        }
    }

    /* access modifiers changed from: package-private */
    public final class ServiceDumper {
        private final String[] args;
        private final boolean dumpAll;
        private final String dumpPackage;
        private final FileDescriptor fd;
        private final ActivityManagerService.ItemMatcher matcher;
        private boolean needSep;
        private final long nowReal = SystemClock.elapsedRealtime();
        private boolean printed;
        private boolean printedAnything;
        private final PrintWriter pw;
        private final ArrayList<ServiceRecord> services = new ArrayList<>();
        final /* synthetic */ ActiveServices this$0;

        ServiceDumper(ActiveServices this$02, FileDescriptor fd2, PrintWriter pw2, String[] args2, int opti, boolean dumpAll2, String dumpPackage2) {
            ActiveServices activeServices = this$02;
            this.this$0 = activeServices;
            int i = 0;
            this.needSep = false;
            this.printedAnything = false;
            this.printed = false;
            this.fd = fd2;
            this.pw = pw2;
            this.args = args2;
            this.dumpAll = dumpAll2;
            this.dumpPackage = dumpPackage2;
            this.matcher = new ActivityManagerService.ItemMatcher();
            this.matcher.build(args2, opti);
            int[] users = activeServices.mAm.mUserController.getUsers();
            int length = users.length;
            while (i < length) {
                ServiceMap smap = activeServices.getServiceMapLocked(users[i]);
                if (smap.mServicesByInstanceName.size() > 0) {
                    for (int si = 0; si < smap.mServicesByInstanceName.size(); si++) {
                        ServiceRecord r = smap.mServicesByInstanceName.valueAt(si);
                        if (this.matcher.match(r, r.name) && (dumpPackage2 == null || dumpPackage2.equals(r.appInfo.packageName))) {
                            this.services.add(r);
                        }
                    }
                }
                i++;
                activeServices = this$02;
            }
        }

        private void dumpHeaderLocked() {
            this.pw.println("ACTIVITY MANAGER SERVICES (dumpsys activity services)");
            if (this.this$0.mLastAnrDump != null) {
                this.pw.println("  Last ANR service:");
                this.pw.print(this.this$0.mLastAnrDump);
                this.pw.println();
            }
        }

        /* access modifiers changed from: package-private */
        public void dumpLocked() {
            dumpHeaderLocked();
            try {
                int[] users = this.this$0.mAm.mUserController.getUsers();
                int length = users.length;
                for (int i = 0; i < length; i++) {
                    int user = users[i];
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && this.services.get(serviceIdx).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
                        while (true) {
                            if (serviceIdx >= this.services.size()) {
                                break;
                            }
                            ServiceRecord r = this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            dumpServiceLocalLocked(r);
                        }
                        this.needSep |= this.printed;
                    }
                    dumpUserRemainsLocked(user);
                }
            } catch (Exception e) {
                Slog.w(ActiveServices.TAG, "Exception in dumpServicesLocked", e);
            }
            dumpRemainsLocked();
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public void dumpWithClient() {
            synchronized (this.this$0.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpHeaderLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            try {
                int[] users = this.this$0.mAm.mUserController.getUsers();
                int length = users.length;
                for (int i = 0; i < length; i++) {
                    int user = users[i];
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && this.services.get(serviceIdx).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
                        while (true) {
                            if (serviceIdx >= this.services.size()) {
                                break;
                            }
                            ServiceRecord r = this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            synchronized (this.this$0.mAm) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    dumpServiceLocalLocked(r);
                                } catch (Throwable th) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            dumpServiceClient(r);
                        }
                        this.needSep |= this.printed;
                    }
                    synchronized (this.this$0.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            dumpUserRemainsLocked(user);
                        } catch (Throwable th2) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th2;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Exception e) {
                Slog.w(ActiveServices.TAG, "Exception in dumpServicesLocked", e);
            }
            synchronized (this.this$0.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpRemainsLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        private void dumpUserHeaderLocked(int user) {
            if (!this.printed) {
                if (this.printedAnything) {
                    this.pw.println();
                }
                PrintWriter printWriter = this.pw;
                printWriter.println("  User " + user + " active services:");
                this.printed = true;
            }
            this.printedAnything = true;
            if (this.needSep) {
                this.pw.println();
            }
        }

        private void dumpServiceLocalLocked(ServiceRecord r) {
            dumpUserHeaderLocked(r.userId);
            this.pw.print("  * ");
            this.pw.println(r);
            if (this.dumpAll) {
                r.dump(this.pw, "    ");
                this.needSep = true;
                return;
            }
            this.pw.print("    app=");
            this.pw.println(r.app);
            this.pw.print("    created=");
            TimeUtils.formatDuration(r.createRealTime, this.nowReal, this.pw);
            this.pw.print(" started=");
            this.pw.print(r.startRequested);
            this.pw.print(" connections=");
            ArrayMap<IBinder, ArrayList<ConnectionRecord>> connections = r.getConnections();
            this.pw.println(connections.size());
            if (connections.size() > 0) {
                this.pw.println("    Connections:");
                for (int conni = 0; conni < connections.size(); conni++) {
                    ArrayList<ConnectionRecord> clist = connections.valueAt(conni);
                    for (int i = 0; i < clist.size(); i++) {
                        ConnectionRecord conn = clist.get(i);
                        this.pw.print("      ");
                        this.pw.print(conn.binding.intent.intent.getIntent().toShortString(false, false, false, false));
                        this.pw.print(" -> ");
                        ProcessRecord proc = conn.binding.client;
                        this.pw.println(proc != null ? proc.toShortString() : "null");
                    }
                }
            }
        }

        private void dumpServiceClient(ServiceRecord r) {
            IApplicationThread thread;
            ProcessRecord proc = r.app;
            if (proc != null && (thread = proc.thread) != null) {
                this.pw.println("    Client:");
                this.pw.flush();
                try {
                    TransferPipe tp = new TransferPipe();
                    try {
                        thread.dumpService(tp.getWriteFd(), r, this.args);
                        tp.setBufferPrefix("      ");
                        tp.go(this.fd, 2000);
                    } finally {
                        tp.kill();
                    }
                } catch (IOException e) {
                    PrintWriter printWriter = this.pw;
                    printWriter.println("      Failure while dumping the service: " + e);
                } catch (RemoteException e2) {
                    this.pw.println("      Got a RemoteException while dumping the service");
                }
                this.needSep = true;
            }
        }

        private void dumpUserRemainsLocked(int user) {
            String str;
            String str2;
            ServiceMap smap = this.this$0.getServiceMapLocked(user);
            this.printed = false;
            int SN = smap.mDelayedStartList.size();
            for (int si = 0; si < SN; si++) {
                ServiceRecord r = smap.mDelayedStartList.get(si);
                if (this.matcher.match(r, r.name) && ((str2 = this.dumpPackage) == null || str2.equals(r.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        PrintWriter printWriter = this.pw;
                        printWriter.println("  User " + user + " delayed start services:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Delayed start ");
                    this.pw.println(r);
                }
            }
            this.printed = false;
            int SN2 = smap.mStartingBackground.size();
            for (int si2 = 0; si2 < SN2; si2++) {
                ServiceRecord r2 = smap.mStartingBackground.get(si2);
                if (this.matcher.match(r2, r2.name) && ((str = this.dumpPackage) == null || str.equals(r2.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        PrintWriter printWriter2 = this.pw;
                        printWriter2.println("  User " + user + " starting in background:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Starting bg ");
                    this.pw.println(r2);
                }
            }
        }

        private void dumpRemainsLocked() {
            String str;
            String str2;
            String str3;
            if (this.this$0.mPendingServices.size() > 0) {
                this.printed = false;
                for (int i = 0; i < this.this$0.mPendingServices.size(); i++) {
                    ServiceRecord r = this.this$0.mPendingServices.get(i);
                    if (this.matcher.match(r, r.name) && ((str3 = this.dumpPackage) == null || str3.equals(r.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Pending services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Pending ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.this$0.mRestartingServices.size() > 0) {
                this.printed = false;
                for (int i2 = 0; i2 < this.this$0.mRestartingServices.size(); i2++) {
                    ServiceRecord r2 = this.this$0.mRestartingServices.get(i2);
                    if (this.matcher.match(r2, r2.name) && ((str2 = this.dumpPackage) == null || str2.equals(r2.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Restarting services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Restarting ");
                        this.pw.println(r2);
                        r2.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.this$0.mDestroyingServices.size() > 0) {
                this.printed = false;
                for (int i3 = 0; i3 < this.this$0.mDestroyingServices.size(); i3++) {
                    ServiceRecord r3 = this.this$0.mDestroyingServices.get(i3);
                    if (this.matcher.match(r3, r3.name) && ((str = this.dumpPackage) == null || str.equals(r3.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Destroying services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Destroy ");
                        this.pw.println(r3);
                        r3.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.dumpAll) {
                this.printed = false;
                for (int ic = 0; ic < this.this$0.mServiceConnections.size(); ic++) {
                    ArrayList<ConnectionRecord> r4 = this.this$0.mServiceConnections.valueAt(ic);
                    for (int i4 = 0; i4 < r4.size(); i4++) {
                        ConnectionRecord cr = r4.get(i4);
                        if (this.matcher.match(cr.binding.service, cr.binding.service.name) && (this.dumpPackage == null || (cr.binding.client != null && this.dumpPackage.equals(cr.binding.client.info.packageName)))) {
                            this.printedAnything = true;
                            if (!this.printed) {
                                if (this.needSep) {
                                    this.pw.println();
                                }
                                this.needSep = true;
                                this.pw.println("  Connection bindings to services:");
                                this.printed = true;
                            }
                            this.pw.print("  * ");
                            this.pw.println(cr);
                            cr.dump(this.pw, "    ");
                        }
                    }
                }
            }
            if (this.matcher.all) {
                long nowElapsed = SystemClock.elapsedRealtime();
                int[] users = this.this$0.mAm.mUserController.getUsers();
                for (int user : users) {
                    boolean printedUser = false;
                    ServiceMap smap = this.this$0.mServiceMap.get(user);
                    if (smap != null) {
                        for (int i5 = smap.mActiveForegroundApps.size() - 1; i5 >= 0; i5--) {
                            ActiveForegroundApp aa = smap.mActiveForegroundApps.valueAt(i5);
                            String str4 = this.dumpPackage;
                            if (str4 == null || str4.equals(aa.mPackageName)) {
                                if (!printedUser) {
                                    printedUser = true;
                                    this.printedAnything = true;
                                    if (this.needSep) {
                                        this.pw.println();
                                    }
                                    this.needSep = true;
                                    this.pw.print("Active foreground apps - user ");
                                    this.pw.print(user);
                                    this.pw.println(":");
                                }
                                this.pw.print("  #");
                                this.pw.print(i5);
                                this.pw.print(": ");
                                this.pw.println(aa.mPackageName);
                                if (aa.mLabel != null) {
                                    this.pw.print("    mLabel=");
                                    this.pw.println(aa.mLabel);
                                }
                                this.pw.print("    mNumActive=");
                                this.pw.print(aa.mNumActive);
                                this.pw.print(" mAppOnTop=");
                                this.pw.print(aa.mAppOnTop);
                                this.pw.print(" mShownWhileTop=");
                                this.pw.print(aa.mShownWhileTop);
                                this.pw.print(" mShownWhileScreenOn=");
                                this.pw.println(aa.mShownWhileScreenOn);
                                this.pw.print("    mStartTime=");
                                TimeUtils.formatDuration(aa.mStartTime - nowElapsed, this.pw);
                                this.pw.print(" mStartVisibleTime=");
                                TimeUtils.formatDuration(aa.mStartVisibleTime - nowElapsed, this.pw);
                                this.pw.println();
                                if (aa.mEndTime != 0) {
                                    this.pw.print("    mEndTime=");
                                    TimeUtils.formatDuration(aa.mEndTime - nowElapsed, this.pw);
                                    this.pw.println();
                                }
                            }
                        }
                        if (smap.hasMessagesOrCallbacks()) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.printedAnything = true;
                            this.needSep = true;
                            this.pw.print("  Handler - user ");
                            this.pw.print(user);
                            this.pw.println(":");
                            smap.dumpMine(new PrintWriterPrinter(this.pw), "    ");
                        }
                    }
                }
            }
            if (!this.printedAnything) {
                this.pw.println("  (nothing)");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ServiceDumper newServiceDumperLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        return new ServiceDumper(this, fd, pw, args, opti, dumpAll, dumpPackage);
    }

    /* access modifiers changed from: protected */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        int i;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long outterToken = proto.start(fieldId);
                int[] users = this.mAm.mUserController.getUsers();
                int length = users.length;
                int i2 = 0;
                int i3 = 0;
                while (i3 < length) {
                    int user = users[i3];
                    ServiceMap smap = this.mServiceMap.get(user);
                    if (smap == null) {
                        i = i3;
                    } else {
                        long token = proto.start(2246267895809L);
                        proto.write(1120986464257L, user);
                        ArrayMap<ComponentName, ServiceRecord> alls = smap.mServicesByInstanceName;
                        int i4 = i2;
                        while (i4 < alls.size()) {
                            alls.valueAt(i4).writeToProto(proto, 2246267895810L);
                            i4++;
                            i3 = i3;
                        }
                        i = i3;
                        proto.end(token);
                    }
                    i3 = i + 1;
                    i2 = 0;
                }
                proto.end(outterToken);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public boolean dumpService(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        ArrayList<ServiceRecord> services = new ArrayList<>();
        Predicate<ServiceRecord> filter = DumpUtils.filterRecord(name);
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int user : this.mAm.mUserController.getUsers()) {
                    ServiceMap smap = this.mServiceMap.get(user);
                    if (smap != null) {
                        ArrayMap<ComponentName, ServiceRecord> alls = smap.mServicesByInstanceName;
                        for (int i = 0; i < alls.size(); i++) {
                            ServiceRecord r1 = alls.valueAt(i);
                            if (filter.test(r1)) {
                                services.add(r1);
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (services.size() <= 0) {
            return false;
        }
        services.sort(Comparator.comparing($$Lambda$Y_KRxxoOXfyYceuDG7WHd46Y_I.INSTANCE));
        boolean needSep = false;
        int i2 = 0;
        while (i2 < services.size()) {
            if (needSep) {
                pw.println();
            }
            dumpService("", fd, pw, services.get(i2), args, dumpAll);
            i2++;
            needSep = true;
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    private void dumpService(String prefix, FileDescriptor fd, PrintWriter pw, ServiceRecord r, String[] args, boolean dumpAll) {
        String innerPrefix = prefix + "  ";
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                pw.print(prefix);
                pw.print("SERVICE ");
                pw.print(r.shortInstanceName);
                pw.print(StringUtils.SPACE);
                pw.print(Integer.toHexString(System.identityHashCode(r)));
                pw.print(" pid=");
                if (r.app != null) {
                    pw.println(r.app.pid);
                } else {
                    pw.println("(not running)");
                }
                if (dumpAll) {
                    r.dump(pw, innerPrefix);
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (r.app != null && r.app.thread != null) {
            pw.print(prefix);
            pw.println("  Client:");
            pw.flush();
            try {
                TransferPipe tp = new TransferPipe();
                try {
                    r.app.thread.dumpService(tp.getWriteFd(), r, args);
                    tp.setBufferPrefix(prefix + "    ");
                    tp.go(fd);
                } finally {
                    tp.kill();
                }
            } catch (IOException e) {
                pw.println(prefix + "    Failure while dumping the service: " + e);
            } catch (RemoteException e2) {
                pw.println(prefix + "    Got a RemoteException while dumping the service");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDynamicalLogEnable(boolean on) {
        DEBUG_DELAYED_SERVICE = on;
        DEBUG_DELAYED_STARTS = on;
        LOG_SERVICE_START_STOP = on;
    }
}

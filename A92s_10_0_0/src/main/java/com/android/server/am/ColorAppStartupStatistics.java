package com.android.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.coloros.OppoListManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class ColorAppStartupStatistics {
    protected static final String NO_NEED = "noNeed";
    private static final String TAG = "ColorAppStartupManager";
    protected static final String TENCENT_MM = "com.tencent.mm";
    protected static final String TENCENT_QQ = "com.tencent.mobileqq";
    protected static final String TYPE_ALLOW_CALLED_CONTAINS_KEY = "allow_calledContainsKey";
    protected static final String TYPE_ALLOW_CALLED_IS_ACCESSIBILITY = "allow_calledIsAccessibility";
    protected static final String TYPE_ALLOW_CALLED_IS_BROWSER = "allow_calledIsBrowser";
    protected static final String TYPE_ALLOW_CALLED_IS_BROWSERWHITE = "allow_calledIsBrowserWhite";
    protected static final String TYPE_ALLOW_CALLED_IS_BROWSER_IM = "allow_calledIsBrowser_Im";
    protected static final String TYPE_ALLOW_CALLED_IS_IM = "allow_calledIsIm";
    protected static final String TYPE_ALLOW_CALLED_IS_SYSTEM = "allow_calledIsSystem";
    protected static final String TYPE_ALLOW_CALLED_SAME_LAST = "allow_calledSameLast";
    protected static final String TYPE_ALLOW_CALLER_IS_BOOTWHITE = "allow_callerIsBootWhite";
    protected static final String TYPE_ALLOW_CALLER_IS_CONTROL_CENTER = "allow_callerIsControlCenter";
    protected static final String TYPE_ALLOW_CALLER_IS_DEFAULT_DIALER = "allow_callerIsDefaultDialer";
    protected static final String TYPE_ALLOW_CALLER_IS_EMPTY = "allow_callerIsEmpty";
    protected static final String TYPE_ALLOW_CALLER_IS_FLOAT = "allow_callerIsFloat";
    protected static final String TYPE_ALLOW_CALLER_IS_NOTIFY = "allow_callerIsNotify";
    protected static final String TYPE_ALLOW_CALLER_IS_PLAYING = "allow_callerIsPlaying";
    protected static final String TYPE_ALLOW_CALLER_IS_SYSTEM = "allow_callerIsSystem";
    protected static final String TYPE_ALLOW_CALLER_IS_WIDGET = "allow_callerIsWidget";
    protected static final String TYPE_ALLOW_FOCUS_IS_CALLER = "allow_focusIsCaller";
    protected static final String TYPE_ALLOW_LUNCHER = "allow_launcher";
    protected static final String TYPE_ALLOW_MM_QQ = "allow_mm_qq";
    protected static final String TYPE_ALLOW_PENDING_OR_RECENT = "allow_pendingOrRecent";
    protected static final String TYPE_ALLOW_PKG_KEY = "allow_pkg_key";
    protected static final String TYPE_ALLOW_SYSTEM = "allow_system";
    protected static final String TYPE_ALLOW_TOP_IS_ANDROID = "allow_topIsAndroid";
    protected static final String TYPE_ALLOW_TOP_IS_APPLOCK = "allow_topIsApplock";
    protected static final String TYPE_ALLOW_TOP_IS_SPLIT_SCREEN = "allow_topIsSplitScreen";
    protected static final String TYPE_ALLOW_WHITE = "allow_white";
    protected static final String TYPE_FORBID_BLACK = "forbid_black";
    protected static final String TYPE_FORBID_POPUP = "forbid_popup";
    protected static final String TYPE_FORBID_PUSH = "forbid_push";
    protected static final String UPLOAD_APP_RESTRICTED_WHITE = "AppRestrictedWhite";
    protected static final String UPLOAD_APP_STARTUP_EVENTID = "app_startup";
    protected static final String UPLOAD_POPUP_ACTIVITY_EVENTID = "popup_activity";
    private static ColorAppStartupStatistics sOppoAppStartupStatistics = null;
    /* access modifiers changed from: private */
    public ActivityManagerService mAms = null;
    /* access modifiers changed from: private */
    public IColorAppStartupManager mAppStartupManager = null;
    /* access modifiers changed from: private */
    public List<ColorAppStartupMonitorInfo> mCollectAppStartupList = new ArrayList();
    /* access modifiers changed from: private */
    public List<ColorAppStartupMonitorInfo> mCollectPopupActivityList = new ArrayList();
    private Handler mHandler = null;

    private ColorAppStartupStatistics() {
    }

    public static final ColorAppStartupStatistics getInstance() {
        if (sOppoAppStartupStatistics == null) {
            sOppoAppStartupStatistics = new ColorAppStartupStatistics();
        }
        return sOppoAppStartupStatistics;
    }

    /* access modifiers changed from: protected */
    public void init(IColorAppStartupManager appStartupManager, Handler handler, ActivityManagerService ams) {
        this.mAppStartupManager = appStartupManager;
        this.mHandler = handler;
        this.mAms = ams;
    }

    public static class OppoCallActivityEntry {
        public String mCpnName;
        public String mPkgName;

        public OppoCallActivityEntry(String pkgName, String cpnName) {
            this.mPkgName = pkgName;
            this.mCpnName = cpnName;
        }
    }

    /* access modifiers changed from: protected */
    public void collectAppStartInfo(int callingPid, int callingUid, ProcessRecord callerApp, Intent intent, ApplicationInfo appInfo, String hostType) {
        IColorAppStartupManager iColorAppStartupManager;
        if (this.mHandler != null && (iColorAppStartupManager = this.mAppStartupManager) != null) {
            if (iColorAppStartupManager.getDynamicDebug()) {
                Log.d("ColorAppStartupManager", "collectAppStartInfo: callingPid=" + callingPid + " callingUid=" + callingUid + " callerApp=" + callerApp + " intent=" + intent + " appInfo=" + appInfo + " hostType=" + hostType);
            }
            this.mHandler.post(new CollectAppStartRunnable(callingPid, callingUid, callerApp, intent, appInfo, hostType));
        }
    }

    /* access modifiers changed from: protected */
    public void collectPopupActivityInfo(String callerPkg, String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        IColorAppStartupManager iColorAppStartupManager;
        if (this.mHandler != null && (iColorAppStartupManager = this.mAppStartupManager) != null) {
            if (iColorAppStartupManager.getSwitchMonitor()) {
                if (callerPkg != null && calledPkg != null && calledCpnName != null && topPkg != null) {
                    if (type != null) {
                        if (this.mAppStartupManager.getDynamicDebug()) {
                            Log.d("ColorAppStartupManager", "collectPopupActivityInfo: callerPkg=" + callerPkg + " calledPkg=" + calledPkg + " calledCpnName=" + calledCpnName + " topPkg=" + topPkg + " isScreeOn=" + screenState + " type=" + type);
                        }
                        this.mHandler.post(new CollectPopupActivityRunnable(callerPkg, calledPkg, calledCpnName, topPkg, screenState, type));
                    }
                }
            }
        }
    }

    private class CollectAppStartRunnable implements Runnable {
        private ApplicationInfo mAppInfo;
        private ProcessRecord mCallerApp;
        private int mCallingPid;
        private int mCallingUid;
        private String mHostType;
        private Intent mIntent;

        public CollectAppStartRunnable(int callingPid, int callingUid, ProcessRecord callerApp, Intent intent, ApplicationInfo appInfo, String hostType) {
            this.mCallingPid = callingPid;
            this.mCallingUid = callingUid;
            this.mCallerApp = callerApp;
            this.mIntent = intent;
            this.mAppInfo = appInfo;
            this.mHostType = hostType;
        }

        public void run() {
            String callerAppStr;
            String startMode;
            ApplicationInfo applicationInfo = this.mAppInfo;
            if (applicationInfo != null && (applicationInfo.flags & 1) == 0) {
                String callerPkg = ColorAppStartupStatistics.this.mAppStartupManager.getPackageNameForUid(this.mCallingUid);
                String calledPkg = this.mAppInfo.packageName;
                if (callerPkg != null && calledPkg != null && this.mHostType != null) {
                    if (this.mCallingUid == 2000) {
                        try {
                            callerPkg = ColorAppStartupStatistics.this.mAppStartupManager.composePackage(callerPkg, SystemProperties.get("sys.usb.config", ""));
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    ProcessRecord processRecord = this.mCallerApp;
                    if (processRecord == null) {
                        callerAppStr = "null";
                    } else {
                        callerAppStr = processRecord.toString();
                    }
                    boolean isAssociateSwitch = ColorAppStartupStatistics.this.mAppStartupManager.inAssociateStartWhiteList(calledPkg, UserHandle.getUserId(this.mAppInfo.uid));
                    boolean isBootStartSwitch = OppoListManager.getInstance().isInAutoBootWhiteList(calledPkg, UserHandle.getUserId(this.mAppInfo.uid));
                    if (isAssociateSwitch && isBootStartSwitch) {
                        startMode = ColorAppStartupStatistics.this.mAppStartupManager.composePackage("bootstart", "associate");
                    } else if (isAssociateSwitch) {
                        startMode = "associate";
                    } else if (isBootStartSwitch) {
                        startMode = "bootstart";
                    } else if (OppoListManager.getInstance().isInstalledAppWidget(calledPkg, this.mAppInfo.uid) && "broadcast".equals(this.mHostType)) {
                        startMode = ColorCommonListManager.CONFIG_WIDGET;
                    } else if (!calledPkg.equals(this.mAppInfo.processName) || !"activity".equals(this.mHostType)) {
                        startMode = "other";
                    } else {
                        startMode = "click";
                    }
                    Intent intent = this.mIntent;
                    if (intent != null) {
                        ComponentName cpn = intent.getComponent();
                        if (cpn != null && cpn.getClassName() != null) {
                            ColorAppStartupStatistics.this.collectAppStartInfo(callerPkg, this.mCallingPid, callerAppStr, calledPkg, cpn.getClassName(), this.mHostType, startMode);
                        } else if (this.mIntent.getAction() != null) {
                            ColorAppStartupStatistics.this.collectAppStartInfo(callerPkg, this.mCallingPid, callerAppStr, calledPkg, this.mIntent.getAction(), this.mHostType, startMode);
                        }
                    }
                    if (ColorAppStartupStatistics.this.mCollectAppStartupList.size() >= ColorAppStartupManagerUtils.getInstance().getCallCheckCount()) {
                        ColorAppStartupStatistics.this.uploadAppStartupList();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void collectAppStartInfo(String callerPkg, int callingPid, String callerAppStr, String calledPkg, String cpnName, String hostingType, String startMode) {
        ColorAppStartupMonitorInfo appInfo = getAppStartInfo(callerPkg);
        if (appInfo == null) {
            this.mCollectAppStartupList.add(ColorAppStartupMonitorInfo.buildAppStart(callerPkg, callingPid, callerAppStr, calledPkg, cpnName, hostingType, startMode));
            return;
        }
        appInfo.increaseAppStartCount(callingPid, callerAppStr, calledPkg, cpnName, hostingType, startMode);
    }

    private ColorAppStartupMonitorInfo getAppStartInfo(String callerPkg) {
        for (ColorAppStartupMonitorInfo appinfo : this.mCollectAppStartupList) {
            if (appinfo.getCallerPkgName().equals(callerPkg)) {
                return appinfo;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void uploadAppStartupList() {
        if (!this.mCollectAppStartupList.isEmpty() && this.mAppStartupManager != null) {
            List<Map<String, String>> uploadList = new ArrayList<>();
            int length = this.mCollectAppStartupList.size();
            for (int i = 0; i < length; i++) {
                ColorAppStartupMonitorInfo appInfo = this.mCollectAppStartupList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getAppStartMap());
                }
                if (i == length - 1) {
                    appInfo.clearProcessStartList();
                }
            }
            ActivityManagerService activityManagerService = this.mAms;
            if (activityManagerService != null) {
                OppoStatistics.onCommon(activityManagerService.mContext, "20089", UPLOAD_APP_STARTUP_EVENTID, uploadList, false);
            }
            if (this.mAppStartupManager.getDynamicDebug()) {
                Log.d("ColorAppStartupManager", "AppStartupList size " + uploadList.size());
                Iterator<Map<String, String>> it = uploadList.iterator();
                while (it.hasNext()) {
                    Log.d("ColorAppStartupManager", "AppStartupList info " + it.next());
                }
            }
            this.mCollectAppStartupList.clear();
        }
    }

    private class CollectPopupActivityRunnable implements Runnable {
        private String mCalledCpnName;
        private String mCalledPkg;
        private String mCallerPkg;
        private String mScreenState;
        private String mTopPkg;
        private String mType;

        public CollectPopupActivityRunnable(String callerPkg, String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
            this.mCallerPkg = callerPkg;
            this.mCalledPkg = calledPkg;
            this.mCalledCpnName = calledCpnName;
            this.mTopPkg = topPkg;
            this.mScreenState = screenState;
            this.mType = type;
        }

        public void run() {
            ColorAppStartupStatistics.this.collectPopupActivitInfo(this.mCallerPkg, this.mCalledPkg, this.mCalledCpnName, this.mTopPkg, this.mScreenState, this.mType);
            if (ColorAppStartupStatistics.this.mCollectPopupActivityList.size() >= ColorAppStartupManagerUtils.getInstance().getCallCheckCount()) {
                ColorAppStartupStatistics.this.uploadPopupActivityList();
            }
        }
    }

    /* access modifiers changed from: private */
    public void collectPopupActivitInfo(String callerPkg, String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        ColorAppStartupMonitorInfo appInfo = getPopupActivityInfo(callerPkg);
        if (appInfo == null) {
            this.mCollectPopupActivityList.add(ColorAppStartupMonitorInfo.buildPopupActivity(callerPkg, calledPkg, calledCpnName, topPkg, screenState, type));
            return;
        }
        appInfo.increasePopupActivityCount(calledPkg, calledCpnName, topPkg, screenState, type);
    }

    private ColorAppStartupMonitorInfo getPopupActivityInfo(String callerPkg) {
        for (ColorAppStartupMonitorInfo appInfo : this.mCollectPopupActivityList) {
            if (appInfo.getCallerPkgName().equals(callerPkg)) {
                return appInfo;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void uploadPopupActivityList() {
        if (!this.mCollectPopupActivityList.isEmpty() && this.mAppStartupManager != null) {
            List<Map<String, String>> uploadList = new ArrayList<>();
            int length = this.mCollectPopupActivityList.size();
            for (int i = 0; i < length; i++) {
                ColorAppStartupMonitorInfo appInfo = this.mCollectPopupActivityList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getPopupActivityMap());
                }
                if (i == length - 1) {
                    appInfo.clearPopupActivityList();
                }
            }
            ActivityManagerService activityManagerService = this.mAms;
            if (activityManagerService != null) {
                OppoStatistics.onCommon(activityManagerService.mContext, "20089", UPLOAD_POPUP_ACTIVITY_EVENTID, uploadList, false);
            }
            if (this.mAppStartupManager.getDynamicDebug()) {
                Log.d("ColorAppStartupManager", "PopupActivityList size " + uploadList.size());
                Iterator<Map<String, String>> it = uploadList.iterator();
                while (it.hasNext()) {
                    Log.d("ColorAppStartupManager", "PopupActivityList info " + it.next());
                }
            }
            this.mCollectAppStartupList.clear();
        }
    }

    public void collectRestrictedBackgroundWhitelist(String callerPkg, List<String> targetPkgList) {
        Handler handler = this.mHandler;
        if (handler != null && this.mAppStartupManager != null) {
            handler.post(new RestrictedBackgroundWhitelistRunnable(callerPkg, targetPkgList));
        }
    }

    private class RestrictedBackgroundWhitelistRunnable implements Runnable {
        private String mCallerPkg;
        private List<String> mTargetPkgList;

        public RestrictedBackgroundWhitelistRunnable(String callerPkg, List<String> targetPkgList) {
            this.mCallerPkg = callerPkg;
            this.mTargetPkgList = targetPkgList;
        }

        public void run() {
            try {
                if (ColorAppStartupStatistics.this.mAms != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String str : this.mTargetPkgList) {
                        stringBuilder.append(str);
                        stringBuilder.append(",");
                    }
                    Map<String, String> uploadList = new HashMap<>();
                    uploadList.put("callerPkg", this.mCallerPkg);
                    uploadList.put("calledPkg", stringBuilder.toString());
                    OppoStatistics.onCommon(ColorAppStartupStatistics.this.mAms.mContext, "20089", ColorAppStartupStatistics.UPLOAD_APP_RESTRICTED_WHITE, uploadList, false);
                }
            } catch (Exception e) {
            }
        }
    }
}

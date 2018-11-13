package com.android.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.arp.OppoArpPeer;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.coloros.OppoListManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoAppStartupStatistics {
    protected static final String NO_NEED = "noNeed";
    private static final String TAG = "OppoAppStartupManager";
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
    protected static final String UPLOAD_APP_STARTUP_EVENTID = "app_startup";
    protected static final String UPLOAD_POPUP_ACTIVITY_EVENTID = "popup_activity";
    private static OppoAppStartupStatistics sOppoAppStartupStatistics = null;
    private ActivityManagerService mAms = null;
    private OppoAppStartupManager mAppStartupManager = null;
    private List<OppoAppMonitorInfo> mCollectAppStartupList = new ArrayList();
    private List<OppoAppMonitorInfo> mCollectPopupActivityList = new ArrayList();
    private Handler mHandler = null;

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
            if (this.mAppInfo != null && (this.mAppInfo.flags & 1) == 0) {
                String callerPkg = OppoAppStartupStatistics.this.mAppStartupManager.getPackageNameForUid(this.mCallingUid);
                String calledPkg = this.mAppInfo.packageName;
                if (callerPkg != null && calledPkg != null && this.mHostType != null) {
                    String callerAppStr;
                    if (this.mCallingUid == OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT) {
                        try {
                            callerPkg = OppoAppStartupStatistics.this.mAppStartupManager.composePackage(callerPkg, SystemProperties.get("sys.usb.config", ""));
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    if (this.mCallerApp == null) {
                        callerAppStr = "null";
                    } else {
                        callerAppStr = this.mCallerApp.toString();
                    }
                    String startMode = "other";
                    boolean isAssociateSwitch = OppoAppStartupStatistics.this.mAppStartupManager.inAssociateStartWhiteList(calledPkg);
                    boolean isBootStartSwitch = OppoListManager.getInstance().isInAutoBootWhiteList(calledPkg);
                    if (isAssociateSwitch && isBootStartSwitch) {
                        startMode = OppoAppStartupStatistics.this.mAppStartupManager.composePackage("bootstart", "associate");
                    } else if (isAssociateSwitch) {
                        startMode = "associate";
                    } else if (isBootStartSwitch) {
                        startMode = "bootstart";
                    } else if (OppoListManager.getInstance().isInstalledAppWidget(calledPkg) && "broadcast".equals(this.mHostType)) {
                        startMode = "widget";
                    } else if (calledPkg.equals(this.mAppInfo.processName) && OppoAppStartupManager.TYPE_ACTIVITY.equals(this.mHostType)) {
                        startMode = "click";
                    }
                    if (this.mIntent != null) {
                        ComponentName cpn = this.mIntent.getComponent();
                        if (cpn != null && cpn.getClassName() != null) {
                            OppoAppStartupStatistics.this.collectAppStartInfo(callerPkg, this.mCallingPid, callerAppStr, calledPkg, cpn.getClassName(), this.mHostType, startMode);
                        } else if (this.mIntent.getAction() != null) {
                            OppoAppStartupStatistics.this.collectAppStartInfo(callerPkg, this.mCallingPid, callerAppStr, calledPkg, this.mIntent.getAction(), this.mHostType, startMode);
                        }
                    }
                    if (OppoAppStartupStatistics.this.mCollectAppStartupList.size() >= OppoAppStartupManagerUtils.getInstance().getCallCheckCount()) {
                        OppoAppStartupStatistics.this.uploadAppStartupList();
                    }
                }
            }
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
            OppoAppStartupStatistics.this.collectPopupActivitInfo(this.mCallerPkg, this.mCalledPkg, this.mCalledCpnName, this.mTopPkg, this.mScreenState, this.mType);
            if (OppoAppStartupStatistics.this.mCollectPopupActivityList.size() >= OppoAppStartupManagerUtils.getInstance().getCallCheckCount()) {
                OppoAppStartupStatistics.this.uploadPopupActivityList();
            }
        }
    }

    public static class OppoCallActivityEntry {
        public String mCpnName;
        public String mPkgName;

        public OppoCallActivityEntry(String pkgName, String cpnName) {
            this.mPkgName = pkgName;
            this.mCpnName = cpnName;
        }
    }

    private OppoAppStartupStatistics() {
    }

    public static final OppoAppStartupStatistics getInstance() {
        if (sOppoAppStartupStatistics == null) {
            sOppoAppStartupStatistics = new OppoAppStartupStatistics();
        }
        return sOppoAppStartupStatistics;
    }

    protected void init(OppoAppStartupManager appStartupManager, Handler handler, ActivityManagerService ams) {
        this.mAppStartupManager = appStartupManager;
        this.mHandler = handler;
        this.mAms = ams;
    }

    protected void collectAppStartInfo(int callingPid, int callingUid, ProcessRecord callerApp, Intent intent, ApplicationInfo appInfo, String hostType) {
        if (this.mHandler != null && this.mAppStartupManager != null) {
            if (this.mAppStartupManager.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "collectAppStartInfo: callingPid=" + callingPid + " callingUid=" + callingUid + " callerApp=" + callerApp + " intent=" + intent + " appInfo=" + appInfo + " hostType=" + hostType);
            }
            this.mHandler.post(new CollectAppStartRunnable(callingPid, callingUid, callerApp, intent, appInfo, hostType));
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void collectPopupActivityInfo(String callerPkg, String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        if (this.mHandler != null && this.mAppStartupManager != null && (this.mAppStartupManager.mSwitchMonitor ^ 1) == 0 && callerPkg != null && calledPkg != null && calledCpnName != null && topPkg != null && type != null) {
            if (this.mAppStartupManager.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "collectPopupActivityInfo: callerPkg=" + callerPkg + " calledPkg=" + calledPkg + " calledCpnName=" + calledCpnName + " topPkg=" + topPkg + " isScreeOn=" + screenState + " type=" + type);
            }
            this.mHandler.post(new CollectPopupActivityRunnable(callerPkg, calledPkg, calledCpnName, topPkg, screenState, type));
        }
    }

    private void collectAppStartInfo(String callerPkg, int callingPid, String callerAppStr, String calledPkg, String cpnName, String hostingType, String startMode) {
        OppoAppMonitorInfo appInfo = getAppStartInfo(callerPkg);
        if (appInfo == null) {
            this.mCollectAppStartupList.add(OppoAppMonitorInfo.buildAppStart(callerPkg, callingPid, callerAppStr, calledPkg, cpnName, hostingType, startMode));
            return;
        }
        appInfo.increaseAppStartCount(callingPid, callerAppStr, calledPkg, cpnName, hostingType, startMode);
    }

    private OppoAppMonitorInfo getAppStartInfo(String callerPkg) {
        for (OppoAppMonitorInfo appinfo : this.mCollectAppStartupList) {
            if (appinfo.getCallerPkgName().equals(callerPkg)) {
                return appinfo;
            }
        }
        return null;
    }

    protected void uploadAppStartupList() {
        if (!this.mCollectAppStartupList.isEmpty() && this.mAppStartupManager != null) {
            List<Map<String, String>> uploadList = new ArrayList();
            int length = this.mCollectAppStartupList.size();
            for (int i = 0; i < length; i++) {
                OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mCollectAppStartupList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getAppStartMap());
                }
                if (i == length - 1) {
                    appInfo.clearProcessStartList();
                }
            }
            if (this.mAms != null) {
                OppoStatistics.onCommon(this.mAms.mContext, "20089", UPLOAD_APP_STARTUP_EVENTID, uploadList, false);
            }
            if (this.mAppStartupManager.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "AppStartupList size " + uploadList.size());
                for (Map<String, String> info : uploadList) {
                    Log.d("OppoAppStartupManager", "AppStartupList info " + info);
                }
            }
            this.mCollectAppStartupList.clear();
        }
    }

    private void collectPopupActivitInfo(String callerPkg, String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        OppoAppMonitorInfo appInfo = getPopupActivityInfo(callerPkg);
        if (appInfo == null) {
            this.mCollectPopupActivityList.add(OppoAppMonitorInfo.buildPopupActivity(callerPkg, calledPkg, calledCpnName, topPkg, screenState, type));
            return;
        }
        appInfo.increasePopupActivityCount(calledPkg, calledCpnName, topPkg, screenState, type);
    }

    private OppoAppMonitorInfo getPopupActivityInfo(String callerPkg) {
        for (OppoAppMonitorInfo appInfo : this.mCollectPopupActivityList) {
            if (appInfo.getCallerPkgName().equals(callerPkg)) {
                return appInfo;
            }
        }
        return null;
    }

    protected void uploadPopupActivityList() {
        if (!this.mCollectPopupActivityList.isEmpty() && this.mAppStartupManager != null) {
            List<Map<String, String>> uploadList = new ArrayList();
            int length = this.mCollectPopupActivityList.size();
            for (int i = 0; i < length; i++) {
                OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mCollectPopupActivityList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getPopupActivityMap());
                }
                if (i == length - 1) {
                    appInfo.clearPopupActivityList();
                }
            }
            if (this.mAms != null) {
                OppoStatistics.onCommon(this.mAms.mContext, "20089", UPLOAD_POPUP_ACTIVITY_EVENTID, uploadList, false);
            }
            if (this.mAppStartupManager.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "PopupActivityList size " + uploadList.size());
                for (Map<String, String> info : uploadList) {
                    Log.d("OppoAppStartupManager", "PopupActivityList info " + info);
                }
            }
            this.mCollectAppStartupList.clear();
        }
    }
}

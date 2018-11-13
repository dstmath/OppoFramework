package com.android.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.util.Log;
import com.android.server.coloros.OppoListManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

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
public class OppoAppStartupStatistics {
    protected static final String NO_NEED = "noNeed";
    private static final String TAG = null;
    protected static final String TYPE_ALLOW_CALLED_CONTAINS_KEY = "allow_calledContainsKey";
    protected static final String TYPE_ALLOW_CALLED_IS_ACCESSIBILITY = "allow_calledIsAccessibility";
    protected static final String TYPE_ALLOW_CALLED_IS_BROWSER = "allow_calledIsBrowser";
    protected static final String TYPE_ALLOW_CALLED_IS_BROWSERWHITE = "allow_calledIsBrowserWhite";
    protected static final String TYPE_ALLOW_CALLED_IS_BROWSER_IM = "allow_calledIsBrowser_Im";
    protected static final String TYPE_ALLOW_CALLED_IS_IM = "allow_calledIsIm";
    protected static final String TYPE_ALLOW_CALLED_IS_SYSTEM = "allow_calledIsSystem";
    protected static final String TYPE_ALLOW_CALLED_SAME_LAST = "allow_calledSameLast";
    protected static final String TYPE_ALLOW_CALLER_IS_BOOTWHITE = "allow_callerIsBootWhite";
    protected static final String TYPE_ALLOW_CALLER_IS_EMPTY = "allow_callerIsEmpty";
    protected static final String TYPE_ALLOW_CALLER_IS_FLOAT = "allow_callerIsFloat";
    protected static final String TYPE_ALLOW_CALLER_IS_NOTIFY = "allow_callerIsNotify";
    protected static final String TYPE_ALLOW_CALLER_IS_PLAYING = "allow_callerIsPlaying";
    protected static final String TYPE_ALLOW_CALLER_IS_SYSTEM = "allow_callerIsSystem";
    protected static final String TYPE_ALLOW_CALLER_IS_TOP = "allow_callerIsTop";
    protected static final String TYPE_ALLOW_CALLER_IS_WIDGET = "allow_callerIsWidget";
    protected static final String TYPE_ALLOW_FOCUS_IS_CALLER = "allow_focusIsCaller";
    protected static final String TYPE_ALLOW_FOCUS_IS_SYSTEMUI = "allow_focusIsSystemui";
    protected static final String TYPE_ALLOW_LUNCHER = "allow_launcher";
    protected static final String TYPE_ALLOW_PENDING_OR_RECENT = "allow_pendingOrRecent";
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
    private static OppoAppStartupStatistics mOppoAppStartupStatistics;
    private ActivityManagerService mAms;
    private OppoAppStartupManager mAppStartupManager;
    private List<OppoAppMonitorInfo> mCollectAppStartupList;
    private List<OppoAppMonitorInfo> mCollectPopupActivityList;
    private Handler mHandler;

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
                    if (this.mCallerApp == null) {
                        callerAppStr = "null";
                    } else {
                        callerAppStr = this.mCallerApp.toString();
                    }
                    String startMode = "other";
                    if (OppoAppStartupStatistics.this.mAppStartupManager.inAssociateStartWhiteList(calledPkg)) {
                        startMode = "associate";
                    } else if (OppoListManager.getInstance().isInAutoBootWhiteList(calledPkg)) {
                        startMode = "bootstart";
                    } else if (calledPkg.equals(this.mAppInfo.processName)) {
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppStartupStatistics.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppStartupStatistics.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAppStartupStatistics.<clinit>():void");
    }

    private OppoAppStartupStatistics() {
        this.mAppStartupManager = null;
        this.mAms = null;
        this.mHandler = null;
        this.mCollectAppStartupList = new ArrayList();
        this.mCollectPopupActivityList = new ArrayList();
    }

    public static final OppoAppStartupStatistics getInstance() {
        if (mOppoAppStartupStatistics == null) {
            mOppoAppStartupStatistics = new OppoAppStartupStatistics();
        }
        return mOppoAppStartupStatistics;
    }

    protected void init(OppoAppStartupManager appStartupManager, Handler handler, ActivityManagerService ams) {
        this.mAppStartupManager = appStartupManager;
        this.mHandler = handler;
        this.mAms = ams;
    }

    protected void collectAppStartInfo(int callingPid, int callingUid, ProcessRecord callerApp, Intent intent, ApplicationInfo appInfo, String hostType) {
        if (this.mHandler != null && this.mAppStartupManager != null) {
            if (this.mAppStartupManager.DynamicDebug) {
                Log.d(TAG, "collectAppStartInfo: callingPid=" + callingPid + " callingUid=" + callingUid + " callerApp=" + callerApp + " intent=" + intent + " appInfo=" + appInfo + " hostType=" + hostType);
            }
            this.mHandler.post(new CollectAppStartRunnable(callingPid, callingUid, callerApp, intent, appInfo, hostType));
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void collectPopupActivityInfo(String callerPkg, String calledPkg, String calledCpnName, String topPkg, String screenState, String type) {
        if (this.mHandler != null && this.mAppStartupManager != null && this.mAppStartupManager.mSwitchMonitor && callerPkg != null && calledPkg != null && calledCpnName != null && topPkg != null) {
            if (this.mAppStartupManager.DynamicDebug) {
                Log.d(TAG, "collectPopupActivityInfo: callerPkg=" + callerPkg + " calledPkg=" + calledPkg + " calledCpnName=" + calledCpnName + " topPkg=" + topPkg + " isScreeOn=" + screenState + " type=" + type);
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
            if (this.mAppStartupManager.DynamicDebug) {
                Log.d(TAG, "AppStartupList size " + uploadList.size());
                for (Map<String, String> info : uploadList) {
                    Log.d(TAG, "AppStartupList info " + info);
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
            if (this.mAppStartupManager.DynamicDebug) {
                Log.d(TAG, "PopupActivityList size " + uploadList.size());
                for (Map<String, String> info : uploadList) {
                    Log.d(TAG, "PopupActivityList info " + info);
                }
            }
            this.mCollectAppStartupList.clear();
        }
    }
}

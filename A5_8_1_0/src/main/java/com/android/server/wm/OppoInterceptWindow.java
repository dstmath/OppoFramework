package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.coloros.OppoSysStateManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

class OppoInterceptWindow {
    private static final String ENGINE_EXP_VERSION_FEATURE = "persist.sys.oppo.region";
    private static final String INTERCEPT_FEATURE = "open";
    private static final String OPPO_INTERCEPT_CONFIG = "/data/oppo/coloros/config/sys_wms_intercept_window.xml";
    private static final String OPPO_INTERCEPT_PATH = "/data/oppo/coloros/config";
    private static final String REGION = "CN";
    private static final String SKIP_NAME = "skip";
    private static final String TAG = "OppoInterceptWindow";
    private static final Object mLock = new Object();
    private static final Object mSkipLock = new Object();
    private static OppoInterceptWindow sInstance = null;
    private String mAudioPids;
    private FileObserverPolicy mConfigFileObserver;
    private boolean mExpVersion;
    private boolean mInterceptFeature;
    private boolean mOpenChage;
    private ArrayList<RunningAppProcessInfo> mRunProcList;
    private List<String> mSkipApp;
    protected List<String> mSkipNameList;

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(OppoInterceptWindow.OPPO_INTERCEPT_CONFIG)) {
                Slog.i(OppoInterceptWindow.TAG, "onEvent: focusPath = OPPO_INTERCEPT_CONFIG");
                OppoInterceptWindow.this.readConfigFile();
            }
        }
    }

    public static OppoInterceptWindow getInstance() {
        OppoInterceptWindow oppoInterceptWindow;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new OppoInterceptWindow();
            }
            oppoInterceptWindow = sInstance;
        }
        return oppoInterceptWindow;
    }

    private OppoInterceptWindow() {
        this.mConfigFileObserver = null;
        this.mInterceptFeature = true;
        this.mExpVersion = false;
        this.mOpenChage = false;
        this.mRunProcList = new ArrayList();
        this.mAudioPids = null;
        this.mSkipApp = Arrays.asList(new String[]{"com.yuesuoping", "com.tencent.mobileqq", "com.tencent.mm", "com.android.calculator2", "com.toprand.r11s", "com.oppo.im", "com.toprand.voyager", "com.oppo.toprand", "com.hunk.screen.ui"});
        this.mExpVersion = SystemProperties.get(ENGINE_EXP_VERSION_FEATURE, REGION).equalsIgnoreCase(REGION) ^ 1;
        if (this.mExpVersion) {
            this.mInterceptFeature = false;
        } else {
            this.mInterceptFeature = true;
        }
        initDir();
        readConfigFile();
        initFileObserver();
    }

    public boolean interceptWindow(Context context, WindowState win) {
        if (!this.mInterceptFeature || context == null || win == null || win.mAttrs == null) {
            return false;
        }
        if (win.mAttrs.packageName == null || !(win.mAttrs.packageName.contains("android.server.cts") || win.mAttrs.packageName.contains("com.android.compatibility.common.deviceinfo"))) {
            int type = win.mAttrs.type;
            boolean startingWindow = win.mAttrs.type == 3;
            if (win.mSession != null && win.mSession.mUid < 10000 && (startingWindow ^ 1) != 0) {
                return false;
            }
            if (!(this.mSkipNameList == null || win.mAttrs.packageName == null)) {
                for (String pkg : this.mSkipNameList) {
                    if (win.mAttrs.packageName.contains(pkg)) {
                        return false;
                    }
                }
            }
            boolean showWhenLocked = (win.mAttrs.flags & DumpState.DUMP_FROZEN) != 0;
            boolean dismissKeyguard = (win.mAttrs.flags & DumpState.DUMP_CHANGES) != 0;
            if (!showWhenLocked && (dismissKeyguard ^ 1) != 0) {
                return false;
            }
            if (!showWhenLocked && !dismissKeyguard) {
                return false;
            }
            if (isSystemApp(context, win.getOwningPackage())) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                    Slog.v(TAG, "isSystemApp win: " + win);
                }
                return false;
            } else if (!startingWindow && isAudioProcess(context, win)) {
                if (WindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                    Slog.v(TAG, "isAudioProcess win: " + win);
                }
                return false;
            } else if (startingWindow || win.mSession == null || !OppoSysStateManager.getInstance().isSensorWorking(win.mSession.mUid)) {
                LayoutParams layoutParams;
                if (showWhenLocked) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                        Slog.v(TAG, "interceptWindow showWhenLocked ... " + win);
                    }
                    layoutParams = win.mAttrs;
                    layoutParams.flags &= -524289;
                }
                if (dismissKeyguard) {
                    if (WindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                        Slog.v(TAG, "interceptWindow dismissKeyguard ... " + win);
                    }
                    layoutParams = win.mAttrs;
                    layoutParams.flags &= -4194305;
                }
                return true;
            } else {
                if (WindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                    Slog.v(TAG, "isSensorWorking win: " + win);
                }
                return false;
            }
        }
        if (WindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
            Slog.v(TAG, "is App running return! app win: " + win);
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:13:0x0019, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:41:0x0077, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isAudioProcess(Context context, WindowState win) {
        if (win == null || win.mSession == null) {
            return false;
        }
        synchronized (mSkipLock) {
            if (this.mAudioPids == null || this.mAudioPids.length() == 0) {
            } else {
                String[] strPoint = this.mAudioPids.split(":");
                if (strPoint != null) {
                    for (int i = 0; i < strPoint.length; i++) {
                        if (!strPoint[i].isEmpty()) {
                            if (strPoint[i].equals(Integer.toString(win.mSession.mPid))) {
                                return true;
                            } else if (this.mRunProcList != null) {
                                for (int j = 0; j < this.mRunProcList.size(); j++) {
                                    RunningAppProcessInfo appInfo = (RunningAppProcessInfo) this.mRunProcList.get(j);
                                    if (strPoint[i].equals(Integer.toString(appInfo.pid)) && win.mSession.mUid == appInfo.uid) {
                                        return true;
                                    }
                                }
                                continue;
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x001a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isAudioProcess(Context context, Session session, String pkg) {
        String pids = ((AudioManager) context.getSystemService("audio")).getParameters("get_pid");
        if (pids == null || pids.length() == 0 || session == null) {
            return false;
        }
        String[] strPoint = pids.split(":");
        if (strPoint != null) {
            ArrayList<RunningAppProcessInfo> appList = getRunningAppList(context);
            for (int i = 0; i < strPoint.length; i++) {
                if (!strPoint[i].isEmpty()) {
                    if (strPoint[i].equals(Integer.toString(session.mPid))) {
                        return true;
                    }
                    if (appList != null) {
                        for (int j = 0; j < appList.size(); j++) {
                            RunningAppProcessInfo appInfo = (RunningAppProcessInfo) appList.get(j);
                            if (strPoint[i].equals(Integer.toString(appInfo.pid)) && session.mUid == appInfo.uid) {
                                return true;
                            }
                        }
                        continue;
                    } else {
                        continue;
                    }
                }
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:7:0x0012, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSystemApp(Context context, String pkg) {
        if (pkg != null) {
            try {
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(pkg, 0);
                if (!(pkgInfo == null || pkgInfo.applicationInfo == null || (pkgInfo.applicationInfo.flags & 1) == 0)) {
                    return true;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected ArrayList<RunningAppProcessInfo> getRunningAppList(Context context) {
        return (ArrayList) ((ActivityManager) context.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY)).getRunningAppProcesses();
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void getRunningAppList(Context context, Session session, LayoutParams attrs, WindowManagerPolicy policy) {
        if (this.mInterceptFeature && context != null && session != null && attrs != null && session.mUid >= 10000) {
            boolean showWhenLocked = (attrs.flags & DumpState.DUMP_FROZEN) != 0;
            boolean dismissKeyguard = (attrs.flags & DumpState.DUMP_CHANGES) != 0;
            if (showWhenLocked || (dismissKeyguard ^ 1) == 0) {
                ArrayList<RunningAppProcessInfo> procList = (ArrayList) ((ActivityManager) context.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY)).getRunningAppProcesses();
                synchronized (mSkipLock) {
                    this.mRunProcList.clear();
                    this.mRunProcList.addAll(procList);
                    this.mAudioPids = ((AudioManager) context.getSystemService("audio")).getParameters("get_pid");
                }
            }
        }
    }

    private void initDir() {
        File interceptPath = new File(OPPO_INTERCEPT_PATH);
        File interceptConfig = new File(OPPO_INTERCEPT_CONFIG);
        try {
            if (!interceptPath.exists()) {
                interceptPath.mkdirs();
            }
            if (!interceptConfig.exists()) {
                interceptConfig.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init interceptConfig Dir failed!!!");
        }
    }

    private void initFileObserver() {
        this.mConfigFileObserver = new FileObserverPolicy(OPPO_INTERCEPT_CONFIG);
        this.mConfigFileObserver.startWatching();
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x00ef A:{Catch:{ IOException -> 0x0152 }} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0108 A:{Catch:{ IOException -> 0x0152 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        Exception e;
        Throwable th;
        File xmlFile = new File(OPPO_INTERCEPT_CONFIG);
        if (xmlFile.exists()) {
            FileReader fileReader = null;
            this.mOpenChage = false;
            try {
                this.mSkipNameList = new ArrayList();
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader = new FileReader(xmlFile);
                    try {
                        parser.setInput(xmlReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (!parser.getName().equals(SKIP_NAME)) {
                                        if (parser.getName().equals(INTERCEPT_FEATURE)) {
                                            eventType = parser.next();
                                            updateFeature(parser.getAttributeValue(null, "att"));
                                            break;
                                        }
                                    }
                                    eventType = parser.next();
                                    updateListName(parser.getAttributeValue(null, "att"), this.mSkipNameList);
                                    break;
                                    break;
                            }
                        }
                        try {
                            if (!this.mOpenChage) {
                                if (this.mExpVersion) {
                                    this.mInterceptFeature = false;
                                } else {
                                    this.mInterceptFeature = true;
                                }
                            }
                            if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                                this.mSkipNameList = this.mSkipApp;
                            }
                            if (xmlReader != null) {
                                xmlReader.close();
                            }
                        } catch (IOException e2) {
                            Slog.w(TAG, "Got execption close permReader.", e2);
                        }
                    } catch (Exception e3) {
                        e = e3;
                        fileReader = xmlReader;
                        try {
                            Slog.w(TAG, "Got execption parsing permissions.", e);
                            try {
                                if (!this.mOpenChage) {
                                    if (this.mExpVersion) {
                                        this.mInterceptFeature = false;
                                    } else {
                                        this.mInterceptFeature = true;
                                    }
                                }
                                if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                                    this.mSkipNameList = this.mSkipApp;
                                }
                                if (fileReader != null) {
                                    fileReader.close();
                                }
                            } catch (IOException e22) {
                                Slog.w(TAG, "Got execption close permReader.", e22);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                if (!this.mOpenChage) {
                                }
                                this.mSkipNameList = this.mSkipApp;
                                if (fileReader != null) {
                                }
                            } catch (IOException e222) {
                                Slog.w(TAG, "Got execption close permReader.", e222);
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileReader = xmlReader;
                        if (this.mOpenChage) {
                            if (this.mExpVersion) {
                                this.mInterceptFeature = false;
                            } else {
                                this.mInterceptFeature = true;
                            }
                        }
                        if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                            this.mSkipNameList = this.mSkipApp;
                        }
                        if (fileReader != null) {
                            fileReader.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Slog.w(TAG, "Couldn't find or open sys_wms_intercept_window file " + xmlFile);
                    try {
                        if (!this.mOpenChage) {
                            if (this.mExpVersion) {
                                this.mInterceptFeature = false;
                            } else {
                                this.mInterceptFeature = true;
                            }
                        }
                        if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                            this.mSkipNameList = this.mSkipApp;
                        }
                    } catch (IOException e2222) {
                        Slog.w(TAG, "Got execption close permReader.", e2222);
                    }
                }
            } catch (Exception e5) {
                e = e5;
            }
        } else {
            this.mSkipNameList = this.mSkipApp;
        }
    }

    private void updateListName(String tagName, List<String> list) {
        if (tagName != null && tagName != "" && list != null) {
            list.add(tagName);
        }
    }

    private void updateFeature(String feature) {
        if (feature != null) {
            try {
                this.mInterceptFeature = Boolean.parseBoolean(feature);
                this.mOpenChage = true;
            } catch (NumberFormatException e) {
                if (this.mExpVersion) {
                    this.mInterceptFeature = false;
                } else {
                    this.mInterceptFeature = true;
                }
                Slog.e(TAG, "updateFeature NumberFormatException: ", e);
            }
        }
    }
}

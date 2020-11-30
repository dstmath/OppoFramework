package com.android.server.am;

import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import com.oppo.app.IOppoAppStartController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ColorAutostartManager {
    private static final String ACTION_WHITE_FILE_PATH = "/data/oppo/coloros/startup/broadcast_action_white.txt";
    private static final String BOOT_OPTION_PATH = "/startup/bootoption.txt";
    private static final String KEY_PERMISSION_PROPERTIES = "persist.sys.permission.enable";
    private static final String RECORD_AUTO_LAUNCH_MODE = "0";
    private static final String RECORD_CALLER_ANDROID = "Android";
    private static final String RECORD_PREVENT_LAUNCH_TYPE = "1";
    private static final String TAG = "ColorAutostartManager";
    private static final Object mActionWhiteLock = new Object();
    private static final Object mCompareListLock = new Object();
    private static volatile ColorAutostartManager sColorAutostartManager = null;
    private static boolean sDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private ActivityManagerService mAms = null;
    private SparseArray<List<String>> mBootOptionListContainer = new SparseArray<>();
    private BootFileListener mBroadActionFileObserver;
    ColorSettingsChangeListener mColorConfigChangeListener = new ColorSettingsChangeListener(new Handler()) {
        /* class com.android.server.am.ColorAutostartManager.AnonymousClass1 */

        public void onSettingsChange(boolean selfChange, String path, final int userId) {
            if (ColorAutostartManager.sDebug) {
                Log.v(ColorAutostartManager.TAG, "on config change and maybe read config, path=" + path + ", userId=" + userId);
            }
            if (path != null && path.contains(ColorAutostartManager.BOOT_OPTION_PATH)) {
                ColorAutostartManager.this.mHandler.post(new Runnable() {
                    /* class com.android.server.am.ColorAutostartManager.AnonymousClass1.AnonymousClass1 */

                    public void run() {
                        ColorAutostartManager.this.readBootOptionList(userId);
                    }
                });
            }
        }
    };
    private IOppoAppStartController mController = null;
    private Handler mHandler = null;
    private List<String> mWidgetActionList = new ArrayList();

    public static final ColorAutostartManager getInstance() {
        if (sColorAutostartManager == null) {
            synchronized (ColorAutostartManager.class) {
                if (sColorAutostartManager == null) {
                    sColorAutostartManager = new ColorAutostartManager();
                }
            }
        }
        return sColorAutostartManager;
    }

    public class BootFileListener extends FileObserver {
        private String mFocusPath;

        public BootFileListener(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(ColorAutostartManager.ACTION_WHITE_FILE_PATH)) {
                ColorAutostartManager.this.updateActionWhiteList();
            }
        }
    }

    public void initBootList(ActivityManagerService ams, boolean clear) {
        if (clear && this.mBootOptionListContainer.size() != 0) {
            this.mBootOptionListContainer.clear();
        }
        this.mAms = ams;
        initData();
        registerConfigChangeListener();
        readBootOptionList(0);
        updateActionWhiteList();
    }

    private void initData() {
        File actionWhiteFile = new File(ACTION_WHITE_FILE_PATH);
        try {
            if (!actionWhiteFile.exists()) {
                actionWhiteFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mBroadActionFileObserver = new BootFileListener(ACTION_WHITE_FILE_PATH);
        this.mBroadActionFileObserver.startWatching();
        HandlerThread thread = new HandlerThread("AutoStartupManager");
        thread.start();
        this.mHandler = new Handler(thread.getLooper());
    }

    private void registerConfigChangeListener() {
        if (this.mAms.mContext == null) {
            Log.e(TAG, "registerConfigChangeListener failed!");
        } else {
            ColorSettings.registerChangeListenerForAll(this.mAms.mContext, BOOT_OPTION_PATH, 0, this.mColorConfigChangeListener);
        }
    }

    public final boolean checkAutoBootForbiddenStart(BroadcastRecord r, ResolveInfo info) {
        if (!SystemProperties.getBoolean(KEY_PERMISSION_PROPERTIES, true)) {
            if (sDebug) {
                Slog.d(TAG, "A " + info.activityInfo.applicationInfo.packageName + " R:permission disable!");
            }
            return false;
        } else if (info.activityInfo == null || info.activityInfo.applicationInfo == null || !ColorAppStartupManager.getInstance().checkIsBroadcastexcludePkg(r.intent, info.activityInfo.applicationInfo.packageName)) {
            int userId = 0;
            if (!(info.activityInfo == null || info.activityInfo.applicationInfo == null)) {
                userId = UserHandle.getUserId(info.activityInfo.applicationInfo.uid);
                if ((info.activityInfo.applicationInfo.flags & 1) != 0 || info.activityInfo.applicationInfo.uid <= 10000 || ColorAppStartupManager.getInstance().isRecentLockedApps(info.activityInfo.applicationInfo.packageName, userId)) {
                    return false;
                }
            }
            if (!processCanStart(info.activityInfo, userId)) {
                if (r.callingUid >= 10000) {
                    if (r.callerApp == null) {
                        Slog.w(TAG, "r.callerApp == null!!!!");
                        return false;
                    } else if (r.callerApp.info != null && (r.callerApp.info.flags & 1) == 0) {
                        return false;
                    }
                }
                if (OppoListManager.getInstance().isInstalledAppWidget(info.activityInfo.applicationInfo.packageName, info.activityInfo.applicationInfo.uid)) {
                    if (sDebug) {
                        Slog.d(TAG, "A " + info.activityInfo.applicationInfo.packageName + " R:widget");
                    }
                    return false;
                } else if (OppoListManager.getInstance().isInBootSmartWhiteList(info.activityInfo.applicationInfo.packageName)) {
                    if (sDebug) {
                        Slog.d(TAG, "A " + info.activityInfo.applicationInfo.packageName + " in boot smart white list");
                    }
                    return false;
                } else {
                    if (r.intent != null) {
                        String action = r.intent.getAction();
                        synchronized (mActionWhiteLock) {
                            if (action != null) {
                                if (this.mWidgetActionList.contains(action)) {
                                    Slog.d(TAG, action + " in WidgetActionList");
                                    return false;
                                }
                            }
                        }
                    }
                    if (sDebug) {
                        Slog.d(TAG, "*Do not want to launch app " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + " callUid:" + r.callingUid + " callPid:" + r.callingPid);
                    }
                    updateLaunchRecord(RECORD_CALLER_ANDROID, info.activityInfo.applicationInfo.packageName, RECORD_AUTO_LAUNCH_MODE, RECORD_PREVENT_LAUNCH_TYPE, "broadcast");
                    return true;
                }
            } else {
                if (sDebug) {
                    Slog.d(TAG, "A " + info.activityInfo.applicationInfo.packageName + " R:white");
                }
                return false;
            }
        } else {
            if (sDebug) {
                Slog.d(TAG, "*Do not want to launch app in black list " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + " callUid:" + r.callingUid + " callPid:" + r.callingPid);
            }
            return true;
        }
    }

    private boolean processCanStart(ActivityInfo info, int userId) {
        boolean res = true;
        if (info == null) {
            return false;
        }
        String pkgName = info.packageName;
        synchronized (mCompareListLock) {
            List<String> bootOptionList = getBootOptionList(userId);
            if (bootOptionList != null && bootOptionList.contains(pkgName)) {
                res = false;
            }
        }
        return res;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readBootOptionList(int userId) {
        if (sDebug) {
            Slog.d(TAG, "readAutoBootListFile start, userId=" + userId);
        }
        List<String> bootOptionList = new ArrayList<>();
        InputStream is = null;
        InputStreamReader isReader = null;
        BufferedReader reader = null;
        try {
            InputStream is2 = ColorSettings.readConfigAsUser(this.mAms.mContext, BOOT_OPTION_PATH, userId, 0);
            InputStreamReader isReader2 = new InputStreamReader(is2);
            BufferedReader reader2 = new BufferedReader(isReader2);
            while (true) {
                String strT = reader2.readLine();
                if (strT != null) {
                    bootOptionList.add(strT.trim());
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            reader2.close();
            isReader2.close();
            if (is2 != null) {
                is2.close();
            }
        } catch (Exception e2) {
            bootOptionList.clear();
            Slog.v(TAG, "error:" + e2);
            if (0 != 0) {
                reader.close();
            }
            if (0 != 0) {
                isReader.close();
            }
            if (0 != 0) {
                is.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                    throw th;
                }
            }
            if (0 != 0) {
                isReader.close();
            }
            if (0 != 0) {
                is.close();
            }
            throw th;
        }
        if (!bootOptionList.isEmpty()) {
            synchronized (mCompareListLock) {
                List<String> list = getBootOptionListNotNull(userId);
                if (!list.isEmpty()) {
                    list.clear();
                }
                list.addAll(bootOptionList);
            }
        }
    }

    private List<String> getBootOptionList(int userId) {
        List<String> list;
        if (userId == 999) {
            userId = 0;
        }
        synchronized (mCompareListLock) {
            list = this.mBootOptionListContainer.get(userId);
        }
        return list;
    }

    private List<String> getBootOptionListNotNull(int userId) {
        List<String> list = this.mBootOptionListContainer.get(userId);
        if (list != null) {
            return list;
        }
        List<String> list2 = new ArrayList<>();
        this.mBootOptionListContainer.put(userId, list2);
        return list2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateActionWhiteList() {
        File file = new File(ACTION_WHITE_FILE_PATH);
        if (!file.exists()) {
            Slog.e(TAG, "updateActionWhiteList failed: file doesn't exist!");
            return;
        }
        List<String> actionList = new ArrayList<>();
        FileReader fr = null;
        BufferedReader reader = null;
        try {
            FileReader fr2 = new FileReader(file);
            BufferedReader reader2 = new BufferedReader(fr2);
            while (true) {
                String strT = reader2.readLine();
                if (strT != null) {
                    actionList.add(strT.trim());
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            reader2.close();
            try {
                fr2.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } catch (Exception e3) {
            Log.e(TAG, "associateStartFile read execption: " + e3);
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            if (0 != 0) {
                fr.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            if (0 != 0) {
                try {
                    fr.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            throw th;
        }
        if (!actionList.isEmpty()) {
            synchronized (mActionWhiteLock) {
                this.mWidgetActionList.clear();
                this.mWidgetActionList.addAll(actionList);
                Slog.v(TAG, "update broadcast action " + this.mWidgetActionList);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateLaunchRecord(String callerPkg, String calledPkg, String launchMode, String launchType, String reason) {
        this.mHandler.post(new UpdateLaunchRecord(callerPkg, calledPkg, launchMode, launchType, reason));
    }

    /* access modifiers changed from: private */
    public class UpdateLaunchRecord implements Runnable {
        private String mCalledPkgName;
        private String mCallerPkgName;
        private String mLaunchMode;
        private String mLaunchType;
        private String mReason;

        public UpdateLaunchRecord(String callerPkg, String calledPkg, String launchMode, String launchType, String reason) {
            this.mCallerPkgName = callerPkg;
            this.mCalledPkgName = calledPkg;
            this.mLaunchMode = launchMode;
            this.mLaunchType = launchType;
            this.mReason = reason;
        }

        public void run() {
            String str;
            String str2;
            String str3;
            String str4;
            String str5 = this.mCallerPkgName;
            if (str5 != null && (str = this.mCalledPkgName) != null && (str2 = this.mLaunchMode) != null && (str3 = this.mLaunchType) != null && (str4 = this.mReason) != null) {
                ColorAutostartManager.this.notifyPreventStartAppInfo(str5, str, str2, str3, str4);
            }
        }
    }

    public void setPreventStartController(IOppoAppStartController controller) {
        this.mController = controller;
    }

    public void notifyPreventStartAppInfo(String callerPkg, String calledPkg, String launchMode, String launchType, String reason) {
        IOppoAppStartController iOppoAppStartController = this.mController;
        if (iOppoAppStartController != null) {
            try {
                iOppoAppStartController.preventStartMonitor(callerPkg, calledPkg, launchMode, launchType, reason);
            } catch (Exception e) {
                this.mController = null;
                Slog.e(TAG, "notifyPreventStartAppInfo failed!!");
            }
        }
    }
}

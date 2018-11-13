package com.android.server.am;

import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.coloros.OppoListManager;
import com.oppo.app.IOppoAppStartController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OppoAutostartManager {
    private static final String ACTION_WHITE_FILE_PATH = "/data/oppo/coloros/startup/broadcast_action_white.txt";
    private static final String DIR = "//data//oppo//coloros//startup//";
    private static final String FILE_BOOT_OPTION = "//data//oppo//coloros//startup//bootoption.txt";
    private static final String KEY_PERMISSION_PROPERTIES = "persist.sys.permission.enable";
    private static final String PATH = "/data/oppo/coloros/startup/bootoption.txt";
    private static final String RECORD_AUTO_LAUNCH_MODE = "0";
    private static final String RECORD_CALLER_ANDROID = "Android";
    private static final String RECORD_PREVENT_LAUNCH_TYPE = "1";
    private static final String SAVE_FILE_NAME = "bootoption.txt";
    private static final String TAG = "OppoAutostartManager";
    private static final Object mActionWhiteLock = new Object();
    private static final Object mCompareListLock = new Object();
    private static boolean sDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static volatile OppoAutostartManager sOppoAutostartManager = null;
    private ActivityManagerService mAms = null;
    private BootFileListener mBroadActionFileObserver;
    private List<String> mCompareList = new ArrayList();
    private IOppoAppStartController mController = null;
    private File mFile;
    private Handler mHandler = null;
    private BootFileListener mListener;
    private List<String> mWidgetActionList = new ArrayList();

    public class BootFileListener extends FileObserver {
        private String mFocusPath;

        public BootFileListener(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.mFocusPath.equals(OppoAutostartManager.PATH)) {
                OppoAutostartManager.this.getBootList(OppoAutostartManager.this.mFile);
            } else if (this.mFocusPath.equals(OppoAutostartManager.ACTION_WHITE_FILE_PATH)) {
                OppoAutostartManager.this.updateActionWhiteList();
            }
        }
    }

    private class UpdateLaunchRecord implements Runnable {
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
            if (this.mCallerPkgName != null && this.mCalledPkgName != null && this.mLaunchMode != null && this.mLaunchType != null && this.mReason != null) {
                OppoAutostartManager.this.notifyPreventStartAppInfo(this.mCallerPkgName, this.mCalledPkgName, this.mLaunchMode, this.mLaunchType, this.mReason);
            }
        }
    }

    public static final OppoAutostartManager getInstance() {
        if (sOppoAutostartManager == null) {
            synchronized (OppoAutostartManager.class) {
                if (sOppoAutostartManager == null) {
                    sOppoAutostartManager = new OppoAutostartManager();
                }
            }
        }
        return sOppoAutostartManager;
    }

    public void initBootList(ActivityManagerService ams, boolean clear) {
        if (!(!clear || this.mCompareList == null || this.mCompareList.size() == 0)) {
            this.mCompareList.clear();
        }
        this.mAms = ams;
        initData();
        getBootList(this.mFile);
        updateActionWhiteList();
    }

    private void initData() {
        this.mFile = new File(FILE_BOOT_OPTION);
        if (!this.mFile.exists()) {
            Slog.v(TAG, "mFile.exists() is not exit!");
            new File(DIR).mkdirs();
            this.mFile = new File(FILE_BOOT_OPTION);
            try {
                this.mFile.createNewFile();
            } catch (IOException ioe) {
                Slog.e(TAG, "File creation failed " + ioe.getMessage());
            }
        }
        this.mListener = new BootFileListener(PATH);
        this.mListener.startWatching();
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

    public final boolean checkAutoBootForbiddenStart(BroadcastQueue queue, ResolveInfo info, BroadcastRecord r) {
        if (!SystemProperties.getBoolean("persist.sys.permission.enable", true) || processCanStart(info.activityInfo)) {
            return false;
        }
        if (r.callingUid >= 10000) {
            if (r.callerApp == null) {
                Slog.w(TAG, "r.callerApp == null!!!!");
                return false;
            } else if (r.callerApp.info != null && (r.callerApp.info.flags & 1) == 0) {
                return false;
            }
        }
        if (OppoListManager.getInstance().isInstalledAppWidget(info.activityInfo.applicationInfo.packageName)) {
            if (sDebug) {
                Slog.w(TAG, "A " + info.activityInfo.applicationInfo.packageName + " R:widget");
            }
            return false;
        }
        if (r.intent != null) {
            String action = r.intent.getAction();
            synchronized (mActionWhiteLock) {
                if (action != null) {
                    if (this.mWidgetActionList.contains(action)) {
                        Slog.w(TAG, action + " in WidgetActionList");
                        return false;
                    }
                }
            }
        }
        if (sDebug) {
            Slog.w(TAG, "*Do not want to launch app " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + " callUid:" + r.callingUid + " callPid:" + r.callingPid);
        }
        queue.finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, true);
        queue.scheduleBroadcastsLocked();
        updateLaunchRecord(RECORD_CALLER_ANDROID, info.activityInfo.applicationInfo.packageName, RECORD_AUTO_LAUNCH_MODE, "1", "broadcast");
        return true;
    }

    private boolean processCanStart(ActivityInfo info) {
        boolean res = true;
        if (info == null) {
            return false;
        }
        String pkgName = info.packageName;
        synchronized (mCompareListLock) {
            if (this.mCompareList.contains(pkgName)) {
                res = false;
            }
        }
        return res;
    }

    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0084 A:{SYNTHETIC, Splitter: B:46:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0089 A:{SYNTHETIC, Splitter: B:49:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0045 A:{SYNTHETIC, Splitter: B:18:0x0045} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a A:{SYNTHETIC, Splitter: B:21:0x004a} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0045 A:{SYNTHETIC, Splitter: B:18:0x0045} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a A:{SYNTHETIC, Splitter: B:21:0x004a} */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0084 A:{SYNTHETIC, Splitter: B:46:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0089 A:{SYNTHETIC, Splitter: B:49:0x0089} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getBootList(File mFile) {
        Exception e;
        Throwable th;
        List<String> bootOptionList = new ArrayList();
        if (mFile.exists()) {
            FileReader fr = null;
            BufferedReader reader = null;
            try {
                FileReader fr2 = new FileReader(mFile);
                try {
                    BufferedReader reader2 = new BufferedReader(fr2);
                    while (true) {
                        try {
                            String strT = reader2.readLine();
                            if (strT == null) {
                                break;
                            }
                            bootOptionList.add(strT.trim());
                        } catch (Exception e2) {
                            e = e2;
                            reader = reader2;
                            fr = fr2;
                            try {
                                Slog.v(TAG, "error:" + e);
                                if (reader != null) {
                                }
                                if (fr != null) {
                                }
                                if (bootOptionList.isEmpty()) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (reader != null) {
                                    try {
                                        reader.close();
                                    } catch (IOException e3) {
                                        e3.printStackTrace();
                                    }
                                }
                                if (fr != null) {
                                    try {
                                        fr.close();
                                    } catch (IOException e32) {
                                        e32.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            reader = reader2;
                            fr = fr2;
                            if (reader != null) {
                            }
                            if (fr != null) {
                            }
                            throw th;
                        }
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    if (fr2 != null) {
                        try {
                            fr2.close();
                        } catch (IOException e3222) {
                            e3222.printStackTrace();
                        }
                    }
                } catch (Exception e4) {
                    e = e4;
                    fr = fr2;
                    Slog.v(TAG, "error:" + e);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e32222) {
                            e32222.printStackTrace();
                        }
                    }
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (IOException e322222) {
                            e322222.printStackTrace();
                        }
                    }
                    if (bootOptionList.isEmpty()) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fr = fr2;
                    if (reader != null) {
                    }
                    if (fr != null) {
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                Slog.v(TAG, "error:" + e);
                if (reader != null) {
                }
                if (fr != null) {
                }
                if (bootOptionList.isEmpty()) {
                }
            }
        } else {
            Slog.v(TAG, "bootoption  mFile not exists!");
        }
        if (bootOptionList.isEmpty()) {
            synchronized (mCompareListLock) {
                this.mCompareList.clear();
                this.mCompareList.addAll(bootOptionList);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00b4 A:{SYNTHETIC, Splitter: B:50:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b9 A:{SYNTHETIC, Splitter: B:53:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0057 A:{SYNTHETIC, Splitter: B:20:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005c A:{SYNTHETIC, Splitter: B:23:0x005c} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0057 A:{SYNTHETIC, Splitter: B:20:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005c A:{SYNTHETIC, Splitter: B:23:0x005c} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00b4 A:{SYNTHETIC, Splitter: B:50:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b9 A:{SYNTHETIC, Splitter: B:53:0x00b9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateActionWhiteList() {
        Exception e;
        Throwable th;
        File mFile = new File(ACTION_WHITE_FILE_PATH);
        if (mFile.exists()) {
            List<String> actionList = new ArrayList();
            FileReader fr = null;
            BufferedReader reader = null;
            try {
                FileReader fr2 = new FileReader(mFile);
                try {
                    BufferedReader reader2 = new BufferedReader(fr2);
                    while (true) {
                        try {
                            String strT = reader2.readLine();
                            if (strT == null) {
                                break;
                            }
                            actionList.add(strT.trim());
                        } catch (Exception e2) {
                            e = e2;
                            reader = reader2;
                            fr = fr2;
                            try {
                                Log.e(TAG, "associateStartFile read execption: " + e);
                                if (reader != null) {
                                    try {
                                        reader.close();
                                    } catch (IOException e3) {
                                        e3.printStackTrace();
                                    }
                                }
                                if (fr != null) {
                                    try {
                                        fr.close();
                                    } catch (IOException e32) {
                                        e32.printStackTrace();
                                    }
                                }
                                if (!actionList.isEmpty()) {
                                }
                                return;
                            } catch (Throwable th2) {
                                th = th2;
                                if (reader != null) {
                                }
                                if (fr != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            reader = reader2;
                            fr = fr2;
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e322) {
                                    e322.printStackTrace();
                                }
                            }
                            if (fr != null) {
                                try {
                                    fr.close();
                                } catch (IOException e3222) {
                                    e3222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e32222) {
                            e32222.printStackTrace();
                        }
                    }
                    if (fr2 != null) {
                        try {
                            fr2.close();
                        } catch (IOException e322222) {
                            e322222.printStackTrace();
                        }
                    }
                    reader = reader2;
                } catch (Exception e4) {
                    e = e4;
                    fr = fr2;
                    Log.e(TAG, "associateStartFile read execption: " + e);
                    if (reader != null) {
                    }
                    if (fr != null) {
                    }
                    if (actionList.isEmpty()) {
                    }
                    return;
                } catch (Throwable th4) {
                    th = th4;
                    fr = fr2;
                    if (reader != null) {
                    }
                    if (fr != null) {
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                Log.e(TAG, "associateStartFile read execption: " + e);
                if (reader != null) {
                }
                if (fr != null) {
                }
                if (actionList.isEmpty()) {
                }
                return;
            }
            if (actionList.isEmpty()) {
                synchronized (mActionWhiteLock) {
                    this.mWidgetActionList.clear();
                    this.mWidgetActionList.addAll(actionList);
                    Slog.v(TAG, "update broadcast action " + this.mWidgetActionList);
                }
            }
            return;
        }
        Slog.e(TAG, "updateActionWhiteList failed: mFile doesn't exist!");
    }

    protected void updateLaunchRecord(String callerPkg, String calledPkg, String launchMode, String launchType, String reason) {
        this.mHandler.post(new UpdateLaunchRecord(callerPkg, calledPkg, launchMode, launchType, reason));
    }

    public void setPreventStartController(IOppoAppStartController controller) {
        this.mController = controller;
    }

    public void notifyPreventStartAppInfo(String callerPkg, String calledPkg, String launchMode, String launchType, String reason) {
        if (this.mController != null) {
            try {
                this.mController.preventStartMonitor(callerPkg, calledPkg, launchMode, launchType, reason);
            } catch (Exception e) {
                this.mController = null;
                Slog.e(TAG, "notifyPreventStartAppInfo failed!!");
            }
        }
    }
}

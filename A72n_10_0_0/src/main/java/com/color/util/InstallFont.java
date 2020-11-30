package com.color.util;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.ColorUxIconConstants;
import android.app.IActivityManager;
import android.app.OppoActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorBaseConfiguration;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InstallFont {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "com.color.util.InstallFont";
    private static ArrayList<String> inputMethodList = new ArrayList<>();
    private static ArrayList<String> mAppWhiteList = new ArrayList<>();

    static {
        mAppWhiteList.add("com.mediatek.mtklogger");
        mAppWhiteList.add("com.oppo.recents");
        mAppWhiteList.add("com.coloros.recents");
        mAppWhiteList.add("com.oppo.alarmclock");
        mAppWhiteList.add("com.coloros.alarmclock");
        mAppWhiteList.add("com.android.captiveportallogin");
        mAppWhiteList.add("com.android.systemui");
        mAppWhiteList.add("com.android.keyguard");
        mAppWhiteList.add("com.android.settings");
        mAppWhiteList.add("com.coloros.bootreg");
        mAppWhiteList.add("com.oppo.launcher");
        mAppWhiteList.add("com.oppo.weather");
        mAppWhiteList.add("com.coloros.weather");
        mAppWhiteList.add("com.oppo.music");
        mAppWhiteList.add("com.coloros.gallery3d");
        mAppWhiteList.add("com.nearme.themespace");
        mAppWhiteList.add("com.color.safecenter");
        mAppWhiteList.add("com.coloros.safecenter");
        mAppWhiteList.add("com.coloros.filemanager");
        mAppWhiteList.add("com.nearme.gamecenter");
        mAppWhiteList.add(ColorUxIconConstants.IconLoader.COM_ANDROID_CONTACTS);
        mAppWhiteList.add("oppo.multimedia.soundrecorder");
        mAppWhiteList.add("com.coloros.soundrecorder");
        mAppWhiteList.add("com.android.providers.downloads");
        mAppWhiteList.add("com.oppo.backuprestore");
        mAppWhiteList.add("com.coloros.backuprestore");
        mAppWhiteList.add("com.oppo.reader");
        mAppWhiteList.add("com.android.mms");
        mAppWhiteList.add("com.oppo.usercenter");
        mAppWhiteList.add("com.oppo.community");
        mAppWhiteList.add("com.nearme.note");
        mAppWhiteList.add("com.android.email");
        mAppWhiteList.add("com.android.packageinstaller");
        mAppWhiteList.add("com.android.phone");
        mAppWhiteList.add("org.codeaurora.bluetooth");
        mAppWhiteList.add("com.android.bluetooth");
        mAppWhiteList.add("com.android.nfc");
        mAppWhiteList.add("com.android.incallui");
        mAppWhiteList.add("com.google.android.dialer");
        mAppWhiteList.add("com.tencent.mm");
        mAppWhiteList.add("com.tencent.mobileqq");
        mAppWhiteList.add("com.coloros.screenrecorder");
        mAppWhiteList.add("com.nearme.themestore");
        mAppWhiteList.add("com.heytap.themestore");
        mAppWhiteList.add("com.google.android.marvin.talkback");
    }

    public InstallFont() {
        inputMethodList = findInputMethods();
        logd("init InstallFont , inputMethodList size = " + inputMethodList.size());
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void killRecentPackage() {
        IActivityManager activityManager = ActivityManager.getService();
        int userIdProcess = Process.myUserHandle().hashCode();
        List<ActivityManager.RecentTaskInfo> recentlist = new ArrayList<>();
        try {
            recentlist = activityManager.getRecentTasks(100, 1, userIdProcess).getList();
        } catch (Exception e) {
            loge("killRecentPackage", e);
        }
        for (int i = 0; i < recentlist.size(); i++) {
            ActivityManager.RecentTaskInfo info = recentlist.get(i);
            String pakg = info.baseIntent.getComponent().getPackageName();
            int userId = info.userId;
            if (!mAppWhiteList.contains(pakg) && !inputMethodList.contains(pakg) && !pakg.contains("com.oppo.autotest") && !pakg.contains("com.oppo.qe")) {
                logd(" killRecentPackage_forceStopPackage = " + pakg + " , userId = " + userId);
                try {
                    activityManager.forceStopPackage(pakg, userId);
                } catch (Exception e2) {
                    loge("Failed  killRecentPackage_forceStopPackage = " + pakg + " , userId = " + userId, e2);
                }
            }
        }
    }

    public void killAppProcess() {
        ArrayList<ActivityManager.RunningAppProcessInfo> RunningApplist = new ArrayList<>();
        try {
            RunningApplist = (ArrayList) ActivityManager.getService().getRunningAppProcesses();
        } catch (Exception e) {
            loge("killAppProcess", e);
        }
        for (int i = 0; i < RunningApplist.size(); i++) {
            ActivityManager.RunningAppProcessInfo info = RunningApplist.get(i);
            String processName = info.processName;
            int pid = info.pid;
            if (processName == null || (!processName.contains("com.oppo.autotest") && !processName.contains("com.oppo.qe"))) {
                if (processName != null && (processName.contains("com.tencent.mm") || processName.contains("com.tencent.mobileqq"))) {
                    try {
                        killPidForce(pid);
                        logd(" killPidForce processName = " + processName);
                    } catch (Exception e2) {
                        logd(" Failed killPidForce processName = " + processName + " , pid = " + pid);
                    }
                }
                int j = 0;
                while (true) {
                    if (j >= info.pkgList.length) {
                        break;
                    }
                    String pkg = info.pkgList[j];
                    if (inputMethodList.contains(pkg)) {
                        logd(" killAppProcess_killPidForce = " + pkg);
                        try {
                            killPidForce(pid);
                            break;
                        } catch (Exception e3) {
                            loge("Failed  killAppProcess_killPidForce = " + pkg + " , pid = " + pid, e3);
                        }
                    } else {
                        j++;
                    }
                }
            }
        }
    }

    private static void killPidForce(int pid) {
        try {
            new OppoActivityManager().killPidForce(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> findInputMethods() {
        Context context = ActivityThread.currentActivityThread().getSystemUiContext();
        ArrayList<String> names = new ArrayList<>();
        List<ResolveInfo> list = context.getPackageManager().queryIntentServices(new Intent("android.view.InputMethod"), 128);
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            names.add(list.get(i).getComponentInfo().applicationInfo.packageName);
        }
        return names;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0037, code lost:
        if (android.os.Build.VERSION.SDK_INT > 25) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0039, code lost:
        r0.updateConfiguration(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003d, code lost:
        updateConfigurationReflect(r0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0076, code lost:
        if (android.os.Build.VERSION.SDK_INT <= 25) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        return;
     */
    public static void setConfig() {
        IActivityManager am;
        if (Build.VERSION.SDK_INT > 25) {
            am = ActivityManager.getService();
        } else {
            am = ActivityManagerNative.getDefault();
        }
        try {
            Configuration config = am.getConfiguration();
            ColorBaseConfiguration baseConfiguration = (ColorBaseConfiguration) ColorTypeCastingHelper.typeCasting(ColorBaseConfiguration.class, config);
            Random randomizer = new Random(System.currentTimeMillis());
            try {
                baseConfiguration.mOppoExtraConfiguration.mFlipFont = randomizer.nextInt((10000 - 0) + 1) + 0;
            } catch (Error e) {
                try {
                    Field[] fields = config.getClass().getDeclaredFields();
                    int len = fields.length;
                    for (int i = 0; i < len; i++) {
                        if ("FlipFont".equalsIgnoreCase(fields[i].getName())) {
                            fields[i].setInt(config, randomizer.nextInt((10000 - 0) + 1) + 0);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } catch (Throwable th) {
                    if (Build.VERSION.SDK_INT > 25) {
                        am.updateConfiguration(config);
                    } else {
                        updateConfigurationReflect(am, config);
                    }
                    throw th;
                }
            }
        } catch (RemoteException e2) {
            loge("RemoteException e=", e2);
        }
    }

    private static void updateConfigurationReflect(IActivityManager am, Configuration config) {
        Method updateConfiguration;
        if (am == null || config == null) {
            logd("updateConfigurationReflect, update fails, IActivityManager or Configuration is null.");
            return;
        }
        try {
            Class<?> clazz = am.getClass();
            if (clazz != null && (updateConfiguration = clazz.getMethod("updateConfiguration", Configuration.class)) != null) {
                updateConfiguration.invoke(am, config);
            }
        } catch (Exception e) {
            logd("updateConfigurationReflect, e=" + e);
        }
    }

    public static IPackageManager getPackageManager() {
        return IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    }

    private static void logd(String content) {
        if (DEBUG) {
            Log.d(TAG, content);
        }
    }

    private static void loge(String content, Throwable e) {
        if (DEBUG) {
            Log.e(TAG, content + ":" + e.getMessage(), e);
        }
    }
}

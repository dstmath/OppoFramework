package com.mediatek.server.am;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.SystemProperties;
import com.android.server.am.ProcessRecord;
import com.android.server.wm.ActivityRecord;
import java.io.PrintWriter;
import java.util.ArrayList;

public class AmsExt {
    public static final int COLLECT_PSS_FG_MSG = 2;
    public static int DEFAULT_MAX_CACHED_PROCESSES_EX = ("0x20000000".equals(SystemProperties.get("ro.vendor.mtk_config_max_dram_size")) ? 6 : 32);

    public void onAddErrorToDropBox(String dropboxTag, String info, int pid) {
    }

    public void enableMtkAmsLog() {
    }

    public void onSystemReady(Context context) {
    }

    public void onBeforeActivitySwitch(ActivityRecord lastResumedActivity, ActivityRecord nextResumedActivity, boolean pausing, int nextResumedActivityType) {
    }

    public void onAfterActivityResumed(ActivityRecord resumedActivity) {
    }

    public void onTopResumedActivityChanged(ActivityRecord resumedActivity) {
    }

    public void onUpdateSleep(boolean wasSleeping, boolean isSleepingAfterUpdate) {
    }

    public void setAalMode(int mode) {
    }

    public void setAalEnabled(boolean enabled) {
    }

    public int amsAalDump(PrintWriter pw, String[] args, int opti) {
        return opti;
    }

    public void onStartProcess(String hostingType, String packageName) {
    }

    public void onNotifyAppCrash(int pid, int uid, String packageName) {
    }

    public void onEndOfActivityIdle(Context context, Intent idleIntent) {
    }

    public void onWakefulnessChanged(int wakefulness) {
    }

    public void addDuraSpeedService() {
    }

    public void startDuraSpeedService(Context context) {
    }

    public String onReadyToStartComponent(String packageName, int uid, String suppressReason, String className) {
        return null;
    }

    public boolean onBeforeStartProcessForStaticReceiver(String packageName) {
        return false;
    }

    public void addToSuppressRestartList(String packageName) {
    }

    public boolean notRemoveAlarm(String packageName) {
        return false;
    }

    public void enableAmsLog(ArrayList<ProcessRecord> arrayList) {
    }

    public void enableAmsLog(PrintWriter pw, String[] args, int opti, ArrayList<ProcessRecord> arrayList) {
    }

    public boolean preLaunchApplication(String callingPackage, Intent intent, String resolvedType, int startFlags) {
        return false;
    }

    public boolean IsBuildInApp() {
        return true;
    }

    public boolean checkAutoBootPermission(Context context, String packageName, int userId, ArrayList<ProcessRecord> arrayList, int callingPid) {
        return true;
    }

    public void forceStopRelatedApps(Context context, ProcessRecord app, ApplicationInfo appInfo, int userId, ArrayList<ProcessRecord> arrayList) {
    }

    public boolean isComponentNeedsStart(String packageName, String suppressReason) {
        return true;
    }
}

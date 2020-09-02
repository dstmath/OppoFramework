package com.mediatek.server.anr;

import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ProcessRecord;

public class AnrManager {
    public static final int EVENT_BOOT_COMPLETED = 9001;

    public void AddAnrManagerService() {
    }

    public void startAnrManagerService(int pid) {
    }

    public boolean isAnrDeferrable() {
        return false;
    }

    public boolean delayMessage(Handler mHandler, Message msg, int msgId, int time) {
        return false;
    }

    public void writeEvent(int event) {
    }

    public void sendBroadcastMonitorMessage(long timeoutTime, long timeoutPeriod) {
    }

    public void removeBroadcastMonitorMessage() {
    }

    public void sendServiceMonitorMessage() {
    }

    public void removeServiceMonitorMessage() {
    }

    public boolean startAnrDump(ActivityManagerService service, ProcessRecord app, String activityShortComponentName, ApplicationInfo aInfo, String parentShortComponentName, ProcessRecord parentProcess, boolean aboveSystem, String annotation, boolean showBackground, long anrTime) {
        return false;
    }
}

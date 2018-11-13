package com.oppo.app;

import android.app.IActivityController.Stub;
import android.content.Intent;
import android.os.RemoteException;

public class OppoAppProtectController extends Stub {
    public boolean activityStarting(Intent intent, String pkg) {
        return true;
    }

    public boolean activityResuming(String pkg) {
        return true;
    }

    public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) {
        return true;
    }

    public int appNotResponding(String processName, int pid, String processStats) {
        return 0;
    }

    public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
        return 0;
    }

    public int systemNotResponding(String msg) {
        return -1;
    }
}

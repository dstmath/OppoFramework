package com.oppo.app;

import android.app.IActivityController;
import android.content.Intent;
import android.os.RemoteException;

public class OppoAppProtectController extends IActivityController.Stub {
    @Override // android.app.IActivityController
    public boolean activityStarting(Intent intent, String pkg) {
        return true;
    }

    @Override // android.app.IActivityController
    public boolean activityResuming(String pkg) {
        return true;
    }

    @Override // android.app.IActivityController
    public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) {
        return true;
    }

    @Override // android.app.IActivityController
    public int appNotResponding(String processName, int pid, String processStats) {
        return 0;
    }

    @Override // android.app.IActivityController
    public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
        return 0;
    }

    @Override // android.app.IActivityController
    public int systemNotResponding(String msg) {
        return -1;
    }
}

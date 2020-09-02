package com.mediatek.server.powerhal;

import com.mediatek.boostframework.Performance;
import com.mediatek.powerhalwrapper.PowerHalWrapper;

public class PowerHalManagerImpl extends PowerHalManager {
    private static final String TAG = "PowerHalManagerImpl";
    private static Performance mPerformance = new Performance();
    private static PowerHalWrapper mPowerHalWrap = null;
    public boolean mIsRotationBoostEnable = false;

    public PowerHalManagerImpl() {
        mPowerHalWrap = PowerHalWrapper.getInstance();
    }

    public void setRotationBoost(boolean enable) {
        int boostTime = 0;
        if (enable && !this.mIsRotationBoostEnable) {
            boostTime = 2000;
        } else if (!enable && this.mIsRotationBoostEnable) {
            boostTime = 0;
        }
        mPowerHalWrap.setRotationBoost(boostTime);
        this.mIsRotationBoostEnable = enable;
    }

    public void setWFD(boolean enable) {
        mPowerHalWrap.setWFD(enable);
    }

    public void perfLockAcquire(int duration, int... list) {
        mPerformance.perfLockAcquire(duration, list);
    }

    public void perfLockRelease() {
        mPerformance.perfLockRelease();
    }

    public void perfLockRelease(int handle) {
        mPerformance.perfLockRelease(handle);
    }

    public void NotifyAppCrash(int pid, int uid, String packageName) {
        mPowerHalWrap.NotifyAppCrash(pid, uid, packageName);
    }

    public void setInstallationBoost(boolean enable) {
        mPowerHalWrap.setInstallationBoost(enable);
    }

    public void setSpeedDownload(int timeoutMs) {
        mPowerHalWrap.setSpeedDownload(timeoutMs);
    }

    public void amsBoostResume(String lastResumedPackageName, String nextResumedPackageName) {
        mPowerHalWrap.amsBoostResume(lastResumedPackageName, nextResumedPackageName);
    }

    public void amsBoostNotify(int pid, String activityName, String packageName, int uid) {
        mPowerHalWrap.amsBoostNotify(pid, activityName, packageName, uid);
    }

    public void amsBoostProcessCreate(String hostingType, String packageName) {
        mPowerHalWrap.amsBoostProcessCreate(hostingType, packageName);
    }

    public void amsBoostStop() {
        mPowerHalWrap.amsBoostStop();
    }

    public void setVersionInfo(String data) {
        mPowerHalWrap.setSysInfoAsync(9, data);
    }
}

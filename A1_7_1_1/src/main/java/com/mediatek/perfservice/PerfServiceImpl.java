package com.mediatek.perfservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.mediatek.perfservice.IPerfService.Stub;

public class PerfServiceImpl extends Stub {
    private static final String TAG = "PerfService";
    final Context mContext;
    private IPerfServiceManager perfServiceMgr;

    class PerfServiceBroadcastReceiver extends BroadcastReceiver {
        PerfServiceBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                PerfServiceImpl.this.log("Intent.ACTION_SCREEN_OFF");
                PerfServiceImpl.this.perfServiceMgr.userDisableAll();
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                PerfServiceImpl.this.log("Intent.ACTION_SCREEN_ON");
                PerfServiceImpl.this.perfServiceMgr.userRestoreAll();
            } else if ("android.intent.action.PACKAGE_INSTALL_BEGIN".equals(action)) {
                PerfServiceImpl.this.log("Intent.ACTION_PACKAGE_INSTALL_BEGIN");
                PerfServiceImpl.this.perfServiceMgr.boostEnableTimeout(4, 10);
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                PerfServiceImpl.this.log("Intent.ACTION_PACKAGE_ADDED");
                PerfServiceImpl.this.perfServiceMgr.boostDisable(4);
            } else {
                PerfServiceImpl.this.log("Unexpected intent " + intent);
            }
        }
    }

    public PerfServiceImpl(Context context, IPerfServiceManager pm) {
        this.perfServiceMgr = pm;
        this.mContext = context;
        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction("android.intent.action.SCREEN_OFF");
        broadcastFilter.addAction("android.intent.action.SCREEN_ON");
        broadcastFilter.addAction("android.intent.action.PACKAGE_INSTALL_BEGIN");
        broadcastFilter.addAction("android.intent.action.PACKAGE_ADDED");
        this.mContext.registerReceiver(new PerfServiceBroadcastReceiver(), broadcastFilter);
    }

    public void boostEnable(int scenario) {
        this.perfServiceMgr.boostEnable(scenario);
    }

    public void boostDisable(int scenario) {
        this.perfServiceMgr.boostDisable(scenario);
    }

    public void boostEnableTimeout(int scenario, int timeout) {
        this.perfServiceMgr.boostEnableTimeout(scenario, timeout);
    }

    public void boostEnableTimeoutMs(int scenario, int timeout_ms) {
        this.perfServiceMgr.boostEnableTimeoutMs(scenario, timeout_ms);
    }

    public void notifyAppState(String packName, String className, int state, int pid) {
        this.perfServiceMgr.notifyAppState(packName, className, state, pid);
    }

    public int userReg(int scn_core, int scn_freq, int pid, int tid) {
        return this.perfServiceMgr.userReg(scn_core, scn_freq, pid, tid);
    }

    public int userRegBigLittle(int scn_core_big, int scn_freq_big, int scn_core_little, int scn_freq_little, int pid, int tid) {
        return this.perfServiceMgr.userRegBigLittle(scn_core_big, scn_freq_big, scn_core_little, scn_freq_little, pid, tid);
    }

    public void userUnreg(int handle) {
        this.perfServiceMgr.userUnreg(handle);
    }

    public int userGetCapability(int cmd) {
        return this.perfServiceMgr.userGetCapability(cmd);
    }

    public int userRegScn(int pid, int tid) {
        return this.perfServiceMgr.userRegScn(pid, tid);
    }

    public void userRegScnConfig(int handle, int cmd, int param_1, int param_2, int param_3, int param_4) {
        this.perfServiceMgr.userRegScnConfig(handle, cmd, param_1, param_2, param_3, param_4);
    }

    public void userUnregScn(int handle) {
        this.perfServiceMgr.userUnregScn(handle);
    }

    public void userEnable(int handle) {
        this.perfServiceMgr.userEnable(handle);
    }

    public void userEnableTimeout(int handle, int timeout) {
        this.perfServiceMgr.userEnableTimeout(handle, timeout);
    }

    public void userEnableTimeoutMs(int handle, int timeout_ms) {
        this.perfServiceMgr.userEnableTimeoutMs(handle, timeout_ms);
    }

    public void userEnableAsync(int handle) {
        this.perfServiceMgr.userEnableAsync(handle);
    }

    public void userEnableTimeoutAsync(int handle, int timeout) {
        this.perfServiceMgr.userEnableTimeoutAsync(handle, timeout);
    }

    public void userEnableTimeoutMsAsync(int handle, int timeout_ms) {
        this.perfServiceMgr.userEnableTimeoutMsAsync(handle, timeout_ms);
    }

    public void userDisable(int handle) {
        this.perfServiceMgr.userDisable(handle);
    }

    public void userResetAll() {
        this.perfServiceMgr.userResetAll();
    }

    public void userDisableAll() {
        this.perfServiceMgr.userDisableAll();
    }

    public void userRestoreAll() {
        this.perfServiceMgr.userRestoreAll();
    }

    public void dumpAll() {
        this.perfServiceMgr.dumpAll();
    }

    public void setFavorPid(int pid) {
        this.perfServiceMgr.setFavorPid(pid);
    }

    public void restorePolicy(int pid) {
        this.perfServiceMgr.restorePolicy(pid);
    }

    public void notifyFrameUpdate(int level) {
        this.perfServiceMgr.notifyFrameUpdate(level);
    }

    public void notifyDisplayType(int type) {
        this.perfServiceMgr.notifyDisplayType(type);
    }

    public int getLastBoostPid() {
        return this.perfServiceMgr.getLastBoostPid();
    }

    public void notifyUserStatus(int type, int status) {
        this.perfServiceMgr.notifyUserStatus(type, status);
    }

    public int getClusterInfo(int cmd, int id) {
        return this.perfServiceMgr.getClusterInfo(cmd, id);
    }

    public void levelBoost(int level) {
        this.perfServiceMgr.levelBoost(level);
    }

    public int getPackAttr(String packName, int cmd) {
        return this.perfServiceMgr.getPackAttr(packName, cmd);
    }

    public void appBoostEnable(String packName) {
        this.perfServiceMgr.appBoostEnable(packName);
    }

    public String getGiftAttr(String packName, String attrName) {
        return this.perfServiceMgr.getGiftAttr(packName, attrName);
    }

    public int reloadWhiteList() {
        this.perfServiceMgr.reloadWhiteList();
        return 1;
    }

    public void setExclusiveCore(int pid, int cpu_mask) {
        this.perfServiceMgr.setExclusiveCore(pid, cpu_mask);
    }

    public void setUidInfo(int uid, int fromUid) {
        this.perfServiceMgr.setUidInfo(uid, fromUid);
    }

    private void log(String info) {
        Log.d("@M_PerfService", "[PerfService] " + info + " ");
    }

    private void loge(String info) {
        Log.e("@M_PerfService", "[PerfService] ERR: " + info + " ");
    }
}

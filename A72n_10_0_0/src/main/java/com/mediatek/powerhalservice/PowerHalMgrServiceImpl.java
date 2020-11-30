package com.mediatek.powerhalservice;

import android.content.Context;
import android.os.Binder;
import android.os.IRemoteCallback;
import android.util.Log;
import com.mediatek.boostframework.Performance;
import com.mediatek.omadm.PalConstDefs;
import com.mediatek.powerhalmgr.DupLinkInfo;
import com.mediatek.powerhalmgr.IPowerHalMgr;
import com.mediatek.powerhalwrapper.PowerHalWrapper;

public class PowerHalMgrServiceImpl extends IPowerHalMgr.Stub {
    private static Performance mPerformance = new Performance();
    private static PowerHalWrapper mPowerHalWrap = null;
    private static int mhandle = 0;
    private final String TAG = "PowerHalMgrServiceImpl";
    private PowerHalWifiMonitor mPowerHalWifiMonitor = null;

    public PowerHalMgrServiceImpl(Context context) {
        mPowerHalWrap = PowerHalWrapper.getInstance();
        this.mPowerHalWifiMonitor = new PowerHalWifiMonitor(context);
    }

    public int scnReg() {
        return mPowerHalWrap.scnReg();
    }

    public void scnConfig(int handle, int cmd, int param_1, int param_2, int param_3, int param_4) {
        mPowerHalWrap.scnConfig(handle, cmd, param_1, param_2, param_3, param_4);
    }

    public void scnUnreg(int handle) {
        mPowerHalWrap.scnUnreg(handle);
    }

    public void scnEnable(int handle, int timeout) {
        mPowerHalWrap.scnEnable(handle, timeout);
    }

    public void scnDisable(int handle) {
        mPowerHalWrap.scnDisable(handle);
    }

    public void scnUltraCfg(int handle, int ultracmd, int param_1, int param_2, int param_3, int param_4) {
        mPowerHalWrap.scnUltraCfg(handle, ultracmd, param_1, param_2, param_3, param_4);
    }

    public void mtkCusPowerHint(int hint, int data) {
        mPowerHalWrap.mtkCusPowerHint(hint, data);
    }

    public void getCpuCap() {
        mPowerHalWrap.getCpuCap();
    }

    public void getGpuCap() {
        mPowerHalWrap.getGpuCap();
    }

    public void getGpuRTInfo() {
        mPowerHalWrap.getGpuRTInfo();
    }

    public void getCpuRTInfo() {
        mPowerHalWrap.getCpuRTInfo();
    }

    public void UpdateManagementPkt(int type, String packet) {
        mPowerHalWrap.UpdateManagementPkt(type, packet);
    }

    public void setForegroundSports() {
        mPowerHalWrap.setSysInfo(3, PalConstDefs.EMPTY_STRING);
    }

    public void setSysInfo(int type, String data) {
        mPowerHalWrap.setSysInfo(type, data);
    }

    private boolean checkDppPermission() {
        if (mPowerHalWrap.getRildCap(Binder.getCallingUid())) {
            return true;
        }
        logd("checkDppPermission(), no permission");
        return false;
    }

    public boolean startDuplicatePacketPrediction() {
        if (!checkDppPermission()) {
            return false;
        }
        this.mPowerHalWifiMonitor.startDuplicatePacketPrediction();
        return true;
    }

    public boolean stopDuplicatePacketPrediction() {
        if (!checkDppPermission()) {
            return false;
        }
        this.mPowerHalWifiMonitor.stopDuplicatePacketPrediction();
        return true;
    }

    public boolean isDupPacketPredictionStarted() {
        return this.mPowerHalWifiMonitor.isDupPacketPredictionStarted();
    }

    public boolean registerDuplicatePacketPredictionEvent(IRemoteCallback listener) {
        if (!checkDppPermission()) {
            return false;
        }
        return this.mPowerHalWifiMonitor.registerDuplicatePacketPredictionEvent(listener);
    }

    public boolean unregisterDuplicatePacketPredictionEvent(IRemoteCallback listener) {
        if (!checkDppPermission()) {
            return false;
        }
        return this.mPowerHalWifiMonitor.unregisterDuplicatePacketPredictionEvent(listener);
    }

    public boolean updateMultiDuplicatePacketLink(DupLinkInfo[] linkList) {
        String strSrcPort;
        String linkInfo;
        logd("[updateMultiDuplicatePacketLink] len:" + linkList.length + " ");
        if (linkList.length > 3 || !checkDppPermission() || linkList.length <= 0) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("MULTI\t");
        sb.append(String.valueOf(linkList.length));
        for (DupLinkInfo info : linkList) {
            if (!PowerHalAddressUtils.isIpPairValid(info.getSrcIp(), info.getDstIp(), info.getSrcPort(), info.getDstPort())) {
                return false;
            }
            String dstSrcPort = "none";
            if (info.getSrcPort() == -1) {
                strSrcPort = dstSrcPort;
            } else {
                strSrcPort = Integer.toString(info.getSrcPort());
            }
            if (info.getDstPort() != -1) {
                dstSrcPort = Integer.toString(info.getDstPort());
            }
            String linkInfo2 = info.getSrcIp() + " " + strSrcPort + " " + info.getDstIp() + " " + dstSrcPort + " ";
            if (info.getProto() == 1) {
                linkInfo = linkInfo2 + "TCP";
            } else if (info.getProto() == 2) {
                linkInfo = linkInfo2 + "UDP";
            } else {
                logd("[updateMultiDuplicatePacketLink] unknown protocol:" + info.getProto());
                return false;
            }
            sb.append("\t");
            sb.append(linkInfo);
        }
        if (mPowerHalWrap.setSysInfo(8, sb.toString()) == 0) {
            return true;
        }
        return false;
    }

    public void setPredictInfo(String pack_name, int uid) {
        String data = pack_name + " " + uid;
        logd("setPredictInfo:" + data);
        mPowerHalWrap.setSysInfo(7, data);
    }

    public int perfLockAcquire(int handle, int duration, int[] list) {
        if (list.length % 2 != 0) {
            return -1;
        }
        logd("perfLockAcquire hdl:" + handle + " dur:" + duration + " len:" + list.length);
        for (int i = 0; i < list.length; i += 2) {
            logd("perfLockAcquire " + i + " id:" + Integer.toHexString(list[i]) + " value:" + list[i + 1]);
        }
        return mPowerHalWrap.perfLockAcquire(handle, duration, list);
    }

    public void perfLockRelease(int handle) {
        mPowerHalWrap.perfLockRelease(handle);
    }

    public int querySysInfo(int cmd, int param) {
        return mPowerHalWrap.querySysInfo(cmd, param);
    }

    public int setSysInfoSync(int type, String data) {
        return mPowerHalWrap.setSysInfo(type, data);
    }

    public void reloadwhitelist() {
        mPowerHalWrap.setSysInfo(10, PalConstDefs.EMPTY_STRING);
    }

    private void log(String info) {
        Log.i("PowerHalMgrServiceImpl", info + " ");
    }

    private void logd(String info) {
        if (Log.isLoggable("PowerHalMgrServiceImpl", 3)) {
            Log.d("PowerHalMgrServiceImpl", info + " ");
        }
    }

    private void loge(String info) {
        Log.e("PowerHalMgrServiceImpl", "ERR: " + info + " ");
    }
}

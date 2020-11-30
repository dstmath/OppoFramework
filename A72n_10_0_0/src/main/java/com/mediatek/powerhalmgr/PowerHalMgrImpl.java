package com.mediatek.powerhalmgr;

import android.content.Context;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.mediatek.powerhalmgr.IPowerHalMgr;

public class PowerHalMgrImpl extends PowerHalMgr {
    private static final String TAG = "PowerHalMgrImpl";
    private static Object lock = new Object();
    private static PowerHalMgrImpl sInstance = null;
    private int inited = 0;
    private Context mContext;
    private long mPreviousTime = 0;
    private IPowerHalMgr sService = null;
    private int setTid = 0;

    public static native int nativeGetPid();

    public static native int nativeGetTid();

    private void init() {
        IBinder b;
        if (this.inited == 0 && (b = ServiceManager.checkService("power_hal_mgr_service")) != null) {
            this.sService = IPowerHalMgr.Stub.asInterface(b);
            if (this.sService != null) {
                this.inited = 1;
            } else {
                loge("ERR: getService() sService is still null..");
            }
        }
    }

    public static PowerHalMgrImpl getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    sInstance = new PowerHalMgrImpl();
                }
            }
        }
        return sInstance;
    }

    public int scnReg() {
        try {
            init();
            if (this.sService != null) {
                return this.sService.scnReg();
            }
            return -1;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in scnReg:" + e);
            return -1;
        }
    }

    public void scnConfig(int handle, int cmd, int param_1, int param_2, int param_3, int param_4) {
        try {
            init();
            if (this.sService != null) {
                this.sService.scnConfig(handle, cmd, param_1, param_2, param_3, param_4);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in scnConfig:" + e);
        }
    }

    public void scnUnreg(int handle) {
        try {
            init();
            if (this.sService != null) {
                this.sService.scnUnreg(handle);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in scnUnreg:" + e);
        }
    }

    public void scnEnable(int handle, int timeout) {
        try {
            init();
            if (this.sService != null) {
                this.sService.scnEnable(handle, timeout);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in scnEnable:" + e);
        }
    }

    public void scnDisable(int handle) {
        try {
            init();
            if (this.sService != null) {
                this.sService.scnDisable(handle);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in scnDisable:" + e);
        }
    }

    public void scnUltraCfg(int handle, int ultracmd, int param_1, int param_2, int param_3, int param_4) {
        try {
            init();
            if (this.sService != null) {
                this.sService.scnUltraCfg(handle, ultracmd, param_1, param_2, param_3, param_4);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in scnConfig:" + e);
        }
    }

    public void mtkCusPowerHint(int hint, int data) {
        try {
            init();
            if (this.sService != null) {
                this.sService.mtkCusPowerHint(hint, data);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in mtkCusPowerHint:" + e);
        }
    }

    public void getCpuCap() {
        log("getCpuCap");
    }

    public void getGpuCap() {
        log("getGpuCap");
    }

    public void getGpuRTInfo() {
        log("getGpuRTInfo");
    }

    public void getCpuRTInfo() {
        log("getCpuRTInfo");
    }

    public void UpdateManagementPkt(int type, String packet) {
        try {
            init();
            if (this.sService != null) {
                this.sService.UpdateManagementPkt(type, packet);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in UpdateManagementPkt:" + e);
        }
    }

    public void setForegroundSports() {
        try {
            init();
            if (this.sService != null) {
                this.sService.setForegroundSports();
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in setForegroundSports:" + e);
        }
    }

    public void setSysInfo(int type, String data) {
        try {
            init();
            if (this.sService != null) {
                this.sService.setSysInfo(type, data);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in setSysInfo:" + e);
        }
    }

    public boolean startDuplicatePacketPrediction() {
        logd("startDuplicatePacketPrediction()");
        try {
            init();
            if (this.sService != null) {
                return this.sService.startDuplicatePacketPrediction();
            }
            return false;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in startDuplicatePacketPrediction:" + e);
            return false;
        }
    }

    public boolean stopDuplicatePacketPrediction() {
        logd("stopDuplicatePacketPrediction()");
        try {
            init();
            if (this.sService != null) {
                return this.sService.stopDuplicatePacketPrediction();
            }
            return false;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in stopDuplicatePacketPrediction:" + e);
            return false;
        }
    }

    public boolean isDupPacketPredictionStarted() {
        try {
            init();
            if (this.sService == null) {
                return false;
            }
            boolean enable = this.sService.isDupPacketPredictionStarted();
            logd("isDupPacketPredictionStarted() enable:" + enable);
            return enable;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in isDupPacketPredictionStarted:" + e);
            return false;
        }
    }

    public boolean registerDuplicatePacketPredictionEvent(IRemoteCallback listener) {
        logd("registerDuplicatePacketPredictionEvent() " + listener.getClass().toString());
        try {
            init();
            if (this.sService != null) {
                return this.sService.registerDuplicatePacketPredictionEvent(listener);
            }
            return false;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in registerDuplicatePacketPredictionEvent:" + e);
            return false;
        }
    }

    public boolean unregisterDuplicatePacketPredictionEvent(IRemoteCallback listener) {
        logd("unregisterDuplicatePacketPredictionEvent() " + listener.getClass().toString());
        try {
            init();
            if (this.sService != null) {
                return this.sService.unregisterDuplicatePacketPredictionEvent(listener);
            }
            return false;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in unregisterDuplicatePacketPredictionEvent:" + e);
            return false;
        }
    }

    public boolean updateMultiDuplicatePacketLink(DupLinkInfo[] linkList) {
        try {
            init();
            if (this.sService != null) {
                return this.sService.updateMultiDuplicatePacketLink(linkList);
            }
            return false;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in updateMultiDuplicatePacketLink:" + e);
            return false;
        }
    }

    public void setPredictInfo(String pack_name, int uid) {
        try {
            init();
            if (this.sService != null) {
                this.sService.setPredictInfo(pack_name, uid);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in setPredictInfo:" + e);
        }
    }

    public int perfLockAcquire(int handle, int duration, int[] list) {
        try {
            init();
            if (this.sService != null) {
                return this.sService.perfLockAcquire(handle, duration, list);
            }
            return handle;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in perfLockAcquire:" + e);
            return handle;
        }
    }

    public void perfLockRelease(int handle) {
        try {
            init();
            if (this.sService != null) {
                this.sService.perfLockRelease(handle);
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in perfLockRelease:" + e);
        }
    }

    public int querySysInfo(int cmd, int param) {
        try {
            init();
            if (this.sService != null) {
                return this.sService.querySysInfo(cmd, param);
            }
            return -1;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in perfLockAcquire:" + e);
            return -1;
        }
    }

    public void reloadwhitelist() {
        try {
            init();
            if (this.sService != null) {
                this.sService.reloadwhitelist();
            }
        } catch (RemoteException e) {
            loge("ERR: RemoteException in reloadwhitelist:" + e);
        }
    }

    public int setSysInfoSync(int type, String data) {
        try {
            init();
            if (this.sService != null) {
                return this.sService.setSysInfoSync(type, data);
            }
            return -1;
        } catch (RemoteException e) {
            loge("ERR: RemoteException in setPredictInfo:" + e);
            return -1;
        }
    }

    private void log(String info) {
        Log.i(TAG, info + " ");
    }

    private void logd(String info) {
        Log.d(TAG, info + " ");
    }

    private void loge(String info) {
        Log.e(TAG, "ERR: " + info + " ");
    }
}

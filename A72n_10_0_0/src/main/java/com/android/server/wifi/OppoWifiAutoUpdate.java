package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.wifi.OppoWifiSauConfig;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class OppoWifiAutoUpdate {
    private static final String ACTION_WIFI_AUTO_UPDATE = "oppo.intent.action.TRIGGER_WIFI_AUTOUPDATE";
    private static final String BROADCAST_ACTION_SAU_DATA_UPDATE = "com.coloros.sau.DATARES_UPDATE";
    private static boolean DBG = false;
    private static final int MSG_WIFI_SAU_DATA_IDLE = 0;
    private static final int MSG_WIFI_SAU_DATA_UPDATE = 1;
    private static final int MSG_WIFI_SAU_DATA_UPDATE_CB = 2;
    private static final int MSG_WIFI_SAU_DATA_UPDATE_DONE = 6;
    private static final int MSG_WIFI_SAU_DATA_UPDATE_NEXT = 5;
    private static final int MSG_WIFI_SAU_DATA_UPDATE_OBJS = 3;
    private static final int MSG_WIFI_SAU_DATA_UPDATE_OBJS_CB = 4;
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final String RECOVERY_TYPE_GENERAL = "general";
    private static final String RECOVERY_TYPE_SPECIAL1 = "special1";
    private static final String RECOVERY_TYPE_SPECIAL2 = "special2";
    private static final String TAG = "OppoWifiAutoUpdate";
    private static final String TRIGGER_RECOVERY_MODE_MTK = "mtk";
    private static final String TRIGGER_RECOVERY_MODE_NONE = "none";
    private static final String TRIGGER_RECOVERY_MODE_QCOM = "qcom";
    private static final String WIFI_SAU_UPDATE_TAG = "SAU-AUTO_LOAD_FW-10";
    private OppoWifiAutoUpdateBroadcastReceiver mBroadcastReceiver;
    private Context mContext = null;
    private OppoWifiUpdateData mCurUpdateData = null;
    private DirectoryObserver mDirectoryObserver = null;
    private Handler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread(TAG);
    private OppoWifiSauConfig mOppoWifiSauConfig = null;
    private String mRecoveryMode = "none";
    private String mRecoveryType = RECOVERY_TYPE_GENERAL;
    private String mSauConfigFilePath;
    private String mSauDirPath;
    private String mSauFinishFilePath;
    private int mSauStateMachine = 0;
    private LinkedList<OppoWifiSauConfig.StoreData> mUpdteDataList = null;
    private LinkedList<OppoWifiSauConfig.StoreData> mUpdteDataStatisticList = null;

    public OppoWifiAutoUpdate(Context c) {
        this.mHandlerThread.start();
        this.mHandler = new wifiAutoUpdateHander(this.mHandlerThread.getLooper());
        this.mContext = c;
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoWifiAutoUpdate.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String code;
                String action = intent.getAction();
                if (OppoWifiAutoUpdate.DBG) {
                    Log.d(OppoWifiAutoUpdate.TAG, "onReceive,action : " + action);
                }
                boolean wcnRusEnable = WifiRomUpdateHelper.getInstance(OppoWifiAutoUpdate.this.mContext).getBooleanValue("OPPO_BASIC_WIFI_CUSTOM_WCN_SAU", false);
                if (!wcnRusEnable) {
                    if (OppoWifiAutoUpdate.DBG) {
                        Log.d(OppoWifiAutoUpdate.TAG, "wcn rus not enable wcnRusEnable : " + wcnRusEnable);
                    }
                } else if (action.equals(OppoWifiAutoUpdate.BROADCAST_ACTION_SAU_DATA_UPDATE) && (code = intent.getStringExtra("code")) != null && code.equals(OppoWifiAutoUpdate.WIFI_SAU_UPDATE_TAG)) {
                    OppoWifiAutoUpdate.this.mHandler.sendMessageDelayed(OppoWifiAutoUpdate.this.mHandler.obtainMessage(1), 0);
                }
            }
        }, new IntentFilter(BROADCAST_ACTION_SAU_DATA_UPDATE), OPPO_COMPONENT_SAFE_PERMISSION, null);
        this.mBroadcastReceiver = new OppoWifiAutoUpdateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WIFI_AUTO_UPDATE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    private class OppoWifiAutoUpdateBroadcastReceiver extends BroadcastReceiver {
        private OppoWifiAutoUpdateBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OppoWifiAutoUpdate.ACTION_WIFI_AUTO_UPDATE)) {
                Log.d(OppoWifiAutoUpdate.TAG, "receive reset action");
                OppoWifiAutoUpdate.this.mHandler.sendMessageDelayed(OppoWifiAutoUpdate.this.mHandler.obtainMessage(1), 0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void init() {
        this.mOppoWifiSauConfig = new OppoWifiSauConfig(this.mContext);
        this.mOppoWifiSauConfig.enableVerboseLogging(DBG);
        this.mSauConfigFilePath = this.mOppoWifiSauConfig.getConfigFilePath();
        this.mSauFinishFilePath = this.mOppoWifiSauConfig.getFinishFilePath();
        this.mSauDirPath = this.mOppoWifiSauConfig.getConfigDir();
        createDefaultDirs();
        this.mRecoveryMode = "none";
        this.mRecoveryType = RECOVERY_TYPE_GENERAL;
        this.mCurUpdateData = null;
        this.mUpdteDataStatisticList = new LinkedList<>();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deInit() {
        if (this.mOppoWifiSauConfig != null) {
            this.mOppoWifiSauConfig = null;
        }
        DirectoryObserver directoryObserver = this.mDirectoryObserver;
        if (directoryObserver != null) {
            directoryObserver.stopWatching();
            if (DBG) {
                Log.d(TAG, "stop oberver file");
            }
        }
        this.mUpdteDataList = null;
        this.mUpdteDataStatisticList = null;
        this.mSauStateMachine = 0;
        this.mRecoveryMode = "none";
        this.mRecoveryType = RECOVERY_TYPE_GENERAL;
        this.mCurUpdateData = null;
    }

    public void enableVerboseLogging(int verbose) {
        DBG = verbose > 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wcnTriggerRecoveryAndWorks(String recoveryMode, String recoveryType) {
        if (DBG) {
            Log.d(TAG, "wcnTriggerRecoveryAndWorks recoveryMode = " + recoveryMode);
        }
        WifiInjector.getInstance().getOppoSilenceRecovery().trigger_fw_recovery(recoveryMode, recoveryType);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wcnUpdateObjsPreProcess() {
        try {
            this.mOppoWifiSauConfig.read();
        } catch (Exception e) {
            Log.e(TAG, " wcnUpdateObjsPreProcess read failed : ", e);
        }
        OppoWifiSauConfig oppoWifiSauConfig = this.mOppoWifiSauConfig;
        if (oppoWifiSauConfig != null) {
            this.mUpdteDataList = oppoWifiSauConfig.sauValidCheck();
            wcnUpdateObjsProcess();
            return;
        }
        Log.d(TAG, "wcnUpdateObjsPreProcess push failed");
        deInit();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wcnUpdateObjsProcess() {
        Message msg;
        this.mCurUpdateData = null;
        LinkedList<OppoWifiSauConfig.StoreData> linkedList = this.mUpdteDataList;
        if (linkedList == null || linkedList.isEmpty()) {
            Log.d(TAG, " wcnUpdateObjsProcess none");
            msg = this.mHandler.obtainMessage(6);
        } else {
            OppoWifiUpdateData obj = (OppoWifiUpdateData) this.mUpdteDataList.removeFirst();
            this.mCurUpdateData = obj;
            this.mUpdteDataStatisticList.add(obj);
            Log.d(TAG, " wcnUpdateObjsProcess Platform = " + obj.mPlatform + " Type = " + obj.mFileType);
            if (obj.mPlatform.equals("mtk") && obj.mIsValid) {
                boolean wcnRusEnable = WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("OPPO_BASIC_WIFI_CUSTOM_WCN_SAU_MTK", false);
                if (!wcnRusEnable) {
                    Log.d(TAG, "wcn sau not enable mtk objs update : " + wcnRusEnable);
                    this.mCurUpdateData.setUpdateDateVaild(false);
                    this.mCurUpdateData.setUpdateDateFailReason("noEnableMtkRus");
                    msg = this.mHandler.obtainMessage(5);
                } else {
                    msg = this.mHandler.obtainMessage(3);
                }
            } else if (obj.mPlatform.equals("qcom") && obj.mIsValid) {
                boolean wcnRusEnable2 = WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("OPPO_BASIC_WIFI_CUSTOM_WCN_SAU_QCOM", false);
                if (!wcnRusEnable2) {
                    Log.d(TAG, "wcn sau not enable qcom objs update : " + wcnRusEnable2);
                    this.mCurUpdateData.setUpdateDateVaild(false);
                    this.mCurUpdateData.setUpdateDateFailReason("noEnableQcomRus");
                    msg = this.mHandler.obtainMessage(5);
                } else {
                    msg = this.mHandler.obtainMessage(3);
                }
            } else {
                msg = this.mHandler.obtainMessage(5);
                if (obj.mIsValid) {
                    this.mCurUpdateData.setUpdateDateVaild(false);
                    this.mCurUpdateData.setUpdateDateFailReason("wrongPlatform");
                }
            }
        }
        this.mHandler.sendMessageDelayed(msg, 0);
    }

    private void createDefaultDirs() {
        String path = this.mSauDirPath;
        File dir = new File(path);
        if (DBG) {
            Log.d(TAG, " createDefaultDirs  " + path);
        }
        if (!dir.exists()) {
            if (DBG) {
                Log.d(TAG, " mkdirs " + path);
            }
            dir.mkdirs();
            chmod(path);
        }
        this.mDirectoryObserver = new DirectoryObserver(path);
    }

    private void chmod(String path) {
        try {
            if (DBG) {
                Log.d(TAG, " chmod " + path);
            }
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("chmod 777 " + path);
            Runtime runtime2 = Runtime.getRuntime();
            runtime2.exec("chown system:system " + path);
        } catch (IOException e) {
            Log.e(TAG, "can't chmod file:" + path + " error:" + e);
        }
    }

    /* access modifiers changed from: private */
    public class DirectoryObserver extends FileObserver {
        private String mFocusPath;

        public DirectoryObserver(String path) {
            super(path, 256);
            this.mFocusPath = path;
            if (OppoWifiAutoUpdate.DBG) {
                Log.e(OppoWifiAutoUpdate.TAG, "DirectoryObserver path=" + this.mFocusPath);
            }
        }

        public void onEvent(int event, String path) {
            Message msg;
            if (OppoWifiAutoUpdate.DBG) {
                Log.e(OppoWifiAutoUpdate.TAG, "mFocusPath SAU_FILE_DIR  event=" + event + " path=" + path);
            }
            if (event == 256 && path.equals(OppoWifiAutoUpdate.this.mOppoWifiSauConfig.getFinishFname()) && new File(OppoWifiAutoUpdate.this.mSauFinishFilePath).exists()) {
                if (OppoWifiAutoUpdate.DBG) {
                    Log.e(OppoWifiAutoUpdate.TAG, "finish file is write back.");
                }
                if (OppoWifiAutoUpdate.this.mSauStateMachine == 1) {
                    msg = OppoWifiAutoUpdate.this.mHandler.obtainMessage(2);
                } else if (OppoWifiAutoUpdate.this.mSauStateMachine == 3) {
                    msg = OppoWifiAutoUpdate.this.mHandler.obtainMessage(4);
                } else {
                    msg = OppoWifiAutoUpdate.this.mHandler.obtainMessage(6);
                }
                OppoWifiAutoUpdate.this.mHandler.sendMessageDelayed(msg, 1000);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wcnWifiObjUpdateStatistics() {
        OppoWifiSauStatistics dcs = new OppoWifiSauStatistics(this.mContext);
        LinkedList<OppoWifiSauConfig.StoreData> linkedList = this.mUpdteDataStatisticList;
        if (linkedList == null || linkedList.isEmpty()) {
            Log.d(TAG, " mUpdteDataStatisticList null and faild to Statistics");
            return;
        }
        Iterator<OppoWifiSauConfig.StoreData> it = this.mUpdteDataStatisticList.iterator();
        while (it.hasNext()) {
            OppoWifiUpdateData obj = (OppoWifiUpdateData) it.next();
            if (obj.mIsValid) {
                dcs.sauPushSucess(obj.mPlatform, obj.mFileType, obj.mFileName, obj.mEffectMethod, obj.mPushReason, obj.mPushTime);
            } else {
                dcs.sauPushFailed(obj.mPlatform, obj.mFileType, obj.mFileName, obj.mEffectMethod, obj.mPushReason, obj.mPushTime, obj.mPushFail);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wcnTriggerWifiObjUpdate(String updatePlatform, String objtype) {
        Log.d(TAG, "wcnTriggerWifiObjUpdate, updatePlatform = " + updatePlatform + " objtype = " + objtype);
        SystemProperties.set("oppo.wifi.sau.objs.type", objtype);
        if ("mtk".equals(updatePlatform)) {
            SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "sauMtkWifiObjsPreUpgrade");
        } else if ("qcom".equals(updatePlatform)) {
            SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "sauQcomWifiObjsPreUpgrade");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wcnWifiObjUpdateCallBack(String updatePlatform, String recoverytype) {
        boolean isSuccess = false;
        String status = SystemProperties.get("oppo.wifi.sau.objs.upgrade.status", "idle");
        Log.d(TAG, "sau updata mtk objs status = " + status);
        if ("mtk".equals(updatePlatform)) {
            if ("success".equals(status)) {
                this.mRecoveryMode = "mtk";
                this.mRecoveryType = recoverytype;
                isSuccess = true;
            }
        } else if ("qcom".equals(updatePlatform) && "success".equals(status)) {
            this.mRecoveryMode = "qcom";
            this.mRecoveryType = recoverytype;
            isSuccess = true;
        }
        if (!isSuccess) {
            this.mCurUpdateData.setUpdateDateVaild(false);
            this.mCurUpdateData.setUpdateDateFailReason("wrongVersion");
        }
        SystemProperties.set("oppo.wifi.sau.objs.upgrade.status", "idle");
        wcnUpdateObjsProcess();
    }

    private class wifiAutoUpdateHander extends Handler {
        public wifiAutoUpdateHander(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (OppoWifiAutoUpdate.this.mSauStateMachine == 0) {
                        OppoWifiAutoUpdate.this.mSauStateMachine = 1;
                        OppoWifiAutoUpdate.this.init();
                        if (OppoWifiAutoUpdate.DBG) {
                            Log.d(OppoWifiAutoUpdate.TAG, "start wcn sau cp to data");
                        }
                        if (OppoWifiAutoUpdate.this.mDirectoryObserver != null) {
                            OppoWifiAutoUpdate.this.mDirectoryObserver.startWatching();
                            if (OppoWifiAutoUpdate.DBG) {
                                Log.d(OppoWifiAutoUpdate.TAG, "start oberver file");
                            }
                        }
                        SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "sauWifiFileTransfer");
                        return;
                    }
                    Log.d(OppoWifiAutoUpdate.TAG, "wcn sau on going, assert ......");
                    return;
                case 2:
                    OppoWifiAutoUpdate.this.mSauStateMachine = 2;
                    OppoWifiAutoUpdate.this.wcnUpdateObjsPreProcess();
                    return;
                case 3:
                    OppoWifiAutoUpdate.this.mSauStateMachine = 3;
                    OppoWifiAutoUpdate oppoWifiAutoUpdate = OppoWifiAutoUpdate.this;
                    oppoWifiAutoUpdate.wcnTriggerWifiObjUpdate(oppoWifiAutoUpdate.mCurUpdateData.mPlatform, OppoWifiAutoUpdate.this.mCurUpdateData.mFileType);
                    return;
                case 4:
                    OppoWifiAutoUpdate.this.mSauStateMachine = 4;
                    OppoWifiAutoUpdate oppoWifiAutoUpdate2 = OppoWifiAutoUpdate.this;
                    oppoWifiAutoUpdate2.wcnWifiObjUpdateCallBack(oppoWifiAutoUpdate2.mCurUpdateData.mPlatform, OppoWifiAutoUpdate.this.mCurUpdateData.mEffectMethod);
                    return;
                case 5:
                    OppoWifiAutoUpdate.this.mSauStateMachine = 5;
                    OppoWifiAutoUpdate.this.wcnUpdateObjsProcess();
                    return;
                case 6:
                    OppoWifiAutoUpdate.this.mSauStateMachine = 6;
                    OppoWifiAutoUpdate.this.wcnWifiObjUpdateStatistics();
                    OppoWifiAutoUpdate oppoWifiAutoUpdate3 = OppoWifiAutoUpdate.this;
                    oppoWifiAutoUpdate3.wcnTriggerRecoveryAndWorks(oppoWifiAutoUpdate3.mRecoveryMode, OppoWifiAutoUpdate.this.mRecoveryType);
                    OppoWifiAutoUpdate.this.deInit();
                    return;
                default:
                    if (OppoWifiAutoUpdate.DBG) {
                        Log.d(OppoWifiAutoUpdate.TAG, "ignored unknown msg: " + msg.what);
                        return;
                    }
                    return;
            }
        }
    }
}

package com.android.server.biometrics.face.dcs;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.server.UiModeManagerService;
import com.android.server.biometrics.face.tool.ExHandler;
import com.android.server.biometrics.face.utils.LogUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import oppo.util.OppoStatistics;

public class DcsUtil {
    public static final String EVENT_FACE_FAIL = "face_identify_fail";
    public static final String EVENT_FACE_INFO = "face_info";
    public static final String EVENT_FACE_PROCESS_DEATH_INFO = "face_process_death_info";
    public static final String EVENT_FACE_SUCESS = "face_identify_sucess";
    public static final String EVENT_HEALTH_TIMEOUT = "face_health_timeout";
    public static final String FACE_LOG_TAG = "facesystem";
    public static final String KEY_ACQUIRED_INFO = "acquireinfo";
    public static final String KEY_FACEINFO_AGE = "faceAge";
    public static final String KEY_FACEINFO_SEXUALITY = "faceSexuality";
    public static final String KEY_FACE_ID = "faceID";
    public static final String KEY_FACE_PROCESS_DEATH_PID = "face_process_death_pid";
    public static final String KEY_FACE_PROCESS_DEATH_TIME = "face_process_death_time";
    public static final String KEY_PACKAGE_NAME = "packageName";
    public static final String KEY_TIMEOUT_FUNCTION = "functionName";
    public static final int MSG_ACQUIRED_INFO = 2;
    public static final int MSG_CAMERA_WORKING_TIMEOUT_INFO = 7;
    public static final int MSG_CLEAR_MAP = 3;
    public static final int MSG_FACE_ID = 1;
    public static final int MSG_FACE_INFO = 5;
    public static final int MSG_FACE_PROCESS_DEATH_INFO = 6;
    public static final int MSG_HEALTH_TIMEOUT = 4;
    private static Object sMutex = new Object();
    private static DcsUtil sSingleInstance;
    private final String TAG = "FaceService.DcsUtil";
    private Context mContext;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private Looper mLooper;
    public Map<String, String> mStatisticsMap = new ConcurrentHashMap();

    private DcsUtil(Context context) {
        LogUtil.d("FaceService.DcsUtil", "DcsUtil construction");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("DcsUtil thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e("FaceService.DcsUtil", "mLooper null");
        }
        initHandler();
    }

    public static DcsUtil getDcsUtil(Context mContext2) {
        DcsUtil dcsUtil;
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new DcsUtil(mContext2);
            }
            dcsUtil = sSingleInstance;
        }
        return dcsUtil;
    }

    public void stopDcs() {
        this.mLooper.quit();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.face.dcs.DcsUtil.AnonymousClass1 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        DcsUtil.this.handleFaceId(msg);
                        break;
                    case 2:
                        DcsUtil.this.handleAcquiredInfo(msg);
                        break;
                    case 3:
                        DcsUtil.this.handleClearMap();
                        break;
                    case 4:
                        DcsUtil.this.handleHealthTimeout(msg);
                        break;
                    case 5:
                        DcsUtil.this.handleFaceInfo(msg);
                        break;
                    case 6:
                        LogUtil.e("FaceService.DcsUtil", "handleFaceProcessDeathInfo, msg.obj = " + ((String) msg.obj));
                        DcsUtil.this.handleFaceProcessDeathInfo(msg);
                        break;
                    case 7:
                        DcsUtil.this.handleCameraWorkingTimeoutInfo(msg);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void sendFaceId(int faceId, String pkgName) {
        if (pkgName != null) {
            this.mHandler.obtainMessage(1, faceId, 0, pkgName).sendToTarget();
        } else {
            LogUtil.e("FaceService.DcsUtil", "pkgName == null");
        }
    }

    public void sendCameraWorkingTimeoutInfo(int value) {
        LogUtil.e("FaceService.DcsUtil", "sendCameraWorkingTimeoutInfo!");
        this.mHandler.obtainMessage(7, Integer.valueOf(value)).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleCameraWorkingTimeoutInfo(Message msg) {
        LogUtil.e("FaceService.DcsUtil", "handleCameraWorkingTimeoutInfo!");
        this.mStatisticsMap.put("CameraNotRelease", Integer.toString(msg.arg1));
        uploadDataToDcs(this.mStatisticsMap, "camera_not_release");
    }

    /* access modifiers changed from: private */
    public void handleFaceId(Message msg) {
        this.mStatisticsMap.put(KEY_FACE_ID, Integer.toString(msg.arg1));
        this.mStatisticsMap.put("packageName", (String) msg.obj);
        if (msg.arg1 == 0) {
            uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_FAIL);
        } else {
            uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_SUCESS);
        }
    }

    public void sendAcquireInfo(int acquiredInfo, String pkgName) {
        this.mHandler.obtainMessage(2, acquiredInfo, 0, pkgName).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleAcquiredInfo(Message msg) {
    }

    public void clearMap() {
        this.mHandler.obtainMessage(3, 0, 0, null).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleClearMap() {
        this.mStatisticsMap.clear();
    }

    public void sendHealthTimeout(String funcName) {
        if (funcName != null) {
            this.mHandler.obtainMessage(4, 0, 0, funcName).sendToTarget();
        }
    }

    public void sendFaceProcessDeathInfo(String deathTime, int facePid) {
        LogUtil.e("FaceService.DcsUtil", "sendFaceProcessDeathInfo, deathTime = " + deathTime);
        if (deathTime != null) {
            this.mHandler.obtainMessage(6, facePid, 0, deathTime).sendToTarget();
        }
    }

    public void sendFaceInfo(int sexuality, int age) {
        this.mHandler.obtainMessage(5, sexuality, age).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleHealthTimeout(Message msg) {
        this.mStatisticsMap.put("functionName", (String) msg.obj);
        uploadDataToDcs(this.mStatisticsMap, EVENT_HEALTH_TIMEOUT);
    }

    /* access modifiers changed from: private */
    public void handleFaceProcessDeathInfo(Message msg) {
        LogUtil.e("FaceService.DcsUtil", "handleFaceProcessDeathInfo, KEY_FACE_PROCESS_DEATH_TIME = face_process_death_time, msg.obj = " + ((String) msg.obj) + ", msg.arg1 = " + msg.arg1);
        this.mStatisticsMap.put(KEY_FACE_PROCESS_DEATH_TIME, (String) msg.obj);
        Map<String, String> map = this.mStatisticsMap;
        map.put(KEY_FACE_PROCESS_DEATH_PID, "" + msg.arg1);
        uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_PROCESS_DEATH_INFO);
    }

    /* access modifiers changed from: private */
    public void handleFaceInfo(Message msg) {
        String sexuality = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        if (msg.arg1 == 0) {
            sexuality = "male";
        } else if (msg.arg1 == 1) {
            sexuality = "female";
        }
        this.mStatisticsMap.put(KEY_FACEINFO_SEXUALITY, sexuality);
        Map<String, String> map = this.mStatisticsMap;
        map.put(KEY_FACEINFO_AGE, "" + msg.arg2);
        uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_INFO);
    }

    private void uploadDataToDcs(Map<String, String> data, String eventId) {
        OppoStatistics.onCommon(this.mContext, FACE_LOG_TAG, eventId, data, false);
        handleClearMap();
    }
}

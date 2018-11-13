package com.android.server.face.dcs;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import oppo.util.OppoStatistics;

public class DcsUtil {
    public static final String EVENT_FACE_FAIL = "face_identify_fail";
    public static final String EVENT_FACE_INFO = "face_info";
    public static final String EVENT_FACE_SUCESS = "face_identify_sucess";
    public static final String EVENT_HEALTH_TIMEOUT = "face_health_timeout";
    public static final String FACE_LOG_TAG = "facesystem";
    public static final String KEY_ACQUIRED_INFO = "acquireinfo";
    public static final String KEY_FACEINFO_AGE = "faceAge";
    public static final String KEY_FACEINFO_SEXUALITY = "faceSexuality";
    public static final String KEY_FACE_ID = "faceID";
    public static final String KEY_PACKAGE_NAME = "packageName";
    public static final String KEY_TIMEOUT_FUNCTION = "functionName";
    public static final int MSG_ACQUIRED_INFO = 2;
    public static final int MSG_CLEAR_MAP = 3;
    public static final int MSG_FACE_ID = 1;
    public static final int MSG_FACE_INFO = 5;
    public static final int MSG_HEALTH_TIMEOUT = 4;
    private static Object sMutex = new Object();
    private static DcsUtil sSingleInstance;
    private final String TAG = "FaceService.DcsUtil";
    private Context mContext;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private Looper mLooper;
    public Map<String, String> mStatisticsMap = new ConcurrentHashMap();

    public DcsUtil(Context context) {
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

    public static DcsUtil getDcsUtil(Context mContext) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new DcsUtil(mContext);
            }
        }
        return sSingleInstance;
    }

    public void stopDcs() {
        this.mLooper.quit();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
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
                }
                super.handleMessage(msg);
            }
        };
    }

    public void sendFaceId(int faceId, String pkgName) {
        this.mHandler.obtainMessage(1, faceId, 0, null).sendToTarget();
    }

    private void handleFaceId(Message msg) {
        this.mStatisticsMap.put(KEY_FACE_ID, Integer.toString(msg.arg1));
        if (msg.arg1 == 0) {
            uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_FAIL);
        } else {
            uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_SUCESS);
        }
    }

    public void sendAcquireInfo(int acquiredInfo, String pkgName) {
        this.mHandler.obtainMessage(2, acquiredInfo, 0, pkgName).sendToTarget();
    }

    private void handleAcquiredInfo(Message msg) {
    }

    public void clearMap() {
        this.mHandler.obtainMessage(3, 0, 0, null).sendToTarget();
    }

    private void handleClearMap() {
        this.mStatisticsMap.clear();
    }

    public void sendHealthTimeout(String funcName) {
        if (funcName != null) {
            this.mHandler.obtainMessage(4, 0, 0, funcName).sendToTarget();
        }
    }

    public void sendFaceInfo(int sexuality, int age) {
        this.mHandler.obtainMessage(5, sexuality, age).sendToTarget();
    }

    private void handleHealthTimeout(Message msg) {
        this.mStatisticsMap.put(KEY_TIMEOUT_FUNCTION, (String) msg.obj);
        uploadDataToDcs(this.mStatisticsMap, EVENT_HEALTH_TIMEOUT);
    }

    private void handleFaceInfo(Message msg) {
        String sexuality = Shell.NIGHT_MODE_STR_UNKNOWN;
        if (msg.arg1 == 0) {
            sexuality = "male";
        } else if (msg.arg1 == 1) {
            sexuality = "female";
        }
        this.mStatisticsMap.put(KEY_FACEINFO_SEXUALITY, sexuality);
        this.mStatisticsMap.put(KEY_FACEINFO_AGE, "" + msg.arg2);
        uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_INFO);
    }

    private void uploadDataToDcs(Map<String, String> data, String eventId) {
        OppoStatistics.onCommon(this.mContext, FACE_LOG_TAG, eventId, data, false);
        handleClearMap();
    }
}

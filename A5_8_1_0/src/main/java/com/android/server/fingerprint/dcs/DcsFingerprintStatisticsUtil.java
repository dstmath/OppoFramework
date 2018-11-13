package com.android.server.fingerprint.dcs;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.util.LogUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import oppo.util.OppoStatistics;

public class DcsFingerprintStatisticsUtil {
    public static final String EVENT_DOUBLE_HOME = "double_home_screen_on";
    public static final String EVENT_FINGER_ERROR = "fingerprint_identify_fail";
    public static final String EVENT_FINGER_OK = "fingerprint_identify_sucess";
    public static final String EVENT_MOVE_FAST = "fingerprint_move_fast";
    public static final String EVENT_SYNC_TEMPLATE = "sync_template_times";
    public static final String KEY_ACQUIRED_INFO = "acquireInfo";
    public static final String KEY_FINGER_ID = "fingerID";
    public static final String KEY_IMAGE_QUALITY = "imageInfo_quality";
    public static final String KEY_IMAGE_SCORE = "imageInfo_score";
    public static final String KEY_IMAGE_TYPE = "imageInfo_type";
    public static final String KEY_PACKAGE_NAME = "packageName";
    public static final int MSG_ACQUIRED_INFO = 3;
    public static final int MSG_CLEAR_MAP = 5;
    public static final int MSG_DOUBLE_HOME = 4;
    public static final int MSG_FINGER_ID = 1;
    public static final int MSG_IMAGE_INFO = 2;
    public static final int MSG_SYNC_TEMPLATE = 6;
    public static final String SYSTEM_APP_TAG = "20120";
    private static Object sMutex = new Object();
    private static DcsFingerprintStatisticsUtil sSingleInstance;
    private final String TAG = "FingerprintService.DcsUtil";
    private Context mContext;
    public Map<String, String> mFingerprintStatisticsMap = new ConcurrentHashMap();
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private Looper mLooper;

    public DcsFingerprintStatisticsUtil(Context context) {
        LogUtil.d("FingerprintService.DcsUtil", "DcsFingerprintStatisticsUtil construction");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("DcsFingerprintStatistics thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e("FingerprintService.DcsUtil", "mLooper null");
        }
        initHandler();
    }

    public static DcsFingerprintStatisticsUtil getDcsFingerprintStatisticsUtil(Context mContext) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new DcsFingerprintStatisticsUtil(mContext);
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
                        DcsFingerprintStatisticsUtil.this.handleFingerId(msg);
                        break;
                    case 2:
                        DcsFingerprintStatisticsUtil.this.handleImageInfo(msg);
                        break;
                    case 3:
                        DcsFingerprintStatisticsUtil.this.handleAcquiredInfo(msg);
                        break;
                    case 4:
                        DcsFingerprintStatisticsUtil.this.handleDoubleHomeTimes(msg);
                        break;
                    case 5:
                        DcsFingerprintStatisticsUtil.this.handleClearMap();
                        break;
                    case 6:
                        DcsFingerprintStatisticsUtil.this.handleSyncTemplateTimes(msg);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void handleClearMap() {
        this.mFingerprintStatisticsMap.clear();
    }

    private void handleFingerId(Message msg) {
        this.mFingerprintStatisticsMap.put(KEY_FINGER_ID, Integer.toString(msg.arg1));
        if (msg.arg1 == 0) {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_FINGER_ERROR);
        } else {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_FINGER_OK);
        }
    }

    private void handleAcquiredInfo(Message msg) {
        this.mFingerprintStatisticsMap.put("packageName", (String) msg.obj);
        this.mFingerprintStatisticsMap.put(KEY_ACQUIRED_INFO, Integer.toString(msg.arg1));
        if (msg.arg1 == 5) {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_MOVE_FAST);
        }
    }

    private void handleImageInfo(Message msg) {
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_TYPE, Integer.toString(msg.arg1));
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_QUALITY, Integer.toString(msg.arg2));
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_SCORE, Integer.toString(((Integer) msg.obj).intValue()));
    }

    private void handleDoubleHomeTimes(Message msg) {
        uploadDataToDcs(null, EVENT_DOUBLE_HOME);
    }

    private void handleSyncTemplateTimes(Message msg) {
        uploadDataToDcs(null, EVENT_SYNC_TEMPLATE);
    }

    public void sendFingerId(int fingerId, String pkgName) {
        this.mHandler.obtainMessage(1, fingerId, 0, null).sendToTarget();
    }

    public void sendAcquiredInfo(int acquiredInfo, String pkgName) {
        this.mHandler.obtainMessage(3, acquiredInfo, 0, pkgName).sendToTarget();
    }

    public void sendImageInfo(int type, int quality, int matchScore) {
        this.mHandler.obtainMessage(2, type, quality, Integer.valueOf(matchScore)).sendToTarget();
    }

    public void sendDoubleHomeTimes() {
        this.mHandler.obtainMessage(4, 0, 0, null).sendToTarget();
    }

    public void sendSyncTemplateTimes() {
        this.mHandler.obtainMessage(6, 0, 0, null).sendToTarget();
    }

    public void clearMap() {
        this.mHandler.obtainMessage(5, 0, 0, null).sendToTarget();
    }

    private void uploadDataToDcs(Map<String, String> data, String eventId) {
        OppoStatistics.onCommon(this.mContext, SYSTEM_APP_TAG, eventId, data, false);
        handleClearMap();
    }
}

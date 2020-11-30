package com.android.server.biometrics.fingerprint.dcs;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.android.server.biometrics.face.dcs.DcsUtil;
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.theia.NoFocusWindow;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import oppo.util.OppoStatistics;

public class DcsFingerprintStatisticsUtil {
    public static final String DCS_LOG_TAG = "PSW_Android";
    public static final String EVENT_ALIPAY_STATISTICAL = "face_alipay_statistical";
    public static final String EVENT_DOUBLE_HOME = "double_home_screen_on";
    public static final String EVENT_FINGER_ERROR = "fingerprint_identify_fail";
    public static final String EVENT_FINGER_OK = "fingerprint_identify_sucess";
    public static final String EVENT_HEALTH_TIMEOUT = "fingerprint_health_timeout";
    public static final String EVENT_LOCK_OUT_TYPE = "lock_out_times";
    public static final String EVENT_MOVE_FAST = "fingerprint_move_fast";
    public static final String EVENT_SYNC_TEMPLATE = "sync_template_times";
    public static final String FP_ID = "persist.vendor.fingerprint.fp_id";
    public static final String KEY_ACQUIRED_INFO = "acquireInfo";
    public static final String KEY_ALIPAY_AUTHENTICATE = "alipay_authenticate";
    public static final String KEY_ALIPAY_ENROLL = "alipay_enroll";
    public static final String KEY_ALIPAY_EVENT_ID = "alipayEventID";
    public static final String KEY_ALIPAY_FAILED = "alipay_failed";
    public static final String KEY_ALIPAY_RESULT = "alipayResult";
    public static final String KEY_ALIPAY_SUCCESS = "alipay_success";
    public static final String KEY_FINGER_ID = "fingerID";
    public static final String KEY_IMAGE_QUALITY = "imageInfo_quality";
    public static final String KEY_IMAGE_SCORE = "imageInfo_score";
    public static final String KEY_IMAGE_TYPE = "imageInfo_type";
    public static final String KEY_MODULE = "finger_module";
    public static final String KEY_MODULE_ALGORITHM = "finger_algorithm";
    public static final String KEY_PACKAGE_NAME = "packageName";
    public static final String KEY_TIMEOUT_FUNCTION = "functionName";
    public static final int MSG_ACQUIRED_INFO = 3;
    public static final int MSG_CLEAR_MAP = 5;
    public static final int MSG_DOUBLE_HOME = 4;
    public static final int MSG_FINGER_ID = 1;
    public static final int MSG_HEALTH_TIMEOUT = 9;
    public static final int MSG_IMAGE_INFO = 2;
    public static final int MSG_SYNC_TEMPLATE = 6;
    public static final int MSM_ALIPAY = 7;
    public static final int MSM_LOCK_OUT = 8;
    public static final String SYSTEM_APP_TAG = "20120";
    private static Object sMutex = new Object();
    private static DcsFingerprintStatisticsUtil sSingleInstance;
    private final String TAG = "FingerprintService.DcsUtil";
    private String fingerprintModule;
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
        this.fingerprintModule = SystemProperties.get(FP_ID);
        LogUtil.d("FingerprintService.DcsUtil", "DcsFingerprintStatisticsUtil init fp_id=" + this.fingerprintModule);
    }

    public static DcsFingerprintStatisticsUtil getDcsFingerprintStatisticsUtil(Context mContext2) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new DcsFingerprintStatisticsUtil(mContext2);
            }
        }
        return sSingleInstance;
    }

    public void stopDcs() {
        this.mLooper.quit();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil.AnonymousClass1 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
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
                    case 7:
                        DcsFingerprintStatisticsUtil.this.handleAlipayEvent(msg);
                        break;
                    case 8:
                        DcsFingerprintStatisticsUtil.this.handleLockOut(msg);
                        break;
                    case 9:
                        DcsFingerprintStatisticsUtil.this.handleHealthTimeout(msg);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void handleClearMap() {
        this.mFingerprintStatisticsMap.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerId(Message msg) {
        this.mFingerprintStatisticsMap.put(KEY_FINGER_ID, Integer.toString(msg.arg1));
        String str = this.fingerprintModule;
        if (str != null) {
            this.mFingerprintStatisticsMap.put(KEY_MODULE, str);
        } else {
            LogUtil.d("FingerprintService.DcsUtil", "DcsFingerprintStatisticsUtil fp_id = null");
        }
        String str2 = this.fingerprintModule;
        if (str2 != null && str2.startsWith("S_")) {
            if ("0".equals(SystemProperties.get("persist.vendor.silead_newalgo.support", "0"))) {
                this.mFingerprintStatisticsMap.put(KEY_MODULE_ALGORITHM, "8bit");
            } else if (NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("persist.vendor.silead_newalgo.support", "0"))) {
                this.mFingerprintStatisticsMap.put(KEY_MODULE_ALGORITHM, "16bit");
            } else if ("2".equals(SystemProperties.get("persist.vendor.silead_newalgo.support", "0"))) {
                this.mFingerprintStatisticsMap.put(KEY_MODULE_ALGORITHM, "16bit_v421");
            }
        }
        if (msg.arg1 == 0) {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_FINGER_ERROR);
        } else {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_FINGER_OK);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAcquiredInfo(Message msg) {
        this.mFingerprintStatisticsMap.put("packageName", (String) msg.obj);
        this.mFingerprintStatisticsMap.put(KEY_ACQUIRED_INFO, Integer.toString(msg.arg1));
        if (msg.arg1 == 5) {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_MOVE_FAST);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleImageInfo(Message msg) {
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_TYPE, Integer.toString(msg.arg1));
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_QUALITY, Integer.toString(msg.arg2));
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_SCORE, Integer.toString(((Integer) msg.obj).intValue()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDoubleHomeTimes(Message msg) {
        uploadDataToDcs(null, EVENT_DOUBLE_HOME);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSyncTemplateTimes(Message msg) {
        uploadDataToDcs(null, EVENT_SYNC_TEMPLATE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLockOut(Message msg) {
        sendCommonDcsUploader(DCS_LOG_TAG, EVENT_LOCK_OUT_TYPE, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAlipayEvent(Message msg) {
        int result = msg.arg2;
        int i = msg.arg1;
        if (i == 0) {
            this.mFingerprintStatisticsMap.put(KEY_ALIPAY_EVENT_ID, KEY_ALIPAY_ENROLL);
            if (result == 0) {
                this.mFingerprintStatisticsMap.put(KEY_ALIPAY_RESULT, KEY_ALIPAY_SUCCESS);
            } else if (result == 1) {
                this.mFingerprintStatisticsMap.put(KEY_ALIPAY_RESULT, KEY_ALIPAY_FAILED);
            }
        } else if (i == 1) {
            this.mFingerprintStatisticsMap.put(KEY_ALIPAY_EVENT_ID, KEY_ALIPAY_AUTHENTICATE);
            if (result == 0) {
                this.mFingerprintStatisticsMap.put(KEY_ALIPAY_RESULT, KEY_ALIPAY_SUCCESS);
            } else if (result == 1) {
                this.mFingerprintStatisticsMap.put(KEY_ALIPAY_RESULT, KEY_ALIPAY_FAILED);
            }
        }
        uploadAlipayDataToDcs(this.mFingerprintStatisticsMap, EVENT_ALIPAY_STATISTICAL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHealthTimeout(Message msg) {
        this.mFingerprintStatisticsMap.put("functionName", (String) msg.obj);
        uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_HEALTH_TIMEOUT);
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

    public void sendHealthTimeout(String funcName) {
        if (funcName != null) {
            this.mHandler.obtainMessage(9, 0, 0, funcName).sendToTarget();
        }
    }

    public void clearMap() {
        this.mHandler.obtainMessage(5, 0, 0, null).sendToTarget();
    }

    public void sendLockoutMode() {
        this.mHandler.obtainMessage(8, 0, 0, null).sendToTarget();
    }

    public void sendAlipayEvent(int alipayEventId, int alipayResult) {
        this.mHandler.obtainMessage(7, alipayEventId, alipayResult, null).sendToTarget();
    }

    private void uploadDataToDcs(Map<String, String> data, String eventId) {
        OppoStatistics.onCommon(this.mContext, SYSTEM_APP_TAG, eventId, data, false);
        handleClearMap();
    }

    private void uploadAlipayDataToDcs(Map<String, String> data, String eventId) {
        OppoStatistics.onCommon(this.mContext, DcsUtil.FACE_LOG_TAG, eventId, data, false);
        handleClearMap();
    }

    private void sendCommonDcsUploader(String logTag, String eventId, Map<String, String> data) {
        OppoStatistics.onCommon(this.mContext, logTag, eventId, data, false);
        handleClearMap();
    }

    public String readFileByLines(String fileName) {
        StringBuilder sb;
        BufferedReader reader = null;
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(new File(fileName)));
            tempString = reader.readLine();
            LogUtil.d("FingerprintService.DcsUtil", "readFileByLines tempString:" + tempString);
            try {
                reader.close();
            } catch (IOException e) {
                e1 = e;
                sb = new StringBuilder();
            }
        } catch (IOException e2) {
            LogUtil.e("FingerprintService.DcsUtil", "readFileByLines io exception:" + e2.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e1 = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    LogUtil.e("FingerprintService.DcsUtil", "readFileByLines io close exception :" + e1.getMessage());
                }
            }
            throw th;
        }
        return tempString;
        sb.append("readFileByLines io close exception :");
        sb.append(e1.getMessage());
        LogUtil.e("FingerprintService.DcsUtil", sb.toString());
        return tempString;
    }
}

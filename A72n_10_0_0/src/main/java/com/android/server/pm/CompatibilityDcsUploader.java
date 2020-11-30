package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompatibilityDcsUploader {
    private static boolean DEBUG_CPT_UP = CompatibilityHelper.DEBUG_CPT;
    private static final String EVENT_ID_ADD = "Cpt_Exception";
    private static final String LOG_TAG = "compatibility";
    private static final int MSG_ADD_EVENT = 1;
    private static final int MSG_DEBUG_LOG_SWITCH = 2;
    private static final String SPECIAL_EVENT_POINT_LOADING_WEBVIEW = "1000";
    private static final String TAG = "CompatibilityDcsUploader";
    private static CompatibilityDcsUploader sInstance = null;
    private Context mContext;
    private Handler mHandler;
    private PackageManager mPackageManager = this.mContext.getPackageManager();
    private HandlerThread mThread = new HandlerThread("CptSendDcs");

    private CompatibilityDcsUploader(Context context) {
        this.mContext = context;
        this.mThread.start();
        initHandler(this.mThread.getLooper());
    }

    public static CompatibilityDcsUploader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CompatibilityDcsUploader(context);
        }
        return sInstance;
    }

    public void sendToUploadCptTest() {
        Map<String, String> logMap = new ConcurrentHashMap<>();
        logMap.put("point", "test_packaege");
        logMap.put("Package", "test_version");
        logMap.put("version", "test_point");
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1, logMap));
    }

    public void sendToUploadCpt(PackageInfo pkgInfo, String point) {
        if (pkgInfo == null) {
            Slog.w(TAG, "sendToUploadCpt, NULL pkgInfo error!");
            return;
        }
        Map<String, String> logMap = new ConcurrentHashMap<>();
        logMap.put("point", point);
        logMap.put("Package", pkgInfo.packageName);
        logMap.put("version", pkgInfo.versionName);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1, logMap));
    }

    public void sendToUploadCpt(String data, String point) {
        String[] lines = data.split(",");
        Map<String, String> logMap = new ConcurrentHashMap<>();
        Slog.w(TAG, "sendToUploadCpt, point: " + point + " data: " + data);
        if (point.equals(SPECIAL_EVENT_POINT_LOADING_WEBVIEW)) {
            String callerPkg = lines[0];
            String webviewPkg = lines[1];
            String webviewVersion = lines[2];
            logMap.put("point", point);
            logMap.put("callerPackage", callerPkg);
            logMap.put("webviewPackage", webviewPkg);
            logMap.put("webviewVersion", webviewVersion);
        } else {
            try {
                PackageInfo pkgInfo = getPackageManager().getPackageInfoAsUser(lines[0], 0, -2);
                logMap.put("point", point);
                logMap.put("Package", pkgInfo.packageName);
                logMap.put("version", pkgInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Slog.w(TAG, "sendToUploadCpt, NULL pkgInfo error! " + e);
                return;
            }
        }
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1, logMap));
    }

    public void sendToDebugLogSwitch(boolean switchOn) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(2, Boolean.valueOf(switchOn)));
    }

    /* access modifiers changed from: package-private */
    public PackageManager getPackageManager() {
        if (this.mPackageManager == null) {
            this.mPackageManager = this.mContext.getPackageManager();
        }
        return this.mPackageManager;
    }

    private void uploadCptDcs(Map<String, String> msg, String eventId) {
        if (DEBUG_CPT_UP) {
            Slog.d(TAG, "uploadCptDcs, eventId: " + eventId + " UploadMsg: " + msg.toString());
        }
        try {
            Class.forName("oppo.util.OppoStatistics").getMethod("onCommon", Context.class, String.class, String.class, Map.class, Boolean.TYPE).invoke(null, this.mContext, LOG_TAG, eventId, msg, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAddEventMsg(Message msg) {
        if (!(msg.obj instanceof Map)) {
            Slog.e(TAG, "cpt upload data error!");
        } else {
            uploadCptDcs((Map) msg.obj, EVENT_ID_ADD);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLogSwitchMsg(Message msg) {
        DEBUG_CPT_UP = ((Boolean) msg.obj).booleanValue();
    }

    private void initHandler(Looper looper) {
        if (looper == null) {
            Slog.e(TAG, "can not get my looper!");
        } else {
            this.mHandler = new Handler(looper) {
                /* class com.android.server.pm.CompatibilityDcsUploader.AnonymousClass1 */

                public void handleMessage(Message msg) {
                    if (CompatibilityDcsUploader.DEBUG_CPT_UP) {
                        Slog.d(CompatibilityDcsUploader.TAG, " handleMsg, what: " + msg.what);
                    }
                    int i = msg.what;
                    if (i == 1) {
                        CompatibilityDcsUploader.this.handleAddEventMsg(msg);
                    } else if (i != 2) {
                        Slog.w(CompatibilityDcsUploader.TAG, "undefined cpt upload event!");
                    } else {
                        CompatibilityDcsUploader.this.handleLogSwitchMsg(msg);
                    }
                }
            };
        }
    }
}

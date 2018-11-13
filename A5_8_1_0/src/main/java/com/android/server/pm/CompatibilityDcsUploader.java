package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageInfo;
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
    private static final String TAG = "CompatibilityDcsUploader";
    private static CompatibilityDcsUploader sInstance = null;
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mThread = new HandlerThread("CptSendDcs");

    private class UploadMsg {
        public String pkgName;
        public String point;
        public String version;

        public UploadMsg(String pkg, String ver, String pt) {
            this.pkgName = pkg;
            this.version = ver;
            this.point = pt;
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("[ ");
            str.append(this.point);
            str.append(": [package: ");
            str.append(this.pkgName);
            str.append(" version: ");
            str.append(this.version);
            str.append(" ] ]");
            return str.toString();
        }
    }

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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, new UploadMsg("test_packaege", "test_version", "test_point")));
    }

    public void sendToUploadCpt(String pkgName, String version, String point) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, new UploadMsg(pkgName, version, point)));
    }

    public void sendToUploadCpt(PackageInfo pkgInfo, String point) {
        if (pkgInfo == null) {
            Slog.w(TAG, "sendToUploadCpt, NULL pkgInfo error!");
        } else {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, new UploadMsg(pkgInfo.packageName, pkgInfo.versionName, point)));
        }
    }

    public void sendToDebugLogSwitch(boolean switchOn) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Boolean.valueOf(switchOn)));
    }

    private void uploadCptDcs(UploadMsg msg, String eventId) {
        if (DEBUG_CPT_UP) {
            Slog.d(TAG, "uploadCptDcs, eventId: " + eventId + " UploadMsg: " + msg.toString());
        }
        Map logMap = new ConcurrentHashMap();
        logMap.put(msg.point, msg.toString());
        uploadCptDcs(logMap, eventId);
    }

    private void uploadCptDcs(Map<String, String> logMap, String eventId) {
        try {
            Class.forName("oppo.util.OppoStatistics").getMethod("onCommon", new Class[]{Context.class, String.class, String.class, Map.class, Boolean.TYPE}).invoke(this.mContext, new Object[]{LOG_TAG, eventId, logMap, Boolean.valueOf(true)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddEventMsg(Message msg) {
        if (msg.obj instanceof UploadMsg) {
            uploadCptDcs((UploadMsg) msg.obj, EVENT_ID_ADD);
        } else {
            Slog.e(TAG, "cpt upload data error!");
        }
    }

    private void handleLogSwitchMsg(Message msg) {
        DEBUG_CPT_UP = msg.obj.booleanValue();
    }

    private void initHandler(Looper looper) {
        if (looper == null) {
            Slog.e(TAG, "can not get my looper!");
        } else {
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message msg) {
                    if (CompatibilityDcsUploader.DEBUG_CPT_UP) {
                        Slog.d(CompatibilityDcsUploader.TAG, " handleMsg, what: " + msg.what);
                    }
                    switch (msg.what) {
                        case 1:
                            CompatibilityDcsUploader.this.handleAddEventMsg(msg);
                            return;
                        case 2:
                            CompatibilityDcsUploader.this.handleLogSwitchMsg(msg);
                            return;
                        default:
                            Slog.w(CompatibilityDcsUploader.TAG, "undefined cpt upload event!");
                            return;
                    }
                }
            };
        }
    }
}

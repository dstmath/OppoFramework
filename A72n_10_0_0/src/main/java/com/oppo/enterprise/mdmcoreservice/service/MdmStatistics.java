package com.oppo.enterprise.mdmcoreservice.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import com.oppo.statistics.NearMeStatistics;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MdmStatistics {
    private static String TAG = "MdmStat";
    private static volatile MdmStatistics sInstance;
    private final boolean DEBUG_DCS_UPLOADNOW = SystemProperties.getBoolean("persist.sys.oppo.mdm.dcs.uploadnow", false);
    private final boolean PRINT_DETAIL_LOG = SystemProperties.getBoolean("persist.sys.mdm.stat.detaillog", false);
    private int UPLOAD_ITEM_COUNT_LIMIT = SystemProperties.getInt("persist.sys.oppo.mdm.upload.itemlimit", 5000);
    private int UPLOAD_OVERTIME = SystemProperties.getInt("persist.sys.oppo.mdm.upload.overtime", 3660000);
    private final Runnable mCheckUploadRunnable = new Runnable() {
        /* class com.oppo.enterprise.mdmcoreservice.service.MdmStatistics.AnonymousClass1 */

        public void run() {
            MdmStatistics.this.DetailLog("mCheckUploadRunnable called");
            MdmStatistics.this.checkUpload(false);
            MdmStatistics.this.mHandler.postDelayed(this, (long) MdmStatistics.this.UPLOAD_OVERTIME);
        }
    };
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private long mLastUploadTime = SystemClock.uptimeMillis();
    private ArrayMap<String, MdmIterfaceStatItem> mMdmStatMap = new ArrayMap<>();

    public MdmStatistics() {
        this.mHandler.postDelayed(this.mCheckUploadRunnable, (long) this.UPLOAD_OVERTIME);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void DetailLog(String msg) {
        if (this.PRINT_DETAIL_LOG) {
            Log.i(TAG + "-Detail", msg);
        }
    }

    /* access modifiers changed from: private */
    public class MdmIterfaceStatItem {
        public int mCalledCount = 0;
        public int mPermissionExceptionCount = 0;
        public String mStrInterfaceName = null;

        MdmIterfaceStatItem() {
        }

        public String toString() {
            return "[mStrInterfaceName->" + this.mStrInterfaceName + ";mCalledCount->" + this.mCalledCount + ";mPermissionExceptionCount->" + this.mPermissionExceptionCount + "]";
        }
    }

    public static final MdmStatistics getInstance(Context cxt) {
        MdmStatistics mdmStatistics;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (MdmStatistics.class) {
            if (sInstance == null) {
                sInstance = new MdmStatistics();
                sInstance.mContext = cxt;
            }
            mdmStatistics = sInstance;
        }
        return mdmStatistics;
    }

    private static String getFileNameNoEx(String filename) {
        int dot;
        if (filename == null || filename.length() <= 0 || (dot = filename.lastIndexOf(46)) <= -1 || dot >= filename.length()) {
            return filename;
        }
        return filename.substring(0, dot);
    }

    private String getMdmMethodFromCurrentThreadStack() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            DetailLog("----> getLineNumber:" + st.getLineNumber() + " getMethodName:" + st.getMethodName() + " getFileName:" + st.getFileName() + " getClassName:" + st.getClassName() + " isNativeMethod:" + st.isNativeMethod());
            if (!st.isNativeMethod() && (st.getClassName().startsWith("com.oppo.enterprise.mdmcoreservice.service.managerimpl.") || st.getFileName().equals("com.oppo.enterprise.mdmcoreservice.service.OppoMdmManagerFactory"))) {
                return getFileNameNoEx(st.getFileName()) + "::" + st.getMethodName();
            }
        }
        return null;
    }

    private synchronized MdmIterfaceStatItem getMdmStatItem(String methodName) {
        if (this.mMdmStatMap.containsKey(methodName)) {
            return this.mMdmStatMap.get(methodName);
        }
        MdmIterfaceStatItem item = new MdmIterfaceStatItem();
        item.mStrInterfaceName = methodName;
        this.mMdmStatMap.put(methodName, item);
        return item;
    }

    public void recordPermissionException() {
        recordInferfacecSate(true);
    }

    public void recordInferfacecalled() {
        recordInferfacecSate(false);
    }

    public synchronized void recordInferfacecSate(boolean isException) {
        String strMethod = getMdmMethodFromCurrentThreadStack();
        if (strMethod != null) {
            DetailLog("recordInferfacecalled strMethod:" + strMethod + " isException:" + isException);
            if (isException || !strMethod.equals("OppoMdmManagerFactory::getManager")) {
                MdmIterfaceStatItem item = getMdmStatItem(strMethod);
                if (isException) {
                    item.mPermissionExceptionCount++;
                } else {
                    item.mCalledCount++;
                }
                if (!isException && IsUploadNowMethod(strMethod)) {
                    DetailLog("recordInferfacecSate checkUpload now");
                    checkUpload(true);
                }
            } else {
                DetailLog("OppoMdmManagerFactory::getManager called,skip");
            }
        }
    }

    private boolean isUploadOvertime() {
        return SystemClock.uptimeMillis() - this.mLastUploadTime > ((long) this.UPLOAD_OVERTIME);
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(System.currentTimeMillis()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void checkUpload(boolean bUpdateNow) {
        if ((isUploadOvertime() || bUpdateNow) && this.mMdmStatMap.size() > 0) {
            Map<String, String> logMap = new HashMap<>();
            boolean z = false;
            String strInterfaceStr = "";
            int i = 0;
            while (true) {
                if (i >= this.mMdmStatMap.size()) {
                    break;
                } else if (i > this.UPLOAD_ITEM_COUNT_LIMIT) {
                    break;
                } else {
                    strInterfaceStr = strInterfaceStr + this.mMdmStatMap.valueAt(i);
                    i++;
                }
            }
            logMap.put("mdm_interface", strInterfaceStr);
            logMap.put("upload_time", getCurrentTime());
            DetailLog("dcs upload log logMap:" + logMap);
            Log.i(TAG, "call NearMeStatistics.onCommon mMdmStatMap.size():" + this.mMdmStatMap.size() + " bUpdateNow:" + bUpdateNow);
            Context context = this.mContext;
            if (!bUpdateNow) {
                if (!this.DEBUG_DCS_UPLOADNOW) {
                    NearMeStatistics.onCommon(context, "20237", "MdmInterface", "mdm_interface_call", logMap, z);
                    this.mMdmStatMap.clear();
                    this.mLastUploadTime = SystemClock.uptimeMillis();
                }
            }
            z = true;
            NearMeStatistics.onCommon(context, "20237", "MdmInterface", "mdm_interface_call", logMap, z);
            this.mMdmStatMap.clear();
            this.mLastUploadTime = SystemClock.uptimeMillis();
        }
    }

    private boolean IsUploadNowMethod(String strMethod) {
        return strMethod.contains("rebootDevice") || strMethod.contains("shutdownDevice") || strMethod.contains("wipeDeviceData");
    }
}

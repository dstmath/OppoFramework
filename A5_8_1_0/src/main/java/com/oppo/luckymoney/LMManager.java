package com.oppo.luckymoney;

import android.app.ActivityThread;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.app.ILMServiceManager;
import com.android.internal.app.ILMServiceManager.Stub;
import com.android.internal.telephony.ITelephony;

public class LMManager {
    public static final String LUCKY_MONEY_SERVICE = "luckymoney";
    public static final String MM_PACKAGENAME = "com.tencent.mm";
    private static final int MOBILE_POLICY_LAST_TIME = 300;
    public static final String MODE_2_HB_HEIGHT = "hb_height";
    public static final String MODE_2_HB_WIDTH = "hb_width";
    public static final String MODE_2_HG_HASH = "hg_hash";
    public static final String MODE_2_RECEIVER_CLASS = "receiver_class";
    private static final int PER_PING_TIME = 8;
    public static final String QQ_PACKAGENAME = "com.tencent.mobileqq";
    private static final String TAG = "LMManager";
    public static final int TYPE_MODE_MM = 0;
    public static final int TYPE_MODE_NONE = -1;
    public static final int TYPE_MODE_QQ = 1;
    public static int sBoostMode = -1;
    public static boolean sGetHash = false;
    private static LMManager sLMManager = null;
    private static Handler sLastNewMsgTimeoutHandler;
    private static boolean sMODE2_NewMsgDetected = false;
    public static byte[] sMODE_2_VALUE_HB_HASH;
    public static int sMODE_2_VALUE_HB_HEIGHT = 0;
    public static int sMODE_2_VALUE_HB_WIDTH = 0;
    public static String sMODE_2_VALUE_RECEIVER_CLASS = "";
    private static Bundle sModeData = null;
    private static Runnable sNewMsgTimeout = new Runnable() {
        public void run() {
            LMManager.sMODE2_NewMsgDetected = false;
        }
    };
    private static ILMServiceManager sService = null;
    private static ITelephony sTelManager = null;

    public static boolean getNewMsgDetected() {
        return sMODE2_NewMsgDetected;
    }

    public static void setNewMsgDetected(Handler h, boolean value) {
        sMODE2_NewMsgDetected = value;
        if (h != null) {
            h.removeCallbacks(sNewMsgTimeout);
            sLastNewMsgTimeoutHandler = h;
            if (value) {
                h.postDelayed(sNewMsgTimeout, 500);
            }
        } else if (sLastNewMsgTimeoutHandler != null) {
            sLastNewMsgTimeoutHandler.removeCallbacks(sNewMsgTimeout);
        }
    }

    private LMManager() {
        init();
    }

    public static synchronized LMManager getLMManager() {
        LMManager lMManager;
        synchronized (LMManager.class) {
            if (sLMManager == null) {
                sLMManager = new LMManager();
            }
            lMManager = sLMManager;
        }
        return lMManager;
    }

    private void init() {
        if (sService == null) {
            sService = Stub.asInterface(ServiceManager.getService(LUCKY_MONEY_SERVICE));
        }
        if (sTelManager == null) {
            sTelManager = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        }
        if (sBoostMode >= 0) {
            return;
        }
        ApplicationInfo appInfo;
        if (MM_PACKAGENAME.equals(ActivityThread.currentPackageName())) {
            sBoostMode = 1;
            appInfo = ActivityThread.currentApplication().getApplicationInfo();
            if (appInfo != null) {
                getModeData(0, appInfo.versionCode);
            }
        } else if (QQ_PACKAGENAME.equals(ActivityThread.currentPackageName())) {
            appInfo = ActivityThread.currentApplication().getApplicationInfo();
            if (appInfo != null) {
                getModeData(1, appInfo.versionCode);
            }
        } else {
            sBoostMode = 0;
            sModeData = new Bundle();
        }
    }

    private void initQuickValue() {
        if (sModeData != null) {
            sBoostMode = sModeData.getInt("mode", 0);
            sGetHash = sModeData.getBoolean(LuckyMoneyHelper.MODE_2_GET_HASH_MODE, false);
            sMODE_2_VALUE_RECEIVER_CLASS = sModeData.getString(MODE_2_RECEIVER_CLASS, "");
            sMODE_2_VALUE_HB_HASH = sModeData.getByteArray("hg_hash");
            sMODE_2_VALUE_HB_WIDTH = Integer.valueOf(sModeData.getString(MODE_2_HB_WIDTH, "0")).intValue();
            sMODE_2_VALUE_HB_HEIGHT = Integer.valueOf(sModeData.getString(MODE_2_HB_HEIGHT, "0")).intValue();
        }
    }

    public int getBoostMode() {
        return sBoostMode;
    }

    public Bundle getModeData() {
        return sModeData;
    }

    public boolean isGetHash() {
        return sGetHash;
    }

    public byte[] getHBHash() {
        return sMODE_2_VALUE_HB_HASH;
    }

    public void enableBoost(int timeout, int code) {
        if (sService != null) {
            try {
                if (!sService.enableBoost(Process.myPid(), Process.myUid(), timeout, code)) {
                    return;
                }
                if (sTelManager == null || !SystemProperties.get("sys.oppo.nw.hongbao", "0").equals("1")) {
                    sTelManager = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
                    return;
                }
                Slog.d(TAG, "enableBoost");
                sTelManager.getClass().getMethod("startMobileDataHongbaoPolicy", new Class[]{Integer.TYPE, Integer.TYPE, String.class, String.class}).invoke(Integer.valueOf(300), new Object[]{Integer.valueOf(8), null, null});
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        init();
    }

    public String getLuckyMoneyInfo(int type) {
        String tmp = null;
        if (sService != null) {
            try {
                return sService.getLuckyMoneyInfo(type);
            } catch (RemoteException e) {
                e.printStackTrace();
                return tmp;
            }
        }
        init();
        return tmp;
    }

    public Bundle getModeData(int type, int versionCode) {
        if (sService != null) {
            try {
                sModeData = sService.getModeData(type, versionCode);
                initQuickValue();
                return sModeData;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Slog.e(TAG, "Can't get service.");
            return null;
        }
    }
}

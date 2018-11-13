package com.suntek.mway.rcs.client.api.support;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.android.vcard.VCardConfig;
import com.suntek.mway.rcs.client.aidl.constant.Constants.PluginConstants;
import com.suntek.mway.rcs.client.aidl.constant.Main;
import com.suntek.mway.rcs.client.api.log.LogHelper;

public class SupportApi {
    private static final int DMS_VERSION_UNKNOWN = -999;
    public static final int PLUGIN_CLOUD_FILE_SHARING = 4;
    public static final int PLUGIN_EMOTIONS_STORE = 5;
    public static final int PLUGIN_ENHANCE_CALL_SCREEN = 3;
    public static final int PLUGIN_PLUGIN_CENTER = 6;
    public static final int PLUGIN_PROFILE = 0;
    public static final int PLUGIN_PUBLIC_ACCOUNT = 1;
    public static final int PLUGIN_QR_CODE = 2;
    private static final String PROPERTY_NAME_DM_VERSION = "persist.sys.rcs.dm.version";
    private static final String PROPERTY_NAME_RCS_ENABLED = "persist.sys.rcs.enabled";
    private static final String TAG = "RCS_UI";
    private static SupportApi instance;
    private boolean mIsRcsServiceInstalled;

    private SupportApi() {
    }

    public static synchronized SupportApi getInstance() {
        SupportApi supportApi;
        synchronized (SupportApi.class) {
            if (instance == null) {
                instance = new SupportApi();
            }
            supportApi = instance;
        }
        return supportApi;
    }

    public boolean isServiceInstalled(Context context) {
        return isPackageInstalled(context, Main.PACKAGE_NAME);
    }

    public boolean isPluginInstalled(Context context) {
        return isPackageInstalled(context, PluginConstants.CONST_PLUGIN_PACKAGE_NAME);
    }

    public boolean isPluginCenterInstalled(Context context) {
        return isPackageInstalled(context, PluginConstants.CONST_PLUGIN_CENTER_PACKAGE_NAME);
    }

    public void startPluginCenterApp(Context context) throws RemoteException {
        Intent intent = new Intent();
        intent.addFlags(VCardConfig.FLAG_REFRAIN_QP_TO_NAME_PROPERTIES);
        intent.setAction("android.intent.action.VIEW");
        intent.setComponent(new ComponentName(PluginConstants.CONST_PLUGIN_CENTER_PACKAGE_NAME, PluginConstants.CONST_PLUGIN_CENTER_MAIN_ACTIVITY));
        context.startActivity(intent);
    }

    private boolean isPackageInstalled(Context context, String packageName) {
        for (ApplicationInfo info : context.getPackageManager().getInstalledApplications(8192)) {
            if (packageName.equals(info.packageName)) {
                return true;
            }
        }
        return false;
    }

    public void initApi(Context context) {
        this.mIsRcsServiceInstalled = isServiceInstalled(context);
    }

    public boolean isRcsSupported() {
        return (isRcsEnabled() && this.mIsRcsServiceInstalled) ? isSimAvailableForRcs() : false;
    }

    public boolean isRcsSupported(int slotId) {
        LogHelper.d("slotId:" + slotId);
        try {
            if (getDefaultDataSlotId() == slotId) {
                return isRcsSupported();
            }
            return false;
        } catch (Exception e) {
            LogHelper.e(e.getMessage(), e);
            return false;
        }
    }

    public int getDefaultDataSlotId() {
        int slotId = -1;
        try {
            Object dataSubId = getDefaultDataSubId();
            Class<?> subscriptionMgrClass = Class.forName("android.telephony.SubscriptionManager");
            if (dataSubId instanceof Long) {
                slotId = ((Integer) subscriptionMgrClass.getMethod("getSlotId", new Class[]{Long.TYPE}).invoke(subscriptionMgrClass, new Object[]{(Long) dataSubId})).intValue();
            } else if (dataSubId instanceof Integer) {
                slotId = ((Integer) subscriptionMgrClass.getMethod("getSlotId", new Class[]{Integer.TYPE}).invoke(subscriptionMgrClass, new Object[]{(Integer) dataSubId})).intValue();
            }
            LogHelper.d("slotId:" + slotId);
        } catch (Exception e) {
            LogHelper.e(e.getMessage(), e);
        }
        return slotId;
    }

    private Object getDefaultDataSubId() {
        Object obj = null;
        try {
            Class<?> subscriptionMgrClass = Class.forName("android.telephony.SubscriptionManager");
            obj = subscriptionMgrClass.getMethod("getDefaultDataSubId", new Class[0]).invoke(subscriptionMgrClass, new Object[0]);
            LogHelper.d("dataSubId:" + obj);
            return obj;
        } catch (Exception e) {
            LogHelper.e(e.getMessage(), e);
            return obj;
        }
    }

    private boolean isSimAvailableForRcs() {
        int dmVersion = getDmVersion();
        Log.d(TAG, "DM version is " + dmVersion);
        if (dmVersion > 0) {
            return true;
        }
        return false;
    }

    private int getDmVersion() {
        try {
            return SystemProperties.getInt(PROPERTY_NAME_DM_VERSION, -999);
        } catch (Exception e) {
            Log.w(TAG, "Failed getting DM version. " + e.getMessage());
            return -999;
        }
    }

    private boolean isRcsEnabled() {
        boolean isRcsEnabled = SystemProperties.getBoolean(PROPERTY_NAME_RCS_ENABLED, false);
        Log.d(TAG, "isRcsEnabled(): " + isRcsEnabled);
        return isRcsEnabled;
    }
}

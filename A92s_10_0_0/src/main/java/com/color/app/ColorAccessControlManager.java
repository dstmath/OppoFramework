package com.color.app;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import com.color.app.IColorAccessControlManager;
import com.color.content.ColorContext;
import java.util.List;
import java.util.Map;

public class ColorAccessControlManager {
    public static final String ACCESS_CONTROL_LOCK_ENABLED = "access_control_lock_enabled";
    public static final String ACCESS_CONTROL_LOCK_MODE = "access_control_lock_mode";
    public static final String ACCESS_CONTROL_PACKAGE_NAME = "Access_Control_Package_Name";
    public static final String ACCESS_CONTROL_PACKAGE_USERID = "Access_Control_Package_UserId";
    public static final int FLAG_HIDE_ICON = 1;
    public static final int FLAG_HIDE_IN_RECENT = 2;
    public static final int FLAG_HIDE_NOTICE = 4;
    public static final String LAUNCH_WINDOWING_MODE = "Launch_Windowing_Mode";
    public static final int MODE_EACH = 0;
    public static final int MODE_LOCK_SCREEN = 1;
    public static final int RUS_TYPE_FILTER = 0;
    public static final int RUS_TYPE_HIDE_KEYGUARD_LOCK = 1;
    public static final String SHOW_WHEN_LOCK = "show_when_lock";
    private static final String TAG = "ColorAccessControlManager";
    public static final String TASK_ID = "task_id";
    public static final int USER_CURRENT = UserHandle.myUserId();
    public static final int USER_XSPACE = 999;
    private static volatile ColorAccessControlManager sInstance = null;
    private final IColorAccessControlManager mService = IColorAccessControlManager.Stub.asInterface(ServiceManager.getService(ColorContext.ACCESS_CONTROL_SERVICE));

    private ColorAccessControlManager() {
    }

    public static ColorAccessControlManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorAccessControlManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorAccessControlManager();
                }
            }
        }
        return sInstance;
    }

    public void setPrivacyAppsInfoForUser(Map<String, Integer> privacyInfo, boolean enabled, int userId) {
        if (userId != 999) {
            try {
                userId = UserHandle.myUserId();
            } catch (RemoteException e) {
                throw new RuntimeException("setPrivacyAppsInfoForUser exception", e);
            }
        }
        this.mService.setPrivacyAppsInfoForUser(privacyInfo, enabled, userId);
    }

    public boolean getApplicationAccessControlEnabledAsUser(String packageName, int userId) {
        if (userId != 999) {
            try {
                userId = UserHandle.myUserId();
            } catch (RemoteException e) {
                return false;
            }
        }
        return this.mService.getApplicationAccessControlEnabledAsUser(packageName, userId);
    }

    public void addAccessControlPassForUser(String packageName, int windowMode, int userId) {
        if (userId != 999) {
            try {
                userId = UserHandle.myUserId();
            } catch (RemoteException e) {
                throw new RuntimeException("addAccessControlPassForUser exception", e);
            }
        }
        this.mService.addAccessControlPassForUser(packageName, windowMode, userId);
    }

    public void updateRusList(int type, List<String> addList, List<String> deleteList) {
        try {
            this.mService.updateRusList(type, addList, deleteList);
        } catch (RemoteException e) {
            throw new RuntimeException("updateRusList exception", e);
        }
    }

    public Map<String, Integer> getPrivacyAppInfo(int userId) {
        if (userId != 999) {
            try {
                userId = UserHandle.myUserId();
            } catch (RemoteException e) {
                throw new RuntimeException("accessControl manager has died", e);
            }
        }
        return this.mService.getPrivacyAppInfo(userId);
    }

    public boolean isAccessControlPassForUser(String packageName, int userId) {
        if (userId != 999) {
            try {
                userId = UserHandle.myUserId();
            } catch (RemoteException e) {
                throw new RuntimeException("isAccessControlPassForUser exception", e);
            }
        }
        return this.mService.isAccessControlPassForUser(packageName, userId);
    }

    public boolean registerAccessControlObserver(IColorAccessControlObserver observer) {
        try {
            return this.mService.registerAccessControlObserver(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "registerAccessControlObserver remoteException ");
            e.printStackTrace();
            return false;
        }
    }

    public boolean unregisterAccessControlObserver(IColorAccessControlObserver observer) {
        try {
            return this.mService.unregisterAccessControlObserver(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterAccessControlObserver remoteException ");
            e.printStackTrace();
            return false;
        }
    }
}

package cm.android.mdm.manager;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;
import cm.android.mdm.util.CustomizeServiceManager;
import java.util.Locale;

public class DeviceManager extends DeviceBaseManager {
    private static final String FACE_LOCK = "oppo_settings_manager_facelock";
    private static final String TAG = "DeviceManager";
    private static final String TIME = "oppo_settings_manager_time";
    private Context mContext;
    private WallpaperManager mWallpaperManager;

    public DeviceManager(Context context) {
        this.mContext = context;
        this.mWallpaperManager = WallpaperManager.getInstance(context);
    }

    public boolean setDeviceOwner(ComponentName componentName) {
        return CustomizeServiceManager.setDeviceOwner(componentName);
    }

    public boolean setActiveAdmin(ComponentName adminReceiver) {
        CustomizeServiceManager.setEmmAdmin(adminReceiver, true);
        return true;
    }

    public void enableAccessibilityService(ComponentName componentName) {
        CustomizeServiceManager.setAccessibilityEnabled(componentName, true);
    }

    public void allowGetUsageStats(String packageName) {
        CustomizeServiceManager.allowGetUsageStats(packageName);
    }

    public void shutdownDevice() {
        CustomizeServiceManager.deviceShutDown();
    }

    public void rebootDevice() {
        CustomizeServiceManager.deviceReboot();
    }

    public boolean isRooted() {
        return CustomizeServiceManager.isDeviceRoot();
    }

    public void turnOnGPS(boolean on) {
        Log.d(TAG, "turnOnGPS:" + on);
        if (on) {
            CustomizeServiceManager.openCloseGps(true);
        } else {
            CustomizeServiceManager.openCloseGps(false);
        }
    }

    public boolean isGPSTurnOn() {
        ContentResolver cr = this.mContext.getContentResolver();
        boolean gps = Secure.isLocationProviderEnabled(cr, "gps");
        boolean nlp = Secure.isLocationProviderEnabled(cr, "network");
        if (gps || nlp) {
            return true;
        }
        return false;
    }

    public void setTimeChangeDisabled(boolean disabled) {
        CustomizeServiceManager.setDB(TIME, disabled ? 1 : 0);
    }

    public void setFaceLockDisabled(boolean disabled) {
        CustomizeServiceManager.setDB(FACE_LOCK, disabled ? 1 : 0);
    }

    public void setLanguageChangeDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setProp("persist.sys.local_picker_dis", "true");
            try {
                IActivityManager am = ActivityManagerNative.getDefault();
                if (am != null) {
                    Configuration config = am.getConfiguration();
                    if (config.locale != Locale.SIMPLIFIED_CHINESE) {
                        config.locale = Locale.SIMPLIFIED_CHINESE;
                        config.userSetLocale = true;
                        CustomizeServiceManager.updateConfiguration(config);
                        return;
                    }
                    return;
                }
                return;
            } catch (RemoteException e) {
                Log.d(TAG, "set Language Change Disabled failed," + e);
                return;
            }
        }
        CustomizeServiceManager.setProp("persist.sys.local_picker_dis", "false");
    }

    public Bitmap captureScreen() {
        return CustomizeServiceManager.captureScreen();
    }

    public void setWallPaper(Bitmap bitmap) {
        try {
            this.mWallpaperManager.setBitmap(bitmap, null, false, 1);
        } catch (Exception e) {
            Log.d(TAG, "set wallpaper failed," + e);
        }
    }

    public void setLockWallPaper(Bitmap bitmap) {
        try {
            this.mWallpaperManager.setBitmap(bitmap, null, false, 2);
        } catch (Exception e) {
            Log.d(TAG, "set LockWallPaper failed," + e);
        }
    }
}

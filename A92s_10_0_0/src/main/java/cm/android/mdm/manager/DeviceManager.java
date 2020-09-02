package cm.android.mdm.manager;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import cm.android.mdm.util.CustomizeServiceManager;
import cm.android.mdm.util.defaultapp.apptype.Desktop;
import java.util.Locale;

public class DeviceManager extends DeviceBaseManager {
    private static final String FACE_LOCK = "oppo_settings_manager_facelock";
    private static final int START_NETWORK = 1;
    private static final int STOP_NETWORK = 2;
    private static final String TAG = "DeviceManager";
    private static final String TIME = "oppo_settings_manager_time";
    private static final String prefix = "persist.sys.oem_";
    private Context mContext;
    private ServiceHandler mServiceHandler;
    private TelephonyManager mTelephonyManager = TelephonyManager.getDefault();
    private WallpaperManager mWallpaperManager;

    public DeviceManager(Context context) {
        this.mContext = context;
        this.mWallpaperManager = WallpaperManager.getInstance(context);
        HandlerThread thread = new HandlerThread("CustomizeControler");
        thread.start();
        this.mServiceHandler = new ServiceHandler(thread.getLooper());
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public boolean setDeviceOwner(ComponentName componentName) {
        return CustomizeServiceManager.setDeviceOwner(componentName);
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public boolean setActiveAdmin(ComponentName adminReceiver) {
        CustomizeServiceManager.setEmmAdmin(adminReceiver, true);
        return true;
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void enableAccessibilityService(ComponentName componentName) {
        CustomizeServiceManager.setAccessibilityEnabled(componentName, true);
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void allowGetUsageStats(String packageName) {
        CustomizeServiceManager.allowGetUsageStats(packageName);
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void shutdownDevice() {
        CustomizeServiceManager.deviceShutDown();
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void rebootDevice() {
        CustomizeServiceManager.deviceReboot();
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public boolean isRooted() {
        return CustomizeServiceManager.isDeviceRoot();
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void turnOnGPS(boolean on) {
        Log.d(TAG, "turnOnGPS:" + on);
        if (on) {
            CustomizeServiceManager.openCloseGps(true);
        } else {
            CustomizeServiceManager.openCloseGps(false);
        }
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public boolean isGPSTurnOn() {
        ContentResolver cr = this.mContext.getContentResolver();
        boolean gps = Settings.Secure.isLocationProviderEnabled(cr, "gps");
        boolean nlp = Settings.Secure.isLocationProviderEnabled(cr, "network");
        if (gps || nlp) {
            return true;
        }
        return false;
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void setTimeChangeDisabled(boolean disabled) {
        CustomizeServiceManager.setDB(TIME, disabled ? 1 : 0);
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void setFaceLockDisabled(boolean disabled) {
        CustomizeServiceManager.setDB(FACE_LOCK, disabled ? 1 : 0);
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
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
                    }
                }
            } catch (RemoteException e) {
                Log.d(TAG, "set Language Change Disabled failed," + e);
            }
        } else {
            CustomizeServiceManager.setProp("persist.sys.local_picker_dis", "false");
        }
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public Bitmap captureScreen() {
        return CustomizeServiceManager.captureScreen();
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void setWallPaper(Bitmap bitmap) {
        try {
            this.mWallpaperManager.setBitmap(bitmap, null, false, START_NETWORK);
        } catch (Exception e) {
            Log.d(TAG, "set wallpaper failed," + e);
        }
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void setLockWallPaper(Bitmap bitmap) {
        try {
            this.mWallpaperManager.setBitmap(bitmap, null, false, STOP_NETWORK);
        } catch (Exception e) {
            Log.d(TAG, "set LockWallPaper failed," + e);
        }
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public boolean setDefaultLauncher(ComponentName home) {
        try {
            return new Desktop(this.mContext).setDefaultApp(home);
        } catch (Exception e) {
            Log.d(TAG, "set setDefaultLauncher failed," + e);
            return false;
        }
    }

    @Override // cm.android.mdm.manager.DeviceBaseManager, cm.android.mdm.interfaces.IDeviceManager
    public void clearDefaultLauncher() {
        try {
            new Desktop(this.mContext).setDefaultApp("com.oppo.launcher");
        } catch (Exception e) {
            Log.d(TAG, "set clearDefaultLauncher failed," + e);
        }
    }

    public void setMobileDataOn(boolean on) {
        Log.d(TAG, "setMobileDataOn on = " + on);
        if (!on) {
            Message msg = Message.obtain();
            msg.what = STOP_NETWORK;
            ServiceHandler serviceHandler = this.mServiceHandler;
            if (serviceHandler != null) {
                serviceHandler.sendMessage(msg);
                return;
            }
            return;
        }
        Message msg2 = Message.obtain();
        msg2.what = START_NETWORK;
        ServiceHandler serviceHandler2 = this.mServiceHandler;
        if (serviceHandler2 != null) {
            serviceHandler2.sendMessage(msg2);
        }
    }

    private static void propSetEnable(String prop, String defval) {
        try {
            Log.d(TAG, "propSetEnable " + prop + ": " + defval);
            CustomizeServiceManager.setProp(prop, defval);
        } catch (Exception ex) {
            Log.e(TAG, "setProp error :" + ex.getMessage());
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                int i = msg.what;
                if (i == DeviceManager.START_NETWORK) {
                    Log.d(DeviceManager.TAG, "START_NETWORK");
                    CustomizeServiceManager.setDataEnabled(true);
                } else if (i != DeviceManager.STOP_NETWORK) {
                    Log.w(DeviceManager.TAG, "what=" + msg.what);
                } else {
                    Log.d(DeviceManager.TAG, "STOP_NETWORK");
                    CustomizeServiceManager.setDataEnabled(false);
                }
            }
        }
    }

    public static boolean getMobileDataOn() {
        boolean dataStatus = false;
        TelephonyManager telephonyManager = TelephonyManager.getDefault();
        if (telephonyManager != null) {
            dataStatus = telephonyManager.getDataEnabled();
        }
        Log.d(TAG, "getMobileDataStatus" + dataStatus);
        return dataStatus;
    }

    private static boolean propGetEnable(String prop, String defval) {
        try {
            Log.d(TAG, "propGetEnable " + prop + ": " + defval);
            if (Integer.parseInt(SystemProperties.get(prop, defval)) != 0) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "getProp error :" + ex.getMessage());
            return true;
        }
    }
}

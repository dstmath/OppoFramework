package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.R;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;

public class DeviceSettingsManagerImpl extends IDeviceSettingsManager.Stub {
    private final Context mContext;
    private final NotificationCenterManager mNotificationCenterManager;

    public DeviceSettingsManagerImpl(Context context) {
        this.mNotificationCenterManager = NotificationCenterManager.getInstance(context);
        this.mContext = context;
        OppoCustomizeNotificationHelper.getInstance().init(context);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setTimeAndDateSetDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        boolean isSuccess = Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_time", disabled ? 1 : 0);
        if (disabled) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "auto_time", 1);
            Settings.Global.putInt(this.mContext.getContentResolver(), "auto_time_zone", 1);
            try {
                Intent intentStartService = new Intent();
                intentStartService.setComponent(new ComponentName("com.oppo.tzupdate", "com.oppo.tzupdate.timezone.AutoTimeZoneService"));
                this.mContext.startService(intentStartService);
            } catch (Exception e) {
                Log.e("DeviceSettingsManagerImpl", "setTimeAndDateSetDisabled start service error = " + e.getMessage());
            }
        }
        Log.d("DeviceSettingsManagerImpl", "setTimeAndDateSetDisabled disabled = " + disabled + ", isSuccess" + isSuccess);
        return isSuccess;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isTimeAndDateSetDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        boolean isDisabled = true;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_settings_manager_time", 0) != 1) {
            isDisabled = false;
        }
        Log.d("DeviceSettingsManagerImpl", "isTimeAndDateSetDisabled isDisabled = " + isDisabled);
        return isDisabled;
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setRestoreFactoryDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            ((UserManager) this.mContext.getSystemService("user")).setUserRestriction("no_factory_reset", disabled);
            Binder.restoreCallingIdentity(identify);
            Log.d("DeviceSettingsManagerImpl", "setRestoreFactoryDisabled isDisabled = " + disabled);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isRestoreFactoryDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            boolean result = ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_factory_reset");
            Binder.restoreCallingIdentity(identify);
            Log.d("DeviceSettingsManagerImpl", "isRestoreFactoryDisabled isDisabled = " + result);
            return result;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public String getAPIVersion(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.get("ro.build.custom.mdmsdk.version", "00000000");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public String getRomVersion(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        String romVersion = SystemProperties.get("ro.build.version.opporom");
        if (!TextUtils.isEmpty(romVersion) && !romVersion.equalsIgnoreCase("0")) {
            return romVersion;
        }
        if (!TextUtils.isEmpty(Build.VERSION.RELEASE)) {
            return Build.VERSION.RELEASE.toUpperCase();
        }
        Log.d("DeviceSettingsManagerImpl", "No ROM VERSION.");
        return romVersion;
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public void setTetherEnable(boolean isAllow) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            ((UserManager) this.mContext.getSystemService("user")).setUserRestriction("no_config_tethering", !isAllow);
            Binder.restoreCallingIdentity(identify);
            Log.d("DeviceSettingsManagerImpl", "setTetherEnable isAllow = " + isAllow);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean getTetherEnable() {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            boolean result = !((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_config_tethering");
            Binder.restoreCallingIdentity(identify);
            Log.d("DeviceSettingsManagerImpl", "getTetherEnable result = " + result);
            return result;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setDevelopmentOptionsDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceSettingsManagerImpl", "setDevelopmentOptionsDisabled disabled = " + disabled);
        SystemProperties.set("persist.sys.developer_disable", String.valueOf(disabled));
        setDevelopmentEnabled(disabled);
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isDeveloperOptionsDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        boolean disabled = SystemProperties.getBoolean("persist.sys.developer_disable", false);
        Log.d("DeviceSettingsManagerImpl", "isDeveloperOptionsDisabled disabled = " + disabled);
        return disabled;
    }

    private void setDevelopmentEnabled(boolean disable) {
        long identity = Binder.clearCallingIdentity();
        if (disable) {
            try {
                if (Settings.Global.getInt(this.mContext.getContentResolver(), "development_settings_enabled", 0) != 0) {
                    Settings.Global.putInt(this.mContext.getContentResolver(), "development_settings_enabled", 0);
                    Settings.Global.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
                    Intent intent = new Intent();
                    intent.setAction("com.oppo.action_dissable_development");
                    intent.setPackage("com.android.settings");
                    this.mContext.startService(intent);
                }
            } catch (Exception ex) {
                Log.d("DeviceSettingsManagerImpl", "setDevelopmentEnabled failed", ex);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            Settings.Global.putInt(this.mContext.getContentResolver(), "development_settings_enabled", 1);
        }
        ((UserManager) this.mContext.getSystemService("user")).setUserRestriction("no_debugging_features", disable);
        Binder.restoreCallingIdentity(identity);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setVolumeMuted(ComponentName componentName, boolean isMuted) {
        PermissionManager.getInstance().checkPermission();
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (audioManager == null) {
            return false;
        }
        audioManager.setParameters(isMuted ? "OPLUS_AUDIO_SET_MUTE_PHONE:true" : "OPLUS_AUDIO_SET_MUTE_PHONE:false");
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isVolumeMuted(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (audioManager == null) {
            return false;
        }
        String value = audioManager.getParameters("OPLUS_AUDIO_GET_MUTE_PHONE");
        if (TextUtils.isEmpty(value) || !value.equals("true")) {
            return false;
        }
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setFontSize(ComponentName componentName, int size) {
        float[] fontScales;
        int index;
        PermissionManager.getInstance().checkPermission();
        if (size == -1) {
            size = 2;
        }
        if (size < 1 || size > 5 || (fontScales = getFontScales()) == null || fontScales.length <= 0 || size - 1 >= fontScales.length) {
            return false;
        }
        float newFontScale = fontScales[index];
        long identity = Binder.clearCallingIdentity();
        Configuration configuration = this.mContext.getResources().getConfiguration();
        configuration.fontScale = newFontScale;
        try {
            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private float[] getFontScales() {
        int fontScalesResId;
        Context settingsContext = createPackageContext(this.mContext, "com.android.settings");
        float[] fontScales = null;
        String[] strFontScales = null;
        if (settingsContext != null && (fontScalesResId = getResFromPackage(settingsContext, "com.android.settings", "entryvalues_font_size", "array")) > 0) {
            strFontScales = settingsContext.getResources().getStringArray(fontScalesResId);
        }
        if (strFontScales == null) {
            strFontScales = this.mContext.getResources().getStringArray(R.array.entryvalues_font_size);
        }
        if (strFontScales != null && strFontScales.length > 0) {
            fontScales = new float[strFontScales.length];
            for (int i = 0; i < strFontScales.length; i++) {
                fontScales[i] = Float.parseFloat(strFontScales[i]);
            }
        }
        return fontScales;
    }

    private static int getResFromPackage(Context packageContext, String packageName, String resName, String resType) {
        if (packageContext != null) {
            return packageContext.getResources().getIdentifier(resName, resType, packageName);
        }
        return 0;
    }

    private static Context createPackageContext(Context srcContext, String packageName) {
        try {
            return srcContext.createPackageContext(packageName, 2);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setSearchIndexDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        boolean result = false;
        try {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_search", disabled ? 1 : 0);
            result = true;
        } catch (Exception e) {
            Log.e("DeviceSettingsManagerImpl", "setFloatTaskDisabled error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isSearchIndexDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        boolean isDisabled = false;
        try {
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_settings_manager_search", 0) == 1) {
                isDisabled = true;
            }
            return isDisabled;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean enableAllNotificationChannel(String packageName) {
        PermissionManager.getInstance().checkPermission();
        if (this.mContext == null) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return this.mNotificationCenterManager.enableAllNotificationChannel(packageName);
        } catch (Exception e) {
            Log.e("DeviceSettingsManagerImpl", "enableAllNotificationChannel error", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean disableAllNotificationChannel(String packageName) {
        PermissionManager.getInstance().checkPermission();
        if (this.mContext == null) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return this.mNotificationCenterManager.disableAllNotificationChannel(packageName);
        } catch (Exception e) {
            Log.e("DeviceSettingsManagerImpl", "disableAllNotificationChannel error", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean switchNotificationChannel(String packageName, String channelID, String manualType, boolean enabled) {
        PermissionManager.getInstance().checkPermission();
        if (this.mContext == null) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return this.mNotificationCenterManager.switchNotificationChannel(packageName, channelID, manualType, enabled);
        } catch (Exception e) {
            Log.e("DeviceSettingsManagerImpl", "switchNotificationChannel error", e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setVolumeChangeActionState(ComponentName admin, int mode) {
        PermissionManager.getInstance().checkPermission();
        Log.i("DeviceSettingsManagerImpl", "checkedPermission");
        switch (mode) {
            case 0:
                SystemProperties.set("persist.sys.volumechange.forbid", "0");
                return true;
            case 1:
                SystemProperties.set("persist.sys.volumechange.forbid", "1");
                return true;
            default:
                return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public int getVolumeChangeActionState(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        Log.i("DeviceSettingsManagerImpl", "checkedPermission");
        return SystemProperties.get("persist.sys.volumechange.forbid", "0").equals("1") ? 1 : 0;
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean turnOnProtectEyes(ComponentName componentName, boolean enabled) {
        PermissionManager.getInstance().checkPermission();
        if (this.mContext == null) {
            return false;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "color_eyeprotect_enable", enabled ? 1 : 0, -2);
            Binder.restoreCallingIdentity(identify);
            Log.d("DeviceSettingsManagerImpl", "turnOnProtectEyes isEnabled = " + enabled);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isProtectEyesOn(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        boolean result = false;
        if (this.mContext == null) {
            return false;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "color_eyeprotect_enable", 0, -2) == 1) {
                result = true;
            }
            Binder.restoreCallingIdentity(identify);
            Log.d("DeviceSettingsManagerImpl", "isProtectEyesOn isEnabled = " + result);
            return result;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setAutoScreenOffTime(ComponentName componentName, long millis) {
        int i;
        PermissionManager.getInstance().checkPermission();
        if (this.mContext == null) {
            return false;
        }
        long identify = Binder.clearCallingIdentity();
        if (millis == 0) {
            try {
                ContentResolver contentResolver = this.mContext.getContentResolver();
                if (isDefScreenOffTimeoutOneMinute(this.mContext)) {
                    i = 60000;
                } else {
                    i = 30000;
                }
                Settings.System.putInt(this.mContext.getContentResolver(), "screen_off_timeout", Settings.System.getInt(contentResolver, "last_manual_screen_off_timeout", i));
                SystemProperties.set("persist.sys.screen_off_time_set_policy", String.valueOf(false));
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identify);
                throw th;
            }
        } else {
            if (!(millis == 15000 || millis == 30000 || millis == 60000 || millis == 120000 || millis == 300000 || millis == 600000)) {
                if (millis != 1800000) {
                    throw new IllegalArgumentException("setAutoScreenOffTime() Wrong argument : " + millis);
                }
            }
            Settings.System.putInt(this.mContext.getContentResolver(), "screen_off_timeout", (int) millis);
            SystemProperties.set("persist.sys.screen_off_time_set_policy", String.valueOf(true));
        }
        Binder.restoreCallingIdentity(identify);
        Log.d("DeviceSettingsManagerImpl", "setAutoScreenOffTime millis = " + millis);
        return true;
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public long getAutoScreenOffTime(ComponentName componentName) {
        int i;
        PermissionManager.getInstance().checkPermission();
        if (this.mContext == null) {
            return 0;
        }
        long identify = Binder.clearCallingIdentity();
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (isDefScreenOffTimeoutOneMinute(this.mContext)) {
                i = 60000;
            } else {
                i = 30000;
            }
            long result = Settings.System.getLong(contentResolver, "screen_off_timeout", (long) i);
            Binder.restoreCallingIdentity(identify);
            Log.d("DeviceSettingsManagerImpl", "isProtectEyesOn isEnabled = " + result);
            return result;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identify);
            throw th;
        }
    }

    private boolean isDefScreenOffTimeoutOneMinute(Context context) {
        if (context != null) {
            return context.getPackageManager().hasSystemFeature("oppo.customize.screen.timeout.one_minute");
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setSIMLockDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        SystemProperties.set("persist.sys.settings.sim_lock.disable", disabled ? "1" : "0");
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isSIMLockDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.settings.sim_lock.disable", 0) == 1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setBackupRestoreDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        SystemProperties.set("persist.sys.settings.oppo_backup_restore.disable", disabled ? "1" : "0");
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isBackupRestoreDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.settings.oppo_backup_restore.disable", 0) == 1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setInterceptAllNotifications(boolean intercepted) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            return OppoCustomizeNotificationHelper.getInstance().setInterceptAllNotifications(intercepted);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setInterceptNonSystemNotifications(boolean intercepted) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            return OppoCustomizeNotificationHelper.getInstance().setInterceptNonSystemNotifications(intercepted);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean shouldInterceptAllNotifications() {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            return OppoCustomizeNotificationHelper.getInstance().shouldInterceptAllNotifications();
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean shouldInterceptNonSystemNotifications() {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            return OppoCustomizeNotificationHelper.getInstance().shouldInterceptNonSystemNotifications();
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean setPackageNotificationEnable(String pkgName, boolean isMultiApp, boolean enabled) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            return OppoCustomizeNotificationHelper.getInstance().setPackageNotificationEnable(pkgName, isMultiApp, enabled);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean isPackageNotificationEnable(String pkgName, boolean isMultiApp) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            return OppoCustomizeNotificationHelper.getInstance().isPackageNotificationEnable(pkgName, isMultiApp);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean updateNotificationChannel(String pkgName, boolean isMultiApp, String channelId, String switchType, boolean enabled) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            return OppoCustomizeNotificationHelper.getInstance().updateNotificationChannel(pkgName, isMultiApp, channelId, switchType, enabled);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceSettingsManager
    public boolean queryNotificationChannel(String pkgName, boolean isMultiApp, String channelId, String switchType) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        try {
            return OppoCustomizeNotificationHelper.getInstance().queryNotificationChannel(pkgName, isMultiApp, channelId, switchType);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }
}

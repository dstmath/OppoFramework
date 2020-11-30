package com.oppo.enterprise.mdmcoreservice.help;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.IOppoCustomizeService;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.Xml;
import com.oppo.enterprise.mdmcoreservice.service.managerimpl.DevicePhoneManagerImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class DeviceInitManagerHelper {
    public static final String PATH_CUSTOM_WHITELIST = SystemProperties.get("sys.custom.whitelist", "/system/etc/oppo_customize_whitelist.xml");
    private Context mContext;
    private IOppoCustomizeService mCustomService;
    private boolean mNoInitSate;
    private String mOtaVersion;
    private String mOtaVersionBackup;

    public DeviceInitManagerHelper(Context context) {
        this.mContext = context.getApplicationContext();
        loadInitState();
    }

    private IOppoCustomizeService getCustomizeService() {
        if (this.mCustomService != null) {
            return this.mCustomService;
        }
        this.mCustomService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        return this.mCustomService;
    }

    public boolean hasNoInit() {
        return this.mNoInitSate;
    }

    public void initProp() {
        setStatusBarDisable();
        setSystemUpdateDisable();
        setLauncherSlideSearchDisableMode();
        setSettingsSearchDisableMode();
        setWallpaperChangeDisableMode();
        setFloatAssistantDisableMode();
        setAirPlaneDisableMode();
        setGpsPoliciesMode();
        setOtgDisableMode();
        setSlotTwoDisableMode();
        setUsbDebugDisableMode();
        loadCustomWhitelist();
        saveInitState();
    }

    private void loadInitState() {
        this.mOtaVersion = SystemProperties.get("ro.build.custom.version.ota", "");
        this.mOtaVersionBackup = Settings.Secure.getString(this.mContext.getContentResolver(), "mdm.init.version.ota.backup");
        if (this.mOtaVersion.equals("") || this.mOtaVersion.equals(this.mOtaVersionBackup)) {
            LogHelper.d("DeviceInitManagerHelper", "LoadInitState mOtaVersion=" + this.mOtaVersion + " mOtaVersionBackup=" + this.mOtaVersionBackup);
            this.mNoInitSate = false;
        } else {
            this.mNoInitSate = true;
        }
        LogHelper.d("DeviceInitManagerHelper", "LoadInitState mNoInitSate=" + this.mNoInitSate);
    }

    private void saveInitState() {
        LogHelper.d("DeviceInitManagerHelper", "saveInitState");
        if (!this.mOtaVersion.equals(this.mOtaVersionBackup)) {
            Settings.Secure.putString(this.mContext.getContentResolver(), "mdm.init.version.ota.backup", this.mOtaVersion);
        }
    }

    private void setStatusBarDisable() {
        int statusBarDisableMode = SystemProperties.getInt("persist.sys.mdm_statusbar_expand_disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "setStatusBarDisable isStatusBarDisable=" + statusBarDisableMode);
        if (statusBarDisableMode != -1) {
            int i = 0;
            if (statusBarDisableMode == 0) {
                i = 1;
            }
            putSettingsSecureInt("statusbar_expand_disable", i);
        }
    }

    private void setSystemUpdateDisable() {
        int systemUpdateDisable = SystemProperties.getInt("persist.sys.mdm_system_update_disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "setSystemUpdateDisable systemUpdateDisable=" + systemUpdateDisable);
        if (systemUpdateDisable != -1) {
            int i = 0;
            if (!(systemUpdateDisable == 1)) {
                i = 1;
            }
            putSettingsSecureInt("ota_enable_config_custom", i);
        }
    }

    private void setLauncherSlideSearchDisableMode() {
        int slideSearchDisableMode = SystemProperties.getInt("persist.sys.mdm_launcher_slide_search_disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "checkLauncherSlideSearchDisable slideSearchDisableMode=" + slideSearchDisableMode);
        if (slideSearchDisableMode != -1) {
            int i = 0;
            if (slideSearchDisableMode == 1) {
                i = 1;
            }
            putSettingsSecureInt("launcher_slide_search_disable", i);
        }
    }

    private void setSettingsSearchDisableMode() {
        int settingsSearchDisableMode = SystemProperties.getInt("persist.sys.mdm_settings_search_disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "checkSettingsSearchDisable settingsSearchDisableMode=" + settingsSearchDisableMode);
        if (settingsSearchDisableMode != -1) {
            int i = 0;
            if (settingsSearchDisableMode == 1) {
                i = 1;
            }
            putSettingsSecureInt("oppo_settings_manager_search", i);
        }
    }

    private void setWallpaperChangeDisableMode() {
        int wallpaperChangeDisableMode = SystemProperties.getInt("persist.sys.mdm_wallpaper_change_disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "checkWallpaperChangeDisable wallpaperChangeDisableMode=" + wallpaperChangeDisableMode);
        if (wallpaperChangeDisableMode != -1) {
            int i = 0;
            if (wallpaperChangeDisableMode == 1) {
                i = 1;
            }
            putSettingsSecureInt("changeWallpaperDisabledState", i);
        }
    }

    private void setFloatAssistantDisableMode() {
        int floatAssistantDisableMode = SystemProperties.getInt("persist.sys.custom_float.disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "checkFloatAssistantDisableMode floatAssistantDisableMode=" + floatAssistantDisableMode);
        if (floatAssistantDisableMode != -1) {
            boolean isFloatAssistantDisable = true;
            if (floatAssistantDisableMode != 1) {
                isFloatAssistantDisable = false;
            }
            if (isFloatAssistantDisable) {
                putSettingsSystemInt("floating_ball_switch", 0);
            }
        }
    }

    private void setAirPlaneDisableMode() {
        int airPlaneDisableMode = SystemProperties.getInt("persist.sys.airplane_disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "checkAirPlaneDisableMode airPlaneDisableMode=" + airPlaneDisableMode);
        if (airPlaneDisableMode != -1) {
            boolean isAirPlaneDisable = true;
            if (airPlaneDisableMode != 1) {
                isAirPlaneDisable = false;
            }
            if (isAirPlaneDisable) {
                setSystemPropValue("persist.sys.airplane_clickable", "0");
                setSystemPropValue("persist.sys.airplane_grey", "1");
                setAirplaneMode(false);
                return;
            }
            setSystemPropValue("persist.sys.airplane_clickable", "-1");
            setSystemPropValue("persist.sys.airplane_grey", "0");
        }
    }

    private void setAirplaneMode(boolean on) {
        ConnectivityManager connectivityManager;
        if (this.mContext != null && (connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity")) != null) {
            Log.d("DeviceInitManagerHelper", "setAirplaneMode : on = " + on);
            connectivityManager.setAirplaneMode(on);
            setSystemPropValue("persist.sys.airplane_on", on ? "1" : "0");
        }
    }

    private void putSettingsSecureInt(String key, int value) {
        if (this.mContext != null) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), key, value);
        }
    }

    private void putSettingsSystemInt(String key, int value) {
        if (this.mContext != null) {
            Settings.System.putInt(this.mContext.getContentResolver(), key, value);
        }
    }

    private void putSettingsGlobalInt(String key, int value) {
        if (this.mContext != null) {
            Settings.Global.putInt(this.mContext.getContentResolver(), key, value);
        }
    }

    private boolean setSystemPropValue(String prop, String defval) {
        try {
            Log.d("DeviceInitManagerHelper", "setSystemPropValue " + prop + ": " + defval);
            SystemProperties.set(prop, defval);
            return true;
        } catch (Exception ex) {
            Log.e("DeviceInitManagerHelper", "setSystemPropValue error :" + ex.getMessage());
            return false;
        }
    }

    private void setGpsPoliciesMode() {
        int gpsPoliciesMode = SystemProperties.getInt("persist.sys.mdm_gps_mode", -1);
        LogHelper.d("DeviceInitManagerHelper", "checkGpsPoliciesMode gpsPoliciesMode=" + gpsPoliciesMode);
        if (gpsPoliciesMode == -1) {
            return;
        }
        if (gpsPoliciesMode == 0) {
            turnOnGPS(false);
            setSystemPropValue("persist.sys.gps_disable", String.valueOf(1));
            setSystemPropValue("persist.sys.gps_clickable", "0");
            setSystemPropValue("persist.sys.gps_grey", "1");
        } else if (gpsPoliciesMode == 1) {
            setSystemPropValue("persist.sys.gps_disable", String.valueOf(0));
            setSystemPropValue("persist.sys.gps_clickable", "0");
            setSystemPropValue("persist.sys.gps_grey", "0");
            turnOnGPS(true);
        } else if (gpsPoliciesMode == 2) {
            setSystemPropValue("persist.sys.gps_disable", String.valueOf(-1));
            setSystemPropValue("persist.sys.gps_clickable", "1");
            setSystemPropValue("persist.sys.gps_grey", "0");
        }
    }

    private void turnOnGPS(boolean on) {
        if (this.mContext != null) {
            UserHandle user = Process.myUserHandle();
            LocationManager manager = (LocationManager) this.mContext.getSystemService("location");
            if (manager == null) {
                Log.d("DeviceInitManagerHelper", "mLocationManager null");
            } else {
                manager.setLocationEnabledForUser(on, user);
            }
        }
    }

    private void setSlotTwoDisableMode() {
        getCustomizeService();
        int DisableMode = SystemProperties.getInt("persist.sys.oem_disable_slot_two", -1);
        if (DisableMode != -1) {
            if (DisableMode == 0) {
                int deactivateSubId = DevicePhoneManagerImpl.getSubId(1);
                Log.d("DeviceInitManagerHelper", "checkSlotTwoDisableMode deactivateSubId=" + deactivateSubId);
                try {
                    this.mCustomService.deactivateSubId(deactivateSubId);
                } catch (Exception e) {
                    Log.d("DeviceInitManagerHelper", "deactivateSubId:err!" + e);
                }
            } else {
                int activateSubId = DevicePhoneManagerImpl.getSubId(1);
                Log.d("DeviceInitManagerHelper", "checkSlotTwoDisableMode activateSubId=" + activateSubId);
                try {
                    this.mCustomService.activateSubId(activateSubId);
                } catch (Exception e2) {
                    Log.d("DeviceInitManagerHelper", "activateSubId:err!" + e2);
                }
            }
        }
    }

    private void setOtgDisableMode() {
        int otgDisableMode = SystemProperties.getInt("persist.sys.mdm_otg_disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "checkOtgDisableMode otgDisableMode=" + otgDisableMode);
        if (otgDisableMode != -1) {
            int i = 1;
            boolean isOtgDisable = otgDisableMode == 1;
            if (isOtgDisable) {
                setSystemPropValue("persist.vendor.otg.switch", Boolean.toString(false));
                putSettingsGlobalInt("sys.oppo.otg_support", 0);
            }
            if (isOtgDisable) {
                i = 0;
            }
            putSettingsSecureInt("OTG_ENABLED", i);
        }
    }

    private void setUsbDebugDisableMode() {
        int usbDisableMode = SystemProperties.getInt("persist.sys.usb_debugging_disable", -1);
        LogHelper.d("DeviceInitManagerHelper", "checkUsbDisableMode usbDisableMode=" + usbDisableMode);
        if (usbDisableMode != -1) {
            boolean isUsbDisable = usbDisableMode == 1;
            if (isUsbDisable) {
                putSettingsSecureInt("ZQ_ADB_ENABLED", 0);
                putSettingsSecureInt("adb_enabled", 0);
            } else {
                putSettingsSecureInt("ZQ_ADB_ENABLED", 1);
            }
            setSystemPropValue("persist.sys.usb_debugging_clickable", isUsbDisable ? "0" : "1");
        }
    }

    private void loadCustomWhitelist() {
        ArrayList<String> grayUninstall = loadWhiteListConfig("gray-uninstall");
        if (!grayUninstall.isEmpty()) {
            List<String> list = new ArrayList<>();
            Iterator<String> it = grayUninstall.iterator();
            while (it.hasNext()) {
                list.add(it.next());
            }
            if (!list.isEmpty()) {
                try {
                    getCustomizeService().addDisallowedUninstallPackages(list);
                } catch (Exception e) {
                    Log.w("DeviceInitManagerHelper", "addDisallowedUninstallPackages failed: ", e);
                }
            }
        }
    }

    public ArrayList<String> loadWhiteListConfig(String configTag) {
        int type;
        String tag;
        String value;
        Map<String, ArrayList<String>> whiteListConfigMap = new HashMap<>();
        ArrayList<String> ret = new ArrayList<>();
        File file = new File(PATH_CUSTOM_WHITELIST);
        if (!file.exists()) {
            Log.w("DeviceInitManagerHelper", "custom whitelist not exist!!!");
            return ret;
        }
        Log.d("DeviceInitManagerHelper", "load custom whitelist.");
        FileInputStream listFileInputStream = null;
        try {
            FileInputStream listFileInputStream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(listFileInputStream2, null);
            do {
                type = parser.next();
                if (type == 2 && (tag = parser.getName()) != null && !tag.isEmpty() && (value = parser.getAttributeValue(null, "att")) != null && !value.isEmpty()) {
                    ArrayList<String> tmpList = whiteListConfigMap.get(tag);
                    if (tmpList == null) {
                        tmpList = new ArrayList<>();
                        whiteListConfigMap.put(tag, tmpList);
                    }
                    Log.d("DeviceInitManagerHelper", "read: tag=" + tag + ", value=" + value);
                    tmpList.add(value);
                }
            } while (type != 1);
            ArrayList<String> list = whiteListConfigMap.get(configTag);
            if (list != null) {
                ret = list;
            }
            try {
                listFileInputStream2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            Log.w("DeviceInitManagerHelper", "parsing error: ", e2);
            if (0 != 0) {
                listFileInputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    listFileInputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
        return ret;
    }
}

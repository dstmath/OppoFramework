package com.android.server.location;

import android.content.Context;
import android.location.LocationManager;
import android.os.Binder;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.location.interfaces.IPswOppoGnssWhiteListProxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoGnssWhiteListProxy implements IPswOppoGnssWhiteListProxy {
    private static final String COMMAND_DELETE_AIDING_DATA = "delete_aiding_data";
    private static final String COMMAND_FORCE_PSDS_INJECTION = "force_psds_injection";
    private static final String COMMAND_FORCE_TIME_INJECTION = "force_time_injection";
    private static final String KEY_ACCESS_BACKGROUND_LOCATION_WHITELIST = "config_accessBackgroundLocationWhitelist";
    private static final String KEY_ACCESS_BACKGROUND_LOCATION_WHITELIST_ENABLED = "config_accessBackgroundLocationWhitelistEnabled";
    private static final String KEY_DELAIDINGDATA_WHITE_LIST = "config_delAidingDataWhitelist";
    private static final String KEY_DELAIDINGDATA_WHITE_LIST_ENABLED = "config_delAidingDataWhitelistEnabled";
    private static final String KEY_INJECT_TIME_WHITE_LIST = "config_injectTimeWhitelist";
    private static final String KEY_INJECT_TIME_WHITE_LIST_ENABLED = "config_injectTimeWhitelistEnabled";
    private static final String KEY_LOCATION_INTERACTIVE_ENABLED = "config_locationInteractiveEnabled";
    private static final String KEY_LOCATION_INTERACTIVE_LIST = "config_gpsBackgroudRunningApp";
    private static final String KEY_NETWORK_LOCATION_ALWAY_ON = "config_networklocationAlwayOn";
    private static final String KEY_NETWORK_LOCATION_WHITE_LIST = "config_networklocationWhitelist";
    public static final String LOGTAG_GPSLOCATION = "30101";
    private static final String SWITCH_OFF = "off";
    private static final String SWITCH_ON = "on";
    private static final String TAG = "OppoGnssWhiteList";
    public static final String USAGE_DEAL_EXTRA_COMMAND = "dealExtraCommand";
    private static OppoGnssWhiteListProxy mInstall = null;
    private static boolean mIsDebug = false;
    private ArrayList<String> mAccessBackgroundLocationWhitelist;
    private boolean mAccessBackgroundLocationWhitelistEnabled;
    private Context mContext;
    private ArrayList<String> mDelAidingDataWhitelist;
    private boolean mDelAidingDataWhitelistEnable;
    private ArrayList<String> mInjectTimeWhitelist;
    private boolean mInjectTimeWhitelistEnable;
    private boolean mIsCn;
    private boolean mIsNetworkLocationAlwayOn;
    private LocationManager mLocManager;
    private boolean mLocationInteractiveEnabled;
    private ArrayList<String> mLocationInteractiveList;
    private final Object mLock;
    private ArrayList<String> mNetworkLocationWhiteList;
    private OppoLbsRomUpdateUtil mRomUpdateUtil;

    public static OppoGnssWhiteListProxy getInstall(Context context) {
        Log.d(TAG, "on get OppoGnssWhiteListProxy!");
        if (mInstall == null) {
            mInstall = new OppoGnssWhiteListProxy(context);
        }
        return mInstall;
    }

    private OppoGnssWhiteListProxy(Context context) {
        this.mContext = null;
        this.mLock = new Object();
        this.mIsCn = true;
        this.mIsNetworkLocationAlwayOn = true;
        this.mNetworkLocationWhiteList = null;
        this.mDelAidingDataWhitelistEnable = true;
        this.mDelAidingDataWhitelist = null;
        this.mInjectTimeWhitelistEnable = true;
        this.mInjectTimeWhitelist = null;
        this.mLocationInteractiveEnabled = true;
        this.mLocationInteractiveList = null;
        this.mAccessBackgroundLocationWhitelistEnabled = true;
        this.mAccessBackgroundLocationWhitelist = null;
        this.mRomUpdateUtil = null;
        this.mLocManager = null;
        this.mIsCn = SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
        this.mContext = context;
        initValue(context);
    }

    private void initValue(Context context) {
        this.mRomUpdateUtil = OppoLbsRomUpdateUtil.getInstall(context);
        setNetworkLocationAlwayOn(this.mRomUpdateUtil.getString(KEY_NETWORK_LOCATION_ALWAY_ON));
        this.mNetworkLocationWhiteList = new ArrayList<>();
        this.mNetworkLocationWhiteList = this.mRomUpdateUtil.getStringArray(KEY_NETWORK_LOCATION_WHITE_LIST);
        this.mDelAidingDataWhitelistEnable = this.mRomUpdateUtil.getBoolean(KEY_DELAIDINGDATA_WHITE_LIST_ENABLED);
        this.mDelAidingDataWhitelist = new ArrayList<>();
        this.mDelAidingDataWhitelist = this.mRomUpdateUtil.getStringArray(KEY_DELAIDINGDATA_WHITE_LIST);
        this.mInjectTimeWhitelistEnable = this.mRomUpdateUtil.getBoolean(KEY_INJECT_TIME_WHITE_LIST_ENABLED);
        this.mInjectTimeWhitelist = new ArrayList<>();
        this.mInjectTimeWhitelist = this.mRomUpdateUtil.getStringArray(KEY_INJECT_TIME_WHITE_LIST);
        this.mLocationInteractiveEnabled = this.mRomUpdateUtil.getBoolean(KEY_LOCATION_INTERACTIVE_ENABLED);
        this.mLocationInteractiveList = new ArrayList<>();
        this.mLocationInteractiveList = this.mRomUpdateUtil.getStringArray(KEY_LOCATION_INTERACTIVE_LIST);
        this.mLocManager = (LocationManager) this.mContext.getSystemService("location");
        this.mAccessBackgroundLocationWhitelistEnabled = this.mRomUpdateUtil.getBoolean(KEY_ACCESS_BACKGROUND_LOCATION_WHITELIST_ENABLED);
        this.mAccessBackgroundLocationWhitelist = new ArrayList<>();
        this.mAccessBackgroundLocationWhitelist = this.mRomUpdateUtil.getStringArray(KEY_ACCESS_BACKGROUND_LOCATION_WHITELIST);
    }

    private void setNetworkLocationAlwayOn(String onOff) {
        if (onOff == null) {
            if (mIsDebug) {
                Log.w(TAG, "Get NetworkLocation Always On config fail from RomUpdateUtil");
            }
        } else if (onOff.equals(SWITCH_ON)) {
            this.mIsNetworkLocationAlwayOn = true;
        } else if (onOff.equals(SWITCH_OFF)) {
            this.mIsNetworkLocationAlwayOn = false;
        }
    }

    public boolean isNetworkUseablechanged(String providerName, boolean providerUsable) {
        if (!this.mIsNetworkLocationAlwayOn || !this.mIsCn || providerName == null || !providerName.equals("network")) {
            return providerUsable;
        }
        if (providerUsable || !mIsDebug) {
            return true;
        }
        Log.d(TAG, "--network allowed starting --");
        return true;
    }

    public static void setDebug(boolean debug) {
        mIsDebug = debug;
    }

    public boolean inNetworkLocationWhiteList(boolean providerUsable, String packageName) {
        if (!this.mIsNetworkLocationAlwayOn || !this.mIsCn || packageName == null || this.mNetworkLocationWhiteList == null) {
            return false;
        }
        if (!providerUsable && mIsDebug) {
            Log.d(TAG, "allow network " + packageName + ", " + this.mNetworkLocationWhiteList.contains(packageName));
        }
        return this.mNetworkLocationWhiteList.contains(packageName);
    }

    public boolean isAllowedPassLocationAccess(String packageName) {
        if (!this.mAccessBackgroundLocationWhitelistEnabled) {
            if (mIsDebug) {
                Log.w(TAG, "AccessBackgroundLocationWhitelistEnabled is " + this.mAccessBackgroundLocationWhitelistEnabled);
            }
            return false;
        } else if (this.mAccessBackgroundLocationWhitelist == null) {
            return false;
        } else {
            if (mIsDebug) {
                Log.d(TAG, "allow package " + packageName + "to ignore Access_Background_Location :" + this.mAccessBackgroundLocationWhitelist.contains(packageName));
            }
            return this.mAccessBackgroundLocationWhitelist.contains(packageName);
        }
    }

    private boolean collectExtraCommandData(String provider, String command, int uid, String[] packages) {
        if (uid <= 1000 || packages == null) {
            return false;
        }
        String packageNames = null;
        for (String name : packages) {
            if (packageNames == null) {
                packageNames = name;
            } else {
                packageNames = packageNames + ", " + name;
            }
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("provider", provider);
        map.put("command", command);
        map.put("packages", packageNames);
        OppoStatistics.onCommon(this.mContext, "30101", USAGE_DEAL_EXTRA_COMMAND, map, false);
        return true;
    }

    private boolean isCtsCase(String packageName) {
        if (packageName == null) {
            return false;
        }
        if (packageName.contains(".cts") || packageName.contains(".gts")) {
            return true;
        }
        return false;
    }

    public boolean isAllowedChangeChipData(String provider, String command) {
        int uid = Binder.getCallingUid();
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        collectExtraCommandData(provider, command, uid, packages);
        if (!"gps".equals(provider) || 1000 >= uid || packages == null) {
            return true;
        }
        ArrayList<String> packageList = null;
        if (COMMAND_DELETE_AIDING_DATA.equals(command) && this.mDelAidingDataWhitelistEnable) {
            packageList = this.mDelAidingDataWhitelist;
        } else if (COMMAND_FORCE_TIME_INJECTION.equals(command) && this.mInjectTimeWhitelistEnable) {
            packageList = this.mInjectTimeWhitelist;
        }
        if (packageList == null) {
            return true;
        }
        for (String pkg : packages) {
            if (packageList.contains(pkg) || isCtsCase(pkg)) {
                if (mIsDebug) {
                    Log.w(TAG, "Deleting aiding data is allowed ,package : " + pkg);
                }
                return true;
            }
        }
        return false;
    }

    public boolean isLocationInteractive() {
        List<String> inUseList;
        boolean isInteractive = false;
        if (this.mLocationInteractiveEnabled && this.mLocationInteractiveList != null && (inUseList = LocationManagerWrapper.getInUsePackagesList(this.mLocManager)) != null && inUseList.size() != 0) {
            Iterator<String> it = inUseList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String name = it.next();
                if (isCtsCase(name)) {
                    isInteractive = false;
                    if (mIsDebug) {
                        Log.w(TAG, "This is cts/gts app continue: " + name);
                    }
                } else if (this.mLocationInteractiveList.contains(name)) {
                    isInteractive = true;
                    if (mIsDebug) {
                        Log.w(TAG, "Ignore this idle deal : " + name);
                    }
                }
            }
        }
        return isInteractive;
    }
}

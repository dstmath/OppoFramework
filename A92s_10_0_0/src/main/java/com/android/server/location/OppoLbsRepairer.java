package com.android.server.location;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.location.interfaces.IPswLbsRepairer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OppoLbsRepairer implements IPswLbsRepairer {
    private static final String ENGINEER_PACKAGE_NAME = "com.oppo.engineermode";
    private static final int FOREGROUND_UI_IMPORTANCE = 100;
    private static final int GONE_UI_IMPORTANCE = 1000;
    private static final String SHOW_GMS_DIALOG = "config_showGmsDialogEnabled";
    private static final String TAG = "OppoLbsRepairer";
    private static OppoLbsRepairer mInstance = null;
    private ActivityManager mActivityManager;
    private Context mContext = null;
    /* access modifiers changed from: private */
    public LocationManager mLocationManager;
    private final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final ConcurrentHashMap<String, String> mMockPackages = new ConcurrentHashMap<>();
    private boolean mNeedShowGooglePermissionFlag = true;
    private OppoLbsRomUpdateUtil mOppoLbsRomUpdateUtil = null;
    /* access modifiers changed from: private */
    public PackageManager mPackageManager;
    @GuardedBy({"mLock"})
    private int mRec;

    public static OppoLbsRepairer getInstance(Context context) {
        Log.d(TAG, "on get OppoLbsRepairer!");
        if (mInstance == null) {
            mInstance = new OppoLbsRepairer(context);
        }
        return mInstance;
    }

    private void initManager() {
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mPackageManager = this.mContext.getPackageManager();
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mOppoLbsRomUpdateUtil = OppoLbsRomUpdateUtil.getInstall(this.mContext);
    }

    private OppoLbsRepairer(Context context) {
        this.mContext = context;
        this.mRec = 0;
        initManager();
        addMockAppDieListener();
    }

    private void addMockAppDieListener() {
        this.mActivityManager.addOnUidImportanceListener(new ActivityManager.OnUidImportanceListener() {
            /* class com.android.server.location.OppoLbsRepairer.AnonymousClass1 */

            public void onUidImportance(int uid, int importance) {
                String[] packages;
                if (importance == OppoLbsRepairer.GONE_UI_IMPORTANCE && (packages = OppoLbsRepairer.this.mPackageManager.getPackagesForUid(uid)) != null) {
                    for (String name : packages) {
                        for (Map.Entry<String, String> entry : OppoLbsRepairer.this.mMockPackages.entrySet()) {
                            if (name.equals(entry.getValue())) {
                                Log.e(OppoLbsRepairer.TAG, name + " die, will removeTestProvider:" + entry.getKey());
                                try {
                                    OppoLbsRepairer.this.mLocationManager.removeTestProvider(entry.getKey());
                                } catch (IllegalArgumentException e) {
                                    Log.e(OppoLbsRepairer.TAG, "failed to remove mock provider:" + entry.getKey());
                                    OppoLbsRepairer.this.mMockPackages.remove(entry.getKey());
                                }
                            }
                        }
                    }
                }
            }
        }, GONE_UI_IMPORTANCE);
    }

    public void onAddMockProvider(String packageName, String providerName) {
        Log.e(TAG, packageName + " will addMockProvider: " + providerName);
        this.mMockPackages.put(providerName, packageName);
    }

    public void onRemoveMockProvider(String packageName, String providerName) {
        Log.e(TAG, "onRemoveMockProvider, mMockPackages: " + this.mMockPackages);
        if (this.mMockPackages.containsKey(providerName)) {
            this.mMockPackages.remove(providerName);
        }
    }

    public void updateSettings(String name, int uid) {
        boolean isDomes = SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
        boolean isLocationEnabled = false;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, uid) != 0) {
            isLocationEnabled = true;
        }
        String allowedProviders = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", uid);
        if (isDomes && isLocationEnabled) {
            if ((name.equals("network") || name.equals("gps")) && !TextUtils.delimitedStringContains(allowedProviders, ',', name)) {
                Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", "+" + name, uid);
                this.mRec = this.mRec + 1;
                Log.d(TAG, "add " + name + " provider to Settings");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x008d, code lost:
        return false;
     */
    public boolean ignoreDisabled(String name, boolean enabled) {
        synchronized (this.mLock) {
            if (SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN") && ((name.equals("network") || name.equals("gps")) && !enabled)) {
                int uid = Binder.getCallingUid();
                String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
                if (packages == null) {
                    return false;
                }
                Log.e(TAG, name + " setEnable(false) from " + Arrays.toString(packages) + "(uid :" + uid + ")");
                int length = packages.length;
                for (int i = 0; i < length; i++) {
                    if (packages[i].equals("com.google.android.gms")) {
                        Log.w(TAG, "ignore gms setEnable(false) :" + name);
                        return true;
                    }
                }
            }
        }
    }

    public int getRec() {
        return this.mRec;
    }

    public void getProviderStatus(String providerName, boolean enable, boolean useable, boolean allowed, boolean forceShow, int uid) {
        boolean featureEnable = false;
        try {
            if (this.mOppoLbsRomUpdateUtil != null) {
                featureEnable = this.mOppoLbsRomUpdateUtil.getBoolean(SHOW_GMS_DIALOG);
            }
        } catch (Exception e) {
            featureEnable = false;
            e.printStackTrace();
        }
        if (featureEnable) {
            boolean wizardCompleteFlag = false;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, uid) != 0) {
                wizardCompleteFlag = true;
            }
            Log.e(TAG, "wizard complete:" + wizardCompleteFlag);
            if (wizardCompleteFlag && providerName != null && "network".equals(providerName)) {
                doGmsPermissionJudge(providerName, enable, useable, allowed, forceShow, uid);
            }
        }
    }

    public boolean isForegroundActivity(int uidImportance) {
        return uidImportance <= FOREGROUND_UI_IMPORTANCE;
    }

    private void doGmsPermissionJudge(String providerName, boolean enable, boolean useable, boolean allowed, boolean forceShow, int uid) {
        boolean isLocationEnabled = false;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, uid) != 0) {
            isLocationEnabled = true;
        }
        Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", uid);
        if (!useable && isLocationEnabled) {
            if (!enable) {
                if (!forceShow) {
                    if (this.mNeedShowGooglePermissionFlag) {
                        showGoogleNlpPermissionDialog(isEngineerForeground());
                    }
                    this.mNeedShowGooglePermissionFlag = true ^ this.mNeedShowGooglePermissionFlag;
                    return;
                }
                showGoogleNlpPermissionDialog(isEngineerForeground());
            } else if (!allowed) {
                Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", "+" + providerName, uid);
            }
        }
    }

    private void showGoogleNlpPermissionDialog(boolean inEngineerMode) {
        Log.d(TAG, "inEngineerMode:" + inEngineerMode);
        if (!inEngineerMode) {
            Intent tempIntent = new Intent();
            tempIntent.setPackage("com.google.android.gms");
            tempIntent.setComponent(new ComponentName("com.google.android.gms", "com.google.android.location.network.NetworkConsentActivity"));
            tempIntent.setFlags(268435456);
            try {
                this.mContext.startActivityAsUser(tempIntent, new UserHandle(-2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isEngineerForeground() {
        return isForegroundActivity(this.mActivityManager.getPackageImportance(ENGINEER_PACKAGE_NAME));
    }
}

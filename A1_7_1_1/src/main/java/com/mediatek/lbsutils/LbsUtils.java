package com.mediatek.lbsutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.ILocationManager.Stub;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest.Builder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoPermissionConstants;
import java.util.List;

public class LbsUtils {
    private static final boolean DEBUG = true;
    private static final String NETWORK_LOCATION_SERVICE_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    private static LbsUtils sInstance;
    private final ConnectivityManager mConnMgr;
    private Context mContext;
    private String[] mGmsLpPkgs;
    private Handler mGpsHandler = null;
    private boolean mInTestMode = false;
    private Object mLock = new Object();
    private String mMccMnc;
    private final NetworkCallback mNetworkConnectivityCallback = new NetworkCallback() {
        public void onAvailable(Network network) {
            if (LbsUtils.this.mWifiConnected == 0) {
                LbsUtils.this.mWifiConnected = 1;
                LbsUtils.log("wifi connected state changed: " + LbsUtils.this.mWifiConnected);
                LbsUtils.this.testModeConditionChanged();
            }
        }

        public void onLost(Network network) {
            if (LbsUtils.this.mWifiConnected == 1) {
                LbsUtils.this.mWifiConnected = 0;
                LbsUtils.log("wifi connected state changed: " + LbsUtils.this.mWifiConnected);
                LbsUtils.this.testModeConditionChanged();
            }
        }

        public void onUnavailable() {
            if (LbsUtils.this.mWifiConnected == 1) {
                LbsUtils.this.mWifiConnected = 0;
                LbsUtils.log("wifi connected state changed: " + LbsUtils.this.mWifiConnected);
                LbsUtils.this.testModeConditionChanged();
            }
        }
    };
    private PackageManager mPackageManager;
    private int mStayAwake = 0;
    private int mUsbPlugged = 0;
    private String[] mVendorLpPkgs;
    private String mVendorNlpPackageName;
    private int mWifiConnected = 0;

    public static LbsUtils getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LbsUtils(context);
        }
        return sInstance;
    }

    private LbsUtils(Context context) {
        log("LbsUtils constructor");
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public void setHandler(Handler handler) {
        this.mGpsHandler = handler;
    }

    public void listenPhoneState(String[] strArr) {
        log("listenPhoneState");
        this.mGmsLpPkgs = strArr;
        if (SystemProperties.get("persist.mtk_nlp_switch_support", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON).equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            registerAutoSwitchNlpFilter();
        }
    }

    public void setVendorLpPkgs(String[] strArr) {
        log("setVendorLpPkgs");
        this.mVendorLpPkgs = strArr;
    }

    private void registerAutoSwitchNlpFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SERVICE_STATE".equals(action)) {
                    LbsUtils.this.serviceStateChanged(intent);
                } else if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    int intExtra = intent.getIntExtra("plugged", 0);
                    if (intExtra != LbsUtils.this.mUsbPlugged) {
                        LbsUtils.log("usb plugged state changed: " + intExtra);
                        LbsUtils.this.mUsbPlugged = intExtra;
                        if (LbsUtils.this.mUsbPlugged == 0) {
                            LbsUtils.this.mInTestMode = false;
                        }
                        LbsUtils.this.testModeConditionChanged();
                    }
                }
            }
        }, intentFilter);
        Builder builder = new Builder();
        builder.addTransportType(1);
        this.mConnMgr.registerNetworkCallback(builder.build(), this.mNetworkConnectivityCallback);
        if (this.mGpsHandler != null) {
            try {
                this.mStayAwake = Global.getInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in");
            } catch (SettingNotFoundException e) {
                log("settings not found exception");
            }
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("stay_on_while_plugged_in"), true, new ContentObserver(this.mGpsHandler) {
                public void onChange(boolean z) {
                    try {
                        int i = Global.getInt(LbsUtils.this.mContext.getContentResolver(), "stay_on_while_plugged_in");
                        if (i != LbsUtils.this.mStayAwake) {
                            LbsUtils.log("Stay awake state changed: " + i);
                            LbsUtils.this.mStayAwake = i;
                            LbsUtils.this.testModeConditionChanged();
                        }
                    } catch (SettingNotFoundException e) {
                        LbsUtils.log("settings not found exception");
                    }
                }
            }, -1);
        }
    }

    private int getNetworkProviderCount() {
        List queryIntentServicesAsUser = this.mPackageManager.queryIntentServicesAsUser(new Intent(NETWORK_LOCATION_SERVICE_ACTION), 128, 0);
        if (queryIntentServicesAsUser == null) {
            log("installed NLP count= 0");
            return 0;
        }
        log("installed NLP count= " + queryIntentServicesAsUser.size());
        return queryIntentServicesAsUser.size();
    }

    private void serviceStateChanged(Intent intent) {
        Object obj = null;
        synchronized (this.mLock) {
            ServiceState newFromBundle = ServiceState.newFromBundle(intent.getExtras());
            if (newFromBundle != null) {
                obj = newFromBundle.getOperatorNumeric();
            }
            Object stringExtra = intent.getStringExtra("mccMnc");
            log("received action ACTION_SERVICE_STATE_CHANGED, mccMnc=" + obj + " testStr=" + stringExtra);
            if (!TextUtils.isEmpty(obj)) {
                this.mMccMnc = obj;
            } else if (!TextUtils.isEmpty(stringExtra)) {
                this.mMccMnc = stringExtra;
            }
            if (TextUtils.isEmpty(this.mMccMnc)) {
                log("Network mMccMnc is not yet set");
            } else {
                log("Network mMccMnc is set: " + this.mMccMnc);
                maybeRebindNetworkProvider();
            }
        }
    }

    private void testModeConditionChanged() {
        maybeRebindNetworkProvider();
    }

    private String getNetworkProviderPackage() {
        try {
            return Stub.asInterface(ServiceManager.getService("location")).getNetworkProviderPackage();
        } catch (RemoteException e) {
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:19:0x0031, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void maybeRebindNetworkProvider() {
        synchronized (this.mLock) {
            if (getNetworkProviderCount() >= 2) {
                String networkProviderPackage = getNetworkProviderPackage();
                if (networkProviderPackage == null) {
                    log("currently there is no NLP binded.");
                } else {
                    log("current NLP package name: " + networkProviderPackage);
                }
                boolean arrayContainsStr = arrayContainsStr(this.mGmsLpPkgs, networkProviderPackage);
                if (!arrayContainsStr) {
                    this.mVendorNlpPackageName = networkProviderPackage;
                }
                if (!(this.mUsbPlugged == 0 || this.mWifiConnected == 0 || this.mStayAwake == 0) || this.mInTestMode) {
                    log("current in test mode, mInTestMode=" + this.mInTestMode + " mVendorNlpPackageName=" + this.mVendorNlpPackageName);
                    this.mInTestMode = true;
                    resetPermissions();
                    if (!arrayContainsStr || networkProviderPackage == null) {
                        reBindNetworkProvider(true);
                    }
                } else if (this.mMccMnc != null && this.mMccMnc.startsWith("460")) {
                    if (arrayContainsStr || networkProviderPackage == null) {
                        reBindNetworkProvider(false);
                    }
                } else if (!arrayContainsStr || networkProviderPackage == null) {
                    reBindNetworkProvider(true);
                }
            }
        }
    }

    private void resetPermissions() {
        if (this.mVendorNlpPackageName != null) {
            resetPermissionGrant(this.mVendorNlpPackageName);
        } else if (this.mVendorLpPkgs != null) {
            for (String resetPermissionGrant : this.mVendorLpPkgs) {
                resetPermissionGrant(resetPermissionGrant);
            }
        }
    }

    private void resetPermissionGrant(String str) {
        if (str != null) {
            try {
                log("revokeRuntimePermission package: " + str);
                this.mPackageManager.revokeRuntimePermission(str, OppoPermissionConstants.PERMISSION_READ_PHONE_STATE, new UserHandle(0));
                this.mPackageManager.revokeRuntimePermission(str, "android.permission.ACCESS_COARSE_LOCATION", new UserHandle(0));
                this.mPackageManager.revokeRuntimePermission(str, OppoPermissionConstants.PERMISSION_ACCESS, new UserHandle(0));
                this.mPackageManager.revokeRuntimePermission(str, "android.permission.WRITE_EXTERNAL_STORAGE", new UserHandle(0));
                this.mPackageManager.revokeRuntimePermission(str, "android.permission.READ_EXTERNAL_STORAGE", new UserHandle(0));
            } catch (IllegalArgumentException e) {
                log("RevokeRuntimePermission IllegalArgumentException: " + str);
            } catch (SecurityException e2) {
                log("RevokeRuntimePermission SecurityException: " + str);
            }
        }
    }

    private boolean arrayContainsStr(String[] strArr, String str) {
        if (strArr != null) {
            for (String str2 : strArr) {
                if (str2 != null && str2.equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void reBindNetworkProvider(boolean z) {
        log("reBindNetworkProvider bindGmsPackage: " + z);
        String str = "com.mediatek.lbs.action.NLP_BIND_REQUEST";
        Intent intent = new Intent("com.mediatek.lbs.action.NLP_BIND_REQUEST");
        intent.putExtra("bindGmsPackage", z);
        this.mContext.sendBroadcast(intent);
    }

    public static void log(String str) {
        Log.d("LbsUtils", str);
    }
}

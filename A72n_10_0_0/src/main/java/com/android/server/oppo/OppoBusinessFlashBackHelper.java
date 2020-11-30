package com.android.server.oppo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.IOppoGromService;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.oppo.OppoCustomizeNotificationHelper;
import com.android.server.wm.OppoUsageManager;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class OppoBusinessFlashBackHelper {
    private static final String ACTION_MODE_LOCK = "com.oppo.intent.action.KEY_LOCK_MODE";
    private static final String BREENO_DISABLE_KEY = "breeno_disable_key";
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.business.debug", false);
    private static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private static final String EXTRA_KEY_LOCK_MODE = "KeyLockMode";
    private static final String EXTRA_PROCESS_NAME = "ProcessName";
    private static final String FILE_NAME = "b_flag.cfg";
    private static final int FLAG_BUSINESS = 1;
    private static final int FLAG_INVALID = 2;
    private static final int FLAG_OPEN_MARKET = 0;
    private static final String FLASH_BACK_FLAG_FILE = "/mnt/vendor/opporeserve/system/flag/b_flag.cfg";
    private static final String GROM_FEATURE = "oplus.software.customize.gromservice";
    private static final int LOCK_MODE = 4;
    private static final int OPPORESEVE2_TYPE_SYSTEM_FLAG = 1017;
    private static final int RESPONE_CODE_BUSINESS = 10200;
    private static final int RESPONE_CODE_ERROR = 10400;
    private static final int RESPONE_CODE_MARKET = 10204;
    private static final String SERIALNO = SystemProperties.get("ro.serialno", "00000000");
    private static final String SETTING_STATUS_SETTED = "navi_key_setted";
    private static final String SETTING_ZQ_ADB_ENABLED = "ZQ_ADB_ENABLED";
    private static final String SLIDE_BAR_DISABLED = "oppo_systemui_hide_edgepanel";
    private static final String STATUSBAR_EXPAND_KEY = "statusbar_expand_disable";
    private static final String TAG = "OppoBusinessFlashBackHelper";
    private static String mImei = null;
    private static boolean sCustomized = false;
    private static int sFlag = 2;
    private static volatile OppoBusinessFlashBackHelper sInstance;
    private static boolean sUpdated = false;
    String METADATA_CACHE_PATH = "/data/system/oppo_log1";
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private Object mLock = new Object();
    NetworkCallbackImpl mNetworkCallback;
    private boolean mNetworkTring = false;
    private ContentObserver mObserver;
    private OppoCustomizeService mOppoCustomizeService;
    Map<String, Object> mParameterMap = null;
    private boolean mSetupWizardCompleted;

    private OppoBusinessFlashBackHelper(Context context, OppoCustomizeService oppoCustomizeService) {
        this.mContext = context;
        this.mOppoCustomizeService = oppoCustomizeService;
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (!EXP_VERSION && this.mContext.getPackageManager().hasSystemFeature(GROM_FEATURE)) {
            initCustomFlashBackFuction();
        }
    }

    public static OppoBusinessFlashBackHelper getInstance(Context context, OppoCustomizeService oppoCustomizeService) {
        if (sInstance == null) {
            synchronized (OppoBusinessFlashBackHelper.class) {
                if (sInstance == null) {
                    sInstance = new OppoBusinessFlashBackHelper(context, oppoCustomizeService);
                }
            }
        }
        return sInstance;
    }

    private void initCustomFlashBackFuction() {
        Slog.d(TAG, "ota.version : " + SystemProperties.get("ro.build.version.ota", "property_not_found"));
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 1) {
            z = true;
        }
        this.mSetupWizardCompleted = z;
        if (!this.mSetupWizardCompleted) {
            registerSetupWizardCompletedOberserver();
        }
        resetNaviAndStatusBar();
        registerNetworkReceiver(this.mContext);
        Slog.d(TAG, "register network receiver...");
    }

    private String getAPPVersion(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerSetupWizardCompletedOberserver() {
        this.mObserver = new SetupWizardCompletedObserver();
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), true, this.mObserver);
    }

    /* access modifiers changed from: package-private */
    public class SetupWizardCompletedObserver extends ContentObserver {
        public SetupWizardCompletedObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            OppoBusinessFlashBackHelper oppoBusinessFlashBackHelper = OppoBusinessFlashBackHelper.this;
            boolean z = true;
            if (Settings.Global.getInt(oppoBusinessFlashBackHelper.mContext.getContentResolver(), "device_provisioned", 0) != 1) {
                z = false;
            }
            oppoBusinessFlashBackHelper.mSetupWizardCompleted = z;
            if (OppoBusinessFlashBackHelper.DEBUG) {
                Slog.d(OppoBusinessFlashBackHelper.TAG, "device_providsioned = " + Settings.Global.getInt(OppoBusinessFlashBackHelper.this.mContext.getContentResolver(), "device_provisioned", 0));
            }
            if (OppoBusinessFlashBackHelper.this.mSetupWizardCompleted) {
                synchronized (OppoBusinessFlashBackHelper.this.mLock) {
                    OppoBusinessFlashBackHelper.this.mLock.notify();
                }
                OppoBusinessFlashBackHelper.this.unregisterSetupWizardCompletedObserver();
            }
        }
    }

    private void resetNaviAndStatusBar() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), SETTING_STATUS_SETTED, 0) != 0) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), STATUSBAR_EXPAND_KEY, 0);
            Settings.Secure.putInt(this.mContext.getContentResolver(), SETTING_ZQ_ADB_ENABLED, 1);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "adb_enabled", 1);
            Settings.Secure.putInt(this.mContext.getContentResolver(), SLIDE_BAR_DISABLED, 1);
            Settings.Secure.putInt(this.mContext.getContentResolver(), SETTING_STATUS_SETTED, 0);
            Settings.Secure.putInt(this.mContext.getContentResolver(), BREENO_DISABLE_KEY, 0);
            SystemProperties.set("persist.sys.oem_ota", TemperatureProvider.SWITCH_OFF);
        }
    }

    private void registerBootCompletedReceiver() {
        if (DEBUG) {
            Slog.d(TAG, "--> registerBootCompletedReceiver");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(new BootCompletedReceiver(), filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterSetupWizardCompletedObserver() {
        if (DEBUG) {
            Slog.d(TAG, "unregister device_provisioned observer");
        }
        if (this.mObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        }
    }

    class BootCompletedReceiver extends BroadcastReceiver {
        BootCompletedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (OppoBusinessFlashBackHelper.DEBUG) {
                Slog.d(OppoBusinessFlashBackHelper.TAG, "--->" + intent);
            }
            int unused = OppoBusinessFlashBackHelper.sFlag = OppoBusinessFlashBackHelper.this.readBusinessFlag();
            if (!OppoBusinessFlashBackHelper.this.isBusinessVersion(context)) {
                int i = OppoBusinessFlashBackHelper.sFlag;
                if (i == 1 || i == 2) {
                    OppoBusinessFlashBackHelper.this.registerNetworkReceiver(context);
                }
            } else if (OppoBusinessFlashBackHelper.sFlag != 1) {
                int unused2 = OppoBusinessFlashBackHelper.sFlag = 1;
                OppoBusinessFlashBackHelper.this.writeBusinessFlag();
            }
        }
    }

    public int readBusinessFlag() {
        Exception e;
        int ret;
        String message = "";
        try {
            int length = OppoUsageManager.getFileSize(FLASH_BACK_FLAG_FILE);
            if (length > 0) {
                byte[] mes = OppoUsageManager.readOppoFile(FLASH_BACK_FLAG_FILE, 0, length);
                if (mes != null) {
                    message = new String(mes);
                }
                if (DEBUG) {
                    Slog.d(TAG, "readOppoFile /mnt/vendor/opporeserve/system/flag/b_flag.cfg message=" + mes + " strmes: " + message);
                }
            }
        } catch (Exception e2) {
            Slog.e(TAG, "getOppoFile error!", e2);
        }
        if (TextUtils.isEmpty(message)) {
            return 2;
        }
        try {
            ret = Integer.parseInt(String.valueOf(message.charAt(message.length() - 1)));
            try {
                if (DEBUG) {
                    Slog.d(TAG, "readBusinessFlag: " + message + " flag: " + ret);
                }
                return ret;
            } catch (Exception e3) {
                e = e3;
                Slog.e(TAG, "exception: " + e);
                return ret;
            }
        } catch (Exception e4) {
            e = e4;
            ret = 2;
            Slog.e(TAG, "exception: " + e);
            return ret;
        }
    }

    public void writeBusinessFlag() {
        String message = SERIALNO + getImei() + sFlag;
        byte[] mesByte = message.getBytes();
        try {
            OppoUsageManager.saveOppoFile(mesByte.length, FLASH_BACK_FLAG_FILE, 0, false, mesByte.length, mesByte);
            if (DEBUG) {
                Slog.d(TAG, "writeOppoFile /mnt/vendor/opporeserve/system/flag/b_flag.cfg message=" + message + " mesByte: " + mesByte);
            }
        } catch (Exception e) {
            Slog.e(TAG, "writeOppoFile error!", e);
        }
        if (DEBUG) {
            Slog.d(TAG, "writeBusinessFlag: " + message + "serial: " + SERIALNO + " flag: " + sFlag);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isBusinessVersion(Context context) {
        return context.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerNetworkReceiver(Context context) {
        if (this.mConnectivityManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "registerNetworkReceiver");
            }
            this.mNetworkCallback = new NetworkCallbackImpl();
            this.mConnectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), this.mNetworkCallback);
        }
    }

    private String getImei() {
        if (!TextUtils.isEmpty(mImei)) {
            return mImei;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (telephonyManager != null) {
                String imei = telephonyManager.getImei();
                if (!TextUtils.isEmpty(imei)) {
                    mImei = imei;
                }
            }
        } catch (Exception ex) {
            Slog.e(TAG, "getImei() Exception:" + ex.getMessage());
        }
        if (!TextUtils.isEmpty(mImei)) {
            return mImei;
        }
        return "";
    }

    /* access modifiers changed from: package-private */
    public class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
        NetworkCallbackImpl() {
        }

        public void onAvailable(Network network) {
            super.onAvailable(network);
            if (OppoBusinessFlashBackHelper.DEBUG) {
                Slog.d(OppoBusinessFlashBackHelper.TAG, "--->onAvailable");
            }
            int unused = OppoBusinessFlashBackHelper.sFlag = OppoBusinessFlashBackHelper.this.readBusinessFlag();
            OppoBusinessFlashBackHelper oppoBusinessFlashBackHelper = OppoBusinessFlashBackHelper.this;
            if (!oppoBusinessFlashBackHelper.isBusinessVersion(oppoBusinessFlashBackHelper.mContext)) {
                int i = OppoBusinessFlashBackHelper.sFlag;
                if (i == 0) {
                    OppoBusinessFlashBackHelper.this.unregisterNetworkCallback();
                } else if (i == 1 || i == 2) {
                    OppoBusinessFlashBackHelper oppoBusinessFlashBackHelper2 = OppoBusinessFlashBackHelper.this;
                    oppoBusinessFlashBackHelper2.getBusinessFlagFromServer(oppoBusinessFlashBackHelper2.mContext);
                }
            } else if (OppoBusinessFlashBackHelper.sFlag != 1) {
                int unused2 = OppoBusinessFlashBackHelper.sFlag = 1;
                OppoBusinessFlashBackHelper.this.writeBusinessFlag();
            }
            if (OppoBusinessFlashBackHelper.DEBUG) {
                Slog.d(OppoBusinessFlashBackHelper.TAG, "onAvailable");
            }
        }

        public void onLost(Network network) {
            super.onLost(network);
            if (OppoBusinessFlashBackHelper.DEBUG) {
                Slog.e(OppoBusinessFlashBackHelper.TAG, "onLost!");
            }
        }

        public void onUnavailable() {
            super.onUnavailable();
            Slog.e(OppoBusinessFlashBackHelper.TAG, "onUnavailable");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getBusinessFlagFromServer(final Context context) {
        if (!this.mNetworkTring) {
            this.mNetworkTring = true;
            if (this.mParameterMap == null) {
                this.mParameterMap = new HashMap();
                this.mParameterMap.put("imei", getImei().trim());
            }
            if (mImei == null) {
                getImei();
            }
            new Thread(new Runnable() {
                /* class com.android.server.oppo.OppoBusinessFlashBackHelper.AnonymousClass1 */
                private int mTimes = 3;

                public void run() {
                    synchronized (OppoBusinessFlashBackHelper.this.mLock) {
                        if (!OppoBusinessFlashBackHelper.this.mSetupWizardCompleted) {
                            if (OppoBusinessFlashBackHelper.DEBUG) {
                                Slog.d(OppoBusinessFlashBackHelper.TAG, "waiting for setup wizard completed.");
                            }
                            try {
                                OppoBusinessFlashBackHelper.this.mLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    while (true) {
                        int i = this.mTimes;
                        this.mTimes = i - 1;
                        if (i <= 0) {
                            break;
                        }
                        try {
                            IOppoGromService iOppoGromService = IOppoGromService.Stub.asInterface(ServiceManager.getService("oppo_grom"));
                            if (iOppoGromService == null) {
                                Slog.e(OppoBusinessFlashBackHelper.TAG, "get OppoGromService is null");
                                break;
                            }
                            int gromGetCode = iOppoGromService.gromGetCode(OppoBusinessFlashBackHelper.mImei);
                            if (gromGetCode == OppoBusinessFlashBackHelper.RESPONE_CODE_BUSINESS) {
                                if (iOppoGromService.gromGetCustomize(OppoBusinessFlashBackHelper.mImei)) {
                                    if (OppoBusinessFlashBackHelper.DEBUG) {
                                        Slog.d(OppoBusinessFlashBackHelper.TAG, "getBusinessFlagFromServer flag = 1!");
                                    }
                                    int unused = OppoBusinessFlashBackHelper.sFlag = 1;
                                } else {
                                    int unused2 = OppoBusinessFlashBackHelper.sFlag = 0;
                                }
                                OppoBusinessFlashBackHelper.this.writeBusinessFlag();
                                if (OppoBusinessFlashBackHelper.sFlag == 1 && iOppoGromService.gromGetUpdated(OppoBusinessFlashBackHelper.mImei)) {
                                    OppoBusinessFlashBackHelper.this.disableNaviAndStatuBar();
                                    OppoBusinessFlashBackHelper.this.startBusinessActivity(context);
                                }
                                OppoBusinessFlashBackHelper.this.unregisterNetworkCallback();
                            } else if (gromGetCode == OppoBusinessFlashBackHelper.RESPONE_CODE_MARKET) {
                                if (OppoBusinessFlashBackHelper.DEBUG) {
                                    Slog.d(OppoBusinessFlashBackHelper.TAG, "getBusinessFlagFromServer flag = 0!");
                                }
                                int unused3 = OppoBusinessFlashBackHelper.sFlag = 0;
                                OppoBusinessFlashBackHelper.this.writeBusinessFlag();
                                OppoBusinessFlashBackHelper.this.unregisterNetworkCallback();
                            } else if (gromGetCode != OppoBusinessFlashBackHelper.RESPONE_CODE_ERROR) {
                                Slog.e(OppoBusinessFlashBackHelper.TAG, "unkown grom code. message: " + iOppoGromService.getGromMsg());
                            } else {
                                Slog.e(OppoBusinessFlashBackHelper.TAG, "the param error! message " + iOppoGromService.getGromMsg());
                            }
                            Slog.d(OppoBusinessFlashBackHelper.TAG, "something wrong with request!");
                            try {
                                Thread.sleep(BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        } catch (Exception e2) {
                            Slog.e(OppoBusinessFlashBackHelper.TAG, "connect to server error! exception: " + e2);
                        }
                    }
                    OppoBusinessFlashBackHelper.this.mNetworkTring = false;
                }
            }).start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableNaviAndStatuBar() {
        Settings.Secure.putInt(this.mContext.getContentResolver(), STATUSBAR_EXPAND_KEY, 1);
        Settings.Secure.putInt(this.mContext.getContentResolver(), SETTING_ZQ_ADB_ENABLED, 0);
        Settings.Secure.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
        Settings.Secure.putInt(this.mContext.getContentResolver(), SLIDE_BAR_DISABLED, 0);
        setKeyLockMode();
        Settings.Secure.putInt(this.mContext.getContentResolver(), SETTING_STATUS_SETTED, 1);
        Settings.Secure.putInt(this.mContext.getContentResolver(), BREENO_DISABLE_KEY, 1);
        SystemProperties.set("persist.sys.oem_ota", TemperatureProvider.SWITCH_ON);
    }

    private void setKeyLockMode() {
        Intent intent = new Intent(ACTION_MODE_LOCK);
        intent.putExtra(EXTRA_KEY_LOCK_MODE, 4);
        intent.putExtra(EXTRA_PROCESS_NAME, this.mContext.getPackageName());
        this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startBusinessActivity(Context context) {
        if (DEBUG) {
            Slog.d(TAG, "--> startBusinessActivity");
        }
        addInitProtectApps();
        Intent intent = new Intent();
        intent.setAction("oppo.settings.WIFI_ENTERPRISE_CONNECTIVITY");
        intent.addFlags(268435456);
        context.startActivityAsUser(intent, UserHandle.SYSTEM);
    }

    private void addInitProtectApps() {
        try {
            Method methodAddInitProtectApps = this.mOppoCustomizeService.getClass().getDeclaredMethod("addInitProtectApps", new Class[0]);
            methodAddInitProtectApps.setAccessible(true);
            methodAddInitProtectApps.invoke(this.mOppoCustomizeService, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterNetworkCallback() {
        if (DEBUG) {
            Slog.d(TAG, "--> unregisterNetworkCallback");
        }
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
        }
    }

    public static void dump(PrintWriter fout) {
        fout.println("\nBUSINESS FLASH BACK:");
        fout.println("\tflag: " + sFlag);
        fout.println("\tcustomized: " + sCustomized);
        fout.println("\tupdated: " + sUpdated);
    }
}

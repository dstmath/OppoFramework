package com.mediatek.server.display;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.server.display.WifiDisplayController;
import com.mediatek.server.MtkSystemServiceFactory;
import com.mediatek.server.powerhal.PowerHalManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

public class MtkWifiDisplayController {
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;
    private static boolean DEBUG = true;
    private static final int RECONNECT_RETRY_DELAY_MILLIS = 1000;
    private static final int RESCAN_RETRY_DELAY_MILLIS = 2000;
    private static final String TAG = "MtkWifiDisplayController";
    private static final long WIFI_SCAN_TIMER = 100000;
    private static final String goIntent = SystemProperties.get("wfd.source.go_intent", String.valueOf(14));
    private int WFDCONTROLLER_DISPLAY_POWER_SAVING_DELAY;
    private int WFDCONTROLLER_DISPLAY_POWER_SAVING_OPTION;
    private int WFDCONTROLLER_DISPLAY_RESOLUTION;
    private AlarmManager mAlarmManager;
    private final Context mContext;
    private WifiDisplayController mController;
    private final Handler mHandler;
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.server.display.MtkWifiDisplayController.AnonymousClass2 */

        public void onChange(boolean selfChange, Uri uri) {
            if (!selfChange) {
                MtkWifiDisplayController.this.handleResolutionChange();
                MtkWifiDisplayController.this.handlePortraitResolutionSupportChange();
            }
        }
    };
    private PowerHalManager mPowerHalManager = MtkSystemServiceFactory.getInstance().makePowerHalManager();
    private int mPrevResolution;
    private final Runnable mReConnect = new Runnable() {
        /* class com.mediatek.server.display.MtkWifiDisplayController.AnonymousClass1 */

        public void run() {
            Iterator<WifiP2pDevice> it = MtkWifiDisplayController.this.mController.mAvailableWifiDisplayPeers.iterator();
            while (it.hasNext()) {
                WifiP2pDevice device = it.next();
                if (MtkWifiDisplayController.DEBUG) {
                    Slog.d(MtkWifiDisplayController.TAG, "\t" + MtkWifiDisplayController.describeWifiP2pDevice(device));
                }
                if (device.deviceAddress.equals(MtkWifiDisplayController.this.mReConnectDevice.deviceAddress)) {
                    Slog.i(MtkWifiDisplayController.TAG, "connect() in mReConnect. Set mReConnecting as true");
                    MtkWifiDisplayController.this.mReConnectDevice = null;
                    MtkWifiDisplayController.this.mController.requestConnect(device.deviceAddress);
                    return;
                }
            }
            MtkWifiDisplayController mtkWifiDisplayController = MtkWifiDisplayController.this;
            mtkWifiDisplayController.mReConnection_Timeout_Remain_Seconds--;
            if (MtkWifiDisplayController.this.mReConnection_Timeout_Remain_Seconds > 0) {
                Slog.i(MtkWifiDisplayController.TAG, "post mReconnect, s:" + MtkWifiDisplayController.this.mReConnection_Timeout_Remain_Seconds);
                MtkWifiDisplayController.this.mHandler.postDelayed(MtkWifiDisplayController.this.mReConnect, 1000);
                return;
            }
            Slog.e(MtkWifiDisplayController.TAG, "reconnect timeout!");
            Toast.makeText(MtkWifiDisplayController.this.mContext, MtkWifiDisplayController.this.getMtkStringResourceId("wifi_display_disconnected"), 0).show();
            MtkWifiDisplayController.this.mReConnectDevice = null;
            MtkWifiDisplayController.this.mReConnection_Timeout_Remain_Seconds = 0;
            MtkWifiDisplayController.this.mHandler.removeCallbacks(MtkWifiDisplayController.this.mReConnect);
        }
    };
    private WifiP2pDevice mReConnectDevice;
    private int mReConnection_Timeout_Remain_Seconds;
    private int mResolution;
    public boolean mStopWifiScan = false;
    private final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        /* class com.mediatek.server.display.MtkWifiDisplayController.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (MtkWifiDisplayController.this.mController.mConnectedDevice != null) {
                    NetworkInfo.State state = info.getState();
                    if (state == NetworkInfo.State.DISCONNECTED && MtkWifiDisplayController.this.mStopWifiScan) {
                        Slog.i(MtkWifiDisplayController.TAG, "Resume WiFi scan/reconnect if WiFi is disconnected");
                        MtkWifiDisplayController.this.stopWifiScan(false);
                        MtkWifiDisplayController.this.mAlarmManager.cancel(MtkWifiDisplayController.this.mWifiScanTimerListener);
                        MtkWifiDisplayController.this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + MtkWifiDisplayController.WIFI_SCAN_TIMER, "Set WiFi scan timer", MtkWifiDisplayController.this.mWifiScanTimerListener, MtkWifiDisplayController.this.mHandler);
                    } else if (state == NetworkInfo.State.CONNECTED && !MtkWifiDisplayController.this.mStopWifiScan) {
                        Slog.i(MtkWifiDisplayController.TAG, "Stop WiFi scan/reconnect if WiFi is connected");
                        MtkWifiDisplayController.this.mAlarmManager.cancel(MtkWifiDisplayController.this.mWifiScanTimerListener);
                        MtkWifiDisplayController.this.stopWifiScan(true);
                    }
                }
            }
        }
    };
    private final AlarmManager.OnAlarmListener mWifiScanTimerListener = new AlarmManager.OnAlarmListener() {
        /* class com.mediatek.server.display.MtkWifiDisplayController.AnonymousClass3 */

        public void onAlarm() {
            Slog.i(MtkWifiDisplayController.TAG, "Stop WiFi scan/reconnect due to scan timer timeout");
            MtkWifiDisplayController.this.stopWifiScan(true);
        }
    };

    public MtkWifiDisplayController(Context context, Handler handler, WifiDisplayController controller) {
        this.mContext = context;
        this.mHandler = handler;
        this.mController = controller;
        registerEMObserver();
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        context.registerReceiver(this.mWifiReceiver, intentFilter, null, this.mHandler);
    }

    public WifiP2pConfig overWriteConfig(WifiP2pConfig oldConfig) {
        WifiP2pConfig config = new WifiP2pConfig(oldConfig);
        Slog.i(TAG, "oldConfig:" + oldConfig);
        config.groupOwnerIntent = Integer.valueOf(goIntent).intValue();
        if (this.mController.mConnectingDevice.deviceName.contains("BRAVIA")) {
            Slog.i(TAG, "Force temporary group");
            config.netId = -1;
        }
        Slog.i(TAG, "config:" + config);
        stopWifiScan(true);
        return config;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0032 A[ADDED_TO_REGION] */
    private boolean isForce720p() {
        char c;
        String sPlatform = SystemProperties.get("ro.mediatek.platform", "");
        int hashCode = sPlatform.hashCode();
        if (hashCode != -2011283611) {
            if (hashCode == -2011283609 && sPlatform.equals("MT6765")) {
                c = 1;
                if (c != 0 || c == 1) {
                    Slog.d(TAG, "Platform (Force 720p): " + sPlatform);
                    return true;
                }
                Slog.d(TAG, "Platform: " + sPlatform);
                return false;
            }
        } else if (sPlatform.equals("MT6763")) {
            c = 0;
            if (c != 0) {
            }
            Slog.d(TAG, "Platform (Force 720p): " + sPlatform);
            return true;
        }
        c = 65535;
        if (c != 0) {
        }
        Slog.d(TAG, "Platform (Force 720p): " + sPlatform);
        return true;
    }

    public void setWFD(boolean enable) {
        Slog.d(TAG, "setWFD(), enable: " + enable);
        this.mPowerHalManager.setWFD(enable);
        stopWifiScan(enable);
    }

    private int getResolutionIndex(int settingValue) {
        if (settingValue != 0) {
            if (settingValue == 1 || settingValue == 2) {
                return 7;
            }
            if (settingValue != 3) {
                return 5;
            }
        }
        return 5;
    }

    /* access modifiers changed from: private */
    public static String describeWifiP2pDevice(WifiP2pDevice device) {
        return device != null ? device.toString().replace('\n', ',') : "null";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleResolutionChange() {
        int r = Settings.Global.getInt(this.mContext.getContentResolver(), getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_RESOLUTION"), 0);
        int i = this.mResolution;
        if (r != i) {
            this.mPrevResolution = i;
            this.mResolution = r;
            Slog.d(TAG, "handleResolutionChange(), resolution:" + this.mPrevResolution + "->" + this.mResolution);
            int idxModified = getResolutionIndex(this.mResolution);
            int idxOriginal = getResolutionIndex(this.mPrevResolution);
            if (idxModified != idxOriginal) {
                Slog.d(TAG, "index:" + idxOriginal + "->" + idxModified + ", doNotRemind:true");
                SystemProperties.set("vendor.media.wfd.video-format", String.valueOf(idxModified));
                if (this.mController.mConnectedDevice != null || this.mController.mConnectingDevice != null) {
                    Slog.d(TAG, "-- reconnect for resolution change --");
                    if (this.mController.mConnectedDevice != null) {
                        this.mReConnectDevice = this.mController.mConnectedDevice;
                    }
                    this.mController.requestDisconnect();
                }
            }
        }
    }

    public void checkReConnect() {
        if (this.mReConnectDevice != null) {
            Slog.i(TAG, "requestStartScan() for resolution change.");
            this.mController.requestStartScan();
            this.mReConnection_Timeout_Remain_Seconds = 30;
            this.mHandler.postDelayed(this.mReConnect, 1000);
        }
    }

    private void initPortraitResolutionSupport() {
        Settings.Global.putInt(this.mContext.getContentResolver(), getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_PORTRAIT_RESOLUTION"), 0);
        SystemProperties.set("vendor.media.wfd.portrait", String.valueOf(0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePortraitResolutionSupportChange() {
        int value = Settings.Global.getInt(this.mContext.getContentResolver(), getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_PORTRAIT_RESOLUTION"), 0);
        Slog.i(TAG, "handlePortraitResolutionSupportChange:" + value);
        SystemProperties.set("vendor.media.wfd.portrait", String.valueOf(value));
    }

    private void registerEMObserver() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(dm);
        int widthPixels = dm.widthPixels;
        int heightPixels = dm.heightPixels;
        this.WFDCONTROLLER_DISPLAY_RESOLUTION = this.mContext.getResources().getInteger(getMtkIntegerResourceId("wfd_display_default_resolution", -1));
        this.WFDCONTROLLER_DISPLAY_POWER_SAVING_OPTION = this.mContext.getResources().getInteger(getMtkIntegerResourceId("wfd_display_power_saving_option", 1));
        this.WFDCONTROLLER_DISPLAY_POWER_SAVING_DELAY = this.mContext.getResources().getInteger(getMtkIntegerResourceId("wfd_display_power_saving_delay", 10));
        Slog.d(TAG, "registerObserver() w:" + widthPixels + "h:" + heightPixels + "res:" + this.WFDCONTROLLER_DISPLAY_RESOLUTION + ",ps:" + this.WFDCONTROLLER_DISPLAY_POWER_SAVING_OPTION + ",psd:" + this.WFDCONTROLLER_DISPLAY_POWER_SAVING_DELAY);
        int r = Settings.Global.getInt(this.mContext.getContentResolver(), getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_RESOLUTION"), -1);
        if (r == -1) {
            boolean bForce = isForce720p();
            int i = this.WFDCONTROLLER_DISPLAY_RESOLUTION;
            if (i >= 0 && i <= 3) {
                this.mResolution = i;
                this.mPrevResolution = i;
            } else if (bForce) {
                this.mResolution = 0;
                this.mPrevResolution = 0;
            } else if (widthPixels < 1080 || heightPixels < 1920) {
                this.mResolution = 0;
                this.mPrevResolution = 0;
            } else {
                this.mResolution = 2;
                this.mPrevResolution = 2;
            }
        } else if (r < 0 || r > 3) {
            this.mResolution = 0;
            this.mPrevResolution = 0;
        } else {
            this.mResolution = r;
            this.mPrevResolution = r;
        }
        int resolutionIndex = getResolutionIndex(this.mResolution);
        Slog.i(TAG, "mResolution:" + this.mResolution + ", resolutionIndex: " + resolutionIndex);
        SystemProperties.set("vendor.media.wfd.video-format", String.valueOf(resolutionIndex));
        Settings.Global.putInt(this.mContext.getContentResolver(), getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_RESOLUTION"), this.mResolution);
        Settings.Global.putInt(this.mContext.getContentResolver(), getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_POWER_SAVING_OPTION"), this.WFDCONTROLLER_DISPLAY_POWER_SAVING_OPTION);
        Settings.Global.putInt(this.mContext.getContentResolver(), getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_POWER_SAVING_DELAY"), this.WFDCONTROLLER_DISPLAY_POWER_SAVING_DELAY);
        initPortraitResolutionSupport();
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_RESOLUTION")), false, this.mObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(getMtkSettingsExtGlobalSetting("WIFI_DISPLAY_PORTRAIT_RESOLUTION")), false, this.mObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getMtkStringResourceId(String name) {
        try {
            Field field = Class.forName("com.mediatek.internal.R$string", false, ClassLoader.getSystemClassLoader()).getField(name);
            field.setAccessible(true);
            return field.getInt(null);
        } catch (Exception e) {
            Slog.e(TAG, "Cannot get MTK resource - " + e);
            return 0;
        }
    }

    private String getMtkSettingsExtGlobalSetting(String name) {
        try {
            Class<?> rCls = Class.forName("com.mediatek.provider.MtkSettingsExt$Global", false, ClassLoader.getSystemClassLoader());
            Field field = rCls.getField(name);
            field.setAccessible(true);
            return (String) field.get(rCls);
        } catch (Exception e) {
            Slog.e(TAG, "Cannot get MTK settings - " + e);
            return "";
        }
    }

    private int getMtkIntegerResourceId(String name, int defaultVal) {
        try {
            Field field = Class.forName("com.mediatek.internal.R$integer", false, ClassLoader.getSystemClassLoader()).getField(name);
            field.setAccessible(true);
            return field.getInt(null);
        } catch (Exception e) {
            Slog.e(TAG, "Cannot get MTK resource - " + e);
            return defaultVal;
        }
    }

    private Handler getClientModeImplHandler(Object wifiInjector) {
        try {
            Method method = wifiInjector.getClass().getDeclaredMethod("getClientModeImplHandler", new Class[0]);
            method.setAccessible(true);
            return (Handler) method.invoke(wifiInjector, new Object[0]);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void stopWifiScan(boolean ifStop) {
        if (this.mStopWifiScan != ifStop) {
            Slog.i(TAG, "stopWifiScan()," + ifStop);
            try {
                Method method = Class.forName("com.android.server.wifi.WifiInjector", false, getClass().getClassLoader()).getDeclaredMethod("getInstance", new Class[0]);
                method.setAccessible(true);
                Object wi = method.invoke(null, new Object[0]);
                Method method2 = wi.getClass().getDeclaredMethod("getClientModeImpl", new Class[0]);
                method2.setAccessible(true);
                Object wsm = method2.invoke(wi, new Object[0]);
                Method method1 = wsm.getClass().getDeclaredMethod("enableWifiConnectivityManager", Boolean.TYPE);
                method1.setAccessible(true);
                Object[] objArr = new Object[1];
                objArr[0] = Boolean.valueOf(!ifStop);
                method1.invoke(wsm, objArr);
                if (!ifStop) {
                    Handler h = getClientModeImplHandler(wi);
                    Field fieldWcm = wsm.getClass().getDeclaredField("mWifiConnectivityManager");
                    fieldWcm.setAccessible(true);
                    Object wcm = fieldWcm.get(wsm);
                    Method methodScan = wcm.getClass().getDeclaredMethod("startPeriodicScan", Boolean.TYPE);
                    methodScan.setAccessible(true);
                    h.post(new Runnable(methodScan, wcm, ifStop) {
                        /* class com.mediatek.server.display.$$Lambda$MtkWifiDisplayController$rPL_xTBo08qw2lCIGUsU7FduRxw */
                        private final /* synthetic */ Method f$0;
                        private final /* synthetic */ Object f$1;
                        private final /* synthetic */ boolean f$2;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            MtkWifiDisplayController.lambda$stopWifiScan$0(this.f$0, this.f$1, this.f$2);
                        }
                    });
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            if (!ifStop) {
                this.mAlarmManager.cancel(this.mWifiScanTimerListener);
            }
            this.mStopWifiScan = ifStop;
        }
    }

    static /* synthetic */ void lambda$stopWifiScan$0(Method methodScan, Object wcm, boolean ifStop) {
        try {
            Slog.i(TAG, "reflection to startPeriodicScan");
            boolean z = true;
            Object[] objArr = new Object[1];
            if (ifStop) {
                z = false;
            }
            objArr[0] = Boolean.valueOf(z);
            methodScan.invoke(wcm, objArr);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}

package com.mediatek.amplus;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.hardware.display.WifiDisplayStatus;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.util.Slog;
import com.mediatek.common.jpe.a;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PowerSavingUtils {
    private static final String FILEPATH = "/system/etc/alarmplus.config";
    private static final long MIN_FUZZABLE_INTERVAL = 10000;
    private static final int NEW_POWER_SAVING_MODE = 2;
    private static final long SCREENOFF_TIME_INTERVAL_THRESHOLD = 300000;
    private static final String TAG = "AlarmManager";
    private final Context mContext;
    private boolean mIsEnabled = false;
    private boolean mIsUsbConnected = false;
    private boolean mIsWFDConnected = false;
    private PowerSavingEnableObserver mPowerSavingEnableObserver = null;
    private PowerSavingReceiver mPowerSavingReceiver;
    private int mSavingMode = 0;
    private boolean mScreenOff = false;
    private long mScreenOffTime = 0;
    final ArrayList<String> mWhitelist = new ArrayList();

    class PowerSavingEnableObserver extends ContentObserver {
        PowerSavingEnableObserver(Handler handler) {
            super(handler);
            observe();
        }

        void observe() {
            PowerSavingUtils.this.mContext.getContentResolver().registerContentObserver(System.getUriFor("background_power_saving_enable"), false, this, -1);
            PowerSavingUtils.this.setPowerSavingEnable();
        }

        public void onChange(boolean z) {
            PowerSavingUtils.this.setPowerSavingEnable();
        }
    }

    class PowerSavingReceiver extends BroadcastReceiver {
        public PowerSavingReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.hardware.usb.action.USB_STATE");
            intentFilter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
            intentFilter.addAction("android.intent.action.TIME_TICK");
            PowerSavingUtils.this.mContext.registerReceiver(this, intentFilter);
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                PowerSavingUtils.this.mScreenOff = true;
                PowerSavingUtils.this.mScreenOffTime = System.currentTimeMillis();
            } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                PowerSavingUtils.this.mScreenOff = false;
                PowerSavingUtils.this.mScreenOffTime = 0;
            } else if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction())) {
                PowerSavingUtils.this.mIsUsbConnected = intent.getBooleanExtra("connected", false);
            } else if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(intent.getAction())) {
                boolean z;
                WifiDisplayStatus wifiDisplayStatus = (WifiDisplayStatus) intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS");
                PowerSavingUtils powerSavingUtils = PowerSavingUtils.this;
                if (2 != wifiDisplayStatus.getActiveDisplayState()) {
                    z = false;
                } else {
                    z = true;
                }
                powerSavingUtils.mIsWFDConnected = z;
                Slog.v(PowerSavingUtils.TAG, "PowerSavingReceiver mIsWFDConnected = " + PowerSavingUtils.this.mIsWFDConnected);
            } else if ("android.intent.action.TIME_TICK".equals(intent.getAction())) {
                Slog.v(PowerSavingUtils.TAG, "isPowerSavingStart  mIsEnabled = " + PowerSavingUtils.this.mIsEnabled + "   mIsUsbConnected = " + PowerSavingUtils.this.mIsUsbConnected + "   mScreenOff = " + PowerSavingUtils.this.mScreenOff + "   mIsWFDConnected = " + PowerSavingUtils.this.mIsWFDConnected);
            }
        }
    }

    public PowerSavingUtils(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        new a().a();
        readList();
        this.mPowerSavingReceiver = new PowerSavingReceiver();
        this.mPowerSavingEnableObserver = new PowerSavingEnableObserver(null);
    }

    private void readList() {
        File file = new File(FILEPATH);
        if (file.exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                for (Object readLine = bufferedReader.readLine(); readLine != null; readLine = bufferedReader.readLine()) {
                    this.mWhitelist.add(readLine);
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX WARNING: Missing block: B:37:0x00d9, code:
            if (r3.startsWith("android") == false) goto L_0x001f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isAlarmNeedAlign(int i, PendingIntent pendingIntent, boolean z) {
        boolean z2 = false;
        if (isPowerSavingStart() && (this.mSavingMode == 2 || i == 0 || i == 2)) {
            if (pendingIntent != null) {
                String targetPackage = pendingIntent.getTargetPackage();
                if (targetPackage != null) {
                    for (int i2 = 0; i2 < this.mWhitelist.size(); i2++) {
                        if (((String) this.mWhitelist.get(i2)).equals(targetPackage)) {
                            Slog.v(TAG, "isAlarmNeedAlign : packageName = " + targetPackage + "is in whitelist");
                            return false;
                        }
                    }
                    if (z) {
                        PackageManager packageManager = this.mContext.getPackageManager();
                        try {
                            long clearCallingIdentity = Binder.clearCallingIdentity();
                            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(targetPackage, 0);
                            Binder.restoreCallingIdentity(clearCallingIdentity);
                            if ((applicationInfo.flags & 1) != 0) {
                                if (!targetPackage.startsWith("com.android")) {
                                    if (!targetPackage.startsWith("com.google.android.deskclock")) {
                                    }
                                }
                                if (Build.TYPE.equals("eng")) {
                                    Slog.v(TAG, "isAlarmNeedAlign : " + targetPackage + " skip!");
                                }
                            }
                        } catch (NameNotFoundException e) {
                            Slog.v(TAG, "isAlarmNeedAlign : packageName not fount");
                        }
                    }
                    z2 = true;
                } else {
                    Slog.v(TAG, "isAlarmNeedAlign : packageName is null");
                }
            } else {
                Slog.v(TAG, "isAlarmNeedAlign : operation is null");
            }
        }
        return z2;
    }

    private long getMTKMaxTriggerTime(int i, PendingIntent pendingIntent, long j, boolean z) {
        if (!z) {
            return j;
        }
        if (isAlarmNeedAlign(i, pendingIntent, true) && z) {
            return 300000 + j;
        }
        return 0 - j;
    }

    public boolean isPowerSavingStart() {
        if (!this.mIsEnabled || this.mIsUsbConnected || this.mIsWFDConnected || !this.mScreenOff) {
            return false;
        }
        long j;
        boolean z;
        long currentTimeMillis = System.currentTimeMillis();
        if (this.mSavingMode == 2) {
            j = 60000;
        } else {
            j = 300000;
        }
        if (currentTimeMillis - this.mScreenOffTime >= j) {
            z = true;
        } else {
            z = false;
        }
        if (z) {
            return true;
        }
        if (Build.TYPE.equals("eng")) {
            Slog.v(TAG, "mScreenOff time is not enough");
        }
        return false;
    }

    private long adjustMaxTriggerTime(long j, long j2, long j3, PendingIntent pendingIntent, int i, boolean z, boolean z2) {
        if (j3 == 0) {
            j3 = j2 - j;
        }
        if ((j3 >= 10000 ? 1 : null) == null) {
            j3 = 0;
        }
        long j4 = j2 + ((long) (((double) j3) * 0.75d));
        if (this.mSavingMode == 2) {
            if (isAlarmNeedAlign(i, pendingIntent, z2)) {
                if ((j4 - j2 >= 300000 ? 1 : null) == null) {
                    return 300000 + j2;
                }
            }
            return 0 - j4;
        } else if (!z) {
            return j4;
        } else {
            if (!isAlarmNeedAlign(i, pendingIntent, true)) {
                return 0 - j4;
            }
            if ((j4 - j2 >= 300000 ? 1 : null) == null) {
                return 300000 + j2;
            }
            return j4;
        }
    }

    public long getMaxTriggerTime(int i, long j, long j2, long j3, PendingIntent pendingIntent, int i2, boolean z) {
        this.mSavingMode = i2;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (j2 == 0) {
            return getMTKMaxTriggerTime(i, pendingIntent, j, z);
        }
        if (j2 == -1) {
            return adjustMaxTriggerTime(elapsedRealtime, j, j3, pendingIntent, i, z, false);
        }
        return j + j2;
    }

    private void setPowerSavingEnable() {
        boolean z = false;
        if (System.getInt(this.mContext.getContentResolver(), "background_power_saving_enable", 1) != 0) {
            z = true;
        }
        this.mIsEnabled = z;
    }
}

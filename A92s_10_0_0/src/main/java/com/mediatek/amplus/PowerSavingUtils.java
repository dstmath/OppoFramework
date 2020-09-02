package com.mediatek.amplus;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
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
    /* access modifiers changed from: private */
    public final Context mContext;
    private boolean mIsEnabled = false;
    /* access modifiers changed from: private */
    public boolean mIsUsbConnected = false;
    /* access modifiers changed from: private */
    public boolean mIsWFDConnected = false;
    private PowerSavingEnableObserver mPowerSavingEnableObserver = null;
    private PowerSavingReceiver mPowerSavingReceiver;
    private int mSavingMode = 0;
    /* access modifiers changed from: private */
    public boolean mScreenOff = false;
    /* access modifiers changed from: private */
    public long mScreenOffTime = 0;
    final ArrayList<String> mWhitelist = new ArrayList<>();

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
        File WhitelistFile = new File(FILEPATH);
        if (WhitelistFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(WhitelistFile));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    this.mWhitelist.add(line);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isAlarmNeedAlign(int type, PendingIntent operation, boolean isExactAlarm) {
        if (!isPowerSavingStart()) {
            return false;
        }
        if (this.mSavingMode != 2 && type != 0 && type != 2) {
            return false;
        }
        if (operation == null) {
            Slog.v(TAG, "isAlarmNeedAlign : operation is null");
            return false;
        }
        String packageName = operation.getTargetPackage();
        if (packageName == null) {
            Slog.v(TAG, "isAlarmNeedAlign : packageName is null");
            return false;
        }
        for (int i = 0; i < this.mWhitelist.size(); i++) {
            if (this.mWhitelist.get(i).equals(packageName)) {
                Slog.v(TAG, "isAlarmNeedAlign : packageName = " + packageName + "is in whitelist");
                return false;
            }
        }
        if (isExactAlarm) {
            PackageManager pm = this.mContext.getPackageManager();
            try {
                long origId = Binder.clearCallingIdentity();
                ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                Binder.restoreCallingIdentity(origId);
                if ((info.flags & 1) != 0 && (packageName.startsWith("com.android") || packageName.startsWith("com.google.android.deskclock") || packageName.startsWith("android"))) {
                    if (!Build.TYPE.equals("eng")) {
                        return false;
                    }
                    Slog.v(TAG, "isAlarmNeedAlign : " + packageName + " skip!");
                    return false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.v(TAG, "isAlarmNeedAlign : packageName not fount");
                return false;
            }
        }
        return true;
    }

    private long getMTKMaxTriggerTime(int type, PendingIntent operation, long triggerAtTime, boolean needGrouping) {
        if (!needGrouping) {
            return triggerAtTime;
        }
        if (!isAlarmNeedAlign(type, operation, true) || !needGrouping) {
            return 0 - triggerAtTime;
        }
        return SCREENOFF_TIME_INTERVAL_THRESHOLD + triggerAtTime;
    }

    public boolean isPowerSavingStart() {
        long screenOffThreshold;
        if (!this.mIsEnabled || this.mIsUsbConnected || this.mIsWFDConnected || !this.mScreenOff) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (this.mSavingMode != 2) {
            screenOffThreshold = SCREENOFF_TIME_INTERVAL_THRESHOLD;
        } else {
            screenOffThreshold = 60000;
        }
        if (currentTime - this.mScreenOffTime >= screenOffThreshold) {
            return true;
        }
        if (Build.TYPE.equals("eng")) {
            Slog.v(TAG, "mScreenOff time is not enough");
        }
        return false;
    }

    private long adjustMaxTriggerTime(long now, long triggerAtTime, long interval, PendingIntent operation, int type, boolean needGrouping, boolean isExactAlarm) {
        long futurity;
        if (interval == 0) {
            futurity = triggerAtTime - now;
        } else {
            futurity = interval;
        }
        if (futurity < MIN_FUZZABLE_INTERVAL) {
            futurity = 0;
        }
        long maxTriggerAtTime = triggerAtTime + ((long) (((double) futurity) * 0.75d));
        if (this.mSavingMode != 2) {
            if (!needGrouping) {
                return maxTriggerAtTime;
            }
            if (!isAlarmNeedAlign(type, operation, true)) {
                return 0 - maxTriggerAtTime;
            }
            if (maxTriggerAtTime - triggerAtTime < SCREENOFF_TIME_INTERVAL_THRESHOLD) {
                return triggerAtTime + SCREENOFF_TIME_INTERVAL_THRESHOLD;
            }
            return maxTriggerAtTime;
        } else if (!isAlarmNeedAlign(type, operation, isExactAlarm) || maxTriggerAtTime - triggerAtTime >= SCREENOFF_TIME_INTERVAL_THRESHOLD) {
            return 0 - maxTriggerAtTime;
        } else {
            return triggerAtTime + SCREENOFF_TIME_INTERVAL_THRESHOLD;
        }
    }

    public long getMaxTriggerTime(int type, long triggerElapsed, long windowLength, long interval, PendingIntent operation, int mAlarmMode, boolean needGrouping) {
        this.mSavingMode = mAlarmMode;
        long nowElapsed = SystemClock.elapsedRealtime();
        if (windowLength == 0) {
            return getMTKMaxTriggerTime(type, operation, triggerElapsed, needGrouping);
        }
        if (windowLength == -1) {
            return adjustMaxTriggerTime(nowElapsed, triggerElapsed, interval, operation, type, needGrouping, false);
        }
        return triggerElapsed + windowLength;
    }

    /* access modifiers changed from: private */
    public void setPowerSavingEnable() {
        boolean z = true;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "background_power_saving_enable", 1) == 0) {
            z = false;
        }
        this.mIsEnabled = z;
    }

    class PowerSavingReceiver extends BroadcastReceiver {
        public PowerSavingReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.hardware.usb.action.USB_STATE");
            filter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
            PowerSavingUtils.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                boolean unused = PowerSavingUtils.this.mScreenOff = true;
                long unused2 = PowerSavingUtils.this.mScreenOffTime = System.currentTimeMillis();
            } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                boolean unused3 = PowerSavingUtils.this.mScreenOff = false;
                long unused4 = PowerSavingUtils.this.mScreenOffTime = 0;
            } else if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction())) {
                boolean unused5 = PowerSavingUtils.this.mIsUsbConnected = intent.getBooleanExtra("connected", false);
            } else if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(intent.getAction())) {
                PowerSavingUtils powerSavingUtils = PowerSavingUtils.this;
                if (2 != intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS").getActiveDisplayState()) {
                    z = false;
                }
                boolean unused6 = powerSavingUtils.mIsWFDConnected = z;
                Slog.v(PowerSavingUtils.TAG, "PowerSavingReceiver mIsWFDConnected = " + PowerSavingUtils.this.mIsWFDConnected);
            }
        }
    }

    class PowerSavingEnableObserver extends ContentObserver {
        PowerSavingEnableObserver(Handler handler) {
            super(handler);
            observe();
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            PowerSavingUtils.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("background_power_saving_enable"), false, this, -1);
            PowerSavingUtils.this.setPowerSavingEnable();
        }

        public void onChange(boolean selfChange) {
            PowerSavingUtils.this.setPowerSavingEnable();
        }
    }
}

package com.android.server.pm;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.pm.ColorDexMetadataCompileHelper;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ColorDexMetadataCompileHelper {
    private static final int MAX_BATTERY = 100;
    private static final String TAG = "ColorDexMetadataCompileHelper";
    private static volatile ColorDexMetadataCompileHelper sDmCompileHelper = null;
    /* access modifiers changed from: private */
    public final AtomicBoolean mAbortIdleOptimization = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public boolean mAlarmSet;
    /* access modifiers changed from: private */
    public Context mContext;
    private final File mDataDir = Environment.getDataDirectory();
    private boolean mDebugDetail = ColorDexMetadataManager.sDebugDetail;
    /* access modifiers changed from: private */
    public boolean mDebugSwitch = (this.mDebugDetail | this.mDynamicDebug);
    private DexMetadataCompileReceiver mDmCompileReceiver;
    /* access modifiers changed from: private */
    public boolean mDockIdle;
    private boolean mDynamicDebug = false;
    /* access modifiers changed from: private */
    public IPackageManager mPackageManager;
    /* access modifiers changed from: private */
    public boolean mScreenOn;

    public static ColorDexMetadataCompileHelper getInstance() {
        if (sDmCompileHelper == null) {
            synchronized (ColorDexMetadataCompileHelper.class) {
                if (sDmCompileHelper == null) {
                    sDmCompileHelper = new ColorDexMetadataCompileHelper();
                }
            }
        }
        return sDmCompileHelper;
    }

    private ColorDexMetadataCompileHelper() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "CompileHelper: constructor!");
        }
    }

    public void onSystemReady(Context context) {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "CompileHelper: onSystemReady!");
        }
        this.mScreenOn = true;
        this.mDockIdle = false;
        this.mAlarmSet = false;
        this.mContext = context;
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService(BrightnessConstants.AppSplineXml.TAG_PACKAGE));
        this.mDmCompileReceiver = new DexMetadataCompileReceiver();
        this.mDmCompileReceiver.startCompileTracking(context);
    }

    /* access modifiers changed from: private */
    public class DexMetadataCompileReceiver extends BroadcastReceiver {
        private AlarmManager mAlarm;
        private AlarmManager.OnAlarmListener mIdleAlarmListener = new AlarmManager.OnAlarmListener() {
            /* class com.android.server.pm.$$Lambda$ColorDexMetadataCompileHelper$DexMetadataCompileReceiver$7xPA3FU77yzxWoaREwBz4Dmllp8 */

            public final void onAlarm() {
                ColorDexMetadataCompileHelper.DexMetadataCompileReceiver.this.lambda$new$0$ColorDexMetadataCompileHelper$DexMetadataCompileReceiver();
            }
        };

        public DexMetadataCompileReceiver() {
            this.mAlarm = (AlarmManager) ColorDexMetadataCompileHelper.this.mContext.getSystemService("alarm");
        }

        public /* synthetic */ void lambda$new$0$ColorDexMetadataCompileHelper$DexMetadataCompileReceiver() {
            new Thread(new Runnable() {
                /* class com.android.server.pm.ColorDexMetadataCompileHelper.DexMetadataCompileReceiver.AnonymousClass1 */

                public void run() {
                    DexMetadataCompileReceiver.this.runBackgroundDexOpt();
                }
            }).start();
        }

        /* access modifiers changed from: private */
        public void runBackgroundDexOpt() {
            if (!ColorDexMetadataCompileHelper.this.mScreenOn || ColorDexMetadataCompileHelper.this.mDockIdle) {
                if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                    Slog.i(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt start!");
                }
                ColorDexMetadataCompileHelper.this.mAbortIdleOptimization.set(false);
                ArraySet<String> pkgs = ColorDexMetadataManagerHelper.getInstance().getCompilePackageSet();
                if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                    Slog.d(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt: compile package set size = " + pkgs.size());
                }
                if (!pkgs.isEmpty()) {
                    try {
                        Iterator<String> it = pkgs.iterator();
                        while (it.hasNext()) {
                            String pkg = it.next();
                            if (ColorDexMetadataCompileHelper.this.mAbortIdleOptimization.get()) {
                                if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                                    Slog.d(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt abort by screen on!");
                                    return;
                                }
                                return;
                            } else if (ColorDexMetadataCompileHelper.this.isLowStorage()) {
                                if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                                    Slog.d(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt abort by low storage!");
                                    return;
                                }
                                return;
                            } else if (ColorDexMetadataCompileHelper.this.isLowBattery()) {
                                if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                                    Slog.d(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt abort by low battery!");
                                    return;
                                }
                                return;
                            } else if (!ColorDexMetadataCompileHelper.this.isHighTemperature()) {
                                long startTime = System.currentTimeMillis();
                                boolean ret = ColorDexMetadataCompileHelper.this.mPackageManager.performDexOptMode(pkg, true, "speed-profile", true, true, (String) null);
                                long endTime = System.currentTimeMillis();
                                if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                                    Slog.i(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt: pkg = " + pkg + ", compile success = " + ret + ", costTime = " + (endTime - startTime) + " ms");
                                }
                                int userId = ActivityManager.getCurrentUser();
                                if (userId >= 0) {
                                    PackageInfo packageInfo = ColorDexMetadataCompileHelper.this.mPackageManager.getPackageInfo(pkg, 0, userId);
                                    ColorDexMetadataManager.getInstance().recordCompiledApp(pkg);
                                    ColorDexMetadataManagerHelper.getInstance().uploadToDcs(String.valueOf(endTime - startTime), packageInfo);
                                }
                                ColorDexMetadataManagerHelper.getInstance().removeFromCompilePackageSet(pkg);
                                if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                                    Slog.d(ColorDexMetadataCompileHelper.TAG, "performDexOpt done, remove from compile package set for " + pkg);
                                }
                            } else if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                                Slog.d(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt abort by high temperature!");
                                return;
                            } else {
                                return;
                            }
                        }
                        if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                            Slog.i(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt done!");
                        }
                    } catch (Exception e) {
                        Slog.e(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt Exception: ", e);
                    }
                } else if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                    Slog.i(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt done, compile package set is empty!");
                }
            } else if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                Slog.i(ColorDexMetadataCompileHelper.TAG, "runBackgroundDexOpt stop, conditions not permit, mScreenOn = " + ColorDexMetadataCompileHelper.this.mScreenOn + ", mDockIdle = " + ColorDexMetadataCompileHelper.this.mDockIdle);
            }
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                Slog.i(ColorDexMetadataCompileHelper.TAG, "DexMetadataCompileReceiver onReceive: action = " + action);
            }
            if (ColorDexMetadataCompileHelper.this.checkBackgroundCompileEnable()) {
                if (action.equals("android.intent.action.SCREEN_OFF") || action.equals("android.intent.action.DREAMING_STARTED") || action.equals("android.intent.action.DOCK_IDLE")) {
                    if (!action.equals("android.intent.action.DOCK_IDLE")) {
                        boolean unused = ColorDexMetadataCompileHelper.this.mScreenOn = false;
                        boolean unused2 = ColorDexMetadataCompileHelper.this.mDockIdle = false;
                    } else if (ColorDexMetadataCompileHelper.this.mScreenOn) {
                        boolean unused3 = ColorDexMetadataCompileHelper.this.mDockIdle = true;
                    } else {
                        return;
                    }
                    ArraySet<String> pkgs = ColorDexMetadataManagerHelper.getInstance().getCompilePackageSet();
                    if (!pkgs.isEmpty()) {
                        if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                            Slog.d(ColorDexMetadataCompileHelper.TAG, "DexMetadataCompileReceiver: compile package set is not empty, need to compile");
                            Iterator<String> it = pkgs.iterator();
                            while (it.hasNext()) {
                                Slog.d(ColorDexMetadataCompileHelper.TAG, "DexMetadataCompileReceiver: need to compile pkg = " + it.next());
                            }
                        }
                        long nowElapsed = SystemClock.elapsedRealtimeClock().millis();
                        int idleTime = ColorDexMetadataConfigHelper.getInstance().getIdleTimeValue();
                        long idleWindowSlop = TimeUnit.MINUTES.toMillis((long) ColorDexMetadataConfigHelper.getInstance().getTimeSlotValue());
                        this.mAlarm.setWindow(2, nowElapsed + TimeUnit.MINUTES.toMillis((long) idleTime), idleWindowSlop, "Compile DexMetadata", this.mIdleAlarmListener, null);
                        boolean unused4 = ColorDexMetadataCompileHelper.this.mAlarmSet = true;
                    } else if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                        Slog.d(ColorDexMetadataCompileHelper.TAG, "DexMetadataCompileReceiver: compile package set is empty, no need to compile");
                    }
                } else if (action.equals("android.intent.action.SCREEN_ON") || action.equals("android.intent.action.DREAMING_STOPPED") || action.equals("android.intent.action.DOCK_ACTIVE")) {
                    if (!action.equals("android.intent.action.DOCK_ACTIVE")) {
                        boolean unused5 = ColorDexMetadataCompileHelper.this.mScreenOn = true;
                        boolean unused6 = ColorDexMetadataCompileHelper.this.mDockIdle = false;
                    } else if (ColorDexMetadataCompileHelper.this.mScreenOn) {
                        boolean unused7 = ColorDexMetadataCompileHelper.this.mDockIdle = false;
                    } else {
                        return;
                    }
                    if (ColorDexMetadataCompileHelper.this.mAlarmSet) {
                        this.mAlarm.cancel(this.mIdleAlarmListener);
                    }
                    boolean unused8 = ColorDexMetadataCompileHelper.this.mAlarmSet = false;
                    ColorDexMetadataCompileHelper.this.mAbortIdleOptimization.set(true);
                }
            }
        }

        /* access modifiers changed from: private */
        public void startCompileTracking(Context context) {
            if (ColorDexMetadataCompileHelper.this.mDebugSwitch) {
                Slog.i(ColorDexMetadataCompileHelper.TAG, "start dex metadata compile tracking!");
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.DREAMING_STARTED");
            filter.addAction("android.intent.action.DREAMING_STOPPED");
            filter.addAction("android.intent.action.DOCK_IDLE");
            filter.addAction("android.intent.action.DOCK_ACTIVE");
            context.registerReceiver(this, filter);
        }
    }

    /* access modifiers changed from: private */
    public boolean checkBackgroundCompileEnable() {
        if (ColorDexMetadataConfigHelper.getInstance().isOppoBackgroundCompileSwitchEnable()) {
            return true;
        }
        if (!this.mDebugSwitch) {
            return false;
        }
        Slog.d(TAG, "oppo profile compile switch is disable!");
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isLowStorage() {
        if (this.mDataDir.getUsableSpace() < getLowStorageThreshold(this.mContext)) {
            return true;
        }
        return false;
    }

    private long getLowStorageThreshold(Context context) {
        long lowThreshold = StorageManager.from(context).getStorageLowBytes(this.mDataDir);
        if (lowThreshold == 0) {
            Slog.e(TAG, "Invalid low storage threshold");
        }
        return lowThreshold;
    }

    /* access modifiers changed from: private */
    public boolean isLowBattery() {
        if (getBatteryLevel() < this.mContext.getResources().getInteger(17694829)) {
            return true;
        }
        return false;
    }

    private int getBatteryLevel() {
        Intent intent = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        if (!intent.getBooleanExtra("present", true)) {
            return 100;
        }
        if (level < 0 || scale <= 0) {
            return 0;
        }
        return (level * 100) / scale;
    }

    /* access modifiers changed from: private */
    public boolean isHighTemperature() {
        if (getBatteryTemperature() > ColorDexMetadataConfigHelper.getInstance().getCompileTemperatureThreshold()) {
            return true;
        }
        return false;
    }

    private int getBatteryTemperature() {
        return this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("temperature", -1);
    }
}

package com.mediatek.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.server.LocationManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.display.DisplayTransformManager;
import com.android.server.pm.Installer;
import com.mediatek.am.AMEventHookAction;
import com.mediatek.am.AMEventHookData.AfterPostEnableScreenAfterBoot;
import com.mediatek.am.AMEventHookData.SystemReady;
import com.mediatek.am.AMEventHookData.SystemReady.Index;
import com.mediatek.am.AMEventHookResult;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.ITelephonyEx.Stub;
import com.mediatek.ipomanager.ActivityManagerPlus;
import dalvik.system.VMRuntime;

public class PowerOffAlarmUtility {
    private static final String ACTION_AIRPLANE_CHANGE_DONE = "com.mediatek.intent.action.AIRPLANE_CHANGE_DONE";
    private static final String ALARM_BOOT_DONE = "android.intent.action.normal.boot.done";
    private static final String EXTRA_AIRPLANE_MODE = "airplaneMode";
    private static final String REMOVE_IPOWIN = "alarm.boot.remove.ipowin";
    private static final String TAG = "PowerOffAlarmUtility";
    private static PowerOffAlarmUtility mInstance;
    private Context mContext;
    public boolean mFirstBoot = false;
    private boolean mIsAirModeEnableBeforeAlarmBoot = false;
    private boolean mNeedTurnOffAirMode = false;
    private boolean mRollback = false;
    private ActivityManagerService mService;

    public static PowerOffAlarmUtility getInstance(Context ctx, ActivityManagerService aService) {
        if (mInstance != null) {
            return mInstance;
        }
        if (!(ctx == null || aService == null)) {
            mInstance = new PowerOffAlarmUtility(ctx, aService);
        }
        return mInstance;
    }

    private PowerOffAlarmUtility(Context ctx, ActivityManagerService aService) {
        this.mContext = ctx;
        this.mService = aService;
        registerNormalBootReceiver(this.mContext);
        if (SystemProperties.getBoolean("persist.sys.ams.recover", false)) {
            checkFlightMode(true, false);
        }
    }

    public void launchPowerOffAlarm(Boolean recover, Boolean shutdown) {
        if (!(recover == null || shutdown == null)) {
            checkFlightMode(recover.booleanValue(), shutdown.booleanValue());
        }
        this.mContext.sendBroadcast(new Intent("android.intent.action.LAUNCH_POWEROFF_ALARM"));
    }

    public static boolean isAlarmBoot() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        if (bootReason == null || !bootReason.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            return false;
        }
        return true;
    }

    private final void registerNormalBootReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.normal.boot");
        filter.addAction("android.intent.action.normal.shutdown");
        filter.addAction("android.intent.action.normal.boot.done");
        filter.addAction(REMOVE_IPOWIN);
        filter.addAction("android.intent.action.ACTION_BOOT_IPO");
        filter.addAction(ACTION_AIRPLANE_CHANGE_DONE);
        this.mFirstBoot = true;
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if ("android.intent.action.normal.boot".equals(action)) {
                        Log.i(PowerOffAlarmUtility.TAG, "DeskClock normally boots-up device");
                        PowerOffAlarmUtility.this.mNeedTurnOffAirMode = true;
                        if (PowerOffAlarmUtility.this.mRollback) {
                            PowerOffAlarmUtility.this.checkFlightMode(false, false);
                        }
                        if (PowerOffAlarmUtility.this.mFirstBoot) {
                            synchronized (PowerOffAlarmUtility.this.mService) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    PowerOffAlarmUtility.this.mService.resumeTopActivityOnSystemReadyFocusedStackLocked();
                                } finally {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                        } else {
                            ActivityManagerPlus.ipoBootCompleted();
                        }
                    } else if ("android.intent.action.normal.shutdown".equals(action)) {
                        Log.v(PowerOffAlarmUtility.TAG, "DeskClock normally shutdowns device");
                        ActivityManagerPlus.createIPOWin();
                        if (PowerOffAlarmUtility.this.mRollback) {
                            PowerOffAlarmUtility.this.checkFlightMode(false, true);
                        }
                    } else if ("android.intent.action.normal.boot.done".equals(action)) {
                        Log.w(PowerOffAlarmUtility.TAG, "ALARM_BOOT_DONE normally shutdowns device");
                        synchronized (PowerOffAlarmUtility.this.mService) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                PowerOffAlarmUtility.this.mService.resumeTopActivityOnSystemReadyFocusedStackLocked();
                            } finally {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                    } else if (PowerOffAlarmUtility.REMOVE_IPOWIN.equals(action)) {
                        ActivityManagerPlus.removeIPOWin();
                    } else if ("android.intent.action.ACTION_BOOT_IPO".equals(action) && PowerOffAlarmUtility.isAlarmBoot()) {
                        Slog.v(PowerOffAlarmUtility.TAG, "power off alarm enabled with IPO boot");
                        PowerOffAlarmUtility.this.launchPowerOffAlarm(Boolean.valueOf(false), Boolean.valueOf(false));
                    } else if (PowerOffAlarmUtility.ACTION_AIRPLANE_CHANGE_DONE.equals(action)) {
                        Log.d(PowerOffAlarmUtility.TAG, "onReceive, ACTION_AIRPLANE_CHANGE_DONE = " + intent.getBooleanExtra(PowerOffAlarmUtility.EXTRA_AIRPLANE_MODE, false) + " , mIsAirModeEnableBeforeAlarmBoot = " + PowerOffAlarmUtility.this.mIsAirModeEnableBeforeAlarmBoot + " , mNeedTurnOffAirMode = " + PowerOffAlarmUtility.this.mNeedTurnOffAirMode);
                        if (!PowerOffAlarmUtility.this.mIsAirModeEnableBeforeAlarmBoot && PowerOffAlarmUtility.this.mNeedTurnOffAirMode) {
                            PowerOffAlarmUtility.this.mNeedTurnOffAirMode = false;
                            SystemProperties.set("persist.sys.ams.recover", "false");
                            Global.putInt(PowerOffAlarmUtility.this.mContext.getContentResolver(), "airplane_mode_on", 0);
                            Intent intent_chg_done = new Intent("android.intent.action.AIRPLANE_MODE");
                            intent_chg_done.addFlags(536870912);
                            intent_chg_done.putExtra("state", false);
                            PowerOffAlarmUtility.this.mContext.sendBroadcast(intent_chg_done);
                            Log.v(PowerOffAlarmUtility.TAG, "turn off flight mode with airplane mode change done");
                        }
                    }
                }
            }
        }, filter);
    }

    private void checkFlightMode(boolean recover, boolean shutdown) {
        Log.v(TAG, "mRollback = " + this.mRollback + ", recover = " + recover);
        if (recover) {
            Log.v(TAG, "since system crash, switch flight mode to off");
            Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
            SystemProperties.set("persist.sys.ams.recover", "false");
            return;
        }
        if (this.mRollback) {
            this.mRollback = false;
            if (!shutdown) {
                if (isAirplanemodeAvailableNow() && this.mNeedTurnOffAirMode) {
                    this.mNeedTurnOffAirMode = false;
                    SystemProperties.set("persist.sys.ams.recover", "false");
                    Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
                    Intent intent_avail = new Intent("android.intent.action.AIRPLANE_MODE");
                    intent_avail.addFlags(536870912);
                    intent_avail.putExtra("state", false);
                    this.mContext.sendBroadcast(intent_avail);
                    Log.v(TAG, "turn off flight mode with available");
                } else {
                    this.mNeedTurnOffAirMode = true;
                    Log.v(TAG, "turn off flight mode with waitting");
                }
            }
        } else {
            boolean mode = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
            Log.v(TAG, "flight mode enable = " + mode);
            if (mode) {
                Log.v(TAG, "No need turn on flight mode");
                this.mIsAirModeEnableBeforeAlarmBoot = true;
            } else {
                Log.v(TAG, "turn on flight mode");
                this.mIsAirModeEnableBeforeAlarmBoot = false;
                SystemProperties.set("persist.sys.ams.recover", "true");
                this.mRollback = true;
                Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", 1);
                Intent intent_turn_on = new Intent("android.intent.action.AIRPLANE_MODE");
                intent_turn_on.addFlags(536870912);
                intent_turn_on.putExtra("state", true);
                this.mContext.sendBroadcast(intent_turn_on);
            }
        }
    }

    private void markBootComplete(Installer installer) {
        ArraySet<String> completedIsas = new ArraySet();
        for (String abi : Build.SUPPORTED_ABIS) {
            Process.establishZygoteConnectionForAbi(abi);
            String instructionSet = VMRuntime.getInstructionSet(abi);
            if (!completedIsas.contains(instructionSet)) {
                try {
                    installer.markBootComplete(VMRuntime.getInstructionSet(abi));
                } catch (InstallerException e) {
                    Slog.e(TAG, "Unable to mark boot complete for abi: " + abi, e);
                }
                completedIsas.add(instructionSet);
            }
        }
    }

    public static PowerOffAlarmUtility getInstance(SystemReady data) {
        return getInstance((Context) data.get(Index.context), (ActivityManagerService) data.get(Index.ams));
    }

    public void onSystemReady(SystemReady data, AMEventHookResult result) {
        Context context = (Context) data.get(Index.context);
        ActivityManagerService ams = (ActivityManagerService) data.get(Index.ams);
        switch (data.getInt(Index.phase)) {
            case DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE /*200*/:
                this.mFirstBoot = true;
                return;
            case 300:
                if (!isAlarmBoot()) {
                    result.addAction(AMEventHookAction.AM_SkipHomeActivityLaunching);
                    return;
                }
                return;
            case DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR /*400*/:
                if (isAlarmBoot()) {
                    result.addAction(AMEventHookAction.AM_PostEnableScreenAfterBoot);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onAfterPostEnableScreenAfterBoot(AfterPostEnableScreenAfterBoot data, AMEventHookResult result) {
        markBootComplete((Installer) data.get(AfterPostEnableScreenAfterBoot.Index.installer));
        Slog.v(TAG, "power off alarm enabled ScreenAfterBoot");
        launchPowerOffAlarm(Boolean.valueOf(false), Boolean.valueOf(false));
        result.addAction(AMEventHookAction.AM_Interrupt);
    }

    private boolean isAirplanemodeAvailableNow() {
        ITelephonyEx telephonyEx = Stub.asInterface(ServiceManager.getService("phoneEx"));
        boolean isAvailable = false;
        if (telephonyEx != null) {
            try {
                isAvailable = telephonyEx.isAirplanemodeAvailableNow();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "telephonyEx == null");
        }
        Log.d(TAG, "isAirplaneModeAvailable = " + isAvailable);
        return isAvailable;
    }
}

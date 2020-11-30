package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.health.V1_0.HealthInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.theia.NoFocusWindow;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public final class OppoBatteryService {
    private static final String ACTION_OPPO_USB_HW_SATUS = "oppo.intent.action.USB_HW_STATUS";
    private static final int AC_PLUG_IN_EVENT = 100000;
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int CHARGERID_UPDATED_EVENT = 100002;
    public static boolean DEBUG = false;
    private static final int ENTER_FASTCHARGE_EVENT = 100004;
    private static final int FLAG_OTG_ON_LINE = 1;
    private static final int FLAG_USB_TEMP_ABNORNAL = 1;
    private static final int PLUG_OUT_EVENT = 100003;
    private static final String SEPARATOR = "\n";
    private static final String TAG = OppoBatteryService.class.getSimpleName();
    private static final int UPDATE_BATTERY_HISTORY_EVENT = 100005;
    private static final int USB_PLUG_IN_EVENT = 100001;
    private String charger_plugin_desc = null;
    private String charger_plugout_desc = null;
    private ActivityManagerInternal mActivityManagerInternal;
    private OppoBaseBatteryService mBas;
    public int mBattCbStatus;
    private String mBatteryCriticalData = null;
    public int mBatteryCurrent;
    public int mBatteryFcc;
    public int mBatteryHwStatus = 1;
    public int mBatteryIcStatus;
    private int mBatteryJumpSoc;
    public int mBatteryJumpSocTotal;
    private int mBatteryLevel;
    public int mBatteryNotifyCode;
    public int mBatteryRealtimeCapacity;
    private int mBatteryRequestPowerOff;
    private int mBatteryTemperature;
    public boolean mChargeFastCharger;
    public int mChargeIdVoltage;
    private int mChargerTechnology;
    private int mChargerVoltage;
    private int mChargeridVoltage;
    private final Context mContext;
    public int mFast2Normal;
    public int mFastChgType;
    private final Handler mHandler;
    private final ServiceThread mHandlerThread;
    public int mHwStatusIsSet;
    public int mIcStatusIsSet;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.OppoBatteryService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str = OppoBatteryService.TAG;
            Slog.i(str, "action received : " + action);
            if (action.equals("android.intent.action.SCREEN_ON")) {
                OppoBatteryService.this.mBas.handleScreenState(true);
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                OppoBatteryService.this.mBas.handleScreenState(false);
            }
        }
    };
    private boolean mIsSellModeVersion = false;
    public int mLastBattCbStatus;
    public int mLastBatteryCurrent;
    public int mLastBatteryHwStatus;
    public int mLastBatteryIcStatus;
    private int mLastBatteryJumpSoc;
    public int mLastBatteryTemperature;
    private boolean mLastChargeFastCharger;
    private int mLastChargerTechnology;
    private int mLastChargerVoltage;
    private int mLastChargeridVoltage;
    public int mLastFastChgType;
    private int mLastMaxChargeCurrent = 0;
    private int mLastMaxChargeTemperature = Integer.MIN_VALUE;
    private int mLastMinChargeTemperature = Integer.MAX_VALUE;
    public int mLastOtgOnline;
    private int mLastPlugType;
    public int mLastUsbStatus;
    public long mLastUsbStatusUpdateTime;
    public int mLastVoltageMin;
    public int mLastVoltageNow;
    public int mLastWirelessDeviated;
    public int mLastWirelessReserve;
    public int mOtgOnline;
    private PackageManager mPackageManager = null;
    private int mPlugType;
    private int mPrimalType;
    private boolean mUpdatesStopped = false;
    public int mUsbStatus;
    public int mVoltageMin;
    public int mVoltageNow;
    public int mWirelessDeviated;
    public int mWirelessReserve;

    public native void native_update();

    private class BatteryHandler extends Handler {
        public BatteryHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (OppoBatteryService.DEBUG) {
                String str = OppoBatteryService.TAG;
                Slog.i(str, "handleMessage : " + message.what);
            }
            switch (message.what) {
                case OppoBatteryService.AC_PLUG_IN_EVENT /* 100000 */:
                case OppoBatteryService.USB_PLUG_IN_EVENT /* 100001 */:
                case OppoBatteryService.CHARGERID_UPDATED_EVENT /* 100002 */:
                case OppoBatteryService.PLUG_OUT_EVENT /* 100003 */:
                case OppoBatteryService.ENTER_FASTCHARGE_EVENT /* 100004 */:
                    OppoBatteryService.this.writeCriticalLog((int[]) message.obj);
                    return;
                case OppoBatteryService.UPDATE_BATTERY_HISTORY_EVENT /* 100005 */:
                    String history = (String) message.obj;
                    String str2 = OppoBatteryService.TAG;
                    Slog.i(str2, "new battery critical data : " + history);
                    OppoManager.writeCriticalData(OppoManager.TYPE_BATTERY_CHARGE_HISTORY + 1024, history);
                    OppoManager.writeCriticalData(OppoManager.TYPE_BATTERY_CHARGE_HISTORY, history);
                    return;
                default:
                    return;
            }
        }
    }

    public OppoBatteryService(Context context, OppoBaseBatteryService bas, ActivityManagerInternal ams) {
        String[] historyData;
        this.mContext = context;
        this.mBas = bas;
        this.mActivityManagerInternal = ams;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new BatteryHandler(this.mHandlerThread.getLooper());
        this.charger_plugin_desc = this.mContext.getResources().getString(201653546);
        this.charger_plugout_desc = this.mContext.getResources().getString(201653547);
        this.mBatteryCriticalData = OppoManager.readCriticalData(OppoManager.TYPE_BATTERY_CHARGE_HISTORY + 1024, OppoManager.TYPE_CRITICAL_DATA_SIZE);
        String str = TAG;
        Slog.i(str, "load from criticaldata : " + this.mBatteryCriticalData);
        String str2 = this.mBatteryCriticalData;
        if (!(str2 == null || (historyData = str2.split(",")) == null || historyData.length != 3)) {
            try {
                this.mLastMaxChargeTemperature = Integer.valueOf(historyData[0]).intValue();
                this.mLastMinChargeTemperature = Integer.valueOf(historyData[1]).intValue();
                this.mLastMaxChargeCurrent = Integer.valueOf(historyData[2]).intValue();
            } catch (NumberFormatException e) {
                Slog.e(TAG, e.getMessage());
                this.mLastMaxChargeTemperature = Integer.MIN_VALUE;
                this.mLastMinChargeTemperature = Integer.MAX_VALUE;
                this.mLastMaxChargeCurrent = Integer.MIN_VALUE;
            }
        }
        this.mHwStatusIsSet = SystemProperties.getInt("persist.sys.hw_status", 1);
        this.mIcStatusIsSet = SystemProperties.getInt("persist.sys.ic_status", 1);
    }

    public void onStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        if (this.mPackageManager == null) {
            this.mPackageManager = this.mContext.getPackageManager();
        }
        PackageManager packageManager = this.mPackageManager;
        if (packageManager != null) {
            this.mIsSellModeVersion = packageManager.hasSystemFeature("oppo.specialversion.exp.sellmode");
        }
    }

    public void processOppoBatteryHwStatusChanedLocked() {
        int i = this.mBatteryHwStatus;
        if (!(i == this.mLastBatteryHwStatus && this.mHwStatusIsSet == i)) {
            this.mHwStatusIsSet = SystemProperties.getInt("persist.sys.hw_status", 1);
            this.mLastBatteryHwStatus = this.mBatteryHwStatus;
            final Intent hw_status_intent = new Intent("oppo.intent.action.BATTERY_HW_STATUS");
            hw_status_intent.putExtra("hw_status_extra", this.mBatteryHwStatus);
            hw_status_intent.setPackage("com.oppo.oppopowermonitor");
            if (this.mActivityManagerInternal.isSystemReady()) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.OppoBatteryService.AnonymousClass2 */

                    public void run() {
                        OppoBaseBatteryService unused = OppoBatteryService.this.mBas;
                        if (OppoBaseBatteryService.DEBUG_PANIC) {
                            String str = OppoBatteryService.TAG;
                            Slog.d(str, "send broadcast : oppo.intent.action.BATTERY_HW_STATUS : mBatteryHwStatus" + OppoBatteryService.this.mBatteryHwStatus);
                        }
                        ActivityManager.broadcastStickyIntent(hw_status_intent, -1);
                    }
                });
            }
        }
        int i2 = this.mBatteryIcStatus;
        if (i2 != this.mLastBatteryIcStatus || this.mIcStatusIsSet != i2) {
            this.mIcStatusIsSet = SystemProperties.getInt("persist.sys.ic_status", 1);
            this.mLastBatteryIcStatus = this.mBatteryIcStatus;
            final Intent ic_status_intent = new Intent("oppo.intent.action.BATTERY_IC_STATUS");
            ic_status_intent.putExtra("ic_status_extra", this.mBatteryIcStatus);
            ic_status_intent.setPackage("com.oppo.oppopowermonitor");
            if (this.mActivityManagerInternal.isSystemReady()) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.OppoBatteryService.AnonymousClass3 */

                    public void run() {
                        OppoBaseBatteryService unused = OppoBatteryService.this.mBas;
                        if (OppoBaseBatteryService.DEBUG_PANIC) {
                            String str = OppoBatteryService.TAG;
                            Slog.d(str, "send broadcast : oppo.intent.action.BATTERY_IC_STATUS : mBatteryIcStatus" + OppoBatteryService.this.mBatteryIcStatus);
                        }
                        ActivityManager.broadcastStickyIntent(ic_status_intent, -1);
                    }
                });
            }
        }
    }

    public void processOppoBatteryPluggedChanedLocked(final int plugType, int lastPlugType, HealthInfo healthInfo) {
        if (this.mIsSellModeVersion && plugType != 0 && getCurrentChargeStateForSaleInternal() == 0) {
            healthInfo.batteryStatus = 2;
            OppoBaseBatteryService oppoBaseBatteryService = this.mBas;
            if (OppoBaseBatteryService.DEBUG_PANIC) {
                String str = TAG;
                Slog.d(str, "SellMode version batteryStatus: " + healthInfo.batteryStatus + "  mPlugType:" + plugType);
            }
        }
        if (plugType != lastPlugType) {
            final Intent plugged_changed_intent = new Intent("oppo.intent.action.BATTERY_PLUGGED_CHANGED");
            plugged_changed_intent.addFlags(536870912);
            plugged_changed_intent.putExtra("plugged", plugType);
            if (this.mActivityManagerInternal.isSystemReady()) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.OppoBatteryService.AnonymousClass4 */

                    public void run() {
                        OppoBaseBatteryService unused = OppoBatteryService.this.mBas;
                        if (OppoBaseBatteryService.DEBUG_PANIC) {
                            String str = OppoBatteryService.TAG;
                            Slog.d(str, "send broadcast : oppo.intent.action.BATTERY_PLUGGED_CHANGED : mPlugType = " + plugType);
                        }
                        ActivityManager.broadcastStickyIntent(plugged_changed_intent, -1);
                    }
                });
            }
        }
        if (this.mUsbStatus != this.mLastUsbStatus || plugType != lastPlugType || this.mOtgOnline != this.mLastOtgOnline) {
            OppoBaseBatteryService oppoBaseBatteryService2 = this.mBas;
            if (OppoBaseBatteryService.DEBUG_PANIC) {
                String str2 = TAG;
                Slog.d(str2, "USB interface status changed    UsbStatus:" + this.mUsbStatus + "  LastUsbStatus:" + this.mLastUsbStatus + "  OtgOnline:" + this.mOtgOnline + "  LastOtgOnline:" + this.mLastOtgOnline + "  PlugType:" + plugType + "  LastPlugType:" + lastPlugType);
            }
            this.mLastUsbStatus = this.mUsbStatus;
            this.mLastOtgOnline = this.mOtgOnline;
            this.mLastUsbStatusUpdateTime = SystemClock.elapsedRealtime();
            final Intent usbStatusIntent = new Intent(ACTION_OPPO_USB_HW_SATUS);
            usbStatusIntent.putExtra("usb_hw_status_extra", this.mUsbStatus);
            usbStatusIntent.putExtra("otg_online_extra", this.mOtgOnline);
            usbStatusIntent.putExtra("plug_type_extra", plugType);
            usbStatusIntent.setPackage("com.oppo.oppopowermonitor");
            if (this.mActivityManagerInternal.isSystemReady()) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.OppoBatteryService.AnonymousClass5 */

                    public void run() {
                        OppoBaseBatteryService unused = OppoBatteryService.this.mBas;
                        if (OppoBaseBatteryService.DEBUG_PANIC) {
                            Slog.d(OppoBatteryService.TAG, "send broadcast : oppo.intent.action.USB_HW_STATUS");
                        }
                        ActivityManager.broadcastStickyIntent(usbStatusIntent, -1);
                    }
                });
            }
        }
    }

    public void processAdditionalValuesLocked(int batteryTemperature, int plugType, int batteryLevel) {
        if (!this.mUpdatesStopped) {
            this.mBatteryTemperature = batteryTemperature;
            this.mPlugType = plugType;
            this.mBatteryLevel = batteryLevel;
        }
        if (DEBUG) {
            Slog.d(TAG, "Processing additional values: mChargerVoltage=" + this.mChargerVoltage + ", mLastChargerVoltage=" + this.mLastChargerVoltage + ", mBatteryCurrent=" + this.mBatteryCurrent + ", mLastBatteryCurrent=" + this.mLastBatteryCurrent + ", mBatteryLevel=" + this.mBatteryLevel + ", mBatteryTemperature=" + this.mBatteryTemperature + ", mChargerTechnology=" + this.mChargerTechnology + ", mChargeFastCharger=" + this.mChargeFastCharger + ", mChargeridVoltage=" + this.mChargeridVoltage + ", mLastChargeridVoltage=" + this.mLastChargeridVoltage + ", mPlugType=" + this.mPlugType + ", mLastPlugType=" + this.mLastPlugType + ", mUsbStatus=" + this.mUsbStatus + ", mBatteryHwStatus=" + this.mBatteryHwStatus + ", mHwStatusIsSet=" + this.mHwStatusIsSet + ", mBatteryIcStatus=" + this.mBatteryIcStatus + ", mIcStatusIsSet=" + this.mIcStatusIsSet + ", mFastChgType=" + this.mFastChgType);
        }
        if (this.mChargerVoltage != this.mLastChargerVoltage || this.mBatteryCurrent != this.mLastBatteryCurrent || this.mBatteryTemperature != this.mLastBatteryTemperature || this.mChargerTechnology != this.mLastChargerTechnology || this.mChargeFastCharger != this.mLastChargeFastCharger || this.mBatteryJumpSoc != this.mLastBatteryJumpSoc || this.mChargeridVoltage != this.mLastChargeridVoltage || this.mPlugType != this.mLastPlugType || this.mFastChgType != this.mLastFastChgType) {
            int i = this.mBatteryJumpSoc;
            if (i != this.mLastBatteryJumpSoc) {
                this.mBatteryJumpSocTotal += i;
                this.mLastBatteryJumpSoc = i;
            }
            int i2 = this.mPlugType;
            if (i2 != this.mLastPlugType) {
                if (i2 == 0) {
                    if (this.mHandler.hasMessages(AC_PLUG_IN_EVENT)) {
                        if (DEBUG) {
                            Slog.i(TAG, "Drop AC_PLUG_IN_EVENT");
                        }
                        this.mHandler.removeMessages(AC_PLUG_IN_EVENT);
                    } else {
                        if (this.mHandler.hasMessages(PLUG_OUT_EVENT)) {
                            this.mHandler.removeMessages(PLUG_OUT_EVENT);
                        }
                        Handler handler = this.mHandler;
                        handler.sendMessage(handler.obtainMessage(PLUG_OUT_EVENT, getChargerCriticalLog()));
                    }
                } else if (i2 == 1) {
                    if (this.mHandler.hasMessages(AC_PLUG_IN_EVENT)) {
                        this.mHandler.removeMessages(AC_PLUG_IN_EVENT);
                    }
                    Handler handler2 = this.mHandler;
                    handler2.sendMessageDelayed(handler2.obtainMessage(AC_PLUG_IN_EVENT, getChargerCriticalLog()), 2000);
                } else if (i2 == 2) {
                    if (this.mHandler.hasMessages(USB_PLUG_IN_EVENT)) {
                        this.mHandler.removeMessages(USB_PLUG_IN_EVENT);
                    }
                    Handler handler3 = this.mHandler;
                    handler3.sendMessage(handler3.obtainMessage(USB_PLUG_IN_EVENT, getChargerCriticalLog()));
                } else {
                    Slog.i(TAG, "unknown plug type");
                }
            }
            int i3 = this.mChargeridVoltage;
            if (i3 != this.mLastChargeridVoltage && i3 > 0 && this.mHandler.hasMessages(AC_PLUG_IN_EVENT)) {
                if (DEBUG) {
                    Slog.i(TAG, "mChargeridVoltage updated, update AC_PLUG_IN_EVENT");
                }
                this.mHandler.removeMessages(AC_PLUG_IN_EVENT);
                Handler handler4 = this.mHandler;
                handler4.sendMessage(handler4.obtainMessage(AC_PLUG_IN_EVENT, getChargerCriticalLog()));
            }
            if (this.mChargeFastCharger && !this.mLastChargeFastCharger) {
                if (this.mHandler.hasMessages(AC_PLUG_IN_EVENT)) {
                    if (DEBUG) {
                        Slog.i(TAG, "mChargeFastCharger updated, drop AC_PLUG_IN_EVENT");
                    }
                    this.mHandler.removeMessages(AC_PLUG_IN_EVENT);
                }
                Handler handler5 = this.mHandler;
                handler5.sendMessage(handler5.obtainMessage(ENTER_FASTCHARGE_EVENT, getChargerCriticalLog()));
            }
            int i4 = this.mBatteryCurrent;
            if (i4 < 0 && Math.abs(i4) < 10000 && Math.abs(this.mBatteryCurrent) > this.mLastMaxChargeCurrent) {
                this.mLastMaxChargeCurrent = Math.abs(this.mBatteryCurrent);
            }
            int i5 = this.mBatteryTemperature;
            if (i5 > this.mLastMaxChargeTemperature) {
                this.mLastMaxChargeTemperature = i5;
            }
            int i6 = this.mBatteryTemperature;
            if (i6 < this.mLastMinChargeTemperature) {
                this.mLastMinChargeTemperature = i6;
            }
            String newData = String.format(Locale.US, "%d,%d,%d", Integer.valueOf(this.mLastMaxChargeTemperature), Integer.valueOf(this.mLastMinChargeTemperature), Integer.valueOf(this.mLastMaxChargeCurrent));
            String str = this.mBatteryCriticalData;
            if (str == null || !str.equals(newData)) {
                Handler handler6 = this.mHandler;
                handler6.sendMessage(handler6.obtainMessage(UPDATE_BATTERY_HISTORY_EVENT, newData));
                this.mBatteryCriticalData = newData;
            }
            if (!(this.mChargerTechnology == this.mLastChargerTechnology && this.mChargeFastCharger == this.mLastChargeFastCharger && this.mLastPlugType == this.mPlugType && this.mLastFastChgType == this.mFastChgType)) {
                sendAdditionalIntentLocked();
            }
            this.mLastChargerVoltage = this.mChargerVoltage;
            this.mLastBatteryCurrent = this.mBatteryCurrent;
            this.mLastBatteryTemperature = this.mBatteryTemperature;
            this.mLastChargeridVoltage = this.mChargeridVoltage;
            this.mLastChargeFastCharger = this.mChargeFastCharger;
            this.mLastChargerTechnology = this.mChargerTechnology;
            this.mLastPlugType = this.mPlugType;
            this.mLastFastChgType = this.mFastChgType;
        }
    }

    private int[] getChargerCriticalLog() {
        return new int[]{this.mChargeFastCharger ? 1 : 0, this.mPrimalType, this.mPlugType, this.mBatteryTemperature, this.mBatteryLevel, this.mChargeridVoltage};
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeCriticalLog(int[] parameter) {
        StringBuilder critical_log_sb = new StringBuilder();
        critical_log_sb.append("F:" + parameter[0]);
        critical_log_sb.append("\n");
        critical_log_sb.append("primal:");
        critical_log_sb.append(parameter[1]);
        critical_log_sb.append("\n");
        critical_log_sb.append("type:");
        if (parameter[2] == 0) {
            critical_log_sb.append("Unknown");
        } else if (parameter[2] == 1) {
            critical_log_sb.append("AC");
        } else if (parameter[2] == 2) {
            critical_log_sb.append("USB");
        } else {
            Slog.d(TAG, "new charger type plugin / plugout");
            critical_log_sb.append("" + parameter[2]);
        }
        critical_log_sb.append("\n");
        critical_log_sb.append("batt_temp:");
        critical_log_sb.append(parameter[3]);
        critical_log_sb.append("\n");
        critical_log_sb.append("capital:");
        critical_log_sb.append(parameter[4]);
        critical_log_sb.append("\n");
        critical_log_sb.append("chargerid_volt:");
        critical_log_sb.append(parameter[5]);
        String critical_log = critical_log_sb.toString();
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "charger critical_log : " + critical_log);
        }
        if (parameter[2] == 0) {
            OppoManager.writeLogToPartition(626, critical_log, "KERNEL", "charger_plugout", this.charger_plugout_desc);
        } else {
            OppoManager.writeLogToPartition(625, critical_log, "KERNEL", "charger_plugin", this.charger_plugin_desc);
        }
    }

    private void sendAdditionalIntentLocked() {
        final Intent intent = new Intent("android.intent.action.ADDITIONAL_BATTERY_CHANGED");
        intent.addFlags(1610612736);
        intent.putExtra("chargervoltage", this.mChargerVoltage);
        intent.putExtra("batterycurrent", this.mBatteryCurrent);
        intent.putExtra("chargertechnology", this.mChargerTechnology);
        intent.putExtra("chargefastcharger", this.mChargeFastCharger);
        intent.putExtra("chargeplugged", this.mPlugType);
        intent.putExtra("fast_chg_type", this.mFastChgType);
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "Sending ACTION_ADDITIONAL_BATTERY_CHANGED.  chargervoltage: " + this.mChargerVoltage + " ,batterycurrent: " + this.mBatteryCurrent + " ,chargertechnology: " + this.mChargerTechnology + " ,chargefastcharger: " + this.mChargeFastCharger + " ,mPlugType: " + this.mPlugType + " ,mFastChgType: " + this.mFastChgType);
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.OppoBatteryService.AnonymousClass6 */

            public void run() {
                ActivityManagerNative.broadcastStickyIntent(intent, (String) null, -1);
            }
        });
    }

    public int getChargerVoltage() {
        return this.mChargerVoltage;
    }

    public boolean getChargeFastCharger() {
        return this.mChargeFastCharger;
    }

    public boolean dumpAddition(FileDescriptor fd, PrintWriter pw, String[] args) {
        boolean update = true;
        if (!(args == null || args.length == 0)) {
            boolean z = false;
            if (!"-a".equals(args[0])) {
                if (args.length == 3 && "set".equals(args[0])) {
                    String key = args[1];
                    String value = args[2];
                    try {
                        if ("temperature".equals(key)) {
                            this.mBatteryTemperature = Integer.parseInt(value);
                        } else if ("current".equals(key)) {
                            this.mBatteryCurrent = Integer.parseInt(value);
                        } else if ("chargervol".equals(key)) {
                            this.mChargerVoltage = Integer.parseInt(value);
                        } else if ("chargertechnology".equals(key)) {
                            this.mChargerTechnology = Integer.parseInt(value);
                        } else if ("chargefastcharger".equals(key)) {
                            if (Integer.parseInt(value) != 0) {
                                z = true;
                            }
                            this.mChargeFastCharger = z;
                        } else {
                            update = false;
                        }
                        if (update) {
                            this.mUpdatesStopped = true;
                        }
                    } catch (NumberFormatException e) {
                        pw.println("Bad value: " + value);
                    }
                    return update;
                } else if (args.length == 1 && "reset".equals(args[0])) {
                    this.mUpdatesStopped = false;
                    return update;
                } else if (args.length == 1 && "unplug".equals(args[0])) {
                    return true;
                } else {
                    pw.println("Dump current OPPO battery state, or:");
                    pw.println("  set temperature|current|chargervol|chargertechnology|chargefastcharger <value>");
                    pw.println("  reset");
                    pw.println("");
                    return update;
                }
            }
        }
        pw.println("Current OPPO Battery Service state:");
        pw.println("  Charger voltage : " + this.mChargerVoltage);
        pw.println("  Battery current : " + this.mBatteryCurrent);
        pw.println("  ChargerTechnology: " + this.mChargerTechnology);
        pw.println("  ChargeFastCharger: " + this.mChargeFastCharger);
        pw.println("  PlugType: " + this.mPlugType);
        pw.println("  UpdatesStopped: " + this.mUpdatesStopped);
        pw.println("  UsbHwStatus: " + this.mUsbStatus);
        pw.println("  BatteryHwStatus: " + this.mBatteryHwStatus);
        pw.println("  HwStatusIsSet: " + this.mHwStatusIsSet);
        pw.println("  BatteryIcStatus: " + this.mBatteryIcStatus);
        pw.println("  IcStatusIsSet: " + this.mIcStatusIsSet);
        pw.println("");
        return update;
    }

    public boolean ignoreShutdownIfOverTempByOppoLocked() {
        return true;
    }

    public void printArgs(String[] args) {
        if (args != null && args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String str : args) {
                sb.append(str);
                sb.append("    ");
            }
            Slog.i(TAG, "dumpInternal args is : " + sb.toString().trim());
        }
    }

    public void printOppoBatteryFeature(PrintWriter pw) {
        if (this.mIsSellModeVersion) {
            pw.println("  IsSellModeVersion: " + this.mIsSellModeVersion);
        }
        pw.println("  mUsbStatus: " + this.mUsbStatus);
    }

    /* access modifiers changed from: protected */
    public boolean dynamicallyConfigBatteryServiceLogTag(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args == null) {
            pw.println("dynamicallyConfigBatteryServiceLogTag failed for args empty!");
            return false;
        } else if (args.length < 1 || !"log".equals(args[0])) {
            return false;
        } else {
            if (args.length != 3) {
                pw.println("Invalid argument! Get detail help as bellow:");
                logOutBatteryServiceLogTagHelp(pw);
                return true;
            }
            pw.println("dynamicallyConfigBatteryServiceLogTag, args.length:" + args.length);
            for (int index = 0; index < args.length; index++) {
                pw.println("dynamicallyConfigBatteryServiceLogTag, args[" + index + "]:" + args[index]);
            }
            if (args.length < 2) {
                pw.println("dynamicallyConfigBatteryServiceLogTag: args illegal for log config");
                return false;
            }
            String logCategoryTag = args[1];
            boolean on = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(args[2]);
            pw.println("dynamicallyConfigBatteryServiceLogTag, logCategoryTag:" + logCategoryTag + ", on:" + on);
            if ("all".equals(logCategoryTag)) {
                DEBUG = on;
                this.mBas.setDebugSwitchState(on);
                DEBUG = on;
            } else {
                pw.println("Invalid log tag argument! Get detail help as bellow:");
                logOutBatteryServiceLogTagHelp(pw);
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void logOutBatteryServiceLogTagHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 All BatteryService log");
        pw.println("cmd: dumpsys battery log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    private int getCurrentChargeStateForSaleInternal() {
        String tempString;
        String str;
        StringBuilder sb;
        File file = new File("/sys/class/power_supply/battery/mmi_charging_enable");
        BufferedReader reader = null;
        try {
            if (file.exists()) {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                tempString = reader2.readLine();
                try {
                    reader2.close();
                } catch (IOException e) {
                    e1 = e;
                    str = TAG;
                    sb = new StringBuilder();
                }
                if (tempString != null || tempString.trim().length() == 0) {
                    return -1;
                }
                try {
                    return Integer.valueOf(tempString).intValue();
                } catch (NumberFormatException e2) {
                    String str2 = TAG;
                    Slog.e(str2, "readIntFromFile NumberFormatException:" + e2.getMessage());
                    return -1;
                }
            } else {
                Slog.e(TAG, "mmi_charging_enable is no existed");
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        String str3 = TAG;
                        Slog.e(str3, "readIntFromFile io close exception :" + e1.getMessage());
                    }
                }
                return -2;
            }
            sb.append("readIntFromFile io close exception :");
            sb.append(e1.getMessage());
            Slog.e(str, sb.toString());
            if (tempString != null) {
            }
            return -1;
        } catch (IOException e3) {
            tempString = null;
            String str4 = TAG;
            Slog.e(str4, "readIntFromFile io exception:" + e3.getMessage());
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    e1 = e4;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e12) {
                    String str5 = TAG;
                    Slog.e(str5, "readIntFromFile io close exception :" + e12.getMessage());
                }
            }
            throw th;
        }
    }

    public boolean isWirelessStatChange() {
        return (this.mWirelessReserve == this.mLastWirelessReserve && this.mWirelessDeviated == this.mLastWirelessDeviated) ? false : true;
    }
}

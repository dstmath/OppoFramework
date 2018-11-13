package com.android.server;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoManager;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Locale;

public final class OppoBatteryService {
    private static final int AC_PLUG_IN_EVENT = 100000;
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int CHARGERID_UPDATED_EVENT = 100002;
    public static boolean DEBUG = false;
    private static final int ENTER_FASTCHARGE_EVENT = 100004;
    private static final int PLUG_OUT_EVENT = 100003;
    private static final String SEPARATOR = "\n";
    private static final String TAG = OppoBatteryService.class.getSimpleName();
    private static final int UPDATE_BATTERY_HISTORY_EVENT = 100005;
    private static final int USB_PLUG_IN_EVENT = 100001;
    private String charger_plugin_desc = null;
    private String charger_plugout_desc = null;
    private String mBatteryCriticalData = null;
    public int mBatteryCurrent;
    public int mBatteryHwStatus;
    private int mBatteryJumpSoc;
    public int mBatteryJumpSocTotal;
    private int mBatteryLevel;
    public int mBatteryNotifyCode;
    public int mBatteryRealtimeCapacity;
    private int mBatteryRequestPowerOff;
    private int mBatteryTemperature;
    private boolean mChargeFastCharger;
    private int mChargerTechnology;
    private int mChargerVoltage;
    private int mChargeridVoltage;
    private final Context mContext;
    private final Handler mHandler;
    private final ServiceThread mHandlerThread;
    public int mLastBatteryCurrent;
    private int mLastBatteryJumpSoc;
    public int mLastBatteryTemperature;
    private boolean mLastChargeFastCharger;
    private int mLastChargerTechnology;
    private int mLastChargerVoltage;
    private int mLastChargeridVoltage;
    private int mLastMaxChargeCurrent = 0;
    private int mLastMaxChargeTemperature = Integer.MIN_VALUE;
    private int mLastMinChargeTemperature = Integer.MAX_VALUE;
    private int mLastPlugType;
    public int mOtgOnline;
    private int mPlugType;
    private int mPrimalType;
    private boolean mUpdatesStopped = false;

    private class BatteryHandler extends Handler {
        public BatteryHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (OppoBatteryService.DEBUG) {
                Slog.i(OppoBatteryService.TAG, "handleMessage : " + message.what);
            }
            switch (message.what) {
                case OppoBatteryService.AC_PLUG_IN_EVENT /*100000*/:
                case 100001:
                case 100002:
                case 100003:
                case 100004:
                    OppoBatteryService.this.writeCriticalLog((int[]) message.obj);
                    return;
                case 100005:
                    String history = message.obj;
                    Slog.i(OppoBatteryService.TAG, "new battery critical data : " + history);
                    OppoManager.writeCriticalData(OppoManager.TYPE_BATTERY_CHARGE_HISTORY + 1024, history);
                    OppoManager.writeCriticalData(OppoManager.TYPE_BATTERY_CHARGE_HISTORY, history);
                    return;
                default:
                    return;
            }
        }
    }

    public native void native_update();

    public OppoBatteryService(Context context) {
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new BatteryHandler(this.mHandlerThread.getLooper());
        this.charger_plugin_desc = this.mContext.getResources().getString(17041166);
        this.charger_plugout_desc = this.mContext.getResources().getString(17041167);
        this.mBatteryCriticalData = OppoManager.readCriticalData(OppoManager.TYPE_BATTERY_CHARGE_HISTORY + 1024, OppoManager.TYPE_CRITICAL_DATA_SIZE);
        Slog.i(TAG, "load from criticaldata : " + this.mBatteryCriticalData);
        if (this.mBatteryCriticalData != null) {
            String[] historyData = this.mBatteryCriticalData.split(",");
            if (historyData != null && historyData.length == 3) {
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
        }
    }

    public void processAdditionalValuesLocked(int batteryTemperature, int plugType, int batteryLevel) {
        if (!this.mUpdatesStopped) {
            this.mBatteryTemperature = batteryTemperature;
            this.mPlugType = plugType;
            this.mBatteryLevel = batteryLevel;
        }
        if (DEBUG) {
            Slog.d(TAG, "Processing additional values: mChargerVoltage=" + this.mChargerVoltage + ", mLastChargerVoltage=" + this.mLastChargerVoltage + ", mBatteryCurrent=" + this.mBatteryCurrent + ", mLastBatteryCurrent=" + this.mLastBatteryCurrent + ", mBatteryLevel=" + this.mBatteryLevel + ", mBatteryTemperature=" + this.mBatteryTemperature + ", mChargerTechnology=" + this.mChargerTechnology + ", mChargeFastCharger=" + this.mChargeFastCharger + ", mChargeridVoltage=" + this.mChargeridVoltage + ", mLastChargeridVoltage=" + this.mLastChargeridVoltage + ", mPlugType=" + this.mPlugType + ", mLastPlugType=" + this.mLastPlugType);
        }
        if (this.mChargerVoltage != this.mLastChargerVoltage || this.mBatteryCurrent != this.mLastBatteryCurrent || this.mBatteryTemperature != this.mLastBatteryTemperature || this.mChargerTechnology != this.mLastChargerTechnology || this.mChargeFastCharger != this.mLastChargeFastCharger || this.mBatteryJumpSoc != this.mLastBatteryJumpSoc || this.mChargeridVoltage != this.mLastChargeridVoltage || this.mPlugType != this.mLastPlugType) {
            if (this.mBatteryJumpSoc != this.mLastBatteryJumpSoc) {
                this.mBatteryJumpSocTotal += this.mBatteryJumpSoc;
                this.mLastBatteryJumpSoc = this.mBatteryJumpSoc;
            }
            if (this.mPlugType != this.mLastPlugType) {
                if (this.mPlugType == 0) {
                    if (this.mHandler.hasMessages(AC_PLUG_IN_EVENT)) {
                        if (DEBUG) {
                            Slog.i(TAG, "Drop AC_PLUG_IN_EVENT");
                        }
                        this.mHandler.removeMessages(AC_PLUG_IN_EVENT);
                    } else {
                        if (this.mHandler.hasMessages(100003)) {
                            this.mHandler.removeMessages(100003);
                        }
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(100003, getChargerCriticalLog()));
                    }
                } else if (this.mPlugType == 1) {
                    if (this.mHandler.hasMessages(AC_PLUG_IN_EVENT)) {
                        this.mHandler.removeMessages(AC_PLUG_IN_EVENT);
                    }
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(AC_PLUG_IN_EVENT, getChargerCriticalLog()), 2000);
                } else if (this.mPlugType == 2) {
                    if (this.mHandler.hasMessages(100001)) {
                        this.mHandler.removeMessages(100001);
                    }
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(100001, getChargerCriticalLog()));
                } else {
                    Slog.i(TAG, "unknown plug type");
                }
            }
            if (this.mChargeridVoltage != this.mLastChargeridVoltage && this.mChargeridVoltage > 0 && this.mHandler.hasMessages(AC_PLUG_IN_EVENT)) {
                if (DEBUG) {
                    Slog.i(TAG, "mChargeridVoltage updated, update AC_PLUG_IN_EVENT");
                }
                this.mHandler.removeMessages(AC_PLUG_IN_EVENT);
                this.mHandler.sendMessage(this.mHandler.obtainMessage(AC_PLUG_IN_EVENT, getChargerCriticalLog()));
            }
            if (this.mChargeFastCharger && (this.mLastChargeFastCharger ^ 1) != 0) {
                if (this.mHandler.hasMessages(AC_PLUG_IN_EVENT)) {
                    if (DEBUG) {
                        Slog.i(TAG, "mChargeFastCharger updated, drop AC_PLUG_IN_EVENT");
                    }
                    this.mHandler.removeMessages(AC_PLUG_IN_EVENT);
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(100004, getChargerCriticalLog()));
            }
            if (this.mBatteryCurrent < 0 && Math.abs(this.mBatteryCurrent) < 10000 && Math.abs(this.mBatteryCurrent) > this.mLastMaxChargeCurrent) {
                this.mLastMaxChargeCurrent = Math.abs(this.mBatteryCurrent);
            }
            if (this.mBatteryTemperature > this.mLastMaxChargeTemperature) {
                this.mLastMaxChargeTemperature = this.mBatteryTemperature;
            }
            if (this.mBatteryTemperature < this.mLastMinChargeTemperature) {
                this.mLastMinChargeTemperature = this.mBatteryTemperature;
            }
            String newData = String.format(Locale.US, "%d,%d,%d", new Object[]{Integer.valueOf(this.mLastMaxChargeTemperature), Integer.valueOf(this.mLastMinChargeTemperature), Integer.valueOf(this.mLastMaxChargeCurrent)});
            if (this.mBatteryCriticalData == null || (this.mBatteryCriticalData.equals(newData) ^ 1) != 0) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(100005, newData));
                this.mBatteryCriticalData = newData;
            }
            if (!(this.mChargerTechnology == this.mLastChargerTechnology && this.mChargeFastCharger == this.mLastChargeFastCharger && this.mLastPlugType == this.mPlugType)) {
                sendAdditionalIntentLocked();
            }
            this.mLastChargerVoltage = this.mChargerVoltage;
            this.mLastBatteryCurrent = this.mBatteryCurrent;
            this.mLastBatteryTemperature = this.mBatteryTemperature;
            this.mLastChargeridVoltage = this.mChargeridVoltage;
            this.mLastChargeFastCharger = this.mChargeFastCharger;
            this.mLastChargerTechnology = this.mChargerTechnology;
            this.mLastPlugType = this.mPlugType;
        }
    }

    private int[] getChargerCriticalLog() {
        int[] iArr = new int[6];
        iArr[0] = this.mChargeFastCharger ? 1 : 0;
        iArr[1] = this.mPrimalType;
        iArr[2] = this.mPlugType;
        iArr[3] = this.mBatteryTemperature;
        iArr[4] = this.mBatteryLevel;
        iArr[5] = this.mChargeridVoltage;
        return iArr;
    }

    private void writeCriticalLog(int[] parameter) {
        StringBuilder critical_log_sb = new StringBuilder();
        String critical_log = "";
        critical_log_sb.append("F:").append(parameter[0]);
        critical_log_sb.append(SEPARATOR);
        critical_log_sb.append("primal:");
        critical_log_sb.append(parameter[1]);
        critical_log_sb.append(SEPARATOR);
        critical_log_sb.append("type:");
        if (parameter[2] == 0) {
            critical_log_sb.append("Unknown");
        } else if (parameter[2] == 1) {
            critical_log_sb.append("AC");
        } else if (parameter[2] == 2) {
            critical_log_sb.append("USB");
        } else {
            Slog.d(TAG, "new charger type plugin / plugout");
            critical_log_sb.append("").append(parameter[2]);
        }
        critical_log_sb.append(SEPARATOR);
        critical_log_sb.append("batt_temp:");
        critical_log_sb.append(parameter[3]);
        critical_log_sb.append(SEPARATOR);
        critical_log_sb.append("capital:");
        critical_log_sb.append(parameter[4]);
        critical_log_sb.append(SEPARATOR);
        critical_log_sb.append("chargerid_volt:");
        critical_log_sb.append(parameter[5]);
        critical_log = critical_log_sb.toString();
        if (DEBUG) {
            Slog.d(TAG, "charger critical_log : " + critical_log);
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
        if (DEBUG) {
            Slog.d(TAG, "Sending ACTION_ADDITIONAL_BATTERY_CHANGED.  chargervoltage: " + this.mChargerVoltage + " ,batterycurrent: " + this.mBatteryCurrent + " ,chargertechnology: " + this.mChargerTechnology + " ,chargefastcharger: " + this.mChargeFastCharger + " ,mPlugType: " + this.mPlugType);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
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
        boolean z = true;
        boolean update = true;
        if (args == null || args.length == 0 || "-a".equals(args[0])) {
            pw.println("Current OPPO Battery Service state:");
            pw.println("  Charger voltage : " + this.mChargerVoltage);
            pw.println("  Battery current : " + this.mBatteryCurrent);
            pw.println("  ChargerTechnology: " + this.mChargerTechnology);
            pw.println("  ChargeFastCharger: " + this.mChargeFastCharger);
            pw.println("  PlugType: " + this.mPlugType);
            pw.println("  UpdatesStopped: " + this.mUpdatesStopped);
            pw.println("");
        } else if (args.length == 3 && "set".equals(args[0])) {
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
                    if (Integer.parseInt(value) == 0) {
                        z = false;
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
        } else if (args.length == 1 && "reset".equals(args[0])) {
            this.mUpdatesStopped = false;
        } else {
            pw.println("Dump current OPPO battery state, or:");
            pw.println("  set temperature|current|chargervol|chargertechnology|chargefastcharger <value>");
            pw.println("  reset");
            pw.println("");
        }
        return update;
    }
}

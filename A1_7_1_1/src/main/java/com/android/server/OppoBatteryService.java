package com.android.server;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.OppoManager;
import android.util.Slog;
import com.android.server.oppo.IElsaManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class OppoBatteryService {
    private static final int AC_PLUG_IN_EVENT = 0;
    private static final int BATTERY_PLUGGED_NONE = 0;
    private static final int CHARGERID_UPDATED_EVENT = 2;
    public static boolean DEBUG = false;
    private static final int ENTER_FASTCHARGE_EVENT = 4;
    private static final int PLUG_OUT_EVENT = 3;
    private static final String SEPARATOR = "\n";
    private static final String TAG = null;
    private static final int USB_PLUG_IN_EVENT = 1;
    public int mBatteryCurrent;
    private int mBatteryJumpSoc;
    public int mBatteryJumpSocTotal;
    private int mBatteryLevel;
    public int mBatteryNotifyCode;
    public int mBatteryRealtimeCapacity;
    private int mBatteryRequestPowerOff;
    private int mBatteryTemperature;
    private boolean mChargeFastCharger;
    private String mChargerPluginDesc;
    private String mChargerPlugoutDesc;
    private int mChargerTechnology;
    private int mChargerVoltage;
    private int mChargeridVoltage;
    private final Context mContext;
    private final Handler mHandler;
    public int mLastBatteryCurrent;
    private int mLastBatteryJumpSoc;
    private boolean mLastChargeFastCharger;
    private int mLastChargerTechnology;
    private int mLastChargerVoltage;
    private int mLastChargeridVoltage;
    private int mLastPlugType;
    public int mOtgOnline;
    private int mPlugType;
    private int mPrimalType;
    private Handler mSecondHandler;
    private boolean mUpdatesStopped;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.OppoBatteryService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.OppoBatteryService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.OppoBatteryService.<clinit>():void");
    }

    public native void native_update();

    public OppoBatteryService(Context context) {
        this.mUpdatesStopped = false;
        this.mChargerPluginDesc = null;
        this.mChargerPlugoutDesc = null;
        this.mSecondHandler = new Handler() {
            public void handleMessage(Message message) {
                if (OppoBatteryService.DEBUG) {
                    Slog.i(OppoBatteryService.TAG, "handleMessage : " + message.what);
                }
                switch (message.what) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        OppoBatteryService.this.writeCriticalLog((int[]) message.obj);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mContext = context;
        this.mHandler = new Handler(true);
        this.mChargerPluginDesc = this.mContext.getResources().getString(17040984);
        this.mChargerPlugoutDesc = this.mContext.getResources().getString(17040985);
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
        if (this.mChargerVoltage != this.mLastChargerVoltage || this.mBatteryCurrent != this.mLastBatteryCurrent || this.mChargerTechnology != this.mLastChargerTechnology || this.mChargeFastCharger != this.mLastChargeFastCharger || this.mBatteryJumpSoc != this.mLastBatteryJumpSoc || this.mChargeridVoltage != this.mLastChargeridVoltage || this.mPlugType != this.mLastPlugType) {
            if (this.mBatteryJumpSoc != this.mLastBatteryJumpSoc) {
                this.mBatteryJumpSocTotal += this.mBatteryJumpSoc;
                this.mLastBatteryJumpSoc = this.mBatteryJumpSoc;
            }
            if (this.mPlugType != this.mLastPlugType) {
                if (this.mPlugType == 0) {
                    if (this.mSecondHandler.hasMessages(0)) {
                        if (DEBUG) {
                            Slog.i(TAG, "Drop AC_PLUG_IN_EVENT");
                        }
                        this.mSecondHandler.removeMessages(0);
                    } else {
                        if (this.mSecondHandler.hasMessages(3)) {
                            this.mSecondHandler.removeMessages(3);
                        }
                        this.mSecondHandler.sendMessage(this.mSecondHandler.obtainMessage(3, getChargerCriticalLog()));
                    }
                } else if (this.mPlugType == 1) {
                    if (this.mSecondHandler.hasMessages(0)) {
                        this.mSecondHandler.removeMessages(0);
                    }
                    this.mSecondHandler.sendMessageDelayed(this.mSecondHandler.obtainMessage(0, getChargerCriticalLog()), 2000);
                } else if (this.mPlugType == 2) {
                    if (this.mSecondHandler.hasMessages(1)) {
                        this.mSecondHandler.removeMessages(1);
                    }
                    this.mSecondHandler.sendMessage(this.mSecondHandler.obtainMessage(1, getChargerCriticalLog()));
                } else {
                    Slog.i(TAG, "unknown plug type");
                }
            }
            if (this.mChargeridVoltage != this.mLastChargeridVoltage && this.mChargeridVoltage > 0 && this.mSecondHandler.hasMessages(0)) {
                if (DEBUG) {
                    Slog.i(TAG, "mChargeridVoltage updated, update AC_PLUG_IN_EVENT");
                }
                this.mSecondHandler.removeMessages(0);
                this.mSecondHandler.sendMessage(this.mSecondHandler.obtainMessage(0, getChargerCriticalLog()));
            }
            if (this.mChargeFastCharger && !this.mLastChargeFastCharger) {
                if (this.mSecondHandler.hasMessages(0)) {
                    if (DEBUG) {
                        Slog.i(TAG, "mChargeFastCharger updated, drop AC_PLUG_IN_EVENT");
                    }
                    this.mSecondHandler.removeMessages(0);
                }
                this.mSecondHandler.sendMessage(this.mSecondHandler.obtainMessage(4, getChargerCriticalLog()));
            }
            if (!(this.mChargerTechnology == this.mLastChargerTechnology && this.mChargeFastCharger == this.mLastChargeFastCharger && this.mLastPlugType == this.mPlugType)) {
                sendAdditionalIntentLocked();
            }
            this.mLastChargerVoltage = this.mChargerVoltage;
            this.mLastBatteryCurrent = this.mBatteryCurrent;
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
        String critical_log = IElsaManager.EMPTY_PACKAGE;
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
            critical_log_sb.append(IElsaManager.EMPTY_PACKAGE).append(parameter[2]);
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
            OppoManager.writeLogToPartition(626, critical_log, "KERNEL", "charger_plugout", this.mChargerPlugoutDesc);
        } else {
            OppoManager.writeLogToPartition(625, critical_log, "KERNEL", "charger_plugin", this.mChargerPluginDesc);
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
            pw.println(IElsaManager.EMPTY_PACKAGE);
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
            pw.println(IElsaManager.EMPTY_PACKAGE);
        }
        return update;
    }
}

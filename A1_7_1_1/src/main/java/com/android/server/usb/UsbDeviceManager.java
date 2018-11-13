package com.android.server.usb;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.net.ConnectivityManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings.Global;
import android.util.Pair;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.FgThread;
import com.android.server.LocationManagerService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import com.oppo.hypnus.Hypnus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class UsbDeviceManager {
    private static final int ACCESSORY_REQUEST_TIMEOUT = 10000;
    private static final String ACCESSORY_START_MATCH = "DEVPATH=/devices/virtual/misc/usb_accessory";
    private static final String ACM_PORT_INDEX_PATH = "/sys/class/android_usb/android0/f_acm/port_index";
    private static final int AUDIO_MODE_SOURCE = 1;
    private static final String AUDIO_SOURCE_PCM_PATH = "/sys/class/android_usb/android0/f_audio_source/pcm";
    private static final String BOOT_MODE_PROPERTY = "ro.bootmode";
    private static final boolean DEBUG = false;
    private static final String FUNCTIONS_PATH = "/sys/class/android_usb/android0/functions";
    private static final int IFACE_BR_MAIN_ADDED = 1;
    private static final int IFACE_BR_MAIN_NONE = 0;
    private static final String MIDI_ALSA_PATH = "/sys/class/android_usb/android0/f_midi/alsa";
    private static final int MSG_BOOT_COMPLETED = 4;
    private static final int MSG_ENABLE_ACM = 101;
    private static final int MSG_ENABLE_ADB = 1;
    private static final int MSG_SET_BYPASS = 103;
    private static final int MSG_SET_BYPASS_MODE = 102;
    private static final int MSG_SET_CURRENT_FUNCTIONS = 2;
    private static final int MSG_SET_USB_DATA_UNLOCKED = 6;
    private static final int MSG_SYSTEM_READY = 3;
    private static final int MSG_UPDATE_HOST_STATE = 8;
    private static final int MSG_UPDATE_STATE = 0;
    private static final int MSG_UPDATE_USER_RESTRICTIONS = 7;
    private static final int MSG_USER_SWITCHED = 5;
    private static final String MTP_STATE_MATCH = "DEVPATH=/devices/virtual/misc/mtp_usb";
    private static final String RNDIS_ETH_ADDR_PATH = "/sys/class/android_usb/android0/f_rndis/ethaddr";
    private static final String STATE_PATH = "/sys/class/android_usb/android0/state";
    private static final String TAG = "UsbDeviceManager";
    private static final int UPDATE_DELAY = 1000;
    private static final String USB_CONFIG_PROPERTY = "sys.usb.config";
    private static final String USB_PERSISTENT_CONFIG_PROPERTY = "persist.sys.usb.config";
    private static final String USB_STATE_MATCH = "DEVPATH=/devices/virtual/android_usb/android0";
    private static final String USB_STATE_PROPERTY = "sys.usb.state";
    private static final boolean bEvdoDtViaSupport = false;
    private static int br0State;
    private long mAccessoryModeRequestTime;
    private String[] mAccessoryStrings;
    private boolean mAcmEnabled;
    private String mAcmPortIdx;
    private boolean mAdbEnabled;
    private boolean mAudioSourceEnabled;
    private boolean mBootCompleted;
    private Intent mBroadcastedIntent;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    @GuardedBy("mLock")
    private UsbSettingsManager mCurrentSettings;
    private UsbDebuggingManager mDebuggingManager;
    private UsbHandler mHandler;
    private final boolean mHasUsbAccessory;
    private final BroadcastReceiver mHostReceiver;
    private boolean mHwDisconnected;
    private Hypnus mHyp;
    private boolean mIsUsbSimSecurity;
    private boolean mIsUsbSimSecurityCheck;
    private final Object mLock;
    private int mMidiCard;
    private int mMidiDevice;
    private boolean mMidiEnabled;
    private boolean mMtpAskDisconnect;
    private NotificationManager mNotificationManager;
    private INetworkManagementService mNwService;
    private Map<String, List<Pair<String, String>>> mOemModeMap;
    private final UEventObserver mUEventObserver;
    private final UsbAlsaManager mUsbAlsaManager;
    private boolean mUsbConfigured;
    private String mUsbStorageType;
    private boolean mUseUsbNotification;

    private class AcmSettingsObserver extends ContentObserver {
        public AcmSettingsObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            UsbDeviceManager.this.mHandler.sendMessage(101, Integer.valueOf(Global.getInt(UsbDeviceManager.this.mContentResolver, "acm_enabled", 0)));
        }
    }

    private class AdbSettingsObserver extends ContentObserver {
        public AdbSettingsObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            boolean enable = Global.getInt(UsbDeviceManager.this.mContentResolver, "adb_enabled", 0) > 0;
            UsbDeviceManager.this.mHandler.sendMessage(1, enable);
            if (!enable && SystemProperties.getInt("persist.sys.adb.engineermode", 1) == 0) {
                if (UsbDeviceManager.DEBUG) {
                    Slog.i(UsbDeviceManager.TAG, "reset engineermode adb property");
                }
                SystemProperties.set("persist.sys.allcommode", "false");
                SystemProperties.set("persist.sys.oppo.usbactive", "false");
                SystemProperties.set("persist.sys.adb.engineermode", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            }
        }
    }

    private final class UsbHandler extends Handler {
        private boolean mAdbNotificationShown;
        private Bypass mBypass;
        private boolean mConfigured;
        private boolean mConnected;
        private UsbAccessory mCurrentAccessory;
        private String mCurrentFunctions;
        private boolean mCurrentFunctionsApplied;
        private int mCurrentUser = -10000;
        private boolean mHostConnected;
        private Mbim mMbim;
        private boolean mSinkPower;
        private boolean mSourcePower;
        private UsbCheck mUsbCheck;
        private boolean mUsbDataUnlocked;
        private int mUsbNotificationId;
        private boolean mUsbSetBypassWithTether = false;

        private final class Bypass {
            private static final String ACTION_RADIO_AVAILABLE = "android.intent.action.RADIO_AVAILABLE";
            private static final String ACTION_USB_BYPASS_GETBYPASS = "com.via.bypass.action.getbypass";
            private static final String ACTION_USB_BYPASS_GETBYPASS_RESULT = "com.via.bypass.action.getbypass_result";
            private static final String ACTION_USB_BYPASS_SETBYPASS = "com.via.bypass.action.setbypass";
            private static final String ACTION_USB_BYPASS_SETBYPASS_RESULT = "com.via.bypass.action.setbypass_result";
            private static final String ACTION_USB_BYPASS_SETFUNCTION = "com.via.bypass.action.setfunction";
            private static final String ACTION_USB_BYPASS_SETTETHERFUNCTION = "com.via.bypass.action.settetherfunction";
            private static final String ACTION_VIA_ETS_DEV_CHANGED = "via.cdma.action.ets.dev.changed";
            private static final String ACTION_VIA_SET_ETS_DEV = "via.cdma.action.set.ets.dev";
            private static final String EXTRAL_VIA_ETS_DEV = "via.cdma.extral.ets.dev";
            private static final String USB_FUNCTION_BYPASS = "via_bypass";
            private static final String VALUE_BYPASS_CODE = "com.via.bypass.bypass_code";
            private static final String VALUE_ENABLE_BYPASS = "com.via.bypass.enable_bypass";
            private static final String VALUE_ISSET_BYPASS = "com.via.bypass.isset_bypass";
            private int mBypassAll;
            private final int[] mBypassCodes = new int[]{1, 2, 4, 8, 16};
            private File[] mBypassFiles;
            private final String[] mBypassName;
            private final BroadcastReceiver mBypassReceiver;
            private int mBypassToSet;
            private boolean mEtsDevInUse;

            public Bypass() {
                String[] strArr = new String[5];
                strArr[0] = "gps";
                strArr[1] = "pcv";
                strArr[2] = "atc";
                strArr[3] = "ets";
                strArr[4] = "data";
                this.mBypassName = strArr;
                this.mBypassAll = 0;
                this.mEtsDevInUse = false;
                this.mBypassReceiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        if (UsbDeviceManager.DEBUG) {
                            Slog.i(UsbDeviceManager.TAG, "onReceive=" + intent.getAction());
                        }
                        if (intent.getAction() == null) {
                            return;
                        }
                        Intent reintent;
                        if (intent.getAction().equals(Bypass.ACTION_USB_BYPASS_SETFUNCTION)) {
                            if (Boolean.valueOf(intent.getBooleanExtra(Bypass.VALUE_ENABLE_BYPASS, false)).booleanValue()) {
                                UsbDeviceManager.this.setCurrentFunctions(Bypass.USB_FUNCTION_BYPASS);
                            } else {
                                Bypass.this.closeBypassFunction();
                            }
                        } else if (intent.getAction().equals(Bypass.ACTION_USB_BYPASS_SETTETHERFUNCTION)) {
                            ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
                            if (Boolean.valueOf(intent.getBooleanExtra(Bypass.VALUE_ENABLE_BYPASS, false)).booleanValue()) {
                                Slog.i(UsbDeviceManager.TAG, "Enable the byass with Tethering");
                                UsbHandler.this.mUsbSetBypassWithTether = true;
                                cm.setUsbTethering(true);
                                return;
                            }
                            Slog.i(UsbDeviceManager.TAG, "disable the byass with Tethering");
                            Bypass.this.updateBypassMode(0);
                            cm.setUsbTethering(false);
                        } else if (intent.getAction().equals(Bypass.ACTION_USB_BYPASS_SETBYPASS)) {
                            int bypasscode = intent.getIntExtra(Bypass.VALUE_BYPASS_CODE, -1);
                            if (bypasscode < 0 || bypasscode > Bypass.this.mBypassAll) {
                                Bypass.this.notifySetBypassResult(Boolean.valueOf(false), Bypass.this.getCurrentBypassMode());
                            } else {
                                Bypass.this.setBypassMode(bypasscode);
                            }
                        } else if (intent.getAction().equals(Bypass.ACTION_USB_BYPASS_GETBYPASS)) {
                            reintent = new Intent(Bypass.ACTION_USB_BYPASS_GETBYPASS_RESULT);
                            reintent.putExtra(Bypass.VALUE_BYPASS_CODE, Bypass.this.getCurrentBypassMode());
                            UsbDeviceManager.this.mContext.sendBroadcast(reintent);
                        } else if (intent.getAction().equals(Bypass.ACTION_VIA_ETS_DEV_CHANGED)) {
                            int bypass;
                            if (intent.getBooleanExtra("set.ets.dev.result", false)) {
                                bypass = Bypass.this.mBypassToSet;
                            } else {
                                bypass = Bypass.this.getCurrentBypassMode();
                            }
                            Message m = Message.obtain(UsbDeviceManager.this.mHandler, 103);
                            m.arg1 = bypass;
                            UsbHandler.this.sendMessage(m);
                        } else if (intent.getAction().equals(Bypass.ACTION_RADIO_AVAILABLE) && Bypass.this.mEtsDevInUse) {
                            reintent = new Intent(Bypass.ACTION_VIA_SET_ETS_DEV);
                            reintent.putExtra(Bypass.EXTRAL_VIA_ETS_DEV, 1);
                            UsbDeviceManager.this.mContext.sendBroadcast(reintent);
                        }
                    }
                };
                this.mBypassFiles = new File[this.mBypassName.length];
                for (int i = 0; i < this.mBypassName.length; i++) {
                    this.mBypassFiles[i] = new File("/sys/class/usb_rawbulk/" + this.mBypassName[i] + "/enable");
                    this.mBypassAll += this.mBypassCodes[i];
                }
                if (UsbDeviceManager.bEvdoDtViaSupport) {
                    IntentFilter intent = new IntentFilter(ACTION_USB_BYPASS_SETFUNCTION);
                    intent.addAction(ACTION_USB_BYPASS_SETTETHERFUNCTION);
                    intent.addAction(ACTION_USB_BYPASS_SETBYPASS);
                    intent.addAction(ACTION_USB_BYPASS_GETBYPASS);
                    intent.addAction(ACTION_VIA_ETS_DEV_CHANGED);
                    intent.addAction(ACTION_RADIO_AVAILABLE);
                    UsbDeviceManager.this.mContext.registerReceiver(this.mBypassReceiver, intent);
                }
            }

            private int getCurrentBypassMode() {
                int bypassmode = 0;
                int i = 0;
                while (i < this.mBypassCodes.length) {
                    try {
                        String code;
                        if (i == 2) {
                            code = SystemProperties.get("sys.cp.bypass.at", "0");
                        } else {
                            code = FileUtils.readTextFile(this.mBypassFiles[i], 0, null);
                        }
                        if (UsbDeviceManager.DEBUG) {
                            Slog.d(UsbDeviceManager.TAG, "'" + this.mBypassFiles[i].getAbsolutePath() + "' value is " + code);
                        }
                        if (code != null && code.trim().equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                            bypassmode |= this.mBypassCodes[i];
                        }
                        i++;
                    } catch (IOException e) {
                        Slog.e(UsbDeviceManager.TAG, "failed to read bypass mode code!");
                    }
                }
                if (UsbDeviceManager.DEBUG) {
                    Slog.d(UsbDeviceManager.TAG, "getCurrentBypassMode()=" + bypassmode);
                }
                return bypassmode;
            }

            private void setBypass(int bypassmode) {
                Slog.d(UsbDeviceManager.TAG, "setBypass bypass = " + bypassmode);
                int bypassResult = getCurrentBypassMode();
                if (bypassmode == bypassResult) {
                    Slog.d(UsbDeviceManager.TAG, "setBypass bypass == oldbypass!!");
                    notifySetBypassResult(Boolean.valueOf(true), bypassResult);
                    return;
                }
                int i = 0;
                while (i < this.mBypassCodes.length) {
                    try {
                        if ((this.mBypassCodes[i] & bypassmode) != 0) {
                            if (UsbDeviceManager.DEBUG) {
                                Slog.d(UsbDeviceManager.TAG, "Write '" + this.mBypassFiles[i].getAbsolutePath() + LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                            }
                            if (i == 2) {
                                SystemProperties.set("sys.cp.bypass.at", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                            } else {
                                FileUtils.stringToFile(this.mBypassFiles[i].getAbsolutePath(), LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                            }
                            bypassResult |= this.mBypassCodes[i];
                        } else {
                            if (UsbDeviceManager.DEBUG) {
                                Slog.d(UsbDeviceManager.TAG, "Write '" + this.mBypassFiles[i].getAbsolutePath() + "0");
                            }
                            if (i == 2) {
                                SystemProperties.set("sys.cp.bypass.at", "0");
                            } else {
                                FileUtils.stringToFile(this.mBypassFiles[i].getAbsolutePath(), "0");
                            }
                            if ((this.mBypassCodes[i] & bypassResult) != 0) {
                                bypassResult ^= this.mBypassCodes[i];
                            }
                        }
                        if (UsbDeviceManager.DEBUG) {
                            Slog.d(UsbDeviceManager.TAG, "Write '" + this.mBypassFiles[i].getAbsolutePath() + "' successsfully!");
                        }
                        i++;
                    } catch (IOException e) {
                        Slog.e(UsbDeviceManager.TAG, "failed to operate bypass!");
                        notifySetBypassResult(Boolean.valueOf(false), bypassResult);
                    }
                }
                notifySetBypassResult(Boolean.valueOf(true), bypassResult);
                Slog.d(UsbDeviceManager.TAG, "setBypass success bypassResult = " + bypassResult);
            }

            void updateBypassMode(int bypassmode) {
                Slog.d(UsbDeviceManager.TAG, "updateBypassMode");
                if (setEtsDev(bypassmode)) {
                    Slog.d(UsbDeviceManager.TAG, "updateBypassMode mBypassToSet = " + this.mBypassToSet);
                    this.mBypassToSet = bypassmode;
                    return;
                }
                setBypass(bypassmode);
            }

            private boolean setEtsDev(int bypass) {
                int oldBypass = getCurrentBypassMode();
                Slog.d(UsbDeviceManager.TAG, "setEtsDev bypass = " + bypass + " oldBypass = " + oldBypass);
                Intent reintent;
                if ((this.mBypassCodes[3] & bypass) != 0 && (this.mBypassCodes[3] & oldBypass) == 0) {
                    Slog.d(UsbDeviceManager.TAG, "setEtsDev mEtsDevInUse = true");
                    reintent = new Intent(ACTION_VIA_SET_ETS_DEV);
                    reintent.putExtra(EXTRAL_VIA_ETS_DEV, 1);
                    UsbDeviceManager.this.mContext.sendBroadcast(reintent);
                    this.mEtsDevInUse = true;
                    return true;
                } else if ((this.mBypassCodes[3] & bypass) != 0 || (this.mBypassCodes[3] & oldBypass) == 0) {
                    return false;
                } else {
                    Slog.d(UsbDeviceManager.TAG, "setEtsDev mEtsDevInUse = false");
                    reintent = new Intent(ACTION_VIA_SET_ETS_DEV);
                    reintent.putExtra(EXTRAL_VIA_ETS_DEV, 0);
                    UsbDeviceManager.this.mContext.sendBroadcast(reintent);
                    this.mEtsDevInUse = false;
                    return true;
                }
            }

            private void setBypassMode(int bypassmode) {
                if (UsbDeviceManager.DEBUG) {
                    Slog.d(UsbDeviceManager.TAG, "setBypassMode()=" + bypassmode);
                }
                Message m = Message.obtain(UsbDeviceManager.this.mHandler, 102);
                m.arg1 = bypassmode;
                UsbHandler.this.sendMessage(m);
            }

            private void notifySetBypassResult(Boolean isset, int bypassCode) {
                if (UsbDeviceManager.this.mBootCompleted) {
                    Intent intent = new Intent(ACTION_USB_BYPASS_SETBYPASS_RESULT);
                    intent.putExtra(VALUE_ISSET_BYPASS, isset);
                    intent.putExtra(VALUE_BYPASS_CODE, bypassCode);
                    UsbDeviceManager.this.mContext.sendBroadcast(intent);
                }
            }

            void closeBypassFunction() {
                if (UsbDeviceManager.DEBUG) {
                    Slog.d(UsbDeviceManager.TAG, "closeBypassFunction() CurrentFunctions = " + UsbHandler.this.mCurrentFunctions + ",DefaultFunctions=" + UsbHandler.this.getDefaultFunctions());
                }
                updateBypassMode(0);
                if (UsbHandler.this.mCurrentFunctions.contains(USB_FUNCTION_BYPASS)) {
                    UsbHandler.this.setEnabledFunctions(null, false);
                }
            }
        }

        private final class Mbim {
            private static final String ACTION_USB_MBIM_SETFUNCTION = "com.mbim.action.setfunction";
            private static final String USB_FUNCTION_MBIM = "mbim_dun";
            private static final String VALUE_ENABLE_MBIM = "com.mbim.enable";
            private final BroadcastReceiver mMbimReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (UsbDeviceManager.DEBUG) {
                        Slog.i(UsbDeviceManager.TAG, "onReceive=" + intent.getAction());
                    }
                    if (intent.getAction() != null && intent.getAction().equals(Mbim.ACTION_USB_MBIM_SETFUNCTION)) {
                        if (intent.getIntExtra(Mbim.VALUE_ENABLE_MBIM, 0) == 1) {
                            UsbDeviceManager.this.setCurrentFunctions(Mbim.USB_FUNCTION_MBIM);
                        } else {
                            Mbim.this.closeMbimFunction();
                        }
                    }
                }
            };

            public Mbim() {
                UsbDeviceManager.this.mContext.registerReceiver(this.mMbimReceiver, new IntentFilter(ACTION_USB_MBIM_SETFUNCTION));
            }

            void closeMbimFunction() {
                if (UsbDeviceManager.DEBUG) {
                    Slog.d(UsbDeviceManager.TAG, "closeMbimFunction() CurrentFunctions = " + UsbHandler.this.mCurrentFunctions + ",DefaultFunctions=" + UsbHandler.this.getDefaultFunctions());
                }
                if (UsbHandler.this.mCurrentFunctions.contains(USB_FUNCTION_MBIM)) {
                    UsbHandler.this.setEnabledFunctions(null, false);
                }
            }
        }

        private final class UsbCheck {
            private static final String INTENT_USB_ACTIVATION = "com.mediatek.action.USB_ACTIVATION";
            private final BroadcastReceiver mUsbCheckReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(UsbCheck.INTENT_USB_ACTIVATION)) {
                        UsbDeviceManager.this.mIsUsbSimSecurityCheck = true;
                        Slog.d(UsbDeviceManager.TAG, "USB SIM Check Unlock!!");
                        UsbHandler.this.setEnabledFunctions(UsbHandler.this.mCurrentFunctions, true);
                    }
                }
            };

            public UsbCheck() {
                UsbDeviceManager.this.mContext.registerReceiver(this.mUsbCheckReceiver, new IntentFilter(INTENT_USB_ACTIVATION));
            }
        }

        public UsbHandler(Looper looper) {
            super(looper);
            try {
                if (UsbDeviceManager.bEvdoDtViaSupport) {
                    this.mBypass = new Bypass();
                }
                this.mMbim = new Mbim();
                this.mUsbCheck = new UsbCheck();
                this.mCurrentFunctions = SystemProperties.get(UsbDeviceManager.USB_CONFIG_PROPERTY, "none");
                this.mCurrentFunctionsApplied = this.mCurrentFunctions.equals(SystemProperties.get(UsbDeviceManager.USB_STATE_PROPERTY));
                UsbDeviceManager.this.mAdbEnabled = UsbManager.containsFunction(getDefaultFunctions(), "adb");
                UsbDeviceManager.this.mAcmEnabled = UsbManager.containsFunction(getDefaultFunctions(), "acm");
                UsbDeviceManager.this.mAcmPortIdx = IElsaManager.EMPTY_PACKAGE;
                setEnabledFunctions(null, false);
                updateState(FileUtils.readTextFile(new File(UsbDeviceManager.STATE_PATH), 0, null).trim());
                String value = SystemProperties.get("persist.radio.port_index", IElsaManager.EMPTY_PACKAGE);
                if (UsbDeviceManager.DEBUG) {
                    Slog.d(UsbDeviceManager.TAG, "persist.radio.port_index:" + value);
                }
                if (!(value == null || value.isEmpty() || validPortNum(value) <= 0)) {
                    UsbDeviceManager.this.mAcmPortIdx = value;
                    writeFile(UsbDeviceManager.ACM_PORT_INDEX_PATH, UsbDeviceManager.this.mAcmPortIdx);
                    SystemProperties.set(UsbDeviceManager.USB_CONFIG_PROPERTY, UsbManager.addFunction(this.mCurrentFunctions, "acm"));
                }
                if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"))) {
                    Slog.d(UsbDeviceManager.TAG, "Aging test version, not register observer to listen for settings changes");
                } else {
                    UsbDeviceManager.this.mContentResolver.registerContentObserver(Global.getUriFor("adb_enabled"), false, new AdbSettingsObserver());
                }
                UsbDeviceManager.this.mContentResolver.registerContentObserver(Global.getUriFor("acm_enabled"), false, new AcmSettingsObserver());
                UsbDeviceManager.this.mUEventObserver.startObserving(UsbDeviceManager.USB_STATE_MATCH);
                UsbDeviceManager.this.mUEventObserver.startObserving(UsbDeviceManager.ACCESSORY_START_MATCH);
                UsbDeviceManager.this.mUEventObserver.startObserving(UsbDeviceManager.MTP_STATE_MATCH);
            } catch (Exception e) {
                Slog.e(UsbDeviceManager.TAG, "Error initializing UsbHandler", e);
            }
        }

        public void sendMessage(int what, boolean arg) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.arg1 = arg ? 1 : 0;
            sendMessage(m);
        }

        public void sendMessage(int what, Object arg) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.obj = arg;
            sendMessage(m);
        }

        public void updateState(String state) {
            int connected;
            int configured;
            int i = 0;
            if ("HWDISCONNECTED".equals(state)) {
                connected = 0;
                configured = 0;
                UsbDeviceManager.this.mHwDisconnected = true;
            } else if ("DISCONNECTED".equals(state)) {
                connected = 0;
                configured = 0;
                UsbDeviceManager.this.mHwDisconnected = false;
            } else if ("CONNECTED".equals(state)) {
                connected = 1;
                configured = 0;
                UsbDeviceManager.this.mHwDisconnected = false;
            } else if ("CONFIGURED".equals(state)) {
                connected = 1;
                configured = 1;
                UsbDeviceManager.this.mHwDisconnected = false;
            } else if ("MTPASKDISCONNECT".equals(state)) {
                Slog.w(UsbDeviceManager.TAG, "MTPASKDISCONNECT");
                UsbDeviceManager.this.mMtpAskDisconnect = true;
                UsbDeviceManager.this.setCurrentFunctions(this.mCurrentFunctions);
                return;
            } else {
                Slog.e(UsbDeviceManager.TAG, "unknown state " + state);
                return;
            }
            removeMessages(0);
            Message msg = Message.obtain(this, 0);
            msg.arg1 = connected;
            msg.arg2 = configured;
            if (connected == 0) {
                i = 1000;
            }
            sendMessageDelayed(msg, (long) i);
        }

        public void updateHostState(UsbPort port, UsbPortStatus status) {
            int i;
            int i2 = 1;
            boolean hostConnected = status.getCurrentDataRole() == 1;
            boolean sourcePower = status.getCurrentPowerRole() == 1;
            boolean sinkPower = status.getCurrentPowerRole() == 2;
            if (UsbDeviceManager.DEBUG) {
                Slog.i(UsbDeviceManager.TAG, "updateHostState " + port + " status=" + status);
            }
            SomeArgs args = SomeArgs.obtain();
            if (hostConnected) {
                i = 1;
            } else {
                i = 0;
            }
            args.argi1 = i;
            if (sourcePower) {
                i = 1;
            } else {
                i = 0;
            }
            args.argi2 = i;
            if (!sinkPower) {
                i2 = 0;
            }
            args.argi3 = i2;
            obtainMessage(8, args).sendToTarget();
        }

        private boolean waitForState(String state) {
            String value = null;
            for (int i = 0; i < 20; i++) {
                value = SystemProperties.get(UsbDeviceManager.USB_STATE_PROPERTY);
                if (state.equals(value)) {
                    return true;
                }
                SystemClock.sleep(50);
            }
            Slog.e(UsbDeviceManager.TAG, "waitForState(" + state + ") FAILED: got " + value);
            return false;
        }

        private boolean setUsbConfig(String config) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setUsbConfig(" + config + ")");
            }
            SystemProperties.set(UsbDeviceManager.USB_CONFIG_PROPERTY, config);
            return waitForState(config);
        }

        private void setUsbDataUnlocked(boolean enable) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setUsbDataUnlocked: " + enable);
            }
            this.mUsbDataUnlocked = enable;
            setEnabledFunctions(this.mCurrentFunctions, true);
        }

        private void setAdbEnabled(boolean enable) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setAdbEnabled: " + enable);
            }
            if (enable != UsbDeviceManager.this.mAdbEnabled) {
                UsbDeviceManager.this.mAdbEnabled = enable;
                String oldFunctions = getDefaultFunctions();
                String newFunctions = applyAdbFunction(oldFunctions);
                if (!oldFunctions.equals(newFunctions)) {
                    SystemProperties.set(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, newFunctions);
                }
                setEnabledFunctions(this.mCurrentFunctions, false);
                updateAdbNotification();
            }
            if (UsbDeviceManager.this.mDebuggingManager != null) {
                UsbDeviceManager.this.mDebuggingManager.setAdbEnabled(UsbDeviceManager.this.mAdbEnabled);
            }
        }

        private void setAcmEnabled(boolean enable) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setAcmEnabled: " + enable);
            }
            if (enable != UsbDeviceManager.this.mAcmEnabled) {
                UsbDeviceManager.this.mAcmEnabled = enable;
                setEnabledFunctions(this.mCurrentFunctions, true);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x004d A:{SYNTHETIC, Splitter: B:15:0x004d} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0070 A:{SYNTHETIC, Splitter: B:21:0x0070} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void writeFile(String path, String data) {
            Throwable th;
            FileOutputStream fos = null;
            try {
                FileOutputStream fos2 = new FileOutputStream(path);
                try {
                    fos2.write(data.getBytes());
                    if (fos2 != null) {
                        try {
                            fos2.close();
                        } catch (IOException e) {
                            Slog.w(UsbDeviceManager.TAG, "Unable to close fos at path: " + path);
                        }
                    }
                    fos = fos2;
                } catch (IOException e2) {
                    fos = fos2;
                    try {
                        Slog.w(UsbDeviceManager.TAG, "Unable to write " + path);
                        if (fos == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e3) {
                                Slog.w(UsbDeviceManager.TAG, "Unable to close fos at path: " + path);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fos = fos2;
                    if (fos != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                Slog.w(UsbDeviceManager.TAG, "Unable to write " + path);
                if (fos == null) {
                    try {
                        fos.close();
                    } catch (IOException e5) {
                        Slog.w(UsbDeviceManager.TAG, "Unable to close fos at path: " + path);
                    }
                }
            }
        }

        private void setEnabledFunctions(String functions, boolean forceRestart) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setEnabledFunctions functions=" + functions + ", " + "forceRestart=" + forceRestart);
            }
            if (!UsbDeviceManager.this.mIsUsbSimSecurity || UsbDeviceManager.this.mIsUsbSimSecurityCheck || !SystemProperties.get("persist.sys.usb.activation", "no").equals("no") || functions == null) {
                if (UsbManager.containsFunction(this.mCurrentFunctions, "bicr")) {
                    if (this.mCurrentFunctions.equals(functions)) {
                        forceRestart = false;
                    } else {
                        Slog.d(UsbDeviceManager.TAG, "setEnabledFunctions - [CLEAN USB BICR SETTING]");
                        SystemProperties.set("sys.usb.bicr", "no");
                    }
                }
                String br0Name = SystemProperties.get("ro.tethering.bridge.interface");
                if (!(!SystemProperties.get("ro.mtk_md_direct_tethering").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || br0Name == null || br0Name.isEmpty())) {
                    if (functions != null && UsbManager.containsFunction(functions, "rndis") && !UsbManager.containsFunction(this.mCurrentFunctions, "rndis")) {
                        Slog.i(UsbDeviceManager.TAG, "addBridge");
                        UsbDeviceManager.br0State = 1;
                        try {
                            UsbDeviceManager.this.mNwService.addBridge(br0Name);
                        } catch (RemoteException e) {
                            Slog.e(UsbDeviceManager.TAG, "Error addBridge: ", e);
                        }
                    } else if ((functions == null || !UsbManager.containsFunction(functions, "rndis")) && UsbManager.containsFunction(this.mCurrentFunctions, "rndis")) {
                        Slog.i(UsbDeviceManager.TAG, "deleteBridge");
                        try {
                            UsbDeviceManager.this.mNwService.deleteBridge(br0Name);
                        } catch (RemoteException e2) {
                            Slog.e(UsbDeviceManager.TAG, "Error deleteBridge: ", e2);
                        }
                        UsbDeviceManager.br0State = 0;
                    }
                }
                String oldFunctions = this.mCurrentFunctions;
                boolean oldFunctionsApplied = this.mCurrentFunctionsApplied;
                if (!trySetEnabledFunctions(functions, forceRestart)) {
                    if (oldFunctionsApplied && !oldFunctions.equals(functions)) {
                        Slog.e(UsbDeviceManager.TAG, "Failsafe 1: Restoring previous USB functions.");
                        if (trySetEnabledFunctions(oldFunctions, false)) {
                            return;
                        }
                    }
                    Slog.e(UsbDeviceManager.TAG, "Failsafe 2: Restoring default USB functions.");
                    if (!trySetEnabledFunctions(null, false)) {
                        Slog.e(UsbDeviceManager.TAG, "Failsafe 3: Restoring empty function list (with ADB if enabled).");
                        if (!trySetEnabledFunctions("none", false)) {
                            Slog.e(UsbDeviceManager.TAG, "Unable to set any USB functions!");
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            Slog.d(UsbDeviceManager.TAG, "Usb is non-activated!");
            trySetEnabledFunctions("none", false);
        }

        private boolean trySetEnabledFunctions(String functions, boolean forceRestart) {
            if (functions == null) {
                this.mUsbSetBypassWithTether = false;
                functions = getDefaultFunctions();
            }
            if (UsbDeviceManager.bEvdoDtViaSupport) {
                if ((UsbManager.containsFunction(functions, "rndis") || UsbManager.containsFunction(functions, "eem")) && this.mUsbSetBypassWithTether) {
                    functions = UsbManager.addFunction(functions, "via_bypass");
                    Slog.d(UsbDeviceManager.TAG, "add the bypass functions to tethering : " + functions);
                }
                this.mUsbSetBypassWithTether = false;
            }
            functions = UsbDeviceManager.this.applyOemOverrideFunction(applyAcmFunction(applyAdbFunction(functions)));
            if (!(functions == null || !UsbManager.containsFunction(functions, "none") || "none".equals(functions))) {
                Slog.d(UsbDeviceManager.TAG, "contains none, remove USB_FUNCTION_NONE\n");
                functions = UsbManager.removeFunction(functions, "none");
            }
            if (UsbDeviceManager.this.mIsUsbSimSecurity && !UsbDeviceManager.this.mIsUsbSimSecurityCheck && SystemProperties.get("persist.sys.usb.activation", "no").equals("no")) {
                Slog.d(UsbDeviceManager.TAG, "Usb is non-activated!");
                functions = UsbManager.removeFunction(UsbManager.removeFunction(UsbManager.removeFunction(functions, "adb"), "acm"), "dual_acm");
            }
            if (!(this.mCurrentFunctions.equals(functions) && this.mCurrentFunctionsApplied && !forceRestart)) {
                Slog.i(UsbDeviceManager.TAG, "Setting USB config to " + functions);
                this.mCurrentFunctions = functions;
                this.mCurrentFunctionsApplied = false;
                SystemClock.sleep(100);
                setUsbConfig("none");
                SystemClock.sleep(100);
                if (functions != null && "none".equals(functions)) {
                    if (this.mConnected) {
                        Slog.d(UsbDeviceManager.TAG, "functions is  none, return \n");
                        return true;
                    } else if (UsbDeviceManager.this.mBootCompleted) {
                        functions = UsbManager.addFunction(UsbManager.removeFunction(functions, "none"), "adb");
                        Slog.d(UsbDeviceManager.TAG, "functions is  none and mConnected is false, we change to use adb config! functions=" + functions);
                    }
                }
                if (setUsbConfig(functions)) {
                    this.mCurrentFunctionsApplied = true;
                } else {
                    Slog.e(UsbDeviceManager.TAG, "Failed to switch USB config to " + functions);
                    String br0Name = SystemProperties.get("ro.tethering.bridge.interface");
                    if (UsbDeviceManager.br0State == 1) {
                        try {
                            Slog.e(UsbDeviceManager.TAG, "deleteBridge");
                            UsbDeviceManager.this.mNwService.deleteBridge(br0Name);
                        } catch (RemoteException e) {
                            Slog.e(UsbDeviceManager.TAG, "Error deleteBridge: ", e);
                        }
                        UsbDeviceManager.br0State = 0;
                    }
                    return false;
                }
            }
            return true;
        }

        private String applyAdbFunction(String functions) {
            if (UsbDeviceManager.this.mAdbEnabled) {
                return UsbManager.addFunction(functions, "adb");
            }
            return UsbManager.removeFunction(functions, "adb");
        }

        private int validPortNum(String port) {
            String[] tmp = port.split(",");
            int portNum = 0;
            int i = 0;
            while (i < tmp.length) {
                if (Integer.valueOf(tmp[i]).intValue() > 0 && Integer.valueOf(tmp[i]).intValue() < 5) {
                    portNum++;
                }
                i++;
            }
            return portNum == tmp.length ? portNum : 0;
        }

        private String applyAcmFunction(String functions) {
            String acmIdx = SystemProperties.get("sys.usb.acm_idx", IElsaManager.EMPTY_PACKAGE);
            Slog.d(UsbDeviceManager.TAG, "applyAcmFunction - sys.usb.acm_idx=" + acmIdx + ",mAcmPortIdx=" + UsbDeviceManager.this.mAcmPortIdx);
            if (UsbDeviceManager.this.mAcmEnabled || !((acmIdx == null || acmIdx.isEmpty()) && (UsbDeviceManager.this.mAcmPortIdx == null || UsbDeviceManager.this.mAcmPortIdx.isEmpty()))) {
                int portNum = 0;
                String portStr = IElsaManager.EMPTY_PACKAGE;
                if (!acmIdx.isEmpty()) {
                    portNum = validPortNum(acmIdx);
                    if (portNum > 0) {
                        portStr = acmIdx;
                        UsbDeviceManager.this.mAcmPortIdx = acmIdx;
                    }
                } else if (!UsbDeviceManager.this.mAcmPortIdx.isEmpty()) {
                    portNum = validPortNum(UsbDeviceManager.this.mAcmPortIdx);
                    if (portNum > 0) {
                        portStr = UsbDeviceManager.this.mAcmPortIdx;
                    }
                }
                Slog.d(UsbDeviceManager.TAG, "applyAcmFunction - port_num=" + portNum);
                if (portNum > 0) {
                    Slog.d(UsbDeviceManager.TAG, "applyAcmFunction - Write port_str=" + portStr);
                    writeFile(UsbDeviceManager.ACM_PORT_INDEX_PATH, portStr);
                }
                functions = UsbManager.addFunction(UsbManager.removeFunction(UsbManager.removeFunction(functions, "acm"), "dual_acm"), portNum == 2 ? "dual_acm" : "acm");
            } else {
                functions = UsbManager.removeFunction(UsbManager.removeFunction(functions, "acm"), "dual_acm");
            }
            Slog.d(UsbDeviceManager.TAG, "applyAcmFunction - functions: " + functions);
            return functions;
        }

        private boolean isUsbTransferAllowed() {
            return !((UserManager) UsbDeviceManager.this.mContext.getSystemService("user")).hasUserRestriction("no_usb_file_transfer");
        }

        private void updateCurrentAccessory() {
            boolean enteringAccessoryMode = UsbDeviceManager.this.mAccessoryModeRequestTime > 0 ? SystemClock.elapsedRealtime() < UsbDeviceManager.this.mAccessoryModeRequestTime + 10000 : false;
            Slog.d(UsbDeviceManager.TAG, "updateCurrentAccessory: enteringAccessoryMode = " + enteringAccessoryMode + ", mAccessoryModeRequestTime = " + UsbDeviceManager.this.mAccessoryModeRequestTime + ", mConfigured = " + this.mConfigured);
            if (this.mConfigured && enteringAccessoryMode) {
                if (UsbDeviceManager.this.mAccessoryStrings != null) {
                    this.mCurrentAccessory = new UsbAccessory(UsbDeviceManager.this.mAccessoryStrings);
                    Slog.d(UsbDeviceManager.TAG, "entering USB accessory mode: " + this.mCurrentAccessory);
                    if (UsbDeviceManager.this.mBootCompleted) {
                        UsbDeviceManager.this.getCurrentSettings().accessoryAttached(this.mCurrentAccessory);
                        UsbDeviceManager.this.mAccessoryModeRequestTime = 0;
                        return;
                    }
                    return;
                }
                Slog.e(UsbDeviceManager.TAG, "nativeGetAccessoryStrings failed");
            } else if (enteringAccessoryMode) {
                Slog.d(UsbDeviceManager.TAG, "USB Accessory Wrong state!!, need to FIXME");
            } else {
                Slog.d(UsbDeviceManager.TAG, "exited USB accessory mode");
                setEnabledFunctions(null, false);
                if (this.mCurrentAccessory != null) {
                    if (UsbDeviceManager.this.mBootCompleted) {
                        UsbDeviceManager.this.getCurrentSettings().accessoryDetached(this.mCurrentAccessory);
                    }
                    this.mCurrentAccessory = null;
                    UsbDeviceManager.this.mAccessoryStrings = null;
                }
            }
        }

        private boolean isUsbStateChanged(Intent intent) {
            Set<String> keySet = intent.getExtras().keySet();
            if (UsbDeviceManager.this.mBroadcastedIntent == null) {
                for (String key : keySet) {
                    if (intent.getBooleanExtra(key, false) && !"none".equals(key)) {
                        return true;
                    }
                }
            } else if (!keySet.equals(UsbDeviceManager.this.mBroadcastedIntent.getExtras().keySet())) {
                return true;
            } else {
                for (String key2 : keySet) {
                    if (intent.getBooleanExtra(key2, false) != UsbDeviceManager.this.mBroadcastedIntent.getBooleanExtra(key2, false)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void updateUsbStateBroadcastIfNeeded() {
            Intent intent = new Intent("android.hardware.usb.action.USB_STATE");
            intent.addFlags(805306368);
            intent.addFlags(4194304);
            intent.putExtra("connected", this.mConnected);
            intent.putExtra("host_connected", this.mHostConnected);
            intent.putExtra("configured", this.mConfigured);
            intent.putExtra("unlocked", isUsbTransferAllowed() ? this.mUsbDataUnlocked : false);
            intent.putExtra("USB_HW_DISCONNECTED", UsbDeviceManager.this.mHwDisconnected);
            if (this.mCurrentFunctions != null) {
                String[] functions = this.mCurrentFunctions.split(",");
                for (String function : functions) {
                    if (!"none".equals(function)) {
                        intent.putExtra(function, true);
                    }
                }
            }
            if (isUsbStateChanged(intent)) {
                if (UsbDeviceManager.this.mHyp == null) {
                    UsbDeviceManager.this.mHyp = new Hypnus();
                }
                if (UsbDeviceManager.this.mHyp != null) {
                    UsbDeviceManager.this.mHyp.hypnusSetAction(19, 5000);
                    Slog.d(UsbDeviceManager.TAG, "hypnusSetAction ACTION_BURST_ANR +5000");
                }
                if (UsbDeviceManager.DEBUG) {
                    Slog.d(UsbDeviceManager.TAG, "broadcasting " + intent + " extras: " + intent.getExtras());
                }
                UsbDeviceManager.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                UsbDeviceManager.this.mBroadcastedIntent = intent;
                return;
            }
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "skip broadcasting " + intent + " extras: " + intent.getExtras());
            }
        }

        private void updateUsbFunctions() {
            updateAudioSourceFunction();
            updateMidiFunction();
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x0052  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateAudioSourceFunction() {
            FileNotFoundException e;
            Throwable th;
            boolean enabled = UsbManager.containsFunction(this.mCurrentFunctions, "audio_source");
            if (enabled != UsbDeviceManager.this.mAudioSourceEnabled) {
                int card = -1;
                int device = -1;
                if (enabled) {
                    Scanner scanner = null;
                    try {
                        Scanner scanner2 = new Scanner(new File(UsbDeviceManager.AUDIO_SOURCE_PCM_PATH));
                        try {
                            card = scanner2.nextInt();
                            device = scanner2.nextInt();
                            if (scanner2 != null) {
                                scanner2.close();
                            }
                        } catch (FileNotFoundException e2) {
                            e = e2;
                            scanner = scanner2;
                        } catch (Throwable th2) {
                            th = th2;
                            scanner = scanner2;
                            if (scanner != null) {
                                scanner.close();
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        try {
                            Slog.e(UsbDeviceManager.TAG, "could not open audio source PCM file", e);
                            if (scanner != null) {
                                scanner.close();
                            }
                            UsbDeviceManager.this.mUsbAlsaManager.setAccessoryAudioState(enabled, card, device);
                            UsbDeviceManager.this.mAudioSourceEnabled = enabled;
                        } catch (Throwable th3) {
                            th = th3;
                            if (scanner != null) {
                            }
                            throw th;
                        }
                    }
                }
                UsbDeviceManager.this.mUsbAlsaManager.setAccessoryAudioState(enabled, card, device);
                UsbDeviceManager.this.mAudioSourceEnabled = enabled;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:26:0x0075  */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0071  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x006a  */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0075  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateMidiFunction() {
            FileNotFoundException e;
            UsbAlsaManager -get25;
            boolean z;
            Throwable th;
            boolean enabled = UsbManager.containsFunction(this.mCurrentFunctions, "midi");
            if (enabled != UsbDeviceManager.this.mMidiEnabled) {
                if (enabled) {
                    Scanner scanner = null;
                    try {
                        Scanner scanner2 = new Scanner(new File(UsbDeviceManager.MIDI_ALSA_PATH));
                        try {
                            UsbDeviceManager.this.mMidiCard = scanner2.nextInt();
                            UsbDeviceManager.this.mMidiDevice = scanner2.nextInt();
                            if (scanner2 != null) {
                                scanner2.close();
                            }
                        } catch (FileNotFoundException e2) {
                            e = e2;
                            scanner = scanner2;
                            try {
                                Slog.e(UsbDeviceManager.TAG, "could not open MIDI PCM file", e);
                                enabled = false;
                                if (scanner != null) {
                                    scanner.close();
                                }
                                UsbDeviceManager.this.mMidiEnabled = enabled;
                                -get25 = UsbDeviceManager.this.mUsbAlsaManager;
                                if (UsbDeviceManager.this.mMidiEnabled) {
                                }
                                -get25.setPeripheralMidiState(z, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
                            } catch (Throwable th2) {
                                th = th2;
                                if (scanner != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            scanner = scanner2;
                            if (scanner != null) {
                                scanner.close();
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        Slog.e(UsbDeviceManager.TAG, "could not open MIDI PCM file", e);
                        enabled = false;
                        if (scanner != null) {
                        }
                        UsbDeviceManager.this.mMidiEnabled = enabled;
                        -get25 = UsbDeviceManager.this.mUsbAlsaManager;
                        if (UsbDeviceManager.this.mMidiEnabled) {
                        }
                        -get25.setPeripheralMidiState(z, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
                    }
                }
                UsbDeviceManager.this.mMidiEnabled = enabled;
            }
            -get25 = UsbDeviceManager.this.mUsbAlsaManager;
            if (UsbDeviceManager.this.mMidiEnabled) {
                z = this.mConfigured;
            } else {
                z = false;
            }
            -get25.setPeripheralMidiState(z, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            boolean z2 = true;
            switch (msg.what) {
                case 0:
                    this.mConnected = msg.arg1 == 1;
                    if (msg.arg2 != 1) {
                        z2 = false;
                    }
                    this.mConfigured = z2;
                    UsbDeviceManager.this.mUsbConfigured = this.mConfigured;
                    if (!this.mConnected) {
                        this.mUsbDataUnlocked = false;
                    }
                    updateUsbNotification();
                    updateAdbNotification();
                    if (UsbManager.containsFunction(this.mCurrentFunctions, "accessory")) {
                        updateCurrentAccessory();
                    } else if (!this.mConnected) {
                        Slog.d(UsbDeviceManager.TAG, "mConnected is false,setEnabledFunctions to default");
                        setEnabledFunctions(null, false);
                    }
                    if (UsbDeviceManager.this.mBootCompleted) {
                        updateUsbStateBroadcastIfNeeded();
                        updateUsbFunctions();
                    }
                    if (UsbDeviceManager.bEvdoDtViaSupport && !this.mConnected) {
                        this.mBypass.updateBypassMode(0);
                        return;
                    }
                    return;
                case 1:
                    if (msg.arg1 != 1) {
                        z2 = false;
                    }
                    setAdbEnabled(z2);
                    return;
                case 2:
                    String functions = msg.obj;
                    if (UsbDeviceManager.this.mMtpAskDisconnect) {
                        setEnabledFunctions(functions, true);
                        UsbDeviceManager.this.mMtpAskDisconnect = false;
                        return;
                    }
                    setEnabledFunctions(functions, false);
                    return;
                case 3:
                    updateUsbNotification();
                    updateAdbNotification();
                    updateUsbStateBroadcastIfNeeded();
                    updateUsbFunctions();
                    return;
                case 4:
                    UsbDeviceManager.this.mBootCompleted = true;
                    if (this.mCurrentAccessory != null) {
                        UsbDeviceManager.this.getCurrentSettings().accessoryAttached(this.mCurrentAccessory);
                    }
                    if (UsbDeviceManager.this.mDebuggingManager != null) {
                        UsbDeviceManager.this.mDebuggingManager.setAdbEnabled(UsbDeviceManager.this.mAdbEnabled);
                        return;
                    }
                    return;
                case 5:
                    if (this.mCurrentUser != msg.arg1) {
                        boolean active;
                        if (UsbManager.containsFunction(this.mCurrentFunctions, "mtp")) {
                            active = true;
                        } else {
                            active = UsbManager.containsFunction(this.mCurrentFunctions, "ptp");
                        }
                        if (this.mUsbDataUnlocked && active && this.mCurrentUser != -10000) {
                            Slog.v(UsbDeviceManager.TAG, "Current user switched to " + this.mCurrentUser + "; resetting USB host stack for MTP or PTP");
                            this.mUsbDataUnlocked = false;
                            setEnabledFunctions(this.mCurrentFunctions, true);
                        }
                        this.mCurrentUser = msg.arg1;
                        return;
                    }
                    return;
                case 6:
                    if (msg.arg1 != 1) {
                        z2 = false;
                    }
                    setUsbDataUnlocked(z2);
                    return;
                case 7:
                    setEnabledFunctions(this.mCurrentFunctions, false);
                    return;
                case 8:
                    boolean z3;
                    SomeArgs args = msg.obj;
                    this.mHostConnected = args.argi1 == 1;
                    if (args.argi2 == 1) {
                        z3 = true;
                    } else {
                        z3 = false;
                    }
                    this.mSourcePower = z3;
                    if (args.argi3 != 1) {
                        z2 = false;
                    }
                    this.mSinkPower = z2;
                    args.recycle();
                    updateUsbNotification();
                    if (UsbDeviceManager.this.mBootCompleted) {
                        updateUsbStateBroadcastIfNeeded();
                        return;
                    }
                    return;
                case 101:
                    int portNum = ((Integer) msg.obj).intValue();
                    if (portNum >= 1 && portNum <= 4) {
                        UsbDeviceManager.this.mAcmPortIdx = String.valueOf(portNum);
                    } else if (portNum == 5) {
                        UsbDeviceManager.this.mAcmPortIdx = "1,3";
                    } else {
                        UsbDeviceManager.this.mAcmPortIdx = IElsaManager.EMPTY_PACKAGE;
                    }
                    Slog.d(UsbDeviceManager.TAG, "mAcmPortIdx=" + UsbDeviceManager.this.mAcmPortIdx);
                    if (!UsbDeviceManager.this.mAcmPortIdx.isEmpty()) {
                        z = true;
                    }
                    setAcmEnabled(z);
                    return;
                case 102:
                    if (UsbDeviceManager.bEvdoDtViaSupport) {
                        this.mBypass.updateBypassMode(msg.arg1);
                        return;
                    }
                    return;
                case 103:
                    if (UsbDeviceManager.bEvdoDtViaSupport) {
                        this.mBypass.setBypass(msg.arg1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        public UsbAccessory getCurrentAccessory() {
            return this.mCurrentAccessory;
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "TongXi.Li@Plf.Framework, modify for oppo_usb ", property = OppoRomType.ROM)
        private void updateUsbNotification() {
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "TongXi.Li@Plf.Framework, modify for oppo_usb ", property = OppoRomType.ROM)
        private void updateAdbNotification() {
        }

        private String getDefaultFunctions() {
            return SystemProperties.get(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, "none");
        }

        public void dump(IndentingPrintWriter pw) {
            pw.println("USB Device State:");
            pw.println("  mCurrentFunctions: " + this.mCurrentFunctions);
            pw.println("  mCurrentFunctionsApplied: " + this.mCurrentFunctionsApplied);
            pw.println("  mConnected: " + this.mConnected);
            pw.println("  mConfigured: " + this.mConfigured);
            pw.println("  mUsbDataUnlocked: " + this.mUsbDataUnlocked);
            pw.println("  mCurrentAccessory: " + this.mCurrentAccessory);
            pw.println("  mHostConnected: " + this.mHostConnected);
            pw.println("  mSourcePower: " + this.mSourcePower);
            pw.println("  mSinkPower: " + this.mSinkPower);
            try {
                pw.println("  Kernel state: " + FileUtils.readTextFile(new File(UsbDeviceManager.STATE_PATH), 0, null).trim());
                pw.println("  Kernel function list: " + FileUtils.readTextFile(new File(UsbDeviceManager.FUNCTIONS_PATH), 0, null).trim());
            } catch (IOException e) {
                pw.println("IOException: " + e);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.usb.UsbDeviceManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.usb.UsbDeviceManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usb.UsbDeviceManager.<clinit>():void");
    }

    private native String[] nativeGetAccessoryStrings();

    private native int nativeGetAudioMode();

    private native boolean nativeIsStartRequested();

    private native ParcelFileDescriptor nativeOpenAccessory();

    public UsbDeviceManager(Context context, UsbAlsaManager alsaManager) {
        this.mHyp = null;
        this.mAccessoryModeRequestTime = 0;
        this.mLock = new Object();
        this.mIsUsbSimSecurityCheck = false;
        this.mHwDisconnected = true;
        this.mUsbConfigured = false;
        this.mUEventObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                if (UsbDeviceManager.DEBUG) {
                    Slog.v(UsbDeviceManager.TAG, "USB UEVENT: " + event.toString());
                }
                String state = event.get("USB_STATE");
                String accessory = event.get("ACCESSORY");
                if (state != null) {
                    UsbDeviceManager.this.mHandler.updateState(state);
                } else if ("START".equals(accessory)) {
                    if (UsbDeviceManager.DEBUG) {
                        Slog.d(UsbDeviceManager.TAG, "got accessory start");
                    }
                    UsbDeviceManager.this.startAccessoryMode();
                }
            }
        };
        this.mHostReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UsbDeviceManager.this.mHandler.updateHostState((UsbPort) intent.getParcelableExtra("port"), (UsbPortStatus) intent.getParcelableExtra("portStatus"));
            }
        };
        this.mContext = context;
        this.mUsbAlsaManager = alsaManager;
        this.mContentResolver = context.getContentResolver();
        this.mHasUsbAccessory = this.mContext.getPackageManager().hasSystemFeature("android.hardware.usb.accessory");
        this.mIsUsbSimSecurity = false;
        this.mMtpAskDisconnect = false;
        initRndisAddress();
        readOemUsbOverrideConfig();
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_usb_cba_support")) && "OP01".equals(SystemProperties.get("persist.operator.optr"))) {
            Slog.d(TAG, "Have USB SIM Security!!");
            this.mIsUsbSimSecurity = true;
        }
        this.mHandler = new UsbHandler(FgThread.get().getLooper());
        if (nativeIsStartRequested()) {
            if (DEBUG) {
                Slog.d(TAG, "accessory attached at boot");
            }
            startAccessoryMode();
        }
        boolean secureAdbEnabled = SystemProperties.getBoolean("ro.adb.secure", false);
        boolean dataEncrypted = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("vold.decrypt"));
        if (secureAdbEnabled && !dataEncrypted) {
            this.mDebuggingManager = new UsbDebuggingManager(context);
        }
        this.mContext.registerReceiver(this.mHostReceiver, new IntentFilter("android.hardware.usb.action.USB_PORT_CHANGED"));
        this.mNwService = Stub.asInterface(ServiceManager.getService("network_management"));
    }

    private UsbSettingsManager getCurrentSettings() {
        UsbSettingsManager usbSettingsManager;
        synchronized (this.mLock) {
            usbSettingsManager = this.mCurrentSettings;
        }
        return usbSettingsManager;
    }

    public void systemReady() {
        boolean massStorageSupported;
        boolean z;
        int i = 1;
        if (DEBUG) {
            Slog.d(TAG, "systemReady");
        }
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
        String config = SystemProperties.get(USB_PERSISTENT_CONFIG_PROPERTY, "none");
        this.mUsbStorageType = SystemProperties.get("ro.sys.usb.storage.type", "mtp");
        Slog.d(TAG, "systemReady - mUsbStorageType: " + this.mUsbStorageType + ", config: " + config);
        StorageManager storageManager = StorageManager.from(this.mContext);
        StorageVolume primary = storageManager.getPrimaryVolume();
        if (primary != null) {
            massStorageSupported = primary.allowMassStorage();
        } else {
            massStorageSupported = false;
        }
        if (massStorageSupported) {
            z = false;
        } else {
            z = this.mContext.getResources().getBoolean(17956900);
        }
        this.mUseUsbNotification = z;
        if (this.mUsbStorageType.equals("mass_storage")) {
            StorageVolume[] volumes = storageManager.getVolumeList();
            if (volumes != null) {
                for (StorageVolume allowMassStorage : volumes) {
                    if (allowMassStorage.allowMassStorage()) {
                        Slog.d(TAG, "systemReady - massStorageSupported: " + massStorageSupported);
                        massStorageSupported = true;
                        break;
                    }
                }
            }
            if (massStorageSupported) {
                z = false;
            } else {
                z = true;
            }
            this.mUseUsbNotification = z;
        } else {
            Slog.d(TAG, "systemReady - MTP(+UMS)");
            this.mUseUsbNotification = true;
        }
        try {
            ContentResolver contentResolver = this.mContentResolver;
            String str = "adb_enabled";
            if (!this.mAdbEnabled) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        } catch (SecurityException e) {
            Slog.d(TAG, "ADB_ENABLED is restricted.");
        }
        this.mHandler.sendEmptyMessage(3);
    }

    public void bootCompleted() {
        if (DEBUG) {
            Slog.d(TAG, "boot completed");
        }
        this.mHandler.sendEmptyMessage(4);
    }

    public void setCurrentUser(int userId, UsbSettingsManager settings) {
        synchronized (this.mLock) {
            this.mCurrentSettings = settings;
            this.mHandler.obtainMessage(5, userId, 0).sendToTarget();
        }
    }

    public void updateUserRestrictions() {
        this.mHandler.sendEmptyMessage(7);
    }

    private void startAccessoryMode() {
        boolean z = true;
        if (this.mHasUsbAccessory) {
            boolean enableAccessory;
            this.mAccessoryStrings = nativeGetAccessoryStrings();
            boolean enableAudio = nativeGetAudioMode() == 1;
            if (this.mAccessoryStrings == null || this.mAccessoryStrings[0] == null) {
                enableAccessory = false;
            } else {
                if (this.mAccessoryStrings[1] == null) {
                    z = false;
                }
                enableAccessory = z;
            }
            String functions = null;
            if (enableAccessory && enableAudio) {
                functions = "accessory,audio_source";
            } else if (enableAccessory) {
                functions = "accessory";
            } else if (enableAudio) {
                functions = "audio_source";
            }
            if (functions != null) {
                this.mAccessoryModeRequestTime = SystemClock.elapsedRealtime();
                setCurrentFunctions(functions);
            }
        }
    }

    private static void initRndisAddress() {
        int[] address = new int[6];
        address[0] = 2;
        String serial = SystemProperties.get("ro.serialno", "1234567890ABCDEF");
        int serialLength = serial.length();
        for (int i = 0; i < serialLength; i++) {
            int i2 = (i % 5) + 1;
            address[i2] = address[i2] ^ serial.charAt(i);
        }
        Object[] objArr = new Object[6];
        objArr[0] = Integer.valueOf(address[0]);
        objArr[1] = Integer.valueOf(address[1]);
        objArr[2] = Integer.valueOf(address[2]);
        objArr[3] = Integer.valueOf(address[3]);
        objArr[4] = Integer.valueOf(address[4]);
        objArr[5] = Integer.valueOf(address[5]);
        try {
            FileUtils.stringToFile(RNDIS_ETH_ADDR_PATH, String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X", objArr));
        } catch (IOException e) {
            Slog.e(TAG, "failed to write to /sys/class/android_usb/android0/f_rndis/ethaddr");
        }
    }

    public UsbAccessory getCurrentAccessory() {
        return this.mHandler.getCurrentAccessory();
    }

    public ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
        UsbAccessory currentAccessory = this.mHandler.getCurrentAccessory();
        if (currentAccessory == null) {
            throw new IllegalArgumentException("no accessory attached");
        } else if (currentAccessory.equals(accessory)) {
            getCurrentSettings().checkPermission(accessory);
            return nativeOpenAccessory();
        } else {
            throw new IllegalArgumentException(accessory.toString() + " does not match current accessory " + currentAccessory);
        }
    }

    public boolean isFunctionEnabled(String function) {
        return UsbManager.containsFunction(SystemProperties.get(USB_CONFIG_PROPERTY), function);
    }

    public void setCurrentFunctions(String functions) {
        if (DEBUG) {
            Slog.d(TAG, "setCurrentFunctions(" + functions + ")");
        }
        this.mHandler.sendMessage(2, (Object) functions);
    }

    public int getCurrentState() {
        int state = 0;
        if (this.mUsbConfigured) {
            state = 1;
        }
        if (DEBUG) {
            Slog.d(TAG, "getCurrentState - " + state);
        }
        return state;
    }

    public void setUsbDataUnlocked(boolean unlocked) {
        if (DEBUG) {
            Slog.d(TAG, "setUsbDataUnlocked(" + unlocked + ")");
        }
        this.mHandler.sendMessage(6, unlocked);
    }

    private void readOemUsbOverrideConfig() {
        String[] configList = this.mContext.getResources().getStringArray(17236021);
        if (configList != null) {
            for (String config : configList) {
                String[] items = config.split(":");
                if (items.length == 3) {
                    if (this.mOemModeMap == null) {
                        this.mOemModeMap = new HashMap();
                    }
                    List<Pair<String, String>> overrideList = (List) this.mOemModeMap.get(items[0]);
                    if (overrideList == null) {
                        overrideList = new LinkedList();
                        this.mOemModeMap.put(items[0], overrideList);
                    }
                    overrideList.add(new Pair(items[1], items[2]));
                }
            }
        }
    }

    private String applyOemOverrideFunction(String usbFunctions) {
        if (usbFunctions == null || this.mOemModeMap == null) {
            return usbFunctions;
        }
        List<Pair<String, String>> overrides = (List) this.mOemModeMap.get(SystemProperties.get(BOOT_MODE_PROPERTY, "unknown"));
        if (overrides != null) {
            for (Pair<String, String> pair : overrides) {
                if (((String) pair.first).equals(usbFunctions)) {
                    Slog.d(TAG, "OEM USB override: " + ((String) pair.first) + " ==> " + ((String) pair.second));
                    return (String) pair.second;
                }
            }
        }
        return usbFunctions;
    }

    public void allowUsbDebugging(boolean alwaysAllow, String publicKey) {
        if (this.mDebuggingManager != null) {
            this.mDebuggingManager.allowUsbDebugging(alwaysAllow, publicKey);
        }
    }

    public void denyUsbDebugging() {
        if (this.mDebuggingManager != null) {
            this.mDebuggingManager.denyUsbDebugging();
        }
    }

    public void clearUsbDebuggingKeys() {
        if (this.mDebuggingManager != null) {
            this.mDebuggingManager.clearUsbDebuggingKeys();
            return;
        }
        throw new RuntimeException("Cannot clear Usb Debugging keys, UsbDebuggingManager not enabled");
    }

    public void dump(IndentingPrintWriter pw) {
        if (this.mHandler != null) {
            this.mHandler.dump(pw);
        }
        if (this.mDebuggingManager != null) {
            this.mDebuggingManager.dump(pw);
        }
    }
}

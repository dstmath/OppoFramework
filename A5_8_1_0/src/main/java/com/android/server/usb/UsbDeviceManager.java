package com.android.server.usb;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class UsbDeviceManager {
    private static final int ACCESSORY_REQUEST_TIMEOUT = 10000;
    private static final String ACCESSORY_START_MATCH = "DEVPATH=/devices/virtual/misc/usb_accessory";
    private static final String ADB_NOTIFICATION_CHANNEL_ID_TV = "usbdevicemanager.adb.tv";
    private static final int AUDIO_MODE_SOURCE = 1;
    private static final String AUDIO_SOURCE_PCM_PATH = "/sys/class/android_usb/android0/f_audio_source/pcm";
    private static final String BOOT_MODE_PROPERTY = "ro.bootmode";
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String FUNCTIONS_PATH = "/sys/class/android_usb/android0/functions";
    private static final String MIDI_ALSA_PATH = "/sys/class/android_usb/android0/f_midi/alsa";
    private static final int MSG_ACCESSORY_MODE_ENTER_TIMEOUT = 8;
    private static final int MSG_BOOT_COMPLETED = 4;
    private static final int MSG_ENABLE_ADB = 1;
    private static final int MSG_LOCALE_CHANGED = 11;
    private static final int MSG_SET_CURRENT_FUNCTIONS = 2;
    private static final int MSG_SYSTEM_READY = 3;
    private static final int MSG_UPDATE_CHARGING_STATE = 9;
    private static final int MSG_UPDATE_HOST_STATE = 10;
    private static final int MSG_UPDATE_PORT_STATE = 7;
    private static final int MSG_UPDATE_STATE = 0;
    private static final int MSG_UPDATE_USER_RESTRICTIONS = 6;
    private static final int MSG_USER_SWITCHED = 5;
    private static final String NORMAL_BOOT = "normal";
    private static final String RNDIS_ETH_ADDR_PATH = "/sys/class/android_usb/android0/f_rndis/ethaddr";
    private static final String STATE_PATH = "/sys/class/android_usb/android0/state";
    private static final String TAG = "UsbDeviceManager";
    private static final int UPDATE_DELAY = 1000;
    private static final String USB_CONFIG_PROPERTY = "sys.usb.config";
    private static final String USB_PERSISTENT_CONFIG_PROPERTY = "persist.sys.usb.config";
    private static final String USB_STATE_MATCH = "DEVPATH=/devices/virtual/android_usb/android0";
    private static final String USB_STATE_PROPERTY = "sys.usb.state";
    private static Set<Integer> sBlackListedInterfaces = new HashSet();
    private String[] mAccessoryStrings;
    private boolean mAdbEnabled;
    private boolean mAudioSourceEnabled;
    private boolean mBootCompleted;
    private Intent mBroadcastedIntent;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    @GuardedBy("mLock")
    private UsbProfileGroupSettingsManager mCurrentSettings;
    private UsbDebuggingManager mDebuggingManager;
    private UsbHandler mHandler;
    private final boolean mHasUsbAccessory;
    private final Object mLock = new Object();
    private int mMidiCard;
    private int mMidiDevice;
    private boolean mMidiEnabled;
    private NotificationManager mNotificationManager;
    private HashMap<String, HashMap<String, Pair<String, String>>> mOemModeMap;
    private boolean mPendingBootBroadcast;
    private final UsbSettingsManager mSettingsManager;
    private final UEventObserver mUEventObserver = new UEventObserver() {
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
    private final UsbAlsaManager mUsbAlsaManager;
    private boolean mUseUsbNotification;

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
        private boolean mAudioAccessoryConnected;
        private boolean mAudioAccessorySupported;
        private boolean mConfigured;
        private boolean mConnected;
        private UsbAccessory mCurrentAccessory;
        private String mCurrentFunctions;
        private boolean mCurrentFunctionsApplied;
        private String mCurrentOemFunctions;
        private int mCurrentUser = -10000;
        private boolean mHideUsbNotification;
        private boolean mHostConnected;
        private boolean mSinkPower;
        private boolean mSourcePower;
        private boolean mSupportsAllCombinations;
        private boolean mUsbCharging;
        private boolean mUsbDataUnlocked;
        private int mUsbNotificationId;

        public UsbHandler(Looper looper) {
            super(looper);
            try {
                if (isNormalBoot()) {
                    this.mCurrentFunctions = SystemProperties.get(UsbDeviceManager.USB_CONFIG_PROPERTY, "none");
                    this.mCurrentFunctionsApplied = this.mCurrentFunctions.equals(SystemProperties.get(UsbDeviceManager.USB_STATE_PROPERTY));
                } else {
                    this.mCurrentFunctions = SystemProperties.get(UsbDeviceManager.getPersistProp(true), "none");
                    this.mCurrentFunctionsApplied = SystemProperties.get(UsbDeviceManager.USB_CONFIG_PROPERTY, "none").equals(SystemProperties.get(UsbDeviceManager.USB_STATE_PROPERTY));
                }
                UsbDeviceManager.this.mAdbEnabled = UsbManager.containsFunction(SystemProperties.get(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY), "adb");
                String persisted = SystemProperties.get(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY);
                if (UsbManager.containsFunction(persisted, "mtp") || UsbManager.containsFunction(persisted, "ptp")) {
                    SystemProperties.set(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, UsbManager.removeFunction(UsbManager.removeFunction(persisted, "mtp"), "ptp"));
                }
                updateState(FileUtils.readTextFile(new File(UsbDeviceManager.STATE_PATH), 0, null).trim());
                if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"))) {
                    Slog.d(UsbDeviceManager.TAG, "Aging test version, not register observer to listen for settings changes");
                } else {
                    UsbDeviceManager.this.mContentResolver.registerContentObserver(Global.getUriFor("adb_enabled"), false, new AdbSettingsObserver());
                }
                UsbDeviceManager.this.mUEventObserver.startObserving(UsbDeviceManager.USB_STATE_MATCH);
                UsbDeviceManager.this.mUEventObserver.startObserving(UsbDeviceManager.ACCESSORY_START_MATCH);
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

        public void sendMessage(int what, Object arg, boolean arg1) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.obj = arg;
            m.arg1 = arg1 ? 1 : 0;
            sendMessage(m);
        }

        public void updateState(String state) {
            int connected;
            int configured;
            int i = 0;
            if ("DISCONNECTED".equals(state)) {
                connected = 0;
                configured = 0;
            } else if ("CONNECTED".equals(state)) {
                connected = 1;
                configured = 0;
            } else if ("CONFIGURED".equals(state)) {
                connected = 1;
                configured = 1;
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
            if (UsbDeviceManager.DEBUG) {
                Slog.i(UsbDeviceManager.TAG, "updateHostState " + port + " status=" + status);
            }
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = port;
            args.arg2 = status;
            removeMessages(7);
            sendMessageDelayed(obtainMessage(7, args), 1000);
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

        private void setUsbConfig(String config) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setUsbConfig(" + config + ")");
            }
            SystemProperties.set(UsbDeviceManager.USB_CONFIG_PROPERTY, config);
        }

        private void setAdbEnabled(boolean enable) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setAdbEnabled: " + enable);
            }
            if (enable != UsbDeviceManager.this.mAdbEnabled) {
                UsbDeviceManager.this.mAdbEnabled = enable;
                String oldFunctions = this.mCurrentFunctions;
                SystemProperties.set(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, applyAdbFunction(SystemProperties.get(UsbDeviceManager.USB_PERSISTENT_CONFIG_PROPERTY, "none")));
                if (oldFunctions.equals("mtp") && (this.mUsbDataUnlocked ^ 1) != 0 && enable) {
                    oldFunctions = "none";
                }
                setEnabledFunctions(oldFunctions, true, this.mUsbDataUnlocked);
                updateAdbNotification(false);
            }
            if (UsbDeviceManager.this.mDebuggingManager != null) {
                UsbDeviceManager.this.mDebuggingManager.setAdbEnabled(UsbDeviceManager.this.mAdbEnabled);
            }
        }

        private void setEnabledFunctions(String functions, boolean forceRestart, boolean usbDataUnlocked) {
            if (UsbDeviceManager.DEBUG) {
                Slog.d(UsbDeviceManager.TAG, "setEnabledFunctions functions=" + functions + ", " + "forceRestart=" + forceRestart + ", usbDataUnlocked=" + usbDataUnlocked + ",mUsbDataUnlocked = " + this.mUsbDataUnlocked);
            }
            if (usbDataUnlocked != this.mUsbDataUnlocked) {
                this.mUsbDataUnlocked = usbDataUnlocked;
                updateUsbNotification(false);
                forceRestart = true;
            }
            String oldFunctions = this.mCurrentFunctions;
            boolean oldFunctionsApplied = this.mCurrentFunctionsApplied;
            if (!trySetEnabledFunctions(functions, forceRestart)) {
                if (oldFunctionsApplied && (oldFunctions.equals(functions) ^ 1) != 0) {
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
                    }
                }
            }
        }

        private boolean isNormalBoot() {
            String bootMode = SystemProperties.get(UsbDeviceManager.BOOT_MODE_PROPERTY, Shell.NIGHT_MODE_STR_UNKNOWN);
            if (UsbDeviceManager.DEBUG) {
                Slog.i(UsbDeviceManager.TAG, "bootMode = " + bootMode);
            }
            return !bootMode.equals(UsbDeviceManager.NORMAL_BOOT) ? bootMode.equals(Shell.NIGHT_MODE_STR_UNKNOWN) : true;
        }

        private boolean trySetEnabledFunctions(String functions, boolean forceRestart) {
            if (functions == null || applyAdbFunction(functions).equals("none")) {
                functions = getDefaultFunctions();
            }
            functions = applyAdbFunction(functions);
            String oemFunctions = UsbDeviceManager.this.applyOemOverrideFunction(functions);
            if (UsbDeviceManager.DEBUG) {
                Slog.i(UsbDeviceManager.TAG, "functions=" + functions + ", oemFunctions=" + oemFunctions + ",mCurrentFunctions=" + this.mCurrentFunctions + ",mCurrentFunctionsApplied=" + this.mCurrentFunctionsApplied + ",forceRestart=" + forceRestart);
                if (this.mCurrentOemFunctions != null) {
                    Slog.i(UsbDeviceManager.TAG, "mCurrentOemFunctions=" + this.mCurrentOemFunctions);
                }
            }
            if (UsbManager.containsFunction(functions, "none") && (functions.equals("none") ^ 1) != 0) {
                UsbManager.removeFunction(functions, "none");
                Slog.i(UsbDeviceManager.TAG, "after remove USB_FUNCTION_NONE, functions=" + functions);
            }
            if (!(isNormalBoot() || (this.mCurrentFunctions.equals(functions) ^ 1) == 0)) {
                SystemProperties.set(UsbDeviceManager.getPersistProp(true), functions);
            }
            if ((!functions.equals(oemFunctions) && (this.mCurrentOemFunctions == null || (this.mCurrentOemFunctions.equals(oemFunctions) ^ 1) != 0)) || (this.mCurrentFunctions.equals(functions) ^ 1) != 0 || (this.mCurrentFunctionsApplied ^ 1) != 0 || forceRestart) {
                Slog.i(UsbDeviceManager.TAG, "Setting USB config to " + functions);
                this.mCurrentFunctions = functions;
                this.mCurrentOemFunctions = oemFunctions;
                this.mCurrentFunctionsApplied = false;
                setUsbConfig("none");
                if (waitForState("none")) {
                    setUsbConfig(oemFunctions);
                    if (UsbDeviceManager.this.mBootCompleted && (UsbManager.containsFunction(functions, "mtp") || UsbManager.containsFunction(functions, "ptp"))) {
                        updateUsbStateBroadcastIfNeeded(true);
                    }
                    if (waitForState(oemFunctions)) {
                        this.mCurrentFunctionsApplied = true;
                    } else {
                        Slog.e(UsbDeviceManager.TAG, "Failed to switch USB config to " + functions);
                        return false;
                    }
                }
                Slog.e(UsbDeviceManager.TAG, "Failed to kick USB config");
                return false;
            }
            return true;
        }

        private boolean isStrictOpEnable() {
            return SystemProperties.getBoolean("persist.vendor.strict_op_enable", false);
        }

        private String applyAdbFunction(String functions) {
            if (functions == null) {
                functions = "";
            }
            boolean adbEnable = UsbDeviceManager.this.mAdbEnabled;
            if (isStrictOpEnable() && UsbManager.containsFunction(functions, "mtp") && (this.mUsbDataUnlocked ^ 1) != 0) {
                adbEnable = false;
            }
            if (adbEnable) {
                return UsbManager.addFunction(functions, "adb");
            }
            return UsbManager.removeFunction(functions, "adb");
        }

        private boolean isUsbTransferAllowed() {
            return ((UserManager) UsbDeviceManager.this.mContext.getSystemService("user")).hasUserRestriction("no_usb_file_transfer") ^ 1;
        }

        private void updateCurrentAccessory() {
            boolean enteringAccessoryMode = hasMessages(8);
            if (this.mConfigured && enteringAccessoryMode) {
                if (UsbDeviceManager.this.mAccessoryStrings != null) {
                    this.mCurrentAccessory = new UsbAccessory(UsbDeviceManager.this.mAccessoryStrings);
                    Slog.d(UsbDeviceManager.TAG, "entering USB accessory mode: " + this.mCurrentAccessory);
                    if (UsbDeviceManager.this.mBootCompleted) {
                        UsbDeviceManager.this.getCurrentSettings().accessoryAttached(this.mCurrentAccessory);
                        return;
                    }
                    return;
                }
                Slog.e(UsbDeviceManager.TAG, "nativeGetAccessoryStrings failed");
            } else if (!enteringAccessoryMode) {
                notifyAccessoryModeExit();
            } else if (UsbDeviceManager.DEBUG) {
                Slog.v(UsbDeviceManager.TAG, "Debouncing accessory mode exit");
            }
        }

        private void notifyAccessoryModeExit() {
            Slog.d(UsbDeviceManager.TAG, "exited USB accessory mode");
            setEnabledFunctions(null, false, false);
            if (this.mCurrentAccessory != null) {
                if (UsbDeviceManager.this.mBootCompleted) {
                    UsbDeviceManager.this.mSettingsManager.usbAccessoryRemoved(this.mCurrentAccessory);
                }
                this.mCurrentAccessory = null;
                UsbDeviceManager.this.mAccessoryStrings = null;
            }
        }

        private boolean isUsbStateChanged(Intent intent) {
            Set<String> keySet = intent.getExtras().keySet();
            if (UsbDeviceManager.this.mBroadcastedIntent == null) {
                for (String key : keySet) {
                    if (intent.getBooleanExtra(key, false)) {
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

        private void updateUsbStateBroadcastIfNeeded(boolean configChanged) {
            Intent intent = new Intent("android.hardware.usb.action.USB_STATE");
            intent.addFlags(822083584);
            intent.addFlags(DumpState.DUMP_DEXOPT);
            intent.putExtra("connected", this.mConnected);
            intent.putExtra("host_connected", this.mHostConnected);
            intent.putExtra("configured", this.mConfigured);
            intent.putExtra("unlocked", isUsbTransferAllowed() ? this.mUsbDataUnlocked : false);
            intent.putExtra("config_changed", configChanged);
            if (this.mCurrentFunctions != null) {
                String[] functions = this.mCurrentFunctions.split(",");
                for (String function : functions) {
                    if (!"none".equals(function)) {
                        intent.putExtra(function, true);
                    }
                }
            }
            if (isUsbStateChanged(intent) || (configChanged ^ 1) == 0) {
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
                                Slog.e(UsbDeviceManager.TAG, "could not open MIDI file", e);
                                enabled = false;
                                if (scanner != null) {
                                    scanner.close();
                                }
                                UsbDeviceManager.this.mMidiEnabled = enabled;
                                if (UsbDeviceManager.this.mMidiEnabled) {
                                }
                                UsbDeviceManager.this.mUsbAlsaManager.setPeripheralMidiState(UsbDeviceManager.this.mMidiEnabled ? this.mConfigured : false, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
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
                        Slog.e(UsbDeviceManager.TAG, "could not open MIDI file", e);
                        enabled = false;
                        if (scanner != null) {
                        }
                        UsbDeviceManager.this.mMidiEnabled = enabled;
                        if (UsbDeviceManager.this.mMidiEnabled) {
                        }
                        UsbDeviceManager.this.mUsbAlsaManager.setPeripheralMidiState(UsbDeviceManager.this.mMidiEnabled ? this.mConfigured : false, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
                    }
                }
                UsbDeviceManager.this.mMidiEnabled = enabled;
            }
            UsbDeviceManager.this.mUsbAlsaManager.setPeripheralMidiState(UsbDeviceManager.this.mMidiEnabled ? this.mConfigured : false, UsbDeviceManager.this.mMidiCard, UsbDeviceManager.this.mMidiDevice);
        }

        public void handleMessage(Message msg) {
            Slog.i(UsbDeviceManager.TAG, "handleMessage msg.what=" + msg.what);
            switch (msg.what) {
                case 0:
                    this.mConnected = msg.arg1 == 1;
                    this.mConfigured = msg.arg2 == 1;
                    updateUsbNotification(false);
                    updateAdbNotification(false);
                    if (UsbDeviceManager.this.mBootCompleted) {
                        updateUsbStateBroadcastIfNeeded(false);
                    }
                    if (UsbManager.containsFunction(this.mCurrentFunctions, "accessory")) {
                        updateCurrentAccessory();
                    }
                    if (UsbDeviceManager.this.mBootCompleted) {
                        if (!this.mConnected) {
                            if (isNormalBoot() || !UsbManager.containsFunction(this.mCurrentFunctions, "rndis")) {
                                setEnabledFunctions(null, UsbDeviceManager.this.mAdbEnabled ^ 1, false);
                            } else {
                                Slog.i(UsbDeviceManager.TAG, "usb plug out, rndis should close in reboot bootmode!");
                                setEnabledFunctions("midi", UsbDeviceManager.this.mAdbEnabled ^ 1, false);
                            }
                        }
                        updateUsbFunctions();
                        return;
                    }
                    UsbDeviceManager.this.mPendingBootBroadcast = true;
                    return;
                case 1:
                    setAdbEnabled(msg.arg1 == 1);
                    return;
                case 2:
                    setEnabledFunctions(msg.obj, false, msg.arg1 == 1);
                    return;
                case 3:
                    updateUsbNotification(false);
                    updateAdbNotification(false);
                    updateUsbFunctions();
                    return;
                case 4:
                    UsbDeviceManager.this.mBootCompleted = true;
                    if (UsbDeviceManager.this.mPendingBootBroadcast) {
                        updateUsbStateBroadcastIfNeeded(false);
                        UsbDeviceManager.this.mPendingBootBroadcast = false;
                    }
                    if ("true".equals(SystemProperties.get("persist.sys.allcommode", "false")) || (isNormalBoot() ^ 1) != 0) {
                        setEnabledFunctions(null, true, false);
                    } else {
                        setEnabledFunctions(null, false, false);
                    }
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
                        if (this.mUsbDataUnlocked && isUsbDataTransferActive() && this.mCurrentUser != -10000) {
                            Slog.v(UsbDeviceManager.TAG, "Current user switched to " + msg.arg1 + "; resetting USB host stack for MTP or PTP");
                            if (SystemProperties.getBoolean(AlertWindowNotification.PROPERTY_PERMISSION_ENABLE, true)) {
                                setEnabledFunctions(null, true, false);
                            } else {
                                setEnabledFunctions(null, false, false);
                            }
                        }
                        this.mCurrentUser = msg.arg1;
                        return;
                    }
                    return;
                case 6:
                    boolean forceRestart;
                    if (this.mUsbDataUnlocked && isUsbDataTransferActive()) {
                        forceRestart = isUsbTransferAllowed() ^ 1;
                    } else {
                        forceRestart = false;
                    }
                    setEnabledFunctions(this.mCurrentFunctions, forceRestart, this.mUsbDataUnlocked ? forceRestart ^ 1 : false);
                    return;
                case 7:
                    boolean isRoleCombinationSupported;
                    SomeArgs args = msg.obj;
                    boolean prevHostConnected = this.mHostConnected;
                    UsbPort port = args.arg1;
                    UsbPortStatus status = args.arg2;
                    this.mHostConnected = status.getCurrentDataRole() == 1;
                    this.mSourcePower = status.getCurrentPowerRole() == 1;
                    this.mSinkPower = status.getCurrentPowerRole() == 2;
                    this.mAudioAccessoryConnected = status.getCurrentMode() == 4;
                    this.mAudioAccessorySupported = port.isModeSupported(4);
                    if (status.isRoleCombinationSupported(1, 1) && status.isRoleCombinationSupported(2, 1) && status.isRoleCombinationSupported(1, 2)) {
                        isRoleCombinationSupported = status.isRoleCombinationSupported(2, 1);
                    } else {
                        isRoleCombinationSupported = false;
                    }
                    this.mSupportsAllCombinations = isRoleCombinationSupported;
                    args.recycle();
                    updateUsbNotification(false);
                    if (!UsbDeviceManager.this.mBootCompleted) {
                        UsbDeviceManager.this.mPendingBootBroadcast = true;
                        return;
                    } else if (this.mHostConnected || prevHostConnected) {
                        updateUsbStateBroadcastIfNeeded(false);
                        return;
                    } else {
                        return;
                    }
                case 8:
                    if (UsbDeviceManager.DEBUG) {
                        Slog.v(UsbDeviceManager.TAG, "Accessory mode enter timeout: " + this.mConnected);
                    }
                    if (!this.mConnected || (UsbManager.containsFunction(this.mCurrentFunctions, "accessory") ^ 1) != 0) {
                        notifyAccessoryModeExit();
                        return;
                    }
                    return;
                case 9:
                    this.mUsbCharging = msg.arg1 == 1;
                    updateUsbNotification(false);
                    return;
                case 10:
                    Iterator devices = msg.obj;
                    boolean connected = msg.arg1 == 1;
                    if (UsbDeviceManager.DEBUG) {
                        Slog.i(UsbDeviceManager.TAG, "HOST_STATE connected:" + connected);
                    }
                    this.mHideUsbNotification = false;
                    while (devices.hasNext()) {
                        Entry pair = (Entry) devices.next();
                        if (UsbDeviceManager.DEBUG) {
                            Slog.i(UsbDeviceManager.TAG, pair.getKey() + " = " + pair.getValue());
                        }
                        UsbDevice device = (UsbDevice) pair.getValue();
                        int configurationCount = device.getConfigurationCount() - 1;
                        while (configurationCount >= 0) {
                            UsbConfiguration config = device.getConfiguration(configurationCount);
                            configurationCount--;
                            int interfaceCount = config.getInterfaceCount() - 1;
                            while (interfaceCount >= 0) {
                                UsbInterface intrface = config.getInterface(interfaceCount);
                                interfaceCount--;
                                if (UsbDeviceManager.sBlackListedInterfaces.contains(Integer.valueOf(intrface.getInterfaceClass()))) {
                                    this.mHideUsbNotification = true;
                                }
                            }
                        }
                    }
                    updateUsbNotification(false);
                    return;
                case 11:
                    updateAdbNotification(true);
                    updateUsbNotification(true);
                    return;
                default:
                    return;
            }
        }

        private boolean isUsbDataTransferActive() {
            if (UsbManager.containsFunction(this.mCurrentFunctions, "mtp")) {
                return true;
            }
            return UsbManager.containsFunction(this.mCurrentFunctions, "ptp");
        }

        public UsbAccessory getCurrentAccessory() {
            return this.mCurrentAccessory;
        }

        private void updateUsbNotification(boolean force) {
        }

        private void updateAdbNotification(boolean force) {
        }

        private String getDefaultFunctions() {
            String func = SystemProperties.get(UsbDeviceManager.getPersistProp(true), "none");
            if (!UsbManager.containsFunction(func, "adb")) {
                Slog.i(UsbDeviceManager.TAG, "getDefaultFunctions: USB_FUNCTION_MIDI");
                return "midi";
            } else if ("adb".equals(func)) {
                return "adb";
            } else {
                return func;
            }
        }

        public void dump(IndentingPrintWriter pw) {
            pw.println("USB Device State:");
            pw.println("  mCurrentFunctions: " + this.mCurrentFunctions);
            pw.println("  mCurrentOemFunctions: " + this.mCurrentOemFunctions);
            pw.println("  mCurrentFunctionsApplied: " + this.mCurrentFunctionsApplied);
            pw.println("  mConnected: " + this.mConnected);
            pw.println("  mConfigured: " + this.mConfigured);
            pw.println("  mUsbDataUnlocked: " + this.mUsbDataUnlocked);
            pw.println("  mCurrentAccessory: " + this.mCurrentAccessory);
            pw.println("  mHostConnected: " + this.mHostConnected);
            pw.println("  mSourcePower: " + this.mSourcePower);
            pw.println("  mSinkPower: " + this.mSinkPower);
            pw.println("  mUsbCharging: " + this.mUsbCharging);
            pw.println("  mHideUsbNotification: " + this.mHideUsbNotification);
            pw.println("  mAudioAccessoryConnected: " + this.mAudioAccessoryConnected);
            try {
                pw.println("  Kernel state: " + FileUtils.readTextFile(new File(UsbDeviceManager.STATE_PATH), 0, null).trim());
                pw.println("  Kernel function list: " + FileUtils.readTextFile(new File(UsbDeviceManager.FUNCTIONS_PATH), 0, null).trim());
            } catch (IOException e) {
                pw.println("IOException: " + e);
            }
        }
    }

    private native String[] nativeGetAccessoryStrings();

    private native int nativeGetAudioMode();

    private native boolean nativeIsStartRequested();

    private native ParcelFileDescriptor nativeOpenAccessory();

    static {
        sBlackListedInterfaces.add(Integer.valueOf(1));
        sBlackListedInterfaces.add(Integer.valueOf(2));
        sBlackListedInterfaces.add(Integer.valueOf(3));
        sBlackListedInterfaces.add(Integer.valueOf(7));
        sBlackListedInterfaces.add(Integer.valueOf(8));
        sBlackListedInterfaces.add(Integer.valueOf(9));
        sBlackListedInterfaces.add(Integer.valueOf(10));
        sBlackListedInterfaces.add(Integer.valueOf(11));
        sBlackListedInterfaces.add(Integer.valueOf(13));
        sBlackListedInterfaces.add(Integer.valueOf(14));
        sBlackListedInterfaces.add(Integer.valueOf(224));
    }

    public UsbDeviceManager(Context context, UsbAlsaManager alsaManager, UsbSettingsManager settingsManager) {
        this.mContext = context;
        this.mUsbAlsaManager = alsaManager;
        this.mSettingsManager = settingsManager;
        this.mContentResolver = context.getContentResolver();
        this.mHasUsbAccessory = this.mContext.getPackageManager().hasSystemFeature("android.hardware.usb.accessory");
        initRndisAddress();
        readOemUsbOverrideConfig();
        this.mHandler = new UsbHandler(FgThread.get().getLooper());
        if (nativeIsStartRequested()) {
            if (DEBUG) {
                Slog.d(TAG, "accessory attached at boot");
            }
            startAccessoryMode();
        }
        boolean secureAdbEnabled = SystemProperties.getBoolean("ro.adb.secure", false);
        boolean dataEncrypted = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("vold.decrypt"));
        if (secureAdbEnabled && (dataEncrypted ^ 1) != 0) {
            this.mDebuggingManager = new UsbDebuggingManager(context);
        }
        BroadcastReceiver portReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UsbDeviceManager.this.mHandler.updateHostState((UsbPort) intent.getParcelableExtra("port"), (UsbPortStatus) intent.getParcelableExtra("portStatus"));
            }
        };
        BroadcastReceiver chargingReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UsbDeviceManager.this.mHandler.sendMessage(9, intent.getIntExtra("plugged", -1) == 2);
            }
        };
        BroadcastReceiver hostReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Iterator devices = ((UsbManager) context.getSystemService("usb")).getDeviceList().entrySet().iterator();
                if (intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                    UsbDeviceManager.this.mHandler.sendMessage(10, devices, true);
                } else {
                    UsbDeviceManager.this.mHandler.sendMessage(10, devices, false);
                }
            }
        };
        BroadcastReceiver languageChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UsbDeviceManager.this.mHandler.sendEmptyMessage(11);
            }
        };
        this.mContext.registerReceiver(portReceiver, new IntentFilter("android.hardware.usb.action.USB_PORT_CHANGED"));
        this.mContext.registerReceiver(chargingReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        IntentFilter filter = new IntentFilter("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        this.mContext.registerReceiver(hostReceiver, filter);
        this.mContext.registerReceiver(languageChangedReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
    }

    private UsbProfileGroupSettingsManager getCurrentSettings() {
        UsbProfileGroupSettingsManager usbProfileGroupSettingsManager;
        synchronized (this.mLock) {
            usbProfileGroupSettingsManager = this.mCurrentSettings;
        }
        return usbProfileGroupSettingsManager;
    }

    public void systemReady() {
        boolean z;
        int i = 0;
        if (DEBUG) {
            Slog.d(TAG, "systemReady");
        }
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (isTv()) {
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(ADB_NOTIFICATION_CHANNEL_ID_TV, this.mContext.getString(17039456), 4));
        }
        StorageVolume primary = StorageManager.from(this.mContext).getPrimaryVolume();
        if (primary != null ? primary.allowMassStorage() : false) {
            z = false;
        } else {
            z = this.mContext.getResources().getBoolean(17957047);
        }
        this.mUseUsbNotification = z;
        try {
            ContentResolver contentResolver = this.mContentResolver;
            String str = "adb_enabled";
            if (this.mAdbEnabled) {
                i = 1;
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

    public void setCurrentUser(int newCurrentUserId, UsbProfileGroupSettingsManager settings) {
        synchronized (this.mLock) {
            this.mCurrentSettings = settings;
            this.mHandler.obtainMessage(5, newCurrentUserId, 0).sendToTarget();
        }
    }

    public void updateUserRestrictions() {
        this.mHandler.sendEmptyMessage(6);
    }

    private void startAccessoryMode() {
        if (this.mHasUsbAccessory) {
            this.mAccessoryStrings = nativeGetAccessoryStrings();
            boolean enableAudio = nativeGetAudioMode() == 1;
            boolean enableAccessory = (this.mAccessoryStrings == null || this.mAccessoryStrings[0] == null) ? false : this.mAccessoryStrings[1] != null;
            String functions = null;
            if (enableAccessory && enableAudio) {
                functions = "accessory,audio_source";
            } else if (enableAccessory) {
                functions = "accessory";
            } else if (enableAudio) {
                functions = "audio_source";
            }
            if (functions != null) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(8), 10000);
                setCurrentFunctions(functions, false);
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
        try {
            FileUtils.stringToFile(RNDIS_ETH_ADDR_PATH, String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X", new Object[]{Integer.valueOf(address[0]), Integer.valueOf(address[1]), Integer.valueOf(address[2]), Integer.valueOf(address[3]), Integer.valueOf(address[4]), Integer.valueOf(address[5])}));
        } catch (IOException e) {
            Slog.e(TAG, "failed to write to /sys/class/android_usb/android0/f_rndis/ethaddr");
        }
    }

    private boolean isTv() {
        return this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
    }

    public UsbAccessory getCurrentAccessory() {
        return this.mHandler.getCurrentAccessory();
    }

    public ParcelFileDescriptor openAccessory(UsbAccessory accessory, UsbUserSettingsManager settings) {
        UsbAccessory currentAccessory = this.mHandler.getCurrentAccessory();
        if (currentAccessory == null) {
            throw new IllegalArgumentException("no accessory attached");
        } else if (currentAccessory.equals(accessory)) {
            settings.checkPermission(accessory);
            return nativeOpenAccessory();
        } else {
            throw new IllegalArgumentException(accessory.toString() + " does not match current accessory " + currentAccessory);
        }
    }

    public boolean isFunctionEnabled(String function) {
        return UsbManager.containsFunction(SystemProperties.get(USB_CONFIG_PROPERTY), function);
    }

    public void setCurrentFunctions(String functions, boolean usbDataUnlocked) {
        if (DEBUG) {
            Slog.d(TAG, "setCurrentFunctions(" + functions + ", " + usbDataUnlocked + ")");
        }
        this.mHandler.sendMessage(2, functions, usbDataUnlocked);
    }

    private void readOemUsbOverrideConfig() {
        String[] configList = this.mContext.getResources().getStringArray(17236027);
        if (configList != null) {
            for (String config : configList) {
                String[] items = config.split(":");
                if (items.length == 3 || items.length == 4) {
                    if (this.mOemModeMap == null) {
                        this.mOemModeMap = new HashMap();
                    }
                    HashMap<String, Pair<String, String>> overrideMap = (HashMap) this.mOemModeMap.get(items[0]);
                    if (overrideMap == null) {
                        overrideMap = new HashMap();
                        this.mOemModeMap.put(items[0], overrideMap);
                    }
                    if (!overrideMap.containsKey(items[1])) {
                        if (items.length == 3) {
                            overrideMap.put(items[1], new Pair(items[2], ""));
                        } else {
                            overrideMap.put(items[1], new Pair(items[2], items[3]));
                        }
                    }
                }
            }
        }
    }

    private String applyOemOverrideFunction(String usbFunctions) {
        if (usbFunctions == null || this.mOemModeMap == null) {
            return usbFunctions;
        }
        String bootMode = SystemProperties.get(BOOT_MODE_PROPERTY, Shell.NIGHT_MODE_STR_UNKNOWN);
        Slog.d(TAG, "applyOemOverride usbfunctions=" + usbFunctions + " bootmode=" + bootMode);
        Map<String, Pair<String, String>> overridesMap = (Map) this.mOemModeMap.get(bootMode);
        if (overridesMap != null) {
            int i;
            if (bootMode.equals(NORMAL_BOOT)) {
                i = 1;
            } else {
                i = bootMode.equals(Shell.NIGHT_MODE_STR_UNKNOWN);
            }
            if ((i ^ 1) != 0) {
                Pair<String, String> overrideFunctions = (Pair) overridesMap.get(usbFunctions);
                if (overrideFunctions != null) {
                    Slog.d(TAG, "OEM USB override: " + usbFunctions + " ==> " + ((String) overrideFunctions.first) + " persist across reboot " + ((String) overrideFunctions.second));
                    if (!((String) overrideFunctions.second).equals("")) {
                        String newFunction;
                        if (this.mAdbEnabled) {
                            newFunction = UsbManager.addFunction((String) overrideFunctions.second, "adb");
                        } else {
                            newFunction = (String) overrideFunctions.second;
                        }
                        Slog.d(TAG, "OEM USB override persisting: " + newFunction + "in prop: " + getPersistProp(false));
                        SystemProperties.set(getPersistProp(false), newFunction);
                    }
                    return (String) overrideFunctions.first;
                } else if (this.mAdbEnabled) {
                    SystemProperties.set(getPersistProp(false), UsbManager.addFunction("none", "adb"));
                } else {
                    SystemProperties.set(getPersistProp(false), "none");
                }
            }
        }
        return usbFunctions;
    }

    public static String getPersistProp(boolean functions) {
        String bootMode = SystemProperties.get(BOOT_MODE_PROPERTY, Shell.NIGHT_MODE_STR_UNKNOWN);
        String persistProp = USB_PERSISTENT_CONFIG_PROPERTY;
        if (bootMode.equals("reboot") && SystemProperties.getBoolean("persist.sys.allcommode", false)) {
            return persistProp;
        }
        if (!(!bootMode.equals(NORMAL_BOOT) ? bootMode.equals(Shell.NIGHT_MODE_STR_UNKNOWN) : true)) {
            if (functions) {
                persistProp = "persist.sys.usb." + bootMode + ".func";
            } else {
                persistProp = "persist.sys.usb." + bootMode + ".config";
            }
        }
        return persistProp;
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

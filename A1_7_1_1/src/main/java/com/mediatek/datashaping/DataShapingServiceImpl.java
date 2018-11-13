package com.mediatek.datashaping;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Slog;
import android.view.InputEvent;
import android.view.InputFilter;
import android.view.KeyEvent;
import android.view.WindowManagerInternal;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.input.InputManagerService;
import com.mediatek.datashaping.IDataShapingManager.Stub;

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
public class DataShapingServiceImpl extends Stub {
    public static final long ALARM_MANAGER_OPEN_GATE_INTERVAL = 300000;
    private static final String CLOSE_TIME_EXPIRED_ACTION = "com.mediatek.datashaping.CLOSE_TIME_EXPIRED";
    public static final int DATA_SHAPING_STATE_CLOSE = 3;
    public static final int DATA_SHAPING_STATE_OPEN = 2;
    public static final int DATA_SHAPING_STATE_OPEN_LOCKED = 1;
    private static final boolean DBG = false;
    public static final long GATE_CLOSE_EXPIRED_TIME = 300000;
    public static final int GATE_CLOSE_SAFE_TIMER = 600000;
    private static final int MSG_ALARM_MANAGER_TRIGGER = 14;
    private static final int MSG_APPSTANDBY_CHANGED = 22;
    private static final int MSG_BT_AP_STATE_CHANGED = 19;
    private static final int MSG_CHECK_USER_PREFERENCE = 1;
    private static final int MSG_CONNECTIVITY_CHANGED = 20;
    private static final int MSG_DEVICEIDLE_CHANGED = 21;
    private static final int MSG_GATE_CLOSE_TIMER_EXPIRED = 17;
    private static final int MSG_HEADSETHOOK_CHANGED = 18;
    private static final int MSG_INIT = 2;
    private static final int MSG_INPUTFILTER_STATE_CHANGED = 23;
    private static final int MSG_LTE_AS_STATE_CHANGED = 15;
    private static final int MSG_NETWORK_TYPE_CHANGED = 11;
    private static final int MSG_SCREEN_STATE_CHANGED = 10;
    private static final int MSG_SHARED_DEFAULT_APN_STATE_CHANGED = 16;
    private static final int MSG_STOP = 3;
    private static final int MSG_USB_STATE_CHANGED = 13;
    private static final int MSG_WIFI_AP_STATE_CHANGED = 12;
    private static final int WAKE_LOCK_TIMEOUT = 30000;
    private final String TAG;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private DataShapingState mCurrentState;
    private boolean mDataShapingEnabled;
    private DataShapingHandler mDataShapingHandler;
    private DataShapingUtils mDataShapingUtils;
    private DataShapingState mGateCloseState;
    private DataShapingState mGateOpenLockedState;
    private DataShapingState mGateOpenState;
    private HandlerThread mHandlerThread;
    private DataShapingInputFilter mInputFilter;
    private InputManagerService mInputManagerService;
    private long mLastAlarmTriggerSuccessTime;
    private final Object mLock;
    private PendingIntent mPendingIntent;
    private boolean mRegisterInput;
    private ContentObserver mSettingsObserver;
    private UsageStatsManagerInternal mUsageStats;
    private WakeLock mWakelock;
    private WindowManagerInternal mWindowManagerService;

    private class AppIdleStateChangeListener extends android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener {
        /* synthetic */ AppIdleStateChangeListener(DataShapingServiceImpl this$0, AppIdleStateChangeListener appIdleStateChangeListener) {
            this();
        }

        private AppIdleStateChangeListener() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle) {
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            Slog.d("DataShapingService", "onParoleStateChanged is " + isParoleOn);
            DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(22, Boolean.valueOf(isParoleOn)).sendToTarget();
        }
    }

    private class DataShapingHandler extends Handler {
        public DataShapingHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    sendEmptyMessage(2);
                    return;
                case 2:
                    Slog.d("DataShapingService", "[handleMessage] msg_init");
                    DataShapingServiceImpl.this.mWindowManagerService = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
                    DataShapingServiceImpl.this.mInputManagerService = (InputManagerService) ServiceManager.getService("input");
                    DataShapingServiceImpl.this.mInputFilter = new DataShapingInputFilter(DataShapingServiceImpl.this.mContext);
                    return;
                case 10:
                    Slog.d("DataShapingService", "[handleMessage] msg_screen_state_changed");
                    DataShapingServiceImpl.this.mCurrentState.onScreenStateChanged(((Boolean) msg.obj).booleanValue());
                    return;
                case 11:
                    Slog.d("DataShapingService", "[handleMessage] msg_network_type_changed");
                    DataShapingServiceImpl.this.mCurrentState.onNetworkTypeChanged((Intent) msg.obj);
                    return;
                case 12:
                    Slog.d("DataShapingService", "[handleMessage] msg_wifi_ap_state_changed");
                    DataShapingServiceImpl.this.mCurrentState.onWifiTetherStateChanged((Intent) msg.obj);
                    return;
                case 13:
                    Slog.d("DataShapingService", "[handleMessage] msg_usb_state_changed");
                    DataShapingServiceImpl.this.mCurrentState.onUsbConnectionChanged((Intent) msg.obj);
                    return;
                case 14:
                    Slog.d("DataShapingService", "[handleMessage] msg_alarm_manager_trigger");
                    DataShapingServiceImpl.this.mCurrentState.onAlarmManagerTrigger();
                    return;
                case 15:
                    Slog.d("DataShapingService", "[handleMessage] msg_lte_as_state_changed");
                    DataShapingServiceImpl.this.mCurrentState.onLteAccessStratumStateChanged((Intent) msg.obj);
                    return;
                case 16:
                    Slog.d("DataShapingService", "[handleMessage] msg_shared_default_apn_state_changed");
                    DataShapingServiceImpl.this.mCurrentState.onSharedDefaultApnStateChanged((Intent) msg.obj);
                    return;
                case 17:
                    Slog.d("DataShapingService", "[handleMessage] msg_gate_close_timer_expired");
                    DataShapingServiceImpl.this.mCurrentState.onCloseTimeExpired();
                    DataShapingServiceImpl.this.releaseWakeLock();
                    return;
                case 18:
                    Slog.d("DataShapingService", "[handleMessage] msg_headsethook_changed");
                    DataShapingServiceImpl.this.mCurrentState.onMediaButtonTrigger();
                    return;
                case 19:
                    Slog.d("DataShapingService", "[handleMessage] msg_bt_ap_state_changed");
                    DataShapingServiceImpl.this.mCurrentState.onBTStateChanged((Intent) msg.obj);
                    return;
                case 20:
                    Slog.d("DataShapingService", "[handleMessage] msg_connectivity_changed");
                    DataShapingServiceImpl.this.mDataShapingUtils.setLteAsReport();
                    return;
                case 21:
                    DataShapingServiceImpl.this.mCurrentState.onDeviceIdleStateChanged(((Boolean) msg.obj).booleanValue());
                    return;
                case 22:
                    DataShapingServiceImpl.this.mCurrentState.onAPPStandbyStateChanged(((Boolean) msg.obj).booleanValue());
                    return;
                case 23:
                    DataShapingServiceImpl.this.mCurrentState.onInputFilterStateChanged(((Boolean) msg.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }
    }

    private class DataShapingInputFilter extends InputFilter {
        private final Context mContext;

        DataShapingInputFilter(Context context) {
            super(context.getMainLooper());
            this.mContext = context;
        }

        public void onInputEvent(InputEvent event, int policyFlags) {
            if (event instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent) event;
                if (keyEvent.getAction() == 0 || keyEvent.getAction() == 1) {
                    Slog.d("DataShapingService", "Received event ACTION_UP or ACTION_DOWN");
                    if (DataShapingServiceImpl.this.mDataShapingHandler != null) {
                        DataShapingServiceImpl.this.mDataShapingHandler.sendEmptyMessage(18);
                    }
                }
            }
            super.onInputEvent(event, policyFlags);
        }

        public void onUninstalled() {
            Slog.d("DataShapingService", "onUninstalled : " + DataShapingServiceImpl.this.mCurrentState);
            synchronized (DataShapingServiceImpl.this.mLock) {
                DataShapingServiceImpl.this.mRegisterInput = false;
                if (DataShapingServiceImpl.this.mCurrentState instanceof GateCloseState) {
                    DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(23, Boolean.valueOf(DataShapingServiceImpl.this.mRegisterInput)).sendToTarget();
                    Slog.d("DataShapingService", "onUninstalled : Change to Gate Open");
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.datashaping.DataShapingServiceImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.datashaping.DataShapingServiceImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.datashaping.DataShapingServiceImpl.<clinit>():void");
    }

    public DataShapingServiceImpl(Context context) {
        this.TAG = "DataShapingService";
        this.mRegisterInput = false;
        this.mLock = new Object();
        this.mSettingsObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                boolean dataShapingEnabled = System.getInt(DataShapingServiceImpl.this.mContext.getContentResolver(), "background_power_saving_enable", 0) != 0;
                if ("0".equals(SystemProperties.get("persist.datashaping.enable", "-1"))) {
                    dataShapingEnabled = false;
                    Slog.d("DataShapingService", "persist.datashaping.enable is false");
                } else if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.datashaping.enable", "-1"))) {
                    dataShapingEnabled = true;
                    Slog.d("DataShapingService", "persist.datashaping.enable is true");
                }
                if (dataShapingEnabled != DataShapingServiceImpl.this.mDataShapingEnabled) {
                    if (dataShapingEnabled) {
                        Slog.d("DataShapingService", "data shaping enabled, start handler thread!");
                        DataShapingServiceImpl.this.mHandlerThread = new HandlerThread("DataShapingService");
                        DataShapingServiceImpl.this.mHandlerThread.start();
                        DataShapingServiceImpl.this.mDataShapingHandler = new DataShapingHandler(DataShapingServiceImpl.this.mHandlerThread.getLooper());
                        DataShapingServiceImpl.this.mDataShapingHandler.sendEmptyMessage(2);
                        DataShapingServiceImpl.this.setCurrentState(1);
                        DataShapingServiceImpl.this.registerReceiver();
                    } else {
                        if (DataShapingServiceImpl.this.mBroadcastReceiver != null) {
                            DataShapingServiceImpl.this.mContext.unregisterReceiver(DataShapingServiceImpl.this.mBroadcastReceiver);
                        }
                        if (DataShapingServiceImpl.this.mHandlerThread != null) {
                            DataShapingServiceImpl.this.mHandlerThread.quitSafely();
                        }
                        DataShapingServiceImpl.this.mDataShapingUtils.reset();
                        DataShapingServiceImpl.this.reset();
                        Slog.d("DataShapingService", "data shaping disabled, stop handler thread and reset!");
                    }
                    DataShapingServiceImpl.this.mDataShapingEnabled = dataShapingEnabled;
                }
            }
        };
        this.mContext = context;
        this.mDataShapingUtils = DataShapingUtils.getInstance(this.mContext);
        this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
    }

    public void registerReceiver() {
        Slog.d("DataShapingService", "registerReceiver start");
        if (this.mBroadcastReceiver == null) {
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Slog.d("DataShapingService", "received broadcast, action is: " + action);
                    if ("android.intent.action.SCREEN_ON" == action) {
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(10, Boolean.valueOf(true)).sendToTarget();
                    } else if ("android.intent.action.SCREEN_OFF" == action) {
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(10, Boolean.valueOf(false)).sendToTarget();
                    } else if ("mediatek.intent.action.PS_NETWORK_TYPE_CHANGED" == action) {
                        DataShapingServiceImpl.this.mDataShapingUtils.setCurrentNetworkType(intent);
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(11, intent).sendToTarget();
                    } else if ("android.net.conn.CONNECTIVITY_CHANGE" == action) {
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(20, intent).sendToTarget();
                    } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED" == action) {
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(12, intent).sendToTarget();
                    } else if ("android.hardware.usb.action.USB_STATE" == action) {
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(13, intent).sendToTarget();
                    } else if ("mediatek.intent.action.LTE_ACCESS_STRATUM_STATE_CHANGED" == action) {
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(15, intent).sendToTarget();
                    } else if ("mediatek.intent.action.SHARED_DEFAULT_APN_STATE_CHANGED" == action) {
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(16, intent).sendToTarget();
                    } else if (DataShapingServiceImpl.CLOSE_TIME_EXPIRED_ACTION == action) {
                        DataShapingServiceImpl.this.getWakeLock();
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(17).sendToTarget();
                    } else if ("android.bluetooth.device.action.ACL_CONNECTED".equals(action) || "android.bluetooth.device.action.ACL_DISCONNECTED".equals(action)) {
                        DataShapingServiceImpl.this.mDataShapingHandler.obtainMessage(19, intent).sendToTarget();
                    }
                }
            };
        }
        IntentFilter eventsFilter = new IntentFilter();
        eventsFilter.addAction("android.intent.action.SCREEN_ON");
        eventsFilter.addAction("android.intent.action.SCREEN_OFF");
        eventsFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        eventsFilter.addAction("mediatek.intent.action.PS_NETWORK_TYPE_CHANGED");
        eventsFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        eventsFilter.addAction("android.hardware.usb.action.USB_STATE");
        eventsFilter.addAction("mediatek.intent.action.LTE_ACCESS_STRATUM_STATE_CHANGED");
        eventsFilter.addAction("mediatek.intent.action.SHARED_DEFAULT_APN_STATE_CHANGED");
        eventsFilter.addAction(CLOSE_TIME_EXPIRED_ACTION);
        eventsFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        eventsFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, eventsFilter);
        Slog.d("DataShapingService", "registerReceiver end");
        this.mUsageStats.addAppIdleStateChangeListener(new AppIdleStateChangeListener(this, null));
        Slog.d("DataShapingService", "addAppIdleStateChangeListener end");
    }

    boolean registerListener() {
        if (this.mWindowManagerService == null || this.mInputManagerService == null) {
            Slog.d("DataShapingService", "registerListener get WindowManager fail !");
            return false;
        }
        synchronized (this.mLock) {
            Slog.d("DataShapingService", "registerListener registerInput Before: " + this.mRegisterInput);
            if (!this.mRegisterInput && !this.mInputManagerService.alreadyHasInputFilter()) {
                Slog.d("DataShapingService", "registerListener!!!");
                this.mWindowManagerService.setInputFilter(this.mInputFilter);
                this.mRegisterInput = true;
            } else if (this.mRegisterInput) {
                Slog.d("DataShapingService", "I have registered it");
            } else {
                Slog.d("DataShapingService", "Someone registered it !!!");
            }
            Slog.d("DataShapingService", "registerListener registerInput After: " + this.mRegisterInput);
        }
        return this.mRegisterInput;
    }

    void unregisterListener() {
        if (this.mWindowManagerService == null) {
            Slog.d("DataShapingService", "unregisterListener get WindowManager fail !");
            return;
        }
        synchronized (this.mLock) {
            if (this.mRegisterInput) {
                Slog.d("DataShapingService", "unregisterListener registerInput is TRUE , Set myself to null!");
                this.mWindowManagerService.setInputFilter(null);
                this.mRegisterInput = false;
            } else {
                Slog.d("DataShapingService", "unregisterListener registerInput is False , Not to set to null!");
            }
        }
    }

    public void start() {
        this.mGateOpenState = new GateOpenState(this, this.mContext);
        this.mGateOpenLockedState = new GateOpenLockedState(this, this.mContext);
        this.mGateCloseState = new GateCloseState(this, this.mContext);
        setCurrentState(1);
        Slog.d("DataShapingService", "start check user preference");
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("background_power_saving_enable"), true, this.mSettingsObserver);
        this.mSettingsObserver.onChange(false);
    }

    public void setCurrentState(int stateType) {
        switch (stateType) {
            case 1:
                this.mCurrentState = this.mGateOpenLockedState;
                unregisterListener();
                Slog.d("DataShapingService", "[setCurrentState]: set to STATE_OPEN_LOCKED");
                return;
            case 2:
                this.mCurrentState = this.mGateOpenState;
                unregisterListener();
                Slog.d("DataShapingService", "[setCurrentState]: set to STATE_OPEN");
                return;
            case 3:
                this.mCurrentState = this.mGateCloseState;
                Slog.d("DataShapingService", "[setCurrentState]: set to STATE_CLOSE");
                return;
            default:
                return;
        }
    }

    public void enableDataShaping() {
        Slog.d("DataShapingService", "enableDataShaping");
    }

    public void disableDataShaping() {
        Slog.d("DataShapingService", "disableDataShaping");
    }

    public boolean openLteDataUpLinkGate(boolean isForce) {
        if (this.mDataShapingEnabled) {
            boolean powerSavingEnabled = System.getInt(this.mContext.getContentResolver(), "background_power_saving_enable", 0) != 0;
            if ("0".equals(SystemProperties.get("persist.alarmgroup.enable", "-1"))) {
                powerSavingEnabled = false;
                Slog.d("DataShapingService", "[openLteDataUpLinkGate] persist.alarmgroup.enable is false");
            } else if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.alarmgroup.enable", "-1"))) {
                powerSavingEnabled = true;
                Slog.d("DataShapingService", "[openLteDataUpLinkGate] persist.alarmgroup.enable is true");
            }
            if (!powerSavingEnabled) {
                Slog.d("DataShapingService", "[openLteDataUpLinkGate] powerSaving is Disabled!");
                return false;
            } else if (System.currentTimeMillis() - this.mLastAlarmTriggerSuccessTime >= 300000) {
                if (this.mDataShapingHandler != null) {
                    this.mDataShapingHandler.sendEmptyMessage(14);
                }
                this.mLastAlarmTriggerSuccessTime = System.currentTimeMillis();
                Slog.d("DataShapingService", "Alarm manager openLteDataUpLinkGate: true");
                return true;
            } else {
                Slog.d("DataShapingService", "Alarm manager openLteDataUpLinkGate: false");
                return false;
            }
        }
        if (DBG) {
            Slog.d("DataShapingService", "[openLteDataUpLinkGate] DataShaping is Disabled!");
        }
        return false;
    }

    public void setDeviceIdleMode(boolean enabled) {
        if (this.mDataShapingEnabled) {
            Slog.d("DataShapingService", "setDeviceIdleMode is " + enabled);
            this.mDataShapingUtils.setDeviceIdleState(enabled);
            this.mDataShapingHandler.obtainMessage(21, Boolean.valueOf(enabled)).sendToTarget();
            return;
        }
        Slog.d("DataShapingService", "[setDeviceIdleMode] Data Shaping isn't enable.");
    }

    public void cancelCloseExpiredAlarm() {
        Slog.d("DataShapingService", "[cancelCloseExpiredAlarm]");
        if (this.mPendingIntent != null) {
            ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mPendingIntent);
        }
    }

    public void startCloseExpiredAlarm() {
        Slog.d("DataShapingService", "[startCloseExpiredAlarm] cancel previous alarm");
        cancelCloseExpiredAlarm();
        Slog.d("DataShapingService", "[startCloseExpiredAlarm] start new alarm");
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mPendingIntent == null) {
            this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(CLOSE_TIME_EXPIRED_ACTION), 0);
        }
        alarmManager.set(0, System.currentTimeMillis() + 300000, this.mPendingIntent);
    }

    private void getWakeLock() {
        Slog.d("DataShapingService", "[getWakeLock]");
        releaseWakeLock();
        if (this.mWakelock == null) {
            this.mWakelock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, getClass().getCanonicalName());
        }
        this.mWakelock.acquire(30000);
    }

    private void releaseWakeLock() {
        Slog.d("DataShapingService", "[releaseWakeLock]");
        if (this.mWakelock != null && this.mWakelock.isHeld()) {
            Slog.d("DataShapingService", "really release WakeLock");
            this.mWakelock.release();
            this.mWakelock = null;
        }
    }

    private void reset() {
        setCurrentState(1);
        releaseWakeLock();
        cancelCloseExpiredAlarm();
    }
}

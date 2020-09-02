package com.android.server;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothCallback;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothHeadset;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.common.OppoFeatureCache;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.FeatureFlagUtils;
import android.util.Log;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.util.DumpUtils;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.pm.DumpState;
import com.android.server.pm.Settings;
import com.android.server.pm.UserRestrictionsUtils;
import com.android.server.usage.AppStandbyController;
import com.android.server.utils.PriorityDump;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.mediatek.cta.CtaManagerFactory;
import com.oppo.hypnus.Hypnus;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class BluetoothManagerService extends IBluetoothManager.Stub {
    private static final String ACTION_PACKAGE_DATA_CLEARED = "android.intent.action.PACKAGE_DATA_CLEARED";
    private static final int ACTIVE_LOG_MAX_SIZE = 20;
    private static final int ADD_PROXY_DELAY_MS = 100;
    private static final String BLUETOOTH_ADMIN_PERM = "android.permission.BLUETOOTH_ADMIN";
    private static final int BLUETOOTH_OFF = 0;
    private static final int BLUETOOTH_ON_AIRPLANE = 2;
    private static final int BLUETOOTH_ON_BLUETOOTH = 1;
    private static final String BLUETOOTH_PERM = "android.permission.BLUETOOTH";
    private static final int CRASH_LOG_MAX_SIZE = 100;
    private static final boolean DBG = true;
    private static final int ERROR_RESTART_TIME_MS = 3000;
    private static final int MAX_ERROR_RESTART_RETRIES = 6;
    private static final int MAX_SAVE_RETRIES = 3;
    private static final int MESSAGE_ADD_PROXY_DELAYED = 400;
    private static final int MESSAGE_BIND_PROFILE_SERVICE = 401;
    private static final int MESSAGE_BLUETOOTH_SERVICE_CONNECTED = 40;
    private static final int MESSAGE_BLUETOOTH_SERVICE_DISCONNECTED = 41;
    private static final int MESSAGE_BLUETOOTH_STATE_CHANGE = 60;
    private static final int MESSAGE_DISABLE = 2;
    private static final int MESSAGE_ENABLE = 1;
    private static final int MESSAGE_GET_NAME_AND_ADDRESS = 200;
    private static final int MESSAGE_REGISTER_ADAPTER = 20;
    private static final int MESSAGE_REGISTER_STATE_CHANGE_CALLBACK = 30;
    private static final int MESSAGE_RESTART_BLUETOOTH_SERVICE = 42;
    private static final int MESSAGE_RESTORE_USER_SETTING = 500;
    private static final int MESSAGE_SAVE_NAME_AND_ADDRESS = 201;
    private static final int MESSAGE_TIMEOUT_BIND = 100;
    private static final int MESSAGE_TIMEOUT_UNBIND = 101;
    private static final int MESSAGE_UNREGISTER_ADAPTER = 21;
    private static final int MESSAGE_UNREGISTER_STATE_CHANGE_CALLBACK = 31;
    private static final int MESSAGE_USER_SWITCHED = 300;
    private static final int MESSAGE_USER_UNLOCKED = 301;
    private static final String PACKAGE_NAME_OSHARE = "com.coloros.oshare";
    private static final int RESTORE_SETTING_TO_OFF = 0;
    private static final int RESTORE_SETTING_TO_ON = 1;
    private static final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
    private static final String SECURE_SETTINGS_BLUETOOTH_ADDR_VALID = "bluetooth_addr_valid";
    private static final String SECURE_SETTINGS_BLUETOOTH_NAME = "bluetooth_name";
    private static final int SERVICE_IBLUETOOTH = 1;
    private static final int SERVICE_IBLUETOOTHGATT = 2;
    private static final int SERVICE_RESTART_TIME_MS = 200;
    private static final String TAG = "BluetoothManagerService";
    private static final int TIMEOUT_BIND_MS = 3000;
    private static final int TIMEOUT_SAVE_MS = 500;
    private static final int USER_SWITCHED_TIME_MS = 200;
    private final LinkedList<ActiveLog> mActiveLogs = new LinkedList<>();
    private ActivityManager mActivityManager;
    private String mAddress;
    private final ContentObserver mAirplaneModeObserver = new ContentObserver(null) {
        /* class com.android.server.BluetoothManagerService.AnonymousClass3 */

        public void onChange(boolean unused) {
            ReentrantReadWriteLock.ReadLock readLock;
            synchronized (this) {
                if (BluetoothManagerService.this.isBluetoothPersistedStateOn()) {
                    if (BluetoothManagerService.this.isAirplaneModeOn()) {
                        BluetoothManagerService.this.persistBluetoothSetting(2);
                    } else {
                        BluetoothManagerService.this.persistBluetoothSetting(1);
                    }
                }
                int st = 10;
                try {
                    BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                    if (BluetoothManagerService.this.mBluetooth != null) {
                        st = BluetoothManagerService.this.mBluetooth.getState();
                    }
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    Slog.d(BluetoothManagerService.TAG, "Airplane Mode change - current state:  " + BluetoothAdapter.nameForState(st) + ", isAirplaneModeOn()=" + BluetoothManagerService.this.isAirplaneModeOn());
                    if (BluetoothManagerService.this.isAirplaneModeOn()) {
                        BluetoothManagerService.this.clearBleApps();
                        if (st == 15 || st == 14) {
                            try {
                                BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                                if (BluetoothManagerService.this.mBluetooth != null) {
                                    BluetoothManagerService.this.addActiveLog(2, BluetoothManagerService.this.mContext.getPackageName(), false);
                                    BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                                    boolean unused2 = BluetoothManagerService.this.mEnable = false;
                                }
                                readLock = BluetoothManagerService.this.mBluetoothLock.readLock();
                            } catch (RemoteException e) {
                                Slog.e(BluetoothManagerService.TAG, "Unable to call onBrEdrDown", e);
                                readLock = BluetoothManagerService.this.mBluetoothLock.readLock();
                            } catch (Throwable th) {
                                throw th;
                            }
                            readLock.unlock();
                        } else if (st == 12 || st == 11) {
                            BluetoothManagerService.this.sendDisableMsg(2, BluetoothManagerService.this.mContext.getPackageName());
                        }
                    } else if (BluetoothManagerService.this.mEnableExternal && st != 12 && BluetoothManagerService.this.isBluetoothPersistedStateOn()) {
                        BluetoothManagerService.this.sendEnableMsg(BluetoothManagerService.this.mQuietEnableExternal, 2, BluetoothManagerService.this.mContext.getPackageName());
                    }
                } catch (RemoteException e2) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to call getState", e2);
                } finally {
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                }
            }
        }
    };
    private AppOpsManager mAppOps;
    /* access modifiers changed from: private */
    public boolean mBinding;
    /* access modifiers changed from: private */
    public Map<IBinder, ClientDeathRecipient> mBleApps = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public IBluetooth mBluetooth;
    /* access modifiers changed from: private */
    public IBinder mBluetoothBinder;
    /* access modifiers changed from: private */
    public final IBluetoothCallback mBluetoothCallback = new IBluetoothCallback.Stub() {
        /* class com.android.server.BluetoothManagerService.AnonymousClass1 */

        public void onBluetoothStateChange(int prevState, int newState) throws RemoteException {
            BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(60, prevState, newState));
        }
    };
    /* access modifiers changed from: private */
    public IBluetoothGatt mBluetoothGatt;
    /* access modifiers changed from: private */
    public final ReentrantReadWriteLock mBluetoothLock = new ReentrantReadWriteLock();
    /* access modifiers changed from: private */
    public final RemoteCallbackList<IBluetoothManagerCallback> mCallbacks;
    /* access modifiers changed from: private */
    public BluetoothServiceConnection mConnection = new BluetoothServiceConnection();
    private final ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final LinkedList<Long> mCrashTimestamps = new LinkedList<>();
    private int mCrashes;
    /* access modifiers changed from: private */
    public boolean mEnable;
    private boolean mEnableBLE;
    /* access modifiers changed from: private */
    public boolean mEnableExternal;
    /* access modifiers changed from: private */
    public int mErrorRecoveryRetryCounter;
    /* access modifiers changed from: private */
    public final BluetoothHandler mHandler = new BluetoothHandler(IoThread.get().getLooper());
    private Hypnus mHyp = null;
    private boolean mIsHearingAidProfileSupported;
    /* access modifiers changed from: private */
    public boolean mIsUserSwitch = false;
    private long mLastEnabledTime;
    private String mName;
    /* access modifiers changed from: private */
    public final Map<Integer, ProfileServiceConnections> mProfileServices = new HashMap();
    /* access modifiers changed from: private */
    public boolean mQuietEnable = false;
    /* access modifiers changed from: private */
    public boolean mQuietEnableExternal;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.BluetoothManagerService.AnonymousClass5 */

        public void onReceive(Context context, Intent intent) {
            int i;
            String action = intent.getAction();
            if ("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED".equals(action)) {
                String newName = intent.getStringExtra("android.bluetooth.adapter.extra.LOCAL_NAME");
                Slog.d(BluetoothManagerService.TAG, "Bluetooth Adapter name changed to " + newName);
                if (newName != null) {
                    BluetoothManagerService.this.storeNameAndAddress(newName, null);
                }
            } else if ("android.bluetooth.adapter.action.BLUETOOTH_ADDRESS_CHANGED".equals(action)) {
                String newAddress = intent.getStringExtra("android.bluetooth.adapter.extra.BLUETOOTH_ADDRESS");
                if (newAddress != null) {
                    Slog.d(BluetoothManagerService.TAG, "Bluetooth Adapter address changed to " + newAddress);
                    BluetoothManagerService.this.storeNameAndAddress(null, newAddress);
                    return;
                }
                Slog.e(BluetoothManagerService.TAG, "No Bluetooth Adapter address parameter found");
            } else if ("android.os.action.SETTING_RESTORED".equals(action) && "bluetooth_on".equals(intent.getStringExtra("setting_name"))) {
                String prevValue = intent.getStringExtra("previous_value");
                String newValue = intent.getStringExtra("new_value");
                Slog.d(BluetoothManagerService.TAG, "ACTION_SETTING_RESTORED with BLUETOOTH_ON, prevValue=" + prevValue + ", newValue=" + newValue);
                if (newValue != null && prevValue != null && !prevValue.equals(newValue)) {
                    BluetoothHandler access$200 = BluetoothManagerService.this.mHandler;
                    if (newValue.equals("0")) {
                        i = 0;
                    } else {
                        i = 1;
                    }
                    BluetoothManagerService.this.mHandler.sendMessage(access$200.obtainMessage(500, i, 0));
                }
            }
        }
    };
    private final BroadcastReceiver mReceiverDataCleared = new BroadcastReceiver() {
        /* class com.android.server.BluetoothManagerService.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if (BluetoothManagerService.ACTION_PACKAGE_DATA_CLEARED.equals(intent.getAction())) {
                Slog.d(BluetoothManagerService.TAG, "Bluetooth package data cleared");
                try {
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    Slog.d(BluetoothManagerService.TAG, "handleEnable: mBluetooth = " + BluetoothManagerService.this.mBluetooth + ", mBinding = " + BluetoothManagerService.this.mBinding);
                    if (BluetoothManagerService.this.mBluetooth == null && BluetoothManagerService.this.mEnable) {
                        Slog.d(BluetoothManagerService.TAG, "Bind AdapterService");
                        BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(100), BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
                        if (!BluetoothManagerService.this.doBind(new Intent(IBluetooth.class.getName()), BluetoothManagerService.this.mConnection, 65, UserHandle.CURRENT)) {
                            BluetoothManagerService.this.mHandler.removeMessages(100);
                            Slog.e(BluetoothManagerService.TAG, "Fail to bind to: " + IBluetooth.class.getName());
                        } else {
                            boolean unused = BluetoothManagerService.this.mBinding = true;
                        }
                    }
                } finally {
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mState;
    /* access modifiers changed from: private */
    public final RemoteCallbackList<IBluetoothStateChangeCallback> mStateChangeCallbacks;
    private final int mSystemUiUid;
    /* access modifiers changed from: private */
    public boolean mUnbinding;
    private final UserManagerInternal.UserRestrictionsListener mUserRestrictionsListener = new UserManagerInternal.UserRestrictionsListener() {
        /* class com.android.server.BluetoothManagerService.AnonymousClass2 */

        public void onUserRestrictionsChanged(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
            if (UserRestrictionsUtils.restrictionsChanged(prevRestrictions, newRestrictions, "no_bluetooth_sharing")) {
                BluetoothManagerService.this.updateOppLauncherComponentState(userId, newRestrictions.getBoolean("no_bluetooth_sharing"));
            }
            if (userId == 0 && UserRestrictionsUtils.restrictionsChanged(prevRestrictions, newRestrictions, "no_bluetooth")) {
                if (userId != 0 || !newRestrictions.getBoolean("no_bluetooth")) {
                    BluetoothManagerService.this.updateOppLauncherComponentState(userId, newRestrictions.getBoolean("no_bluetooth_sharing"));
                    return;
                }
                BluetoothManagerService.this.updateOppLauncherComponentState(userId, true);
                BluetoothManagerService bluetoothManagerService = BluetoothManagerService.this;
                bluetoothManagerService.sendDisableMsg(3, bluetoothManagerService.mContext.getPackageName());
            }
        }
    };
    private final boolean mWirelessConsentRequired;

    /* access modifiers changed from: private */
    public static CharSequence timeToLog(long timestamp) {
        return DateFormat.format("MM-dd HH:mm:ss", timestamp);
    }

    private class ActiveLog {
        private boolean mEnable;
        private String mPackageName;
        private int mReason;
        private long mTimestamp;

        ActiveLog(int reason, String packageName, boolean enable, long timestamp) {
            this.mReason = reason;
            this.mPackageName = packageName;
            this.mEnable = enable;
            this.mTimestamp = timestamp;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append((Object) BluetoothManagerService.timeToLog(this.mTimestamp));
            sb.append(this.mEnable ? "  Enabled " : " Disabled ");
            sb.append(" due to ");
            sb.append(BluetoothManagerService.getEnableDisableReasonString(this.mReason));
            sb.append(" by ");
            sb.append(this.mPackageName);
            return sb.toString();
        }
    }

    BluetoothManagerService(Context context) {
        this.mContext = context;
        this.mWirelessConsentRequired = context.getResources().getBoolean(17891603) || CtaManagerFactory.getInstance().makeCtaManager().isCtaSupported();
        this.mCrashes = 0;
        this.mBluetooth = null;
        this.mBluetoothBinder = null;
        this.mBluetoothGatt = null;
        this.mBinding = false;
        this.mUnbinding = false;
        this.mEnable = false;
        this.mEnableBLE = false;
        this.mState = 10;
        this.mQuietEnableExternal = false;
        this.mEnableExternal = false;
        this.mAddress = null;
        this.mName = null;
        this.mErrorRecoveryRetryCounter = 0;
        this.mContentResolver = context.getContentResolver();
        registerForBleScanModeChange();
        this.mCallbacks = new RemoteCallbackList<>();
        this.mStateChangeCallbacks = new RemoteCallbackList<>();
        this.mIsHearingAidProfileSupported = false;
        String value = SystemProperties.get("persist.sys.fflag.override.settings_bluetooth_hearing_aid");
        if (!TextUtils.isEmpty(value)) {
            boolean isHearingAidEnabled = Boolean.parseBoolean(value);
            Log.v(TAG, "set feature flag HEARING_AID_SETTINGS to " + isHearingAidEnabled);
            FeatureFlagUtils.setEnabled(context, "settings_bluetooth_hearing_aid", isHearingAidEnabled);
            if (isHearingAidEnabled && !this.mIsHearingAidProfileSupported) {
                this.mIsHearingAidProfileSupported = true;
            }
        }
        IntentFilter filterDataCleared = new IntentFilter(ACTION_PACKAGE_DATA_CLEARED);
        filterDataCleared.addDataScheme(Settings.ATTR_PACKAGE);
        this.mContext.registerReceiver(this.mReceiverDataCleared, filterDataCleared);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.BLUETOOTH_ADDRESS_CHANGED");
        filter.addAction("android.os.action.SETTING_RESTORED");
        filter.setPriority(1000);
        this.mContext.registerReceiver(this.mReceiver, filter);
        loadStoredNameAndAddress();
        if (SystemProperties.getInt("persist.sys.bluetooth_policy", 2) == 3) {
            Slog.d(TAG, "The  bluetooth_policy is MODE_BT_ENABLED  and save bluetooth'state ON to enable.");
            persistBluetoothSetting(1);
        } else if (SystemProperties.getInt("persist.sys.bluetooth_policy", 2) == 0) {
            Slog.d(TAG, "The  bluetooth_policy is MODE_BT_DISABLE  and save bluetooth'state OFF to keep off");
            persistBluetoothSetting(0);
        }
        if (isBluetoothPersistedStateOn()) {
            Slog.d(TAG, "Startup: Bluetooth persisted state is ON.");
            this.mEnableExternal = true;
        }
        String airplaneModeRadios = Settings.Global.getString(this.mContentResolver, "airplane_mode_radios");
        if (airplaneModeRadios == null || airplaneModeRadios.contains("bluetooth")) {
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        }
        int systemUiUid = -1;
        try {
            systemUiUid = !this.mContext.getResources().getBoolean(17891488) ? this.mContext.getPackageManager().getPackageUidAsUser("com.android.systemui", DumpState.DUMP_DEXOPT, 0) : systemUiUid;
            Slog.d(TAG, "Detected SystemUiUid: " + Integer.toString(systemUiUid));
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "Unable to resolve SystemUI's UID.", e);
        }
        this.mSystemUiUid = systemUiUid;
    }

    /* access modifiers changed from: private */
    public boolean isAirplaneModeOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private boolean supportBluetoothPersistedState() {
        return this.mContext.getResources().getBoolean(17891531);
    }

    /* access modifiers changed from: private */
    public boolean isBluetoothPersistedStateOn() {
        if (!supportBluetoothPersistedState()) {
            return false;
        }
        int state = Settings.Global.getInt(this.mContentResolver, "bluetooth_on", -1);
        Slog.d(TAG, "Bluetooth persisted state: " + state);
        if (state != 0) {
            return true;
        }
        return false;
    }

    private boolean isBluetoothPersistedStateOnBluetooth() {
        if (supportBluetoothPersistedState() && Settings.Global.getInt(this.mContentResolver, "bluetooth_on", 1) == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void persistBluetoothSetting(int value) {
        Slog.d(TAG, "Persisting Bluetooth Setting: " + value);
        long callingIdentity = Binder.clearCallingIdentity();
        Settings.Global.putInt(this.mContext.getContentResolver(), "bluetooth_on", value);
        Binder.restoreCallingIdentity(callingIdentity);
    }

    /* access modifiers changed from: private */
    public boolean isNameAndAddressSet() {
        String str = this.mName;
        return str != null && this.mAddress != null && str.length() > 0 && this.mAddress.length() > 0;
    }

    private void loadStoredNameAndAddress() {
        Slog.d(TAG, "Loading stored name and address");
        if (!this.mContext.getResources().getBoolean(17891374) || Settings.Secure.getInt(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDR_VALID, 0) != 0) {
            this.mName = Settings.Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME);
            this.mAddress = Settings.Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
            Slog.d(TAG, "Stored bluetooth Name=" + this.mName + ",Address=" + this.mAddress);
            return;
        }
        Slog.d(TAG, "invalid bluetooth name and address stored");
    }

    /* access modifiers changed from: private */
    public void storeNameAndAddress(String name, String address) {
        if (name != null) {
            Settings.Secure.putString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME, name);
            this.mName = name;
            Slog.d(TAG, "Stored Bluetooth name: " + Settings.Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME));
        }
        if (address != null) {
            Settings.Secure.putString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS, address);
            this.mAddress = address;
            Slog.d(TAG, "Stored Bluetoothaddress: " + Settings.Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS));
        }
        if (name != null && address != null) {
            Settings.Secure.putInt(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDR_VALID, 1);
        }
    }

    public IBluetooth registerAdapter(IBluetoothManagerCallback callback) {
        if (callback == null) {
            Slog.w(TAG, "Callback is null in registerAdapter");
            return null;
        }
        Message msg = this.mHandler.obtainMessage(20);
        msg.obj = callback;
        this.mHandler.sendMessageAtFrontOfQueue(msg);
        try {
            this.mBluetoothLock.writeLock().lock();
            return this.mBluetooth;
        } finally {
            this.mBluetoothLock.writeLock().unlock();
        }
    }

    public void unregisterAdapter(IBluetoothManagerCallback callback) {
        if (callback == null) {
            Slog.w(TAG, "Callback is null in unregisterAdapter");
            return;
        }
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Message msg = this.mHandler.obtainMessage(21);
        msg.obj = callback;
        this.mHandler.sendMessage(msg);
    }

    public void registerStateChangeCallback(IBluetoothStateChangeCallback callback) {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (callback == null) {
            Slog.w(TAG, "registerStateChangeCallback: Callback is null!");
            return;
        }
        Message msg = this.mHandler.obtainMessage(30);
        msg.obj = callback;
        this.mHandler.sendMessage(msg);
    }

    public void unregisterStateChangeCallback(IBluetoothStateChangeCallback callback) {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (callback == null) {
            Slog.w(TAG, "unregisterStateChangeCallback: Callback is null!");
            return;
        }
        Message msg = this.mHandler.obtainMessage(31);
        msg.obj = callback;
        this.mHandler.sendMessageAtFrontOfQueue(msg);
    }

    public boolean isEnabled() {
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    boolean isEnabled = this.mBluetooth.isEnabled();
                    this.mBluetoothLock.readLock().unlock();
                    return isEnabled;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "isEnabled()", e);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
            return false;
        }
        Slog.w(TAG, "isEnabled(): not allowed for non-active and non system user");
        return false;
    }

    public int getState() {
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    int state = this.mBluetooth.getState();
                    this.mBluetoothLock.readLock().unlock();
                    return state;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "getState()", e);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
            return 10;
        }
        Slog.w(TAG, "getState(): report OFF for non-active and non system user");
        return 10;
    }

    /* access modifiers changed from: package-private */
    public class ClientDeathRecipient implements IBinder.DeathRecipient {
        private String mPackageName;

        ClientDeathRecipient(String packageName) {
            this.mPackageName = packageName;
        }

        public void binderDied() {
            Slog.d(BluetoothManagerService.TAG, "Binder is dead - unregister " + this.mPackageName);
            for (Map.Entry<IBinder, ClientDeathRecipient> entry : BluetoothManagerService.this.mBleApps.entrySet()) {
                IBinder token = entry.getKey();
                if (entry.getValue().equals(this)) {
                    BluetoothManagerService.this.updateBleAppCount(token, false, this.mPackageName);
                    return;
                }
            }
        }

        public String getPackageName() {
            return this.mPackageName;
        }
    }

    public boolean isBleScanAlwaysAvailable() {
        if (isAirplaneModeOn() && !this.mEnable) {
            return false;
        }
        try {
            if (Settings.Global.getInt(this.mContentResolver, "ble_scan_always_enabled") != 0) {
                return true;
            }
            return false;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    public boolean isHearingAidProfileSupported() {
        return this.mIsHearingAidProfileSupported;
    }

    private void registerForBleScanModeChange() {
        this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("ble_scan_always_enabled"), false, new ContentObserver(null) {
            /* class com.android.server.BluetoothManagerService.AnonymousClass6 */

            public void onChange(boolean selfChange) {
                if (!BluetoothManagerService.this.isBleScanAlwaysAvailable()) {
                    BluetoothManagerService.this.disableBleScanMode();
                    BluetoothManagerService.this.clearBleApps();
                    try {
                        BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.addActiveLog(1, BluetoothManagerService.this.mContext.getPackageName(), false);
                            BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                        }
                    } catch (RemoteException e) {
                        Slog.e(BluetoothManagerService.TAG, "error when disabling bluetooth", e);
                    } catch (Throwable th) {
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        throw th;
                    }
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void disableBleScanMode() {
        try {
            this.mBluetoothLock.writeLock().lock();
            if (!(this.mBluetooth == null || this.mBluetooth.getState() == 12)) {
                Slog.d(TAG, "Reseting the mEnable flag for clean disable");
                if (!this.mEnableExternal) {
                    this.mEnable = false;
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "getState()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
            throw th;
        }
        this.mBluetoothLock.writeLock().unlock();
    }

    public int updateBleAppCount(IBinder token, boolean enable, String packageName) {
        int callingUid = Binder.getCallingUid();
        if (!(UserHandle.getAppId(callingUid) == 1000)) {
            checkPackage(callingUid, packageName);
        }
        ClientDeathRecipient r = this.mBleApps.get(token);
        if (r == null && enable) {
            ClientDeathRecipient deathRec = new ClientDeathRecipient(packageName);
            try {
                token.linkToDeath(deathRec, 0);
                this.mBleApps.put(token, deathRec);
                Slog.d(TAG, "Registered for death of " + packageName);
            } catch (RemoteException e) {
                throw new IllegalArgumentException("BLE app (" + packageName + ") already dead!");
            }
        } else if (!enable && r != null) {
            try {
                token.unlinkToDeath(r, 0);
            } catch (NoSuchElementException nsee) {
                Slog.e(TAG, "updateBleAppCount(), Unable to unlinkToDeath", nsee);
            }
            this.mBleApps.remove(token);
            Slog.d(TAG, "Unregistered for death of " + packageName);
        }
        if (enable) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth == null || !(this.mBluetooth.getState() == 15 || this.mBluetooth.getState() == 12)) {
                    this.mEnableBLE = true;
                }
            } catch (RemoteException e2) {
                Slog.e(TAG, "Unable to call getState", e2);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
        }
        int appCount = this.mBleApps.size();
        Slog.d(TAG, appCount + " registered Ble Apps");
        if (appCount == 0 && this.mEnable) {
            disableBleScanMode();
        }
        if (appCount == 0 && !this.mEnableExternal) {
            sendBrEdrDownCallback();
        }
        return appCount;
    }

    /* access modifiers changed from: private */
    public void clearBleApps() {
        this.mBleApps.clear();
    }

    public boolean isBleAppPresent() {
        Slog.d(TAG, "isBleAppPresent() count: " + this.mBleApps.size());
        return this.mBleApps.size() > 0;
    }

    /* access modifiers changed from: private */
    public void continueFromBleOnState() {
        Slog.d(TAG, "continueFromBleOnState()");
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth == null) {
                Slog.e(TAG, "onBluetoothServiceUp: mBluetooth is null!");
                this.mBluetoothLock.readLock().unlock();
                return;
            }
            if (this.mAddress == null) {
                storeNameAndAddress(null, this.mBluetooth.getAddress());
            }
            if (isBluetoothPersistedStateOnBluetooth() || !isBleAppPresent() || this.mEnableExternal) {
                this.mBluetooth.onLeServiceUp();
                persistBluetoothSetting(1);
            }
            this.mBluetoothLock.readLock().unlock();
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call onServiceUp", e);
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
    }

    private void sendBrEdrDownCallback() {
        Slog.d(TAG, "Calling sendBrEdrDownCallback callbacks");
        if (this.mBluetooth == null) {
            Slog.w(TAG, "Bluetooth handle is null");
        } else if (!isBleAppPresent() || this.mIsUserSwitch) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    this.mBluetooth.onBrEdrDown();
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Call to onBrEdrDown() failed.", e);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
        } else {
            IBluetoothGatt iBluetoothGatt = this.mBluetoothGatt;
            if (iBluetoothGatt == null) {
                Slog.w(TAG, "BluetoothGatt is null");
                return;
            }
            try {
                iBluetoothGatt.unregAll();
            } catch (RemoteException e2) {
                Slog.e(TAG, "Unable to disconnect all apps.", e2);
            }
        }
    }

    public boolean enableNoAutoConnect(String packageName) {
        boolean isCallerSystem = false;
        if (isBluetoothDisallowed()) {
            Slog.d(TAG, "enableNoAutoConnect(): not enabling - bluetooth disallowed");
            return false;
        }
        int callingUid = Binder.getCallingUid();
        if (UserHandle.getAppId(callingUid) == 1000) {
            isCallerSystem = true;
        }
        if (!isCallerSystem) {
            checkPackage(callingUid, packageName);
        }
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permission");
        Slog.d(TAG, "enableNoAutoConnect():  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding);
        int callingAppId = UserHandle.getAppId(callingUid);
        String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Slog.d(TAG, "callingApp = " + callingApp);
        if (callingAppId == 1027 || PACKAGE_NAME_OSHARE.equals(callingApp)) {
            synchronized (this.mReceiver) {
                this.mQuietEnableExternal = true;
                this.mEnableExternal = true;
                sendEnableMsg(true, 1, packageName);
            }
            return true;
        }
        throw new SecurityException("no permission to enable Bluetooth quietly");
    }

    public boolean enable(String packageName) throws RemoteException {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction((int) HdmiCecKeycode.CEC_KEYCODE_F3_GREEN, Binder.getCallingUid());
        int callingUid = Binder.getCallingUid();
        boolean callerSystem = UserHandle.getAppId(callingUid) == 1000;
        if (isBluetoothDisallowed()) {
            Slog.d(TAG, "enable(): not enabling - bluetooth disallowed");
            return false;
        }
        if (!callerSystem) {
            checkPackage(callingUid, packageName);
            if (!checkIfCallerIsForegroundUser()) {
                Slog.w(TAG, "enable(): not allowed for non-active and non system user");
                return false;
            }
            this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permission");
            if (!isEnabled() && this.mWirelessConsentRequired && !CtaManagerFactory.getInstance().makeCtaManager().isSystemApp(this.mContext, packageName) && startConsentUiIfNeeded(packageName, callingUid, "android.bluetooth.adapter.action.REQUEST_ENABLE")) {
                return false;
            }
        }
        Slog.d(TAG, "enable(" + packageName + "):  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding + " mState = " + BluetoothAdapter.nameForState(this.mState));
        if (this.mHyp == null) {
            this.mHyp = Hypnus.getHypnus();
        }
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            hypnus.hypnusSetAction(12, 2500);
        }
        synchronized (this.mReceiver) {
            this.mQuietEnableExternal = false;
            if (!this.mEnableBLE) {
                this.mEnableExternal = true;
            } else {
                this.mEnableBLE = false;
            }
            sendEnableMsg(false, 1, packageName);
        }
        Slog.d(TAG, "enable returning");
        return true;
    }

    public boolean disable(String packageName, boolean persist) throws RemoteException {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction((int) HdmiCecKeycode.CEC_KEYCODE_F4_YELLOW, Binder.getCallingUid());
        int callingUid = Binder.getCallingUid();
        if (!(UserHandle.getAppId(callingUid) == 1000)) {
            checkPackage(callingUid, packageName);
            if (!checkIfCallerIsForegroundUser()) {
                Slog.w(TAG, "disable(): not allowed for non-active and non system user");
                return false;
            }
            this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permission");
            if (isEnabled() && this.mWirelessConsentRequired && !CtaManagerFactory.getInstance().makeCtaManager().isSystemApp(this.mContext, packageName) && startConsentUiIfNeeded(packageName, callingUid, "android.bluetooth.adapter.action.REQUEST_DISABLE")) {
                return false;
            }
        }
        Slog.d(TAG, "disable(" + packageName + "):  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding);
        synchronized (this.mReceiver) {
            if (persist) {
                if ("com.oppo.logkit".equals(packageName)) {
                    synchronized (this) {
                        clearBleApps();
                    }
                }
                persistBluetoothSetting(0);
            }
            this.mEnableExternal = false;
            sendDisableMsg(1, packageName);
        }
        return true;
    }

    private boolean startConsentUiIfNeeded(String packageName, int callingUid, String intentAction) throws RemoteException {
        if (checkBluetoothPermissionWhenWirelessConsentRequired()) {
            return false;
        }
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 268435456, UserHandle.getUserId(callingUid)).uid == callingUid) {
                Intent intent = new Intent(intentAction);
                intent.putExtra("android.intent.extra.PACKAGE_NAME", packageName);
                intent.setFlags(276824064);
                try {
                    this.mContext.startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    Slog.e(TAG, "Intent to handle action " + intentAction + " missing");
                    return false;
                }
            } else {
                throw new SecurityException("Package " + packageName + " not in uid " + callingUid);
            }
        } catch (PackageManager.NameNotFoundException e2) {
            throw new RemoteException(e2.getMessage());
        }
    }

    private void checkPackage(int uid, String packageName) {
        AppOpsManager appOpsManager = this.mAppOps;
        if (appOpsManager == null) {
            Slog.w(TAG, "checkPackage(): called before system boot up, uid " + uid + ", packageName " + packageName);
            throw new IllegalStateException("System has not boot yet");
        } else if (packageName == null) {
            Slog.w(TAG, "checkPackage(): called with null packageName from " + uid);
        } else {
            try {
                appOpsManager.checkPackage(uid, packageName);
            } catch (SecurityException e) {
                Slog.w(TAG, "checkPackage(): " + packageName + " does not belong to uid " + uid);
                throw new SecurityException(e.getMessage());
            }
        }
    }

    private boolean checkBluetoothPermissionWhenWirelessConsentRequired() {
        return this.mContext.checkCallingPermission("android.permission.MANAGE_BLUETOOTH_WHEN_WIRELESS_CONSENT_REQUIRED") == 0;
    }

    public void unbindAndFinish() {
        Slog.d(TAG, "unbindAndFinish(): " + this.mBluetooth + " mBinding = " + this.mBinding + " mUnbinding = " + this.mUnbinding);
        try {
            this.mBluetoothLock.writeLock().lock();
            if (!this.mUnbinding) {
                this.mUnbinding = true;
                this.mHandler.removeMessages(60);
                this.mHandler.removeMessages(MESSAGE_BIND_PROFILE_SERVICE);
                if (this.mBluetooth != null) {
                    try {
                        this.mBluetooth.unregisterCallback(this.mBluetoothCallback);
                    } catch (RemoteException re) {
                        Slog.e(TAG, "Unable to unregister BluetoothCallback", re);
                    }
                    this.mBluetoothBinder = null;
                    this.mBluetooth = null;
                    this.mContext.unbindService(this.mConnection);
                    this.mUnbinding = false;
                    this.mBinding = false;
                } else {
                    this.mUnbinding = false;
                }
                this.mBluetoothGatt = null;
                this.mBluetoothLock.writeLock().unlock();
            }
        } finally {
            this.mBluetoothLock.writeLock().unlock();
        }
    }

    public IBluetoothGatt getBluetoothGatt() {
        return this.mBluetoothGatt;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005d, code lost:
        r0 = r7.mHandler.obtainMessage(400);
        r0.arg1 = r8;
        r0.obj = r9;
        r7.mHandler.sendMessageDelayed(r0, 100);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0070, code lost:
        return true;
     */
    public boolean bindBluetoothProfileService(int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
        if (!this.mEnable || !isEnabled()) {
            Slog.d(TAG, "Trying to bind to profile: " + bluetoothProfile + ", while Bluetooth was disabled");
            return false;
        }
        synchronized (this.mProfileServices) {
            if (this.mProfileServices.get(new Integer(bluetoothProfile)) == null) {
                Slog.d(TAG, "Creating new ProfileServiceConnections object for profile: " + bluetoothProfile);
                if (bluetoothProfile != 1) {
                    return false;
                }
                ProfileServiceConnections psc = new ProfileServiceConnections(new Intent(IBluetoothHeadset.class.getName()));
                if (!psc.bindService()) {
                    return false;
                }
                this.mProfileServices.put(new Integer(bluetoothProfile), psc);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        return;
     */
    public void unbindBluetoothProfileService(int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
        synchronized (this.mProfileServices) {
            Integer profile = new Integer(bluetoothProfile);
            ProfileServiceConnections psc = this.mProfileServices.get(profile);
            if (psc != null) {
                psc.removeProxy(proxy);
                if (psc.isEmpty()) {
                    try {
                        this.mContext.unbindService(psc);
                    } catch (IllegalArgumentException e) {
                        Slog.e(TAG, "Unable to unbind service with intent: " + psc.mIntent, e);
                    }
                    this.mProfileServices.remove(profile);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void unbindAllBluetoothProfileServices() {
        synchronized (this.mProfileServices) {
            for (Integer i : this.mProfileServices.keySet()) {
                ProfileServiceConnections psc = this.mProfileServices.get(i);
                try {
                    this.mContext.unbindService(psc);
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "Unable to unbind service with intent: " + psc.mIntent, e);
                }
                psc.removeAllProxies();
            }
            this.mProfileServices.clear();
        }
    }

    public void handleOnBootPhase() {
        Slog.d(TAG, "Bluetooth boot completed");
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).addUserRestrictionsListener(this.mUserRestrictionsListener);
        if (!isBluetoothDisallowed()) {
            try {
                if (AppGlobals.getPackageManager().isFirstBoot() && this.mActivityManager.isLowRamDevice()) {
                    Slog.d(TAG, "Low Ram: Change Bluetooth to persisted off for the first boot");
                    persistBluetoothSetting(0);
                    this.mEnableExternal = false;
                }
                if (this.mEnableExternal && isBluetoothPersistedStateOnBluetooth()) {
                    Slog.d(TAG, "Auto-enabling Bluetooth.");
                    sendEnableMsg(this.mQuietEnableExternal, 6, this.mContext.getPackageName());
                } else if (!isNameAndAddressSet()) {
                    Slog.d(TAG, "Getting adapter name and address");
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(200));
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void handleOnSwitchUser(int userHandle) {
        Slog.d(TAG, "User " + userHandle + " switched");
        this.mHandler.obtainMessage(300, userHandle, 0).sendToTarget();
    }

    public void handleOnUnlockUser(int userHandle) {
        Slog.d(TAG, "User " + userHandle + " unlocked");
        this.mHandler.obtainMessage(MESSAGE_USER_UNLOCKED, userHandle, 0).sendToTarget();
    }

    /* access modifiers changed from: private */
    public final class ProfileServiceConnections implements ServiceConnection, IBinder.DeathRecipient {
        ComponentName mClassName = null;
        Intent mIntent;
        boolean mInvokingProxyCallbacks = false;
        final RemoteCallbackList<IBluetoothProfileServiceConnection> mProxies = new RemoteCallbackList<>();
        IBinder mService = null;

        ProfileServiceConnections(Intent intent) {
            this.mIntent = intent;
        }

        /* access modifiers changed from: private */
        public boolean bindService() {
            int state = 10;
            try {
                BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                if (BluetoothManagerService.this.mBluetooth != null) {
                    state = BluetoothManagerService.this.mBluetooth.getState();
                }
                BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                if (!BluetoothManagerService.this.mEnable || state != 12) {
                    Slog.d(BluetoothManagerService.TAG, "Unable to bindService while Bluetooth is disabled");
                    return false;
                }
                Intent intent = this.mIntent;
                if (intent == null || this.mService != null || !BluetoothManagerService.this.doBind(intent, this, 0, UserHandle.CURRENT_OR_SELF)) {
                    Slog.w(BluetoothManagerService.TAG, "Unable to bind with intent: " + this.mIntent);
                    return false;
                }
                Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE);
                msg.obj = this;
                BluetoothManagerService.this.mHandler.sendMessageDelayed(msg, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
                return true;
            } catch (RemoteException e) {
                Slog.e(BluetoothManagerService.TAG, "Unable to call getState", e);
                BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                return false;
            } catch (Throwable th) {
                BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                throw th;
            }
        }

        /* access modifiers changed from: private */
        public void addProxy(IBluetoothProfileServiceConnection proxy) {
            this.mProxies.register(proxy);
            IBinder iBinder = this.mService;
            if (iBinder != null) {
                try {
                    proxy.onServiceConnected(this.mClassName, iBinder);
                } catch (RemoteException e) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to connect to proxy", e);
                }
            } else if (!BluetoothManagerService.this.mHandler.hasMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, this)) {
                Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE);
                msg.obj = this;
                BluetoothManagerService.this.mHandler.sendMessage(msg);
            }
        }

        /* access modifiers changed from: private */
        public void removeProxy(IBluetoothProfileServiceConnection proxy) {
            if (proxy != null) {
                this.mProxies.unregister(proxy);
                if (this.mProxies.getRegisteredCallbackCount() == 0) {
                    Slog.e(BluetoothManagerService.TAG, "No proxy, unbind");
                    try {
                        BluetoothManagerService.this.mContext.unbindService(this);
                    } catch (IllegalArgumentException e) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to unbind service", e);
                    }
                    onServiceDisconnected(this.mClassName);
                    return;
                }
                return;
            }
            Slog.w(BluetoothManagerService.TAG, "Trying to remove a null proxy");
        }

        /* access modifiers changed from: private */
        public void removeAllProxies() {
            onServiceDisconnected(this.mClassName);
            this.mProxies.kill();
        }

        /* access modifiers changed from: private */
        public boolean isEmpty() {
            return this.mProxies.getRegisteredCallbackCount() == 0;
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, this);
            this.mClassName = className;
            try {
                synchronized (this.mClassName) {
                    try {
                        this.mService = service;
                        this.mService.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to linkToDeath", e);
                    }
                }
                if (this.mInvokingProxyCallbacks) {
                    Slog.e(BluetoothManagerService.TAG, "Proxy callbacks already in progress.");
                    return;
                }
                this.mInvokingProxyCallbacks = true;
                synchronized (this.mProxies) {
                    int n = this.mProxies.beginBroadcast();
                    for (int i = 0; i < n; i++) {
                        try {
                            this.mProxies.getBroadcastItem(i).onServiceConnected(className, service);
                        } catch (RemoteException e2) {
                            Slog.e(BluetoothManagerService.TAG, "Unable to connect to proxy", e2);
                        } catch (Throwable th) {
                            this.mProxies.finishBroadcast();
                            this.mInvokingProxyCallbacks = false;
                            throw th;
                        }
                    }
                    this.mProxies.finishBroadcast();
                    this.mInvokingProxyCallbacks = false;
                }
            } catch (NullPointerException npe) {
                Slog.e(BluetoothManagerService.TAG, "NullPointerException for synchronized(mClassName)", npe);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            ComponentName componentName;
            if (this.mService != null && (componentName = this.mClassName) != null) {
                try {
                    synchronized (componentName) {
                        this.mService.unlinkToDeath(this, 0);
                        this.mService = null;
                        this.mClassName = null;
                    }
                    if (this.mInvokingProxyCallbacks) {
                        Slog.e(BluetoothManagerService.TAG, "Proxy callbacks already in progress.");
                        return;
                    }
                    this.mInvokingProxyCallbacks = true;
                    synchronized (this.mProxies) {
                        int n = this.mProxies.beginBroadcast();
                        for (int i = 0; i < n; i++) {
                            try {
                                this.mProxies.getBroadcastItem(i).onServiceDisconnected(className);
                            } catch (RemoteException e) {
                                Slog.e(BluetoothManagerService.TAG, "Unable to disconnect from proxy", e);
                            } catch (Throwable th) {
                                this.mProxies.finishBroadcast();
                                this.mInvokingProxyCallbacks = false;
                                throw th;
                            }
                        }
                        this.mProxies.finishBroadcast();
                        this.mInvokingProxyCallbacks = false;
                    }
                } catch (NullPointerException npe) {
                    Slog.e(BluetoothManagerService.TAG, "NullPointerException for synchronized(mClassName)", npe);
                } catch (NoSuchElementException e2) {
                    Slog.e(BluetoothManagerService.TAG, "NoSuchElementException when unlinkToDeath", e2);
                }
            }
        }

        public void binderDied() {
            Slog.w(BluetoothManagerService.TAG, "Profile service for profile: " + this.mClassName + " died.");
            onServiceDisconnected(this.mClassName);
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE);
            msg.obj = this;
            BluetoothManagerService.this.mHandler.sendMessageDelayed(msg, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
        }
    }

    private void sendBluetoothStateCallback(boolean isUp) {
        try {
            int n = this.mStateChangeCallbacks.beginBroadcast();
            Slog.d(TAG, "Broadcasting onBluetoothStateChange(" + isUp + ") to " + n + " receivers.");
            for (int i = 0; i < n; i++) {
                try {
                    this.mStateChangeCallbacks.getBroadcastItem(i).onBluetoothStateChange(isUp);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to call onBluetoothStateChange() on callback #" + i, e);
                } catch (SecurityException se) {
                    Slog.e(TAG, "Unable to call onBluetoothStateChange() on callback #" + i, se);
                }
            }
        } finally {
            this.mStateChangeCallbacks.finishBroadcast();
        }
    }

    /* access modifiers changed from: private */
    public void sendBluetoothServiceUpCallback() {
        try {
            int n = this.mCallbacks.beginBroadcast();
            Slog.d(TAG, "Broadcasting onBluetoothServiceUp() to " + n + " receivers.");
            for (int i = 0; i < n; i++) {
                try {
                    this.mCallbacks.getBroadcastItem(i).onBluetoothServiceUp(this.mBluetooth);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to call onBluetoothServiceUp() on callback #" + i, e);
                } catch (SecurityException se) {
                    Slog.e(TAG, "Unable to call onBluetoothServiceUp() on callback #" + i, se);
                }
            }
        } finally {
            this.mCallbacks.finishBroadcast();
        }
    }

    /* access modifiers changed from: private */
    public void sendBluetoothServiceDownCallback() {
        try {
            int n = this.mCallbacks.beginBroadcast();
            Slog.d(TAG, "Broadcasting onBluetoothServiceDown() to " + n + " receivers.");
            for (int i = 0; i < n; i++) {
                try {
                    this.mCallbacks.getBroadcastItem(i).onBluetoothServiceDown();
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to call onBluetoothServiceDown() on callback #" + i, e);
                } catch (SecurityException se) {
                    Slog.e(TAG, "Unable to call onBluetoothServiceDown() on callback #" + i, se);
                }
            }
        } finally {
            this.mCallbacks.finishBroadcast();
        }
    }

    public String getAddress() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (Binder.getCallingUid() != 1000 && !checkIfCallerIsForegroundUser()) {
            Slog.w(TAG, "getAddress(): not allowed for non-active and non system user");
            return null;
        } else if (this.mContext.checkCallingOrSelfPermission("android.permission.LOCAL_MAC_ADDRESS") != 0) {
            return "02:00:00:00:00:00";
        } else {
            try {
                this.mBluetoothLock.readLock().lock();
                if (!(this.mBluetooth == null || this.mState == 14 || this.mState == 16)) {
                    String address = this.mBluetooth.getAddress();
                    this.mBluetoothLock.readLock().unlock();
                    return address;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "getAddress(): Unable to retrieve address remotely. Returning cached address", e);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
            return this.mAddress;
        }
    }

    public String getName() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    String name = this.mBluetooth.getName();
                    this.mBluetoothLock.readLock().unlock();
                    return name;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "getName(): Unable to retrieve name remotely. Returning cached name", e);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
            return this.mName;
        }
        Slog.w(TAG, "getName(): not allowed for non-active and non system user");
        return null;
    }

    /* access modifiers changed from: private */
    public class BluetoothServiceConnection implements ServiceConnection {
        private BluetoothServiceConnection() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            String name = componentName.getClassName();
            Slog.d(BluetoothManagerService.TAG, "BluetoothServiceConnection: " + name);
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(40);
            if (name.equals("com.android.bluetooth.btservice.AdapterService")) {
                msg.arg1 = 1;
            } else if (name.equals("com.android.bluetooth.gatt.GattService")) {
                msg.arg1 = 2;
            } else {
                Slog.e(BluetoothManagerService.TAG, "Unknown service connected: " + name);
                return;
            }
            msg.obj = service;
            BluetoothManagerService.this.mHandler.sendMessage(msg);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            String name = componentName.getClassName();
            Slog.d(BluetoothManagerService.TAG, "BluetoothServiceConnection, disconnected: " + name);
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(41);
            if (name.equals("com.android.bluetooth.btservice.AdapterService")) {
                msg.arg1 = 1;
            } else if (name.equals("com.android.bluetooth.gatt.GattService")) {
                msg.arg1 = 2;
            } else {
                Slog.e(BluetoothManagerService.TAG, "Unknown service disconnected: " + name);
                return;
            }
            BluetoothManagerService.this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class BluetoothHandler extends Handler {
        boolean mGetNameAddressOnly = false;

        BluetoothHandler(Looper looper) {
            super(looper);
        }

        /* JADX INFO: finally extract failed */
        public void handleMessage(Message msg) {
            IBluetoothProfileServiceConnection proxy;
            int i = msg.what;
            if (i == 1) {
                Slog.d(BluetoothManagerService.TAG, "MESSAGE_ENABLE(" + msg.arg1 + "): mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                BluetoothManagerService.this.mHandler.removeMessages(42);
                boolean unused = BluetoothManagerService.this.mEnable = true;
                int state = 0;
                try {
                    BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                    if (BluetoothManagerService.this.mBluetooth != null) {
                        state = BluetoothManagerService.this.mBluetooth.getState();
                        if (state == 15) {
                            Slog.w(BluetoothManagerService.TAG, "BT Enable in BLE_ON State, going to ON");
                            BluetoothManagerService.this.mBluetooth.onLeServiceUp();
                            BluetoothManagerService.this.persistBluetoothSetting(1);
                        } else if (state == 14 || state == 11 || state == 13) {
                            Slog.w(BluetoothManagerService.TAG, "BT is enabling or disableing, ignore new enable request.");
                        }
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        return;
                    }
                } catch (RemoteException e) {
                    Slog.e(BluetoothManagerService.TAG, "", e);
                } catch (Throwable th) {
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    throw th;
                }
                BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                boolean unused2 = BluetoothManagerService.this.mQuietEnable = msg.arg1 == 1;
                if (BluetoothManagerService.this.mBluetooth == null) {
                    BluetoothManagerService bluetoothManagerService = BluetoothManagerService.this;
                    bluetoothManagerService.handleEnable(bluetoothManagerService.mQuietEnable);
                } else if (state != 14 && state != 11 && state != 12 && state != 13) {
                    boolean unused3 = BluetoothManagerService.this.waitForOnOff(false, true);
                    BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 400);
                }
            } else if (i == 2) {
                Slog.d(BluetoothManagerService.TAG, "MESSAGE_DISABLE: mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                BluetoothManagerService.this.mHandler.removeMessages(42);
                if (!BluetoothManagerService.this.mEnable || BluetoothManagerService.this.mBluetooth == null) {
                    boolean unused4 = BluetoothManagerService.this.mEnable = false;
                    BluetoothManagerService.this.handleDisable();
                    return;
                }
                boolean unused5 = BluetoothManagerService.this.waitForOnOff(true, false);
                boolean unused6 = BluetoothManagerService.this.mEnable = false;
                BluetoothManagerService.this.handleDisable();
                boolean unused7 = BluetoothManagerService.this.waitForOnOff(false, false);
            } else if (i == 20) {
                BluetoothManagerService.this.mCallbacks.register((IBluetoothManagerCallback) msg.obj);
            } else if (i == 21) {
                BluetoothManagerService.this.mCallbacks.unregister((IBluetoothManagerCallback) msg.obj);
            } else if (i == 30) {
                BluetoothManagerService.this.mStateChangeCallbacks.register((IBluetoothStateChangeCallback) msg.obj);
            } else if (i == 31) {
                BluetoothManagerService.this.mStateChangeCallbacks.unregister((IBluetoothStateChangeCallback) msg.obj);
            } else if (i == 60) {
                int prevState = msg.arg1;
                int newState = msg.arg2;
                Slog.d(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_STATE_CHANGE: " + BluetoothAdapter.nameForState(prevState) + " > " + BluetoothAdapter.nameForState(newState));
                int unused8 = BluetoothManagerService.this.mState = newState;
                BluetoothManagerService.this.bluetoothStateChangeHandler(prevState, newState);
                if (prevState == 14 && newState == 10 && BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mEnable) {
                    BluetoothManagerService.this.recoverBluetoothServiceFromError(false);
                }
                if (prevState == 11 && newState == 15 && BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mEnable) {
                    BluetoothManagerService.this.recoverBluetoothServiceFromError(true);
                }
                if (prevState == 16 && newState == 10 && BluetoothManagerService.this.mEnable) {
                    Slog.d(BluetoothManagerService.TAG, "Entering STATE_OFF but mEnabled is true; restarting.");
                    boolean unused9 = BluetoothManagerService.this.waitForOnOff(false, true);
                    BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 400);
                }
                if ((newState == 12 || newState == 15) && BluetoothManagerService.this.mErrorRecoveryRetryCounter != 0) {
                    Slog.w(BluetoothManagerService.TAG, "bluetooth is recovered from error");
                    int unused10 = BluetoothManagerService.this.mErrorRecoveryRetryCounter = 0;
                }
            } else if (i != 500) {
                if (i == 100) {
                    Slog.e(BluetoothManagerService.TAG, "MESSAGE_TIMEOUT_BIND");
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    boolean unused11 = BluetoothManagerService.this.mBinding = false;
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                } else if (i == 101) {
                    Slog.e(BluetoothManagerService.TAG, "MESSAGE_TIMEOUT_UNBIND");
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    boolean unused12 = BluetoothManagerService.this.mUnbinding = false;
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                } else if (i == 200) {
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_GET_NAME_AND_ADDRESS");
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (BluetoothManagerService.this.mBluetooth == null && !BluetoothManagerService.this.mBinding) {
                            Slog.d(BluetoothManagerService.TAG, "Binding to service to get name and address");
                            this.mGetNameAddressOnly = true;
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(100), BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
                            if (!BluetoothManagerService.this.doBind(new Intent(IBluetooth.class.getName()), BluetoothManagerService.this.mConnection, 65, UserHandle.CURRENT)) {
                                BluetoothManagerService.this.mHandler.removeMessages(100);
                            } else {
                                boolean unused13 = BluetoothManagerService.this.mBinding = true;
                            }
                        } else if (BluetoothManagerService.this.mBluetooth != null) {
                            Message saveMsg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_SAVE_NAME_AND_ADDRESS);
                            saveMsg.arg1 = 0;
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                BluetoothManagerService.this.mHandler.sendMessage(saveMsg);
                            } else {
                                BluetoothManagerService.this.mHandler.sendMessageDelayed(saveMsg, 500);
                            }
                        }
                    } finally {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    }
                } else if (i == BluetoothManagerService.MESSAGE_SAVE_NAME_AND_ADDRESS) {
                    boolean unbind = false;
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_SAVE_NAME_AND_ADDRESS");
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (!BluetoothManagerService.this.mEnable && BluetoothManagerService.this.mBluetooth != null) {
                            try {
                                BluetoothManagerService.this.mBluetooth.enable();
                            } catch (RemoteException e2) {
                                Slog.e(BluetoothManagerService.TAG, "Unable to call enable()", e2);
                            }
                        }
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            boolean unused14 = BluetoothManagerService.this.waitForBleOn();
                        }
                        try {
                            BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                String name = null;
                                String address = null;
                                try {
                                    name = BluetoothManagerService.this.mBluetooth.getName();
                                    address = BluetoothManagerService.this.mBluetooth.getAddress();
                                } catch (RemoteException re) {
                                    Slog.e(BluetoothManagerService.TAG, "", re);
                                }
                                if (name != null && address != null) {
                                    BluetoothManagerService.this.storeNameAndAddress(name, address);
                                    if (this.mGetNameAddressOnly) {
                                        unbind = true;
                                    }
                                } else if (msg.arg1 < 3) {
                                    Message retryMsg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_SAVE_NAME_AND_ADDRESS);
                                    retryMsg.arg1 = msg.arg1 + 1;
                                    Slog.d(BluetoothManagerService.TAG, "Retrying name/address remote retrieval and save.....Retry count =" + retryMsg.arg1);
                                    BluetoothManagerService.this.mHandler.sendMessageDelayed(retryMsg, 500);
                                } else {
                                    Slog.w(BluetoothManagerService.TAG, "Maximum name/address remoteretrieval retry exceeded");
                                    if (this.mGetNameAddressOnly) {
                                        unbind = true;
                                    }
                                }
                                if (!BluetoothManagerService.this.mEnable) {
                                    try {
                                        BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                                    } catch (RemoteException e3) {
                                        Slog.e(BluetoothManagerService.TAG, "Unable to call disable()", e3);
                                    }
                                }
                            } else {
                                BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(200));
                            }
                            this.mGetNameAddressOnly = false;
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            if (!BluetoothManagerService.this.mEnable && BluetoothManagerService.this.mBluetooth != null) {
                                boolean unused15 = BluetoothManagerService.this.waitForOnOff(false, true);
                            }
                            if (unbind) {
                                BluetoothManagerService.this.unbindAndFinish();
                            }
                        } catch (Throwable th2) {
                            this.mGetNameAddressOnly = false;
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            throw th2;
                        }
                    } catch (Throwable th3) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th3;
                    }
                } else if (i == 300) {
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_USER_SWITCHED");
                    BluetoothManagerService.this.mHandler.removeMessages(300);
                    boolean unused16 = BluetoothManagerService.this.mIsUserSwitch = true;
                    if (BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.isEnabled()) {
                        try {
                            BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                BluetoothManagerService.this.mBluetooth.unregisterCallback(BluetoothManagerService.this.mBluetoothCallback);
                            }
                        } catch (RemoteException re2) {
                            Slog.e(BluetoothManagerService.TAG, "Unable to unregister", re2);
                        } catch (Throwable th4) {
                            BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                            throw th4;
                        }
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        if (BluetoothManagerService.this.mState == 13) {
                            boolean unused17 = BluetoothManagerService.this.waitForBleOn();
                            BluetoothManagerService bluetoothManagerService2 = BluetoothManagerService.this;
                            bluetoothManagerService2.bluetoothStateChangeHandler(bluetoothManagerService2.mState, 15);
                            int unused18 = BluetoothManagerService.this.mState = 15;
                        }
                        if (BluetoothManagerService.this.mState == 15) {
                            BluetoothManagerService bluetoothManagerService3 = BluetoothManagerService.this;
                            bluetoothManagerService3.bluetoothStateChangeHandler(bluetoothManagerService3.mState, 16);
                            int unused19 = BluetoothManagerService.this.mState = 16;
                        }
                        if (BluetoothManagerService.this.mState == 16) {
                            BluetoothManagerService bluetoothManagerService4 = BluetoothManagerService.this;
                            bluetoothManagerService4.bluetoothStateChangeHandler(bluetoothManagerService4.mState, 10);
                            int unused20 = BluetoothManagerService.this.mState = 10;
                        }
                        if (BluetoothManagerService.this.mState == 10) {
                            BluetoothManagerService bluetoothManagerService5 = BluetoothManagerService.this;
                            bluetoothManagerService5.bluetoothStateChangeHandler(bluetoothManagerService5.mState, 14);
                            int unused21 = BluetoothManagerService.this.mState = 14;
                        }
                        if (BluetoothManagerService.this.mState == 14) {
                            boolean unused22 = BluetoothManagerService.this.waitForBleOn();
                            BluetoothManagerService bluetoothManagerService6 = BluetoothManagerService.this;
                            bluetoothManagerService6.bluetoothStateChangeHandler(bluetoothManagerService6.mState, 15);
                            int unused23 = BluetoothManagerService.this.mState = 15;
                        }
                        if (BluetoothManagerService.this.mState == 15) {
                            BluetoothManagerService bluetoothManagerService7 = BluetoothManagerService.this;
                            bluetoothManagerService7.bluetoothStateChangeHandler(bluetoothManagerService7.mState, 11);
                            int unused24 = BluetoothManagerService.this.mState = 11;
                        }
                        boolean unused25 = BluetoothManagerService.this.waitForOnOff(true, false);
                        if (BluetoothManagerService.this.mState == 11) {
                            BluetoothManagerService bluetoothManagerService8 = BluetoothManagerService.this;
                            bluetoothManagerService8.bluetoothStateChangeHandler(bluetoothManagerService8.mState, 12);
                        }
                        BluetoothManagerService.this.unbindAllBluetoothProfileServices();
                        BluetoothManagerService bluetoothManagerService9 = BluetoothManagerService.this;
                        bluetoothManagerService9.addActiveLog(8, bluetoothManagerService9.mContext.getPackageName(), false);
                        BluetoothManagerService.this.handleDisable();
                        BluetoothManagerService.this.bluetoothStateChangeHandler(12, 13);
                        boolean unused26 = BluetoothManagerService.this.waitForBleOn();
                        BluetoothManagerService.this.bluetoothStateChangeHandler(13, 15);
                        BluetoothManagerService.this.bluetoothStateChangeHandler(15, 16);
                        boolean didDisableTimeout = !BluetoothManagerService.this.waitForOnOff(false, true);
                        BluetoothManagerService.this.bluetoothStateChangeHandler(16, 10);
                        BluetoothManagerService.this.sendBluetoothServiceDownCallback();
                        try {
                            BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                IBluetooth unused27 = BluetoothManagerService.this.mBluetooth = null;
                                BluetoothManagerService.this.mContext.unbindService(BluetoothManagerService.this.mConnection);
                            }
                            IBluetoothGatt unused28 = BluetoothManagerService.this.mBluetoothGatt = null;
                            if (didDisableTimeout) {
                                SystemClock.sleep(BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
                            } else {
                                SystemClock.sleep(2000);
                            }
                            BluetoothManagerService.this.mHandler.removeMessages(60);
                            int unused29 = BluetoothManagerService.this.mState = 10;
                            BluetoothManagerService bluetoothManagerService10 = BluetoothManagerService.this;
                            bluetoothManagerService10.addActiveLog(8, bluetoothManagerService10.mContext.getPackageName(), true);
                            boolean unused30 = BluetoothManagerService.this.mEnable = true;
                            BluetoothManagerService bluetoothManagerService11 = BluetoothManagerService.this;
                            bluetoothManagerService11.handleEnable(bluetoothManagerService11.mQuietEnable);
                        } finally {
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        }
                    } else if (BluetoothManagerService.this.mBinding || BluetoothManagerService.this.mBluetooth != null) {
                        Message userMsg = BluetoothManagerService.this.mHandler.obtainMessage(300);
                        userMsg.arg2 = msg.arg2 + 1;
                        BluetoothManagerService.this.mHandler.sendMessageDelayed(userMsg, 200);
                        Slog.d(BluetoothManagerService.TAG, "Retry MESSAGE_USER_SWITCHED " + userMsg.arg2);
                    }
                } else if (i == BluetoothManagerService.MESSAGE_USER_UNLOCKED) {
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_USER_UNLOCKED");
                    BluetoothManagerService.this.mHandler.removeMessages(300);
                    if (!BluetoothManagerService.this.mEnable || BluetoothManagerService.this.mBinding || BluetoothManagerService.this.mBluetooth != null || BluetoothManagerService.this.mIsUserSwitch) {
                        boolean unused31 = BluetoothManagerService.this.mIsUserSwitch = false;
                        return;
                    }
                    Slog.d(BluetoothManagerService.TAG, "Enabled but not bound; retrying after unlock");
                    BluetoothManagerService bluetoothManagerService12 = BluetoothManagerService.this;
                    bluetoothManagerService12.handleEnable(bluetoothManagerService12.mQuietEnable);
                } else if (i == 400) {
                    ProfileServiceConnections psc = (ProfileServiceConnections) BluetoothManagerService.this.mProfileServices.get(Integer.valueOf(msg.arg1));
                    if (psc != null && (proxy = (IBluetoothProfileServiceConnection) msg.obj) != null) {
                        psc.addProxy(proxy);
                    }
                } else if (i != BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE) {
                    switch (i) {
                        case 40:
                            Slog.d(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_SERVICE_CONNECTED: " + msg.arg1);
                            IBinder service = (IBinder) msg.obj;
                            try {
                                BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                                if (msg.arg1 == 2) {
                                    IBluetoothGatt unused32 = BluetoothManagerService.this.mBluetoothGatt = IBluetoothGatt.Stub.asInterface(Binder.allowBlocking(service));
                                    BluetoothManagerService.this.continueFromBleOnState();
                                    return;
                                }
                                boolean unused33 = BluetoothManagerService.this.mIsUserSwitch = false;
                                BluetoothManagerService.this.mHandler.removeMessages(100);
                                boolean unused34 = BluetoothManagerService.this.mBinding = false;
                                IBinder unused35 = BluetoothManagerService.this.mBluetoothBinder = service;
                                IBluetooth unused36 = BluetoothManagerService.this.mBluetooth = IBluetooth.Stub.asInterface(Binder.allowBlocking(service));
                                if (!BluetoothManagerService.this.isNameAndAddressSet()) {
                                    BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(200));
                                    if (this.mGetNameAddressOnly) {
                                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                        return;
                                    }
                                }
                                try {
                                    BluetoothManagerService.this.mBluetooth.registerCallback(BluetoothManagerService.this.mBluetoothCallback);
                                } catch (RemoteException re3) {
                                    Slog.e(BluetoothManagerService.TAG, "Unable to register BluetoothCallback", re3);
                                }
                                BluetoothManagerService.this.sendBluetoothServiceUpCallback();
                                try {
                                    if (!BluetoothManagerService.this.mQuietEnable) {
                                        if (!BluetoothManagerService.this.mBluetooth.enable()) {
                                            Slog.e(BluetoothManagerService.TAG, "IBluetooth.enable() returned false");
                                        }
                                    } else if (!BluetoothManagerService.this.mBluetooth.enableNoAutoConnect()) {
                                        Slog.e(BluetoothManagerService.TAG, "IBluetooth.enableNoAutoConnect() returned false");
                                    }
                                } catch (RemoteException e4) {
                                    Slog.e(BluetoothManagerService.TAG, "Unable to call enable()", e4);
                                }
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                if (!BluetoothManagerService.this.mEnable) {
                                    boolean unused37 = BluetoothManagerService.this.waitForOnOff(true, false);
                                    BluetoothManagerService.this.handleDisable();
                                    boolean unused38 = BluetoothManagerService.this.waitForOnOff(false, false);
                                    return;
                                }
                                return;
                            } finally {
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            }
                        case 41:
                            Slog.e(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_SERVICE_DISCONNECTED(" + msg.arg1 + ")");
                            try {
                                BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                                if (msg.arg1 == 1) {
                                    if (BluetoothManagerService.this.mBluetooth != null) {
                                        IBluetooth unused39 = BluetoothManagerService.this.mBluetooth = null;
                                        boolean unused40 = BluetoothManagerService.this.mIsUserSwitch = false;
                                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                        BluetoothManagerService.this.addCrashLog();
                                        BluetoothManagerService bluetoothManagerService13 = BluetoothManagerService.this;
                                        bluetoothManagerService13.addActiveLog(7, bluetoothManagerService13.mContext.getPackageName(), false);
                                        if (BluetoothManagerService.this.mEnable) {
                                            boolean unused41 = BluetoothManagerService.this.mEnable = false;
                                            BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 200);
                                        }
                                        BluetoothManagerService.this.sendBluetoothServiceDownCallback();
                                        if (BluetoothManagerService.this.mState == 11 || BluetoothManagerService.this.mState == 12) {
                                            BluetoothManagerService.this.bluetoothStateChangeHandler(12, 13);
                                            int unused42 = BluetoothManagerService.this.mState = 13;
                                        }
                                        if (BluetoothManagerService.this.mState == 13) {
                                            BluetoothManagerService.this.bluetoothStateChangeHandler(13, 15);
                                            int unused43 = BluetoothManagerService.this.mState = 15;
                                        }
                                        if (BluetoothManagerService.this.mState == 14 || BluetoothManagerService.this.mState == 15) {
                                            BluetoothManagerService.this.bluetoothStateChangeHandler(15, 16);
                                            int unused44 = BluetoothManagerService.this.mState = 16;
                                        }
                                        if (BluetoothManagerService.this.mState == 16) {
                                            BluetoothManagerService.this.bluetoothStateChangeHandler(16, 10);
                                        }
                                        BluetoothManagerService.this.mHandler.removeMessages(60);
                                        int unused45 = BluetoothManagerService.this.mState = 10;
                                        return;
                                    }
                                } else if (msg.arg1 == 2) {
                                    IBluetoothGatt unused46 = BluetoothManagerService.this.mBluetoothGatt = null;
                                } else {
                                    Slog.e(BluetoothManagerService.TAG, "Unknown argument for service disconnect!");
                                }
                                return;
                            } finally {
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            }
                        case 42:
                            Slog.d(BluetoothManagerService.TAG, "MESSAGE_RESTART_BLUETOOTH_SERVICE");
                            boolean unused47 = BluetoothManagerService.this.mEnable = true;
                            BluetoothManagerService bluetoothManagerService14 = BluetoothManagerService.this;
                            bluetoothManagerService14.addActiveLog(4, bluetoothManagerService14.mContext.getPackageName(), true);
                            BluetoothManagerService bluetoothManagerService15 = BluetoothManagerService.this;
                            bluetoothManagerService15.handleEnable(bluetoothManagerService15.mQuietEnable);
                            return;
                        default:
                            return;
                    }
                } else {
                    ProfileServiceConnections psc2 = (ProfileServiceConnections) msg.obj;
                    removeMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, msg.obj);
                    if (psc2 != null) {
                        boolean unused48 = psc2.bindService();
                    }
                }
            } else if (msg.arg1 == 0 && BluetoothManagerService.this.mEnable) {
                Slog.d(BluetoothManagerService.TAG, "Restore Bluetooth state to disabled");
                BluetoothManagerService.this.persistBluetoothSetting(0);
                boolean unused49 = BluetoothManagerService.this.mEnableExternal = false;
                BluetoothManagerService bluetoothManagerService16 = BluetoothManagerService.this;
                bluetoothManagerService16.sendDisableMsg(9, bluetoothManagerService16.mContext.getPackageName());
            } else if (msg.arg1 == 1 && !BluetoothManagerService.this.mEnable) {
                Slog.d(BluetoothManagerService.TAG, "Restore Bluetooth state to enabled");
                boolean unused50 = BluetoothManagerService.this.mQuietEnableExternal = false;
                boolean unused51 = BluetoothManagerService.this.mEnableExternal = true;
                BluetoothManagerService bluetoothManagerService17 = BluetoothManagerService.this;
                bluetoothManagerService17.sendEnableMsg(false, 9, bluetoothManagerService17.mContext.getPackageName());
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleEnable(boolean quietMode) {
        this.mQuietEnable = quietMode;
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mBluetooth == null && !this.mBinding) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
                if (!doBind(new Intent(IBluetooth.class.getName()), this.mConnection, 65, UserHandle.CURRENT)) {
                    this.mHandler.removeMessages(100);
                    this.mIsUserSwitch = false;
                } else {
                    this.mBinding = true;
                }
            } else if (this.mBluetooth != null) {
                this.mIsUserSwitch = false;
                try {
                    if (!this.mQuietEnable) {
                        if (!this.mBluetooth.enable()) {
                            Slog.e(TAG, "IBluetooth.enable() returned false");
                        }
                    } else if (!this.mBluetooth.enableNoAutoConnect()) {
                        Slog.e(TAG, "IBluetooth.enableNoAutoConnect() returned false");
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to call enable()", e);
                }
            }
        } finally {
            this.mBluetoothLock.writeLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean doBind(Intent intent, ServiceConnection conn, int flags, UserHandle user) {
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, conn, flags, user)) {
            return true;
        }
        Slog.e(TAG, "Fail to bind to: " + intent);
        return false;
    }

    /* access modifiers changed from: private */
    public void handleDisable() {
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                Slog.d(TAG, "Sending off request.");
                if (!this.mBluetooth.disable()) {
                    Slog.e(TAG, "IBluetooth.disable() returned false");
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call disable()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
        this.mBluetoothLock.readLock().unlock();
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0042 A[Catch:{ all -> 0x0075 }] */
    private boolean checkIfCallerIsForegroundUser() {
        boolean valid;
        int callingUser = UserHandle.getCallingUserId();
        int callingUid = Binder.getCallingUid();
        long callingIdentity = Binder.clearCallingIdentity();
        UserInfo ui = ((UserManager) this.mContext.getSystemService("user")).getProfileParent(callingUser);
        int parentUser = ui != null ? ui.id : -10000;
        int callingAppId = UserHandle.getAppId(callingUid);
        try {
            int foregroundUser = ActivityManager.getCurrentUser();
            if (!(callingUser == foregroundUser || parentUser == foregroundUser || callingUser == 999 || callingAppId == 1027)) {
                if (callingAppId != this.mSystemUiUid) {
                    valid = false;
                    if (!valid) {
                        Slog.d(TAG, "checkIfCallerIsForegroundUser: valid=" + valid + " callingUser=" + callingUser + " parentUser=" + parentUser + " foregroundUser=" + foregroundUser);
                    }
                    return valid;
                }
            }
            valid = true;
            if (!valid) {
            }
            return valid;
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    private void sendBleStateChanged(int prevState, int newState) {
        Slog.d(TAG, "Sending BLE State Change: " + BluetoothAdapter.nameForState(prevState) + " > " + BluetoothAdapter.nameForState(newState));
        Intent intent = new Intent("android.bluetooth.adapter.action.BLE_STATE_CHANGED");
        intent.putExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", prevState);
        intent.putExtra("android.bluetooth.adapter.extra.STATE", newState);
        intent.addFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
    }

    /* access modifiers changed from: private */
    public void bluetoothStateChangeHandler(int prevState, int newState) {
        boolean isStandardBroadcast = true;
        if (prevState != newState) {
            boolean intermediate_off = true;
            boolean isUp = false;
            if (newState == 15 || newState == 10) {
                if (!(prevState == 13 && newState == 15)) {
                    intermediate_off = false;
                }
                if (newState == 10) {
                    Slog.d(TAG, "Bluetooth is complete send Service Down");
                    sendBluetoothServiceDownCallback();
                    unbindAndFinish();
                    sendBleStateChanged(prevState, newState);
                    isStandardBroadcast = false;
                } else if (!intermediate_off) {
                    Slog.d(TAG, "Bluetooth is in LE only mode");
                    if (this.mBluetoothGatt != null || !this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
                        continueFromBleOnState();
                    } else {
                        Slog.d(TAG, "Binding Bluetooth GATT service");
                        doBind(new Intent(IBluetoothGatt.class.getName()), this.mConnection, 65, UserHandle.CURRENT);
                    }
                    sendBleStateChanged(prevState, newState);
                    isStandardBroadcast = false;
                } else if (intermediate_off) {
                    Slog.d(TAG, "Intermediate off, back to LE only mode");
                    sendBleStateChanged(prevState, newState);
                    sendBluetoothStateCallback(false);
                    newState = 10;
                    sendBrEdrDownCallback();
                }
            } else if (newState == 12) {
                if (newState == 12) {
                    isUp = true;
                }
                this.mEnable = true;
                sendBluetoothStateCallback(isUp);
                sendBleStateChanged(prevState, newState);
            } else if (newState == 14 || newState == 16) {
                sendBleStateChanged(prevState, newState);
                isStandardBroadcast = false;
            } else if (newState == 11 || newState == 13) {
                sendBleStateChanged(prevState, newState);
            }
            if (isStandardBroadcast) {
                if (prevState == 15) {
                    prevState = 10;
                }
                Intent intent = new Intent("android.bluetooth.adapter.action.STATE_CHANGED");
                intent.putExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", prevState);
                intent.putExtra("android.bluetooth.adapter.extra.STATE", newState);
                intent.addFlags(67108864);
                intent.addFlags(268435456);
                intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
                Slog.d(TAG, "bluetoothStateChangeHandler() - Broadcast Adapter State: " + prevState + " > " + newState);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean waitForOnOff(boolean on, boolean off) {
        int i = 0;
        while (true) {
            if (i >= 10) {
                break;
            }
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth == null) {
                    this.mBluetoothLock.readLock().unlock();
                    break;
                }
                if (on) {
                    if (this.mBluetooth.getState() == 12) {
                        this.mBluetoothLock.readLock().unlock();
                        return true;
                    }
                } else if (off) {
                    if (this.mBluetooth.getState() == 10) {
                        this.mBluetoothLock.readLock().unlock();
                        return true;
                    }
                } else if (this.mBluetooth.getState() != 12) {
                    this.mBluetoothLock.readLock().unlock();
                    return true;
                }
                if (on || off) {
                    SystemClock.sleep(300);
                } else {
                    SystemClock.sleep(50);
                }
                i++;
            } catch (RemoteException e) {
                Slog.e(TAG, "getState()", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        }
        Slog.e(TAG, "waitForOnOff time out");
        return false;
    }

    /* access modifiers changed from: private */
    public boolean waitForBleOn() {
        int i = 0;
        while (true) {
            if (i >= 10) {
                break;
            }
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth == null) {
                    break;
                } else if (this.mBluetooth.getState() == 15) {
                    this.mBluetoothLock.readLock().unlock();
                    return true;
                } else {
                    this.mBluetoothLock.readLock().unlock();
                    SystemClock.sleep(300);
                    i++;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "getState()", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        }
        Slog.e(TAG, "waitForBleOn time out");
        return false;
    }

    /* access modifiers changed from: private */
    public void sendDisableMsg(int reason, String packageName) {
        BluetoothHandler bluetoothHandler = this.mHandler;
        bluetoothHandler.sendMessage(bluetoothHandler.obtainMessage(2));
        addActiveLog(reason, packageName, false);
    }

    /* access modifiers changed from: private */
    public void sendEnableMsg(boolean quietMode, int reason, String packageName) {
        BluetoothHandler bluetoothHandler = this.mHandler;
        bluetoothHandler.sendMessage(bluetoothHandler.obtainMessage(1, quietMode ? 1 : 0, 0));
        addActiveLog(reason, packageName, true);
        this.mLastEnabledTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    public void addActiveLog(int reason, String packageName, boolean enable) {
        int state;
        synchronized (this.mActiveLogs) {
            if (this.mActiveLogs.size() > 20) {
                this.mActiveLogs.remove();
            }
            this.mActiveLogs.add(new ActiveLog(reason, packageName, enable, System.currentTimeMillis()));
        }
        if (enable) {
            state = 1;
        } else {
            state = 2;
        }
        StatsLog.write_non_chained(67, Binder.getCallingUid(), null, state, reason, packageName);
    }

    /* access modifiers changed from: private */
    public void addCrashLog() {
        synchronized (this.mCrashTimestamps) {
            if (this.mCrashTimestamps.size() == 100) {
                this.mCrashTimestamps.removeFirst();
            }
            this.mCrashTimestamps.add(Long.valueOf(System.currentTimeMillis()));
            this.mCrashes++;
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public void recoverBluetoothServiceFromError(boolean clearBle) {
        Slog.e(TAG, "recoverBluetoothServiceFromError");
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                this.mBluetooth.unregisterCallback(this.mBluetoothCallback);
            }
        } catch (RemoteException re) {
            Slog.e(TAG, "Unable to unregister", re);
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
        this.mBluetoothLock.readLock().unlock();
        SystemClock.sleep(500);
        addActiveLog(5, this.mContext.getPackageName(), false);
        handleDisable();
        waitForOnOff(false, true);
        sendBluetoothServiceDownCallback();
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mBluetooth != null) {
                this.mBluetooth = null;
                this.mContext.unbindService(this.mConnection);
            }
            this.mBluetoothGatt = null;
            this.mBluetoothLock.writeLock().unlock();
            this.mHandler.removeMessages(60);
            this.mState = 10;
            if (clearBle) {
                clearBleApps();
            }
            this.mEnable = false;
            int i = this.mErrorRecoveryRetryCounter;
            this.mErrorRecoveryRetryCounter = i + 1;
            if (i < 6) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(42), BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
            }
        } catch (Throwable th2) {
            this.mBluetoothLock.writeLock().unlock();
            throw th2;
        }
    }

    private boolean isBluetoothDisallowed() {
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            return ((UserManager) this.mContext.getSystemService(UserManager.class)).hasUserRestriction("no_bluetooth", UserHandle.SYSTEM);
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: private */
    public void updateOppLauncherComponentState(int userId, boolean bluetoothSharingDisallowed) {
        int newState;
        ComponentName oppLauncherComponent = new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
        if (bluetoothSharingDisallowed) {
            newState = 2;
        } else {
            newState = 0;
        }
        try {
            AppGlobals.getPackageManager().setComponentEnabledSetting(oppLauncherComponent, newState, 1, userId);
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x01f6  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x01fb  */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        String[] args2;
        IBinder iBinder;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            String errorMsg = null;
            boolean protoOut = args.length > 0 && args[0].startsWith(PriorityDump.PROTO_ARG);
            if (!protoOut) {
                writer.println("Bluetooth Status");
                writer.println("  enabled: " + isEnabled());
                writer.println("  state: " + BluetoothAdapter.nameForState(this.mState));
                writer.println("  address: " + this.mAddress);
                writer.println("  name: " + this.mName);
                if (this.mEnable) {
                    long onDuration = SystemClock.elapsedRealtime() - this.mLastEnabledTime;
                    String onDurationString = String.format(Locale.US, "%02d:%02d:%02d.%03d", Integer.valueOf((int) (onDuration / AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT)), Integer.valueOf((int) ((onDuration / 60000) % 60)), Integer.valueOf((int) ((onDuration / 1000) % 60)), Integer.valueOf((int) (onDuration % 1000)));
                    writer.println("  time since enabled: " + onDurationString);
                }
                if (this.mActiveLogs.size() == 0) {
                    writer.println("\nBluetooth never enabled!");
                } else {
                    writer.println("\nEnable log:");
                    Iterator<ActiveLog> it = this.mActiveLogs.iterator();
                    while (it.hasNext()) {
                        writer.println("  " + it.next());
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("\nBluetooth crashed ");
                sb.append(this.mCrashes);
                sb.append(" time");
                String str = "s";
                sb.append(this.mCrashes == 1 ? "" : str);
                writer.println(sb.toString());
                if (this.mCrashes == 100) {
                    writer.println("(last 100)");
                }
                Iterator<Long> it2 = this.mCrashTimestamps.iterator();
                while (it2.hasNext()) {
                    writer.println("  " + ((Object) timeToLog(it2.next().longValue())));
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append(StringUtils.LF);
                sb2.append(this.mBleApps.size());
                sb2.append(" BLE app");
                if (this.mBleApps.size() == 1) {
                    str = "";
                }
                sb2.append(str);
                sb2.append("registered");
                writer.println(sb2.toString());
                Iterator<ClientDeathRecipient> it3 = this.mBleApps.values().iterator();
                while (it3.hasNext()) {
                    writer.println("  " + it3.next().getPackageName());
                }
                writer.println("");
                writer.flush();
                if (args.length == 0) {
                    args2 = new String[]{"--print"};
                    iBinder = this.mBluetoothBinder;
                    if (iBinder != null) {
                        errorMsg = "Bluetooth Service not connected";
                    } else {
                        try {
                            iBinder.dump(fd, args2);
                        } catch (RemoteException e) {
                            errorMsg = "RemoteException while dumping Bluetooth Service";
                        }
                    }
                    if (errorMsg != null && !protoOut) {
                        writer.println(errorMsg);
                        return;
                    }
                }
            }
            args2 = args;
            iBinder = this.mBluetoothBinder;
            if (iBinder != null) {
            }
            if (errorMsg != null) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static String getEnableDisableReasonString(int reason) {
        switch (reason) {
            case 1:
                return "APPLICATION_REQUEST";
            case 2:
                return "AIRPLANE_MODE";
            case 3:
                return "DISALLOWED";
            case 4:
                return "RESTARTED";
            case 5:
                return "START_ERROR";
            case 6:
                return "SYSTEM_BOOT";
            case 7:
                return "CRASH";
            case 8:
                return "USER_SWITCH";
            case 9:
                return "RESTORE_USER_SETTING";
            default:
                return "UNKNOWN[" + reason + "]";
        }
    }
}

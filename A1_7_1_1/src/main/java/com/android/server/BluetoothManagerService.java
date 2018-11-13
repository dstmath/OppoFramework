package com.android.server;

import android.app.ActivityManager;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothCallback;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothHeadset;
import android.bluetooth.IBluetoothManager.Stub;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.util.Slog;
import com.android.server.am.OppoProcessManager;
import com.android.server.display.DisplayTransformManager;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.oppo.IElsaManager;
import com.mediatek.cta.CtaUtils;
import com.oppo.hypnus.Hypnus;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
class BluetoothManagerService extends Stub {
    private static final String ACTION_BOOT_IPO = "android.intent.action.ACTION_BOOT_IPO";
    private static final String ACTION_PACKAGE_DATA_CLEARED = "android.intent.action.PACKAGE_DATA_CLEARED";
    private static final String ACTION_SERVICE_STATE_CHANGED = "com.android.bluetooth.btservice.action.STATE_CHANGED";
    private static final int ADDRESS_LENGTH = 17;
    private static final int ADD_PROXY_DELAY_MS = 100;
    private static final String BLUETOOTH_ADMIN_PERM = "android.permission.BLUETOOTH_ADMIN";
    private static final int BLUETOOTH_OFF = 0;
    private static final int BLUETOOTH_ON_AIRPLANE = 2;
    private static final int BLUETOOTH_ON_BLUETOOTH = 1;
    private static final String BLUETOOTH_PERM = "android.permission.BLUETOOTH";
    private static final boolean DBG = true;
    private static final int ERROR_RESTART_TIME_MS = 3000;
    private static final String EXTRA_ACTION = "action";
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
    private static final int MESSAGE_SAVE_NAME_AND_ADDRESS = 201;
    private static final int MESSAGE_TIMEOUT_BIND = 100;
    private static final int MESSAGE_TIMEOUT_UNBIND = 101;
    private static final int MESSAGE_UNREGISTER_ADAPTER = 21;
    private static final int MESSAGE_UNREGISTER_STATE_CHANGE_CALLBACK = 31;
    private static final int MESSAGE_USER_SWITCHED = 300;
    private static final int MESSAGE_USER_UNLOCKED = 301;
    private static final int MESSAGE_WHOLE_CHIP_RESET = 5010;
    private static final String PACKAGE_NAME_OSHARE = "com.coloros.oshare";
    private static final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
    private static final String SECURE_SETTINGS_BLUETOOTH_ADDR_VALID = "bluetooth_addr_valid";
    private static final String SECURE_SETTINGS_BLUETOOTH_NAME = "bluetooth_name";
    private static final int SERVICE_IBLUETOOTH = 1;
    private static final int SERVICE_IBLUETOOTHGATT = 2;
    private static final int SERVICE_RESTART_TIME_MS = 200;
    private static final String TAG = "BluetoothManagerService";
    private static final int TIMEOUT_BIND_MS = 3000;
    private static final int TIMEOUT_SAVE_MS = 500;
    private static final boolean USER_MODE = false;
    private static final int USER_SWITCHED_TIME_MS = 200;
    private static int mBleAppCount;
    private String mAddress;
    private boolean mBinding;
    Map<IBinder, ClientDeathRecipient> mBleApps;
    private IBluetooth mBluetooth;
    private IBinder mBluetoothBinder;
    private final IBluetoothCallback mBluetoothCallback;
    private IBluetoothGatt mBluetoothGatt;
    private final ReentrantReadWriteLock mBluetoothLock;
    private final RemoteCallbackList<IBluetoothManagerCallback> mCallbacks;
    private BluetoothServiceConnection mConnection;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private boolean mEnable;
    private boolean mEnableBLE;
    private boolean mEnableExternal;
    private int mErrorRecoveryRetryCounter;
    private final BluetoothHandler mHandler;
    private Hypnus mHyp;
    private String mName;
    private final Map<Integer, ProfileServiceConnections> mProfileServices;
    private boolean mQuietEnable;
    private boolean mQuietEnableExternal;
    private final BroadcastReceiver mReceiver;
    private final BroadcastReceiver mReceiverDataCleared;
    private int mState;
    private final RemoteCallbackList<IBluetoothStateChangeCallback> mStateChangeCallbacks;
    private final int mSystemUiUid;
    private boolean mUnbinding;

    private class BluetoothHandler extends Handler {
        boolean mGetNameAddressOnly = false;

        public BluetoothHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Slog.d(BluetoothManagerService.TAG, "Message: " + msg.what);
            Object callback;
            String str;
            StringBuilder append;
            IBluetoothStateChangeCallback callback2;
            ProfileServiceConnections psc;
            switch (msg.what) {
                case 1:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_ENABLE: mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                    BluetoothManagerService.this.mHandler.removeMessages(42);
                    BluetoothManagerService.this.mEnable = true;
                    int state = 0;
                    try {
                        BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            state = BluetoothManagerService.this.mBluetooth.getState();
                            if (state != 15) {
                                if (state == 14 || state == 11) {
                                    Slog.w(BluetoothManagerService.TAG, "BT is enabling, ignore new enable request.");
                                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                                    break;
                                }
                            }
                            Slog.w(BluetoothManagerService.TAG, "BT is in BLE_ON State");
                            BluetoothManagerService.this.mBluetooth.onLeServiceUp();
                            BluetoothManagerService.this.persistBluetoothSetting(1);
                            BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                            break;
                        }
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    } catch (RemoteException e) {
                        Slog.e(BluetoothManagerService.TAG, IElsaManager.EMPTY_PACKAGE, e);
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    } catch (Throwable th) {
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        throw th;
                    }
                    BluetoothManagerService.this.mQuietEnable = msg.arg1 == 1;
                    if (BluetoothManagerService.this.mBluetooth != null) {
                        if (!(state == 14 || state == 11 || state == 12)) {
                            BluetoothManagerService.this.waitForOnOff(false, true);
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 400);
                            break;
                        }
                    }
                    BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                    break;
                case 2:
                    BluetoothManagerService.this.mHandler.removeMessages(42);
                    if (!BluetoothManagerService.this.mEnable || BluetoothManagerService.this.mBluetooth == null || !BluetoothManagerService.this.isEnabled()) {
                        BluetoothManagerService.this.mEnable = false;
                        BluetoothManagerService.this.handleDisable();
                        break;
                    }
                    BluetoothManagerService.this.waitForOnOff(true, false);
                    BluetoothManagerService.this.mEnable = false;
                    BluetoothManagerService.this.handleDisable();
                    BluetoothManagerService.this.waitForOnOff(false, false);
                    break;
                case 20:
                    callback = msg.obj;
                    boolean added = BluetoothManagerService.this.mCallbacks.register(callback, Integer.valueOf(msg.arg1));
                    str = BluetoothManagerService.TAG;
                    append = new StringBuilder().append("Added callback: ");
                    if (callback == null) {
                        callback = "null";
                    }
                    Slog.d(str, append.append(callback).append(":").append(added).toString());
                    break;
                case 21:
                    callback = (IBluetoothManagerCallback) msg.obj;
                    boolean removed = BluetoothManagerService.this.mCallbacks.unregister(callback);
                    str = BluetoothManagerService.TAG;
                    append = new StringBuilder().append("Removed callback: ");
                    if (callback == null) {
                        callback = "null";
                    }
                    Slog.d(str, append.append(callback).append(":").append(removed).toString());
                    break;
                case 30:
                    callback2 = msg.obj;
                    Slog.d(BluetoothManagerService.TAG, "Register callback = " + callback2);
                    if (callback2 != null) {
                        BluetoothManagerService.this.mStateChangeCallbacks.register(callback2, Integer.valueOf(msg.arg1));
                        break;
                    }
                    break;
                case 31:
                    callback2 = (IBluetoothStateChangeCallback) msg.obj;
                    Slog.d(BluetoothManagerService.TAG, "Unregister callback = " + callback2);
                    if (callback2 != null) {
                        BluetoothManagerService.this.mStateChangeCallbacks.unregister(callback2);
                        break;
                    }
                    break;
                case 40:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_SERVICE_CONNECTED: " + msg.arg1);
                    IBinder service = msg.obj;
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (msg.arg1 != 2) {
                            BluetoothManagerService.this.mHandler.removeMessages(100);
                            BluetoothManagerService.this.mBinding = false;
                            BluetoothManagerService.this.mBluetoothBinder = service;
                            BluetoothManagerService.this.mBluetooth = IBluetooth.Stub.asInterface(service);
                            if (!BluetoothManagerService.this.isNameAndAddressSet()) {
                                BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE));
                                if (this.mGetNameAddressOnly) {
                                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                    return;
                                }
                            }
                            if (!BluetoothManagerService.this.mBluetooth.configHciSnoopLog(Secure.getInt(BluetoothManagerService.this.mContentResolver, "bluetooth_hci_log", 0) == 1)) {
                                Slog.e(BluetoothManagerService.TAG, "IBluetooth.configHciSnoopLog return false");
                            }
                            try {
                                BluetoothManagerService.this.mBluetooth.registerCallback(BluetoothManagerService.this.mBluetoothCallback);
                            } catch (Throwable re) {
                                Slog.e(BluetoothManagerService.TAG, "Unable to register BluetoothCallback", re);
                            }
                            BluetoothManagerService.this.sendBluetoothServiceUpCallback();
                            try {
                                if (BluetoothManagerService.this.mQuietEnable) {
                                    if (!BluetoothManagerService.this.mBluetooth.enableNoAutoConnect()) {
                                        Slog.e(BluetoothManagerService.TAG, "IBluetooth.enableNoAutoConnect() returned false");
                                    }
                                } else if (!BluetoothManagerService.this.mBluetooth.enable()) {
                                    Slog.e(BluetoothManagerService.TAG, "IBluetooth.enable() returned false");
                                }
                            } catch (RemoteException e2) {
                                Slog.e(BluetoothManagerService.TAG, "Unable to call enable()", e2);
                            }
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            if (!BluetoothManagerService.this.mEnable) {
                                BluetoothManagerService.this.waitForOnOff(true, false);
                                BluetoothManagerService.this.handleDisable();
                                BluetoothManagerService.this.waitForOnOff(false, false);
                                break;
                            }
                        }
                        BluetoothManagerService.this.mBluetoothGatt = IBluetoothGatt.Stub.asInterface(service);
                        BluetoothManagerService.this.onBluetoothGattServiceUp();
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        break;
                    } catch (RemoteException e22) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to call configHciSnoopLog", e22);
                    } catch (Throwable th2) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th2;
                    }
                    break;
                case 41:
                    Slog.e(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_SERVICE_DISCONNECTED: " + msg.arg1);
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (msg.arg1 != 1) {
                            if (msg.arg1 != 2) {
                                Slog.e(BluetoothManagerService.TAG, "Bad msg.arg1: " + msg.arg1);
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                break;
                            }
                            BluetoothManagerService.this.mBluetoothGatt = null;
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            break;
                        } else if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.mBluetooth = null;
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            if (BluetoothManagerService.this.mEnable) {
                                BluetoothManagerService.this.mEnable = false;
                                BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 200);
                            }
                            BluetoothManagerService.this.sendBluetoothServiceDownCallback();
                            if (BluetoothManagerService.this.mState == 11 || BluetoothManagerService.this.mState == 12) {
                                BluetoothManagerService.this.bluetoothStateChangeHandler(12, 13);
                                BluetoothManagerService.this.mState = 13;
                            }
                            if (BluetoothManagerService.this.mState == 13) {
                                BluetoothManagerService.this.bluetoothStateChangeHandler(13, 10);
                            }
                            BluetoothManagerService.this.mHandler.removeMessages(60);
                            BluetoothManagerService.this.mState = 10;
                            break;
                        } else {
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            break;
                        }
                    } catch (Throwable th22) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th22;
                    }
                case 42:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_RESTART_BLUETOOTH_SERVICE: Restart IBluetooth service");
                    BluetoothManagerService.this.mEnable = true;
                    BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                    break;
                case 60:
                    int prevState = msg.arg1;
                    int newState = msg.arg2;
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_STATE_CHANGE: prevState = " + prevState + ", newState =" + newState);
                    BluetoothManagerService.this.mState = newState;
                    BluetoothManagerService.this.bluetoothStateChangeHandler(prevState, newState);
                    if (prevState == 14 && newState == 10 && BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mEnable) {
                        BluetoothManagerService.this.recoverBluetoothServiceFromError();
                    }
                    if (prevState == 11 && newState == 15 && BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mEnable) {
                        BluetoothManagerService.this.recoverBluetoothServiceFromError();
                    }
                    if (prevState == 16 && newState == 10 && BluetoothManagerService.this.mEnable) {
                        Slog.d(BluetoothManagerService.TAG, "Entering STATE_OFF but mEnabled is true; restarting.");
                        BluetoothManagerService.this.waitForOnOff(false, true);
                        BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 400);
                    }
                    if ((newState == 12 || newState == 15) && BluetoothManagerService.this.mErrorRecoveryRetryCounter != 0) {
                        Slog.w(BluetoothManagerService.TAG, "bluetooth is recovered from error");
                        BluetoothManagerService.this.mErrorRecoveryRetryCounter = 0;
                        break;
                    }
                case 100:
                    Slog.e(BluetoothManagerService.TAG, "MESSAGE_TIMEOUT_BIND");
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    BluetoothManagerService.this.mBinding = false;
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    break;
                case 101:
                    Slog.e(BluetoothManagerService.TAG, "MESSAGE_TIMEOUT_UNBIND");
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    BluetoothManagerService.this.mUnbinding = false;
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    break;
                case DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE /*200*/:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_GET_NAME_AND_ADDRESS");
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (BluetoothManagerService.this.mBluetooth == null && !BluetoothManagerService.this.mBinding) {
                            Slog.d(BluetoothManagerService.TAG, "Binding to service to get name and address");
                            this.mGetNameAddressOnly = true;
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(100), 3000);
                            if (BluetoothManagerService.this.doBind(new Intent(IBluetooth.class.getName()), BluetoothManagerService.this.mConnection, 65, UserHandle.CURRENT)) {
                                BluetoothManagerService.this.mBinding = true;
                            } else {
                                BluetoothManagerService.this.mHandler.removeMessages(100);
                                Slog.e(BluetoothManagerService.TAG, "fail to bind to: " + IBluetooth.class.getName());
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
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        break;
                    } catch (Throwable th222) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th222;
                    }
                    break;
                case BluetoothManagerService.MESSAGE_SAVE_NAME_AND_ADDRESS /*201*/:
                    boolean unbind = false;
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_SAVE_NAME_AND_ADDRESS");
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (!(BluetoothManagerService.this.mEnable || BluetoothManagerService.this.mBluetooth == null)) {
                            BluetoothManagerService.this.mBluetooth.enable();
                        }
                    } catch (RemoteException e222) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to call enable()", e222);
                    } catch (Throwable th2222) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th2222;
                    }
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    if (BluetoothManagerService.this.mBluetooth != null) {
                        BluetoothManagerService.this.waitForBleOn();
                    }
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            String str2 = null;
                            String address = null;
                            str2 = BluetoothManagerService.this.mBluetooth.getName();
                            address = BluetoothManagerService.this.mBluetooth.getAddress();
                            if (str2 != null && address != null) {
                                BluetoothManagerService.this.storeNameAndAddress(str2, address);
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
                                } catch (RemoteException e2222) {
                                    Slog.e(BluetoothManagerService.TAG, "Unable to call disable()", e2222);
                                }
                            }
                        } else {
                            BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE));
                        }
                    } catch (Throwable re2) {
                        Slog.e(BluetoothManagerService.TAG, IElsaManager.EMPTY_PACKAGE, re2);
                    } catch (Throwable th22222) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th22222;
                    }
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    if (!(BluetoothManagerService.this.mEnable || BluetoothManagerService.this.mBluetooth == null)) {
                        BluetoothManagerService.this.waitForOnOff(false, true);
                    }
                    if (unbind) {
                        BluetoothManagerService.this.unbindAndFinish();
                    }
                    this.mGetNameAddressOnly = false;
                    break;
                case 300:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_USER_SWITCHED");
                    BluetoothManagerService.this.mHandler.removeMessages(300);
                    if (!BluetoothManagerService.this.mEnable || BluetoothManagerService.this.mBluetooth == null || BluetoothManagerService.this.mBluetoothGatt == null) {
                        if (BluetoothManagerService.this.mBinding || BluetoothManagerService.this.mBluetooth != null) {
                            Message userMsg = BluetoothManagerService.this.mHandler.obtainMessage(300);
                            userMsg.arg2 = msg.arg2 + 1;
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(userMsg, 200);
                            Slog.d(BluetoothManagerService.TAG, "delay MESSAGE_USER_SWITCHED " + userMsg.arg2);
                            break;
                        }
                    }
                    try {
                        BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.mBluetooth.unregisterCallback(BluetoothManagerService.this.mBluetoothCallback);
                        }
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    } catch (Throwable re22) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to unregister", re22);
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    } catch (Throwable th222222) {
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        throw th222222;
                    }
                    if (BluetoothManagerService.this.mState == 13) {
                        BluetoothManagerService.this.waitForBleOn();
                        BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 15);
                        BluetoothManagerService.this.mState = 15;
                    }
                    if (BluetoothManagerService.this.mState == 15) {
                        BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 16);
                        BluetoothManagerService.this.mState = 16;
                    }
                    if (BluetoothManagerService.this.mState == 16) {
                        BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 10);
                        BluetoothManagerService.this.mState = 10;
                    }
                    if (BluetoothManagerService.this.mState == 10) {
                        BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 14);
                        BluetoothManagerService.this.mState = 14;
                    }
                    if (BluetoothManagerService.this.mState == 14) {
                        BluetoothManagerService.this.waitForBleOn();
                        BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 15);
                        BluetoothManagerService.this.mState = 15;
                    }
                    if (BluetoothManagerService.this.mState == 15) {
                        BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 11);
                        BluetoothManagerService.this.mState = 11;
                    }
                    BluetoothManagerService.this.waitForOnOff(true, false);
                    if (BluetoothManagerService.this.mState == 11) {
                        BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 12);
                    }
                    BluetoothManagerService.this.unbindAllBluetoothProfileServices();
                    BluetoothManagerService.this.handleDisable();
                    BluetoothManagerService.this.bluetoothStateChangeHandler(12, 13);
                    BluetoothManagerService.this.waitForBleOn();
                    BluetoothManagerService.this.bluetoothStateChangeHandler(13, 15);
                    BluetoothManagerService.this.bluetoothStateChangeHandler(15, 16);
                    boolean didDisableTimeout = !BluetoothManagerService.this.waitForOnOff(false, true);
                    BluetoothManagerService.this.bluetoothStateChangeHandler(16, 10);
                    BluetoothManagerService.this.sendBluetoothServiceDownCallback();
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.mBluetooth = null;
                            BluetoothManagerService.this.mContext.unbindService(BluetoothManagerService.this.mConnection);
                        }
                        BluetoothManagerService.this.mBluetoothGatt = null;
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        if (didDisableTimeout) {
                            SystemClock.sleep(3000);
                        } else {
                            SystemClock.sleep(100);
                        }
                        BluetoothManagerService.this.mHandler.removeMessages(60);
                        BluetoothManagerService.this.mState = 10;
                        BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                        break;
                    } catch (Throwable th2222222) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th2222222;
                    }
                    break;
                case 301:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_USER_UNLOCKED");
                    BluetoothManagerService.this.mHandler.removeMessages(300);
                    if (BluetoothManagerService.this.mEnable && !BluetoothManagerService.this.mBinding && BluetoothManagerService.this.mBluetooth == null) {
                        Slog.d(BluetoothManagerService.TAG, "Enabled but not bound; retrying after unlock");
                        BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                        break;
                    }
                case 400:
                    psc = (ProfileServiceConnections) BluetoothManagerService.this.mProfileServices.get(new Integer(msg.arg1));
                    if (psc != null) {
                        psc.addProxy(msg.obj);
                        break;
                    }
                    break;
                case 401:
                    psc = (ProfileServiceConnections) msg.obj;
                    removeMessages(401, msg.obj);
                    if (psc != null) {
                        psc.bindService();
                        break;
                    }
                    break;
                case BluetoothManagerService.MESSAGE_WHOLE_CHIP_RESET /*5010*/:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_WHOLE_CHIP_RESET");
                    BluetoothManagerService.this.handleWholeChipReset();
                    break;
            }
        }
    }

    private class BluetoothServiceConnection implements ServiceConnection {
        /* synthetic */ BluetoothServiceConnection(BluetoothManagerService this$0, BluetoothServiceConnection bluetoothServiceConnection) {
            this();
        }

        private BluetoothServiceConnection() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            Slog.d(BluetoothManagerService.TAG, "BluetoothServiceConnection: " + className.getClassName());
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(40);
            if (className.getClassName().equals("com.android.bluetooth.btservice.AdapterService")) {
                msg.arg1 = 1;
            } else if (className.getClassName().equals("com.android.bluetooth.gatt.GattService")) {
                msg.arg1 = 2;
            } else {
                Slog.e(BluetoothManagerService.TAG, "Unknown service connected: " + className.getClassName());
                return;
            }
            msg.obj = service;
            BluetoothManagerService.this.mHandler.sendMessage(msg);
        }

        public void onServiceDisconnected(ComponentName className) {
            Slog.d(BluetoothManagerService.TAG, "BluetoothServiceConnection, disconnected: " + className.getClassName());
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(41);
            if (className.getClassName().equals("com.android.bluetooth.btservice.AdapterService")) {
                msg.arg1 = 1;
            } else if (className.getClassName().equals("com.android.bluetooth.gatt.GattService")) {
                msg.arg1 = 2;
            } else {
                Slog.e(BluetoothManagerService.TAG, "Unknown service disconnected: " + className.getClassName());
                return;
            }
            BluetoothManagerService.this.mHandler.sendMessage(msg);
        }
    }

    class ClientDeathRecipient implements DeathRecipient {
        ClientDeathRecipient() {
        }

        public void binderDied() {
            Slog.d(BluetoothManagerService.TAG, "Binder is dead - unregister Ble App");
            if (BluetoothManagerService.mBleAppCount > 0) {
                BluetoothManagerService.mBleAppCount = BluetoothManagerService.mBleAppCount - 1;
            }
            if (BluetoothManagerService.mBleAppCount == 0) {
                Slog.d(BluetoothManagerService.TAG, "Disabling LE only mode after application crash");
                try {
                    BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                    if (BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mBluetooth.getState() == 15) {
                        BluetoothManagerService.this.mEnable = false;
                        BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                    }
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                } catch (RemoteException e) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to call onBrEdrDown", e);
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                } catch (Throwable th) {
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    throw th;
                }
            }
        }
    }

    private final class ProfileServiceConnections implements ServiceConnection, DeathRecipient {
        ComponentName mClassName = null;
        Intent mIntent;
        boolean mInvokingProxyCallbacks = false;
        final RemoteCallbackList<IBluetoothProfileServiceConnection> mProxies = new RemoteCallbackList();
        IBinder mService = null;

        ProfileServiceConnections(Intent intent) {
            this.mIntent = intent;
        }

        private boolean bindService() {
            if (this.mIntent != null && this.mService == null && BluetoothManagerService.this.doBind(this.mIntent, this, 0, UserHandle.CURRENT_OR_SELF)) {
                Message msg = BluetoothManagerService.this.mHandler.obtainMessage(401);
                msg.obj = this;
                BluetoothManagerService.this.mHandler.sendMessageDelayed(msg, 3000);
                return true;
            }
            Slog.w(BluetoothManagerService.TAG, "Unable to bind with intent: " + this.mIntent);
            return false;
        }

        private void addProxy(IBluetoothProfileServiceConnection proxy) {
            this.mProxies.register(proxy);
            if (this.mService != null) {
                try {
                    proxy.onServiceConnected(this.mClassName, this.mService);
                } catch (RemoteException e) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to connect to proxy", e);
                }
            } else if (!BluetoothManagerService.this.mHandler.hasMessages(401, this)) {
                Message msg = BluetoothManagerService.this.mHandler.obtainMessage(401);
                msg.obj = this;
                BluetoothManagerService.this.mHandler.sendMessage(msg);
            }
        }

        private void removeProxy(IBluetoothProfileServiceConnection proxy) {
            if (proxy == null) {
                Slog.w(BluetoothManagerService.TAG, "Trying to remove a null proxy");
            } else if (this.mProxies.unregister(proxy)) {
                try {
                    proxy.onServiceDisconnected(this.mClassName);
                } catch (RemoteException e) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to disconnect proxy", e);
                }
            }
        }

        private void removeAllProxies() {
            onServiceDisconnected(this.mClassName);
            this.mProxies.kill();
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothManagerService.this.mHandler.removeMessages(401, this);
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
                            ((IBluetoothProfileServiceConnection) this.mProxies.getBroadcastItem(i)).onServiceConnected(className, service);
                        } catch (RemoteException e2) {
                            Slog.e(BluetoothManagerService.TAG, "Unable to connect to proxy", e2);
                        } catch (Throwable th) {
                            this.mProxies.finishBroadcast();
                            this.mInvokingProxyCallbacks = false;
                        }
                    }
                    this.mProxies.finishBroadcast();
                    this.mInvokingProxyCallbacks = false;
                }
                return;
            } catch (NullPointerException npe) {
                Slog.e(BluetoothManagerService.TAG, "NullPointerException for synchronized(mClassName)", npe);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (this.mService != null && this.mClassName != null) {
                try {
                    synchronized (this.mClassName) {
                        try {
                            this.mService.unlinkToDeath(this, 0);
                            this.mService = null;
                            this.mClassName = null;
                        } catch (NoSuchElementException nsee) {
                            Slog.e(BluetoothManagerService.TAG, "Unable to unlinkToDeath", nsee);
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
                                ((IBluetoothProfileServiceConnection) this.mProxies.getBroadcastItem(i)).onServiceDisconnected(className);
                            } catch (RemoteException e) {
                                Slog.e(BluetoothManagerService.TAG, "Unable to disconnect from proxy", e);
                            } catch (Throwable th) {
                                this.mProxies.finishBroadcast();
                                this.mInvokingProxyCallbacks = false;
                            }
                        }
                        this.mProxies.finishBroadcast();
                        this.mInvokingProxyCallbacks = false;
                    }
                    return;
                } catch (NullPointerException npe) {
                    Slog.e(BluetoothManagerService.TAG, "NullPointerException for synchronized(mClassName)", npe);
                    return;
                } catch (NoSuchElementException e2) {
                    Slog.e(BluetoothManagerService.TAG, "NoSuchElementException when unlinkToDeath", e2);
                    return;
                }
            }
            return;
        }

        public void binderDied() {
            Slog.w(BluetoothManagerService.TAG, "Profile service for profile: " + this.mClassName + " died.");
            onServiceDisconnected(this.mClassName);
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(401);
            msg.obj = this;
            BluetoothManagerService.this.mHandler.sendMessageDelayed(msg, 3000);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.BluetoothManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.BluetoothManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BluetoothManagerService.<clinit>():void");
    }

    private void registerForAirplaneMode(IntentFilter filter) {
        boolean mIsAirplaneSensitive;
        ContentResolver resolver = this.mContext.getContentResolver();
        String airplaneModeRadios = Global.getString(resolver, "airplane_mode_radios");
        String toggleableRadios = Global.getString(resolver, "airplane_mode_toggleable_radios");
        if (airplaneModeRadios == null) {
            mIsAirplaneSensitive = true;
        } else {
            mIsAirplaneSensitive = airplaneModeRadios.contains(OppoProcessManager.RESUME_REASON_BLUETOOTH_STR);
        }
        if (mIsAirplaneSensitive) {
            filter.addAction("android.intent.action.AIRPLANE_MODE");
        }
    }

    BluetoothManagerService(Context context) {
        this.mBluetoothLock = new ReentrantReadWriteLock();
        this.mQuietEnable = false;
        this.mHyp = null;
        this.mProfileServices = new HashMap();
        this.mBluetoothCallback = new IBluetoothCallback.Stub() {
            public void onBluetoothStateChange(int prevState, int newState) throws RemoteException {
                BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(60, prevState, newState));
            }

            public void onWholeChipReset() throws RemoteException {
                BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_WHOLE_CHIP_RESET));
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED".equals(action)) {
                    String newName = intent.getStringExtra("android.bluetooth.adapter.extra.LOCAL_NAME");
                    Slog.d(BluetoothManagerService.TAG, "Bluetooth Adapter name changed to " + newName);
                    if (newName != null) {
                        BluetoothManagerService.this.storeNameAndAddress(newName, null);
                    }
                } else {
                    BroadcastReceiver -get17;
                    if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                        boolean airplaneMode = intent.getBooleanExtra("state", false);
                        Slog.d(BluetoothManagerService.TAG, "Receive airplane mode change: airplaneMode = " + airplaneMode);
                        -get17 = BluetoothManagerService.this.mReceiver;
                        synchronized (-get17) {
                            if (BluetoothManagerService.this.isBluetoothPersistedStateOn()) {
                                if (airplaneMode) {
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
                            } catch (RemoteException e) {
                                Slog.e(BluetoothManagerService.TAG, "Unable to call getState", e);
                                BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                            } catch (Throwable th) {
                                BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                            }
                            Slog.d(BluetoothManagerService.TAG, "Airplane Mode change - current state: " + st);
                            if (airplaneMode) {
                                synchronized (this) {
                                    BluetoothManagerService.mBleAppCount = 0;
                                    BluetoothManagerService.this.mBleApps.clear();
                                }
                                if (st == 15 || st == 14 || st == 10) {
                                    try {
                                        BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                                        if (BluetoothManagerService.this.mBluetooth != null) {
                                            BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                                            BluetoothManagerService.this.mEnable = false;
                                            BluetoothManagerService.this.mEnableExternal = false;
                                        }
                                    } catch (RemoteException e2) {
                                        Slog.e(BluetoothManagerService.TAG, "Unable to call onBrEdrDown", e2);
                                    } finally {
                                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                                    }
                                } else if (st == 12 || st == 11) {
                                    Slog.d(BluetoothManagerService.TAG, "Calling disable");
                                    BluetoothManagerService.this.sendDisableMsg();
                                }
                            } else if (BluetoothManagerService.this.mEnableExternal && st != 12) {
                                Slog.d(BluetoothManagerService.TAG, "Calling enable");
                                BluetoothManagerService.this.sendEnableMsg(BluetoothManagerService.this.mQuietEnableExternal);
                            }
                        }
                    } else if ("android.intent.action.ACTION_BOOT_IPO".equals(action)) {
                        Slog.d(BluetoothManagerService.TAG, "Bluetooth boot completed");
                        -get17 = BluetoothManagerService.this.mReceiver;
                        synchronized (-get17) {
                            boolean bluetoothStateBluetooth = BluetoothManagerService.this.isBluetoothPersistedStateOnBluetooth();
                            boolean waitNvramDaemon = false;
                            Slog.d(BluetoothManagerService.TAG, "Recevie action: " + action + ", mEnableExternal = " + BluetoothManagerService.this.mEnableExternal + ", bluetoothStateBluetooth = " + bluetoothStateBluetooth);
                            if ("android.intent.action.ACTION_BOOT_IPO".equals(action)) {
                                SystemProperties.set("service.nvram_init", "0");
                                SystemProperties.set("ctl.start", "nvram_daemon");
                                waitNvramDaemon = true;
                                Slog.d(BluetoothManagerService.TAG, "Wait for nvram daemon.");
                                if (BluetoothManagerService.this.isBluetoothPersistedStateOn()) {
                                    BluetoothManagerService.this.mEnableExternal = true;
                                    Slog.d(BluetoothManagerService.TAG, "isBluetoothPersistedStateOn() = true, mEnableExternal = " + BluetoothManagerService.this.mEnableExternal);
                                    BluetoothManagerService.this.mHandler.post(new Runnable() {
                                        public void run() {
                                            BluetoothManagerService.this.waitForNvramDaemonReady();
                                        }
                                    });
                                }
                            }
                            if (waitNvramDaemon) {
                            } else {
                                if (BluetoothManagerService.this.mEnableExternal && bluetoothStateBluetooth) {
                                    Slog.d(BluetoothManagerService.TAG, "Auto-enabling Bluetooth.");
                                    BluetoothManagerService.this.sendEnableMsg(BluetoothManagerService.this.mQuietEnableExternal);
                                }
                                if (!BluetoothManagerService.this.isNameAndAddressSet()) {
                                    Slog.d(BluetoothManagerService.TAG, "Retrieving Bluetooth Adapter name and address...");
                                    BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE));
                                }
                            }
                        }
                        return;
                    }
                }
            }
        };
        this.mReceiverDataCleared = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (BluetoothManagerService.ACTION_PACKAGE_DATA_CLEARED.equals(intent.getAction())) {
                    Slog.d(BluetoothManagerService.TAG, "Bluetooth package data cleared");
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        Slog.d(BluetoothManagerService.TAG, "handleEnable: mBluetooth = " + BluetoothManagerService.this.mBluetooth + ", mBinding = " + BluetoothManagerService.this.mBinding);
                        if (BluetoothManagerService.this.mBluetooth == null && BluetoothManagerService.this.mEnable) {
                            Slog.d(BluetoothManagerService.TAG, "Bind AdapterService");
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(100), 3000);
                            if (BluetoothManagerService.this.doBind(new Intent(IBluetooth.class.getName()), BluetoothManagerService.this.mConnection, 65, UserHandle.CURRENT)) {
                                BluetoothManagerService.this.mBinding = true;
                            } else {
                                BluetoothManagerService.this.mHandler.removeMessages(100);
                                Slog.e(BluetoothManagerService.TAG, "Fail to bind to: " + IBluetooth.class.getName());
                            }
                        }
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    } catch (Throwable th) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    }
                }
            }
        };
        this.mBleApps = new HashMap();
        this.mConnection = new BluetoothServiceConnection(this, null);
        this.mHandler = new BluetoothHandler(IoThread.get().getLooper());
        this.mContext = context;
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
        this.mCallbacks = new RemoteCallbackList();
        this.mStateChangeCallbacks = new RemoteCallbackList();
        IntentFilter filterDataCleared = new IntentFilter(ACTION_PACKAGE_DATA_CLEARED);
        filterDataCleared.addDataScheme("package");
        this.mContext.registerReceiver(this.mReceiverDataCleared, filterDataCleared);
        IntentFilter filter_IPO = new IntentFilter("android.intent.action.ACTION_BOOT_IPO");
        filter_IPO.setPriority(JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
        this.mContext.registerReceiver(this.mReceiver, filter_IPO);
        IntentFilter filter = new IntentFilter("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        registerForAirplaneMode(filter);
        filter.setPriority(1000);
        this.mContext.registerReceiver(this.mReceiver, filter);
        loadStoredNameAndAddress();
        if (isBluetoothPersistedStateOn()) {
            Slog.d(TAG, "Startup: Bluetooth persisted state is ON.");
            this.mEnableExternal = true;
            Slog.d(TAG, "isBluetoothPersistedStateOn() = true, mEnableExternal = " + this.mEnableExternal);
        }
        int sysUiUid = -1;
        try {
            sysUiUid = this.mContext.getPackageManager().getPackageUidAsUser("com.android.systemui", DumpState.DUMP_DEXOPT, 0);
        } catch (NameNotFoundException e) {
            Slog.w(TAG, "Unable to resolve SystemUI's UID.", e);
        }
        this.mSystemUiUid = sysUiUid;
    }

    private final boolean isAirplaneModeOn() {
        return Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private final boolean isBluetoothPersistedStateOn() {
        int state = Global.getInt(this.mContentResolver, "bluetooth_on", -1);
        Slog.d(TAG, "Bluetooth persisted state: " + state);
        if (state != 0) {
            return true;
        }
        return false;
    }

    private final boolean isBluetoothPersistedStateOnBluetooth() {
        return Global.getInt(this.mContentResolver, "bluetooth_on", 1) == 1;
    }

    private void persistBluetoothSetting(int value) {
        Slog.d(TAG, "Persisting Bluetooth Setting: " + value);
        Global.putInt(this.mContext.getContentResolver(), "bluetooth_on", value);
    }

    private boolean isNameAndAddressSet() {
        return this.mName != null && this.mAddress != null && this.mName.length() > 0 && this.mAddress.length() > 0;
    }

    private void loadStoredNameAndAddress() {
        Slog.d(TAG, "Loading stored name and address");
        if (this.mContext.getResources().getBoolean(17956951) && Secure.getInt(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDR_VALID, 0) == 0) {
            Slog.d(TAG, "invalid bluetooth name and address stored");
            return;
        }
        this.mName = Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME);
        this.mAddress = Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
        if (!USER_MODE || this.mAddress == null || this.mAddress.length() < 17) {
            Slog.d(TAG, "Stored bluetooth Name=" + this.mName + ",Address=" + this.mAddress);
        } else {
            Slog.d(TAG, "Stored bluetooth Name=" + this.mName + ",Address=" + this.mAddress.substring(0, 9) + "XX:XX:XX");
        }
    }

    private void storeNameAndAddress(String name, String address) {
        if (name != null) {
            Secure.putString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME, name);
            this.mName = name;
            Slog.d(TAG, "Stored Bluetooth name: " + Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME));
        }
        if (address != null) {
            Secure.putString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS, address);
            this.mAddress = address;
            Slog.d(TAG, "Stored Bluetoothaddress: " + Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS));
        }
        if (name != null && address != null) {
            Secure.putInt(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDR_VALID, 1);
        }
    }

    public IBluetooth registerAdapter(IBluetoothManagerCallback callback) {
        if (callback == null) {
            Slog.w(TAG, "Callback is null in registerAdapter");
            return null;
        }
        Message msg = this.mHandler.obtainMessage(20);
        msg.obj = callback;
        msg.arg1 = Binder.getCallingPid();
        this.mHandler.sendMessageAtFrontOfQueue(msg);
        try {
            this.mBluetoothLock.writeLock().lock();
            IBluetooth iBluetooth = this.mBluetooth;
            return iBluetooth;
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
        Slog.d(TAG, "registerStateChangeCallback: callback = " + callback);
        Message msg = this.mHandler.obtainMessage(30);
        msg.obj = callback;
        msg.arg1 = Binder.getCallingPid();
        this.mHandler.sendMessage(msg);
    }

    public void unregisterStateChangeCallback(IBluetoothStateChangeCallback callback) {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Slog.d(TAG, "unregisterStateChangeCallback: callback = " + callback);
        if (callback == null) {
            Slog.e(TAG, "Abnormal case happens, callback is NULL");
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
                    return isEnabled;
                }
                this.mBluetoothLock.readLock().unlock();
                return false;
            } catch (RemoteException e) {
                Slog.e(TAG, "isEnabled()", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        } else {
            Slog.w(TAG, "isEnabled(): not allowed for non-active and non system user");
            return false;
        }
    }

    public int getState() {
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    int state = this.mBluetooth.getState();
                    return state;
                }
                this.mBluetoothLock.readLock().unlock();
                return 10;
            } catch (RemoteException e) {
                Slog.e(TAG, "getState()", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        } else {
            Slog.w(TAG, "getState(): not allowed for non-active and non system user");
            return 10;
        }
    }

    public boolean isBleScanAlwaysAvailable() {
        boolean z = false;
        if (isAirplaneModeOn() && !this.mEnable) {
            return false;
        }
        try {
            if (Global.getInt(this.mContentResolver, "ble_scan_always_enabled") != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private void registerForBleScanModeChange() {
        this.mContentResolver.registerContentObserver(Global.getUriFor("ble_scan_always_enabled"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                if (!BluetoothManagerService.this.isBleScanAlwaysAvailable()) {
                    BluetoothManagerService.this.disableBleScanMode();
                    BluetoothManagerService.this.clearBleApps();
                    try {
                        BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                        }
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    } catch (RemoteException e) {
                        Slog.e(BluetoothManagerService.TAG, "error when disabling bluetooth", e);
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    } catch (Throwable th) {
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        throw th;
                    }
                }
            }
        });
    }

    private void disableBleScanMode() {
        try {
            this.mBluetoothLock.writeLock().lock();
            if (!(this.mBluetooth == null || this.mBluetooth.getState() == 12)) {
                Slog.d(TAG, "Reseting the mEnable flag for clean disable");
                if (!this.mEnableExternal) {
                    this.mEnable = false;
                }
            }
            this.mBluetoothLock.writeLock().unlock();
        } catch (RemoteException e) {
            Slog.e(TAG, "getState()", e);
            this.mBluetoothLock.writeLock().unlock();
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
            throw th;
        }
    }

    public int updateBleAppCount(IBinder token, boolean enable) {
        if (enable) {
            if (((ClientDeathRecipient) this.mBleApps.get(token)) == null) {
                ClientDeathRecipient deathRec = new ClientDeathRecipient();
                try {
                    token.linkToDeath(deathRec, 0);
                    this.mBleApps.put(token, deathRec);
                    synchronized (this) {
                        try {
                            mBleAppCount++;
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                    Slog.d(TAG, "Registered for death Notification");
                } catch (RemoteException e) {
                    throw new IllegalArgumentException("Wake lock is already dead.");
                }
            }
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth == null || !(this.mBluetooth.getState() == 15 || this.mBluetooth.getState() == 12)) {
                    this.mEnableBLE = true;
                }
                this.mBluetoothLock.readLock().unlock();
            } catch (RemoteException e2) {
                Slog.e(TAG, "Unable to call getState", e2);
                this.mBluetoothLock.readLock().unlock();
            } catch (Throwable th2) {
                this.mBluetoothLock.readLock().unlock();
                throw th2;
            }
        }
        ClientDeathRecipient r = (ClientDeathRecipient) this.mBleApps.get(token);
        if (r != null) {
            token.unlinkToDeath(r, 0);
            this.mBleApps.remove(token);
            synchronized (this) {
                try {
                    if (mBleAppCount > 0) {
                        mBleAppCount--;
                    }
                } catch (Throwable th22) {
                    throw th22;
                }
            }
            Slog.d(TAG, "Unregistered for death Notification");
        }
        Slog.d(TAG, "Updated BleAppCount" + mBleAppCount);
        if (mBleAppCount == 0 && this.mEnable) {
            disableBleScanMode();
        }
        return mBleAppCount;
    }

    private void clearBleApps() {
        synchronized (this) {
            this.mBleApps.clear();
            mBleAppCount = 0;
        }
    }

    public boolean isBleAppPresent() {
        Slog.d(TAG, "isBleAppPresent() count: " + mBleAppCount);
        if (mBleAppCount > 0) {
            return true;
        }
        return false;
    }

    private void onBluetoothGattServiceUp() {
        Slog.d(TAG, "BluetoothGatt Service is Up");
        try {
            this.mBluetoothLock.readLock().lock();
            if ((!isBleAppPresent() || this.mEnableExternal || isBluetoothPersistedStateOnBluetooth()) && this.mBluetooth != null && this.mBluetooth.getState() == 15) {
                this.mBluetooth.onLeServiceUp();
                long callingIdentity = Binder.clearCallingIdentity();
                persistBluetoothSetting(1);
                Binder.restoreCallingIdentity(callingIdentity);
            }
            this.mBluetoothLock.readLock().unlock();
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call onServiceUp", e);
            this.mBluetoothLock.readLock().unlock();
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
    }

    private void sendBrEdrDownCallback() {
        Slog.d(TAG, "Calling sendBrEdrDownCallback callbacks");
        if (this.mBluetooth == null) {
            Slog.w(TAG, "Bluetooth handle is null");
            return;
        }
        if (isBleAppPresent()) {
            try {
                if (this.mBluetoothGatt != null) {
                    this.mBluetoothGatt.unregAll();
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Unable to disconnect all apps.", e);
            }
        } else {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    this.mBluetooth.onBrEdrDown();
                }
                this.mBluetoothLock.readLock().unlock();
            } catch (RemoteException e2) {
                Slog.e(TAG, "Call to onBrEdrDown() failed.", e2);
                this.mBluetoothLock.readLock().unlock();
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
        }
    }

    public boolean enableNoAutoConnect() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BLUETOOTH_ADMIN", "Need BLUETOOTH ADMIN permission");
        Slog.d(TAG, "enableNoAutoConnect():  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding);
        int callingAppId = UserHandle.getAppId(Binder.getCallingUid());
        String callingApp = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Slog.d(TAG, "callingApp = " + callingApp);
        if (callingAppId == 1027 || PACKAGE_NAME_OSHARE.equals(callingApp)) {
            synchronized (this.mReceiver) {
                this.mQuietEnableExternal = true;
                this.mEnableExternal = true;
                sendEnableMsg(true);
            }
            return true;
        }
        throw new SecurityException("no permission to enable Bluetooth quietly");
    }

    public boolean enable() {
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.BLUETOOTH_ADMIN", "Need BLUETOOTH ADMIN permission");
            CtaUtils.enforceCheckPermission("com.mediatek.permission.CTA_ENABLE_BT", "Enable bluetooth");
            Slog.d(TAG, "enable():  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding + " mState = " + this.mState);
            if (this.mHyp == null) {
                this.mHyp = new Hypnus();
            }
            if (this.mHyp != null) {
                this.mHyp.hypnusSetAction(12, 2500);
            }
            synchronized (this.mReceiver) {
                this.mQuietEnableExternal = false;
                if (this.mEnableBLE) {
                    this.mEnableBLE = false;
                } else {
                    this.mEnableExternal = true;
                }
                sendEnableMsg(false);
            }
            Slog.d(TAG, "enable returning");
            return true;
        }
        Slog.w(TAG, "enable(): not allowed for non-active and non system user");
        return false;
    }

    public boolean disable(boolean persist) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BLUETOOTH_ADMIN", "Need BLUETOOTH ADMIN permissicacheNameAndAddresson");
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            Slog.d(TAG, "disable(): mBluetooth = " + this.mBluetooth + " mBinding = " + this.mBinding);
            synchronized (this.mReceiver) {
                if (persist) {
                    long callingIdentity = Binder.clearCallingIdentity();
                    persistBluetoothSetting(0);
                    Binder.restoreCallingIdentity(callingIdentity);
                }
                this.mEnableExternal = false;
                sendDisableMsg();
            }
            return true;
        }
        Slog.w(TAG, "disable(): not allowed for non-active and non system user");
        return false;
    }

    public void unbindAndFinish() {
        Slog.d(TAG, "unbindAndFinish(): " + this.mBluetooth + " mBinding = " + this.mBinding);
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mUnbinding) {
                this.mBluetoothLock.writeLock().unlock();
                return;
            }
            this.mUnbinding = true;
            this.mHandler.removeMessages(60);
            this.mHandler.removeMessages(401);
            if (this.mBluetooth != null) {
                this.mBluetooth.unregisterCallback(this.mBluetoothCallback);
                Slog.d(TAG, "Sending unbind request.");
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
        } catch (RemoteException re) {
            Slog.e(TAG, "Unable to unregister BluetoothCallback", re);
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
        }
    }

    public IBluetoothGatt getBluetoothGatt() {
        return this.mBluetoothGatt;
    }

    /* JADX WARNING: Missing block: B:24:0x0083, code:
            r0 = r9.mHandler.obtainMessage(400);
            r0.arg1 = r10;
            r0.obj = r11;
            r9.mHandler.sendMessageDelayed(r0, 100);
     */
    /* JADX WARNING: Missing block: B:25:0x0096, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean bindBluetoothProfileService(int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
        if (this.mEnable ? isEnabled() : false) {
            synchronized (this.mProfileServices) {
                if (((ProfileServiceConnections) this.mProfileServices.get(new Integer(bluetoothProfile))) == null) {
                    Slog.d(TAG, "Creating new ProfileServiceConnections object for profile: " + bluetoothProfile);
                    if (bluetoothProfile != 1) {
                        return false;
                    }
                    ProfileServiceConnections psc = new ProfileServiceConnections(new Intent(IBluetoothHeadset.class.getName()));
                    if (psc.bindService()) {
                        this.mProfileServices.put(new Integer(bluetoothProfile), psc);
                    } else {
                        return false;
                    }
                }
            }
        }
        Slog.d(TAG, "Trying to bind to profile: " + bluetoothProfile + ", while Bluetooth was disabled");
        return false;
    }

    public void unbindBluetoothProfileService(int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
        synchronized (this.mProfileServices) {
            ProfileServiceConnections psc = (ProfileServiceConnections) this.mProfileServices.get(new Integer(bluetoothProfile));
            if (psc == null) {
                return;
            }
            psc.removeProxy(proxy);
        }
    }

    private void unbindAllBluetoothProfileServices() {
        synchronized (this.mProfileServices) {
            for (Integer i : this.mProfileServices.keySet()) {
                ProfileServiceConnections psc = (ProfileServiceConnections) this.mProfileServices.get(i);
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
        if (this.mEnableExternal && isBluetoothPersistedStateOnBluetooth()) {
            Slog.d(TAG, "Auto-enabling Bluetooth.");
            sendEnableMsg(this.mQuietEnableExternal);
        } else if (!isNameAndAddressSet()) {
            Slog.d(TAG, "Getting adapter name and address");
            this.mHandler.sendMessage(this.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE));
        }
    }

    public void handleOnSwitchUser(int userHandle) {
        Slog.d(TAG, "User " + userHandle + " switched");
        this.mHandler.obtainMessage(300, userHandle, 0).sendToTarget();
    }

    public void handleOnUnlockUser(int userHandle) {
        Slog.d(TAG, "User " + userHandle + " unlocked");
        this.mHandler.obtainMessage(301, userHandle, 0).sendToTarget();
    }

    private void sendBluetoothStateCallback(boolean isUp) {
        int i;
        try {
            int n = this.mStateChangeCallbacks.beginBroadcast();
            Slog.d(TAG, "Broadcasting onBluetoothStateChange(" + isUp + ") to " + n + " receivers.");
            i = 0;
            while (i < n) {
                if (tryResumeProcess(((Integer) this.mStateChangeCallbacks.getBroadcastCookie(i)).intValue())) {
                    ((IBluetoothStateChangeCallback) this.mStateChangeCallbacks.getBroadcastItem(i)).onBluetoothStateChange(isUp);
                } else {
                    Slog.e(TAG, "sendBluetoothStateCallback() resume failed, don't call back!!! i = " + i);
                }
                i++;
            }
            this.mStateChangeCallbacks.finishBroadcast();
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call onBluetoothStateChange() on callback #" + i, e);
        } catch (Throwable th) {
            this.mStateChangeCallbacks.finishBroadcast();
        }
    }

    private void sendBluetoothServiceUpCallback() {
        Slog.d(TAG, "Calling onBluetoothServiceUp callbacks");
        int i;
        try {
            int n = this.mCallbacks.beginBroadcast();
            Slog.d(TAG, "Broadcasting onBluetoothServiceUp() to " + n + " receivers.");
            i = 0;
            while (i < n) {
                if (tryResumeProcess(((Integer) this.mCallbacks.getBroadcastCookie(i)).intValue())) {
                    ((IBluetoothManagerCallback) this.mCallbacks.getBroadcastItem(i)).onBluetoothServiceUp(this.mBluetooth);
                } else {
                    Slog.e(TAG, "sendBluetoothServiceUpCallback() resume failed, don't call back!!! i = " + i);
                }
                i++;
            }
            this.mCallbacks.finishBroadcast();
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call onBluetoothServiceUp() on callback #" + i, e);
        } catch (Throwable th) {
            this.mCallbacks.finishBroadcast();
        }
    }

    private void sendBluetoothServiceDownCallback() {
        Slog.d(TAG, "Calling onBluetoothServiceDown callbacks");
        int i;
        try {
            int n = this.mCallbacks.beginBroadcast();
            Slog.d(TAG, "Broadcasting onBluetoothServiceDown() to " + n + " receivers.");
            i = 0;
            while (i < n) {
                if (tryResumeProcess(((Integer) this.mCallbacks.getBroadcastCookie(i)).intValue())) {
                    ((IBluetoothManagerCallback) this.mCallbacks.getBroadcastItem(i)).onBluetoothServiceDown();
                } else {
                    Slog.e(TAG, "sendBluetoothServiceDownCallback() resume failed, don't call back!!! i = " + i);
                }
                i++;
            }
            this.mCallbacks.finishBroadcast();
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call onBluetoothServiceDown() on callback #" + i, e);
        } catch (Throwable th) {
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
                if (this.mBluetooth != null) {
                    String address = this.mBluetooth.getAddress();
                    return address;
                }
                this.mBluetoothLock.readLock().unlock();
                Slog.e(TAG, "getAddress: Return from mAddress = " + this.mAddress);
                return this.mAddress;
            } catch (RemoteException e) {
                Slog.e(TAG, "getAddress(): Unable to retrieve address remotely. Returning cached address", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        }
    }

    public String getName() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    String name = this.mBluetooth.getName();
                    return name;
                }
                this.mBluetoothLock.readLock().unlock();
                Slog.e(TAG, "getAddress: Return from mName = " + this.mName);
                return this.mName;
            } catch (RemoteException e) {
                Slog.e(TAG, "getName(): Unable to retrieve name remotely. Returning cached name", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        } else {
            Slog.w(TAG, "getName(): not allowed for non-active and non system user");
            return null;
        }
    }

    private void handleWholeChipReset() {
        Slog.d(TAG, "handleWholeChipReset");
        sendDisableMsg();
        sendEnableMsg(this.mQuietEnableExternal);
    }

    private void handleEnable(boolean quietMode) {
        this.mQuietEnable = quietMode;
        try {
            this.mBluetoothLock.writeLock().lock();
            Slog.d(TAG, "handleEnable: mBluetooth = " + this.mBluetooth + ", mBinding = " + this.mBinding + "quietMode = " + quietMode);
            if (this.mBluetooth == null && !this.mBinding) {
                Slog.d(TAG, "Bind AdapterService");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 3000);
                if (doBind(new Intent(IBluetooth.class.getName()), this.mConnection, 65, UserHandle.CURRENT)) {
                    this.mBinding = true;
                } else {
                    this.mHandler.removeMessages(100);
                    Slog.e(TAG, "Fail to bind to: " + IBluetooth.class.getName());
                }
            } else if (this.mBluetooth != null) {
                if (this.mQuietEnable) {
                    if (!this.mBluetooth.enableNoAutoConnect()) {
                        Slog.e(TAG, "IBluetooth.enableNoAutoConnect() returned false");
                    }
                } else if (!this.mBluetooth.enable()) {
                    Slog.e(TAG, "IBluetooth.enable() returned false");
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call enable()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
        }
        this.mBluetoothLock.writeLock().unlock();
    }

    boolean doBind(Intent intent, ServiceConnection conn, int flags, UserHandle user) {
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, conn, flags, user)) {
            return true;
        }
        Slog.e(TAG, "Fail to bind to: " + intent);
        return false;
    }

    private void handleDisable() {
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                Slog.d(TAG, "Sending off request.");
                if (!this.mBluetooth.disable()) {
                    Slog.e(TAG, "IBluetooth.disable() returned false");
                }
            }
            this.mBluetoothLock.readLock().unlock();
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call disable()", e);
            this.mBluetoothLock.readLock().unlock();
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
    }

    private boolean checkIfCallerIsForegroundUser() {
        int callingUser = UserHandle.getCallingUserId();
        int callingUid = Binder.getCallingUid();
        long callingIdentity = Binder.clearCallingIdentity();
        UserInfo ui = ((UserManager) this.mContext.getSystemService("user")).getProfileParent(callingUser);
        int parentUser = ui != null ? ui.id : -10000;
        int callingAppId = UserHandle.getAppId(callingUid);
        boolean valid = false;
        try {
            int foregroundUser = ActivityManager.getCurrentUser();
            valid = (callingUser == foregroundUser || parentUser == foregroundUser || callingAppId == 1027) ? true : callingAppId == this.mSystemUiUid;
            Slog.d(TAG, "checkIfCallerIsForegroundUser: valid=" + valid + " callingUser=" + callingUser + " parentUser=" + parentUser + " foregroundUser=" + foregroundUser);
            return valid;
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    private void sendBleStateChanged(int prevState, int newState) {
        Slog.d(TAG, "BLE State Change Intent: " + prevState + " -> " + newState);
        Intent intent = new Intent("android.bluetooth.adapter.action.BLE_STATE_CHANGED");
        intent.putExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", prevState);
        intent.putExtra("android.bluetooth.adapter.extra.STATE", newState);
        intent.addFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
    }

    private void bluetoothStateChangeHandler(int prevState, int newState) {
        boolean isStandardBroadcast = true;
        Slog.d(TAG, "bluetoothStateChangeHandler: " + prevState + " ->  " + newState);
        if (prevState != newState) {
            if (newState == 15 || newState == 10) {
                boolean intermediate_off = prevState == 13 ? newState == 15 : false;
                if (newState == 10) {
                    Slog.d(TAG, "Bluetooth is complete turn off");
                    sendBluetoothServiceDownCallback();
                    unbindAndFinish();
                    sendBleStateChanged(prevState, newState);
                    isStandardBroadcast = false;
                } else if (!intermediate_off) {
                    Slog.d(TAG, "Bluetooth is in LE only mode");
                    if (this.mBluetoothGatt != null) {
                        Slog.d(TAG, "Calling BluetoothGattServiceUp");
                        onBluetoothGattServiceUp();
                    } else {
                        Slog.d(TAG, "Binding Bluetooth GATT service");
                        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
                            doBind(new Intent(IBluetoothGatt.class.getName()), this.mConnection, 65, UserHandle.CURRENT);
                        }
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
                boolean isUp = newState == 12;
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
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
            }
        }
    }

    private boolean waitForOnOff(boolean on, boolean off) {
        int i = 0;
        while (i < 20) {
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
                this.mBluetoothLock.readLock().unlock();
                if (on || off) {
                    SystemClock.sleep(300);
                } else {
                    SystemClock.sleep(50);
                }
                i++;
            } catch (RemoteException e) {
                Slog.e(TAG, "getState()", e);
                this.mBluetoothLock.readLock().unlock();
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
        }
        Slog.e(TAG, "waitForOnOff time out");
        return false;
    }

    private boolean waitForBleOn() {
        int i = 0;
        while (i < 10) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth == null) {
                    this.mBluetoothLock.readLock().unlock();
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
                this.mBluetoothLock.readLock().unlock();
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
        }
        Slog.e(TAG, "waitForBleOn time out");
        return false;
    }

    private void sendDisableMsg() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
    }

    private void sendEnableMsg(boolean quietMode) {
        int i;
        BluetoothHandler bluetoothHandler = this.mHandler;
        BluetoothHandler bluetoothHandler2 = this.mHandler;
        if (quietMode) {
            i = 1;
        } else {
            i = 0;
        }
        bluetoothHandler.sendMessage(bluetoothHandler2.obtainMessage(1, i, 0));
    }

    private void recoverBluetoothServiceFromError() {
        Slog.e(TAG, "recoverBluetoothServiceFromError");
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                this.mBluetooth.unregisterCallback(this.mBluetoothCallback);
            }
            this.mBluetoothLock.readLock().unlock();
        } catch (RemoteException re) {
            Slog.e(TAG, "Unable to unregister", re);
            this.mBluetoothLock.readLock().unlock();
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
        SystemClock.sleep(500);
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
            this.mEnable = false;
            int i = this.mErrorRecoveryRetryCounter;
            this.mErrorRecoveryRetryCounter = i + 1;
            if (i < 6) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(42), 3000);
            }
        } catch (Throwable th2) {
            this.mBluetoothLock.writeLock().unlock();
            throw th2;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        String errorMsg = null;
        if (this.mBluetoothBinder == null) {
            errorMsg = "Bluetooth Service not connected";
        } else {
            try {
                this.mBluetoothBinder.dump(fd, args);
            } catch (RemoteException e) {
                errorMsg = "RemoteException while calling Bluetooth Service";
            }
        }
        if (errorMsg != null && (args.length <= 0 || !args[0].startsWith("--proto"))) {
            writer.println(errorMsg);
        }
    }

    private void waitForNvramDaemonReady() {
        for (int i = 0; i < 5; i++) {
            if (SystemProperties.get("service.nvram_init").equals("Ready")) {
                Slog.d(TAG, "Nvram daemon is ready.");
                boolean bluetoothStateBluetooth = isBluetoothPersistedStateOnBluetooth();
                if (this.mEnableExternal && bluetoothStateBluetooth) {
                    Slog.d(TAG, "Auto-enabling Bluetooth.");
                    sendEnableMsg(this.mQuietEnableExternal);
                }
                if (!isNameAndAddressSet()) {
                    Slog.d(TAG, "Retrieving Bluetooth Adapter name and address...");
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE));
                    return;
                }
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }

    private boolean tryResumeProcess(int pid) {
        int count = 0;
        if (!Process.isProcessSuspend(pid)) {
            return true;
        }
        do {
            int i = count;
            count = i + 1;
            if (i < 3) {
                Process.sendSignal(pid, 18);
            } else {
                Slog.e(TAG, "tryResumeProcess() failed count = " + count + "; pid = " + pid);
                return false;
            }
        } while (Process.isProcessSuspend(pid));
        Slog.d(TAG, "tryResumeProcess() count = " + count);
        return true;
    }
}

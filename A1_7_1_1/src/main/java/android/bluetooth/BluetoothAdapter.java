package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanSettings.Builder;
import android.content.Context;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SynchronousResultReceiver;
import android.os.SynchronousResultReceiver.Result;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public final class BluetoothAdapter {
    public static final String ACTION_BLE_ACL_CONNECTED = "android.bluetooth.adapter.action.BLE_ACL_CONNECTED";
    public static final String ACTION_BLE_ACL_DISCONNECTED = "android.bluetooth.adapter.action.BLE_ACL_DISCONNECTED";
    public static final String ACTION_BLE_STATE_CHANGED = "android.bluetooth.adapter.action.BLE_STATE_CHANGED";
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_DISCOVERY_FINISHED = "android.bluetooth.adapter.action.DISCOVERY_FINISHED";
    public static final String ACTION_DISCOVERY_STARTED = "android.bluetooth.adapter.action.DISCOVERY_STARTED";
    public static final String ACTION_LOCAL_NAME_CHANGED = "android.bluetooth.adapter.action.LOCAL_NAME_CHANGED";
    public static final String ACTION_REQUEST_BLE_SCAN_ALWAYS_AVAILABLE = "android.bluetooth.adapter.action.REQUEST_BLE_SCAN_ALWAYS_AVAILABLE";
    public static final String ACTION_REQUEST_DISCOVERABLE = "android.bluetooth.adapter.action.REQUEST_DISCOVERABLE";
    public static final String ACTION_REQUEST_ENABLE = "android.bluetooth.adapter.action.REQUEST_ENABLE";
    public static final String ACTION_SCAN_MODE_CHANGED = "android.bluetooth.adapter.action.SCAN_MODE_CHANGED";
    public static final String ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private static final int ADDRESS_LENGTH = 17;
    public static final String BLUETOOTH_MANAGER_SERVICE = "bluetooth_manager";
    private static final boolean DBG = true;
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    public static final int ERROR = Integer.MIN_VALUE;
    public static final String EXTRA_CONNECTION_STATE = "android.bluetooth.adapter.extra.CONNECTION_STATE";
    public static final String EXTRA_DISCOVERABLE_DURATION = "android.bluetooth.adapter.extra.DISCOVERABLE_DURATION";
    public static final String EXTRA_LOCAL_NAME = "android.bluetooth.adapter.extra.LOCAL_NAME";
    public static final String EXTRA_PREVIOUS_CONNECTION_STATE = "android.bluetooth.adapter.extra.PREVIOUS_CONNECTION_STATE";
    public static final String EXTRA_PREVIOUS_SCAN_MODE = "android.bluetooth.adapter.extra.PREVIOUS_SCAN_MODE";
    public static final String EXTRA_PREVIOUS_STATE = "android.bluetooth.adapter.extra.PREVIOUS_STATE";
    public static final String EXTRA_SCAN_MODE = "android.bluetooth.adapter.extra.SCAN_MODE";
    public static final String EXTRA_STATE = "android.bluetooth.adapter.extra.STATE";
    public static final int SCAN_MODE_CONNECTABLE = 21;
    public static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;
    public static final int SCAN_MODE_NONE = 20;
    public static final int SOCKET_CHANNEL_AUTO_STATIC_NO_SDP = -2;
    public static final int STATE_BLE_ON = 15;
    public static final int STATE_BLE_TURNING_OFF = 16;
    public static final int STATE_BLE_TURNING_ON = 14;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_DISCONNECTING = 3;
    public static final int STATE_OFF = 10;
    public static final int STATE_ON = 12;
    public static final int STATE_TURNING_OFF = 13;
    public static final int STATE_TURNING_ON = 11;
    private static final String TAG = "BluetoothAdapter";
    private static final boolean VDBG = false;
    private static BluetoothAdapter sAdapter;
    private static BluetoothLeAdvertiser sBluetoothLeAdvertiser;
    private static BluetoothLeScanner sBluetoothLeScanner;
    private final Map<LeScanCallback, ScanCallback> mLeScanClients;
    private final Object mLock;
    private final IBluetoothManagerCallback mManagerCallback;
    private final IBluetoothManager mManagerService;
    private final ArrayList<IBluetoothManagerCallback> mProxyServiceStateCallbacks;
    private IBluetooth mService;
    private final ReentrantReadWriteLock mServiceLock;
    private final IBinder mToken;

    public interface BluetoothStateChangeCallback {
        void onBluetoothStateChange(boolean z);
    }

    public interface LeScanCallback {
        void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr);
    }

    public class StateChangeCallbackWrapper extends Stub {
        private BluetoothStateChangeCallback mCallback;
        final /* synthetic */ BluetoothAdapter this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.<init>(android.bluetooth.BluetoothAdapter, android.bluetooth.BluetoothAdapter$BluetoothStateChangeCallback):void, dex:  in method: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.<init>(android.bluetooth.BluetoothAdapter, android.bluetooth.BluetoothAdapter$BluetoothStateChangeCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.<init>(android.bluetooth.BluetoothAdapter, android.bluetooth.BluetoothAdapter$BluetoothStateChangeCallback):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        StateChangeCallbackWrapper(android.bluetooth.BluetoothAdapter r1, android.bluetooth.BluetoothAdapter.BluetoothStateChangeCallback r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.<init>(android.bluetooth.BluetoothAdapter, android.bluetooth.BluetoothAdapter$BluetoothStateChangeCallback):void, dex:  in method: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.<init>(android.bluetooth.BluetoothAdapter, android.bluetooth.BluetoothAdapter$BluetoothStateChangeCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.<init>(android.bluetooth.BluetoothAdapter, android.bluetooth.BluetoothAdapter$BluetoothStateChangeCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.onBluetoothStateChange(boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onBluetoothStateChange(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.onBluetoothStateChange(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothAdapter.StateChangeCallbackWrapper.onBluetoothStateChange(boolean):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.bluetooth.BluetoothAdapter.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.bluetooth.BluetoothAdapter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothAdapter.<clinit>():void");
    }

    public static synchronized BluetoothAdapter getDefaultAdapter() {
        BluetoothAdapter bluetoothAdapter;
        synchronized (BluetoothAdapter.class) {
            if (sAdapter == null) {
                IBinder b = ServiceManager.getService(BLUETOOTH_MANAGER_SERVICE);
                if (b != null) {
                    sAdapter = new BluetoothAdapter(IBluetoothManager.Stub.asInterface(b));
                } else {
                    Log.e(TAG, "Bluetooth binder is null");
                }
            }
            bluetoothAdapter = sAdapter;
        }
        return bluetoothAdapter;
    }

    BluetoothAdapter(IBluetoothManager managerService) {
        this.mServiceLock = new ReentrantReadWriteLock();
        this.mLock = new Object();
        this.mManagerCallback = new IBluetoothManagerCallback.Stub() {
            public void onBluetoothServiceUp(IBluetooth bluetoothService) {
                Log.d(BluetoothAdapter.TAG, "onBluetoothServiceUp: " + bluetoothService);
                BluetoothAdapter.this.mServiceLock.writeLock().lock();
                BluetoothAdapter.this.mService = bluetoothService;
                BluetoothAdapter.this.mServiceLock.writeLock().unlock();
                synchronized (BluetoothAdapter.this.mProxyServiceStateCallbacks) {
                    for (IBluetoothManagerCallback cb : BluetoothAdapter.this.mProxyServiceStateCallbacks) {
                        if (cb != null) {
                            try {
                                cb.onBluetoothServiceUp(bluetoothService);
                            } catch (Exception e) {
                                Log.e(BluetoothAdapter.TAG, "", e);
                            }
                        } else {
                            Log.d(BluetoothAdapter.TAG, "onBluetoothServiceUp: cb is null!!!");
                        }
                    }
                }
            }

            public void onBluetoothServiceDown() {
                Log.d(BluetoothAdapter.TAG, "onBluetoothServiceDown: " + BluetoothAdapter.this.mService);
                try {
                    BluetoothAdapter.this.mServiceLock.writeLock().lock();
                    BluetoothAdapter.this.mService = null;
                    if (BluetoothAdapter.this.mLeScanClients != null) {
                        BluetoothAdapter.this.mLeScanClients.clear();
                    }
                    if (BluetoothAdapter.sBluetoothLeAdvertiser != null) {
                        BluetoothAdapter.sBluetoothLeAdvertiser.cleanup();
                    }
                    if (BluetoothAdapter.sBluetoothLeScanner != null) {
                        BluetoothAdapter.sBluetoothLeScanner.cleanup();
                    }
                    BluetoothAdapter.this.mServiceLock.writeLock().unlock();
                    synchronized (BluetoothAdapter.this.mProxyServiceStateCallbacks) {
                        for (IBluetoothManagerCallback cb : BluetoothAdapter.this.mProxyServiceStateCallbacks) {
                            if (cb != null) {
                                try {
                                    cb.onBluetoothServiceDown();
                                } catch (Exception e) {
                                    Log.e(BluetoothAdapter.TAG, "", e);
                                }
                            } else {
                                Log.d(BluetoothAdapter.TAG, "onBluetoothServiceDown: cb is null!!!");
                            }
                        }
                    }
                } catch (Throwable th) {
                    BluetoothAdapter.this.mServiceLock.writeLock().unlock();
                }
            }

            public void onBrEdrDown() {
                Log.i(BluetoothAdapter.TAG, "onBrEdrDown:");
            }
        };
        this.mProxyServiceStateCallbacks = new ArrayList();
        if (managerService == null) {
            throw new IllegalArgumentException("bluetooth manager service is null");
        }
        try {
            this.mServiceLock.writeLock().lock();
            this.mService = managerService.registerAdapter(this.mManagerCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.writeLock().unlock();
        }
        this.mManagerService = managerService;
        this.mLeScanClients = new HashMap();
        this.mToken = new Binder();
    }

    public BluetoothDevice getRemoteDevice(String address) {
        return new BluetoothDevice(address);
    }

    public BluetoothDevice getRemoteDevice(byte[] address) {
        if (address == null || address.length != 6) {
            throw new IllegalArgumentException("Bluetooth address must have 6 bytes");
        }
        Object[] objArr = new Object[6];
        objArr[0] = Byte.valueOf(address[0]);
        objArr[1] = Byte.valueOf(address[1]);
        objArr[2] = Byte.valueOf(address[2]);
        objArr[3] = Byte.valueOf(address[3]);
        objArr[4] = Byte.valueOf(address[4]);
        objArr[5] = Byte.valueOf(address[5]);
        return new BluetoothDevice(String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X", objArr));
    }

    public BluetoothLeAdvertiser getBluetoothLeAdvertiser() {
        if (!getLeAccess()) {
            return null;
        }
        if (isMultipleAdvertisementSupported() || isPeripheralModeSupported()) {
            synchronized (this.mLock) {
                if (sBluetoothLeAdvertiser == null) {
                    sBluetoothLeAdvertiser = new BluetoothLeAdvertiser(this.mManagerService);
                }
            }
            return sBluetoothLeAdvertiser;
        }
        Log.e(TAG, "Bluetooth LE advertising not supported");
        return null;
    }

    public BluetoothLeScanner getBluetoothLeScanner() {
        if (!getLeAccess()) {
            return null;
        }
        synchronized (this.mLock) {
            if (sBluetoothLeScanner == null) {
                sBluetoothLeScanner = new BluetoothLeScanner(this.mManagerService);
            }
        }
        return sBluetoothLeScanner;
    }

    public boolean isEnabled() {
        Log.d(TAG, "isEnabled");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isEnabled = this.mService.isEnabled();
                return isEnabled;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isLeEnabled() {
        int state = getLeState();
        if (state == 12) {
            Log.d(TAG, "STATE_ON");
        } else if (state == 15) {
            Log.d(TAG, "STATE_BLE_ON");
        } else {
            Log.d(TAG, "STATE_OFF");
            return false;
        }
        return true;
    }

    private void notifyUserAction(boolean enable) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null) {
                Log.e(TAG, "mService is null");
                return;
            }
            if (enable) {
                this.mService.onLeServiceUp();
            } else {
                this.mService.onBrEdrDown();
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean disableBLE() {
        if (!isBleScanAlwaysAvailable()) {
            return false;
        }
        int state = getLeState();
        if (state == 12) {
            Log.d(TAG, "STATE_ON: shouldn't disable");
            try {
                this.mManagerService.updateBleAppCount(this.mToken, false);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
            return true;
        } else if (state == 15) {
            Log.d(TAG, "STATE_BLE_ON");
            int bleAppCnt = 0;
            try {
                bleAppCnt = this.mManagerService.updateBleAppCount(this.mToken, false);
            } catch (RemoteException e2) {
                Log.e(TAG, "", e2);
            }
            if (bleAppCnt == 0) {
                notifyUserAction(false);
            }
            return true;
        } else {
            Log.d(TAG, "STATE_OFF: Already disabled");
            return false;
        }
    }

    public boolean enableBLE() {
        if (!isBleScanAlwaysAvailable()) {
            return false;
        }
        try {
            this.mManagerService.updateBleAppCount(this.mToken, true);
            if (isLeEnabled()) {
                Log.d(TAG, "enableBLE(): Bluetooth already enabled");
                return true;
            }
            Log.d(TAG, "enableBLE(): Calling enable");
            return this.mManagerService.enable();
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public int getState() {
        int state = 10;
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                state = this.mService.getState();
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            this.mServiceLock.readLock().unlock();
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
        if (state == 15 || state == 14 || state == 16) {
            if (VDBG) {
                Log.d(TAG, "Consider internal state as OFF");
            }
            state = 10;
        }
        if (VDBG) {
            Log.d(TAG, "" + hashCode() + ": getState(). Returning " + state);
        }
        return state;
    }

    public int getLeState() {
        int state = 10;
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                state = this.mService.getState();
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            this.mServiceLock.readLock().unlock();
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
        if (VDBG) {
            Log.d(TAG, "getLeState() returning " + state);
        }
        return state;
    }

    boolean getLeAccess() {
        if (getLeState() == 12 || getLeState() == 15) {
            return true;
        }
        return false;
    }

    public boolean enable() {
        if (isEnabled()) {
            Log.d(TAG, "enable(): BT is already enabled..!");
            return true;
        }
        Log.d(TAG, "enable(): BT is called by UID: " + Process.myPid());
        try {
            Log.d(TAG, "enable");
            return this.mManagerService.enable();
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean disable() {
        Log.d(TAG, "disable(): BT is called by UID: " + Process.myPid());
        try {
            Log.d(TAG, "disable");
            return this.mManagerService.disable(true);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean disable(boolean persist) {
        try {
            Log.d(TAG, "disable: persist = " + persist);
            return this.mManagerService.disable(persist);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public String getAddress() {
        Log.d(TAG, "getAddress");
        try {
            return this.mManagerService.getAddress();
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public String getName() {
        Log.d(TAG, "getName");
        try {
            return this.mManagerService.getName();
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public boolean configHciSnoopLog(boolean enable) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean configHciSnoopLog = this.mService.configHciSnoopLog(enable);
                return configHciSnoopLog;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean factoryReset() {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean factoryReset = this.mService.factoryReset();
                return factoryReset;
            }
            SystemProperties.set("persist.bluetooth.factoryreset", "true");
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public ParcelUuid[] getUuids() {
        Log.d(TAG, "getUuids");
        if (getState() != 12) {
            return null;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                ParcelUuid[] uuids = this.mService.getUuids();
                return uuids;
            }
            this.mServiceLock.readLock().unlock();
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean setName(String name) {
        Log.d(TAG, "setName: name = " + name);
        if (getState() != 12) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean name2 = this.mService.setName(name);
                return name2;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getScanMode() {
        Log.d(TAG, "getScanMode");
        if (getState() != 12) {
            return 20;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                int scanMode = this.mService.getScanMode();
                return scanMode;
            }
            this.mServiceLock.readLock().unlock();
            return 20;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean setScanMode(int mode, int duration) {
        Log.d(TAG, "setScanMode: mode = " + mode + ", duration = " + duration);
        if (getState() != 12) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean scanMode = this.mService.setScanMode(mode, duration);
                return scanMode;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean setScanMode(int mode) {
        Log.d(TAG, "setScanMode: mode = " + mode + ", getDiscoverableTimeout() = " + getDiscoverableTimeout());
        if (getState() != 12) {
            return false;
        }
        return setScanMode(mode, getDiscoverableTimeout());
    }

    public int getDiscoverableTimeout() {
        Log.d(TAG, "getDiscoverableTimeout");
        if (getState() != 12) {
            return -1;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                int discoverableTimeout = this.mService.getDiscoverableTimeout();
                return discoverableTimeout;
            }
            this.mServiceLock.readLock().unlock();
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public void setDiscoverableTimeout(int timeout) {
        Log.d(TAG, "setDiscoverableTimeout: timeout = " + timeout);
        if (getState() == 12) {
            try {
                this.mServiceLock.readLock().lock();
                if (this.mService != null) {
                    this.mService.setDiscoverableTimeout(timeout);
                }
                this.mServiceLock.readLock().unlock();
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                this.mServiceLock.readLock().unlock();
            } catch (Throwable th) {
                this.mServiceLock.readLock().unlock();
                throw th;
            }
        }
    }

    public boolean startDiscovery() {
        Log.i(TAG, "startDiscovery(), called by pid: " + Process.myPid());
        if (getState() != 12) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean startDiscovery = this.mService.startDiscovery();
                return startDiscovery;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean cancelDiscovery() {
        Log.i(TAG, "cancelDiscovery(), called by pid: " + Process.myPid());
        if (getState() != 12) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean cancelDiscovery = this.mService.cancelDiscovery();
                return cancelDiscovery;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isDiscovering() {
        Log.d(TAG, "isDiscovering");
        if (getState() != 12) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isDiscovering = this.mService.isDiscovering();
                return isDiscovering;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isMultipleAdvertisementSupported() {
        if (getState() != 12) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isMultiAdvertisementSupported = this.mService.isMultiAdvertisementSupported();
                return isMultiAdvertisementSupported;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get isMultipleAdvertisementSupported, error: ", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isBleScanAlwaysAvailable() {
        try {
            return this.mManagerService.isBleScanAlwaysAvailable();
        } catch (RemoteException e) {
            Log.e(TAG, "remote expection when calling isBleScanAlwaysAvailable", e);
            return false;
        }
    }

    public boolean isPeripheralModeSupported() {
        if (getState() != 12) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isPeripheralModeSupported = this.mService.isPeripheralModeSupported();
                return isPeripheralModeSupported;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get peripheral mode capability: ", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isOffloadedFilteringSupported() {
        if (!getLeAccess()) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isOffloadedFilteringSupported = this.mService.isOffloadedFilteringSupported();
                return isOffloadedFilteringSupported;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get isOffloadedFilteringSupported, error: ", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isOffloadedScanBatchingSupported() {
        if (!getLeAccess()) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isOffloadedScanBatchingSupported = this.mService.isOffloadedScanBatchingSupported();
                return isOffloadedScanBatchingSupported;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get isOffloadedScanBatchingSupported, error: ", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isHardwareTrackingFiltersAvailable() {
        boolean z = false;
        if (!getLeAccess()) {
            return false;
        }
        try {
            IBluetoothGatt iGatt = this.mManagerService.getBluetoothGatt();
            if (iGatt == null) {
                return false;
            }
            if (iGatt.numHwTrackFiltersAvailable() != 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @Deprecated
    public BluetoothActivityEnergyInfo getControllerActivityEnergyInfo(int updateType) {
        SynchronousResultReceiver receiver = new SynchronousResultReceiver();
        requestControllerActivityEnergyInfo(receiver);
        try {
            Result result = receiver.awaitResult(1000);
            if (result.bundle != null) {
                return (BluetoothActivityEnergyInfo) result.bundle.getParcelable(BatteryStats.RESULT_RECEIVER_CONTROLLER_KEY);
            }
        } catch (TimeoutException e) {
            Log.e(TAG, "getControllerActivityEnergyInfo timed out");
        }
        return null;
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void requestControllerActivityEnergyInfo(ResultReceiver result) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                this.mService.requestActivityInfo(result);
                result = null;
            }
            this.mServiceLock.readLock().unlock();
            if (result != null) {
                result.send(0, null);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getControllerActivityEnergyInfoCallback: " + e);
            this.mServiceLock.readLock().unlock();
            if (result != null) {
                result.send(0, null);
            }
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            if (result != null) {
                result.send(0, null);
            }
            throw th;
        }
    }

    public Set<BluetoothDevice> getBondedDevices() {
        if (getState() != 12) {
            return toDeviceSet(new BluetoothDevice[0]);
        }
        try {
            this.mServiceLock.readLock().lock();
            Set<BluetoothDevice> toDeviceSet;
            if (this.mService != null) {
                toDeviceSet = toDeviceSet(this.mService.getBondedDevices());
                return toDeviceSet;
            }
            toDeviceSet = toDeviceSet(new BluetoothDevice[0]);
            this.mServiceLock.readLock().unlock();
            return toDeviceSet;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            this.mServiceLock.readLock().unlock();
            return null;
        }
    }

    public int getConnectionState() {
        if (getState() != 12) {
            return 0;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                int adapterConnectionState = this.mService.getAdapterConnectionState();
                return adapterConnectionState;
            }
            this.mServiceLock.readLock().unlock();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "getConnectionState:", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getProfileConnectionState(int profile) {
        if (getState() != 12) {
            return 0;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                int profileConnectionState = this.mService.getProfileConnectionState(profile);
                return profileConnectionState;
            }
            this.mServiceLock.readLock().unlock();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "getProfileConnectionState:", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public BluetoothServerSocket listenUsingRfcommOn(int channel) throws IOException {
        return listenUsingRfcommOn(channel, false, false);
    }

    public BluetoothServerSocket listenUsingRfcommOn(int channel, boolean mitm, boolean min16DigitPin) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(1, true, true, channel, mitm, min16DigitPin);
        int errno = socket.mSocket.bindListen();
        if (channel == -2) {
            socket.setChannel(socket.mSocket.getPort());
        }
        if (errno == 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public BluetoothServerSocket listenUsingRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
        return createNewRfcommSocketAndRecord(name, uuid, true, true);
    }

    public BluetoothServerSocket listenUsingInsecureRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
        return createNewRfcommSocketAndRecord(name, uuid, false, false);
    }

    public BluetoothServerSocket listenUsingEncryptedRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
        return createNewRfcommSocketAndRecord(name, uuid, false, true);
    }

    private BluetoothServerSocket createNewRfcommSocketAndRecord(String name, UUID uuid, boolean auth, boolean encrypt) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(1, auth, encrypt, new ParcelUuid(uuid));
        socket.setServiceName(name);
        int errno = socket.mSocket.bindListen();
        if (errno == 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public BluetoothServerSocket listenUsingInsecureRfcommOn(int port) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(1, false, false, port);
        int errno = socket.mSocket.bindListen();
        if (port == -2) {
            socket.setChannel(socket.mSocket.getPort());
        }
        if (errno == 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public BluetoothServerSocket listenUsingEncryptedRfcommOn(int port) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(1, false, true, port);
        int errno = socket.mSocket.bindListen();
        if (port == -2) {
            socket.setChannel(socket.mSocket.getPort());
        }
        if (errno >= 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public static BluetoothServerSocket listenUsingScoOn() throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(2, false, false, -1);
        if (socket.mSocket.bindListen() < 0) {
        }
        return socket;
    }

    public BluetoothServerSocket listenUsingL2capOn(int port, boolean mitm, boolean min16DigitPin) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(3, true, true, port, mitm, min16DigitPin);
        int errno = socket.mSocket.bindListen();
        if (port == -2) {
            socket.setChannel(socket.mSocket.getPort());
        }
        if (errno == 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public BluetoothServerSocket listenUsingL2capOn(int port) throws IOException {
        return listenUsingL2capOn(port, false, false);
    }

    public Pair<byte[], byte[]> readOutOfBandData() {
        return getState() != 12 ? null : null;
    }

    public boolean getProfileProxy(Context context, ServiceListener listener, int profile) {
        if (context == null || listener == null) {
            return false;
        }
        if (profile == 1) {
            BluetoothHeadset headset = new BluetoothHeadset(context, listener);
            return true;
        } else if (profile == 2) {
            BluetoothA2dp a2dp = new BluetoothA2dp(context, listener);
            return true;
        } else if (profile == 11) {
            BluetoothA2dpSink a2dpSink = new BluetoothA2dpSink(context, listener);
            return true;
        } else if (profile == 12) {
            BluetoothAvrcpController avrcp = new BluetoothAvrcpController(context, listener);
            return true;
        } else if (profile == 4) {
            BluetoothInputDevice iDev = new BluetoothInputDevice(context, listener);
            return true;
        } else if (profile == 5) {
            BluetoothPan pan = new BluetoothPan(context, listener);
            return true;
        } else if (profile == 3) {
            BluetoothHealth health = new BluetoothHealth(context, listener);
            return true;
        } else if (profile == 9) {
            BluetoothMap map = new BluetoothMap(context, listener);
            return true;
        } else if (profile == 16) {
            BluetoothHeadsetClient headsetClient = new BluetoothHeadsetClient(context, listener);
            return true;
        } else if (profile == 10) {
            BluetoothSap sap = new BluetoothSap(context, listener);
            return true;
        } else if (profile != 17) {
            return false;
        } else {
            BluetoothPbapClient pbapClient = new BluetoothPbapClient(context, listener);
            return true;
        }
    }

    public void closeProfileProxy(int profile, BluetoothProfile proxy) {
        if (proxy != null) {
            switch (profile) {
                case 1:
                    ((BluetoothHeadset) proxy).close();
                    break;
                case 2:
                    ((BluetoothA2dp) proxy).close();
                    break;
                case 3:
                    ((BluetoothHealth) proxy).close();
                    break;
                case 4:
                    ((BluetoothInputDevice) proxy).close();
                    break;
                case 5:
                    ((BluetoothPan) proxy).close();
                    break;
                case 7:
                    ((BluetoothGatt) proxy).close();
                    break;
                case 8:
                    ((BluetoothGattServer) proxy).close();
                    break;
                case 9:
                    ((BluetoothMap) proxy).close();
                    break;
                case 10:
                    ((BluetoothSap) proxy).close();
                    break;
                case 11:
                    ((BluetoothA2dpSink) proxy).close();
                    break;
                case 12:
                    ((BluetoothAvrcpController) proxy).close();
                    break;
                case 16:
                    ((BluetoothHeadsetClient) proxy).close();
                    break;
                case 17:
                    ((BluetoothPbapClient) proxy).close();
                    break;
            }
        }
    }

    public boolean enableNoAutoConnect() {
        if (isEnabled()) {
            Log.d(TAG, "enableNoAutoConnect(): BT is already enabled..!");
            return true;
        }
        try {
            Log.d(TAG, "enableNoAutoConnect");
            return this.mManagerService.enableNoAutoConnect();
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean changeApplicationBluetoothState(boolean on, BluetoothStateChangeCallback callback) {
        return callback == null ? false : false;
    }

    private Set<BluetoothDevice> toDeviceSet(BluetoothDevice[] devices) {
        return Collections.unmodifiableSet(new HashSet(Arrays.asList(devices)));
    }

    protected void finalize() throws Throwable {
        try {
            this.mManagerService.unregisterAdapter(this.mManagerCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } finally {
            super.finalize();
        }
    }

    public static boolean checkBluetoothAddress(String address) {
        if (address == null || address.length() != 17) {
            return false;
        }
        for (int i = 0; i < 17; i++) {
            char c = address.charAt(i);
            switch (i % 3) {
                case 0:
                case 1:
                    if ((c < '0' || c > '9') && (c < 'A' || c > 'F')) {
                        return false;
                    }
                case 2:
                    if (c == ':') {
                        break;
                    }
                    return false;
                default:
                    break;
            }
        }
        return true;
    }

    IBluetoothManager getBluetoothManager() {
        return this.mManagerService;
    }

    IBluetooth getBluetoothService(IBluetoothManagerCallback cb) {
        synchronized (this.mProxyServiceStateCallbacks) {
            if (cb == null) {
                Log.w(TAG, "getBluetoothService() called with no BluetoothManagerCallback");
            } else if (!this.mProxyServiceStateCallbacks.contains(cb)) {
                this.mProxyServiceStateCallbacks.add(cb);
            }
        }
        return this.mService;
    }

    void removeServiceStateCallback(IBluetoothManagerCallback cb) {
        synchronized (this.mProxyServiceStateCallbacks) {
            this.mProxyServiceStateCallbacks.remove(cb);
        }
    }

    @Deprecated
    public boolean startLeScan(LeScanCallback callback) {
        return startLeScan(null, callback);
    }

    @Deprecated
    public boolean startLeScan(final UUID[] serviceUuids, final LeScanCallback callback) {
        Log.d(TAG, "startLeScan(): " + Arrays.toString(serviceUuids));
        if (callback == null) {
            Log.e(TAG, "startLeScan: null callback");
            return false;
        }
        BluetoothLeScanner scanner = getBluetoothLeScanner();
        if (scanner == null) {
            Log.e(TAG, "startLeScan: cannot get BluetoothLeScanner");
            return false;
        }
        synchronized (this.mLeScanClients) {
            if (this.mLeScanClients.containsKey(callback)) {
                Log.e(TAG, "LE Scan has already started");
                return false;
            }
            try {
                if (this.mManagerService.getBluetoothGatt() == null) {
                    return false;
                }
                ScanCallback scanCallback = new ScanCallback() {
                    public void onScanResult(int callbackType, ScanResult result) {
                        if (callbackType != 1) {
                            Log.e(BluetoothAdapter.TAG, "LE Scan has already started");
                            return;
                        }
                        ScanRecord scanRecord = result.getScanRecord();
                        if (scanRecord != null) {
                            if (serviceUuids != null) {
                                List<ParcelUuid> uuids = new ArrayList();
                                for (UUID uuid : serviceUuids) {
                                    uuids.add(new ParcelUuid(uuid));
                                }
                                List<ParcelUuid> scanServiceUuids = scanRecord.getServiceUuids();
                                if (scanServiceUuids == null || !scanServiceUuids.containsAll(uuids)) {
                                    Log.d(BluetoothAdapter.TAG, "uuids does not match");
                                    return;
                                }
                            }
                            callback.onLeScan(result.getDevice(), result.getRssi(), scanRecord.getBytes());
                        }
                    }
                };
                ScanSettings settings = new Builder().setCallbackType(1).setScanMode(2).build();
                List<ScanFilter> filters = new ArrayList();
                if (serviceUuids != null && serviceUuids.length > 0) {
                    filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(serviceUuids[0])).build());
                }
                scanner.startScan(filters, settings, scanCallback);
                this.mLeScanClients.put(callback, scanCallback);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                return false;
            }
        }
    }

    @Deprecated
    public void stopLeScan(LeScanCallback callback) {
        Log.d(TAG, "stopLeScan()");
        BluetoothLeScanner scanner = getBluetoothLeScanner();
        if (scanner != null) {
            synchronized (this.mLeScanClients) {
                ScanCallback scanCallback = (ScanCallback) this.mLeScanClients.remove(callback);
                if (scanCallback == null) {
                    Log.d(TAG, "scan not started yet");
                    return;
                }
                scanner.stopScan(scanCallback);
            }
        }
    }
}

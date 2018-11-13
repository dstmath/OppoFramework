package android.bluetooth;

import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;

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
public final class BluetoothDevice implements Parcelable {
    public static final int ACCESS_ALLOWED = 1;
    public static final int ACCESS_REJECTED = 2;
    public static final int ACCESS_UNKNOWN = 0;
    public static final String ACTION_ACL_CONNECTED = "android.bluetooth.device.action.ACL_CONNECTED";
    public static final String ACTION_ACL_DISCONNECTED = "android.bluetooth.device.action.ACL_DISCONNECTED";
    public static final String ACTION_ACL_DISCONNECT_REQUESTED = "android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED";
    public static final String ACTION_ALIAS_CHANGED = "android.bluetooth.device.action.ALIAS_CHANGED";
    public static final String ACTION_BOND_STATE_CHANGED = "android.bluetooth.device.action.BOND_STATE_CHANGED";
    public static final String ACTION_CLASS_CHANGED = "android.bluetooth.device.action.CLASS_CHANGED";
    public static final String ACTION_CONNECTION_ACCESS_CANCEL = "android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL";
    public static final String ACTION_CONNECTION_ACCESS_REPLY = "android.bluetooth.device.action.CONNECTION_ACCESS_REPLY";
    public static final String ACTION_CONNECTION_ACCESS_REQUEST = "android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST";
    public static final String ACTION_DISAPPEARED = "android.bluetooth.device.action.DISAPPEARED";
    public static final String ACTION_FOUND = "android.bluetooth.device.action.FOUND";
    public static final String ACTION_MAS_INSTANCE = "android.bluetooth.device.action.MAS_INSTANCE";
    public static final String ACTION_NAME_CHANGED = "android.bluetooth.device.action.NAME_CHANGED";
    public static final String ACTION_NAME_FAILED = "android.bluetooth.device.action.NAME_FAILED";
    public static final String ACTION_PAIRING_CANCEL = "android.bluetooth.device.action.PAIRING_CANCEL";
    public static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
    public static final String ACTION_SDP_RECORD = "android.bluetooth.device.action.SDP_RECORD";
    public static final String ACTION_UUID = "android.bluetooth.device.action.UUID";
    private static final int ADDRESS_LENGTH = 17;
    public static final int BOND_BONDED = 12;
    public static final int BOND_BONDING = 11;
    public static final int BOND_NONE = 10;
    public static final int BOND_SUCCESS = 0;
    public static final int CONNECTION_ACCESS_NO = 2;
    public static final int CONNECTION_ACCESS_YES = 1;
    private static final int CONNECTION_STATE_CONNECTED = 1;
    private static final int CONNECTION_STATE_DISCONNECTED = 0;
    private static final int CONNECTION_STATE_ENCRYPTED_BREDR = 2;
    private static final int CONNECTION_STATE_ENCRYPTED_LE = 4;
    public static final Creator<BluetoothDevice> CREATOR = null;
    private static final boolean DBG = false;
    public static final int DEVICE_TYPE_CLASSIC = 1;
    public static final int DEVICE_TYPE_DUAL = 3;
    public static final int DEVICE_TYPE_LE = 2;
    public static final int DEVICE_TYPE_UNKNOWN = 0;
    public static final int ERROR = Integer.MIN_VALUE;
    public static final String EXTRA_ACCESS_REQUEST_TYPE = "android.bluetooth.device.extra.ACCESS_REQUEST_TYPE";
    public static final String EXTRA_ALWAYS_ALLOWED = "android.bluetooth.device.extra.ALWAYS_ALLOWED";
    public static final String EXTRA_BOND_STATE = "android.bluetooth.device.extra.BOND_STATE";
    public static final String EXTRA_CLASS = "android.bluetooth.device.extra.CLASS";
    public static final String EXTRA_CLASS_NAME = "android.bluetooth.device.extra.CLASS_NAME";
    public static final String EXTRA_CONNECTION_ACCESS_RESULT = "android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT";
    public static final String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
    public static final String EXTRA_MAS_INSTANCE = "android.bluetooth.device.extra.MAS_INSTANCE";
    public static final String EXTRA_NAME = "android.bluetooth.device.extra.NAME";
    public static final String EXTRA_PACKAGE_NAME = "android.bluetooth.device.extra.PACKAGE_NAME";
    public static final String EXTRA_PAIRING_KEY = "android.bluetooth.device.extra.PAIRING_KEY";
    public static final String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
    public static final String EXTRA_PREVIOUS_BOND_STATE = "android.bluetooth.device.extra.PREVIOUS_BOND_STATE";
    public static final String EXTRA_REASON = "android.bluetooth.device.extra.REASON";
    public static final String EXTRA_RSSI = "android.bluetooth.device.extra.RSSI";
    public static final String EXTRA_SDP_RECORD = "android.bluetooth.device.extra.SDP_RECORD";
    public static final String EXTRA_SDP_SEARCH_STATUS = "android.bluetooth.device.extra.SDP_SEARCH_STATUS";
    public static final String EXTRA_UUID = "android.bluetooth.device.extra.UUID";
    public static final int PAIRING_VARIANT_CONSENT = 3;
    public static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    public static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
    public static final int PAIRING_VARIANT_OOB_CONSENT = 6;
    public static final int PAIRING_VARIANT_PASSKEY = 1;
    public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    public static final int PAIRING_VARIANT_PIN = 0;
    public static final int PAIRING_VARIANT_PIN_16_DIGITS = 7;
    public static final int REQUEST_TYPE_MESSAGE_ACCESS = 3;
    public static final int REQUEST_TYPE_PHONEBOOK_ACCESS = 2;
    public static final int REQUEST_TYPE_PROFILE_CONNECTION = 1;
    public static final int REQUEST_TYPE_SIM_ACCESS = 4;
    private static final String TAG = "BluetoothDevice";
    public static final int TRANSPORT_AUTO = 0;
    public static final int TRANSPORT_BREDR = 1;
    public static final int TRANSPORT_LE = 2;
    public static final int UNBOND_REASON_AUTH_CANCELED = 3;
    public static final int UNBOND_REASON_AUTH_FAILED = 1;
    public static final int UNBOND_REASON_AUTH_REJECTED = 2;
    public static final int UNBOND_REASON_AUTH_TIMEOUT = 6;
    public static final int UNBOND_REASON_DISCOVERY_IN_PROGRESS = 5;
    public static final int UNBOND_REASON_REMOTE_AUTH_CANCELED = 8;
    public static final int UNBOND_REASON_REMOTE_DEVICE_DOWN = 4;
    public static final int UNBOND_REASON_REMOVED = 9;
    public static final int UNBOND_REASON_REPEATED_ATTEMPTS = 7;
    private static final boolean USER_MODE = false;
    static IBluetoothManagerCallback mStateChangeCallback;
    private static IBluetooth sService;
    private static final ReadWriteLock sServiceLock = null;
    private final String mAddress;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.bluetooth.BluetoothDevice.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.bluetooth.BluetoothDevice.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.BluetoothDevice.<clinit>():void");
    }

    static IBluetooth getService() {
        sServiceLock.readLock().lock();
        if (sService == null) {
            sService = BluetoothAdapter.getDefaultAdapter().getBluetoothService(mStateChangeCallback);
        }
        sServiceLock.readLock().unlock();
        return sService;
    }

    BluetoothDevice(String address) {
        getService();
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            this.mAddress = address;
            return;
        }
        throw new IllegalArgumentException(address + " is not a valid Bluetooth address");
    }

    public boolean equals(Object o) {
        if (o instanceof BluetoothDevice) {
            return this.mAddress.equals(((BluetoothDevice) o).getAddress());
        }
        return false;
    }

    public int hashCode() {
        return this.mAddress.hashCode();
    }

    public String toString() {
        if (!USER_MODE || this.mAddress == null || this.mAddress.length() < 17) {
            return this.mAddress;
        }
        return this.mAddress.substring(0, 9) + "XX:XX:XX";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mAddress);
    }

    public String getAddress() {
        return this.mAddress;
    }

    public String getName() {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device name");
            return null;
        }
        try {
            String name = sService.getRemoteName(this);
            if (name != null) {
                return name.replaceAll("[\\t\\n\\r]+", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getName() of device (" + toString() + ")", npe);
            return null;
        }
    }

    public int getType() {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device type");
            return 0;
        }
        try {
            int type = sService.getRemoteType(this);
            if (DBG) {
                Log.d(TAG, "getType: type = " + type);
            }
            return type;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getType() of device (" + toString() + ")", npe);
            return 0;
        }
    }

    public String getAlias() {
        String alias = null;
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device Alias");
            return null;
        }
        try {
            alias = sService.getRemoteAlias(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getAlias() of device (" + toString() + ")", npe);
        }
        if (DBG) {
            Log.d(TAG, "getAlias: alias = " + alias);
        }
        return alias;
    }

    public boolean setAlias(String alias) {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot set Remote Device name");
            return false;
        }
        try {
            if (DBG) {
                Log.d(TAG, "setAlias: alias = " + alias);
            }
            return sService.setRemoteAlias(this, alias);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for setAlias()", npe);
            return false;
        }
    }

    public String getAliasName() {
        String name = getAlias();
        if (name == null) {
            name = getName();
        }
        if (DBG) {
            Log.d(TAG, "getAliasName: name = " + name);
        }
        return name;
    }

    public boolean createBond() {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot create bond to Remote Device");
            return false;
        }
        try {
            Log.i(TAG, "createBond() for device " + toString() + " called by pid: " + Process.myPid() + " tid: " + Process.myTid());
            if (DBG) {
                Log.d(TAG, "createBond: " + this);
            }
            return sService.createBond(this, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for createBond()", npe);
            return false;
        }
    }

    public boolean createBond(int transport) {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot create bond to Remote Device");
            return false;
        } else if (transport < 0 || transport > 2) {
            throw new IllegalArgumentException(transport + " is not a valid Bluetooth transport");
        } else {
            try {
                Log.i(TAG, "createBond() for device " + toString() + " called by pid: " + Process.myPid() + " tid: " + Process.myTid());
                if (DBG) {
                    Log.d(TAG, "createBond: " + this + ", transport = " + transport);
                }
                return sService.createBond(this, transport);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                return false;
            } catch (NullPointerException npe) {
                Log.e(TAG, "NullPointerException for createBond()", npe);
                return false;
            }
        }
    }

    public boolean createBondOutOfBand(int transport, OobData oobData) {
        try {
            return sService.createBondOutOfBand(this, transport, oobData);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setDeviceOutOfBandData(byte[] hash, byte[] randomizer) {
        return false;
    }

    public boolean cancelBondProcess() {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot cancel Remote Device bond");
            return false;
        }
        try {
            Log.i(TAG, "cancelBondProcess() for device " + toString() + " called by pid: " + Process.myPid() + " tid: " + Process.myTid());
            if (DBG) {
                Log.d(TAG, "cancelBondProcess: " + this);
            }
            return sService.cancelBondProcess(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for cancelBondProcess()", npe);
            return false;
        }
    }

    public boolean removeBond() {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot remove Remote Device bond");
            return false;
        }
        try {
            Log.i(TAG, "removeBond() for device " + toString() + " called by pid: " + Process.myPid() + " tid: " + Process.myTid());
            if (DBG) {
                Log.d(TAG, "removeBond: " + this);
            }
            return sService.removeBond(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for removeBond()", npe);
            return false;
        }
    }

    public int getBondState() {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot get bond state");
            return 10;
        }
        try {
            int state = sService.getBondState(this);
            if (DBG) {
                Log.d(TAG, "getBondState: state = " + state);
            }
            return state;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 10;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getBondState() of device (" + toString() + ")", npe);
            return 10;
        }
    }

    public boolean isConnected() {
        if (sService == null) {
            return false;
        }
        try {
            boolean isConnected = sService.getConnectionState(this) != 0;
            if (DBG) {
                Log.d(TAG, "isConnected: isConnected = " + isConnected);
            }
            return isConnected;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for isConnected()", npe);
            return false;
        }
    }

    public boolean isEncrypted() {
        boolean z = true;
        if (sService == null) {
            return false;
        }
        try {
            if (sService.getConnectionState(this) <= 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for isEncrypted()", npe);
            return false;
        }
    }

    public BluetoothClass getBluetoothClass() {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot get Bluetooth Class");
            return null;
        }
        try {
            int classInt = sService.getRemoteClass(this);
            if (classInt == -16777216) {
                return null;
            }
            if (DBG) {
                Log.d(TAG, "getBluetoothClass: classInt = " + classInt);
            }
            return new BluetoothClass(classInt);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getBluetoothClass() of device (" + toString() + ")", npe);
            return null;
        }
    }

    public ParcelUuid[] getUuids() {
        if (sService == null || !isBluetoothEnabled()) {
            Log.e(TAG, "BT not enabled. Cannot get remote device Uuids");
            return null;
        }
        try {
            ParcelUuid[] uuids = sService.getRemoteUuids(this);
            if (uuids != null) {
                for (int i = 0; i < uuids.length; i++) {
                    if (DBG) {
                        Log.d(TAG, "uuids[" + i + "] = " + uuids[i]);
                    }
                }
            }
            return uuids;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getUuids() of device (" + toString() + ")", npe);
            return null;
        }
    }

    public boolean fetchUuidsWithSdp() {
        IBluetooth service = sService;
        if (service == null || !isBluetoothEnabled()) {
            Log.e(TAG, "BT not enabled. Cannot fetchUuidsWithSdp");
            return false;
        }
        try {
            if (DBG) {
                Log.d(TAG, "fetchUuidsWithSdp");
            }
            return service.fetchRemoteUuids(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for fetchUuidsWithSdp() of device (" + toString() + ")", npe);
            return false;
        }
    }

    public boolean sdpSearch(ParcelUuid uuid) {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot query remote device sdp records");
            return false;
        }
        try {
            if (DBG) {
                Log.d(TAG, "sdpSearch");
            }
            return sService.sdpSearch(this, uuid);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for sdpSearch", npe);
            return false;
        }
    }

    public boolean setPin(byte[] pin) {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot set Remote Device pin");
            return false;
        }
        try {
            return sService.setPin(this, true, pin.length, pin);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for setPin of device", npe);
        }
        return false;
    }

    public boolean setPasskey(int passkey) {
        try {
            return sService.setPasskeyEx(this, true, 4, passkey);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setPairingConfirmation(boolean confirm) {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot set pairing confirmation");
            return false;
        }
        try {
            if (DBG) {
                Log.d(TAG, "setPairingConfirmation: device = " + this + "confirm = " + confirm);
            }
            return sService.setPairingConfirmation(this, confirm);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for setPairingConfirmation of device", npe);
            return false;
        }
    }

    public boolean setRemoteOutOfBandData() {
        return false;
    }

    public boolean cancelPairingUserInput() {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot create pairing user input");
            return false;
        }
        try {
            if (DBG) {
                Log.d(TAG, "cancelPairingUserInput: " + this);
            }
            return sService.cancelBondProcess(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for cancelPairingUserInput of device", npe);
            return false;
        }
    }

    public boolean isBluetoothDock() {
        return false;
    }

    boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            return false;
        }
        return true;
    }

    public int getPhonebookAccessPermission() {
        if (sService == null) {
            if (DBG) {
                Log.d(TAG, "sService == null, return ACCESS_UNKNOWN");
            }
            return 0;
        }
        try {
            int permission = sService.getPhonebookAccessPermission(this);
            if (DBG) {
                Log.d(TAG, "getPhonebookAccessPermission: permission = " + permission);
            }
            return permission;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getPhonebookAccessPermission() of device (" + toString() + ")", npe);
            return 0;
        }
    }

    public boolean setPhonebookAccessPermission(int value) {
        if (sService == null) {
            if (DBG) {
                Log.d(TAG, "sService == null, return false");
            }
            return false;
        }
        try {
            if (DBG) {
                Log.d(TAG, "setPhonebookAccessPermission: value = " + value);
            }
            return sService.setPhonebookAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for setPhonebookAccessPermission() of device", npe);
            return false;
        }
    }

    public int getMessageAccessPermission() {
        if (sService == null) {
            if (DBG) {
                Log.d(TAG, "sService == null, return ACCESS_UNKNOWN");
            }
            return 0;
        }
        try {
            int permission = sService.getMessageAccessPermission(this);
            if (DBG) {
                Log.d(TAG, "getMessageAccessPermission: permission = " + permission);
            }
            return permission;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getMessageAccessPermission() of device (" + toString() + ")", npe);
            return 0;
        }
    }

    public boolean setMessageAccessPermission(int value) {
        if (sService == null) {
            if (DBG) {
                Log.d(TAG, "sService == null, return false");
            }
            return false;
        }
        try {
            if (DBG) {
                Log.d(TAG, "setMessageAccessPermission: value = " + value);
            }
            return sService.setMessageAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for setMessageAccessPermission() of device", npe);
            return false;
        }
    }

    public int getSimAccessPermission() {
        if (sService == null) {
            return 0;
        }
        try {
            return sService.getSimAccessPermission(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for getSimAccessPermission() of device (" + toString() + ")", npe);
        }
        return 0;
    }

    public boolean setSimAccessPermission(int value) {
        if (sService == null) {
            return false;
        }
        try {
            return sService.setSimAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException for setSimAccessPermission() of device", npe);
        }
        return false;
    }

    public BluetoothSocket createRfcommSocket(int channel) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, true, true, this, channel, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createL2capSocket(int channel) throws IOException {
        return new BluetoothSocket(3, -1, true, true, this, channel, null);
    }

    public BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, true, true, this, -1, new ParcelUuid(uuid));
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createInsecureRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, false, false, this, -1, new ParcelUuid(uuid));
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createInsecureRfcommSocket(int port) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, false, false, this, port, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createScoSocket() throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(2, -1, true, true, this, -1, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public static byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        try {
            byte[] pinBytes = pin.getBytes("UTF-8");
            if (pinBytes.length <= 0 || pinBytes.length > 16) {
                return null;
            }
            return pinBytes;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 not supported?!?");
            return null;
        }
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback) {
        return connectGatt(context, autoConnect, callback, 0);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport) {
        try {
            IBluetoothGatt iGatt = BluetoothAdapter.getDefaultAdapter().getBluetoothManager().getBluetoothGatt();
            if (iGatt == null) {
                return null;
            }
            BluetoothGatt gatt = new BluetoothGatt(iGatt, this, transport);
            gatt.connect(Boolean.valueOf(autoConnect), callback);
            return gatt;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }
}

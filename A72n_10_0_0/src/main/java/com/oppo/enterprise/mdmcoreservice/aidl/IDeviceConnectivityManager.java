package com.oppo.enterprise.mdmcoreservice.aidl;

import android.content.ComponentName;
import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IDeviceConnectivityManager extends IInterface {
    boolean addBSSIDToBlackList(List<String> list) throws RemoteException;

    boolean addBSSIDToWhiteList(List<String> list) throws RemoteException;

    boolean addBluetoothDevicesToBlackList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean addBluetoothDevicesToWhiteList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean addSSIDToBlackList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean addSSIDToWhiteList(ComponentName componentName, List<String> list) throws RemoteException;

    List<String> getBSSIDBlackList() throws RemoteException;

    List<String> getBSSIDWhiteList() throws RemoteException;

    List<String> getBluetoothDevicesFromBlackLists(ComponentName componentName) throws RemoteException;

    List<String> getBluetoothDevicesFromWhiteLists(ComponentName componentName) throws RemoteException;

    int getBluetoothPolicies(ComponentName componentName) throws RemoteException;

    String getDevicePosition(ComponentName componentName) throws RemoteException;

    int getNfcPolicies(ComponentName componentName) throws RemoteException;

    List<String> getSSIDBlackList(ComponentName componentName) throws RemoteException;

    List<String> getSSIDWhiteList(ComponentName componentName) throws RemoteException;

    int getSecurityLevel(ComponentName componentName) throws RemoteException;

    int getWifiApPolicies(ComponentName componentName) throws RemoteException;

    String getWifiMacAddress(ComponentName componentName) throws RemoteException;

    List<String> getWifiProfileList(ComponentName componentName) throws RemoteException;

    List<String> getWlanApClientBlackList(ComponentName componentName) throws RemoteException;

    List<String> getWlanConfiguration(ComponentName componentName) throws RemoteException;

    int getWlanPolicies(ComponentName componentName) throws RemoteException;

    boolean isBlackListedBSSID(String str) throws RemoteException;

    boolean isBlackListedDevice(ComponentName componentName, String str) throws RemoteException;

    boolean isBlackListedSSID(ComponentName componentName, String str) throws RemoteException;

    boolean isGPSDisabled(ComponentName componentName) throws RemoteException;

    boolean isGPSTurnOn(ComponentName componentName) throws RemoteException;

    boolean isUnSecureSoftApDisabled(ComponentName componentName) throws RemoteException;

    boolean isUserProfilesDisabled(ComponentName componentName) throws RemoteException;

    boolean isWhiteListedBSSID(String str) throws RemoteException;

    boolean isWhiteListedDevice(ComponentName componentName, String str) throws RemoteException;

    boolean isWhiteListedSSID(String str) throws RemoteException;

    boolean isWifiAutoConnectionDisabled() throws RemoteException;

    boolean isWifiEditDisabled(ComponentName componentName) throws RemoteException;

    boolean isWifiProfileSet(ComponentName componentName, WifiConfiguration wifiConfiguration) throws RemoteException;

    boolean removeBSSIDFromBlackList(List<String> list) throws RemoteException;

    boolean removeBSSIDFromWhiteList(List<String> list) throws RemoteException;

    boolean removeBluetoothDevicesFromBlackList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean removeBluetoothDevicesFromWhiteList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean removeSSIDFromBlackList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean removeSSIDFromWhiteList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean removeWifiProfile(ComponentName componentName, WifiConfiguration wifiConfiguration) throws RemoteException;

    void removeWlanApClientBlackList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean setBluetoothPolicies(ComponentName componentName, int i) throws RemoteException;

    void setGPSDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setNfcPolicies(ComponentName componentName, int i) throws RemoteException;

    boolean setSecurityLevel(ComponentName componentName, int i) throws RemoteException;

    boolean setUnSecureSoftApDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setUserProfilesDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setWifiApPolicies(ComponentName componentName, int i) throws RemoteException;

    boolean setWifiAutoConnectionDisabled(boolean z) throws RemoteException;

    boolean setWifiEditDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setWifiProfile(ComponentName componentName, WifiConfiguration wifiConfiguration) throws RemoteException;

    void setWlanApClientBlackList(ComponentName componentName, List<String> list) throws RemoteException;

    boolean setWlanConfiguration(ComponentName componentName, List<String> list) throws RemoteException;

    boolean setWlanPolicies(ComponentName componentName, int i) throws RemoteException;

    void turnOnGPS(ComponentName componentName, boolean z) throws RemoteException;

    public static abstract class Stub extends Binder implements IDeviceConnectivityManager {
        private static final String DESCRIPTOR = "com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager";
        static final int TRANSACTION_addBSSIDToBlackList = 18;
        static final int TRANSACTION_addBSSIDToWhiteList = 17;
        static final int TRANSACTION_addBluetoothDevicesToBlackList = 1;
        static final int TRANSACTION_addBluetoothDevicesToWhiteList = 2;
        static final int TRANSACTION_addSSIDToBlackList = 9;
        static final int TRANSACTION_addSSIDToWhiteList = 13;
        static final int TRANSACTION_getBSSIDBlackList = 22;
        static final int TRANSACTION_getBSSIDWhiteList = 21;
        static final int TRANSACTION_getBluetoothDevicesFromBlackLists = 5;
        static final int TRANSACTION_getBluetoothDevicesFromWhiteLists = 6;
        static final int TRANSACTION_getBluetoothPolicies = 36;
        static final int TRANSACTION_getDevicePosition = 39;
        static final int TRANSACTION_getNfcPolicies = 38;
        static final int TRANSACTION_getSSIDBlackList = 11;
        static final int TRANSACTION_getSSIDWhiteList = 15;
        static final int TRANSACTION_getSecurityLevel = 43;
        static final int TRANSACTION_getWifiApPolicies = 28;
        static final int TRANSACTION_getWifiMacAddress = 34;
        static final int TRANSACTION_getWifiProfileList = 50;
        static final int TRANSACTION_getWlanApClientBlackList = 30;
        static final int TRANSACTION_getWlanConfiguration = 32;
        static final int TRANSACTION_getWlanPolicies = 26;
        static final int TRANSACTION_isBlackListedBSSID = 24;
        static final int TRANSACTION_isBlackListedDevice = 7;
        static final int TRANSACTION_isBlackListedSSID = 12;
        static final int TRANSACTION_isGPSDisabled = 54;
        static final int TRANSACTION_isGPSTurnOn = 52;
        static final int TRANSACTION_isUnSecureSoftApDisabled = 45;
        static final int TRANSACTION_isUserProfilesDisabled = 40;
        static final int TRANSACTION_isWhiteListedBSSID = 23;
        static final int TRANSACTION_isWhiteListedDevice = 8;
        static final int TRANSACTION_isWhiteListedSSID = 16;
        static final int TRANSACTION_isWifiAutoConnectionDisabled = 47;
        static final int TRANSACTION_isWifiEditDisabled = 56;
        static final int TRANSACTION_isWifiProfileSet = 57;
        static final int TRANSACTION_removeBSSIDFromBlackList = 20;
        static final int TRANSACTION_removeBSSIDFromWhiteList = 19;
        static final int TRANSACTION_removeBluetoothDevicesFromBlackList = 3;
        static final int TRANSACTION_removeBluetoothDevicesFromWhiteList = 4;
        static final int TRANSACTION_removeSSIDFromBlackList = 10;
        static final int TRANSACTION_removeSSIDFromWhiteList = 14;
        static final int TRANSACTION_removeWifiProfile = 49;
        static final int TRANSACTION_removeWlanApClientBlackList = 31;
        static final int TRANSACTION_setBluetoothPolicies = 35;
        static final int TRANSACTION_setGPSDisabled = 53;
        static final int TRANSACTION_setNfcPolicies = 37;
        static final int TRANSACTION_setSecurityLevel = 42;
        static final int TRANSACTION_setUnSecureSoftApDisabled = 44;
        static final int TRANSACTION_setUserProfilesDisabled = 41;
        static final int TRANSACTION_setWifiApPolicies = 27;
        static final int TRANSACTION_setWifiAutoConnectionDisabled = 46;
        static final int TRANSACTION_setWifiEditDisabled = 55;
        static final int TRANSACTION_setWifiProfile = 48;
        static final int TRANSACTION_setWlanApClientBlackList = 29;
        static final int TRANSACTION_setWlanConfiguration = 33;
        static final int TRANSACTION_setWlanPolicies = 25;
        static final int TRANSACTION_turnOnGPS = 51;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDeviceConnectivityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDeviceConnectivityManager)) {
                return new Proxy(obj);
            }
            return (IDeviceConnectivityManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            ComponentName _arg02;
            ComponentName _arg03;
            if (code != 1598968902) {
                boolean _arg1 = false;
                ComponentName _arg04 = null;
                WifiConfiguration _arg12 = null;
                ComponentName _arg05 = null;
                ComponentName _arg06 = null;
                ComponentName _arg07 = null;
                ComponentName _arg08 = null;
                ComponentName _arg09 = null;
                ComponentName _arg010 = null;
                ComponentName _arg011 = null;
                WifiConfiguration _arg13 = null;
                WifiConfiguration _arg14 = null;
                ComponentName _arg012 = null;
                ComponentName _arg013 = null;
                ComponentName _arg014 = null;
                ComponentName _arg015 = null;
                ComponentName _arg016 = null;
                ComponentName _arg017 = null;
                ComponentName _arg018 = null;
                ComponentName _arg019 = null;
                ComponentName _arg020 = null;
                ComponentName _arg021 = null;
                ComponentName _arg022 = null;
                ComponentName _arg023 = null;
                ComponentName _arg024 = null;
                ComponentName _arg025 = null;
                ComponentName _arg026 = null;
                ComponentName _arg027 = null;
                ComponentName _arg028 = null;
                ComponentName _arg029 = null;
                ComponentName _arg030 = null;
                ComponentName _arg031 = null;
                ComponentName _arg032 = null;
                ComponentName _arg033 = null;
                ComponentName _arg034 = null;
                ComponentName _arg035 = null;
                ComponentName _arg036 = null;
                ComponentName _arg037 = null;
                ComponentName _arg038 = null;
                ComponentName _arg039 = null;
                ComponentName _arg040 = null;
                ComponentName _arg041 = null;
                ComponentName _arg042 = null;
                ComponentName _arg043 = null;
                ComponentName _arg044 = null;
                ComponentName _arg045 = null;
                ComponentName _arg046 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean addBluetoothDevicesToBlackList = addBluetoothDevicesToBlackList(_arg04, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(addBluetoothDevicesToBlackList ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg046 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean addBluetoothDevicesToWhiteList = addBluetoothDevicesToWhiteList(_arg046, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(addBluetoothDevicesToWhiteList ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg045 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean removeBluetoothDevicesFromBlackList = removeBluetoothDevicesFromBlackList(_arg045, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeBluetoothDevicesFromBlackList ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg044 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean removeBluetoothDevicesFromWhiteList = removeBluetoothDevicesFromWhiteList(_arg044, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeBluetoothDevicesFromWhiteList ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg043 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result = getBluetoothDevicesFromBlackLists(_arg043);
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg042 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result2 = getBluetoothDevicesFromWhiteLists(_arg042);
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg041 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isBlackListedDevice = isBlackListedDevice(_arg041, data.readString());
                        reply.writeNoException();
                        reply.writeInt(isBlackListedDevice ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg040 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isWhiteListedDevice = isWhiteListedDevice(_arg040, data.readString());
                        reply.writeNoException();
                        reply.writeInt(isWhiteListedDevice ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg039 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean addSSIDToBlackList = addSSIDToBlackList(_arg039, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(addSSIDToBlackList ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg038 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean removeSSIDFromBlackList = removeSSIDFromBlackList(_arg038, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeSSIDFromBlackList ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg037 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result3 = getSSIDBlackList(_arg037);
                        reply.writeNoException();
                        reply.writeStringList(_result3);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg036 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isBlackListedSSID = isBlackListedSSID(_arg036, data.readString());
                        reply.writeNoException();
                        reply.writeInt(isBlackListedSSID ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg035 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean addSSIDToWhiteList = addSSIDToWhiteList(_arg035, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(addSSIDToWhiteList ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg034 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean removeSSIDFromWhiteList = removeSSIDFromWhiteList(_arg034, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeSSIDFromWhiteList ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg033 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result4 = getSSIDWhiteList(_arg033);
                        reply.writeNoException();
                        reply.writeStringList(_result4);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWhiteListedSSID = isWhiteListedSSID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isWhiteListedSSID ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addBSSIDToWhiteList = addBSSIDToWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(addBSSIDToWhiteList ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addBSSIDToBlackList = addBSSIDToBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(addBSSIDToBlackList ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeBSSIDFromWhiteList = removeBSSIDFromWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeBSSIDFromWhiteList ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeBSSIDFromBlackList = removeBSSIDFromBlackList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeBSSIDFromBlackList ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result5 = getBSSIDWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result5);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result6 = getBSSIDBlackList();
                        reply.writeNoException();
                        reply.writeStringList(_result6);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWhiteListedBSSID = isWhiteListedBSSID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isWhiteListedBSSID ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBlackListedBSSID = isBlackListedBSSID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isBlackListedBSSID ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg032 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean wlanPolicies = setWlanPolicies(_arg032, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(wlanPolicies ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg031 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result7 = getWlanPolicies(_arg031);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg030 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean wifiApPolicies = setWifiApPolicies(_arg030, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(wifiApPolicies ? 1 : 0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg029 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result8 = getWifiApPolicies(_arg029);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg028 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setWlanApClientBlackList(_arg028, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg027 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result9 = getWlanApClientBlackList(_arg027);
                        reply.writeNoException();
                        reply.writeStringList(_result9);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg026 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removeWlanApClientBlackList(_arg026, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg025 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result10 = getWlanConfiguration(_arg025);
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg024 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean wlanConfiguration = setWlanConfiguration(_arg024, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(wlanConfiguration ? 1 : 0);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg023 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        String _result11 = getWifiMacAddress(_arg023);
                        reply.writeNoException();
                        reply.writeString(_result11);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg022 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean bluetoothPolicies = setBluetoothPolicies(_arg022, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(bluetoothPolicies ? 1 : 0);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg021 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result12 = getBluetoothPolicies(_arg021);
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg020 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean nfcPolicies = setNfcPolicies(_arg020, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(nfcPolicies ? 1 : 0);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg019 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result13 = getNfcPolicies(_arg019);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        String _result14 = getDevicePosition(_arg018);
                        reply.writeNoException();
                        reply.writeString(_result14);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isUserProfilesDisabled = isUserProfilesDisabled(_arg017);
                        reply.writeNoException();
                        reply.writeInt(isUserProfilesDisabled ? 1 : 0);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean userProfilesDisabled = setUserProfilesDisabled(_arg016, _arg1);
                        reply.writeNoException();
                        reply.writeInt(userProfilesDisabled ? 1 : 0);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean securityLevel = setSecurityLevel(_arg015, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(securityLevel ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result15 = getSecurityLevel(_arg014);
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean unSecureSoftApDisabled = setUnSecureSoftApDisabled(_arg013, _arg1);
                        reply.writeNoException();
                        reply.writeInt(unSecureSoftApDisabled ? 1 : 0);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isUnSecureSoftApDisabled = isUnSecureSoftApDisabled(_arg012);
                        reply.writeNoException();
                        reply.writeInt(isUnSecureSoftApDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setWifiAutoConnectionDisabled /* 46 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean wifiAutoConnectionDisabled = setWifiAutoConnectionDisabled(_arg1);
                        reply.writeNoException();
                        reply.writeInt(wifiAutoConnectionDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isWifiAutoConnectionDisabled /* 47 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWifiAutoConnectionDisabled = isWifiAutoConnectionDisabled();
                        reply.writeNoException();
                        reply.writeInt(isWifiAutoConnectionDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setWifiProfile /* 48 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean wifiProfile = setWifiProfile(_arg0, _arg14);
                        reply.writeNoException();
                        reply.writeInt(wifiProfile ? 1 : 0);
                        return true;
                    case TRANSACTION_removeWifiProfile /* 49 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean removeWifiProfile = removeWifiProfile(_arg02, _arg13);
                        reply.writeNoException();
                        reply.writeInt(removeWifiProfile ? 1 : 0);
                        return true;
                    case TRANSACTION_getWifiProfileList /* 50 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result16 = getWifiProfileList(_arg011);
                        reply.writeNoException();
                        reply.writeStringList(_result16);
                        return true;
                    case TRANSACTION_turnOnGPS /* 51 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        turnOnGPS(_arg010, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isGPSTurnOn /* 52 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isGPSTurnOn = isGPSTurnOn(_arg09);
                        reply.writeNoException();
                        reply.writeInt(isGPSTurnOn ? 1 : 0);
                        return true;
                    case TRANSACTION_setGPSDisabled /* 53 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setGPSDisabled(_arg08, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isGPSDisabled /* 54 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isGPSDisabled = isGPSDisabled(_arg07);
                        reply.writeNoException();
                        reply.writeInt(isGPSDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setWifiEditDisabled /* 55 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean wifiEditDisabled = setWifiEditDisabled(_arg06, _arg1);
                        reply.writeNoException();
                        reply.writeInt(wifiEditDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isWifiEditDisabled /* 56 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isWifiEditDisabled = isWifiEditDisabled(_arg05);
                        reply.writeNoException();
                        reply.writeInt(isWifiEditDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isWifiProfileSet /* 57 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean isWifiProfileSet = isWifiProfileSet(_arg03, _arg12);
                        reply.writeNoException();
                        reply.writeInt(isWifiProfileSet ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDeviceConnectivityManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean addBluetoothDevicesToBlackList(ComponentName admin, List<String> devices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(devices);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean addBluetoothDevicesToWhiteList(ComponentName admin, List<String> devices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(devices);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean removeBluetoothDevicesFromBlackList(ComponentName admin, List<String> devices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(devices);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean removeBluetoothDevicesFromWhiteList(ComponentName admin, List<String> devices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(devices);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getBluetoothDevicesFromBlackLists(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getBluetoothDevicesFromWhiteLists(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isBlackListedDevice(ComponentName admin, String device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(device);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isWhiteListedDevice(ComponentName admin, String device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(device);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean addSSIDToBlackList(ComponentName admin, List<String> ssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(ssids);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean removeSSIDFromBlackList(ComponentName admin, List<String> ssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(ssids);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getSSIDBlackList(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isBlackListedSSID(ComponentName admin, String ssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(ssid);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean addSSIDToWhiteList(ComponentName admin, List<String> ssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(ssids);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean removeSSIDFromWhiteList(ComponentName admin, List<String> ssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(ssids);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getSSIDWhiteList(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isWhiteListedSSID(String ssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ssid);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean addBSSIDToWhiteList(List<String> bssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(bssids);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean addBSSIDToBlackList(List<String> bssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(bssids);
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean removeBSSIDFromWhiteList(List<String> bssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(bssids);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean removeBSSIDFromBlackList(List<String> bssids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(bssids);
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getBSSIDWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getBSSIDBlackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isWhiteListedBSSID(String bssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(bssid);
                    boolean _result = false;
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isBlackListedBSSID(String bssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(bssid);
                    boolean _result = false;
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setWlanPolicies(ComponentName admin, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public int getWlanPolicies(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setWifiApPolicies(ComponentName admin, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public int getWifiApPolicies(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public void setWlanApClientBlackList(ComponentName admin, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(list);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getWlanApClientBlackList(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public void removeWlanApClientBlackList(ComponentName admin, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(list);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getWlanConfiguration(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setWlanConfiguration(ComponentName admin, List<String> wlanConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(wlanConfig);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public String getWifiMacAddress(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setBluetoothPolicies(ComponentName admin, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public int getBluetoothPolicies(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setNfcPolicies(ComponentName admin, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public int getNfcPolicies(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public String getDevicePosition(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isUserProfilesDisabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setUserProfilesDisabled(ComponentName admin, boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disable ? 1 : 0);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setSecurityLevel(ComponentName admin, int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(level);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public int getSecurityLevel(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setUnSecureSoftApDisabled(ComponentName admin, boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disable ? 1 : 0);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isUnSecureSoftApDisabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setWifiAutoConnectionDisabled(boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disable ? 1 : 0);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setWifiAutoConnectionDisabled, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isWifiAutoConnectionDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isWifiAutoConnectionDisabled, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setWifiProfile(ComponentName admin, WifiConfiguration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setWifiProfile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean removeWifiProfile(ComponentName admin, WifiConfiguration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_removeWifiProfile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public List<String> getWifiProfileList(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getWifiProfileList, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public void turnOnGPS(ComponentName admin, boolean on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(on ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_turnOnGPS, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isGPSTurnOn(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isGPSTurnOn, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public void setGPSDisabled(ComponentName admin, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setGPSDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isGPSDisabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isGPSDisabled, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean setWifiEditDisabled(ComponentName admin, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setWifiEditDisabled, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isWifiEditDisabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isWifiEditDisabled, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
            public boolean isWifiProfileSet(ComponentName admin, WifiConfiguration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isWifiProfileSet, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}

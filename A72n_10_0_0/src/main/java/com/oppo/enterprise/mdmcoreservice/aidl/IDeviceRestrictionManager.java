package com.oppo.enterprise.mdmcoreservice.aidl;

import android.content.ComponentName;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IDeviceRestrictionManager extends IInterface {
    void addAppRestriction(ComponentName componentName, int i, List<String> list) throws RemoteException;

    void addBrowserRestriction(int i, List<String> list) throws RemoteException;

    boolean allowWifiCellularNetwork(ComponentName componentName, String str) throws RemoteException;

    void clearBrowserRestriction(int i) throws RemoteException;

    boolean disableClipboard(ComponentName componentName, boolean z) throws RemoteException;

    int getAirplanePolices(ComponentName componentName) throws RemoteException;

    List<String> getAppRestriction(ComponentName componentName, int i) throws RemoteException;

    int getAppRestrictionPolicies(ComponentName componentName) throws RemoteException;

    List<String> getAppUninstallationPolicies() throws RemoteException;

    List<String> getApplicationDisabledInLauncherOrRecentTask(int i) throws RemoteException;

    List<String> getBrowserRestrictionUrls(int i) throws RemoteException;

    int getCameraPolicies() throws RemoteException;

    int getDefaultDataCard(ComponentName componentName) throws RemoteException;

    int getDefaultVoiceCard(ComponentName componentName) throws RemoteException;

    int getGpsPolicies(ComponentName componentName) throws RemoteException;

    int getMicrophonePolicies(ComponentName componentName) throws RemoteException;

    int getMobileDataMode(ComponentName componentName) throws RemoteException;

    boolean getPowerDisable(ComponentName componentName) throws RemoteException;

    long getRequiredStrongAuthTime(ComponentName componentName) throws RemoteException;

    int getSlot1DataConnectivityDisabled(ComponentName componentName) throws RemoteException;

    int getSlot2DataConnectivityDisabled(ComponentName componentName) throws RemoteException;

    int getSpeakerPolicies(ComponentName componentName) throws RemoteException;

    boolean getSplitScreenDisable(ComponentName componentName) throws RemoteException;

    int getSystemUpdatePolicies(ComponentName componentName) throws RemoteException;

    int getUnlockByFacePolicies(ComponentName componentName) throws RemoteException;

    int getUnlockByFingerprintPolicies(ComponentName componentName) throws RemoteException;

    int getUserPasswordPolicies(ComponentName componentName) throws RemoteException;

    boolean isAdbDisabled(ComponentName componentName) throws RemoteException;

    boolean isAndroidBeamDisabled(ComponentName componentName) throws RemoteException;

    boolean isBackButtonDisabled(ComponentName componentName) throws RemoteException;

    boolean isBluetoothDataTransferDisabled() throws RemoteException;

    boolean isBluetoothDisabled(ComponentName componentName) throws RemoteException;

    boolean isBluetoothEnabled(ComponentName componentName) throws RemoteException;

    boolean isBluetoothOutGoingCallDisabled() throws RemoteException;

    boolean isBluetoothPairingDisabled() throws RemoteException;

    boolean isBluetoothTetheringDisabled(ComponentName componentName) throws RemoteException;

    boolean isChangeWallpaperDisabled(ComponentName componentName) throws RemoteException;

    boolean isClipboardDisabled() throws RemoteException;

    boolean isDataRoamingDisabled(ComponentName componentName) throws RemoteException;

    boolean isExternalStorageDisabled(ComponentName componentName) throws RemoteException;

    boolean isFloatTaskDisabled(ComponentName componentName) throws RemoteException;

    boolean isHomeButtonDisabled(ComponentName componentName) throws RemoteException;

    boolean isLanguageChangeDisabled(ComponentName componentName) throws RemoteException;

    boolean isMmsDisabled(ComponentName componentName) throws RemoteException;

    boolean isMultiAppSupport() throws RemoteException;

    boolean isNFCDisabled(ComponentName componentName) throws RemoteException;

    boolean isNFCTurnOn(ComponentName componentName) throws RemoteException;

    boolean isNavigationBarDisabled(ComponentName componentName) throws RemoteException;

    boolean isPowerSavingModeDisabled(ComponentName componentName) throws RemoteException;

    boolean isRecordDisabled(ComponentName componentName) throws RemoteException;

    boolean isSafeModeDisabled() throws RemoteException;

    boolean isScreenCaptureDisabled(ComponentName componentName) throws RemoteException;

    boolean isSettingsApplicationDisabled(ComponentName componentName) throws RemoteException;

    boolean isStatusBarExpandPanelDisabled(ComponentName componentName) throws RemoteException;

    boolean isSystemBrowserDisabled(ComponentName componentName) throws RemoteException;

    boolean isTaskButtonDisabled(ComponentName componentName) throws RemoteException;

    boolean isUSBDataDisabled(ComponentName componentName) throws RemoteException;

    boolean isUSBOtgDisabled(ComponentName componentName) throws RemoteException;

    boolean isUnknownSourceAppInstallDisabled(ComponentName componentName) throws RemoteException;

    boolean isUnlockByFaceDisabled(ComponentName componentName) throws RemoteException;

    boolean isUnlockByFingerprintDisabled(ComponentName componentName) throws RemoteException;

    boolean isUsbTetheringDisabled(ComponentName componentName) throws RemoteException;

    boolean isVoiceDisabled(ComponentName componentName) throws RemoteException;

    boolean isVoiceIncomingDisabled(ComponentName componentName, int i) throws RemoteException;

    boolean isVoiceOutgoingDisabled(ComponentName componentName, int i) throws RemoteException;

    boolean isWifiApDisabled(ComponentName componentName) throws RemoteException;

    boolean isWifiDisabled(ComponentName componentName) throws RemoteException;

    boolean isWifiOpen(ComponentName componentName) throws RemoteException;

    boolean isWifiP2pDisabled(ComponentName componentName) throws RemoteException;

    boolean isWifiSharingDisabled(ComponentName componentName) throws RemoteException;

    void openCloseNFC(ComponentName componentName, boolean z) throws RemoteException;

    List<String> queryBrowserHistory(int i, int i2) throws RemoteException;

    void removeAllAppRestriction(ComponentName componentName, int i) throws RemoteException;

    void removeAppRestriction(ComponentName componentName, int i, List<String> list) throws RemoteException;

    void removeBrowserRestriction(int i, List<String> list) throws RemoteException;

    void setAdbDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setAirplanePolices(ComponentName componentName, int i) throws RemoteException;

    boolean setAndroidBeamDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setAppRestrictionPolicies(ComponentName componentName, int i) throws RemoteException;

    boolean setAppUninstallationPolicies(int i, List<String> list) throws RemoteException;

    void setApplicationDisabledInLauncherOrRecentTask(List<String> list, int i) throws RemoteException;

    boolean setBackButtonDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setBluetoothDataTransferDisabled(boolean z) throws RemoteException;

    void setBluetoothDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setBluetoothEnabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setBluetoothOutGoingCallDisabled(boolean z) throws RemoteException;

    boolean setBluetoothPairingDisabled(boolean z) throws RemoteException;

    void setBluetoothTetheringDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setBrowserRestriction(int i) throws RemoteException;

    boolean setCameraPolicies(int i) throws RemoteException;

    void setChangeWallpaperDisable(ComponentName componentName, boolean z) throws RemoteException;

    boolean setDataRoamingDisabled(ComponentName componentName, boolean z) throws RemoteException;

    Bundle setDefaultDataCard(ComponentName componentName, int i) throws RemoteException;

    Bundle setDefaultVoiceCard(ComponentName componentName, int i) throws RemoteException;

    void setExternalStorageDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setFloatTaskDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setGpsPolicies(ComponentName componentName, int i) throws RemoteException;

    boolean setHomeButtonDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setLanguageChangeDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setMicrophonePolicies(ComponentName componentName, int i) throws RemoteException;

    void setMmsDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setMobileDataMode(ComponentName componentName, int i) throws RemoteException;

    void setMultiAppSupport(boolean z) throws RemoteException;

    void setNFCDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setNavigationBarDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setPowerDisable(ComponentName componentName, boolean z) throws RemoteException;

    void setPowerSavingModeDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setRecordDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setRequiredStrongAuthTime(ComponentName componentName, long j) throws RemoteException;

    void setSafeModeDisabled(boolean z) throws RemoteException;

    boolean setScreenCaptureDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setSettingsApplicationDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setSlot1DataConnectivityDisabled(ComponentName componentName, String str) throws RemoteException;

    void setSlot2DataConnectivityDisabled(ComponentName componentName, String str) throws RemoteException;

    boolean setSpeakerPolicies(ComponentName componentName, int i) throws RemoteException;

    boolean setSplitScreenDisable(ComponentName componentName, boolean z) throws RemoteException;

    void setStatusBarExpandPanelDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setSystemBrowserDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setSystemUpdatePolicies(ComponentName componentName, int i) throws RemoteException;

    boolean setTaskButtonDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setUSBDataDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setUSBOtgDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setUnknownSourceAppInstallDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setUnlockByFaceDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setUnlockByFacePolicies(ComponentName componentName, int i) throws RemoteException;

    boolean setUnlockByFingerprintDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setUnlockByFingerprintPolicies(ComponentName componentName, int i) throws RemoteException;

    void setUsbTetheringDisable(ComponentName componentName, boolean z) throws RemoteException;

    boolean setUserPasswordPolicies(ComponentName componentName, int i) throws RemoteException;

    void setVoiceDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setVoiceIncomingDisable(ComponentName componentName, boolean z) throws RemoteException;

    void setVoiceOutgoingDisable(ComponentName componentName, boolean z) throws RemoteException;

    void setWifiApDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setWifiDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setWifiInBackground(ComponentName componentName, boolean z) throws RemoteException;

    void setWifiP2pDisabled(ComponentName componentName, boolean z) throws RemoteException;

    void setWifiSharingDisabled(ComponentName componentName, boolean z) throws RemoteException;

    public static abstract class Stub extends Binder implements IDeviceRestrictionManager {
        private static final String DESCRIPTOR = "com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager";
        static final int TRANSACTION_addAppRestriction = 48;
        static final int TRANSACTION_addBrowserRestriction = 92;
        static final int TRANSACTION_allowWifiCellularNetwork = 110;
        static final int TRANSACTION_clearBrowserRestriction = 91;
        static final int TRANSACTION_disableClipboard = 106;
        static final int TRANSACTION_getAirplanePolices = 99;
        static final int TRANSACTION_getAppRestriction = 47;
        static final int TRANSACTION_getAppRestrictionPolicies = 46;
        static final int TRANSACTION_getAppUninstallationPolicies = 72;
        static final int TRANSACTION_getApplicationDisabledInLauncherOrRecentTask = 136;
        static final int TRANSACTION_getBrowserRestrictionUrls = 95;
        static final int TRANSACTION_getCameraPolicies = 33;
        static final int TRANSACTION_getDefaultDataCard = 111;
        static final int TRANSACTION_getDefaultVoiceCard = 54;
        static final int TRANSACTION_getGpsPolicies = 60;
        static final int TRANSACTION_getMicrophonePolicies = 68;
        static final int TRANSACTION_getMobileDataMode = 57;
        static final int TRANSACTION_getPowerDisable = 62;
        static final int TRANSACTION_getRequiredStrongAuthTime = 109;
        static final int TRANSACTION_getSlot1DataConnectivityDisabled = 117;
        static final int TRANSACTION_getSlot2DataConnectivityDisabled = 118;
        static final int TRANSACTION_getSpeakerPolicies = 70;
        static final int TRANSACTION_getSplitScreenDisable = 64;
        static final int TRANSACTION_getSystemUpdatePolicies = 126;
        static final int TRANSACTION_getUnlockByFacePolicies = 85;
        static final int TRANSACTION_getUnlockByFingerprintPolicies = 83;
        static final int TRANSACTION_getUserPasswordPolicies = 87;
        static final int TRANSACTION_isAdbDisabled = 77;
        static final int TRANSACTION_isAndroidBeamDisabled = 30;
        static final int TRANSACTION_isBackButtonDisabled = 103;
        static final int TRANSACTION_isBluetoothDataTransferDisabled = 20;
        static final int TRANSACTION_isBluetoothDisabled = 14;
        static final int TRANSACTION_isBluetoothEnabled = 16;
        static final int TRANSACTION_isBluetoothOutGoingCallDisabled = 24;
        static final int TRANSACTION_isBluetoothPairingDisabled = 22;
        static final int TRANSACTION_isBluetoothTetheringDisabled = 18;
        static final int TRANSACTION_isChangeWallpaperDisabled = 38;
        static final int TRANSACTION_isClipboardDisabled = 107;
        static final int TRANSACTION_isDataRoamingDisabled = 114;
        static final int TRANSACTION_isExternalStorageDisabled = 40;
        static final int TRANSACTION_isFloatTaskDisabled = 97;
        static final int TRANSACTION_isHomeButtonDisabled = 101;
        static final int TRANSACTION_isLanguageChangeDisabled = 137;
        static final int TRANSACTION_isMmsDisabled = 52;
        static final int TRANSACTION_isMultiAppSupport = 131;
        static final int TRANSACTION_isNFCDisabled = 26;
        static final int TRANSACTION_isNFCTurnOn = 28;
        static final int TRANSACTION_isNavigationBarDisabled = 130;
        static final int TRANSACTION_isPowerSavingModeDisabled = 134;
        static final int TRANSACTION_isRecordDisabled = 36;
        static final int TRANSACTION_isSafeModeDisabled = 35;
        static final int TRANSACTION_isScreenCaptureDisabled = 74;
        static final int TRANSACTION_isSettingsApplicationDisabled = 128;
        static final int TRANSACTION_isStatusBarExpandPanelDisabled = 2;
        static final int TRANSACTION_isSystemBrowserDisabled = 105;
        static final int TRANSACTION_isTaskButtonDisabled = 89;
        static final int TRANSACTION_isUSBDataDisabled = 42;
        static final int TRANSACTION_isUSBOtgDisabled = 44;
        static final int TRANSACTION_isUnknownSourceAppInstallDisabled = 66;
        static final int TRANSACTION_isUnlockByFaceDisabled = 81;
        static final int TRANSACTION_isUnlockByFingerprintDisabled = 79;
        static final int TRANSACTION_isUsbTetheringDisabled = 124;
        static final int TRANSACTION_isVoiceDisabled = 55;
        static final int TRANSACTION_isVoiceIncomingDisabled = 122;
        static final int TRANSACTION_isVoiceOutgoingDisabled = 121;
        static final int TRANSACTION_isWifiApDisabled = 8;
        static final int TRANSACTION_isWifiDisabled = 4;
        static final int TRANSACTION_isWifiOpen = 12;
        static final int TRANSACTION_isWifiP2pDisabled = 6;
        static final int TRANSACTION_isWifiSharingDisabled = 10;
        static final int TRANSACTION_openCloseNFC = 27;
        static final int TRANSACTION_queryBrowserHistory = 94;
        static final int TRANSACTION_removeAllAppRestriction = 50;
        static final int TRANSACTION_removeAppRestriction = 49;
        static final int TRANSACTION_removeBrowserRestriction = 93;
        static final int TRANSACTION_setAdbDisabled = 76;
        static final int TRANSACTION_setAirplanePolices = 98;
        static final int TRANSACTION_setAndroidBeamDisabled = 29;
        static final int TRANSACTION_setAppRestrictionPolicies = 45;
        static final int TRANSACTION_setAppUninstallationPolicies = 71;
        static final int TRANSACTION_setApplicationDisabledInLauncherOrRecentTask = 135;
        static final int TRANSACTION_setBackButtonDisabled = 102;
        static final int TRANSACTION_setBluetoothDataTransferDisabled = 19;
        static final int TRANSACTION_setBluetoothDisabled = 13;
        static final int TRANSACTION_setBluetoothEnabled = 15;
        static final int TRANSACTION_setBluetoothOutGoingCallDisabled = 23;
        static final int TRANSACTION_setBluetoothPairingDisabled = 21;
        static final int TRANSACTION_setBluetoothTetheringDisabled = 17;
        static final int TRANSACTION_setBrowserRestriction = 90;
        static final int TRANSACTION_setCameraPolicies = 32;
        static final int TRANSACTION_setChangeWallpaperDisable = 37;
        static final int TRANSACTION_setDataRoamingDisabled = 113;
        static final int TRANSACTION_setDefaultDataCard = 112;
        static final int TRANSACTION_setDefaultVoiceCard = 53;
        static final int TRANSACTION_setExternalStorageDisabled = 39;
        static final int TRANSACTION_setFloatTaskDisabled = 96;
        static final int TRANSACTION_setGpsPolicies = 59;
        static final int TRANSACTION_setHomeButtonDisabled = 100;
        static final int TRANSACTION_setLanguageChangeDisabled = 75;
        static final int TRANSACTION_setMicrophonePolicies = 67;
        static final int TRANSACTION_setMmsDisabled = 51;
        static final int TRANSACTION_setMobileDataMode = 58;
        static final int TRANSACTION_setMultiAppSupport = 132;
        static final int TRANSACTION_setNFCDisabled = 25;
        static final int TRANSACTION_setNavigationBarDisabled = 129;
        static final int TRANSACTION_setPowerDisable = 61;
        static final int TRANSACTION_setPowerSavingModeDisabled = 133;
        static final int TRANSACTION_setRecordDisabled = 31;
        static final int TRANSACTION_setRequiredStrongAuthTime = 108;
        static final int TRANSACTION_setSafeModeDisabled = 34;
        static final int TRANSACTION_setScreenCaptureDisabled = 73;
        static final int TRANSACTION_setSettingsApplicationDisabled = 127;
        static final int TRANSACTION_setSlot1DataConnectivityDisabled = 115;
        static final int TRANSACTION_setSlot2DataConnectivityDisabled = 116;
        static final int TRANSACTION_setSpeakerPolicies = 69;
        static final int TRANSACTION_setSplitScreenDisable = 63;
        static final int TRANSACTION_setStatusBarExpandPanelDisabled = 1;
        static final int TRANSACTION_setSystemBrowserDisabled = 104;
        static final int TRANSACTION_setSystemUpdatePolicies = 125;
        static final int TRANSACTION_setTaskButtonDisabled = 88;
        static final int TRANSACTION_setUSBDataDisabled = 41;
        static final int TRANSACTION_setUSBOtgDisabled = 43;
        static final int TRANSACTION_setUnknownSourceAppInstallDisabled = 65;
        static final int TRANSACTION_setUnlockByFaceDisabled = 80;
        static final int TRANSACTION_setUnlockByFacePolicies = 84;
        static final int TRANSACTION_setUnlockByFingerprintDisabled = 78;
        static final int TRANSACTION_setUnlockByFingerprintPolicies = 82;
        static final int TRANSACTION_setUsbTetheringDisable = 123;
        static final int TRANSACTION_setUserPasswordPolicies = 86;
        static final int TRANSACTION_setVoiceDisabled = 56;
        static final int TRANSACTION_setVoiceIncomingDisable = 120;
        static final int TRANSACTION_setVoiceOutgoingDisable = 119;
        static final int TRANSACTION_setWifiApDisabled = 7;
        static final int TRANSACTION_setWifiDisabled = 3;
        static final int TRANSACTION_setWifiInBackground = 11;
        static final int TRANSACTION_setWifiP2pDisabled = 5;
        static final int TRANSACTION_setWifiSharingDisabled = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDeviceRestrictionManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDeviceRestrictionManager)) {
                return new Proxy(obj);
            }
            return (IDeviceRestrictionManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg1 = false;
                ComponentName _arg0 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setStatusBarExpandPanelDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isStatusBarExpandPanelDisabled = isStatusBarExpandPanelDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isStatusBarExpandPanelDisabled ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setWifiDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isWifiDisabled = isWifiDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isWifiDisabled ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setWifiP2pDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isWifiP2pDisabled = isWifiP2pDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isWifiP2pDisabled ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setWifiApDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isWifiApDisabled = isWifiApDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isWifiApDisabled ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setWifiSharingDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isWifiSharingDisabled = isWifiSharingDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isWifiSharingDisabled ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean wifiInBackground = setWifiInBackground(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(wifiInBackground ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isWifiOpen = isWifiOpen(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isWifiOpen ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setBluetoothDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isBluetoothDisabled = isBluetoothDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isBluetoothDisabled ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setBluetoothEnabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isBluetoothEnabled = isBluetoothEnabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isBluetoothEnabled ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setBluetoothTetheringDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isBluetoothTetheringDisabled = isBluetoothTetheringDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isBluetoothTetheringDisabled ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean bluetoothDataTransferDisabled = setBluetoothDataTransferDisabled(_arg1);
                        reply.writeNoException();
                        reply.writeInt(bluetoothDataTransferDisabled ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBluetoothDataTransferDisabled = isBluetoothDataTransferDisabled();
                        reply.writeNoException();
                        reply.writeInt(isBluetoothDataTransferDisabled ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean bluetoothPairingDisabled = setBluetoothPairingDisabled(_arg1);
                        reply.writeNoException();
                        reply.writeInt(bluetoothPairingDisabled ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBluetoothPairingDisabled = isBluetoothPairingDisabled();
                        reply.writeNoException();
                        reply.writeInt(isBluetoothPairingDisabled ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean bluetoothOutGoingCallDisabled = setBluetoothOutGoingCallDisabled(_arg1);
                        reply.writeNoException();
                        reply.writeInt(bluetoothOutGoingCallDisabled ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isBluetoothOutGoingCallDisabled = isBluetoothOutGoingCallDisabled();
                        reply.writeNoException();
                        reply.writeInt(isBluetoothOutGoingCallDisabled ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setNFCDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isNFCDisabled = isNFCDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isNFCDisabled ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        openCloseNFC(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isNFCTurnOn = isNFCTurnOn(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isNFCTurnOn ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean androidBeamDisabled = setAndroidBeamDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(androidBeamDisabled ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isAndroidBeamDisabled = isAndroidBeamDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isAndroidBeamDisabled ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean recordDisabled = setRecordDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(recordDisabled ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cameraPolicies = setCameraPolicies(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(cameraPolicies ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getCameraPolicies();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setSafeModeDisabled(_arg1);
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSafeModeDisabled = isSafeModeDisabled();
                        reply.writeNoException();
                        reply.writeInt(isSafeModeDisabled ? 1 : 0);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isRecordDisabled = isRecordDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isRecordDisabled ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setChangeWallpaperDisable(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isChangeWallpaperDisabled = isChangeWallpaperDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isChangeWallpaperDisabled ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setExternalStorageDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isExternalStorageDisabled = isExternalStorageDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isExternalStorageDisabled ? 1 : 0);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setUSBDataDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isUSBDataDisabled = isUSBDataDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isUSBDataDisabled ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setUSBOtgDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isUSBOtgDisabled = isUSBOtgDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isUSBOtgDisabled ? 1 : 0);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setAppRestrictionPolicies(_arg0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getAppRestrictionPolicies /* 46 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result2 = getAppRestrictionPolicies(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case TRANSACTION_getAppRestriction /* 47 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result3 = getAppRestriction(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result3);
                        return true;
                    case TRANSACTION_addAppRestriction /* 48 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        addAppRestriction(_arg0, data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_removeAppRestriction /* 49 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removeAppRestriction(_arg0, data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_removeAllAppRestriction /* 50 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removeAllAppRestriction(_arg0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setMmsDisabled /* 51 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setMmsDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isMmsDisabled /* 52 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isMmsDisabled = isMmsDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isMmsDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setDefaultVoiceCard /* 53 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        Bundle _result4 = setDefaultVoiceCard(_arg0, data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getDefaultVoiceCard /* 54 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result5 = getDefaultVoiceCard(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case TRANSACTION_isVoiceDisabled /* 55 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isVoiceDisabled = isVoiceDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isVoiceDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setVoiceDisabled /* 56 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setVoiceDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getMobileDataMode /* 57 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result6 = getMobileDataMode(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case TRANSACTION_setMobileDataMode /* 58 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setMobileDataMode(_arg0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setGpsPolicies /* 59 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean gpsPolicies = setGpsPolicies(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(gpsPolicies ? 1 : 0);
                        return true;
                    case TRANSACTION_getGpsPolicies /* 60 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result7 = getGpsPolicies(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case TRANSACTION_setPowerDisable /* 61 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean powerDisable = setPowerDisable(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(powerDisable ? 1 : 0);
                        return true;
                    case TRANSACTION_getPowerDisable /* 62 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean powerDisable2 = getPowerDisable(_arg0);
                        reply.writeNoException();
                        reply.writeInt(powerDisable2 ? 1 : 0);
                        return true;
                    case TRANSACTION_setSplitScreenDisable /* 63 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean splitScreenDisable = setSplitScreenDisable(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(splitScreenDisable ? 1 : 0);
                        return true;
                    case TRANSACTION_getSplitScreenDisable /* 64 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean splitScreenDisable2 = getSplitScreenDisable(_arg0);
                        reply.writeNoException();
                        reply.writeInt(splitScreenDisable2 ? 1 : 0);
                        return true;
                    case TRANSACTION_setUnknownSourceAppInstallDisabled /* 65 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean unknownSourceAppInstallDisabled = setUnknownSourceAppInstallDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(unknownSourceAppInstallDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isUnknownSourceAppInstallDisabled /* 66 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isUnknownSourceAppInstallDisabled = isUnknownSourceAppInstallDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isUnknownSourceAppInstallDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setMicrophonePolicies /* 67 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean microphonePolicies = setMicrophonePolicies(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(microphonePolicies ? 1 : 0);
                        return true;
                    case TRANSACTION_getMicrophonePolicies /* 68 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result8 = getMicrophonePolicies(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case TRANSACTION_setSpeakerPolicies /* 69 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean speakerPolicies = setSpeakerPolicies(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(speakerPolicies ? 1 : 0);
                        return true;
                    case TRANSACTION_getSpeakerPolicies /* 70 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result9 = getSpeakerPolicies(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case TRANSACTION_setAppUninstallationPolicies /* 71 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean appUninstallationPolicies = setAppUninstallationPolicies(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(appUninstallationPolicies ? 1 : 0);
                        return true;
                    case TRANSACTION_getAppUninstallationPolicies /* 72 */:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result10 = getAppUninstallationPolicies();
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    case TRANSACTION_setScreenCaptureDisabled /* 73 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean screenCaptureDisabled = setScreenCaptureDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(screenCaptureDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isScreenCaptureDisabled /* 74 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isScreenCaptureDisabled = isScreenCaptureDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isScreenCaptureDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setLanguageChangeDisabled /* 75 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setLanguageChangeDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setAdbDisabled /* 76 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setAdbDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isAdbDisabled /* 77 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isAdbDisabled = isAdbDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isAdbDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setUnlockByFingerprintDisabled /* 78 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean unlockByFingerprintDisabled = setUnlockByFingerprintDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(unlockByFingerprintDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isUnlockByFingerprintDisabled /* 79 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isUnlockByFingerprintDisabled = isUnlockByFingerprintDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isUnlockByFingerprintDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setUnlockByFaceDisabled /* 80 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean unlockByFaceDisabled = setUnlockByFaceDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(unlockByFaceDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isUnlockByFaceDisabled /* 81 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isUnlockByFaceDisabled = isUnlockByFaceDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isUnlockByFaceDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setUnlockByFingerprintPolicies /* 82 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean unlockByFingerprintPolicies = setUnlockByFingerprintPolicies(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(unlockByFingerprintPolicies ? 1 : 0);
                        return true;
                    case TRANSACTION_getUnlockByFingerprintPolicies /* 83 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result11 = getUnlockByFingerprintPolicies(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case TRANSACTION_setUnlockByFacePolicies /* 84 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean unlockByFacePolicies = setUnlockByFacePolicies(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(unlockByFacePolicies ? 1 : 0);
                        return true;
                    case TRANSACTION_getUnlockByFacePolicies /* 85 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result12 = getUnlockByFacePolicies(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case TRANSACTION_setUserPasswordPolicies /* 86 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean userPasswordPolicies = setUserPasswordPolicies(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(userPasswordPolicies ? 1 : 0);
                        return true;
                    case TRANSACTION_getUserPasswordPolicies /* 87 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result13 = getUserPasswordPolicies(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case TRANSACTION_setTaskButtonDisabled /* 88 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean taskButtonDisabled = setTaskButtonDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(taskButtonDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isTaskButtonDisabled /* 89 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isTaskButtonDisabled = isTaskButtonDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isTaskButtonDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setBrowserRestriction /* 90 */:
                        data.enforceInterface(DESCRIPTOR);
                        setBrowserRestriction(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_clearBrowserRestriction /* 91 */:
                        data.enforceInterface(DESCRIPTOR);
                        clearBrowserRestriction(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_addBrowserRestriction /* 92 */:
                        data.enforceInterface(DESCRIPTOR);
                        addBrowserRestriction(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_removeBrowserRestriction /* 93 */:
                        data.enforceInterface(DESCRIPTOR);
                        removeBrowserRestriction(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_queryBrowserHistory /* 94 */:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result14 = queryBrowserHistory(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result14);
                        return true;
                    case TRANSACTION_getBrowserRestrictionUrls /* 95 */:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result15 = getBrowserRestrictionUrls(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result15);
                        return true;
                    case TRANSACTION_setFloatTaskDisabled /* 96 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean floatTaskDisabled = setFloatTaskDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(floatTaskDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isFloatTaskDisabled /* 97 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isFloatTaskDisabled = isFloatTaskDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isFloatTaskDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setAirplanePolices /* 98 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean airplanePolices = setAirplanePolices(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(airplanePolices ? 1 : 0);
                        return true;
                    case TRANSACTION_getAirplanePolices /* 99 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result16 = getAirplanePolices(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case TRANSACTION_setHomeButtonDisabled /* 100 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean homeButtonDisabled = setHomeButtonDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(homeButtonDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isHomeButtonDisabled /* 101 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isHomeButtonDisabled = isHomeButtonDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isHomeButtonDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setBackButtonDisabled /* 102 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean backButtonDisabled = setBackButtonDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(backButtonDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isBackButtonDisabled /* 103 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isBackButtonDisabled = isBackButtonDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isBackButtonDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setSystemBrowserDisabled /* 104 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean systemBrowserDisabled = setSystemBrowserDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(systemBrowserDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isSystemBrowserDisabled /* 105 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isSystemBrowserDisabled = isSystemBrowserDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isSystemBrowserDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_disableClipboard /* 106 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean disableClipboard = disableClipboard(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(disableClipboard ? 1 : 0);
                        return true;
                    case TRANSACTION_isClipboardDisabled /* 107 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isClipboardDisabled = isClipboardDisabled();
                        reply.writeNoException();
                        reply.writeInt(isClipboardDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setRequiredStrongAuthTime /* 108 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setRequiredStrongAuthTime(_arg0, data.readLong());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getRequiredStrongAuthTime /* 109 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        long _result17 = getRequiredStrongAuthTime(_arg0);
                        reply.writeNoException();
                        reply.writeLong(_result17);
                        return true;
                    case TRANSACTION_allowWifiCellularNetwork /* 110 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean allowWifiCellularNetwork = allowWifiCellularNetwork(_arg0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(allowWifiCellularNetwork ? 1 : 0);
                        return true;
                    case TRANSACTION_getDefaultDataCard /* 111 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result18 = getDefaultDataCard(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case TRANSACTION_setDefaultDataCard /* 112 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        Bundle _result19 = setDefaultDataCard(_arg0, data.readInt());
                        reply.writeNoException();
                        if (_result19 != null) {
                            reply.writeInt(1);
                            _result19.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_setDataRoamingDisabled /* 113 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean dataRoamingDisabled = setDataRoamingDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(dataRoamingDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isDataRoamingDisabled /* 114 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isDataRoamingDisabled = isDataRoamingDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isDataRoamingDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setSlot1DataConnectivityDisabled /* 115 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setSlot1DataConnectivityDisabled(_arg0, data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setSlot2DataConnectivityDisabled /* 116 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setSlot2DataConnectivityDisabled(_arg0, data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getSlot1DataConnectivityDisabled /* 117 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result20 = getSlot1DataConnectivityDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case TRANSACTION_getSlot2DataConnectivityDisabled /* 118 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result21 = getSlot2DataConnectivityDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case TRANSACTION_setVoiceOutgoingDisable /* 119 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setVoiceOutgoingDisable(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setVoiceIncomingDisable /* 120 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setVoiceIncomingDisable(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isVoiceOutgoingDisabled /* 121 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isVoiceOutgoingDisabled = isVoiceOutgoingDisabled(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVoiceOutgoingDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isVoiceIncomingDisabled /* 122 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isVoiceIncomingDisabled = isVoiceIncomingDisabled(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVoiceIncomingDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setUsbTetheringDisable /* 123 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setUsbTetheringDisable(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isUsbTetheringDisabled /* 124 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isUsbTetheringDisabled = isUsbTetheringDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isUsbTetheringDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setSystemUpdatePolicies /* 125 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean systemUpdatePolicies = setSystemUpdatePolicies(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(systemUpdatePolicies ? 1 : 0);
                        return true;
                    case TRANSACTION_getSystemUpdatePolicies /* 126 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result22 = getSystemUpdatePolicies(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case TRANSACTION_setSettingsApplicationDisabled /* 127 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setSettingsApplicationDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isSettingsApplicationDisabled /* 128 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isSettingsApplicationDisabled = isSettingsApplicationDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isSettingsApplicationDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setNavigationBarDisabled /* 129 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setNavigationBarDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isNavigationBarDisabled /* 130 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isNavigationBarDisabled = isNavigationBarDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isNavigationBarDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_isMultiAppSupport /* 131 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isMultiAppSupport = isMultiAppSupport();
                        reply.writeNoException();
                        reply.writeInt(isMultiAppSupport ? 1 : 0);
                        return true;
                    case TRANSACTION_setMultiAppSupport /* 132 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setMultiAppSupport(_arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setPowerSavingModeDisabled /* 133 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setPowerSavingModeDisabled(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isPowerSavingModeDisabled /* 134 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isPowerSavingModeDisabled = isPowerSavingModeDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isPowerSavingModeDisabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setApplicationDisabledInLauncherOrRecentTask /* 135 */:
                        data.enforceInterface(DESCRIPTOR);
                        setApplicationDisabledInLauncherOrRecentTask(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getApplicationDisabledInLauncherOrRecentTask /* 136 */:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result23 = getApplicationDisabledInLauncherOrRecentTask(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result23);
                        return true;
                    case TRANSACTION_isLanguageChangeDisabled /* 137 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isLanguageChangeDisabled = isLanguageChangeDisabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isLanguageChangeDisabled ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDeviceRestrictionManager {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setStatusBarExpandPanelDisabled(ComponentName componentName, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isStatusBarExpandPanelDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setWifiDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isWifiDisabled(ComponentName admin) throws RemoteException {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setWifiP2pDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isWifiP2pDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(6, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setWifiApDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isWifiApDisabled(ComponentName admin) throws RemoteException {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setWifiSharingDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isWifiSharingDisabled(ComponentName admin) throws RemoteException {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setWifiInBackground(ComponentName admin, boolean enable) throws RemoteException {
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
                    _data.writeInt(enable ? 1 : 0);
                    this.mRemote.transact(11, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isWifiOpen(ComponentName admin) throws RemoteException {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setBluetoothDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isBluetoothDisabled(ComponentName admin) throws RemoteException {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setBluetoothEnabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isBluetoothEnabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(16, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setBluetoothTetheringDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isBluetoothTetheringDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(18, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setBluetoothDataTransferDisabled(boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disabled ? 1 : 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isBluetoothDataTransferDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setBluetoothPairingDisabled(boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disabled ? 1 : 0);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isBluetoothPairingDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(22, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setBluetoothOutGoingCallDisabled(boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disabled ? 1 : 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isBluetoothOutGoingCallDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setNFCDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isNFCDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(26, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void openCloseNFC(ComponentName admin, boolean enable) throws RemoteException {
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
                    _data.writeInt(enable ? 1 : 0);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isNFCTurnOn(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(28, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setAndroidBeamDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(29, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isAndroidBeamDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(30, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setRecordDisabled(ComponentName componentName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enable ? 1 : 0);
                    this.mRemote.transact(31, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setCameraPolicies(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    boolean _result = false;
                    this.mRemote.transact(32, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getCameraPolicies() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setSafeModeDisabled(boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isSafeModeDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(35, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isRecordDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setChangeWallpaperDisable(ComponentName componentName, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isChangeWallpaperDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(38, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setExternalStorageDisabled(ComponentName componentName, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isExternalStorageDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setUSBDataDisabled(ComponentName componentName, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isUSBDataDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setUSBOtgDisabled(ComponentName componentName, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isUSBOtgDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setAppRestrictionPolicies(ComponentName admin, int pattern) throws RemoteException {
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
                    _data.writeInt(pattern);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getAppRestrictionPolicies(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getAppRestrictionPolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public List<String> getAppRestriction(ComponentName admin, int pattern) throws RemoteException {
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
                    _data.writeInt(pattern);
                    this.mRemote.transact(Stub.TRANSACTION_getAppRestriction, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void addAppRestriction(ComponentName admin, int pattern, List<String> pkgs) throws RemoteException {
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
                    _data.writeInt(pattern);
                    _data.writeStringList(pkgs);
                    this.mRemote.transact(Stub.TRANSACTION_addAppRestriction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void removeAppRestriction(ComponentName admin, int pattern, List<String> pkgs) throws RemoteException {
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
                    _data.writeInt(pattern);
                    _data.writeStringList(pkgs);
                    this.mRemote.transact(Stub.TRANSACTION_removeAppRestriction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void removeAllAppRestriction(ComponentName admin, int pattern) throws RemoteException {
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
                    _data.writeInt(pattern);
                    this.mRemote.transact(Stub.TRANSACTION_removeAllAppRestriction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setMmsDisabled(ComponentName componentName, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setMmsDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isMmsDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isMmsDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public Bundle setDefaultVoiceCard(ComponentName componentName, int slotId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(slotId);
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultVoiceCard, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getDefaultVoiceCard(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultVoiceCard, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isVoiceDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isVoiceDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setVoiceDisabled(ComponentName componentName, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setVoiceDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getMobileDataMode(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getMobileDataMode, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setMobileDataMode(ComponentName componentName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_setMobileDataMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setGpsPolicies(ComponentName admin, int mode) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setGpsPolicies, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getGpsPolicies(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getGpsPolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setPowerDisable(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setPowerDisable, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean getPowerDisable(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getPowerDisable, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setSplitScreenDisable(ComponentName admin, boolean disable) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setSplitScreenDisable, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean getSplitScreenDisable(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getSplitScreenDisable, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setUnknownSourceAppInstallDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setUnknownSourceAppInstallDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isUnknownSourceAppInstallDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isUnknownSourceAppInstallDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setMicrophonePolicies(ComponentName admin, int mode) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setMicrophonePolicies, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getMicrophonePolicies(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getMicrophonePolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setSpeakerPolicies(ComponentName admin, int mode) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setSpeakerPolicies, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getSpeakerPolicies(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getSpeakerPolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setAppUninstallationPolicies(int mode, List<String> appPackageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeStringList(appPackageNames);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setAppUninstallationPolicies, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public List<String> getAppUninstallationPolicies() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAppUninstallationPolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setScreenCaptureDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setScreenCaptureDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isScreenCaptureDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isScreenCaptureDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setLanguageChangeDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setLanguageChangeDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setAdbDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setAdbDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isAdbDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isAdbDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setUnlockByFingerprintDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setUnlockByFingerprintDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isUnlockByFingerprintDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isUnlockByFingerprintDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setUnlockByFaceDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setUnlockByFaceDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isUnlockByFaceDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isUnlockByFaceDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setUnlockByFingerprintPolicies(ComponentName admin, int policy) throws RemoteException {
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
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_setUnlockByFingerprintPolicies, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getUnlockByFingerprintPolicies(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getUnlockByFingerprintPolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setUnlockByFacePolicies(ComponentName admin, int policy) throws RemoteException {
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
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_setUnlockByFacePolicies, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getUnlockByFacePolicies(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getUnlockByFacePolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setUserPasswordPolicies(ComponentName admin, int mode) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setUserPasswordPolicies, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getUserPasswordPolicies(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getUserPasswordPolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setTaskButtonDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setTaskButtonDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isTaskButtonDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isTaskButtonDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setBrowserRestriction(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(Stub.TRANSACTION_setBrowserRestriction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void clearBrowserRestriction(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(Stub.TRANSACTION_clearBrowserRestriction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void addBrowserRestriction(int pattern, List<String> urls) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(urls);
                    this.mRemote.transact(Stub.TRANSACTION_addBrowserRestriction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void removeBrowserRestriction(int pattern, List<String> urls) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(urls);
                    this.mRemote.transact(Stub.TRANSACTION_removeBrowserRestriction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public List<String> queryBrowserHistory(int position, int pageSize) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(position);
                    _data.writeInt(pageSize);
                    this.mRemote.transact(Stub.TRANSACTION_queryBrowserHistory, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public List<String> getBrowserRestrictionUrls(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    this.mRemote.transact(Stub.TRANSACTION_getBrowserRestrictionUrls, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setFloatTaskDisabled(ComponentName admin, boolean disable) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setFloatTaskDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isFloatTaskDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isFloatTaskDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setAirplanePolices(ComponentName admin, int policy) throws RemoteException {
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
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_setAirplanePolices, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getAirplanePolices(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getAirplanePolices, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setHomeButtonDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setHomeButtonDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isHomeButtonDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isHomeButtonDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setBackButtonDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setBackButtonDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isBackButtonDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isBackButtonDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setSystemBrowserDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setSystemBrowserDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isSystemBrowserDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isSystemBrowserDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean disableClipboard(ComponentName admin, boolean disable) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_disableClipboard, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isClipboardDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isClipboardDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setRequiredStrongAuthTime(ComponentName admin, long timeoutMs) throws RemoteException {
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
                    _data.writeLong(timeoutMs);
                    this.mRemote.transact(Stub.TRANSACTION_setRequiredStrongAuthTime, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public long getRequiredStrongAuthTime(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getRequiredStrongAuthTime, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean allowWifiCellularNetwork(ComponentName compName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_allowWifiCellularNetwork, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getDefaultDataCard(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultDataCard, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public Bundle setDefaultDataCard(ComponentName admin, int slot) throws RemoteException {
                Bundle _result;
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
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultDataCard, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setDataRoamingDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setDataRoamingDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isDataRoamingDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isDataRoamingDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setSlot1DataConnectivityDisabled(ComponentName admin, String value) throws RemoteException {
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
                    _data.writeString(value);
                    this.mRemote.transact(Stub.TRANSACTION_setSlot1DataConnectivityDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setSlot2DataConnectivityDisabled(ComponentName admin, String value) throws RemoteException {
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
                    _data.writeString(value);
                    this.mRemote.transact(Stub.TRANSACTION_setSlot2DataConnectivityDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getSlot1DataConnectivityDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getSlot1DataConnectivityDisabled, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getSlot2DataConnectivityDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_getSlot2DataConnectivityDisabled, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setVoiceOutgoingDisable(ComponentName admin, boolean disable) throws RemoteException {
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
                    _data.writeInt(disable ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setVoiceOutgoingDisable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setVoiceIncomingDisable(ComponentName admin, boolean disable) throws RemoteException {
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
                    _data.writeInt(disable ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setVoiceIncomingDisable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isVoiceOutgoingDisabled(ComponentName admin, int slotId) throws RemoteException {
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
                    _data.writeInt(slotId);
                    this.mRemote.transact(Stub.TRANSACTION_isVoiceOutgoingDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isVoiceIncomingDisabled(ComponentName admin, int slotId) throws RemoteException {
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
                    _data.writeInt(slotId);
                    this.mRemote.transact(Stub.TRANSACTION_isVoiceIncomingDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setUsbTetheringDisable(ComponentName componentName, boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disable ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setUsbTetheringDisable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isUsbTetheringDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isUsbTetheringDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean setSystemUpdatePolicies(ComponentName admin, int mode) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setSystemUpdatePolicies, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public int getSystemUpdatePolicies(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getSystemUpdatePolicies, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setSettingsApplicationDisabled(ComponentName componentName, boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disable ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setSettingsApplicationDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isSettingsApplicationDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isSettingsApplicationDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setNavigationBarDisabled(ComponentName componentName, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setNavigationBarDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isNavigationBarDisabled(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isNavigationBarDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isMultiAppSupport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isMultiAppSupport, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setMultiAppSupport(boolean support) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(support ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setMultiAppSupport, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setPowerSavingModeDisabled(ComponentName admin, boolean disabled) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_setPowerSavingModeDisabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isPowerSavingModeDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isPowerSavingModeDisabled, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public void setApplicationDisabledInLauncherOrRecentTask(List<String> list, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(list);
                    _data.writeInt(flag);
                    this.mRemote.transact(Stub.TRANSACTION_setApplicationDisabledInLauncherOrRecentTask, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public List<String> getApplicationDisabledInLauncherOrRecentTask(int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    this.mRemote.transact(Stub.TRANSACTION_getApplicationDisabledInLauncherOrRecentTask, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
            public boolean isLanguageChangeDisabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(Stub.TRANSACTION_isLanguageChangeDisabled, _data, _reply, 0);
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

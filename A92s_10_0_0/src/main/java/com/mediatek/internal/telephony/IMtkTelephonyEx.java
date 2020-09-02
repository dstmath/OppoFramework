package com.mediatek.internal.telephony;

import android.net.NetworkStats;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.CellInfo;
import android.telephony.RadioAccessFamily;
import android.telephony.ServiceState;
import java.util.List;

public interface IMtkTelephonyEx extends IInterface {
    boolean abortFemtoCellList(int i) throws RemoteException;

    boolean cancelAvailableNetworks(int i) throws RemoteException;

    boolean exitEmergencyCallbackMode(int i) throws RemoteException;

    int[] getAdnStorageInfo(int i) throws RemoteException;

    List<CellInfo> getAllCellInfo(int i, String str) throws RemoteException;

    PseudoCellInfo getApcInfoUsingSlotId(int i) throws RemoteException;

    int getCdmaSubscriptionActStatus(int i) throws RemoteException;

    Bundle getCellLocationUsingSlotId(int i) throws RemoteException;

    int getDisable2G(int i) throws RemoteException;

    List<FemtoCellInfo> getFemtoCellList(int i) throws RemoteException;

    int getIccAppFamily(int i) throws RemoteException;

    String getIccAtr(int i) throws RemoteException;

    String getIccCardType(int i) throws RemoteException;

    boolean getIsLastEccIms() throws RemoteException;

    String getLocatedPlmn(int i) throws RemoteException;

    String getLteAccessStratumState() throws RemoteException;

    int getMainCapabilityPhoneId() throws RemoteException;

    NetworkStats getMobileDataUsage(int i) throws RemoteException;

    String getMvnoMatchType(int i) throws RemoteException;

    String getMvnoPattern(int i, String str) throws RemoteException;

    int getPCO520State(int i) throws RemoteException;

    int[] getRoamingEnable(int i) throws RemoteException;

    int[] getRxTestResult(int i) throws RemoteException;

    int getSelfActivateState(int i) throws RemoteException;

    ServiceState getServiceStateByPhoneId(int i, String str) throws RemoteException;

    int getSimOnOffExecutingState(int i) throws RemoteException;

    int getSimOnOffState(int i) throws RemoteException;

    String[] getSimOperatorNumericForPhoneEx(int i) throws RemoteException;

    String getSimSerialNumber(String str, int i) throws RemoteException;

    String[] getSuggestedPlmnList(int i, int i2, int i3, int i4, String str) throws RemoteException;

    String getUimSubscriberId(String str, int i) throws RemoteException;

    byte[] iccExchangeSimIOEx(int i, int i2, int i3, int i4, int i5, int i6, String str, String str2, String str3) throws RemoteException;

    int invokeOemRilRequestRaw(byte[] bArr, byte[] bArr2) throws RemoteException;

    int invokeOemRilRequestRawBySlot(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean isAppTypeSupported(int i, int i2) throws RemoteException;

    boolean isCapabilitySwitching() throws RemoteException;

    boolean isEmergencyNumber(int i, String str) throws RemoteException;

    boolean isFdnEnabled(int i) throws RemoteException;

    boolean isImsRegistered(int i) throws RemoteException;

    boolean isInCsCall(int i) throws RemoteException;

    boolean isInHomeNetwork(int i) throws RemoteException;

    boolean isPhbReady(int i) throws RemoteException;

    boolean isRadioOffBySimManagement(int i) throws RemoteException;

    boolean isSharedDefaultApn() throws RemoteException;

    boolean isTestIccCard(int i) throws RemoteException;

    boolean isVolteEnabled(int i) throws RemoteException;

    boolean isWifiCallingEnabled(int i) throws RemoteException;

    List<String> loadEFLinearFixedAll(int i, int i2, int i3, String str) throws RemoteException;

    byte[] loadEFTransparent(int i, int i2, int i3, String str) throws RemoteException;

    int queryFemtoCellSystemSelectionMode(int i) throws RemoteException;

    Bundle queryNetworkLock(int i, int i2) throws RemoteException;

    void repollIccStateForNetworkLock(int i, boolean z) throws RemoteException;

    boolean selectFemtoCell(int i, FemtoCellInfo femtoCellInfo) throws RemoteException;

    int selfActivationAction(int i, Bundle bundle, int i2) throws RemoteException;

    void setApcModeUsingSlotId(int i, int i2, boolean z, int i3) throws RemoteException;

    boolean setDisable2G(int i, boolean z) throws RemoteException;

    boolean setFemtoCellSystemSelectionMode(int i, int i2) throws RemoteException;

    void setIsLastEccIms(boolean z) throws RemoteException;

    boolean setLteAccessStratumReport(boolean z) throws RemoteException;

    boolean setLteUplinkDataTransfer(boolean z, int i) throws RemoteException;

    void setMobileDataUsageSum(int i, long j, long j2, long j3, long j4) throws RemoteException;

    boolean setRadioCapability(RadioAccessFamily[] radioAccessFamilyArr) throws RemoteException;

    boolean setRoamingEnable(int i, int[] iArr) throws RemoteException;

    int[] setRxTestConfig(int i, int i2) throws RemoteException;

    int setSimPower(int i, int i2) throws RemoteException;

    boolean setupPdnByType(int i, String str) throws RemoteException;

    byte[] simAkaAuthentication(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    byte[] simGbaAuthBootStrapMode(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    byte[] simGbaAuthNafMode(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    int[] supplyDeviceNetworkDepersonalization(String str) throws RemoteException;

    int supplyNetworkDepersonalization(int i, String str) throws RemoteException;

    boolean tearDownPdnByType(int i, String str) throws RemoteException;

    public static class Default implements IMtkTelephonyEx {
        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isInHomeNetwork(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int getIccAppFamily(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String getIccCardType(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isAppTypeSupported(int slotId, int appType) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isTestIccCard(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String getIccAtr(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public byte[] iccExchangeSimIOEx(int subId, int fileID, int command, int p1, int p2, int p3, String filePath, String data, String pin2) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public byte[] loadEFTransparent(int slotId, int family, int fileID, String filePath) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public List<String> loadEFLinearFixedAll(int slotId, int family, int fileID, String filePath) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int setSimPower(int slotId, int state) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int getSimOnOffState(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int getSimOnOffExecutingState(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public Bundle queryNetworkLock(int subId, int category) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int supplyNetworkDepersonalization(int subId, String strPasswd) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public void repollIccStateForNetworkLock(int subId, boolean needIntent) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String getMvnoMatchType(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String getMvnoPattern(int subId, String type) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public byte[] simAkaAuthentication(int slotId, int family, byte[] byteRand, byte[] byteAutn) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public byte[] simGbaAuthBootStrapMode(int slotId, int family, byte[] byteRand, byte[] byteAutn) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public byte[] simGbaAuthNafMode(int slotId, int family, byte[] byteNafId, byte[] byteImpi) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isRadioOffBySimManagement(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isFdnEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public Bundle getCellLocationUsingSlotId(int slotId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String getUimSubscriberId(String callingPackage, int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String getSimSerialNumber(String callingPackage, int slotId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String[] getSimOperatorNumericForPhoneEx(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean setRadioCapability(RadioAccessFamily[] rafs) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isCapabilitySwitching() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int getMainCapabilityPhoneId() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isImsRegistered(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isVolteEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isWifiCallingEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean setLteAccessStratumReport(boolean enabled) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean setLteUplinkDataTransfer(boolean isOn, int timeMillis) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String getLteAccessStratumState() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isSharedDefaultApn() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int[] getAdnStorageInfo(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isPhbReady(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int[] setRxTestConfig(int phoneId, int config) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int[] getRxTestResult(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int selfActivationAction(int action, Bundle param, int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int getSelfActivateState(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int getPCO520State(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean exitEmergencyCallbackMode(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public void setApcModeUsingSlotId(int slotId, int mode, boolean reportOn, int reportInterval) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public PseudoCellInfo getApcInfoUsingSlotId(int slotId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int getCdmaSubscriptionActStatus(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public void setIsLastEccIms(boolean val) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean getIsLastEccIms() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int invokeOemRilRequestRawBySlot(int slotId, byte[] oemReq, byte[] oemResp) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isInCsCall(int phoneId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public List<CellInfo> getAllCellInfo(int phoneId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String getLocatedPlmn(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean setDisable2G(int phoneId, boolean mode) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int getDisable2G(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public List<FemtoCellInfo> getFemtoCellList(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean abortFemtoCellList(int phoneId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean selectFemtoCell(int phoneId, FemtoCellInfo femtocell) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int queryFemtoCellSystemSelectionMode(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean setFemtoCellSystemSelectionMode(int phoneId, int mode) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean cancelAvailableNetworks(int phoneId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int[] supplyDeviceNetworkDepersonalization(String pwd) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean tearDownPdnByType(int phoneId, String type) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean setupPdnByType(int phoneId, String type) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public ServiceState getServiceStateByPhoneId(int phoneId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean setRoamingEnable(int phoneId, int[] config) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public int[] getRoamingEnable(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public String[] getSuggestedPlmnList(int phoneId, int rat, int num, int timer, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public NetworkStats getMobileDataUsage(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public void setMobileDataUsageSum(int phoneId, long txBytes, long txPkts, long rxBytes, long rxPkts) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
        public boolean isEmergencyNumber(int phoneId, String number) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkTelephonyEx {
        private static final String DESCRIPTOR = "com.mediatek.internal.telephony.IMtkTelephonyEx";
        static final int TRANSACTION_abortFemtoCellList = 58;
        static final int TRANSACTION_cancelAvailableNetworks = 62;
        static final int TRANSACTION_exitEmergencyCallbackMode = 44;
        static final int TRANSACTION_getAdnStorageInfo = 37;
        static final int TRANSACTION_getAllCellInfo = 53;
        static final int TRANSACTION_getApcInfoUsingSlotId = 46;
        static final int TRANSACTION_getCdmaSubscriptionActStatus = 47;
        static final int TRANSACTION_getCellLocationUsingSlotId = 23;
        static final int TRANSACTION_getDisable2G = 56;
        static final int TRANSACTION_getFemtoCellList = 57;
        static final int TRANSACTION_getIccAppFamily = 2;
        static final int TRANSACTION_getIccAtr = 6;
        static final int TRANSACTION_getIccCardType = 3;
        static final int TRANSACTION_getIsLastEccIms = 49;
        static final int TRANSACTION_getLocatedPlmn = 54;
        static final int TRANSACTION_getLteAccessStratumState = 35;
        static final int TRANSACTION_getMainCapabilityPhoneId = 29;
        static final int TRANSACTION_getMobileDataUsage = 70;
        static final int TRANSACTION_getMvnoMatchType = 16;
        static final int TRANSACTION_getMvnoPattern = 17;
        static final int TRANSACTION_getPCO520State = 43;
        static final int TRANSACTION_getRoamingEnable = 68;
        static final int TRANSACTION_getRxTestResult = 40;
        static final int TRANSACTION_getSelfActivateState = 42;
        static final int TRANSACTION_getServiceStateByPhoneId = 66;
        static final int TRANSACTION_getSimOnOffExecutingState = 12;
        static final int TRANSACTION_getSimOnOffState = 11;
        static final int TRANSACTION_getSimOperatorNumericForPhoneEx = 26;
        static final int TRANSACTION_getSimSerialNumber = 25;
        static final int TRANSACTION_getSuggestedPlmnList = 69;
        static final int TRANSACTION_getUimSubscriberId = 24;
        static final int TRANSACTION_iccExchangeSimIOEx = 7;
        static final int TRANSACTION_invokeOemRilRequestRaw = 50;
        static final int TRANSACTION_invokeOemRilRequestRawBySlot = 51;
        static final int TRANSACTION_isAppTypeSupported = 4;
        static final int TRANSACTION_isCapabilitySwitching = 28;
        static final int TRANSACTION_isEmergencyNumber = 72;
        static final int TRANSACTION_isFdnEnabled = 22;
        static final int TRANSACTION_isImsRegistered = 30;
        static final int TRANSACTION_isInCsCall = 52;
        static final int TRANSACTION_isInHomeNetwork = 1;
        static final int TRANSACTION_isPhbReady = 38;
        static final int TRANSACTION_isRadioOffBySimManagement = 21;
        static final int TRANSACTION_isSharedDefaultApn = 36;
        static final int TRANSACTION_isTestIccCard = 5;
        static final int TRANSACTION_isVolteEnabled = 31;
        static final int TRANSACTION_isWifiCallingEnabled = 32;
        static final int TRANSACTION_loadEFLinearFixedAll = 9;
        static final int TRANSACTION_loadEFTransparent = 8;
        static final int TRANSACTION_queryFemtoCellSystemSelectionMode = 60;
        static final int TRANSACTION_queryNetworkLock = 13;
        static final int TRANSACTION_repollIccStateForNetworkLock = 15;
        static final int TRANSACTION_selectFemtoCell = 59;
        static final int TRANSACTION_selfActivationAction = 41;
        static final int TRANSACTION_setApcModeUsingSlotId = 45;
        static final int TRANSACTION_setDisable2G = 55;
        static final int TRANSACTION_setFemtoCellSystemSelectionMode = 61;
        static final int TRANSACTION_setIsLastEccIms = 48;
        static final int TRANSACTION_setLteAccessStratumReport = 33;
        static final int TRANSACTION_setLteUplinkDataTransfer = 34;
        static final int TRANSACTION_setMobileDataUsageSum = 71;
        static final int TRANSACTION_setRadioCapability = 27;
        static final int TRANSACTION_setRoamingEnable = 67;
        static final int TRANSACTION_setRxTestConfig = 39;
        static final int TRANSACTION_setSimPower = 10;
        static final int TRANSACTION_setupPdnByType = 65;
        static final int TRANSACTION_simAkaAuthentication = 18;
        static final int TRANSACTION_simGbaAuthBootStrapMode = 19;
        static final int TRANSACTION_simGbaAuthNafMode = 20;
        static final int TRANSACTION_supplyDeviceNetworkDepersonalization = 63;
        static final int TRANSACTION_supplyNetworkDepersonalization = 14;
        static final int TRANSACTION_tearDownPdnByType = 64;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkTelephonyEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkTelephonyEx)) {
                return new Proxy(obj);
            }
            return (IMtkTelephonyEx) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            byte[] _arg12;
            byte[] _arg2;
            FemtoCellInfo _arg13;
            if (code != 1598968902) {
                boolean _arg14 = false;
                boolean _arg15 = false;
                boolean _arg0 = false;
                boolean _arg22 = false;
                boolean _arg02 = false;
                boolean _arg03 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInHomeNetwork = isInHomeNetwork(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isInHomeNetwork ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getIccAppFamily(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getIccCardType(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAppTypeSupported = isAppTypeSupported(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAppTypeSupported ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTestIccCard = isTestIccCard(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isTestIccCard ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getIccAtr(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result4 = iccExchangeSimIOEx(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result5 = loadEFTransparent(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result5);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result6 = loadEFLinearFixedAll(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result6);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = setSimPower(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getSimOnOffState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getSimOnOffExecutingState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result10 = queryNetworkLock(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = supplyNetworkDepersonalization(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = true;
                        }
                        repollIccStateForNetworkLock(_arg04, _arg14);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = getMvnoMatchType(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _result13 = getMvnoPattern(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result14 = simAkaAuthentication(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result14);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result15 = simGbaAuthBootStrapMode(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result15);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result16 = simGbaAuthNafMode(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result16);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRadioOffBySimManagement = isRadioOffBySimManagement(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isRadioOffBySimManagement ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFdnEnabled = isFdnEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isFdnEnabled ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result17 = getCellLocationUsingSlotId(data.readInt());
                        reply.writeNoException();
                        if (_result17 != null) {
                            reply.writeInt(1);
                            _result17.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = getUimSubscriberId(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    case TRANSACTION_getSimSerialNumber /*{ENCODED_INT: 25}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result19 = getSimSerialNumber(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result19);
                        return true;
                    case TRANSACTION_getSimOperatorNumericForPhoneEx /*{ENCODED_INT: 26}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result20 = getSimOperatorNumericForPhoneEx(data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result20);
                        return true;
                    case TRANSACTION_setRadioCapability /*{ENCODED_INT: 27}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean radioCapability = setRadioCapability((RadioAccessFamily[]) data.createTypedArray(RadioAccessFamily.CREATOR));
                        reply.writeNoException();
                        reply.writeInt(radioCapability ? 1 : 0);
                        return true;
                    case TRANSACTION_isCapabilitySwitching /*{ENCODED_INT: 28}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCapabilitySwitching = isCapabilitySwitching();
                        reply.writeNoException();
                        reply.writeInt(isCapabilitySwitching ? 1 : 0);
                        return true;
                    case TRANSACTION_getMainCapabilityPhoneId /*{ENCODED_INT: 29}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = getMainCapabilityPhoneId();
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case TRANSACTION_isImsRegistered /*{ENCODED_INT: 30}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isImsRegistered = isImsRegistered(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isImsRegistered ? 1 : 0);
                        return true;
                    case TRANSACTION_isVolteEnabled /*{ENCODED_INT: 31}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVolteEnabled = isVolteEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVolteEnabled ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWifiCallingEnabled = isWifiCallingEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isWifiCallingEnabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setLteAccessStratumReport /*{ENCODED_INT: 33}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        boolean lteAccessStratumReport = setLteAccessStratumReport(_arg03);
                        reply.writeNoException();
                        reply.writeInt(lteAccessStratumReport ? 1 : 0);
                        return true;
                    case TRANSACTION_setLteUplinkDataTransfer /*{ENCODED_INT: 34}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean lteUplinkDataTransfer = setLteUplinkDataTransfer(_arg02, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(lteUplinkDataTransfer ? 1 : 0);
                        return true;
                    case TRANSACTION_getLteAccessStratumState /*{ENCODED_INT: 35}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result22 = getLteAccessStratumState();
                        reply.writeNoException();
                        reply.writeString(_result22);
                        return true;
                    case TRANSACTION_isSharedDefaultApn /*{ENCODED_INT: 36}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSharedDefaultApn = isSharedDefaultApn();
                        reply.writeNoException();
                        reply.writeInt(isSharedDefaultApn ? 1 : 0);
                        return true;
                    case TRANSACTION_getAdnStorageInfo /*{ENCODED_INT: 37}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result23 = getAdnStorageInfo(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result23);
                        return true;
                    case TRANSACTION_isPhbReady /*{ENCODED_INT: 38}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPhbReady = isPhbReady(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPhbReady ? 1 : 0);
                        return true;
                    case TRANSACTION_setRxTestConfig /*{ENCODED_INT: 39}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result24 = setRxTestConfig(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result24);
                        return true;
                    case TRANSACTION_getRxTestResult /*{ENCODED_INT: 40}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result25 = getRxTestResult(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result25);
                        return true;
                    case TRANSACTION_selfActivationAction /*{ENCODED_INT: 41}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result26 = selfActivationAction(_arg05, _arg1, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case TRANSACTION_getSelfActivateState /*{ENCODED_INT: 42}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = getSelfActivateState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case TRANSACTION_getPCO520State /*{ENCODED_INT: 43}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = getPCO520State(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case TRANSACTION_exitEmergencyCallbackMode /*{ENCODED_INT: 44}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean exitEmergencyCallbackMode = exitEmergencyCallbackMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(exitEmergencyCallbackMode ? 1 : 0);
                        return true;
                    case TRANSACTION_setApcModeUsingSlotId /*{ENCODED_INT: 45}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setApcModeUsingSlotId(_arg06, _arg16, _arg22, data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getApcInfoUsingSlotId /*{ENCODED_INT: 46}*/:
                        data.enforceInterface(DESCRIPTOR);
                        PseudoCellInfo _result29 = getApcInfoUsingSlotId(data.readInt());
                        reply.writeNoException();
                        if (_result29 != null) {
                            reply.writeInt(1);
                            _result29.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getCdmaSubscriptionActStatus /*{ENCODED_INT: 47}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result30 = getCdmaSubscriptionActStatus(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case TRANSACTION_setIsLastEccIms /*{ENCODED_INT: 48}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setIsLastEccIms(_arg0);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getIsLastEccIms /*{ENCODED_INT: 49}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isLastEccIms = getIsLastEccIms();
                        reply.writeNoException();
                        reply.writeInt(isLastEccIms ? 1 : 0);
                        return true;
                    case TRANSACTION_invokeOemRilRequestRaw /*{ENCODED_INT: 50}*/:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _arg07 = data.createByteArray();
                        int _arg1_length = data.readInt();
                        if (_arg1_length < 0) {
                            _arg12 = null;
                        } else {
                            _arg12 = new byte[_arg1_length];
                        }
                        int _result31 = invokeOemRilRequestRaw(_arg07, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        reply.writeByteArray(_arg12);
                        return true;
                    case TRANSACTION_invokeOemRilRequestRawBySlot /*{ENCODED_INT: 51}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        byte[] _arg17 = data.createByteArray();
                        int _arg2_length = data.readInt();
                        if (_arg2_length < 0) {
                            _arg2 = null;
                        } else {
                            _arg2 = new byte[_arg2_length];
                        }
                        int _result32 = invokeOemRilRequestRawBySlot(_arg08, _arg17, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        reply.writeByteArray(_arg2);
                        return true;
                    case TRANSACTION_isInCsCall /*{ENCODED_INT: 52}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInCsCall = isInCsCall(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isInCsCall ? 1 : 0);
                        return true;
                    case TRANSACTION_getAllCellInfo /*{ENCODED_INT: 53}*/:
                        data.enforceInterface(DESCRIPTOR);
                        List<CellInfo> _result33 = getAllCellInfo(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result33);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        String _result34 = getLocatedPlmn(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result34);
                        return true;
                    case TRANSACTION_setDisable2G /*{ENCODED_INT: 55}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean disable2G = setDisable2G(_arg09, _arg15);
                        reply.writeNoException();
                        reply.writeInt(disable2G ? 1 : 0);
                        return true;
                    case TRANSACTION_getDisable2G /*{ENCODED_INT: 56}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result35 = getDisable2G(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case TRANSACTION_getFemtoCellList /*{ENCODED_INT: 57}*/:
                        data.enforceInterface(DESCRIPTOR);
                        List<FemtoCellInfo> _result36 = getFemtoCellList(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result36);
                        return true;
                    case TRANSACTION_abortFemtoCellList /*{ENCODED_INT: 58}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean abortFemtoCellList = abortFemtoCellList(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(abortFemtoCellList ? 1 : 0);
                        return true;
                    case TRANSACTION_selectFemtoCell /*{ENCODED_INT: 59}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = FemtoCellInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean selectFemtoCell = selectFemtoCell(_arg010, _arg13);
                        reply.writeNoException();
                        reply.writeInt(selectFemtoCell ? 1 : 0);
                        return true;
                    case TRANSACTION_queryFemtoCellSystemSelectionMode /*{ENCODED_INT: 60}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result37 = queryFemtoCellSystemSelectionMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result37);
                        return true;
                    case TRANSACTION_setFemtoCellSystemSelectionMode /*{ENCODED_INT: 61}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean femtoCellSystemSelectionMode = setFemtoCellSystemSelectionMode(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(femtoCellSystemSelectionMode ? 1 : 0);
                        return true;
                    case TRANSACTION_cancelAvailableNetworks /*{ENCODED_INT: 62}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cancelAvailableNetworks = cancelAvailableNetworks(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(cancelAvailableNetworks ? 1 : 0);
                        return true;
                    case TRANSACTION_supplyDeviceNetworkDepersonalization /*{ENCODED_INT: 63}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result38 = supplyDeviceNetworkDepersonalization(data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result38);
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        boolean tearDownPdnByType = tearDownPdnByType(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(tearDownPdnByType ? 1 : 0);
                        return true;
                    case TRANSACTION_setupPdnByType /*{ENCODED_INT: 65}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean z = setupPdnByType(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(z ? 1 : 0);
                        return true;
                    case TRANSACTION_getServiceStateByPhoneId /*{ENCODED_INT: 66}*/:
                        data.enforceInterface(DESCRIPTOR);
                        ServiceState _result39 = getServiceStateByPhoneId(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result39 != null) {
                            reply.writeInt(1);
                            _result39.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_setRoamingEnable /*{ENCODED_INT: 67}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean roamingEnable = setRoamingEnable(data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(roamingEnable ? 1 : 0);
                        return true;
                    case TRANSACTION_getRoamingEnable /*{ENCODED_INT: 68}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result40 = getRoamingEnable(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result40);
                        return true;
                    case TRANSACTION_getSuggestedPlmnList /*{ENCODED_INT: 69}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result41 = getSuggestedPlmnList(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeStringArray(_result41);
                        return true;
                    case TRANSACTION_getMobileDataUsage /*{ENCODED_INT: 70}*/:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkStats _result42 = getMobileDataUsage(data.readInt());
                        reply.writeNoException();
                        if (_result42 != null) {
                            reply.writeInt(1);
                            _result42.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        setMobileDataUsageSum(data.readInt(), data.readLong(), data.readLong(), data.readLong(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isEmergencyNumber /*{ENCODED_INT: 72}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEmergencyNumber = isEmergencyNumber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isEmergencyNumber ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMtkTelephonyEx {
            public static IMtkTelephonyEx sDefaultImpl;
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

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isInHomeNetwork(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInHomeNetwork(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int getIccAppFamily(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIccAppFamily(slotId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String getIccCardType(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIccCardType(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isAppTypeSupported(int slotId, int appType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(appType);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAppTypeSupported(slotId, appType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isTestIccCard(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTestIccCard(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String getIccAtr(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIccAtr(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public byte[] iccExchangeSimIOEx(int subId, int fileID, int command, int p1, int p2, int p3, String filePath, String data, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(fileID);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(command);
                        _data.writeInt(p1);
                        _data.writeInt(p2);
                        _data.writeInt(p3);
                        _data.writeString(filePath);
                        _data.writeString(data);
                        _data.writeString(pin2);
                        if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            byte[] _result = _reply.createByteArray();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        byte[] iccExchangeSimIOEx = Stub.getDefaultImpl().iccExchangeSimIOEx(subId, fileID, command, p1, p2, p3, filePath, data, pin2);
                        _reply.recycle();
                        _data.recycle();
                        return iccExchangeSimIOEx;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public byte[] loadEFTransparent(int slotId, int family, int fileID, String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(family);
                    _data.writeInt(fileID);
                    _data.writeString(filePath);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().loadEFTransparent(slotId, family, fileID, filePath);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public List<String> loadEFLinearFixedAll(int slotId, int family, int fileID, String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(family);
                    _data.writeInt(fileID);
                    _data.writeString(filePath);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().loadEFLinearFixedAll(slotId, family, fileID, filePath);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int setSimPower(int slotId, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(state);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSimPower(slotId, state);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int getSimOnOffState(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimOnOffState(slotId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int getSimOnOffExecutingState(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimOnOffExecutingState(slotId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public Bundle queryNetworkLock(int subId, int category) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(category);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryNetworkLock(subId, category);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int supplyNetworkDepersonalization(int subId, String strPasswd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(strPasswd);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyNetworkDepersonalization(subId, strPasswd);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public void repollIccStateForNetworkLock(int subId, boolean needIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(needIntent ? 1 : 0);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().repollIccStateForNetworkLock(subId, needIntent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String getMvnoMatchType(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMvnoMatchType(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String getMvnoPattern(int subId, String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(type);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMvnoPattern(subId, type);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public byte[] simAkaAuthentication(int slotId, int family, byte[] byteRand, byte[] byteAutn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(family);
                    _data.writeByteArray(byteRand);
                    _data.writeByteArray(byteAutn);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().simAkaAuthentication(slotId, family, byteRand, byteAutn);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public byte[] simGbaAuthBootStrapMode(int slotId, int family, byte[] byteRand, byte[] byteAutn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(family);
                    _data.writeByteArray(byteRand);
                    _data.writeByteArray(byteAutn);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().simGbaAuthBootStrapMode(slotId, family, byteRand, byteAutn);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public byte[] simGbaAuthNafMode(int slotId, int family, byte[] byteNafId, byte[] byteImpi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(family);
                    _data.writeByteArray(byteNafId);
                    _data.writeByteArray(byteImpi);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().simGbaAuthNafMode(slotId, family, byteNafId, byteImpi);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isRadioOffBySimManagement(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRadioOffBySimManagement(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isFdnEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFdnEnabled(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public Bundle getCellLocationUsingSlotId(int slotId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCellLocationUsingSlotId(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String getUimSubscriberId(String callingPackage, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUimSubscriberId(callingPackage, subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String getSimSerialNumber(String callingPackage, int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSimSerialNumber, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimSerialNumber(callingPackage, slotId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String[] getSimOperatorNumericForPhoneEx(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSimOperatorNumericForPhoneEx, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimOperatorNumericForPhoneEx(phoneId);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean setRadioCapability(RadioAccessFamily[] rafs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    _data.writeTypedArray(rafs, 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setRadioCapability, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setRadioCapability(rafs);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isCapabilitySwitching() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isCapabilitySwitching, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCapabilitySwitching();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int getMainCapabilityPhoneId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getMainCapabilityPhoneId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMainCapabilityPhoneId();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isImsRegistered(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isImsRegistered, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isImsRegistered(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isVolteEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isVolteEnabled, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVolteEnabled(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isWifiCallingEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWifiCallingEnabled(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean setLteAccessStratumReport(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enabled ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setLteAccessStratumReport, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setLteAccessStratumReport(enabled);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean setLteUplinkDataTransfer(boolean isOn, int timeMillis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(isOn ? 1 : 0);
                    _data.writeInt(timeMillis);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setLteUplinkDataTransfer, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setLteUplinkDataTransfer(isOn, timeMillis);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String getLteAccessStratumState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getLteAccessStratumState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLteAccessStratumState();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isSharedDefaultApn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isSharedDefaultApn, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSharedDefaultApn();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int[] getAdnStorageInfo(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getAdnStorageInfo, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnStorageInfo(subId);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isPhbReady(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isPhbReady, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPhbReady(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int[] setRxTestConfig(int phoneId, int config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(config);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setRxTestConfig, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setRxTestConfig(phoneId, config);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int[] getRxTestResult(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getRxTestResult, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRxTestResult(phoneId);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int selfActivationAction(int action, Bundle param, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_selfActivationAction, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().selfActivationAction(action, param, subId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int getSelfActivateState(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSelfActivateState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSelfActivateState(subId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int getPCO520State(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getPCO520State, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPCO520State(subId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean exitEmergencyCallbackMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_exitEmergencyCallbackMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().exitEmergencyCallbackMode(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public void setApcModeUsingSlotId(int slotId, int mode, boolean reportOn, int reportInterval) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(mode);
                    _data.writeInt(reportOn ? 1 : 0);
                    _data.writeInt(reportInterval);
                    if (this.mRemote.transact(Stub.TRANSACTION_setApcModeUsingSlotId, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setApcModeUsingSlotId(slotId, mode, reportOn, reportInterval);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public PseudoCellInfo getApcInfoUsingSlotId(int slotId) throws RemoteException {
                PseudoCellInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getApcInfoUsingSlotId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApcInfoUsingSlotId(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PseudoCellInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int getCdmaSubscriptionActStatus(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCdmaSubscriptionActStatus, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaSubscriptionActStatus(subId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public void setIsLastEccIms(boolean val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(val ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_setIsLastEccIms, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIsLastEccIms(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean getIsLastEccIms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_getIsLastEccIms, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsLastEccIms();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(oemReq);
                    if (oemResp == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(oemResp.length);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_invokeOemRilRequestRaw, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().invokeOemRilRequestRaw(oemReq, oemResp);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(oemResp);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int invokeOemRilRequestRawBySlot(int slotId, byte[] oemReq, byte[] oemResp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeByteArray(oemReq);
                    if (oemResp == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(oemResp.length);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_invokeOemRilRequestRawBySlot, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().invokeOemRilRequestRawBySlot(slotId, oemReq, oemResp);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(oemResp);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isInCsCall(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isInCsCall, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInCsCall(phoneId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public List<CellInfo> getAllCellInfo(int phoneId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getAllCellInfo, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllCellInfo(phoneId, callingPackage);
                    }
                    _reply.readException();
                    List<CellInfo> _result = _reply.createTypedArrayList(CellInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String getLocatedPlmn(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLocatedPlmn(phoneId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean setDisable2G(int phoneId, boolean mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = true;
                    _data.writeInt(mode ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setDisable2G, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDisable2G(phoneId, mode);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int getDisable2G(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getDisable2G, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisable2G(phoneId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public List<FemtoCellInfo> getFemtoCellList(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getFemtoCellList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFemtoCellList(phoneId);
                    }
                    _reply.readException();
                    List<FemtoCellInfo> _result = _reply.createTypedArrayList(FemtoCellInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean abortFemtoCellList(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_abortFemtoCellList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().abortFemtoCellList(phoneId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean selectFemtoCell(int phoneId, FemtoCellInfo femtocell) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = true;
                    if (femtocell != null) {
                        _data.writeInt(1);
                        femtocell.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_selectFemtoCell, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().selectFemtoCell(phoneId, femtocell);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int queryFemtoCellSystemSelectionMode(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_queryFemtoCellSystemSelectionMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryFemtoCellSystemSelectionMode(phoneId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean setFemtoCellSystemSelectionMode(int phoneId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(mode);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_setFemtoCellSystemSelectionMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setFemtoCellSystemSelectionMode(phoneId, mode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean cancelAvailableNetworks(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_cancelAvailableNetworks, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancelAvailableNetworks(phoneId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int[] supplyDeviceNetworkDepersonalization(String pwd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pwd);
                    if (!this.mRemote.transact(Stub.TRANSACTION_supplyDeviceNetworkDepersonalization, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyDeviceNetworkDepersonalization(pwd);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean tearDownPdnByType(int phoneId, String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(type);
                    boolean _result = false;
                    if (!this.mRemote.transact(64, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tearDownPdnByType(phoneId, type);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean setupPdnByType(int phoneId, String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(type);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_setupPdnByType, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setupPdnByType(phoneId, type);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public ServiceState getServiceStateByPhoneId(int phoneId, String callingPackage) throws RemoteException {
                ServiceState _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getServiceStateByPhoneId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceStateByPhoneId(phoneId, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ServiceState) ServiceState.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean setRoamingEnable(int phoneId, int[] config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeIntArray(config);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_setRoamingEnable, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setRoamingEnable(phoneId, config);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public int[] getRoamingEnable(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getRoamingEnable, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRoamingEnable(phoneId);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public String[] getSuggestedPlmnList(int phoneId, int rat, int num, int timer, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(rat);
                    _data.writeInt(num);
                    _data.writeInt(timer);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSuggestedPlmnList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSuggestedPlmnList(phoneId, rat, num, timer, callingPackage);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public NetworkStats getMobileDataUsage(int phoneId) throws RemoteException {
                NetworkStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getMobileDataUsage, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMobileDataUsage(phoneId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public void setMobileDataUsageSum(int phoneId, long txBytes, long txPkts, long rxBytes, long rxPkts) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(phoneId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(txBytes);
                        _data.writeLong(txPkts);
                        _data.writeLong(rxBytes);
                        _data.writeLong(rxPkts);
                        if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().setMobileDataUsageSum(phoneId, txBytes, txPkts, rxBytes, rxPkts);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkTelephonyEx
            public boolean isEmergencyNumber(int phoneId, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(number);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isEmergencyNumber, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEmergencyNumber(phoneId, number);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkTelephonyEx impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkTelephonyEx getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

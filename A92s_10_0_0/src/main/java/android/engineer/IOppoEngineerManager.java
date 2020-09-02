package android.engineer;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoEngineerManager extends IInterface {
    boolean disablePartionWriteProtect(boolean z) throws RemoteException;

    boolean fastbootUnlock(byte[] bArr, int i) throws RemoteException;

    String getBackCoverColorId() throws RemoteException;

    byte[] getBadBatteryConfig(int i, int i2) throws RemoteException;

    String getBootImgWaterMark() throws RemoteException;

    byte[] getCalibrationStatusFromNvram() throws RemoteException;

    String getCarrierVersion() throws RemoteException;

    byte[] getCarrierVersionFromNvram() throws RemoteException;

    String getDeviceLockDays() throws RemoteException;

    String getDeviceLockFirstBindTime() throws RemoteException;

    String getDeviceLockICCID() throws RemoteException;

    String getDeviceLockIMSI() throws RemoteException;

    String getDeviceLockLastBindTime() throws RemoteException;

    String getDeviceLockStatus() throws RemoteException;

    String getDeviceLockUnlockTime() throws RemoteException;

    String getDownloadStatus() throws RemoteException;

    byte[] getEmmcHealthInfo() throws RemoteException;

    String getHeytapID(int i) throws RemoteException;

    String getOppoUsageRecords(String str) throws RemoteException;

    byte[] getProductLineTestResult() throws RemoteException;

    String getRegionNetlockStatus() throws RemoteException;

    String getSimOperatorSwitchStatus() throws RemoteException;

    String getSingleDoubleCardStatus() throws RemoteException;

    String getSystemProperties(String str, String str2) throws RemoteException;

    String getTelcelSimlockStatus() throws RemoteException;

    String getTelcelSimlockUnlockTimes() throws RemoteException;

    boolean isEngineerItemInBlackList(int i, String str) throws RemoteException;

    boolean isPartionWriteProtectDisabled() throws RemoteException;

    boolean isSerialPortEnabled() throws RemoteException;

    byte[] readEngineerData(int i) throws RemoteException;

    boolean resetProductLineTestResult() throws RemoteException;

    boolean resetWriteProtectState() throws RemoteException;

    boolean saveCarrierVersionToNvram(byte[] bArr) throws RemoteException;

    boolean saveEngineerData(int i, byte[] bArr, int i2) throws RemoteException;

    boolean saveHeytapID(int i, String str) throws RemoteException;

    boolean saveOppoUsageRecords(String str, String str2, boolean z) throws RemoteException;

    boolean setBackCoverColorId(String str) throws RemoteException;

    int setBatteryBatteryConfig(int i, int i2, byte[] bArr) throws RemoteException;

    boolean setCarrierVersion(String str) throws RemoteException;

    boolean setDeviceLockDays(String str) throws RemoteException;

    boolean setDeviceLockFirstBindTime(String str) throws RemoteException;

    boolean setDeviceLockICCID(String str) throws RemoteException;

    boolean setDeviceLockIMSI(String str) throws RemoteException;

    boolean setDeviceLockLastBindTime(String str) throws RemoteException;

    boolean setDeviceLockStatus(String str) throws RemoteException;

    boolean setDeviceLockUnlockTime(String str) throws RemoteException;

    boolean setProductLineTestResult(int i, int i2) throws RemoteException;

    boolean setRegionNetlock(String str) throws RemoteException;

    boolean setSerialPortState(boolean z) throws RemoteException;

    boolean setSimOperatorSwitch(String str) throws RemoteException;

    boolean setSingleDoubleCard(String str) throws RemoteException;

    void setSystemProperties(String str, String str2) throws RemoteException;

    boolean setTelcelSimlock(String str) throws RemoteException;

    boolean setTelcelSimlockUnlockTimes(String str) throws RemoteException;

    void setTorchState(String str) throws RemoteException;

    void turnBreathLightFlashOn(int i) throws RemoteException;

    void turnBreathLightOff() throws RemoteException;

    void turnBreathLightOn(int i) throws RemoteException;

    void turnButtonLightOff() throws RemoteException;

    void turnButtonLightOn(int i) throws RemoteException;

    public static class Default implements IOppoEngineerManager {
        @Override // android.engineer.IOppoEngineerManager
        public void turnButtonLightOn(int brightness) throws RemoteException {
        }

        @Override // android.engineer.IOppoEngineerManager
        public void turnButtonLightOff() throws RemoteException {
        }

        @Override // android.engineer.IOppoEngineerManager
        public void turnBreathLightOn(int brightness) throws RemoteException {
        }

        @Override // android.engineer.IOppoEngineerManager
        public void turnBreathLightFlashOn(int brightness) throws RemoteException {
        }

        @Override // android.engineer.IOppoEngineerManager
        public void turnBreathLightOff() throws RemoteException {
        }

        @Override // android.engineer.IOppoEngineerManager
        public void setTorchState(String state) throws RemoteException {
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getDownloadStatus() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean isSerialPortEnabled() throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setSerialPortState(boolean enable) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public byte[] getEmmcHealthInfo() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean isPartionWriteProtectDisabled() throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean disablePartionWriteProtect(boolean disable) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean resetWriteProtectState() throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getBackCoverColorId() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setBackCoverColorId(String colorId) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getCarrierVersion() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setCarrierVersion(String version) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getRegionNetlockStatus() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setRegionNetlock(String lock) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getTelcelSimlockStatus() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setTelcelSimlock(String lock) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getTelcelSimlockUnlockTimes() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setTelcelSimlockUnlockTimes(String times) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getSingleDoubleCardStatus() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setSingleDoubleCard(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public byte[] getBadBatteryConfig(int offset, int size) throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public int setBatteryBatteryConfig(int offset, int size, byte[] data) throws RemoteException {
            return 0;
        }

        @Override // android.engineer.IOppoEngineerManager
        public byte[] getProductLineTestResult() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setProductLineTestResult(int position, int result) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean resetProductLineTestResult() throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public byte[] getCarrierVersionFromNvram() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean saveCarrierVersionToNvram(byte[] version) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public byte[] getCalibrationStatusFromNvram() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setSimOperatorSwitch(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getSimOperatorSwitchStatus() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setDeviceLockStatus(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getDeviceLockStatus() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setDeviceLockIMSI(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getDeviceLockIMSI() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setDeviceLockDays(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getDeviceLockDays() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setDeviceLockLastBindTime(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getDeviceLockLastBindTime() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setDeviceLockICCID(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getDeviceLockICCID() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setDeviceLockFirstBindTime(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getDeviceLockFirstBindTime() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean setDeviceLockUnlockTime(String state) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getDeviceLockUnlockTime() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getBootImgWaterMark() throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public byte[] readEngineerData(int type) throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean saveEngineerData(int type, byte[] engineerData, int length) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean fastbootUnlock(byte[] data, int length) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public void setSystemProperties(String key, String val) throws RemoteException {
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getSystemProperties(String key, String val) throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean saveOppoUsageRecords(String path, String usageRecord, boolean isSingleRecord) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean isEngineerItemInBlackList(int type, String item) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getOppoUsageRecords(String path) throws RemoteException {
            return null;
        }

        @Override // android.engineer.IOppoEngineerManager
        public boolean saveHeytapID(int type, String id) throws RemoteException {
            return false;
        }

        @Override // android.engineer.IOppoEngineerManager
        public String getHeytapID(int type) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoEngineerManager {
        private static final String DESCRIPTOR = "android.engineer.IOppoEngineerManager";
        static final int TRANSACTION_disablePartionWriteProtect = 12;
        static final int TRANSACTION_fastbootUnlock = 53;
        static final int TRANSACTION_getBackCoverColorId = 14;
        static final int TRANSACTION_getBadBatteryConfig = 26;
        static final int TRANSACTION_getBootImgWaterMark = 50;
        static final int TRANSACTION_getCalibrationStatusFromNvram = 33;
        static final int TRANSACTION_getCarrierVersion = 16;
        static final int TRANSACTION_getCarrierVersionFromNvram = 31;
        static final int TRANSACTION_getDeviceLockDays = 41;
        static final int TRANSACTION_getDeviceLockFirstBindTime = 47;
        static final int TRANSACTION_getDeviceLockICCID = 45;
        static final int TRANSACTION_getDeviceLockIMSI = 39;
        static final int TRANSACTION_getDeviceLockLastBindTime = 43;
        static final int TRANSACTION_getDeviceLockStatus = 37;
        static final int TRANSACTION_getDeviceLockUnlockTime = 49;
        static final int TRANSACTION_getDownloadStatus = 7;
        static final int TRANSACTION_getEmmcHealthInfo = 10;
        static final int TRANSACTION_getHeytapID = 60;
        static final int TRANSACTION_getOppoUsageRecords = 58;
        static final int TRANSACTION_getProductLineTestResult = 28;
        static final int TRANSACTION_getRegionNetlockStatus = 18;
        static final int TRANSACTION_getSimOperatorSwitchStatus = 35;
        static final int TRANSACTION_getSingleDoubleCardStatus = 24;
        static final int TRANSACTION_getSystemProperties = 55;
        static final int TRANSACTION_getTelcelSimlockStatus = 20;
        static final int TRANSACTION_getTelcelSimlockUnlockTimes = 22;
        static final int TRANSACTION_isEngineerItemInBlackList = 57;
        static final int TRANSACTION_isPartionWriteProtectDisabled = 11;
        static final int TRANSACTION_isSerialPortEnabled = 8;
        static final int TRANSACTION_readEngineerData = 51;
        static final int TRANSACTION_resetProductLineTestResult = 30;
        static final int TRANSACTION_resetWriteProtectState = 13;
        static final int TRANSACTION_saveCarrierVersionToNvram = 32;
        static final int TRANSACTION_saveEngineerData = 52;
        static final int TRANSACTION_saveHeytapID = 59;
        static final int TRANSACTION_saveOppoUsageRecords = 56;
        static final int TRANSACTION_setBackCoverColorId = 15;
        static final int TRANSACTION_setBatteryBatteryConfig = 27;
        static final int TRANSACTION_setCarrierVersion = 17;
        static final int TRANSACTION_setDeviceLockDays = 40;
        static final int TRANSACTION_setDeviceLockFirstBindTime = 46;
        static final int TRANSACTION_setDeviceLockICCID = 44;
        static final int TRANSACTION_setDeviceLockIMSI = 38;
        static final int TRANSACTION_setDeviceLockLastBindTime = 42;
        static final int TRANSACTION_setDeviceLockStatus = 36;
        static final int TRANSACTION_setDeviceLockUnlockTime = 48;
        static final int TRANSACTION_setProductLineTestResult = 29;
        static final int TRANSACTION_setRegionNetlock = 19;
        static final int TRANSACTION_setSerialPortState = 9;
        static final int TRANSACTION_setSimOperatorSwitch = 34;
        static final int TRANSACTION_setSingleDoubleCard = 25;
        static final int TRANSACTION_setSystemProperties = 54;
        static final int TRANSACTION_setTelcelSimlock = 21;
        static final int TRANSACTION_setTelcelSimlockUnlockTimes = 23;
        static final int TRANSACTION_setTorchState = 6;
        static final int TRANSACTION_turnBreathLightFlashOn = 4;
        static final int TRANSACTION_turnBreathLightOff = 5;
        static final int TRANSACTION_turnBreathLightOn = 3;
        static final int TRANSACTION_turnButtonLightOff = 2;
        static final int TRANSACTION_turnButtonLightOn = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoEngineerManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoEngineerManager)) {
                return new Proxy(obj);
            }
            return (IOppoEngineerManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "turnButtonLightOn";
                case 2:
                    return "turnButtonLightOff";
                case 3:
                    return "turnBreathLightOn";
                case 4:
                    return "turnBreathLightFlashOn";
                case 5:
                    return "turnBreathLightOff";
                case 6:
                    return "setTorchState";
                case 7:
                    return "getDownloadStatus";
                case 8:
                    return "isSerialPortEnabled";
                case 9:
                    return "setSerialPortState";
                case 10:
                    return "getEmmcHealthInfo";
                case 11:
                    return "isPartionWriteProtectDisabled";
                case 12:
                    return "disablePartionWriteProtect";
                case 13:
                    return "resetWriteProtectState";
                case 14:
                    return "getBackCoverColorId";
                case 15:
                    return "setBackCoverColorId";
                case 16:
                    return "getCarrierVersion";
                case 17:
                    return "setCarrierVersion";
                case 18:
                    return "getRegionNetlockStatus";
                case 19:
                    return "setRegionNetlock";
                case 20:
                    return "getTelcelSimlockStatus";
                case 21:
                    return "setTelcelSimlock";
                case 22:
                    return "getTelcelSimlockUnlockTimes";
                case 23:
                    return "setTelcelSimlockUnlockTimes";
                case 24:
                    return "getSingleDoubleCardStatus";
                case 25:
                    return "setSingleDoubleCard";
                case 26:
                    return "getBadBatteryConfig";
                case 27:
                    return "setBatteryBatteryConfig";
                case 28:
                    return "getProductLineTestResult";
                case 29:
                    return "setProductLineTestResult";
                case 30:
                    return "resetProductLineTestResult";
                case 31:
                    return "getCarrierVersionFromNvram";
                case 32:
                    return "saveCarrierVersionToNvram";
                case 33:
                    return "getCalibrationStatusFromNvram";
                case 34:
                    return "setSimOperatorSwitch";
                case 35:
                    return "getSimOperatorSwitchStatus";
                case 36:
                    return "setDeviceLockStatus";
                case 37:
                    return "getDeviceLockStatus";
                case 38:
                    return "setDeviceLockIMSI";
                case 39:
                    return "getDeviceLockIMSI";
                case 40:
                    return "setDeviceLockDays";
                case 41:
                    return "getDeviceLockDays";
                case 42:
                    return "setDeviceLockLastBindTime";
                case 43:
                    return "getDeviceLockLastBindTime";
                case 44:
                    return "setDeviceLockICCID";
                case 45:
                    return "getDeviceLockICCID";
                case 46:
                    return "setDeviceLockFirstBindTime";
                case 47:
                    return "getDeviceLockFirstBindTime";
                case 48:
                    return "setDeviceLockUnlockTime";
                case 49:
                    return "getDeviceLockUnlockTime";
                case 50:
                    return "getBootImgWaterMark";
                case 51:
                    return "readEngineerData";
                case 52:
                    return "saveEngineerData";
                case 53:
                    return "fastbootUnlock";
                case 54:
                    return "setSystemProperties";
                case 55:
                    return "getSystemProperties";
                case 56:
                    return "saveOppoUsageRecords";
                case 57:
                    return "isEngineerItemInBlackList";
                case 58:
                    return "getOppoUsageRecords";
                case 59:
                    return "saveHeytapID";
                case 60:
                    return "getHeytapID";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                boolean _arg2 = false;
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        turnButtonLightOn(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        turnButtonLightOff();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        turnBreathLightOn(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        turnBreathLightFlashOn(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        turnBreathLightOff();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setTorchState(data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getDownloadStatus();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSerialPortEnabled = isSerialPortEnabled();
                        reply.writeNoException();
                        reply.writeInt(isSerialPortEnabled ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        boolean serialPortState = setSerialPortState(_arg0);
                        reply.writeNoException();
                        reply.writeInt(serialPortState ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = getEmmcHealthInfo();
                        reply.writeNoException();
                        reply.writeByteArray(_result2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPartionWriteProtectDisabled = isPartionWriteProtectDisabled();
                        reply.writeNoException();
                        reply.writeInt(isPartionWriteProtectDisabled ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean disablePartionWriteProtect = disablePartionWriteProtect(_arg02);
                        reply.writeNoException();
                        reply.writeInt(disablePartionWriteProtect ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean resetWriteProtectState = resetWriteProtectState();
                        reply.writeNoException();
                        reply.writeInt(resetWriteProtectState ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getBackCoverColorId();
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean backCoverColorId = setBackCoverColorId(data.readString());
                        reply.writeNoException();
                        reply.writeInt(backCoverColorId ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getCarrierVersion();
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean carrierVersion = setCarrierVersion(data.readString());
                        reply.writeNoException();
                        reply.writeInt(carrierVersion ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getRegionNetlockStatus();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean regionNetlock = setRegionNetlock(data.readString());
                        reply.writeNoException();
                        reply.writeInt(regionNetlock ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getTelcelSimlockStatus();
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean telcelSimlock = setTelcelSimlock(data.readString());
                        reply.writeNoException();
                        reply.writeInt(telcelSimlock ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = getTelcelSimlockUnlockTimes();
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean telcelSimlockUnlockTimes = setTelcelSimlockUnlockTimes(data.readString());
                        reply.writeNoException();
                        reply.writeInt(telcelSimlockUnlockTimes ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _result8 = getSingleDoubleCardStatus();
                        reply.writeNoException();
                        reply.writeString(_result8);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        boolean singleDoubleCard = setSingleDoubleCard(data.readString());
                        reply.writeNoException();
                        reply.writeInt(singleDoubleCard ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result9 = getBadBatteryConfig(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result9);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = setBatteryBatteryConfig(data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result11 = getProductLineTestResult();
                        reply.writeNoException();
                        reply.writeByteArray(_result11);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        boolean productLineTestResult = setProductLineTestResult(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(productLineTestResult ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean resetProductLineTestResult = resetProductLineTestResult();
                        reply.writeNoException();
                        reply.writeInt(resetProductLineTestResult ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result12 = getCarrierVersionFromNvram();
                        reply.writeNoException();
                        reply.writeByteArray(_result12);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean saveCarrierVersionToNvram = saveCarrierVersionToNvram(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(saveCarrierVersionToNvram ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result13 = getCalibrationStatusFromNvram();
                        reply.writeNoException();
                        reply.writeByteArray(_result13);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        boolean simOperatorSwitch = setSimOperatorSwitch(data.readString());
                        reply.writeNoException();
                        reply.writeInt(simOperatorSwitch ? 1 : 0);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        String _result14 = getSimOperatorSwitchStatus();
                        reply.writeNoException();
                        reply.writeString(_result14);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceLockStatus = setDeviceLockStatus(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deviceLockStatus ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        String _result15 = getDeviceLockStatus();
                        reply.writeNoException();
                        reply.writeString(_result15);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceLockIMSI = setDeviceLockIMSI(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deviceLockIMSI ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        String _result16 = getDeviceLockIMSI();
                        reply.writeNoException();
                        reply.writeString(_result16);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceLockDays = setDeviceLockDays(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deviceLockDays ? 1 : 0);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        String _result17 = getDeviceLockDays();
                        reply.writeNoException();
                        reply.writeString(_result17);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceLockLastBindTime = setDeviceLockLastBindTime(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deviceLockLastBindTime ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = getDeviceLockLastBindTime();
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceLockICCID = setDeviceLockICCID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deviceLockICCID ? 1 : 0);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        String _result19 = getDeviceLockICCID();
                        reply.writeNoException();
                        reply.writeString(_result19);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceLockFirstBindTime = setDeviceLockFirstBindTime(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deviceLockFirstBindTime ? 1 : 0);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        String _result20 = getDeviceLockFirstBindTime();
                        reply.writeNoException();
                        reply.writeString(_result20);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deviceLockUnlockTime = setDeviceLockUnlockTime(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deviceLockUnlockTime ? 1 : 0);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getDeviceLockUnlockTime();
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        String _result22 = getBootImgWaterMark();
                        reply.writeNoException();
                        reply.writeString(_result22);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result23 = readEngineerData(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result23);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        boolean saveEngineerData = saveEngineerData(data.readInt(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(saveEngineerData ? 1 : 0);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        boolean fastbootUnlock = fastbootUnlock(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(fastbootUnlock ? 1 : 0);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        setSystemProperties(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        String _result24 = getSystemProperties(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result24);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean saveOppoUsageRecords = saveOppoUsageRecords(_arg03, _arg1, _arg2);
                        reply.writeNoException();
                        reply.writeInt(saveOppoUsageRecords ? 1 : 0);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEngineerItemInBlackList = isEngineerItemInBlackList(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isEngineerItemInBlackList ? 1 : 0);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        String _result25 = getOppoUsageRecords(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result25);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        boolean saveHeytapID = saveHeytapID(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(saveHeytapID ? 1 : 0);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        String _result26 = getHeytapID(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result26);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IOppoEngineerManager {
            public static IOppoEngineerManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.engineer.IOppoEngineerManager
            public void turnButtonLightOn(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().turnButtonLightOn(brightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.engineer.IOppoEngineerManager
            public void turnButtonLightOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().turnButtonLightOff();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.engineer.IOppoEngineerManager
            public void turnBreathLightOn(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().turnBreathLightOn(brightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.engineer.IOppoEngineerManager
            public void turnBreathLightFlashOn(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().turnBreathLightFlashOn(brightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.engineer.IOppoEngineerManager
            public void turnBreathLightOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().turnBreathLightOff();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.engineer.IOppoEngineerManager
            public void setTorchState(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTorchState(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.engineer.IOppoEngineerManager
            public String getDownloadStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDownloadStatus();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean isSerialPortEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSerialPortEnabled();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setSerialPortState(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSerialPortState(enable);
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

            @Override // android.engineer.IOppoEngineerManager
            public byte[] getEmmcHealthInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEmmcHealthInfo();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean isPartionWriteProtectDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPartionWriteProtectDisabled();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean disablePartionWriteProtect(boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(disable ? 1 : 0);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disablePartionWriteProtect(disable);
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean resetWriteProtectState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resetWriteProtectState();
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

            @Override // android.engineer.IOppoEngineerManager
            public String getBackCoverColorId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBackCoverColorId();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setBackCoverColorId(String colorId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(colorId);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setBackCoverColorId(colorId);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getCarrierVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierVersion();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setCarrierVersion(String version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(version);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCarrierVersion(version);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getRegionNetlockStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRegionNetlockStatus();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setRegionNetlock(String lock) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(lock);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setRegionNetlock(lock);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getTelcelSimlockStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTelcelSimlockStatus();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setTelcelSimlock(String lock) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(lock);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setTelcelSimlock(lock);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getTelcelSimlockUnlockTimes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTelcelSimlockUnlockTimes();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setTelcelSimlockUnlockTimes(String times) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(times);
                    boolean _result = false;
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setTelcelSimlockUnlockTimes(times);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getSingleDoubleCardStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSingleDoubleCardStatus();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setSingleDoubleCard(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSingleDoubleCard(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public byte[] getBadBatteryConfig(int offset, int size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(offset);
                    _data.writeInt(size);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBadBatteryConfig(offset, size);
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

            @Override // android.engineer.IOppoEngineerManager
            public int setBatteryBatteryConfig(int offset, int size, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(offset);
                    _data.writeInt(size);
                    _data.writeByteArray(data);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setBatteryBatteryConfig(offset, size, data);
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

            @Override // android.engineer.IOppoEngineerManager
            public byte[] getProductLineTestResult() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProductLineTestResult();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setProductLineTestResult(int position, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(position);
                    _data.writeInt(result);
                    boolean _result = false;
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setProductLineTestResult(position, result);
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean resetProductLineTestResult() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resetProductLineTestResult();
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

            @Override // android.engineer.IOppoEngineerManager
            public byte[] getCarrierVersionFromNvram() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierVersionFromNvram();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean saveCarrierVersionToNvram(byte[] version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(version);
                    boolean _result = false;
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saveCarrierVersionToNvram(version);
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

            @Override // android.engineer.IOppoEngineerManager
            public byte[] getCalibrationStatusFromNvram() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCalibrationStatusFromNvram();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setSimOperatorSwitch(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSimOperatorSwitch(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getSimOperatorSwitchStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimOperatorSwitchStatus();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setDeviceLockStatus(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceLockStatus(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getDeviceLockStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceLockStatus();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setDeviceLockIMSI(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceLockIMSI(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getDeviceLockIMSI() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceLockIMSI();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setDeviceLockDays(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceLockDays(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getDeviceLockDays() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceLockDays();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setDeviceLockLastBindTime(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceLockLastBindTime(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getDeviceLockLastBindTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceLockLastBindTime();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setDeviceLockICCID(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceLockICCID(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getDeviceLockICCID() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceLockICCID();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setDeviceLockFirstBindTime(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceLockFirstBindTime(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getDeviceLockFirstBindTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceLockFirstBindTime();
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean setDeviceLockUnlockTime(String state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceLockUnlockTime(state);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getDeviceLockUnlockTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceLockUnlockTime();
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

            @Override // android.engineer.IOppoEngineerManager
            public String getBootImgWaterMark() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBootImgWaterMark();
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

            @Override // android.engineer.IOppoEngineerManager
            public byte[] readEngineerData(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readEngineerData(type);
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean saveEngineerData(int type, byte[] engineerData, int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeByteArray(engineerData);
                    _data.writeInt(length);
                    boolean _result = false;
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saveEngineerData(type, engineerData, length);
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean fastbootUnlock(byte[] data, int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    _data.writeInt(length);
                    boolean _result = false;
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().fastbootUnlock(data, length);
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

            @Override // android.engineer.IOppoEngineerManager
            public void setSystemProperties(String key, String val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(val);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSystemProperties(key, val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.engineer.IOppoEngineerManager
            public String getSystemProperties(String key, String val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(val);
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemProperties(key, val);
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean saveOppoUsageRecords(String path, String usageRecord, boolean isSingleRecord) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeString(usageRecord);
                    boolean _result = true;
                    _data.writeInt(isSingleRecord ? 1 : 0);
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saveOppoUsageRecords(path, usageRecord, isSingleRecord);
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean isEngineerItemInBlackList(int type, String item) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(item);
                    boolean _result = false;
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEngineerItemInBlackList(type, item);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getOppoUsageRecords(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOppoUsageRecords(path);
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

            @Override // android.engineer.IOppoEngineerManager
            public boolean saveHeytapID(int type, String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(id);
                    boolean _result = false;
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saveHeytapID(type, id);
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

            @Override // android.engineer.IOppoEngineerManager
            public String getHeytapID(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHeytapID(type);
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
        }

        public static boolean setDefaultImpl(IOppoEngineerManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoEngineerManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

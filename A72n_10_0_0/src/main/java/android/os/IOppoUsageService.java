package android.os;

import java.util.List;

public interface IOppoUsageService extends IInterface {
    boolean accumulateDialOutDuration(int i) throws RemoteException;

    boolean accumulateHistoryCountOfReceivedMsg(int i) throws RemoteException;

    boolean accumulateHistoryCountOfSendedMsg(int i) throws RemoteException;

    boolean accumulateInComingCallDuration(int i) throws RemoteException;

    boolean deleteOppoFile(String str) throws RemoteException;

    byte[] engineerReadDevBlock(String str, int i, int i2) throws RemoteException;

    int engineerWriteDevBlock(String str, byte[] bArr, int i) throws RemoteException;

    int getApkDeleteEventRecordCount() throws RemoteException;

    List<String> getApkDeleteEventRecords(int i, int i2) throws RemoteException;

    int getApkInstallEventRecordCount() throws RemoteException;

    List<String> getApkInstallEventRecords(int i, int i2) throws RemoteException;

    List<String> getAppUsageCountHistoryRecords(int i, int i2) throws RemoteException;

    int getAppUsageHistoryRecordCount() throws RemoteException;

    List<String> getAppUsageHistoryRecords(int i, int i2) throws RemoteException;

    List<String> getDialCountHistoryRecords(int i, int i2) throws RemoteException;

    int getDialOutDuration() throws RemoteException;

    String getDownloadStatusString(int i) throws RemoteException;

    int getFileSize(String str) throws RemoteException;

    List<String> getHistoryBootTime() throws RemoteException;

    int getHistoryCountOfReceivedMsg() throws RemoteException;

    int getHistoryCountOfSendedMsg() throws RemoteException;

    List<String> getHistoryImeiNO() throws RemoteException;

    List<String> getHistoryPcbaNO() throws RemoteException;

    int getHistoryRecordsCountOfPhoneCalls() throws RemoteException;

    int getInComingCallDuration() throws RemoteException;

    int getMaxChargeCurrent() throws RemoteException;

    int getMaxChargeTemperature() throws RemoteException;

    String getMcsConnectID() throws RemoteException;

    int getMinChargeTemperature() throws RemoteException;

    List<String> getOriginalSimcardData() throws RemoteException;

    List<String> getPhoneCallHistoryRecords(int i, int i2) throws RemoteException;

    int getProductLineLastTestFlag() throws RemoteException;

    String loadSecrecyConfig() throws RemoteException;

    boolean readEntireOppoDir(String str, String str2, boolean z) throws RemoteException;

    boolean readEntireOppoFile(String str, String str2, boolean z) throws RemoteException;

    byte[] readOppoFile(String str, int i, int i2) throws RemoteException;

    boolean recordApkDeleteEvent(String str, String str2, String str3) throws RemoteException;

    boolean recordApkInstallEvent(String str, String str2, String str3) throws RemoteException;

    boolean recordMcsConnectID(String str) throws RemoteException;

    boolean saveEntireOppoDir(String str, String str2, boolean z) throws RemoteException;

    int saveEntireOppoFile(String str, String str2, boolean z) throws RemoteException;

    int saveOppoFile(int i, String str, int i2, boolean z, int i3, byte[] bArr) throws RemoteException;

    int saveSecrecyConfig(String str) throws RemoteException;

    boolean setProductLineLastTestFlag(int i) throws RemoteException;

    void shutDown() throws RemoteException;

    void testSaveSomeData(int i, String str) throws RemoteException;

    boolean updateMaxChargeCurrent(int i) throws RemoteException;

    boolean updateMaxChargeTemperature(int i) throws RemoteException;

    boolean updateMinChargeTemperature(int i) throws RemoteException;

    boolean writeAppUsageHistoryRecord(String str, String str2) throws RemoteException;

    boolean writePhoneCallHistoryRecord(String str, String str2) throws RemoteException;

    public static class Default implements IOppoUsageService {
        @Override // android.os.IOppoUsageService
        public void testSaveSomeData(int dataType, String dataContent) throws RemoteException {
        }

        @Override // android.os.IOppoUsageService
        public List<String> getHistoryBootTime() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getOriginalSimcardData() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getHistoryImeiNO() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getHistoryPcbaNO() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public int getAppUsageHistoryRecordCount() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getAppUsageHistoryRecords(int startIndex, int endIndex) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getAppUsageCountHistoryRecords(int startIndex, int endIndex) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getDialCountHistoryRecords(int startIndex, int endIndex) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public boolean writeAppUsageHistoryRecord(String appName, String dateTime) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public int getHistoryCountOfSendedMsg() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public int getHistoryCountOfReceivedMsg() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public boolean accumulateHistoryCountOfSendedMsg(int newCountIncrease) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public boolean accumulateHistoryCountOfReceivedMsg(int newCountIncrease) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public int getDialOutDuration() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public int getInComingCallDuration() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public boolean accumulateDialOutDuration(int durationInMinute) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public boolean accumulateInComingCallDuration(int durationInMinute) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public int getHistoryRecordsCountOfPhoneCalls() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getPhoneCallHistoryRecords(int startIndex, int endIndex) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public boolean writePhoneCallHistoryRecord(String phoneNoStr, String dateTime) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public void shutDown() throws RemoteException {
        }

        @Override // android.os.IOppoUsageService
        public int getMinChargeTemperature() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public int getMaxChargeTemperature() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public int getMaxChargeCurrent() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public boolean updateMinChargeTemperature(int temp) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public boolean updateMaxChargeTemperature(int temp) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public boolean updateMaxChargeCurrent(int current) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public byte[] engineerReadDevBlock(String partion, int offset, int count) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public int engineerWriteDevBlock(String partion, byte[] content, int offset) throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public String getDownloadStatusString(int part) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public int saveSecrecyConfig(String content) throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public String loadSecrecyConfig() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public boolean setProductLineLastTestFlag(int flag) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public int getProductLineLastTestFlag() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public boolean recordApkDeleteEvent(String deleteAppPkgName, String callerAppPkgName, String dateTime) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public int getApkDeleteEventRecordCount() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getApkDeleteEventRecords(int startIndex, int endIndex) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public boolean recordApkInstallEvent(String installAppPkgName, String callerAppPkgName, String dateTime) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public int getApkInstallEventRecordCount() throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public List<String> getApkInstallEventRecords(int startIndex, int endIndex) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public boolean recordMcsConnectID(String connectID) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public String getMcsConnectID() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public int getFileSize(String path) throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public byte[] readOppoFile(String path, int startPosition, int length) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoUsageService
        public int saveOppoFile(int fileMax, String path, int offset, boolean append, int length, byte[] data) throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public int saveEntireOppoFile(String sourcePath, String targetPath, boolean deleteSource) throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoUsageService
        public boolean readEntireOppoFile(String sourcePath, String targetPath, boolean deleteSource) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public boolean deleteOppoFile(String filePath) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public boolean saveEntireOppoDir(String sourceDir, String targetDir, boolean deleteSource) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoUsageService
        public boolean readEntireOppoDir(String sourceDir, String targetDir, boolean deleteSource) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoUsageService {
        private static final String DESCRIPTOR = "android.os.IOppoUsageService";
        static final int TRANSACTION_accumulateDialOutDuration = 17;
        static final int TRANSACTION_accumulateHistoryCountOfReceivedMsg = 14;
        static final int TRANSACTION_accumulateHistoryCountOfSendedMsg = 13;
        static final int TRANSACTION_accumulateInComingCallDuration = 18;
        static final int TRANSACTION_deleteOppoFile = 49;
        static final int TRANSACTION_engineerReadDevBlock = 29;
        static final int TRANSACTION_engineerWriteDevBlock = 30;
        static final int TRANSACTION_getApkDeleteEventRecordCount = 37;
        static final int TRANSACTION_getApkDeleteEventRecords = 38;
        static final int TRANSACTION_getApkInstallEventRecordCount = 40;
        static final int TRANSACTION_getApkInstallEventRecords = 41;
        static final int TRANSACTION_getAppUsageCountHistoryRecords = 8;
        static final int TRANSACTION_getAppUsageHistoryRecordCount = 6;
        static final int TRANSACTION_getAppUsageHistoryRecords = 7;
        static final int TRANSACTION_getDialCountHistoryRecords = 9;
        static final int TRANSACTION_getDialOutDuration = 15;
        static final int TRANSACTION_getDownloadStatusString = 31;
        static final int TRANSACTION_getFileSize = 44;
        static final int TRANSACTION_getHistoryBootTime = 2;
        static final int TRANSACTION_getHistoryCountOfReceivedMsg = 12;
        static final int TRANSACTION_getHistoryCountOfSendedMsg = 11;
        static final int TRANSACTION_getHistoryImeiNO = 4;
        static final int TRANSACTION_getHistoryPcbaNO = 5;
        static final int TRANSACTION_getHistoryRecordsCountOfPhoneCalls = 19;
        static final int TRANSACTION_getInComingCallDuration = 16;
        static final int TRANSACTION_getMaxChargeCurrent = 25;
        static final int TRANSACTION_getMaxChargeTemperature = 24;
        static final int TRANSACTION_getMcsConnectID = 43;
        static final int TRANSACTION_getMinChargeTemperature = 23;
        static final int TRANSACTION_getOriginalSimcardData = 3;
        static final int TRANSACTION_getPhoneCallHistoryRecords = 20;
        static final int TRANSACTION_getProductLineLastTestFlag = 35;
        static final int TRANSACTION_loadSecrecyConfig = 33;
        static final int TRANSACTION_readEntireOppoDir = 51;
        static final int TRANSACTION_readEntireOppoFile = 48;
        static final int TRANSACTION_readOppoFile = 45;
        static final int TRANSACTION_recordApkDeleteEvent = 36;
        static final int TRANSACTION_recordApkInstallEvent = 39;
        static final int TRANSACTION_recordMcsConnectID = 42;
        static final int TRANSACTION_saveEntireOppoDir = 50;
        static final int TRANSACTION_saveEntireOppoFile = 47;
        static final int TRANSACTION_saveOppoFile = 46;
        static final int TRANSACTION_saveSecrecyConfig = 32;
        static final int TRANSACTION_setProductLineLastTestFlag = 34;
        static final int TRANSACTION_shutDown = 22;
        static final int TRANSACTION_testSaveSomeData = 1;
        static final int TRANSACTION_updateMaxChargeCurrent = 28;
        static final int TRANSACTION_updateMaxChargeTemperature = 27;
        static final int TRANSACTION_updateMinChargeTemperature = 26;
        static final int TRANSACTION_writeAppUsageHistoryRecord = 10;
        static final int TRANSACTION_writePhoneCallHistoryRecord = 21;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoUsageService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoUsageService)) {
                return new Proxy(obj);
            }
            return (IOppoUsageService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "testSaveSomeData";
                case 2:
                    return "getHistoryBootTime";
                case 3:
                    return "getOriginalSimcardData";
                case 4:
                    return "getHistoryImeiNO";
                case 5:
                    return "getHistoryPcbaNO";
                case 6:
                    return "getAppUsageHistoryRecordCount";
                case 7:
                    return "getAppUsageHistoryRecords";
                case 8:
                    return "getAppUsageCountHistoryRecords";
                case 9:
                    return "getDialCountHistoryRecords";
                case 10:
                    return "writeAppUsageHistoryRecord";
                case 11:
                    return "getHistoryCountOfSendedMsg";
                case 12:
                    return "getHistoryCountOfReceivedMsg";
                case 13:
                    return "accumulateHistoryCountOfSendedMsg";
                case 14:
                    return "accumulateHistoryCountOfReceivedMsg";
                case 15:
                    return "getDialOutDuration";
                case 16:
                    return "getInComingCallDuration";
                case 17:
                    return "accumulateDialOutDuration";
                case 18:
                    return "accumulateInComingCallDuration";
                case 19:
                    return "getHistoryRecordsCountOfPhoneCalls";
                case 20:
                    return "getPhoneCallHistoryRecords";
                case 21:
                    return "writePhoneCallHistoryRecord";
                case 22:
                    return "shutDown";
                case 23:
                    return "getMinChargeTemperature";
                case 24:
                    return "getMaxChargeTemperature";
                case 25:
                    return "getMaxChargeCurrent";
                case 26:
                    return "updateMinChargeTemperature";
                case 27:
                    return "updateMaxChargeTemperature";
                case 28:
                    return "updateMaxChargeCurrent";
                case 29:
                    return "engineerReadDevBlock";
                case 30:
                    return "engineerWriteDevBlock";
                case 31:
                    return "getDownloadStatusString";
                case 32:
                    return "saveSecrecyConfig";
                case 33:
                    return "loadSecrecyConfig";
                case 34:
                    return "setProductLineLastTestFlag";
                case 35:
                    return "getProductLineLastTestFlag";
                case 36:
                    return "recordApkDeleteEvent";
                case 37:
                    return "getApkDeleteEventRecordCount";
                case 38:
                    return "getApkDeleteEventRecords";
                case 39:
                    return "recordApkInstallEvent";
                case 40:
                    return "getApkInstallEventRecordCount";
                case 41:
                    return "getApkInstallEventRecords";
                case 42:
                    return "recordMcsConnectID";
                case 43:
                    return "getMcsConnectID";
                case 44:
                    return "getFileSize";
                case 45:
                    return "readOppoFile";
                case 46:
                    return "saveOppoFile";
                case 47:
                    return "saveEntireOppoFile";
                case 48:
                    return "readEntireOppoFile";
                case 49:
                    return "deleteOppoFile";
                case 50:
                    return "saveEntireOppoDir";
                case 51:
                    return "readEntireOppoDir";
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
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        testSaveSomeData(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getHistoryBootTime();
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getOriginalSimcardData();
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result3 = getHistoryImeiNO();
                        reply.writeNoException();
                        reply.writeStringList(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result4 = getHistoryPcbaNO();
                        reply.writeNoException();
                        reply.writeStringList(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getAppUsageHistoryRecordCount();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result6 = getAppUsageHistoryRecords(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result7 = getAppUsageCountHistoryRecords(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result8 = getDialCountHistoryRecords(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean writeAppUsageHistoryRecord = writeAppUsageHistoryRecord(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(writeAppUsageHistoryRecord ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getHistoryCountOfSendedMsg();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getHistoryCountOfReceivedMsg();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean accumulateHistoryCountOfSendedMsg = accumulateHistoryCountOfSendedMsg(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(accumulateHistoryCountOfSendedMsg ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean accumulateHistoryCountOfReceivedMsg = accumulateHistoryCountOfReceivedMsg(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(accumulateHistoryCountOfReceivedMsg ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getDialOutDuration();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getInComingCallDuration();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean accumulateDialOutDuration = accumulateDialOutDuration(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(accumulateDialOutDuration ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean accumulateInComingCallDuration = accumulateInComingCallDuration(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(accumulateInComingCallDuration ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getHistoryRecordsCountOfPhoneCalls();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result14 = getPhoneCallHistoryRecords(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result14);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean writePhoneCallHistoryRecord = writePhoneCallHistoryRecord(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(writePhoneCallHistoryRecord ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        shutDown();
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getMinChargeTemperature();
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getMaxChargeTemperature();
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = getMaxChargeCurrent();
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateMinChargeTemperature = updateMinChargeTemperature(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(updateMinChargeTemperature ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateMaxChargeTemperature = updateMaxChargeTemperature(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(updateMaxChargeTemperature ? 1 : 0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateMaxChargeCurrent = updateMaxChargeCurrent(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(updateMaxChargeCurrent ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result18 = engineerReadDevBlock(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result18);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = engineerWriteDevBlock(data.readString(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String _result20 = getDownloadStatusString(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result20);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = saveSecrecyConfig(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        String _result22 = loadSecrecyConfig();
                        reply.writeNoException();
                        reply.writeString(_result22);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        boolean productLineLastTestFlag = setProductLineLastTestFlag(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(productLineLastTestFlag ? 1 : 0);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = getProductLineLastTestFlag();
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean recordApkDeleteEvent = recordApkDeleteEvent(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(recordApkDeleteEvent ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = getApkDeleteEventRecordCount();
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result25 = getApkDeleteEventRecords(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result25);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        boolean recordApkInstallEvent = recordApkInstallEvent(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(recordApkInstallEvent ? 1 : 0);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = getApkInstallEventRecordCount();
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result27 = getApkInstallEventRecords(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result27);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        boolean recordMcsConnectID = recordMcsConnectID(data.readString());
                        reply.writeNoException();
                        reply.writeInt(recordMcsConnectID ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        String _result28 = getMcsConnectID();
                        reply.writeNoException();
                        reply.writeString(_result28);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        int _result29 = getFileSize(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result30 = readOppoFile(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result30);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = saveOppoFile(data.readInt(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        int _result32 = saveEntireOppoFile(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean readEntireOppoFile = readEntireOppoFile(_arg02, _arg12, _arg2);
                        reply.writeNoException();
                        reply.writeInt(readEntireOppoFile ? 1 : 0);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deleteOppoFile = deleteOppoFile(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deleteOppoFile ? 1 : 0);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean saveEntireOppoDir = saveEntireOppoDir(_arg03, _arg13, _arg2);
                        reply.writeNoException();
                        reply.writeInt(saveEntireOppoDir ? 1 : 0);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean readEntireOppoDir = readEntireOppoDir(_arg04, _arg14, _arg2);
                        reply.writeNoException();
                        reply.writeInt(readEntireOppoDir ? 1 : 0);
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
        public static class Proxy implements IOppoUsageService {
            public static IOppoUsageService sDefaultImpl;
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

            @Override // android.os.IOppoUsageService
            public void testSaveSomeData(int dataType, String dataContent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dataType);
                    _data.writeString(dataContent);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().testSaveSomeData(dataType, dataContent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoUsageService
            public List<String> getHistoryBootTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHistoryBootTime();
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

            @Override // android.os.IOppoUsageService
            public List<String> getOriginalSimcardData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOriginalSimcardData();
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

            @Override // android.os.IOppoUsageService
            public List<String> getHistoryImeiNO() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHistoryImeiNO();
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

            @Override // android.os.IOppoUsageService
            public List<String> getHistoryPcbaNO() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHistoryPcbaNO();
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

            @Override // android.os.IOppoUsageService
            public int getAppUsageHistoryRecordCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUsageHistoryRecordCount();
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

            @Override // android.os.IOppoUsageService
            public List<String> getAppUsageHistoryRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUsageHistoryRecords(startIndex, endIndex);
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

            @Override // android.os.IOppoUsageService
            public List<String> getAppUsageCountHistoryRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUsageCountHistoryRecords(startIndex, endIndex);
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

            @Override // android.os.IOppoUsageService
            public List<String> getDialCountHistoryRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDialCountHistoryRecords(startIndex, endIndex);
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

            @Override // android.os.IOppoUsageService
            public boolean writeAppUsageHistoryRecord(String appName, String dateTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appName);
                    _data.writeString(dateTime);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().writeAppUsageHistoryRecord(appName, dateTime);
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

            @Override // android.os.IOppoUsageService
            public int getHistoryCountOfSendedMsg() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHistoryCountOfSendedMsg();
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

            @Override // android.os.IOppoUsageService
            public int getHistoryCountOfReceivedMsg() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHistoryCountOfReceivedMsg();
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

            @Override // android.os.IOppoUsageService
            public boolean accumulateHistoryCountOfSendedMsg(int newCountIncrease) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newCountIncrease);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().accumulateHistoryCountOfSendedMsg(newCountIncrease);
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

            @Override // android.os.IOppoUsageService
            public boolean accumulateHistoryCountOfReceivedMsg(int newCountIncrease) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newCountIncrease);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().accumulateHistoryCountOfReceivedMsg(newCountIncrease);
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

            @Override // android.os.IOppoUsageService
            public int getDialOutDuration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDialOutDuration();
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

            @Override // android.os.IOppoUsageService
            public int getInComingCallDuration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInComingCallDuration();
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

            @Override // android.os.IOppoUsageService
            public boolean accumulateDialOutDuration(int durationInMinute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(durationInMinute);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().accumulateDialOutDuration(durationInMinute);
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

            @Override // android.os.IOppoUsageService
            public boolean accumulateInComingCallDuration(int durationInMinute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(durationInMinute);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().accumulateInComingCallDuration(durationInMinute);
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

            @Override // android.os.IOppoUsageService
            public int getHistoryRecordsCountOfPhoneCalls() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHistoryRecordsCountOfPhoneCalls();
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

            @Override // android.os.IOppoUsageService
            public List<String> getPhoneCallHistoryRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPhoneCallHistoryRecords(startIndex, endIndex);
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

            @Override // android.os.IOppoUsageService
            public boolean writePhoneCallHistoryRecord(String phoneNoStr, String dateTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(phoneNoStr);
                    _data.writeString(dateTime);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().writePhoneCallHistoryRecord(phoneNoStr, dateTime);
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

            @Override // android.os.IOppoUsageService
            public void shutDown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().shutDown();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoUsageService
            public int getMinChargeTemperature() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMinChargeTemperature();
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

            @Override // android.os.IOppoUsageService
            public int getMaxChargeTemperature() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxChargeTemperature();
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

            @Override // android.os.IOppoUsageService
            public int getMaxChargeCurrent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxChargeCurrent();
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

            @Override // android.os.IOppoUsageService
            public boolean updateMinChargeTemperature(int temp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(temp);
                    boolean _result = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateMinChargeTemperature(temp);
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

            @Override // android.os.IOppoUsageService
            public boolean updateMaxChargeTemperature(int temp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(temp);
                    boolean _result = false;
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateMaxChargeTemperature(temp);
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

            @Override // android.os.IOppoUsageService
            public boolean updateMaxChargeCurrent(int current) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(current);
                    boolean _result = false;
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateMaxChargeCurrent(current);
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

            @Override // android.os.IOppoUsageService
            public byte[] engineerReadDevBlock(String partion, int offset, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(partion);
                    _data.writeInt(offset);
                    _data.writeInt(count);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().engineerReadDevBlock(partion, offset, count);
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

            @Override // android.os.IOppoUsageService
            public int engineerWriteDevBlock(String partion, byte[] content, int offset) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(partion);
                    _data.writeByteArray(content);
                    _data.writeInt(offset);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().engineerWriteDevBlock(partion, content, offset);
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

            @Override // android.os.IOppoUsageService
            public String getDownloadStatusString(int part) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(part);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDownloadStatusString(part);
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

            @Override // android.os.IOppoUsageService
            public int saveSecrecyConfig(String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(content);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saveSecrecyConfig(content);
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

            @Override // android.os.IOppoUsageService
            public String loadSecrecyConfig() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().loadSecrecyConfig();
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

            @Override // android.os.IOppoUsageService
            public boolean setProductLineLastTestFlag(int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    boolean _result = false;
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setProductLineLastTestFlag(flag);
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

            @Override // android.os.IOppoUsageService
            public int getProductLineLastTestFlag() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProductLineLastTestFlag();
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

            @Override // android.os.IOppoUsageService
            public boolean recordApkDeleteEvent(String deleteAppPkgName, String callerAppPkgName, String dateTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deleteAppPkgName);
                    _data.writeString(callerAppPkgName);
                    _data.writeString(dateTime);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().recordApkDeleteEvent(deleteAppPkgName, callerAppPkgName, dateTime);
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

            @Override // android.os.IOppoUsageService
            public int getApkDeleteEventRecordCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApkDeleteEventRecordCount();
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

            @Override // android.os.IOppoUsageService
            public List<String> getApkDeleteEventRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApkDeleteEventRecords(startIndex, endIndex);
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

            @Override // android.os.IOppoUsageService
            public boolean recordApkInstallEvent(String installAppPkgName, String callerAppPkgName, String dateTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(installAppPkgName);
                    _data.writeString(callerAppPkgName);
                    _data.writeString(dateTime);
                    boolean _result = false;
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().recordApkInstallEvent(installAppPkgName, callerAppPkgName, dateTime);
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

            @Override // android.os.IOppoUsageService
            public int getApkInstallEventRecordCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApkInstallEventRecordCount();
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

            @Override // android.os.IOppoUsageService
            public List<String> getApkInstallEventRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApkInstallEventRecords(startIndex, endIndex);
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

            @Override // android.os.IOppoUsageService
            public boolean recordMcsConnectID(String connectID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connectID);
                    boolean _result = false;
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().recordMcsConnectID(connectID);
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

            @Override // android.os.IOppoUsageService
            public String getMcsConnectID() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMcsConnectID();
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

            @Override // android.os.IOppoUsageService
            public int getFileSize(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileSize(path);
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

            @Override // android.os.IOppoUsageService
            public byte[] readOppoFile(String path, int startPosition, int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeInt(startPosition);
                    _data.writeInt(length);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readOppoFile(path, startPosition, length);
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

            @Override // android.os.IOppoUsageService
            public int saveOppoFile(int fileMax, String path, int offset, boolean append, int length, byte[] data) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(fileMax);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(path);
                        try {
                            _data.writeInt(offset);
                            _data.writeInt(append ? 1 : 0);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(length);
                            try {
                                _data.writeByteArray(data);
                            } catch (Throwable th4) {
                                th = th4;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(46, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int saveOppoFile = Stub.getDefaultImpl().saveOppoFile(fileMax, path, offset, append, length, data);
                        _reply.recycle();
                        _data.recycle();
                        return saveOppoFile;
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IOppoUsageService
            public int saveEntireOppoFile(String sourcePath, String targetPath, boolean deleteSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sourcePath);
                    _data.writeString(targetPath);
                    _data.writeInt(deleteSource ? 1 : 0);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saveEntireOppoFile(sourcePath, targetPath, deleteSource);
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

            @Override // android.os.IOppoUsageService
            public boolean readEntireOppoFile(String sourcePath, String targetPath, boolean deleteSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sourcePath);
                    _data.writeString(targetPath);
                    boolean _result = true;
                    _data.writeInt(deleteSource ? 1 : 0);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readEntireOppoFile(sourcePath, targetPath, deleteSource);
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

            @Override // android.os.IOppoUsageService
            public boolean deleteOppoFile(String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    boolean _result = false;
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteOppoFile(filePath);
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

            @Override // android.os.IOppoUsageService
            public boolean saveEntireOppoDir(String sourceDir, String targetDir, boolean deleteSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sourceDir);
                    _data.writeString(targetDir);
                    boolean _result = true;
                    _data.writeInt(deleteSource ? 1 : 0);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saveEntireOppoDir(sourceDir, targetDir, deleteSource);
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

            @Override // android.os.IOppoUsageService
            public boolean readEntireOppoDir(String sourceDir, String targetDir, boolean deleteSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sourceDir);
                    _data.writeString(targetDir);
                    boolean _result = true;
                    _data.writeInt(deleteSource ? 1 : 0);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readEntireOppoDir(sourceDir, targetDir, deleteSource);
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
        }

        public static boolean setDefaultImpl(IOppoUsageService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoUsageService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

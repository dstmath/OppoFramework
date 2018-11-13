package android.os;

import java.util.List;

public interface IOppoUsageService extends IInterface {

    public static abstract class Stub extends Binder implements IOppoUsageService {
        private static final String DESCRIPTOR = "android.os.IOppoUsageService";
        static final int TRANSACTION_accumulateDialOutDuration = 15;
        static final int TRANSACTION_accumulateHistoryCountOfReceivedMsg = 12;
        static final int TRANSACTION_accumulateHistoryCountOfSendedMsg = 11;
        static final int TRANSACTION_accumulateInComingCallDuration = 16;
        static final int TRANSACTION_engineerReadDevBlock = 27;
        static final int TRANSACTION_engineerWriteDevBlock = 28;
        static final int TRANSACTION_getApkDeleteEventRecordCount = 35;
        static final int TRANSACTION_getApkDeleteEventRecords = 36;
        static final int TRANSACTION_getApkInstallEventRecordCount = 38;
        static final int TRANSACTION_getApkInstallEventRecords = 39;
        static final int TRANSACTION_getAppUsageHistoryRecordCount = 6;
        static final int TRANSACTION_getAppUsageHistoryRecords = 7;
        static final int TRANSACTION_getDialOutDuration = 13;
        static final int TRANSACTION_getDownloadStatusString = 29;
        static final int TRANSACTION_getHistoryBootTime = 2;
        static final int TRANSACTION_getHistoryCountOfReceivedMsg = 10;
        static final int TRANSACTION_getHistoryCountOfSendedMsg = 9;
        static final int TRANSACTION_getHistoryImeiNO = 4;
        static final int TRANSACTION_getHistoryPcbaNO = 5;
        static final int TRANSACTION_getHistoryRecordsCountOfPhoneCalls = 17;
        static final int TRANSACTION_getInComingCallDuration = 14;
        static final int TRANSACTION_getMaxChargeCurrent = 23;
        static final int TRANSACTION_getMaxChargeTemperature = 22;
        static final int TRANSACTION_getMcsConnectID = 41;
        static final int TRANSACTION_getMinChargeTemperature = 21;
        static final int TRANSACTION_getOriginalSimcardData = 3;
        static final int TRANSACTION_getPhoneCallHistoryRecords = 18;
        static final int TRANSACTION_getProductLineLastTestFlag = 33;
        static final int TRANSACTION_loadSecrecyConfig = 31;
        static final int TRANSACTION_recordApkDeleteEvent = 34;
        static final int TRANSACTION_recordApkInstallEvent = 37;
        static final int TRANSACTION_recordMcsConnectID = 40;
        static final int TRANSACTION_saveSecrecyConfig = 30;
        static final int TRANSACTION_setProductLineLastTestFlag = 32;
        static final int TRANSACTION_shutDown = 20;
        static final int TRANSACTION_testSaveSomeData = 1;
        static final int TRANSACTION_updateMaxChargeCurrent = 26;
        static final int TRANSACTION_updateMaxChargeTemperature = 25;
        static final int TRANSACTION_updateMinChargeTemperature = 24;
        static final int TRANSACTION_writeAppUsageHistoryRecord = 8;
        static final int TRANSACTION_writePhoneCallHistoryRecord = 19;

        private static class Proxy implements IOppoUsageService {
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

            public void testSaveSomeData(int dataType, String dataContent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dataType);
                    _data.writeString(dataContent);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getHistoryBootTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getOriginalSimcardData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getHistoryImeiNO() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getHistoryPcbaNO() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAppUsageHistoryRecordCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAppUsageHistoryRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean writeAppUsageHistoryRecord(String appName, String dateTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appName);
                    _data.writeString(dateTime);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHistoryCountOfSendedMsg() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHistoryCountOfReceivedMsg() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean accumulateHistoryCountOfSendedMsg(int newCountIncrease) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newCountIncrease);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean accumulateHistoryCountOfReceivedMsg(int newCountIncrease) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newCountIncrease);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDialOutDuration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInComingCallDuration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean accumulateDialOutDuration(int durationInMinute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(durationInMinute);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean accumulateInComingCallDuration(int durationInMinute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(durationInMinute);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHistoryRecordsCountOfPhoneCalls() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPhoneCallHistoryRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean writePhoneCallHistoryRecord(String phoneNoStr, String dateTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(phoneNoStr);
                    _data.writeString(dateTime);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shutDown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMinChargeTemperature() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxChargeTemperature() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxChargeCurrent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateMinChargeTemperature(int temp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(temp);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateMaxChargeTemperature(int temp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(temp);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateMaxChargeCurrent(int current) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(current);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] engineerReadDevBlock(String partion, int offset, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(partion);
                    _data.writeInt(offset);
                    _data.writeInt(count);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int engineerWriteDevBlock(String partion, byte[] content, int offset) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(partion);
                    _data.writeByteArray(content);
                    _data.writeInt(offset);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDownloadStatusString(int part) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(part);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saveSecrecyConfig(String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(content);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String loadSecrecyConfig() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setProductLineLastTestFlag(int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getProductLineLastTestFlag() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean recordApkDeleteEvent(String deleteAppPkgName, String callerAppPkgName, String dateTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deleteAppPkgName);
                    _data.writeString(callerAppPkgName);
                    _data.writeString(dateTime);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getApkDeleteEventRecordCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getApkDeleteEventRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean recordApkInstallEvent(String installAppPkgName, String callerAppPkgName, String dateTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(installAppPkgName);
                    _data.writeString(callerAppPkgName);
                    _data.writeString(dateTime);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getApkInstallEventRecordCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getApkInstallEventRecords(int startIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(startIndex);
                    _data.writeInt(endIndex);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean recordMcsConnectID(String connectID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connectID);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getMcsConnectID() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            List<String> _result;
            int _result2;
            boolean _result3;
            String _result4;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    testSaveSomeData(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getHistoryBootTime();
                    reply.writeNoException();
                    reply.writeStringList(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getOriginalSimcardData();
                    reply.writeNoException();
                    reply.writeStringList(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getHistoryImeiNO();
                    reply.writeNoException();
                    reply.writeStringList(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getHistoryPcbaNO();
                    reply.writeNoException();
                    reply.writeStringList(_result);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAppUsageHistoryRecordCount();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAppUsageHistoryRecords(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = writeAppUsageHistoryRecord(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getHistoryCountOfSendedMsg();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getHistoryCountOfReceivedMsg();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = accumulateHistoryCountOfSendedMsg(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = accumulateHistoryCountOfReceivedMsg(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDialOutDuration();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getInComingCallDuration();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = accumulateDialOutDuration(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = accumulateInComingCallDuration(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getHistoryRecordsCountOfPhoneCalls();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPhoneCallHistoryRecords(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = writePhoneCallHistoryRecord(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    shutDown();
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMinChargeTemperature();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMaxChargeTemperature();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMaxChargeCurrent();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateMinChargeTemperature(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateMaxChargeTemperature(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateMaxChargeCurrent(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result5 = engineerReadDevBlock(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result5);
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = engineerWriteDevBlock(data.readString(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getDownloadStatusString(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = saveSecrecyConfig(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = loadSecrecyConfig();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setProductLineLastTestFlag(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getProductLineLastTestFlag();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = recordApkDeleteEvent(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getApkDeleteEventRecordCount();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getApkDeleteEventRecords(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result);
                    return true;
                case 37:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = recordApkInstallEvent(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 38:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getApkInstallEventRecordCount();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 39:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getApkInstallEventRecords(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result);
                    return true;
                case 40:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = recordMcsConnectID(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getMcsConnectID();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean accumulateDialOutDuration(int i) throws RemoteException;

    boolean accumulateHistoryCountOfReceivedMsg(int i) throws RemoteException;

    boolean accumulateHistoryCountOfSendedMsg(int i) throws RemoteException;

    boolean accumulateInComingCallDuration(int i) throws RemoteException;

    byte[] engineerReadDevBlock(String str, int i, int i2) throws RemoteException;

    int engineerWriteDevBlock(String str, byte[] bArr, int i) throws RemoteException;

    int getApkDeleteEventRecordCount() throws RemoteException;

    List<String> getApkDeleteEventRecords(int i, int i2) throws RemoteException;

    int getApkInstallEventRecordCount() throws RemoteException;

    List<String> getApkInstallEventRecords(int i, int i2) throws RemoteException;

    int getAppUsageHistoryRecordCount() throws RemoteException;

    List<String> getAppUsageHistoryRecords(int i, int i2) throws RemoteException;

    int getDialOutDuration() throws RemoteException;

    String getDownloadStatusString(int i) throws RemoteException;

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

    boolean recordApkDeleteEvent(String str, String str2, String str3) throws RemoteException;

    boolean recordApkInstallEvent(String str, String str2, String str3) throws RemoteException;

    boolean recordMcsConnectID(String str) throws RemoteException;

    int saveSecrecyConfig(String str) throws RemoteException;

    boolean setProductLineLastTestFlag(int i) throws RemoteException;

    void shutDown() throws RemoteException;

    void testSaveSomeData(int i, String str) throws RemoteException;

    boolean updateMaxChargeCurrent(int i) throws RemoteException;

    boolean updateMaxChargeTemperature(int i) throws RemoteException;

    boolean updateMinChargeTemperature(int i) throws RemoteException;

    boolean writeAppUsageHistoryRecord(String str, String str2) throws RemoteException;

    boolean writePhoneCallHistoryRecord(String str, String str2) throws RemoteException;
}

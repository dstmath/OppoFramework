package android.os;

import android.os.IOppoUsageService.Stub;
import android.util.Log;
import java.util.List;

public final class OppoUsageManager {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_E = true;
    private static final boolean DEBUG_W = true;
    public static final String SERVICE_NAME = "usage";
    public static final String TAG = "OppoUsageManager";
    private static OppoUsageManager mInstance = null;
    private IOppoUsageService mOppoUsageService;

    private OppoUsageManager() {
        this.mOppoUsageService = null;
        this.mOppoUsageService = Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
    }

    public static OppoUsageManager getOppoUsageManager() {
        if (mInstance == null) {
            mInstance = new OppoUsageManager();
        }
        return mInstance;
    }

    public void testSaveSomeData(int dataType, String dataContent) {
        if (this.mOppoUsageService != null) {
            try {
                this.mOppoUsageService.testSaveSomeData(dataType, dataContent);
                return;
            } catch (RemoteException exce) {
                Log.e(TAG, "testSaveSomeData failed!", exce);
                return;
            }
        }
        Log.w(TAG, "testSaveSomeData:service not publish!");
    }

    public List<String> getHistoryBootTime() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getHistoryBootTime();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryBootTime failed!", exce);
            }
        } else {
            Log.w(TAG, "getHistoryBootTime:service not publish!");
            return null;
        }
    }

    public List<String> getHistoryImeiNO() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getHistoryImeiNO();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryImeiNO failed!", exce);
            }
        } else {
            Log.w(TAG, "getHistoryImeiNO:service not publish!");
            return null;
        }
    }

    public List<String> getOriginalSimcardData() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getOriginalSimcardData();
            } catch (RemoteException exce) {
                Log.e(TAG, "getOriginalImeiMeidNO failed!", exce);
            }
        } else {
            Log.w(TAG, "getOriginalImeiMeidNO:service not publish!");
            return null;
        }
    }

    public List<String> getHistoryPcbaNO() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getHistoryPcbaNO();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryPcbaNO failed!", exce);
            }
        } else {
            Log.w(TAG, "getHistoryPcbaNO:service not publish!");
            return null;
        }
    }

    public int getAppUsageHistoryRecordCount() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getAppUsageHistoryRecordCount();
            } catch (RemoteException exce) {
                Log.e(TAG, "getAppUsageHistoryRecordCount failed!", exce);
            }
        } else {
            Log.w(TAG, "getAppUsageHistoryRecordCount:service not publish!");
            return 0;
        }
    }

    public List<String> getAppUsageHistoryRecords(int startIndex, int endIndex) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getAppUsageHistoryRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getAppUsageHistoryRecords failed!", exce);
            }
        } else {
            Log.w(TAG, "getAppUsageHistoryRecords:service not publish!");
            return null;
        }
    }

    public boolean writeAppUsageHistoryRecord(String appName, String dateTime) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.writeAppUsageHistoryRecord(appName, dateTime);
            } catch (RemoteException exce) {
                Log.e(TAG, "getAppUsageHistoryRecords failed!", exce);
            }
        } else {
            Log.w(TAG, "getAppUsageHistoryRecords:service not publish!");
            return false;
        }
    }

    public int getHistoryCountOfSendedMsg() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getHistoryCountOfSendedMsg();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryCountOfSendedMsg failed!", exce);
            }
        } else {
            Log.w(TAG, "getHistoryCountOfSendedMsg:service not publish!");
            return 0;
        }
    }

    public int getHistoryCountOfReceivedMsg() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getHistoryCountOfReceivedMsg();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryCountOfReceivedMsg failed!", exce);
            }
        } else {
            Log.w(TAG, "getHistoryCountOfReceivedMsg:service not publish!");
            return 0;
        }
    }

    public boolean accumulateHistoryCountOfSendedMsg(int newCountIncrease) {
        if (newCountIncrease <= 0) {
            Log.w(TAG, "accumulateHistoryCountOfSendedMsg:illegal param!");
            return false;
        } else if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.accumulateHistoryCountOfSendedMsg(newCountIncrease);
            } catch (RemoteException exce) {
                Log.e(TAG, "accumulateHistoryCountOfSendedMsg failed!", exce);
            }
        } else {
            Log.w(TAG, "accumulateHistoryCountOfSendedMsg:service not publish!");
            return false;
        }
    }

    public boolean accumulateHistoryCountOfReceivedMsg(int newCountIncrease) {
        if (newCountIncrease <= 0) {
            Log.w(TAG, "accumulateHistoryCountOfReceivedMsg:illegal param!");
            return false;
        } else if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.accumulateHistoryCountOfReceivedMsg(newCountIncrease);
            } catch (RemoteException exce) {
                Log.e(TAG, "accumulateHistoryCountOfReceivedMsg failed!", exce);
            }
        } else {
            Log.w(TAG, "accumulateHistoryCountOfReceivedMsg:service not publish!");
            return false;
        }
    }

    public int getDialOutDuration() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getDialOutDuration();
            } catch (RemoteException exce) {
                Log.e(TAG, "getDialOutDuration failed!", exce);
            }
        } else {
            Log.w(TAG, "getDialOutDuration:service not publish!");
            return 0;
        }
    }

    public int getInComingCallDuration() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getInComingCallDuration();
            } catch (RemoteException exce) {
                Log.e(TAG, "getInComingCallDuration failed!", exce);
            }
        } else {
            Log.w(TAG, "getInComingCallDuration:service not publish!");
            return 0;
        }
    }

    public boolean accumulateDialOutDuration(int durationInMinute) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.accumulateDialOutDuration(durationInMinute);
            } catch (RemoteException exce) {
                Log.e(TAG, "accumulateDialOutDuration failed!", exce);
            }
        } else {
            Log.w(TAG, "accumulateDialOutDuration:service not publish!");
            return false;
        }
    }

    public boolean accumulateInComingCallDuration(int durationInMinute) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.accumulateInComingCallDuration(durationInMinute);
            } catch (RemoteException exce) {
                Log.e(TAG, "accumulateInComingCallDuration failed!", exce);
            }
        } else {
            Log.w(TAG, "accumulateInComingCallDuration:service not publish!");
            return false;
        }
    }

    public int getHistoryRecordsCountOfPhoneCalls() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getHistoryRecordsCountOfPhoneCalls();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryRecordsCountOfPhoneCalls failed!", exce);
            }
        } else {
            Log.w(TAG, "getHistoryRecordsCountOfPhoneCalls:service not publish!");
            return 0;
        }
    }

    public List<String> getPhoneCallHistoryRecords(int startIndex, int endIndex) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getPhoneCallHistoryRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getPhoneCallHistoryRecords failed!", exce);
            }
        } else {
            Log.w(TAG, "getPhoneCallHistoryRecords:service not publish!");
            return null;
        }
    }

    public boolean writePhoneCallHistoryRecord(String phoneNoStr, String dateTime) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.writePhoneCallHistoryRecord(phoneNoStr, dateTime);
            } catch (RemoteException exce) {
                Log.e(TAG, "writePhoneCallHistoryRecord failed!", exce);
            }
        } else {
            Log.w(TAG, "writePhoneCallHistoryRecord:service not publish!");
            return false;
        }
    }

    public boolean updateMaxChargeCurrent(int current) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.updateMaxChargeCurrent(current);
            } catch (RemoteException exce) {
                Log.e(TAG, "updateMaxChargeCurrent failed!", exce);
            }
        } else {
            Log.w(TAG, "updateMaxChargeCurrent:service not publish!");
            return false;
        }
    }

    public boolean updateMaxChargeTemperature(int temp) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.updateMaxChargeCurrent(temp);
            } catch (RemoteException exce) {
                Log.e(TAG, "updateMaxChargeTemperature failed!", exce);
            }
        } else {
            Log.w(TAG, "updateMaxChargeTemperature:service not publish!");
            return false;
        }
    }

    public boolean updateMinChargeTemperature(int temp) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.updateMinChargeTemperature(temp);
            } catch (RemoteException exce) {
                Log.e(TAG, "updateMinChargeTemperature failed!", exce);
            }
        } else {
            Log.w(TAG, "updateMinChargeTemperature:service not publish!");
            return false;
        }
    }

    public int getMaxChargeCurrent() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getMaxChargeCurrent();
            } catch (RemoteException exce) {
                Log.e(TAG, "getMaxChargeCurrent failed!", exce);
            }
        } else {
            Log.w(TAG, "getMaxChargeCurrent:service not publish!");
            return 0;
        }
    }

    public int getMaxChargeTemperature() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getMaxChargeTemperature();
            } catch (RemoteException exce) {
                Log.e(TAG, "getMaxChargeTemperature failed!", exce);
            }
        } else {
            Log.w(TAG, "getMaxChargeTemperature:service not publish!");
            return 0;
        }
    }

    public int getMinChargeTemperature() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getMinChargeTemperature();
            } catch (RemoteException exce) {
                Log.e(TAG, "getMinChargeTemperature failed!", exce);
            }
        } else {
            Log.w(TAG, "getMinChargeTemperature:service not publish!");
            return 0;
        }
    }

    public byte[] engineerReadDevBlock(String partion, int offset, int count) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.engineerReadDevBlock(partion, offset, count);
            } catch (RemoteException exce) {
                Log.e(TAG, "engineerReadDevBlock failed!", exce);
            }
        } else {
            Log.w(TAG, "engineerReadDevBlock:service not publish!");
            return null;
        }
    }

    public int engineerWriteDevBlock(String partion, byte[] content, int offset) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.engineerWriteDevBlock(partion, content, offset);
            } catch (RemoteException exce) {
                Log.e(TAG, "engineerWriteDevBlock failed!", exce);
            }
        } else {
            Log.w(TAG, "engineerWriteDevBlock:service not publish!");
            return -1;
        }
    }

    public String getDownloadStatusString(int part) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getDownloadStatusString(part);
            } catch (RemoteException exce) {
                Log.e(TAG, "getDownloadStatusString failed!", exce);
            }
        } else {
            Log.w(TAG, "getDownloadStatusString:service not publish!");
            return null;
        }
    }

    public String loadSecrecyConfig() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.loadSecrecyConfig();
            } catch (RemoteException exce) {
                Log.e(TAG, "loadSecrecyConfig failed!", exce);
            }
        } else {
            Log.w(TAG, "loadSecrecyConfig:service not publish!");
            return null;
        }
    }

    public int saveSecrecyConfig(String content) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.saveSecrecyConfig(content);
            } catch (RemoteException exce) {
                Log.e(TAG, "saveSecrecyConfig failed!", exce);
            }
        } else {
            Log.w(TAG, "saveSecrecyConfig:service not publish!");
            return -1;
        }
    }

    public boolean setProductLineLastTestFlag(int flag) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.setProductLineLastTestFlag(flag);
            } catch (RemoteException exce) {
                Log.e(TAG, "setProductLineLastTestFlag failed!", exce);
            }
        } else {
            Log.w(TAG, "setProductLineLastTestFlag:service not publish!");
            return false;
        }
    }

    public int getProductLineLastTestFlag() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getProductLineLastTestFlag();
            } catch (RemoteException exce) {
                Log.e(TAG, "getProductLineLastTestFlag failed!", exce);
            }
        } else {
            Log.w(TAG, "getProductLineLastTestFlag:service not publish!");
            return -1;
        }
    }

    public boolean recordApkDeleteEvent(String deleteAppPkgName, String callerAppPkgName, String dateTime) {
        if (deleteAppPkgName == null || deleteAppPkgName.isEmpty()) {
            Log.w(TAG, "recordApkDeleteEvent:deleteAppPkgName empty!");
            return false;
        } else if (callerAppPkgName == null || callerAppPkgName.isEmpty()) {
            Log.w(TAG, "recordApkDeleteEvent:callerAppPkgName empty!");
            return false;
        } else if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.recordApkDeleteEvent(deleteAppPkgName, callerAppPkgName, dateTime);
            } catch (RemoteException exce) {
                Log.e(TAG, "recordApkDeleteEvent failed!", exce);
            }
        } else {
            Log.w(TAG, "recordApkDeleteEvent:service not publish!");
            return false;
        }
    }

    public int getApkDeleteEventRecordCount() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getApkDeleteEventRecordCount();
            } catch (RemoteException exce) {
                Log.e(TAG, "getApkDeleteEventRecordCount failed!", exce);
            }
        } else {
            Log.w(TAG, "getApkDeleteEventRecordCount:service not publish!");
            return 0;
        }
    }

    public List<String> getApkDeleteEventRecords(int startIndex, int endIndex) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getApkDeleteEventRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getApkDeleteEventRecords failed!", exce);
            }
        } else {
            Log.w(TAG, "getApkDeleteEventRecords:service not publish!");
            return null;
        }
    }

    public boolean recordApkInstallEvent(String installAppPkgName, String callerAppPkgName, String dateTime) {
        if (installAppPkgName == null || installAppPkgName.isEmpty()) {
            Log.w(TAG, "recordApkInstallEvent:deleteAppPkgName empty!");
            return false;
        } else if (callerAppPkgName == null || callerAppPkgName.isEmpty()) {
            Log.w(TAG, "recordApkInstallEvent:callerAppPkgName empty!");
            return false;
        } else if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.recordApkInstallEvent(installAppPkgName, callerAppPkgName, dateTime);
            } catch (RemoteException exce) {
                Log.e(TAG, "recordApkInstallEvent failed!", exce);
            }
        } else {
            Log.w(TAG, "recordApkInstallEvent:service not publish!");
            return false;
        }
    }

    public int getApkInstallEventRecordCount() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getApkInstallEventRecordCount();
            } catch (RemoteException exce) {
                Log.e(TAG, "getApkInstallEventRecordCount failed!", exce);
            }
        } else {
            Log.w(TAG, "getApkInstallEventRecordCount:service not publish!");
            return 0;
        }
    }

    public List<String> getApkInstallEventRecords(int startIndex, int endIndex) {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getApkInstallEventRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getApkInstallEventRecords failed!", exce);
            }
        } else {
            Log.w(TAG, "getApkInstallEventRecords:service not publish!");
            return null;
        }
    }

    public boolean recordMcsConnectID(String connectID) {
        if (connectID == null || connectID.isEmpty()) {
            Log.w(TAG, "recordMcsConnectID:connectID empty!");
            return false;
        } else if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.recordMcsConnectID(connectID);
            } catch (RemoteException exce) {
                Log.e(TAG, "recordMcsConnectID failed!", exce);
            }
        } else {
            Log.w(TAG, "recordMcsConnectID:service not publish!");
            return false;
        }
    }

    public String getMcsConnectID() {
        if (this.mOppoUsageService != null) {
            try {
                return this.mOppoUsageService.getMcsConnectID();
            } catch (RemoteException exce) {
                Log.e(TAG, "getMcsConnectID failed!", exce);
            }
        } else {
            Log.w(TAG, "getMcsConnectID:service not publish!");
            return null;
        }
    }
}

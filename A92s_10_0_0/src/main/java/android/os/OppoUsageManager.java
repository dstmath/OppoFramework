package android.os;

import android.os.IOppoUsageService;
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
        this.mOppoUsageService = IOppoUsageService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
    }

    public static OppoUsageManager getOppoUsageManager() {
        if (mInstance == null) {
            mInstance = new OppoUsageManager();
        }
        return mInstance;
    }

    public void testSaveSomeData(int dataType, String dataContent) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                iOppoUsageService.testSaveSomeData(dataType, dataContent);
            } catch (RemoteException exce) {
                Log.e(TAG, "testSaveSomeData failed!", exce);
            }
        } else {
            Log.w(TAG, "testSaveSomeData:service not publish!");
        }
    }

    public List<String> getHistoryBootTime() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getHistoryBootTime();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryBootTime failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getHistoryBootTime:service not publish!");
            return null;
        }
    }

    public List<String> getHistoryImeiNO() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getHistoryImeiNO();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryImeiNO failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getHistoryImeiNO:service not publish!");
            return null;
        }
    }

    public List<String> getOriginalSimcardData() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getOriginalSimcardData();
            } catch (RemoteException exce) {
                Log.e(TAG, "getOriginalImeiMeidNO failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getOriginalImeiMeidNO:service not publish!");
            return null;
        }
    }

    public List<String> getHistoryPcbaNO() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getHistoryPcbaNO();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryPcbaNO failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getHistoryPcbaNO:service not publish!");
            return null;
        }
    }

    public int getAppUsageHistoryRecordCount() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getAppUsageHistoryRecordCount();
            } catch (RemoteException exce) {
                Log.e(TAG, "getAppUsageHistoryRecordCount failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getAppUsageHistoryRecordCount:service not publish!");
            return 0;
        }
    }

    public List<String> getAppUsageHistoryRecords(int startIndex, int endIndex) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getAppUsageHistoryRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getAppUsageHistoryRecords failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getAppUsageHistoryRecords:service not publish!");
            return null;
        }
    }

    public List<String> getAppUsageCountHistoryRecords(int startIndex, int endIndex) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getAppUsageCountHistoryRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getAppUsageCountHistoryRecords failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getAppUsageCountHistoryRecords:service not publish!");
            return null;
        }
    }

    public List<String> getDialCountHistoryRecords(int startIndex, int endIndex) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getDialCountHistoryRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getDialCountHistoryRecords failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getDialCountHistoryRecords:service not publish!");
            return null;
        }
    }

    public boolean writeAppUsageHistoryRecord(String appName, String dateTime) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.writeAppUsageHistoryRecord(appName, dateTime);
            } catch (RemoteException exce) {
                Log.e(TAG, "getAppUsageHistoryRecords failed!", exce);
                return false;
            }
        } else {
            Log.w(TAG, "getAppUsageHistoryRecords:service not publish!");
            return false;
        }
    }

    public int getHistoryCountOfSendedMsg() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getHistoryCountOfSendedMsg();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryCountOfSendedMsg failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getHistoryCountOfSendedMsg:service not publish!");
            return 0;
        }
    }

    public int getHistoryCountOfReceivedMsg() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getHistoryCountOfReceivedMsg();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryCountOfReceivedMsg failed!", exce);
                return 0;
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
        }
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.accumulateHistoryCountOfSendedMsg(newCountIncrease);
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
        }
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.accumulateHistoryCountOfReceivedMsg(newCountIncrease);
            } catch (RemoteException exce) {
                Log.e(TAG, "accumulateHistoryCountOfReceivedMsg failed!", exce);
            }
        } else {
            Log.w(TAG, "accumulateHistoryCountOfReceivedMsg:service not publish!");
            return false;
        }
    }

    public int getDialOutDuration() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getDialOutDuration();
            } catch (RemoteException exce) {
                Log.e(TAG, "getDialOutDuration failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getDialOutDuration:service not publish!");
            return 0;
        }
    }

    public int getInComingCallDuration() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getInComingCallDuration();
            } catch (RemoteException exce) {
                Log.e(TAG, "getInComingCallDuration failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getInComingCallDuration:service not publish!");
            return 0;
        }
    }

    public boolean accumulateDialOutDuration(int durationInMinute) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.accumulateDialOutDuration(durationInMinute);
            } catch (RemoteException exce) {
                Log.e(TAG, "accumulateDialOutDuration failed!", exce);
                return false;
            }
        } else {
            Log.w(TAG, "accumulateDialOutDuration:service not publish!");
            return false;
        }
    }

    public boolean accumulateInComingCallDuration(int durationInMinute) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.accumulateInComingCallDuration(durationInMinute);
            } catch (RemoteException exce) {
                Log.e(TAG, "accumulateInComingCallDuration failed!", exce);
                return false;
            }
        } else {
            Log.w(TAG, "accumulateInComingCallDuration:service not publish!");
            return false;
        }
    }

    public int getHistoryRecordsCountOfPhoneCalls() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getHistoryRecordsCountOfPhoneCalls();
            } catch (RemoteException exce) {
                Log.e(TAG, "getHistoryRecordsCountOfPhoneCalls failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getHistoryRecordsCountOfPhoneCalls:service not publish!");
            return 0;
        }
    }

    public List<String> getPhoneCallHistoryRecords(int startIndex, int endIndex) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getPhoneCallHistoryRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getPhoneCallHistoryRecords failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getPhoneCallHistoryRecords:service not publish!");
            return null;
        }
    }

    public boolean writePhoneCallHistoryRecord(String phoneNoStr, String dateTime) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.writePhoneCallHistoryRecord(phoneNoStr, dateTime);
            } catch (RemoteException exce) {
                Log.e(TAG, "writePhoneCallHistoryRecord failed!", exce);
                return false;
            }
        } else {
            Log.w(TAG, "writePhoneCallHistoryRecord:service not publish!");
            return false;
        }
    }

    public boolean updateMaxChargeCurrent(int current) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.updateMaxChargeCurrent(current);
            } catch (RemoteException exce) {
                Log.e(TAG, "updateMaxChargeCurrent failed!", exce);
                return false;
            }
        } else {
            Log.w(TAG, "updateMaxChargeCurrent:service not publish!");
            return false;
        }
    }

    public boolean updateMaxChargeTemperature(int temp) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.updateMaxChargeCurrent(temp);
            } catch (RemoteException exce) {
                Log.e(TAG, "updateMaxChargeTemperature failed!", exce);
                return false;
            }
        } else {
            Log.w(TAG, "updateMaxChargeTemperature:service not publish!");
            return false;
        }
    }

    public boolean updateMinChargeTemperature(int temp) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.updateMinChargeTemperature(temp);
            } catch (RemoteException exce) {
                Log.e(TAG, "updateMinChargeTemperature failed!", exce);
                return false;
            }
        } else {
            Log.w(TAG, "updateMinChargeTemperature:service not publish!");
            return false;
        }
    }

    public int getMaxChargeCurrent() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getMaxChargeCurrent();
            } catch (RemoteException exce) {
                Log.e(TAG, "getMaxChargeCurrent failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getMaxChargeCurrent:service not publish!");
            return 0;
        }
    }

    public int getMaxChargeTemperature() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getMaxChargeTemperature();
            } catch (RemoteException exce) {
                Log.e(TAG, "getMaxChargeTemperature failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getMaxChargeTemperature:service not publish!");
            return 0;
        }
    }

    public int getMinChargeTemperature() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getMinChargeTemperature();
            } catch (RemoteException exce) {
                Log.e(TAG, "getMinChargeTemperature failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getMinChargeTemperature:service not publish!");
            return 0;
        }
    }

    public byte[] engineerReadDevBlock(String partion, int offset, int count) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.engineerReadDevBlock(partion, offset, count);
            } catch (RemoteException exce) {
                Log.e(TAG, "engineerReadDevBlock failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "engineerReadDevBlock:service not publish!");
            return null;
        }
    }

    public int engineerWriteDevBlock(String partion, byte[] content, int offset) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.engineerWriteDevBlock(partion, content, offset);
            } catch (RemoteException exce) {
                Log.e(TAG, "engineerWriteDevBlock failed!", exce);
                return -1;
            }
        } else {
            Log.w(TAG, "engineerWriteDevBlock:service not publish!");
            return -1;
        }
    }

    public String getDownloadStatusString(int part) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getDownloadStatusString(part);
            } catch (RemoteException exce) {
                Log.e(TAG, "getDownloadStatusString failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getDownloadStatusString:service not publish!");
            return null;
        }
    }

    public String loadSecrecyConfig() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.loadSecrecyConfig();
            } catch (RemoteException exce) {
                Log.e(TAG, "loadSecrecyConfig failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "loadSecrecyConfig:service not publish!");
            return null;
        }
    }

    public int saveSecrecyConfig(String content) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.saveSecrecyConfig(content);
            } catch (RemoteException exce) {
                Log.e(TAG, "saveSecrecyConfig failed!", exce);
                return -1;
            }
        } else {
            Log.w(TAG, "saveSecrecyConfig:service not publish!");
            return -1;
        }
    }

    public boolean setProductLineLastTestFlag(int flag) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.setProductLineLastTestFlag(flag);
            } catch (RemoteException exce) {
                Log.e(TAG, "setProductLineLastTestFlag failed!", exce);
                return false;
            }
        } else {
            Log.w(TAG, "setProductLineLastTestFlag:service not publish!");
            return false;
        }
    }

    public int getProductLineLastTestFlag() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getProductLineLastTestFlag();
            } catch (RemoteException exce) {
                Log.e(TAG, "getProductLineLastTestFlag failed!", exce);
                return -1;
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
        } else {
            IOppoUsageService iOppoUsageService = this.mOppoUsageService;
            if (iOppoUsageService != null) {
                try {
                    return iOppoUsageService.recordApkDeleteEvent(deleteAppPkgName, callerAppPkgName, dateTime);
                } catch (RemoteException exce) {
                    Log.e(TAG, "recordApkDeleteEvent failed!", exce);
                }
            } else {
                Log.w(TAG, "recordApkDeleteEvent:service not publish!");
                return false;
            }
        }
    }

    public int getApkDeleteEventRecordCount() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getApkDeleteEventRecordCount();
            } catch (RemoteException exce) {
                Log.e(TAG, "getApkDeleteEventRecordCount failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getApkDeleteEventRecordCount:service not publish!");
            return 0;
        }
    }

    public List<String> getApkDeleteEventRecords(int startIndex, int endIndex) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getApkDeleteEventRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getApkDeleteEventRecords failed!", exce);
                return null;
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
        } else {
            IOppoUsageService iOppoUsageService = this.mOppoUsageService;
            if (iOppoUsageService != null) {
                try {
                    return iOppoUsageService.recordApkInstallEvent(installAppPkgName, callerAppPkgName, dateTime);
                } catch (RemoteException exce) {
                    Log.e(TAG, "recordApkInstallEvent failed!", exce);
                }
            } else {
                Log.w(TAG, "recordApkInstallEvent:service not publish!");
                return false;
            }
        }
    }

    public int getApkInstallEventRecordCount() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getApkInstallEventRecordCount();
            } catch (RemoteException exce) {
                Log.e(TAG, "getApkInstallEventRecordCount failed!", exce);
                return 0;
            }
        } else {
            Log.w(TAG, "getApkInstallEventRecordCount:service not publish!");
            return 0;
        }
    }

    public List<String> getApkInstallEventRecords(int startIndex, int endIndex) {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getApkInstallEventRecords(startIndex, endIndex);
            } catch (RemoteException exce) {
                Log.e(TAG, "getApkInstallEventRecords failed!", exce);
                return null;
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
        }
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.recordMcsConnectID(connectID);
            } catch (RemoteException exce) {
                Log.e(TAG, "recordMcsConnectID failed!", exce);
            }
        } else {
            Log.w(TAG, "recordMcsConnectID:service not publish!");
            return false;
        }
    }

    public String getMcsConnectID() {
        IOppoUsageService iOppoUsageService = this.mOppoUsageService;
        if (iOppoUsageService != null) {
            try {
                return iOppoUsageService.getMcsConnectID();
            } catch (RemoteException exce) {
                Log.e(TAG, "getMcsConnectID failed!", exce);
                return null;
            }
        } else {
            Log.w(TAG, "getMcsConnectID:service not publish!");
            return null;
        }
    }
}

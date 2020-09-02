package com.android.server.oppo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.engineer.OppoEngineerManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.IOppoUsageService;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Base64;
import android.util.Slog;
import com.android.server.BatteryService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.pm.Settings;
import com.android.server.slice.SliceClientPermissions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import oppo.util.OppoStatistics;
import vendor.oppo.usage.transmessage.V1_0.ITransmessge;

public final class OppoUsageService extends IOppoUsageService.Stub {
    private static final String CONNECTOR_FOR_PKGNAME_AND_TIME = "|";
    private static final int DATA_TYPE_APK_DEL_EVENT = 22;
    private static final int DATA_TYPE_APK_INSTALL_EVENT = 23;
    private static final int DATA_TYPE_APP_USAGE = 9;
    private static final int DATA_TYPE_BOOT_TIME = 1;
    private static final int DATA_TYPE_DIAL_OUT_DURATION = 6;
    private static final int DATA_TYPE_IMEI_NO = 2;
    private static final int DATA_TYPE_INCOMING_DURATION = 7;
    private static final int DATA_TYPE_MAX = 25;
    private static final int DATA_TYPE_MAX_CHARGE_CURRENT_CONFIG = 17;
    private static final int DATA_TYPE_MAX_CHARGE_TEMPERATURE_CONFIG = 19;
    private static final int DATA_TYPE_MCS_CONNECTID = 24;
    private static final int DATA_TYPE_MIN_CHARGE_TEMPERATURE_CONFIG = 18;
    private static final int DATA_TYPE_MOS_CONFIG = 16;
    private static final int DATA_TYPE_MSG_RECEIVE = 5;
    private static final int DATA_TYPE_MSG_SEND = 4;
    private static final int DATA_TYPE_ORIGINAL_SIM_DATA = 25;
    private static final int DATA_TYPE_PCBA_NO = 3;
    private static final int DATA_TYPE_PHONE_CALL_RECORD = 8;
    private static final int DATA_TYPE_PRODUCTLINE_LAST_TEST_FLAG = 21;
    private static final int DATA_TYPE_SECRECY_CONFIG = 20;
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (DEBUG_ALL || SystemProperties.getBoolean("persist.sys.assert.panic", false));
    /* access modifiers changed from: private */
    public static final boolean DEBUG_ALL = SystemProperties.getBoolean("persist.sys.usage.debug", false);
    /* access modifiers changed from: private */
    public static boolean DEBUG_SCORE_M = false;
    private static final long DELAY_TIME = 86400000;
    private static final boolean FEATURE_SUPPORT_COM_TEL = false;
    private static final int MAX_BATCH_COUNT = 10;
    private static final int MSG_GET_IMEI_NO = 1;
    private static final int MSG_GET_PCBA_NO = 2;
    private static final int MSG_SAVE_APK_INSTALL = 4;
    private static final int MSG_SAVE_BOOT_TIME = 3;
    private static final int MSG_SAVE_EMMC_INFO = 5;
    private static final int NORMAL_MSG_DELAY = 10000;
    private static final String PROP_NAME_PCBA_NO_MTK = "vendor.gsm.serial";
    private static final String PROP_NAME_PCBA_NO_QCOM = "gsm.serial";
    private static final String TAG = "OppoUsageService";
    /* access modifiers changed from: private */
    public static HidlDeathRecipient sHidlDeathRecipient = new HidlDeathRecipient();
    /* access modifiers changed from: private */
    public static ITransmessge sTransmessge;
    private Context mContext = null;
    private int mCurrentCountOfReceivedMsg = 0;
    private int mCurrentCountOfSendedMsg = 0;
    private int mCurrentDialOutDuration = 0;
    private int mCurrentIncomingDuration = 0;
    /* access modifiers changed from: private */
    public String mCurrentPcbaNO = null;
    private SimCardData mCurrentSimCardData = null;
    /* access modifiers changed from: private */
    public EmmcUsageCollector mEmmcInfoCollector = null;
    /* access modifiers changed from: private */
    public int mGetImeiNORetry = 7;
    /* access modifiers changed from: private */
    public int mGetPcbaNORetry = 7;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
        /* class com.android.server.oppo.OppoUsageService.AnonymousClass1 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1) {
                if (i != 2) {
                    if (i == 3) {
                        OppoUsageService.this.saveCurrentBootTime("startUp:" + OppoUsageService.this.getCurrentDateStr());
                    } else if (i == 4) {
                        String pkgName = (String) msg.obj;
                        if (pkgName != null) {
                            OppoUsageService.this.saveApkInstallEvent(pkgName);
                        }
                    } else if (i == 5) {
                        Slog.d(OppoUsageService.TAG, "msg to loadStorageDeviceInfo()..");
                        boolean res = OppoUsageService.this.mEmmcInfoCollector.loadStorageDeviceInfo();
                        Slog.d(OppoUsageService.TAG, "loadStorageDeviceInfo res:" + res);
                    }
                } else if (OppoUsageService.this.mGetPcbaNORetry != 0) {
                    if (OppoUsageService.this.getPcbaNoFromPhone()) {
                        int unused = OppoUsageService.this.mGetPcbaNORetry = 0;
                        OppoUsageService oppoUsageService = OppoUsageService.this;
                        oppoUsageService.savePcbaNoIfNew(oppoUsageService.mCurrentPcbaNO);
                        return;
                    }
                    OppoUsageService.access$610(OppoUsageService.this);
                    sendMessageDelayed(obtainMessage(2), 10000);
                }
            } else if (OppoUsageService.this.mGetImeiNORetry == 0) {
                sendMessageDelayed(obtainMessage(2), 10000);
            } else if (OppoUsageService.this.getImeiNoFromPhone()) {
                int unused2 = OppoUsageService.this.mGetImeiNORetry = 0;
                OppoUsageService.this.saveCurrentSimCardData();
                sendMessageDelayed(obtainMessage(2), 10000);
            } else {
                OppoUsageService.access$310(OppoUsageService.this);
                sendMessageDelayed(obtainMessage(1), 10000);
            }
        }
    };
    private boolean mHasGotDialOutDuration = false;
    private boolean mHasGotHistoryCountOfReceivedMsg = false;
    private boolean mHasGotHistoryCountOfSendedMsg = false;
    private boolean mHasGotIncomingDuration = false;
    private IntergrateReserveManager mIntergrateReserveManager = null;
    private Kahaleesi mKahaleesi = null;
    private BroadcastReceiver mPkgMsgReceiver = new BroadcastReceiver() {
        /* class com.android.server.oppo.OppoUsageService.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            Uri data;
            String action = intent.getAction();
            if (OppoUsageService.DEBUG) {
                Slog.d(OppoUsageService.TAG, "install pkg");
            }
            if (action.equals("android.intent.action.PACKAGE_ADDED") && (data = intent.getData()) != null) {
                String pkgName = data.getSchemeSpecificPart();
                if (OppoUsageService.DEBUG) {
                    Slog.d(OppoUsageService.TAG, "install pkg, name:" + pkgName);
                }
                if (pkgName != null) {
                    Message apkInstallMsg = OppoUsageService.this.mHandler.obtainMessage(4);
                    apkInstallMsg.obj = pkgName;
                    OppoUsageService.this.mHandler.sendMessageDelayed(apkInstallMsg, 20);
                }
            }
        }
    };
    private boolean mRawPartionInitOk = false;
    private String mRecordStrSlitter = Pattern.quote("#");
    private ScoreMonitor mScoreMonitor = null;
    /* access modifiers changed from: private */
    public Time mTimeObj = new Time();
    private Timer timer;

    private native byte[] native_engineer_read_dev_block(String str, int i, int i2);

    private native int native_engineer_write_dev_block(String str, byte[] bArr, int i);

    private native void native_finalizeRawPartition();

    private native String native_get_download_status(int i);

    private native boolean native_initUsageRawPartition();

    private native int native_readDataRecordCount(int i);

    private native String native_readDataStrContent(int i, int i2, int i3);

    private native String native_readDataStrContentForSingleRecord(int i);

    private native byte[] native_read_emmc_info(int i, int i2);

    private native int native_writeStringContentData(int i, String str, int i2);

    static /* synthetic */ int access$310(OppoUsageService x0) {
        int i = x0.mGetImeiNORetry;
        x0.mGetImeiNORetry = i - 1;
        return i;
    }

    static /* synthetic */ int access$610(OppoUsageService x0) {
        int i = x0.mGetPcbaNORetry;
        x0.mGetPcbaNORetry = i - 1;
        return i;
    }

    private static byte[] transferByteArrayList(List<Byte> byteArrayList) {
        if (byteArrayList == null || byteArrayList.size() == 0) {
            return null;
        }
        byte[] byteArray = new byte[byteArrayList.size()];
        for (int i = 0; i < byteArrayList.size(); i++) {
            byteArray[i] = byteArrayList.get(i).byteValue();
        }
        return byteArray;
    }

    private static String transferByteArrayToString(byte[] byteArray) {
        if (byteArray == null || byteArray.length <= 0) {
            return null;
        }
        int arrayLength = byteArray.length;
        int contentLength = 0;
        int i = 0;
        while (i < arrayLength && byteArray[i] != 0) {
            contentLength = i + 1;
            i++;
        }
        return new String(byteArray, 0, contentLength, StandardCharsets.UTF_8);
    }

    private static void initUsageHwService() {
        if (sTransmessge == null) {
            try {
                sTransmessge = ITransmessge.getService();
                if (sTransmessge != null) {
                    if (DEBUG) {
                        Slog.d(TAG, "achieve sTransmessge success");
                    }
                    sTransmessge.linkToDeath(sHidlDeathRecipient, 0);
                }
            } catch (Exception e) {
                Slog.e(TAG, "exception caught " + e.getMessage());
                sTransmessge = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class HidlDeathRecipient implements IHwBinder.DeathRecipient {
        private HidlDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Slog.d(OppoUsageService.TAG, "serviceDied! cookie = " + cookie);
            if (OppoUsageService.sTransmessge != null) {
                Slog.d(OppoUsageService.TAG, "unlinkToDeath!");
                try {
                    OppoUsageService.sTransmessge.unlinkToDeath(OppoUsageService.sHidlDeathRecipient);
                } catch (RemoteException e) {
                    Slog.i(OppoUsageService.TAG, "unable to unlink DeathRecipient");
                } catch (Throwable th) {
                    ITransmessge unused = OppoUsageService.sTransmessge = null;
                    throw th;
                }
                ITransmessge unused2 = OppoUsageService.sTransmessge = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean nativeWriteFile(int type, String fileName, String data, boolean append) {
        initUsageHwService();
        try {
            if (sTransmessge != null) {
                return sTransmessge.saveOppoUsageRecords(type, fileName, data, append);
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "nativeWriteFile exception caught " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static String nativeReadFile(int type, String path) {
        ArrayList<Byte> byteList;
        initUsageHwService();
        try {
            if (sTransmessge == null || (byteList = sTransmessge.readOppoUsageRecords(type, path)) == null) {
                return null;
            }
            return transferByteArrayToString(transferByteArrayList(byteList));
        } catch (Exception e) {
            Slog.e(TAG, "nativeReadOppoUsageRecords exception caught " + e.getMessage());
            return null;
        }
    }

    private class SimCardData {
        String mImeiStrSlot0 = null;
        String mImeiStrSlot1 = null;
        String mMeidStrSlot0 = null;
        String mMeidStrSlot1 = null;
        int mSimCardCount = 0;

        public SimCardData() {
        }

        public String toString() {
            return "[c:" + this.mSimCardCount + ", I0:" + this.mImeiStrSlot0 + ", M0:" + this.mMeidStrSlot0 + ", I1:" + this.mImeiStrSlot1 + ", M1:" + this.mMeidStrSlot1 + "]";
        }

        public boolean isValid() {
            return (this.mImeiStrSlot0 == null && this.mImeiStrSlot1 == null) ? false : true;
        }
    }

    public OppoUsageService(Context context) {
        if (DEBUG) {
            Slog.d(TAG, "OppoUsageService:create..");
        }
        this.mContext = context;
        this.mKahaleesi = new Kahaleesi();
        this.mIntergrateReserveManager = new IntergrateReserveManager(this.mContext, this.mKahaleesi);
        this.mRawPartionInitOk = true;
        if (DEBUG) {
            Slog.d(TAG, "OppoUsageService:initRes = " + this.mRawPartionInitOk);
        }
    }

    public void systemReady() {
        if (DEBUG) {
            Slog.i(TAG, "inform OPPO Usage Service systemReady");
        }
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(3), 20000);
        Handler handler2 = this.mHandler;
        handler2.sendMessageDelayed(handler2.obtainMessage(1), 30000);
        initPkgBroadcastReceive();
        this.mScoreMonitor = new ScoreMonitor(this.mContext);
        this.mEmmcInfoCollector = new EmmcUsageCollector(this.mContext);
        Handler handler3 = this.mHandler;
        handler3.sendMessageDelayed(handler3.obtainMessage(5), 40000);
        if (DEBUG) {
            Slog.i(TAG, "inform OPPO Usage Service systemReady end.");
        }
    }

    /* access modifiers changed from: private */
    public void saveCurrentBootTime(String bootTimeDateStr) {
        if (bootTimeDateStr == null || bootTimeDateStr.isEmpty()) {
            Slog.w(TAG, "saveCurrentBootTime:bootTimeDateStr is null or empty!");
            return;
        }
        ArrayList<String> contentList = new ArrayList<>();
        contentList.add(bootTimeDateStr);
        boolean saveRes = this.mIntergrateReserveManager.getBootTimeRecorder().saveContentList(contentList);
        if (DEBUG) {
            Slog.d(TAG, "saveCurrentBootTime, saveRes = " + saveRes);
        }
    }

    /* access modifiers changed from: private */
    public String getCurrentDateStr() {
        this.mTimeObj.setToNow();
        return this.mTimeObj.format("%Y-%m-%d %H:%M:%S");
    }

    public void testSaveSomeData(int dataType, String dataContent) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "testSaveSomeData failed for permission!");
            }
        } else if (DEBUG) {
            if (isValidDataType(dataType)) {
                if (DEBUG) {
                    Slog.d(TAG, "testSaveSomeData, type:" + dataType + ", content:" + dataContent);
                }
                if (dataType == 1) {
                    String dataStr = dataContent;
                    if (dataContent == null || dataContent.isEmpty()) {
                        dataStr = getCurrentDateStr();
                    }
                    saveCurrentBootTime(dataStr);
                } else if (dataType != 2) {
                    if (dataType != 3) {
                        if (DEBUG) {
                            Slog.d(TAG, "not provide for dataType:" + dataType + ", use service api instead.");
                        }
                    } else if ((dataContent == null || dataContent.isEmpty()) && getPcbaNoFromPhone()) {
                        savePcbaNoIfNew(this.mCurrentPcbaNO);
                    } else {
                        savePcbaNoIfNew(dataContent);
                    }
                } else if (this.mCurrentSimCardData != null || getImeiNoFromPhone()) {
                    saveCurrentSimCardData();
                }
            } else if (DEBUG) {
                Slog.e(TAG, "testSaveSomeData: invalid dataType!");
            }
        }
    }

    public List<String> getHistoryBootTime() {
        if (checkOppoUsagePermission()) {
            return this.mIntergrateReserveManager.getBootTimeRecorder().getCurHistoryFileInfoList();
        }
        if (!DEBUG) {
            return null;
        }
        Slog.d(TAG, "getHistoryBootTime failed for permission!");
        return null;
    }

    public List<String> getHistoryImeiNO() {
        if (checkOppoUsagePermission()) {
            return this.mIntergrateReserveManager.getSimCardDataRecorder().getHistorySimCardInfoList();
        }
        if (!DEBUG) {
            return null;
        }
        Slog.d(TAG, "getHistoryImeiNO failed for permission!");
        return null;
    }

    public List<String> getOriginalSimcardData() {
        if (checkOppoUsagePermission()) {
            return this.mIntergrateReserveManager.getSimCardDataRecorder().getOriginalSimCardData();
        }
        if (!DEBUG) {
            return null;
        }
        Slog.d(TAG, "getOriginalSimcardData failed for permission!");
        return null;
    }

    public List<String> getHistoryPcbaNO() {
        if (checkOppoUsagePermission()) {
            return this.mIntergrateReserveManager.getPcbaDataRecorder().getHistoryPcbaInfoList();
        }
        if (!DEBUG) {
            return null;
        }
        Slog.d(TAG, "getHistoryPcbaNO failed for permission!");
        return null;
    }

    public int getAppUsageHistoryRecordCount() {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "getAppUsageHistoryRecordCount failed for permission!");
        }
        return 0;
    }

    public List<String> getAppUsageHistoryRecords(int startIndex, int endIndex) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "getPhoneCallHistoryRecords failed for permission!");
            }
            return null;
        } else if (startIndex == 0 && endIndex == 0) {
            return this.mIntergrateReserveManager.getAppUsageRecorder().getCurHistoryFileInfoList();
        } else {
            if (1 == startIndex && 1 == endIndex) {
                return this.mIntergrateReserveManager.getAppUsageRecorder().getPreHistoryFileInfoList();
            }
            return null;
        }
    }

    public List<String> getAppUsageCountHistoryRecords(int startIndex, int endIndex) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "getPhoneCallHistoryRecords failed for permission!");
            }
            return null;
        } else if (startIndex == 0 && endIndex == 0) {
            return this.mIntergrateReserveManager.getAppUsageCountRecorder().getCurHistoryFileInfoList();
        } else {
            if (1 == startIndex && 1 == endIndex) {
                return this.mIntergrateReserveManager.getAppUsageCountRecorder().getPreHistoryFileInfoList();
            }
            return null;
        }
    }

    public List<String> getDialCountHistoryRecords(int startIndex, int endIndex) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "getPhoneCallHistoryRecords failed for permission!");
            }
            return null;
        } else if (startIndex == 0 && endIndex == 0) {
            return this.mIntergrateReserveManager.getDialCountRecorder().getCurHistoryFileInfoList();
        } else {
            if (1 == startIndex && 1 == endIndex) {
                return this.mIntergrateReserveManager.getDialCountRecorder().getPreHistoryFileInfoList();
            }
            return null;
        }
    }

    public boolean writeAppUsageHistoryRecord(String appName, String dateTime) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "writeAppUsageHistoryRecord failed for permission!");
            }
            return false;
        }
        String dateTimeStr = dateTime;
        if (appName == null || appName.isEmpty()) {
            if (DEBUG) {
                Slog.w(TAG, "appusage:contentStr is empty!");
            }
            return false;
        }
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            if (DEBUG) {
                Slog.w(TAG, "appusage:contentStr use current time instead!");
            }
            dateTimeStr = getCurrentDateStr();
            if (dateTimeStr == null || dateTimeStr.isEmpty()) {
                if (DEBUG) {
                    Slog.w(TAG, "appusage:contentStr failed:can't get dateTimeStr!");
                }
                return false;
            }
        }
        String dateStr = dateTimeStr.split(StringUtils.SPACE)[0];
        if (dateStr != null) {
            if (dateStr.equals(this.mIntergrateReserveManager.mDateStrOfApkUsage)) {
                this.mIntergrateReserveManager.mCountOneDayOfApkUsage++;
            } else {
                if (this.mIntergrateReserveManager.mCountOneDayOfApkUsage > 0) {
                    String tempContentStr = this.mIntergrateReserveManager.mDateStrOfApkUsage + " : " + this.mIntergrateReserveManager.mCountOneDayOfApkUsage;
                    if (DEBUG) {
                        Slog.d(TAG, "writeAppUsageHistoryRecord:tempContentStr = " + tempContentStr);
                    }
                    ArrayList<String> tempContentList = new ArrayList<>();
                    tempContentList.add(tempContentStr);
                    this.mIntergrateReserveManager.getAppUsageCountRecorder().saveContentList(tempContentList);
                }
                this.mIntergrateReserveManager.updateCountStr(dateStr, 1);
            }
        } else if (DEBUG) {
            Slog.d(TAG, "writeAppUsageHistoryRecord dateTime is invalid!");
        }
        String lastContentStr = appName + CONNECTOR_FOR_PKGNAME_AND_TIME + dateTimeStr;
        if (lastContentStr == null || lastContentStr.isEmpty()) {
            if (DEBUG) {
                Slog.w(TAG, "appusage:contentStr:lastContentStr is empty!");
            }
            return false;
        }
        ArrayList<String> contentList = new ArrayList<>();
        contentList.add(lastContentStr);
        return this.mIntergrateReserveManager.getAppUsageRecorder().saveContentList(contentList);
    }

    public int getHistoryCountOfSendedMsg() {
        if (checkOppoUsagePermission()) {
            return this.mIntergrateReserveManager.getStaticDataRecorder().getHistorySmsSendCount();
        }
        if (!DEBUG) {
            return 0;
        }
        Slog.d(TAG, "getHistoryCountOfSendedMsg failed for permission!");
        return 0;
    }

    public int getHistoryCountOfReceivedMsg() {
        if (checkOppoUsagePermission()) {
            return this.mIntergrateReserveManager.getStaticDataRecorder().getHistorySmsRecCount();
        }
        if (!DEBUG) {
            return 0;
        }
        Slog.d(TAG, "getHistoryCountOfReceivedMsg failed for permission!");
        return 0;
    }

    public boolean accumulateHistoryCountOfSendedMsg(int newCountIncrease) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "accumulateHistoryCountOfSendedMsg failed for permission!");
            }
            return false;
        } else if (newCountIncrease > 0) {
            return this.mIntergrateReserveManager.getStaticDataRecorder().accumulateSmsSendCount(newCountIncrease);
        } else {
            if (DEBUG) {
                Slog.w(TAG, "accumulateHistoryCountOfSendedMsg:illegal param!");
            }
            return false;
        }
    }

    public boolean accumulateHistoryCountOfReceivedMsg(int newCountIncrease) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "accumulateHistoryCountOfReceivedMsg failed for permission!");
            }
            return false;
        } else if (newCountIncrease > 0) {
            return this.mIntergrateReserveManager.getStaticDataRecorder().accumulateSmsRecCount(newCountIncrease);
        } else {
            if (DEBUG) {
                Slog.w(TAG, "accumulateHistoryCountOfReceivedMsg:illegal param!");
            }
            return false;
        }
    }

    public int getDialOutDuration() {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "getDialOutDuration failed for permission!");
        }
        return 0;
    }

    public int getInComingCallDuration() {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "getInComingCallDuration failed for permission!");
        }
        return 0;
    }

    private int strValueToIntValue(String strValue, int defaultValue) {
        if (strValue == null || strValue.isEmpty()) {
            if (DEBUG) {
                Slog.d(TAG, "strValueToIntValue, strValue is empty!");
            }
            return defaultValue;
        }
        try {
            int intValue = Integer.parseInt(strValue);
            if (DEBUG) {
                Slog.d(TAG, "strValueToIntValue, intValue:" + intValue);
            }
            if (intValue >= 0) {
                return intValue;
            }
            if (DEBUG) {
                Slog.w(TAG, "strValueToIntValue:parse data failed!");
            }
            return 0;
        } catch (NumberFormatException e) {
            if (DEBUG) {
                Slog.w(TAG, "strValueToIntValue, parse failed!");
            }
            return defaultValue;
        }
    }

    public boolean accumulateDialOutDuration(int durationInMinute) {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "accumulateDialOutDuration failed for permission!");
        }
        return false;
    }

    public boolean accumulateInComingCallDuration(int durationInMinute) {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "accumulateInComingCallDuration failed for permission!");
        }
        return false;
    }

    public int getHistoryRecordsCountOfPhoneCalls() {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "getHistoryRecordsCountOfPhoneCalls failed for permission!");
        }
        return 0;
    }

    public List<String> getPhoneCallHistoryRecords(int startIndex, int endIndex) {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "getPhoneCallHistoryRecords failed for permission!");
        }
        return null;
    }

    public boolean writePhoneCallHistoryRecord(String phoneNoStr, String dateTime) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "writePhoneCallHistoryRecord failed for permission!");
            }
            return false;
        } else if (dateTime == null || dateTime.isEmpty()) {
            Slog.d(TAG, "writePhoneCallHistoryRecord dateTime is null!");
            return false;
        } else {
            String dateStr = dateTime.split(StringUtils.SPACE)[0];
            if (dateStr != null) {
                if (dateStr.equals(this.mIntergrateReserveManager.mDateStrOfPhoneCall)) {
                    this.mIntergrateReserveManager.mCountOneDayOfPhoneCall++;
                } else {
                    if (this.mIntergrateReserveManager.mCountOneDayOfPhoneCall > 0) {
                        String contentStr = this.mIntergrateReserveManager.mDateStrOfPhoneCall + " : " + this.mIntergrateReserveManager.mCountOneDayOfPhoneCall;
                        if (DEBUG) {
                            Slog.d(TAG, "writePhoneCallHistoryRecord:contentStr = " + contentStr);
                        }
                        ArrayList<String> contentList = new ArrayList<>();
                        contentList.add(contentStr);
                        this.mIntergrateReserveManager.getDialCountRecorder().saveContentList(contentList);
                    }
                    this.mIntergrateReserveManager.updateCountStr(dateStr, 0);
                }
            } else if (DEBUG) {
                Slog.d(TAG, "writePhoneCallHistoryRecord dateTime is invalid!");
            }
            return false;
        }
    }

    public void shutDown() {
        if (checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.i(TAG, "shutDown OppoUsageService.");
            }
            saveCurrentBootTime("shutDown:" + getCurrentDateStr());
        } else if (DEBUG) {
            Slog.d(TAG, "shutDown failed for permission!");
        }
    }

    private boolean isValidDataType(int dataType) {
        if (dataType < 1 || dataType > 25) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean getImeiNoFromPhone() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (telephonyManager == null) {
                if (DEBUG) {
                    Slog.e(TAG, "TelephonyManager service is not ready!");
                }
                return false;
            }
            int simCardCount = telephonyManager.getSimCount();
            this.mCurrentSimCardData = new SimCardData();
            this.mCurrentSimCardData.mSimCardCount = simCardCount;
            this.mCurrentSimCardData.mImeiStrSlot0 = telephonyManager.getImei(0);
            this.mCurrentSimCardData.mMeidStrSlot0 = telephonyManager.getMeid(0);
            if (this.mCurrentSimCardData.mSimCardCount > 1) {
                this.mCurrentSimCardData.mImeiStrSlot1 = telephonyManager.getImei(1);
                this.mCurrentSimCardData.mMeidStrSlot1 = telephonyManager.getMeid(1);
            }
            if (DEBUG) {
                Slog.d(TAG, "current simCard data:" + this.mCurrentSimCardData);
            }
            return true;
        } catch (Exception ex) {
            if (DEBUG) {
                Slog.e(TAG, "getDeviceId Exception:" + ex.getMessage());
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void saveCurrentSimCardData() {
        if (this.mCurrentSimCardData != null) {
            boolean saveRes = this.mIntergrateReserveManager.getSimCardDataRecorder().saveSimCardInfo(this.mCurrentSimCardData);
            if (DEBUG) {
                Slog.d(TAG, "saveCurrentSimCardData:" + saveRes);
            }
        }
    }

    /* access modifiers changed from: private */
    public void savePcbaNoIfNew(String numberStr) {
        if (numberStr != null && !numberStr.isEmpty()) {
            boolean saveRes = this.mIntergrateReserveManager.getPcbaDataRecorder().savePcbaInfo(numberStr);
            if (DEBUG) {
                Slog.d(TAG, "savePcbaNoIfNew saveRes:" + saveRes);
            }
        } else if (DEBUG) {
            Slog.w(TAG, "savePcbaNoIfNew:numberStr is empty!");
        }
    }

    /* access modifiers changed from: private */
    public boolean getPcbaNoFromPhone() {
        String pcbaNOStr;
        if ("qcom".contains(SystemProperties.get("ro.boot.hardware", ""))) {
            pcbaNOStr = SystemProperties.get(PROP_NAME_PCBA_NO_QCOM);
        } else {
            pcbaNOStr = SystemProperties.get(PROP_NAME_PCBA_NO_MTK);
        }
        if (pcbaNOStr == null || pcbaNOStr.isEmpty()) {
            return false;
        }
        this.mCurrentPcbaNO = pcbaNOStr;
        return true;
    }

    private boolean splitStr(String contentStr, String strSlitter, ArrayList<String> recordList) {
        if (contentStr == null || contentStr.isEmpty()) {
            if (DEBUG) {
                Slog.w(TAG, "splitStr:contentStr is empty!");
            }
            return false;
        } else if (strSlitter == null || recordList == null) {
            if (DEBUG) {
                Slog.w(TAG, "splitStr:strSlitter or recordList is empty!");
            }
            return false;
        } else {
            if (DEBUG) {
                Slog.d(TAG, "splitStr, strSlitter:" + strSlitter + ", contentStr:" + contentStr);
            }
            String[] tmpResArray = contentStr.split(strSlitter);
            if (tmpResArray == null || tmpResArray.length <= 0) {
                return true;
            }
            for (String str : tmpResArray) {
                recordList.add(str);
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        native_finalizeRawPartition();
        OppoUsageService.super.finalize();
    }

    private boolean updateChargeInfomation(int dataType, int value) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(value);
        String contentStr = strBuilder.toString();
        if (DEBUG) {
            Slog.d(TAG, "updateChargeInfomation:contentStr = " + contentStr);
        }
        if (contentStr == null || contentStr.isEmpty()) {
            if (DEBUG) {
                Slog.w(TAG, "updateChargeInfomation:contentStr is empty for type:" + dataType);
            }
            return false;
        } else if (goToKahaleesi(dataType, contentStr, 1, false) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateMaxChargeCurrent(int current) {
        return updateChargeInfomation(17, current);
    }

    public boolean updateMaxChargeTemperature(int temp) {
        return updateChargeInfomation(19, temp);
    }

    public boolean updateMinChargeTemperature(int temp) {
        return updateChargeInfomation(18, temp);
    }

    private int getChargeInformation(int dataType, int default_value) {
        String chargeInfomation = comeBackFromKahaleesiSingle(dataType, false);
        int value = strValueToIntValue(chargeInfomation, default_value);
        if (DEBUG) {
            Slog.d(TAG, "getChargeInformation, chargeInfomation:" + chargeInfomation + ", value:" + value);
        }
        return value;
    }

    public int getMaxChargeCurrent() {
        return getChargeInformation(17, Integer.MIN_VALUE);
    }

    public int getMaxChargeTemperature() {
        return getChargeInformation(19, Integer.MIN_VALUE);
    }

    public int getMinChargeTemperature() {
        return getChargeInformation(18, Integer.MAX_VALUE);
    }

    public byte[] engineerReadDevBlock(String partion, int offset, int count) {
        if (partion != null && !partion.isEmpty() && count > 0) {
            return native_engineer_read_dev_block(partion, offset, count);
        }
        if (!DEBUG) {
            return null;
        }
        Slog.d(TAG, "engineerReadDevBlock parameter invalid");
        return null;
    }

    public int engineerWriteDevBlock(String partion, byte[] content, int offset) {
        if (partion != null && !partion.isEmpty() && content != null) {
            return native_engineer_write_dev_block(partion, content, offset);
        }
        if (!DEBUG) {
            return -1;
        }
        Slog.d(TAG, "engineerWriteDevBlock parameter invalid");
        return -1;
    }

    public String getDownloadStatusString(int part) {
        return native_get_download_status(part);
    }

    public String loadSecrecyConfig() {
        return comeBackFromKahaleesiSingle(20, false);
    }

    public int saveSecrecyConfig(String content) {
        return goToKahaleesi(20, content, 1, false);
    }

    public int getProductLineLastTestFlag() {
        return strValueToIntValue(comeBackFromKahaleesiSingle(21, false), -1);
    }

    public boolean setProductLineLastTestFlag(int flag) {
        return goToKahaleesi(21, Integer.toString(flag), 1, false) > 0;
    }

    public boolean recordApkDeleteEvent(String deleteAppPkgName, String callerAppPkgName, String dateTime) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "recordApkDeleteEvent failed for permission!");
            }
            return false;
        } else if (deleteAppPkgName == null || deleteAppPkgName.isEmpty() || callerAppPkgName == null || callerAppPkgName.isEmpty()) {
            return false;
        } else {
            ArrayList<String> contentList = new ArrayList<>();
            contentList.add(deleteAppPkgName + SliceClientPermissions.SliceAuthority.DELIMITER + callerAppPkgName);
            return this.mIntergrateReserveManager.getAppUninstallRecorder().saveContentList(contentList);
        }
    }

    public int getApkDeleteEventRecordCount() {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "getApkDeleteEventRecordCount failed for permission!");
        }
        return 0;
    }

    public List<String> getApkDeleteEventRecords(int startIndex, int endIndex) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "getApkDeleteEventRecords failed for permission!");
            }
            return null;
        } else if (startIndex == 0 && endIndex == 0) {
            return this.mIntergrateReserveManager.getAppUninstallRecorder().getCurHistoryFileInfoList();
        } else {
            if (1 == startIndex && 1 == endIndex) {
                return this.mIntergrateReserveManager.getAppUninstallRecorder().getPreHistoryFileInfoList();
            }
            return null;
        }
    }

    private void initPkgBroadcastReceive() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addDataScheme(Settings.ATTR_PACKAGE);
        this.mContext.registerReceiver(this.mPkgMsgReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public void saveApkInstallEvent(String pkgName) {
        boolean saveRes = recordApkInstallEvent(pkgName, "installer", null);
        if (DEBUG) {
            Slog.d(TAG, "saveApkInstallEvent, saveRes:" + saveRes);
        }
    }

    public boolean recordApkInstallEvent(String installAppPkgName, String callerAppPkgName, String dateTime) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "recordApkInstallEvent failed for permission!");
            }
            return false;
        } else if (installAppPkgName == null || installAppPkgName.isEmpty() || callerAppPkgName == null || callerAppPkgName.isEmpty()) {
            return false;
        } else {
            String content = installAppPkgName + SliceClientPermissions.SliceAuthority.DELIMITER + callerAppPkgName;
            if (DEBUG) {
                Slog.d(TAG, "recordApkInstallEvent:" + content);
            }
            ArrayList<String> contentList = new ArrayList<>();
            contentList.add(content);
            return this.mIntergrateReserveManager.getAppInstallRecorder().saveContentList(contentList);
        }
    }

    public int getApkInstallEventRecordCount() {
        if (!checkOppoUsagePermission() && DEBUG) {
            Slog.d(TAG, "getApkInstallEventRecordCount failed for permission!");
        }
        return 0;
    }

    public List<String> getApkInstallEventRecords(int startIndex, int endIndex) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "getApkInstallEventRecords failed for permission!");
            }
            return null;
        } else if (startIndex == 0 && endIndex == 0) {
            return this.mIntergrateReserveManager.getAppInstallRecorder().getCurHistoryFileInfoList();
        } else {
            if (1 == startIndex && 1 == endIndex) {
                return this.mIntergrateReserveManager.getAppInstallRecorder().getPreHistoryFileInfoList();
            }
            return null;
        }
    }

    public boolean recordMcsConnectID(String connectID) {
        if (!checkOppoUsagePermission()) {
            if (DEBUG) {
                Slog.d(TAG, "recordMcsConnectID failed for permission!");
            }
            return false;
        } else if (connectID != null && !connectID.isEmpty()) {
            return this.mIntergrateReserveManager.getMcsDataRecorder().saveMcsInfo(connectID);
        } else {
            if (DEBUG) {
                Slog.w(TAG, "recordMcsConnectID:connectID empty!");
            }
            return false;
        }
    }

    public String getMcsConnectID() {
        if (checkOppoUsagePermission()) {
            return this.mIntergrateReserveManager.getMcsDataRecorder().getMcsInfo();
        }
        if (!DEBUG) {
            return null;
        }
        Slog.d(TAG, "getMcsConnectID failed for permission!");
        return null;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        EmmcUsageCollector emmcUsageCollector;
        HashSet<String> argSet = new HashSet<>();
        for (String arg : args) {
            argSet.add(arg);
        }
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            if (argSet.contains("--openScLog") && this.mScoreMonitor != null) {
                pw.println("oppo usage state:open sc log");
                DEBUG_SCORE_M = true;
            } else if (argSet.contains("--emulateSc") && this.mScoreMonitor != null) {
                pw.println("oppo usage state:emulateSc");
                this.mScoreMonitor.emulateScoreMonitorStart();
            } else if (argSet.contains("--emmcD") && this.mEmmcInfoCollector != null) {
                pw.println("oppo usage state:open emmcD");
                this.mEmmcInfoCollector.mDebugEmmcInfo = true;
            } else if (argSet.contains("--emmc") && (emmcUsageCollector = this.mEmmcInfoCollector) != null) {
                boolean res2 = emmcUsageCollector.loadStorageDeviceInfo();
                Slog.d(TAG, "loadStorageDeviceInfo:" + res2);
                pw.println("loadStorageDeviceInfo:" + res2);
            }
        }
    }

    private boolean checkOppoUsagePermission() {
        return isSystemAppByUid(Binder.getCallingUid());
    }

    private boolean isSystemAppByUid(int uid) {
        if (uid < 10000) {
            if (DEBUG) {
                Slog.d(TAG, "isSystemApp system app, uid =" + uid);
            }
            return true;
        }
        PackageManager pm = this.mContext.getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null || packages.length == 0) {
            if (DEBUG) {
                Slog.d(TAG, "isSystemApp, no pkgs for uid:" + uid);
            }
            return true;
        }
        for (String packageName : packages) {
            if (isSystemAppByPkgName(packageName, pm)) {
                if (DEBUG) {
                    Slog.d(TAG, "isSystemApp:" + packageName);
                }
                return true;
            }
        }
        if (!DEBUG) {
            return false;
        }
        Slog.d(TAG, "not system app for uid:" + uid);
        return false;
    }

    private boolean isSystemAppByPkgName(String packageName, PackageManager pm) {
        if (packageName != null) {
            try {
                PackageInfo pkgInfo = pm.getPackageInfo(packageName, 0);
                ApplicationInfo info = pkgInfo != null ? pkgInfo.applicationInfo : null;
                if (!(info == null || (info.flags & 1) == 0)) {
                    if (DEBUG) {
                        Slog.d(TAG, "isSystemAppByPkgName system app");
                    }
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                if (DEBUG) {
                    Slog.d(TAG, "isSystemAppByPkgName NameNotFoundException, return false");
                }
                return false;
            }
        }
        return false;
    }

    private class ScoreMonitor {
        private static final String ACTION_MONITOR_TIMER = "com.oppo.ScoreAppMonitor.MONITOR_TIMER";
        private static final String BROADCAST_ACTION_INFO_UPLOAD = "com.oppo.ScoreAppMonitor.UPLOAD";
        private static final int DEFAULT_START_TIME = 17;
        private static final String EXTRA_KEY_FILE_NAME = "filename";
        private static final String PERMISSION_START_MONITOR = "com.oppo.ScoreAppMonitor.permission.START_MONITOR";
        private static final String PROP_CONTROL_VERSION = "persist.version.confidential";
        private static final String SCORE_MONITOR_PACKAGE = "com.oppo.ScoreAppMonitor";
        private static final String SCORE_MONITOR_SERVICE = "com.oppo.ScoreAppMonitor.MonitorService";
        private AlarmManager mAlarmManager = null;
        private boolean mIsControlVersion = false;
        private Context mLocalContext = null;
        private BroadcastReceiver mScoreMonitorReceiver = new BroadcastReceiver() {
            /* class com.android.server.oppo.OppoUsageService.ScoreMonitor.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (OppoUsageService.DEBUG_SCORE_M) {
                    Slog.d(OppoUsageService.TAG, "mScoreMonitorReceiver onReceive:" + action);
                }
                if (action != null) {
                    if (action.equals(ScoreMonitor.BROADCAST_ACTION_INFO_UPLOAD)) {
                        String uploadFileName = intent.getStringExtra(ScoreMonitor.EXTRA_KEY_FILE_NAME);
                        if (OppoUsageService.DEBUG_SCORE_M) {
                            Slog.d(OppoUsageService.TAG, "mScoreMonitorReceiver onReceive:" + uploadFileName);
                        }
                        if (uploadFileName != null) {
                            ScoreMonitor.this.onFileUpload(uploadFileName);
                        }
                    } else if (action.equals(ScoreMonitor.ACTION_MONITOR_TIMER)) {
                        ScoreMonitor.this.startScoreMonitor();
                    }
                }
            }
        };

        public ScoreMonitor(Context context) {
            this.mLocalContext = context;
            initScoreMonitor();
        }

        private void initScoreMonitor() {
            this.mIsControlVersion = SystemProperties.getBoolean(PROP_CONTROL_VERSION, false);
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "initScoreMonitor, mIsControlVersion:" + this.mIsControlVersion);
            }
            if (this.mIsControlVersion) {
                regMonitorReceiver();
                initMonitorTimer();
            }
        }

        private void regMonitorReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_MONITOR_TIMER);
            intentFilter.addAction(BROADCAST_ACTION_INFO_UPLOAD);
            this.mLocalContext.registerReceiver(this.mScoreMonitorReceiver, intentFilter);
        }

        private void initMonitorTimer() {
            long interval;
            int startTimeMinute;
            int startTimeHour;
            this.mAlarmManager = (AlarmManager) this.mLocalContext.getSystemService("alarm");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mLocalContext, 0, new Intent(ACTION_MONITOR_TIMER, (Uri) null), 0);
            int startTimeHour2 = 17;
            int startTimeMinute2 = 0;
            long interval2 = 86400000;
            try {
                if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    startTimeHour2 = SystemProperties.getInt("persist.sys.sc.h", 17);
                    startTimeMinute2 = SystemProperties.getInt("persist.sys.sc.m", 0);
                    interval2 = Long.valueOf(SystemProperties.getLong("persist.sys.sc.i", 86400000)).longValue();
                }
                startTimeHour = startTimeHour2;
                startTimeMinute = startTimeMinute2;
                interval = interval2;
            } catch (Exception e) {
                startTimeHour = 17;
                startTimeMinute = 0;
                interval = 86400000;
            }
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "initMonitorTimer, startTimeHour:" + startTimeHour + ", startTimeMinute:" + startTimeMinute + ", interval:" + interval);
            }
            this.mAlarmManager.setRepeating(0, getTime(startTimeHour, startTimeMinute), interval, pendingIntent);
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "initMonitorTimer, start timer.");
            }
        }

        public boolean isControlVersion() {
            return this.mIsControlVersion;
        }

        /* access modifiers changed from: package-private */
        public void emulateScoreMonitorStart() {
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "emulateScoreMonitorStart ...");
            }
            this.mLocalContext.sendBroadcast(new Intent(ACTION_MONITOR_TIMER, (Uri) null));
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "emulateScoreMonitorStart end.");
            }
        }

        /* access modifiers changed from: private */
        public void startScoreMonitor() {
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "startScoreMonitor, enable:" + this.mIsControlVersion);
            }
            if (this.mIsControlVersion) {
                ComponentName scoreMonitorServiceComponent = new ComponentName(SCORE_MONITOR_PACKAGE, SCORE_MONITOR_SERVICE);
                try {
                    Intent serviceIntent = new Intent();
                    serviceIntent.setComponent(scoreMonitorServiceComponent);
                    this.mLocalContext.startService(serviceIntent);
                } catch (Exception e) {
                    Slog.w(OppoUsageService.TAG, "startScoreMonitor failed!", e);
                }
                if (OppoUsageService.DEBUG_SCORE_M) {
                    Slog.d(OppoUsageService.TAG, "startScoreMonitor, send start action.");
                }
            }
        }

        private long getTime(int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(11, hourOfDay);
            calendar.set(12, minute);
            calendar.set(13, 0);
            calendar.set(14, 0);
            long time = calendar.getTimeInMillis();
            if (System.currentTimeMillis() <= time) {
                return time;
            }
            calendar.add(5, 1);
            return calendar.getTimeInMillis();
        }

        /* access modifiers changed from: private */
        public void onFileUpload(String fileName) {
        }
    }

    private class Kahaleesi {
        private static final String KEY_ALGORITHM = "DES";
        private static final String KEY_STRORE_OF_K = "AndroidKeyStore";
        private byte[] mIv = {6, 5, 6, 5, 7, 3, 6, 9};
        private String mNameOfKahaleesi = "Kahalees";
        private Cipher mPowerOfFire = null;
        private Cipher mPowerOfIce = null;
        private boolean mfrozen = false;

        public Kahaleesi() {
            try {
                IvParameterSpec zeroIv = new IvParameterSpec(this.mIv);
                SecretKey scKey = new SecretKeySpec(this.mNameOfKahaleesi.getBytes(), KEY_ALGORITHM);
                this.mPowerOfIce = Cipher.getInstance(KEY_ALGORITHM);
                this.mPowerOfIce.init(1, scKey, zeroIv);
                this.mPowerOfFire = Cipher.getInstance(KEY_ALGORITHM);
                this.mPowerOfFire.init(2, scKey, zeroIv);
            } catch (Exception e) {
                Slog.e(OppoUsageService.TAG, "Failed to encrypt key", e);
                this.mfrozen = false;
            }
        }

        public String frozenIntoIce(String sword) {
            if (!this.mfrozen || sword == null || sword.isEmpty()) {
                return sword;
            }
            try {
                this.mPowerOfIce.doFinal(sword.getBytes());
                return sword;
            } catch (Exception e) {
                Slog.w(OppoUsageService.TAG, "Failed frozenIntoIce ", e);
                return sword;
            }
        }

        public String unfrozenFromFire(String ice) {
            if (!this.mfrozen || ice == null || ice.isEmpty()) {
                return ice;
            }
            try {
                this.mPowerOfFire.doFinal(ice.getBytes());
                return ice;
            } catch (Exception e) {
                Slog.w(OppoUsageService.TAG, "Failed unfrozenFromFire ", e);
                return ice;
            }
        }
    }

    private int goToKahaleesi(int dataType, String dataContent, int isSingleRecord, boolean frozen) {
        if (dataContent == null) {
            return 0;
        }
        return native_writeStringContentData(dataType, frozen ? this.mKahaleesi.frozenIntoIce(dataContent) : dataContent, isSingleRecord);
    }

    private String comeBackFromKahaleesi(int dataType, int startIndex, int endIndex, boolean unfrozen) {
        String res = native_readDataStrContent(dataType, startIndex, endIndex);
        return unfrozen ? this.mKahaleesi.unfrozenFromFire(res) : res;
    }

    private String comeBackFromKahaleesiSingle(int dataType, boolean unfrozen) {
        String res = native_readDataStrContentForSingleRecord(dataType);
        return unfrozen ? this.mKahaleesi.unfrozenFromFire(res) : res;
    }

    private class IntergrateReserveManager {
        private static final String APP_DEL_FILE_NAME = "apd";
        private static final String APP_INSTALL_FILE_NAME = "api";
        private static final String APP_USAGE_COUNT_FILE_NAME = "apuc";
        private static final String APP_USAGE_FILE_NAME = "apu";
        private static final String BOOT_TIME_FILE_NAME = "bt";
        private static final String DIAL_COUNT_FILE_NAME = "dc";
        public static final int ERRO_NO_ITEGRATE = -10;
        public static final String ITRM_TAG = "IntergrateReserveManager";
        public static final int OPPORESEVE2_TYPE_END = 1017;
        public static final int OPPORESEVE2_TYPE_START = 1000;
        public static final String PROP_STR_ITRM = "ro.sys.reserve.integrate";
        public static final int READ_OPPORESEVE2_TYPE_CONNECTIVITY = 1009;
        public static final int READ_OPPORESEVE2_TYPE_LOST_FOUND = 1013;
        public static final int READ_OPPORESEVE2_TYPE_MEDIA = 1010;
        public static final int READ_OPPORESEVE2_TYPE_MEDIA_LOG_USAGE = 1016;
        public static final int READ_OPPORESEVE2_TYPE_PHOENIX = 1014;
        public static final int READ_OPPORESEVE2_TYPE_RADIO = 1011;
        public static final int READ_OPPORESEVE2_TYPE_RECOVERY_INFO = 1015;
        public static final int READ_OPPORESEVE2_TYPE_SYSTEM = 1012;
        public static final String STR_RESERVE_DIR = "/mnt/vendor/opporeserve/media/log/usage/";
        public static final String STR_USAGE_CACHE = "cache";
        public static final String STR_USAGE_PERSIST = "persist";
        private static final String TEL_CALL_FILE_NAME = "tc";
        public static final int WRITE_OPPORESEVE2_TYPE_CONNECTIVITY = 1001;
        public static final int WRITE_OPPORESEVE2_TYPE_LOST_FOUND = 1005;
        public static final int WRITE_OPPORESEVE2_TYPE_MEDIA = 1002;
        public static final int WRITE_OPPORESEVE2_TYPE_MEDIA_LOG_USAGE = 1008;
        public static final int WRITE_OPPORESEVE2_TYPE_PHOENIX = 1006;
        public static final int WRITE_OPPORESEVE2_TYPE_RADIO = 1003;
        public static final int WRITE_OPPORESEVE2_TYPE_RECOVERY_INFO = 1007;
        public static final int WRITE_OPPORESEVE2_TYPE_SYSTEM = 1004;
        private CacheRecordsRecorder mAppDeleteRecorder = null;
        private CacheRecordsRecorder mAppInstallRecorder = null;
        private CacheRecordsRecorder mAppUsageCountRecorder = null;
        private CacheRecordsRecorder mAppUsageRecorder = null;
        private CacheRecordsRecorder mBootTimeRecorder = null;
        private CacheRecordsRecorder mComInfoRecorder = null;
        public int mCountOneDayOfApkUsage = 0;
        public int mCountOneDayOfPhoneCall = 0;
        public String mDateStrOfApkUsage = "0000-00-00";
        public String mDateStrOfPhoneCall = "0000-00-00";
        private CacheRecordsRecorder mDialCountRecorder = null;
        private Context mLocalContext = null;
        private Kahaleesi mLocalKahaleesi = null;
        private McsDataRecorder mMcsDataRecorder = null;
        private PcbaDataRecorder mPcbaDataRecorder = null;
        private SimCardDataRecorder mSimCardDataRecorder = null;
        private StatsticDataRecorder mStatsticDataRecorder = null;

        public IntergrateReserveManager(Context context, Kahaleesi kahaleesi) {
            this.mLocalContext = context;
            this.mLocalKahaleesi = kahaleesi;
            this.mSimCardDataRecorder = new SimCardDataRecorder(this.mLocalContext, this.mLocalKahaleesi);
            this.mPcbaDataRecorder = new PcbaDataRecorder(this.mLocalContext, this.mLocalKahaleesi);
            this.mMcsDataRecorder = new McsDataRecorder(this.mLocalContext, this.mLocalKahaleesi);
            this.mStatsticDataRecorder = new StatsticDataRecorder(this.mLocalContext, this.mLocalKahaleesi);
            this.mAppUsageRecorder = new CacheRecordsRecorder(this.mLocalContext, this.mLocalKahaleesi, APP_USAGE_FILE_NAME);
            this.mAppInstallRecorder = new CacheRecordsRecorder(this.mLocalContext, this.mLocalKahaleesi, APP_INSTALL_FILE_NAME);
            this.mAppDeleteRecorder = new CacheRecordsRecorder(this.mLocalContext, this.mLocalKahaleesi, APP_DEL_FILE_NAME);
            this.mComInfoRecorder = new CacheRecordsRecorder(this.mLocalContext, this.mLocalKahaleesi, TEL_CALL_FILE_NAME);
            this.mBootTimeRecorder = new CacheRecordsRecorder(this.mLocalContext, this.mLocalKahaleesi, BOOT_TIME_FILE_NAME);
            this.mAppUsageCountRecorder = new CacheRecordsRecorder(this.mLocalContext, this.mLocalKahaleesi, APP_USAGE_COUNT_FILE_NAME);
            this.mDialCountRecorder = new CacheRecordsRecorder(this.mLocalContext, this.mLocalKahaleesi, DIAL_COUNT_FILE_NAME);
        }

        public void setKahaleesi(Kahaleesi kahaleesi) {
            this.mLocalKahaleesi = kahaleesi;
        }

        public SimCardDataRecorder getSimCardDataRecorder() {
            return this.mSimCardDataRecorder;
        }

        public PcbaDataRecorder getPcbaDataRecorder() {
            return this.mPcbaDataRecorder;
        }

        public McsDataRecorder getMcsDataRecorder() {
            return this.mMcsDataRecorder;
        }

        public CacheRecordsRecorder getAppUsageRecorder() {
            return this.mAppUsageRecorder;
        }

        public CacheRecordsRecorder getAppInstallRecorder() {
            return this.mAppInstallRecorder;
        }

        public CacheRecordsRecorder getAppUninstallRecorder() {
            return this.mAppDeleteRecorder;
        }

        public CacheRecordsRecorder getComInfoRecorder() {
            return this.mComInfoRecorder;
        }

        public CacheRecordsRecorder getBootTimeRecorder() {
            return this.mBootTimeRecorder;
        }

        public CacheRecordsRecorder getAppUsageCountRecorder() {
            return this.mAppUsageCountRecorder;
        }

        public CacheRecordsRecorder getDialCountRecorder() {
            return this.mDialCountRecorder;
        }

        public StatsticDataRecorder getStaticDataRecorder() {
            return this.mStatsticDataRecorder;
        }

        public void updateCountStr(String dateStr, int type) {
            if (dateStr == null || dateStr.isEmpty()) {
                Slog.d(OppoUsageService.TAG, "updateCountStr: the dataStr is null !");
            } else if (type == 0) {
                this.mDateStrOfPhoneCall = dateStr;
                this.mCountOneDayOfPhoneCall = 1;
            } else if (type == 1) {
                this.mDateStrOfApkUsage = dateStr;
                this.mCountOneDayOfApkUsage = 1;
            }
        }
    }

    private class UsageDataRecorder {
        protected String mCacheFileDir = "cache/";
        protected Context mContext = null;
        protected int mFileReadType = IntergrateReserveManager.READ_OPPORESEVE2_TYPE_MEDIA_LOG_USAGE;
        protected int mFileWriteType = IntergrateReserveManager.WRITE_OPPORESEVE2_TYPE_MEDIA_LOG_USAGE;
        protected Kahaleesi mLocalKahaleesi = null;
        protected String mPersistFileDir = "persist/";
        protected File mUsageCacheFileDir = null;
        protected File mUsagePersistFileDir = null;

        public UsageDataRecorder(Context context, Kahaleesi localKahaleesi) {
            this.mContext = context;
            this.mLocalKahaleesi = localKahaleesi;
            this.mUsageCacheFileDir = new File("/mnt/vendor/opporeserve/media/log/usage/cache");
            this.mUsagePersistFileDir = new File("/mnt/vendor/opporeserve/media/log/usage/persist");
        }

        public ArrayList<String> loadHistoryDataFromFile(int type, String srcFile) {
            String result = OppoUsageService.nativeReadFile(type, srcFile);
            if (result == null || result.isEmpty()) {
                return null;
            }
            return new ArrayList<>(Arrays.asList(result.split(StringUtils.LF)));
        }

        /* access modifiers changed from: protected */
        public boolean doSaveData(ArrayList<String> contentList, String filePath, boolean append, boolean frozen) {
            if (OppoUsageService.DEBUG_ALL) {
                Slog.d(OppoUsageService.TAG, "doSaveData destFile:" + filePath + ", contentList:" + contentList);
            }
            boolean result = false;
            if (contentList == null || contentList.size() <= 0) {
                return false;
            }
            Iterator<String> it = contentList.iterator();
            while (it.hasNext()) {
                String contentStr = it.next();
                if (contentStr != null && !contentStr.isEmpty()) {
                    result = OppoUsageService.nativeWriteFile(this.mFileWriteType, filePath, frozen ? this.mLocalKahaleesi.frozenIntoIce(contentStr) : contentStr, append);
                }
            }
            return result;
        }
    }

    private class SimCardDataRecorder extends UsageDataRecorder {
        private static final String LOGTAG = "SimCardDataRecorder";
        private static final String STR_USAGE_SIMCARD_HISTORY = "sdh.data";
        private static final String STR_USAGE_SIMCARD_ORIGINAL = "sdo.data";
        private File mSDHistoryFile;
        private String mSDHistoryFileName;
        private String mSDOrighinalFileName;
        private File mSDOriginalFile;
        private SimCardData mSimCardData;
        private boolean mUpdateOriginalSD;

        public SimCardDataRecorder(Context context, Kahaleesi localKahaleesi) {
            super(context, localKahaleesi);
            this.mSDOriginalFile = null;
            this.mSDHistoryFile = null;
            this.mSimCardData = null;
            this.mUpdateOriginalSD = false;
            this.mSDOriginalFile = new File(this.mUsagePersistFileDir, STR_USAGE_SIMCARD_ORIGINAL);
            this.mSDHistoryFile = new File(this.mUsagePersistFileDir, STR_USAGE_SIMCARD_HISTORY);
            this.mSDOrighinalFileName = this.mPersistFileDir + STR_USAGE_SIMCARD_ORIGINAL;
            this.mSDHistoryFileName = this.mPersistFileDir + STR_USAGE_SIMCARD_HISTORY;
        }

        public boolean saveSimCardInfo(SimCardData simcardData) {
            if (simcardData == null) {
                return false;
            }
            if (!simcardData.isValid()) {
                if (OppoUsageService.DEBUG_ALL) {
                    Slog.w(LOGTAG, "inValid sd data!");
                }
                return false;
            }
            ArrayList<String> recordList = new ArrayList<>();
            if (simcardData.mImeiStrSlot0 != null) {
                recordList.add("0|" + simcardData.mImeiStrSlot0 + OppoUsageService.CONNECTOR_FOR_PKGNAME_AND_TIME + simcardData.mMeidStrSlot0);
            }
            if (simcardData.mImeiStrSlot1 != null) {
                recordList.add("1|" + simcardData.mImeiStrSlot1 + OppoUsageService.CONNECTOR_FOR_PKGNAME_AND_TIME + simcardData.mMeidStrSlot1);
            }
            boolean saveOrigScInfoRes = saveOriginalSimCardInfo(recordList);
            if (OppoUsageService.DEBUG_ALL) {
                Slog.d(OppoUsageService.TAG, "saveOrigScInfoRes:" + saveOrigScInfoRes);
            }
            if (saveOrigScInfoRes) {
                boolean saveHistoryScInfoRes = saveSimCardInfoToHistory(recordList);
                if (OppoUsageService.DEBUG_ALL) {
                    Slog.d(OppoUsageService.TAG, "saveHistoryScInfoRes:" + saveHistoryScInfoRes);
                }
            }
            return saveOrigScInfoRes;
        }

        private boolean saveOriginalSimCardInfo(ArrayList<String> recordList) {
            boolean needSave = false;
            List<String> tmpStr = getOriginalSimCardData();
            if (tmpStr == null || tmpStr.size() <= 0) {
                needSave = true;
            } else {
                Iterator<String> it = recordList.iterator();
                while (it.hasNext()) {
                    if (!tmpStr.contains(it.next())) {
                        needSave = true;
                    }
                }
            }
            if (needSave) {
                boolean saveRes = doSaveData(recordList, this.mSDOrighinalFileName, true, true);
                if (OppoUsageService.DEBUG_ALL) {
                    Slog.d(LOGTAG, "save org sd data res:" + saveRes);
                }
                return saveRes;
            }
            Slog.d(OppoUsageService.TAG, "no need to save same data");
            return false;
        }

        private boolean saveSimCardInfoToHistory(ArrayList<String> recordList) {
            boolean saveRes = doSaveData(recordList, this.mSDHistoryFileName, true, true);
            if (OppoUsageService.DEBUG_ALL) {
                Slog.d(LOGTAG, "save history sd data res:" + saveRes);
            }
            return saveRes;
        }

        public ArrayList<String> getOriginalSimCardData() {
            return loadHistoryDataFromFile(this.mFileReadType, this.mSDOrighinalFileName);
        }

        public ArrayList<String> getHistorySimCardInfoList() {
            return loadHistoryDataFromFile(this.mFileReadType, this.mSDHistoryFileName);
        }
    }

    private class PcbaDataRecorder extends UsageDataRecorder {
        private static final String LOGTAG = "PcbaDataRecorder";
        private static final String STR_USAGE_PCBA_HISTORY = "pb.data";
        private String mFilePath;
        private File mHistoryFile;

        public PcbaDataRecorder(Context context, Kahaleesi localKahaleesi) {
            super(context, localKahaleesi);
            this.mFilePath = null;
            this.mHistoryFile = null;
            this.mHistoryFile = new File(this.mUsagePersistFileDir, STR_USAGE_PCBA_HISTORY);
            this.mFilePath = this.mPersistFileDir + STR_USAGE_PCBA_HISTORY;
        }

        public boolean savePcbaInfo(String numberStr) {
            if (numberStr == null || numberStr.isEmpty()) {
                return false;
            }
            List<String> tmpStr = getHistoryPcbaInfoList();
            if (tmpStr != null && tmpStr.size() > 0 && tmpStr.contains(numberStr)) {
                return false;
            }
            ArrayList<String> saveStrList = new ArrayList<>();
            saveStrList.add(numberStr);
            boolean saveRes = doSaveData(saveStrList, this.mFilePath, true, true);
            Slog.d(OppoUsageService.TAG, "savePcbaInfo:" + saveRes);
            return saveRes;
        }

        public ArrayList<String> getHistoryPcbaInfoList() {
            return loadHistoryDataFromFile(this.mFileReadType, this.mFilePath);
        }
    }

    private class McsDataRecorder extends UsageDataRecorder {
        private static final String LOGTAG = "McsDataRecorder";
        private static final String STR_USAGE_MCS = "mcs.data";
        private String mFileName;
        private File mHistoryFile;

        public McsDataRecorder(Context context, Kahaleesi localKahaleesi) {
            super(context, localKahaleesi);
            this.mFileName = null;
            this.mHistoryFile = null;
            this.mFileName = this.mPersistFileDir + STR_USAGE_MCS;
            this.mHistoryFile = new File(this.mUsagePersistFileDir, STR_USAGE_MCS);
        }

        public boolean saveMcsInfo(String numberStr) {
            if (numberStr == null || numberStr.isEmpty() || numberStr.equals(getMcsInfo())) {
                return false;
            }
            ArrayList<String> saveStrList = new ArrayList<>();
            saveStrList.add(numberStr);
            boolean saveRes = doSaveData(saveStrList, this.mFileName, false, true);
            if (OppoUsageService.DEBUG_ALL) {
                Slog.d(OppoUsageService.TAG, "saveMcsInfo:" + saveRes);
            }
            return saveRes;
        }

        public String getMcsInfo() {
            ArrayList<String> resStrList = loadHistoryDataFromFile(this.mFileReadType, this.mFileName);
            if (resStrList == null || resStrList.size() <= 0) {
                return null;
            }
            return resStrList.get(resStrList.size() - 1);
        }
    }

    private class CacheRecordsRecorder extends UsageDataRecorder {
        private static final String LOGTAG = "CacheRecordsRecorder";
        protected String mCurFileName = null;
        private File mCurHistoryFile = null;
        protected String mPreFileName = null;
        private File mPreHistoryFile = null;

        public CacheRecordsRecorder(Context context, Kahaleesi localKahaleesi, String fileName) {
            super(context, localKahaleesi);
            String str;
            String str2 = BatteryService.HealthServiceWrapper.INSTANCE_VENDOR;
            if (fileName == null || fileName.isEmpty()) {
                str = str2;
            } else {
                str = this.mCacheFileDir + fileName + ".dat";
            }
            this.mCurFileName = str;
            if (fileName != null && !fileName.isEmpty()) {
                str2 = this.mCacheFileDir + "pre_" + fileName + ".dat";
            }
            this.mPreFileName = str2;
            this.mCurHistoryFile = new File(this.mUsageCacheFileDir, fileName + ".dat");
            this.mPreHistoryFile = new File(this.mUsageCacheFileDir, "pre_" + fileName + ".dat");
        }

        public boolean saveContentList(ArrayList<String> contentList) {
            if (contentList == null || contentList.size() <= 0) {
                return false;
            }
            boolean saveRes = doSaveData(contentList, this.mCurFileName, true, true);
            if (OppoUsageService.DEBUG_ALL) {
                Slog.d(LOGTAG, "saveContentList:" + saveRes);
            }
            return saveRes;
        }

        public List<String> getCurHistoryFileInfoList() {
            return loadHistoryDataFromFile(this.mFileReadType, this.mCurFileName);
        }

        public List<String> getPreHistoryFileInfoList() {
            return loadHistoryDataFromFile(this.mFileReadType, this.mPreFileName);
        }
    }

    private class StatsticDataRecorder extends UsageDataRecorder {
        private static final String LOGTAG = "StatsticDataRecorder";
        private static final String STR_USAGE_DAIL_IN_DURATION = "did.data";
        private static final String STR_USAGE_DAIL_OUT_DURATION = "dod.data";
        private static final String STR_USAGE_SMS_RECEIVE_COUNT = "smsr.data";
        private static final String STR_USAGE_SMS_SEND_COUNT = "smss.data";
        private int mDailInDuration = 0;
        private String mDailInDurationHistoryName = (this.mPersistFileDir + STR_USAGE_DAIL_IN_DURATION);
        private int mDailOutDuration = 0;
        private String mDailOutDurationHistoryName = (this.mPersistFileDir + STR_USAGE_DAIL_OUT_DURATION);
        private int mSmsRecCount = 0;
        private String mSmsRecCountHistoryName = (this.mPersistFileDir + STR_USAGE_SMS_RECEIVE_COUNT);
        private int mSmsSendCount = 0;
        private String mSmsSendCountHistoryName = (this.mPersistFileDir + STR_USAGE_SMS_SEND_COUNT);

        public StatsticDataRecorder(Context context, Kahaleesi localKahaleesi) {
            super(context, localKahaleesi);
            updateCurDataFromFiles();
        }

        private void updateCurDataFromFiles() {
            int readRes = readSignalCountFromFile(this.mSmsRecCountHistoryName);
            int i = 0;
            this.mSmsRecCount = readRes < 0 ? 0 : readRes;
            int readRes2 = readSignalCountFromFile(this.mSmsSendCountHistoryName);
            this.mSmsSendCount = readRes2 < 0 ? 0 : readRes2;
            int readRes3 = readSignalCountFromFile(this.mDailOutDurationHistoryName);
            this.mDailOutDuration = readRes3 < 0 ? 0 : readRes3;
            int readRes4 = readSignalCountFromFile(this.mDailInDurationHistoryName);
            if (readRes4 >= 0) {
                i = readRes4;
            }
            this.mDailInDuration = i;
        }

        private int readSignalCountFromFile(String fileName) {
            String countStr;
            ArrayList<String> resStrList = loadHistoryDataFromFile(this.mFileReadType, fileName);
            if (resStrList == null || resStrList.size() <= 0 || (countStr = resStrList.get(resStrList.size() - 1)) == null || countStr.isEmpty()) {
                return 0;
            }
            try {
                return Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                Slog.e(OppoUsageService.TAG, "parser failed num.");
                return 0;
            } catch (Exception e2) {
                Slog.e(OppoUsageService.TAG, "parser failed.");
                return 0;
            }
        }

        private boolean saveCountInfoToFile(String fileName, int count, boolean append) {
            if (count < 0 || fileName == null || fileName.isEmpty()) {
                return false;
            }
            String saveStr = Integer.toString(count);
            ArrayList<String> saveStrList = new ArrayList<>();
            saveStrList.add(saveStr);
            boolean saveRes = doSaveData(saveStrList, fileName, append, true);
            if (OppoUsageService.DEBUG_ALL) {
                Slog.d(OppoUsageService.TAG, "saveCountInfoToFile:" + saveRes);
            }
            return saveRes;
        }

        public boolean accumulateSmsRecCount(int delataRecCount) {
            if (delataRecCount <= 0) {
                return false;
            }
            this.mSmsRecCount += delataRecCount;
            return saveCountInfoToFile(this.mSmsRecCountHistoryName, this.mSmsRecCount, false);
        }

        public int getHistorySmsRecCount() {
            return readSignalCountFromFile(this.mSmsRecCountHistoryName);
        }

        public boolean accumulateSmsSendCount(int delataSendCount) {
            if (delataSendCount <= 0) {
                return false;
            }
            this.mSmsSendCount += delataSendCount;
            return saveCountInfoToFile(this.mSmsSendCountHistoryName, this.mSmsSendCount, false);
        }

        public int getHistorySmsSendCount() {
            return readSignalCountFromFile(this.mSmsSendCountHistoryName);
        }

        public boolean accumulateDailOutDuration(int delataDailOutDuration) {
            if (delataDailOutDuration <= 0) {
                return false;
            }
            this.mDailOutDuration += delataDailOutDuration;
            return saveCountInfoToFile(this.mDailOutDurationHistoryName, this.mDailOutDuration, false);
        }

        public int getHistoryDailOutDuration() {
            return readSignalCountFromFile(this.mDailOutDurationHistoryName);
        }

        public boolean accumulateDailInDuration(int delataDailInDuration) {
            if (delataDailInDuration <= 0) {
                return false;
            }
            this.mDailInDuration += delataDailInDuration;
            return saveCountInfoToFile(this.mDailInDurationHistoryName, this.mDailInDuration, false);
        }

        public int getHistoryDailInDuration() {
            return readSignalCountFromFile(this.mDailInDurationHistoryName);
        }
    }

    /* access modifiers changed from: private */
    public class EmmcUsageCollector {
        private static final String BLOCK_SDA = "/dev/block/sda";
        private static final String EMMC_INFO_FILE_SUFFIX = ".txt";
        private static final String PATH_EMMC_INFO_FILE = "/data/system/dropbox/emmcInfo-";
        private static final int READ_COUNT = 256;
        private static final int READ_OFFSET_INTERGRATE = 4300800;
        private static final int READ_OFFSET_UNINTERGRATE = 15729664;
        private static final boolean SAVE_AS_FILE = false;
        private boolean BOARD_PLATFORM_IS_MTK = SystemProperties.get("ro.board.platform", "oppo").toLowerCase().startsWith("mt");
        private String mCurrentEmmcInfo = null;
        boolean mDebugEmmcInfo = false;
        private boolean mIsUserDebugVersion = false;
        private Context mLocalContext = null;

        public EmmcUsageCollector(Context context) {
            this.mLocalContext = context;
            this.mIsUserDebugVersion = "userdebug".equals(SystemProperties.get("ro.build.type"));
        }

        public boolean readEmmcInfoLable(boolean ignoreVersion) {
            if (ignoreVersion || !this.mIsUserDebugVersion) {
                return getEmmcInfoLableImplIntergrate();
            }
            return false;
        }

        private boolean getEmmcInfoLableImplIntergrate() {
            return encodeAndSend(OppoEngineerManager.getEmmcHealthInfo());
        }

        private boolean encodeAndSend(byte[] emmcInfoByteArray) {
            if (emmcInfoByteArray == null || emmcInfoByteArray.length <= 0) {
                Slog.e(OppoUsageService.TAG, "readEmmcInfoLable failed, res empty.");
                return false;
            }
            int maxLen = 256;
            if (emmcInfoByteArray.length < 256) {
                maxLen = emmcInfoByteArray.length;
            }
            if (this.mDebugEmmcInfo) {
                Slog.d(OppoUsageService.TAG, "readEmmcInfoLable, beging decode");
            }
            String resInBase64 = new String(Base64.encode(emmcInfoByteArray, 0, maxLen, 2));
            if (this.mDebugEmmcInfo) {
                Slog.d(OppoUsageService.TAG, "readEmmcInfoLable, after resInBase64:" + resInBase64);
            }
            sendDcsMsg(resInBase64);
            if (this.mDebugEmmcInfo) {
                byte[] decodeByteArray = Base64.decode(resInBase64, 2);
                StringBuilder sb = new StringBuilder();
                int sbCount = 0;
                for (byte b : decodeByteArray) {
                    sb.append(Integer.toHexString(b & 255));
                    sbCount++;
                    if (sbCount == 10) {
                        Slog.d(OppoUsageService.TAG, "readEmmcInfoLable, decode:" + sb.toString());
                        sb = new StringBuilder();
                        sbCount = 0;
                    }
                }
                Slog.d(OppoUsageService.TAG, "readEmmcInfoLable, decode at last:" + sb.toString());
            }
            if (!this.mDebugEmmcInfo) {
                return true;
            }
            Slog.d(OppoUsageService.TAG, "readEmmcInfoLable:01");
            return true;
        }

        private String getCurrentDateStrForEmmcFile() {
            OppoUsageService.this.mTimeObj.setToNow();
            return OppoUsageService.this.mTimeObj.format("%Y-%m-%d-%H-%M-%S");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0029, code lost:
            return;
         */
        private synchronized void sendDcsMsg(String strMsg) {
            if (strMsg != null) {
                if (!strMsg.isEmpty()) {
                    try {
                        Map<String, String> logMap = new HashMap<>();
                        logMap.put("EmmcInfo", strMsg);
                        OppoStatistics.onCommon(this.mLocalContext, "EmmcInfo", "EmmcInfoID", logMap, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public boolean loadStorageDeviceInfo() {
            try {
                Map<String, String> logMap = new HashMap<>();
                String emmcHealthInfo = getEmmcHealthInfo();
                if (emmcHealthInfo == null || emmcHealthInfo.isEmpty()) {
                    logMap.put("EmmcInfo", "null");
                } else {
                    logMap.put("EmmcInfo", emmcHealthInfo);
                }
                String strStorageSize = getStorageSize();
                if (strStorageSize == null || strStorageSize.isEmpty()) {
                    logMap.put("StorageSize", "null");
                } else {
                    logMap.put("StorageSize", strStorageSize);
                }
                String strStorageDev = getStorageDevInfo();
                if (strStorageDev == null || strStorageDev.isEmpty()) {
                    logMap.put("StorageDev", "null");
                } else {
                    logMap.put("StorageDev", strStorageDev);
                }
                String strMemoryDev = getMemoryDeviceVersion();
                if (strMemoryDev == null || strMemoryDev.isEmpty()) {
                    logMap.put("MemoryDev", "null");
                } else {
                    logMap.put("MemoryDev", strMemoryDev);
                }
                String strLifeTime = getLifeTimeContent();
                if (strLifeTime == null || strLifeTime.isEmpty()) {
                    logMap.put("LifeTime", "null");
                } else {
                    logMap.put("LifeTime", strLifeTime);
                }
                String strErrorState = getErrorState();
                if (strErrorState == null || strErrorState.isEmpty()) {
                    logMap.put("ErrorState", "null");
                } else {
                    logMap.put("ErrorState", strErrorState);
                }
                logMap.put("RecordTime", OppoUsageService.this.getCurrentDateStr());
                OppoStatistics.onCommon(this.mLocalContext, "EmmcInfo", "EmmcInfoID", logMap, false);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private String getEmmcHealthInfo() {
            byte[] resData = OppoEngineerManager.getEmmcHealthInfo();
            if (resData == null || resData.length <= 0) {
                Slog.e(OppoUsageService.TAG, "getEmmcHealthInfo failed, res empty.");
                return null;
            }
            int maxLen = 256;
            if (resData.length < 256) {
                maxLen = resData.length;
            }
            return new String(Base64.encode(resData, 0, maxLen, 2));
        }

        private boolean isUfsStorage() {
            return new File(BLOCK_SDA).exists();
        }

        private String getDevInfoContent(String tag, String filePath) {
            StringBuilder sb;
            if (filePath == null || filePath.isEmpty()) {
                Slog.w(OppoUsageService.TAG, "getDevInfoContent-" + tag + ", filePath empty");
                return null;
            }
            File devFile = new File(filePath);
            if (!devFile.exists()) {
                Slog.w(OppoUsageService.TAG, "getDevInfoContent-" + tag + ", file not exists:" + filePath);
                return null;
            }
            BufferedReader reader = null;
            StringBuilder strBuilder = new StringBuilder();
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(devFile));
                while (true) {
                    String tempString = reader2.readLine();
                    if (tempString != null) {
                        Slog.i(OppoUsageService.TAG, " getDevInfoContent tempString : " + tempString);
                        strBuilder.append(tempString);
                        strBuilder.append(OppoUsageService.CONNECTOR_FOR_PKGNAME_AND_TIME);
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                            e1 = e;
                            sb = new StringBuilder();
                        }
                    }
                }
                reader2.close();
            } catch (Exception e2) {
                Slog.e(OppoUsageService.TAG, "getDevInfoContent-" + tag + "io exception:" + e2.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                        e1 = e3;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        Slog.e(OppoUsageService.TAG, "getDevInfoContent-" + tag + " io close exception :" + e1.getMessage());
                    }
                }
                throw th;
            }
            return strBuilder.toString();
            sb.append("getDevInfoContent-");
            sb.append(tag);
            sb.append(" io close exception :");
            sb.append(e1.getMessage());
            Slog.e(OppoUsageService.TAG, sb.toString());
            return strBuilder.toString();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0084, code lost:
            if (r0.length <= 2) goto L_0x00a6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0086, code lost:
            r10 = r0[2];
            android.util.Slog.i(com.android.server.oppo.OppoUsageService.TAG, "emmcSize = " + r10);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x00a4, code lost:
            r6 = analyseTotalStorage(java.lang.Long.parseLong(r10));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
            r10.close();
         */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x004c A[Catch:{ Exception -> 0x00c4, all -> 0x00c2 }] */
        private String getStorageSize() {
            String tempString;
            StringBuilder sb;
            File file = new File("/proc/partitions");
            if (!file.exists()) {
                Slog.i(OppoUsageService.TAG, "getStorageSize file not exists : " + "/proc/partitions");
                return null;
            }
            BufferedReader reader = null;
            String ufsPartitionName = this.BOARD_PLATFORM_IS_MTK ? "sdc" : "sda";
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    String readLine = reader2.readLine();
                    tempString = readLine;
                    if (readLine != null) {
                        Slog.i(OppoUsageService.TAG, "tempString : " + tempString);
                        if (tempString.trim().endsWith("mmcblk0") || tempString.trim().endsWith(ufsPartitionName)) {
                            String[] para = tempString.trim().replaceAll("\\s+", StringUtils.SPACE).split(StringUtils.SPACE);
                        }
                        String readLine2 = reader2.readLine();
                        tempString = readLine2;
                        if (readLine2 != null) {
                        }
                    }
                    try {
                        break;
                    } catch (IOException e) {
                        e1 = e;
                        sb = new StringBuilder();
                    }
                }
            } catch (Exception e2) {
                tempString = null;
                Slog.e(OppoUsageService.TAG, "getStorageSize io exception:" + e2.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                        e1 = e3;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        Slog.e(OppoUsageService.TAG, "getStorageSize io close exception :" + e1.getMessage());
                    }
                }
                throw th;
            }
            return tempString;
            sb.append("getStorageSize io close exception :");
            sb.append(e1.getMessage());
            Slog.e(OppoUsageService.TAG, sb.toString());
            return tempString;
        }

        private String analyseTotalStorage(long size) {
            double total = ((double) size) / 1048576.0d;
            Slog.d(OppoUsageService.TAG, "analyseTotalStorage(), total: " + total);
            int storageSize = 1;
            while (total > ((double) storageSize)) {
                storageSize <<= 1;
            }
            return String.format(Locale.US, "%dG", Integer.valueOf(storageSize));
        }

        private String getStorageDevInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("/proc/devinfo/");
            sb.append(isUfsStorage() ? "ufs_version" : "emmc_version");
            return getDevInfoContent("storageDev", sb.toString());
        }

        private String getLifeTimeContent() {
            String pathLifeTime;
            if (isUfsStorage()) {
                pathLifeTime = "/dev/block/bootdevice/health_descriptor/eol_info";
            } else {
                pathLifeTime = "/sys/kernel/debug/mmc0/mmc0:0001/life_time";
            }
            return getDevInfoContent("lifeTime", pathLifeTime);
        }

        private String getMemoryDeviceVersion() {
            StringBuilder sb = new StringBuilder();
            sb.append("/proc/devinfo/");
            sb.append(isUfsStorage() ? "ufs" : "emmc");
            return getDevInfoContent("memDevVer", sb.toString());
        }

        private String getErrorState() {
            String pathErrorState;
            String propBootDevice = SystemProperties.get("ro.boot.bootdevice", "null");
            if (isUfsStorage()) {
                pathErrorState = "/sys/kernel/debug/" + propBootDevice + "err_state";
            } else {
                pathErrorState = "/sys/kernel/debug/mmc0/err_state";
            }
            return getDevInfoContent("errorState", pathErrorState);
        }
    }
}

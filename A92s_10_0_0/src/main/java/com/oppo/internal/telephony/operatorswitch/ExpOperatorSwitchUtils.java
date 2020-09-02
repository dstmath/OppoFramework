package com.oppo.internal.telephony.operatorswitch;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.uicc.IccRecords;
import com.oppo.internal.telephony.utils.OppoEngineerManager;

public class ExpOperatorSwitchUtils {
    private static final boolean DBG = OemConstant.SWITCH_LOG;
    private static final int EVENT_NOTIFY_OPERATOR_CHANGED_DELAY_TIME = 2000;
    private static final int EVENT_NOTIFY_OPERATOR_CHANGED_WAIT = 100;
    private static final int MAX_LENGTH = 8;
    private static final long OPERATOR_SWITCH_OFFSET = 0;
    private static final int RW_RETRY_TIMES = 3;
    private static final String SIM_FIRST_INSERT_FLAG = "sim_first_insert_flag";
    private static final long SIM_SWITCH_OPERATOR_OFFSET = 1280;
    private static final String SIM_SWITCH_OPERATOR_ON = "1";
    private static int SLOT0 = 0;
    private static int SLOT1 = 1;
    private static final String TAG = "ExpOperatorSwitchUtils";
    static boolean hasOperatorNormalize = false;
    static boolean isOperatorNormalize = false;
    private static final String operatorNV = "00000000";
    private static Context sContext;
    private static ContentObserver sInWelcomePage = new ContentObserver(new Handler()) {
        /* class com.oppo.internal.telephony.operatorswitch.ExpOperatorSwitchUtils.AnonymousClass1 */

        public void onChange(boolean selfChange) {
            if (ExpOperatorSwitchUtils.sOperatorHandler.hasMessages(ExpOperatorSwitchUtils.EVENT_NOTIFY_OPERATOR_CHANGED_WAIT)) {
                ExpOperatorSwitchUtils.sOperatorHandler.removeMessages(ExpOperatorSwitchUtils.EVENT_NOTIFY_OPERATOR_CHANGED_WAIT);
                Rlog.d(ExpOperatorSwitchUtils.TAG, "removeMessages EVENT_NOTIFY_OPERATOR_CHANGED_WAIT");
            }
            ExpOperatorSwitchUtils.sOperatorHandler.sendMessageDelayed(ExpOperatorSwitchUtils.sOperatorHandler.obtainMessage(ExpOperatorSwitchUtils.EVENT_NOTIFY_OPERATOR_CHANGED_WAIT), 2000);
        }
    };
    private static int sIndex = -1;
    private static int sNameIndex = 2;
    private static int sNvIndex = 3;
    private static int sOpIndex = 0;
    private static String sOpVersion = SystemProperties.get("ro.oppo.operator", "OPPO");
    /* access modifiers changed from: private */
    public static Handler sOperatorHandler = new Handler() {
        /* class com.oppo.internal.telephony.operatorswitch.ExpOperatorSwitchUtils.AnonymousClass2 */

        public void handleMessage(Message msg) {
            if (msg.what != ExpOperatorSwitchUtils.EVENT_NOTIFY_OPERATOR_CHANGED_WAIT) {
                Rlog.d(ExpOperatorSwitchUtils.TAG, "sOperatorHandler do not handlemessage");
            } else {
                ExpOperatorSwitchUtils.oppoHandlerChangeOperator(ExpOperatorSwitchUtils.getOperatorIndexValue());
            }
        }
    };
    private static boolean sOperatorSwitchComplete = false;
    private static String[][] sOperatorSwitchInfo = {new String[]{"SGOP", "52501", "Singtel", "00100100", "SINGTEL"}, new String[]{"SGOP", "52505", "StarHub", "00100101", "STARHUB"}, new String[]{"SGOP", "52505", "MyRepublic", "01101111", "MyRepublic"}, new String[]{"SGOP", "52503", "M1", "00100110", "M1"}, new String[]{"SGOP", "52510", "TPG", "01101110", "TPG"}, new String[]{"NZOP", "53024", "2degrees", "00101001", "2DEGREES"}, new String[]{"NZOP", "53024", "Warehouse", "01111000", "WAREHOUSE"}, new String[]{"NZOP", "53005", "Spark NZ", "00101010", "SPARK"}, new String[]{"NZOP", "53005", "Skinny", "01111001", "SKINNY"}, new String[]{"TWOP", "46601", "FET", "00000101", "FET"}, new String[]{"TWOP", "46602", "FET", "00000101", "FET"}, new String[]{"TWOP", "46605", "APT", "00111101", "APT"}, new String[]{"TWOP", "46697", "TWM", "00000110", "TWM"}, new String[]{"MYOP", "50219", "Celcom", "01000000", "CELCOM"}, new String[]{"MYOP", "50212", "Maxis", "01000001", "MAXIS"}, new String[]{"MYOP", "50216", "Digi", "01000010", "DIGI"}, new String[]{"VODAFONE_EUEX", "20404", "Vodafone", "01010010", "NL"}, new String[]{"VODAFONE_EUEX", "22210", "Vodafone", "01010000", "IT"}, new String[]{"ORANGE", "20800", "Orange", "01001010", "FR"}, new String[]{"ORANGE", "20801", "Orange", "01001010", "FR"}, new String[]{"ORANGE", "20802", "Orange", "01001010", "FR"}, new String[]{"ORANGE", "26003", "Orange", "01011000", "PL"}};
    private static boolean sOppoMtkPlatform = false;
    private static boolean sOppoQcomPlatform = false;
    public static String sRegionMark = SystemProperties.get("ro.oppo.regionmark", "OPPO");
    private static int sSimCodeIndex = 1;
    private static String[] sSupportOperator = {"SGOP", "NZOP", "TWOP", "MYOP", "VODAFONE_EUEX", "ORANGE"};
    private static int sVersionIndex = 4;

    public static void init(Context context) {
        sContext = context;
        Context context2 = sContext;
        if (context2 != null) {
            sOppoMtkPlatform = isBasedOnMtk(context2);
            sOppoQcomPlatform = isBasedOnQcom(sContext);
        }
        Rlog.d(TAG, "ExpOperatorSwitchUtils init");
    }

    public static boolean isSupportOperatorSwitch() {
        if (isOperatorNormalize()) {
            return true;
        }
        return false;
    }

    public static String getSupportOperatorSwitch() {
        return sOpVersion;
    }

    static boolean isOperatorNormalize() {
        if (hasOperatorNormalize) {
            return isOperatorNormalize;
        }
        isOperatorNormalize = SystemProperties.get("ro.oppo.operator_normalize", "false").equalsIgnoreCase("true");
        log("isOperatorNormalize = " + isOperatorNormalize);
        hasOperatorNormalize = true;
        return isOperatorNormalize;
    }

    private static String getOperatorVersion() {
        String operatorPartition = OppoEngineerManager.getCarrierVersion();
        String operatorNVRAM = "";
        if (sOppoMtkPlatform) {
            byte[] operatorBytes = readOperatorSwitchNVRAM();
            if (operatorBytes != null) {
                operatorNVRAM = new String(operatorBytes);
            }
            if (operatorPartition != null && operatorPartition.length() > 8) {
                operatorPartition = operatorPartition.substring(0, 8);
            }
            if (operatorNVRAM.length() > 8) {
                operatorNVRAM = operatorNVRAM.substring(0, 8);
            }
        } else if (sOppoQcomPlatform) {
            operatorNVRAM = readOperatorSwitchQCNV();
        }
        logd("getOperatoVersion,operatorPartition==" + operatorPartition + ",operatorNVRAM==" + operatorNVRAM);
        return operatorNVRAM;
    }

    public static boolean setOperatorVersion(String opVersion) {
        boolean result3;
        boolean result1 = OppoEngineerManager.setCarrierVersion(opVersion);
        boolean result2 = OppoEngineerManager.setSimOperatorSwitch("1");
        if (sOppoMtkPlatform) {
            result3 = writeOperatorSwitchNVRAM(opVersion);
        } else {
            result3 = writeOperatorSwitchQCNV(opVersion);
        }
        log("setOperatorVersion,result1==" + result1 + ",result2==" + result2 + ",result3==" + result3);
        if (!result1 || !result2 || !result3) {
            return false;
        }
        return true;
    }

    public static boolean isBasedOnMtk(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
    }

    public static boolean isBasedOnQcom(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.qualcomm");
    }

    private static byte[] readOperatorSwitchNVRAM() {
        return OppoEngineerManager.getCarrierVersionFromNvram();
    }

    private static boolean writeOperatorSwitchNVRAM(String operator) {
        logd("writeOperatorSwitchNVRAM,operator==" + operator);
        if (operator == null) {
            log("value null setOperatorVersion fail");
            return false;
        }
        byte[] buff = new byte[9];
        char[] temp = operator.toCharArray();
        for (int i = 0; i < operator.length(); i++) {
            buff[i] = (byte) temp[i];
            logd("byte = " + ((int) buff[i]));
        }
        buff[operator.length()] = 0;
        log("length = " + operator.length());
        return OppoEngineerManager.saveCarrierVersionToNvram(buff);
    }

    private static String readOperatorSwitchQCNV() {
        logd("readOperatorSwitchQCNV,operatorNVRAM==" + ((String) null));
        return null;
    }

    private static boolean writeOperatorSwitchQCNV(String value) {
        return false;
    }

    public static boolean isFirstInsertSim() {
        if (Settings.Global.getInt(sContext.getContentResolver(), SIM_FIRST_INSERT_FLAG, 0) != 0) {
            return false;
        }
        return true;
    }

    public static void setFirstInsertSimFlag(int flag) {
        Settings.Global.putInt(sContext.getContentResolver(), SIM_FIRST_INSERT_FLAG, flag);
    }

    public static boolean oppoIsSpecOperator(String simCode, int slot, String spn, boolean checkGid1OrSpn, IccRecords iccRecords) {
        boolean isNeedChange = false;
        int index = -1;
        int i = 0;
        while (true) {
            if (i >= sOperatorSwitchInfo.length) {
                break;
            } else if (!getSupportOperatorSwitch().equals(sOperatorSwitchInfo[i][sOpIndex]) || !simCode.equals(sOperatorSwitchInfo[i][sSimCodeIndex])) {
                i++;
            } else {
                if (isSgVirtualOperator(simCode, iccRecords)) {
                    index = i + 1;
                } else if (isNZVirtualOperator(simCode, iccRecords)) {
                    index = i + 1;
                } else {
                    index = i;
                }
                isNeedChange = true;
            }
        }
        log("oppoIsSpecOperator,isNeedChange==" + isNeedChange + ",index==" + index + "length=" + sOperatorSwitchInfo.length);
        if (isNeedChange && index != -1) {
            String[][] strArr = sOperatorSwitchInfo;
            if (index < strArr.length) {
                if (setOperatorVersion(strArr[index][sNvIndex])) {
                    sOperatorSwitchComplete = true;
                } else {
                    sOperatorSwitchComplete = false;
                }
                logd("oppoIsSpecOperator,sOperatorSwitchComplete =" + sOperatorSwitchComplete);
                if (sOperatorSwitchComplete && sOperatorSwitchInfo[index][sNvIndex].equals(getOperatorVersion())) {
                    setOperatorIndexValue(index);
                    notifyOpVersionChange(index, slot);
                    log("oppoIsSpecOperator, set slot=" + slot + "NVRAM and Partition to modem complete!!!");
                    return true;
                }
            }
        }
        return false;
    }

    private static void oppoBroadCastChangeOperator(Context context, int index) {
        if (index < 0 || index >= sOperatorSwitchInfo.length) {
            logd("oppoBroadCastChangeOperator,index = " + index);
        } else {
            log("oppoBroadCastChangeOperator");
            SystemProperties.set("gsm.sim.switch.operator", "true");
            Intent intent = new Intent("oppo.intent.action.CHANGE_OPERATOR");
            intent.putExtra("operatorname", sOperatorSwitchInfo[index][sNameIndex]);
            intent.putExtra("operatorcode", sOperatorSwitchInfo[index][sSimCodeIndex]);
            intent.putExtra("switchreboot", "true");
            sContext.sendBroadcastAsUser(intent, UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
        }
        resetOperatorIndexValue();
    }

    public static void oppoBroadCastDelayHotswap() {
        if (!sContext.getPackageManager().hasSystemFeature("oppo.commcenter.reboot.dialog")) {
            logd("oppoBroadCastDelayHotswap,needn't show reboot dialog");
            return;
        }
        log("oppoBroadCastDelayHotswap");
        Intent intent = new Intent("oppo.intent.action.HOT_SWAP");
        intent.putExtra("operatorname", "null");
        intent.putExtra("operatorcode", "null");
        intent.putExtra("switchreboot", "false");
        sContext.sendBroadcastAsUser(intent, UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    private static void notifyOpVersionChange(int index, int slot) {
        oppoHandlerChangeOperator(index);
    }

    /* access modifiers changed from: private */
    public static void oppoHandlerChangeOperator(int index) {
        if (isBootWizardCompleted()) {
            oppoBroadCastChangeOperator(sContext, index);
        } else {
            oppoRegisterBootWizard(index);
        }
    }

    private static void oppoRegisterBootWizard(int index) {
        logd("bootreg welcomepage showing, no need show sim changed dialog");
        sContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), true, sInWelcomePage);
    }

    private static boolean isBootWizardCompleted() {
        boolean isBootWizardCompleted = false;
        if (Settings.Global.getInt(sContext.getContentResolver(), "device_provisioned", 0) == 1) {
            isBootWizardCompleted = true;
        }
        logd("isBootWizardCompleted:" + isBootWizardCompleted);
        return isBootWizardCompleted;
    }

    private static void setOperatorIndexValue(int index) {
        sIndex = index;
    }

    /* access modifiers changed from: private */
    public static int getOperatorIndexValue() {
        return sIndex;
    }

    private static void resetOperatorIndexValue() {
        sIndex = -1;
    }

    public static boolean isSgVirtualOperator(String simCode, IccRecords iccRecords) {
        if (!"52505".equals(simCode) || iccRecords == null) {
            return false;
        }
        String imsi = iccRecords.getIMSI();
        if (TextUtils.isEmpty(imsi) || !imsi.startsWith("52505292")) {
            return false;
        }
        return true;
    }

    public static boolean isNZVirtualOperator(String simCode, IccRecords iccRecords) {
        if ("53024".equals(simCode)) {
            if (iccRecords == null) {
                return false;
            }
            String imsi = iccRecords.getIMSI();
            if (TextUtils.isEmpty(imsi) || !imsi.startsWith("5302410")) {
                return false;
            }
            return true;
        } else if (!"53005".equals(simCode) || iccRecords == null) {
            return false;
        } else {
            String imsi2 = iccRecords.getIMSI();
            if (TextUtils.isEmpty(imsi2) || !imsi2.startsWith("53005204")) {
                return false;
            }
            return true;
        }
    }

    public static void log(String string) {
        Rlog.d(TAG, string);
    }

    public static void logd(String string) {
        if (DBG) {
            log(string);
        }
    }
}

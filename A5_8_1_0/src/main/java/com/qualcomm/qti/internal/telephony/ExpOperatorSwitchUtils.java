package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.Rlog;
import android.util.Log;
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qti.internal.telephony.primarycard.SubsidyLockSettingsObserver;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ExpOperatorSwitchUtils {
    private static final int EVENT_NOTIFY_OPERATOR_CHANGED_DELAY_TIME = 2000;
    private static final int EVENT_NOTIFY_OPERATOR_CHANGED_WAIT = 100;
    private static final String EXP_OPERATOR_SWITCH_PATH = "/opporeserve/radio/exp_operator_switch.config";
    private static final int MAX_LENGTH = 8;
    private static final String MTK_COMMONSOFT_FLAG_BACKUP_PATH = "/data/nvram/APCFG/APRDEB/CARRIER_VER";
    private static final String MTK_PARTITION_PATH_OPERATOR_SWITCH = "/dev/block/platform/mtk-msdc.0/11230000.msdc0/by-name/reserve_exp1";
    private static final long OPERATOR_SWITCH_OFFSET = 0;
    private static final String OPPO_PARTITION = "ro.sys.reserve.integrate";
    private static final String QCOM_PARTITION_PATH_OPERATOR_SWITCH = "/dev/block/bootdevice/by-name/reserve_exp1";
    private static final int RW_RETRY_TIMES = 3;
    private static final String SIM_FIRST_INSERT_FLAG = "sim_first_insert_flag";
    private static final int SIM_SWITCH_WRITE_SLEEP_TIMES = 100;
    private static int SLOT0 = 0;
    private static int SLOT1 = 1;
    private static final String TAG = "ExpOperatorSwitchUtils";
    private static final String operatorNV = "00000000";
    private static Context sContext;
    private static ContentObserver sInWelcomePage = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Rlog.d(ExpOperatorSwitchUtils.TAG, "onChange :" + ExpOperatorSwitchUtils.isBootWizardCompleted());
            if (ExpOperatorSwitchUtils.sOperatorHandler.hasMessages(100)) {
                ExpOperatorSwitchUtils.sOperatorHandler.removeMessages(100);
                Rlog.d(ExpOperatorSwitchUtils.TAG, "removeMessages EVENT_NOTIFY_OPERATOR_CHANGED_WAIT");
            }
            Rlog.d(ExpOperatorSwitchUtils.TAG, "onChange time1 : " + SystemClock.elapsedRealtime());
            ExpOperatorSwitchUtils.sOperatorHandler.sendMessageDelayed(ExpOperatorSwitchUtils.sOperatorHandler.obtainMessage(100), 2000);
        }
    };
    private static int sIndex = -1;
    private static int sNameIndex = 2;
    private static int sNvIndex = 3;
    private static int sOpIndex = 0;
    private static Handler sOperatorHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SubsidyLockSettingsObserver.PERMANENTLY_UNLOCKED /*100*/:
                    Rlog.d(ExpOperatorSwitchUtils.TAG, "handleMessage time2 : " + SystemClock.elapsedRealtime());
                    ExpOperatorSwitchUtils.oppoHandlerChangeOperator(ExpOperatorSwitchUtils.getOperatorIndexValue());
                    return;
                default:
                    Rlog.d(ExpOperatorSwitchUtils.TAG, "sOperatorHandler do not handlemessage");
                    return;
            }
        }
    };
    private static boolean sOperatorSwitchComplete = false;
    private static String[][] sOperatorSwitchInfo;
    private static boolean sOppoMtkPlatform = false;
    private static boolean sOppoPartition = false;
    private static boolean sOppoQcomPlatform = false;
    private static QcNvItems sQcNvItems = null;
    private static int sSimCodeIndex = 1;
    private static String[] sSupportOperator = new String[]{"SGOP", "NZOP", "TWOP", "MYOP"};
    private static int sVersionIndex = 4;

    static {
        r0 = new String[12][];
        r0[0] = new String[]{"SGOP", "52501", "Singtel", "00100100", "SINGTEL"};
        r0[1] = new String[]{"SGOP", "52505", "StarHub", "00100101", "STARHUB"};
        r0[2] = new String[]{"SGOP", "52503", "M1", "00100110", "M1"};
        r0[3] = new String[]{"NZOP", "53024", "2degrees", "00101001", "2DEGREES"};
        r0[4] = new String[]{"NZOP", "53005", "Spark", "00101010", "SPARK"};
        r0[5] = new String[]{"TWOP", "46601", "FET", "00000101", "FET"};
        r0[6] = new String[]{"TWOP", "46602", "FET", "00000101", "FET"};
        r0[7] = new String[]{"TWOP", "46605", "APT", "00111101", "APT"};
        r0[8] = new String[]{"TWOP", "46697", "TWM", "00000110", "TWM"};
        r0[9] = new String[]{"MYOP", "50219", "Celcom", "01000000", "CELCOM"};
        r0[10] = new String[]{"MYOP", "50212", "Maxis", "01000001", "MAXIS"};
        r0[11] = new String[]{"MYOP", "50216", "Digi", "01000010", "DIGI"};
        sOperatorSwitchInfo = r0;
    }

    public static void init(Context context) {
        sContext = context;
        if (sContext != null) {
            sOppoMtkPlatform = isBasedOnMtk(sContext);
            sOppoQcomPlatform = isBasedOnQcom(sContext);
            sQcNvItems = new QcNvItems(sContext);
        }
        sOppoPartition = SystemProperties.getBoolean(OPPO_PARTITION, false);
        Rlog.d(TAG, "ExpOperatorSwitchUtils init,sOppoPartition==" + sOppoPartition);
    }

    public static boolean isSupportOperatorSwitch(String opVersion) {
        for (String equals : sSupportOperator) {
            if (equals.equals(opVersion)) {
                Rlog.d(TAG, "support sim switch operator version");
                return true;
            }
        }
        Rlog.d(TAG, "not support sim switch");
        return false;
    }

    private static String getOperatorVersion() {
        String operatorPartition = readPartitionValues(OPERATOR_SWITCH_OFFSET);
        String operatorNVRAM = "";
        if (sOppoMtkPlatform) {
            operatorNVRAM = readOperatorSwitchMTKNV(MTK_COMMONSOFT_FLAG_BACKUP_PATH);
        } else if (sOppoQcomPlatform) {
            operatorNVRAM = readOperatorSwitchQCNV();
        }
        Rlog.d(TAG, "getOperatoVersion,operatorPartition==" + operatorPartition + ",operatorNVRAM==" + operatorNVRAM);
        return operatorNVRAM;
    }

    public static boolean setOperatorVersion(String opVersion) {
        writeOperatorSwitchPartition(opVersion);
        if (sOppoMtkPlatform) {
            if (writeOperatorSwitchMTKNV(opVersion)) {
                return true;
            }
        } else if (sOppoQcomPlatform && writeOperatorSwitchQCNV(opVersion)) {
            return true;
        }
        return false;
    }

    public static boolean isBasedOnMtk(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
    }

    public static boolean isBasedOnQcom(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.qualcomm");
    }

    private static void writeOperatorSwitchPartition(String opVersion) {
        int retryTimes = 3;
        while (!writePartitionValues(OPERATOR_SWITCH_OFFSET, opVersion) && retryTimes > 0) {
            try {
                SystemClock.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            retryTimes--;
        }
    }

    private static String getFileName() {
        if (sOppoMtkPlatform) {
            return MTK_PARTITION_PATH_OPERATOR_SWITCH;
        }
        if (sOppoQcomPlatform) {
            if (sOppoPartition) {
                return EXP_OPERATOR_SWITCH_PATH;
            }
            return QCOM_PARTITION_PATH_OPERATOR_SWITCH;
        } else if (sOppoPartition) {
            return EXP_OPERATOR_SWITCH_PATH;
        } else {
            return QCOM_PARTITION_PATH_OPERATOR_SWITCH;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a9 A:{SYNTHETIC, Splitter: B:31:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ae A:{Catch:{ IOException -> 0x00b7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b3 A:{Catch:{ IOException -> 0x00b7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0093 A:{SYNTHETIC, Splitter: B:21:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0098 A:{Catch:{ IOException -> 0x00a1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x009d A:{Catch:{ IOException -> 0x00a1 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean writePartitionValues(long offSet, String opVersion) {
        Exception e;
        Throwable th;
        boolean result = false;
        FileChannel fileChannel = null;
        FileLock fileLock = null;
        RandomAccessFile randomAccessFile = null;
        try {
            File file = new File(getFileName());
            boolean setWritable = file.setWritable(true);
            RandomAccessFile randomAccessFile2 = new RandomAccessFile(file, "rw");
            try {
                fileChannel = randomAccessFile2.getChannel();
                fileLock = fileChannel.lock();
                randomAccessFile2.seek(offSet);
                randomAccessFile2.writeBytes(opVersion);
                Rlog.d(TAG, "writePartitionValues " + file + "[" + setWritable + "]" + ", write opVersion: " + opVersion);
                result = true;
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (randomAccessFile2 != null) {
                    randomAccessFile2.close();
                }
                randomAccessFile = randomAccessFile2;
            } catch (Exception e3) {
                e = e3;
                randomAccessFile = randomAccessFile2;
                try {
                    Rlog.d(TAG, "writeData Exception e = " + e);
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (fileChannel != null) {
                        fileChannel.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                            throw th;
                        }
                    }
                    if (fileChannel != null) {
                        fileChannel.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = randomAccessFile2;
                if (fileLock != null) {
                }
                if (fileChannel != null) {
                }
                if (randomAccessFile != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Rlog.d(TAG, "writeData Exception e = " + e);
            if (fileLock != null) {
            }
            if (fileChannel != null) {
            }
            if (randomAccessFile != null) {
            }
            return result;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ea A:{SYNTHETIC, Splitter: B:37:0x00ea} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00ef A:{Catch:{ IOException -> 0x00f8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00f4 A:{Catch:{ IOException -> 0x00f8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0076  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String readPartitionValues(long offSet) {
        Exception e;
        Throwable th;
        String version = operatorNV;
        int result = -1;
        byte[] buffer = new byte[9];
        FileChannel fileChannel = null;
        FileLock fileLock = null;
        RandomAccessFile randomAccessFile = null;
        try {
            File file = new File(getFileName());
            Rlog.d(TAG, "readPartitionValues setReadable " + file + "[" + file.setReadable(true) + "]");
            RandomAccessFile randomAccessFile2 = new RandomAccessFile(file, "rw");
            try {
                fileChannel = randomAccessFile2.getChannel();
                fileLock = fileChannel.lock();
                randomAccessFile2.seek(offSet);
                result = randomAccessFile2.read(buffer, (int) offSet, operatorNV.length());
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (randomAccessFile2 != null) {
                    randomAccessFile2.close();
                }
                randomAccessFile = randomAccessFile2;
            } catch (Exception e3) {
                e = e3;
                randomAccessFile = randomAccessFile2;
            } catch (Throwable th2) {
                th = th2;
                randomAccessFile = randomAccessFile2;
                if (fileLock != null) {
                }
                if (fileChannel != null) {
                }
                if (randomAccessFile != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            try {
                Rlog.e(TAG, "readData Exception e = " + e);
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (buffer != null) {
                }
                version = version.substring(0, 8);
                Rlog.d(TAG, "readPartitionValues,result==" + result + ",version==" + version);
                return version;
            } catch (Throwable th3) {
                th = th3;
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                        throw th;
                    }
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        }
        if (buffer != null) {
            version = new String(buffer);
        }
        if (version != null && version.length() > 8) {
            version = version.substring(0, 8);
        }
        Rlog.d(TAG, "readPartitionValues,result==" + result + ",version==" + version);
        return version;
    }

    private static String readOperatorSwitchMTKNV(String path) {
        Rlog.d(TAG, "readOperatorSwitchMTKNV,operatorNVRAM==" + null);
        return null;
    }

    private static boolean writeOperatorSwitchMTKNV(String operator) {
        Rlog.d(TAG, "setOperatorVersion,operator==" + operator);
        if (operator == null) {
            Log.d(TAG, "value null setOperatorVersion fail");
            return false;
        }
        byte[] buff = new byte[9];
        char[] temp = operator.toCharArray();
        for (int i = 0; i < operator.length(); i++) {
            buff[i] = (byte) temp[i];
            Rlog.d(TAG, "byte = " + buff[i]);
        }
        buff[operator.length()] = (byte) 0;
        Rlog.d(TAG, "length = " + operator.length());
        return writeCommSoftMTKNV(MTK_COMMONSOFT_FLAG_BACKUP_PATH, buff);
    }

    private static boolean writeCommSoftMTKNV(String path, byte[] data) {
        Rlog.d(TAG, "writeCommSoftNVRAM,res==" + false);
        return false;
    }

    private static String readOperatorSwitchQCNV() {
        String operatorNVRAM = null;
        try {
            Rlog.d(TAG, "readOperatorSwitchQCNV");
            operatorNVRAM = sQcNvItems.getCarrierVersion();
        } catch (IOException e) {
            Rlog.d(TAG, "readOperatorSwitchQCNV IOException" + e.getMessage());
        }
        if (operatorNVRAM != null && operatorNVRAM.length() > 8) {
            operatorNVRAM = operatorNVRAM.substring(0, 8);
        }
        Rlog.d(TAG, "readOperatorSwitchQCNV,operatorNVRAM==" + operatorNVRAM);
        return operatorNVRAM;
    }

    private static boolean writeOperatorSwitchQCNV(String value) {
        try {
            Rlog.d(TAG, "setOperatorVersion,value==" + value);
            sQcNvItems.setCarrierVersion(value);
            return true;
        } catch (IOException e) {
            Rlog.d(TAG, "setOperatorVersion IOException" + e.getMessage());
            return false;
        }
    }

    public static boolean isFirstInsertSim() {
        if (Global.getInt(sContext.getContentResolver(), SIM_FIRST_INSERT_FLAG, 0) != 0) {
            return false;
        }
        return true;
    }

    public static void setFirstInsertSimFlag(int flag) {
        Global.putInt(sContext.getContentResolver(), SIM_FIRST_INSERT_FLAG, flag);
    }

    public static boolean oppoIsSpecOperator(String simCode, int slot, String version) {
        boolean isNeedChange = false;
        int index = -1;
        int i = 0;
        while (i < sOperatorSwitchInfo.length) {
            if (version.equals(sOperatorSwitchInfo[i][sOpIndex]) && simCode.equals(sOperatorSwitchInfo[i][sSimCodeIndex])) {
                isNeedChange = true;
                index = i;
                break;
            }
            i++;
        }
        Rlog.d(TAG, "oppoIsSpecOperator,isNeedChange==" + isNeedChange + ",index==" + index + "length=" + sOperatorSwitchInfo.length);
        if (isNeedChange && index != -1 && index < sOperatorSwitchInfo.length) {
            if (setOperatorVersion(sOperatorSwitchInfo[index][sNvIndex])) {
                sOperatorSwitchComplete = true;
            } else {
                sOperatorSwitchComplete = false;
            }
            Rlog.d(TAG, "oppoIsSpecOperator,sOperatorSwitchComplete =" + sOperatorSwitchComplete);
            if (sOperatorSwitchComplete && sOperatorSwitchInfo[index][sNvIndex].equals(getOperatorVersion())) {
                setOperatorIndexValue(index);
                notifyOpVersionChange(index, slot);
                Rlog.d(TAG, "oppoIsSpecOperator, set slot=" + slot + "NVRAM and Partition to modem complete!!!");
                return true;
            }
        }
        return false;
    }

    private static void oppoBroadCastChangeOperator(Context context, int index) {
        Rlog.d(TAG, "oppoBroadCastChangeOperator");
        Intent intent = new Intent("oppo.intent.action.CHANGE_OPERATOR");
        if (index < 0 || index >= sOperatorSwitchInfo.length) {
            Rlog.d(TAG, "oppoBroadCastChangeOperator,index = " + index);
        } else {
            intent.putExtra("operatorname", sOperatorSwitchInfo[index][sNameIndex]);
            intent.putExtra("operatorcode", sOperatorSwitchInfo[index][sSimCodeIndex]);
            intent.putExtra("switchreboot", "true");
            sContext.sendBroadcast(intent);
        }
        resetOperatorIndexValue();
    }

    public static void oppoBroadCastDelayHotswap() {
        if (sContext.getPackageManager().hasSystemFeature("oppo.commcenter.reboot.dialog")) {
            Rlog.d(TAG, "oppoBroadCastDelayHotswap");
            Intent intent = new Intent("oppo.intent.action.HOT_SWAP");
            intent.putExtra("operatorname", "null");
            intent.putExtra("operatorcode", "null");
            intent.putExtra("switchreboot", "false");
            sContext.sendBroadcast(intent);
            return;
        }
        Rlog.d(TAG, "oppoBroadCastDelayHotswap,needn't show reboot dialog");
    }

    private static void oppoSetDataRoamingEnabled(int index, int slot) {
        if (sOperatorSwitchInfo[index][sSimCodeIndex].equals("53024")) {
            ColorOSTelephonyManager colorOSTelephonyManager = ColorOSTelephonyManager.getDefault(sContext);
            if (colorOSTelephonyManager != null) {
                colorOSTelephonyManager.colorSetDataRoamingEnabled(SLOT0, true);
                colorOSTelephonyManager.colorSetDataRoamingEnabled(SLOT1, true);
            }
            Rlog.d(TAG, "colorSetDataRoamingEnabled is true");
        }
    }

    private static void notifyOpVersionChange(int index, int slot) {
        oppoHandlerChangeOperator(index);
    }

    private static void oppoHandlerChangeOperator(int index) {
        if (isBootWizardCompleted()) {
            oppoBroadCastChangeOperator(sContext, index);
        } else {
            oppoRegisterBootWizard(index);
        }
    }

    private static void oppoRegisterBootWizard(int index) {
        Rlog.d(TAG, "bootreg welcomepage showing, no need show sim changed dialog");
        sContext.getContentResolver().registerContentObserver(Global.getUriFor("device_provisioned"), true, sInWelcomePage);
    }

    private static boolean isBootWizardCompleted() {
        boolean isBootWizardCompleted = Global.getInt(sContext.getContentResolver(), "device_provisioned", 0) == 1;
        Rlog.d(TAG, "isBootWizardCompleted:" + isBootWizardCompleted);
        return isBootWizardCompleted;
    }

    private static void setOperatorIndexValue(int index) {
        sIndex = index;
    }

    private static int getOperatorIndexValue() {
        return sIndex;
    }

    private static void resetOperatorIndexValue() {
        sIndex = -1;
    }
}

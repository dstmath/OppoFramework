package com.android.internal.telephony;

import android.content.Context;
import android.engineer.OppoEngineerManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;

public class OemDeviceLockUtils {
    private static long DAY_MILLIS = 86400000;
    public static final int FIRST_BIND = 0;
    public static String INIT_ICCID = "00000000000000000000";
    public static String INIT_IMSI = "000000000000000";
    public static long INIT_TIME_TYPE1 = 0;
    public static String INIT_TIME_TYPE2 = "000000000000000";
    public static final int LAST_BIND = 1;
    public static final String LOCK_FLAG_OFF = "0";
    public static final String LOCK_FLAG_ON = "1";
    public static final String LOCK_SIM_DAYS = "locked_sim_days";
    public static final String LOCK_SIM_ICCID = "locked_sim_iccid_key";
    public static final String LOCK_SIM_IMSI = "locked_sim_imsi_key";
    public static final String LOCK_SIM_OPERATOR = "locked_sim_operator_key";
    public static final String LOCK_SIM_STATUS = "locked_sim_status";
    public static final String LOCK_SIM_TIME = "locked_sim_time";
    private static final int MAX_LENGTH = 8;
    private static final int RW_RETRY_TIMES = 3;
    private static final int SIM_SWITCH_WRITE_SLEEP_TIMES = 100;
    public static final int SIZE_FIRST_LOCKED_TIME = 15;
    private static final String TAG = "OemDeviceLockUtils";
    private static Context sContext;
    private static boolean sOppoMtkPlatform = false;
    private static boolean sOppoQcomPlatform = false;

    public static void init(Context context) {
        sContext = context;
        if (sContext != null) {
            sOppoMtkPlatform = isBasedOnMtk(sContext);
            sOppoQcomPlatform = isBasedOnQcom(sContext);
        }
    }

    public static boolean isBasedOnMtk(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
    }

    public static boolean isBasedOnQcom(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.qualcomm");
    }

    public static String getLockedIMSI(Context context) {
        String imsi = OppoEngineerManager.getDeviceLockIMSI();
        if (imsi == null) {
            imsi = INIT_IMSI;
        }
        try {
            Long.valueOf(imsi);
        } catch (Exception e) {
            imsi = INIT_IMSI;
        }
        if (OemDeviceLock.DBG) {
            Rlog.d(TAG, "getLockedIMSI = " + imsi);
        }
        return imsi;
    }

    public static boolean setSimIMSI(Context context, String imsi) {
        return OppoEngineerManager.setDeviceLockIMSI(imsi);
    }

    public static String getLockedIccid(Context context) {
        String iccid = OppoEngineerManager.getDeviceLockICCID();
        if (iccid == null) {
            iccid = INIT_ICCID;
        }
        Rlog.d(TAG, "getLockedIccid ==" + iccid);
        try {
            Long.valueOf(iccid.substring(0, 6));
        } catch (Exception e) {
            iccid = INIT_ICCID;
        }
        if (OemDeviceLock.DBG) {
            Rlog.d(TAG, "getLockedIccid = " + iccid);
        }
        return iccid;
    }

    public static boolean setSimIccid(Context context, String iccid) {
        return OppoEngineerManager.setDeviceLockICCID(iccid);
    }

    public static boolean getLockedStatusFlag(Context context) {
        String status = SystemProperties.get(OemDeviceLock.PERSIST_LOCK, "NA");
        if ("NA".equals(status)) {
            status = OppoEngineerManager.getDeviceLockStatus();
        }
        if (status != null) {
            Rlog.d(TAG, "getLockedStatusFlag==" + status);
        }
        if (!"1".equals(status) && "0".equals(status)) {
            return false;
        }
        return true;
    }

    public static void setLockedStatusFlag(Context context, String flag) {
        OppoEngineerManager.setDeviceLockStatus(flag);
        SystemProperties.set(OemDeviceLock.PERSIST_LOCK, flag);
    }

    public static long getDeviceLockedTimes(Context context, int type) {
        long j = INIT_TIME_TYPE1;
        return getPartitionLockedTimes(type);
    }

    private static long getPartitionLockedTimes(int type) {
        String times = INIT_TIME_TYPE2;
        if (type == 0) {
            times = OppoEngineerManager.getDeviceLockFirstBindTime();
        } else if (1 == type) {
            times = OppoEngineerManager.getDeviceLockLastBindTime();
        }
        if (times == null) {
            times = INIT_TIME_TYPE2;
        }
        int index = times.indexOf(46);
        if (index != -1) {
            times = times.substring(0, index);
        }
        long time = INIT_TIME_TYPE1;
        try {
            time = Long.valueOf(times).longValue();
        } catch (Exception e) {
        }
        if (OemDeviceLock.DBG) {
            Rlog.d(TAG, "getPartitionLockedTimes==" + times + "time==" + time);
        }
        return time;
    }

    public static void setDeviceLockedTimes(Context context, String times, int type) {
        if (!INIT_TIME_TYPE2.equals(times)) {
            int length = times.length();
            for (int i = times.length(); i < 15; i++) {
                times = times + ".";
            }
        }
        setPartitionLockedTimes(times, type);
    }

    private static void setPartitionLockedTimes(String times, int type) {
        if (type == 0) {
            OppoEngineerManager.setDeviceLockFirstBindTime(times);
        } else if (1 == type) {
            OppoEngineerManager.setDeviceLockLastBindTime(times);
        }
    }

    public static long getLockedContractDays(Context context) {
        long days = getPartitionContractDays();
        if (OemDeviceLock.DBG) {
            Rlog.d(TAG, "getLockedContractDays ==" + days);
        }
        return days;
    }

    public static long getUnlockDateTimeMillis() {
        String times = OppoEngineerManager.getDeviceLockUnlockTime();
        if (TextUtils.isEmpty(times)) {
            Rlog.d(TAG, "getUnlockDateTimeMillis not set or has error");
            return INIT_TIME_TYPE1;
        }
        int index = times.indexOf(46);
        if (index != -1) {
            times = times.substring(0, index);
        }
        long time = INIT_TIME_TYPE1;
        try {
            time = Long.valueOf(times).longValue();
        } catch (Exception e) {
        }
        if (OemDeviceLock.DBG) {
            Rlog.d(TAG, "getUnlockDateTimeMillis==" + times + "time==" + time);
        }
        return time;
    }

    public static boolean getAutoUnLockStatus(Context context, long time) {
        boolean success = false;
        long days = 0;
        long unlockTime = 0;
        long lockedTime = getDeviceLockedTimes(context, 0);
        long timeGap = time - lockedTime;
        if (timeGap < 0 || lockedTime == 0) {
            Rlog.d(TAG, "getAutoUnLockStatus is wrong");
        } else {
            unlockTime = getUnlockDateTimeMillis();
            if (unlockTime == INIT_TIME_TYPE1) {
                days = timeGap / DAY_MILLIS;
                if (days >= getLockedContractDays(context)) {
                    success = true;
                    Rlog.d(TAG, "getAutoUnLockStatus is success for days");
                }
            } else if (time >= unlockTime) {
                success = true;
                Rlog.d(TAG, "getAutoUnLockStatus is success after unlock time");
            }
        }
        if (OemDeviceLock.DBG) {
            Rlog.d(TAG, "getAutoUnLockStatus success " + success + ",days = " + days + ",time = " + time + "lockedTime = " + lockedTime + ",unlockTime = " + unlockTime);
        }
        return success;
    }

    public static void setNeedSyncLockedTime(Context context) {
        if (INIT_TIME_TYPE1 == getDeviceLockedTimes(context, 0)) {
            setDeviceLockedTimes(context, String.valueOf(getDeviceLockedTimes(context, 1)), 0);
        }
    }

    public static long getPartitionContractDays() {
        String sDays = OppoEngineerManager.getDeviceLockDays();
        if (sDays == null) {
            return 365;
        }
        long days;
        try {
            days = Long.valueOf(sDays).longValue();
        } catch (Exception e) {
            days = 365;
        }
        return days;
    }

    public static void setLockedContractDays(Context context, String days) {
        setPartitionContractDays(days);
    }

    private static void setPartitionContractDays(String days) {
        OppoEngineerManager.setDeviceLockDays(days);
    }
}

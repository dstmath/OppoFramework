package com.oppo.internal.telephony.explock;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.oppo.internal.telephony.OppoNewNitzStateMachine;
import com.oppo.internal.telephony.explock.util.DeviceLockData;
import com.oppo.internal.telephony.explock.util.RegionLockData;
import com.oppo.internal.telephony.explock.util.RpmbUtil;
import com.oppo.internal.telephony.utils.OppoEngineerManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OemLockUtils {
    private static long DAY_MILLIS = OppoNewNitzStateMachine.NITZ_NTP_INTERVAL_OEM;
    private static final boolean DBG = OemConstant.SWITCH_LOG;
    public static final int FIRST_BIND = 0;
    public static final String IGNORE_HASH_ICCID = "gsm.ignore.hash.iccid";
    public static final String IGNORE_HASH_NETLOCK_STATUS = "gsm.ignore.hash.netlock.status";
    public static final String IGNORE_HASH_STATUS = "gsm.ignore.hash.status";
    public static final String INIT_ICCID = "00000000000000000000";
    public static final String INIT_IMSI = "000000000000000";
    public static final long INIT_TIME_TYPE1 = 0;
    public static final String INIT_TIME_TYPE2 = "000000000000000";
    public static Map<String, String> IccidMap = new ConcurrentHashMap();
    public static final int LAST_BIND = 1;
    public static final String LOCK_FLAG_OFF = "0";
    public static final String LOCK_FLAG_ON = "1";
    public static final List<String> OPERATOR_LIST = new ArrayList(Arrays.asList(OPERATOR_NAME));
    public static final String[] OPERATOR_NAME = {"AIS", "TRUE", "DTAC"};
    public static final String REGION_LOCK_STATUS = "region_lock_status";
    public static final String REGION_NETLOCK_RPMB = "persist.sys.rpmb.lock";
    public static final int SIZE_FIRST_LOCKED_TIME = 15;
    private static final String TAG = "OemLockUtils";
    public static final String TH_LOCK_ICCID = "th_lock_iccid";
    public static final String TH_LOCK_STATUS = "th_lock_status";
    private static boolean isEncryptVersion_1 = false;
    private static boolean isGetUpgradeProject = false;
    private static boolean isLockUpgradeProject = false;
    public static Map<String, String> rLockstatusMap = new ConcurrentHashMap();
    private static Context sContext;
    private static String sCurrentRegionLockCountry = null;
    private static boolean sHasCountry = false;
    private static boolean sOppoMtkPlatform = false;
    private static boolean sOppoQcomPlatform = false;
    public static Map<String, String> statusMap = new ConcurrentHashMap();

    public static void init(Context context) {
        sContext = context;
    }

    public static String getLockedIMSI(Context context) {
        String imsi;
        RpmbUtil.checkMoveDataToRpmb();
        if (RpmbUtil.hasMovedDataToRpmb()) {
            String iccidRpmb = RpmbUtil.getRpmbLockIMSI();
            if (!TextUtils.isEmpty(iccidRpmb)) {
                imsi = iccidRpmb;
            } else {
                imsi = OppoEngineerManager.getDeviceLockIMSI();
            }
        } else {
            imsi = OppoEngineerManager.getDeviceLockIMSI();
        }
        if (imsi == null) {
            imsi = "000000000000000";
        }
        try {
            Long.valueOf(imsi);
            return imsi;
        } catch (Exception e) {
            return "000000000000000";
        }
    }

    public static boolean setSimIMSI(Context context, String imsi) {
        boolean success = RpmbUtil.setRpmbLockIMSI(imsi);
        if (success) {
            return OppoEngineerManager.setDeviceLockIMSI(imsi);
        }
        return success;
    }

    public static String getLockedIccid(Context context) {
        String iccid;
        String hashIccid = getHashLockIccid();
        if (getHashLockIccid() != null) {
            if (DBG) {
                Rlog.d(TAG, "getLockedIccid hash = " + hashIccid);
            }
            return hashIccid;
        }
        boolean fromRpmb = false;
        boolean fromReserve = false;
        RpmbUtil.checkMoveDataToRpmb();
        if (RpmbUtil.hasMovedDataToRpmb()) {
            String iccidRpmb = RpmbUtil.getRpmbLockIccid();
            if (!TextUtils.isEmpty(iccidRpmb)) {
                iccid = iccidRpmb;
                fromRpmb = true;
            } else {
                iccid = OppoEngineerManager.getDeviceLockICCID();
                fromReserve = true;
            }
        } else {
            iccid = OppoEngineerManager.getDeviceLockICCID();
            fromReserve = true;
        }
        if (iccid == null) {
            iccid = INIT_ICCID;
        }
        try {
            Long.valueOf(iccid.substring(0, 6));
        } catch (Exception e) {
            iccid = INIT_ICCID;
        }
        if (iccid != INIT_ICCID) {
            IccidMap.put(TH_LOCK_ICCID, iccid);
        }
        if (DBG) {
            Rlog.d(TAG, "getLockedIccid = " + iccid + ",fromRpmb = " + fromRpmb + ",fromReserve = " + fromReserve);
        }
        return iccid;
    }

    private static String getHashLockIccid() {
        Map<String, String> map;
        if (!isIgnoreHashLockInfo(IGNORE_HASH_ICCID) && (map = IccidMap) != null && map.size() > 0) {
            return IccidMap.get(TH_LOCK_ICCID);
        }
        return null;
    }

    public static boolean setSimIccid(Context context, String iccid) {
        boolean success = RpmbUtil.setRpmbLockIccid(iccid);
        if (success) {
            updateHashLockIccid(iccid);
            OppoEngineerManager.setDeviceLockICCID(iccid);
            setUseHashLockInfo(IGNORE_HASH_ICCID);
        }
        return success;
    }

    public static boolean getLockedStatusFlag() {
        String statusFlag;
        String status = getHashLockStatus();
        if (!TextUtils.isEmpty(status)) {
            if ("1".equals(status)) {
                return true;
            }
            if ("0".equals(status)) {
                return false;
            }
        }
        RpmbUtil.checkMoveDataToRpmb();
        if (RpmbUtil.hasMovedDataToRpmb()) {
            String statusRpmb = RpmbUtil.getRpmbLockStatus();
            if (TextUtils.isEmpty(statusRpmb)) {
                statusFlag = OppoEngineerManager.getDeviceLockStatus();
            } else if ("1".equals(statusRpmb) || "0".equals(statusRpmb)) {
                statusFlag = statusRpmb;
            } else {
                statusFlag = OppoEngineerManager.getDeviceLockStatus();
            }
        } else {
            statusFlag = OppoEngineerManager.getDeviceLockStatus();
        }
        if (DBG) {
            Rlog.d(TAG, "getLockedStatusFlag = " + statusFlag);
        }
        return "1".equals(statusFlag) || !"0".equals(statusFlag);
    }

    private static void updateHashLockStatus(String status) {
        statusMap.clear();
        statusMap.put(TH_LOCK_STATUS, status);
    }

    private static String getHashLockStatus() {
        Map<String, String> map;
        if (!isIgnoreHashLockInfo(IGNORE_HASH_STATUS) && (map = statusMap) != null && map.size() > 0) {
            return statusMap.get(TH_LOCK_STATUS);
        }
        return null;
    }

    public static boolean setLockedStatusFlag(Context context, String flag) {
        boolean success = RpmbUtil.setRpmbLockStatus(flag);
        if (!success) {
            return success;
        }
        updateHashLockStatus(flag);
        boolean success2 = OppoEngineerManager.setDeviceLockStatus(flag);
        SystemProperties.set(OemDeviceLock.PERSIST_LOCK, flag);
        setUseHashLockInfo(IGNORE_HASH_ICCID);
        return success2;
    }

    public static long getDeviceLockedTimes(Context context, int type) {
        long time = 0;
        String times = "000000000000000";
        RpmbUtil.checkMoveDataToRpmb();
        if (RpmbUtil.hasMovedDataToRpmb()) {
            if (type == 0) {
                times = RpmbUtil.getRpmbLockFirstBindTime();
            } else if (1 == type) {
                times = RpmbUtil.getRpmbLockLastBindTime();
            }
        } else if (type == 0) {
            times = OppoEngineerManager.getDeviceLockFirstBindTime();
        } else if (1 == type) {
            times = OppoEngineerManager.getDeviceLockLastBindTime();
        }
        if (times == null) {
            times = "000000000000000";
        }
        int index = times.indexOf(46);
        if (index != -1) {
            times = times.substring(0, index);
        }
        try {
            time = Long.valueOf(times).longValue();
        } catch (Exception e) {
        }
        if (DBG) {
            Rlog.d(TAG, "getPartitionLockedTimes==" + times + "time==" + time);
        }
        return time;
    }

    public static void setDeviceLockedTimes(Context context, String times, int type) {
        if (!"000000000000000".equals(times)) {
            times.length();
            for (int i = times.length(); i < 15; i++) {
                times = times + ".";
            }
        }
        setPartitionLockedTimes(times, type);
    }

    private static void setPartitionLockedTimes(String times, int type) {
        if (type == 0) {
            if (RpmbUtil.setRpmbLockFirstBindTime(times)) {
                OppoEngineerManager.setDeviceLockFirstBindTime(times);
            }
        } else if (1 == type && RpmbUtil.setRpmbLockLastBindTime(times)) {
            OppoEngineerManager.setDeviceLockLastBindTime(times);
        }
    }

    public static long getLockedContractDays(Context context) {
        long days;
        RpmbUtil.checkMoveDataToRpmb();
        if (RpmbUtil.hasMovedDataToRpmb()) {
            String dayRpmb = RpmbUtil.getRpmbLockDays();
            if (!TextUtils.isEmpty(dayRpmb)) {
                try {
                    days = Long.valueOf(dayRpmb).longValue();
                } catch (Exception e) {
                    days = 365;
                }
            } else {
                days = getPartitionContractDays();
            }
        } else {
            days = getPartitionContractDays();
        }
        if (DBG) {
            Rlog.d(TAG, "getLockedStatusFlag = " + days);
        }
        return days;
    }

    public static long getUnlockDateTimeMillis(Context context) {
        String timeStr;
        long time = 0;
        RpmbUtil.checkMoveDataToRpmb();
        if (RpmbUtil.hasMovedDataToRpmb()) {
            timeStr = RpmbUtil.getRpmbLockUnlockTime();
        } else {
            timeStr = OppoEngineerManager.getDeviceLockUnlockTime();
        }
        if (TextUtils.isEmpty(timeStr)) {
            Rlog.d(TAG, "getUnlockDateTimeMillis not set or has error");
            return 0;
        }
        int index = timeStr.indexOf(46);
        if (index != -1) {
            timeStr = timeStr.substring(0, index);
        }
        try {
            time = Long.valueOf(timeStr).longValue();
        } catch (Exception e) {
        }
        if (OemDeviceLock.DBG) {
            Rlog.d(TAG, "getUnlockDateTimeMillis = " + timeStr + " time = " + time);
        }
        return time;
    }

    public static boolean getAutoUnLockStatus(Context context, long time) {
        boolean success = false;
        long days = 0;
        long unlockTime = 0;
        if (isSkipCurrentAutoUnlock(time)) {
            return false;
        }
        long lockedTime = getDeviceLockedTimes(context, 0);
        long timeGap = time - lockedTime;
        if (timeGap < 0 || lockedTime == 0) {
            Rlog.d(TAG, "getAutoUnLockStatus is wrong");
        } else {
            unlockTime = getUnlockDateTimeMillis(context);
            if (unlockTime == 0) {
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
        if (DBG) {
            Rlog.d(TAG, "getAutoUnLockStatus success " + success + ",days = " + days + ",time = " + time + "lockedTime = " + lockedTime + ",unlockTime = " + unlockTime);
        }
        return success;
    }

    private static boolean isSkipCurrentAutoUnlock(long time) {
        if ((time - System.currentTimeMillis()) / DAY_MILLIS >= 3) {
            return true;
        }
        return false;
    }

    public static void setNeedSyncLockedTime(Context context) {
        if (0 == getDeviceLockedTimes(context, 0)) {
            setDeviceLockedTimes(context, String.valueOf(getDeviceLockedTimes(context, 1)), 0);
        }
    }

    public static long getPartitionContractDays() {
        String sDays = OppoEngineerManager.getDeviceLockDays();
        if (sDays == null) {
            return 365;
        }
        try {
            return Long.valueOf(sDays).longValue();
        } catch (Exception e) {
            return 365;
        }
    }

    public static void setLockedContractDays(Context context, String days) {
        if (RpmbUtil.setRpmbLockDays(days)) {
            setPartitionContractDays(days);
        }
    }

    private static void setPartitionContractDays(String days) {
        OppoEngineerManager.setDeviceLockDays(days);
    }

    public static DeviceLockData getDeviceLockDataToRpmb(boolean isLockEnabled) {
        DeviceLockData rpmbData = new DeviceLockData();
        if (OemDeviceLock.IS_TH_LOCK) {
            rpmbData.setLockedOperator(OemDeviceLock.OP_LOCK);
        } else if (isLockUpgradeProject()) {
            return null;
        } else {
            rpmbData.setLockedOperator("NA");
        }
        String lockedState = getLockedState();
        if (TextUtils.isEmpty(lockedState)) {
            return null;
        }
        rpmbData.setLockedState(lockedState);
        String lockedIMSI = getLockedIMSI();
        if (TextUtils.isEmpty(lockedIMSI)) {
            return null;
        }
        rpmbData.setLockedIMSI(lockedIMSI);
        String lockedDays = getLockedContractDays();
        if (TextUtils.isEmpty(lockedDays)) {
            return null;
        }
        rpmbData.setContractDays(lockedDays);
        String firstBindTime = getPartitionBindTime(0);
        if (TextUtils.isEmpty(firstBindTime)) {
            return null;
        }
        rpmbData.setFirstBindTime(firstBindTime);
        String lockedICCID = getLockedIccid();
        if (TextUtils.isEmpty(lockedICCID)) {
            return null;
        }
        rpmbData.setLockedICCID(lockedICCID);
        String lastBindTime = getPartitionBindTime(1);
        if (TextUtils.isEmpty(lastBindTime)) {
            return null;
        }
        rpmbData.setLastBindTime(lastBindTime);
        String unlockDate = getPartitionUnlockDate();
        if (TextUtils.isEmpty(unlockDate)) {
            rpmbData.setUnlockDate("000000000000000");
        } else {
            rpmbData.setUnlockDate(unlockDate);
        }
        return rpmbData;
    }

    private static String getLockedState() {
        String lockState = OppoEngineerManager.getDeviceLockStatus();
        if (!"1".equals(lockState) && !"0".equals(lockState)) {
            lockState = "1";
        }
        Rlog.d(TAG, "getLockedState = " + lockState);
        return lockState;
    }

    public static String getLockedIMSI() {
        String imsi = OppoEngineerManager.getDeviceLockIMSI();
        if (imsi == null) {
            imsi = "000000000000000";
        }
        try {
            Long.valueOf(imsi);
        } catch (Exception e) {
            imsi = "000000000000000";
        }
        if (DBG) {
            Rlog.d(TAG, "getLockedIMSI = " + imsi);
        }
        return imsi;
    }

    public static String getLockedContractDays() {
        String dayStr = OppoEngineerManager.getDeviceLockDays();
        if (dayStr == null) {
            dayStr = "365";
        }
        try {
            Long.valueOf(dayStr);
            return dayStr;
        } catch (Exception e) {
            return "365";
        }
    }

    private static String getPartitionBindTime(int type) {
        String patitionTime = null;
        if (type == 0) {
            patitionTime = OppoEngineerManager.getDeviceLockFirstBindTime();
        } else if (1 == type) {
            patitionTime = OppoEngineerManager.getDeviceLockLastBindTime();
        }
        if (TextUtils.isEmpty(patitionTime)) {
            return "000000000000000";
        }
        return patitionTime;
    }

    public static String getLockedIccid() {
        try {
            String iccid = OppoEngineerManager.getDeviceLockICCID();
            Long.valueOf(iccid.substring(0, 6));
            return iccid;
        } catch (Exception e) {
            return INIT_ICCID;
        }
    }

    public static String getPartitionUnlockDate() {
        String dateString = OppoEngineerManager.getDeviceLockUnlockTime();
        if (TextUtils.isEmpty(dateString)) {
            Rlog.d(TAG, "getPartitionUnlockDate not set or has error");
            return "000000000000000";
        }
        int index = dateString.indexOf(46);
        if (index != -1) {
            dateString = dateString.substring(0, index);
        }
        try {
            Long.valueOf(dateString);
        } catch (Exception e) {
            dateString = "000000000000000";
        }
        Rlog.d(TAG, "getPartitionUnlockDate = " + dateString);
        return dateString;
    }

    public static void setUseHashLockInfo(String value) {
        SystemProperties.set(value, "false");
    }

    private static boolean isIgnoreHashLockInfo(String value) {
        if ("true".equals(SystemProperties.get(value, "false"))) {
            return true;
        }
        return false;
    }

    private static void updateHashLockIccid(String iccid) {
        IccidMap.clear();
        IccidMap.put(TH_LOCK_ICCID, iccid);
    }

    public static RegionLockData getRegionNetLockDataToRpmb() {
        RegionLockData rpmbData = new RegionLockData();
        String country = RegionLockConstant.VERSION;
        if (TextUtils.isEmpty(country) || !country.equals("IN")) {
            rpmbData.setRegionLockCountry("NA");
        } else {
            rpmbData.setRegionLockCountry(country);
        }
        String status = getRegionLockStatusFlag();
        if (TextUtils.isEmpty(status)) {
            return null;
        }
        rpmbData.setRegionLockStatus(status);
        return rpmbData;
    }

    private static String getRegionLockStatusFlag() {
        String lockStatus = OppoEngineerManager.getRegionNetlockStatus();
        if (!"1".equals(lockStatus) && !"0".equals(lockStatus)) {
            lockStatus = "1";
        }
        Rlog.d(TAG, "getRegionLockStatus = " + lockStatus);
        return lockStatus;
    }

    public static String getRegionLockVersion() {
        if (sHasCountry && !TextUtils.isEmpty(sCurrentRegionLockCountry)) {
            return sCurrentRegionLockCountry;
        }
        RpmbUtil.checkMoveDataToRpmb();
        if (RpmbUtil.hasMovedDataToRpmb()) {
            String country = RpmbUtil.getRpmbRegionLockCountry();
            if ("IN".equals(country)) {
                sCurrentRegionLockCountry = country;
                SystemProperties.set(REGION_NETLOCK_RPMB, country);
            } else {
                SystemProperties.set(REGION_NETLOCK_RPMB, "NA");
                sCurrentRegionLockCountry = RegionLockConstant.VERSION;
            }
        } else {
            sCurrentRegionLockCountry = RegionLockConstant.VERSION;
            SystemProperties.set(REGION_NETLOCK_RPMB, "NA");
        }
        sHasCountry = true;
        return sCurrentRegionLockCountry;
    }

    public static boolean isRegionLock() {
        if (!isLockUpgradeProject() && OemConstant.EXP_VERSION && !OemConstant.RM_VERSION && "IN".equals(getRegionLockVersion())) {
            return true;
        }
        return false;
    }

    public static boolean isEnableUpRlock() {
        return OemConstant.EXP_VERSION && !OemConstant.RM_VERSION && RegionLockConstant.UP_TO_V2 && !RegionLockConstant.DISABLE_V2;
    }

    public static boolean getRegionLockStatus() {
        String statusFlag;
        String statusFlag2;
        String status = getHashRegionLockStatus();
        if (!TextUtils.isEmpty(status)) {
            if ("1".equals(status)) {
                return true;
            }
            if ("0".equals(status)) {
                return false;
            }
        }
        RpmbUtil.checkMoveDataToRpmb();
        if (RpmbUtil.hasMovedDataToRpmb()) {
            String statusRpmb = RpmbUtil.getRpmbRegionLockStatus();
            if (TextUtils.isEmpty(statusRpmb)) {
                statusFlag = OppoEngineerManager.getRegionNetlockStatus();
            } else if ("1".equals(statusRpmb) || "0".equals(statusRpmb)) {
                statusFlag = statusRpmb;
            } else {
                statusFlag = OppoEngineerManager.getRegionNetlockStatus();
            }
        } else {
            statusFlag = OppoEngineerManager.getRegionNetlockStatus();
        }
        if ("0".equals(statusFlag)) {
            statusFlag2 = "0";
        } else {
            statusFlag2 = "1";
        }
        updateHashRegionLockStatus(statusFlag2);
        if ("1".equals(statusFlag2)) {
            return true;
        }
        return "0".equals(statusFlag2) ? false : false;
    }

    public static boolean setRegionLockedStatus(String status) {
        boolean success = RpmbUtil.setRpmbRegionLockStatus(status);
        if (!success) {
            return success;
        }
        updateHashRegionLockStatus(status);
        boolean success2 = OppoEngineerManager.setRegionNetlock(status);
        setUseHashLockInfo(IGNORE_HASH_NETLOCK_STATUS);
        return success2;
    }

    private static void updateHashRegionLockStatus(String status) {
        rLockstatusMap.clear();
        rLockstatusMap.put(REGION_LOCK_STATUS, status);
    }

    private static String getHashRegionLockStatus() {
        Map<String, String> map;
        if (!isIgnoreHashLockInfo(IGNORE_HASH_NETLOCK_STATUS) && (map = rLockstatusMap) != null && map.size() > 0) {
            return rLockstatusMap.get(REGION_LOCK_STATUS);
        }
        return null;
    }

    public static boolean isCallOutEnableExp(Phone phone) {
        if (isDeviceLockEnable(phone)) {
            return false;
        }
        return true;
    }

    private static boolean isDeviceLockEnable(Phone phone) {
        if (!OemConstant.EXP_VERSION || !isLockUpgradeProject() || phone == null) {
            return false;
        }
        OemDeviceLock.getInstance(phone.getContext());
        if (!OemDeviceLock.getSimLoaded(phone.getPhoneId()) || !OemDeviceLock.getDeviceLockedForPhone(phone.getPhoneId())) {
            return false;
        }
        return true;
    }

    public static boolean isDeviceLockVersion() {
        if (OemConstant.EXP_VERSION && isLockUpgradeProject()) {
            return OemDeviceLock.isOperatorLock();
        }
        return false;
    }

    public static boolean isLockUpgradeProject() {
        if (isGetUpgradeProject) {
            return isLockUpgradeProject;
        }
        if ("BD120".equals(OemConstant.PROJECT_NAME) || "BD150".equals(OemConstant.PROJECT_NAME) || "BD153".equals(OemConstant.PROJECT_NAME) || "BD173".equals(OemConstant.PROJECT_NAME) || "BD176".equals(OemConstant.PROJECT_NAME) || "BD209".equals(OemConstant.PROJECT_NAME)) {
            isLockUpgradeProject = true;
        } else if ("DC066".equals(OemConstant.PROJECT_NAME) || "DC069".equals(OemConstant.PROJECT_NAME) || "DC073".equals(OemConstant.PROJECT_NAME) || "DC090".equals(OemConstant.PROJECT_NAME) || "DC097".equals(OemConstant.PROJECT_NAME) || "DC091".equals(OemConstant.PROJECT_NAME) || "DC092".equals(OemConstant.PROJECT_NAME) || "DC111".equals(OemConstant.PROJECT_NAME)) {
            isLockUpgradeProject = true;
            isEncryptVersion_1 = true;
        } else {
            isLockUpgradeProject = false;
        }
        SystemProperties.set(OemDeviceLock.LOCK_UPGRADE_PRJ, Boolean.toString(isLockUpgradeProject));
        isGetUpgradeProject = true;
        return isLockUpgradeProject;
    }

    public static boolean isEncryptVersion_1() {
        return isEncryptVersion_1;
    }
}

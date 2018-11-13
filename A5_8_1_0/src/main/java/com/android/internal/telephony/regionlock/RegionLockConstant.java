package com.android.internal.telephony.regionlock;

import android.os.SystemProperties;

public class RegionLockConstant {
    public static final String ACTION_CALLING_DISCONNECTED = "oppo.intent.action.CALLING_DISCONNECTED";
    public static final String ACTION_NETWORK_LOCK = "oppo.intent.action.REGION_NETWORK_LOCK_STATUS";
    public static final String ACTION_UNLOCK_NETWORK_SIM1 = "oppo.action.UNLOCK_NETWORK_SIM1";
    public static final String ACTION_UNLOCK_NETWORK_SIM2 = "oppo.action.UNLOCK_NETWORK_SIM2";
    public static final int DEFAULT_TIMES = 3600000;
    public static final int EVENT_NETWORK_LOCK_STATUS = 5000;
    public static final int EVENT_UPDATE_POWER_RADIO = 5001;
    public static final boolean IS_REGION_LOCK = (SystemProperties.get(NETLOCK_VERSION, "NULL").equals("NULL") ^ 1);
    public static final String NETLOCK_FLAG_OFF = "0";
    public static final String NETLOCK_FLAG_ON = "1";
    public static final String NETLOCK_STATUS = "netlockstatus";
    public static final String NETLOCK_VERSION = "ro.oppo.region.netlock";
    public static final String NOTIFY_NETLOCK_FLAG = "ril.oppo.lock.flag";
    public static final String NOTIFY_NETWORK_OFF = "0";
    public static final String NOTIFY_NETWORK_ON = "1";
    public static final String PERSIST_LOCK_TIME = "persist.oppo.nw.locktime";
    public static final String PERSIST_NETLOCK = "persist.oppo.nw.region.lock";
    public static final String PERSIST_TEST_OP = "persist.oppo.nw.test.operator";
    public static final String TEST_OP_CMCC = "1";
    public static final String TEST_OP_CU = "0";
    public static final String TEST_OP_CUANDCMCC = "2";
    public static final String TEST_OP_DEFAULT = "4";
    public static final String UNLOCK_NORMAL = "1";
    public static final String UNLOCK_TYPE = "unlocktype";
    public static final String VERSION = SystemProperties.get(NETLOCK_VERSION, "NULL");

    public static boolean getRegionLockStatus() {
        return SystemProperties.get(PERSIST_NETLOCK, "0").equals("1");
    }
}

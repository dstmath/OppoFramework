package com.oppo.internal.telephony.explock;

import android.os.SystemProperties;
import com.android.internal.telephony.OemConstant;

public class RegionLockConstant {
    public static final String ACTION_CALLING_DISCONNECTED = "oppo.intent.action.CALLING_DISCONNECTED";
    public static final String ACTION_NETWORK_LOCK = "oppo.intent.action.REGION_NETWORK_LOCK_STATUS";
    public static final String ACTION_UNLOCK_NETWORK_SIM1 = "oppo.action.UNLOCK_NETWORK_SIM1";
    public static final String ACTION_UNLOCK_NETWORK_SIM2 = "oppo.action.UNLOCK_NETWORK_SIM2";
    public static final int DEFAULT_TIMES = 21600000;
    public static final boolean DISABLE_V2 = SystemProperties.get("persist.sys.rlock.disable", "false").equals("true");
    public static final int EVENT_NETWORK_LOCK_LOCKED = 5002;
    public static final int EVENT_NETWORK_LOCK_STATUS = 5000;
    public static final int EVENT_UPDATE_POWER_RADIO = 5001;
    public static final boolean IS_REGION_LOCK = (!OemConstant.RM_VERSION && !SystemProperties.get(NETLOCK_VERSION, "NULL").equals("NULL"));
    public static final String NETLOCK_FLAG_OFF = "0";
    public static final String NETLOCK_FLAG_ON = "1";
    public static final String NETLOCK_STATUS = "netlockstatus";
    public static final String NETLOCK_VERSION = "ro.oppo.region.netlock";
    public static final String NOTIFY_NETLOCK_FLAG = "ril.oppo.lock.flag";
    public static final String NOTIFY_NETWORK_OFF = "0";
    public static final String NOTIFY_NETWORK_ON = "1";
    public static final String PERSIST_LOCK_TIME = "persist.oppo.nw.locktime";
    public static final String PERSIST_NETLOCK = "persist.oppo.nw.region.lock";
    public static final String PERSIST_NETLOCK_RUS_CONFIG = "persist.sys.netlock.rus.config";
    public static final String PERSIST_TEST_OP = "persist.oppo.nw.test.operator";
    public static final String TEST_OP = SystemProperties.get(PERSIST_TEST_OP, TEST_OP_DEFAULT);
    public static final String TEST_OP_CMCC = "1";
    public static final String TEST_OP_CU = "0";
    public static final String TEST_OP_CUANDCMCC = "2";
    public static final String TEST_OP_DEFAULT = "4";
    public static final String UNLOCK_NORMAL = "1";
    public static final String UNLOCK_TYPE = "unlocktype";
    public static final boolean UP_TO_V2 = SystemProperties.get("ro.product.oppo.india.new.fch", "false").equals("true");
    public static final String VERSION = SystemProperties.get(NETLOCK_VERSION, "NA");
}

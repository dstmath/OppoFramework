package com.android.internal.telephony;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SpnOverride;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OemDeviceLock {
    public static final String ACTION_NO_SERVICE_LOCKED = "oppo.action.NO_SERVICE_LOCKED";
    public static final int ALL = 2;
    public static final String BIND_STATUS = "gsm.sim.oppo.bind.locked";
    public static final boolean DBG = OemConstant.SWITCH_LOG;
    private static final int DEFAULT = 4;
    public static final int DEFAULT_TIMES = 604800000;
    public static final int EVENT_DEVICE_LOCK_NOTIFY = 1;
    public static final int EVENT_DEVICE_LOCK_STATUS = 0;
    public static final int INVAILD = -1;
    public static boolean IS_OP_LOCK = (SystemProperties.get(LOCK_OPERATOR, "OPPO").equals("OPPO") ^ 1);
    public static final String LOCKED_SLOT = "gsm.sim.oppo.slot.locked";
    public static final String LOCK_OFF = "0";
    public static final String LOCK_ON = "1";
    public static final String LOCK_OPERATOR = "ro.oppo.operator.device.lock";
    public static final String LOCK_STATUS = "gsm.sim.oppo.device.locked";
    public static final String OOS_LOCK = "gsm.sim.oppo.oos.force.locked";
    public static final String OPPO_DEVICE_LOCKED_OPERATOR = "oppo_device_locked_operator";
    public static final String OPPO_SHOW_DEVICE_LOCKED = "oppo_show_device_locked";
    public static final String OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT = "oppo_show_device_locked_forbid_slot";
    private static final int OP_CMCC = 0;
    private static final int OP_CU = 1;
    private static String[][] OP_EICCID = null;
    private static String[][] OP_EPLMN = null;
    public static String OP_LOCK = SystemProperties.get(LOCK_OPERATOR, "OPPO");
    public static final String[] OP_SUPPORT = new String[]{"AIS", "TRUE", "DTAC", "CMCC", "CU"};
    public static final String PERSIST_LOCK = "persist.sys.oppo.device.lock";
    public static final String PERSIST_LOCK_TIME = "persist.sys.oppo.locktime";
    public static final String SIM_LOADED = "gsm.sim.oppo.sim.loaded";
    public static final int SLOT0 = 0;
    public static final int SLOT1 = 1;
    public static final String TAG = "OemDeviceLock";
    public static final String TEST_MODE = "persist.sys.test.operator";
    public static final boolean VDBG = "true".equals(SystemProperties.get("persist.sys.oem.devicelock", "false"));
    public static final boolean VDBG2 = "true".equals(SystemProperties.get("persist.sys.oem.devicelock.ui", "true"));
    private static int currentForbidSlot = -1;
    private static int currentLock = 0;
    private static int currentNotBindSlot = -1;
    private static int lastForbidSlot = -1;
    private static int lastLock = 0;
    private static int lastNotBindSlot = -1;
    private static ConnectivityManager mCm = null;
    private static Context mContext = null;
    protected static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (OemDeviceLock.VDBG) {
                        Toast.makeText(OemDeviceLock.mContext, "locked sim card has no service 7 days", 0).show();
                    }
                    OemDeviceLock.oosForceUpdateDeviceLocked(msg.arg1, true);
                    return;
                case 1:
                    Toast.makeText(OemDeviceLock.mContext, "update device lock status show forbid slot " + msg.arg1, 1).show();
                    return;
                default:
                    return;
            }
        }
    };
    private static PendingIntent mResetIntent = null;
    private static int operatorIndex = 0;
    private static ArrayList<OperatorEntry> sAllowSimIccidList = new ArrayList();
    public static final int sAllowSimIndex = 1;
    private static ArrayList<OperatorEntry> sAllowSimPlmnList = new ArrayList();
    public static final String sGid1Value = "01ff";
    public static final int sHasInitSuccess = 3;
    public static final int sHasLockedIndex = 2;
    public static final int sHasinitIndex = 0;
    private static OemDeviceLock sInstance = null;
    private static Phone[] sPhone = new Phone[2];
    private static boolean[] sSimInsert = new boolean[2];
    private BroadcastReceiver mLockReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OemDeviceLock.ACTION_NO_SERVICE_LOCKED)) {
                Message msg = OemDeviceLock.mHandler.obtainMessage(0);
                msg.arg1 = intent.getIntExtra("phoneId", 0);
                OemDeviceLock.mHandler.sendMessage(msg);
            }
        }
    };

    public static class OperatorEntry {
        public String ICCID = "iccid";
        public String OPERATOR = IccProvider.STR_OP;
        public String PLMN = "plmn";
        private String mIccid;
        private String mOperator;
        private String mPlmn;

        public OperatorEntry(String operator, String plmn, String iccid) {
            this.mOperator = operator;
            this.mPlmn = plmn;
            this.mIccid = iccid;
        }

        public String getOperator() {
            return this.mOperator;
        }

        public String getPlmn() {
            return this.mPlmn;
        }

        public String getIccid() {
            return this.mIccid;
        }

        public String toString() {
            return "\n" + this.OPERATOR + "=" + getOperator() + ", " + this.PLMN + "=" + getPlmn() + ", " + this.ICCID + "=" + getIccid();
        }
    }

    static {
        r0 = new String[5][];
        r0[0] = new String[]{"52001", "52003", "52023"};
        r0[1] = new String[]{"52000", "52004", "52099"};
        r0[2] = new String[]{"52018", "52005", "52047"};
        r0[3] = new String[]{"46000", "46002", "46004", "46007", "46008"};
        r0[4] = new String[]{"46001", "46006", "46009"};
        OP_EPLMN = r0;
        r0 = new String[5][];
        r0[0] = new String[]{"896601", "896603", "896623"};
        r0[1] = new String[]{"896600", "896604", "896699"};
        r0[2] = new String[]{"896618", "896605", "896647"};
        r0[3] = new String[]{"898600", "898602", "898604", "898607", "898608"};
        r0[4] = new String[]{"898601", "898606", "898609"};
        OP_EICCID = r0;
    }

    public static OemDeviceLock getInstance(Context context) {
        if (sInstance == null) {
            Rlog.d(TAG, "sInstance run");
            sInstance = new OemDeviceLock(context);
        }
        return sInstance;
    }

    public OemDeviceLock(Context context) {
        mContext = context;
        mCm = (ConnectivityManager) mContext.getSystemService("connectivity");
        OemDeviceLockUtils.init(mContext);
        initLoadDeviceAllStatus();
        setAllowSimPlmn();
        setAllowSimIccid();
        sPhone[0] = PhoneFactory.getPhone(0);
        sPhone[1] = PhoneFactory.getPhone(1);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NO_SERVICE_LOCKED);
        context.registerReceiver(this.mLockReceiver, filter);
    }

    private static void initLoadDeviceAllStatus() {
        if (OemDeviceLockUtils.getLockedStatusFlag(mContext)) {
            OemDeviceLockUtils.setLockedStatusFlag(mContext, "1");
            OemDeviceLockUtils.setNeedSyncLockedTime(mContext);
            setSimBindedStatus();
        } else {
            OemDeviceLockUtils.setLockedStatusFlag(mContext, "0");
        }
        setDeviceLockedSlot(-1);
        Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED, 0);
        Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, 0);
        Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT, -1);
        getCurrentLockOperator(OP_LOCK);
    }

    private static String getCurrentLockOperator(String operator) {
        if ("AIS".equals(operator)) {
            operatorIndex = 1;
        } else if ("TRUE".equals(operator)) {
            operatorIndex = 2;
        } else if ("DTAC".equals(operator)) {
            operatorIndex = 3;
        }
        if (SystemProperties.getInt(TEST_MODE, 4) == 0) {
            return "CMCC";
        }
        if (SystemProperties.getInt(TEST_MODE, 4) == 1) {
            return "CU";
        }
        return operator;
    }

    public static boolean isAllowSimCheck(int slotId, String value, boolean checkGid1OrSpn) {
        return isAllowSimIccid(slotId, value, checkGid1OrSpn);
    }

    public static boolean isAllowSimPlmn(String plmn) {
        String operator = getCurrentLockOperator(OP_LOCK);
        if (!(sAllowSimPlmnList == null || (sAllowSimPlmnList.isEmpty() ^ 1) == 0)) {
            for (OperatorEntry entry : sAllowSimPlmnList) {
                if (entry.getOperator().equals(operator) && !TextUtils.isEmpty(plmn) && plmn.equals(entry.getPlmn())) {
                    if (VDBG) {
                        Rlog.d(TAG, "isAllowSimPlmn,operator=" + operator + ",plmn==" + plmn);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static void setAllowSimPlmn() {
        if (!(sAllowSimPlmnList == null || (sAllowSimPlmnList.isEmpty() ^ 1) == 0)) {
            sAllowSimPlmnList.clear();
        }
        if (OP_EPLMN.length == OP_SUPPORT.length) {
            for (int i = 0; i < OP_SUPPORT.length; i++) {
                for (int j = 0; j < OP_EPLMN[i].length; j++) {
                    sAllowSimPlmnList.add(new OperatorEntry(OP_SUPPORT[i], OP_EPLMN[i][j], SpnOverride.MVNO_TYPE_NONE));
                    if (VDBG) {
                        Rlog.d(TAG, "setAllowSimPlmn" + new OperatorEntry(OP_SUPPORT[i], OP_EPLMN[i][j], SpnOverride.MVNO_TYPE_NONE));
                    }
                }
            }
        }
    }

    public static boolean isAllowSimIccid(int slotId, String iccid, boolean checkGid1OrSpn) {
        String operator = getCurrentLockOperator(OP_LOCK);
        if (!(sAllowSimIccidList == null || (sAllowSimIccidList.isEmpty() ^ 1) == 0)) {
            for (OperatorEntry entry : sAllowSimIccidList) {
                if (entry.getOperator().equals(operator) && !TextUtils.isEmpty(iccid) && iccid.equals(entry.getIccid())) {
                    if (VDBG) {
                        Rlog.d(TAG, "isAllowSimIccid,operator=" + operator + ",iccid==" + entry.getIccid());
                    }
                    if ("TRUE".equals(operator) && checkGid1OrSpn) {
                        return needCheckGid1OrSpn(slotId);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean needCheckGid1OrSpn(int slotId) {
        if (sPhone == null || sPhone[slotId] == null) {
            return true;
        }
        String gid1 = " ";
        gid1 = sPhone[slotId].getGroupIdLevel1();
        Rlog.d(TAG, "needCheckGid1OrSpn: get sim GID1 is " + gid1);
        if (sGid1Value.equalsIgnoreCase(gid1)) {
            return true;
        }
        IccRecords records = sPhone[slotId].getIccCard().getIccRecords();
        if (records == null) {
            Rlog.d(TAG, "needCheckGid1OrSpn: records null");
            return true;
        }
        String spn = records != null ? records.getServiceProviderName() : SpnOverride.MVNO_TYPE_NONE;
        Rlog.d(TAG, "needCheckGid1OrSpn: get sim spn is " + spn);
        if ("TRUE-H".equalsIgnoreCase(spn)) {
            return true;
        }
        return false;
    }

    private static void setAllowSimIccid() {
        if (!(sAllowSimIccidList == null || (sAllowSimIccidList.isEmpty() ^ 1) == 0)) {
            sAllowSimIccidList.clear();
        }
        if (OP_EICCID.length == OP_SUPPORT.length) {
            for (int i = 0; i < OP_SUPPORT.length; i++) {
                for (int j = 0; j < OP_EICCID[i].length; j++) {
                    sAllowSimIccidList.add(new OperatorEntry(OP_SUPPORT[i], SpnOverride.MVNO_TYPE_NONE, OP_EICCID[i][j]));
                    if (VDBG) {
                        Rlog.d(TAG, "setAllowSimIccid" + new OperatorEntry(OP_SUPPORT[i], SpnOverride.MVNO_TYPE_NONE, OP_EICCID[i][j]));
                    }
                }
            }
        }
    }

    public static boolean[] initOperatorDeviceLock(String simCode, String values, int slot, boolean checkGid1orSpn) {
        boolean[] lockState = new boolean[4];
        String defaultValue = OemDeviceLockUtils.INIT_ICCID;
        String lockedValue = OemDeviceLockUtils.getLockedIccid(mContext);
        if (TextUtils.isEmpty(lockedValue) || (defaultValue.equals(lockedValue) ^ 1) == 0) {
            lockState[0] = false;
            lockState[3] = false;
        } else {
            lockState[0] = true;
        }
        if (isAllowSimCheck(slot, simCode, checkGid1orSpn)) {
            lockState[1] = true;
            if (DBG) {
                Rlog.d(TAG, "initOperatorDeviceLock lock[0] = " + lockState[0] + "lock[" + 1 + "] = " + lockState[1] + "lock[" + 2 + "] = " + lockState[2] + "lock[" + 3 + "] = " + lockState[3]);
            }
            if (!lockState[0] && lockState[1]) {
                lockState[3] = OemDeviceLockUtils.setSimIccid(mContext, values);
                Rlog.d(TAG, "record first lock imsi or iccid = " + values + ",operator = " + simCode);
                updateLockedTime(true, Long.valueOf(SystemProperties.get("gsm.nitz.time", OemDeviceLockUtils.INIT_TIME_TYPE2)).longValue());
                if (!lockState[3]) {
                    Rlog.d(TAG, "failed to init locked simcard");
                    return lockState;
                }
            }
            lockedValue = OemDeviceLockUtils.getLockedIccid(mContext);
            if (TextUtils.isEmpty(lockedValue) || !lockedValue.startsWith(values)) {
                lockState[2] = false;
                lockState[3] = false;
            } else {
                lockState[0] = true;
                lockState[2] = true;
            }
            if (DBG) {
                Rlog.d(TAG, "oppoIsLockOperator simCode,= " + simCode + "imsi or iccid = " + values + ",slot = " + slot);
                Rlog.d(TAG, "initOperatorDeviceLock lock[0] = " + lockState[0] + "lock[" + 1 + "] = " + lockState[1] + "lock[" + 2 + "] = " + lockState[2] + "lock[" + 3 + "] = " + lockState[3]);
            }
            return lockState;
        }
        if (DBG) {
            Rlog.d(TAG, "device insert not allow sim card operator" + simCode);
        }
        lockState[1] = false;
        return lockState;
    }

    public static boolean isSimBindingCompleted() {
        String defaultValue = OemDeviceLockUtils.INIT_ICCID;
        String lockedValue = OemDeviceLockUtils.getLockedIccid(mContext);
        if (TextUtils.isEmpty(lockedValue) || (defaultValue.equals(lockedValue) ^ 1) == 0) {
            TelephonyManager.getDefault();
            TelephonyManager.setTelephonyProperty(0, BIND_STATUS, "false");
            return false;
        }
        Rlog.d(TAG, "device has binded sim card,iccid value is  " + lockedValue);
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(0, BIND_STATUS, "true");
        return true;
    }

    private static void setSimBindedStatus() {
        isSimBindingCompleted();
    }

    public static boolean getSimBindedStatus() {
        TelephonyManager.getDefault();
        if ("true".equals(TelephonyManager.getTelephonyProperty(0, BIND_STATUS, "false"))) {
            return true;
        }
        return false;
    }

    public static boolean[] isNeedAllowedOperator(String simCode, String value, int slotId, boolean checkGidOrSpn) {
        boolean[] lockState = new boolean[4];
        String defaultValue = OemDeviceLockUtils.INIT_ICCID;
        String lockedValue = OemDeviceLockUtils.getLockedIccid(mContext);
        if (TextUtils.isEmpty(lockedValue) || (defaultValue.equals(lockedValue) ^ 1) == 0) {
            lockState[0] = false;
        } else {
            lockState[0] = true;
        }
        if (isAllowSimCheck(slotId, simCode, checkGidOrSpn)) {
            lockState[1] = true;
            if (TextUtils.isEmpty(lockedValue) || !lockedValue.startsWith(value)) {
                lockState[2] = false;
                lockState[3] = false;
            } else {
                lockState[0] = true;
                lockState[2] = true;
            }
            if (DBG) {
                Rlog.d(TAG, "isNeedAllowedOperator simCode,= " + simCode + "imsi or iccid = " + value + ",slotId = " + slotId);
                Rlog.d(TAG, "isNeedAllowedOperator lock[0] = " + lockState[0] + "lock[" + 1 + "] = " + lockState[1] + "lock[" + 2 + "] = " + lockState[2] + "lock[" + 3 + "] = " + lockState[3]);
            }
            return lockState;
        }
        if (DBG) {
            Rlog.d(TAG, "device insert not allow sim card operator" + simCode);
        }
        lockState[1] = false;
        return lockState;
    }

    public static boolean getDeviceLockedForPhone(int slotId) {
        TelephonyManager.getDefault();
        if ("true".equals(TelephonyManager.getTelephonyProperty(slotId, LOCK_STATUS, "false"))) {
            return true;
        }
        TelephonyManager.getDefault();
        if ("true".equals(TelephonyManager.getTelephonyProperty(slotId, OOS_LOCK, "false"))) {
            return true;
        }
        return false;
    }

    public static void setDeviceLockedForPhone(int slotId, boolean lockState) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(slotId, LOCK_STATUS, SpnOverride.MVNO_TYPE_NONE + lockState);
    }

    public static void oosForceSetDeviceLocked(int slotId, boolean lockState) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(slotId, OOS_LOCK, SpnOverride.MVNO_TYPE_NONE + lockState);
    }

    public static void setDeviceLockedSlot(int slotId) {
        SystemProperties.set(LOCKED_SLOT, SpnOverride.MVNO_TYPE_NONE + slotId);
    }

    public static int getDeviceLockedSlot() {
        return SystemProperties.getInt(LOCKED_SLOT, -1);
    }

    public static boolean getSimLoaded(int slotId) {
        TelephonyManager.getDefault();
        if ("true".equals(TelephonyManager.getTelephonyProperty(slotId, SIM_LOADED, "false"))) {
            return true;
        }
        return false;
    }

    public static void setSimLoadedForPhone(boolean loaded) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(0, SIM_LOADED, SpnOverride.MVNO_TYPE_NONE + loaded);
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(1, SIM_LOADED, SpnOverride.MVNO_TYPE_NONE + loaded);
    }

    public static void updateLockedTime() {
        if (mCm == null) {
            mCm = (ConnectivityManager) mContext.getSystemService("connectivity");
        }
        if (mCm != null) {
            long timeMillis = mCm.getCurrentTimeMillis();
            if (DBG) {
                Rlog.d(TAG, "updateLockedTime,timeMillis = " + timeMillis + ",date = " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(timeMillis)));
            }
            if (Long.MAX_VALUE != timeMillis) {
                updateLockedTime(false, timeMillis);
            }
        }
    }

    public static void updateLockedTime(boolean forceUpdate, long ms) {
        if (forceUpdate) {
            OemDeviceLockUtils.setDeviceLockedTimes(mContext, String.valueOf(ms), 1);
            if (DBG) {
                Rlog.d(TAG, "force updateLockedTime set last bind time was " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
            }
            if (OemDeviceLockUtils.INIT_TIME_TYPE1 == OemDeviceLockUtils.getDeviceLockedTimes(mContext, 0)) {
                OemDeviceLockUtils.setDeviceLockedTimes(mContext, String.valueOf(ms), 0);
                if (DBG) {
                    Rlog.d(TAG, "force updateLockedTime set first bind time was " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
                }
            }
            calculateLockDays(ms);
        } else if (getSimBindedStatus()) {
            if (OemDeviceLockUtils.INIT_TIME_TYPE1 == OemDeviceLockUtils.getDeviceLockedTimes(mContext, 1)) {
                OemDeviceLockUtils.setDeviceLockedTimes(mContext, String.valueOf(ms), 1);
                if (DBG) {
                    Rlog.d(TAG, "updateLockedTime set last bind time was " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
                }
                if (OemDeviceLockUtils.INIT_TIME_TYPE1 == OemDeviceLockUtils.getDeviceLockedTimes(mContext, 0)) {
                    OemDeviceLockUtils.setDeviceLockedTimes(mContext, String.valueOf(ms), 0);
                    if (DBG) {
                        Rlog.d(TAG, "updateLockedTime set first bind time was " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
                    }
                }
            } else {
                calculateLockDays(ms);
            }
        }
        if (DBG) {
            Rlog.d(TAG, "updateLockedTime ms = " + ms + "forceUpdate =" + forceUpdate);
            Rlog.d(TAG, "updateLockedTime " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
        }
    }

    private static void calculateLockDays(long time) {
        if (OemDeviceLockUtils.getAutoUnLockStatus(mContext, time)) {
            clearAllDeviceLockState();
        }
    }

    private static void clearAllDeviceLockState() {
        setDeviceLockedForPhone(0, false);
        setDeviceLockedForPhone(1, false);
        oosForceSetDeviceLocked(0, false);
        oosForceSetDeviceLocked(1, false);
        OemDeviceLockUtils.setLockedStatusFlag(mContext, "0");
        Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED, 0);
        Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, 0);
        Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT, -1);
    }

    public static boolean getDeviceLockStatus() {
        return OemDeviceLockUtils.getLockedStatusFlag(mContext);
    }

    public static void updateServiceStatusTime(int phoneId, boolean change) {
        if (-1 != getDeviceLockedSlot() && phoneId == getDeviceLockedSlot()) {
            if (change) {
                startResetNetworkStatusAlarm(phoneId);
            } else {
                cancelNetworkStatusAlarm(phoneId);
            }
        }
    }

    private static void startResetNetworkStatusAlarm(int phoneId) {
        int delayInMs = SystemProperties.getInt(PERSIST_LOCK_TIME, DEFAULT_TIMES);
        Rlog.d(TAG, "lock timer, delayInMs = " + delayInMs + "phoneId==" + phoneId);
        Intent intent = new Intent(ACTION_NO_SERVICE_LOCKED);
        AlarmManager alarm = (AlarmManager) mContext.getSystemService("alarm");
        intent.putExtra("phoneId", phoneId);
        mResetIntent = PendingIntent.getBroadcast(mContext, phoneId, intent, 134217728);
        alarm.setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), mResetIntent);
    }

    private static void cancelNetworkStatusAlarm(int phoneId) {
        AlarmManager alarm = (AlarmManager) mContext.getSystemService("alarm");
        if (mResetIntent != null) {
            alarm.cancel(mResetIntent);
            oosForceUpdateDeviceLocked(phoneId, false);
            mResetIntent = null;
        }
    }

    private static void oosForceUpdateDeviceLocked(int phoneId, boolean lock) {
        oosForceSetDeviceLocked(0, lock);
        oosForceSetDeviceLocked(1, lock);
        notifyDeviceLocked(lock);
        updateServiceState(sSimInsert);
    }

    public static void notifyDeviceLocked(boolean forceupdate) {
        int lockState;
        Intent intent = new Intent("oppo.intent.action.DEVICE_LOCK_STATUS");
        if (getDeviceLockedForPhone(0) && getDeviceLockedForPhone(1)) {
            intent.putExtra("locked", "true");
            lockState = 1;
        } else {
            intent.putExtra("locked", "false");
            lockState = 0;
        }
        if (forceupdate) {
            intent.putExtra("locked", "true");
            lockState = 1;
        }
        currentLock = lockState;
        Rlog.d(TAG, "notifyDeviceLocked: lastLock == " + lastLock + ",currentLock==" + currentLock);
        if (!isSimBindingCompleted()) {
            notifyNotBindingAllowSim();
        } else if (lastLock != currentLock) {
            if (VDBG2) {
                Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED, currentLock);
                sendBroadCastChangedNetlockStatus(intent);
                Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, operatorIndex);
            }
            if (VDBG) {
                Rlog.d(TAG, "notifyDeviceLocked: update device lock status show locked UI");
            }
            lastLock = currentLock;
        }
        notifyHasNotAllowOperator();
    }

    private static void notifyNotBindingAllowSim() {
        if (getSimInsertForPhone(0)) {
            currentNotBindSlot = 0;
        } else if (getSimInsertForPhone(1)) {
            currentNotBindSlot = 1;
        }
        Rlog.d(TAG, "notifyNotBindingAllowSim: lastNotBindSlot == " + lastNotBindSlot + ",currentNotBindSlot==" + currentNotBindSlot);
        if (lastNotBindSlot != currentNotBindSlot) {
            Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT, currentNotBindSlot);
            Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, operatorIndex);
            Intent forbidIntent = new Intent("oppo.intent.action.DEVICE_LOCK_HINT");
            forbidIntent.putExtra("forbid_slot", currentNotBindSlot);
            sendBroadCastChangedNetlockStatus(forbidIntent);
            lastNotBindSlot = currentNotBindSlot;
        }
    }

    private static void notifyHasNotAllowOperator() {
        if (getDeviceLockedForPhone(0) && !getDeviceLockedForPhone(1)) {
            currentForbidSlot = 0;
        }
        if (getDeviceLockedForPhone(1) && !getDeviceLockedForPhone(0)) {
            currentForbidSlot = 1;
        }
        Rlog.d(TAG, "notifyDeviceLocked: lastForbidSlot == " + lastForbidSlot + ",currentForbidSlot==" + currentForbidSlot);
        if (lastForbidSlot != currentForbidSlot && getSimInsertForPhone(currentForbidSlot)) {
            Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT, currentForbidSlot);
            Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, operatorIndex);
            Intent forbidIntent = new Intent("oppo.intent.action.DEVICE_LOCK_HINT");
            forbidIntent.putExtra("forbid_slot", currentForbidSlot);
            sendBroadCastChangedNetlockStatus(forbidIntent);
            if (VDBG) {
                Message msg = mHandler.obtainMessage(1);
                msg.arg1 = currentForbidSlot;
                mHandler.sendMessage(msg);
            }
            lastForbidSlot = currentForbidSlot;
        }
    }

    public static void notifyUpdateDataCapacity(Phone[] phone, boolean[] insert) {
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (insert[slot] && getDeviceLockedForPhone(slot) && phone != null && phone[slot] != null && phone[slot].getDataEnabled() && phone[slot].getSubId() == SubscriptionManager.getDefaultDataSubId()) {
                phone[slot].setDataEnabled(false);
                Rlog.d(TAG, "notifyUpdateDataCapacity close the data");
            }
            slot++;
        }
        if (DBG) {
            Rlog.d(TAG, "notifyUpdateDataCapacity end");
        }
    }

    public static void updateServiceState(boolean[] insert) {
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (!(!insert[slot] || sPhone == null || sPhone[slot] == null)) {
                sPhone[slot].getServiceStateTracker().pollState();
                Rlog.d(TAG, "updateServiceState success");
            }
            slot++;
        }
        Rlog.d(TAG, "updateServiceState end");
    }

    public static boolean isNeedShowOutService(int phoneId) {
        return getSimInsertForPhone(phoneId) ? getDeviceLockedForPhone(phoneId) : false;
    }

    public static void setSimInsertForPhone(boolean[] insert) {
        sSimInsert[0] = insert[0];
        sSimInsert[1] = insert[1];
    }

    public static boolean getSimInsertForPhone(int phoneId) {
        return sSimInsert[phoneId];
    }

    private static void sendBroadCastChangedNetlockStatus(Intent intent) {
        mContext.sendBroadcast(intent);
    }
}

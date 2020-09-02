package com.oppo.internal.telephony.explock;

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
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.uicc.IccRecords;
import com.oppo.internal.telephony.explock.util.THRpmbUtil;
import com.oppo.internal.telephony.utils.ConnectivityManagerHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class OemDeviceLock {
    public static final String ACTION_NO_SERVICE_LOCKED = "oppo.action.NO_SERVICE_LOCKED";
    public static final int ALL = 2;
    public static final String BIND_STATUS = "gsm.sim.oppo.bind.locked";
    public static final boolean DBG = OemConstant.SWITCH_LOG;
    private static final int DEFAULT = 4;
    public static final int DEFAULT_TIMES = 604800000;
    public static final String DEVICE_LOCK_RPMB = "persist.sys.rpmb.device.lock";
    public static final int EVENT_DEVICE_LOCK_NOTIFY = 1;
    public static final int EVENT_DEVICE_LOCK_STATUS = 0;
    public static final int INVAILD = -1;
    public static boolean IS_TH_LOCK = (!SystemProperties.get(LOCK_OPERATOR, "OPPO").equals("OPPO"));
    public static final String LOCKED_SLOT = "gsm.sim.oppo.slot.locked";
    public static final String LOCK_OFF = "0";
    public static final String LOCK_ON = "1";
    public static final String LOCK_OPERATOR = "ro.oppo.operator.device.lock";
    public static final String LOCK_STATUS = "gsm.sim.oppo.device.locked";
    public static final String LOCK_UPGRADE_PRJ = "persist.sys.device.lock.prj";
    public static final String OOS_LOCK = "gsm.sim.oppo.oos.force.locked";
    public static final String OPPO_DEVICE_LOCKED_OPERATOR = "oppo_device_locked_operator";
    public static final String OPPO_SHOW_DEVICE_LOCKED = "oppo_show_device_locked";
    public static final String OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT = "oppo_show_device_locked_forbid_slot";
    private static final int OP_CMCC = 0;
    private static final int OP_CU = 1;
    private static String[][] OP_EICCID = {new String[]{"896601", "896603", "896623"}, new String[]{"896600", "896604", "896699"}, new String[]{"896618", "896605", "896647"}, new String[]{"898600", "898602", "898604", "898607", "898608"}, new String[]{"898601", "898606", "898609"}};
    private static String[][] OP_EPLMN = {new String[]{"52001", "52003", "52023"}, new String[]{"52000", "52004", "52099"}, new String[]{"52018", "52005", "52047"}, new String[]{"46000", "46002", "46004", "46007", "46008"}, new String[]{"46001", "46006", "46009"}};
    public static String OP_LOCK = SystemProperties.get(LOCK_OPERATOR, "OPPO");
    public static final String[] OP_SUPPORT = {"AIS", "TRUE", "DTAC", "CMCC", "CU"};
    public static final String PERSIST_LOCK = "persist.sys.oppo.device.lock";
    public static final String PERSIST_LOCK_TIME = "persist.sys.oppo.locktime";
    public static final String PKG_NAME_LOCK_UI = "com.coloros.simsettings";
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
    /* access modifiers changed from: private */
    public static Context mContext = null;
    protected static Handler mHandler = new Handler() {
        /* class com.oppo.internal.telephony.explock.OemDeviceLock.AnonymousClass2 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                if (OemDeviceLock.VDBG && OemDeviceLock.mContext != null) {
                    Toast.makeText(OemDeviceLock.mContext, "locked sim card has no service 7 days", 0).show();
                }
                OemDeviceLock.oosForceUpdateDeviceLocked(msg.arg1, true);
            } else if (i == 1 && OemDeviceLock.mContext != null) {
                Context access$100 = OemDeviceLock.mContext;
                Toast.makeText(access$100, "update device lock status show forbid slot " + msg.arg1, 1).show();
            }
        }
    };
    private static PendingIntent mResetIntent = null;
    private static int operatorIndex = 0;
    private static ArrayList<OperatorEntry> sAllowSimIccidList = new ArrayList<>();
    public static final int sAllowSimIndex = 1;
    private static ArrayList<OperatorEntry> sAllowSimPlmnList = new ArrayList<>();
    private static final int sCheckCountMax = 2;
    private static int sCheckOperatorCount = 0;
    private static int sCheckOperatorNameCount = 0;
    private static String sCurrentLockOperator = null;
    public static final String sGid1Value = "01ff";
    private static boolean sHasCheckLockOperator = false;
    public static final int sHasInitSuccess = 3;
    public static final int sHasLockedIndex = 2;
    private static boolean sHasOperator = false;
    public static final int sHasinitIndex = 0;
    private static OemDeviceLock sInstance = null;
    private static boolean sIsLockOperator = false;
    private static Phone[] sPhone = new Phone[2];
    private static boolean[] sSimInsert = new boolean[2];
    /* access modifiers changed from: private */
    public static boolean sStopRpmb = false;
    private BroadcastReceiver mLockReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.explock.OemDeviceLock.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OemDeviceLock.ACTION_NO_SERVICE_LOCKED)) {
                Message msg = OemDeviceLock.mHandler.obtainMessage(0);
                msg.arg1 = intent.getIntExtra("phoneId", 0);
                OemDeviceLock.mHandler.sendMessage(msg);
            } else if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN")) {
                boolean unused = OemDeviceLock.sStopRpmb = true;
                Rlog.d(OemDeviceLock.TAG, "sStopRpmb = " + OemDeviceLock.sStopRpmb + ",action " + intent.getAction());
            }
        }
    };

    public static boolean isCancelReadRpmb() {
        return sStopRpmb;
    }

    public static OemDeviceLock getInstance(Context context) {
        if (sInstance == null) {
            Rlog.d(TAG, "sInstance run");
            sInstance = new OemDeviceLock(context);
            if (isOperatorLock()) {
                try {
                    THRpmbUtil.checkMoveDataToRpmb();
                } catch (Exception e) {
                }
            }
        }
        return sInstance;
    }

    public OemDeviceLock(Context context) {
        mContext = context;
        Context context2 = mContext;
        if (context2 != null) {
            mCm = (ConnectivityManager) context2.getSystemService("connectivity");
            initLoadDeviceAllStatus();
            if (isOperatorLock()) {
                OemLockUtils.init(mContext);
                setAllowSimPlmn();
                setAllowSimIccid();
                sPhone[0] = PhoneFactory.getPhone(0);
                sPhone[1] = PhoneFactory.getPhone(1);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NO_SERVICE_LOCKED);
                filter.addAction("android.intent.action.ACTION_SHUTDOWN");
                mContext.registerReceiver(this.mLockReceiver, filter);
            }
        }
    }

    public static String getOperatorVersion() {
        if (sHasOperator && !TextUtils.isEmpty(sCurrentLockOperator)) {
            return sCurrentLockOperator;
        }
        String operator = null;
        THRpmbUtil.checkMoveDataToRpmb();
        if (THRpmbUtil.hasMovedDataToRpmb()) {
            operator = THRpmbUtil.getRpmbOperatorData();
        }
        if (operator != null) {
            sCurrentLockOperator = operator;
            sHasOperator = true;
            SystemProperties.set(DEVICE_LOCK_RPMB, operator);
        } else {
            sCurrentLockOperator = OP_LOCK;
            if (!sHasOperator) {
                sCheckOperatorNameCount++;
                if (sCheckOperatorNameCount >= 2) {
                    sHasOperator = true;
                }
            } else {
                sCheckOperatorNameCount = 0;
            }
            SystemProperties.set(DEVICE_LOCK_RPMB, "NA");
        }
        return sCurrentLockOperator;
    }

    public static boolean isOperatorLock() {
        if (!OemLockUtils.isLockUpgradeProject()) {
            return false;
        }
        if (sHasCheckLockOperator) {
            return sIsLockOperator;
        }
        String rpmbOperator = null;
        THRpmbUtil.checkMoveDataToRpmb();
        if (THRpmbUtil.hasMovedDataToRpmb()) {
            rpmbOperator = THRpmbUtil.getRpmbOperatorData();
        }
        if (rpmbOperator != null) {
            sIsLockOperator = true;
            sHasCheckLockOperator = true;
            SystemProperties.set(DEVICE_LOCK_RPMB, rpmbOperator);
        } else {
            if (!SystemProperties.get(LOCK_OPERATOR, "OPPO").equals("OPPO")) {
                sIsLockOperator = true;
                sHasCheckLockOperator = true;
            } else {
                sIsLockOperator = false;
            }
            SystemProperties.set(DEVICE_LOCK_RPMB, "NA");
        }
        if (!sHasCheckLockOperator) {
            sCheckOperatorCount++;
            if (sCheckOperatorCount >= 2) {
                sHasCheckLockOperator = true;
            }
        } else {
            sCheckOperatorCount = 0;
        }
        return sIsLockOperator;
    }

    private static void initLoadDeviceAllStatus() {
        if (OemLockUtils.getLockedStatusFlag()) {
            OemLockUtils.setLockedStatusFlag(mContext, "1");
            OemLockUtils.setNeedSyncLockedTime(mContext);
            setSimBindedStatus();
        } else {
            OemLockUtils.setLockedStatusFlag(mContext, "0");
        }
        setDeviceLockedSlot(-1);
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED, 0);
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, 0);
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT, -1);
        getCurrentLockOperator(getOperatorVersion());
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
        String operator = getCurrentLockOperator(getOperatorVersion());
        ArrayList<OperatorEntry> arrayList = sAllowSimPlmnList;
        if (arrayList == null || arrayList.isEmpty()) {
            return false;
        }
        Iterator<OperatorEntry> it = sAllowSimPlmnList.iterator();
        while (it.hasNext()) {
            OperatorEntry entry = it.next();
            if (entry.getOperator().equals(operator) && !TextUtils.isEmpty(plmn) && plmn.equals(entry.getPlmn())) {
                if (!VDBG) {
                    return true;
                }
                Rlog.d(TAG, "isAllowSimPlmn,operator=" + operator + ",plmn==" + plmn);
                return true;
            }
        }
        return false;
    }

    private static void setAllowSimPlmn() {
        ArrayList<OperatorEntry> arrayList = sAllowSimPlmnList;
        if (arrayList != null && !arrayList.isEmpty()) {
            sAllowSimPlmnList.clear();
        }
        if (OP_EPLMN.length == OP_SUPPORT.length && sAllowSimPlmnList != null) {
            for (int i = 0; i < OP_SUPPORT.length; i++) {
                int j = 0;
                while (true) {
                    String[][] strArr = OP_EPLMN;
                    if (j >= strArr[i].length) {
                        break;
                    }
                    sAllowSimPlmnList.add(new OperatorEntry(OP_SUPPORT[i], strArr[i][j], ""));
                    if (VDBG) {
                        Rlog.d(TAG, "setAllowSimPlmn" + new OperatorEntry(OP_SUPPORT[i], OP_EPLMN[i][j], ""));
                    }
                    j++;
                }
            }
        }
    }

    public static boolean isAllowSimIccid(int slotId, String iccid, boolean checkGid1OrSpn) {
        String operator = getCurrentLockOperator(getOperatorVersion());
        ArrayList<OperatorEntry> arrayList = sAllowSimIccidList;
        if (arrayList == null || arrayList.isEmpty()) {
            return false;
        }
        Iterator<OperatorEntry> it = sAllowSimIccidList.iterator();
        while (it.hasNext()) {
            OperatorEntry entry = it.next();
            if (entry.getOperator().equals(operator) && !TextUtils.isEmpty(iccid) && iccid.equals(entry.getIccid())) {
                if (VDBG) {
                    Rlog.d(TAG, "isAllowSimIccid,operator=" + operator + ",iccid==" + entry.getIccid());
                }
                if (!"TRUE".equals(operator) || !checkGid1OrSpn) {
                    return true;
                }
                return needCheckGid1OrSpn(slotId);
            }
        }
        return false;
    }

    private static boolean needCheckGid1OrSpn(int slotId) {
        IccRecords records;
        Phone[] phoneArr = sPhone;
        if (phoneArr == null || phoneArr[slotId] == null || sGid1Value.equalsIgnoreCase(phoneArr[slotId].getGroupIdLevel1()) || (records = sPhone[slotId].getIccCard().getIccRecords()) == null) {
            return true;
        }
        String spn = records.getServiceProviderName();
        if (VDBG) {
            Rlog.d(TAG, "needCheckGid1OrSpn: get sim spn is " + spn);
        }
        if ("TRUE-H".equalsIgnoreCase(spn)) {
            return true;
        }
        return false;
    }

    private static void setAllowSimIccid() {
        ArrayList<OperatorEntry> arrayList = sAllowSimIccidList;
        if (arrayList != null && !arrayList.isEmpty()) {
            sAllowSimIccidList.clear();
        }
        if (OP_EICCID.length == OP_SUPPORT.length && sAllowSimIccidList != null) {
            for (int i = 0; i < OP_SUPPORT.length; i++) {
                int j = 0;
                while (true) {
                    String[][] strArr = OP_EICCID;
                    if (j >= strArr[i].length) {
                        break;
                    }
                    sAllowSimIccidList.add(new OperatorEntry(OP_SUPPORT[i], "", strArr[i][j]));
                    if (VDBG) {
                        Rlog.d(TAG, "setAllowSimIccid" + new OperatorEntry(OP_SUPPORT[i], "", OP_EICCID[i][j]));
                    }
                    j++;
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0114  */
    public static boolean[] initOperatorDeviceLock(String simCode, String values, int slot, boolean checkGid1orSpn) {
        char c;
        boolean[] lockState = new boolean[4];
        String lockedValue = OemLockUtils.getLockedIccid(mContext);
        if (TextUtils.isEmpty(lockedValue) || OemLockUtils.INIT_ICCID.equals(lockedValue)) {
            lockState[0] = false;
            lockState[3] = false;
        } else {
            lockState[0] = true;
        }
        if (!isAllowSimCheck(slot, simCode, checkGid1orSpn)) {
            if (DBG) {
                Rlog.d(TAG, "device insert not allow sim card operator" + simCode);
            }
            lockState[1] = false;
            return lockState;
        }
        lockState[1] = true;
        if (VDBG) {
            Rlog.d(TAG, "initOperatorDeviceLock lock[0] = " + lockState[0] + "lock[" + 1 + "] = " + lockState[1] + "lock[" + 2 + "] = " + lockState[2] + "lock[" + 3 + "] = " + lockState[3]);
        }
        if (!lockState[0] && lockState[1]) {
            lockState[3] = OemLockUtils.setSimIccid(mContext, values);
            Rlog.d(TAG, "record first lock imsi or iccid = " + values + ",operator = " + simCode);
            updateLockedTime(true, Long.valueOf(SystemProperties.get("gsm.nitz.time", "000000000000000")).longValue());
            if (!lockState[3]) {
                Rlog.d(TAG, "failed to init locked simcard");
                return lockState;
            }
        }
        String lockedValue2 = OemLockUtils.getLockedIccid(mContext);
        if (TextUtils.isEmpty(lockedValue2)) {
            c = 2;
        } else if (lockedValue2.startsWith(values) || lockedValue2.equalsIgnoreCase(values)) {
            lockState[0] = true;
            lockState[2] = true;
            if (DBG) {
                Rlog.d(TAG, "oppoIsLockOperator simCode,= " + simCode + "imsi or iccid = " + values + ",slot = " + slot);
                Rlog.d(TAG, "initOperatorDeviceLock lock[0] = " + lockState[0] + "lock[" + 1 + "] = " + lockState[1] + "lock[" + 2 + "] = " + lockState[2] + "lock[" + 3 + "] = " + lockState[3]);
            }
            return lockState;
        } else {
            c = 2;
        }
        lockState[c] = false;
        lockState[3] = false;
        if (DBG) {
        }
        return lockState;
    }

    public static boolean isSimBindingCompleted() {
        String lockedValue = OemLockUtils.getLockedIccid(mContext);
        if (TextUtils.isEmpty(lockedValue) || OemLockUtils.INIT_ICCID.equals(lockedValue)) {
            TelephonyManager.getDefault();
            TelephonyManager.setTelephonyProperty(0, BIND_STATUS, "false");
            return false;
        }
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
        String lockedValue = OemLockUtils.getLockedIccid(mContext);
        if (TextUtils.isEmpty(lockedValue) || OemLockUtils.INIT_ICCID.equals(lockedValue)) {
            lockState[0] = false;
        } else {
            lockState[0] = true;
        }
        if (!isAllowSimCheck(slotId, simCode, checkGidOrSpn)) {
            if (VDBG) {
                Rlog.d(TAG, "device insert not allow sim card operator" + simCode);
            }
            lockState[1] = false;
            return lockState;
        }
        lockState[1] = true;
        if (TextUtils.isEmpty(lockedValue) || (!lockedValue.startsWith(value) && !lockedValue.equalsIgnoreCase(value))) {
            lockState[2] = false;
            lockState[3] = false;
        } else {
            lockState[0] = true;
            lockState[2] = true;
        }
        if (VDBG) {
            Rlog.d(TAG, "isNeedAllowedOperator simCode,= " + simCode + "imsi or iccid = " + value + ",slotId = " + slotId);
            Rlog.d(TAG, "isNeedAllowedOperator lock[0] = " + lockState[0] + "lock[" + 1 + "] = " + lockState[1] + "lock[" + 2 + "] = " + lockState[2] + "lock[" + 3 + "] = " + lockState[3]);
        }
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
        TelephonyManager.setTelephonyProperty(slotId, LOCK_STATUS, "" + lockState);
    }

    public static void oosForceSetDeviceLocked(int slotId, boolean lockState) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(slotId, OOS_LOCK, "" + lockState);
    }

    public static void setDeviceLockedSlot(int slotId) {
        SystemProperties.set(LOCKED_SLOT, "" + slotId);
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
        TelephonyManager.setTelephonyProperty(0, SIM_LOADED, "" + loaded);
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(1, SIM_LOADED, "" + loaded);
    }

    public static void updateLockedTime() {
        Context context;
        if (mCm == null && (context = mContext) != null) {
            mCm = (ConnectivityManager) context.getSystemService("connectivity");
        }
        ConnectivityManager connectivityManager = mCm;
        if (connectivityManager != null) {
            long timeMillis = ConnectivityManagerHelper.getCurrentTimeMillis(connectivityManager);
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
            OemLockUtils.setDeviceLockedTimes(mContext, String.valueOf(ms), 1);
            if (DBG) {
                Rlog.d(TAG, "force updateLockedTime set last bind time was " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
            }
            if (0 == OemLockUtils.getDeviceLockedTimes(mContext, 0)) {
                OemLockUtils.setDeviceLockedTimes(mContext, String.valueOf(ms), 0);
                if (DBG) {
                    Rlog.d(TAG, "force updateLockedTime set first bind time was " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
                }
            }
            calculateLockDays(ms);
        } else if (getSimBindedStatus()) {
            if (0 == OemLockUtils.getDeviceLockedTimes(mContext, 1)) {
                OemLockUtils.setDeviceLockedTimes(mContext, String.valueOf(ms), 1);
                if (DBG) {
                    Rlog.d(TAG, "updateLockedTime set last bind time was " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
                }
                if (0 == OemLockUtils.getDeviceLockedTimes(mContext, 0)) {
                    OemLockUtils.setDeviceLockedTimes(mContext, String.valueOf(ms), 0);
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
            StringBuilder sb = new StringBuilder();
            sb.append("updateLockedTime ");
            sb.append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ms)));
            Rlog.d(TAG, sb.toString());
        }
    }

    private static void calculateLockDays(long time) {
        if (OemLockUtils.getAutoUnLockStatus(mContext, time)) {
            clearAllDeviceLockState();
        }
    }

    private static void clearAllDeviceLockState() {
        if (mContext == null) {
            Rlog.d(TAG, "mContext is null and return");
            return;
        }
        setDeviceLockedForPhone(0, false);
        setDeviceLockedForPhone(1, false);
        oosForceSetDeviceLocked(0, false);
        oosForceSetDeviceLocked(1, false);
        OemLockUtils.setLockedStatusFlag(mContext, "0");
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED, 0);
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, 0);
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT, -1);
        notifyDeviceLocked(false);
    }

    public static boolean getDeviceLockStatus() {
        return OemLockUtils.getLockedStatusFlag();
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
        if (mContext == null) {
            Rlog.d(TAG, "mContext is null and return");
            return;
        }
        int delayInMs = SystemProperties.getInt(PERSIST_LOCK_TIME, (int) DEFAULT_TIMES);
        if (DBG) {
            Rlog.d(TAG, "lock timer, delayInMs = " + delayInMs + "phoneId==" + phoneId);
        }
        Intent intent = new Intent(ACTION_NO_SERVICE_LOCKED);
        intent.putExtra("phoneId", phoneId);
        mResetIntent = PendingIntent.getBroadcast(mContext, phoneId, intent, 134217728);
        ((AlarmManager) mContext.getSystemService("alarm")).setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), mResetIntent);
    }

    private static void cancelNetworkStatusAlarm(int phoneId) {
        Context context = mContext;
        if (context == null) {
            Rlog.d(TAG, "mContext is null and return");
            return;
        }
        AlarmManager alarm = (AlarmManager) context.getSystemService("alarm");
        PendingIntent pendingIntent = mResetIntent;
        if (pendingIntent != null) {
            alarm.cancel(pendingIntent);
            mResetIntent = null;
            oosForceUpdateDeviceLocked(phoneId, false);
        }
    }

    /* access modifiers changed from: private */
    public static void oosForceUpdateDeviceLocked(int phoneId, boolean lock) {
        oosForceSetDeviceLocked(0, lock);
        oosForceSetDeviceLocked(1, lock);
        notifyDeviceLocked(lock);
        updateServiceState(sSimInsert);
    }

    public static void notifyDeviceLocked(boolean forceupdate) {
        int lockState;
        if (!getDeviceLockedForPhone(0) || !getDeviceLockedForPhone(1)) {
            lockState = 0;
        } else {
            lockState = 1;
        }
        if (forceupdate) {
            lockState = 1;
        }
        currentLock = lockState;
        if (DBG) {
            Rlog.d(TAG, "notifyDeviceLocked: lastLock == " + lastLock + ",currentLock==" + currentLock);
        }
        if (isSimBindingCompleted()) {
            int i = lastLock;
            int i2 = currentLock;
            if (i != i2) {
                if (VDBG2) {
                    notifyShowLockedActivity(i2);
                }
                if (VDBG) {
                    Rlog.d(TAG, "notifyDeviceLocked: update device lock status show locked UI");
                }
                lastLock = currentLock;
            }
        } else {
            notifyNotBindingAllowSim();
        }
        notifyHasNotAllowOperator();
    }

    private static void notifyNotBindingAllowSim() {
        if (getSimInsertForPhone(0)) {
            currentNotBindSlot = 0;
        } else if (getSimInsertForPhone(1)) {
            currentNotBindSlot = 1;
        }
        if (DBG) {
            Rlog.d(TAG, "notifyNotBindingAllowSim: lastNotBindSlot == " + lastNotBindSlot + ",currentNotBindSlot==" + currentNotBindSlot);
        }
        int i = lastNotBindSlot;
        int i2 = currentNotBindSlot;
        if (i != i2) {
            notifyShowLockedAlertMessage(i2);
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
        if (DBG) {
            Rlog.d(TAG, "notifyDeviceLocked: lastForbidSlot == " + lastForbidSlot + ",currentForbidSlot==" + currentForbidSlot);
        }
        int i = lastForbidSlot;
        int i2 = currentForbidSlot;
        if (i != i2 && getSimInsertForPhone(i2)) {
            notifyShowLockedAlertMessage(currentForbidSlot);
            if (VDBG) {
                Message msg = mHandler.obtainMessage(1);
                msg.arg1 = currentForbidSlot;
                mHandler.sendMessage(msg);
            }
            lastForbidSlot = currentForbidSlot;
        }
    }

    private static void notifyShowLockedActivity(int lockState) {
        if (mContext == null) {
            Rlog.d(TAG, "mContext is null and return");
            return;
        }
        Intent intent = new Intent("oppo.intent.action.DEVICE_LOCK_STATUS");
        if (lockState == 1) {
            intent.putExtra("locked", "true");
        } else {
            intent.putExtra("locked", "false");
        }
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED, lockState);
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, operatorIndex);
        sendBroadCastChangedNetlockStatus(intent);
    }

    private static void notifyShowLockedAlertMessage(int currentForbidSlot2) {
        if (mContext == null) {
            Rlog.d(TAG, "mContext is null and return");
            return;
        }
        Intent forbidIntent = new Intent("oppo.intent.action.DEVICE_LOCK_HINT");
        forbidIntent.putExtra("forbid_slot", currentForbidSlot2);
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_SHOW_DEVICE_LOCKED_FORBID_SLOT, currentForbidSlot2);
        Settings.Global.putInt(mContext.getContentResolver(), OPPO_DEVICE_LOCKED_OPERATOR, operatorIndex);
        sendBroadCastChangedNetlockStatus(forbidIntent);
    }

    public static void notifyUpdateDataCapacity(boolean[] insert) {
        Phone phone;
        for (int slot = 0; slot < TelephonyManager.getDefault().getPhoneCount(); slot++) {
            if (insert[slot] && getDeviceLockedForPhone(slot) && (phone = PhoneFactory.getPhone(slot)) != null && phone.isUserDataEnabled() && phone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
                phone.getDataEnabledSettings().setUserDataEnabled(false);
                Rlog.d(TAG, "notifyUpdateDataCapacity close the data");
            }
        }
        if (DBG) {
            Rlog.d(TAG, "notifyUpdateDataCapacity end");
        }
    }

    public static void updateServiceState(boolean[] insert) {
        Phone[] phoneArr;
        for (int slot = 0; slot < TelephonyManager.getDefault().getPhoneCount(); slot++) {
            if (!(!insert[slot] || (phoneArr = sPhone) == null || phoneArr[slot] == null)) {
                phoneArr[slot].getServiceStateTracker().pollState();
                Rlog.d(TAG, "updateServiceState success");
            }
        }
    }

    public static boolean isNeedShowOutService(int phoneId) {
        return getSimInsertForPhone(phoneId) && getDeviceLockedForPhone(phoneId);
    }

    public static void setSimInsertForPhone(boolean[] insert) {
        boolean[] zArr = sSimInsert;
        zArr[0] = insert[0];
        zArr[1] = insert[1];
    }

    public static boolean getSimInsertForPhone(int phoneId) {
        return sSimInsert[phoneId];
    }

    private static void sendBroadCastChangedNetlockStatus(Intent intent) {
        if (mContext != null) {
            intent.setPackage(PKG_NAME_LOCK_UI);
            mContext.sendBroadcast(intent);
        }
    }

    public static boolean isLockOperatorRpmb() {
        String operator = THRpmbUtil.getRpmbOperatorData();
        if (TextUtils.isEmpty(operator)) {
            return false;
        }
        if ("AIS".equals(operator) || "TRUE".equals(operator) || "DTAC".equals(operator)) {
            return true;
        }
        return false;
    }

    public static boolean getRpmbLockStatusOff() {
        String lockStatus = THRpmbUtil.getRpmbLockStatus();
        if (TextUtils.isEmpty(lockStatus) || !"0".equals(lockStatus)) {
            return false;
        }
        return true;
    }

    public static class OperatorEntry {
        public String ICCID = "iccid";
        public String OPERATOR = "operator";
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
}

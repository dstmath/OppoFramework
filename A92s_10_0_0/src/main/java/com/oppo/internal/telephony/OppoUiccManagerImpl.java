package com.oppo.internal.telephony;

import android.app.ActivityThread;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractSubscriptionController;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IOppoUiccManager;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoSimlockManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.CatLog;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.dataconnection.AbstractDcTracker;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.explock.OemDeviceLock;
import com.oppo.internal.telephony.explock.OemLockUtils;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import com.oppo.internal.telephony.explock.RegionLockPlmnListParser;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import com.oppo.internal.telephony.operatorswitch.ExpOperatorSwitchUtils;
import com.oppo.internal.telephony.utils.HypnusServiceHelper;
import java.lang.reflect.Method;
import java.util.Arrays;

public class OppoUiccManagerImpl implements IOppoUiccManager {
    private static final String ACTION_CARD_TYPE_CHANGED = "oppo.intent.action.SUBINFO_STATE_CHANGE";
    private static final boolean DBG = true;
    private static final int EVENT_SIM_READ_DELAY = 2;
    private static final int EVENT_WRITE_MSISDN_DONE = 1;
    private static final String INTENT_KEY_CARD_TYPE = "typeValue";
    private static final String INTENT_KEY_SIM_STATE = "simstate";
    private static final String INTENT_KEY_SLOT_ID = "slotid";
    private static final String INTENT_KEY_SUB_ID = "subid";
    private static final String INTENT_VALUE_SIM_STATE = "CARDTYPE";
    private static final String LOG_TAG = "OppoUiccManagerImpl";
    private static String PROPERTY_VSIM_SLOT_VALUE = "gsm.vsim.slotid";
    private static String URI_RED_TEA_PROVIDER_VALUE = "content://com.redteamobile.roaming.provider";
    private static boolean sHasInSertLockSim = false;
    private static OppoUiccManagerImpl sInstance = null;
    private final String ICCID_STRING_FOR_NO_SIM = "N/A";
    protected final uiccManagerHandler mBaseHandler;
    private Context mContext;
    private HandlerThread mHandlerThread = new HandlerThread("uiccManagerHandler");
    protected final Object mLock = new Object();
    private OemDeviceLock mOemLock = null;
    public OppoSimlockManager mOppoSimlockManager = null;
    private int mPhoneNum = 0;

    protected class uiccManagerHandler extends Handler {
        public uiccManagerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean z = OppoUiccManagerImpl.DBG;
            if (i == 1) {
                AsyncResult ar = (AsyncResult) msg.obj;
                synchronized (OppoUiccManagerImpl.this.mLock) {
                    boolean result = false;
                    updateMsisdnToSim msisdn = null;
                    if (ar != null) {
                        if (ar.exception != null) {
                            z = false;
                        }
                        result = z;
                        msisdn = (updateMsisdnToSim) ar.userObj;
                    } else {
                        OppoUiccManagerImpl.logd("EVENT_WRITE_MSISDN_DONE, ar = null");
                    }
                    if (msisdn != null) {
                        msisdn.setResult(result);
                    } else {
                        OppoUiccManagerImpl.logd("EVENT_WRITE_MSISDN_DONE, msisdn = null");
                    }
                    OppoUiccManagerImpl.log("EVENT_WRITE_MSISDN_DONE, mSuccess = " + result);
                    OppoUiccManagerImpl.this.mLock.notifyAll();
                }
            } else if (i != 2) {
                OppoUiccManagerImpl.logd("wrong message: " + msg.what);
            } else if (ExpOperatorSwitchUtils.isSupportOperatorSwitch()) {
                OppoUiccManagerImpl.logd("conf slot = " + msg.arg1);
                OppoUiccManagerImpl.this.setOperatorConf((String[]) msg.obj);
            }
        }
    }

    private OppoUiccManagerImpl() {
        this.mHandlerThread.start();
        this.mBaseHandler = new uiccManagerHandler(this.mHandlerThread.getLooper());
        this.mContext = ActivityThread.currentApplication().getApplicationContext();
        this.mPhoneNum = TelephonyManager.getDefault().getPhoneCount();
        if (OemConstant.EXP_VERSION && OemLockUtils.isDeviceLockVersion()) {
            this.mOemLock = OemDeviceLock.getInstance(this.mContext);
        }
        if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
            ExpOperatorSwitchUtils.init(this.mContext);
        }
    }

    public static OppoUiccManagerImpl getInstance() {
        OppoUiccManagerImpl oppoUiccManagerImpl;
        OppoUiccManagerImpl oppoUiccManagerImpl2 = sInstance;
        if (oppoUiccManagerImpl2 != null) {
            return oppoUiccManagerImpl2;
        }
        synchronized (OppoUiccManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new OppoUiccManagerImpl();
            }
            oppoUiccManagerImpl = sInstance;
        }
        return oppoUiccManagerImpl;
    }

    public void updateSubscriptionInfoByIccIdExt(Context context, String[] iccid) {
        this.mContext = context;
        enableHypnusAction();
        if (ExpOperatorSwitchUtils.isSupportOperatorSwitch()) {
            setOperatorConf(iccid);
        }
        if (OemConstant.EXP_VERSION && this.mOemLock != null && OemDeviceLock.isOperatorLock() && OemDeviceLock.getDeviceLockStatus()) {
            OemDeviceLock oemDeviceLock = this.mOemLock;
            if (OemDeviceLock.isSimBindingCompleted()) {
                updateDeviceLockUI(iccid);
                sHasInSertLockSim = hasInsertBindingSimCard(iccid);
                updateOperatorDeviceLockStatus(iccid, DBG, false);
            }
        }
    }

    public void updateSimReadyExt(int slotId) {
        updateRegionLockPlmnList();
    }

    public void updateSimLoadedExt(int slotId, String[] iccid) {
        DcTracker mDcTracker;
        Phone phone = PhoneFactory.getPhone(slotId);
        if (!(phone == null || (mDcTracker = phone.getDcTracker(1)) == null)) {
            ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, mDcTracker)).setDataRoamingEnabledForOperator(slotId);
        }
        if (OemConstant.EXP_VERSION && this.mOemLock != null && OemDeviceLock.isOperatorLock() && OemDeviceLock.getDeviceLockStatus()) {
            if (allSimActived()) {
                updateOperatorDeviceLock(iccid, DBG, DBG);
            } else {
                updateOperatorDeviceLock(iccid, false, DBG);
            }
        }
    }

    public void updateRegionLockPlmnList() {
        if (OemConstant.EXP_VERSION && !OemConstant.RM_VERSION && "true".equals(SystemProperties.get(RegionLockConstant.PERSIST_NETLOCK_RUS_CONFIG, "false"))) {
            RegionLockPlmnListParser.getInstance(this.mContext).updateRegionLockPlmnList();
            logd("update regionlock plmn again");
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0095, code lost:
        r4 = ((com.android.internal.telephony.uicc.AbstractUiccController) com.android.internal.telephony.util.OemTelephonyUtils.typeCasting(com.android.internal.telephony.uicc.AbstractUiccController.class, com.android.internal.telephony.uicc.UiccController.getInstance())).getSimHotSwapPlugInState();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a5, code lost:
        if (r0 != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a7, code lost:
        if (r1 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a9, code lost:
        if (r4 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ab, code lost:
        com.oppo.internal.telephony.operatorswitch.ExpOperatorSwitchUtils.oppoBroadCastDelayHotswap();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        return;
     */
    public void setOperatorConf(String[] iccid) {
        String operator;
        if (!ExpOperatorSwitchUtils.isFirstInsertSim()) {
            logd("setOperatorConf, not first insert simcard!!");
            return;
        }
        boolean isSpecOperator = false;
        boolean isInsertSim = false;
        int slot = 0;
        while (true) {
            if (slot >= this.mPhoneNum) {
                break;
            }
            if (iccid[slot] != null && !iccid[slot].equals("N/A")) {
                Phone phone = PhoneFactory.getPhone(slot);
                IccRecords records = null;
                String spn = null;
                if (phone != null) {
                    records = phone.getIccCard().getIccRecords();
                }
                if (records == null || (operator = records.getOperatorNumeric()) == null) {
                    uiccManagerHandler uiccmanagerhandler = this.mBaseHandler;
                    uiccmanagerhandler.sendMessageDelayed(uiccmanagerhandler.obtainMessage(2, slot, -1, iccid), 3000);
                } else {
                    if (needCheckGid1OrSpn()) {
                        if (!isGid1OrSpnReady(records)) {
                            uiccManagerHandler uiccmanagerhandler2 = this.mBaseHandler;
                            uiccmanagerhandler2.sendMessageDelayed(uiccmanagerhandler2.obtainMessage(2, slot, -1, iccid), 3000);
                            return;
                        }
                        spn = records.getServiceProviderName();
                    }
                    ExpOperatorSwitchUtils.setFirstInsertSimFlag(1);
                    isInsertSim = DBG;
                    logd("setOperatorConf, slot = " + slot + " operator=" + operator);
                    if (ExpOperatorSwitchUtils.oppoIsSpecOperator(operator, slot, spn, needCheckGid1OrSpn(), records)) {
                        isSpecOperator = DBG;
                        break;
                    }
                }
            }
            slot++;
        }
        uiccManagerHandler uiccmanagerhandler3 = this.mBaseHandler;
        uiccmanagerhandler3.sendMessageDelayed(uiccmanagerhandler3.obtainMessage(2, slot, -1, iccid), 3000);
    }

    private boolean needCheckGid1OrSpn() {
        return false;
    }

    private boolean isGid1OrSpnReady(IccRecords records) {
        if (records != null && records.isLoaded()) {
            return DBG;
        }
        SIMRecords sIMRecords = (SIMRecords) records;
        if (!TextUtils.isEmpty(records != null ? records.getServiceProviderName() : "")) {
            return DBG;
        }
        return false;
    }

    private void updateDeviceLockUI(String[] iccid) {
        boolean[] isSimInsert = new boolean[2];
        for (int slot = 0; slot < this.mPhoneNum; slot++) {
            if (iccid[slot] == null || iccid[slot].equals("N/A")) {
                isSimInsert[slot] = false;
            } else {
                isSimInsert[slot] = DBG;
            }
        }
        OemDeviceLock oemDeviceLock = this.mOemLock;
        if (!isSimInsert[0] && !isSimInsert[1]) {
            OemDeviceLock.setDeviceLockedForPhone(0, DBG);
            OemDeviceLock oemDeviceLock2 = this.mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(1, DBG);
            OemDeviceLock oemDeviceLock3 = this.mOemLock;
            OemDeviceLock.setSimLoadedForPhone(DBG);
            OemDeviceLock oemDeviceLock4 = this.mOemLock;
            OemDeviceLock.setSimInsertForPhone(isSimInsert);
            OemDeviceLock oemDeviceLock5 = this.mOemLock;
            OemDeviceLock.notifyDeviceLocked(false);
        }
    }

    private boolean hasInsertBindingSimCard(String[] iccid) {
        boolean[] lock = new boolean[4];
        for (int slot = 0; slot < this.mPhoneNum; slot++) {
            if (iccid[slot] != null && !iccid[slot].equals("N/A")) {
                if (iccid[slot].length() >= 15) {
                    String simOperator = iccid[slot].substring(0, 6);
                    OemDeviceLock oemDeviceLock = this.mOemLock;
                    lock = OemDeviceLock.isNeedAllowedOperator(simOperator, iccid[slot], slot, false);
                }
                if (lock[0] && lock[1] && lock[2]) {
                    logd("has insert locked sim card");
                    OemDeviceLock oemDeviceLock2 = this.mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    OemDeviceLock oemDeviceLock3 = this.mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                    return DBG;
                }
            }
        }
        return false;
    }

    private void updateOperatorDeviceLock(String[] iccid, boolean simAllActived, boolean checkGid1OrSpn) {
        boolean[] zArr = new boolean[2];
        OemDeviceLock oemDeviceLock = this.mOemLock;
        if (!OemDeviceLock.isSimBindingCompleted()) {
            firstBindingDeviceLock(iccid, checkGid1OrSpn);
        }
        updateOperatorDeviceLockStatus(iccid, simAllActived, checkGid1OrSpn);
    }

    private void firstBindingDeviceLock(String[] iccid, boolean checkGid1OrSpn) {
        boolean[] lock = new boolean[4];
        String simOperator = null;
        for (int slot = 0; slot < this.mPhoneNum; slot++) {
            if (iccid[slot] != null && !iccid[slot].equals("N/A")) {
                if (iccid[slot].length() >= 15) {
                    simOperator = iccid[slot].substring(0, 6);
                    OemDeviceLock oemDeviceLock = this.mOemLock;
                    lock = OemDeviceLock.initOperatorDeviceLock(simOperator, iccid[slot], slot, DBG);
                }
                OemDeviceLock oemDeviceLock2 = this.mOemLock;
                boolean success = lock[0] && lock[1] && lock[2];
                logd("firstBindingDeviceLock,success" + success + ",slotId = " + slot + ",simOperator = " + simOperator + ",mIccId[" + slot + "] = " + iccid[slot]);
                if (success) {
                    OemDeviceLock oemDeviceLock3 = this.mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    OemDeviceLock oemDeviceLock4 = this.mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                    OemDeviceLock oemDeviceLock5 = this.mOemLock;
                    if (lock[3]) {
                        logd("has binding operator success");
                        return;
                    }
                } else {
                    OemDeviceLock oemDeviceLock6 = this.mOemLock;
                    if (!lock[0] && lock[1] && !lock[3]) {
                        logd("firstBindingDeviceLock,first init locked fail");
                    }
                }
            }
        }
    }

    private void updateOperatorDeviceLockStatus(String[] iccid, boolean allSimActived, boolean checkGid1OrSpn) {
        boolean[] lock = new boolean[4];
        boolean[] isSimInsert = new boolean[2];
        boolean insertLockedSim = false;
        int currentSlot = -1;
        for (int slot = 0; slot < this.mPhoneNum; slot++) {
            if (iccid[slot] != null && !iccid[slot].equals("N/A")) {
                String simOperator = null;
                if (iccid[slot].length() >= 15) {
                    simOperator = iccid[slot].substring(0, 6);
                    OemDeviceLock oemDeviceLock = this.mOemLock;
                    lock = OemDeviceLock.isNeedAllowedOperator(simOperator, iccid[slot], slot, checkGid1OrSpn);
                }
                isSimInsert[slot] = DBG;
                logd("updateOperatorDeviceLock,slot = " + slot + ",lock[0] = " + lock[0] + ",lock[1]  = " + lock[1] + ",lock[2] = " + lock[2] + ",simOperator = " + simOperator);
                if (lock[0] && lock[1] && lock[2]) {
                    logd("has insert locked sim card");
                    insertLockedSim = DBG;
                    currentSlot = slot;
                    OemDeviceLock oemDeviceLock2 = this.mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    OemDeviceLock oemDeviceLock3 = this.mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                }
            }
        }
        if (!allSimActived && !insertLockedSim) {
            insertLockedSim = sHasInSertLockSim;
            OemDeviceLock oemDeviceLock4 = this.mOemLock;
            currentSlot = OemDeviceLock.getDeviceLockedSlot();
        }
        OemDeviceLock oemDeviceLock5 = this.mOemLock;
        int anotherSlot = currentSlot == 0 ? 1 : 0;
        if (insertLockedSim) {
            OemDeviceLock oemDeviceLock6 = this.mOemLock;
            if (currentSlot != -1) {
                if (iccid[anotherSlot] == null || iccid[anotherSlot].equals("N/A")) {
                    isSimInsert[anotherSlot] = false;
                    OemDeviceLock oemDeviceLock7 = this.mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(anotherSlot, DBG);
                    OemDeviceLock oemDeviceLock8 = this.mOemLock;
                    OemDeviceLock.setSimLoadedForPhone(DBG);
                    OemDeviceLock oemDeviceLock9 = this.mOemLock;
                    OemDeviceLock.setSimInsertForPhone(isSimInsert);
                    OemDeviceLock oemDeviceLock10 = this.mOemLock;
                    OemDeviceLock.notifyDeviceLocked(false);
                    OemDeviceLock oemDeviceLock11 = this.mOemLock;
                    OemDeviceLock.notifyUpdateDataCapacity(isSimInsert);
                    OemDeviceLock oemDeviceLock12 = this.mOemLock;
                    OemDeviceLock.updateServiceState(isSimInsert);
                }
                isSimInsert[anotherSlot] = DBG;
                if (iccid[anotherSlot].length() >= 15) {
                    OemDeviceLock oemDeviceLock13 = this.mOemLock;
                    if (OemDeviceLock.isAllowSimCheck(anotherSlot, iccid[anotherSlot].substring(0, 6), DBG)) {
                        OemDeviceLock oemDeviceLock14 = this.mOemLock;
                        OemDeviceLock.setDeviceLockedForPhone(anotherSlot, false);
                    } else {
                        OemDeviceLock oemDeviceLock15 = this.mOemLock;
                        OemDeviceLock.setDeviceLockedForPhone(anotherSlot, DBG);
                    }
                } else {
                    OemDeviceLock oemDeviceLock16 = this.mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(anotherSlot, DBG);
                }
                OemDeviceLock oemDeviceLock82 = this.mOemLock;
                OemDeviceLock.setSimLoadedForPhone(DBG);
                OemDeviceLock oemDeviceLock92 = this.mOemLock;
                OemDeviceLock.setSimInsertForPhone(isSimInsert);
                OemDeviceLock oemDeviceLock102 = this.mOemLock;
                OemDeviceLock.notifyDeviceLocked(false);
                OemDeviceLock oemDeviceLock112 = this.mOemLock;
                OemDeviceLock.notifyUpdateDataCapacity(isSimInsert);
                OemDeviceLock oemDeviceLock122 = this.mOemLock;
                OemDeviceLock.updateServiceState(isSimInsert);
            }
        }
        OemDeviceLock oemDeviceLock17 = this.mOemLock;
        boolean[] lock2 = OemDeviceLock.isNeedAllowedOperator("", "", 0, false);
        OemDeviceLock oemDeviceLock18 = this.mOemLock;
        if (lock2[0]) {
            logd("updateOperatorDeviceLock, has init locked,but not insert simcard");
        } else {
            logd("updateOperatorDeviceLock has not init locked");
        }
        OemDeviceLock oemDeviceLock19 = this.mOemLock;
        OemDeviceLock.setDeviceLockedForPhone(0, DBG);
        OemDeviceLock oemDeviceLock20 = this.mOemLock;
        OemDeviceLock.setDeviceLockedForPhone(1, DBG);
        OemDeviceLock oemDeviceLock822 = this.mOemLock;
        OemDeviceLock.setSimLoadedForPhone(DBG);
        OemDeviceLock oemDeviceLock922 = this.mOemLock;
        OemDeviceLock.setSimInsertForPhone(isSimInsert);
        OemDeviceLock oemDeviceLock1022 = this.mOemLock;
        OemDeviceLock.notifyDeviceLocked(false);
        OemDeviceLock oemDeviceLock1122 = this.mOemLock;
        OemDeviceLock.notifyUpdateDataCapacity(isSimInsert);
        OemDeviceLock oemDeviceLock1222 = this.mOemLock;
        OemDeviceLock.updateServiceState(isSimInsert);
    }

    private boolean allSimActived() {
        if (currentInsertSimCount() > SubscriptionController.getInstance().getActiveSubInfoCount(getClass().getPackage().getName())) {
            return false;
        }
        return DBG;
    }

    private int currentInsertSimCount() {
        int slot = 0;
        while (slot < this.mPhoneNum) {
            Phone phone = PhoneFactory.getPhone(slot);
            slot = (phone == null || phone.getIccCard().hasIccCard()) ? slot + 1 : slot + 1;
        }
        return 0;
    }

    public boolean isHasSoftSimCard() {
        if (getSoftSimCardSlotId() >= 0) {
            return DBG;
        }
        return false;
    }

    public int getSoftSimCardSlotId() {
        return SystemProperties.getInt(PROPERTY_VSIM_SLOT_VALUE, -1);
    }

    public void checkSoftSimCard(Context context) {
        if (getSoftSimCardSlotId() != -1 && TextUtils.isEmpty(getSoftSimIccid(context))) {
            logd("checkSoftSimCard clear");
            SystemProperties.set(PROPERTY_VSIM_SLOT_VALUE, "-1");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0063 A[SYNTHETIC, Splitter:B:17:0x0063] */
    private String getSoftSimIccid(Context context) {
        StringBuilder sb;
        String[] columns = {"slot", "iccid"};
        Cursor cursor = null;
        try {
            Cursor cursor2 = context.getContentResolver().query(Uri.parse(URI_RED_TEA_PROVIDER_VALUE), columns, null, null, null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    try {
                        cursor2.close();
                    } catch (Exception e) {
                        ex = e;
                        sb = new StringBuilder();
                    }
                }
                return null;
            }
            do {
                cursor2.getString(cursor2.getColumnIndex("slot"));
                String iccid = cursor2.getString(cursor2.getColumnIndex("iccid"));
                if (!TextUtils.isEmpty(iccid)) {
                    try {
                        cursor2.close();
                    } catch (Exception ex) {
                        log("execption happen when close cursor:" + ex);
                    }
                    return iccid;
                }
            } while (cursor2.moveToNext());
            if (cursor2 != null) {
            }
            return null;
            sb.append("execption happen when close cursor:");
            sb.append(ex);
            log(sb.toString());
            return null;
        } catch (Exception e2) {
            log("execption happen when query softSim:" + e2);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e3) {
                    ex = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex2) {
                    log("execption happen when close cursor:" + ex2);
                }
            }
            throw th;
        }
    }

    public boolean isSoftSimSubId(int subId) {
        int softSimSlotId;
        SubscriptionController subc;
        int[] subIds;
        boolean result = false;
        if (isHasSoftSimCard() && (softSimSlotId = getSoftSimCardSlotId()) > -1 && (subc = SubscriptionController.getInstance()) != null && (subIds = subc.getSubId(softSimSlotId)) != null) {
            logd("[isSoftSimSubId]- vsim slotId: " + softSimSlotId + "vsim subId: " + subIds[0]);
            if (subIds.length > 0 && subIds[0] == subId) {
                logd("[isSoftSimSubId]- it is vsim");
                result = DBG;
            }
        }
        logd("[isSoftSimSubId]- result:" + result);
        return result;
    }

    public void enableHypnusAction() {
        Object obj = ReflectionHelper.getDeclaredField((Object) null, "com.oppo.hypnus.Hypnus", "ACTION_IO");
        int ACTION_IO = obj != null ? ((Integer) obj).intValue() : 12;
        Object obj2 = ReflectionHelper.getDeclaredField((Object) null, "com.oppo.hypnus.Hypnus", "TIME_LAUNCH");
        enableHypnusAction(ACTION_IO, obj2 != null ? ((Integer) obj2).intValue() : 2000);
    }

    public void enableHypnusAction(int action, int timeout) {
        HypnusServiceHelper.hypnusServiceInit();
        HypnusServiceHelper.hypnusSetAction(action, timeout);
    }

    public boolean isUiccSlotForbid(int slotId) {
        return "1".equals(TelephonyManager.getTelephonyProperty(slotId, "persist.sys.oem_forbid_slots", "0"));
    }

    public String getCarrierName(Context context, String name, String imsi, String iccid, int slotId) {
        String operatorNumic = "";
        String plmn = "";
        if (!TextUtils.isEmpty(imsi) && imsi.length() >= 5) {
            operatorNumic = imsi.substring(0, 5);
        } else if (TextUtils.isEmpty(iccid)) {
            operatorNumic = "";
        } else if (iccid.startsWith("898601234")) {
            plmn = "Test";
        } else if (iccid.startsWith("898600") || iccid.startsWith("986800")) {
            operatorNumic = "46000";
        } else if (iccid.startsWith("898601") || iccid.startsWith("986810")) {
            operatorNumic = "46001";
        } else if (iccid.startsWith("898602")) {
            operatorNumic = "46002";
        } else if (iccid.startsWith("898603") || iccid.startsWith("986830") || iccid.startsWith("898606") || iccid.startsWith("898611")) {
            operatorNumic = "46003";
        } else if (iccid.startsWith("898520")) {
            operatorNumic = "45407";
        }
        if (!TextUtils.isEmpty(operatorNumic)) {
            plmn = getOemOperator(context, operatorNumic);
        }
        if (TextUtils.isEmpty(plmn) && !TextUtils.isEmpty(name)) {
            plmn = name;
        }
        if (TextUtils.isEmpty(plmn)) {
            plmn = "SIM";
        }
        String mSimConfig = SystemProperties.get("persist.radio.multisim.config", "");
        if (getSoftSimCardSlotId() == slotId) {
            return OemTelephonyUtils.getOemRes(context, "redtea_virtul_card", "SIM");
        }
        if (!mSimConfig.equals("dsds") && !mSimConfig.equals("dsda")) {
            return plmn;
        }
        return plmn + Integer.toString(slotId + 1);
    }

    public String getExportSimDefaultName(Context context, int slotId) {
        if (isOppoSingleSimCard(context)) {
            return "SIM";
        }
        if (slotId == 1) {
            return "SIM2";
        }
        return "SIM1";
    }

    public boolean isOppoSingleSimCard(Context context) {
        boolean isSingleSimCard = false;
        try {
            Class colorTelephoneManager = Class.forName("android.telephony.ColorOSTelephonyManager");
            Method getDefaultMethod = colorTelephoneManager.getMethod("getDefault", Context.class);
            isSingleSimCard = ((Boolean) colorTelephoneManager.getMethod("isOppoSingleSimCard", new Class[0]).invoke(getDefaultMethod.invoke(colorTelephoneManager, context), new Object[0])).booleanValue();
        } catch (Exception e) {
            logd("isOppoSingleSimCard failed, " + e.getMessage());
        }
        logd("isSingleSimCard = " + isSingleSimCard);
        return isSingleSimCard;
    }

    public String updateSimNameIfNeed(Context context, int slotId, int subId, String iccid, String defaultVaule) {
        int simState;
        String nameToSet;
        if (OemConstant.EXP_VERSION && getSoftSimCardSlotId() != slotId) {
            return getExportSimDefaultName(context, slotId);
        }
        String imsi = TelephonyManager.from(context).getSubscriberId(subId);
        int subState = getSubState(subId);
        SubscriptionController subc = SubscriptionController.getInstance();
        if (subc != null) {
            simState = subc.getSimStateForSlotIndex(slotId);
        } else {
            simState = 0;
        }
        logd("updateSimNameIfNeed, subState = " + subState + ", simState = " + simState);
        if (simState == 6 || subState == 0) {
            nameToSet = getCarrierName(context, "", "", iccid, slotId);
        } else {
            nameToSet = getCarrierName(context, defaultVaule, imsi, iccid, slotId);
        }
        logd("updateSimNameIfNeed, nameToSet = " + nameToSet + ", defaultVaule = " + defaultVaule);
        return nameToSet;
    }

    public int getSubState(int subId) {
        int subStatus = 0;
        SubscriptionController subc = SubscriptionController.getInstance();
        if (subc != null) {
            if (SubscriptionController.getInstance().getSlotIndex(subId) < 0) {
                logd("getSubState-- subId < 0--" + subId);
                return 0;
            }
            subStatus = ((AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, subc)).getSubState(subId);
        }
        logd("getSubState--subStatus:" + subStatus);
        return subStatus;
    }

    public int getCardType(String imsi, String iccid) {
        int result = -1;
        logd("getCardType imsi : " + imsi + " iccid : " + iccid);
        if (imsi != null && imsi.length() > 5) {
            String mccmnc = imsi.substring(0, 5);
            if (mccmnc.equals("00101") || SystemProperties.getInt("persist.sys.oppo.ctlab", 0) == 1) {
                result = 9;
            } else if (mccmnc.equals("46003") || mccmnc.equals("46011") || mccmnc.equals("45502")) {
                result = 1;
            } else if (mccmnc.equals("46001") || mccmnc.equals("46009") || mccmnc.equals("45407")) {
                result = 3;
            } else if (mccmnc.equals("46000") || mccmnc.equals("46002") || mccmnc.equals("46004") || mccmnc.equals("46007") || mccmnc.equals("46008")) {
                result = 2;
            }
            if (result != -1) {
                logd("getCardType by imsi result = " + result);
                return result;
            }
        }
        if (iccid != null && iccid.length() > 6 && result == -1) {
            String operator = iccid.substring(0, 6);
            if (operator.equals("898603") || operator.equals("898611")) {
                result = 1;
            } else if (operator.equals("898600") || operator.equals("898602") || operator.equals("898607")) {
                result = 2;
            } else if (iccid.startsWith("898601234")) {
                result = 9;
            } else if (operator.equals("898601") || operator.equals("898609")) {
                result = 3;
            } else {
                result = 4;
            }
        }
        logd("getCardType by iccid result = " + result);
        return result;
    }

    public void broadcastSimCardTypeReady(Context context, String slotid, String subid, int cardType) {
        Intent intent = new Intent("oppo.intent.action.SUBINFO_STATE_CHANGE");
        intent.putExtra("slotid", slotid);
        intent.putExtra("subid", subid);
        intent.putExtra("simstate", "CARDTYPE");
        intent.putExtra(INTENT_KEY_CARD_TYPE, cardType);
        logd("Broadcasting intent ACTION_CARD_TYPE_CHANGED slotid:" + slotid + " cardType:" + cardType);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    public String getOemOperator(Context context, String plmn) {
        if (TextUtils.isEmpty(plmn)) {
            return "";
        }
        try {
            Resources resources = context.getResources();
            return context.getString(resources.getIdentifier("mccmnc" + plmn, "string", "com.android.phone"));
        } catch (Exception e) {
            log("getCarrierName no res for " + plmn);
            return "";
        }
    }

    public boolean updateMsisdnToSim(Context context, String number, int subId, Phone phone) {
        if (new updateMsisdnToSim(phone).writeToSim(context, number, subId)) {
            logd("[updateMsisdnToSim]- write to sim sucessfully");
            return DBG;
        }
        log("[updateMsisdnToSim]- write to sim fail");
        return false;
    }

    private class updateMsisdnToSim {
        Phone mPhone;
        boolean mSuccess = false;

        public updateMsisdnToSim(Phone phone) {
            this.mPhone = phone;
            OppoUiccManagerImpl.logd("updateMsisdnToSim created");
        }

        /* access modifiers changed from: private */
        public void setResult(boolean result) {
            this.mSuccess = result;
        }

        /* access modifiers changed from: private */
        public boolean writeToSim(Context context, String number, int subId) {
            String alphaTag = TelephonyManager.from(context).getLine1AlphaTag(subId);
            synchronized (OppoUiccManagerImpl.this.mLock) {
                this.mSuccess = false;
                if (this.mPhone.setLine1Number(alphaTag, number, OppoUiccManagerImpl.this.mBaseHandler.obtainMessage(1, this))) {
                    try {
                        OppoUiccManagerImpl.this.mLock.wait(3000);
                    } catch (InterruptedException e) {
                        OppoUiccManagerImpl.log("interrupted while trying to write MSISDN");
                    }
                }
            }
            return this.mSuccess;
        }
    }

    public boolean ifInterceptPopupTextMsg(CatCmdMessage cmdMsg, CatService catService) {
        String tmpText = cmdMsg.geTextMessage().text;
        if (tmpText == null) {
            return false;
        }
        if (!tmpText.equals("Error in application") && !tmpText.equals("invalid input") && !tmpText.equals("DF A8'H Default Error") && !tmpText.equals("DF A8'H, Default Error") && !tmpText.equals("Out of variable memory")) {
            return false;
        }
        String tmpMccMnc = TelephonyManager.getDefault().getSimOperatorNumeric();
        if ((tmpMccMnc == null || (!tmpMccMnc.startsWith("404") && !tmpMccMnc.startsWith("405") && !tmpMccMnc.equals(""))) && tmpMccMnc != null) {
            return false;
        }
        CatLog.d(catService, "Ignore India sim card popup info, send TR directly");
        return DBG;
    }

    public CatCmdMessage syncCurrentCmd(CatCmdMessage currntCmd, CatCmdMessage sCurrntCmd, CatService catService, int slotId) {
        if (!(currntCmd == null || sCurrntCmd == null || currntCmd == sCurrntCmd)) {
            currntCmd = sCurrntCmd;
        }
        if (currntCmd != null) {
            return currntCmd;
        }
        CatLog.d(catService, "syncCurrentCmd  currntCmd: is null ");
        return sCurrntCmd;
    }

    public boolean OppoSpecialProcessForLockState(Intent simIntent, int slotId) {
        UiccCard card;
        try {
            String state = simIntent.getStringExtra(NetworkDiagnoseUtils.INFO_SERVICESTATE);
            if (state == null || !state.equals("LOCKED") || (card = UiccController.getInstance().getUiccCardForSlot(slotId)) == null) {
                return false;
            }
            UiccCardApplication app = card.getApplication(1);
            if (app == null) {
                app = card.getApplication(2);
            }
            if (app == null) {
                return false;
            }
            IccCardApplicationStatus.AppState appState = app.getState();
            if (appState != IccCardApplicationStatus.AppState.APPSTATE_PIN) {
                if (appState != IccCardApplicationStatus.AppState.APPSTATE_PUK) {
                    logd("OppoSpecialProcessForLockState, app is not locked state, return true");
                    return DBG;
                }
            }
            logd("OppoSpecialProcessForLockState, app is locked state");
            return false;
        } catch (Exception e) {
            logd("OppoSpecialProcessForLockState, exception happen,return!");
            return false;
        }
    }

    public int[] getAllOtherSameIccidSlot(String currentIccid, int currentSlotId) {
        int simCount = TelephonyManager.getDefault().getSimCount();
        int[] result = new int[simCount];
        Arrays.fill(result, 0);
        for (int slot = 0; slot < simCount; slot++) {
            if (slot != currentSlotId) {
                Phone phone = PhoneFactory.getPhone(slot);
                String iccId = null;
                if (phone != null) {
                    iccId = phone.getFullIccSerialNumber();
                }
                if (iccId != null && !TextUtils.isEmpty(iccId) && iccId.equals(currentIccid)) {
                    result[0] = result[0] + 1;
                    result[result[0]] = slot;
                    logd("getAllOtherSameIccidSlot,find one same iccid: slot=" + slot + ",length:" + result[0]);
                }
            }
        }
        return result;
    }

    public boolean isRecordsDoNotExist(String currentIccid, int currentSlotId, Cursor cursor) {
        boolean isNotExist = false;
        int dataBaseSlot = cursor.getInt(1);
        int[] result = getAllOtherSameIccidSlot(currentIccid, currentSlotId);
        if (result != null && result[0] > 0) {
            int i = 1;
            while (true) {
                if (i > result[0]) {
                    break;
                } else if (result[i] == dataBaseSlot) {
                    isNotExist = DBG;
                    logd("isRecordsDoNotExist,find one,isNotExist=" + DBG + ",dataBaseSlot:" + dataBaseSlot);
                    break;
                } else {
                    i++;
                }
            }
        }
        if (isNotExist) {
            int recordCounter = cursor.getCount();
            logd("isRecordsDoNotExist,recordCounter=" + recordCounter);
            if (recordCounter > 1) {
                int j = 0;
                while (true) {
                    if (j >= recordCounter) {
                        break;
                    }
                    dataBaseSlot = cursor.getInt(1);
                    if (dataBaseSlot == currentSlotId || dataBaseSlot == -1) {
                        isNotExist = false;
                        logd("isRecordsDoNotExist,find one in database,dataBaseSlot:" + dataBaseSlot);
                    } else if (!cursor.moveToNext()) {
                        logd("isRecordsDoNotExist,to end,j:" + j);
                        break;
                    } else {
                        j++;
                    }
                }
                isNotExist = false;
                logd("isRecordsDoNotExist,find one in database,dataBaseSlot:" + dataBaseSlot);
            }
        }
        logd("isRecordsDoNotExist,isNotExist=" + isNotExist + ",dataBaseSlot:" + dataBaseSlot);
        return isNotExist;
    }

    public OppoSimlockManager createOppoSimlockManager(Phone[] phone, CommandsInterface[] ci, Context context) {
        this.mOppoSimlockManager = new OppoSimlockManagerImpl(phone, ci, context);
        return this.mOppoSimlockManager;
    }

    public OppoSimlockManager getOppoSimlockManager() {
        return this.mOppoSimlockManager;
    }

    public static void log(String string) {
        Rlog.d(LOG_TAG, string);
    }

    public static void logd(String string) {
        log(string);
    }
}

package com.oppo.internal.telephony;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.AbstractSubscriptionController;
import com.android.internal.telephony.IOppoSubscriptionController;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.dataconnection.AbstractDcTracker;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.uicc.AbstractUiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.uicc.OppoUiccController;
import com.oppo.internal.telephony.utils.OppoServiceStateTrackerUtil;
import java.util.Arrays;
import java.util.HashSet;

public class OppoSubscriptionController implements IOppoSubscriptionController {
    public static final String ACTION_SUBINFO_STATE_CHANGE = "oppo.intent.action.SUBINFO_STATE_CHANGE";
    protected static final boolean DBG = true;
    protected static final int EVENT_CHECK_VSIM = 2;
    protected static final int EVENT_WRITE_MSISDN_DONE = 1;
    public static final String INTENT_KEY_SIM_STATE = "simstate";
    public static final String INTENT_KEY_SLOT_ID = "slotid";
    public static final String INTENT_KEY_SUB_ID = "subid";
    public static final String INTENT_VALUE_SIM_CARD_TYPE = "CARDTYPE";
    private static final String LOG_TAG = "OppoSubscriptionController";
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    public static final int SIM_POWER_STATE_SIM_OFF = 10;
    public static final int SIM_POWER_STATE_SIM_ON = 11;
    protected static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    public Context mContext;
    public final Object mLock = new Object();
    private SubscriptionController mSubscriptionController;
    public boolean mSuccess;

    public OppoSubscriptionController(SubscriptionController subscriptionController, Context c) {
        this.mSubscriptionController = subscriptionController;
        this.mContext = c;
        ((AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, this.mSubscriptionController)).mHandler = new Handler() {
            /* class com.oppo.internal.telephony.OppoSubscriptionController.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                boolean z = OppoSubscriptionController.DBG;
                if (i == 1) {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    synchronized (OppoSubscriptionController.this.mLock) {
                        OppoSubscriptionController oppoSubscriptionController = OppoSubscriptionController.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        oppoSubscriptionController.mSuccess = z;
                        Rlog.d(OppoSubscriptionController.LOG_TAG, "EVENT_WRITE_MSISDN_DONE, mSuccess = " + OppoSubscriptionController.this.mSuccess);
                        OppoSubscriptionController.this.mLock.notifyAll();
                    }
                } else if (i == 2) {
                    OppoSubscriptionController.this.checkSoftSimCard();
                }
            }
        };
    }

    private boolean isSubInfoReady() {
        return SubscriptionInfoUpdater.isSubInfoInitialized();
    }

    public boolean isCTCCard(int slotId) {
        Phone phone = PhoneFactory.getPhone(slotId);
        if (phone != null) {
            String[] vCdmaImsi = ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone)).getLteCdmaImsi(slotId);
            boolean vRet = false;
            if (vCdmaImsi[0] == null || vCdmaImsi[1] == null) {
                return false;
            }
            boolean vCard0 = innerCTCCard(vCdmaImsi[0], slotId);
            boolean vCard1 = innerCTCCard(vCdmaImsi[1], slotId);
            if (vCard0 || vCard1) {
                vRet = true;
            }
            Log.d(LOG_TAG, "isCTCCard-->return->" + vRet);
            return vRet;
        }
        Log.d(LOG_TAG, "isCTCCard-->Failed");
        return false;
    }

    private boolean innerCTCCard(String vimsi, int slotId) {
        Log.d(LOG_TAG, "innerCTCCard vimsi-->" + vimsi);
        int[] subIdA = this.mSubscriptionController.getSubId(slotId);
        if (subIdA != null) {
            String iccid = TelephonyManager.getDefault().getSimSerialNumber(subIdA[0]);
            Log.d(LOG_TAG, "innerCTCCard iccid-1-->" + iccid);
        }
        if (vimsi == null || vimsi.length() <= 5) {
            int[] subId = this.mSubscriptionController.getSubId(slotId);
            if (subId == null) {
                return false;
            }
            String iccid2 = TelephonyManager.getDefault().getSimSerialNumber(subId[0]);
            Log.d(LOG_TAG, "isCTCCard iccid-2-->" + iccid2);
            if (iccid2 == null || iccid2.length() < 6) {
                return false;
            }
            String vOperatorStr = iccid2.substring(0, 6);
            if (vOperatorStr.equals("898603") || vOperatorStr.equals("898611")) {
                return DBG;
            }
            return false;
        }
        String mccmnc = vimsi.substring(0, 5);
        if (mccmnc.equals("46003") || mccmnc.equals("46011") || mccmnc.equals("45502")) {
            return DBG;
        }
        return false;
    }

    public boolean isHasSoftSimCard() {
        if (getSoftSimCardSlotId() >= 0) {
            return DBG;
        }
        return false;
    }

    public int getSoftSimCardSlotId() {
        return getSoftSimCardSlotIdInner();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkSoftSimCard() {
        if (SystemProperties.getInt("gsm.vsim.slotid", -1) != -1 && TextUtils.isEmpty(getSoftSimIccid(this.mContext))) {
            logd("checkSoftSimCard clear");
            SystemProperties.set("gsm.vsim.slotid", "-1");
        }
    }

    private int getSoftSimCardSlotIdInner() {
        return SystemProperties.getInt("gsm.vsim.slotid", -1);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0050 A[SYNTHETIC, Splitter:B:16:0x0050] */
    private String getSoftSimIccid(Context context) {
        String[] columns = {"slot", "iccid"};
        Cursor cursor = null;
        try {
            Cursor cursor2 = context.getContentResolver().query(Uri.parse("content://com.redteamobile.roaming.provider"), columns, null, null, null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    try {
                        cursor2.close();
                    } catch (Exception e) {
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
                    } catch (Exception e2) {
                    }
                    return iccid;
                }
            } while (cursor2.moveToNext());
            if (cursor2 != null) {
            }
            return null;
        } catch (Exception e3) {
            if (0 != 0) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (Exception e4) {
                }
            }
            throw th;
        }
    }

    public void activateSubId(int subId) {
        logd("activateSubId,subId:" + subId);
        if (getSubState(subId) == 1) {
            logd("activateSubId: subscription already active, subId = " + subId);
            return;
        }
        int slotId = this.mSubscriptionController.getSlotIndex(subId);
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            logd("activateSubId, slotId id is invalid");
        } else if (!OemConstant.isUiccSlotForbid(slotId)) {
            ((AbstractUiccController) OemTelephonyUtils.typeCasting(AbstractUiccController.class, OppoUiccController.mUiccController)).setSimPower(slotId, 11, (Message) null);
        }
    }

    public void deactivateSubId(int subId) {
        logd("deactivateSubId,subId:" + subId);
        if (getSubState(subId) == 0) {
            logd("activateSubId: subscription already deactivated, subId = " + subId);
            return;
        }
        int slotId = this.mSubscriptionController.getSlotIndex(subId);
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            logd("activateSubId, slotId id is invalid");
        } else if (!OemConstant.isUiccSlotForbid(slotId)) {
            ((AbstractUiccController) OemTelephonyUtils.typeCasting(AbstractUiccController.class, OppoUiccController.mUiccController)).setSimPower(slotId, 10, (Message) null);
        }
    }

    public int getSubState(int subId) {
        int slotId = this.mSubscriptionController.getSlotIndex(subId);
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            logd("activateSubId, slotId id is invalid");
            return -1;
        }
        int state = ((AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, this.mSubscriptionController)).getMtkSimOnoffState(slotId);
        if (state == 11) {
            return 1;
        }
        if (state == 10) {
            return 0;
        }
        return -1;
    }

    private void broadcastSimInfoContentChanged(int subId, String columnName, int intContent, String stringContent) {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.putExtra("_id", subId);
        intent.putExtra("columnName", columnName);
        intent.putExtra("intContent", intContent);
        intent.putExtra("stringContent", stringContent);
        if (intContent != -100) {
            logd("[broadcastSimInfoContentChanged] subId" + subId + " changed, " + columnName + " -> " + intContent);
        } else {
            logd("[broadcastSimInfoContentChanged] subId" + subId + " changed, " + columnName + " -> " + stringContent);
        }
        this.mSubscriptionController.mContext.sendBroadcast(intent);
    }

    public boolean isUsimWithCsim(int slotId) {
        HashSet<String> fullUiccType = new HashSet<>(Arrays.asList(PhoneFactory.getPhone(slotId).getUiccCard() != null ? getFullIccCardType(slotId) : new String[]{""}));
        if (!fullUiccType.contains("USIM") || !fullUiccType.contains("CSIM")) {
            return false;
        }
        return DBG;
    }

    public String[] getFullIccCardType(int phoneId) {
        return SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[phoneId]).split(",");
    }

    public String getCarrierName(Context context, String name, String imsi, String iccid, int slotid) {
        String operatorNumic = "";
        String plmn = "";
        if (!TextUtils.isEmpty(name)) {
            plmn = name;
        } else if (!TextUtils.isEmpty(imsi) && imsi.length() >= 5) {
            operatorNumic = imsi.substring(0, 5);
        } else if (TextUtils.isEmpty(iccid)) {
            operatorNumic = "";
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
        if (TextUtils.isEmpty(plmn)) {
            plmn = "SIM";
        }
        String mSimConfig = SystemProperties.get("persist.radio.multisim.config", "");
        if (getSoftSimCardSlotIdInner() == slotid) {
            plmn = OemConstant.getOemRes(context, "redtea_virtul_card", "");
        } else if (mSimConfig.equals("dsds") || mSimConfig.equals("dsda")) {
            plmn = plmn + Integer.toString(slotid + 1);
        }
        Rlog.d(LOG_TAG, "[getCarrierName]- name:" + name + " iccid:" + iccid + " plmn:" + plmn);
        return plmn;
    }

    public String getOemOperator(Context context, String plmn) {
        if (TextUtils.isEmpty(plmn)) {
            return "";
        }
        return OppoServiceStateTrackerUtil.oppoGetPlmnOverride(context, plmn, null);
    }

    public String getExportSimDefaultName(int slotId) {
        if (OppoUiccManagerImpl.getInstance().isOppoSingleSimCard(this.mContext)) {
            return "SIM";
        }
        if (slotId != 0 && slotId == 1) {
            return "SIM2";
        }
        return "SIM1";
    }

    public String getOperatorNumericForData(int phoneId) {
        DcTracker dcTracker;
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone == null || (dcTracker = phone.getDcTracker(1)) == null) {
            return "";
        }
        return ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, dcTracker)).getOperatorNumeric();
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c4 A[SYNTHETIC, Splitter:B:41:0x00c4] */
    public int setDisplayNumber(String number, int subId, boolean writeToSim) {
        int result;
        Throwable th;
        int result2;
        logd("[setDisplayNumber]+ subId:" + subId);
        enforceModifyPhoneState("setDisplayNumber");
        long identity = Binder.clearCallingIdentity();
        try {
            if (!this.mSubscriptionController.isActiveSubId(subId)) {
                return -1;
            }
            int phoneId = this.mSubscriptionController.getPhoneId(subId);
            if (number != null) {
                if (SubscriptionManager.isValidPhoneId(phoneId)) {
                    ContentValues value = new ContentValues(1);
                    value.put("number", number);
                    if (writeToSim) {
                        Phone phone = PhoneFactory.getPhone(phoneId);
                        String alphaTag = TelephonyManager.from(this.mContext).getLine1AlphaTag(subId);
                        synchronized (this.mLock) {
                            try {
                                this.mSuccess = false;
                                this.mSuccess = phone.setLine1Number(alphaTag, number, ((AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, this.mSubscriptionController)).mHandler.obtainMessage(1));
                                loge("[setDisplayNumber]setLine1Number result is :" + this.mSuccess);
                                if (this.mSuccess) {
                                    try {
                                        result = -1;
                                        try {
                                            this.mLock.wait(3000);
                                        } catch (InterruptedException e) {
                                        }
                                    } catch (InterruptedException e2) {
                                        result = -1;
                                        loge("interrupted while trying to write MSISDN");
                                        synchronized (this.mLock) {
                                        }
                                    }
                                } else {
                                    result = -1;
                                    loge("[setDisplayNumber]setLine1Number fail due to iccrecord is null");
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    } else {
                        result = -1;
                    }
                    synchronized (this.mLock) {
                        try {
                            if (!this.mSuccess) {
                                if (writeToSim) {
                                    result2 = result;
                                    Binder.restoreCallingIdentity(identity);
                                    return result2;
                                }
                            }
                            result2 = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
                            try {
                                this.mSubscriptionController.refreshCachedActiveSubscriptionInfoList();
                                logd("[setDisplayNumber]- update result :" + result2);
                                this.mSubscriptionController.notifySubscriptionInfoChanged();
                                Binder.restoreCallingIdentity(identity);
                                return result2;
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            throw th;
                        }
                    }
                }
            }
            logd("[setDispalyNumber]- fail");
            Binder.restoreCallingIdentity(identity);
            return -1;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void broadcastSubInfoUpdateIntent(String slotid, String subid, String simstate) {
        Intent intent = new Intent(ACTION_SUBINFO_STATE_CHANGE);
        intent.putExtra(INTENT_KEY_SLOT_ID, slotid);
        intent.putExtra(INTENT_KEY_SUB_ID, subid);
        intent.putExtra(INTENT_KEY_SIM_STATE, simstate);
        intent.addFlags(16777216);
        logd("Broadcasting intent ACTION_SUBINFO_STATE_CHANGE slotid:" + slotid + " simstate:" + simstate + " subid:" + subid);
        this.mContext.sendBroadcast(intent);
    }

    public boolean isSoftSimCardSubId(int subId) {
        boolean result = false;
        if (isHasSoftSimCard()) {
            int softSimSlotId = getSoftSimCardSlotId();
            if (softSimSlotId > -1) {
                int[] subIds = this.mSubscriptionController.getSubId(softSimSlotId);
                if (subIds == null || subIds.length <= 0) {
                    logd("[isSoftSimCardSubId]- getSubId failed subIds == null || length == 0 subIds=" + subIds);
                } else {
                    logd("[isSoftSimCardSubId]- Soft sim slot id: " + softSimSlotId + "Soft sim subId: " + subIds[0]);
                    if (subIds[0] == subId) {
                        result = DBG;
                    }
                }
            } else {
                logd("[isSoftSimCardSubId]- SoftSimCard enable,but slotId is wrong!!,softSimSlotId:" + softSimSlotId);
            }
        } else {
            logd("[isSoftSimCardSubId]- soft sim disable!!");
        }
        logd("[isSoftSimCardSubId]-result:" + result);
        return result;
    }

    private void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private void enforceReadPrivilegedPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
    }

    private void logv(String msg) {
        Rlog.v(LOG_TAG, msg);
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}

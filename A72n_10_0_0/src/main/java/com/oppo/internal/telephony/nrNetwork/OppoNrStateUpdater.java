package com.oppo.internal.telephony.nrNetwork;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.DataSpecificRegistrationInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OemFeature;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.UiccController;
import com.oppo.internal.telephony.OppoServiceStateTracker;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseService;
import com.oppo.internal.telephony.nwdiagnose.OppoPhoneStateMonitor;
import com.oppo.internal.telephony.recovery.OppoFastRecovery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OppoNrStateUpdater {
    private static final int EVENT_ICC_CHANGED = 203;
    private static final int EVENT_NETWORK_TYPE_CHANGED = 204;
    private static final int EVENT_NRSTATE_SMOOTH = 202;
    private static final int EVENT_NR_STATE_CHANGED = 201;
    private static final int EVENT_OEM_SCREEN_CHANGED = 200;
    private static final int NR_CFG_CM = 2;
    private static final int NR_CFG_CT = 3;
    private static final int NR_CFG_CU = 4;
    private static final int NR_CFG_DEFAULT = 1;
    private static final boolean mExpVersion = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    private final String CFG_URI = "oppo_nr_common_cfg";
    private String LOG_TAG = "OppoNrStateUpdater";
    private final String NR_ICON_RULE = "persist.vendor.radio.nr_display_rule";
    private final String NR_ICON_RULE_CFG_A_D = "UPDATE_DISPLAY_RULE, 4, 0, 0, 15, 31";
    private final String NR_ICON_RULE_CFG_D = "UPDATE_DISPLAY_RULE, 1, 0, 0, 0, 0";
    private long mCellIdCacheTimer = 259200000;
    private int mCellIdListSize = 50;
    private Map<Integer, Long> mCellIdMap = new ConcurrentHashMap();
    private Context mContext;
    private EventHandler mEventHandler = null;
    private boolean mIsOpenRetrictedDcnr = false;
    private boolean mIsSupportOptimizedNr = SystemProperties.getBoolean("persist.vendor.radio.support_optimized_nr", false);
    private boolean mModemResetSmoothEnabled = true;
    private int mModemResetSmoothTime = OppoServiceStateTracker.DELAYTIME_10S;
    private int mNetworkMode = 33;
    private final Uri mNrConfigUri = Settings.Global.getUriFor("oppo_nr_common_cfg");
    private final int mNrOpeartorConfig = SystemProperties.getInt("persist.vendor.radio.nroperator", 1);
    private int mNrState = -1;
    private int mNrStateSmoothDelay = OppoServiceStateTracker.DELAYTIME_10S;
    private Handler mOppoSST;
    private GsmCdmaPhone mPhone;
    private int mPhoneId = 0;
    private int mPreviousSubId = -1;
    private ContentObserver mSettingObserver = new ContentObserver(new Handler()) {
        /* class com.oppo.internal.telephony.nrNetwork.OppoNrStateUpdater.AnonymousClass1 */

        public void onChange(boolean selfChange) {
            OppoNrStateUpdater.this.updateNrConfig();
        }
    };

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNrConfig() {
        String nrConfig = Settings.Global.getString(this.mContext.getContentResolver(), "oppo_nr_common_cfg");
        boolean z = false;
        if (nrConfig != null && !nrConfig.isEmpty() && nrConfig.contains(";")) {
            String[] paraArray = nrConfig.split(";");
            if (7 != paraArray.length) {
                loge("updateNrConfig length is  " + paraArray.length);
                return;
            }
            if (paraArray[0].contains("support_optimized_nr")) {
                String[] tempPara = paraArray[0].split("=");
                if (2 == tempPara.length && isNumeric(tempPara[1])) {
                    this.mIsSupportOptimizedNr = Integer.parseInt(tempPara[1]) == 1;
                }
            }
            if (paraArray[1].contains("cellId_save_timeout")) {
                String[] tempPara2 = paraArray[1].split("=");
                if (2 == tempPara2.length && isNumeric(tempPara2[1])) {
                    this.mCellIdCacheTimer = Long.parseLong(tempPara2[1]) * 30 * 60 * 1000;
                }
            }
            if (paraArray[2].contains("cellId_list_size")) {
                String[] tempPara3 = paraArray[2].split("=");
                if (2 == tempPara3.length && isNumeric(tempPara3[1])) {
                    this.mCellIdListSize = Integer.parseInt(tempPara3[1]);
                }
            }
            if (paraArray[3].contains("nrstate_smooth_delay")) {
                String[] tempPara4 = paraArray[3].split("=");
                if (2 == tempPara4.length && isNumeric(tempPara4[1])) {
                    this.mNrStateSmoothDelay = Integer.parseInt(tempPara4[1]) * 1000;
                }
            }
            if (paraArray[4].contains("nr_modem_reset_smooth_enabled")) {
                String[] tempPara5 = paraArray[4].split("=");
                if (2 == tempPara5.length && isNumeric(tempPara5[1])) {
                    this.mModemResetSmoothEnabled = Integer.parseInt(tempPara5[1]) == 1;
                }
            }
            if (paraArray[5].contains("nr_modem_reset_smooth_time")) {
                String[] tempPara6 = paraArray[5].split("=");
                if (2 == tempPara6.length && isNumeric(tempPara6[1])) {
                    this.mModemResetSmoothTime = Integer.parseInt(tempPara6[1]) * 1000;
                }
            }
            if (paraArray[6].contains("open_restricted_dcnr")) {
                String[] tempPara7 = paraArray[6].split("=");
                if (2 == tempPara7.length && isNumeric(tempPara7[1])) {
                    this.mIsOpenRetrictedDcnr = Integer.parseInt(tempPara7[1]) > 0;
                }
            }
        }
        this.mIsSupportOptimizedNr = OemFeature.FEATURE_NW_REG_SWITCH_SMOOTH && this.mIsSupportOptimizedNr;
        if (OemFeature.FEATURE_NW_REG_SWITCH_SMOOTH && this.mModemResetSmoothEnabled) {
            z = true;
        }
        this.mModemResetSmoothEnabled = z;
        logd("updateNrConfig mIsSupportOptimizedNr: " + this.mIsSupportOptimizedNr + ", mCellIdCacheTimer: " + this.mCellIdCacheTimer + ", mCellIdListSize: " + this.mCellIdListSize + ", mNrStateSmoothDelay: " + this.mNrStateSmoothDelay + ", mModemResetSmoothEnabled: " + this.mModemResetSmoothEnabled + ", mModemResetSmoothTime: " + this.mModemResetSmoothTime + ", mIsOpenRetrictedDcnr: " + this.mIsOpenRetrictedDcnr);
    }

    private boolean isNumeric(String str) {
        int i = str.length();
        do {
            i--;
            if (i < 0) {
                return true;
            }
        } while (Character.isDigit(str.charAt(i)));
        return false;
    }

    public boolean isModemResetSmoothEnabled() {
        return this.mModemResetSmoothEnabled;
    }

    public int getModemResetSmoothTime() {
        return this.mModemResetSmoothTime;
    }

    public OppoNrStateUpdater(Context context, GsmCdmaPhone phone, Handler sst) {
        this.mContext = context;
        this.mPhone = phone;
        this.mOppoSST = sst;
        this.mPhoneId = this.mPhone.getPhoneId();
        this.LOG_TAG += "/" + this.mPhoneId;
        logd("create OppoNrStateUpdater");
        this.mEventHandler = new EventHandler();
        UiccController.getInstance().registerForIccChanged(this.mEventHandler, (int) EVENT_ICC_CHANGED, (Object) null);
        this.mContext.getContentResolver().registerContentObserver(this.mNrConfigUri, true, this.mSettingObserver);
        updateNrConfig();
        if (mExpVersion) {
            updateNrDisplayRuleEx();
        }
        SubscriptionManager.from(this.mPhone.getContext()).addOnSubscriptionsChangedListener(new SubscriptionManager.OnSubscriptionsChangedListener(this.mEventHandler.getLooper()) {
            /* class com.oppo.internal.telephony.nrNetwork.OppoNrStateUpdater.AnonymousClass2 */

            public void onSubscriptionsChanged() {
                int subId = OppoNrStateUpdater.this.mPhone.getSubId();
                if (OppoNrStateUpdater.this.mPreviousSubId != subId) {
                    OppoNrStateUpdater.this.mPreviousSubId = subId;
                    String str = OppoNrStateUpdater.this.LOG_TAG;
                    Rlog.d(str, "onSubscriptionsChanged subId:" + OppoNrStateUpdater.this.mPreviousSubId);
                    OppoNrStateUpdater oppoNrStateUpdater = OppoNrStateUpdater.this;
                    oppoNrStateUpdater.mNetworkMode = PhoneFactory.calculatePreferredNetworkType(oppoNrStateUpdater.mContext, subId);
                    NetworkDiagnoseService diagnoseService = NetworkDiagnoseService.getInstance();
                    if (diagnoseService == null) {
                        diagnoseService = NetworkDiagnoseService.make(OppoNrStateUpdater.this.mContext);
                    }
                    OppoPhoneStateMonitor stateMonitor = diagnoseService.getPhoneStateMonitor(OppoNrStateUpdater.this.mPhone.getPhoneId());
                    if (stateMonitor != null) {
                        stateMonitor.unregisterForNetworkTypeChanged(OppoNrStateUpdater.this.mEventHandler);
                        stateMonitor.registerForNetworkTypeChanged(OppoNrStateUpdater.this.mEventHandler, OppoNrStateUpdater.EVENT_NETWORK_TYPE_CHANGED, null);
                    }
                }
            }
        });
    }

    private boolean isNotAllowUpdateNrState(ServiceState ss) {
        if (!(SubscriptionController.getInstance().getDefaultDataSubId() == this.mPhone.getSubId()) || this.mNetworkMode < 23) {
            return true;
        }
        int dataRadioTechnology = ss.getRilDataRadioTechnology();
        int voiceRadioTechnology = ss.getRilVoiceRadioTechnology();
        return !(ServiceState.isLte(dataRadioTechnology) || 20 == dataRadioTechnology || ServiceState.isLte(voiceRadioTechnology) || 20 == voiceRadioTechnology) && !isOtherPhoneIncall();
    }

    public int getNrState(int newNrState, boolean hasNrSecondaryServingCell, ServiceState ss) {
        if (isNotAllowUpdateNrState(ss)) {
            if (this.mIsSupportOptimizedNr) {
                this.mNrState = -1;
                this.mEventHandler.removeMessages(EVENT_NRSTATE_SMOOTH);
            }
            return -1;
        }
        if (hasNrSecondaryServingCell) {
            newNrState = 3;
        }
        return optimizedNrIconType(newNrState, ss);
    }

    /* access modifiers changed from: private */
    public class EventHandler extends Handler {
        private EventHandler() {
        }

        public void handleMessage(Message msg) {
            OppoNrStateUpdater oppoNrStateUpdater = OppoNrStateUpdater.this;
            oppoNrStateUpdater.logd("EventHandler:" + msg.what);
            switch (msg.what) {
                case OppoNrStateUpdater.EVENT_NR_STATE_CHANGED /* 201 */:
                default:
                    return;
                case OppoNrStateUpdater.EVENT_NRSTATE_SMOOTH /* 202 */:
                    if (!OppoNrStateUpdater.this.isOtherPhoneIncall()) {
                        OppoNrStateUpdater.this.mNrState = msg.arg1;
                    }
                    ServiceStateTracker sst = OppoNrStateUpdater.this.mPhone.getServiceStateTracker();
                    if (sst != null) {
                        sst.pollState();
                        return;
                    }
                    return;
                case OppoNrStateUpdater.EVENT_ICC_CHANGED /* 203 */:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    new Integer(0);
                    if (ar.result != null) {
                        Integer cardIndex = (Integer) ar.result;
                        if (cardIndex.intValue() == OppoNrStateUpdater.this.mPhoneId && SubscriptionController.getInstance().getSimStateForSlotIndex(cardIndex.intValue()) == 1) {
                            OppoNrStateUpdater.this.logd("clear for SIM_STATE_ABSENT");
                            OppoNrStateUpdater.this.mNrState = -1;
                            return;
                        }
                        return;
                    }
                    OppoNrStateUpdater.this.loge("Error: Invalid card index EVENT_ICC_CHANGED ");
                    return;
                case OppoNrStateUpdater.EVENT_NETWORK_TYPE_CHANGED /* 204 */:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.result != null) {
                        OppoNrStateUpdater.this.mNetworkMode = ((Integer) ar2.result).intValue();
                        OppoNrStateUpdater oppoNrStateUpdater2 = OppoNrStateUpdater.this;
                        oppoNrStateUpdater2.logd("new networkMode = " + OppoNrStateUpdater.this.mNetworkMode);
                        if (OppoNrStateUpdater.this.mNetworkMode < 23 && OppoNrStateUpdater.this.mIsSupportOptimizedNr) {
                            OppoNrStateUpdater.this.mNrState = -1;
                            OppoNrStateUpdater.this.mEventHandler.removeMessages(OppoNrStateUpdater.EVENT_NRSTATE_SMOOTH);
                            ServiceStateTracker SSTracker = OppoNrStateUpdater.this.mPhone.getServiceStateTracker();
                            if (SSTracker != null && SSTracker.getServiceState().getNrState() > 1) {
                                SSTracker.pollState();
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isOtherPhoneIncall() {
        Phone oPhone = PhoneFactory.getPhone(1 - this.mPhone.getPhoneId());
        if (oPhone == null || oPhone.getState() == PhoneConstants.State.IDLE) {
            return false;
        }
        return true;
    }

    private int getLTECid(ServiceState ss) {
        CellIdentity id;
        int cid = -1;
        NetworkRegistrationInfo regInfo = ss.getNetworkRegistrationInfo(2, 1);
        if (regInfo == null || regInfo.getCellIdentity() == null) {
            regInfo = ss.getNetworkRegistrationInfo(1, 1);
        }
        if (regInfo == null || (id = regInfo.getCellIdentity()) == null) {
            return -1;
        }
        if (id.getType() == 3) {
            cid = ((CellIdentityLte) id).getCi();
        }
        if (cid == Integer.MAX_VALUE) {
            return -1;
        }
        return cid;
    }

    private boolean isCellIdInvalid(int cellId) {
        return cellId == -1 || cellId == Integer.MAX_VALUE || cellId == 0;
    }

    private boolean isNeedSaveCellId(int state, int cellId) {
        if (isCellIdInvalid(cellId)) {
            return false;
        }
        return true;
    }

    private int getNrStateFromServiceState(ServiceState ss) {
        DataSpecificRegistrationInfo dsri;
        int nrState = -1;
        NetworkRegistrationInfo nri = ss.getNetworkRegistrationInfo(2, 1);
        if (nri == null || (dsri = nri.getDataSpecificInfo()) == null) {
            return -1;
        }
        if (dsri.isEnDcAvailable && !dsri.isDcNrRestricted && dsri.isNrAvailable) {
            nrState = 2;
        }
        if (!this.mIsOpenRetrictedDcnr || !dsri.isDcNrRestricted) {
            return nrState;
        }
        return 1;
    }

    private int smoothForNrIconType(int nrState, ServiceState ss) {
        if (this.mNrState != nrState) {
            logd("smoothForNrIconType new NrState " + nrState + ", old NrState: " + this.mNrState);
        }
        if (nrState > 1 || this.mNrState < 2) {
            if (!this.mEventHandler.hasMessages(EVENT_NRSTATE_SMOOTH)) {
                this.mNrState = nrState;
                this.mEventHandler.removeMessages(EVENT_NRSTATE_SMOOTH);
            } else if (nrState == getNrStateFromServiceState(ss)) {
                this.mNrState = nrState;
                this.mEventHandler.removeMessages(EVENT_NRSTATE_SMOOTH);
            }
        } else if (!this.mEventHandler.hasMessages(EVENT_NRSTATE_SMOOTH)) {
            logd("EVENT_NRSTATE_SMOOTH Create. ");
            Message msg = Message.obtain();
            msg.what = EVENT_NRSTATE_SMOOTH;
            msg.arg1 = nrState;
            this.mEventHandler.sendMessageDelayed(msg, (long) this.mNrStateSmoothDelay);
        }
        return this.mNrState;
    }

    public synchronized int optimizedNrIconType(int newNrState, ServiceState ss) {
        int state = newNrState;
        if (!this.mIsOpenRetrictedDcnr && state == 1) {
            if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
                logd("optimizedNrIconType EXP VERSION keep nr state as default config");
            } else {
                logd("change nr state when mIsOpenRetrictedDcnr is not true and NR_STATE_RESTRICTED ");
                state = 2;
            }
        }
        if (!this.mIsSupportOptimizedNr) {
            return state;
        }
        int cellId = getLTECid(ss);
        updateCellIdMap();
        if (state == 3) {
            if (isNeedSaveCellId(state, cellId)) {
                updateCellIdCache(cellId);
            }
            this.mNrState = state;
            this.mEventHandler.removeMessages(EVENT_NRSTATE_SMOOTH);
        } else if (state != -1 || isCellIdInvalid(cellId) || (!this.mCellIdMap.containsKey(Integer.valueOf(cellId)) && !OppoFastRecovery.make(this.mContext).isCellinBlackList(cellId))) {
            state = smoothForNrIconType(state, ss);
        } else {
            state = 2;
            this.mNrState = 2;
            this.mEventHandler.removeMessages(EVENT_NRSTATE_SMOOTH);
            logd("update NR_STATE_NOT_RESTRICTED for the bearerAllocationCell");
        }
        return state;
    }

    private void updateCellIdMap() {
        Iterator<Map.Entry<Integer, Long>> entries = this.mCellIdMap.entrySet().iterator();
        while (entries.hasNext()) {
            if (SystemClock.elapsedRealtime() - entries.next().getValue().longValue() > this.mCellIdCacheTimer) {
                entries.remove();
            }
        }
        if (this.mCellIdMap.size() > 0) {
            logd("mBearerCellIdMap.size: " + this.mCellIdMap.size());
        }
    }

    private void updateCellIdCache(int cellId) {
        if (this.mCellIdMap.size() == this.mCellIdListSize && !this.mCellIdMap.containsKey(Integer.valueOf(cellId))) {
            logd("mBearerCellIdMap is full , remove first cellid.");
            List<Map.Entry<Integer, Long>> list = new ArrayList<>(this.mCellIdMap.entrySet());
            sortCellIdMap(list);
            int removeKey = -1;
            if (!(list.size() == 0 || list.get(0) == null)) {
                removeKey = list.get(0).getKey().intValue();
            }
            this.mCellIdMap.remove(Integer.valueOf(removeKey));
        }
        this.mCellIdMap.put(Integer.valueOf(cellId), Long.valueOf(SystemClock.elapsedRealtime()));
        logd("updateBearerCellIdCache size: " + this.mCellIdMap.size());
    }

    private void sortCellIdMap(List<Map.Entry<Integer, Long>> list) {
        Collections.sort(list, new Comparator<Map.Entry<Integer, Long>>() {
            /* class com.oppo.internal.telephony.nrNetwork.OppoNrStateUpdater.AnonymousClass3 */

            public int compare(Map.Entry<Integer, Long> o1, Map.Entry<Integer, Long> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
    }

    private void updateNrDisplayRuleEx() {
        String regionMark = SystemProperties.get("ro.oppo.regionmark", "oppo");
        int nrIconRule = SystemProperties.getInt("persist.vendor.radio.nr_display_rule", 1);
        if ("TW".equals(regionMark)) {
            if (nrIconRule == 4) {
                logd("5g icon rule is config A&D,not change it");
                return;
            }
            sendDisplayRule("UPDATE_DISPLAY_RULE, 4, 0, 0, 15, 31");
            logd("oppo set 5G icon rule Config A+D");
        } else if (nrIconRule == 1) {
            logd("5g icon rule is config D,not change it");
        } else {
            sendDisplayRule("UPDATE_DISPLAY_RULE, 1, 0, 0, 0, 0");
            logd("change 5G icon to Config D");
        }
    }

    private void sendDisplayRule(String cmdLine) {
        try {
            byte[] rawData = cmdLine.getBytes();
            byte[] cmdByte = new byte[(rawData.length + 1)];
            System.arraycopy(rawData, 0, cmdByte, 0, rawData.length);
            cmdByte[cmdByte.length - 1] = 0;
            this.mPhone.invokeOemRilRequestRaw(cmdByte, (Message) null);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.LOG_TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }
}

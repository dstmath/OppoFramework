package com.android.internal.telephony;

import android.content.Context;
import android.os.Handler;
import com.android.internal.telephony.ISub;
import java.util.Map;

public abstract class AbstractSubscriptionController extends ISub.Stub {
    public static final String ACTION_SUBINFO_STATE_CHANGE = "oppo.intent.action.SUBINFO_STATE_CHANGE";
    public static final int EVENT_CHECK_VSIM = 2;
    public static final int EVENT_WRITE_MSISDN_DONE = 1;
    public static final String INTENT_KEY_SIM_STATE = "simstate";
    public static final String INTENT_KEY_SLOT_ID = "slotid";
    public static final String INTENT_KEY_SUB_ID = "subid";
    public static final String INTENT_VALUE_SIM_CARD_TYPE = "CARDTYPE";
    private static final String LOG_TAG = "AbstractSubscriptionController";
    public static final String OPPO_MULTI_SIM_NETWORK_PRIMARY_SLOT = "oppo_multi_sim_network_primary_slot";
    public Handler mHandler;
    protected IOppoSubscriptionController mReference = null;

    public boolean isCTCCard(int slotId) {
        return this.mReference.isCTCCard(slotId);
    }

    public boolean isHasSoftSimCard() {
        return this.mReference.isHasSoftSimCard();
    }

    public int getSoftSimCardSlotId() {
        return this.mReference.getSoftSimCardSlotId();
    }

    public void activateSubId(int subId) {
        this.mReference.activateSubId(subId);
    }

    public void deactivateSubId(int subId) {
        this.mReference.deactivateSubId(subId);
    }

    public int setSubState(int subId, int subStatus) {
        return this.mReference.setSubState(subId, subStatus);
    }

    public int getSubState(int subId) {
        return this.mReference.getSubState(subId);
    }

    public boolean isUsimWithCsim(int slotId) {
        return this.mReference.isUsimWithCsim(slotId);
    }

    public String getCarrierName(Context context, String name, String imsi, String iccid, int slotid) {
        return this.mReference.getCarrierName(context, name, imsi, iccid, slotid);
    }

    public String getOemOperator(Context context, String plmn) {
        return this.mReference.getOemOperator(context, plmn);
    }

    public String getExportSimDefaultName(int slotId) {
        return this.mReference.getExportSimDefaultName(slotId);
    }

    public String getOperatorNumericForData(int phoneId) {
        return this.mReference.getOperatorNumericForData(phoneId);
    }

    public int setDisplayNumber(String number, int subId, boolean writeToSim) {
        return this.mReference.setDisplayNumber(number, subId, writeToSim);
    }

    /* access modifiers changed from: protected */
    public void broadcastSubInfoUpdateIntent(String slotid, String subid, String simstate) {
        this.mReference.broadcastSubInfoUpdateIntent(slotid, subid, simstate);
    }

    public boolean informNewSimCardLoaded(int slotIndex) {
        return this.mReference.informNewSimCardLoaded(slotIndex);
    }

    public void updateMapValue(Map<Integer, Integer> map, int slotIndex, int value) {
        this.mReference.updateMapValue(map, slotIndex, value);
    }

    public boolean isSoftSimCardSubId(int subId) {
        return this.mReference.isSoftSimCardSubId(subId);
    }

    public int getMtkSimOnoffState(int slotId) {
        return -1;
    }
}

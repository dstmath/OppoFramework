package com.android.internal.telephony.uicc;

import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.IccCardStatus;

public class IccSlotStatus {
    public String atr;
    public IccCardStatus.CardState cardState;
    public String eid;
    public String iccid;
    public int logicalSlotIndex;
    public SlotState slotState;

    public enum SlotState {
        SLOTSTATE_INACTIVE,
        SLOTSTATE_ACTIVE
    }

    public void setCardState(int state) {
        if (state == 0) {
            this.cardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
        } else if (state == 1) {
            this.cardState = IccCardStatus.CardState.CARDSTATE_PRESENT;
        } else if (state == 2) {
            this.cardState = IccCardStatus.CardState.CARDSTATE_ERROR;
        } else if (state == 3) {
            this.cardState = IccCardStatus.CardState.CARDSTATE_RESTRICTED;
        } else {
            throw new RuntimeException("Unrecognized RIL_CardState: " + state);
        }
    }

    public void setSlotState(int state) {
        if (state == 0) {
            this.slotState = SlotState.SLOTSTATE_INACTIVE;
        } else if (state == 1) {
            this.slotState = SlotState.SLOTSTATE_ACTIVE;
        } else {
            throw new RuntimeException("Unrecognized RIL_SlotState: " + state);
        }
    }

    public String toString() {
        return "IccSlotStatus {" + this.cardState + "," + this.slotState + "," + "logicalSlotIndex=" + this.logicalSlotIndex + "," + "atr=" + this.atr + ",iccid=" + SubscriptionInfo.givePrintableIccid(this.iccid) + "," + "eid=" + this.eid + "}";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IccSlotStatus that = (IccSlotStatus) obj;
        if (this.cardState != that.cardState || this.slotState != that.slotState || this.logicalSlotIndex != that.logicalSlotIndex || !TextUtils.equals(this.atr, that.atr) || !TextUtils.equals(this.iccid, that.iccid) || !TextUtils.equals(this.eid, that.eid)) {
            return false;
        }
        return true;
    }
}

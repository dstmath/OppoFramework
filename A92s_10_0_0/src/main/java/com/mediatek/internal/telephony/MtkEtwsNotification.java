package com.mediatek.internal.telephony;

import android.text.TextUtils;
import com.android.internal.telephony.uicc.IccUtils;

public class MtkEtwsNotification {
    public int messageId;
    public String plmnId;
    public String securityInfo;
    public int serialNumber;
    public int warningType;

    public String toString() {
        return "MtkEtwsNotification: " + this.warningType + ", " + this.messageId + ", " + this.serialNumber + ", " + this.plmnId + ", " + this.securityInfo;
    }

    public boolean isDuplicatedEtws(MtkEtwsNotification other) {
        if (this.warningType == other.warningType && this.messageId == other.messageId && this.serialNumber == other.serialNumber && this.plmnId.equals(other.plmnId)) {
            return true;
        }
        return false;
    }

    public byte[] getEtwsPdu() {
        byte[] etwsPdu = new byte[56];
        System.arraycopy(MtkEtwsUtils.intToBytes(this.serialNumber), 2, etwsPdu, 0, 2);
        System.arraycopy(MtkEtwsUtils.intToBytes(this.messageId), 2, etwsPdu, 2, 2);
        System.arraycopy(MtkEtwsUtils.intToBytes(this.warningType), 2, etwsPdu, 4, 2);
        if (!TextUtils.isEmpty(this.securityInfo)) {
            System.arraycopy(IccUtils.hexStringToBytes(this.securityInfo), 0, etwsPdu, 6, 50);
        }
        return etwsPdu;
    }
}

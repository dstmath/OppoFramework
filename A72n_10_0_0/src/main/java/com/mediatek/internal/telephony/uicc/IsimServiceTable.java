package com.mediatek.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccServiceTable;

public final class IsimServiceTable extends IccServiceTable {

    public enum IsimService {
        PCSCF_ADDRESS,
        GBA,
        HTTP_DIGEST,
        GBA_LOCAL_KEY_ESTABLISHMENT,
        PCSCF_DISCOVERY,
        SMS,
        SMSR,
        SM_OVER_IP,
        COMMUNICATION_CONTROL_BY_ISIM,
        UICC_ACCESS_IMS
    }

    public IsimServiceTable(byte[] table) {
        super(table);
    }

    public boolean isAvailable(IsimService service) {
        return IsimServiceTable.super.isAvailable(service.ordinal());
    }

    /* access modifiers changed from: protected */
    public String getTag() {
        return "IsimServiceTable";
    }

    /* access modifiers changed from: protected */
    public Object[] getValues() {
        return IsimService.values();
    }
}

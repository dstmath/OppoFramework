package com.mediatek.internal.telephony.dataconnection;

import android.telephony.PcoData;

public class PcoDataAfterAttached extends PcoData {
    public final String apnName;

    public PcoDataAfterAttached(int cid, String apnName2, String bearerProto, int pcoId, byte[] contents) {
        super(cid, bearerProto, pcoId, contents);
        this.apnName = apnName2;
    }

    public String toString() {
        return "PcoDataAfterAttached(" + this.cid + ", " + this.apnName + ", " + this.bearerProto + ", " + this.pcoId + ", contents[" + this.contents.length + "])";
    }
}

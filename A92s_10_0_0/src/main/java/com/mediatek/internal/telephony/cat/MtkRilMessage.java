package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.RilMessage;

/* access modifiers changed from: package-private */
public class MtkRilMessage extends RilMessage {
    boolean mSetUpMenuFromMD;

    MtkRilMessage(int msgId, String rawData) {
        super(msgId, rawData);
        this.mSetUpMenuFromMD = false;
    }

    MtkRilMessage(MtkRilMessage other) {
        super(other);
        this.mSetUpMenuFromMD = other.mSetUpMenuFromMD;
    }

    /* access modifiers changed from: package-private */
    public void setSetUpMenuFromMD(boolean flag) {
        this.mSetUpMenuFromMD = flag;
    }
}

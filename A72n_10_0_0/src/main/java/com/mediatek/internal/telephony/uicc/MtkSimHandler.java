package com.mediatek.internal.telephony.uicc;

import android.content.Context;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public class MtkSimHandler implements IMtkSimHandler {
    private static String TAG = "MtkSimHandler";
    protected int mPhoneId = -1;

    public MtkSimHandler() {
        mtkLog(TAG, "Enter MtkSimHandler");
    }

    public MtkSimHandler(Context context, CommandsInterface ci) {
        mtkLog(TAG, "Enter MtkSimHandler context");
    }

    @Override // com.mediatek.internal.telephony.uicc.IMtkSimHandler
    public void setPhoneId(int phoneId) {
        this.mPhoneId = phoneId;
    }

    @Override // com.mediatek.internal.telephony.uicc.IMtkSimHandler
    public void dispose() {
    }

    /* access modifiers changed from: protected */
    public void mtkLog(String tag, String s) {
        Rlog.d(tag, s + " (slot " + this.mPhoneId + ")");
    }

    /* access modifiers changed from: protected */
    public void mtkLoge(String tag, String s) {
        Rlog.e(tag, s + " (slot " + this.mPhoneId + ")");
    }
}

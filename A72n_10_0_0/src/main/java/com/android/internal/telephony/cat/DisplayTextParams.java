package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;

public class DisplayTextParams extends CommandParams {
    @UnsupportedAppUsage
    public TextMessage mTextMsg;

    @UnsupportedAppUsage
    public DisplayTextParams(CommandDetails cmdDet, TextMessage textMsg) {
        super(cmdDet);
        this.mTextMsg = textMsg;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.CommandParams
    public boolean setIcon(Bitmap icon) {
        TextMessage textMessage;
        if (icon == null || (textMessage = this.mTextMsg) == null) {
            return false;
        }
        textMessage.icon = icon;
        return true;
    }

    @Override // com.android.internal.telephony.cat.CommandParams
    public String toString() {
        return "TextMessage=" + this.mTextMsg + " " + super.toString();
    }
}

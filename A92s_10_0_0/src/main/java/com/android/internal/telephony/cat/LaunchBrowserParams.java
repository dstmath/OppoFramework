package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

public class LaunchBrowserParams extends CommandParams {
    public TextMessage mConfirmMsg;
    public LaunchBrowserMode mMode;
    public String mUrl;

    public LaunchBrowserParams(CommandDetails cmdDet, TextMessage confirmMsg, String url, LaunchBrowserMode mode) {
        super(cmdDet);
        this.mConfirmMsg = confirmMsg;
        this.mMode = mode;
        this.mUrl = url;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.CommandParams
    public boolean setIcon(Bitmap icon) {
        TextMessage textMessage;
        if (icon == null || (textMessage = this.mConfirmMsg) == null) {
            return false;
        }
        textMessage.icon = icon;
        return true;
    }

    @Override // com.android.internal.telephony.cat.CommandParams
    public String toString() {
        return "TextMessage=" + this.mConfirmMsg + " " + super.toString();
    }
}

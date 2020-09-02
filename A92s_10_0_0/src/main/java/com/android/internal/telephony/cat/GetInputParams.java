package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;

public class GetInputParams extends CommandParams {
    Input mInput = null;

    @UnsupportedAppUsage
    public GetInputParams(CommandDetails cmdDet, Input input) {
        super(cmdDet);
        this.mInput = input;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.CommandParams
    public boolean setIcon(Bitmap icon) {
        Input input;
        if (icon == null || (input = this.mInput) == null) {
            return true;
        }
        input.icon = icon;
        return true;
    }
}

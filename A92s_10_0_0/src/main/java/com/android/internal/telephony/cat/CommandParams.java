package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;
import com.android.internal.telephony.cat.AppInterface;

public class CommandParams {
    @UnsupportedAppUsage
    public CommandDetails mCmdDet;
    public boolean mLoadIconFailed = false;

    @UnsupportedAppUsage
    public CommandParams(CommandDetails cmdDet) {
        this.mCmdDet = cmdDet;
    }

    @UnsupportedAppUsage
    public AppInterface.CommandType getCommandType() {
        return AppInterface.CommandType.fromInt(this.mCmdDet.typeOfCommand);
    }

    /* access modifiers changed from: package-private */
    public boolean setIcon(Bitmap icon) {
        return true;
    }

    public String toString() {
        return this.mCmdDet.toString();
    }
}

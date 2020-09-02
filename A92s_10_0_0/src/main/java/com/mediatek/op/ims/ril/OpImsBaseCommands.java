package com.mediatek.op.ims.ril;

import android.content.Context;
import com.mediatek.ims.ril.OpImsCommandsInterface;

public abstract class OpImsBaseCommands implements OpImsCommandsInterface {
    protected Context mContext;
    protected int mPhoneId;

    public OpImsBaseCommands(Context context, int instanceId) {
        this.mContext = context;
        this.mPhoneId = instanceId;
    }
}

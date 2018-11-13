package com.mediatek.common.sms;

import android.content.Context;

public interface IInboundAutoRegSmsFwkExt {
    public static final int TELESERVICE_REG_SMS_CT = 65005;

    boolean handleAutoRegMessage(Context context, int i, byte[] bArr, int i2);
}

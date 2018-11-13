package com.mediatek.common.telephony;

import android.content.Intent;

public interface IOnlyOwnerSimSupport {
    public static final String MTK_NORMALUSER_CB_ACTION = "mediatek.Telephony.NORMALUSER_CB_RECEIVED";
    public static final String MTK_NORMALUSER_MMS_ACTION = "mediatek.Telephony.NORMALUSER_MMS_RECEIVED";
    public static final String MTK_NORMALUSER_SMS_ACTION = "mediatek.Telephony.NORMALUSER_SMS_RECEIVED";

    void dispatchMsgOwner(Intent intent, int i, String str, int i2);

    void intercept(Object obj, int i);

    boolean isCurrentUserOwner();

    boolean isMsgDispatchOwner(Intent intent, String str, int i);

    boolean isNetworkTypeMobile(int i);

    boolean isOnlyOwnerSimSupport();
}

package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoInboundSmsHandler extends IOppoCommonFeature {
    public static final IOppoInboundSmsHandler DEFAULT = new IOppoInboundSmsHandler() {
        /* class com.android.internal.telephony.IOppoInboundSmsHandler.AnonymousClass1 */
    };
    public static final String TAG = "IOppoInboundSmsHandler";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoInboundSmsHandler;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoInboundSmsHandler getDefault() {
        return DEFAULT;
    }

    default void oemInitUIHandler(Context context, Context phoneContext) {
    }

    default void oemSetDefaultSms(Context context) {
    }

    default void oemSetDefaultWappush(Context context) {
    }

    default void oemSetWapPushScAddress(WapPushOverSms wapPush, byte[][] pdus, InboundSmsTracker tracker) {
    }

    default void oemRemoveAbortFlag(Intent intent, UserHandle user) {
    }

    default void oemMtSmsCount(Intent intent) {
    }

    default boolean oemIsProgressing() {
        return false;
    }

    default Uri oemCheckSubIdWhenMtSms(UserManager userManager, Phone phone) {
        return null;
    }

    default boolean oemIsCurrentFormat3gpp2(Context context) {
        return false;
    }

    default boolean oemDealWithCtImsSms(byte[][] pdus, String format, Context context) {
        return false;
    }

    default boolean oemDealWithHealthcheckSms(byte[][] pdus, String format, Context context) {
        return false;
    }
}

package com.oppo.internal.telephony;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.internal.telephony.IOppoSmsDispatchersController;
import com.android.internal.telephony.ImsSmsDispatcher;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.util.ReflectionHelper;
import java.util.ArrayList;

public class OppoSmsDispatchersControllerReference implements IOppoSmsDispatchersController {
    private static final String TAG = "OppoSmsDispatchersControllerReference";
    private SmsDispatchersController mRef;

    public OppoSmsDispatchersControllerReference(SmsDispatchersController ref) {
        this.mRef = ref;
    }

    public void sendTextOem(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm, int encodingType) {
        ImsSmsDispatcher mRef_mImsSmsDispatcher = (ImsSmsDispatcher) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.SmsDispatchersController", "mImsSmsDispatcher");
        if (mRef_mImsSmsDispatcher.isAvailable() || mRef_mImsSmsDispatcher.isEmergencySmsSupport(destAddr)) {
            ReflectionHelper.callMethod(mRef_mImsSmsDispatcher, "com.android.internal.telephony.AbstractSMSDispatcher", "sendTextOem", new Class[]{String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, Boolean.valueOf(persistMessage), -1, false, -1, Boolean.valueOf(isForVvm), Integer.valueOf(encodingType)});
            return;
        }
        boolean mRef_isCdmaMoPublic = ((Boolean) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SmsDispatchersController", "isCdmaMoPublic", new Class[0], new Object[0])).booleanValue();
        OppoRlog.Rlog.d(TAG, "mRef_isCdmaMoPublic=" + mRef_isCdmaMoPublic);
        if (mRef_isCdmaMoPublic) {
            ReflectionHelper.callMethod((CdmaSMSDispatcher) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.SmsDispatchersController", "mCdmaDispatcher"), "com.android.internal.telephony.AbstractSMSDispatcher", "sendTextOem", new Class[]{String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, Boolean.valueOf(persistMessage), Integer.valueOf(priority), Boolean.valueOf(expectMore), Integer.valueOf(validityPeriod), Boolean.valueOf(isForVvm), Integer.valueOf(encodingType)});
            return;
        }
        ReflectionHelper.callMethod((GsmSMSDispatcher) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.SmsDispatchersController", "mGsmDispatcher"), "com.android.internal.telephony.AbstractSMSDispatcher", "sendTextOem", new Class[]{String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, Boolean.valueOf(persistMessage), Integer.valueOf(priority), Boolean.valueOf(expectMore), Integer.valueOf(validityPeriod), Boolean.valueOf(isForVvm), Integer.valueOf(encodingType)});
    }

    public void sendMultipartTextOem(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        ImsSmsDispatcher mRef_mImsSmsDispatcher = (ImsSmsDispatcher) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.SmsDispatchersController", "mImsSmsDispatcher");
        if (mRef_mImsSmsDispatcher.isAvailable()) {
            ReflectionHelper.callMethod(mRef_mImsSmsDispatcher, "com.android.internal.telephony.AbstractSMSDispatcher", "sendMultipartTextOem", new Class[]{String.class, String.class, new ArrayList().getClass(), new ArrayList().getClass(), new ArrayList().getClass(), Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, Boolean.valueOf(persistMessage), -1, false, -1, Integer.valueOf(encodingType)});
            return;
        }
        boolean mRef_isCdmaMoPublic = ((Boolean) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SmsDispatchersController", "isCdmaMoPublic", new Class[0], new Object[0])).booleanValue();
        OppoRlog.Rlog.d(TAG, "mRef_isCdmaMoPublic=" + mRef_isCdmaMoPublic);
        if (mRef_isCdmaMoPublic) {
            ReflectionHelper.callMethod((CdmaSMSDispatcher) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.SmsDispatchersController", "mCdmaDispatcher"), "com.android.internal.telephony.AbstractSMSDispatcher", "sendMultipartTextOem", new Class[]{String.class, String.class, new ArrayList().getClass(), new ArrayList().getClass(), new ArrayList().getClass(), Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, Boolean.valueOf(persistMessage), Integer.valueOf(priority), Boolean.valueOf(expectMore), Integer.valueOf(validityPeriod), Integer.valueOf(encodingType)});
            return;
        }
        ReflectionHelper.callMethod((GsmSMSDispatcher) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.SmsDispatchersController", "mGsmDispatcher"), "com.android.internal.telephony.AbstractSMSDispatcher", "sendMultipartTextOem", new Class[]{String.class, String.class, new ArrayList().getClass(), new ArrayList().getClass(), new ArrayList().getClass(), Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, Boolean.valueOf(persistMessage), Integer.valueOf(priority), Boolean.valueOf(expectMore), Integer.valueOf(validityPeriod), Integer.valueOf(encodingType)});
    }

    public void oemSendData(Context context, String callingPackage) {
        try {
            String pkg = OppoSmsCommonUtils.oemGetPackageNameViaProcessId(context, callingPackage);
            if (!TextUtils.isEmpty(pkg) && pkg.equals(OppoInboundSmsHandlerReference.CT_AUTO_IMS_REG_PACKAGE)) {
                SystemProperties.set("persist.sys.ct_auto_ims", "1");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

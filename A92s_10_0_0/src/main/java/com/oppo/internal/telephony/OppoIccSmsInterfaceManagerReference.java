package com.oppo.internal.telephony;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import com.android.internal.telephony.IOppoIccSmsInterfaceManager;
import com.android.internal.telephony.IccSmsInterfaceManager;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsPermissions;
import com.android.internal.telephony.util.ReflectionHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OppoIccSmsInterfaceManagerReference implements IOppoIccSmsInterfaceManager {
    private static final int CB_ACTIVATION_OFF = 0;
    private static final int CB_ACTIVATION_ON = 1;
    private static final int CB_ACTIVATION_UNKNOWN = -1;
    private static final String LOG_TAG = "OppoIccSmsInterfaceManagerReference";
    private int mCurrentCellBroadcastActivation = -1;
    private IccSmsInterfaceManager mRef;
    private int mSetCellBroadcastActivationNum = 0;

    /* access modifiers changed from: protected */
    public void log(String msg) {
        OppoRlog.Log.d(LOG_TAG, "" + msg);
    }

    public OppoIccSmsInterfaceManagerReference(IccSmsInterfaceManager ref) {
        this.mRef = ref;
    }

    public void sendTextWithOptionsOem(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        SmsPermissions mRef_mSmsPermissions = (SmsPermissions) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mSmsPermissions");
        if (mRef_mSmsPermissions != null) {
            if (!mRef_mSmsPermissions.checkCallingOrSelfCanSendSms(callingPackage, "Sending SMS message")) {
                ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "returnUnspecifiedFailurePublic", new Class[]{PendingIntent.class}, new Object[]{sentIntent});
                return;
            }
        }
        sendTextInternalOem(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, false, encodingType);
    }

    private void sendTextInternalOem(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, boolean isForVvm, int encodingType) {
        if (OppoRlog.Rlog.isLoggable("SMS", 2)) {
            log("sendText: destAddr=" + destAddr + " scAddr=" + scAddr + " text='" + text + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent + " priority=" + priority + " expectMore=" + expectMore + " validityPeriod=" + validityPeriod + " isForVVM=" + isForVvm);
        }
        ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "filterDestAddressPublic", new Class[]{String.class}, new Object[]{destAddr});
        ReflectionHelper.callMethod(this.mRef.mDispatchersController, "com.android.internal.telephony.AbstractSmsDispatchersController", "sendTextOem", new Class[]{String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, text, sentIntent, deliveryIntent, null, callingPackage, Boolean.valueOf(persistMessageForNonDefaultSmsApp), Integer.valueOf(priority), Boolean.valueOf(expectMore), Integer.valueOf(validityPeriod), Boolean.valueOf(isForVvm), Integer.valueOf(encodingType)});
    }

    public void sendMultipartTextWithOptionsOem(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        String singlePart;
        SmsPermissions mRef_mSmsPermissions = (SmsPermissions) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mSmsPermissions");
        if (mRef_mSmsPermissions != null) {
            if (!mRef_mSmsPermissions.checkCallingCanSendText(persistMessageForNonDefaultSmsApp, callingPackage, "Sending SMS message")) {
                ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "returnUnspecifiedFailurePublic", new Class[]{new ArrayList().getClass()}, new Object[]{sentIntents});
                return;
            }
        }
        if (OppoRlog.Rlog.isLoggable("SMS", 2)) {
            int i = 0;
            Iterator<String> it = parts.iterator();
            while (it.hasNext()) {
                log("sendMultipartTextWithOptions: destAddr=" + destAddr + ", srAddr=" + scAddr + ", part[" + i + "]=" + it.next());
                i++;
            }
        }
        ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "filterDestAddressPublic", new Class[]{String.class}, new Object[]{destAddr});
        if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
            ReflectionHelper.callMethod(((IccSmsInterfaceManager) this.mRef).mDispatchersController, "com.android.internal.telephony.AbstractSmsDispatchersController", "sendMultipartTextOem", new Class[]{String.class, String.class, new ArrayList().getClass(), new ArrayList().getClass(), new ArrayList().getClass(), Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, (ArrayList) parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, Boolean.valueOf(persistMessageForNonDefaultSmsApp), Integer.valueOf(priority), Boolean.valueOf(expectMore), Integer.valueOf(validityPeriod), Integer.valueOf(encodingType)});
            return;
        }
        int i2 = 0;
        while (i2 < parts.size()) {
            String singlePart2 = parts.get(i2);
            if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                singlePart = String.valueOf(i2 + 1) + '/' + parts.size() + ' ' + singlePart2;
            } else {
                singlePart = singlePart2.concat(' ' + String.valueOf(i2 + 1) + '/' + parts.size());
            }
            PendingIntent singleSentIntent = null;
            if (sentIntents != null && sentIntents.size() > i2) {
                singleSentIntent = sentIntents.get(i2);
            }
            PendingIntent singleDeliveryIntent = null;
            if (deliveryIntents != null && deliveryIntents.size() > i2) {
                singleDeliveryIntent = deliveryIntents.get(i2);
            }
            ReflectionHelper.callMethod(this.mRef.mDispatchersController, "com.android.internal.telephony.AbstractSmsDispatchersController", "sendTextOem", new Class[]{String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Uri.class, String.class, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE}, new Object[]{destAddr, scAddr, singlePart, singleSentIntent, singleDeliveryIntent, null, callingPackage, Boolean.valueOf(persistMessageForNonDefaultSmsApp), Integer.valueOf(priority), Boolean.valueOf(expectMore), Integer.valueOf(validityPeriod), false, Integer.valueOf(encodingType)});
            i2++;
            mRef_mSmsPermissions = mRef_mSmsPermissions;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:37:? A[RETURN, SYNTHETIC] */
    public boolean oemSetCellBroadcastActivation(boolean activate, int what) {
        Object mRef_mSuccess;
        int newActivationState = activate ? 1 : 0;
        try {
            this.mSetCellBroadcastActivationNum++;
            boolean needSet = false;
            if (this.mSetCellBroadcastActivationNum % 10 == 0) {
                needSet = true;
                this.mSetCellBroadcastActivationNum = 0;
            }
            if (!needSet) {
                if (this.mCurrentCellBroadcastActivation == newActivationState) {
                    ReflectionHelper.setDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mSuccess", true);
                    boolean mRef_mSuccess2 = ((Boolean) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mSuccess")).booleanValue();
                    if (mRef_mSuccess2 && this.mCurrentCellBroadcastActivation != newActivationState) {
                        this.mCurrentCellBroadcastActivation = newActivationState;
                        log("mCurrentCellBroadcastActivation change to " + this.mCurrentCellBroadcastActivation);
                    }
                    log("Calling setCellBroadcastActivation return " + mRef_mSuccess2 + " cur=" + this.mSetCellBroadcastActivationNum);
                    mRef_mSuccess = ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mSuccess");
                    if (mRef_mSuccess == null) {
                        return ((Boolean) mRef_mSuccess).booleanValue();
                    }
                    return false;
                }
            }
            Object mRef_mLock = ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mLock");
            synchronized (mRef_mLock) {
                Message response = ((Handler) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mHandler")).obtainMessage(what);
                ReflectionHelper.setDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mSuccess", false);
                ((Phone) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mPhone")).mCi.setGsmBroadcastActivation(activate, response);
                try {
                    mRef_mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to set cell broadcast activation");
                }
            }
            boolean mRef_mSuccess22 = ((Boolean) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mSuccess")).booleanValue();
            this.mCurrentCellBroadcastActivation = newActivationState;
            log("mCurrentCellBroadcastActivation change to " + this.mCurrentCellBroadcastActivation);
            log("Calling setCellBroadcastActivation return " + mRef_mSuccess22 + " cur=" + this.mSetCellBroadcastActivationNum);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mRef_mSuccess = ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.IccSmsInterfaceManager", "mSuccess");
        if (mRef_mSuccess == null) {
        }
    }
}

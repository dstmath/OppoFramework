package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.AsyncTask;
import android.os.Handler;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.SMSDispatcher;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractSMSDispatcher extends Handler {
    private static String LOG_TAG = "AbstractSMSDispatcher";
    public IOppoSMSDispatcher mReference;

    public abstract void sendMultipartText(String str, String str2, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, Uri uri, String str3, boolean z, int i, boolean z2, int i2);

    public abstract void sendText(String str, String str2, String str3, PendingIntent pendingIntent, PendingIntent pendingIntent2, Uri uri, String str4, boolean z, int i, boolean z2, int i2, boolean z3);

    public AbstractSMSDispatcher(Phone phone) {
        this.mReference = null;
        this.mReference = (IOppoSMSDispatcher) OppoTelephonyFactory.getInstance().getFeature(IOppoSMSDispatcher.DEFAULT, this);
        if (phone != null) {
            LOG_TAG = "AbstractSMSDispatcher[" + phone.getPhoneId() + "]";
        }
        String str = LOG_TAG;
        OppoRlog.Rlog.d(str, "mReference=" + this.mReference);
    }

    public void sendTextOem(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm, int encodingType) {
        IOppoSMSDispatcher iOppoSMSDispatcher = this.mReference;
        if (iOppoSMSDispatcher != null) {
            iOppoSMSDispatcher.sendTextOem(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, isForVvm, encodingType);
            return;
        }
        OppoRlog.Rlog.e(LOG_TAG, "sendTextOem--disable");
        sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, isForVvm);
    }

    public void sendMultipartTextOem(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        IOppoSMSDispatcher iOppoSMSDispatcher = this.mReference;
        if (iOppoSMSDispatcher != null) {
            iOppoSMSDispatcher.sendMultipartTextOem(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType);
            return;
        }
        OppoRlog.Rlog.e(LOG_TAG, "sendMultipartTextOem--disable");
        sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
    }

    /* access modifiers changed from: protected */
    public boolean handleSmsSendControl(SMSDispatcher.SmsTracker smsTracker, Phone phone, Context context, int event) {
        IOppoSMSDispatcher iOppoSMSDispatcher = this.mReference;
        if (iOppoSMSDispatcher == null) {
            return false;
        }
        return iOppoSMSDispatcher.handleSmsSendControl(smsTracker, phone, context, event);
    }

    public boolean oemMoImsErrorCode30(AsyncResult ar, SMSDispatcher.SmsTracker tracker) {
        IOppoSMSDispatcher iOppoSMSDispatcher = this.mReference;
        if (iOppoSMSDispatcher != null) {
            return iOppoSMSDispatcher.oemMoImsErrorCode30(ar, tracker);
        }
        OppoRlog.Rlog.e(LOG_TAG, "oemMoImsErrorCode30--disable");
        return false;
    }

    public void oemMoSmsCount(SMSDispatcher.SmsTracker tracker) {
        IOppoSMSDispatcher iOppoSMSDispatcher = this.mReference;
        if (iOppoSMSDispatcher != null) {
            iOppoSMSDispatcher.oemMoSmsCount(tracker);
        } else {
            OppoRlog.Rlog.e(LOG_TAG, "oemMoSmsCount--disable");
        }
    }

    public static abstract class OemSmsTracker {
        public abstract void persistOrUpdateMessagePublic(Context context, int i, int i2);

        public class OemAsyncPersistOrUpdateTask extends AsyncTask<Void, Void, Void> {
            private String SEND_NEXT_MSG_EXTRA = "SendNextMsg";
            private final Context mContext;
            private int mError;
            private int mErrorCode;
            private boolean mFail;
            private int mMessageType;
            private Uri mMessageUri;
            private PendingIntent mSentIntent;
            private AtomicInteger mUnsentPartCount;

            public OemAsyncPersistOrUpdateTask(Context context, int messageType, int errorCode, int error, boolean fail, Uri mMessageUriDerived, PendingIntent mSentIntentDerived, AtomicInteger mUnsentPartCountDerived, String SEND_NEXT_MSG_EXTRA_DERIVED) {
                this.mContext = context;
                this.mMessageType = messageType;
                this.mErrorCode = errorCode;
                this.mError = error;
                this.mFail = fail;
                this.mMessageUri = mMessageUriDerived;
                this.mSentIntent = mSentIntentDerived;
                this.mUnsentPartCount = mUnsentPartCountDerived;
                this.SEND_NEXT_MSG_EXTRA = SEND_NEXT_MSG_EXTRA_DERIVED;
            }

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                try {
                    OemSmsTracker.this.persistOrUpdateMessagePublic(this.mContext, this.mMessageType, this.mErrorCode);
                    return null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void result) {
                if (this.mSentIntent != null) {
                    try {
                        if (this.mFail) {
                            Intent fillIn = new Intent();
                            if (this.mMessageUri != null) {
                                fillIn.putExtra("uri", this.mMessageUri.toString());
                            }
                            if (this.mErrorCode != 0) {
                                fillIn.putExtra("errorCode", this.mErrorCode);
                            }
                            if (this.mUnsentPartCount != null) {
                                fillIn.putExtra(this.SEND_NEXT_MSG_EXTRA, true);
                            }
                            this.mSentIntent.send(this.mContext, this.mError, fillIn);
                            return;
                        }
                        Intent fillIn2 = new Intent();
                        if (this.mMessageUri != null) {
                            fillIn2.putExtra("uri", this.mMessageUri.toString());
                        }
                        if (this.mUnsentPartCount != null) {
                            fillIn2.putExtra(this.SEND_NEXT_MSG_EXTRA, true);
                        }
                        this.mSentIntent.send(this.mContext, -1, fillIn2);
                    } catch (PendingIntent.CanceledException e) {
                        OppoRlog.Rlog.e(AbstractSMSDispatcher.LOG_TAG, "Failed to send result");
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }
}
